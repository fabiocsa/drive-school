import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/login', name: 'Login', component: () => import('../views/Login.vue') },
  { path: '/register', name: 'Register', component: () => import('../views/Register.vue') },
  {
    path: '/student',
    name: 'StudentLayout',
    component: () => import('../views/layouts/StudentLayout.vue'),
    meta: { role: 'ROLE_STUDENT' },
    children: [
      { path: '', name: 'StudentDashboard', component: () => import('../views/student/Dashboard.vue') },
      { path: 'registration', name: 'StudentRegistration', component: () => import('../views/student/Registration.vue') },
      { path: 'status', name: 'StudentStatus', component: () => import('../views/student/Status.vue') },
      { path: 'trainings', name: 'StudentTrainings', component: () => import('../views/student/Trainings.vue') },
      { path: 'appointments', name: 'StudentAppointments', component: () => import('../views/student/Appointments.vue') },
      { path: 'exams', name: 'StudentExams', component: () => import('../views/student/Exams.vue') },
      { path: 'pdf', name: 'StudentPdf', component: () => import('../views/student/PdfDownload.vue') }
    ]
  },
  {
    path: '/coach',
    name: 'CoachLayout',
    component: () => import('../views/layouts/CoachLayout.vue'),
    meta: { role: 'ROLE_COACH' },
    children: [
      { path: '', name: 'CoachDashboard', component: () => import('../views/coach/Dashboard.vue') },
      { path: 'training', name: 'CoachTraining', component: () => import('../views/coach/Training.vue') },
      { path: 'appointments', name: 'CoachAppointments', component: () => import('../views/coach/Appointments.vue') }
    ]
  },
  {
    path: '/admin',
    name: 'AdminLayout',
    component: () => import('../views/layouts/AdminLayout.vue'),
    meta: { role: 'ROLE_ADMIN' },
    children: [
      { path: '', name: 'AdminDashboard', component: () => import('../views/admin/Dashboard.vue') },
      { path: 'students', name: 'AdminStudents', component: () => import('../views/admin/Students.vue') },
      { path: 'coaches', name: 'AdminCoaches', component: () => import('../views/admin/Coaches.vue') },
      { path: 'basic', name: 'AdminBasic', component: () => import('../views/admin/BasicInfo.vue') },
      { path: 'exams', name: 'AdminExams', component: () => import('../views/admin/Exams.vue') },
      { path: 'cert', name: 'AdminCert', component: () => import('../views/admin/CertIssue.vue') },
      { path: 'statistics', name: 'AdminStatistics', component: () => import('../views/admin/Statistics.vue') }
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/login' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const role = localStorage.getItem('role')

  // 公开页面直接放行
  if (to.path === '/login' || to.path === '/register') {
    next()
    return
  }

  // 未登录重定向到登录页
  if (!token) {
    next('/login')
    return
  }

  // 角色校验：确保用户只能访问自己角色对应的路由
  const requiredRole = to.matched[0]?.meta?.role
  if (requiredRole && role !== requiredRole) {
    // 用户角色不匹配，重定向到其角色对应的首页
    const redirectMap = {
      'ROLE_ADMIN': '/admin',
      'ROLE_COACH': '/coach',
      'ROLE_STUDENT': '/student'
    }
    next(redirectMap[role] || '/login')
    return
  }

  next()
})

export default router
