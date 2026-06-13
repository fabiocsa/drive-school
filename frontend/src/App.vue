<template>
  <router-view />
</template>

<script setup>
</script>

<style>
/* ========================================
   驾校管理系统 — 全局样式
   ======================================== */

/* CSS 变量 / 设计令牌 */
:root {
  --color-primary: #409EFF;
  --color-success: #67C23A;
  --color-warning: #E6A23C;
  --color-danger: #F56C6C;
  --color-info: #909399;

  --sidebar-bg: #1d1e2c;
  --sidebar-text: #a6a7b3;
  --sidebar-active: #ffffff;
  --sidebar-width: 220px;

  --header-height: 56px;
  --header-bg: #ffffff;
  --header-shadow: 0 1px 4px rgba(0,0,0,0.06);

  --bg-page: #f0f2f5;
  --bg-card: #ffffff;
  --border-color: #e8e8e8;
  --text-primary: #1f2937;
  --text-secondary: #6b7280;

  --radius-sm: 6px;
  --radius-md: 10px;
  --radius-lg: 14px;
  --shadow-card: 0 2px 12px rgba(0,0,0,0.06);
  --shadow-hover: 0 6px 24px rgba(0,0,0,0.1);
  --transition: 0.25s cubic-bezier(0.4, 0, 0.2, 1);

  --table-stripe: #fafafa;
  --table-hover: #f5f7fa;
}

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC',
               'Microsoft YaHei', 'Helvetica Neue', Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  background: var(--bg-page);
  color: var(--text-primary);
  line-height: 1.6;
}

/* 页面标题统一风格 */
h2 {
  font-size: 22px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 4px;
}

/* Element Plus 全局微调 */
.el-card {
  border-radius: var(--radius-md) !important;
  border: 1px solid var(--border-color) !important;
  box-shadow: var(--shadow-card) !important;
  transition: box-shadow var(--transition);
}
.el-card:hover {
  box-shadow: var(--shadow-hover) !important;
}
.el-card__header {
  font-weight: 600;
  font-size: 15px;
  border-bottom: 1px solid var(--border-color);
  padding: 14px 20px;
}

.el-button {
  border-radius: var(--radius-sm);
  font-weight: 500;
  transition: all var(--transition);
}

.el-table {
  border-radius: var(--radius-md);
  overflow: hidden;
}
.el-table th.el-table__cell {
  background: #fafafa;
  color: var(--text-secondary);
  font-weight: 600;
  font-size: 13px;
}

.el-tag {
  border-radius: var(--radius-sm);
  font-weight: 500;
}

.el-dialog {
  border-radius: var(--radius-lg);
}

.el-menu {
  border-right: none !important;
}

.el-input__wrapper, .el-select .el-input__wrapper, .el-date-picker .el-input__wrapper {
  border-radius: var(--radius-sm);
}

/* 滚动条美化 */
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}
::-webkit-scrollbar-thumb {
  background: #c0c4cc;
  border-radius: 3px;
}
::-webkit-scrollbar-thumb:hover {
  background: #909399;
}

/* 页面内容区通用间距 */
.page-content {
  padding: 4px 0;
}

/* 统计数字卡片 */
.stat-card-value {
  font-size: 36px;
  font-weight: 700;
  line-height: 1.2;
  margin: 8px 0 4px;
}

/* 渐入动画 */
@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(12px); }
  to   { opacity: 1; transform: translateY(0); }
}
.fade-in-up {
  animation: fadeInUp 0.4s ease-out;
}

/* ===== 页面标题区域 ===== */
.page-header {
  margin-bottom: 20px;
}
.page-header .subtitle {
  font-size: 13px;
  color: var(--text-secondary);
  margin-top: 4px;
}

/* ===== 统计卡片网格 ===== */
.stat-cards-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}
@media (max-width: 1400px) {
  .stat-cards-row { grid-template-columns: repeat(3, 1fr); }
}
@media (max-width: 1000px) {
  .stat-cards-row { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 600px) {
  .stat-cards-row { grid-template-columns: 1fr; }
}

/* ===== 筛选工具栏 ===== */
.filter-toolbar {
  background: var(--bg-card);
  padding: 16px 20px;
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-card);
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

/* ===== 档期标签容器 ===== */
.schedule-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

/* ===== 空状态容器 ===== */
.empty-state-wrapper {
  padding: 48px 0;
  display: flex;
  justify-content: center;
}

/* ===== 表格容器增强 ===== */
.table-container {
  background: var(--bg-card);
  border-radius: var(--radius-md);
  padding: 4px;
  box-shadow: var(--shadow-card);
}
</style>
