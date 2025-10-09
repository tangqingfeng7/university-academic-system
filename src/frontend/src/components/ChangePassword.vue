<template>
  <el-dialog
    v-model="dialogVisible"
    :title="isForced ? '首次登录，请修改密码' : '修改密码'"
    :close-on-click-modal="!isForced"
    :close-on-press-escape="!isForced"
    :show-close="!isForced"
    width="500px"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="100px"
    >
      <el-form-item label="原密码" prop="oldPassword">
        <el-input
          v-model="form.oldPassword"
          type="password"
          placeholder="请输入原密码"
          show-password
          autocomplete="off"
        />
      </el-form-item>
      <el-form-item label="新密码" prop="newPassword">
        <el-input
          v-model="form.newPassword"
          type="password"
          placeholder="请输入新密码（至少6位）"
          show-password
          autocomplete="off"
        />
      </el-form-item>
      <el-form-item label="确认密码" prop="confirmPassword">
        <el-input
          v-model="form.confirmPassword"
          type="password"
          placeholder="请再次输入新密码"
          show-password
          autocomplete="off"
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button v-if="!isForced" @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="loading" @click="handleSubmit">
        确定
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { changePassword } from '@/api/auth'

const emit = defineEmits(['success'])

const dialogVisible = ref(false)
const isForced = ref(false) // 是否强制修改密码
const loading = ref(false)
const formRef = ref(null)

const form = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 密码强度验证
const validatePassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入新密码'))
  } else if (value.length < 6) {
    callback(new Error('密码长度至少6位'))
  } else {
    callback()
  }
}

// 确认密码验证
const validateConfirmPassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请再次输入密码'))
  } else if (value !== form.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

// 新密码不能与原密码相同
const validateNewPassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入新密码'))
  } else if (value === form.oldPassword) {
    callback(new Error('新密码不能与原密码相同'))
  } else {
    validatePassword(rule, value, callback)
  }
}

const rules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, validator: validateNewPassword, trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

// 打开对话框
const open = (forced = false) => {
  isForced.value = forced
  dialogVisible.value = true
  // 重置表单
  formRef.value?.resetFields()
}

// 提交修改
const handleSubmit = () => {
  formRef.value?.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      await changePassword({
        oldPassword: form.oldPassword,
        newPassword: form.newPassword,
        confirmPassword: form.confirmPassword
      })

      ElMessage.success('密码修改成功')
      dialogVisible.value = false
      
      // 重置表单
      formRef.value?.resetFields()
      
      // 触发成功事件
      emit('success')

    } catch (error) {
      console.error('修改密码失败:', error)
      const errorMsg = error.response?.data?.message || '修改密码失败'
      ElMessage.error(errorMsg)
    } finally {
      loading.value = false
    }
  })
}

// 暴露方法
defineExpose({
  open
})
</script>

<style scoped lang="scss">
:deep(.el-dialog__header) {
  font-weight: bold;
}

:deep(.el-form-item__label) {
  font-weight: 500;
}
</style>

