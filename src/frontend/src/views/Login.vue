<template>
  <div class="login-container">
    <div class="login-box">
      <h2 class="login-title">大学教务管理系统</h2>
      <el-form ref="loginFormRef" :model="loginForm" :rules="rules" class="login-form">
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            prefix-icon="User"
            size="large"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            size="large"
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" :loading="loading" class="login-button" @click="handleLogin">
            登 录
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 修改密码对话框 -->
    <ChangePassword ref="changePasswordRef" @success="handlePasswordChanged" />
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { login } from '@/api/auth'
import ChangePassword from '@/components/ChangePassword.vue'

const router = useRouter()
const userStore = useUserStore()

const loginFormRef = ref(null)
const changePasswordRef = ref(null)
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ]
}

const handleLogin = () => {
  loginFormRef.value?.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      const res = await login({
        username: loginForm.username,
        password: loginForm.password
      })

      // 存储token（后端返回的是accessToken）
      userStore.setToken(res.data.accessToken)
      
      // 存储用户信息
      const userInfo = {
        userId: res.data.userId,
        username: res.data.username,
        role: res.data.role,
        firstLogin: res.data.firstLogin
      }
      userStore.setUserInfo(userInfo)

      // 检查是否首次登录
      if (res.data.firstLogin) {
        ElMessage.warning('首次登录，请修改密码')
        changePasswordRef.value?.open(true) // 强制修改密码
        return
      }

      ElMessage.success('登录成功')

      // 根据角色跳转
      const roleRoutes = {
        'ADMIN': '/admin',
        'TEACHER': '/teacher',
        'STUDENT': '/student'
      }
      router.push(roleRoutes[res.data.role] || '/login')

    } catch (error) {
      console.error('登录失败:', error)
      ElMessage.error(error.response?.data?.message || '登录失败，请重试')
    } finally {
      loading.value = false
    }
  })
}

// 密码修改成功后的回调
const handlePasswordChanged = () => {
  // 更新用户信息（不再是首次登录）
  const updatedUserInfo = { ...userStore.userInfo, firstLogin: false }
  userStore.setUserInfo(updatedUserInfo)

  ElMessage.success('密码修改成功，正在跳转...')

  // 跳转到对应的首页
  setTimeout(() => {
    const roleRoutes = {
      'ADMIN': '/admin',
      'TEACHER': '/teacher',
      'STUDENT': '/student'
    }
    router.push(roleRoutes[userStore.userInfo.role] || '/login')
  }, 500)
}
</script>

<style scoped lang="scss">
.login-container {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-box {
  width: 400px;
  padding: 40px;
  background: white;
  border-radius: 10px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.login-title {
  margin-bottom: 30px;
  font-size: 24px;
  font-weight: bold;
  text-align: center;
  color: #303133;
}

.login-form {
  .el-form-item {
    margin-bottom: 20px;
  }
}

.login-button {
  width: 100%;
}
</style>

