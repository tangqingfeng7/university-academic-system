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
                    <div class="course-info">{{ shouldShowCourse(day.value, period.value).course.location || '待定' }}</div>
                    <div class="course-info">学生: {{ shouldShowCourse(day.value, period.value).course.studentCount || 0 }}人</div>
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
          <el-descriptions-item label="选课人数">
            <span class="detail-value">{{ selectedCourse.studentCount || 0 }} 人</span>
          </el-descriptions-item>
          <el-descriptions-item label="课程容量">
            <span class="detail-value">{{ selectedCourse.capacity }} 人</span>
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
import { ref, onMounted, watch } from 'vue'
import { ElMessage, ElLoading } from 'element-plus'
import { Calendar, Download } from '@element-plus/icons-vue'
import { getTeacherSchedule, exportSchedule } from '@/api/schedule'
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

// 获取学期列表
const fetchSemesters = async () => {
  try {
    const res = await getAllSemesters()
    semesters.value = res.data || []
    
    // 获取当前活动学期
    const activeRes = await getActiveSemester()
    if (activeRes.data) {
      selectedSemesterId.value = activeRes.data.id
      fetchSchedule()
    } else if (semesters.value.length > 0) {
      selectedSemesterId.value = semesters.value[0].id
      fetchSchedule()
    }
  } catch (error) {
    console.error('获取学期列表失败:', error)
    ElMessage.error('获取学期列表失败: ' + (error.message || '未知错误'))
  }
}

// 获取课表数据
const fetchSchedule = async () => {
  if (!selectedSemesterId.value) return
  
  loading.value = true
  try {
    const res = await getTeacherSchedule(selectedSemesterId.value)
    scheduleData.value = res.data
    buildCourseMap()
  } catch (error) {
    console.error('获取课表失败:', error)
    ElMessage.error('获取课表失败: ' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// 构建课程映射表
const buildCourseMap = () => {
  courseMap.value.clear()
  
  if (!scheduleData.value || !scheduleData.value.items) {
    return
  }
  
  scheduleData.value.items.forEach((item) => {
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

// 格式化周次
const formatWeeks = (weeks) => {
  if (!weeks || weeks.length === 0) return '所有周'
  
  // 简化显示，如果周次连续，显示范围
  const sortedWeeks = [...weeks].sort((a, b) => a - b)
  if (sortedWeeks.length === 1) {
    return `第${sortedWeeks[0]}周`
  }
  
  // 检查是否连续
  let isConsecutive = true
  for (let i = 1; i < sortedWeeks.length; i++) {
    if (sortedWeeks[i] - sortedWeeks[i-1] !== 1) {
      isConsecutive = false
      break
    }
  }
  
  if (isConsecutive) {
    return `第${sortedWeeks[0]}-${sortedWeeks[sortedWeeks.length-1]}周`
  }
  
  return `第${sortedWeeks.join(',')}周`
}

// 获取课程颜色
const getCourseColor = (course) => {
  if (!course) return ''
  
  // 根据课程ID或名称生成一致的颜色
  const colors = [
    'course-color-1',
    'course-color-2', 
    'course-color-3',
    'course-color-4',
    'course-color-5'
  ]
  
  const hash = course.courseId % colors.length
  return colors[hash]
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
  
  const loadingInstance = ElLoading.service({
    lock: true,
    text: '正在导出课表...',
    background: 'rgba(0, 0, 0, 0.7)'
  })
  
  try {
    const res = await exportSchedule(selectedSemesterId.value)
    
    // 创建下载链接
    const blob = new Blob([res], { type: 'application/pdf' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    
    const semester = semesters.value.find(s => s.id === selectedSemesterId.value)
    const filename = semester 
      ? `课表_${semester.academicYear}_${semester.semesterType === 1 ? '春季' : '秋季'}.pdf`
      : '课表.pdf'
    
    link.setAttribute('download', filename)
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    
    ElMessage.success('课表导出成功')
  } catch (error) {
    console.error('导出课表失败:', error)
    ElMessage.error('导出课表失败: ' + (error.message || '未知错误'))
  } finally {
    loadingInstance.close()
  }
}

// 监听周次变化，重新构建课程映射
watch(currentWeek, () => {
  buildCourseMap()
})

// 页面加载
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
  font-size: 20px;
  font-weight: bold;
  color: #303133;
}

.title-icon {
  margin-right: 10px;
  font-size: 24px;
  color: #409EFF;
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
  border: 1px solid #EBEEF5;
  padding: 8px;
  text-align: center;
}

.schedule-table th {
  background-color: #F5F7FA;
  color: #606266;
  font-weight: bold;
  height: 50px;
}

.time-col {
  width: 100px;
  background-color: #F5F7FA;
}

.period-num {
  font-weight: bold;
  color: #303133;
  margin-bottom: 4px;
}

.period-time {
  font-size: 12px;
  color: #909399;
}

.course-cell {
  height: 70px;
  padding: 4px;
  vertical-align: middle;
  position: relative;
}

.course-item {
  height: 100%;
  padding: 8px;
  border-radius: 4px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  transition: all 0.3s;
}

.course-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.course-name {
  font-weight: bold;
  color: #fff;
  margin-bottom: 6px;
  font-size: 14px;
  text-align: center;
}

.course-info {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.9);
  margin-bottom: 2px;
  text-align: center;
}

.course-weeks {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.8);
  margin-top: 4px;
  text-align: center;
}

/* 课程颜色 */
.course-color-1 {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.course-color-2 {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.course-color-3 {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.course-color-4 {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.course-color-5 {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
}

/* 响应式适配 */
@media (max-width: 1200px) {
  .schedule-wrapper {
    min-width: 800px;
  }
  
  .course-name {
    font-size: 12px;
  }
  
  .course-info {
    font-size: 11px;
  }
}

@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .controls {
    margin-top: 15px;
    flex-wrap: wrap;
    gap: 10px;
  }
  
  .time-col {
    width: 80px;
  }
  
  .course-cell {
    height: 70px;
  }
  
  .course-name {
    font-size: 11px;
  }
  
  .course-info,
  .course-weeks {
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

