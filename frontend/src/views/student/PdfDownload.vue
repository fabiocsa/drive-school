<template>
  <div>
    <h2>材料下载</h2>
    <p style="color: var(--text-secondary); font-size: 14px; margin-bottom: 24px;">
      以下文档根据您的报名信息自动生成，可导出 PDF 或直接打印。
      如有数据缺失，系统会提示缺少哪些信息。
    </p>

    <!-- 三份文档卡片 -->
    <el-row :gutter="20">
      <el-col :span="8" v-for="doc in docList" :key="doc.key">
        <el-card class="doc-card" :class="{ 'doc-disabled': !doc.status.available }">
          <template #header>
            <div class="doc-card-header">
              <el-icon :size="22" :color="doc.iconColor"><component :is="doc.icon" /></el-icon>
              <span class="doc-title">{{ doc.label }}</span>
              <el-tag v-if="doc.status.available" type="success" size="small" effect="dark">可生成</el-tag>
              <el-tag v-else type="danger" size="small" effect="dark">暂不可用</el-tag>
            </div>
          </template>

          <!-- 文档描述 -->
          <p class="doc-desc">{{ doc.description }}</p>

          <!-- 缺失数据提示 -->
          <div v-if="!doc.status.available && doc.status.missingFields?.length" class="missing-info">
            <el-icon :size="14" color="#F56C6C"><WarningFilled /></el-icon>
            <div class="missing-list">
              <div v-for="m in doc.status.missingFields" :key="m" class="missing-item">{{ m }}</div>
            </div>
          </div>

          <!-- 可用时显示操作按钮 -->
          <div class="doc-actions" v-if="doc.status.available">
            <el-button type="primary" :icon="Download" @click="exportPdf(doc)" :loading="doc.downloading">
              导出 PDF
            </el-button>
            <el-button :icon="Printer" @click="printPdf(doc)">
              直接打印
            </el-button>
          </div>
          <div class="doc-actions" v-else>
            <el-button type="primary" disabled>导出 PDF</el-button>
            <el-button disabled>直接打印</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 隐藏的 iframe 用于打印 -->
    <iframe v-if="printUrl" :src="printUrl" style="display:none" @load="onPrintFrameLoad" />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '../../stores/user'
import { studentApi } from '../../api'
import { ElMessage } from 'element-plus'
import { Document, Tickets, Postcard, Download, Printer, WarningFilled } from '@element-plus/icons-vue'

const userStore = useUserStore()
const studentId = ref(null)
const studentName = ref('')
const printUrl = ref(null)
const printFrameLoaded = ref(false)

const docList = reactive([
  {
    key: 'registration',
    label: '报名表',
    description: '包含您的个人信息、报考车型、报名日期、分配教练等信息。',
    icon: Document,
    iconColor: '#409EFF',
    downloading: false,
    status: { available: false, missingFields: [] }
  },
  {
    key: 'health',
    label: '体检表',
    description: '包含体检项目结果、体检结论、体检日期。需体检审核通过后才可生成。',
    icon: Postcard,
    iconColor: '#67C23A',
    downloading: false,
    status: { available: false, missingFields: [] }
  },
  {
    key: 'examcard',
    label: '准考证',
    description: '包含考试科目、考试时间、考试地点。需有已排考的考试记录。',
    icon: Tickets,
    iconColor: '#E6A23C',
    downloading: false,
    status: { available: false, missingFields: [] }
  }
])

onMounted(async () => {
  try {
    const infoRes = await studentApi().getMyInfo()
    studentId.value = infoRes.data?.studentInfo?.id
    studentName.value = infoRes.data?.studentInfo?.realName
      || infoRes.data?.user?.realName
      || userStore.realName
      || ''
    if (studentId.value) {
      await checkAllPdf()
    }
  } catch (e) {}
})

/** 检查所有文档的可生成状态 */
async function checkAllPdf() {
  try {
    const res = await studentApi().getPdfList(studentId.value)
    const statuses = res.data || {}
    for (const doc of docList) {
      doc.status = statuses[doc.key] || { available: false, missingFields: ['数据加载失败'] }
    }
  } catch (e) {
    for (const doc of docList) {
      doc.status = { available: false, missingFields: ['检查失败，请刷新重试'] }
    }
  }
}

/** 导出 PDF —— 触发浏览器下载 */
async function exportPdf(doc) {
  doc.downloading = true
  try {
    const filename = buildFilename(doc.key)
    const res = await studentApi().downloadPdf(studentId.value, doc.key, filename)
    const blob = new Blob([res], { type: 'application/pdf' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = filename
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    URL.revokeObjectURL(url)
    ElMessage.success(`${doc.label} 下载成功`)
  } catch (e) {
    ElMessage.error('下载失败，请重试')
  } finally {
    doc.downloading = false
  }
}

/** 直接打印 —— 在隐藏 iframe 中加载 PDF 后调用打印 */
async function printPdf(doc) {
  try {
    const filename = buildFilename(doc.key)
    const res = await studentApi().downloadPdf(studentId.value, doc.key, filename)
    const blob = new Blob([res], { type: 'application/pdf' })
    const url = URL.createObjectURL(blob)
    printUrl.value = url
    printFrameLoaded.value = false
  } catch (e) {
    ElMessage.error('加载打印内容失败')
  }
}

/** iframe 加载完成后触发浏览器打印 */
function onPrintFrameLoad() {
  if (printFrameLoaded.value) return
  printFrameLoaded.value = true
  try {
    const iframe = document.querySelector('iframe')
    if (iframe && iframe.contentWindow) {
      iframe.contentWindow.focus()
      iframe.contentWindow.print()
    }
  } catch (e) {
    ElMessage.warning('无法自动打印，请下载后手动打印')
  }
}

/** 构建规范文件名：张三_报名表.pdf */
function buildFilename(docKey) {
  const name = studentName.value || '学员'
  const labels = { registration: '报名表', health: '体检表', examcard: '准考证' }
  return `${name}_${labels[docKey] || docKey}.pdf`
}
</script>

<style scoped>
.doc-card {
  height: 100%;
  transition: all var(--transition, 0.2s);
}
.doc-card.doc-disabled {
  opacity: 0.65;
}
.doc-card-header {
  display: flex;
  align-items: center;
  gap: 10px;
}
.doc-title {
  font-weight: 600;
  font-size: 16px;
  flex: 1;
}
.doc-desc {
  font-size: 13px;
  color: #606266;
  line-height: 1.6;
  min-height: 42px;
  margin-bottom: 12px;
}
.missing-info {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  background: #fef0f0;
  border-radius: 6px;
  padding: 10px 12px;
  margin-bottom: 12px;
}
.missing-list {
  flex: 1;
}
.missing-item {
  font-size: 12px;
  color: #F56C6C;
  line-height: 1.6;
}
.doc-actions {
  display: flex;
  gap: 10px;
  padding-top: 4px;
}
</style>
