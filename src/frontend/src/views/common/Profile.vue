<template>
  <div class="profile-container">
    <el-card class="info-card">
      <template #header>
        <div class="card-header">
          <el-icon><User /></el-icon>
          <span>个人信息</span>
        </div>
      </template>

      <el-descriptions :column="2" border v-if="profileData">
        <!-- 通用信息 -->
        <el-descriptions-item label="用户名">
          {{ profileData.username || userStore.userInfo.username }}
        </el-descriptions-item>
        <el-descriptions-item label="角色">
          <el-tag :type="getRoleType(profileData.role || userStore.userInfo.role)">
            {{ getRoleLabel(profileData.role || userStore.userInfo.role) }}
          </el-tag>
        </el-descriptions-item>

        <!-- 学生信息 -->
        <template v-if="isStudent">
          <el-descriptions-item label="学号">
            {{ profileData.studentNo }}
          </el-descriptions-item>
          <el-descriptions-item label="姓名">
            {{ profileData.name }}
          </el-descriptions-item>
          <el-descriptions-item label="性别">
            {{ profileData.gender === 'MALE' ? '男' : '女' }}
          </el-descriptions-item>
          <el-descriptions-item label="出生日期">
            {{ profileData.birthDate }}
          </el-descriptions-item>
          <el-descriptions-item label="入学年份">
            {{ profileData.enrollmentYear }}
          </el-descriptions-item>
          <el-descriptions-item label="院系">
            {{ profileData.departmentName }}
          </el-descriptions-item>
          <el-descriptions-item label="专业">
            {{ profileData.majorName }}
          </el-descriptions-item>
          <el-descriptions-item label="班级">
            {{ profileData.className }}
          </el-descriptions-item>
          <el-descriptions-item label="联系电话">
            <div class="editable-field">
              <span v-if="!editingPhone">{{ profileData.phone || '未设置' }}</span>
              <el-input
                v-else
                v-model="editForm.phone"
                size="small"
                style="width: 150px"
              />
              <el-button
                v-if="!editingPhone"
                type="primary"
                size="small"
                link
                @click="startEditPhone"
              >
                <el-icon><Edit /></el-icon>
                修改
              </el-button>
              <div v-else>
                <el-button type="primary" size="small" @click="savePhone">保存</el-button>
                <el-button size="small" @click="cancelEditPhone">取消</el-button>
              </div>
            </div>
          </el-descriptions-item>
        </template>

        <!-- 教师信息 -->
        <template v-if="isTeacher">
          <el-descriptions-item label="工号">
            {{ profileData.teacherNo }}
          </el-descriptions-item>
          <el-descriptions-item label="姓名">
            {{ profileData.name }}
          </el-descriptions-item>
          <el-descriptions-item label="性别">
            {{ profileData.gender === 'MALE' ? '男' : '女' }}
          </el-descriptions-item>
          <el-descriptions-item label="职称">
            {{ profileData.title }}
          </el-descriptions-item>
          <el-descriptions-item label="院系">
            {{ profileData.departmentName }}
          </el-descriptions-item>
          <el-descriptions-item label="联系电话">
            <div class="editable-field">
              <span v-if="!editingPhone">{{ profileData.phone || '未设置' }}</span>
              <el-input
                v-else
                v-model="editForm.phone"
                size="small"
                style="width: 150px"
              />
              <el-button
                v-if="!editingPhone"
                type="primary"
                size="small"
                link
                @click="startEditPhone"
              >
                <el-icon><Edit /></el-icon>
                修改
              </el-button>
              <div v-else>
                <el-button type="primary" size="small" @click="savePhone">保存</el-button>
                <el-button size="small" @click="cancelEditPhone">取消</el-button>
              </div>
            </div>
          </el-descriptions-item>
          <el-descriptions-item label="电子邮箱">
            <div class="editable-field">
              <span v-if="!editingEmail">{{ profileData.email || '未设置' }}</span>
              <el-input
                v-else
                v-model="editForm.email"
                size="small"
                style="width: 200px"
              />
              <el-button
                v-if="!editingEmail"
                type="primary"
                size="small"
                link
                @click="startEditEmail"
              >
                <el-icon><Edit /></el-icon>
                修改
              </el-button>
              <div v-else>
                <el-button type="primary" size="small" @click="saveEmail">保存</el-button>
                <el-button size="small" @click="cancelEditEmail">取消</el-button>
              </div>
            </div>
          </el-descriptions-item>
        </template>
      </el-descriptions>
    </el-card>

    <el-card class="password-card">
      <template #header>
        <div class="card-header">
          <el-icon><Lock /></el-icon>
          <span>修改密码</span>
        </div>
      </template>

      <el-form
        ref="passwordFormRef"
        :model="passwordForm"
        :rules="passwordRules"
        label-width="120px"
        style="max-width: 500px"
      >
        <el-form-item label="原密码" prop="oldPassword">
          <el-input
            v-model="passwordForm.oldPassword"
            type="password"
            placeholder="请输入原密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="passwordForm.newPassword"
            type="password"
            placeholder="请输入新密码（至少8位）"
            show-password
          />
        </el-form-item>
        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            show-password
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleChangePassword">
            <el-icon><Check /></el-icon>
            确认修改
          </el-button>
          <el-button @click="resetPasswordForm">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { User, Lock, Edit, Check, Refresh } from '@element-plus/icons-vue'
import { getProfile, updateProfile, changePassword } from '@/api/profile'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

// 个人信息数据
const profileData = ref(null)
const editingPhone = ref(false)
const editingEmail = ref(false)
const editForm = ref({
  phone: '',
  email: ''
})

// 密码表单
const passwordFormRef = ref(null)
const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 密码验证规则
const validateConfirmPassword = (rule, value, callback) => {
  if (value !== passwordForm.value.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 8, message: '密码长度至少8位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

// 计算属性
const isStudent = computed(() => {
  return userStore.userInfo?.role === 'STUDENT'
})

const isTeacher = computed(() => {
  return userStore.userInfo?.role === 'TEACHER'
})

// 获取角色标签
const getRoleLabel = (role) => {
  const roleMap = {
    'ADMIN': '管理员',
    'TEACHER': '教师',
    'STUDENT': '学生'
  }
  return roleMap[role] || role
}

// 获取角色类型
const getRoleType = (role) => {
  const typeMap = {
    'ADMIN': 'danger',
    'TEACHER': 'success',
    'STUDENT': 'primary'
  }
  return typeMap[role] || ''
}

// 加载个人信息
const loadProfile = async () => {
  try {
    const res = await getProfile()
    profileData.value = res.data
  } catch (error) {
    ElMessage.error('加载个人信息失败: ' + error.message)
  }
}

// 开始编辑电话
const startEditPhone = () => {
  editForm.value.phone = profileData.value.phone || ''
  editingPhone.value = true
}

// 取消编辑电话
const cancelEditPhone = () => {
  editingPhone.value = false
  editForm.value.phone = ''
}

// 保存电话
const savePhone = async () => {
  if (!editForm.value.phone) {
    ElMessage.warning('请输入电话号码')
    return
  }
  
  try {
    await updateProfile({ phone: editForm.value.phone })
    ElMessage.success('电话更新成功')
    profileData.value.phone = editForm.value.phone
    editingPhone.value = false
  } catch (error) {
    ElMessage.error('更新失败: ' + error.message)
  }
}

// 开始编辑邮箱
const startEditEmail = () => {
  editForm.value.email = profileData.value.email || ''
  editingEmail.value = true
}

// 取消编辑邮箱
const cancelEditEmail = () => {
  editingEmail.value = false
  editForm.value.email = ''
}

// 保存邮箱
const saveEmail = async () => {
  if (!editForm.value.email) {
    ElMessage.warning('请输入电子邮箱')
    return
  }
  
  // 简单的邮箱格式验证
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailRegex.test(editForm.value.email)) {
    ElMessage.warning('请输入有效的电子邮箱格式')
    return
  }
  
  try {
    await updateProfile({ email: editForm.value.email })
    ElMessage.success('邮箱更新成功')
    profileData.value.email = editForm.value.email
    editingEmail.value = false
  } catch (error) {
    ElMessage.error('更新失败: ' + error.message)
  }
}

// 修改密码
const handleChangePassword = async () => {
  await passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await changePassword({
          oldPassword: passwordForm.value.oldPassword,
          newPassword: passwordForm.value.newPassword,
          confirmPassword: passwordForm.value.confirmPassword
        })
        ElMessage.success('密码修改成功，请重新登录')
        
        // 清空表单
        resetPasswordForm()
        
        // 延迟跳转到登录页
        setTimeout(() => {
          userStore.logout()
        }, 1500)
      } catch (error) {
        ElMessage.error('密码修改失败: ' + error.message)
      }
    }
  })
}

// 重置密码表单
const resetPasswordForm = () => {
  passwordFormRef.value?.resetFields()
  passwordForm.value = {
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  }
}

// 页面加载
onMounted(() => {
  loadProfile()
})
</script>

<style scoped lang="scss">
.profile-container {
  padding: 20px;

  .info-card,
  .password-card {
    margin-bottom: 20px;
  }

  .card-header {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 16px;
    font-weight: bold;
  }

  .editable-field {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  :deep(.el-descriptions__body) {
    background-color: white;
  }
}
</style>

