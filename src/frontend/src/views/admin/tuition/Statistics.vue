<template>
  <div class="tuition-statistics-page">
    <!-- 页面头部 -->
    <div class="page-header animate-fade-in-down">
      <div class="header-content">
        <h1 class="page-title">财务统计分析与报表</h1>
        <p class="page-subtitle">学费缴纳情况统计、分析与财务报表生成</p>
      </div>
      <div class="header-actions">
        <el-button 
          type="success" 
          :icon="Document"
          size="large"
          @click="handleExportDetailedReport"
        >
          导出详细报表
        </el-button>
        <el-button 
          type="primary" 
          :icon="Download"
          size="large"
          @click="handleExport"
        >
          导出汇总报表
        </el-button>
      </div>
    </div>

    <!-- 筛选区域 -->
    <div class="filter-section animate-fade-in-up" style="animation-delay: 0.1s;">
      <el-card shadow="hover">
        <el-form :inline="true" :model="queryForm">
          <el-form-item label="学年">
            <el-input 
              v-model="queryForm.academicYear" 
              placeholder="如：2024-2025"
              clearable
              style="width: 160px;"
            />
          </el-form-item>
          <el-form-item label="院系">
            <el-select 
              v-model="queryForm.departmentId" 
              placeholder="选择院系"
              clearable
              filterable
              style="width: 200px;"
            >
              <el-option
                v-for="dept in departments"
                :key="dept.id"
                :label="dept.name"
                :value="dept.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :icon="Search" @click="fetchStatistics">查询</el-button>
            <el-button :icon="Refresh" @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>

    <!-- 总体统计卡片 -->
    <div class="overall-stats-section animate-fade-in-up" style="animation-delay: 0.2s;">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content">
              <div class="stat-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
                <el-icon :size="28"><User /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ overallStats.totalStudents }}</div>
                <div class="stat-label">在校学生总数</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content">
              <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);">
                <el-icon :size="28"><Money /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">¥{{ formatMoney(overallStats.totalAmount) }}</div>
                <div class="stat-label">应收学费总额</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content">
              <div class="stat-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);">
                <el-icon :size="28"><Wallet /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">¥{{ formatMoney(overallStats.paidAmount) }}</div>
                <div class="stat-label">已收学费总额</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content">
              <div class="stat-icon" style="background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);">
                <el-icon :size="28"><TrendCharts /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ overallStats.paymentRate }}%</div>
                <div class="stat-label">缴费率</div>
                <el-progress 
                  :percentage="overallStats.paymentRate" 
                  :show-text="false"
                  :stroke-width="4"
                  style="margin-top: 8px;"
                />
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 缴费状态分布 -->
    <div class="status-distribution-section animate-fade-in-up" style="animation-delay: 0.3s;">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-card shadow="hover">
            <template #header>
              <div class="card-header">
                <span class="header-title">缴费状态分布</span>
              </div>
            </template>
            <div ref="statusChartRef" class="chart-container"></div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card shadow="hover">
            <template #header>
              <div class="card-header">
                <span class="header-title">缴费方式分布</span>
              </div>
            </template>
            <div ref="methodChartRef" class="chart-container"></div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 院系缴费率对比 -->
    <div class="department-comparison-section animate-fade-in-up" style="animation-delay: 0.4s;">
      <el-card shadow="hover">
        <template #header>
          <div class="card-header">
            <span class="header-title">院系缴费率对比</span>
          </div>
        </template>
        <div ref="departmentChartRef" class="chart-container-large"></div>
      </el-card>
    </div>

    <!-- 详细统计表格 -->
    <div class="detailed-stats-section animate-fade-in-up" style="animation-delay: 0.5s;">
      <el-card shadow="hover">
        <template #header>
          <div class="card-header">
            <span class="header-title">院系详细统计</span>
          </div>
        </template>
        <el-table 
          :data="departmentStats" 
          v-loading="loading"
          stripe
          border
          style="width: 100%"
        >
          <el-table-column prop="departmentName" label="院系" width="200" />
          <el-table-column prop="totalStudents" label="学生总数" width="100" align="center" />
          <el-table-column prop="paidStudents" label="已缴费" width="100" align="center" />
          <el-table-column prop="unpaidStudents" label="未缴费" width="100" align="center" />
          <el-table-column prop="totalAmount" label="应收金额" width="150" align="right">
            <template #default="{ row }">
              ¥{{ row.totalAmount.toFixed(2) }}
            </template>
          </el-table-column>
          <el-table-column prop="paidAmount" label="已收金额" width="150" align="right">
            <template #default="{ row }">
              ¥{{ row.paidAmount.toFixed(2) }}
            </template>
          </el-table-column>
          <el-table-column prop="outstandingAmount" label="欠费金额" width="150" align="right">
            <template #default="{ row }">
              <span class="amount-danger">¥{{ row.outstandingAmount.toFixed(2) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="paymentRate" label="缴费率" width="120" align="center">
            <template #default="{ row }">
              <el-progress 
                :percentage="row.paymentRate" 
                :color="getPaymentRateColor(row.paymentRate)"
              />
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox, ElLoading } from 'element-plus'
import { 
  Download, Search, Refresh, Document,
  User, Money, Wallet, TrendCharts
} from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { 
  getPaymentStatistics,
  exportFinancialReport,
  exportBillList
} from '@/api/tuition'
import { getAllDepartments } from '@/api/department'

// 数据
const departments = ref([])
const departmentStats = ref([])
const loading = ref(false)
const statusChartRef = ref(null)
const methodChartRef = ref(null)
const departmentChartRef = ref(null)
let statusChart = null
let methodChart = null
let departmentChart = null

const queryForm = reactive({
  academicYear: '2024-2025',
  departmentId: null
})

const overallStats = reactive({
  totalStudents: 0,
  totalAmount: 0,
  paidAmount: 0,
  paymentRate: 0
})

// 获取统计数据
const fetchStatistics = async () => {
  try {
    loading.value = true
    const res = await getPaymentStatistics(queryForm)
    const data = res.data

    // 更新总体统计
    Object.assign(overallStats, {
      totalStudents: data.totalStudents || 0,
      totalAmount: data.totalAmount || 0,
      paidAmount: data.paidAmount || 0,
      paymentRate: data.paymentRate || 0
    })

    // 更新院系统计
    departmentStats.value = data.departmentStats || []

    // 更新图表
    await nextTick()
    updateStatusChart(data.statusDistribution || {})
    updateMethodChart(data.methodDistribution || {})
    updateDepartmentChart(data.departmentStats || [])
  } catch (error) {
    console.error('获取统计数据失败:', error)
    ElMessage.error('获取统计数据失败')
  } finally {
    loading.value = false
  }
}

// 更新缴费状态分布图表
const updateStatusChart = (data) => {
  if (!statusChart) {
    statusChart = echarts.init(statusChartRef.value)
  }

  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      right: '10%',
      top: 'center'
    },
    series: [
      {
        name: '缴费状态',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 20,
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: [
          { value: data.PAID || 0, name: '已缴费', itemStyle: { color: '#67C23A' } },
          { value: data.PARTIAL || 0, name: '部分缴费', itemStyle: { color: '#E6A23C' } },
          { value: data.UNPAID || 0, name: '未缴费', itemStyle: { color: '#F56C6C' } },
          { value: data.OVERDUE || 0, name: '已逾期', itemStyle: { color: '#909399' } }
        ]
      }
    ]
  }

  statusChart.setOption(option)
}

// 更新缴费方式分布图表
const updateMethodChart = (data) => {
  if (!methodChart) {
    methodChart = echarts.init(methodChartRef.value)
  }

  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      right: '10%',
      top: 'center'
    },
    series: [
      {
        name: '缴费方式',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 20,
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: [
          { value: data.ALIPAY || 0, name: '支付宝', itemStyle: { color: '#1677FF' } },
          { value: data.WECHAT || 0, name: '微信支付', itemStyle: { color: '#07C160' } },
          { value: data.BANK_CARD || 0, name: '银行卡', itemStyle: { color: '#FF6A00' } },
          { value: data.BANK_TRANSFER || 0, name: '银行转账', itemStyle: { color: '#722ED1' } },
          { value: data.CASH || 0, name: '现金', itemStyle: { color: '#13C2C2' } }
        ]
      }
    ]
  }

  methodChart.setOption(option)
}

// 更新院系对比图表
const updateDepartmentChart = (data) => {
  if (!departmentChart) {
    departmentChart = echarts.init(departmentChartRef.value)
  }

  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    legend: {
      data: ['缴费率', '学生总数']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: data.map(item => item.departmentName),
      axisLabel: {
        interval: 0,
        rotate: 30
      }
    },
    yAxis: [
      {
        type: 'value',
        name: '缴费率 (%)',
        position: 'left',
        axisLabel: {
          formatter: '{value}%'
        }
      },
      {
        type: 'value',
        name: '学生总数',
        position: 'right'
      }
    ],
    series: [
      {
        name: '缴费率',
        type: 'bar',
        data: data.map(item => item.paymentRate),
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#83bff6' },
            { offset: 0.5, color: '#188df0' },
            { offset: 1, color: '#188df0' }
          ])
        },
        yAxisIndex: 0
      },
      {
        name: '学生总数',
        type: 'line',
        data: data.map(item => item.totalStudents),
        itemStyle: {
          color: '#67C23A'
        },
        yAxisIndex: 1
      }
    ]
  }

  departmentChart.setOption(option)
}

// 获取院系列表
const fetchDepartments = async () => {
  try {
    const res = await getAllDepartments()
    departments.value = res.data || []
  } catch (error) {
    console.error('获取院系列表失败:', error)
  }
}

// 重置查询
const handleReset = () => {
  queryForm.academicYear = '2024-2025'
  queryForm.departmentId = null
  fetchStatistics()
}

// 导出汇总报表
const handleExport = async () => {
  try {
    const res = await exportFinancialReport(queryForm)
    
    // 创建下载链接
    const url = window.URL.createObjectURL(new Blob([res]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', `财务汇总报表_${queryForm.academicYear}_${new Date().getTime()}.xlsx`)
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  }
}

// 导出详细报表（包含所有学生明细）
const handleExportDetailedReport = async () => {
  try {
    ElMessageBox.confirm(
      '详细报表将包含所有学生的缴费明细，文件可能较大，确认导出吗？',
      '确认导出',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'info'
      }
    ).then(async () => {
      const loading = ElLoading.service({
        lock: true,
        text: '正在生成报表，请稍候...',
        background: 'rgba(0, 0, 0, 0.7)'
      })

      try {
        const res = await exportBillList({
          ...queryForm,
          includePaymentDetails: true
        })
        
        // 创建下载链接
        const url = window.URL.createObjectURL(new Blob([res]))
        const link = document.createElement('a')
        link.href = url
        link.setAttribute('download', `学费明细报表_${queryForm.academicYear}_${new Date().getTime()}.xlsx`)
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        window.URL.revokeObjectURL(url)
        
        ElMessage.success('导出成功')
      } finally {
        loading.close()
      }
    }).catch(() => {
      // 用户取消
    })
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  }
}

// 格式化金额
const formatMoney = (amount) => {
  if (!amount) return '0.00'
  return amount.toLocaleString('zh-CN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  })
}

// 获取缴费率颜色
const getPaymentRateColor = (rate) => {
  if (rate >= 90) return '#67C23A'
  if (rate >= 70) return '#E6A23C'
  return '#F56C6C'
}

// 初始化
onMounted(() => {
  fetchDepartments()
  fetchStatistics()

  // 窗口大小改变时重绘图表
  window.addEventListener('resize', () => {
    statusChart?.resize()
    methodChart?.resize()
    departmentChart?.resize()
  })
})
</script>

<style scoped lang="scss">
.tuition-statistics-page {
  padding: 20px;

  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 20px;

    .header-content {
      .page-title {
        font-size: 24px;
        font-weight: 600;
        color: #303133;
        margin: 0 0 8px 0;
      }

      .page-subtitle {
        font-size: 14px;
        color: #909399;
        margin: 0;
      }
    }

    .header-actions {
      display: flex;
      gap: 12px;
    }
  }

  .filter-section {
    margin-bottom: 20px;
  }

  .overall-stats-section {
    margin-bottom: 20px;

    .stat-card {
      .stat-content {
        display: flex;
        align-items: center;
        gap: 16px;

        .stat-icon {
          width: 64px;
          height: 64px;
          display: flex;
          align-items: center;
          justify-content: center;
          border-radius: 16px;
          color: white;
          flex-shrink: 0;
        }

        .stat-info {
          flex: 1;

          .stat-value {
            font-size: 24px;
            font-weight: 600;
            color: #303133;
            margin-bottom: 8px;
          }

          .stat-label {
            font-size: 14px;
            color: #909399;
            margin-bottom: 8px;
          }
        }
      }
    }
  }

  .status-distribution-section {
    margin-bottom: 20px;

    .card-header {
      .header-title {
        font-size: 16px;
        font-weight: 600;
      }
    }

    .chart-container {
      height: 300px;
    }
  }

  .department-comparison-section {
    margin-bottom: 20px;

    .card-header {
      .header-title {
        font-size: 16px;
        font-weight: 600;
      }
    }

    .chart-container-large {
      height: 400px;
    }
  }

  .detailed-stats-section {
    .card-header {
      .header-title {
        font-size: 16px;
        font-weight: 600;
      }
    }

    .amount-danger {
      color: #F56C6C;
      font-weight: 600;
    }
  }
}

// 动画
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

