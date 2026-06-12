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
    <el-dialog v-model="showDialog" title="发起约课" width="520px" @closed="onDialogClosed">
      <!-- 步骤 1：选择日期 -->
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

      <!-- 步骤 2：选择上午/下午的时间槽 -->
      <div v-else-if="selectedDate">
        <!-- 上午 -->
        <div class="half-day-section">
          <div class="half-day-header">
            <span class="half-day-label">☀ 上午（08:00 - 12:00）</span>
            <el-tag v-if="morningAvailable" type="success" size="small">可选</el-tag>
            <el-tag v-else type="info" size="small">暂无可用</el-tag>
          </div>
          <div v-if="morningAvailable" class="slot-list">
            <el-button
              v-for="slot in morningSlots"
              :key="slot"
              :type="selectedSlot === slot ? 'primary' : 'default'"
              @click="selectSlot(slot)"
              class="slot-btn"
            >{{ slot }}</el-button>
          </div>
          <div v-else class="empty-hint">
            {{ morningSlots.length === 0 && hasData ? '该时段暂无可约时间' : '' }}
          </div>
        </div>

        <!-- 下午 -->
        <div class="half-day-section">
          <div class="half-day-header">
            <span class="half-day-label">🌤 下午（13:00 - 17:00）</span>
            <el-tag v-if="afternoonAvailable" type="success" size="small">可选</el-tag>
            <el-tag v-else type="info" size="small">暂无可用</el-tag>
          </div>
          <div v-if="afternoonAvailable" class="slot-list">
            <el-button
              v-for="slot in afternoonSlots"
              :key="slot"
              :type="selectedSlot === slot ? 'primary' : 'default'"
              @click="selectSlot(slot)"
              class="slot-btn"
            >{{ slot }}</el-button>
          </div>
          <div v-else class="empty-hint">
            {{ afternoonSlots.length === 0 && hasData ? '该时段暂无可约时间' : '' }}
          </div>
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
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '../../stores/user'
import { studentApi } from '../../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const userStore = useUserStore()
const appointments = ref([])
const showDialog = ref(false)

// --- 约课表单状态 ---
const selectedDate = ref(null)
const selectedSlot = ref('')
const morningSlots = ref([])
const afternoonSlots = ref([])
const morningAvailable = ref(false)
const afternoonAvailable = ref(false)
const loadingSlots = ref(false)
const hasData = ref(false)        // 是否已完成数据加载（区分"加载中"与"无数据"）

const coachId = ref(null)
const studentInfoId = ref(null)
const availableDates = ref([])    // 有可用槽位的日期列表（用于 date-picker 高亮）

const statusMap = { PENDING: '待确认', CONFIRMED: '已确认', CANCELLED: '已取消', COMPLETED: '已完成' }

// 日期快捷选项
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

  // 预加载未来 30 天的可用日期（用于日期选择器禁用不可用日期）
  await loadAvailableDates()
})

/** 预加载未来 N 天内哪些日期有可用槽位 */
async function loadAvailableDates() {
  if (!coachId.value) return
  try {
    const today = new Date()
    const end = new Date()
    end.setDate(today.getDate() + 30)
    const startStr = today.toISOString().slice(0, 10)
    const endStr = end.toISOString().slice(0, 10)
    const res = await studentApi().getCoachAvailableDates(coachId.value, startStr, endStr)
    availableDates.value = res.data || []
  } catch (e) {}
}

/** 打开弹窗时重置状态，并预填明天的日期 */
function openDialog() {
  // 边界检查：未分配教练时不允许约课
  if (!coachId.value) {
    ElMessage.warning('您尚未分配教练，无法发起约课')
    return
  }
  showDialog.value = true

  // 重置状态
  selectedSlot.value = ''
  morningSlots.value = []
  afternoonSlots.value = []
  morningAvailable.value = false
  afternoonAvailable.value = false
  hasData.value = false

  // 自动选中明天，触发可用时段加载
  const tomorrow = new Date()
  tomorrow.setDate(tomorrow.getDate() + 1)
  tomorrow.setHours(0, 0, 0, 0)
  selectedDate.value = tomorrow
  onDateChange(tomorrow)
}

/** 弹窗关闭时清理 */
function onDialogClosed() {
  selectedDate.value = null
  selectedSlot.value = ''
  loadingSlots.value = false
  hasData.value = false
}

/** 日期选择器的禁用规则：禁用过去 + 无可用槽位的日期 */
function disabledDateFn(date) {
  // 禁用今天之前
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  if (date.getTime() < today.getTime()) return true
  // 如果没有预加载可用日期列表，则不禁用（但后端会校验）
  if (!availableDates.value.length) return false
  // 转换为 YYYY-MM-DD 比较
  const dateStr = date.toISOString().slice(0, 10)
  return !availableDates.value.includes(dateStr)
}

/** 选择日期后，请求该日期的可用时间槽 */
async function onDateChange(date) {
  if (!date || !coachId.value) return
  selectedSlot.value = ''
  loadingSlots.value = true
  hasData.value = false
  try {
    const dateStr = date.toISOString ? date.toISOString().slice(0, 10) : date
    const res = await studentApi().getCoachSlots(coachId.value, dateStr)
    const data = res.data
    morningAvailable.value = data.morning?.available || false
    morningSlots.value = data.morning?.slots || []
    afternoonAvailable.value = data.afternoon?.available || false
    afternoonSlots.value = data.afternoon?.slots || []
    hasData.value = true
  } catch (e) {
    morningAvailable.value = false
    morningSlots.value = []
    afternoonAvailable.value = false
    afternoonSlots.value = []
    hasData.value = true
  } finally {
    loadingSlots.value = false
  }
}

/** 选中一个时间槽 */
function selectSlot(slot) {
  selectedSlot.value = slot
}

/** 提交约课 */
async function submitAppointment() {
  if (!selectedDate.value || !selectedSlot.value) {
    ElMessage.warning('请选择日期和时间段')
    return
  }
  try {
    const dateStr = selectedDate.value.toISOString
      ? selectedDate.value.toISOString().slice(0, 10)
      : selectedDate.value
    await studentApi().createAppointment({
      studentId: studentInfoId.value,
      coachId: coachId.value,
      appointmentTime: selectedSlot.value,    // 如 "08:00-10:00"
      appointmentDate: dateStr                 // 如 "2024-06-17"
    })
    ElMessage.success('约课申请已提交')
    showDialog.value = false
    // 刷新约课列表 + 可用日期缓存（刚约的日期可能不再可用）
    const res = await studentApi().getMyAppointments()
    appointments.value = res.data || []
    await loadAvailableDates()
  } catch (e) {
    // 错误已在拦截器中 toast
  }
}

/** 取消约课 */
async function handleCancel(row) {
  try {
    const { value: reason } = await ElMessageBox.prompt('请输入取消原因', '取消约课')
    await studentApi().cancelAppointment(row.id, { reason: reason || '' })
    ElMessage.success('取消成功')
    const res = await studentApi().getMyAppointments()
    appointments.value = res.data || []
    // 取消后可能有新的可用日期，刷新缓存
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
  gap: 10px;
  padding-left: 4px;
}
.slot-btn {
  min-width: 120px;
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
