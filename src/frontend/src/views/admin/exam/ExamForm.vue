<template>
  <div class="exam-form-container">
    <!-- 面包屑导航 -->
    <el-breadcrumb separator="/" class="breadcrumb">
      <el-breadcrumb-item :to="{ path: '/admin' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item :to="{ path: '/admin/exam' }">考试管理</el-breadcrumb-item>
      <el-breadcrumb-item>{{ isEdit ? '编辑考试' : '创建考试' }}</el-breadcrumb-item>
    </el-breadcrumb>

    <!-- 表单卡片 -->
    <el-card class="form-card">
      <template #header>
        <div class="card-header">
          <span>{{ isEdit ? '编辑考试' : '创建考试' }}</span>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
        style="max-width: 800px"
      >
        <!-- 考试名称 -->
        <el-form-item label="考试名称" prop="name">
          <el-input
            v-model="formData.name"
            placeholder="请输入考试名称，如：高等数学期末考试"
            clearable
          />
        </el-form-item>

        <!-- 考试类型 -->
        <el-form-item label="考试类型" prop="type">
          <el-select
            v-model="formData.type"
            placeholder="请选择考试类型"
            style="width: 100%"
          >
            <el-option label="期中考试" value="MIDTERM" />
            <el-option label="期末考试" value="FINAL" />
            <el-option label="补考" value="MAKEUP" />
            <el-option label="重修考试" value="RETAKE" />
          </el-select>
        </el-form-item>

        <!-- 开课计划选择器（级联） -->
        <el-form-item label="关联课程" prop="courseOfferingId">
          <div style="width: 100%">
            <!-- 第一步：选择学期 -->
            <el-select
              v-model="selectedSemesterId"
              placeholder="第一步：请选择学期"
              style="width: 100%; margin-bottom: 10px"
              @change="handleSemesterChange"
              :disabled="isEdit"
            >
              <el-option
                v-for="semester in semesterList"
                :key="semester.id"
                :label="semester.semesterName"
                :value="semester.id"
              />
            </el-select>

            <!-- 第二步：选择课程（可选，用于筛选） -->
            <el-select
              v-model="selectedCourseId"
              placeholder="第二步：选择课程（可选，用于筛选）"
              style="width: 100%; margin-bottom: 10px"
              clearable
              filterable
              @change="handleCourseFilter"
              :disabled="!selectedSemesterId || isEdit"
            >
              <el-option
                v-for="course in courseList"
                :key="course.id"
                :label="`${course.courseNo} - ${course.name}`"
                :value="course.id"
              />
            </el-select>

            <!-- 第三步：选择开课计划 -->
            <el-select
              v-model="formData.courseOfferingId"
              placeholder="第三步：请选择开课计划"
              style="width: 100%"
              filterable
              :disabled="!selectedSemesterId || isEdit"
            >
              <el-option
                v-for="offering in filteredOfferingList"
                :key="offering.id"
                :label="getOfferingLabel(offering)"
                :value="offering.id"
              >
                <div style="display: flex; justify-content: space-between">
                  <span>{{ offering.course.courseNo }} - {{ offering.course.name }}</span>
                  <span style="color: #909399; font-size: 12px">
                    {{ offering.teacher.name }} | {{ offering.schedule }}
                  </span>
                </div>
              </el-option>
            </el-select>

            <!-- 已选择的开课计划信息 -->
            <div v-if="selectedOffering" class="selected-offering-info">
              <el-descriptions :column="2" size="small" border>
                <el-descriptions-item label="课程">
                  {{ selectedOffering.course.courseNo }} - {{ selectedOffering.course.name }}
                </el-descriptions-item>
                <el-descriptions-item label="教师">
                  {{ selectedOffering.teacher.name }}
                </el-descriptions-item>
                <el-descriptions-item label="学期">
                  {{ selectedOffering.semester.semesterName }}
                </el-descriptions-item>
                <el-descriptions-item label="上课时间">
                  {{ selectedOffering.schedule }}
                </el-descriptions-item>
              </el-descriptions>
            </div>
          </div>
        </el-form-item>

        <!-- 考试时间 -->
        <el-form-item label="考试时间" prop="examTime">
          <el-date-picker
            v-model="formData.examTime"
            type="datetime"
            placeholder="请选择考试时间"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DDTHH:mm:ss"
            :disabled-date="disabledDate"
            style="width: 100%"
          />
          <div class="form-tip">考试时间不能早于当前时间</div>
        </el-form-item>

        <!-- 考试时长 -->
        <el-form-item label="考试时长" prop="duration">
          <el-select
            v-model="formData.duration"
            placeholder="请选择考试时长"
            style="width: 200px"
          >
            <el-option label="60分钟 (1小时)" :value="60" />
            <el-option label="90分钟 (1.5小时)" :value="90" />
            <el-option label="120分钟 (2小时)" :value="120" />
            <el-option label="150分钟 (2.5小时)" :value="150" />
            <el-option label="180分钟 (3小时)" :value="180" />
          </el-select>
          <span style="margin: 0 10px">或</span>
          <el-input-number
            v-model="formData.duration"
            :min="30"
            :max="300"
            :step="10"
            placeholder="自定义分钟数"
            style="width: 200px"
          />
          <span style="margin-left: 10px">分钟</span>
        </el-form-item>

        <!-- 总分 -->
        <el-form-item label="总分" prop="totalScore">
          <el-input-number
            v-model="formData.totalScore"
            :min="1"
            :max="200"
            placeholder="请输入总分"
            style="width: 200px"
          />
          <span style="margin-left: 10px">分</span>
        </el-form-item>

        <!-- 考试说明 -->
        <el-form-item label="考试说明" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="4"
            placeholder="请输入考试说明，如考试要求、注意事项等（可选）"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <!-- 操作按钮 -->
        <el-form-item>
          <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
            {{ isEdit ? '保存修改' : '创建考试' }}
          </el-button>
          <el-button @click="handleCancel">取消</el-button>
          <el-button v-if="!isEdit" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  getExamById, 
  createExam, 
  updateExam 
} from '@/api/exam'
import { getAllSemesters } from '@/api/semester'
import { getOfferingList } from '@/api/offering'

const router = useRouter()
const route = useRoute()

// 表单引用
const formRef = ref(null)

// 是否编辑模式
const isEdit = computed(() => !!route.params.id)

// 提交loading
const submitLoading = ref(false)

// 表单数据
const formData = reactive({
  name: '',
  type: '',
  courseOfferingId: null,
  examTime: '',
  duration: 120, // 默认2小时
  totalScore: 100, // 默认100分
  description: ''
})

// 级联选择器的中间状态
const selectedSemesterId = ref(null)
const selectedCourseId = ref(null)

// 下拉列表数据
const semesterList = ref([])
const offeringList = ref([])
const courseList = ref([])

// 自定义验证器：验证考试时间不能早于当前时间
const validateExamTime = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请选择考试时间'))
  } else {
    const examTime = new Date(value)
    const now = new Date()
    // 考试时间必须晚于当前时间至少1小时
    if (examTime.getTime() < now.getTime() + 60 * 60 * 1000) {
      callback(new Error('考试时间必须晚于当前时间至少1小时'))
    } else {
      callback()
    }
  }
}

// 自定义验证器：验证考试时长
const validateDuration = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入考试时长'))
  } else if (typeof value !== 'number') {
    callback(new Error('考试时长必须是数字'))
  } else if (value < 30) {
    callback(new Error('考试时长不能少于30分钟'))
  } else if (value > 300) {
    callback(new Error('考试时长不能超过300分钟（5小时）'))
  } else {
    callback()
  }
}

// 表单验证规则
const formRules = {
  name: [
    { required: true, message: '请输入考试名称', trigger: 'blur' },
    { min: 2, max: 200, message: '长度在 2 到 200 个字符', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择考试类型', trigger: 'change' }
  ],
  courseOfferingId: [
    { required: true, message: '请选择开课计划', trigger: 'change' }
  ],
  examTime: [
    { required: true, message: '请选择考试时间', trigger: 'change' },
    { validator: validateExamTime, trigger: 'change' }
  ],
  duration: [
    { required: true, message: '请输入考试时长', trigger: 'blur' },
    { validator: validateDuration, trigger: 'blur' }
  ],
  totalScore: [
    { required: true, message: '请输入总分', trigger: 'blur' },
    { type: 'number', min: 1, max: 200, message: '总分需在 1-200 分之间', trigger: 'blur' }
  ]
}

// ==================== 计算属性 ====================

// 已选择的开课计划详情
const selectedOffering = computed(() => {
  if (!formData.courseOfferingId) return null
  return offeringList.value.find(o => o.id === formData.courseOfferingId)
})

// 筛选后的开课计划列表
const filteredOfferingList = computed(() => {
  if (!selectedCourseId.value) {
    return offeringList.value
  }
  return offeringList.value.filter(o => o.course.id === selectedCourseId.value)
})

// ==================== 日期限制 ====================

// 禁用过去的日期
const disabledDate = (time) => {
  return time.getTime() < Date.now() - 24 * 60 * 60 * 1000 // 不能选择今天之前的日期
}

// ==================== 数据加载 ====================

// 获取学期列表
const fetchSemesters = async () => {
  try {
    const res = await getAllSemesters()
    semesterList.value = res.data
  } catch (error) {
    console.error('获取学期列表失败:', error)
    ElMessage.error('获取学期列表失败')
  }
}

// 获取开课计划列表
const fetchOfferings = async (semesterId) => {
  try {
    const res = await getOfferingList({
      semesterId: semesterId,
      status: 'PUBLISHED', // 只显示已发布的开课计划
      page: 0,
      size: 1000 // 获取全部
    })
    offeringList.value = res.data.content

    // 提取课程列表（去重）
    const courseMap = new Map()
    offeringList.value.forEach(offering => {
      if (!courseMap.has(offering.course.id)) {
        courseMap.set(offering.course.id, offering.course)
      }
    })
    courseList.value = Array.from(courseMap.values())
  } catch (error) {
    console.error('获取开课计划列表失败:', error)
    ElMessage.error('获取开课计划列表失败')
  }
}

// 加载考试详情（编辑模式）
const fetchExamDetail = async () => {
  try {
    const res = await getExamById(route.params.id)
    const exam = res.data
    
    // 回显表单数据
    formData.name = exam.name
    formData.type = exam.type
    formData.courseOfferingId = exam.courseOffering.id
    formData.examTime = exam.examTime
    formData.duration = exam.duration
    formData.totalScore = exam.totalScore
    formData.description = exam.description || ''

    // 回显级联选择器
    selectedSemesterId.value = exam.courseOffering.semester.id
    await fetchOfferings(selectedSemesterId.value)
  } catch (error) {
    console.error('获取考试详情失败:', error)
    ElMessage.error('获取考试详情失败')
    router.back()
  }
}

// ==================== 事件处理 ====================

// 学期改变
const handleSemesterChange = async (semesterId) => {
  selectedCourseId.value = null
  formData.courseOfferingId = null
  offeringList.value = []
  courseList.value = []
  
  if (semesterId) {
    await fetchOfferings(semesterId)
  }
}

// 课程筛选改变
const handleCourseFilter = () => {
  formData.courseOfferingId = null
}

// 获取开课计划显示标签
const getOfferingLabel = (offering) => {
  return `${offering.course.courseNo} - ${offering.course.name} (${offering.teacher.name})`
}

// 提交表单
const handleSubmit = async () => {
  try {
    // 验证表单
    await formRef.value.validate()

    submitLoading.value = true

    const submitData = {
      name: formData.name,
      type: formData.type,
      courseOfferingId: formData.courseOfferingId,
      examTime: formData.examTime,
      duration: formData.duration,
      totalScore: formData.totalScore,
      description: formData.description
    }

    if (isEdit.value) {
      // 编辑模式
      await updateExam(route.params.id, submitData)
      ElMessage.success('考试更新成功')
    } else {
      // 创建模式
      await createExam(submitData)
      ElMessage.success('考试创建成功')
    }

    // 返回列表页
    router.push('/admin/exam')
  } catch (error) {
    if (error !== 'validation-failed') {
      console.error('提交失败:', error)
      ElMessage.error(error.message || '操作失败')
    }
  } finally {
    submitLoading.value = false
  }
}

// 取消
const handleCancel = () => {
  router.back()
}

// 重置表单
const handleReset = () => {
  formRef.value.resetFields()
  selectedSemesterId.value = null
  selectedCourseId.value = null
  offeringList.value = []
  courseList.value = []
}

// ==================== 生命周期 ====================

onMounted(async () => {
  await fetchSemesters()
  
  if (isEdit.value) {
    await fetchExamDetail()
  }
})
</script>

<style scoped>
.exam-form-container {
  padding: 20px;
}

.breadcrumb {
  margin-bottom: 20px;
}

.form-card {
  max-width: 1200px;
}

.card-header {
  font-size: 18px;
  font-weight: 600;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.selected-offering-info {
  margin-top: 10px;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

:deep(.el-descriptions__label) {
  width: 100px;
}
</style>

