<template>
  <div class="quality-report-page">
    <!-- 页面头部 -->
    <div class="page-header animate-fade-in-down">
      <div class="header-content">
        <h1 class="page-title">教学质量报告</h1>
        <p class="page-subtitle">基于学生评价的教学质量综合分析</p>
      </div>
      <el-button 
        type="primary" 
        :icon="Download"
        size="large"
        :loading="exporting"
        @click="handleExport"
      >
        导出报告
      </el-button>
    </div>

    <!-- 学期选择 -->
    <div class="filter-section animate-fade-in-up" style="animation-delay: 0.1s;">
      <el-select
        v-model="selectedSemester"
        placeholder="选择学期"
        size="large"
        @change="loadReport"
        style="width: 250px"
      >
        <el-option
          v-for="semester in semesters"
          :key="semester.id"
          :label="semester.academicYear + ' ' + getSemesterTypeName(semester.semesterType)"
          :value="semester.id"
        />
      </el-select>
    </div>

    <!-- 报告内容 -->
    <div v-loading="loading" class="report-container animate-fade-in-up" style="animation-delay: 0.2s;">
      <el-empty 
        v-if="!report && !loading"
        description="请选择学期生成报告"
      />

      <div v-else-if="report" class="report-content">
        <!-- 整体概况 -->
        <div class="section-card">
          <div class="section-header">
            <h2>整体概况</h2>
            <span class="report-time">报告生成时间：{{ formatDateTime(report.reportTime) }}</span>
          </div>
          <div class="stats-grid">
            <div class="stat-item">
              <div class="stat-label">课程总数</div>
              <div class="stat-value">{{ report.totalCourses }}</div>
            </div>
            <div class="stat-item">
              <div class="stat-label">教师总数</div>
              <div class="stat-value">{{ report.totalTeachers }}</div>
            </div>
            <div class="stat-item">
              <div class="stat-label">课程评价数</div>
              <div class="stat-value">{{ report.totalCourseEvaluations }}</div>
            </div>
            <div class="stat-item">
              <div class="stat-label">教师评价数</div>
              <div class="stat-value">{{ report.totalTeacherEvaluations }}</div>
            </div>
            <div class="stat-item">
              <div class="stat-label">参与学生数</div>
              <div class="stat-value">{{ report.totalStudentsEvaluated }}</div>
            </div>
            <div class="stat-item">
              <div class="stat-label">参与率</div>
              <div class="stat-value stat-percentage">{{ formatPercentage(report.overallParticipationRate) }}</div>
            </div>
          </div>
        </div>

        <!-- 评分概览 -->
        <div class="section-card">
          <div class="section-header">
            <h2>评分概览</h2>
          </div>
          <el-row :gutter="24">
            <el-col :span="12">
              <div class="score-card">
                <div class="score-header">
                  <el-icon :size="32" color="#1890ff"><Reading /></el-icon>
                  <h3>课程平均评分</h3>
                </div>
                <div class="score-value">{{ formatScore(report.overallCourseAverageRating) }}</div>
                <el-rate 
                  :model-value="report.overallCourseAverageRating" 
                  disabled 
                  show-score
                  :score-template="`${formatScore(report.overallCourseAverageRating)}`"
                />
              </div>
            </el-col>
            <el-col :span="12">
              <div class="score-card">
                <div class="score-header">
                  <el-icon :size="32" color="#52c41a"><UserFilled /></el-icon>
                  <h3>教师综合评分</h3>
                </div>
                <div class="teacher-scores">
                  <div class="score-item">
                    <span class="label">教学能力</span>
                    <span class="value">{{ formatScore(report.overallTeacherAverageTeachingRating) }}</span>
                  </div>
                  <div class="score-item">
                    <span class="label">教学态度</span>
                    <span class="value">{{ formatScore(report.overallTeacherAverageAttitudeRating) }}</span>
                  </div>
                  <div class="score-item">
                    <span class="label">教学内容</span>
                    <span class="value">{{ formatScore(report.overallTeacherAverageContentRating) }}</span>
                  </div>
                </div>
              </div>
            </el-col>
          </el-row>
        </div>

        <!-- 优秀课程 -->
        <div class="section-card">
          <div class="section-header">
            <h2>优秀课程</h2>
            <el-tag type="success">评分 ≥ 4.5 且参与率 ≥ 50%</el-tag>
          </div>
          <el-table
            :data="report.excellentCourses"
            style="width: 100%"
            :default-sort="{ prop: 'averageRating', order: 'descending' }"
          >
            <el-table-column type="index" label="排名" width="80" />
            <el-table-column prop="courseName" label="课程名称" min-width="180" />
            <el-table-column prop="teacherName" label="教师" width="120" />
            <el-table-column prop="averageRating" label="平均评分" width="150" sortable>
              <template #default="{ row }">
                <el-rate :model-value="row.averageRating" disabled show-score />
              </template>
            </el-table-column>
            <el-table-column prop="totalEvaluations" label="评价数" width="100" align="center" sortable />
            <el-table-column prop="participationRate" label="参与率" width="120" sortable>
              <template #default="{ row }">
                {{ formatPercentage(row.participationRate) }}
              </template>
            </el-table-column>
          </el-table>
        </div>

        <!-- 待改进课程 -->
        <div v-if="report.coursesToImprove && report.coursesToImprove.length > 0" class="section-card">
          <div class="section-header">
            <h2>待改进课程</h2>
            <el-tag type="warning">评分 < 3.0</el-tag>
          </div>
          <el-table
            :data="report.coursesToImprove"
            style="width: 100%"
            :default-sort="{ prop: 'averageRating', order: 'ascending' }"
          >
            <el-table-column type="index" label="序号" width="80" />
            <el-table-column prop="courseName" label="课程名称" min-width="180" />
            <el-table-column prop="teacherName" label="教师" width="120" />
            <el-table-column prop="averageRating" label="平均评分" width="150" sortable>
              <template #default="{ row }">
                <el-rate :model-value="row.averageRating" disabled show-score />
              </template>
            </el-table-column>
            <el-table-column prop="totalEvaluations" label="评价数" width="100" align="center" sortable />
            <el-table-column prop="participationRate" label="参与率" width="120" sortable>
              <template #default="{ row }">
                {{ formatPercentage(row.participationRate) }}
              </template>
            </el-table-column>
          </el-table>
        </div>

        <!-- 优秀教师 -->
        <div class="section-card">
          <div class="section-header">
            <h2>优秀教师</h2>
            <el-tag type="success">综合评分 ≥ 4.5</el-tag>
          </div>
          <el-table
            :data="report.excellentTeachers"
            style="width: 100%"
            :default-sort="{ prop: 'overallAverageRating', order: 'descending' }"
          >
            <el-table-column type="index" label="排名" width="80" />
            <el-table-column prop="teacherName" label="教师姓名" width="120" />
            <el-table-column prop="averageTeachingRating" label="教学能力" width="150">
              <template #default="{ row }">
                <el-rate :model-value="row.averageTeachingRating" disabled />
                <span class="score-text">{{ formatScore(row.averageTeachingRating) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="averageAttitudeRating" label="教学态度" width="150">
              <template #default="{ row }">
                <el-rate :model-value="row.averageAttitudeRating" disabled />
                <span class="score-text">{{ formatScore(row.averageAttitudeRating) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="averageContentRating" label="教学内容" width="150">
              <template #default="{ row }">
                <el-rate :model-value="row.averageContentRating" disabled />
                <span class="score-text">{{ formatScore(row.averageContentRating) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="overallAverageRating" label="综合评分" width="150" sortable>
              <template #default="{ row }">
                <el-rate :model-value="row.overallAverageRating" disabled show-score />
              </template>
            </el-table-column>
            <el-table-column prop="totalEvaluations" label="评价数" width="100" align="center" sortable />
          </el-table>
        </div>

        <!-- 数据可视化 -->
        <div class="section-card">
          <div class="section-header">
            <h2>评价趋势分析</h2>
          </div>
          <el-row :gutter="24">
            <el-col :span="12">
              <div class="chart-container">
                <h3>课程评分分布</h3>
                <div ref="courseChartRef" class="chart"></div>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="chart-container">
                <h3>教师评价维度分布</h3>
                <div ref="teacherChartRef" class="chart"></div>
              </div>
            </el-col>
          </el-row>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Download, Reading, UserFilled } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { generateQualityReport } from '@/api/evaluation'
import { getAllSemesters } from '@/api/semester'

const loading = ref(false)
const exporting = ref(false)
const semesters = ref([])
const selectedSemester = ref(null)
const report = ref(null)

const courseChartRef = ref(null)
const teacherChartRef = ref(null)
let courseChart = null
let teacherChart = null

// 加载学期列表
const loadSemesters = async () => {
  try {
    const res = await getAllSemesters()
    if (res.code === 200) {
      semesters.value = res.data || []
      // 默认选择最新学期
      if (semesters.value.length > 0) {
        const activeSemester = semesters.value.find(s => s.active)
        selectedSemester.value = activeSemester ? activeSemester.id : semesters.value[0].id
        loadReport()
      }
    }
  } catch (error) {
    ElMessage.error('加载学期列表失败')
  }
}

// 加载报告
const loadReport = async () => {
  if (!selectedSemester.value) return

  loading.value = true
  try {
    const res = await generateQualityReport(selectedSemester.value)
    if (res.code === 200) {
      report.value = res.data
      await nextTick()
      initCharts()
    }
  } catch (error) {
    ElMessage.error('加载报告失败')
  } finally {
    loading.value = false
  }
}

// 初始化图表
const initCharts = () => {
  initCourseChart()
  initTeacherChart()
}

// 初始化课程评分分布图
const initCourseChart = () => {
  if (!courseChartRef.value || !report.value) return

  if (courseChart) {
    courseChart.dispose()
  }

  courseChart = echarts.init(courseChartRef.value)

  // 统计各评分段的课程数量
  const ratings = report.value.allCourseStatistics.map(c => c.averageRating)
  const distribution = [0, 0, 0, 0, 0] // 0-1, 1-2, 2-3, 3-4, 4-5
  ratings.forEach(rating => {
    const index = Math.min(Math.floor(rating), 4)
    distribution[index]++
  })

  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: ['0-1分', '1-2分', '2-3分', '3-4分', '4-5分'],
      axisTick: {
        alignWithLabel: true
      }
    },
    yAxis: {
      type: 'value',
      name: '课程数量'
    },
    series: [
      {
        name: '课程数量',
        type: 'bar',
        barWidth: '60%',
        data: distribution,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#83bff6' },
            { offset: 0.5, color: '#188df0' },
            { offset: 1, color: '#188df0' }
          ])
        }
      }
    ]
  }

  courseChart.setOption(option)
}

// 初始化教师评价维度图
const initTeacherChart = () => {
  if (!teacherChartRef.value || !report.value) return

  if (teacherChart) {
    teacherChart.dispose()
  }

  teacherChart = echarts.init(teacherChartRef.value)

  const option = {
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['教学能力', '教学态度', '教学内容']
    },
    radar: {
      indicator: [
        { name: '教学能力', max: 5 },
        { name: '教学态度', max: 5 },
        { name: '教学内容', max: 5 }
      ]
    },
    series: [
      {
        name: '教师评价维度',
        type: 'radar',
        data: [
          {
            value: [
              report.value.overallTeacherAverageTeachingRating,
              report.value.overallTeacherAverageAttitudeRating,
              report.value.overallTeacherAverageContentRating
            ],
            name: '平均评分',
            areaStyle: {
              color: 'rgba(24, 144, 255, 0.3)'
            }
          }
        ]
      }
    ]
  }

  teacherChart.setOption(option)
}

// 导出报告
const handleExport = () => {
  ElMessage.info('导出功能开发中...')
}

// 格式化分数
const formatScore = (score) => {
  return score ? score.toFixed(1) : '0.0'
}

// 格式化百分比
const formatPercentage = (rate) => {
  return rate ? `${(rate * 100).toFixed(1)}%` : '0.0%'
}

// 获取学期类型名称
const getSemesterTypeName = (type) => {
  return type === 1 ? '春季学期' : '秋季学期'
}

// 格式化日期时间
const formatDateTime = (datetime) => {
  if (!datetime) return ''
  const date = new Date(datetime)
  return date.toLocaleString('zh-CN')
}

// 窗口大小变化时重新渲染图表
const handleResize = () => {
  courseChart?.resize()
  teacherChart?.resize()
}

onMounted(() => {
  loadSemesters()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  courseChart?.dispose()
  teacherChart?.dispose()
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.quality-report-page {
  padding: 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.header-content {
  flex: 1;
}

.page-title {
  font-size: 28px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 8px 0;
}

.page-subtitle {
  font-size: 14px;
  color: #666;
  margin: 0;
}

.filter-section {
  margin-bottom: 24px;
}

.report-container {
  min-height: 600px;
}

.report-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.section-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 2px solid #f0f2f5;
}

.section-header h2 {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0;
}

.report-time {
  font-size: 12px;
  color: #909399;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 20px;
}

.stat-item {
  text-align: center;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 8px;
}

.stat-label {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 32px;
  font-weight: 600;
  color: #1890ff;
}

.stat-percentage {
  color: #52c41a;
}

.score-card {
  padding: 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  color: white;
  text-align: center;
}

.score-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.score-header h3 {
  font-size: 16px;
  margin: 0;
}

.score-value {
  font-size: 48px;
  font-weight: 600;
  margin-bottom: 12px;
}

.teacher-scores {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 20px;
}

.score-item {
  display: flex;
  justify-content: space-between;
  padding: 12px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 6px;
}

.score-item .label {
  font-size: 14px;
}

.score-item .value {
  font-size: 18px;
  font-weight: 600;
}

.score-text {
  margin-left: 8px;
  font-size: 14px;
  color: #606266;
}

.chart-container {
  padding: 16px;
  background: #f9fafb;
  border-radius: 8px;
}

.chart-container h3 {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 16px 0;
  text-align: center;
}

.chart {
  height: 300px;
}

/* 动画 */
@keyframes fade-in-down {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes fade-in-up {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.animate-fade-in-down {
  animation: fade-in-down 0.5s ease-out;
}

.animate-fade-in-up {
  animation: fade-in-up 0.5s ease-out;
}
</style>

