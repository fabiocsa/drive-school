<template>
  <div class="register-page">
    <!-- 动态背景 -->
    <div class="bg-shapes">
      <div class="shape shape-1"></div>
      <div class="shape shape-2"></div>
      <div class="shape shape-3"></div>
    </div>

    <div class="register-wrapper fade-in-up">
      <div class="register-brand">
        <div class="brand-icon">📝</div>
        <h1>学员注册</h1>
        <p>加入驾校，开启驾驶之旅</p>
      </div>

      <div class="register-card">
        <h2 class="card-title">创建账号</h2>

        <el-form :model="form" :rules="rules" ref="formRef" @keyup.enter="handleRegister">
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item prop="username">
                <el-input
                  v-model="form.username"
                  placeholder="用户名"
                  :prefix-icon="User"
                  size="large"
                  clearable
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item prop="password">
                <el-input
                  v-model="form.password"
                  type="password"
                  placeholder="密码（至少6位）"
                  :prefix-icon="Lock"
                  size="large"
                  show-password
                />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item prop="realName">
                <el-input
                  v-model="form.realName"
                  placeholder="真实姓名"
                  :prefix-icon="UserFilled"
                  size="large"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item prop="phone">
                <el-input
                  v-model="form.phone"
                  placeholder="手机号"
                  :prefix-icon="Phone"
                  size="large"
                />
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item>
            <el-button
              type="primary"
              @click="handleRegister"
              :loading="loading"
              size="large"
              class="register-btn"
            >
              注 册
            </el-button>
          </el-form-item>
        </el-form>

        <div class="register-footer">
          已有账号？
          <router-link to="/login" class="link">返回登录 →</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { authApi } from '../api'
import { ElMessage } from 'element-plus'
import { User, Lock, UserFilled, Phone } from '@element-plus/icons-vue'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const form = reactive({ username: '', password: '', phone: '', realName: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, min: 6, message: '密码至少6位', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }]
}

async function handleRegister() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await authApi().register(form)
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #0f172a 0%, #1e3a5f 30%, #059669 60%, #0d9488 100%);
  background-size: 400% 400%;
  animation: gradientShift 15s ease infinite;
  overflow: hidden;
  position: relative;
}

@keyframes gradientShift {
  0%, 100% { background-position: 0% 50%; }
  50%      { background-position: 100% 50%; }
}

.bg-shapes {
  position: absolute;
  inset: 0;
  pointer-events: none;
  overflow: hidden;
}
.shape {
  position: absolute;
  border-radius: 50%;
  opacity: 0.06;
  background: white;
}
.shape-1 {
  width: 400px; height: 400px;
  top: -100px; left: -80px;
  animation: float1 20s ease-in-out infinite;
}
.shape-2 {
  width: 250px; height: 250px;
  bottom: -50px; right: -50px;
  animation: float2 16s ease-in-out infinite;
}
.shape-3 {
  width: 180px; height: 180px;
  top: 50%; right: 20%;
  animation: float3 18s ease-in-out infinite;
}
@keyframes float1 {
  0%, 100% { transform: translate(0, 0) scale(1); }
  50%      { transform: translate(30px, -30px) scale(1.05); }
}
@keyframes float2 {
  0%, 100% { transform: translate(0, 0); }
  50%      { transform: translate(-30px, -20px); }
}
@keyframes float3 {
  0%, 100% { transform: translate(0, 0); }
  50%      { transform: translate(20px, 25px); }
}

.register-wrapper {
  position: relative;
  z-index: 1;
  width: 520px;
}

.register-brand {
  text-align: center;
  margin-bottom: 24px;
}
.brand-icon {
  font-size: 44px;
  margin-bottom: 8px;
  filter: drop-shadow(0 4px 8px rgba(0,0,0,0.2));
}
.register-brand h1 {
  font-size: 24px;
  font-weight: 700;
  color: #ffffff;
  letter-spacing: 2px;
  margin: 0;
}
.register-brand p {
  font-size: 13px;
  color: rgba(255,255,255,0.5);
  margin-top: 4px;
}

.register-card {
  background: rgba(255,255,255,0.95);
  backdrop-filter: blur(20px);
  border-radius: 16px;
  padding: 32px 30px 24px;
  box-shadow: 0 24px 64px rgba(0,0,0,0.3), 0 0 0 1px rgba(255,255,255,0.1);
}
.card-title {
  font-size: 20px;
  font-weight: 600;
  color: #1f2937;
  text-align: center;
  margin-bottom: 24px;
  padding-bottom: 14px;
  border-bottom: 2px solid #f0f2f5;
  position: relative;
}
.card-title::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 50%;
  transform: translateX(-50%);
  width: 48px;
  height: 2px;
  background: #059669;
  border-radius: 2px;
}

.register-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  letter-spacing: 4px;
  background: linear-gradient(135deg, #059669, #0d9488);
  border: none;
}
.register-btn:hover {
  opacity: 0.9;
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(5, 150, 105, 0.4);
}

.register-footer {
  text-align: center;
  font-size: 14px;
  color: #9ca3af;
}
.link {
  color: #059669;
  text-decoration: none;
  font-weight: 500;
}
.link:hover {
  color: #0d9488;
}

:deep(.el-form-item) {
  margin-bottom: 18px;
}
</style>
