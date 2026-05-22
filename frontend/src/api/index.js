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
    submitRegistration: (data) => api.post('/student/register', data),
    uploadFile: (file) => {
      const formData = new FormData()
      formData.append('file', file)
      return api.post('/student/upload', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      })
    },
    getMyInfo: (userId) => api.get('/student/myinfo', { params: { userId } }),
    getTrainings: (studentId) => api.get('/student/trainings', { params: { studentId } }),
    createAppointment: (data) => api.post('/student/appointment', data),
    cancelAppointment: (id, data) => api.put(`/student/appointment/cancel/${id}`, data),
    getMyAppointments: (userId) => api.get('/student/appointments', { params: { userId } }),
    registerExam: (data) => api.post('/student/exam/register', data),
    getMyExams: (userId) => api.get('/student/exams', { params: { userId } }),
    getPdfList: (studentId) => api.get(`/student/pdf/${studentId}`),
    downloadPdf: (studentId, pdfType) => api.get(`/student/pdf/download/${studentId}/${pdfType}`, { responseType: 'blob' })
  }
}

export function coachApi() {
  return {
    getMyInfo: (userId) => api.get('/coach/myinfo', { params: { userId } }),
    recordTraining: (data) => api.post('/coach/training/record', data),
    getStudentTrainings: (studentId) => api.get('/coach/students/trainings', { params: { studentId } }),
    getAppointments: (userId) => api.get('/coach/appointments', { params: { userId } }),
    confirmAppointment: (id, userId) => api.put(`/coach/appointment/confirm/${id}`, null, { params: { userId } })
  }
}

export function adminApi() {
  return {
    listStudents: () => api.get('/admin/students'),
    auditStudent: (id, data) => api.put(`/admin/students/audit/${id}`, data),
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
