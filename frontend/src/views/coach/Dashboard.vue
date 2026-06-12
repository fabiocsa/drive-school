<template>
  <div class="page-content fade-in-up">
    <h2>工作台</h2>
    <p style="color: var(--text-secondary); font-size: 14px; margin-bottom: 24px;">
      欢迎回来，{{ userStore.realName || userStore.username }}
    </p>

    <!-- 统计卡片 -->
    <el-row :gutter="20">
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

    <!-- 个人信息 + 快捷操作 -->
    <el-row :gutter="20" style="margin-top: 28px;">
      <el-col :span="14">
        <el-card>
          <template #header>
            <div class="card-header">
              <el-icon :size="18" color="#67C23A"><UserFilled /></el-icon>
              <span>个人信息</span>
            </div>
          </template>
          <el-descriptions :column="2" border size="default">
            <el-descriptions-item label="姓名">{{ info?.realName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="电话">{{ info?.phone || '-' }}</el-descriptions-item>
            <el-descriptions-item label="教练评分">
              <el-rate :model-value="info?.coach?.rating || 0" disabled text-color="#E6A23C" />
            </el-descriptions-item>
            <el-descriptions-item label="执教车型">{{ info?.coach?.vehicleTypeId || '-' }}</el-descriptions-item>
            <el-descriptions-item label="空闲档期" :span="2">
              <span style="font-size:13px">{{ info?.coach?.scheduleJson || '暂无' }}</span>
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>

      <el-col :span="10">
        <el-card>
          <template #header>
            <div class="card-header">
              <el-icon :size="18" color="#409EFF"><Promotion /></el-icon>
              <span>快捷操作</span>
            </div>
          </template>
          <div class="quick-actions">
            <el-button type="primary" :icon="DataLine" size="large" @click="$router.push('/coach/training')" style="width:100%; margin-bottom:12px">
              学时管理
            </el-button>
            <el-button type="success" :icon="Calendar" size="large" @click="$router.push('/coach/appointments')" style="width:100%">
              约课管理
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useUserStore } from '../../stores/user'
import { coachApi, commonApi } from '../../api'
import { UserFilled, DataLine, Calendar, Promotion, User, Clock, Tickets } from '@element-plus/icons-vue'

const userStore = useUserStore()
const info = ref(null)

const statCards = reactive([
  { key: 'students', label: '学员数量', value: 0, color: '#409EFF', bg: '#ecf5ff', icon: User },
  { key: 'pending', label: '待确认约课', value: 0, color: '#E6A23C', bg: '#fdf6ec', icon: Clock },
  { key: 'today', label: '今日约课', value: 0, color: '#67C23A', bg: '#f0f9eb', icon: Tickets },
  { key: 'hours', label: '总学时', value: 0, color: '#909399', bg: '#f4f4f5', icon: DataLine }
])

onMounted(async () => {
  try {
    const res = await coachApi().getMyInfo()
    info.value = res.data

    const [studentsRes, appointmentsRes] = await Promise.all([
      commonApi().getCoachStudents(),
      coachApi().getAppointments()
    ])
    const students = studentsRes.data || []
    const appointments = appointmentsRes.data || []
    const today = new Date().toISOString().split('T')[0]

    statCards[0].value = students.length
    statCards[1].value = appointments.filter(a => a.status === 'PENDING').length
    statCards[2].value = appointments.filter(a => a.appointmentDate === today && a.status !== 'CANCELLED').length
    statCards[3].value = students.reduce((sum, s) => {
      const lp = s.learningPhase
      if (!lp) return sum
      return sum + (Number(lp.phase1Hours) || 0) + (Number(lp.phase2Hours) || 0)
        + (Number(lp.phase3Hours) || 0) + (Number(lp.phase4Hours) || 0)
    }, 0)
  } catch (e) { /* ignore */ }
})
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

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
}

.quick-actions {
  padding: 8px 0;
}
</style>
