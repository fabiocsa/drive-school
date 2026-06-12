<template>
  <div>
    <h2>学时记录</h2>
    <el-table :data="records" border style="margin-top:20px">
      <el-table-column prop="recordDate" label="培训日期" width="120" />
      <el-table-column prop="duration" label="学时(小时)" width="100" />
      <el-table-column prop="content" label="练车内容" />
      <el-table-column prop="createdTime" label="记录时间" width="180" />
    </el-table>
    <p v-if="records.length === 0" style="color:#999; margin-top:20px;">暂无学时记录</p>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '../../stores/user'
import { studentApi } from '../../api'

const userStore = useUserStore()
const records = ref([])

onMounted(async () => {
  try {
    const res = await studentApi().getTrainings()
    records.value = res.data || []
  } catch (e) {}
})
</script>
