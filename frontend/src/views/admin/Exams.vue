<template>
  <div>
    <h2>考试管理</h2>
    <el-table :data="exams" border style="margin-top:20px">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="studentName" label="学员姓名" width="100" />
      <el-table-column prop="studentCard" label="身份证号" width="180" />
      <el-table-column label="科目" width="80">
        <template #default="{row}">{{ '科目' + row.subjectId }}</template>
      </el-table-column>
      <el-table-column prop="examDate" label="考试日期" width="120" />
      <el-table-column label="状态" width="100">
        <template #default="{row}">
          <el-tag :type="row.status === 'COMPLETED' ? (row.isPassed ? 'success' : 'danger') : row.status === 'APPROVED' ? 'primary' : 'warning'">
            {{ statusMap[row.status] || row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="score" label="分数" width="80" />
      <el-table-column label="操作" width="200">
        <template #default="{row}">
          <el-button v-if="row.status === 'PENDING'" type="primary" size="small" @click="auditExam(row, 'APPROVED')">通过审核</el-button>
          <el-button v-if="row.status === 'PENDING'" type="danger" size="small" @click="auditExam(row, 'REJECTED')">拒绝</el-button>
          <el-button v-if="row.status === 'APPROVED'" type="success" size="small" @click="scoreDialog(row)">录入成绩</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="showScore" title="录入成绩" width="300px">
      <el-form>
        <el-form-item label="分数"><el-input-number v-model="scoreForm.score" :min="0" :max="100" /></el-form-item>
        <el-form-item label="是否通过">
          <el-radio-group v-model="scoreForm.isPassed">
            <el-radio :value="1">通过</el-radio>
            <el-radio :value="0">不通过</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showScore = false">取消</el-button>
        <el-button type="primary" @click="submitScore">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { adminApi } from '../../api'
import { ElMessage } from 'element-plus'

const exams = ref([])
const showScore = ref(false)
const currentExam = ref(null)
const scoreForm = reactive({ score: 0, isPassed: 1 })
const statusMap = { PENDING: '待审核', APPROVED: '已通过审核', REJECTED: '已拒绝', COMPLETED: '已完成' }

onMounted(async () => {
  try {
    const res = await adminApi().listExams()
    exams.value = res.data || []
  } catch (e) {}
})

async function auditExam(row, status) {
  await adminApi().auditExam(row.id, { status })
  ElMessage.success('操作成功')
  const res = await adminApi().listExams()
  exams.value = res.data || []
}

function scoreDialog(row) {
  currentExam.value = row
  scoreForm.score = 0
  scoreForm.isPassed = 1
  showScore.value = true
}

async function submitScore() {
  await adminApi().recordScore(currentExam.value.id, scoreForm)
  ElMessage.success('成绩录入成功')
  showScore.value = false
  const res = await adminApi().listExams()
  exams.value = res.data || []
}
</script>
