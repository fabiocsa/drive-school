<template>
  <div>
    <h2>约课管理</h2>
    <el-table :data="appointments" border style="margin-top:20px">
      <el-table-column prop="studentId" label="学员ID" width="100" />
      <el-table-column prop="appointmentTime" label="约课时间" width="160" />
      <el-table-column prop="appointmentDate" label="约课日期" width="120" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{row}">
          <el-tag :type="row.status === 'CONFIRMED' ? 'success' : row.status === 'CANCELLED' ? 'info' : 'warning'">
            {{ statusMap[row.status] || row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100">
        <template #default="{row}">
          <el-button v-if="row.status === 'PENDING'" type="primary" size="small" @click="confirmAppt(row)">确认</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '../../stores/user'
import { coachApi } from '../../api'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const appointments = ref([])
const statusMap = { PENDING: '待确认', CONFIRMED: '已确认', CANCELLED: '已取消', COMPLETED: '已完成' }

onMounted(async () => {
  try {
    const res = await coachApi().getAppointments(userStore.userId)
    appointments.value = res.data || []
  } catch (e) {}
})

async function confirmAppt(row) {
  await coachApi().confirmAppointment(row.id, userStore.userId)
  ElMessage.success('确认成功')
  const res = await coachApi().getAppointments(userStore.userId)
  appointments.value = res.data || []
}
</script>
