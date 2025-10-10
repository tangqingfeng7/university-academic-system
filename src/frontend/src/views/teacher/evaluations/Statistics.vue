<template>
  <div class="evaluation-statistics-page">
    <!-- 页面头部 -->
    <div class="page-header animate-fade-in-down">
      <div class="header-content">
        <h1 class="page-title">教学评价统计</h1>
        <p class="page-subtitle">查看学生对你的教学评价和反馈</p>
      </div>
    </div>

    <!-- 学期选择 -->
    <div class="filter-section animate-fade-in-up" style="animation-delay: 0.1s;">
      <el-select
        v-model="selectedSemester"
        placeholder="全部学期"
        clearable
        size="large"
        @change="loadStatistics"
        style="width: 200px"
      >
        <el-option label="全部学期" :value="null" />
        <el-option
          v-for="semester in semesters"
          :key="semester.id"
          :label="semester.academicYear + ' ' + getSemesterTypeName(semester.semesterType)"
          :value="semester.id"
        />
      </el-select>
    </div>

    <!-- 统计卡片 -->
    <div 
      class="stats-grid animate-fade-in-up" 
      style="animation-delay: 0.2s;"
      v-loading="loading"
    >
      <div class="stat-card">
        <div class="stat-icon" style="background: #e6f7ff;">
          <el-icon :size="24" color="#1890ff"><Document /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-label">评价总数</div>
          <div class="stat-value">{{ statistics.totalEvaluations || 0 }}</div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon" style="background: #f6ffed;">
          <el-icon :size="24" color="#52c41a"><Star /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-label">教学能力</div>
          <div class="stat-value stat-success">{{ formatRating(statistics.averageTeachingRating) }}</div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon" style="background: #fff7e6;">
          <el-icon :size="24" color="#faad14"><Trophy /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-label">教学态度</div>
          <div class="stat-value stat-warning">{{ formatRating(statistics.averageAttitudeRating) }}</div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon" style="background: #fff1f0;">
          <el-icon :size="24" color="#f5222d"><Reading /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-label">教学内容</div>
          <div class="stat-value stat-danger">{{ formatRating(statistics.averageContentRating) }}</div>
        </div>
      </div>

      <div class="stat-card stat-card-highlight">
        <div class="stat-icon" style="background: #f0f5ff;">
          <el-icon :size="24" color="#2f54eb"><TrendCharts /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-label">综合评分</div>
          <div class="stat-value stat-primary">{{ formatRating(statistics.overallAverageRating) }}</div>
        </div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="charts-section animate-fade-in-up" style="animation-delay: 0.3s;">
      <el-row :gutter="24">
        <!-- 评分趋势图 -->
        <el-col :span="12">
          <div class="chart-card">
            <div class="chart-header">
              <h3>评分分布</h3>
            </div>
            <div ref="ratingChartRef" class="chart-container"></div>
          </div>
        </el-col>

        <!-- 各维度对比图 -->
        <el-col :span="12">
          <div class="chart-card">
            <div class="chart-header">
              <h3>各维度评分对比</h3>
            </div>
            <div ref="dimensionChartRef" class="chart-container"></div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 评价列表 -->
    <div class="evaluations-section animate-fade-in-up" style="animation-delay: 0.4s;">
      <div class="section-header">
        <h3>学生评价</h3>
        <el-button 
          type="primary" 
          :icon="View"
          @click="viewAllEvaluations"
        >
          查看全部
        </el-button>
      </div>

      <div v-loading="evaluationsLoading" class="evaluations-list">
        <el-empty 
          v-if="recentEvaluations.length === 0 && !evaluationsLoading"
          description="暂无评价"
        />

        <div v-else class="evaluation-cards">
          <div 
            v-for="evaluation in recentEvaluations" 
            :key="evaluation.id"
            class="evaluation-card"
          >
            <div class="card-header">
              <div class="course-info">
                <h4>{{ evaluation.courseName }}</h4>
                <span class="semester-text">{{ evaluation.semesterName }}</span>
              </div>
              <div class="rating-display">
                <el-rate 
                  :model-value="getAverageRating(evaluation)" 
                  disabled 
                  show-score
                  :score-template="`${getAverageRating(evaluation).toFixed(1)}`"
                />
              </div>
            </div>

            <div class="card-body">
              <div class="rating-items">
                <div class="rating-item">
                  <span class="label">教学能力：</span>
                  <el-rate :model-value="evaluation.teachingRating" disabled size="small" />
                </div>
                <div class="rating-item">
                  <span class="label">教学态度：</span>
                  <el-rate :model-value="evaluation.attitudeRating" disabled size="small" />
                </div>
                <div class="rating-item">
                  <span class="label">教学内容：</span>
                  <el-rate :model-value="evaluation.contentRating" disabled size="small" />
                </div>
              </div>

              <div v-if="evaluation.comment" class="comment-section">
                <p class="comment-text">{{ evaluation.comment }}</p>
              </div>

              <div class="card-meta">
                <el-tag size="small" type="info">
                  {{ evaluation.anonymous ? '匿名评价' : '实名评价' }}
                </el-tag>
                <span class="time-text">{{ formatTime(evaluation.createdAt) }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Document, Star, Trophy, Reading, TrendCharts, View } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { getTeacherStatistics, getMyTeacherEvaluationList } from '@/api/evaluation'
import { getAllSemesters } from '@/api/semester'

const router = useRouter()
const loading = ref(false)
const evaluationsLoading = ref(false)
const semesters = ref([])
const selectedSemester = ref(null)
const statistics = ref({})
const recentEvaluations = ref([])

const ratingChartRef = ref(null)
const dimensionChartRef = ref(null)
let ratingChart = null
let dimensionChart = null

// 加载学期列表
const loadSemesters = async () => {
  try {
    const res = await getAllSemesters()
    if (res.code === 200) {
      semesters.value = res.data
    }
  } catch (error) {
    ElMessage.error('加载学期列表失败')
  }
}

// 加载统计数据
const loadStatistics = async () => {
  loading.value = true
  try {
    const res = await getTeacherStatistics({
      semesterId: selectedSemester.value
    })
    if (res.code === 200) {
      statistics.value = res.data || {}
      await nextTick()
      initCharts()
    }
  } catch (error) {
    ElMessage.error('加载统计数据失败')
  } finally {
    loading.value = false
  }
}

// 加载最近评价
const loadRecentEvaluations = async () => {
  evaluationsLoading.value = true
  try {
    const res = await getMyTeacherEvaluationList({
      semesterId: selectedSemester.value,
      page: 0,
      size: 5
    })
    if (res.code === 200) {
      recentEvaluations.value = res.data.content || []
    }
  } catch (error) {
    ElMessage.error('加载评价列表失败')
  } finally {
    evaluationsLoading.value = false
  }
}

// 初始化图表
const initCharts = () => {
  initRatingChart()
  initDimensionChart()
}

// 初始化评分分布图
const initRatingChart = () => {
  if (!ratingChartRef.value) return

  if (ratingChart) {
    ratingChart.dispose()
  }

  ratingChart = echarts.init(ratingChartRef.value)

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
      data: ['1星', '2星', '3星', '4星', '5星'],
      axisTick: {
        alignWithLabel: true
      }
    },
    yAxis: {
      type: 'value',
      name: '评价数量'
    },
    series: [
      {
        name: '评价数量',
        type: 'bar',
        barWidth: '60%',
        data: generateRatingDistribution(),
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#83bff6' },
            { offset: 0.5, color: '#188df0' },
            { offset: 1, color: '#188df0' }
          ])
        },
        emphasis: {
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#2378f7' },
              { offset: 0.7, color: '#2378f7' },
              { offset: 1, color: '#83bff6' }
            ])
          }
        }
      }
    ]
  }

  ratingChart.setOption(option)
}

// 初始化维度对比图
const initDimensionChart = () => {
  if (!dimensionChartRef.value) return

  if (dimensionChart) {
    dimensionChart.dispose()
  }

  dimensionChart = echarts.init(dimensionChartRef.value)

  const option = {
    tooltip: {
      trigger: 'axis'
    },
    radar: {
      indicator: [
        { name: '教学能力', max: 5 },
        { name: '教学态度', max: 5 },
        { name: '教学内容', max: 5 }
      ],
      splitNumber: 5,
      name: {
        textStyle: {
          color: '#666'
        }
      },
      splitLine: {
        lineStyle: {
          color: ['#ddd', '#ddd', '#ddd', '#ddd', '#ddd']
        }
      },
      splitArea: {
        show: true,
        areaStyle: {
          color: ['rgba(250, 250, 250, 0.3)', 'rgba(200, 200, 200, 0.3)']
        }
      }
    },
    series: [
      {
        name: '评分',
        type: 'radar',
        data: [
          {
            value: [
              statistics.value.averageTeachingRating || 0,
              statistics.value.averageAttitudeRating || 0,
              statistics.value.averageContentRating || 0
            ],
            name: '我的评分',
            areaStyle: {
              color: 'rgba(24, 144, 255, 0.3)'
            },
            lineStyle: {
              color: '#1890ff',
              width: 2
            },
            itemStyle: {
              color: '#1890ff'
            }
          }
        ]
      }
    ]
  }

  dimensionChart.setOption(option)
}

// 生成评分分布数据（模拟数据，实际应从后端获取）
const generateRatingDistribution = () => {
  // 这里简化处理，实际应该从statistics中获取各星级的分布数据
  const total = statistics.value.totalEvaluations || 0
  const avg = statistics.value.overallAverageRating || 0
  
  // 模拟正态分布
  if (total === 0) return [0, 0, 0, 0, 0]
  
  const distribution = []
  const center = Math.round(avg)
  for (let i = 1; i <= 5; i++) {
    const distance = Math.abs(i - center)
    const value = Math.round(total * Math.exp(-distance * distance / 2) / 2.5)
    distribution.push(value)
  }
  return distribution
}

// 计算平均评分
const getAverageRating = (evaluation) => {
  return (
    (evaluation.teachingRating + evaluation.attitudeRating + evaluation.contentRating) / 3
  )
}

// 格式化评分
const formatRating = (rating) => {
  return rating ? rating.toFixed(1) : '0.0'
}

// 获取学期类型名称
const getSemesterTypeName = (type) => {
  return type === 1 ? '春季学期' : '秋季学期'
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleString('zh-CN', { 
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
}

// 查看全部评价
const viewAllEvaluations = () => {
  router.push('/teacher/evaluations/list')
}

// 窗口大小变化时重新渲染图表
const handleResize = () => {
  ratingChart?.resize()
  dimensionChart?.resize()
}

onMounted(() => {
  loadSemesters()
  loadStatistics()
  loadRecentEvaluations()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  ratingChart?.dispose()
  dimensionChart?.dispose()
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.evaluation-statistics-page {
  padding: 24px;
}

.page-header {
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

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  display: flex;
  align-items: center;
  gap: 16px;
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.12);
}

.stat-card-highlight {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.stat-card-highlight .stat-label {
  color: rgba(255, 255, 255, 0.9);
}

.stat-card-highlight .stat-value {
  color: white;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-content {
  flex: 1;
}

.stat-label {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #1a1a1a;
}

.stat-success {
  color: #52c41a;
}

.stat-warning {
  color: #faad14;
}

.stat-danger {
  color: #f5222d;
}

.stat-primary {
  color: #1890ff;
}

.charts-section {
  margin-bottom: 24px;
}

.chart-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.chart-header {
  margin-bottom: 16px;
}

.chart-header h3 {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0;
}

.chart-container {
  height: 300px;
}

.evaluations-section {
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
}

.section-header h3 {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0;
}

.evaluations-list {
  min-height: 200px;
}

.evaluation-cards {
  display: grid;
  gap: 16px;
}

.evaluation-card {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 20px;
  transition: all 0.3s ease;
}

.evaluation-card:hover {
  border-color: #409eff;
  box-shadow: 0 2px 12px rgba(64, 158, 255, 0.12);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e4e7ed;
}

.course-info h4 {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 4px 0;
}

.semester-text {
  font-size: 12px;
  color: #909399;
}

.rating-display {
  flex-shrink: 0;
}

.card-body {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.rating-items {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.rating-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.rating-item .label {
  font-size: 12px;
  color: #909399;
}

.comment-section {
  padding: 12px;
  background: #f5f7fa;
  border-radius: 6px;
}

.comment-text {
  margin: 0;
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}

.card-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.time-text {
  font-size: 12px;
  color: #909399;
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

