<template>
  <div class="page-content fade-in-up">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>基础信息管理</h2>
      <p class="subtitle">管理车型、科目、考场及费用标准等基础数据</p>
    </div>

    <!-- 信息标签页 -->
    <el-tabs v-model="activeTab" type="border-card" class="info-tabs">
      <!-- ==================== 车型管理 ==================== -->
      <el-tab-pane label="车型管理" name="vehicle">
        <el-row :gutter="16" style="margin-bottom: 16px;">
          <el-col :span="6">
            <el-card shadow="hover" class="stat-card">
              <div class="stat-inner">
                <div>
                  <div class="stat-label">车型总数</div>
                  <div class="stat-value" style="color: #409EFF;">{{ vehicleTypes.length }}</div>
                </div>
                <div class="stat-icon" style="background: #ecf5ff;">
                  <el-icon :size="24" color="#409EFF"><Van /></el-icon>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <el-button type="primary" :icon="Plus" @click="openVtDialog(null)" style="margin-bottom: 16px;">
          添加车型
        </el-button>

        <el-table
          :data="vehicleTypes" stripe highlight-current-row
          max-height="400" v-loading="loading"
          element-loading-text="加载车型数据..."
        >
          <el-table-column prop="id" label="ID" width="70" align="center" />
          <el-table-column prop="name" label="名称" width="100" />
          <el-table-column label="最低年龄" width="100" align="center">
            <template #default="{ row }">{{ row.minAge }} 岁</template>
          </el-table-column>
          <el-table-column label="各科目学时" min-width="280">
            <template #default="{ row }">
              <div class="schedule-tags">
                <template v-for="(hours, phase) in parseHours(row.subjectHoursJson)" :key="phase">
                  <el-tag size="small" :type="phaseTagType(phase)" effect="plain">
                    {{ formatPhase(phase) }}：{{ hours }}h
                  </el-tag>
                </template>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100" align="center" fixed="right">
            <template #default="{ row }">
              <el-space :size="4">
                <el-tooltip content="编辑车型" placement="top">
                  <el-button type="primary" :icon="Edit" circle size="small" @click="openVtDialog(row)" />
                </el-tooltip>
                <el-tooltip content="删除车型" placement="top">
                  <el-button type="danger" :icon="Delete" circle size="small" @click="deleteVt(row.id)" />
                </el-tooltip>
              </el-space>
            </template>
          </el-table-column>
        </el-table>

        <div v-if="!loading && vehicleTypes.length === 0" class="empty-state-wrapper">
          <el-empty description="暂无车型数据" :image-size="120" />
        </div>
      </el-tab-pane>

      <!-- ==================== 科目管理 ==================== -->
      <el-tab-pane label="科目管理" name="subject">
        <el-row :gutter="16" style="margin-bottom: 16px;">
          <el-col :span="6">
            <el-card shadow="hover" class="stat-card">
              <div class="stat-inner">
                <div>
                  <div class="stat-label">科目总数</div>
                  <div class="stat-value" style="color: #67C23A;">{{ subjects.length }}</div>
                </div>
                <div class="stat-icon" style="background: #f0f9eb;">
                  <el-icon :size="24" color="#67C23A"><Collection /></el-icon>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <el-button type="primary" :icon="Plus" @click="openSubjectDialog(null)" style="margin-bottom: 16px;">
          添加科目
        </el-button>

        <el-table
          :data="subjects" stripe highlight-current-row
          max-height="400" v-loading="loading"
          element-loading-text="加载科目数据..."
        >
          <el-table-column prop="id" label="ID" width="70" align="center" />
          <el-table-column prop="name" label="名称" />
          <el-table-column prop="sortOrder" label="排序" width="80" align="center" />
          <el-table-column label="考试费" width="120" align="center">
            <template #default="{ row }">¥{{ row.examFee }}</template>
          </el-table-column>
          <el-table-column label="操作" width="100" align="center" fixed="right">
            <template #default="{ row }">
              <el-space :size="4">
                <el-tooltip content="编辑科目" placement="top">
                  <el-button type="primary" :icon="Edit" circle size="small" @click="openSubjectDialog(row)" />
                </el-tooltip>
                <el-tooltip content="删除科目" placement="top">
                  <el-button type="danger" :icon="Delete" circle size="small" @click="deleteSubject(row.id)" />
                </el-tooltip>
              </el-space>
            </template>
          </el-table-column>
        </el-table>

        <div v-if="!loading && subjects.length === 0" class="empty-state-wrapper">
          <el-empty description="暂无科目数据" :image-size="120" />
        </div>
      </el-tab-pane>

      <!-- ==================== 考场管理 ==================== -->
      <el-tab-pane label="考场管理" name="location">
        <el-row :gutter="16" style="margin-bottom: 16px;">
          <el-col :span="6">
            <el-card shadow="hover" class="stat-card">
              <div class="stat-inner">
                <div>
                  <div class="stat-label">考场总数</div>
                  <div class="stat-value" style="color: #E6A23C;">{{ locations.length }}</div>
                </div>
                <div class="stat-icon" style="background: #fdf6ec;">
                  <el-icon :size="24" color="#E6A23C"><OfficeBuilding /></el-icon>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <el-button type="primary" :icon="Plus" @click="openLocDialog(null)" style="margin-bottom: 16px;">
          添加考场
        </el-button>

        <el-table
          :data="locations" stripe highlight-current-row
          max-height="400" v-loading="loading"
          element-loading-text="加载考场数据..."
        >
          <el-table-column prop="id" label="ID" width="70" align="center" />
          <el-table-column prop="name" label="名称" width="120" show-overflow-tooltip />
          <el-table-column prop="address" label="地址" min-width="160" show-overflow-tooltip />
          <el-table-column label="容量" width="80" align="center">
            <template #default="{ row }">
              <el-tag size="small" type="info" effect="plain">{{ row.capacity }} 人</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="contact" label="联系人" width="110" />
          <el-table-column label="操作" width="100" align="center" fixed="right">
            <template #default="{ row }">
              <el-space :size="4">
                <el-tooltip content="编辑考场" placement="top">
                  <el-button type="primary" :icon="Edit" circle size="small" @click="openLocDialog(row)" />
                </el-tooltip>
                <el-tooltip content="删除考场" placement="top">
                  <el-button type="danger" :icon="Delete" circle size="small" @click="deleteLoc(row.id)" />
                </el-tooltip>
              </el-space>
            </template>
          </el-table-column>
        </el-table>

        <div v-if="!loading && locations.length === 0" class="empty-state-wrapper">
          <el-empty description="暂无考场数据" :image-size="120" />
        </div>
      </el-tab-pane>

      <!-- ==================== 费用标准 ==================== -->
      <el-tab-pane label="费用标准" name="fee">
        <el-row :gutter="16" style="margin-bottom: 16px;">
          <el-col :span="6">
            <el-card shadow="hover" class="stat-card">
              <div class="stat-inner">
                <div>
                  <div class="stat-label">费用项数</div>
                  <div class="stat-value" style="color: #F56C6C;">{{ fees.length }}</div>
                </div>
                <div class="stat-icon" style="background: #fef0f0;">
                  <el-icon :size="24" color="#F56C6C"><Money /></el-icon>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <el-button type="primary" :icon="Plus" @click="openFeeDialog(null)" style="margin-bottom: 16px;">
          添加费用
        </el-button>

        <el-table
          :data="fees" stripe highlight-current-row
          max-height="400" v-loading="loading"
          element-loading-text="加载费用数据..."
        >
          <el-table-column prop="id" label="ID" width="70" align="center" />
          <el-table-column prop="itemName" label="名称" min-width="120" show-overflow-tooltip />
          <el-table-column label="类型" width="110" align="center">
            <template #default="{ row }">
              <el-tag size="small" :type="feeTypeTagType(row.feeType)" effect="plain">
                {{ feeTypeMap[row.feeType] || row.feeType }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="金额" width="120" align="center">
            <template #default="{ row }">¥{{ row.amount }}</template>
          </el-table-column>
          <el-table-column label="操作" width="100" align="center" fixed="right">
            <template #default="{ row }">
              <el-space :size="4">
                <el-tooltip content="编辑费用" placement="top">
                  <el-button type="primary" :icon="Edit" circle size="small" @click="openFeeDialog(row)" />
                </el-tooltip>
                <el-tooltip content="删除费用" placement="top">
                  <el-button type="danger" :icon="Delete" circle size="small" @click="deleteFee(row.id)" />
                </el-tooltip>
              </el-space>
            </template>
          </el-table-column>
        </el-table>

        <div v-if="!loading && fees.length === 0" class="empty-state-wrapper">
          <el-empty description="暂无费用数据" :image-size="120" />
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- ==================== 车型对话框 ==================== -->
    <el-dialog
      v-model="showVtDialog"
      :title="editingVt ? '编辑车型' : '添加车型'"
      width="440px"
      :close-on-click-modal="false"
      top="8vh"
    >
      <el-form :model="vtForm" label-width="110px">
        <el-form-item label="名称">
          <el-input v-model="vtForm.name" placeholder="请输入车型名称" />
        </el-form-item>
        <el-form-item label="最低年龄">
          <el-input-number v-model="vtForm.minAge" :min="16" :max="70" />
        </el-form-item>
        <el-divider content-position="left">各科目学时</el-divider>
        <el-form-item label="科目一学时">
          <el-input-number v-model="vtForm.phase1" :min="0" />
        </el-form-item>
        <el-form-item label="科目二学时">
          <el-input-number v-model="vtForm.phase2" :min="0" />
        </el-form-item>
        <el-form-item label="科目三学时">
          <el-input-number v-model="vtForm.phase3" :min="0" />
        </el-form-item>
        <el-form-item label="科目四学时">
          <el-input-number v-model="vtForm.phase4" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div style="display: flex; justify-content: flex-end; gap: 8px;">
          <el-button @click="showVtDialog = false">取消</el-button>
          <el-button type="primary" @click="submitVt">确认</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- ==================== 科目对话框 ==================== -->
    <el-dialog
      v-model="showSubjectDialog"
      :title="editingSubject ? '编辑科目' : '添加科目'"
      width="420px"
      :close-on-click-modal="false"
      top="8vh"
    >
      <el-form :model="subjectForm" label-width="100px">
        <el-form-item label="名称">
          <el-input v-model="subjectForm.name" placeholder="请输入科目名称" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="subjectForm.sortOrder" :min="1" />
        </el-form-item>
        <el-form-item label="考试费">
          <el-input-number v-model="subjectForm.examFee" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div style="display: flex; justify-content: flex-end; gap: 8px;">
          <el-button @click="showSubjectDialog = false">取消</el-button>
          <el-button type="primary" @click="submitSubject">确认</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- ==================== 考场对话框 ==================== -->
    <el-dialog
      v-model="showLocDialog"
      :title="editingLoc ? '编辑考场' : '添加考场'"
      width="440px"
      :close-on-click-modal="false"
      top="8vh"
    >
      <el-form :model="locForm" label-width="80px">
        <el-form-item label="名称">
          <el-input v-model="locForm.name" placeholder="请输入考场名称" />
        </el-form-item>
        <el-form-item label="地址">
          <el-input v-model="locForm.address" placeholder="请输入考场地址" />
        </el-form-item>
        <el-form-item label="容量">
          <el-input-number v-model="locForm.capacity" :min="1" />
        </el-form-item>
        <el-form-item label="联系人">
          <el-input v-model="locForm.contact" placeholder="请输入联系人" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div style="display: flex; justify-content: flex-end; gap: 8px;">
          <el-button @click="showLocDialog = false">取消</el-button>
          <el-button type="primary" @click="submitLoc">确认</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- ==================== 费用对话框 ==================== -->
    <el-dialog
      v-model="showFeeDialog"
      :title="editingFee ? '编辑费用' : '添加费用'"
      width="420px"
      :close-on-click-modal="false"
      top="8vh"
    >
      <el-form :model="feeForm" label-width="80px">
        <el-form-item label="名称">
          <el-input v-model="feeForm.itemName" placeholder="请输入费用名称" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="feeForm.feeType" style="width: 100%;">
            <el-option label="报名费" value="REGISTRATION" />
            <el-option label="考试费" value="EXAM" />
            <el-option label="补考费" value="RETAKE" />
          </el-select>
        </el-form-item>
        <el-form-item label="金额">
          <el-input-number v-model="feeForm.amount" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div style="display: flex; justify-content: flex-end; gap: 8px;">
          <el-button @click="showFeeDialog = false">取消</el-button>
          <el-button type="primary" @click="submitFee">确认</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { adminApi } from '../../api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete, Van, Collection, OfficeBuilding, Money } from '@element-plus/icons-vue'

const activeTab = ref('vehicle')
const loading = ref(false)
const vehicleTypes = ref([]); const subjects = ref([]); const locations = ref([]); const fees = ref([])
const showVtDialog = ref(false); const editingVt = ref(null)
const showSubjectDialog = ref(false); const editingSubject = ref(null)
const showLocDialog = ref(false); const editingLoc = ref(null)
const showFeeDialog = ref(false); const editingFee = ref(null)

const vtForm = reactive({ name: '', minAge: 18, phase1: 12, phase2: 16, phase3: 24, phase4: 10 })
const subjectForm = reactive({ name: '', sortOrder: 1, examFee: 0 })
const locForm = reactive({ name: '', address: '', capacity: 50, contact: '' })
const feeForm = reactive({ itemName: '', feeType: 'REGISTRATION', amount: 0 })

// 费用类型映射
const feeTypeMap = { REGISTRATION: '报名费', EXAM: '考试费', RETAKE: '补考费' }

onMounted(() => { loadAll() })

async function loadAll() {
  loading.value = true
  try {
    const [vt, sj, lc, fe] = await Promise.all([
      adminApi().listVehicleTypes(), adminApi().listSubjects(),
      adminApi().listExamLocations(), adminApi().listFeeStandards()
    ])
    vehicleTypes.value = vt.data || []; subjects.value = sj.data || []
    locations.value = lc.data || []; fees.value = fe.data || []
  } catch (e) {
    // API interceptor already shows error
  } finally {
    loading.value = false
  }
}

// 解析学时期限 JSON
function parseHours(json) {
  try {
    if (!json) return {}
    return JSON.parse(json)
  } catch {
    return {}
  }
}

// 科目阶段名称
function formatPhase(phase) {
  const map = { phase1: '科一', phase2: '科二', phase3: '科三', phase4: '科四' }
  return map[phase] || phase
}

// 科目阶段标签颜色
function phaseTagType(phase) {
  const map = { phase1: 'info', phase2: 'success', phase3: 'warning', phase4: 'primary' }
  return map[phase] || 'info'
}

// 费用类型标签颜色
function feeTypeTagType(feeType) {
  const map = { REGISTRATION: '', EXAM: 'warning', RETAKE: 'danger' }
  return map[feeType] || 'info'
}

// ==================== 车型 CRUD ====================
function openVtDialog(row) {
  editingVt.value = row
  if (row) {
    vtForm.name = row.name; vtForm.minAge = row.minAge
    const h = row.subjectHoursJson ? parseHours(row.subjectHoursJson) : {}
    vtForm.phase1 = h.phase1 || 12; vtForm.phase2 = h.phase2 || 16
    vtForm.phase3 = h.phase3 || 24; vtForm.phase4 = h.phase4 || 10
  } else {
    Object.assign(vtForm, { name: '', minAge: 18, phase1: 12, phase2: 16, phase3: 24, phase4: 10 })
  }
  showVtDialog.value = true
}

async function submitVt() {
  const data = {
    name: vtForm.name, minAge: vtForm.minAge,
    subjectHoursJson: JSON.stringify({ phase1: vtForm.phase1, phase2: vtForm.phase2, phase3: vtForm.phase3, phase4: vtForm.phase4 })
  }
  loading.value = true
  try {
    if (editingVt.value) await adminApi().updateVehicleType(editingVt.value.id, data)
    else await adminApi().addVehicleType(data)
    ElMessage.success('操作成功'); showVtDialog.value = false; await loadAll()
  } catch (e) { /* API interceptor already shows error */ }
  finally { loading.value = false }
}

async function deleteVt(id) { await ElMessageBox.confirm('确定删除？', '提示', { type: 'warning' }); loading.value = true; try { await adminApi().deleteVehicleType(id); await loadAll() } catch (e) {} finally { loading.value = false } }

// ==================== 科目 CRUD ====================
function openSubjectDialog(row) {
  editingSubject.value = row
  Object.assign(subjectForm, row ? { name: row.name, sortOrder: row.sortOrder, examFee: row.examFee } : { name: '', sortOrder: 1, examFee: 0 })
  showSubjectDialog.value = true
}

async function submitSubject() {
  loading.value = true
  try {
    if (editingSubject.value) await adminApi().updateSubject(editingSubject.value.id, subjectForm)
    else await adminApi().addSubject(subjectForm)
    ElMessage.success('操作成功'); showSubjectDialog.value = false; await loadAll()
  } catch (e) { /* API interceptor already shows error */ }
  finally { loading.value = false }
}

async function deleteSubject(id) { await ElMessageBox.confirm('确定删除？', '提示', { type: 'warning' }); loading.value = true; try { await adminApi().deleteSubject(id); await loadAll() } catch (e) {} finally { loading.value = false } }

// ==================== 考场 CRUD ====================
function openLocDialog(row) {
  editingLoc.value = row
  Object.assign(locForm, row ? { name: row.name, address: row.address, capacity: row.capacity, contact: row.contact } : { name: '', address: '', capacity: 50, contact: '' })
  showLocDialog.value = true
}

async function submitLoc() {
  loading.value = true
  try {
    if (editingLoc.value) await adminApi().updateExamLocation(editingLoc.value.id, locForm)
    else await adminApi().addExamLocation(locForm)
    ElMessage.success('操作成功'); showLocDialog.value = false; await loadAll()
  } catch (e) { /* API interceptor already shows error */ }
  finally { loading.value = false }
}

async function deleteLoc(id) { await ElMessageBox.confirm('确定删除？', '提示', { type: 'warning' }); loading.value = true; try { await adminApi().deleteExamLocation(id); await loadAll() } catch (e) {} finally { loading.value = false } }

// ==================== 费用 CRUD ====================
function openFeeDialog(row) {
  editingFee.value = row
  Object.assign(feeForm, row ? { itemName: row.itemName, feeType: row.feeType, amount: row.amount } : { itemName: '', feeType: 'REGISTRATION', amount: 0 })
  showFeeDialog.value = true
}

async function submitFee() {
  loading.value = true
  try {
    if (editingFee.value) await adminApi().updateFeeStandard(editingFee.value.id, feeForm)
    else await adminApi().addFeeStandard(feeForm)
    ElMessage.success('操作成功'); showFeeDialog.value = false; await loadAll()
  } catch (e) { /* API interceptor already shows error */ }
  finally { loading.value = false }
}

async function deleteFee(id) { await ElMessageBox.confirm('确定删除？', '提示', { type: 'warning' }); loading.value = true; try { await adminApi().deleteFeeStandard(id); await loadAll() } catch (e) {} finally { loading.value = false } }
</script>

<style scoped>
/* 标签页卡片样式增强 */
.info-tabs {
  box-shadow: var(--shadow-card);
  border-radius: var(--radius-md);
}
.info-tabs :deep(.el-tabs__content) {
  padding: 20px 20px 24px;
}

/* 统计卡片 */
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
