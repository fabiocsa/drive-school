<template>
  <div class="page-content fade-in-up">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>教练管理</h2>
      <p class="subtitle">管理教练信息，编辑空闲档期与评分</p>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="16">
      <el-col :span="8" v-for="card in statCards" :key="card.key">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-inner">
            <div>
              <div class="stat-label">{{ card.label }}</div>
              <div class="stat-value" :style="{ color: card.color }">{{ card.value }}</div>
            </div>
            <div class="stat-icon" :style="{ background: card.bg }">
              <el-icon :size="24" :color="card.color"><component :is="card.icon" /></el-icon>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 操作栏 -->
    <div style="margin-bottom: 16px;">
      <el-button type="primary" :icon="Plus" @click="addDialog = true; editingCoach = null; resetForm()">
        添加教练
      </el-button>
    </div>

    <!-- 教练表格 -->
    <el-table
      :data="coaches" stripe highlight-current-row
      max-height="580" v-loading="loading"
      element-loading-text="加载教练数据..."
    >
      <el-table-column prop="id" label="ID" width="70" align="center" />
      <el-table-column prop="realName" label="姓名" width="100" show-overflow-tooltip />
      <el-table-column prop="phone" label="电话" width="140" />
      <el-table-column label="性别" width="70" align="center">
        <template #default="{ row }">
          <el-tag size="small" :type="row.gender === '男' ? 'primary' : 'danger'" effect="plain">
            {{ row.gender }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="车型" width="90">
        <template #default="{ row }">
          <el-tag size="small" type="info" effect="plain">{{ vehicleTypeName(row.vehicleTypeId) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="评分" width="90" align="center">
        <template #default="{ row }">
          <el-rate
            v-model="row.rating"
            disabled
            size="small"
            :max="5"
            show-score
            score-template="{value}分"
          />
        </template>
      </el-table-column>
      <el-table-column label="空闲档期" min-width="200">
        <template #default="{ row }">
          <div class="schedule-tags" v-if="parseSchedule(row.scheduleJson).length">
            <el-tag
              v-for="s in parseSchedule(row.scheduleJson)"
              :key="s"
              size="small"
              type="info"
              effect="plain"
            >{{ s }}</el-tag>
          </div>
          <span v-else style="color: var(--text-secondary); font-size: 12px;">暂无档期</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100" align="center" fixed="right">
        <template #default="{ row }">
          <el-space :size="4">
            <el-tooltip content="编辑教练" placement="top">
              <el-button type="primary" :icon="Edit" circle size="small" @click="editCoach(row)" />
            </el-tooltip>
            <el-tooltip content="删除教练" placement="top">
              <el-button type="danger" :icon="Delete" circle size="small" @click="deleteCoach(row)" />
            </el-tooltip>
          </el-space>
        </template>
      </el-table-column>
    </el-table>

    <!-- 空状态 -->
    <div v-if="!loading && coaches.length === 0" class="empty-state-wrapper">
      <el-empty description="暂无教练数据" :image-size="140" />
    </div>

    <!-- 添加/编辑教练对话框 -->
    <el-dialog
      v-model="addDialog"
      :title="editingCoach ? '编辑教练' : '添加教练'"
      width="520px"
      :close-on-click-modal="false"
      top="8vh"
    >
      <el-form :model="coachForm" label-width="100px">
        <el-divider content-position="left">账号信息</el-divider>
        <el-form-item label="用户名">
          <el-input v-model="coachForm.username" :disabled="!!editingCoach" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" v-if="!editingCoach">
          <el-input v-model="coachForm.password" placeholder="默认 123456" show-password />
        </el-form-item>

        <el-divider content-position="left">教练信息</el-divider>
        <el-form-item label="姓名">
          <el-input v-model="coachForm.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="电话">
          <el-input v-model="coachForm.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="性别">
          <el-select v-model="coachForm.gender">
            <el-option label="男" value="男" />
            <el-option label="女" value="女" />
          </el-select>
        </el-form-item>
        <el-form-item label="准教车型">
          <el-select v-model="coachForm.vehicleTypeId" placeholder="请选择准教车型">
            <el-option
              v-for="vt in vehicleTypes"
              :key="vt.id"
              :label="vt.name"
              :value="vt.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="评分">
          <el-rate v-model="coachForm.rating" :max="5" show-score />
        </el-form-item>
        <el-form-item label="空闲档期">
          <el-checkbox-group v-model="coachForm.schedule">
            <el-checkbox v-for="s in scheduleOptions" :key="s" :label="s">{{ s }}</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <div style="display: flex; justify-content: flex-end; gap: 8px;">
          <el-button @click="addDialog = false">取消</el-button>
          <el-button type="primary" @click="submitCoach">确认</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { adminApi } from '../../api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete, User, Star, Calendar } from '@element-plus/icons-vue'

const coaches = ref([])
const loading = ref(false)
const addDialog = ref(false)
const editingCoach = ref(null)
const vehicleTypes = ref([])  // 准教车型列表，用于下拉选择显示名称
const scheduleOptions = ['周一上午','周一下午','周二上午','周二下午','周三上午','周三下午','周四上午','周四下午','周五上午','周五下午','周六上午','周六下午','周日上午','周日下午']

const coachForm = reactive({
  username: '', password: '123456', realName: '', phone: '', gender: '男',
  vehicleTypeId: 1, rating: 3, schedule: []
})

// 统计卡片数据
const avgRating = computed(() => {
  if (!coaches.value.length) return 0
  const sum = coaches.value.reduce((acc, c) => acc + (c.rating || 0), 0)
  return (sum / coaches.value.length).toFixed(1)
})

const activeScheduleCount = computed(() =>
  coaches.value.filter(c => {
    const s = parseSchedule(c.scheduleJson)
    return s.length > 0
  }).length
)

const statCards = computed(() => [
  { key: 'total', label: '教练总数', value: coaches.value.length, color: '#409EFF', bg: '#ecf5ff', icon: User },
  { key: 'rating', label: '平均评分', value: avgRating.value, color: '#E6A23C', bg: '#fdf6ec', icon: Star },
  { key: 'active', label: '有档期教练', value: activeScheduleCount.value, color: '#67C23A', bg: '#f0f9eb', icon: Calendar }
])

// 解析档期 JSON
function parseSchedule(json) {
  try {
    if (!json) return []
    const arr = JSON.parse(json)
    return Array.isArray(arr) ? arr : []
  } catch {
    return []
  }
}

// 车型名称映射（优先使用后端返回的车型列表，回退到硬编码映射）
function vehicleTypeName(id) {
  const found = vehicleTypes.value.find(vt => vt.id === id)
  if (found) return found.name
  const map = { 1: 'C1', 2: 'C2', 3: 'A1', 4: 'A2', 5: 'B1', 6: 'B2' }
  return map[id] || `车型${id}`
}

function resetForm() {
  Object.assign(coachForm, {
    username: '', password: '123456', realName: '', phone: '', gender: '男',
    vehicleTypeId: 1, rating: 3, schedule: []
  })
}

onMounted(async () => {
  loading.value = true
  try {
    // 并行获取教练列表和车型列表，提升加载效率
    const [coachRes, vtRes] = await Promise.all([
      adminApi().listCoaches(),
      adminApi().listVehicleTypes()
    ])
    coaches.value = coachRes.data || []
    vehicleTypes.value = vtRes.data || []
  } catch (e) {
    // API interceptor already shows error
  } finally {
    loading.value = false
  }
})

function editCoach(row) {
  editingCoach.value = row
  // 修复：编辑模式下回显用户名（从后端 listWithDetails 返回的 username 字段获取）
  coachForm.username = row.username || ''
  coachForm.realName = row.realName
  coachForm.phone = row.phone
  coachForm.gender = row.gender
  coachForm.vehicleTypeId = row.vehicleTypeId
  coachForm.rating = row.rating
  coachForm.schedule = row.scheduleJson ? parseSchedule(row.scheduleJson) : []
  addDialog.value = true
}

async function submitCoach() {
  const data = {
    ...coachForm,
    scheduleJson: JSON.stringify(coachForm.schedule)
  }
  loading.value = true
  try {
    if (editingCoach.value) {
      await adminApi().updateCoach(editingCoach.value.id, data)
    } else {
      await adminApi().addCoach(data)
    }
    ElMessage.success('操作成功')
    addDialog.value = false
    const res = await adminApi().listCoaches()
    coaches.value = res.data || []
  } catch (e) {
    // API interceptor already shows error
  } finally {
    loading.value = false
  }
}

async function deleteCoach(row) {
  await ElMessageBox.confirm('确定删除该教练吗？', '提示', { type: 'warning' })
  loading.value = true
  try {
    await adminApi().deleteCoach(row.id)
    ElMessage.success('删除成功')
    const res = await adminApi().listCoaches()
    coaches.value = res.data || []
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
