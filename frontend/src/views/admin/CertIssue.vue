<template>
  <div>
    <h2>发证管理</h2>
    <el-table :data="students" border style="margin-top:20px">
      <el-table-column prop="studentInfo.id" label="学员ID" width="80" />
      <el-table-column prop="realName" label="姓名" width="100" />
      <el-table-column prop="phone" label="电话" width="130" />
      <el-table-column prop="studentInfo.idCard" label="身份证号" width="180" />
      <el-table-column label="发证状态" width="100">
        <template #default="{row}">
          <el-tag type="warning">待发证</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100">
        <template #default="{row}">
          <el-button type="primary" size="small" @click="issueCert(row)">发证</el-button>
        </template>
      </el-table-column>
    </el-table>
    <p v-if="students.length === 0" style="color:#999; margin-top:20px;">暂无待发证学员</p>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { adminApi } from '../../api'
import { ElMessage } from 'element-plus'

const students = ref([])

onMounted(async () => {
  try {
    const res = await adminApi().waitingCertStudents()
    students.value = res.data || []
  } catch (e) {}
})

async function issueCert(row) {
  await adminApi().issueCert(row.studentInfo.id)
  ElMessage.success('发证成功')
  const res = await adminApi().waitingCertStudents()
  students.value = res.data || []
}
</script>
