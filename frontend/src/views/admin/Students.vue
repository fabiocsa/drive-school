<template>
  <div>
    <h2>学员管理</h2>

    <!-- 筛选栏 + 批量操作 -->
    <el-row :gutter="10" style="margin:16px 0">
      <el-col :span="4">
        <el-select v-model="filterMedical" placeholder="体检状态筛选" clearable>
          <el-option label="全部" value="" />
          <el-option label="待体检" value="PENDING" />
          <el-option label="已体检" value="PASSED" />
          <el-option label="不合格" value="FAILED" />
        </el-select>
      </el-col>
      <el-col :span="4">
        <el-select v-model="filterAudit" placeholder="审核状态筛选" clearable>
          <el-option label="全部" value="" />
          <el-option label="待审核" value="PENDING" />
          <el-option label="已通过" value="APPROVED" />
          <el-option label="未通过" value="REJECTED" />
        </el-select>
      </el-col>
      <el-col :span="4" :offset="12" style="text-align:right">
        <el-button type="warning" :disabled="selectedIds.length === 0" @click="batchAuditDialog">
          批量审核 ({{ selectedIds.length }})
        </el-button>
      </el-col>
    </el-row>

    <el-table :data="filteredStudents" border @selection-change="handleSelectionChange" ref="tableRef">
      <el-table-column type="selection" width="50" />
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="realName" label="姓名" width="100" />
      <el-table-column prop="phone" label="电话" width="130" />
      <el-table-column prop="idCard" label="身份证号" width="180" />
      <el-table-column label="报考车型" width="80">
        <template #default="{row}">{{ getVehicleTypeName(row.vehicleTypeId) }}</template>
      </el-table-column>
      <el-table-column label="审核状态" width="100">
        <template #default="{row}">
          <el-tag :type="row.auditStatus === 'APPROVED' ? 'success' : row.auditStatus === 'REJECTED' ? 'danger' : 'warning'">
            {{ statusMap[row.auditStatus] || row.auditStatus }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="体检" width="80">
        <template #default="{row}">
          <el-tag :type="row.medicalStatus === 'PASSED' ? 'success' : row.medicalStatus === 'FAILED' ? 'danger' : 'warning'">
            {{ row.medicalStatus === 'PASSED' ? '合格' : row.medicalStatus === 'FAILED' ? '不合格' : '待审' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="审核人" width="100">
        <template #default="{row}">{{ row.auditedBy || '-' }}</template>
      </el-table-column>
      <el-table-column label="审核时间" width="160">
        <template #default="{row}">{{ row.auditedTime || '-' }}</template>
      </el-table-column>
      <el-table-column label="分配" width="80">
        <template #default="{row}">{{ row.assignStatus === 'ASSIGNED' ? '已分配' : '待分配' }}</template>
      </el-table-column>
      <el-table-column prop="registrationTime" label="报名时间" width="180" />
      <el-table-column label="备注" min-width="120">
        <template #default="{row}">{{ row.auditRemark || '' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="320" fixed="right">
        <template #default="{row}">
          <el-button v-if="row.auditStatus === 'PENDING' || row.auditStatus === 'REJECTED'"
                     type="primary" size="small" @click="auditDialog(row)">审核</el-button>
          <el-button v-if="row.auditStatus === 'APPROVED' && row.assignStatus === 'PENDING'"
                     type="success" size="small" @click="assignDialog(row)">分配教练</el-button>
          <el-button v-if="row.auditStatus === 'APPROVED' && row.assignStatus === 'ASSIGNED'"
                     type="warning" size="small" @click="assignDialog(row)">重新分配</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- ========== 单个审核对话框（两步流程） ========== -->
    <el-dialog v-model="showAudit" title="审核学员" width="500px">
      <el-alert :title="'学员: ' + currentStudent?.realName + ' | 身份证: ' + currentStudent?.idCard"
                type="info" :closable="false" style="margin-bottom:16px" />

      <!-- 第一步：体检状态 -->
      <el-divider content-position="left">第一步：确认体检状态</el-divider>
      <el-form>
        <el-form-item label="体检状态">
          <el-radio-group v-model="auditForm.medicalStatus">
            <el-radio label="PASSED">合格</el-radio>
            <el-radio label="FAILED">不合格</el-radio>
            <el-radio label="PENDING">待体检</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>

      <!-- 第二步：审核决定 -->
      <el-divider content-position="left">第二步：审核决定</el-divider>
      <el-form>
        <el-form-item label="审核结果">
          <el-radio-group v-model="auditForm.status">
            <el-radio label="APPROVED">通过</el-radio>
            <el-radio label="REJECTED">驳回</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-alert v-if="auditForm.status === 'APPROVED' && auditForm.medicalStatus !== 'PASSED'"
                  title="体检未合格，无法审核通过" type="error" :closable="false" show-icon style="margin-bottom:12px" />
        <el-form-item label="审核备注">
          <el-input v-model="auditForm.remark" type="textarea" :rows="3"
                    placeholder="审核通过可不填，驳回请填写原因" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showAudit = false">取消</el-button>
        <el-button type="primary" @click="submitAudit"
                   :disabled="auditForm.status === 'APPROVED' && auditForm.medicalStatus !== 'PASSED'">
          确认审核
        </el-button>
      </template>
    </el-dialog>

    <!-- ========== 批量审核对话框 ========== -->
    <el-dialog v-model="showBatchAudit" title="批量审核" width="500px">
      <el-alert :title="'已选择 ' + selectedIds.length + ' 名学员'" type="warning" :closable="false" style="margin-bottom:16px" />

      <el-divider content-position="left">第一步：确认体检状态</el-divider>
      <el-form>
        <el-form-item label="体检状态">
          <el-radio-group v-model="batchForm.medicalStatus">
            <el-radio label="PASSED">合格</el-radio>
            <el-radio label="FAILED">不合格</el-radio>
            <el-radio label="">不修改</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>

      <el-divider content-position="left">第二步：审核决定</el-divider>
      <el-form>
        <el-form-item label="审核结果">
          <el-radio-group v-model="batchForm.status">
            <el-radio label="APPROVED">通过</el-radio>
            <el-radio label="REJECTED">驳回</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-alert v-if="batchForm.status === 'APPROVED' && batchForm.medicalStatus !== 'PASSED'"
                  title="注意：部分学员可能因体检未合格被跳过" type="warning" :closable="false" show-icon style="margin-bottom:12px" />
        <el-form-item label="审核备注">
          <el-input v-model="batchForm.remark" type="textarea" :rows="3" placeholder="批量审核备注（所有选中学员共用）" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showBatchAudit = false">取消</el-button>
        <el-button type="primary" @click="submitBatchAudit" :loading="batchLoading">
          确认批量审核
        </el-button>
      </template>
    </el-dialog>

    <!-- ========== 分配教练对话框 ========== -->
    <el-dialog v-model="showAssign" :title="isReassign ? '重新分配教练' : '分配教练'" width="600px">
      <h4>推荐教练（按综合匹配度排序，含工作量参考）</h4>
      <el-table :data="recommendedCoaches" border @row-click="selectCoach" highlight-current-row>
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column label="评分" width="80">
          <template #default="{row}">
            <el-rate :model-value="row.coach?.rating || 0" disabled size="small" />
          </template>
        </el-table-column>
        <el-table-column prop="studentCount" label="在带学员" width="90" />
        <el-table-column label="总学时" width="90">
          <template #default="{row}">{{ Number(row.totalHours || 0).toFixed(0) }}h</template>
        </el-table-column>
        <el-table-column label="综合分" width="80">
          <template #default="{row}">
            <el-tag :type="row.compositeScore >= 80 ? 'success' : row.compositeScore >= 60 ? 'warning' : 'info'" size="small">
              {{ row.compositeScore }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="选择" width="70">
          <template #default="{row}">
            <el-radio v-model="selectedCoachId" :value="row.coach.id" />
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="showAssign = false">取消</el-button>
        <el-button type="primary" @click="submitAssign" :disabled="!selectedCoachId">
          {{ isReassign ? '确认重新分配' : '确认分配' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { adminApi, commonApi } from '../../api'
import { ElMessage } from 'element-plus'

const students = ref([])
const showAudit = ref(false)
const showBatchAudit = ref(false)
const showAssign = ref(false)
const isReassign = ref(false)           // true=重新分配, false=首次分配
const currentStudent = ref(null)
const recommendedCoaches = ref([])
const selectedCoachId = ref(null)
const selectedIds = ref([])
const batchLoading = ref(false)
const vehicleTypeMap = ref({})
const filterMedical = ref('')
const filterAudit = ref('')

const statusMap = { PENDING: '待审核', APPROVED: '已通过', REJECTED: '未通过' }

const auditForm = reactive({ medicalStatus: 'PASSED', status: 'APPROVED', remark: '' })
const batchForm = reactive({ medicalStatus: 'PASSED', status: 'APPROVED', remark: '' })

/** 前端筛选后的学员列表 */
const filteredStudents = computed(() => {
  let list = students.value
  if (filterMedical.value) {
    list = list.filter(s => s.medicalStatus === filterMedical.value)
  }
  if (filterAudit.value) {
    list = list.filter(s => s.auditStatus === filterAudit.value)
  }
  return list
})

onMounted(async () => {
  await loadStudents()
  try {
    const vtRes = await commonApi().listVehicleTypes()
    ;(vtRes.data || []).forEach(v => { vehicleTypeMap.value[v.id] = v.name })
  } catch (e) { /* ignore */ }
})

function getVehicleTypeName(id) {
  return vehicleTypeMap.value[id] || id
}

async function loadStudents() {
  try {
    const res = await adminApi().listStudents()
    students.value = res.data || []
  } catch (e) { /* ignore */ }
}

function handleSelectionChange(selection) {
  selectedIds.value = selection.map(s => s.id)
}

// ==================== 单个审核 ====================

function auditDialog(row) {
  currentStudent.value = row
  auditForm.medicalStatus = row.medicalStatus === 'PASSED' ? 'PASSED' : (row.medicalStatus || 'PENDING')
  auditForm.status = 'APPROVED'
  auditForm.remark = ''
  showAudit.value = true
}

async function submitAudit() {
  if (auditForm.status === 'APPROVED' && auditForm.medicalStatus !== 'PASSED') {
    ElMessage.warning('体检未合格，无法审核通过')
    return
  }
  try {
    await adminApi().auditStudent(currentStudent.value.id, {
      medicalStatus: auditForm.medicalStatus,
      status: auditForm.status,
      remark: auditForm.remark
    })
    ElMessage.success('审核完成')
    showAudit.value = false
    await loadStudents()
  } catch (e) { /* error handled by interceptor */ }
}

// ==================== 批量审核 ====================

function batchAuditDialog() {
  batchForm.medicalStatus = 'PASSED'
  batchForm.status = 'APPROVED'
  batchForm.remark = ''
  showBatchAudit.value = true
}

async function submitBatchAudit() {
  batchLoading.value = true
  try {
    const medicalStatus = batchForm.medicalStatus || null
    const res = await adminApi().batchAudit({
      ids: selectedIds.value,
      medicalStatus: medicalStatus,
      status: batchForm.status,
      remark: batchForm.remark
    })
    const data = res.data
    ElMessage.success(`批量审核完成: ${data.successCount}/${data.totalCount} 成功`)
    showBatchAudit.value = false
    selectedIds.value = []
    await loadStudents()
  } catch (e) { /* error handled by interceptor */ }
  finally { batchLoading.value = false }
}

// ==================== 分配教练 ====================

async function assignDialog(row) {
  currentStudent.value = row
  selectedCoachId.value = null
  isReassign.value = row.assignStatus === 'ASSIGNED'
  try {
    const res = await adminApi().recommendCoaches(row.id)
    recommendedCoaches.value = res.data || []
  } catch (e) { /* ignore */ }
  showAssign.value = true
}

function selectCoach(row) {
  selectedCoachId.value = row.coach.id
}

async function submitAssign() {
  const apiCall = isReassign.value
    ? adminApi().reassignCoach(currentStudent.value.id, { coachId: selectedCoachId.value })
    : adminApi().assignCoach(currentStudent.value.id, { coachId: selectedCoachId.value })
  await apiCall
  ElMessage.success(isReassign.value ? '重新分配成功' : '分配成功')
  showAssign.value = false
  await loadStudents()
}
</script>
