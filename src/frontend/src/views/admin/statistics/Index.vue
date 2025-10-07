<template>
  <div class="statistics-page">
    <div class="page-header">
      <h2>统计报表</h2>
      <el-select
        v-model="selectedSemester"
        placeholder="选择学期"
        clearable
        style="width: 200px"
        @change="handleSemesterChange"
      >
        <el-option
          v-for="semester in semesterList"
          :key="semester.id"
          :label="`${semester.academicYear} ${semester.semesterType === 1 ? '春季' : '秋季'}`"
          :value="semester.id"
        />
      </el-select>
    </div>

    <!-- 学生统计 -->
    <el-card class="stat-card">
      <template #header>
        <div class="card-header">
          <span>学生统计</span>
          <el-button type="primary" size="small" @click="handleExport('student')">
            <el-icon><Download /></el-icon>
            导出报表
          </el-button>
        </div>
      </template>
      <el-row :gutter="20">
        <el-col :span="12">
          <div ref="studentMajorChart" class="chart-container"></div>
        </el-col>
        <el-col :span="12">
          <div ref="studentGradeChart" class="chart-container"></div>
        </el-col>
      </el-row>
      <el-row :gutter="20" style="margin-top: 20px">
        <el-col :span="12">
          <div ref="studentGenderChart" class="chart-container"></div>
        </el-col>
        <el-col :span="12">
          <div ref="studentDeptChart" class="chart-container"></div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 课程统计 -->
    <el-card class="stat-card">
      <template #header>
        <div class="card-header">
          <span>课程统计</span>
          <el-button type="primary" size="small" @click="handleExport('course')">
            <el-icon><Download /></el-icon>
            导出报表
          </el-button>
        </div>
      </template>
      <div ref="courseChart" class="chart-container-large"></div>
    </el-card>

    <!-- 成绩统计 -->
    <el-card class="stat-card">
      <template #header>
        <div class="card-header">
          <span>成绩统计</span>
          <el-button type="primary" size="small" @click="handleExport('grade')">
            <el-icon><Download /></el-icon>
            导出报表
          </el-button>
        </div>
      </template>
      <div ref="gradeChart" class="chart-container-large"></div>
    </el-card>

    <!-- 教师工作量统计 -->
    <el-card class="stat-card">
      <template #header>
        <div class="card-header">
          <span>教师工作量统计</span>
          <el-button type="primary" size="small" @click="handleExport('teacher')">
            <el-icon><Download /></el-icon>
            导出报表
          </el-button>
        </div>
      </template>
      <el-table
        v-loading="teacherLoading"
        :data="teacherWorkload"
        stripe
        border
      >
        <el-table-column prop="teacherNo" label="工号" width="120" />
        <el-table-column prop="teacherName" label="姓名" width="120" />
        <el-table-column prop="departmentName" label="院系" width="150" />
        <el-table-column prop="title" label="职称" width="120" />
        <el-table-column prop="courseCount" label="授课门数" width="100" align="center" />
        <el-table-column prop="offeringCount" label="授课班级数" width="120" align="center" />
        <el-table-column prop="totalStudents" label="授课学生数" width="120" align="center" />
        <el-table-column prop="totalHours" label="总学时" width="100" align="center" />
        <el-table-column prop="averageStudentsPerClass" label="平均每班人数" width="130" align="center">
          <template #default="{ row }">
            {{ row.averageStudentsPerClass?.toFixed(1) || '-' }}
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Download } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import {
  getStudentStatistics,
  getCourseStatistics,
  getGradeStatistics,
  getTeacherWorkload,
  exportStudentStatistics,
  exportCourseStatistics,
  exportGradeStatistics,
  exportTeacherWorkload
} from '@/api/statistics'
import { getAllSemesters } from '@/api/semester'

const selectedSemester = ref(null)
const semesterList = ref([])
const teacherWorkload = ref([])
const teacherLoading = ref(false)

// 图表refs
const studentMajorChart = ref(null)
const studentGradeChart = ref(null)
const studentGenderChart = ref(null)
const studentDeptChart = ref(null)
const courseChart = ref(null)
const gradeChart = ref(null)

// 图表实例
let majorChartInstance = null
let gradeChartInstance = null
let genderChartInstance = null
let deptChartInstance = null
let courseChartInstance = null
let gradeStatChartInstance = null

// 获取学期列表
const fetchSemesterList = async () => {
  try {
    const { data } = await getAllSemesters()
    semesterList.value = data || []
    // 默认选择活动学期
    const activeSemester = semesterList.value.find(s => s.active)
    if (activeSemester) {
      selectedSemester.value = activeSemester.id
    }
  } catch (error) {
    console.error('获取学期列表失败:', error)
  }
}

// 加载学生统计
const loadStudentStatistics = async () => {
  try {
    const { data } = await getStudentStatistics()
    
    // 按专业分布（饼图）
    if (majorChartInstance) {
      const majorData = Object.entries(data.byMajor || {}).map(([name, value]) => ({
        name,
        value
      }))
      majorChartInstance.setOption({
        title: { text: '按专业分布', left: 'center' },
        tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
        legend: { orient: 'vertical', left: 'left', top: 'middle' },
        series: [{
          type: 'pie',
          radius: '60%',
          data: majorData,
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          }
        }]
      })
    }

    // 按年级分布（柱状图）
    if (gradeChartInstance) {
      const gradeData = Object.entries(data.byGrade || {}).sort((a, b) => a[0] - b[0])
      gradeChartInstance.setOption({
        title: { text: '按年级分布', left: 'center' },
        tooltip: { trigger: 'axis' },
        xAxis: {
          type: 'category',
          data: gradeData.map(([grade]) => `${grade}年级`)
        },
        yAxis: { type: 'value' },
        series: [{
          type: 'bar',
          data: gradeData.map(([, value]) => value),
          itemStyle: { color: '#409EFF' }
        }]
      })
    }

    // 按性别分布（饼图）
    if (genderChartInstance) {
      const genderData = Object.entries(data.byGender || {}).map(([name, value]) => ({
        name: name === 'MALE' ? '男' : '女',
        value
      }))
      genderChartInstance.setOption({
        title: { text: '按性别分布', left: 'center' },
        tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
        legend: { orient: 'vertical', left: 'left', top: 'middle' },
        series: [{
          type: 'pie',
          radius: '60%',
          data: genderData,
          itemStyle: {
            color: (params) => params.data.name === '男' ? '#409EFF' : '#F56C6C'
          }
        }]
      })
    }

    // 按院系分布（柱状图）
    if (deptChartInstance) {
      const deptData = Object.entries(data.byDepartment || {})
      deptChartInstance.setOption({
        title: { text: '按院系分布', left: 'center' },
        tooltip: { trigger: 'axis' },
        xAxis: {
          type: 'category',
          data: deptData.map(([dept]) => dept),
          axisLabel: { rotate: 30 }
        },
        yAxis: { type: 'value' },
        series: [{
          type: 'bar',
          data: deptData.map(([, value]) => value),
          itemStyle: { color: '#67C23A' }
        }]
      })
    }
  } catch (error) {
    console.error('获取学生统计失败:', error)
    ElMessage.error('获取学生统计失败')
  }
}

// 加载课程统计
const loadCourseStatistics = async () => {
  try {
    const params = selectedSemester.value ? { semesterId: selectedSemester.value } : {}
    const { data } = await getCourseStatistics(params)
    
    if (courseChartInstance && data.courseDetails) {
      const courses = data.courseDetails.slice(0, 20) // 只显示前20个
      courseChartInstance.setOption({
        title: { text: '课程选课人数统计', left: 'center' },
        tooltip: {
          trigger: 'axis',
          axisPointer: { type: 'shadow' }
        },
        legend: { data: ['容量', '已选'], top: 30 },
        grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
        xAxis: {
          type: 'category',
          data: courses.map(c => c.courseName),
          axisLabel: { rotate: 45, interval: 0 }
        },
        yAxis: { type: 'value' },
        series: [
          {
            name: '容量',
            type: 'bar',
            data: courses.map(c => c.capacity),
            itemStyle: { color: '#E6A23C' }
          },
          {
            name: '已选',
            type: 'bar',
            data: courses.map(c => c.enrolled),
            itemStyle: { color: '#409EFF' }
          }
        ]
      })
    }
  } catch (error) {
    console.error('获取课程统计失败:', error)
    ElMessage.error('获取课程统计失败')
  }
}

// 加载成绩统计
const loadGradeStatistics = async () => {
  try {
    const params = selectedSemester.value ? { semesterId: selectedSemester.value } : {}
    const { data } = await getGradeStatistics(params)
    
    if (gradeStatChartInstance && data.courseStats) {
      const courses = data.courseStats.slice(0, 15) // 只显示前15个
      gradeStatChartInstance.setOption({
        title: { text: '课程成绩统计', left: 'center' },
        tooltip: {
          trigger: 'axis',
          axisPointer: { type: 'cross' }
        },
        legend: { data: ['平均分', '及格率', '优秀率'], top: 30 },
        grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
        xAxis: {
          type: 'category',
          data: courses.map(c => c.courseName),
          axisLabel: { rotate: 45, interval: 0 }
        },
        yAxis: [
          { type: 'value', name: '分数', max: 100 },
          { type: 'value', name: '百分比(%)', max: 100 }
        ],
        series: [
          {
            name: '平均分',
            type: 'bar',
            data: courses.map(c => c.averageScore?.toFixed(1)),
            itemStyle: { color: '#409EFF' }
          },
          {
            name: '及格率',
            type: 'line',
            yAxisIndex: 1,
            data: courses.map(c => c.passRate?.toFixed(1)),
            itemStyle: { color: '#67C23A' }
          },
          {
            name: '优秀率',
            type: 'line',
            yAxisIndex: 1,
            data: courses.map(c => c.excellentRate?.toFixed(1)),
            itemStyle: { color: '#E6A23C' }
          }
        ]
      })
    }
  } catch (error) {
    console.error('获取成绩统计失败:', error)
    ElMessage.error('获取成绩统计失败')
  }
}

// 加载教师工作量统计
const loadTeacherWorkload = async () => {
  try {
    teacherLoading.value = true
    const params = selectedSemester.value ? { semesterId: selectedSemester.value } : {}
    const { data } = await getTeacherWorkload(params)
    teacherWorkload.value = data || []
  } catch (error) {
    console.error('获取教师工作量失败:', error)
    ElMessage.error('获取教师工作量失败')
  } finally {
    teacherLoading.value = false
  }
}

// 学期切换
const handleSemesterChange = () => {
  loadCourseStatistics()
  loadGradeStatistics()
  loadTeacherWorkload()
}

// 导出报表
const handleExport = async (type) => {
  try {
    let blob = null
    let filename = ''
    const params = selectedSemester.value ? { semesterId: selectedSemester.value } : {}

    switch (type) {
      case 'student':
        blob = await exportStudentStatistics()
        filename = '学生统计报表.xlsx'
        break
      case 'course':
        blob = await exportCourseStatistics(params)
        filename = '课程统计报表.xlsx'
        break
      case 'grade':
        blob = await exportGradeStatistics(params)
        filename = '成绩统计报表.xlsx'
        break
      case 'teacher':
        blob = await exportTeacherWorkload(params)
        filename = '教师工作量统计报表.xlsx'
        break
    }

    if (blob) {
      const url = window.URL.createObjectURL(new Blob([blob]))
      const link = document.createElement('a')
      link.href = url
      link.setAttribute('download', filename)
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(url)
      ElMessage.success('导出成功')
    }
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  }
}

// 初始化图表
const initCharts = () => {
  majorChartInstance = echarts.init(studentMajorChart.value)
  gradeChartInstance = echarts.init(studentGradeChart.value)
  genderChartInstance = echarts.init(studentGenderChart.value)
  deptChartInstance = echarts.init(studentDeptChart.value)
  courseChartInstance = echarts.init(courseChart.value)
  gradeStatChartInstance = echarts.init(gradeChart.value)

  // 响应式
  window.addEventListener('resize', () => {
    majorChartInstance?.resize()
    gradeChartInstance?.resize()
    genderChartInstance?.resize()
    deptChartInstance?.resize()
    courseChartInstance?.resize()
    gradeStatChartInstance?.resize()
  })
}

onMounted(async () => {
  await fetchSemesterList()
  await nextTick()
  initCharts()
  loadStudentStatistics()
  loadCourseStatistics()
  loadGradeStatistics()
  loadTeacherWorkload()
})
</script>

<style scoped>
.statistics-page {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

.stat-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-container {
  width: 100%;
  height: 350px;
}

.chart-container-large {
  width: 100%;
  height: 500px;
}
</style>

