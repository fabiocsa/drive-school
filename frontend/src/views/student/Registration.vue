<template>
  <div>
    <h2>学员报名</h2>
    <el-form :model="form" ref="formRef" label-width="120px" style="max-width:600px; margin-top:20px">
      <el-form-item label="身份证号" prop="idCard" :rules="[{required:true,message:'请输入身份证号'}]">
        <el-input v-model="form.idCard" placeholder="18位身份证号" />
      </el-form-item>
      <el-form-item label="联系地址" prop="address" :rules="[{required:true,message:'请输入地址'}]">
        <el-input v-model="form.address" placeholder="详细地址" />
      </el-form-item>
      <el-form-item label="报考车型" prop="vehicleTypeId" :rules="[{required:true,message:'请选择车型'}]">
        <el-select v-model="form.vehicleTypeId" placeholder="请选择车型">
          <el-option v-for="vt in vehicleTypes" :key="vt.id" :label="vt.name" :value="vt.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="身份证正面">
        <el-upload :action="uploadUrl" :on-success="(res) => form.idCardFrontPhoto = res.data.filename" :before-upload="checkFile" list-type="picture">
          <el-button type="primary">点击上传</el-button>
        </el-upload>
      </el-form-item>
      <el-form-item label="身份证反面">
        <el-upload :action="uploadUrl" :on-success="(res) => form.idCardBackPhoto = res.data.filename" :before-upload="checkFile" list-type="picture">
          <el-button type="primary">点击上传</el-button>
        </el-upload>
      </el-form-item>
      <el-form-item label="体检表">
        <el-upload :action="uploadUrl" :on-success="(res) => form.healthReportPhoto = res.data.filename" :before-upload="checkFile" list-type="text">
          <el-button type="primary">点击上传</el-button>
        </el-upload>
      </el-form-item>
      <el-form-item label="证件照">
        <el-upload :action="uploadUrl" :on-success="(res) => form.photo = res.data.filename" :before-upload="checkPhotoFile" list-type="picture">
          <el-button type="primary">上传照片</el-button>
        </el-upload>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="submitRegistration" :loading="loading">提交报名</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '../../stores/user'
import { studentApi, commonApi } from '../../api'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)
const vehicleTypes = ref([])
const uploadUrl = '/api/student/upload'

const form = reactive({
  idCard: '', address: '', vehicleTypeId: null,
  idCardFrontPhoto: '', idCardBackPhoto: '', healthReportPhoto: '', photo: ''
})

function checkFile(file) {
  const validTypes = ['image/jpeg', 'image/png', 'application/pdf']
  if (!validTypes.includes(file.type)) {
    ElMessage.error('仅支持 jpg/png/pdf 格式')
    return false
  }
  if (file.size > 10 * 1024 * 1024) {
    ElMessage.error('文件不能超过10MB')
    return false
  }
  return true
}

function checkPhotoFile(file) {
  const validTypes = ['image/jpeg', 'image/png']
  if (!validTypes.includes(file.type)) {
    ElMessage.error('照片仅支持 jpg/png 格式')
    return false
  }
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.error('照片不能超过5MB')
    return false
  }
  return true
}

async function submitRegistration() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await studentApi().submitRegistration(form)
    ElMessage.success('报名提交成功')
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  try {
    const res = await commonApi().listVehicleTypes()
    vehicleTypes.value = res.data || []
  } catch (e) {}
})
</script>
