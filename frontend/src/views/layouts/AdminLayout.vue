<template>
  <el-container>
    <el-header>
      <span class="title">驾校管理系统 - 管理员端</span>
      <div style="flex:1"></div>
      <span style="margin-right:20px">欢迎, {{ userStore.realName || userStore.username }}</span>
      <el-button @click="logout">退出</el-button>
    </el-header>
    <el-container>
      <el-aside width="200px">
        <el-menu router :default-active="$route.path">
          <el-menu-item index="/admin"><el-icon><HomeFilled /></el-icon> 首页</el-menu-item>
          <el-menu-item index="/admin/students"><el-icon><UserFilled /></el-icon> 学员管理</el-menu-item>
          <el-menu-item index="/admin/coaches"><el-icon><Avatar /></el-icon> 教练管理</el-menu-item>
          <el-menu-item index="/admin/basic"><el-icon><Setting /></el-icon> 基础信息</el-menu-item>
          <el-menu-item index="/admin/exams"><el-icon><Tickets /></el-icon> 考试管理</el-menu-item>
          <el-menu-item index="/admin/cert"><el-icon><Medal /></el-icon> 发证管理</el-menu-item>
          <el-menu-item index="/admin/statistics"><el-icon><PieChart /></el-icon> 统计分析</el-menu-item>
        </el-menu>
      </el-aside>
      <el-main><router-view /></el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useUserStore } from '../../stores/user'

const router = useRouter()
const userStore = useUserStore()

function logout() {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.el-header { display: flex; align-items: center; background: #E6A23C; color: white; padding: 0 20px; }
.el-header .title { font-size: 18px; font-weight: bold; }
.el-aside { background: #f5f5f5; min-height: calc(100vh - 60px); }
</style>
