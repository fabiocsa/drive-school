<template>
  <div>
    <h2>学员首页</h2>
    <el-row :gutter="20" style="margin-top:20px">
      <el-col :span="8">
        <el-card>
          <template #header>个人信息</template>
          <p>姓名: {{ info?.user?.realName }}</p>
          <p>手机: {{ info?.user?.phone }}</p>
          <p>审核状态: {{ statusMap[info?.studentInfo?.auditStatus] || '未知' }}</p>
          <p>教练状态: {{ info?.studentInfo?.assignStatus === 'ASSIGNED' ? '已分配' : '待分配' }}</p>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <template #header>学习进度</template>
          <p>当前阶段: {{ phaseMap[info?.learningPhase?.currentPhase] || '未开始' }}</p>
          <p>科目一: {{ info?.learningPhase?.phase1Completed ? '已完成' : '未完成' }} ({{ info?.learningPhase?.phase1Hours || 0 }} 学时)</p>
          <p>科目二: {{ info?.learningPhase?.phase2Completed ? '已完成' : '未完成' }} ({{ info?.learningPhase?.phase2Hours || 0 }} 学时)</p>
          <p>科目三: {{ info?.learningPhase?.phase3Completed ? '已完成' : '未完成' }} ({{ info?.learningPhase?.phase3Hours || 0 }} 学时)</p>
          <p>科目四: {{ info?.learningPhase?.phase4Completed ? '已完成' : '未完成' }} ({{ info?.learningPhase?.phase4Hours || 0 }} 学时)</p>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <template #header>教练信息</template>
          <div v-if="info?.coachInfo">
            <p>教练: {{ info.coachInfo.name }}</p>
            <p>电话: {{ info.coachInfo.phone }}</p>
            <p>评分: {{ info.coachInfo.coach?.rating }} 分</p>
          </div>
          <p v-else>暂未分配教练</p>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '../../stores/user'
import { studentApi } from '../../api'

const userStore = useUserStore()
const info = ref(null)

const statusMap = { PENDING: '待审核', APPROVED: '已通过', REJECTED: '未通过' }
const phaseMap = { PHASE1: '科目一学习', PHASE2: '科目二训练', PHASE3: '科目三训练', PHASE4: '科目四学习', COMPLETED: '已完成' }

onMounted(async () => {
  try {
    const res = await studentApi().getMyInfo()
    info.value = res.data
  } catch (e) {}
})
</script>
