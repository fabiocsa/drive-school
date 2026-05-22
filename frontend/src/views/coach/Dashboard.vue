<template>
  <div>
    <h2>教练首页</h2>
    <el-card style="margin-top:20px">
      <template #header>个人信息</template>
      <p>姓名: {{ info?.realName }}</p>
      <p>电话: {{ info?.phone }}</p>
      <p>评分: {{ info?.coach?.rating }} 分</p>
      <p>执教车型: {{ info?.coach?.vehicleTypeId }}</p>
      <p>空闲档期: {{ info?.coach?.scheduleJson }}</p>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '../../stores/user'
import { coachApi } from '../../api'

const userStore = useUserStore()
const info = ref(null)

onMounted(async () => {
  try {
    const res = await coachApi().getMyInfo(userStore.userId)
    info.value = res.data
  } catch (e) {}
})
</script>
