<template>
  <div class="page-content fade-in-up">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>发证管理</h2>
      <p class="subtitle">为已完成全部科目的学员发放驾驶证</p>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="16">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-inner">
            <div>
              <div class="stat-label">待发证人数</div>
              <div class="stat-value" style="color: #F56C6C;">{{ students.length }}</div>
            </div>
            <div class="stat-icon" style="background: #fef0f0;">
              <el-icon :size="24" color="#F56C6C"><Medal /></el-icon>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 发证表格 -->
    <el-table
      :data="students" stripe highlight-current-row
      max-height="520" v-loading="loading"
      element-loading-text="加载待发证数据..."
    >
      <el-table-column prop="studentInfo.id" label="学员ID" width="80" align="center" />
      <el-table-column prop="realName" label="姓名" width="100" show-overflow-tooltip />
      <el-table-column prop="phone" label="电话" width="140" />
      <el-table-column prop="studentInfo.idCard" label="身份证号" width="190" show-overflow-tooltip />
      <el-table-column label="发证状态" width="110" align="center">
        <template>
          <el-tag type="warning" size="small" effect="dark">
            <el-icon style="margin-right: 4px; vertical-align: middle;"><Clock /></el-icon>
            待发证
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100" align="center" fixed="right">
        <template #default="{ row }">
          <el-button type="success" :icon="Medal" size="small" @click="issueCert(row)">
            发证
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 空状态 -->
    <div v-if="!loading && students.length === 0" class="empty-state-wrapper">
      <el-empty description="暂无待发证学员" :image-size="140" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { adminApi } from '../../api'
import { ElMessage } from 'element-plus'
import { Medal, Clock } from '@element-plus/icons-vue'

const students = ref([])
const loading = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    const res = await adminApi().waitingCertStudents()
    students.value = res.data || []
  } catch (e) {
    // API interceptor already shows error
  } finally {
    loading.value = false
  }
})

async function issueCert(row) {
  loading.value = true
  try {
    await adminApi().issueCert(row.studentInfo.id)
    ElMessage.success('发证成功')
    const res = await adminApi().waitingCertStudents()
    students.value = res.data || []
  } catch (e) {
    // API interceptor already shows error
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.stat-card :deep(.el-card__body) {
  padding: 20px 24px;
}
.stat-inner {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.stat-label {
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 8px;
}
.stat-value {
  font-size: 32px;
  font-weight: 700;
  line-height: 1.1;
}
.stat-icon {
  width: 52px;
  height: 52px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
</style>
