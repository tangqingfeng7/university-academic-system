<template>
  <div class="schedule-container">
    <el-card class="header-card">
      <div class="header-content">
        <div class="title">
          <el-icon class="title-icon"><Calendar /></el-icon>
          <span>我的课表</span>
        </div>
        <div class="controls">
          <el-select 
            v-model="selectedSemesterId" 
            placeholder="选择学期" 
            @change="handleSemesterChange"
            style="width: 200px; margin-right: 10px">
            <el-option
              v-for="semester in semesters"
              :key="semester.id"
              :label="`${semester.academicYear} ${semester.semesterType === 1 ? '春季' : '秋季'}学期`"
              :value="semester.id">
            </el-option>
          </el-select>
          
          <el-select 
            v-model="currentWeek" 
            placeholder="选择周次"
            style="width: 120px; margin-right: 10px">
            <el-option
              v-for="week in 20"
              :key="week"
              :label="`第${week}周`"
              :value="week">
            </el-option>
          </el-select>

          <el-button type="primary" @click="handleExport">
            <el-icon><Download /></el-icon>
            导出课表
          </el-button>
        </div>
      </div>
    </el-card>

    <el-card class="schedule-card" v-loading="loading">
      <div class="schedule-wrapper">
        <table class="schedule-table">
          <thead>
            <tr>
              <th class="time-col">时间</th>
              <th v-for="day in weekDays" :key="day.value">{{ day.label }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="period in periods" :key="period.value">
              <td class="time-col">
                <div class="period-num">{{ period.label }}</div>
                <div class="period-time">{{ period.time }}</div>
              </td>
              <template v-for="day in weekDays" :key="day.value">
                <td 
                  v-if="shouldShowCourse(day.value, period.value).show" 
                  class="course-cell"
                  :rowspan="shouldShowCourse(day.value, period.value).rowspan">
                  <div 
                    class="course-item"
                    :class="getCourseColor(shouldShowCourse(day.value, period.value).course)"
                    @click="showCourseDetail(shouldShowCourse(day.value, period.value).course)">
                    <div class="course-name">{{ shouldShowCourse(day.value, period.value).course.courseName }}</div>
                    <div class="course-info">{{ shouldShowCourse(day.value, period.value).course.teacherName }}</div>
                    <div class="course-info">{{ shouldShowCourse(day.value, period.value).course.location }}</div>
                    <div class="course-weeks">{{ formatWeeks(shouldShowCourse(day.value, period.value).course.weeks) }}</div>
                  </div>
                </td>
                <td v-else-if="!getCourseByDayAndPeriod(day.value, period.value)" class="course-cell">
                  <!-- 空单元格 -->
                </td>
              </template>
            </tr>
          </tbody>
        </table>
      </div>
    </el-card>

    <!-- 课程详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="课程详细信息"
      width="600px"
      :close-on-click-modal="false">
      <div v-if="selectedCourse" class="course-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="课程名称" :span="2">
            <span class="detail-value">{{ selectedCourse.courseName }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="课程编号">
            <span class="detail-value">{{ selectedCourse.courseNo }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="学分">
            <span class="detail-value">{{ selectedCourse.credits }} 学分</span>
          </el-descriptions-item>
          <el-descriptions-item label="学时">
            <span class="detail-value">{{ selectedCourse.hours }} 学时</span>
          </el-descriptions-item>
          <el-descriptions-item label="课程类型">
            <el-tag :type="getCourseTypeTag(selectedCourse.courseType)">
              {{ getCourseTypeName(selectedCourse.courseType) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="授课教师" :span="2">
            <span class="detail-value">{{ selectedCourse.teacherName || '待定' }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="上课时间" :span="2">
            <span class="detail-value">
              {{ formatScheduleTime(selectedCourse) }}
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="上课地点" :span="2">
            <span class="detail-value">{{ selectedCourse.location || '待定' }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="上课周次" :span="2">
            <span class="detail-value">{{ formatWeeks(selectedCourse.weeks) }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="课程描述" :span="2">
            <span class="detail-value">{{ selectedCourse.description || '暂无描述' }}</span>
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElLoading } from 'element-plus'
import { Calendar, Download } from '@element-plus/icons-vue'
import { getStudentSchedule, exportSchedule } from '@/api/schedule'
import { getActiveSemester, getAllSemesters } from '@/api/semester'

// 数据定义
const loading = ref(false)
const semesters = ref([])
const selectedSemesterId = ref(null)
const currentWeek = ref(1)
const scheduleData = ref(null)
const courseMap = ref(new Map())
const detailDialogVisible = ref(false)
const selectedCourse = ref(null)

// 星期定义
const weekDays = [
  { label: '周一', value: 1 },
  { label: '周二', value: 2 },
  { label: '周三', value: 3 },
  { label: '周四', value: 4 },
  { label: '周五', value: 5 },
  { label: '周六', value: 6 },
  { label: '周日', value: 7 }
]

// 节次定义（12节课）
const periods = [
  { label: '第1节', value: 1, time: '08:20-09:00' },
  { label: '第2节', value: 2, time: '09:05-09:45' },
  { label: '第3节', value: 3, time: '10:05-10:45' },
  { label: '第4节', value: 4, time: '10:50-11:30' },
  { label: '第5节', value: 5, time: '11:35-12:15' },
  { label: '第6节', value: 6, time: '14:30-15:10' },
  { label: '第7节', value: 7, time: '15:15-15:55' },
  { label: '第8节', value: 8, time: '16:15-16:55' },
  { label: '第9节', value: 9, time: '17:00-17:40' },
  { label: '第10节', value: 10, time: '19:30-20:10' },
  { label: '第11节', value: 11, time: '20:15-20:55' },
  { label: '第12节', value: 12, time: '21:00-21:40' }
]

// 加载学期列表
const fetchSemesters = async () => {
  try {
    const res = await getAllSemesters()
    semesters.value = res.data
    
    // 获取当前激活学期
    const activeRes = await getActiveSemester()
    if (activeRes.data) {
      selectedSemesterId.value = activeRes.data.id
      await fetchSchedule()
    }
  } catch (error) {
    console.error('获取学期列表失败:', error)
    ElMessage.error('获取学期列表失败')
  }
}

// 加载课表数据
const fetchSchedule = async () => {
  if (!selectedSemesterId.value) return
  
  loading.value = true
  try {
    const res = await getStudentSchedule(selectedSemesterId.value)
    scheduleData.value = res.data
    buildCourseMap()
  } catch (error) {
    console.error('获取课表失败:', error)
    ElMessage.error('获取课表失败')
  } finally {
    loading.value = false
  }
}

// 构建课程映射表（按星期、节次、周次索引）
const buildCourseMap = () => {
  courseMap.value.clear()
  
  if (!scheduleData.value || !scheduleData.value.items) return
  
  scheduleData.value.items.forEach(item => {
    // 检查是否在当前周次
    if (item.weeks && item.weeks.includes(currentWeek.value)) {
      // 解析period范围，例如 "1-2" 或 "7-8" 或 "1"
      const periodParts = item.period.split('-')
      const startPeriod = parseInt(periodParts[0])
      const endPeriod = periodParts.length > 1 ? parseInt(periodParts[1]) : startPeriod
      
      // 为每个节次创建映射
      for (let p = startPeriod; p <= endPeriod; p++) {
        const key = `${item.day}-${p}`
        courseMap.value.set(key, item)
      }
    }
  })
}

// 检查是否应该显示课程（是该课程的第一个节次）
const shouldShowCourse = (day, period) => {
  const key = `${day}-${period}`
  const course = courseMap.value.get(key)
  
  if (!course) return { show: false, course: null, rowspan: 1 }
  
  // 检查上一个节次是否是同一课程
  const prevKey = `${day}-${period - 1}`
  const prevCourse = courseMap.value.get(prevKey)
  
  // 如果上一个节次是同一课程，则不显示（会被合并）
  if (prevCourse && prevCourse.courseId === course.courseId) {
    return { show: false, course: course, rowspan: 1 }
  }
  
  // 计算rowspan（连续多少节）
  let rowspan = 1
  let nextPeriod = period + 1
  let nextKey = `${day}-${nextPeriod}`
  let nextCourse = courseMap.value.get(nextKey)
  
  while (nextCourse && nextCourse.courseId === course.courseId) {
    rowspan++
    nextPeriod++
    nextKey = `${day}-${nextPeriod}`
    nextCourse = courseMap.value.get(nextKey)
  }
  
  return { show: true, course: course, rowspan: rowspan }
}

// 根据星期和节次获取课程
const getCourseByDayAndPeriod = (day, period) => {
  const key = `${day}-${period}`
  return courseMap.value.get(key)
}

// 格式化周次显示
const formatWeeks = (weeks) => {
  if (!weeks || weeks.length === 0) return ''
  
  // 简化显示：如果是连续的周次，显示范围
  const sorted = [...weeks].sort((a, b) => a - b)
  if (sorted.length === 1) return `第${sorted[0]}周`
  
  const first = sorted[0]
  const last = sorted[sorted.length - 1]
  
  // 检查是否连续
  let isContinuous = true
  for (let i = 0; i < sorted.length - 1; i++) {
    if (sorted[i + 1] - sorted[i] !== 1) {
      isContinuous = false
      break
    }
  }
  
  if (isContinuous) {
    return `第${first}-${last}周`
  } else {
    return `第${sorted.join(',')}周`
  }
}

// 获取课程颜色类（用于区分不同课程）
const courseColors = ['color-1', 'color-2', 'color-3', 'color-4', 'color-5']
const courseColorMap = new Map()
let colorIndex = 0

const getCourseColor = (course) => {
  if (!course) return ''
  
  const key = course.courseId
  if (!courseColorMap.has(key)) {
    courseColorMap.set(key, courseColors[colorIndex % courseColors.length])
    colorIndex++
  }
  
  return courseColorMap.get(key)
}

// 显示课程详情
const showCourseDetail = (course) => {
  selectedCourse.value = course
  detailDialogVisible.value = true
}

// 格式化上课时间
const formatScheduleTime = (course) => {
  if (!course.day || !course.period) return '待定'
  
  const dayNames = ['', '周一', '周二', '周三', '周四', '周五', '周六', '周日']
  const dayName = dayNames[course.day] || '未知'
  
  // 解析period，例如 "1-2" 或 "7-8"
  const periodParts = course.period.split('-')
  const startPeriod = parseInt(periodParts[0])
  const endPeriod = periodParts.length > 1 ? parseInt(periodParts[1]) : startPeriod
  
  if (startPeriod === endPeriod) {
    return `${dayName} 第${startPeriod}节`
  } else {
    return `${dayName} 第${startPeriod}-${endPeriod}节`
  }
}

// 获取课程类型名称
const getCourseTypeName = (type) => {
  const typeMap = {
    'REQUIRED': '必修',
    'ELECTIVE': '选修',
    'PUBLIC': '公共'
  }
  return typeMap[type] || type
}

// 获取课程类型标签
const getCourseTypeTag = (type) => {
  const tagMap = {
    'REQUIRED': 'danger',
    'ELECTIVE': 'success',
    'PUBLIC': 'info'
  }
  return tagMap[type] || 'info'
}

// 学期切换
const handleSemesterChange = () => {
  fetchSchedule()
}

// 导出课表
const handleExport = async () => {
  if (!selectedSemesterId.value) {
    ElMessage.warning('请先选择学期')
    return
  }
  
  try {
    const loadingInstance = ElLoading.service({ text: '正在导出课表...' })
    
    const response = await exportSchedule(selectedSemesterId.value)
    
    // 创建下载链接
    const blob = new Blob([response], { type: 'text/plain;charset=utf-8' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `课表_${scheduleData.value.semesterName}.txt`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    
    loadingInstance.close()
    ElMessage.success('课表导出成功')
  } catch (error) {
    console.error('导出课表失败:', error)
    ElMessage.error('导出课表失败')
  }
}

// 监听周次变化，重新构建课程映射
import { watch } from 'vue'
watch(currentWeek, () => {
  buildCourseMap()
})

// 初始化
onMounted(() => {
  fetchSemesters()
})
</script>

<style scoped>
.schedule-container {
  padding: 20px;
}

.header-card {
  margin-bottom: 20px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title {
  display: flex;
  align-items: center;
  font-size: 18px;
  font-weight: 500;
}

.title-icon {
  margin-right: 8px;
  font-size: 20px;
}

.controls {
  display: flex;
  align-items: center;
}

.schedule-card {
  overflow-x: auto;
}

.schedule-wrapper {
  min-width: 1000px;
}

.schedule-table {
  width: 100%;
  border-collapse: collapse;
  table-layout: fixed;
}

.schedule-table th,
.schedule-table td {
  border: 1px solid #dcdfe6;
  text-align: center;
  vertical-align: middle;
}

.schedule-table th {
  background-color: #f5f7fa;
  color: #606266;
  font-weight: 500;
  padding: 12px 8px;
}

.time-col {
  width: 100px;
  background-color: #fafafa;
}

.period-num {
  font-weight: 500;
  margin-bottom: 4px;
}

.period-time {
  font-size: 12px;
  color: #909399;
}

.course-cell {
  padding: 4px;
  height: 70px;
  position: relative;
  vertical-align: middle;
}

.course-item {
  width: 100%;
  height: 100%;
  padding: 8px;
  border-radius: 4px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  transition: all 0.3s;
  cursor: pointer;
}

.course-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

.course-name {
  font-weight: 500;
  font-size: 14px;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  text-align: center;
}

.course-info {
  font-size: 12px;
  opacity: 0.9;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  text-align: center;
}

.course-weeks {
  font-size: 11px;
  margin-top: 4px;
  opacity: 0.8;
  text-align: center;
}

/* 课程颜色方案 */
.color-1 {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.color-2 {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
}

.color-3 {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  color: white;
}

.color-4 {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
  color: white;
}

.color-5 {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
  color: white;
}

/* 响应式布局 */
@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    align-items: stretch;
  }
  
  .title {
    margin-bottom: 16px;
  }
  
  .controls {
    flex-wrap: wrap;
  }
  
  .controls > * {
    margin-bottom: 8px;
  }
  
  .schedule-wrapper {
    min-width: auto;
  }
  
  .schedule-table {
    font-size: 12px;
  }
  
  .course-name {
    font-size: 12px;
  }
  
  .course-info {
    font-size: 10px;
  }
}

/* 课程详情对话框 */
.course-detail {
  padding: 10px 0;
}

.detail-value {
  font-weight: 500;
  color: #303133;
}
</style>

