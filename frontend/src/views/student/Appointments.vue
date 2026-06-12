<template>
  <div>
    <h2>约课管理</h2>
    <el-button type="primary" @click="openDialog" style="margin:20px 0">发起约课</el-button>

    <!-- 已有约课列表 -->
    <el-table :data="appointments" border>
      <el-table-column prop="appointmentDate" label="约课日期" width="120" />
      <el-table-column prop="appointmentTime" label="约课时段" width="140" />
      <el-table-column prop="coachName" label="教练" width="100" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{row}">
          <el-tag :type="row.status === 'CONFIRMED' ? 'success' : row.status === 'CANCELLED' ? 'info' : 'warning'">
            {{ statusMap[row.status] || row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="cancelReason" label="取消原因" />
      <el-table-column label="操作" width="100">
        <template #default="{row}">
          <el-button v-if="row.status === 'PENDING' || row.status === 'CONFIRMED'"
            type="danger" size="small" @click="handleCancel(row)">取消</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 发起约课弹窗 -->
    <el-dialog v-model="showDialog" title="发起约课" width="560px" @closed="onDialogClosed">
      <el-form label-width="80px">
        <el-form-item label="选择日期">
          <el-date-picker
            v-model="selectedDate"
            type="date"
            placeholder="请选择约课日期"
            :disabled-date="disabledDateFn"
            :shortcuts="dateShortcuts"
            @change="onDateChange"
            style="width:100%"
          />
        </el-form-item>
      </el-form>

      <!-- 加载中 -->
      <div v-if="loadingSlots" style="text-align:center;padding:30px 0">
        <el-icon class="is-loading" :size="20"><Loading /></el-icon>
        <span style="margin-left:8px;color:#909399">正在加载可用时段...</span>
      </div>

      <!-- 步骤 2：选择上午/下午的时间槽（含容量展示） -->
      <div v-else-if="selectedDate">
        <!-- 上午 -->
        <div class="half-day-section">
          <div class="half-day-header">
            <span class="half-day-label">☀ 上午（08:00 - 12:00）</span>
            <el-tag v-if="morningAvailable" type="success" size="small">可选</el-tag>
            <el-tag v-else type="info" size="small">暂无可用</el-tag>
            <span v-if="morningCapacity > 0" style="font-size:12px;color:#909399">
              （每时段容量 {{ morningCapacity }} 人）
            </span>
          </div>
          <div v-if="morningSlots.length" class="slot-list">
            <div
              v-for="slot in morningSlots"
              :key="slot.time"
              class="slot-card"
              :class="{
                'slot-selected': selectedSlot === slot.time,
                'slot-full': !slot.available
              }"
              @click="selectSlot(slot)"
            >
              <div class="slot-time">{{ slot.time }}</div>
              <div class="slot-capacity" v-if="slot.available">
                剩余 <strong>{{ slot.remaining }}</strong>/{{ slot.capacity }} 席
              </div>
              <div class="slot-capacity full-text" v-else>已满</div>
            </div>
          </div>
          <div v-else class="empty-hint">该时段暂无可约时间</div>
        </div>

        <!-- 下午 -->
        <div class="half-day-section">
          <div class="half-day-header">
            <span class="half-day-label">🌤 下午（13:00 - 17:00）</span>
            <el-tag v-if="afternoonAvailable" type="success" size="small">可选</el-tag>
            <el-tag v-else type="info" size="small">暂无可用</el-tag>
            <span v-if="afternoonCapacity > 0" style="font-size:12px;color:#909399">
              （每时段容量 {{ afternoonCapacity }} 人）
            </span>
          </div>
          <div v-if="afternoonSlots.length" class="slot-list">
            <div
              v-for="slot in afternoonSlots"
              :key="slot.time"
              class="slot-card"
              :class="{
                'slot-selected': selectedSlot === slot.time,
                'slot-full': !slot.available
              }"
              @click="selectSlot(slot)"
            >
              <div class="slot-time">{{ slot.time }}</div>
              <div class="slot-capacity" v-if="slot.available">
                剩余 <strong>{{ slot.remaining }}</strong>/{{ slot.capacity }} 席
              </div>
              <div class="slot-capacity full-text" v-else>已满</div>
            </div>
          </div>
          <div v-else class="empty-hint">该时段暂无可约时间</div>
        </div>

        <!-- 当天完全无可用时段 -->
        <div v-if="!morningAvailable && !afternoonAvailable && hasData" class="no-slot-tip">
          ⚠ 该日期教练暂无可用时段，请选择其他日期
        </div>
      </div>

      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="submitAppointment" :disabled="!selectedSlot">
          确认约课
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '../../stores/user'
import { studentApi } from '../../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const userStore = useUserStore()
const appointments = ref([])
const showDialog = ref(false)

// --- 约课表单状态 ---
const selectedDate = ref(null)
const selectedSlot = ref('')
const morningSlots = ref([])       // 现在是对象数组: [{time, capacity, used, remaining, available}]
const afternoonSlots = ref([])
const morningAvailable = ref(false)
const afternoonAvailable = ref(false)
const morningCapacity = ref(0)
const afternoonCapacity = ref(0)
const loadingSlots = ref(false)
const hasData = ref(false)

const coachId = ref(null)
const studentInfoId = ref(null)
const availableDates = ref([])

const statusMap = { PENDING: '待确认', CONFIRMED: '已确认', CANCELLED: '已取消', COMPLETED: '已完成' }

const dateShortcuts = [
  { text: '明天', value: () => { const d = new Date(); d.setDate(d.getDate() + 1); return d } },
  { text: '后天', value: () => { const d = new Date(); d.setDate(d.getDate() + 2); return d } },
  { text: '一周后', value: () => { const d = new Date(); d.setDate(d.getDate() + 7); return d } },
]

onMounted(async () => {
  try {
    const infoRes = await studentApi().getMyInfo()
    studentInfoId.value = infoRes.data?.studentInfo?.id
    coachId.value = infoRes.data?.studentInfo?.coachId
    const res = await studentApi().getMyAppointments()
    appointments.value = res.data || []
  } catch (e) {}
  await loadAvailableDates()
})

async function loadAvailableDates() {
  if (!coachId.value) return
  try {
    const today = new Date(); const end = new Date(); end.setDate(today.getDate() + 30)
    const res = await studentApi().getCoachAvailableDates(coachId.value,
      today.toISOString().slice(0, 10), end.toISOString().slice(0, 10))
    availableDates.value = res.data || []
  } catch (e) {}
}

function openDialog() {
  if (!coachId.value) { ElMessage.warning('您尚未分配教练，无法发起约课'); return }
  showDialog.value = true
  selectedSlot.value = ''
  morningSlots.value = []; afternoonSlots.value = []
  morningAvailable.value = false; afternoonAvailable.value = false
  morningCapacity.value = 0; afternoonCapacity.value = 0
  hasData.value = false
  const tomorrow = new Date(); tomorrow.setDate(tomorrow.getDate() + 1); tomorrow.setHours(0,0,0,0)
  selectedDate.value = tomorrow
  onDateChange(tomorrow)
}

function onDialogClosed() {
  selectedDate.value = null; selectedSlot.value = ''; loadingSlots.value = false; hasData.value = false
}

function disabledDateFn(date) {
  const today = new Date(); today.setHours(0,0,0,0)
  if (date.getTime() < today.getTime()) return true
  if (!availableDates.value.length) return false
  return !availableDates.value.includes(date.toISOString().slice(0, 10))
}

async function onDateChange(date) {
  if (!date || !coachId.value) return
  selectedSlot.value = ''
  loadingSlots.value = true; hasData.value = false
  try {
    const dateStr = date.toISOString ? date.toISOString().slice(0, 10) : date
    const res = await studentApi().getCoachSlots(coachId.value, dateStr)
    const data = res.data
    morningAvailable.value = data.morning?.available || false
    morningSlots.value = data.morning?.slots || []
    morningCapacity.value = data.morning?.capacity || 0
    afternoonAvailable.value = data.afternoon?.available || false
    afternoonSlots.value = data.afternoon?.slots || []
    afternoonCapacity.value = data.afternoon?.capacity || 0
    hasData.value = true
  } catch (e) {
    morningAvailable.value = false; morningSlots.value = []; morningCapacity.value = 0
    afternoonAvailable.value = false; afternoonSlots.value = []; afternoonCapacity.value = 0
    hasData.value = true
  } finally {
    loadingSlots.value = false
  }
}

/** 选中一个时间槽（不可用的槽不可选） */
function selectSlot(slot) {
  if (!slot.available) return
  selectedSlot.value = slot.time
}

async function submitAppointment() {
  if (!selectedDate.value || !selectedSlot.value) {
    ElMessage.warning('请选择日期和时间段'); return
  }
  try {
    const dateStr = selectedDate.value.toISOString
      ? selectedDate.value.toISOString().slice(0, 10) : selectedDate.value
    await studentApi().createAppointment({
      studentId: studentInfoId.value,
      coachId: coachId.value,
      appointmentTime: selectedSlot.value,
      appointmentDate: dateStr
    })
    ElMessage.success('约课申请已提交')
    showDialog.value = false
    const res = await studentApi().getMyAppointments()
    appointments.value = res.data || []
    await loadAvailableDates()
  } catch (e) {}
}

async function handleCancel(row) {
  try {
    const { value: reason } = await ElMessageBox.prompt('请输入取消原因', '取消约课')
    await studentApi().cancelAppointment(row.id, { reason: reason || '' })
    ElMessage.success('取消成功')
    const res = await studentApi().getMyAppointments()
    appointments.value = res.data || []
    await loadAvailableDates()
  } catch (e) {}
}
</script>

<style scoped>
.half-day-section {
  margin-bottom: 18px;
}
.half-day-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}
.half-day-label {
  font-weight: 600;
  font-size: 15px;
  color: #303133;
}
.slot-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  padding-left: 4px;
}
.slot-card {
  border: 2px solid #dcdfe6;
  border-radius: 10px;
  padding: 12px 18px;
  cursor: pointer;
  transition: all 0.2s;
  min-width: 140px;
  text-align: center;
}
.slot-card:hover:not(.slot-full) {
  border-color: #409eff;
  background: #ecf5ff;
}
.slot-selected {
  border-color: #409eff;
  background: #ecf5ff;
  box-shadow: 0 0 0 2px rgba(64,158,255,0.2);
}
.slot-full {
  background: #f5f5f5;
  cursor: not-allowed;
  opacity: 0.6;
}
.slot-time {
  font-weight: 700;
  font-size: 15px;
  color: #303133;
  margin-bottom: 6px;
}
.slot-capacity {
  font-size: 12px;
  color: #67C23A;
}
.slot-capacity strong {
  color: #409EFF;
}
.slot-capacity.full-text {
  color: #F56C6C;
  font-weight: 600;
}
.empty-hint {
  color: #c0c4cc;
  font-size: 13px;
  padding-left: 4px;
}
.no-slot-tip {
  text-align: center;
  padding: 20px 0;
  color: #e6a23c;
  font-size: 14px;
}
</style>
