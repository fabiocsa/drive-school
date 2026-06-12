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
              <span style="font-size:13px;white-space:pre-wrap">{{ scheduleSummary }}</span>
            </el-descriptions-item>
          </el-descriptions>
          <el-button type="primary" style="margin-top:12px" @click="openScheduleEditor">编辑空闲档期</el-button>
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

    <!-- 档期编辑弹窗 -->
    <el-dialog v-model="showScheduleDialog" title="编辑空闲档期" width="640px" @closed="loadScheduleData">
      <el-alert type="info" :closable="false" style="margin-bottom:16px" show-icon>
        <template #title>
          为每个时段设置<strong>容量</strong>（该时段最多可约人数）。取消勾选可删除该时段。
          <br/>已有预约的时段若减少容量至小于已有预约数，将提示无法保存。
        </template>
      </el-alert>

      <el-table :data="scheduleItems" border size="small">
        <el-table-column label="启用" width="60" align="center">
          <template #default="{row}">
            <el-checkbox v-model="row.enabled" />
          </template>
        </el-table-column>
        <el-table-column prop="dayLabel" label="时段" width="140" />
        <el-table-column label="容量（最大可约人数）">
          <template #default="{row}">
            <el-input-number v-model="row.capacity" :min="1" :max="20" :step="1" size="small"
              :disabled="!row.enabled" style="width:120px" />
          </template>
        </el-table-column>
      </el-table>

      <template #footer>
        <el-button @click="showScheduleDialog = false">取消</el-button>
        <el-button type="primary" @click="saveSchedule">保存档期</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, computed } from 'vue'
import { useUserStore } from '../../stores/user'
import { coachApi, commonApi } from '../../api'
import { ElMessage } from 'element-plus'
import { UserFilled, DataLine, Calendar, Promotion, User, Clock, Tickets } from '@element-plus/icons-vue'

const userStore = useUserStore()
const info = ref(null)
const showScheduleDialog = ref(false)
const scheduleItems = ref([])       // 弹窗中的编辑数据

// 所有固定时段（14个：周一到周日 × 上午/下午）
const ALL_SLOTS = ['周一','周二','周三','周四','周五','周六','周日'].flatMap(day =>
  [{ dayTime: day + '上午', dayLabel: day + ' 上午（08:00-12:00）' },
   { dayTime: day + '下午', dayLabel: day + ' 下午（13:00-17:00）' }]
)

const statCards = reactive([
  { key: 'students', label: '学员数量', value: 0, color: '#409EFF', bg: '#ecf5ff', icon: User },
  { key: 'pending', label: '待确认约课', value: 0, color: '#E6A23C', bg: '#fdf6ec', icon: Clock },
  { key: 'today', label: '今日约课', value: 0, color: '#67C23A', bg: '#f0f9eb', icon: Tickets },
  { key: 'hours', label: '总学时', value: 0, color: '#909399', bg: '#f4f4f5', icon: DataLine }
])

const scheduleSummary = computed(() => {
  if (!info.value?.coach?.scheduleJson) return '暂无'
  try { return formatScheduleSummary(info.value.coach.scheduleJson) } catch { return info.value.coach.scheduleJson }
})

onMounted(async () => {
  await loadInfo()
})

async function loadInfo() {
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
}

/** 把 schedule_json 转成可读摘要 */
function formatScheduleSummary(json) {
  const slots = parseScheduleJson(json)
  if (slots.length === 0) return '暂无'
  return slots.map(s => `${s.dayTime}（容量${s.capacity}）`).join('\n')
}

/** 解析 schedule_json → [{dayTime, capacity}] */
function parseScheduleJson(json) {
  if (!json || json === '[]') return []
  try {
    if (json.includes('dayTime')) {
      return JSON.parse(json)
    } else {
      // 旧格式: ["周一上午","周一下午"]
      return JSON.parse(json).map(dt => ({ dayTime: dt, capacity: 5 }))
    }
  } catch {
    return []
  }
}

/** 打开档期编辑器，预填现有数据 */
function openScheduleEditor() {
  const existing = parseScheduleJson(info.value?.coach?.scheduleJson || '[]')
  const existingMap = Object.fromEntries(existing.map(s => [s.dayTime, s.capacity]))

  scheduleItems.value = ALL_SLOTS.map(slot => ({
    dayTime: slot.dayTime,
    dayLabel: slot.dayLabel,
    enabled: slot.dayTime in existingMap,
    capacity: existingMap[slot.dayTime] || 5
  }))
  showScheduleDialog.value = true
}

/** 保存档期 */
async function saveSchedule() {
  const enabled = scheduleItems.value.filter(s => s.enabled)
  if (enabled.length === 0) {
    ElMessage.warning('请至少选择一个时段')
    return
  }
  const json = JSON.stringify(enabled.map(s => ({ dayTime: s.dayTime, capacity: s.capacity })))
  try {
    await coachApi().updateSchedule(json)
    ElMessage.success('档期保存成功')
    showScheduleDialog.value = false
    await loadInfo()
  } catch (e) { /* 错误已 toast */ }
}

/** 弹窗关闭后刷新数据 */
async function loadScheduleData() {
  await loadInfo()
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
