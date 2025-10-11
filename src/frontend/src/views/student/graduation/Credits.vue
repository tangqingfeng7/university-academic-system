<template>
  <div class="graduation-credits">
    <el-card class="header-card">
      <h2>学分汇总</h2>
      <p class="subtitle">查看您的学分统计和获得学分的课程明细</p>
    </el-card>

    <el-row :gutter="20" v-loading="loading">
      <!-- 学分统计卡片 -->
      <el-col :xs="24" :sm="12" :md="6" v-for="stat in creditStats" :key="stat.type">
        <el-card class="stat-card" :body-style="{ padding: '20px' }">
          <div class="stat-content">
            <div class="stat-icon" :style="{ backgroundColor: stat.color + '20' }">
              <el-icon :size="28" :color="stat.color">
                <component :is="stat.icon" />
              </el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">{{ stat.label }}</div>
              <div class="stat-value" :style="{ color: stat.color }">
                {{ stat.value }}
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- GPA展示 -->
      <el-col :span="24">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>学业表现</span>
            </div>
          </template>

          <el-row :gutter="20">
            <el-col :xs="24" :sm="12" :md="8">
              <div class="gpa-display">
                <div class="gpa-label">当前GPA</div>
                <div class="gpa-value">{{ creditSummary?.gpa?.toFixed(2) || '0.00' }}</div>
                <el-progress 
                  :percentage="(creditSummary?.gpa / 4) * 100" 
                  :color="gpaColor"
                  :show-text="false"
                />
              </div>
            </el-col>
            <el-col :xs="24" :sm="12" :md="16">
              <el-descriptions :column="2" border>
                <el-descriptions-item label="学号">
                  {{ creditSummary?.studentNo }}
                </el-descriptions-item>
                <el-descriptions-item label="姓名">
                  {{ creditSummary?.studentName }}
                </el-descriptions-item>
                <el-descriptions-item label="专业">
                  {{ creditSummary?.majorName }}
                </el-descriptions-item>
                <el-descriptions-item label="入学年份">
                  {{ creditSummary?.enrollmentYear }}
                </el-descriptions-item>
                <el-descriptions-item label="最后更新" :span="2">
                  {{ formatDateTime(creditSummary?.lastUpdated) }}
                </el-descriptions-item>
              </el-descriptions>
            </el-col>
          </el-row>
        </el-card>
      </el-col>

      <!-- 学分明细 -->
      <el-col :span="24">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>学分明细</span>
              <el-button type="primary" size="small" @click="refreshData">
                <el-icon><Refresh /></el-icon>
                刷新
              </el-button>
            </div>
          </template>

          <el-tabs v-model="activeTab" @tab-change="handleTabChange">
            <el-tab-pane label="所有课程" name="all">
              <course-credit-table :courses="allCourses" />
            </el-tab-pane>
            <el-tab-pane label="必修课程" name="required">
              <course-credit-table :courses="requiredCourses" />
            </el-tab-pane>
            <el-tab-pane label="选修课程" name="elective">
              <course-credit-table :courses="electiveCourses" />
            </el-tab-pane>
            <el-tab-pane label="实践课程" name="practical">
              <course-credit-table :courses="practicalCourses" />
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, Ticket, Document, Reading, Promotion, TrendCharts } from '@element-plus/icons-vue'
  import request from '@/utils/request'
import CourseCreditTable from './components/CourseCreditTable.vue'

const loading = ref(false)
const creditSummary = ref(null)
const creditDetails = ref([])
const activeTab = ref('all')

// 加载学分汇总
const loadCreditSummary = async () => {
  loading.value = true
  try {
    const response = await request.get('/student/graduation/credits')
    creditSummary.value = response.data
  } catch (error) {
    ElMessage.error(error.message || '加载学分汇总失败')
  } finally {
    loading.value = false
  }
}

// 加载学分明细
const loadCreditDetails = async () => {
  try {
    const response = await request.get('/student/graduation/credits/details')
    creditDetails.value = response.data || []
  } catch (error) {
    console.error('加载学分明细失败:', error)
    creditDetails.value = []
  }
}

// 学分统计
const creditStats = computed(() => {
  if (!creditSummary.value) return []
  
  return [
    {
      type: 'total',
      label: '总学分',
      value: creditSummary.value.totalCredits?.toFixed(1) || '0.0',
      color: '#409eff',
      icon: Ticket
    },
    {
      type: 'required',
      label: '必修学分',
      value: creditSummary.value.requiredCredits?.toFixed(1) || '0.0',
      color: '#f56c6c',
      icon: Document
    },
    {
      type: 'elective',
      label: '选修学分',
      value: creditSummary.value.electiveCredits?.toFixed(1) || '0.0',
      color: '#e6a23c',
      icon: Reading
    },
    {
      type: 'practical',
      label: '实践学分',
      value: creditSummary.value.practicalCredits?.toFixed(1) || '0.0',
      color: '#67c23a',
      icon: Promotion
    }
  ]
})

// GPA颜色
const gpaColor = computed(() => {
  const gpa = creditSummary.value?.gpa || 0
  if (gpa >= 3.5) return '#67c23a'
  if (gpa >= 3.0) return '#409eff'
  if (gpa >= 2.5) return '#e6a23c'
  return '#f56c6c'
})

// 所有课程
const allCourses = computed(() => creditDetails.value)

// 必修课程
const requiredCourses = computed(() => 
  creditDetails.value.filter(c => c.courseType === 'REQUIRED')
)

// 选修课程
const electiveCourses = computed(() => 
  creditDetails.value.filter(c => c.courseType === 'ELECTIVE')
)

// 实践课程
const practicalCourses = computed(() => 
  creditDetails.value.filter(c => 
    c.courseName?.includes('实验') || 
    c.courseName?.includes('实践') || 
    c.courseName?.includes('实训')
  )
)

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}

// 刷新数据
const refreshData = async () => {
  await Promise.all([loadCreditSummary(), loadCreditDetails()])
  ElMessage.success('数据已刷新')
}

// 切换标签页
const handleTabChange = (tab) => {
  console.log('切换到:', tab)
}

onMounted(() => {
  loadCreditSummary()
  loadCreditDetails()
})
</script>

<style scoped lang="scss">
.graduation-credits {
  padding: 20px;

  .header-card {
    margin-bottom: 20px;

    h2 {
      margin: 0 0 8px 0;
      font-size: 24px;
      color: #303133;
    }

    .subtitle {
      margin: 0;
      color: #909399;
      font-size: 14px;
    }
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-weight: 600;
  }

  .stat-card {
    margin-bottom: 20px;
    height: 100%;

    .stat-content {
      display: flex;
      align-items: center;
      gap: 16px;

      .stat-icon {
        width: 56px;
        height: 56px;
        border-radius: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        flex-shrink: 0;
      }

      .stat-info {
        flex: 1;

        .stat-label {
          font-size: 14px;
          color: #909399;
          margin-bottom: 8px;
        }

        .stat-value {
          font-size: 28px;
          font-weight: bold;
        }
      }
    }
  }

  .gpa-display {
    padding: 20px;
    text-align: center;

    .gpa-label {
      font-size: 14px;
      color: #909399;
      margin-bottom: 12px;
    }

    .gpa-value {
      font-size: 48px;
      font-weight: bold;
      color: #303133;
      margin-bottom: 16px;
    }
  }
}
</style>

