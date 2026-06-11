<template>
  <div>
    <h2>教练首页</h2>
    <el-row :gutter="20" style="margin-top:20px">
      <el-col :span="6">
        <el-card>
          <template #header>学员数量</template>
          <div style="font-size:32px;text-align:center;color:#409EFF">{{ stats.studentCount }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <template #header>待确认约课</template>
          <div style="font-size:32px;text-align:center;color:#E6A23C">{{ stats.pendingAppointments }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <template #header>今日约课</template>
          <div style="font-size:32px;text-align:center;color:#67C23A">{{ stats.todayAppointments }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <template #header>总学时</template>
          <div style="font-size:32px;text-align:center;color:#909399">{{ stats.totalHours }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card style="margin-top:20px">
      <template #header>个人信息</template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="姓名">{{ info?.realName }}</el-descriptions-item>
        <el-descriptions-item label="电话">{{ info?.phone }}</el-descriptions-item>
        <el-descriptions-item label="评分">{{ info?.coach?.rating }} 分</el-descriptions-item>
        <el-descriptions-item label="执教车型">{{ info?.coach?.vehicleTypeId }}</el-descriptions-item>
        <el-descriptions-item label="空闲档期" :span="2">{{ info?.coach?.scheduleJson }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card style="margin-top:20px">
      <template #header>快捷操作</template>
      <el-space>
        <el-button type="primary" @click="$router.push('/coach/training')">学时管理</el-button>
        <el-button type="success" @click="$router.push('/coach/appointments')">约课管理</el-button>
      </el-space>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '../../stores/user'
import { coachApi, commonApi } from '../../api'

const userStore = useUserStore()
const info = ref(null)
const stats = reactive({ studentCount: 0, pendingAppointments: 0, todayAppointments: 0, totalHours: 0 })

onMounted(async () => {
  try {
    const res = await coachApi().getMyInfo(userStore.userId)
    info.value = res.data

    const [studentsRes, appointmentsRes] = await Promise.all([
      commonApi().getCoachStudents(userStore.userId),
      coachApi().getAppointments(userStore.userId)
    ])
    const students = studentsRes.data || []
    const appointments = appointmentsRes.data || []
    const today = new Date().toISOString().split('T')[0]

    stats.studentCount = students.length
    stats.pendingAppointments = appointments.filter(a => a.status === 'PENDING').length
    stats.todayAppointments = appointments.filter(a => a.appointmentDate === today && a.status !== 'CANCELLED').length
    stats.totalHours = students.reduce((sum, s) => {
      const lp = s.learningPhase
      if (!lp) return sum
      return sum + (Number(lp.phase1Hours) || 0) + (Number(lp.phase2Hours) || 0) + (Number(lp.phase3Hours) || 0) + (Number(lp.phase4Hours) || 0)
    }, 0)
  } catch (e) {}
})
</script>
