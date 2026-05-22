<template>
  <div>
    <h2>基础信息管理</h2>
    <el-tabs v-model="activeTab">
      <el-tab-pane label="车型管理" name="vehicle">
        <el-button type="primary" @click="openVtDialog(null)" style="margin:10px 0">添加车型</el-button>
        <el-table :data="vehicleTypes" border>
          <el-table-column prop="id" label="ID" width="60" />
          <el-table-column prop="name" label="名称" width="80" />
          <el-table-column prop="minAge" label="最低年龄" width="80" />
          <el-table-column prop="subjectHoursJson" label="各科目学时" />
          <el-table-column label="操作">
            <template #default="{row}">
              <el-button type="primary" size="small" @click="openVtDialog(row)">编辑</el-button>
              <el-button type="danger" size="small" @click="deleteVt(row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
      <el-tab-pane label="科目管理" name="subject">
        <el-button type="primary" @click="openSubjectDialog(null)" style="margin:10px 0">添加科目</el-button>
        <el-table :data="subjects" border>
          <el-table-column prop="id" label="ID" width="60" />
          <el-table-column prop="name" label="名称" />
          <el-table-column prop="sortOrder" label="排序" width="80" />
          <el-table-column prop="examFee" label="考试费" width="100" />
          <el-table-column label="操作">
            <template #default="{row}">
              <el-button type="primary" size="small" @click="openSubjectDialog(row)">编辑</el-button>
              <el-button type="danger" size="small" @click="deleteSubject(row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
      <el-tab-pane label="考场管理" name="location">
        <el-button type="primary" @click="openLocDialog(null)" style="margin:10px 0">添加考场</el-button>
        <el-table :data="locations" border>
          <el-table-column prop="id" label="ID" width="60" />
          <el-table-column prop="name" label="名称" />
          <el-table-column prop="address" label="地址" />
          <el-table-column prop="capacity" label="容量" width="80" />
          <el-table-column prop="contact" label="联系人" />
          <el-table-column label="操作">
            <template #default="{row}">
              <el-button type="primary" size="small" @click="openLocDialog(row)">编辑</el-button>
              <el-button type="danger" size="small" @click="deleteLoc(row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
      <el-tab-pane label="费用标准" name="fee">
        <el-button type="primary" @click="openFeeDialog(null)" style="margin:10px 0">添加费用</el-button>
        <el-table :data="fees" border>
          <el-table-column prop="id" label="ID" width="60" />
          <el-table-column prop="itemName" label="名称" />
          <el-table-column prop="feeType" label="类型" />
          <el-table-column prop="amount" label="金额" />
          <el-table-column label="操作">
            <template #default="{row}">
              <el-button type="primary" size="small" @click="openFeeDialog(row)">编辑</el-button>
              <el-button type="danger" size="small" @click="deleteFee(row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="showVtDialog" :title="editingVt ? '编辑车型' : '添加车型'" width="400px">
      <el-form :model="vtForm" label-width="120px">
        <el-form-item label="名称"><el-input v-model="vtForm.name" /></el-form-item>
        <el-form-item label="最低年龄"><el-input-number v-model="vtForm.minAge" :min="16" :max="70" /></el-form-item>
        <el-form-item label="科目一学时"><el-input-number v-model="vtForm.phase1" :min="0" /></el-form-item>
        <el-form-item label="科目二学时"><el-input-number v-model="vtForm.phase2" :min="0" /></el-form-item>
        <el-form-item label="科目三学时"><el-input-number v-model="vtForm.phase3" :min="0" /></el-form-item>
        <el-form-item label="科目四学时"><el-input-number v-model="vtForm.phase4" :min="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showVtDialog = false">取消</el-button>
        <el-button type="primary" @click="submitVt">确认</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showSubjectDialog" :title="editingSubject ? '编辑科目' : '添加科目'" width="400px">
      <el-form :model="subjectForm" label-width="100px">
        <el-form-item label="名称"><el-input v-model="subjectForm.name" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="subjectForm.sortOrder" :min="1" /></el-form-item>
        <el-form-item label="考试费"><el-input-number v-model="subjectForm.examFee" :min="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showSubjectDialog = false">取消</el-button>
        <el-button type="primary" @click="submitSubject">确认</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showLocDialog" :title="editingLoc ? '编辑考场' : '添加考场'" width="400px">
      <el-form :model="locForm" label-width="80px">
        <el-form-item label="名称"><el-input v-model="locForm.name" /></el-form-item>
        <el-form-item label="地址"><el-input v-model="locForm.address" /></el-form-item>
        <el-form-item label="容量"><el-input-number v-model="locForm.capacity" :min="1" /></el-form-item>
        <el-form-item label="联系人"><el-input v-model="locForm.contact" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showLocDialog = false">取消</el-button>
        <el-button type="primary" @click="submitLoc">确认</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showFeeDialog" :title="editingFee ? '编辑费用' : '添加费用'" width="400px">
      <el-form :model="feeForm" label-width="80px">
        <el-form-item label="名称"><el-input v-model="feeForm.itemName" /></el-form-item>
        <el-form-item label="类型">
          <el-select v-model="feeForm.feeType"><el-option label="报名费" value="REGISTRATION" /><el-option label="考试费" value="EXAM" /><el-option label="补考费" value="RETAKE" /></el-select>
        </el-form-item>
        <el-form-item label="金额"><el-input-number v-model="feeForm.amount" :min="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showFeeDialog = false">取消</el-button>
        <el-button type="primary" @click="submitFee">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { adminApi } from '../../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const activeTab = ref('vehicle')
const vehicleTypes = ref([]); const subjects = ref([]); const locations = ref([]); const fees = ref([])
const showVtDialog = ref(false); const editingVt = ref(null)
const showSubjectDialog = ref(false); const editingSubject = ref(null)
const showLocDialog = ref(false); const editingLoc = ref(null)
const showFeeDialog = ref(false); const editingFee = ref(null)

const vtForm = reactive({ name: '', minAge: 18, phase1: 12, phase2: 16, phase3: 24, phase4: 10 })
const subjectForm = reactive({ name: '', sortOrder: 1, examFee: 0 })
const locForm = reactive({ name: '', address: '', capacity: 50, contact: '' })
const feeForm = reactive({ itemName: '', feeType: 'REGISTRATION', amount: 0 })

onMounted(() => { loadAll() })

async function loadAll() {
  try {
    const [vt, sj, lc, fe] = await Promise.all([
      adminApi().listVehicleTypes(), adminApi().listSubjects(),
      adminApi().listExamLocations(), adminApi().listFeeStandards()
    ])
    vehicleTypes.value = vt.data || []; subjects.value = sj.data || []
    locations.value = lc.data || []; fees.value = fe.data || []
  } catch (e) {}
}

function openVtDialog(row) {
  editingVt.value = row
  if (row) {
    vtForm.name = row.name; vtForm.minAge = row.minAge
    const h = row.subjectHoursJson ? JSON.parse(row.subjectHoursJson) : {}
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
  if (editingVt.value) await adminApi().updateVehicleType(editingVt.value.id, data)
  else await adminApi().addVehicleType(data)
  ElMessage.success('操作成功'); showVtDialog.value = false; await loadAll()
}

async function deleteVt(id) { await ElMessageBox.confirm('确定删除？', '提示', { type: 'warning' }); await adminApi().deleteVehicleType(id); await loadAll() }

function openSubjectDialog(row) {
  editingSubject.value = row
  Object.assign(subjectForm, row ? { name: row.name, sortOrder: row.sortOrder, examFee: row.examFee } : { name: '', sortOrder: 1, examFee: 0 })
  showSubjectDialog.value = true
}

async function submitSubject() {
  if (editingSubject.value) await adminApi().updateSubject(editingSubject.value.id, subjectForm)
  else await adminApi().addSubject(subjectForm)
  ElMessage.success('操作成功'); showSubjectDialog.value = false; await loadAll()
}

async function deleteSubject(id) { await ElMessageBox.confirm('确定删除？', '提示', { type: 'warning' }); await adminApi().deleteSubject(id); await loadAll() }

function openLocDialog(row) {
  editingLoc.value = row
  Object.assign(locForm, row ? { name: row.name, address: row.address, capacity: row.capacity, contact: row.contact } : { name: '', address: '', capacity: 50, contact: '' })
  showLocDialog.value = true
}

async function submitLoc() {
  if (editingLoc.value) await adminApi().updateExamLocation(editingLoc.value.id, locForm)
  else await adminApi().addExamLocation(locForm)
  ElMessage.success('操作成功'); showLocDialog.value = false; await loadAll()
}

async function deleteLoc(id) { await ElMessageBox.confirm('确定删除？', '提示', { type: 'warning' }); await adminApi().deleteExamLocation(id); await loadAll() }

function openFeeDialog(row) {
  editingFee.value = row
  Object.assign(feeForm, row ? { itemName: row.itemName, feeType: row.feeType, amount: row.amount } : { itemName: '', feeType: 'REGISTRATION', amount: 0 })
  showFeeDialog.value = true
}

async function submitFee() {
  if (editingFee.value) await adminApi().updateFeeStandard(editingFee.value.id, feeForm)
  else await adminApi().addFeeStandard(feeForm)
  ElMessage.success('操作成功'); showFeeDialog.value = false; await loadAll()
}

async function deleteFee(id) { await ElMessageBox.confirm('确定删除？', '提示', { type: 'warning' }); await adminApi().deleteFeeStandard(id); await loadAll() }
</script>
