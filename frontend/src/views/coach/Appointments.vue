<template>
  <div>
    <h2>约课管理</h2>

    <!-- ========== 今日约课 ========== -->
    <el-card class="today-card">
      <template #header>
        <div class="section-header">
          <el-icon :size="20" color="#E6A23C"><Clock /></el-icon>
          <span class="section-title">今日约课</span>
          <el-tag type="warning" size="small">{{ todayAppointments.length }} 节</el-tag>
          <span style="margin-left:auto;font-size:13px;color:#909399">{{ todayDate }}</span>
        </div>
      </template>
      <el-table v-if="todayAppointments.length" :data="todayAppointments" border size="small" highlight-current-row>
        <el-table-column prop="appointmentTime" label="时段" width="120" />
        <el-table-column prop="studentName" label="学员" width="100" />
        <el-table-column prop="studentPhone" label="电话" width="130" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{row}">
            <el-tag :type="statusTagType(row.status)" size="small">
              {{ statusMap[row.status] || row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80">
          <template #default="{row}">
            <el-button v-if="row.status === 'PENDING'" type="primary" size="small" @click="confirmAppt(row)">确认</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div v-else class="empty-block">🎉 今日暂无约课</div>
    </el-card>

    <!-- ========== 后续约课（按期日分组、可折叠） ========== -->
    <el-card style="margin-top:20px">
      <template #header>
        <div class="section-header">
          <el-icon :size="20" color="#409EFF"><Calendar /></el-icon>
          <span class="section-title">后续约课</span>
          <el-tag type="primary" size="small">{{ upcomingTotal }} 节</el-tag>
        </div>
      </template>

      <el-collapse v-if="upcomingDates.length" v-model="activeDateGroups" accordion>
        <el-collapse-item v-for="dateGroup in upcomingDates" :key="dateGroup.date" :name="dateGroup.date">
          <template #title>
            <div class="collapse-title">
              <span class="date-text">{{ dateGroup.date }}</span>
              <span class="weekday-text">{{ dateGroup.dayOfWeek }}</span>
              <el-tag size="small" type="primary" effect="plain">{{ dateGroup.count }} 节</el-tag>
              <!-- 余量指示 -->
              <span v-if="dateGroup.availableSlots > 0" style="margin-left:8px;font-size:12px;color:#67C23A">
                剩余 {{ dateGroup.availableSlots }} 个可用时段
              </span>
              <span v-else style="margin-left:8px;font-size:12px;color:#F56C6C">已满</span>
            </div>
          </template>
          <el-table :data="dateGroup.appointments" border size="small">
            <el-table-column prop="appointmentTime" label="时段" width="120" />
            <el-table-column prop="studentName" label="学员" width="100" />
            <el-table-column prop="studentPhone" label="电话" width="130" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{row}">
                <el-tag :type="statusTagType(row.status)" size="small">
                  {{ statusMap[row.status] || row.status }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="80">
              <template #default="{row}">
                <el-button v-if="row.status === 'PENDING'" type="primary" size="small" @click="confirmAppt(row)">确认</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-collapse-item>
      </el-collapse>
      <div v-else class="empty-block">暂无后续约课</div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '../../stores/user'
import { coachApi, studentApi } from '../../api'
import { ElMessage } from 'element-plus'
import { Clock, Calendar } from '@element-plus/icons-vue'

const userStore = useUserStore()
const allAppointments = ref([])
const activeDateGroups = ref('')
const coachId = ref(null)

const statusMap = { PENDING: '待确认', CONFIRMED: '已确认', CANCELLED: '已取消', COMPLETED: '已完成' }

function statusTagType(status) {
  return { CONFIRMED: 'success', CANCELLED: 'info', PENDING: 'warning', COMPLETED: 'success' }[status] || 'info'
}

const todayDate = computed(() => {
  const d = new Date()
  const weekDays = ['周日','周一','周二','周三','周四','周五','周六']
  return d.toISOString().split('T')[0] + ' ' + weekDays[d.getDay()]
})

const todayAppointments = computed(() => {
  const today = new Date().toISOString().split('T')[0]
  return allAppointments.value
    .filter(a => a.appointmentDate === today && a.status !== 'CANCELLED')
    .sort((a, b) => (a.appointmentTime || '').localeCompare(b.appointmentTime || ''))
})

/** 后续约课：按期日分组，每组内按时段排序 */
const upcomingDates = computed(() => {
  const today = new Date().toISOString().split('T')[0]
  const future = allAppointments.value.filter(a => a.appointmentDate !== today && a.status !== 'CANCELLED')

  // 按期日分组
  const groups = {}
  for (const a of future) {
    const d = a.appointmentDate
    if (!groups[d]) groups[d] = []
    groups[d].push(a)
  }

  // 每组按时段排序
  const weekDays = ['周日','周一','周二','周三','周四','周五','周六']
  const sorted = Object.entries(groups)
    .sort(([d1], [d2]) => d1.localeCompare(d2))
    .map(([date, appointments]) => {
      appointments.sort((a, b) => (a.appointmentTime || '').localeCompare(b.appointmentTime || ''))
      const d = new Date(date + 'T00:00:00')
      const dayOfWeek = weekDays[d.getDay()]
      // 可用时段数 = 该日期教练档期总容量 - 已约数量（同一时段允许多人）
      const bookedCount = appointments.filter(a => a.status !== 'CANCELLED').length
      return { date, dayOfWeek, count: bookedCount, availableSlots: Math.max(0, 4 - bookedCount), appointments }
    })

  return sorted
})

const upcomingTotal = computed(() => {
  return upcomingDates.value.reduce((sum, g) => sum + g.count, 0)
})

onMounted(async () => {
  try {
    const infoRes = await coachApi().getMyInfo()
    coachId.value = infoRes.data?.coach?.id
  } catch (e) {}
  await loadAppointments()
})

async function loadAppointments() {
  try {
    const res = await coachApi().getAppointments()
    allAppointments.value = res.data || []
  } catch (e) {}
}

async function confirmAppt(row) {
  await coachApi().confirmAppointment(row.id)
  ElMessage.success('确认成功')
  await loadAppointments()
}
</script>

<style scoped>
.section-header {
  display: flex;
  align-items: center;
  gap: 10px;
}
.section-title {
  font-weight: 600;
  font-size: 16px;
}
.today-card {
  border-left: 4px solid #E6A23C;
}
.today-card :deep(.el-card__header) {
  background: #fef9e7;
}
.empty-block {
  text-align: center;
  padding: 30px 0;
  color: #909399;
  font-size: 14px;
}
.collapse-title {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
}
.date-text {
  font-weight: 600;
  font-size: 15px;
  color: #303133;
}
.weekday-text {
  font-size: 12px;
  color: #909399;
}
</style>
