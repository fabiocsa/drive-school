<template>
  <div class="page-content fade-in-up">
    <h2>学习首页</h2>
    <p style="color: var(--text-secondary); font-size: 14px; margin-bottom: 24px;">
      欢迎回来，{{ userStore.realName || userStore.username }}
    </p>

    <el-row :gutter="20">
      <!-- 个人信息 -->
      <el-col :span="8">
        <el-card class="info-card">
          <template #header>
            <div class="card-header">
              <el-icon :size="18" color="#409EFF"><UserFilled /></el-icon>
              <span>个人信息</span>
            </div>
          </template>
          <div class="info-row">
            <span class="info-label">姓名</span>
            <span class="info-value">{{ info?.user?.realName || '-' }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">手机</span>
            <span class="info-value">{{ info?.user?.phone || '-' }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">审核</span>
            <el-tag
              :type="info?.studentInfo?.auditStatus === 'APPROVED' ? 'success'
                : info?.studentInfo?.auditStatus === 'REJECTED' ? 'danger' : 'warning'"
              size="small"
            >
              {{ auditStatusMap[info?.studentInfo?.auditStatus] || '未知' }}
            </el-tag>
          </div>
          <div class="info-row">
            <span class="info-label">教练</span>
            <span class="info-value">
              {{ info?.studentInfo?.assignStatus === 'ASSIGNED' ? '已分配' : '待分配' }}
            </span>
          </div>
        </el-card>
      </el-col>

      <!-- 学习进度 -->
      <el-col :span="8">
        <el-card class="info-card">
          <template #header>
            <div class="card-header">
              <el-icon :size="18" color="#67C23A"><DataLine /></el-icon>
              <span>学习进度</span>
            </div>
          </template>
          <div class="phase-badge">
            <el-tag type="primary" size="large">
              {{ phaseMap[info?.learningPhase?.currentPhase] || '未开始' }}
            </el-tag>
          </div>
          <div class="progress-item" v-for="p in phases" :key="p.key">
            <div class="progress-label">
              <span>{{ p.label }}</span>
              <span :style="{ color: p.done ? '#67C23A' : '#909399' }">
                {{ p.done ? '✓ 已完成' : '未完成' }} · {{ p.hours }} 学时
              </span>
            </div>
            <el-progress
              :percentage="p.done ? 100 : Math.min(p.hours / p.required * 100, 99)"
              :color="p.done ? '#67C23A' : '#409EFF'"
              :stroke-width="8"
            />
          </div>
        </el-card>
      </el-col>

      <!-- 教练信息 -->
      <el-col :span="8">
        <el-card class="info-card">
          <template #header>
            <div class="card-header">
              <el-icon :size="18" color="#E6A23C"><Avatar /></el-icon>
              <span>教练信息</span>
            </div>
          </template>
          <div v-if="info?.coachInfo" class="coach-block">
            <el-avatar :size="56" icon="UserFilled" style="margin-bottom:12px" />
            <div class="coach-name">{{ info.coachInfo.name }}</div>
            <div class="coach-phone">{{ info.coachInfo.phone }}</div>
            <el-rate
              :model-value="info.coachInfo.coach?.rating || 0"
              disabled
              show-score
              text-color="#E6A23C"
              style="justify-content:center; margin-top:8px"
            />
          </div>
          <div v-else class="empty-hint">
            <el-icon :size="40" color="#dcdfe6"><UserFilled /></el-icon>
            <p>暂未分配教练</p>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '../../stores/user'
import { studentApi } from '../../api'
import { UserFilled, DataLine, Avatar } from '@element-plus/icons-vue'

const userStore = useUserStore()
const info = ref(null)

const auditStatusMap = { PENDING: '待审核', APPROVED: '已通过', REJECTED: '未通过' }
const phaseMap = { PHASE1: '科目一学习', PHASE2: '科目二训练', PHASE3: '科目三训练', PHASE4: '科目四学习', COMPLETED: '已完成' }

const phases = computed(() => {
  const lp = info.value?.learningPhase
  if (!lp) return []
  return [
    { key: '1', label: '科目一', done: !!lp.phase1Completed, hours: Number(lp.phase1Hours) || 0, required: 12 },
    { key: '2', label: '科目二', done: !!lp.phase2Completed, hours: Number(lp.phase2Hours) || 0, required: 16 },
    { key: '3', label: '科目三', done: !!lp.phase3Completed, hours: Number(lp.phase3Hours) || 0, required: 24 },
    { key: '4', label: '科目四', done: !!lp.phase4Completed, hours: Number(lp.phase4Hours) || 0, required: 10 }
  ]
})

onMounted(async () => {
  try {
    const res = await studentApi().getMyInfo()
    info.value = res.data
  } catch (e) { /* ignore */ }
})
</script>

<style scoped>
.info-card {
  height: 100%;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #f5f5f5;
}
.info-row:last-child { border-bottom: none; }
.info-label { font-size: 13px; color: var(--text-secondary); }
.info-value { font-size: 14px; font-weight: 500; }

.phase-badge { text-align: center; margin-bottom: 16px; }

.progress-item {
  margin-bottom: 14px;
}
.progress-label {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  margin-bottom: 4px;
}

.coach-block {
  text-align: center;
  padding: 8px 0;
}
.coach-name { font-size: 18px; font-weight: 600; }
.coach-phone { font-size: 13px; color: var(--text-secondary); margin-top: 4px; }

.empty-hint {
  text-align: center;
  padding: 20px 0;
  color: #c0c4cc;
}
.empty-hint p { margin-top: 8px; font-size: 14px; }
</style>
