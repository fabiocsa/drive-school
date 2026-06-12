import axios from 'axios'
import { ElMessage } from 'element-plus'

const api = axios.create({
  baseURL: '/api',
  timeout: 15000
})

api.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

api.interceptors.response.use(
  response => {
    const data = response.data
    if (data.code && data.code !== 200) {
      ElMessage.error(data.message || '操作失败')
      return Promise.reject(new Error(data.message))
    }
    return data
  },
  error => {
    if (error.response && error.response.status === 403) {
      ElMessage.error('无权限访问')
    } else {
      ElMessage.error(error.message || '网络错误')
    }
    return Promise.reject(error)
  }
)

export function authApi() {
  return {
    login: (data) => api.post('/auth/login', data),
    register: (data) => api.post('/auth/register', data)
  }
}

export function studentApi() {
  return {
    getMyInfo: () => api.get('/student/myinfo'),
    submitRegistration: (data) => api.post('/student/register', data),
    uploadFile: (file) => {
      const formData = new FormData()
      formData.append('file', file)
      return api.post('/student/upload', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      })
    },
    getTrainings: () => api.get('/student/trainings'),
    createAppointment: (data) => api.post('/student/appointment', data),
    cancelAppointment: (id, data) => api.put(`/student/appointment/cancel/${id}`, data),
    getMyAppointments: () => api.get('/student/appointments'),
    registerExam: (data) => api.post('/student/exam/register', data),
    getMyExams: () => api.get('/student/exams'),
    getPdfList: (studentId) => api.get(`/student/pdf/${studentId}`),
    downloadPdf: (studentId, pdfType) => api.get(`/student/pdf/download/${studentId}/${pdfType}`, { responseType: 'blob' }),
    /** 查询教练在指定日期的可用时间槽（上午/下午分组） */
    getCoachSlots: (coachId, date) => api.get(`/student/coach/${coachId}/slots`, { params: { date } }),
    /** 查询教练在日期范围内有可用槽位的日期列表 */
    getCoachAvailableDates: (coachId, start, end) => api.get(`/student/coach/${coachId}/available-dates`, { params: { start, end } })
  }
}

export function coachApi() {
  return {
    getMyInfo: () => api.get('/coach/myinfo'),
    recordTraining: (data) => api.post('/coach/training/record', data),
    getStudentTrainings: (studentId) => api.get('/coach/students/trainings', { params: { studentId } }),
    getAppointments: () => api.get('/coach/appointments'),
    confirmAppointment: (id) => api.put(`/coach/appointment/confirm/${id}`),
    adjustPhase: (studentId, data) => api.put(`/coach/phase/${studentId}`, data),
    /** 更新教练空闲档期（含容量） */
    updateSchedule: (scheduleJson) => api.put('/coach/schedule', { scheduleJson }),
    /** 模糊搜索学员 */
    searchStudents: (keyword) => api.get('/coach/students/search', { params: { keyword } }),
    /** 获取学员详细信息摘要 */
    getStudentSummary: (studentInfoId) => api.get(`/coach/students/${studentInfoId}/summary`)
  }
}

export function commonApi() {
  return {
    listVehicleTypes: () => api.get('/common/vehicle-types'),
    listSubjects: () => api.get('/common/subjects'),
    listExamLocations: () => api.get('/common/exam-locations'),
    getCoachStudents: () => api.get('/common/coaches/students')
  }
}

export function adminApi() {
  return {
    listStudents: () => api.get('/admin/students'),
    auditStudent: (id, data) => api.put(`/admin/students/audit/${id}`, data),
    batchAudit: (data) => api.put('/admin/students/batch-audit', data),
    recommendCoaches: (id) => api.get(`/admin/students/${id}/recommend-coaches`),
    assignCoach: (id, data) => api.put(`/admin/students/${id}/assign-coach`, data),

    listCoaches: () => api.get('/admin/coaches'),
    addCoach: (data) => api.post('/admin/coaches', data),
    updateCoach: (id, data) => api.put(`/admin/coaches/${id}`, data),
    deleteCoach: (id) => api.delete(`/admin/coaches/${id}`),

    listVehicleTypes: () => api.get('/admin/vehicle-types'),
    addVehicleType: (data) => api.post('/admin/vehicle-types', data),
    updateVehicleType: (id, data) => api.put(`/admin/vehicle-types/${id}`, data),
    deleteVehicleType: (id) => api.delete(`/admin/vehicle-types/${id}`),

    listSubjects: () => api.get('/admin/subjects'),
    addSubject: (data) => api.post('/admin/subjects', data),
    updateSubject: (id, data) => api.put(`/admin/subjects/${id}`, data),
    deleteSubject: (id) => api.delete(`/admin/subjects/${id}`),

    listExamLocations: () => api.get('/admin/exam-locations'),
    addExamLocation: (data) => api.post('/admin/exam-locations', data),
    updateExamLocation: (id, data) => api.put(`/admin/exam-locations/${id}`, data),
    deleteExamLocation: (id) => api.delete(`/admin/exam-locations/${id}`),

    listFeeStandards: () => api.get('/admin/fee-standards'),
    addFeeStandard: (data) => api.post('/admin/fee-standards', data),
    updateFeeStandard: (id, data) => api.put(`/admin/fee-standards/${id}`, data),
    deleteFeeStandard: (id) => api.delete(`/admin/fee-standards/${id}`),

    listExams: () => api.get('/admin/exams'),
    auditExam: (id, data) => api.put(`/admin/exams/audit/${id}`, data),
    recordScore: (id, data) => api.put(`/admin/exams/score/${id}`, data),
    waitingCertStudents: () => api.get('/admin/waiting-cert'),
    issueCert: (id) => api.put(`/admin/issue-cert/${id}`),

    getRegistrationStats: (year, month) => api.get('/admin/statistics/registration', { params: { year, month } }),
    getPassRateStats: () => api.get('/admin/statistics/pass-rate'),
    getCoachWorkloadStats: () => api.get('/admin/statistics/coach-workload'),
    getAllStats: (year) => api.get('/admin/statistics', { params: { year } })
  }
}

export default api
