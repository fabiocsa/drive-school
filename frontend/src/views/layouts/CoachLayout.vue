<template>
  <div class="coach-layout">
    <!-- 侧边栏 -->
    <aside class="sidebar">
      <div class="sidebar-brand">
        <span class="brand-icon">👨‍🏫</span>
        <div>
          <div class="brand-name">驾校管理系统</div>
          <div class="brand-role">教练中心</div>
        </div>
      </div>

      <el-menu
        router
        :default-active="$route.path"
        class="sidebar-menu"
        background-color="#1d1e2c"
        text-color="#8b8d9e"
        active-text-color="#ffffff"
      >
        <el-menu-item index="/coach">
          <el-icon><HomeFilled /></el-icon>
          <span>工作台</span>
        </el-menu-item>
        <el-menu-item index="/coach/training">
          <el-icon><DataLine /></el-icon>
          <span>学时管理</span>
        </el-menu-item>
        <el-menu-item index="/coach/appointments">
          <el-icon><Calendar /></el-icon>
          <span>约课管理</span>
        </el-menu-item>
      </el-menu>

      <div class="sidebar-footer">
        <span>{{ userStore.realName || userStore.username }}</span>
      </div>
    </aside>

    <!-- 右侧主体 -->
    <div class="main-area">
      <header class="topbar">
        <div class="topbar-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/coach' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="$route.path !== '/coach'">
              {{ currentPageTitle }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="topbar-right">
          <el-dropdown trigger="click">
            <span class="user-dropdown">
              <el-avatar :size="32" icon="UserFilled" />
              <span class="user-name">{{ userStore.realName || userStore.username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item disabled>角色：教练</el-dropdown-item>
                <el-dropdown-item divided @click="logout">
                  <el-icon><SwitchButton /></el-icon> 退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <main class="content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../../stores/user'
import {
  HomeFilled, DataLine, Calendar,
  ArrowDown, SwitchButton
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const pageTitles = {
  '/coach': '工作台',
  '/coach/training': '学时管理',
  '/coach/appointments': '约课管理'
}
const currentPageTitle = computed(() => pageTitles[route.path] || '')

onMounted(() => {
  if (userStore.role !== 'ROLE_COACH') {
    router.replace('/login')
  }
})

function logout() {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.coach-layout {
  display: flex;
  min-height: 100vh;
  background: var(--bg-page);
}

.sidebar {
  width: var(--sidebar-width);
  background: var(--sidebar-bg);
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  z-index: 10;
}

.sidebar-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 20px 18px 18px;
  border-bottom: 1px solid rgba(255,255,255,0.06);
}
.brand-icon { font-size: 28px; flex-shrink: 0; }
.brand-name {
  font-size: 15px; font-weight: 700; color: #ffffff; line-height: 1.3;
}
.brand-role {
  font-size: 11px; color: #67c23a; font-weight: 500; letter-spacing: 1px;
}

.sidebar-menu {
  flex: 1;
  padding-top: 8px;
}
.sidebar-menu :deep(.el-menu-item) {
  height: 44px; line-height: 44px; margin: 2px 10px;
  border-radius: 8px; font-size: 14px; transition: all var(--transition);
}
.sidebar-menu :deep(.el-menu-item:hover) {
  background: rgba(255,255,255,0.06) !important;
}
.sidebar-menu :deep(.el-menu-item.is-active) {
  background: linear-gradient(135deg, rgba(103,194,58,0.25), rgba(103,194,58,0.08)) !important;
  border-left: 3px solid #67c23a; font-weight: 600;
}
.sidebar-menu :deep(.el-icon) { font-size: 18px; }

.sidebar-footer {
  padding: 14px 18px;
  border-top: 1px solid rgba(255,255,255,0.06);
  font-size: 13px; color: #8b8d9e; text-align: center;
}

.main-area {
  flex: 1; display: flex; flex-direction: column; min-width: 0;
}

.topbar {
  height: var(--header-height);
  background: var(--header-bg);
  box-shadow: var(--header-shadow);
  display: flex; align-items: center; justify-content: space-between;
  padding: 0 24px; flex-shrink: 0; z-index: 5;
}

.topbar-left { display: flex; align-items: center; }
.topbar-right { display: flex; align-items: center; gap: 20px; }

.user-dropdown {
  display: flex; align-items: center; gap: 8px; cursor: pointer;
  padding: 4px 8px; border-radius: 8px; transition: background var(--transition);
}
.user-dropdown:hover { background: #f5f5f5; }
.user-name { font-size: 14px; font-weight: 500; color: var(--text-primary); }

.content {
  flex: 1; padding: 24px; overflow-y: auto; background: var(--bg-page);
}
</style>
