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
              <td v-for="day in weekDays" :key="day.value" class="course-cell">
                <div 
                  v-if="getCourseByDayAndPeriod(day.value, period.value)" 
                  class="course-item"
                  :class="getCourseColor(getCourseByDayAndPeriod(day.value, period.value))">
                  <div class="course-name">{{ getCourseByDayAndPeriod(day.value, period.value).courseName }}</div>
                  <div class="course-info">{{ getCourseByDayAndPeriod(day.value, period.value).teacherName }}</div>
                  <div class="course-info">{{ getCourseByDayAndPeriod(day.value, period.value).location }}</div>
                  <div class="course-weeks">{{ formatWeeks(getCourseByDayAndPeriod(day.value, period.value).weeks) }}</div>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </el-card>
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

// 节次定义（1-12节）
const periods = [
  { label: '1-2节', value: '1-2', time: '08:00-09:40' },
  { label: '3-4节', value: '3-4', time: '10:00-11:40' },
  { label: '5-6节', value: '5-6', time: '14:00-15:40' },
  { label: '7-8节', value: '7-8', time: '16:00-17:40' },
  { label: '9-10节', value: '9-10', time: '19:00-20:40' },
  { label: '11-12节', value: '11-12', time: '21:00-22:40' }
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
      const key = `${item.day}-${item.period}`
      courseMap.value.set(key, item)
    }
  })
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
  height: 100px;
  position: relative;
}

.course-item {
  width: 100%;
  height: 100%;
  padding: 8px;
  border-radius: 4px;
  display: flex;
  flex-direction: column;
  justify-content: center;
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
}

.course-info {
  font-size: 12px;
  opacity: 0.9;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.course-weeks {
  font-size: 11px;
  margin-top: 4px;
  opacity: 0.8;
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
</style>

