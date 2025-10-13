<template>
  <el-dialog
    v-model="visible"
    :title="title"
    width="700px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="rules"
      label-width="100px"
      label-position="right"
    >
      <!-- 步骤指示器 -->
      <el-steps
        :active="currentStep"
        finish-status="success"
        align-center
        style="margin-bottom: 30px"
      >
        <el-step title="基本信息" />
        <el-step title="学籍信息" />
        <el-step title="联系方式" />
      </el-steps>

      <!-- 步骤1：基本信息 -->
      <div v-show="currentStep === 0" class="form-step">
        <el-form-item label="学号" prop="studentNo">
          <el-input
            v-model="formData.studentNo"
            placeholder="请输入学号"
            :disabled="mode === 'edit'"
            maxlength="20"
          >
            <template #prefix>
              <el-icon><Postcard /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item label="姓名" prop="name">
          <el-input
            v-model="formData.name"
            placeholder="请输入姓名"
            maxlength="50"
          >
            <template #prefix>
              <el-icon><User /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="性别" prop="gender">
              <el-radio-group v-model="formData.gender">
                <el-radio label="MALE">男</el-radio>
                <el-radio label="FEMALE">女</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="出生日期" prop="birthDate">
              <el-date-picker
                v-model="formData.birthDate"
                type="date"
                placeholder="选择日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </div>

      <!-- 步骤2：学籍信息 -->
      <div v-show="currentStep === 1" class="form-step">
        <el-form-item label="入学年份" prop="enrollmentYear">
          <el-date-picker
            v-model="formData.enrollmentYear"
            type="year"
            placeholder="选择入学年份"
            format="YYYY"
            value-format="YYYY"
            style="width: 100%"
          >
            <template #prefix>
              <el-icon><Calendar /></el-icon>
            </template>
          </el-date-picker>
        </el-form-item>

        <el-form-item label="专业" prop="majorId">
          <el-select
            v-model="formData.majorId"
            placeholder="请选择专业"
            filterable
            style="width: 100%"
            @change="handleMajorChange"
          >
            <el-option
              v-for="major in majorList"
              :key="major.id"
              :label="`${major.name} (${major.departmentName})`"
              :value="major.id"
            >
              <div style="display: flex; justify-content: space-between">
                <span>{{ major.name }}</span>
                <span style="color: #8492a6; font-size: 13px">
                  {{ major.departmentName }}
                </span>
              </div>
            </el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="班级" prop="className">
          <el-select
            v-model="formData.className"
            placeholder="请选择班级"
            clearable
            filterable
            style="width: 100%"
          >
            <el-option
              v-for="className in classList"
              :key="className"
              :label="className"
              :value="className"
            />
          </el-select>
          <template #extra>
            <div style="color: #909399; font-size: 12px; margin-top: 4px">
              从班级管理中选择规范的班级名称
            </div>
          </template>
        </el-form-item>
      </div>

      <!-- 步骤3：联系方式 -->
      <div v-show="currentStep === 2" class="form-step">
        <el-form-item label="联系电话" prop="phone">
          <el-input
            v-model="formData.phone"
            placeholder="请输入联系电话"
            maxlength="20"
          >
            <template #prefix>
              <el-icon><Phone /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item v-if="mode === 'add'" label="用户名" prop="username">
          <el-input
            v-model="formData.username"
            placeholder="请输入登录用户名"
            maxlength="50"
          >
            <template #prefix>
              <el-icon><User /></el-icon>
            </template>
          </el-input>
          <template #extra>
            <div style="color: #909399; font-size: 12px; margin-top: 4px">
              默认与学号相同，可自定义
            </div>
          </template>
        </el-form-item>

        <el-form-item v-if="mode === 'add'" label="初始密码" prop="password">
          <el-input
            v-model="formData.password"
            type="password"
            placeholder="请输入初始密码"
            show-password
            maxlength="50"
          >
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
          <template #extra>
            <div style="color: #909399; font-size: 12px; margin-top: 4px">
              默认密码为123456，学生首次登录需修改密码
            </div>
          </template>
        </el-form-item>

        <!-- 信息确认 -->
        <el-alert
          v-if="currentStep === 2"
          title="信息确认"
          type="info"
          :closable="false"
          show-icon
          style="margin-top: 20px"
        >
          <template #default>
            <div class="confirm-info">
              <p><strong>学号：</strong>{{ formData.studentNo }}</p>
              <p><strong>姓名：</strong>{{ formData.name }}</p>
              <p><strong>性别：</strong>{{ formData.gender === 'MALE' ? '男' : '女' }}</p>
              <p><strong>专业：</strong>{{ selectedMajorName }}</p>
              <p><strong>班级：</strong>{{ formData.className || '未填写' }}</p>
            </div>
          </template>
        </el-alert>
      </div>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button v-if="currentStep > 0" @click="prevStep">
          上一步
        </el-button>
        <el-button @click="handleClose">取消</el-button>
        <el-button
          v-if="currentStep < 2"
          type="primary"
          @click="nextStep"
        >
          下一步
        </el-button>
        <el-button
          v-else
          type="primary"
          :loading="submitting"
          @click="handleSubmit"
        >
          {{ mode === 'add' ? '立即添加' : '保存修改' }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch, computed } from 'vue'
import { ElMessage } from 'element-plus'
import {
  User,
  Postcard,
  Calendar,
  School,
  Phone,
  Lock
} from '@element-plus/icons-vue'
import { getStudentById, createStudent, updateStudent } from '@/api/student'
import { getAllMajors } from '@/api/system'
import { getAllClasses } from '@/api/class'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  studentId: {
    type: Number,
    default: null
  },
  mode: {
    type: String,
    default: 'add',
    validator: (value) => ['add', 'edit'].includes(value)
  }
})

const emit = defineEmits(['update:modelValue', 'success'])

const visible = ref(props.modelValue)
const formRef = ref(null)
const submitting = ref(false)
const currentStep = ref(0)
const majorList = ref([])
const classList = ref([])

const title = computed(() => {
  return props.mode === 'add' ? '添加学生' : '编辑学生'
})

const selectedMajorName = computed(() => {
  const major = majorList.value.find(m => m.id === formData.majorId)
  return major ? major.name : ''
})

// 表单数据
const formData = reactive({
  studentNo: '',
  name: '',
  gender: 'MALE',
  birthDate: '',
  enrollmentYear: new Date().getFullYear().toString(),
  majorId: null,
  className: '',
  phone: '',
  username: '',
  password: '123456'
})

// 验证规则
const rules = {
  studentNo: [
    { required: true, message: '请输入学号', trigger: 'blur' },
    { min: 3, max: 20, message: '学号长度在3-20个字符', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { min: 2, max: 50, message: '姓名长度在2-50个字符', trigger: 'blur' }
  ],
  gender: [
    { required: true, message: '请选择性别', trigger: 'change' }
  ],
  enrollmentYear: [
    { required: true, message: '请选择入学年份', trigger: 'change' }
  ],
  majorId: [
    { required: true, message: '请选择专业', trigger: 'change' }
  ],
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ]
}

watch(() => props.modelValue, (val) => {
  visible.value = val
  if (val) {
    currentStep.value = 0
    if (props.mode === 'edit' && props.studentId) {
      loadStudentData()
    } else {
      resetForm()
    }
    loadMajorList()
    loadClassList()
  }
})

watch(visible, (val) => {
  emit('update:modelValue', val)
})

// 监听学号变化，自动填充用户名
watch(() => formData.studentNo, (val) => {
  if (props.mode === 'add' && !formData.username) {
    formData.username = val
  }
})

// 加载专业列表
const loadMajorList = async () => {
  try {
    const res = await getAllMajors()
    majorList.value = res.data || []
  } catch (error) {
    console.error('加载专业列表失败:', error)
  }
}

const loadClassList = async () => {
  try {
    const res = await getAllClasses()
    // 从班级表获取规范的班级列表，提取className
    classList.value = (res.data || []).map(item => item.className)
  } catch (error) {
    console.error('加载班级列表失败:', error)
  }
}

// 加载学生数据
const loadStudentData = async () => {
  try {
    const res = await getStudentById(props.studentId)
    const student = res.data
    Object.assign(formData, {
      studentNo: student.studentNo,
      name: student.name,
      gender: student.gender,
      birthDate: student.birthDate,
      enrollmentYear: student.enrollmentYear?.toString(),
      majorId: student.majorId,
      className: student.className,
      phone: student.phone
    })
  } catch (error) {
    console.error('加载学生数据失败:', error)
    ElMessage.error('加载学生数据失败')
  }
}

// 重置表单
const resetForm = () => {
  Object.assign(formData, {
    studentNo: '',
    name: '',
    gender: 'MALE',
    birthDate: '',
    enrollmentYear: new Date().getFullYear().toString(),
    majorId: null,
    className: '',
    phone: '',
    username: '',
    password: '123456'
  })
  formRef.value?.clearValidate()
}

// 专业变化处理
const handleMajorChange = () => {
  // 可以在这里根据专业自动填充一些信息
}

// 上一步
const prevStep = () => {
  if (currentStep.value > 0) {
    currentStep.value--
  }
}

// 下一步
const nextStep = () => {
  // 验证当前步骤
  const fieldsToValidate = getStepFields(currentStep.value)
  
  formRef.value.validateField(fieldsToValidate, (valid) => {
    if (valid) {
      currentStep.value++
    }
  })
}

// 获取每个步骤需要验证的字段
const getStepFields = (step) => {
  const fieldMap = {
    0: ['studentNo', 'name', 'gender'],
    1: ['enrollmentYear', 'majorId'],
    2: ['phone']
  }
  
  if (step === 2 && props.mode === 'add') {
    fieldMap[2].push('username')
  }
  
  return fieldMap[step] || []
}

// 提交表单
const handleSubmit = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) {
      ElMessage.warning('请完善表单信息')
      return
    }

    submitting.value = true
    try {
      const data = {
        ...formData,
        enrollmentYear: parseInt(formData.enrollmentYear),
        user: props.mode === 'add' ? {
          username: formData.username,
          password: formData.password
        } : undefined
      }

      if (props.mode === 'add') {
        await createStudent(data)
        ElMessage.success('添加学生成功')
      } else {
        await updateStudent(props.studentId, data)
        ElMessage.success('更新学生成功')
      }

      emit('success')
      handleClose()
    } catch (error) {
      console.error('操作失败:', error)
    } finally {
      submitting.value = false
    }
  })
}

// 关闭对话框
const handleClose = () => {
  visible.value = false
  currentStep.value = 0
  resetForm()
}
</script>

<style scoped lang="scss">
.form-step {
  min-height: 300px;

  :deep(.el-form-item) {
    margin-bottom: 24px;
  }

  :deep(.el-input__prefix) {
    color: #409eff;
  }

  .confirm-info {
    p {
      margin: 8px 0;
      line-height: 1.6;
      color: #606266;

      strong {
        color: #303133;
        margin-right: 8px;
      }
    }
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

:deep(.el-steps) {
  .el-step__title {
    font-size: 14px;
  }
}
</style>

