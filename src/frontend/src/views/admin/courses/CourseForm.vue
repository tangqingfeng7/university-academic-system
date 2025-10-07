<template>
  <el-dialog
    v-model="dialogVisible"
    :title="mode === 'add' ? '添加课程' : '编辑课程'"
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
      <el-form-item label="课程编号" prop="courseNo">
        <el-input 
          v-model="formData.courseNo" 
          placeholder="请输入课程编号"
          :disabled="mode === 'edit'"
        />
      </el-form-item>

      <el-form-item label="课程名称" prop="name">
        <el-input v-model="formData.name" placeholder="请输入课程名称" />
      </el-form-item>

      <el-form-item label="学分" prop="credits">
        <el-input-number
          v-model="formData.credits"
          :min="1"
          :max="10"
          placeholder="请输入学分"
          style="width: 100%"
        />
      </el-form-item>

      <el-form-item label="学时" prop="hours">
        <el-input-number
          v-model="formData.hours"
          :min="8"
          :max="200"
          :step="8"
          placeholder="请输入学时"
          style="width: 100%"
        />
      </el-form-item>

      <el-form-item label="课程类型" prop="type">
        <el-select
          v-model="formData.type"
          placeholder="请选择课程类型"
          style="width: 100%"
        >
          <el-option label="必修课" value="REQUIRED" />
          <el-option label="选修课" value="ELECTIVE" />
          <el-option label="公共课" value="PUBLIC" />
        </el-select>
      </el-form-item>

      <el-form-item label="开课院系" prop="departmentId">
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

      <el-form-item label="课程简介" prop="description">
        <el-input
          v-model="formData.description"
          type="textarea"
          :rows="4"
          placeholder="请输入课程简介"
        />
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
  getCourseById, 
  createCourse, 
  updateCourse,
  checkCourseNo 
} from '@/api/course'
import { getAllDepartments } from '@/api/system'

const props = defineProps({
  modelValue: {
    type: Boolean,
    required: true
  },
  courseId: {
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
  courseNo: '',
  name: '',
  credits: 3,
  hours: 48,
  type: '',
  departmentId: null,
  description: ''
})

// 院系列表
const departmentList = ref([])

// 验证课程编号是否存在
const validateCourseNo = async (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入课程编号'))
    return
  }
  
  if (props.mode === 'edit') {
    callback()
    return
  }
  
  try {
    const res = await checkCourseNo(value)
    if (res.data.exists) {
      callback(new Error('课程编号已存在'))
    } else {
      callback()
    }
  } catch (error) {
    callback()
  }
}

// 表单验证规则
const rules = {
  courseNo: [
    { required: true, validator: validateCourseNo, trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入课程名称', trigger: 'blur' },
    { min: 2, max: 100, message: '课程名称长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  credits: [
    { required: true, message: '请输入学分', trigger: 'blur' },
    { type: 'number', min: 1, max: 10, message: '学分在1-10之间', trigger: 'blur' }
  ],
  hours: [
    { required: true, message: '请输入学时', trigger: 'blur' },
    { type: 'number', min: 8, max: 200, message: '学时在8-200之间', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择课程类型', trigger: 'change' }
  ],
  departmentId: [
    { required: true, message: '请选择院系', trigger: 'change' }
  ],
  description: [
    { max: 500, message: '课程简介不超过500个字符', trigger: 'blur' }
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

// 获取课程详情
const fetchCourseDetail = async (id) => {
  try {
    const res = await getCourseById(id)
    const course = res.data
    Object.assign(formData, {
      courseNo: course.courseNo,
      name: course.name,
      credits: course.credits,
      hours: course.hours,
      type: course.type,
      departmentId: course.departmentId,
      description: course.description
    })
  } catch (error) {
    console.error('获取课程详情失败:', error)
    ElMessage.error('获取课程详情失败')
  }
}

// 重置表单
const resetForm = () => {
  formData.courseNo = ''
  formData.name = ''
  formData.credits = 3
  formData.hours = 48
  formData.type = ''
  formData.departmentId = null
  formData.description = ''
  formRef.value?.clearValidate()
}

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    
    submitLoading.value = true
    
    const data = { ...formData }
    
    if (props.mode === 'add') {
      await createCourse(data)
      ElMessage.success('添加成功')
    } else {
      await updateCourse(props.courseId, data)
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
    
    if (props.mode === 'edit' && props.courseId) {
      await fetchCourseDetail(props.courseId)
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

