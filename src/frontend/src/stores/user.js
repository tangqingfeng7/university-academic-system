import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getCurrentUser, logout as logoutApi, changePassword as changePasswordApi } from '@/api/auth'
import { ElMessage } from 'element-plus'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || 'null'))
  const permissions = ref([])

  // 设置token
  const setToken = (newToken) => {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  // 设置用户信息
  const setUserInfo = (info) => {
    userInfo.value = info
    localStorage.setItem('userInfo', JSON.stringify(info))
  }

  // 设置权限
  const setPermissions = (perms) => {
    permissions.value = perms
  }

  // 获取用户信息
  const getUserInfo = async () => {
    try {
      const res = await getCurrentUser()
      const userInfo = {
        userId: res.data.userId,
        username: res.data.username,
        role: res.data.role,
        name: res.data.name,
        firstLogin: res.data.firstLogin
      }
      setUserInfo(userInfo)
      return userInfo
    } catch (error) {
      console.error('获取用户信息失败:', error)
      throw error
    }
  }

  // 登出
  const logout = async () => {
    try {
      // 调用登出API
      await logoutApi()
    } catch (error) {
      console.error('登出请求失败:', error)
    } finally {
      // 无论API调用成功与否，都清除本地数据
      token.value = ''
      userInfo.value = null
      permissions.value = []
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      
      ElMessage.success('已退出登录')
    }
  }

  // 修改密码
  const changePassword = async (data) => {
    try {
      await changePasswordApi(data)
      return true
    } catch (error) {
      console.error('修改密码失败:', error)
      throw error
    }
  }

  return {
    token,
    userInfo,
    permissions,
    setToken,
    setUserInfo,
    setPermissions,
    getUserInfo,
    logout,
    changePassword
  }
})

