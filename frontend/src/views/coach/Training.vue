<template>
  <div>
    <h2>学时管理</h2>
    <el-card style="margin:20px 0">
      <template #header>录入学时</template>
      <el-form :model="form" inline>
        <el-form-item label="选择学员">
          <el-select v-model="form.studentId" placeholder="请选择学员" @change="onStudentChange">
            <el-option v-for="s in myStudents" :key="s.studentInfo.id" :label="s.realName + ' (ID:' + s.studentInfo.id + ')'" :value="s.studentInfo.id" />
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

    <el-card style="margin:20px 0" v-if="form.studentId">
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

    <el-table :data="records" border>
      <el-table-column prop="recordDate" label="培训日期" width="120" />
      <el-table-column prop="duration" label="学时" width="80" />
      <el-table-column prop="content" label="内容" />
      <el-table-column prop="createdTime" label="记录时间" width="180" />
    </el-table>
    <p v-if="records.length === 0" style="color:#999; margin-top:20px;">选择学员查看学时记录</p>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '../../stores/user'
import { coachApi, commonApi } from '../../api'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const records = ref([])
const myStudents = ref([])
const currentPhase = ref('')
const selectedPhase = ref('')

const phaseMap = { PHASE1: '科目一学习', PHASE2: '科目二训练', PHASE3: '科目三训练', PHASE4: '科目四学习', COMPLETED: '已完成' }

const form = reactive({ studentId: null, duration: 0.5, content: '', recordDate: '' })

onMounted(async () => {
  try {
    const res = await commonApi().getCoachStudents(userStore.userId)
    myStudents.value = res.data || []
  } catch (e) {}
})

function onStudentChange(studentId) {
  loadRecords(studentId)
  // 获取学员当前阶段
  const student = myStudents.value.find(s => s.studentInfo.id === studentId)
  if (student) {
    currentPhase.value = student.studentInfo?.currentPhase || student.learningPhase?.currentPhase || ''
  }
}

async function loadRecords(studentId) {
  try {
    const res = await coachApi().getStudentTrainings(studentId)
    records.value = res.data || []
  } catch (e) {}
}

async function recordTraining() {
  if (!form.studentId || !form.duration || !form.content) {
    ElMessage.warning('请填写完整信息')
    return
  }
  try {
    const infoRes = await coachApi().getMyInfo(userStore.userId)
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
    await loadRecords(form.studentId)
    // 刷新学员列表以更新阶段
    const res = await commonApi().getCoachStudents(userStore.userId)
    myStudents.value = res.data || []
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
    const res = await commonApi().getCoachStudents(userStore.userId)
    myStudents.value = res.data || []
  } catch (e) {}
}
</script>
