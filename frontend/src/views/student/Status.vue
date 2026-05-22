<template>
  <div>
    <h2>审核状态</h2>
    <el-card style="max-width:600px; margin-top:20px">
      <el-descriptions v-if="info?.studentInfo" :column="1" border>
        <el-descriptions-item label="审核状态">
          <el-tag :type="info.studentInfo.auditStatus === 'APPROVED' ? 'success' : info.studentInfo.auditStatus === 'REJECTED' ? 'danger' : 'warning'">
            {{ statusMap[info.studentInfo.auditStatus] }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="体检状态">
          <el-tag :type="info.studentInfo.medicalStatus === 'PASSED' ? 'success' : info.studentInfo.medicalStatus === 'FAILED' ? 'danger' : 'warning'">
            {{ info.studentInfo.medicalStatus === 'PASSED' ? '合格' : info.studentInfo.medicalStatus === 'FAILED' ? '不合格' : '待审核' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="审核备注">{{ info.studentInfo.auditRemark || '无' }}</el-descriptions-item>
        <el-descriptions-item label="教练分配">
          {{ info.studentInfo.assignStatus === 'ASSIGNED' ? '已分配' : '待分配' }}
        </el-descriptions-item>
        <el-descriptions-item label="身份证号">{{ info.studentInfo.idCard }}</el-descriptions-item>
        <el-descriptions-item label="报考车型">{{ info.vehicleType?.name || '未知' }}</el-descriptions-item>
        <el-descriptions-item label="报名时间">{{ info.studentInfo.registrationTime }}</el-descriptions-item>
      </el-descriptions>
      <p v-else style="margin-top:20px; color:#999;">暂无报名信息，请先提交报名</p>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '../../stores/user'
import { studentApi } from '../../api'

const userStore = useUserStore()
const info = ref(null)
const statusMap = { PENDING: '待审核', APPROVED: '已通过', REJECTED: '未通过' }

onMounted(async () => {
  try {
    const res = await studentApi().getMyInfo(userStore.userId)
    info.value = res.data
  } catch (e) {}
})
</script>
