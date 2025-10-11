<template>
  <div class="scholarship-statistics">
    <el-card shadow="never" class="header-card">
      <h2 class="page-title">
        <el-icon><DataAnalysis /></el-icon>
        奖学金统计
      </h2>
      <p class="page-subtitle">奖学金获奖数据分析与统计</p>
    </el-card>

    <!-- 筛选栏 -->
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" :model="queryForm">
        <el-form-item label="学年">
          <el-select v-model="queryForm.academicYear" placeholder="请选择学年">
            <el-option
              v-for="year in academicYears"
              :key="year"
              :label="year"
              :value="year"
            />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="loadStatistics">
            查询
          </el-button>
          <el-button :icon="Download" @click="handleExportReport">
            导出报告
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <el-card shadow="hover" class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
          <el-icon :size="30"><User /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ totalStudents }}</div>
          <div class="stat-label">获奖人数</div>
        </div>
      </el-card>

      <el-card shadow="hover" class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);">
          <el-icon :size="30"><Trophy /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ totalAwards }}</div>
          <div class="stat-label">获奖人次</div>
        </div>
      </el-card>

      <el-card shadow="hover" class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);">
          <el-icon :size="30"><Money /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">¥{{ totalAmount.toLocaleString() }}</div>
          <div class="stat-label">奖金总额</div>
        </div>
      </el-card>

      <el-card shadow="hover" class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);">
          <el-icon :size="30"><Stamp /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ coverageRate }}%</div>
          <div class="stat-label">覆盖率</div>
        </div>
      </el-card>
    </div>

    <!-- 图表区域 -->
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card shadow="never">
          <h3 class="chart-title">按等级分布</h3>
          <div ref="levelChartRef" class="chart-container"></div>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card shadow="never">
          <h3 class="chart-title">按专业分布</h3>
          <div ref="majorChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card shadow="never">
          <h3 class="chart-title">按年级分布</h3>
          <div ref="gradeChartRef" class="chart-container"></div>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card shadow="never">
          <h3 class="chart-title">年度趋势</h3>
          <div ref="trendChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import {
  DataAnalysis,
  Download,
  User,
  Trophy,
  Money,
  Stamp
} from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import {
  getDistribution,
  getStatsByMajor,
  getStatsByGrade,
  generateReport,
  getYearlyComparison
} from '@/api/scholarship'

const academicYears = ref([])
const levelChartRef = ref()
const majorChartRef = ref()
const gradeChartRef = ref()
const trendChartRef = ref()

const queryForm = reactive({
  academicYear: ''
})

// 统计数据
const totalStudents = ref(0)
const totalAwards = ref(0)
const totalAmount = ref(0)
const coverageRate = ref(0)

// 初始化图表
let levelChart = null
let majorChart = null
let gradeChart = null
let trendChart = null

// 加载统计数据
const loadStatistics = async () => {
  await Promise.all([
    loadDistribution(),
    loadMajorStats(),
    loadGradeStats(),
    loadYearlyTrend()
  ])
}

// 加载等级分布
const loadDistribution = async () => {
  try {
    const res = await getDistribution(queryForm.academicYear)
    const data = res.data || {}
    
    // 更新统计卡片（使用后端返回的字段名）
    totalStudents.value = data.totalAwardees || 0  // 获奖人数（去重）
    totalAwards.value = data.totalAwardees || 0    // 获奖人次（与人数相同）
    totalAmount.value = data.totalAmount || 0
    // 覆盖率 = 获奖人数 / 总学生数
    // 使用估算的学生总数（实际约700人，这里使用估算值）
    // TODO: 后端应该在AwardStatisticsDTO中添加totalStudents字段
    const estimatedTotalStudents = 700
    coverageRate.value = totalStudents.value > 0 
      ? ((totalStudents.value / estimatedTotalStudents) * 100).toFixed(1)
      : 0

    // 绘制饼图（byLevel是Map对象，需要转换）
    await nextTick()
    if (!levelChart) {
      levelChart = echarts.init(levelChartRef.value)
    }

    const levelMap = data.byLevel || {}
    const levelData = Object.entries(levelMap).map(([level, count]) => ({
      name: level,  // level已经是中文描述
      value: count
    }))
    
    levelChart.setOption({
      tooltip: {
        trigger: 'item',
        formatter: '{b}: {c}人 ({d}%)'
      },
      legend: {
        orient: 'vertical',
        left: 'left'
      },
      series: [
        {
          type: 'pie',
          radius: '50%',
          data: levelData,
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          }
        }
      ]
    })
  } catch (error) {
    console.error('加载等级分布失败:', error)
  }
}

// 加载专业统计
const loadMajorStats = async () => {
  try {
    const res = await getStatsByMajor(queryForm.academicYear, null)
    // 后端返回的是 Map<String, Integer>，需要转换为数组
    const dataMap = res.data || {}
    const data = Object.entries(dataMap).map(([majorName, count]) => ({
      majorName,
      count
    }))

    await nextTick()
    if (!majorChart) {
      majorChart = echarts.init(majorChartRef.value)
    }

    majorChart.setOption({
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'shadow'
        }
      },
      xAxis: {
        type: 'category',
        data: data.map(item => item.majorName),
        axisLabel: {
          rotate: 45,
          interval: 0
        }
      },
      yAxis: {
        type: 'value'
      },
      series: [
        {
          name: '获奖人数',
          type: 'bar',
          data: data.map(item => item.count),
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#667eea' },
              { offset: 1, color: '#764ba2' }
            ])
          }
        }
      ]
    })
  } catch (error) {
    console.error('加载专业统计失败:', error)
  }
}

// 加载年级统计
const loadGradeStats = async () => {
  try {
    const res = await getStatsByGrade(queryForm.academicYear)
    // 后端返回的是 Map<Integer, Integer>，需要转换为数组
    const dataMap = res.data || {}
    const data = Object.entries(dataMap).map(([grade, count]) => ({
      grade: parseInt(grade),
      count
    })).sort((a, b) => a.grade - b.grade)

    await nextTick()
    if (!gradeChart) {
      gradeChart = echarts.init(gradeChartRef.value)
    }

    gradeChart.setOption({
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'shadow'
        }
      },
      xAxis: {
        type: 'category',
        data: data.map(item => `${item.grade}级`)
      },
      yAxis: {
        type: 'value'
      },
      series: [
        {
          name: '获奖人数',
          type: 'bar',
          data: data.map(item => item.count),
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#f093fb' },
              { offset: 1, color: '#f5576c' }
            ])
          }
        }
      ]
    })
  } catch (error) {
    console.error('加载年级统计失败:', error)
  }
}

// 加载年度趋势
const loadYearlyTrend = async () => {
  try {
    const currentYear = parseInt(queryForm.academicYear.split('-')[0])
    const startYear = `${currentYear - 4}-${currentYear - 3}`
    const endYear = queryForm.academicYear
    const res = await getYearlyComparison(startYear, endYear)
    const data = res.data || []

    await nextTick()
    if (!trendChart) {
      trendChart = echarts.init(trendChartRef.value)
    }

    trendChart.setOption({
      tooltip: {
        trigger: 'axis'
      },
      legend: {
        data: ['获奖人数', '奖金总额(万元)']
      },
      xAxis: {
        type: 'category',
        data: data.map(item => item.academicYear)  // 使用 academicYear 字段
      },
      yAxis: [
        {
          type: 'value',
          name: '获奖人数'
        },
        {
          type: 'value',
          name: '奖金总额(万元)'
        }
      ],
      series: [
        {
          name: '获奖人数',
          type: 'line',
          data: data.map(item => item.totalAwardees),  // 使用 totalAwardees 字段
          smooth: true,
          itemStyle: { color: '#667eea' }
        },
        {
          name: '奖金总额(万元)',
          type: 'line',
          yAxisIndex: 1,
          data: data.map(item => (item.totalAmount / 10000).toFixed(2)),  // 使用 totalAmount 字段
          smooth: true,
          itemStyle: { color: '#f093fb' }
        }
      ]
    })
  } catch (error) {
    console.error('加载年度趋势失败:', error)
  }
}

// 导出报告
const handleExportReport = async () => {
  try {
    const blob = await generateReport(queryForm.academicYear)
    
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `奖学金统计报告_${queryForm.academicYear}.xlsx`
    link.click()
    window.URL.revokeObjectURL(url)
    
    ElMessage.success('导出成功！')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  }
}

// 获取等级文本
const getLevelText = (level) => {
  const texts = {
    NATIONAL: '国家级',
    PROVINCIAL: '省级',
    UNIVERSITY: '校级',
    DEPARTMENT: '院系级'
  }
  return texts[level] || level
}

onMounted(() => {
  // 生成学年列表（从2023年开始，往后5年）
  const currentYear = new Date().getFullYear()
  const years = []
  for (let i = -1; i < 4; i++) {
    const year = currentYear - i
    years.push(`${year}-${year + 1}`)
  }
  academicYears.value = years.sort().reverse()
  // 默认选择2024-2025学年（当前有数据的学年）
  queryForm.academicYear = '2024-2025'

  loadStatistics()

  // 窗口大小改变时重绘图表
  window.addEventListener('resize', () => {
    levelChart?.resize()
    majorChart?.resize()
    gradeChart?.resize()
    trendChart?.resize()
  })
})
</script>

<style scoped>
.scholarship-statistics {
  padding: 20px;
}

.header-card {
  margin-bottom: 20px;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 0 0 10px 0;
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.page-subtitle {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.filter-card {
  margin-bottom: 20px;
}

.filter-card :deep(.el-form) {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
}

.filter-card :deep(.el-form-item) {
  margin-bottom: 0;
}

.filter-card :deep(.el-select) {
  width: 200px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 20px;
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-5px);
}

.stat-icon {
  flex-shrink: 0;
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  color: white;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.chart-title {
  margin: 0 0 20px 0;
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.chart-container {
  width: 100%;
  height: 350px;
}
</style>

