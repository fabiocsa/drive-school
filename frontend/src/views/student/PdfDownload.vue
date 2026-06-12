<template>
  <div>
    <h2>PDF下载</h2>
    <el-row :gutter="20" style="margin-top:20px">
      <el-col :span="8" v-for="item in pdfList" :key="item.key">
        <el-card>
          <template #header>{{ item.label }}</template>
          <el-button type="primary" @click="downloadPdf(item.key)" :disabled="!pdfStatus[item.key]">下载</el-button>
          <el-button @click="previewPdf(item.key)" :disabled="!pdfStatus[item.key]">预览</el-button>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '../../stores/user'
import { studentApi } from '../../api'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const studentId = ref(null)
const pdfStatus = ref({ registration: false, health: false, examcard: false })
const pdfList = [
  { key: 'registration', label: '报名表' },
  { key: 'health', label: '体检表' },
  { key: 'examcard', label: '准考证' }
]

onMounted(async () => {
  try {
    const infoRes = await studentApi().getMyInfo()
    studentId.value = infoRes.data?.studentInfo?.id
    if (studentId.value) {
      const res = await studentApi().getPdfList(studentId.value)
      pdfStatus.value = res.data || {}
    }
  } catch (e) {}
})

async function downloadPdf(type) {
  try {
    const res = await studentApi().downloadPdf(studentId.value, type)
    const blob = new Blob([res], { type: 'application/pdf' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = type + '.pdf'
    a.click()
    URL.revokeObjectURL(url)
  } catch (e) {
    ElMessage.error('下载失败')
  }
}

async function previewPdf(type) {
  try {
    const res = await studentApi().downloadPdf(studentId.value, type)
    const blob = new Blob([res], { type: 'application/pdf' })
    const url = URL.createObjectURL(blob)
    window.open(url)
  } catch (e) {
    ElMessage.error('预览失败')
  }
}
</script>
