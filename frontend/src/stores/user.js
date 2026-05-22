import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const role = ref(localStorage.getItem('role') || '')
  const userId = ref(localStorage.getItem('userId') || '')
  const username = ref(localStorage.getItem('username') || '')
  const realName = ref(localStorage.getItem('realName') || '')

  function setLoginData(data) {
    token.value = data.token
    role.value = data.role
    userId.value = data.userId
    username.value = data.username
    realName.value = data.realName || ''
    localStorage.setItem('token', data.token)
    localStorage.setItem('role', data.role)
    localStorage.setItem('userId', data.userId)
    localStorage.setItem('username', data.username)
    localStorage.setItem('realName', data.realName || '')
  }

  function logout() {
    token.value = ''
    role.value = ''
    userId.value = ''
    username.value = ''
    realName.value = ''
    localStorage.clear()
  }

  return { token, role, userId, username, realName, setLoginData, logout }
})
