<template>
  <div>
    <h2>管理员首页</h2>
    <el-row :gutter="20" style="margin-top:20px">
      <el-col :span="6">
        <el-statistic title="学员总数" :value="stats.studentCount" />
      </el-col>
      <el-col :span="6">
        <el-statistic title="教练总数" :value="stats.coachCount" />
      </el-col>
      <el-col :span="6">
        <el-statistic title="待审核报名" :value="stats.pendingAudit" />
      </el-col>
      <el-col :span="6">
        <el-statistic title="待发证" :value="stats.waitingCert" />
      </el-col>
    </el-row>
    <el-row :gutter="20" style="margin-top:30px">
      <el-col :span="12">
        <el-card><template #header>快捷操作</template>
          <el-space>
            <el-button type="primary" @click="$router.push('/admin/students')">学员管理</el-button>
            <el-button type="success" @click="$router.push('/admin/coaches')">教练管理</el-button>
            <el-button type="warning" @click="$router.push('/admin/exams')">考试管理</el-button>
            <el-button @click="$router.push('/admin/statistics')">统计分析</el-button>
          </el-space>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { reactive, onMounted } from 'vue'
import { adminApi } from '../../api'

const stats = reactive({ studentCount: 0, coachCount: 0, pendingAudit: 0, waitingCert: 0 })

onMounted(async () => {
  try {
    const [students, coaches, certs] = await Promise.all([
      adminApi().listStudents(),
      adminApi().listCoaches(),
      adminApi().waitingCertStudents()
    ])
    stats.studentCount = (students.data || []).length
    stats.coachCount = (coaches.data || []).length
    stats.pendingAudit = (students.data || []).filter(s => s.auditStatus === 'PENDING').length
    stats.waitingCert = (certs.data || []).length
  } catch (e) {}
})
</script>
