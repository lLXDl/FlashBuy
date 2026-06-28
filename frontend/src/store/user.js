import { defineStore } from 'pinia'
import { login as loginApi, logout as logoutApi } from '@/api/auth'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))

  // 登录
  async function login(loginForm) {
    try {
      const data = await loginApi(loginForm)
      token.value = data.token
      userInfo.value = {
        userId: data.userId,
        username: data.username
      }
      localStorage.setItem('token', data.token)
      localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
      return data
    } catch (error) {
      throw error
    }
  }

  // 登出
  async function logout() {
    try {
      await logoutApi()
    } catch (error) {
      console.error('登出失败', error)
    } finally {
      token.value = ''
      userInfo.value = {}
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
    }
  }

  // 获取用户ID
  function getUserId() {
    return userInfo.value.userId
  }

  return {
    token,
    userInfo,
    login,
    logout,
    getUserId
  }
})