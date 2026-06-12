<template>
  <div>
    <h2>学时管理</h2>

    <!-- ========== 录入学时 ========== -->
    <el-card style="margin:20px 0">
      <template #header>录入学时</template>
      <el-form :model="form" inline>
        <el-form-item label="选择学员">
          <el-select
            v-model="form.studentId"
            filterable
            remote
            reserve-keyword
            :remote-method="searchStudents"
            :loading="searchLoading"
            placeholder="输入姓名或学号搜索"
            @change="onStudentChange"
            style="width:240px"
            clearable
          >
            <el-option
              v-for="s in studentOptions"
              :key="s.studentInfo.id"
              :label="`${s.realName}（学号:${s.studentInfo.id}·${s.phone || '暂无电话'}）`"
              :value="s.studentInfo.id"
            >
              <div style="display:flex;justify-content:space-between;align-items:center">
                <span style="font-weight:500">{{ s.realName }}</span>
                <span style="font-size:12px;color:#909399">
                  学号:{{ s.studentInfo.id }} · {{ s.phone || '-' }}
                </span>
              </div>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="学时(小时)">
          <el-input-number v-model="form.duration" :min="0.5" :step="0.5" />
        </el-form-item>
        <el-form-item label="培训日期">
          <el-date-picker v-model="form.recordDate" type="date" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="练车内容">
          <el-input v-model="form.content" placeholder="练车内容描述" style="width:200px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="recordTraining">提交</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- ========== 选中学员后：信息卡片 + 阶段调整 ========== -->
    <el-row :gutter="20" v-if="form.studentId">
      <!-- 学员基础信息卡片 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <el-icon :size="18" color="#409EFF"><UserFilled /></el-icon>
              <span>学员信息</span>
            </div>
          </template>
          <div v-if="studentSummary" class="student-info-grid">
            <div class="info-item">
              <span class="info-label">姓名</span>
              <span class="info-value">{{ studentSummary.realName }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">学号</span>
              <span class="info-value">{{ studentSummary.studentInfoId }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">电话</span>
              <span class="info-value">{{ studentSummary.phone || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">身份证号</span>
              <span class="info-value">{{ studentSummary.idCard || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">审核状态</span>
              <span class="info-value">
                <el-tag :type="studentSummary.auditStatus === 'APPROVED' ? 'success' : 'warning'" size="small">
                  {{ { PENDING:'待审核', APPROVED:'已通过', REJECTED:'已驳回' }[studentSummary.auditStatus] || studentSummary.auditStatus }}
                </el-tag>
              </span>
            </div>
            <div class="info-item">
              <span class="info-label">累计学时</span>
              <span class="info-value highlight">{{ studentSummary.totalHours }} 小时</span>
            </div>
            <div class="info-item">
              <span class="info-label">培训次数</span>
              <span class="info-value">{{ studentSummary.trainingCount }} 次</span>
            </div>
            <div class="info-item">
              <span class="info-label">当前阶段</span>
              <span class="info-value">
                <el-tag type="warning" size="small">{{ phaseMap[studentSummary.currentPhase] || studentSummary.currentPhase }}</el-tag>
              </span>
            </div>
            <div class="info-item info-full" style="grid-column:1/-1">
              <span class="info-label">各阶段学时</span>
              <span class="info-value">
                科目一: {{ studentSummary.phase1Hours }}h ·
                科目二: {{ studentSummary.phase2Hours }}h ·
                科目三: {{ studentSummary.phase3Hours }}h ·
                科目四: {{ studentSummary.phase4Hours }}h
              </span>
            </div>
          </div>
          <div v-else style="text-align:center;padding:20px;color:#909399">加载中...</div>
        </el-card>
      </el-col>

      <!-- 阶段调整卡片 -->
      <el-col :span="12">
        <el-card>
          <template #header>调整学习阶段</template>
          <el-form inline>
            <el-form-item label="当前阶段:">
              <el-tag type="warning">{{ phaseMap[currentPhase] || currentPhase }}</el-tag>
            </el-form-item>
            <el-form-item label="调整为:">
              <el-select v-model="selectedPhase" placeholder="选择阶段">
                <el-option v-for="(label, key) in phaseMap" :key="key" :label="label" :value="key" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="warning" @click="adjustPhase">确认调整</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>

    <!-- ========== 学时记录列表 ========== -->
    <el-card style="margin-top:20px" v-if="form.studentId">
      <template #header>学时记录</template>
      <el-table :data="records" border>
        <el-table-column prop="recordDate" label="培训日期" width="120" />
        <el-table-column prop="duration" label="学时" width="80" />
        <el-table-column prop="content" label="内容" />
        <el-table-column prop="createdTime" label="记录时间" width="180" />
      </el-table>
    </el-card>
    <p v-if="!form.studentId" style="color:#999; margin-top:20px;">请先选择学员以查看学时记录和详细信息</p>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '../../stores/user'
import { coachApi, commonApi } from '../../api'
import { ElMessage } from 'element-plus'
import { UserFilled } from '@element-plus/icons-vue'

const userStore = useUserStore()
const records = ref([])
const studentOptions = ref([])     // 搜索下拉选项
const searchLoading = ref(false)
const currentPhase = ref('')
const selectedPhase = ref('')
const studentSummary = ref(null)   // 选中学员的详细信息

const phaseMap = { PHASE1: '科目一学习', PHASE2: '科目二训练', PHASE3: '科目三训练', PHASE4: '科目四学习', COMPLETED: '已完成' }

const form = reactive({ studentId: null, duration: 0.5, content: '', recordDate: '' })

onMounted(async () => {
  // 初始加载全部学员列表（空关键词）
  await searchStudents('')
})

/** 远程搜索学员 */
async function searchStudents(keyword) {
  searchLoading.value = true
  try {
    const res = await coachApi().searchStudents(keyword || '')
    studentOptions.value = res.data || []
  } catch (e) {
    studentOptions.value = []
  } finally {
    searchLoading.value = false
  }
}

/** 选中学员后：加载详细信息 + 学时记录 + 当前阶段 */
async function onStudentChange(studentId) {
  if (!studentId) {
    studentSummary.value = null
    records.value = []
    currentPhase.value = ''
    return
  }
  // 并行加载信息
  try {
    const [summaryRes, trainingRes] = await Promise.all([
      coachApi().getStudentSummary(studentId),
      coachApi().getStudentTrainings(studentId)
    ])
    studentSummary.value = summaryRes.data
    currentPhase.value = studentSummary.value?.currentPhase || ''
    records.value = trainingRes.data || []
  } catch (e) {
    studentSummary.value = null
    records.value = []
  }
}

async function recordTraining() {
  if (!form.studentId || !form.duration || !form.content) {
    ElMessage.warning('请填写完整信息')
    return
  }
  try {
    const infoRes = await coachApi().getMyInfo()
    const coachId = infoRes.data?.coach?.id
    const data = {
      studentId: form.studentId,
      coachId: coachId,
      duration: form.duration,
      content: form.content,
      recordDate: form.recordDate || new Date().toISOString().split('T')[0]
    }
    await coachApi().recordTraining(data)
    ElMessage.success('学时记录成功')
    // 刷新学时记录 + 学员信息摘要
    await onStudentChange(form.studentId)
  } catch (e) {}
}

async function adjustPhase() {
  if (!selectedPhase.value) {
    ElMessage.warning('请选择目标阶段')
    return
  }
  try {
    await coachApi().adjustPhase(form.studentId, { newPhase: selectedPhase.value })
    ElMessage.success('阶段调整成功')
    currentPhase.value = selectedPhase.value
    // 刷新学员信息
    const summaryRes = await coachApi().getStudentSummary(form.studentId)
    studentSummary.value = summaryRes.data
    currentPhase.value = studentSummary.value?.currentPhase || ''
  } catch (e) {}
}
</script>

<style scoped>
.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
}
.student-info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}
.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.info-full {
  grid-column: 1 / -1;
}
.info-label {
  font-size: 12px;
  color: #909399;
}
.info-value {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}
.info-value.highlight {
  color: #409EFF;
  font-weight: 700;
  font-size: 16px;
}
</style>
