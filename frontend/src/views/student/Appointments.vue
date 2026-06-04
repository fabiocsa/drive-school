<template>
  <div>
    <h2>约课管理</h2>
    <el-button type="primary" @click="showDialog = true" style="margin:20px 0">发起约课</el-button>
    <el-table :data="appointments" border>
      <el-table-column prop="appointmentTime" label="约课时间" width="160" />
      <el-table-column prop="appointmentDate" label="约课日期" width="120" />
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
          <el-button v-if="row.status === 'PENDING' || row.status === 'CONFIRMED'" type="danger" size="small" @click="handleCancel(row)">取消</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="showDialog" title="发起约课" width="400px">
      <el-form :model="apptForm" label-width="100px">
        <el-form-item label="时间档期">
          <el-select v-model="apptForm.appointmentTime" placeholder="选择档期">
            <el-option v-for="s in scheduleOptions" :key="s" :label="s" :value="s" />
          </el-select>
        </el-form-item>
        <el-form-item label="约课日期">
          <el-date-picker v-model="apptForm.appointmentDate" type="date" placeholder="选择日期" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="submitAppointment">提交</el-button>
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
const scheduleOptions = ['周一上午','周一下午','周二上午','周二下午','周三上午','周三下午','周四上午','周四下午','周五上午','周五下午','周六上午','周六下午','周日上午','周日下午']
const studentInfoId = ref(null)
const coachId = ref(null)

const apptForm = reactive({ appointmentTime: '', appointmentDate: '' })
const statusMap = { PENDING: '待确认', CONFIRMED: '已确认', CANCELLED: '已取消', COMPLETED: '已完成' }

onMounted(async () => {
  try {
    const infoRes = await studentApi().getMyInfo(userStore.userId)
    studentInfoId.value = infoRes.data?.studentInfo?.id
    coachId.value = infoRes.data?.studentInfo?.coachId
    const res = await studentApi().getMyAppointments(userStore.userId)
    appointments.value = res.data || []
  } catch (e) {}
})

async function submitAppointment() {
  if (!apptForm.appointmentTime || !apptForm.appointmentDate) {
    ElMessage.warning('请填写完整信息')
    return
  }
  await studentApi().createAppointment({
    studentId: studentInfoId.value,
    coachId: coachId.value,
    appointmentTime: apptForm.appointmentTime,
    appointmentDate: apptForm.appointmentDate
  })
  ElMessage.success('约课申请已提交')
  showDialog.value = false
  const res = await studentApi().getMyAppointments(userStore.userId)
  appointments.value = res.data || []
}

async function handleCancel(row) {
  try {
    const { value: reason } = await ElMessageBox.prompt('请输入取消原因', '取消约课')
    await studentApi().cancelAppointment(row.id, { userId: userStore.userId, reason: reason || '' })
    ElMessage.success('取消成功')
    const res = await studentApi().getMyAppointments(userStore.userId)
    appointments.value = res.data || []
  } catch (e) {}
}
</script>
