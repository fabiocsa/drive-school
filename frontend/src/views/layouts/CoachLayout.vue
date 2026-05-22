<template>
  <el-container>
    <el-header>
      <span class="title">教练中心</span>
      <div style="flex:1"></div>
      <span style="margin-right:20px">欢迎, {{ userStore.realName || userStore.username }}</span>
      <el-button @click="logout">退出</el-button>
    </el-header>
    <el-container>
      <el-aside width="200px">
        <el-menu router :default-active="$route.path">
          <el-menu-item index="/coach"><el-icon><HomeFilled /></el-icon> 首页</el-menu-item>
          <el-menu-item index="/coach/training"><el-icon><DataLine /></el-icon> 学时管理</el-menu-item>
          <el-menu-item index="/coach/appointments"><el-icon><Calendar /></el-icon> 约课管理</el-menu-item>
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
.el-header { display: flex; align-items: center; background: #67C23A; color: white; padding: 0 20px; }
.el-header .title { font-size: 18px; font-weight: bold; }
.el-aside { background: #f5f5f5; min-height: calc(100vh - 60px); }
</style>
