<template>
  <div class="login-page">
    <!-- 动态背景 -->
    <div class="bg-shapes">
      <div class="shape shape-1"></div>
      <div class="shape shape-2"></div>
      <div class="shape shape-3"></div>
      <div class="shape shape-4"></div>
    </div>

    <!-- 主卡片 -->
    <div class="login-wrapper fade-in-up">
      <div class="login-brand">
        <div class="brand-icon">🚗</div>
        <h1>驾校报名管理系统</h1>
        <p>Driving School Management System</p>
      </div>

      <div class="login-card">
        <h2 class="card-title">账号登录</h2>

        <el-form :model="form" :rules="rules" ref="formRef" @keyup.enter="handleLogin">
          <el-form-item prop="username">
            <el-input
              v-model="form.username"
              placeholder="请输入用户名"
              :prefix-icon="User"
              size="large"
              clearable
            />
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="请输入密码"
              :prefix-icon="Lock"
              size="large"
              show-password
              @keyup.enter="handleLogin"
            />
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              @click="handleLogin"
              :loading="loading"
              size="large"
              class="login-btn"
            >
              登 录
            </el-button>
          </el-form-item>
        </el-form>

        <div class="login-footer">
          还没有账号？
          <router-link to="/register" class="link">立即注册 →</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { authApi } from '../api'
import { useUserStore } from '../stores/user'
import { User, Lock } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    const res = await authApi().login(form)
    userStore.setLoginData(res.data)
    const role = res.data.role
    if (role === 'ROLE_ADMIN') router.push('/admin')
    else if (role === 'ROLE_COACH') router.push('/coach')
    else router.push('/student')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/* ========================================
   登录页 — 全屏动态渐变 + 玻璃卡片
   ======================================== */

.login-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #0f172a 0%, #1e3a5f 30%, #2563eb 60%, #7c3aed 100%);
  background-size: 400% 400%;
  animation: gradientShift 15s ease infinite;
  overflow: hidden;
  position: relative;
}

@keyframes gradientShift {
  0%, 100% { background-position: 0% 50%; }
  50%      { background-position: 100% 50%; }
}

/* 浮动装饰形状 */
.bg-shapes {
  position: absolute;
  inset: 0;
  pointer-events: none;
  overflow: hidden;
}
.shape {
  position: absolute;
  border-radius: 50%;
  opacity: 0.08;
  background: white;
}
.shape-1 {
  width: 500px; height: 500px;
  top: -150px; right: -100px;
  animation: float1 20s ease-in-out infinite;
}
.shape-2 {
  width: 300px; height: 300px;
  bottom: -80px; left: -60px;
  animation: float2 18s ease-in-out infinite;
}
.shape-3 {
  width: 200px; height: 200px;
  top: 30%; left: 60%;
  animation: float3 14s ease-in-out infinite;
}
.shape-4 {
  width: 150px; height: 150px;
  top: 60%; left: 15%;
  animation: float1 16s ease-in-out infinite reverse;
}
@keyframes float1 {
  0%, 100% { transform: translate(0, 0) scale(1); }
  33%      { transform: translate(30px, -40px) scale(1.05); }
  66%      { transform: translate(-20px, 20px) scale(0.95); }
}
@keyframes float2 {
  0%, 100% { transform: translate(0, 0); }
  50%      { transform: translate(-40px, -30px); }
}
@keyframes float3 {
  0%, 100% { transform: translate(0, 0); }
  50%      { transform: translate(25px, -25px); }
}

/* 卡片容器 */
.login-wrapper {
  position: relative;
  z-index: 1;
  width: 420px;
}

/* 品牌区 */
.login-brand {
  text-align: center;
  margin-bottom: 28px;
}
.brand-icon {
  font-size: 48px;
  margin-bottom: 8px;
  filter: drop-shadow(0 4px 8px rgba(0,0,0,0.2));
}
.login-brand h1 {
  font-size: 26px;
  font-weight: 700;
  color: #ffffff;
  letter-spacing: 2px;
  margin: 0;
}
.login-brand p {
  font-size: 13px;
  color: rgba(255,255,255,0.55);
  letter-spacing: 1px;
  margin-top: 6px;
}

/* 登录卡片 */
.login-card {
  background: rgba(255,255,255,0.95);
  backdrop-filter: blur(20px);
  border-radius: 16px;
  padding: 36px 32px 28px;
  box-shadow: 0 24px 64px rgba(0,0,0,0.3), 0 0 0 1px rgba(255,255,255,0.1);
}
.card-title {
  font-size: 20px;
  font-weight: 600;
  color: #1f2937;
  text-align: center;
  margin-bottom: 28px;
  padding-bottom: 16px;
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
  background: var(--color-primary);
  border-radius: 2px;
}

/* 登录按钮 */
.login-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  letter-spacing: 4px;
  background: linear-gradient(135deg, #2563eb, #7c3aed);
  border: none;
}
.login-btn:hover {
  opacity: 0.9;
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(37, 99, 235, 0.4);
}

/* 底部链接 */
.login-footer {
  text-align: center;
  font-size: 14px;
  color: #9ca3af;
}
.link {
  color: #2563eb;
  text-decoration: none;
  font-weight: 500;
}
.link:hover {
  color: #7c3aed;
}

/* 表单间距 */
:deep(.el-form-item) {
  margin-bottom: 20px;
}
</style>
