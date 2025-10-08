<template>
  <div class="login-container">
    <!-- 背景装饰 - 极简 -->
    <div class="background-decoration">
      <div class="gradient-bg"></div>
    </div>

    <!-- 登录卡片 - Apple风格 -->
    <div class="login-box animate-scale-in">
      <div class="login-header">
        <div class="logo-wrapper">
          <div class="logo-icon">
            <el-icon :size="40"><School /></el-icon>
          </div>
        </div>
        <h2 class="login-title">教务管理系统</h2>
        <p class="login-subtitle">Academic Management System</p>
      </div>

      <el-form ref="loginFormRef" :model="loginForm" :rules="rules" class="login-form">
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="用户名"
            size="large"
            class="modern-input"
          >
            <template #prefix>
              <el-icon class="input-icon"><User /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="密码"
            size="large"
            class="modern-input"
            show-password
            @keyup.enter="handleLogin"
          >
            <template #prefix>
              <el-icon class="input-icon"><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-button 
            type="primary" 
            size="large" 
            :loading="loading" 
            class="login-button" 
            @click="handleLogin"
          >
            {{ loading ? '登录中...' : '登录' }}
          </el-button>
        </el-form-item>
      </el-form>

      <div class="login-footer">
        <div class="tips">
          <el-icon><InfoFilled /></el-icon>
          <span>首次登录请使用初始密码，登录后需要修改密码</span>
        </div>
      </div>
    </div>

    <!-- 修改密码对话框 -->
    <ChangePassword ref="changePasswordRef" @success="handlePasswordChanged" />
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { School, User, Lock, InfoFilled } from '@element-plus/icons-vue'
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

      // 存储token
      userStore.setToken(res.data.accessToken)
      
      // 存储用户信息
      const userInfo = {
        userId: res.data.userId,
        username: res.data.username,
        role: res.data.role,
        name: res.data.name,
        firstLogin: res.data.firstLogin
      }
      userStore.setUserInfo(userInfo)

      // 检查是否首次登录
      if (res.data.firstLogin) {
        ElMessage.warning('首次登录，请修改密码')
        changePasswordRef.value?.open(true)
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
  const updatedUserInfo = { ...userStore.userInfo, firstLogin: false }
  userStore.setUserInfo(updatedUserInfo)

  ElMessage.success('密码修改成功，正在跳转...')

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
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  min-height: 100vh;
  background: var(--bg-secondary);
  overflow: hidden;
}

// ===================================
// 背景装饰 - 极简
// ===================================

.background-decoration {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  overflow: hidden;
  z-index: 0;
}

.gradient-bg {
  position: absolute;
  top: -50%;
  right: -30%;
  width: 100%;
  height: 150%;
  background: radial-gradient(circle at center, rgba(0, 122, 255, 0.08) 0%, transparent 60%);
  transform: rotate(-15deg);
}

// ===================================
// 登录卡片 - Apple 风格
// ===================================

.login-box {
  position: relative;
  width: 420px;
  padding: 48px 40px;
  background: var(--bg-primary);
  border-radius: var(--radius-2xl);
  box-shadow: var(--shadow-2xl);
  z-index: 1;
  border: 1px solid var(--border-light);
}

// ===================================
// 登录头部
// ===================================

.login-header {
  text-align: center;
  margin-bottom: 40px;
}

.logo-wrapper {
  display: flex;
  justify-content: center;
  margin-bottom: 24px;
}

.logo-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 72px;
  height: 72px;
  background: var(--primary-color);
  border-radius: var(--radius-xl);
  color: white;
  
  .el-icon {
    font-size: 40px;
  }
}

.login-title {
  font-size: 28px;
  font-weight: 600;
  margin: 0 0 8px;
  color: var(--text-primary);
  letter-spacing: -0.02em;
}

.login-subtitle {
  font-size: 14px;
  color: var(--text-secondary);
  font-weight: 400;
  letter-spacing: 0.02em;
  margin: 0;
}

// ===================================
// 表单样式
// ===================================

.login-form {
  .el-form-item {
    margin-bottom: 20px;
  }
}

:deep(.modern-input) {
  .el-input__wrapper {
    padding: 14px 16px;
    background: var(--bg-secondary);
    border: 1px solid transparent;
    box-shadow: none;
    transition: all var(--transition-base);
    border-radius: var(--radius-md);
    
    &:hover {
      background: var(--bg-tertiary);
    }
    
    &.is-focus {
      background: var(--bg-primary);
      border-color: var(--primary-color);
      box-shadow: 0 0 0 4px rgba(0, 122, 255, 0.1);
    }
  }
  
  .el-input__inner {
    font-size: 15px;
    color: var(--text-primary);
    
    &::placeholder {
      color: var(--text-tertiary);
      font-weight: 400;
    }
  }
}

.input-icon {
  color: var(--text-secondary);
  font-size: 18px;
}

// ===================================
// 登录按钮
// ===================================

.login-button {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 500;
  border: none;
  margin-top: 12px;
  background: var(--primary-color);
  border-radius: var(--radius-md);
  transition: all var(--transition-base);
  letter-spacing: -0.01em;
  
  &:hover {
    background: var(--primary-dark);
  }
  
  &:active {
    transform: scale(0.98);
  }
}

// ===================================
// 登录页脚
// ===================================

.login-footer {
  margin-top: 28px;
}

.tips {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 14px 16px;
  background: var(--bg-secondary);
  border-radius: var(--radius-md);
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.6;
  
  .el-icon {
    color: var(--primary-color);
    font-size: 16px;
    flex-shrink: 0;
    margin-top: 1px;
  }
  
  span {
    flex: 1;
  }
}

// ===================================
// 响应式设计
// ===================================

@media (max-width: 768px) {
  .login-box {
    width: 90%;
    max-width: 400px;
    padding: 40px 28px;
  }
  
  .login-title {
    font-size: 24px;
  }
  
  .logo-icon {
    width: 64px;
    height: 64px;
    
    .el-icon {
      font-size: 36px;
    }
  }
}

// ===================================
// 动画
// ===================================

.animate-scale-in {
  animation: scaleIn 0.5s cubic-bezier(0.16, 1, 0.3, 1);
}

@keyframes scaleIn {
  from {
    opacity: 0;
    transform: scale(0.95);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}
</style>
