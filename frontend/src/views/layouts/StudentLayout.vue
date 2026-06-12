<template>
  <el-container>
    <el-header>
      <span class="title">学员中心</span>
      <div style="flex:1"></div>
      <span style="margin-right:20px">欢迎, {{ userStore.realName || userStore.username }}</span>
      <el-button @click="logout">退出</el-button>
    </el-header>
    <el-container>
      <el-aside width="200px">
        <el-menu router :default-active="$route.path">
          <el-menu-item index="/student"><el-icon><HomeFilled /></el-icon> 首页</el-menu-item>
          <el-menu-item index="/student/registration"><el-icon><Edit /></el-icon> 报名信息</el-menu-item>
          <el-menu-item index="/student/status"><el-icon><Document /></el-icon> 审核状态</el-menu-item>
          <el-menu-item index="/student/trainings"><el-icon><DataLine /></el-icon> 学时记录</el-menu-item>
          <el-menu-item index="/student/appointments"><el-icon><Calendar /></el-icon> 约课管理</el-menu-item>
          <el-menu-item index="/student/exams"><el-icon><Tickets /></el-icon> 考试管理</el-menu-item>
          <el-menu-item index="/student/pdf"><el-icon><Files /></el-icon> PDF下载</el-menu-item>
        </el-menu>
      </el-aside>
      <el-main><router-view /></el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../../stores/user'

const router = useRouter()
const userStore = useUserStore()

onMounted(() => {
  if (userStore.role !== 'ROLE_STUDENT') {
    router.replace('/login')
  }
})

function logout() {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.el-header { display: flex; align-items: center; background: #409EFF; color: white; padding: 0 20px; }
.el-header .title { font-size: 18px; font-weight: bold; }
.el-aside { background: #f5f5f5; min-height: calc(100vh - 60px); }
</style>
