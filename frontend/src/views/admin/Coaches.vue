<template>
  <div>
    <h2>教练管理</h2>
    <el-button type="primary" @click="addDialog = true; editingCoach = null; resetForm()" style="margin:20px 0">添加教练</el-button>
    <el-table :data="coaches" border>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="realName" label="姓名" width="100" />
      <el-table-column prop="phone" label="电话" width="130" />
      <el-table-column prop="gender" label="性别" width="60" />
      <el-table-column prop="vehicleTypeId" label="车型ID" width="80" />
      <el-table-column prop="rating" label="评分" width="60" />
      <el-table-column prop="scheduleJson" label="空闲档期" />
      <el-table-column label="操作" width="150">
        <template #default="{row}">
          <el-button type="primary" size="small" @click="editCoach(row)">编辑</el-button>
          <el-button type="danger" size="small" @click="deleteCoach(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="addDialog" :title="editingCoach ? '编辑教练' : '添加教练'" width="500px">
      <el-form :model="coachForm" label-width="100px">
        <el-form-item label="用户名"><el-input v-model="coachForm.username" /></el-form-item>
        <el-form-item label="密码" v-if="!editingCoach"><el-input v-model="coachForm.password" placeholder="默认123456" /></el-form-item>
        <el-form-item label="姓名"><el-input v-model="coachForm.realName" /></el-form-item>
        <el-form-item label="电话"><el-input v-model="coachForm.phone" /></el-form-item>
        <el-form-item label="性别">
          <el-select v-model="coachForm.gender"><el-option label="男" value="男" /><el-option label="女" value="女" /></el-select>
        </el-form-item>
        <el-form-item label="车型ID"><el-input-number v-model="coachForm.vehicleTypeId" :min="1" /></el-form-item>
        <el-form-item label="评分"><el-input-number v-model="coachForm.rating" :min="1" :max="5" /></el-form-item>
        <el-form-item label="空闲档期">
          <el-checkbox-group v-model="coachForm.schedule">
            <el-checkbox v-for="s in scheduleOptions" :key="s" :label="s">{{ s }}</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addDialog = false">取消</el-button>
        <el-button type="primary" @click="submitCoach">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { adminApi } from '../../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const coaches = ref([])
const addDialog = ref(false)
const editingCoach = ref(null)
const scheduleOptions = ['周一上午','周一下午','周二上午','周二下午','周三上午','周三下午','周四上午','周四下午','周五上午','周五下午','周六上午','周六下午','周日上午','周日下午']

const coachForm = reactive({
  username: '', password: '123456', realName: '', phone: '', gender: '男',
  vehicleTypeId: 1, rating: 3, schedule: []
})

function resetForm() {
  Object.assign(coachForm, {
    username: '', password: '123456', realName: '', phone: '', gender: '男',
    vehicleTypeId: 1, rating: 3, schedule: []
  })
}

onMounted(async () => {
  try {
    const res = await adminApi().listCoaches()
    coaches.value = res.data || []
  } catch (e) {}
})

function editCoach(row) {
  editingCoach.value = row
  coachForm.realName = row.realName
  coachForm.phone = row.phone
  coachForm.gender = row.gender
  coachForm.vehicleTypeId = row.vehicleTypeId
  coachForm.rating = row.rating
  coachForm.schedule = row.scheduleJson ? JSON.parse(row.scheduleJson) : []
  addDialog.value = true
}

async function submitCoach() {
  const data = {
    ...coachForm,
    scheduleJson: JSON.stringify(coachForm.schedule)
  }
  if (editingCoach.value) {
    await adminApi().updateCoach(editingCoach.value.id, data)
  } else {
    await adminApi().addCoach(data)
  }
  ElMessage.success('操作成功')
  addDialog.value = false
  const res = await adminApi().listCoaches()
  coaches.value = res.data || []
}

async function deleteCoach(row) {
  await ElMessageBox.confirm('确定删除该教练吗？', '提示', { type: 'warning' })
  await adminApi().deleteCoach(row.id)
  ElMessage.success('删除成功')
  const res = await adminApi().listCoaches()
  coaches.value = res.data || []
}
</script>
