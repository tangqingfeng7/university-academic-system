<template>
  <el-dialog
    v-model="dialogVisible"
    :title="mode === 'add' ? '添加教师' : '编辑教师'"
    width="600px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="rules"
      label-width="100px"
    >
      <el-form-item label="工号" prop="teacherNo">
        <el-input 
          v-model="formData.teacherNo" 
          placeholder="请输入工号"
          :disabled="mode === 'edit'"
        />
      </el-form-item>

      <el-form-item label="姓名" prop="name">
        <el-input v-model="formData.name" placeholder="请输入姓名" />
      </el-form-item>

      <el-form-item label="性别" prop="gender">
        <el-radio-group v-model="formData.gender">
          <el-radio label="MALE">男</el-radio>
          <el-radio label="FEMALE">女</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="职称" prop="title">
        <el-select
          v-model="formData.title"
          placeholder="请选择职称"
          style="width: 100%"
        >
          <el-option label="教授" value="教授" />
          <el-option label="副教授" value="副教授" />
          <el-option label="讲师" value="讲师" />
          <el-option label="助教" value="助教" />
        </el-select>
      </el-form-item>

      <el-form-item label="所属院系" prop="departmentId">
        <el-select
          v-model="formData.departmentId"
          placeholder="请选择院系"
          style="width: 100%"
        >
          <el-option
            v-for="dept in departmentList"
            :key="dept.id"
            :label="dept.name"
            :value="dept.id"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="联系电话" prop="phone">
        <el-input v-model="formData.phone" placeholder="请输入联系电话" />
      </el-form-item>

      <el-form-item label="邮箱" prop="email">
        <el-input v-model="formData.email" placeholder="请输入邮箱" />
      </el-form-item>

      <!-- 只在添加时显示用户名和密码 -->
      <template v-if="mode === 'add'">
        <el-form-item label="用户名" prop="username">
          <el-input 
            v-model="formData.username" 
            placeholder="默认与工号相同，可自定义"
          />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input 
            v-model="formData.password" 
            type="password"
            placeholder="请输入密码"
            show-password
          />
        </el-form-item>
      </template>
    </el-form>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
        确定
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  getTeacherById, 
  createTeacher, 
  updateTeacher,
  checkTeacherNo 
} from '@/api/teacher'
import { getAllDepartments } from '@/api/system'

const props = defineProps({
  modelValue: {
    type: Boolean,
    required: true
  },
  teacherId: {
    type: Number,
    default: null
  },
  mode: {
    type: String,
    default: 'add', // add | edit
    validator: (value) => ['add', 'edit'].includes(value)
  }
})

const emit = defineEmits(['update:modelValue', 'success'])

const dialogVisible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const formRef = ref(null)
const submitLoading = ref(false)

const formData = reactive({
  teacherNo: '',
  name: '',
  gender: 'MALE',
  title: '',
  departmentId: null,
  phone: '',
  email: '',
  username: '',
  password: ''
})

// 院系列表
const departmentList = ref([])

// 验证工号是否存在
const validateTeacherNo = async (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入工号'))
    return
  }
  
  if (props.mode === 'edit') {
    callback()
    return
  }
  
  try {
    const res = await checkTeacherNo(value)
    if (res.data.exists) {
      callback(new Error('工号已存在'))
    } else {
      callback()
    }
  } catch (error) {
    callback()
  }
}

// 验证邮箱格式
const validateEmail = (rule, value, callback) => {
  if (!value) {
    callback()
    return
  }
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailRegex.test(value)) {
    callback(new Error('请输入正确的邮箱地址'))
  } else {
    callback()
  }
}

// 表单验证规则
const rules = {
  teacherNo: [
    { required: true, validator: validateTeacherNo, trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { min: 2, max: 50, message: '姓名长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  gender: [
    { required: true, message: '请选择性别', trigger: 'change' }
  ],
  title: [
    { required: true, message: '请选择职称', trigger: 'change' }
  ],
  departmentId: [
    { required: true, message: '请选择院系', trigger: 'change' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  email: [
    { validator: validateEmail, trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ]
}

// 获取院系列表
const fetchDepartmentList = async () => {
  try {
    const res = await getAllDepartments()
    departmentList.value = res.data
  } catch (error) {
    console.error('获取院系列表失败:', error)
  }
}

// 获取教师详情
const fetchTeacherDetail = async (id) => {
  try {
    const res = await getTeacherById(id)
    const teacher = res.data
    Object.assign(formData, {
      teacherNo: teacher.teacherNo,
      name: teacher.name,
      gender: teacher.gender,
      title: teacher.title,
      departmentId: teacher.departmentId,
      phone: teacher.phone,
      email: teacher.email
    })
  } catch (error) {
    console.error('获取教师详情失败:', error)
    ElMessage.error('获取教师详情失败')
  }
}

// 重置表单
const resetForm = () => {
  formData.teacherNo = ''
  formData.name = ''
  formData.gender = 'MALE'
  formData.title = ''
  formData.departmentId = null
  formData.phone = ''
  formData.email = ''
  formData.username = ''
  formData.password = ''
  formRef.value?.clearValidate()
}

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    
    submitLoading.value = true
    
    const data = { ...formData }
    
    if (props.mode === 'add') {
      // 如果用户名为空，使用工号作为用户名
      if (!data.username) {
        data.username = data.teacherNo
      }
      await createTeacher(data)
      ElMessage.success('添加成功')
    } else {
      // 编辑时不需要username和password
      delete data.username
      delete data.password
      await updateTeacher(props.teacherId, data)
      ElMessage.success('更新成功')
    }
    
    emit('success')
    handleClose()
  } catch (error) {
    if (error !== false) { // 验证失败时error为false
      console.error('提交失败:', error)
      ElMessage.error('操作失败')
    }
  } finally {
    submitLoading.value = false
  }
}

// 关闭对话框
const handleClose = () => {
  resetForm()
  dialogVisible.value = false
}

// 监听对话框打开
watch(() => props.modelValue, async (val) => {
  if (val) {
    await fetchDepartmentList()
    
    if (props.mode === 'edit' && props.teacherId) {
      await fetchTeacherDetail(props.teacherId)
    } else {
      resetForm()
    }
  }
})
</script>

<style scoped lang="scss">
:deep(.el-input-number) {
  width: 100%;
}
</style>

