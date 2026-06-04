<template>
  <div>
    <h2>考试管理</h2>
    <el-button type="primary" @click="showDialog = true" style="margin:20px 0">报名考试</el-button>
    <el-table :data="exams" border>
      <el-table-column label="科目">
        <template #default="{row}">{{ '科目' + row.subjectId }}</template>
      </el-table-column>
      <el-table-column prop="examDate" label="考试日期" width="120" />
      <el-table-column label="状态" width="100">
        <template #default="{row}">
          <el-tag :type="row.status === 'COMPLETED' ? (row.isPassed ? 'success' : 'danger') : row.status === 'APPROVED' ? 'primary' : 'warning'">
            {{ examStatusMap[row.status] || row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="score" label="分数" width="80" />
      <el-table-column label="是否通过" width="80">
        <template #default="{row}">{{ row.isPassed === 1 ? '通过' : row.isPassed === 0 ? '未通过' : '-' }}</template>
      </el-table-column>
      <el-table-column prop="retryCount" label="补考次数" width="80" />
    </el-table>

    <el-dialog v-model="showDialog" title="报名考试" width="450px">
      <el-form :model="examForm" label-width="100px">
        <el-form-item label="科目">
          <el-select v-model="examForm.subjectId" placeholder="选择科目">
            <el-option v-for="s in subjects" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="考场">
          <el-select v-model="examForm.examLocationId" placeholder="选择考场">
            <el-option v-for="l in locations" :key="l.id" :label="l.name" :value="l.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="考试日期">
          <el-date-picker v-model="examForm.examDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="submitExam">提交报名</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '../../stores/user'
import { studentApi, commonApi } from '../../api'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const exams = ref([])
const subjects = ref([])
const locations = ref([])
const showDialog = ref(false)

const examForm = reactive({ userId: userStore.userId, subjectId: null, examLocationId: null, examDate: '' })
const examStatusMap = { PENDING: '待审核', APPROVED: '已通过审核', REJECTED: '已拒绝', COMPLETED: '已完成' }

onMounted(async () => {
  try {
    const [examRes, subjRes, locRes] = await Promise.all([
      studentApi().getMyExams(userStore.userId),
      commonApi().listSubjects(),
      commonApi().listExamLocations()
    ])
    exams.value = examRes.data || []
    subjects.value = subjRes.data || []
    locations.value = locRes.data || []
  } catch (e) {}
})

async function submitExam() {
  if (!examForm.subjectId || !examForm.examLocationId || !examForm.examDate) {
    ElMessage.warning('请填写完整信息')
    return
  }
  await studentApi().registerExam(examForm)
  ElMessage.success('考试报名已提交')
  showDialog.value = false
  const res = await studentApi().getMyExams(userStore.userId)
  exams.value = res.data || []
}
</script>
