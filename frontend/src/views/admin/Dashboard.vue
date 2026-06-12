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

    <!-- 快捷操作 -->
    <el-row :gutter="20" style="margin-top: 28px;">
      <el-col :span="24">
        <el-card>
          <template #header>
            <span style="font-weight:600">快捷操作</span>
          </template>
          <el-space wrap :size="12">
            <el-button type="primary" :icon="UserFilled" @click="$router.push('/admin/students')">
              学员管理
            </el-button>
            <el-button type="success" :icon="Avatar" @click="$router.push('/admin/coaches')">
              教练管理
            </el-button>
            <el-button type="warning" :icon="Tickets" @click="$router.push('/admin/exams')">
              考试管理
            </el-button>
            <el-button :icon="PieChart" @click="$router.push('/admin/statistics')">
              统计分析
            </el-button>
            <el-button type="danger" :icon="Medal" @click="$router.push('/admin/cert')">
              发证管理
            </el-button>
            <el-button :icon="Setting" @click="$router.push('/admin/basic')">
              基础信息
            </el-button>
          </el-space>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { reactive, onMounted } from 'vue'
import { useUserStore } from '../../stores/user'
import { adminApi } from '../../api'
import { UserFilled, Avatar, Tickets, PieChart, Medal, Setting, User, DataLine, Document } from '@element-plus/icons-vue'

const userStore = useUserStore()

const statCards = reactive([
  { key: 'students', label: '学员总数', value: 0, color: '#409EFF', bg: '#ecf5ff', icon: User },
  { key: 'coaches', label: '教练总数', value: 0, color: '#67C23A', bg: '#f0f9eb', icon: Avatar },
  { key: 'pending', label: '待审核报名', value: 0, color: '#E6A23C', bg: '#fdf6ec', icon: Document },
  { key: 'cert', label: '待发证', value: 0, color: '#F56C6C', bg: '#fef0f0', icon: Medal }
])

onMounted(async () => {
  try {
    const [students, coaches, certs] = await Promise.all([
      adminApi().listStudents(),
      adminApi().listCoaches(),
      adminApi().waitingCertStudents()
    ])
    const studentList = students.data || []
    statCards[0].value = studentList.length
    statCards[1].value = (coaches.data || []).length
    statCards[2].value = studentList.filter(s => s.auditStatus === 'PENDING').length
    statCards[3].value = (certs.data || []).length
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
</style>
