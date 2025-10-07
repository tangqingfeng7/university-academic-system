<template>
  <el-dialog
    v-model="visible"
    :title="isAdmin ? '发布系统通知' : '发布课程通知'"
    width="600px"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="rules"
      label-width="100px"
    >
      <el-form-item label="通知标题" prop="title">
        <el-input
          v-model="formData.title"
          placeholder="请输入通知标题"
          maxlength="200"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="通知类型" prop="type" v-if="isAdmin">
        <el-select v-model="formData.type" placeholder="请选择通知类型" style="width: 100%">
          <el-option label="系统通知" value="SYSTEM" />
          <el-option label="课程通知" value="COURSE" />
          <el-option label="成绩通知" value="GRADE" />
        </el-select>
      </el-form-item>

      <el-form-item label="目标角色" prop="targetRole" v-if="isAdmin">
        <el-select v-model="formData.targetRole" placeholder="请选择目标角色" style="width: 100%">
          <el-option label="全部" value="ALL" />
          <el-option label="学生" value="STUDENT" />
          <el-option label="教师" value="TEACHER" />
          <el-option label="管理员" value="ADMIN" />
        </el-select>
      </el-form-item>

      <el-form-item label="通知内容" prop="content">
        <el-input
          v-model="formData.content"
          type="textarea"
          :rows="10"
          placeholder="请输入通知内容"
          maxlength="2000"
          show-word-limit
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="submitting">
        发布
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { 
  publishAdminNotification, 
  publishTeacherNotification 
} from '@/api/notification'

// Props
const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  }
})

// Emits
const emit = defineEmits(['update:modelValue', 'success'])

// Store
const userStore = useUserStore()

// 数据
const formRef = ref(null)
const submitting = ref(false)

// 表单数据
const formData = ref({
  title: '',
  type: 'SYSTEM',
  targetRole: 'ALL',
  content: ''
})

// 验证规则
const rules = {
  title: [
    { required: true, message: '请输入通知标题', trigger: 'blur' },
    { min: 2, max: 200, message: '标题长度在 2 到 200 个字符', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择通知类型', trigger: 'change' }
  ],
  targetRole: [
    { required: true, message: '请选择目标角色', trigger: 'change' }
  ],
  content: [
    { required: true, message: '请输入通知内容', trigger: 'blur' },
    { min: 5, max: 2000, message: '内容长度在 5 到 2000 个字符', trigger: 'blur' }
  ]
}

// 计算属性
const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const isAdmin = computed(() => {
  return userStore.userInfo?.role === 'ADMIN'
})

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      // 根据角色调用不同的API
      if (isAdmin.value) {
        await publishAdminNotification(formData.value)
      } else {
        // 教师发布时，设置默认值
        const data = {
          title: formData.value.title,
          content: formData.value.content,
          type: 'COURSE',
          targetRole: 'STUDENT'
        }
        await publishTeacherNotification(data)
      }
      
      ElMessage.success('通知发布成功')
      visible.value = false
      emit('success')
      resetForm()
    } catch (error) {
      console.error('发布通知失败:', error)
      ElMessage.error('发布通知失败: ' + (error.message || '未知错误'))
    } finally {
      submitting.value = false
    }
  })
}

// 关闭对话框
const handleClose = () => {
  resetForm()
}

// 重置表单
const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  formData.value = {
    title: '',
    type: 'SYSTEM',
    targetRole: 'ALL',
    content: ''
  }
}
</script>

<style scoped>
:deep(.el-textarea__inner) {
  font-family: inherit;
}
</style>

