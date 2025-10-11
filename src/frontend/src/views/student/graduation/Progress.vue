<template>
  <div class="graduation-progress">
    <el-card class="header-card">
      <div class="header-content">
        <div class="title-section">
          <h2>毕业进度</h2>
          <p class="subtitle">查看您的学分完成情况和毕业审核状态</p>
        </div>
        <el-button 
          v-if="progressData?.eligibleForAudit && !progressData?.passed"
          type="primary" 
          size="large"
          @click="navigateToAudit"
        >
          查看审核结果
        </el-button>
      </div>
    </el-card>

    <el-row :gutter="20" v-loading="loading">
      <!-- 毕业进度概览 -->
      <el-col :span="24">
        <el-card class="progress-overview">
          <template #header>
            <div class="card-header">
              <span>毕业进度概览</span>
              <el-tag 
                v-if="progressData?.passed" 
                type="success" 
                size="large"
              >
                <el-icon><Select /></el-icon>
                已通过毕业审核
              </el-tag>
              <el-tag 
                v-else-if="progressData?.eligibleForAudit" 
                type="warning" 
                size="large"
              >
                <el-icon><Warning /></el-icon>
                可以申请毕业审核
              </el-tag>
              <el-tag v-else type="info" size="large">
                <el-icon><Clock /></el-icon>
                学分完成中
              </el-tag>
            </div>
          </template>

          <div class="progress-content">
            <div class="progress-circle">
              <el-progress 
                type="circle" 
                :percentage="progressPercentage" 
                :width="200"
                :color="progressColor"
              >
                <template #default>
                  <div class="progress-text">
                    <div class="percentage">{{ progressPercentage }}%</div>
                    <div class="label">总体完成度</div>
                  </div>
                </template>
              </el-progress>
            </div>

            <div class="progress-details">
              <el-descriptions :column="2" border>
                <el-descriptions-item label="学号">
                  {{ progressData?.credits?.studentNo }}
                </el-descriptions-item>
                <el-descriptions-item label="姓名">
                  {{ progressData?.credits?.studentName }}
                </el-descriptions-item>
                <el-descriptions-item label="专业">
                  {{ progressData?.credits?.majorName }}
                </el-descriptions-item>
                <el-descriptions-item label="入学年份">
                  {{ progressData?.credits?.enrollmentYear }}
                </el-descriptions-item>
              </el-descriptions>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 学分完成情况 -->
      <el-col :xs="24" :sm="12" :md="12" :lg="6" v-for="item in creditItems" :key="item.type">
        <el-card class="credit-card">
          <div class="credit-content">
            <div class="credit-icon" :style="{ backgroundColor: item.color + '20', color: item.color }">
              <el-icon :size="32"><component :is="item.icon" /></el-icon>
            </div>
            <div class="credit-info">
              <div class="credit-label">{{ item.label }}</div>
              <div class="credit-value">
                <span class="current">{{ item.current }}</span>
                <span class="separator">/</span>
                <span class="required">{{ item.required }}</span>
              </div>
              <el-progress 
                :percentage="item.percentage" 
                :status="item.status"
                :show-text="false"
              />
              <div class="credit-remaining" v-if="item.remaining > 0">
                还需 <span class="remaining-value">{{ item.remaining }}</span> 学分
              </div>
              <div class="credit-completed" v-else>
                <el-icon color="#67c23a"><Select /></el-icon>
                已达标
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 毕业条件检查 -->
      <el-col :span="24">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>毕业条件检查</span>
            </div>
          </template>

          <el-timeline>
            <el-timeline-item 
              v-for="(check, index) in requirementChecks" 
              :key="index"
              :icon="check.passed ? Select : check.pending ? Clock : Close"
              :type="check.passed ? 'success' : check.pending ? 'info' : 'danger'"
              :hollow="!check.passed"
            >
              <div class="check-item">
                <div class="check-title">{{ check.title }}</div>
                <div class="check-desc">{{ check.description }}</div>
                <el-tag 
                  :type="check.passed ? 'success' : check.pending ? 'info' : 'warning'"
                  size="small"
                >
                  {{ check.status }}
                </el-tag>
              </div>
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>

      <!-- 审核结果 -->
      <el-col :span="24" v-if="progressData?.audit">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>毕业审核结果</span>
            </div>
          </template>

          <el-result
            :icon="auditResultIcon"
            :title="auditResultTitle"
            :sub-title="progressData.audit.failReason || '恭喜您通过毕业审核！'"
          >
            <template #extra>
              <el-descriptions :column="2" border>
                <el-descriptions-item label="审核年份">
                  {{ progressData.audit.auditYear }}
                </el-descriptions-item>
                <el-descriptions-item label="审核状态">
                  <el-tag :type="auditStatusType">
                    {{ auditStatusText }}
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="审核时间" :span="2">
                  {{ formatDateTime(progressData.audit.auditedAt) }}
                </el-descriptions-item>
              </el-descriptions>
            </template>
          </el-result>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  Select, Warning, Clock, Close, 
  Ticket, Document, Reading, Promotion 
} from '@element-plus/icons-vue'
  import request from '@/utils/request'

const router = useRouter()
const loading = ref(false)
const progressData = ref(null)

// 加载毕业进度数据
const loadProgress = async () => {
  loading.value = true
  try {
    const response = await request.get('/student/graduation/progress')
    progressData.value = response.data
    console.log('Progress Data:', progressData.value)
    console.log('Requirement:', progressData.value?.requirement)
    console.log('Credits:', progressData.value?.credits)
  } catch (error) {
    ElMessage.error(error.message || '加载毕业进度失败')
  } finally {
    loading.value = false
  }
}

// 计算进度百分比
const progressPercentage = computed(() => {
  if (!progressData.value) return 0
  return Math.round(progressData.value.progressPercentage || 0)
})

// 进度颜色
const progressColor = computed(() => {
  const percentage = progressPercentage.value
  if (percentage >= 90) return '#67c23a'
  if (percentage >= 70) return '#e6a23c'
  return '#409eff'
})

// 学分项目
const creditItems = computed(() => {
  if (!progressData.value) return []
  
  const { credits, requirement } = progressData.value
  if (!credits || !requirement) return []

  const calculatePercentage = (current, required) => {
    if (!required || required === 0) return 0
    return Math.min(100, Math.round((current / required) * 100))
  }

  return [
    {
      type: 'total',
      label: '总学分',
      current: credits.totalCredits || 0,
      required: requirement.totalCreditsRequired || 0,
      remaining: progressData.value.remainingCredits || 0,
      percentage: calculatePercentage(credits.totalCredits, requirement.totalCreditsRequired),
      status: credits.totalCredits >= requirement.totalCreditsRequired ? 'success' : '',
      color: '#409eff',
      icon: Ticket
    },
    {
      type: 'required',
      label: '必修学分',
      current: credits.requiredCredits || 0,
      required: requirement.requiredCreditsRequired || 0,
      remaining: progressData.value.remainingRequiredCredits || 0,
      percentage: calculatePercentage(credits.requiredCredits, requirement.requiredCreditsRequired),
      status: credits.requiredCredits >= requirement.requiredCreditsRequired ? 'success' : '',
      color: '#f56c6c',
      icon: Document
    },
    {
      type: 'elective',
      label: '选修学分',
      current: credits.electiveCredits || 0,
      required: requirement.electiveCreditsRequired || 0,
      remaining: progressData.value.remainingElectiveCredits || 0,
      percentage: calculatePercentage(credits.electiveCredits, requirement.electiveCreditsRequired),
      status: credits.electiveCredits >= requirement.electiveCreditsRequired ? 'success' : '',
      color: '#e6a23c',
      icon: Reading
    },
    {
      type: 'practical',
      label: '实践学分',
      current: credits.practicalCredits || 0,
      required: requirement.practicalCreditsRequired || 0,
      remaining: progressData.value.remainingPracticalCredits || 0,
      percentage: calculatePercentage(credits.practicalCredits, requirement.practicalCreditsRequired),
      status: credits.practicalCredits >= requirement.practicalCreditsRequired ? 'success' : '',
      color: '#67c23a',
      icon: Promotion
    }
  ]
})

// 毕业条件检查
const requirementChecks = computed(() => {
  if (!progressData.value) return []
  
  const { credits, requirement, audit } = progressData.value
  if (!credits || !requirement) return []

  const checks = [
    {
      title: '总学分要求',
      description: `需要完成 ${requirement.totalCreditsRequired} 学分，当前已完成 ${credits.totalCredits} 学分`,
      passed: credits.totalCredits >= requirement.totalCreditsRequired,
      pending: false,
      status: credits.totalCredits >= requirement.totalCreditsRequired ? '已达标' : '未达标'
    },
    {
      title: '必修学分要求',
      description: `需要完成 ${requirement.requiredCreditsRequired} 学分，当前已完成 ${credits.requiredCredits} 学分`,
      passed: credits.requiredCredits >= requirement.requiredCreditsRequired,
      pending: false,
      status: credits.requiredCredits >= requirement.requiredCreditsRequired ? '已达标' : '未达标'
    },
    {
      title: '选修学分要求',
      description: `需要完成 ${requirement.electiveCreditsRequired} 学分，当前已完成 ${credits.electiveCredits} 学分`,
      passed: credits.electiveCredits >= requirement.electiveCreditsRequired,
      pending: false,
      status: credits.electiveCredits >= requirement.electiveCreditsRequired ? '已达标' : '未达标'
    },
    {
      title: '实践学分要求',
      description: `需要完成 ${requirement.practicalCreditsRequired} 学分，当前已完成 ${credits.practicalCredits} 学分`,
      passed: credits.practicalCredits >= requirement.practicalCreditsRequired,
      pending: false,
      status: credits.practicalCredits >= requirement.practicalCreditsRequired ? '已达标' : '未达标'
    }
  ]

  // 添加审核状态
  if (audit) {
    checks.push({
      title: '毕业审核',
      description: audit.failReason || '已通过毕业审核',
      passed: audit.status === 'PASS',
      pending: audit.status === 'PENDING',
      status: audit.status === 'PASS' ? '已通过' : 
              audit.status === 'FAIL' ? '未通过' :
              audit.status === 'DEFERRED' ? '暂缓毕业' : '待审核'
    })
  }

  return checks
})

// 审核结果图标
const auditResultIcon = computed(() => {
  if (!progressData.value?.audit) return 'info'
  const status = progressData.value.audit.status
  return status === 'PASS' ? 'success' : status === 'FAIL' ? 'error' : 'warning'
})

// 审核结果标题
const auditResultTitle = computed(() => {
  if (!progressData.value?.audit) return ''
  const status = progressData.value.audit.status
  return status === 'PASS' ? '毕业审核通过' : 
         status === 'FAIL' ? '毕业审核未通过' : '暂缓毕业'
})

// 审核状态类型
const auditStatusType = computed(() => {
  if (!progressData.value?.audit) return 'info'
  const status = progressData.value.audit.status
  return status === 'PASS' ? 'success' : status === 'FAIL' ? 'danger' : 'warning'
})

// 审核状态文本
const auditStatusText = computed(() => {
  if (!progressData.value?.audit) return ''
  const status = progressData.value.audit.status
  return status === 'PASS' ? '通过' : 
         status === 'FAIL' ? '未通过' : 
         status === 'DEFERRED' ? '暂缓' : '待审核'
})

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}

// 跳转到审核结果页
const navigateToAudit = () => {
  router.push('/student/graduation/audit')
}

onMounted(() => {
  loadProgress()
})
</script>

<style scoped lang="scss">
.graduation-progress {
  padding: 20px;

  .header-card {
    margin-bottom: 20px;

    .header-content {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .title-section {
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
    }
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-weight: 600;
  }

  .progress-overview {
    margin-bottom: 20px;

    .progress-content {
      display: flex;
      gap: 40px;
      align-items: center;

      @media (max-width: 768px) {
        flex-direction: column;
      }

      .progress-circle {
        flex-shrink: 0;

        .progress-text {
          text-align: center;

          .percentage {
            font-size: 32px;
            font-weight: bold;
            color: #303133;
          }

          .label {
            font-size: 14px;
            color: #909399;
            margin-top: 8px;
          }
        }
      }

      .progress-details {
        flex: 1;
      }
    }
  }

  .credit-card {
    margin-bottom: 20px;
    height: 100%;

    .credit-content {
      display: flex;
      gap: 16px;

      .credit-icon {
        width: 64px;
        height: 64px;
        border-radius: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        flex-shrink: 0;
      }

      .credit-info {
        flex: 1;

        .credit-label {
          font-size: 14px;
          color: #909399;
          margin-bottom: 8px;
        }

        .credit-value {
          font-size: 24px;
          font-weight: bold;
          margin-bottom: 12px;

          .current {
            color: #303133;
          }

          .separator {
            color: #dcdfe6;
            margin: 0 4px;
          }

          .required {
            color: #909399;
            font-size: 18px;
          }
        }

        .credit-remaining {
          margin-top: 8px;
          font-size: 13px;
          color: #e6a23c;

          .remaining-value {
            font-weight: bold;
          }
        }

        .credit-completed {
          margin-top: 8px;
          font-size: 13px;
          color: #67c23a;
          display: flex;
          align-items: center;
          gap: 4px;
        }
      }
    }
  }

  .check-item {
    .check-title {
      font-size: 16px;
      font-weight: 600;
      color: #303133;
      margin-bottom: 8px;
    }

    .check-desc {
      font-size: 14px;
      color: #606266;
      margin-bottom: 8px;
    }
  }
}
</style>

