<template>
  <div class="page-content fade-in-up">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>考试管理</h2>
      <p class="subtitle">审核考试申请，录入考试成绩</p>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="16">
      <el-col :span="6" v-for="card in statCards" :key="card.key">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-inner">
            <div>
              <div class="stat-label">{{ card.label }}</div>
              <div class="stat-value" :style="{ color: card.color }">{{ card.value }}</div>
            </div>
            <div class="stat-icon" :style="{ background: card.bg }">
              <el-icon :size="24" :color="card.color"><component :is="card.icon" /></el-icon>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 筛选工具栏 -->
    <div class="filter-toolbar">
      <el-form inline>
        <el-form-item label="考试状态">
          <el-select v-model="filterStatus" placeholder="全部状态" clearable style="width: 140px;">
            <el-option label="待审核" value="PENDING" />
            <el-option label="已通过审核" value="APPROVED" />
            <el-option label="已拒绝" value="REJECTED" />
            <el-option label="已完成" value="COMPLETED" />
          </el-select>
        </el-form-item>
        <el-form-item label="考试日期">
          <el-date-picker
            v-model="filterDateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 260px;"
          />
        </el-form-item>
        <el-form-item>
          <el-button :icon="Refresh" @click="resetFilters">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 考试表格 -->
    <el-table
      :data="filteredExams" stripe highlight-current-row
      max-height="500" v-loading="loading"
      element-loading-text="加载考试数据..."
    >
      <el-table-column prop="id" label="ID" width="70" align="center" />
      <el-table-column prop="studentName" label="学员姓名" width="100" show-overflow-tooltip />
      <el-table-column prop="studentCard" label="身份证号" width="190" show-overflow-tooltip />
      <el-table-column label="科目" width="90" align="center">
        <template #default="{ row }">
          <el-tag size="small" :type="subjectTagType(row.subjectId)" effect="plain">
            科目{{ row.subjectId }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="examDate" label="考试日期" width="120" align="center" />
      <el-table-column label="状态" width="110" align="center">
        <template #default="{ row }">
          <el-tag
            size="small"
            effect="plain"
            :type="statusTagType(row)"
          >
            {{ statusMap[row.status] || row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="分数" width="90" align="center">
        <template #default="{ row }">
          <span v-if="row.status === 'COMPLETED'" :class="scoreClass(row.score)">
            {{ row.score }}分
          </span>
          <span v-else style="color: var(--text-secondary);">—</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="140" align="center" fixed="right">
        <template #default="{ row }">
          <el-space :size="4">
            <el-tooltip
              v-if="row.status === 'PENDING'"
              content="通过审核"
              placement="top"
            >
              <el-button
                type="success"
                :icon="Check"
                circle
                size="small"
                @click="auditExam(row, 'APPROVED')"
              />
            </el-tooltip>
            <el-tooltip
              v-if="row.status === 'PENDING'"
              content="拒绝"
              placement="top"
            >
              <el-button
                type="danger"
                :icon="Close"
                circle
                size="small"
                @click="auditExam(row, 'REJECTED')"
              />
            </el-tooltip>
            <el-tooltip
              v-if="row.status === 'APPROVED'"
              content="录入成绩"
              placement="top"
            >
              <el-button
                type="warning"
                :icon="Edit"
                circle
                size="small"
                @click="scoreDialog(row)"
              />
            </el-tooltip>
          </el-space>
        </template>
      </el-table-column>
    </el-table>

    <!-- 空状态 -->
    <div v-if="!loading && filteredExams.length === 0" class="empty-state-wrapper">
      <el-empty description="暂无考试数据" :image-size="140" />
    </div>

    <!-- 录入成绩对话框 -->
    <el-dialog
      v-model="showScore"
      title="录入成绩"
      width="360px"
      :close-on-click-modal="false"
      top="12vh"
    >
      <el-form label-width="80px">
        <el-form-item label="分数">
          <el-input-number v-model="scoreForm.score" :min="0" :max="100" />
        </el-form-item>
        <el-form-item label="是否通过">
          <el-radio-group v-model="scoreForm.isPassed">
            <el-radio :value="1">通过</el-radio>
            <el-radio :value="0">不通过</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <div style="display: flex; justify-content: flex-end; gap: 8px;">
          <el-button @click="showScore = false">取消</el-button>
          <el-button type="primary" @click="submitScore">确认</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { adminApi } from '../../api'
import { ElMessage } from 'element-plus'
import { Tickets, Clock, CircleCheck, DataLine, Refresh, Check, Close, Edit } from '@element-plus/icons-vue'

const exams = ref([])
const loading = ref(false)
const showScore = ref(false)
const currentExam = ref(null)
const scoreForm = reactive({ score: 0, isPassed: 1 })
const statusMap = { PENDING: '待审核', APPROVED: '已通过审核', REJECTED: '已拒绝', COMPLETED: '已完成' }

// 筛选
const filterStatus = ref('')
const filterDateRange = ref([])

// 筛选后的考试列表
const filteredExams = computed(() => {
  let result = exams.value
  if (filterStatus.value) {
    result = result.filter(e => e.status === filterStatus.value)
  }
  if (filterDateRange.value && filterDateRange.value.length === 2) {
    const [start, end] = filterDateRange.value
    result = result.filter(e => e.examDate >= start && e.examDate <= end)
  }
  return result
})

// 统计指标
const pendingCount = computed(() => exams.value.filter(e => e.status === 'PENDING').length)
const approvedCount = computed(() => exams.value.filter(e => e.status === 'APPROVED').length)
const passRate = computed(() => {
  const completed = exams.value.filter(e => e.status === 'COMPLETED')
  if (!completed.length) return '0%'
  const passed = completed.filter(e => e.isPassed).length
  return Math.round((passed / completed.length) * 100) + '%'
})

const statCards = computed(() => [
  { key: 'total', label: '考试总数', value: exams.value.length, color: '#409EFF', bg: '#ecf5ff', icon: Tickets },
  { key: 'pending', label: '待审核', value: pendingCount.value, color: '#E6A23C', bg: '#fdf6ec', icon: Clock },
  { key: 'approved', label: '已通过审核', value: approvedCount.value, color: '#409EFF', bg: '#ecf5ff', icon: CircleCheck },
  { key: 'rate', label: '通过率', value: passRate.value, color: '#67C23A', bg: '#f0f9eb', icon: DataLine }
])

// 科目对应标签颜色
function subjectTagType(subjectId) {
  const map = { 1: 'info', 2: 'success', 3: 'warning', 4: 'primary' }
  return map[subjectId] || 'info'
}

// 状态对应标签颜色
function statusTagType(row) {
  if (row.status === 'COMPLETED') return row.isPassed ? 'success' : 'danger'
  if (row.status === 'APPROVED') return 'primary'
  if (row.status === 'REJECTED') return 'danger'
  return 'warning'
}

// 分数颜色
function scoreClass(score) {
  if (score >= 90) return 'score-excellent'
  if (score >= 60) return 'score-pass'
  return 'score-fail'
}

// 重置筛选
function resetFilters() {
  filterStatus.value = ''
  filterDateRange.value = []
}

onMounted(async () => {
  loading.value = true
  try {
    const res = await adminApi().listExams()
    exams.value = res.data || []
  } catch (e) {
    // API interceptor already shows error
  } finally {
    loading.value = false
  }
})

async function auditExam(row, status) {
  loading.value = true
  try {
    await adminApi().auditExam(row.id, { status })
    ElMessage.success('操作成功')
    const res = await adminApi().listExams()
    exams.value = res.data || []
  } catch (e) {
    // API interceptor already shows error
  } finally {
    loading.value = false
  }
}

function scoreDialog(row) {
  currentExam.value = row
  scoreForm.score = 0
  scoreForm.isPassed = 1
  showScore.value = true
}

async function submitScore() {
  loading.value = true
  try {
    await adminApi().recordScore(currentExam.value.id, scoreForm)
    ElMessage.success('成绩录入成功')
    showScore.value = false
    const res = await adminApi().listExams()
    exams.value = res.data || []
  } catch (e) {
    // API interceptor already shows error
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.stat-card :deep(.el-card__body) {
  padding: 20px 24px;
}
.stat-inner {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.stat-label {
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 8px;
}
.stat-value {
  font-size: 32px;
  font-weight: 700;
  line-height: 1.1;
}
.stat-icon {
  width: 52px;
  height: 52px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

/* 分数颜色标识 */
.score-excellent {
  color: #67C23A;
  font-weight: 600;
}
.score-pass {
  color: #E6A23C;
  font-weight: 600;
}
.score-fail {
  color: #F56C6C;
  font-weight: 600;
}
</style>
