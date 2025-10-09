<template>
  <el-dialog
    v-model="dialogVisible"
    :title="isEdit ? '编辑开课计划' : '添加开课计划'"
    width="800px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
    >
      <el-form-item label="学期" prop="semesterId">
        <el-select
          v-model="formData.semesterId"
          placeholder="请选择学期"
          :disabled="isEdit"
          style="width: 100%"
        >
          <el-option
            v-for="semester in semesters"
            :key="semester.id"
            :label="`${semester.academicYear} ${semester.semesterType === 1 ? '春季' : '秋季'}`"
            :value="semester.id"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="课程" prop="courseId">
        <el-select
          v-model="formData.courseId"
          placeholder="请选择课程"
          filterable
          :disabled="isEdit"
          style="width: 100%"
        >
          <el-option
            v-for="course in courses"
            :key="course.id"
            :label="`${course.courseNo} - ${course.name}`"
            :value="course.id"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="授课教师" prop="teacherId">
        <el-select
          v-model="formData.teacherId"
          placeholder="请选择教师"
          filterable
          style="width: 100%"
        >
          <el-option
            v-for="teacher in teachers"
            :key="teacher.id"
            :label="`${teacher.teacherNo} - ${teacher.name}`"
            :value="teacher.id"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="上课时间" prop="scheduleList">
        <div style="width: 100%">
          <div 
            v-for="(item, index) in formData.scheduleList" 
            :key="index"
            style="display: flex; gap: 10px; margin-bottom: 10px; align-items: center;"
          >
            <el-select v-model="item.day" placeholder="星期" style="width: 120px">
              <el-option label="星期一" :value="1" />
              <el-option label="星期二" :value="2" />
              <el-option label="星期三" :value="3" />
              <el-option label="星期四" :value="4" />
              <el-option label="星期五" :value="5" />
              <el-option label="星期六" :value="6" />
              <el-option label="星期日" :value="7" />
            </el-select>
            <el-select v-model="item.period" placeholder="节次" style="width: 150px">
              <el-option label="第1-2节" value="1-2" />
              <el-option label="第3-4节" value="3-4" />
              <el-option label="第5-6节" value="5-6" />
              <el-option label="第7-8节" value="7-8" />
              <el-option label="第9-10节" value="9-10" />
            </el-select>
            <el-input 
              v-model="item.weeksStr" 
              placeholder="周次范围，如：1-16"
              style="flex: 1"
            />
            <el-button
              type="danger"
              :icon="Delete"
              circle
              @click="removeScheduleItem(index)"
              :disabled="formData.scheduleList.length === 1"
            />
          </div>
          <el-button
            type="primary"
            :icon="Plus"
            @click="addScheduleItem"
            size="small"
          >
            添加时间段
          </el-button>
        </div>
      </el-form-item>

      <el-form-item label="上课地点" prop="location">
        <el-input
          v-model="formData.location"
          placeholder="请输入上课地点，如：教学楼A101"
        />
      </el-form-item>

      <el-form-item label="容量" prop="capacity">
        <el-input-number
          v-model="formData.capacity"
          :min="currentEnrolled || 1"
          :max="500"
          placeholder="请输入容量"
        />
        <div v-if="currentEnrolled > 0" style="color: #909399; font-size: 12px; margin-top: 4px;">
          当前已选：{{ currentEnrolled }}人（容量不能小于已选人数）
        </div>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
        确定
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Delete } from '@element-plus/icons-vue'
import {
  getOfferingById,
  createOffering,
  updateOffering
} from '@/api/offering'
import { getAllSemesters } from '@/api/semester'
import { getAllCourses } from '@/api/course'
import { getAllTeachers } from '@/api/teacher'

const props = defineProps({
  modelValue: Boolean,
  offeringId: [Number, String],
  mode: {
    type: String,
    default: 'add'
  }
})

const emit = defineEmits(['update:modelValue', 'success'])

const dialogVisible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const isEdit = computed(() => props.mode === 'edit')

// 表单引用
const formRef = ref(null)

// 下拉选项
const semesters = ref([])
const courses = ref([])
const teachers = ref([])

// 表单数据
const formData = reactive({
  semesterId: null,
  courseId: null,
  teacherId: null,
  scheduleList: [
    { day: null, period: null, weeksStr: '1-16' }
  ],
  location: '',
  capacity: 30
})

// 表单验证规则
const formRules = {
  semesterId: [
    { required: true, message: '请选择学期', trigger: 'change' }
  ],
  courseId: [
    { required: true, message: '请选择课程', trigger: 'change' }
  ],
  teacherId: [
    { required: true, message: '请选择教师', trigger: 'change' }
  ],
  scheduleList: [
    { 
      required: true, 
      validator: (rule, value, callback) => {
        if (!value || value.length === 0) {
          callback(new Error('请添加至少一个上课时间'))
        } else {
          const hasEmpty = value.some(item => !item.day || !item.period)
          if (hasEmpty) {
            callback(new Error('请完整填写上课时间信息'))
          } else {
            callback()
          }
        }
      },
      trigger: 'change' 
    }
  ],
  location: [
    { required: true, message: '请输入上课地点', trigger: 'blur' }
  ],
  capacity: [
    { required: true, message: '请输入容量', trigger: 'blur' }
  ]
}

const submitLoading = ref(false)

// 添加时间段
const addScheduleItem = () => {
  formData.scheduleList.push({
    day: null,
    period: null,
    weeksStr: '1-16'
  })
}

// 删除时间段
const removeScheduleItem = (index) => {
  if (formData.scheduleList.length > 1) {
    formData.scheduleList.splice(index, 1)
  }
}

// 解析周次字符串为数组
const parseWeeks = (weeksStr) => {
  if (!weeksStr) return []
  
  const weeks = []
  const parts = weeksStr.split(',')
  
  for (const part of parts) {
    if (part.includes('-')) {
      const [start, end] = part.split('-').map(Number)
      for (let i = start; i <= end; i++) {
        weeks.push(i)
      }
    } else {
      weeks.push(Number(part))
    }
  }
  
  return weeks
}

// 获取学期列表
const fetchSemesters = async () => {
  try {
    const res = await getAllSemesters()
    semesters.value = res.data
  } catch (error) {
    console.error('获取学期列表失败:', error)
  }
}

// 获取课程列表
const fetchCourses = async () => {
  try {
    const res = await getAllCourses()
    courses.value = res.data
  } catch (error) {
    console.error('获取课程列表失败:', error)
  }
}

// 获取教师列表
const fetchTeachers = async () => {
  try {
    const res = await getAllTeachers()
    teachers.value = res.data
  } catch (error) {
    console.error('获取教师列表失败:', error)
  }
}

// 当前已选人数
const currentEnrolled = ref(0)

// 获取开课计划详情
const fetchOfferingDetail = async () => {
  if (!props.offeringId) return
  
  try {
    const res = await getOfferingById(props.offeringId)
    const offering = res.data
    
    formData.semesterId = offering.semesterId
    formData.courseId = offering.courseId
    formData.teacherId = offering.teacherId
    formData.location = offering.location
    formData.capacity = offering.capacity
    currentEnrolled.value = offering.enrolled || 0
    
    // 解析schedule
    if (offering.schedule) {
      try {
        const scheduleData = JSON.parse(offering.schedule)
        formData.scheduleList = scheduleData.map(item => ({
          day: item.day,
          period: item.period,
          weeksStr: item.weeks ? item.weeks.join(',').replace(/,(\d+)/g, (match, p1, offset, string) => {
            const prev = parseInt(string.substring(0, offset).split(',').pop())
            return parseInt(p1) === prev + 1 ? '-' + p1 : ',' + p1
          }).replace(/-(\d+),(\d+)-/g, '-$2-') : '1-16'
        }))
      } catch (e) {
        console.error('解析schedule失败:', e)
      }
    }
  } catch (error) {
    console.error('获取开课计划详情失败:', error)
    ElMessage.error('获取开课计划详情失败')
  }
}

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    
    submitLoading.value = true
    
    // 构建schedule JSON
    const scheduleData = formData.scheduleList.map(item => ({
      day: item.day,
      period: item.period,
      weeks: parseWeeks(item.weeksStr)
    }))
    
    const data = {
      semesterId: formData.semesterId,
      courseId: formData.courseId,
      teacherId: formData.teacherId,
      schedule: JSON.stringify(scheduleData),
      location: formData.location,
      capacity: formData.capacity
    }
    
    if (isEdit.value) {
      await updateOffering(props.offeringId, data)
      ElMessage.success('更新成功')
    } else {
      await createOffering(data)
      ElMessage.success('创建成功')
    }
    
    emit('success')
    handleClose()
  } catch (error) {
    if (error !== false) {  // 验证失败会返回false
      console.error('提交失败:', error)
      ElMessage.error('操作失败')
    }
  } finally {
    submitLoading.value = false
  }
}

// 关闭对话框
const handleClose = () => {
  formRef.value?.resetFields()
  formData.scheduleList = [{ day: null, period: null, weeksStr: '1-16' }]
  dialogVisible.value = false
}

// 监听对话框显示
watch(
  () => props.modelValue,
  async (val) => {
    if (val) {
      await fetchSemesters()
      await fetchCourses()
      await fetchTeachers()
      
      if (isEdit.value) {
        await fetchOfferingDetail()
      }
    }
  }
)
</script>

<style scoped>
.el-input-number {
  width: 100%;
}
</style>

