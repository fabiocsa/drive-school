<template>
  <div>
    <h2>学时管理</h2>
    <el-card style="margin:20px 0">
      <template #header>录入学时</template>
      <el-form :model="form" inline>
        <el-form-item label="学员ID">
          <el-input-number v-model="form.studentId" :min="1" />
        </el-form-item>
        <el-form-item label="学时(小时)">
          <el-input-number v-model="form.duration" :min="0.5" :step="0.5" />
        </el-form-item>
        <el-form-item label="培训日期">
          <el-date-picker v-model="form.recordDate" type="date" />
        </el-form-item>
        <el-form-item label="练车内容">
          <el-input v-model="form.content" placeholder="练车内容描述" style="width:200px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="recordTraining">提交</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    <el-table :data="records" border>
      <el-table-column prop="studentId" label="学员ID" width="100" />
      <el-table-column prop="recordDate" label="培训日期" width="120" />
      <el-table-column prop="duration" label="学时" width="80" />
      <el-table-column prop="content" label="内容" />
      <el-table-column prop="createdTime" label="记录时间" width="180" />
    </el-table>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useUserStore } from '../../stores/user'
import { coachApi } from '../../api'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const records = ref([])

const form = reactive({ studentId: null, duration: 0, content: '', recordDate: '', coachId: userStore.userId })

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
    const res = await coachApi().getStudentTrainings(form.studentId)
    records.value = res.data || []
  } catch (e) {}
}
</script>
