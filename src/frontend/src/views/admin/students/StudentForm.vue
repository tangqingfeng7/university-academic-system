<template>
  <el-dialog
    v-model="dialogVisible"
    :title="mode === 'add' ? '添加学生' : '编辑学生'"
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
      <el-form-item label="学号" prop="studentNo">
        <el-input 
          v-model="formData.studentNo" 
          placeholder="请输入学号"
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

      <el-form-item label="出生日期" prop="birthDate">
        <el-date-picker
          v-model="formData.birthDate"
          type="date"
          placeholder="请选择出生日期"
          value-format="YYYY-MM-DD"
          style="width: 100%"
        />
      </el-form-item>

      <el-form-item label="入学年份" prop="enrollmentYear">
        <el-input-number
          v-model="formData.enrollmentYear"
          :min="1990"
          :max="new Date().getFullYear()"
          placeholder="请输入入学年份"
          style="width: 100%"
        />
      </el-form-item>

      <el-form-item label="院系" prop="departmentId">
        <el-select
          v-model="formData.departmentId"
          placeholder="请选择院系"
          style="width: 100%"
          @change="handleDepartmentChange"
        >
          <el-option
            v-for="dept in departmentList"
            :key="dept.id"
            :label="dept.name"
            :value="dept.id"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="专业" prop="majorId">
        <el-select
          v-model="formData.majorId"
          placeholder="请选择专业"
          style="width: 100%"
          :disabled="!formData.departmentId"
        >
          <el-option
            v-for="major in filteredMajorList"
            :key="major.id"
            :label="major.name"
            :value="major.id"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="班级" prop="className">
        <el-input v-model="formData.className" placeholder="请输入班级" />
      </el-form-item>

      <el-form-item label="联系电话" prop="phone">
        <el-input v-model="formData.phone" placeholder="请输入联系电话" />
      </el-form-item>
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
  getStudentById, 
  createStudent, 
  updateStudent,
  checkStudentNo 
} from '@/api/student'
import { getAllDepartments, getAllMajors } from '@/api/system'

const props = defineProps({
  modelValue: {
    type: Boolean,
    required: true
  },
  studentId: {
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
  studentNo: '',
  name: '',
  gender: 'MALE',
  birthDate: '',
  enrollmentYear: new Date().getFullYear(),
  departmentId: null,
  majorId: null,
  className: '',
  phone: ''
})

// 院系和专业列表
const departmentList = ref([])
const majorList = ref([])

// 根据选中的院系过滤专业
const filteredMajorList = computed(() => {
  if (!formData.departmentId) return []
  return majorList.value.filter(major => major.departmentId === formData.departmentId)
})

// 验证学号是否存在
const validateStudentNo = async (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入学号'))
    return
  }
  
  if (props.mode === 'edit') {
    callback()
    return
  }
  
  try {
    const res = await checkStudentNo(value)
    if (res.data) {
      callback(new Error('学号已存在'))
    } else {
      callback()
    }
  } catch (error) {
    callback()
  }
}

// 表单验证规则
const rules = {
  studentNo: [
    { required: true, validator: validateStudentNo, trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { min: 2, max: 50, message: '姓名长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  gender: [
    { required: true, message: '请选择性别', trigger: 'change' }
  ],
  birthDate: [
    { required: true, message: '请选择出生日期', trigger: 'change' }
  ],
  enrollmentYear: [
    { required: true, message: '请输入入学年份', trigger: 'blur' },
    { type: 'number', min: 1990, max: new Date().getFullYear(), message: '入学年份不合法', trigger: 'blur' }
  ],
  departmentId: [
    { required: true, message: '请选择院系', trigger: 'change' }
  ],
  majorId: [
    { required: true, message: '请选择专业', trigger: 'change' }
  ],
  className: [
    { required: true, message: '请输入班级', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
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

// 获取专业列表
const fetchMajorList = async () => {
  try {
    const res = await getAllMajors()
    majorList.value = res.data
  } catch (error) {
    console.error('获取专业列表失败:', error)
  }
}

// 院系改变时清空专业
const handleDepartmentChange = () => {
  formData.majorId = null
}

// 获取学生详情
const fetchStudentDetail = async (id) => {
  try {
    const res = await getStudentById(id)
    const student = res.data
    Object.assign(formData, {
      studentNo: student.studentNo,
      name: student.name,
      gender: student.gender,
      birthDate: student.birthDate,
      enrollmentYear: student.enrollmentYear,
      departmentId: student.departmentId,
      majorId: student.majorId,
      className: student.className,
      phone: student.phone
    })
  } catch (error) {
    console.error('获取学生详情失败:', error)
    ElMessage.error('获取学生详情失败')
  }
}

// 重置表单
const resetForm = () => {
  formData.studentNo = ''
  formData.name = ''
  formData.gender = 'MALE'
  formData.birthDate = ''
  formData.enrollmentYear = new Date().getFullYear()
  formData.departmentId = null
  formData.majorId = null
  formData.className = ''
  formData.phone = ''
  formRef.value?.clearValidate()
}

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    
    submitLoading.value = true
    
    const data = { ...formData }
    
    if (props.mode === 'add') {
      await createStudent(data)
      ElMessage.success('添加成功')
    } else {
      await updateStudent(props.studentId, data)
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
    await fetchMajorList()
    
    if (props.mode === 'edit' && props.studentId) {
      await fetchStudentDetail(props.studentId)
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

