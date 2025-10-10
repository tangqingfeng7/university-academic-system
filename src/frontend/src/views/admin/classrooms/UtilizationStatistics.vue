<template>
  <div class="utilization-statistics-container">
    <!-- 顶部搜索栏 -->
    <div class="search-section">
      <el-card class="search-card" :body-style="{ padding: '24px' }">
        <el-form :inline="true" :model="searchForm">
          <el-form-item>
            <div class="form-label">
              <el-icon><Calendar /></el-icon>
              <span>统计周期</span>
            </div>
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
              @change="handleSearch"
            style="width: 300px"
          />
        </el-form-item>

          <el-form-item>
            <div class="form-label">
              <el-icon><OfficeBuilding /></el-icon>
              <span>楼栋筛选</span>
            </div>
          <el-input
              v-model="searchForm.building"
            placeholder="请输入楼栋"
            clearable
            style="width: 150px"
              @change="handleSearch"
          />
        </el-form-item>

          <el-form-item>
            <div class="form-label">
              <el-icon><Grid /></el-icon>
              <span>教室类型</span>
            </div>
          <el-select
              v-model="searchForm.type"
            placeholder="请选择类型"
            clearable
            style="width: 150px"
              @change="handleSearch"
          >
            <el-option label="普通教室" value="LECTURE" />
            <el-option label="实验室" value="LAB" />
            <el-option label="多媒体教室" value="MULTIMEDIA" />
            <el-option label="会议室" value="CONFERENCE" />
          </el-select>
        </el-form-item>

        <el-form-item>
            <el-button type="primary" @click="handleExport" :loading="exportLoading">
            <el-icon><Download /></el-icon>
            导出报告
          </el-button>
            <el-button @click="handleRefresh" :loading="overviewLoading">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
        </el-form-item>
      </el-form>
    </el-card>
    </div>

    <!-- 核心指标卡片 -->
    <div class="metrics-section">
        <el-row :gutter="20">
          <el-col :span="6">
          <div class="metric-card metric-card-1" v-loading="overviewLoading">
            <div class="metric-icon">
              <el-icon><School /></el-icon>
            </div>
            <div class="metric-content">
              <div class="metric-label">教室总数</div>
              <div class="metric-value">
                {{ overview.totalClassrooms }}
              </div>
              <div class="metric-sub">
                <span class="metric-trend up">
                  <el-icon><TrendCharts /></el-icon>
                  较上期 +{{ overview.totalClassroomChange || 0 }}
                </span>
              </div>
            </div>
          </div>
          </el-col>

          <el-col :span="6">
          <div class="metric-card metric-card-2" v-loading="overviewLoading">
            <div class="metric-icon">
              <el-icon><PieChart /></el-icon>
            </div>
            <div class="metric-content">
              <div class="metric-label">平均使用率</div>
              <div class="metric-value">
                {{ overview.averageUtilization.toFixed(1) }}
                <span class="metric-unit">%</span>
              </div>
              <div class="metric-progress">
                <el-progress
                  :percentage="overview.averageUtilization"
                  :color="getProgressColor(overview.averageUtilization)"
                  :show-text="false"
                  :stroke-width="6"
                />
              </div>
            </div>
          </div>
          </el-col>

          <el-col :span="6">
          <div class="metric-card metric-card-3" v-loading="overviewLoading">
            <div class="metric-icon">
              <el-icon><DataAnalysis /></el-icon>
            </div>
            <div class="metric-content">
              <div class="metric-label">高使用率教室</div>
              <div class="metric-value">
                {{ overview.highUtilizationCount }}
              </div>
              <div class="metric-sub">
                <span class="metric-badge success">
                  ≥90% 使用率
                </span>
              </div>
            </div>
          </div>
          </el-col>

          <el-col :span="6">
          <div class="metric-card metric-card-4" v-loading="overviewLoading">
            <div class="metric-icon">
              <el-icon><WarningFilled /></el-icon>
            </div>
            <div class="metric-content">
              <div class="metric-label">低使用率教室</div>
              <div class="metric-value warning">
                {{ overview.lowUtilizationCount }}
              </div>
              <div class="metric-sub">
                <span class="metric-badge warning">
                  <30% 需优化
                </span>
              </div>
            </div>
          </div>
          </el-col>
        </el-row>
    </div>

    <!-- 智能分析卡片 -->
    <div class="insights-section" v-if="insights.length > 0">
      <el-card class="insights-card">
        <template #header>
          <div class="card-header">
            <span class="card-title">
              <el-icon><MagicStick /></el-icon>
              智能分析
            </span>
          </div>
        </template>
        <div class="insights-list">
          <div 
            v-for="(insight, index) in insights" 
            :key="index" 
            class="insight-item"
            :class="'insight-' + insight.type"
          >
            <div class="insight-icon">
              <el-icon v-if="insight.type === 'success'"><CircleCheckFilled /></el-icon>
              <el-icon v-else-if="insight.type === 'warning'"><WarningFilled /></el-icon>
              <el-icon v-else><InfoFilled /></el-icon>
            </div>
            <div class="insight-content">
              <div class="insight-title">{{ insight.title }}</div>
              <div class="insight-desc">{{ insight.description }}</div>
            </div>
          </div>
      </div>
    </el-card>
    </div>

    <!-- 图表展示区域 -->
    <div class="charts-section">
      <!-- 第一行图表 -->
      <el-row :gutter="20">
        <el-col :span="16">
          <el-card class="chart-card" v-loading="chartLoading">
          <template #header>
              <div class="card-header">
                <span class="card-title">
                  <el-icon><TrendCharts /></el-icon>
                  使用率趋势分析
                </span>
                <el-radio-group v-model="trendPeriod" size="small" @change="fetchTrendData">
                  <el-radio-button label="week">最近一周</el-radio-button>
                  <el-radio-button label="month">最近一月</el-radio-button>
                  <el-radio-button label="custom">自定义</el-radio-button>
                </el-radio-group>
              </div>
          </template>
            <div ref="trendChartRef" class="chart-container"></div>
        </el-card>
      </el-col>

        <el-col :span="8">
          <el-card class="chart-card" v-loading="chartLoading">
          <template #header>
              <div class="card-header">
                <span class="card-title">
                  <el-icon><PieChart /></el-icon>
                  使用率分布
                </span>
              </div>
          </template>
            <div ref="distributionChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

      <!-- 第二行图表 -->
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
          <el-card class="chart-card" v-loading="chartLoading">
          <template #header>
              <div class="card-header">
                <span class="card-title">
                  <el-icon><OfficeBuilding /></el-icon>
                  楼栋使用率对比
                </span>
              </div>
          </template>
            <div ref="buildingChartRef" class="chart-container"></div>
        </el-card>
      </el-col>

      <el-col :span="12">
          <el-card class="chart-card" v-loading="chartLoading">
          <template #header>
              <div class="card-header">
                <span class="card-title">
                  <el-icon><Grid /></el-icon>
                  类型使用率对比
                </span>
              </div>
          </template>
            <div ref="typeChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

      <!-- 第三行图表 -->
      <el-row :gutter="20" style="margin-top: 20px">
        <el-col :span="24">
          <el-card class="chart-card" v-loading="chartLoading">
            <template #header>
              <div class="card-header">
                <span class="card-title">
                  <el-icon><Histogram /></el-icon>
                  使用率排名 TOP 15
                </span>
              </div>
            </template>
            <div ref="topChartRef" class="chart-container" style="height: 500px"></div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 异常教室列表 -->
    <div class="abnormal-section">
      <el-card class="data-card">
      <template #header>
        <div class="card-header">
            <span class="card-title">
              <el-icon><Warning /></el-icon>
              异常使用率教室
            </span>
            <el-segmented v-model="abnormalType" :options="abnormalOptions" @change="fetchAbnormalClassrooms" />
        </div>
      </template>

      <el-table
        v-loading="abnormalLoading"
        :data="abnormalClassrooms"
        style="width: 100%"
          :row-class-name="getRowClassName"
          @row-click="handleViewDetail"
      >
          <el-table-column type="index" label="#" width="60" align="center" />
          
          <el-table-column label="教室信息" min-width="200">
          <template #default="{ row }">
              <div class="classroom-info">
                <div class="classroom-name">
                  <el-icon><Location /></el-icon>
            {{ row.classroom?.roomNo || '-' }}
                </div>
                <div class="classroom-meta">
                  {{ row.classroom?.building || '-' }} · 容量 {{ row.classroom?.capacity || 0 }}人
                </div>
              </div>
          </template>
        </el-table-column>

          <el-table-column label="类型" width="120" align="center">
          <template #default="{ row }">
              <el-tag :type="getTypeTag(row.classroom?.type)" effect="light">
              {{ getTypeName(row.classroom?.type) }}
            </el-tag>
          </template>
        </el-table-column>

          <el-table-column label="使用率" width="200" align="center">
          <template #default="{ row }">
              <div class="utilization-cell">
            <el-progress
              :percentage="row.utilizationRate"
                  :color="getUtilizationGradient(row.utilizationRate)"
                  :stroke-width="12"
            />
              </div>
          </template>
        </el-table-column>

          <el-table-column label="总使用时长" width="150" align="center">
          <template #default="{ row }">
              <div class="time-cell">
                <el-icon><Timer /></el-icon>
            {{ formatMinutes(row.totalUsageMinutes) }}
              </div>
          </template>
        </el-table-column>

          <el-table-column label="使用详情" width="250" align="center">
          <template #default="{ row }">
              <div class="usage-details">
                <el-tag size="small" type="primary" effect="plain">
                  课程 {{ row.courseUsageCount || 0 }}
                </el-tag>
                <el-tag size="small" type="success" effect="plain">
                  考试 {{ row.examUsageCount || 0 }}
                </el-tag>
                <el-tag size="small" type="warning" effect="plain">
                  借用 {{ row.bookingUsageCount || 0 }}
                </el-tag>
              </div>
          </template>
        </el-table-column>

          <el-table-column label="操作" width="100" align="center" fixed="right">
          <template #default="{ row }">
              <el-button type="primary" link @click.stop="handleViewDetail(row)">
              <el-icon><View /></el-icon>
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    </div>

    <!-- 使用率排名 -->
    <div class="ranking-section">
      <el-card class="data-card">
      <template #header>
          <div class="card-header">
            <span class="card-title">
              <el-icon><Trophy /></el-icon>
              使用率排名榜
            </span>
          </div>
      </template>

      <el-table
        v-loading="rankingLoading"
        :data="rankingList"
        style="width: 100%"
      >
          <el-table-column label="排名" width="80" align="center">
          <template #default="{ $index }">
              <div class="rank-cell">
                <el-icon v-if="$index === 0" class="rank-icon gold"><Medal /></el-icon>
                <el-icon v-else-if="$index === 1" class="rank-icon silver"><Medal /></el-icon>
                <el-icon v-else-if="$index === 2" class="rank-icon bronze"><Medal /></el-icon>
                <span v-else class="rank-number">{{ $index + 1 }}</span>
              </div>
          </template>
        </el-table-column>

          <el-table-column label="教室信息" min-width="200">
          <template #default="{ row }">
              <div class="classroom-info">
                <div class="classroom-name">
                  <el-icon><Location /></el-icon>
            {{ row.classroom?.roomNo || '-' }}
                </div>
                <div class="classroom-meta">
                  {{ row.classroom?.building || '-' }} · {{ getTypeName(row.classroom?.type) }}
                </div>
              </div>
          </template>
        </el-table-column>

        <el-table-column label="容量" width="100" align="center">
          <template #default="{ row }">
              <el-tag type="info" effect="plain">
            {{ row.classroom?.capacity || 0 }}人
            </el-tag>
          </template>
        </el-table-column>

          <el-table-column label="使用率" width="200" align="center">
          <template #default="{ row }">
              <div class="utilization-cell">
            <el-progress
              :percentage="row.utilizationRate"
                  :color="getUtilizationGradient(row.utilizationRate)"
                  :stroke-width="12"
            />
              </div>
          </template>
        </el-table-column>

          <el-table-column label="总使用时长" width="150" align="center">
          <template #default="{ row }">
              <div class="time-cell">
                <el-icon><Timer /></el-icon>
            {{ formatMinutes(row.totalUsageMinutes) }}
              </div>
          </template>
        </el-table-column>

          <el-table-column label="使用明细" min-width="250">
          <template #default="{ row }">
              <div class="usage-details">
                <el-tag size="small" type="primary" effect="plain">
                  课程 {{ row.courseUsageCount || 0 }}
                </el-tag>
                <el-tag size="small" type="success" effect="plain">
                  考试 {{ row.examUsageCount || 0 }}
                </el-tag>
                <el-tag size="small" type="warning" effect="plain">
                  借用 {{ row.bookingUsageCount || 0 }}
                </el-tag>
              </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import {
  getUtilizationReport,
  getAbnormalClassrooms,
  getUtilizationRanking,
  exportUtilizationReport
} from '@/api/adminClassroom'

const router = useRouter()

// 搜索表单
const searchForm = reactive({
  building: '',
  type: ''
})

// 日期范围
const dateRange = ref([])
const trendPeriod = ref('month')

// 概览数据
const overviewLoading = ref(false)
const overview = reactive({
  totalClassrooms: 0,
  totalClassroomChange: 0,
  averageUtilization: 0,
  highUtilizationCount: 0,
  lowUtilizationCount: 0
})

// 智能分析
const insights = ref([])

// 图表
const chartLoading = ref(false)
const distributionChartRef = ref(null)
const buildingChartRef = ref(null)
const typeChartRef = ref(null)
const topChartRef = ref(null)
const trendChartRef = ref(null)

let distributionChart = null
let buildingChart = null
let typeChart = null
let topChart = null
let trendChart = null

// 异常教室
const abnormalLoading = ref(false)
const abnormalType = ref('LOW')
const abnormalOptions = [
  { label: '使用率过低 (<30%)', value: 'LOW' },
  { label: '使用率过高 (>90%)', value: 'HIGH' }
]
const abnormalClassrooms = ref([])

// 排名
const rankingLoading = ref(false)
const rankingList = ref([])

// 导出
const exportLoading = ref(false)

// 搜索
const handleSearch = () => {
  fetchUtilizationReport()
  fetchAbnormalClassrooms()
  fetchRanking()
}

// 刷新
const handleRefresh = () => {
  handleSearch()
}

// 获取使用率报告
const fetchUtilizationReport = async () => {
  if (!dateRange.value || dateRange.value.length !== 2) return
  
  overviewLoading.value = true
  chartLoading.value = true
  try {
    const params = {
      startDate: formatDate(dateRange.value[0]),
      endDate: formatDate(dateRange.value[1]),
      building: searchForm.building || undefined,
      type: searchForm.type || undefined
    }
    const response = await getUtilizationReport(params)
    const data = response.data
    
    // 更新概览数据
    overview.totalClassrooms = data.totalClassrooms || 0
    overview.totalClassroomChange = data.totalClassroomChange || 0
    overview.averageUtilization = Math.round((data.averageUtilization || 0) * 10) / 10
    overview.highUtilizationCount = data.highUtilizationCount || 0
    overview.lowUtilizationCount = data.lowUtilizationCount || 0
    
    // 生成智能分析
    generateInsights(data)
    
    // 更新图表
    await nextTick()
    updateCharts(data)
  } catch (error) {
    ElMessage.error(error.message || '获取使用率报告失败')
  } finally {
    overviewLoading.value = false
    chartLoading.value = false
  }
}

// 生成智能分析
const generateInsights = (data) => {
  const newInsights = []
  
  if (data.averageUtilization >= 70) {
    newInsights.push({
      type: 'success',
      title: '整体使用率良好',
      description: `平均使用率达到 ${data.averageUtilization.toFixed(1)}%，教室资源利用充分`
    })
  } else if (data.averageUtilization < 50) {
    newInsights.push({
      type: 'warning',
      title: '整体使用率偏低',
      description: `平均使用率仅 ${data.averageUtilization.toFixed(1)}%，建议优化排课策略`
    })
  }
  
  if (data.lowUtilizationCount > data.totalClassrooms * 0.3) {
    newInsights.push({
      type: 'warning',
      title: '低使用率教室较多',
      description: `有 ${data.lowUtilizationCount} 间教室使用率低于30%，占比${((data.lowUtilizationCount / data.totalClassrooms) * 100).toFixed(1)}%`
    })
  }
  
  if (data.highUtilizationCount > 0) {
    newInsights.push({
      type: 'info',
      title: '高使用率教室',
      description: `有 ${data.highUtilizationCount} 间教室使用率超过90%，可能存在资源紧张`
    })
  }
  
  // 楼栋分析
  if (data.buildingUtilizations && data.buildingUtilizations.length > 0) {
    const maxBuilding = data.buildingUtilizations.reduce((prev, curr) => 
      prev.avgUtilizationRate > curr.avgUtilizationRate ? prev : curr
    )
    const minBuilding = data.buildingUtilizations.reduce((prev, curr) => 
      prev.avgUtilizationRate < curr.avgUtilizationRate ? prev : curr
    )
    
    if (maxBuilding.avgUtilizationRate - minBuilding.avgUtilizationRate > 30) {
      newInsights.push({
        type: 'info',
        title: '楼栋使用不均衡',
        description: `${maxBuilding.building}使用率最高(${maxBuilding.avgUtilizationRate.toFixed(1)}%)，${minBuilding.building}最低(${minBuilding.avgUtilizationRate.toFixed(1)}%)`
      })
    }
  }
  
  insights.value = newInsights
}

// 获取异常教室
const fetchAbnormalClassrooms = async () => {
  if (!dateRange.value || dateRange.value.length !== 2) return
  
  abnormalLoading.value = true
  try {
    const params = {
      startDate: formatDate(dateRange.value[0]),
      endDate: formatDate(dateRange.value[1]),
      type: abnormalType.value
    }
    const response = await getAbnormalClassrooms(params)
    abnormalClassrooms.value = response.data || []
  } catch (error) {
    ElMessage.error(error.message || '获取异常教室失败')
  } finally {
    abnormalLoading.value = false
  }
}

// 获取排名
const fetchRanking = async () => {
  if (!dateRange.value || dateRange.value.length !== 2) return
  
  rankingLoading.value = true
  try {
    const params = {
      startDate: formatDate(dateRange.value[0]),
      endDate: formatDate(dateRange.value[1]),
      limit: 20
    }
    const response = await getUtilizationRanking(params)
    rankingList.value = response.data || []
  } catch (error) {
    ElMessage.error(error.message || '获取使用率排名失败')
  } finally {
    rankingLoading.value = false
  }
}

// 获取趋势数据
const fetchTrendData = () => {
  // 这里可以根据 trendPeriod 获取不同时期的趋势数据
  fetchUtilizationReport()
}

// 更新图表
const updateCharts = (data) => {
  updateDistributionChart(data)
  updateBuildingChart(data)
  updateTypeChart(data)
  updateTopChart(data)
  updateTrendChart(data)
}

// 使用率分布图（环形图）
const updateDistributionChart = (data) => {
  if (!distributionChartRef.value) return
  
    if (!distributionChart) {
      distributionChart = echarts.init(distributionChartRef.value)
    }
    
    const distributionData = [
    { name: '低使用率\n0-30%', value: data.lowUtilizationCount || 0 },
    { name: '中等使用率\n30-60%', value: data.mediumUtilizationCount || 0 },
    { name: '正常使用率\n60-90%', value: data.normalUtilizationCount || 0 },
    { name: '高使用率\n90-100%', value: data.highUtilizationCount || 0 }
    ]
    
    distributionChart.setOption({
      tooltip: {
        trigger: 'item',
      formatter: '{b}: {c}间 ({d}%)',
      backgroundColor: 'rgba(0, 0, 0, 0.8)',
      borderColor: 'transparent',
      textStyle: {
        color: '#fff'
      }
      },
      legend: {
      bottom: 10,
      left: 'center',
      textStyle: {
        fontSize: 12
      }
      },
      series: [{
        type: 'pie',
      radius: ['50%', '75%'],
      avoidLabelOverlap: true,
        itemStyle: {
        borderRadius: 8,
          borderColor: '#fff',
        borderWidth: 3
        },
        label: {
        show: true,
        position: 'center',
        formatter: function() {
          return `总计\n${data.totalClassrooms || 0}间`
        },
        fontSize: 18,
        fontWeight: 'bold',
        color: '#333'
        },
        emphasis: {
          label: {
            show: true,
          fontSize: 20,
            fontWeight: 'bold'
        },
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.3)'
          }
        },
      labelLine: {
        show: false
      },
        data: distributionData,
      color: [
        { type: 'linear', x: 0, y: 0, x2: 0, y2: 1, colorStops: [
          { offset: 0, color: '#ff6b6b' },
          { offset: 1, color: '#ee5a52' }
        ]},
        { type: 'linear', x: 0, y: 0, x2: 0, y2: 1, colorStops: [
          { offset: 0, color: '#ffd93d' },
          { offset: 1, color: '#f6c445' }
        ]},
        { type: 'linear', x: 0, y: 0, x2: 0, y2: 1, colorStops: [
          { offset: 0, color: '#6bcf7f' },
          { offset: 1, color: '#51cf66' }
        ]},
        { type: 'linear', x: 0, y: 0, x2: 0, y2: 1, colorStops: [
          { offset: 0, color: '#4dabf7' },
          { offset: 1, color: '#339af0' }
        ]}
      ]
      }]
    })
  }
  
// 楼栋使用率对比（柱状图）
const updateBuildingChart = (data) => {
  if (!buildingChartRef.value) return
  if (!data.buildingUtilizations || data.buildingUtilizations.length === 0) return
  
    if (!buildingChart) {
      buildingChart = echarts.init(buildingChartRef.value)
    }
    
    const buildings = data.buildingUtilizations.map(item => item.building)
    const rates = data.buildingUtilizations.map(item => item.avgUtilizationRate)
    
    buildingChart.setOption({
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'shadow'
      },
      backgroundColor: 'rgba(0, 0, 0, 0.8)',
      borderColor: 'transparent',
      textStyle: {
        color: '#fff'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '10%',
      top: '10%',
      containLabel: true
    },
      xAxis: {
        type: 'category',
        data: buildings,
        axisLabel: {
        rotate: 30,
        fontSize: 12
      },
      axisLine: {
        lineStyle: {
          color: '#ddd'
        }
        }
      },
      yAxis: {
        type: 'value',
        name: '使用率(%)',
      max: 100,
      axisLabel: {
        formatter: '{value}%'
      },
      splitLine: {
        lineStyle: {
          type: 'dashed',
          color: '#f0f0f0'
        }
      }
      },
      series: [{
        type: 'bar',
      data: rates.map(rate => ({
        value: rate,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#4dabf7' },
            { offset: 1, color: '#339af0' }
          ]),
          borderRadius: [6, 6, 0, 0]
        }
      })),
      barWidth: '50%',
      label: {
        show: true,
        position: 'top',
        formatter: '{c}%',
        fontSize: 12,
        fontWeight: 'bold'
        }
      }]
    })
  }
  
// 类型使用率对比（雷达图）
const updateTypeChart = (data) => {
  if (!typeChartRef.value) return
  if (!data.typeUtilizations || data.typeUtilizations.length === 0) return
  
    if (!typeChart) {
      typeChart = echarts.init(typeChartRef.value)
    }
    
    const typeNames = {
      LECTURE: '普通教室',
      LAB: '实验室',
      MULTIMEDIA: '多媒体教室',
      CONFERENCE: '会议室'
    }
    
  const indicator = data.typeUtilizations.map(item => ({
    name: typeNames[item.type] || item.type,
    max: 100
  }))
  
  const values = data.typeUtilizations.map(item => item.avgUtilizationRate)
    
    typeChart.setOption({
      tooltip: {
      backgroundColor: 'rgba(0, 0, 0, 0.8)',
      borderColor: 'transparent',
      textStyle: {
        color: '#fff'
      }
    },
    radar: {
      indicator: indicator,
      shape: 'circle',
      splitNumber: 5,
      name: {
        textStyle: {
          color: '#333',
          fontSize: 12
        }
      },
      splitLine: {
        lineStyle: {
          color: ['#f0f0f0', '#f0f0f0', '#f0f0f0', '#f0f0f0', '#f0f0f0']
        }
      },
      splitArea: {
        show: true,
        areaStyle: {
          color: ['rgba(77, 171, 247, 0.05)', 'rgba(77, 171, 247, 0.1)']
        }
      },
      axisLine: {
        lineStyle: {
          color: '#ddd'
        }
      }
      },
      series: [{
      type: 'radar',
      data: [{
        value: values,
        name: '使用率',
        areaStyle: {
          color: new echarts.graphic.RadialGradient(0.5, 0.5, 1, [
            { offset: 0, color: 'rgba(77, 171, 247, 0.3)' },
            { offset: 1, color: 'rgba(77, 171, 247, 0.1)' }
          ])
        },
        lineStyle: {
          color: '#4dabf7',
          width: 2
        },
        itemStyle: {
          color: '#4dabf7',
          borderColor: '#fff',
          borderWidth: 2
        }
      }]
      }]
    })
  }
  
// TOP15使用率（横向柱状图）
const updateTopChart = (data) => {
  if (!topChartRef.value) return
  if (!data.classroomUtilizations || data.classroomUtilizations.length === 0) return
  
    if (!topChart) {
      topChart = echarts.init(topChartRef.value)
    }
    
    const topClassrooms = [...data.classroomUtilizations]
      .sort((a, b) => b.utilizationRate - a.utilizationRate)
    .slice(0, 15)
    
    const rooms = topClassrooms.map(item => item.classroom?.roomNo || '未知')
    const rates = topClassrooms.map(item => item.utilizationRate)
    
    topChart.setOption({
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'shadow'
      },
      backgroundColor: 'rgba(0, 0, 0, 0.8)',
      borderColor: 'transparent',
      textStyle: {
        color: '#fff'
      },
      formatter: function(params) {
        const item = topClassrooms[params[0].dataIndex]
        return `
          <div style="padding: 5px;">
            <div style="font-weight: bold; margin-bottom: 5px;">${item.classroom?.roomNo}</div>
            <div>使用率: ${item.utilizationRate}%</div>
            <div>使用时长: ${formatMinutes(item.totalUsageMinutes)}</div>
            <div>课程: ${item.courseUsageCount || 0}次 | 考试: ${item.examUsageCount || 0}次 | 借用: ${item.bookingUsageCount || 0}次</div>
          </div>
        `
        }
      },
      grid: {
      left: '10%',
      right: '10%',
      bottom: '3%',
      top: '3%',
      containLabel: true
      },
      xAxis: {
        type: 'value',
      max: 100,
      axisLabel: {
        formatter: '{value}%'
      },
      splitLine: {
        lineStyle: {
          type: 'dashed',
          color: '#f0f0f0'
        }
      }
      },
      yAxis: {
        type: 'category',
      data: rooms,
      axisLine: {
        lineStyle: {
          color: '#ddd'
        }
      }
      },
      series: [{
        type: 'bar',
      data: rates.map((rate, index) => ({
        value: rate,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: index < 3 ? '#ffd93d' : '#4dabf7' },
            { offset: 1, color: index < 3 ? '#f6c445' : '#339af0' }
          ]),
          borderRadius: [0, 6, 6, 0]
        }
      })),
      barWidth: '60%',
      label: {
        show: true,
        position: 'right',
        formatter: '{c}%',
        fontSize: 12,
        fontWeight: 'bold',
        color: '#333'
        }
      }]
    })
  }

// 趋势图（折线图）
const updateTrendChart = (data) => {
  if (!trendChartRef.value) return
  
  if (!trendChart) {
    trendChart = echarts.init(trendChartRef.value)
  }
  
  // 模拟趋势数据（实际应从后端获取）
  const days = []
  const values = []
  const endDate = new Date(dateRange.value[1])
  const startDate = new Date(dateRange.value[0])
  const daysDiff = Math.ceil((endDate - startDate) / (1000 * 60 * 60 * 24))
  
  for (let i = 0; i <= daysDiff; i++) {
    const date = new Date(startDate)
    date.setDate(date.getDate() + i)
    days.push(`${date.getMonth() + 1}/${date.getDate()}`)
    // 模拟数据：在平均值附近波动
    const baseValue = data.averageUtilization || 60
    const fluctuation = (Math.random() - 0.5) * 20
    values.push(Math.max(0, Math.min(100, baseValue + fluctuation)))
  }
  
  trendChart.setOption({
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(0, 0, 0, 0.8)',
      borderColor: 'transparent',
      textStyle: {
        color: '#fff'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '10%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: days,
      boundaryGap: false,
      axisLine: {
        lineStyle: {
          color: '#ddd'
        }
      }
    },
    yAxis: {
      type: 'value',
      name: '使用率(%)',
      max: 100,
      axisLabel: {
        formatter: '{value}%'
      },
      splitLine: {
        lineStyle: {
          type: 'dashed',
          color: '#f0f0f0'
        }
      }
    },
    series: [{
      type: 'line',
      data: values,
      smooth: true,
      symbol: 'circle',
      symbolSize: 6,
      lineStyle: {
        color: '#4dabf7',
        width: 3
      },
      itemStyle: {
        color: '#4dabf7',
        borderColor: '#fff',
        borderWidth: 2
      },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(77, 171, 247, 0.3)' },
          { offset: 1, color: 'rgba(77, 171, 247, 0.05)' }
        ])
      }
    }]
  })
}

// 导出报告
const handleExport = async () => {
  if (!dateRange.value || dateRange.value.length !== 2) {
    ElMessage.warning('请选择统计周期')
    return
  }
  
  exportLoading.value = true
  try {
    const params = {
      startDate: formatDate(dateRange.value[0]),
      endDate: formatDate(dateRange.value[1]),
      building: searchForm.building || undefined,
      type: searchForm.type || undefined
    }
    const response = await exportUtilizationReport(params)
    
    const blob = new Blob([response], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `教室使用率报告_${params.startDate}_${params.endDate}.xlsx`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    
    ElMessage.success('导出成功')
  } catch (error) {
    ElMessage.error(error.message || '导出失败')
  } finally {
    exportLoading.value = false
  }
}

// 查看详情
const handleViewDetail = (row) => {
  if (row.classroom && row.classroom.id) {
    router.push(`/admin/classrooms/${row.classroom.id}/usage`)
  }
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return ''
  const d = new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

// 格式化分钟
const formatMinutes = (minutes) => {
  if (!minutes) return '0小时0分钟'
  const hours = Math.floor(minutes / 60)
  const mins = minutes % 60
  return `${hours}小时${mins}分钟`
}

// 获取类型标签
const getTypeTag = (type) => {
  const tags = {
    LECTURE: 'info',
    LAB: 'warning',
    MULTIMEDIA: 'success',
    CONFERENCE: 'danger'
  }
  return tags[type] || 'info'
}

// 获取类型名称
const getTypeName = (type) => {
  const names = {
    LECTURE: '普通教室',
    LAB: '实验室',
    MULTIMEDIA: '多媒体教室',
    CONFERENCE: '会议室'
  }
  return names[type] || type
}

// 获取进度条颜色
const getProgressColor = (rate) => {
  if (rate >= 90) return '#f56c6c'
  if (rate >= 60) return '#67c23a'
  if (rate >= 30) return '#e6a23c'
  return '#909399'
}

// 获取使用率渐变色
const getUtilizationGradient = (rate) => {
  if (rate >= 90) {
    return [
      { color: '#ff6b6b', percentage: 0 },
      { color: '#ee5a52', percentage: 100 }
    ]
  }
  if (rate >= 60) {
    return [
      { color: '#6bcf7f', percentage: 0 },
      { color: '#51cf66', percentage: 100 }
    ]
  }
  if (rate >= 30) {
    return [
      { color: '#ffd93d', percentage: 0 },
      { color: '#f6c445', percentage: 100 }
    ]
  }
  return [
    { color: '#c0c4cc', percentage: 0 },
    { color: '#909399', percentage: 100 }
  ]
}

// 获取行样式
const getRowClassName = ({ row }) => {
  if (row.utilizationRate >= 90) return 'high-utilization-row'
  if (row.utilizationRate < 30) return 'low-utilization-row'
  return ''
}

onMounted(() => {
  // 设置默认日期范围（最近30天）
  const endDate = new Date()
  const startDate = new Date()
  startDate.setDate(startDate.getDate() - 30)
  dateRange.value = [startDate, endDate]
  
  fetchUtilizationReport()
  fetchAbnormalClassrooms()
  fetchRanking()
  
  // 监听窗口大小变化
  window.addEventListener('resize', () => {
    distributionChart?.resize()
    buildingChart?.resize()
    typeChart?.resize()
    topChart?.resize()
    trendChart?.resize()
  })
})
</script>

<style scoped lang="scss">
.utilization-statistics-container {
  padding: 24px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e9ecef 100%);
  min-height: 100vh;
}

// 搜索栏
.search-section {
  margin-bottom: 24px;

.search-card {
    border-radius: 12px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
    border: none;

    .form-label {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 14px;
      color: #606266;
      margin-bottom: 8px;
      font-weight: 500;
    }
  }
}

// 指标卡片
.metrics-section {
  margin-bottom: 24px;

  .metric-card {
    height: 140px;
    border-radius: 16px;
    padding: 24px;
    display: flex;
    align-items: center;
    gap: 20px;
    cursor: pointer;
    transition: all 0.3s ease;
    position: relative;
    overflow: hidden;
    border: none;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);

    &::before {
      content: '';
      position: absolute;
      top: 0;
      right: 0;
      width: 200px;
      height: 200px;
      border-radius: 50%;
      opacity: 0.1;
      transition: all 0.3s ease;
    }

    &:hover {
      transform: translateY(-4px);
      box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);

      &::before {
        transform: scale(1.2);
      }
    }

    &-1 {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;

      &::before {
        background: white;
      }
    }

    &-2 {
      background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
      color: white;

      &::before {
        background: white;
      }
    }

    &-3 {
      background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
      color: white;

      &::before {
        background: white;
      }
    }

    &-4 {
      background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
      color: white;

      &::before {
        background: white;
      }
    }

    .metric-icon {
      font-size: 48px;
      opacity: 0.9;
      z-index: 1;
    }

    .metric-content {
      flex: 1;
      z-index: 1;

      .metric-label {
        font-size: 14px;
        opacity: 0.9;
        margin-bottom: 8px;
        font-weight: 500;
      }

      .metric-value {
        font-size: 32px;
        font-weight: 700;
        line-height: 1;
        margin-bottom: 8px;

        .metric-unit {
          font-size: 20px;
          margin-left: 4px;
        }

        &.warning {
          color: #ffe066;
        }
      }

      .metric-sub {
        font-size: 12px;
        opacity: 0.85;

        .metric-trend {
          display: inline-flex;
          align-items: center;
          gap: 4px;

          &.up {
            color: #51cf66;
          }
        }
      }

      .metric-badge {
        display: inline-block;
        padding: 4px 12px;
        border-radius: 12px;
        font-size: 12px;
        font-weight: 500;
        background: rgba(255, 255, 255, 0.2);
        backdrop-filter: blur(10px);

        &.success {
          background: rgba(81, 207, 102, 0.3);
        }

        &.warning {
          background: rgba(255, 224, 102, 0.3);
        }
      }

      .metric-progress {
        margin-top: 8px;
      }
    }
  }
}

// 智能分析
.insights-section {
  margin-bottom: 24px;

  .insights-card {
    border-radius: 12px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
    border: none;

    .card-header {
      display: flex;
      align-items: center;
      gap: 8px;

      .card-title {
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: 16px;
        font-weight: 600;
        color: #303133;
      }
    }

    .insights-list {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
      gap: 16px;

      .insight-item {
        display: flex;
        align-items: flex-start;
        gap: 12px;
        padding: 16px;
        border-radius: 8px;
        border-left: 4px solid;
        transition: all 0.3s ease;

        &:hover {
          transform: translateX(4px);
        }

        &.insight-success {
          background: rgba(103, 194, 58, 0.05);
          border-color: #67c23a;

          .insight-icon {
            color: #67c23a;
          }
        }

        &.insight-warning {
          background: rgba(230, 162, 60, 0.05);
          border-color: #e6a23c;

          .insight-icon {
            color: #e6a23c;
          }
        }

        &.insight-info {
          background: rgba(64, 158, 255, 0.05);
          border-color: #409eff;

          .insight-icon {
            color: #409eff;
          }
        }

        .insight-icon {
          font-size: 24px;
          flex-shrink: 0;
        }

        .insight-content {
          flex: 1;

          .insight-title {
            font-size: 14px;
            font-weight: 600;
            color: #303133;
            margin-bottom: 4px;
          }

          .insight-desc {
            font-size: 13px;
            color: #606266;
            line-height: 1.5;
          }
        }
      }
    }
  }
}

// 图表
.charts-section {
  margin-bottom: 24px;

  .chart-card {
    border-radius: 12px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
    border: none;
    transition: all 0.3s ease;

    &:hover {
      box-shadow: 0 4px 20px rgba(0, 0, 0, 0.12);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;

      .card-title {
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: 16px;
        font-weight: 600;
        color: #303133;
      }
    }

    .chart-container {
      height: 350px;
      width: 100%;
    }
  }
}

// 数据表格
.abnormal-section,
.ranking-section {
  margin-bottom: 24px;

  .data-card {
    border-radius: 12px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
    border: none;

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;

.card-title {
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: 16px;
  font-weight: 600;
        color: #303133;
      }
    }

    .classroom-info {
      .classroom-name {
        display: flex;
        align-items: center;
        gap: 6px;
  font-size: 14px;
        font-weight: 600;
        color: #303133;
        margin-bottom: 4px;
      }

      .classroom-meta {
        font-size: 12px;
        color: #909399;
      }
    }

    .utilization-cell {
      padding: 8px 0;
    }

    .time-cell {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 4px;
      font-size: 13px;
      color: #606266;
    }

    .usage-details {
      display: flex;
      gap: 8px;
      justify-content: center;
      flex-wrap: wrap;
    }

    .rank-cell {
      display: flex;
      align-items: center;
      justify-content: center;

      .rank-icon {
  font-size: 24px;

        &.gold {
          color: #f6c445;
        }

        &.silver {
          color: #c0c4cc;
        }

        &.bronze {
          color: #e6a23c;
        }
      }

      .rank-number {
        font-size: 16px;
  font-weight: 600;
        color: #606266;
      }
    }
  }
}

// 表格行样式
:deep(.high-utilization-row) {
  background-color: rgba(255, 107, 107, 0.03);
}

:deep(.low-utilization-row) {
  background-color: rgba(230, 162, 60, 0.03);
}

// 表格悬停效果
:deep(.el-table__row) {
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    background-color: rgba(64, 158, 255, 0.05) !important;
  }
}

// 响应式
@media (max-width: 1200px) {
  .metrics-section {
    .el-col {
      margin-bottom: 16px;
    }
  }

  .charts-section {
    .el-col {
      margin-bottom: 16px;
    }
  }
}
</style>
