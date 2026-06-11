<template>
  <div>
    <h2>学员管理</h2>
    <el-table :data="students" border style="margin-top:20px">
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
            {{ statusMap[row.auditStatus] }}
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
      <el-table-column label="分配" width="80">
        <template #default="{row}">{{ row.assignStatus === 'ASSIGNED' ? '已分配' : '待分配' }}</template>
      </el-table-column>
      <el-table-column prop="registrationTime" label="报名时间" width="180" />
      <el-table-column label="操作" width="280">
        <template #default="{row}">
          <el-button v-if="row.auditStatus === 'PENDING'" type="primary" size="small" @click="auditDialog(row)">审核</el-button>
          <el-button v-if="row.auditStatus === 'APPROVED' && row.assignStatus === 'PENDING'" type="success" size="small" @click="assignDialog(row)">分配教练</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="showAudit" title="审核学员" width="400px">
      <el-form>
        <el-form-item label="体检状态">
          <el-radio-group v-model="auditForm.medicalStatus">
            <el-radio label="PASSED">合格</el-radio>
            <el-radio label="FAILED">不合格</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审核结果">
          <el-radio-group v-model="auditForm.status">
            <el-radio label="APPROVED">通过</el-radio>
            <el-radio label="REJECTED">不通过</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="auditForm.remark" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAudit = false">取消</el-button>
        <el-button type="primary" @click="submitAudit">确认审核</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showAssign" title="分配教练" width="500px">
      <h4>推荐教练（按评分排序）</h4>
      <el-table :data="recommendedCoaches" border @row-click="selectCoach">
        <el-table-column prop="name" label="姓名" />
        <el-table-column prop="phone" label="电话" />
        <el-table-column prop="coach.rating" label="评分" />
        <el-table-column label="操作">
          <template #default="{row}">
            <el-radio v-model="selectedCoachId" :value="row.coach.id">选择</el-radio>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="showAssign = false">取消</el-button>
        <el-button type="primary" @click="submitAssign" :disabled="!selectedCoachId">确认分配</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { adminApi, commonApi } from '../../api'
import { ElMessage } from 'element-plus'

const students = ref([])
const showAudit = ref(false)
const showAssign = ref(false)
const currentStudent = ref(null)
const recommendedCoaches = ref([])
const selectedCoachId = ref(null)
const vehicleTypeMap = ref({})

const statusMap = { PENDING: '待审核', APPROVED: '已通过', REJECTED: '未通过' }
const auditForm = reactive({ medicalStatus: 'PASSED', status: 'APPROVED', remark: '' })

onMounted(async () => {
  await loadStudents()
  try {
    const vtRes = await commonApi().listVehicleTypes()
    (vtRes.data || []).forEach(v => { vehicleTypeMap.value[v.id] = v.name })
  } catch (e) {}
})

function getVehicleTypeName(id) {
  return vehicleTypeMap.value[id] || id
}

async function loadStudents() {
  try {
    const res = await adminApi().listStudents()
    students.value = res.data || []
  } catch (e) {}
}

function auditDialog(row) {
  currentStudent.value = row
  auditForm.medicalStatus = row.medicalStatus === 'PASSED' ? 'PASSED' : 'PASSED'
  auditForm.status = 'APPROVED'
  auditForm.remark = ''
  showAudit.value = true
}

async function submitAudit() {
  await adminApi().auditStudent(currentStudent.value.id, {
    medicalStatus: auditForm.medicalStatus,
    status: auditForm.status,
    remark: auditForm.remark
  })
  ElMessage.success('审核完成')
  showAudit.value = false
  await loadStudents()
}

async function assignDialog(row) {
  currentStudent.value = row
  selectedCoachId.value = null
  try {
    const res = await adminApi().recommendCoaches(row.id)
    recommendedCoaches.value = res.data || []
  } catch (e) {}
  showAssign.value = true
}

function selectCoach(row) {
  selectedCoachId.value = row.coach.id
}

async function submitAssign() {
  await adminApi().assignCoach(currentStudent.value.id, { coachId: selectedCoachId.value })
  ElMessage.success('分配成功')
  showAssign.value = false
  await loadStudents()
}
</script>
