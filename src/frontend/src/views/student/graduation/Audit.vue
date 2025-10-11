<template>
  <div class="graduation-audit">
    <el-card class="header-card">
      <h2>毕业审核结果</h2>
      <p class="subtitle">查看您的毕业资格审核结果和详细说明</p>
    </el-card>

    <div v-loading="loading">
      <!-- 未审核状态 -->
      <el-empty 
        v-if="!loading && !auditResult"
        description="您还没有毕业审核记录"
        :image-size="200"
      >
        <el-button type="primary" @click="navigateToProgress">
          查看毕业进度
        </el-button>
      </el-empty>

      <!-- 审核结果 -->
      <div v-else-if="auditResult">
        <!-- 审核状态卡片 -->
        <el-card class="result-card">
          <el-result
            :icon="resultIcon"
            :title="resultTitle"
            :sub-title="resultSubtitle"
          >
            <template #icon>
              <div class="result-icon-wrapper" :class="resultIconClass">
                <el-icon :size="80">
                  <component :is="resultIconComponent" />
                </el-icon>
              </div>
            </template>

            <template #extra>
              <el-space direction="vertical" size="large" alignment="center" style="width: 100%">
                <el-descriptions :column="2" border class="result-descriptions">
                  <el-descriptions-item label="学号">
                    {{ auditResult.studentNo }}
                  </el-descriptions-item>
                  <el-descriptions-item label="姓名">
                    {{ auditResult.studentName }}
                  </el-descriptions-item>
                  <el-descriptions-item label="专业">
                    {{ auditResult.majorName }}
                  </el-descriptions-item>
                  <el-descriptions-item label="审核年份">
                    {{ auditResult.auditYear }}
                  </el-descriptions-item>
                  <el-descriptions-item label="审核状态">
                    <el-tag :type="statusTagType" size="large">
                      {{ statusText }}
                    </el-tag>
                  </el-descriptions-item>
                  <el-descriptions-item label="审核时间">
                    {{ formatDateTime(auditResult.auditedAt) }}
                  </el-descriptions-item>
                </el-descriptions>

                <el-button 
                  v-if="auditResult.status === 'PASS'" 
                  type="success" 
                  size="large"
                  @click="downloadCertificate"
                >
                  <el-icon><Document /></el-icon>
                  下载审核证明
                </el-button>
              </el-space>
            </template>
          </el-result>
        </el-card>

        <!-- 学分完成情况 -->
        <el-row :gutter="20">
          <el-col :xs="24" :sm="12" :md="6" v-for="credit in creditData" :key="credit.type">
            <el-card class="credit-card">
              <div class="credit-content">
                <div class="credit-header">
                  <div class="credit-icon" :style="{ color: credit.color }">
                    <el-icon :size="32"><component :is="credit.icon" /></el-icon>
                  </div>
                  <div class="credit-label">{{ credit.label }}</div>
                </div>
                <div class="credit-body">
                  <div class="credit-value">
                    <span class="current">{{ credit.current }}</span>
                    <span class="separator">/</span>
                    <span class="required">{{ credit.required }}</span>
                  </div>
                  <el-progress 
                    :percentage="credit.percentage" 
                    :status="credit.status"
                  />
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <!-- 不通过原因 -->
        <el-card v-if="auditResult.failReason" class="fail-reason-card">
          <template #header>
            <div class="card-header">
              <el-icon color="#f56c6c"><Warning /></el-icon>
              <span>审核未通过原因</span>
            </div>
          </template>
          <el-alert 
            :title="auditResult.failReason" 
            type="error" 
            :closable="false"
            show-icon
          />
          <div class="suggestions">
            <h4>改进建议：</h4>
            <ul>
              <li v-for="(suggestion, index) in suggestions" :key="index">
                {{ suggestion }}
              </li>
            </ul>
          </div>
        </el-card>

        <!-- 审核详情 -->
        <el-card>
          <template #header>
            <div class="card-header">
              <span>审核详情</span>
            </div>
          </template>

          <el-timeline>
            <el-timeline-item 
              v-for="(item, index) in auditTimeline" 
              :key="index"
              :timestamp="item.timestamp"
              :type="item.type"
              :icon="item.icon"
            >
              <div class="timeline-content">
                <div class="timeline-title">{{ item.title }}</div>
                <div class="timeline-desc" v-if="item.description">
                  {{ item.description }}
                </div>
              </div>
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  Select, Close, Warning, Document, Clock,
  Ticket, DocumentChecked, Reading, Promotion
} from '@element-plus/icons-vue'
  import request from '@/utils/request'

const router = useRouter()
const loading = ref(false)
const auditResult = ref(null)

// 加载审核结果
const loadAuditResult = async () => {
  loading.value = true
  try {
    const response = await request.get('/student/graduation/audit')
    // data可能为null(未审核),这是正常情况
    auditResult.value = response.data
    
    // 如果有审核结果，补充毕业要求数据
    if (auditResult.value) {
      try {
        const progressResponse = await request.get('/student/graduation/progress')
        if (progressResponse.data?.requirement) {
          const req = progressResponse.data.requirement
          auditResult.value.requiredTotalCredits = req.totalCreditsRequired
          auditResult.value.requiredRequiredCredits = req.requiredCreditsRequired
          auditResult.value.requiredElectiveCredits = req.electiveCreditsRequired
          auditResult.value.requiredPracticalCredits = req.practicalCreditsRequired
        }
      } catch (e) {
        console.warn('加载毕业要求失败:', e)
      }
    }
  } catch (error) {
    ElMessage.error(error.message || '加载审核结果失败')
    auditResult.value = null
  } finally {
    loading.value = false
  }
}

// 结果图标组件
const resultIconComponent = computed(() => {
  if (!auditResult.value) return Clock
  const status = auditResult.value.status
  return status === 'PASS' ? Select : 
         status === 'FAIL' ? Close : Warning
})

// 结果图标类名
const resultIconClass = computed(() => {
  if (!auditResult.value) return ''
  const status = auditResult.value.status
  return status === 'PASS' ? 'success-icon' : 
         status === 'FAIL' ? 'error-icon' : 'warning-icon'
})

// 结果标题
const resultTitle = computed(() => {
  if (!auditResult.value) return ''
  const status = auditResult.value.status
  return status === 'PASS' ? '恭喜！毕业审核通过' : 
         status === 'FAIL' ? '很遗憾，毕业审核未通过' : '毕业审核暂缓'
})

// 结果副标题
const resultSubtitle = computed(() => {
  if (!auditResult.value) return ''
  const status = auditResult.value.status
  if (status === 'PASS') {
    return '您已满足毕业条件，可以正常毕业'
  } else if (status === 'FAIL') {
    return '请查看下方原因并及时完成相关要求'
  } else {
    return '请联系教务处了解详细情况'
  }
})

// 状态标签类型
const statusTagType = computed(() => {
  if (!auditResult.value) return 'info'
  const status = auditResult.value.status
  return status === 'PASS' ? 'success' : 
         status === 'FAIL' ? 'danger' : 'warning'
})

// 状态文本
const statusText = computed(() => {
  if (!auditResult.value) return ''
  const status = auditResult.value.status
  return status === 'PASS' ? '通过' : 
         status === 'FAIL' ? '未通过' : '暂缓毕业'
})

// 学分数据
const creditData = computed(() => {
  if (!auditResult.value) return []
  
  const calculatePercentage = (current, required) => {
    if (!required || required === 0) return 0
    return Math.min(100, Math.round((current / required) * 100))
  }
  
  return [
    {
      type: 'total',
      label: '总学分',
      current: auditResult.value.totalCredits || 0,
      required: auditResult.value.requiredTotalCredits || 0,
      percentage: calculatePercentage(
        auditResult.value.totalCredits, 
        auditResult.value.requiredTotalCredits
      ),
      status: auditResult.value.totalCredits >= auditResult.value.requiredTotalCredits ? 'success' : '',
      color: '#409eff',
      icon: Ticket
    },
    {
      type: 'required',
      label: '必修学分',
      current: auditResult.value.requiredCredits || 0,
      required: auditResult.value.requiredRequiredCredits || 0,
      percentage: calculatePercentage(
        auditResult.value.requiredCredits, 
        auditResult.value.requiredRequiredCredits
      ),
      status: auditResult.value.requiredCredits >= auditResult.value.requiredRequiredCredits ? 'success' : '',
      color: '#f56c6c',
      icon: DocumentChecked
    },
    {
      type: 'elective',
      label: '选修学分',
      current: auditResult.value.electiveCredits || 0,
      required: auditResult.value.requiredElectiveCredits || 0,
      percentage: calculatePercentage(
        auditResult.value.electiveCredits, 
        auditResult.value.requiredElectiveCredits
      ),
      status: auditResult.value.electiveCredits >= auditResult.value.requiredElectiveCredits ? 'success' : '',
      color: '#e6a23c',
      icon: Reading
    },
    {
      type: 'practical',
      label: '实践学分',
      current: auditResult.value.practicalCredits || 0,
      required: auditResult.value.requiredPracticalCredits || 0,
      percentage: calculatePercentage(
        auditResult.value.practicalCredits, 
        auditResult.value.requiredPracticalCredits
      ),
      status: auditResult.value.practicalCredits >= auditResult.value.requiredPracticalCredits ? 'success' : '',
      color: '#67c23a',
      icon: Promotion
    }
  ]
})

// 改进建议
const suggestions = computed(() => {
  if (!auditResult.value?.failReason) return []
  
  const suggestions = []
  const reason = auditResult.value.failReason
  
  if (reason.includes('总学分')) {
    suggestions.push('请继续修读课程以获得足够的总学分')
  }
  if (reason.includes('必修')) {
    suggestions.push('请完成所有必修课程的学习并通过考试')
  }
  if (reason.includes('选修')) {
    suggestions.push('请选修更多选修课程以满足学分要求')
  }
  if (reason.includes('实践')) {
    suggestions.push('请完成实践课程、实验课程或实训项目')
  }
  
  return suggestions
})

// 审核时间线
const auditTimeline = computed(() => {
  if (!auditResult.value) return []
  
  const timeline = []
  
  // 提交审核
  timeline.push({
    timestamp: formatDateTime(auditResult.value.createdAt),
    type: 'primary',
    icon: Document,
    title: '提交毕业审核申请',
    description: '系统自动提交审核申请'
  })
  
  // 审核结果
  if (auditResult.value.auditedAt) {
    const status = auditResult.value.status
    timeline.push({
      timestamp: formatDateTime(auditResult.value.auditedAt),
      type: status === 'PASS' ? 'success' : status === 'FAIL' ? 'danger' : 'warning',
      icon: status === 'PASS' ? Select : status === 'FAIL' ? Close : Warning,
      title: status === 'PASS' ? '审核通过' : status === 'FAIL' ? '审核未通过' : '审核暂缓',
      description: auditResult.value.failReason || '恭喜通过毕业审核'
    })
  }
  
  return timeline
})

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}

// 下载证明
const downloadCertificate = () => {
  ElMessage.info('证明下载功能开发中...')
}

// 跳转到进度页
const navigateToProgress = () => {
  router.push('/student/graduation/progress')
}

onMounted(() => {
  loadAuditResult()
})
</script>

<style scoped lang="scss">
.graduation-audit {
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

  .result-card {
    margin-bottom: 20px;

    .result-icon-wrapper {
      width: 120px;
      height: 120px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      margin: 0 auto;

      &.success-icon {
        background-color: #f0f9ff;
        color: #67c23a;
      }

      &.error-icon {
        background-color: #fef0f0;
        color: #f56c6c;
      }

      &.warning-icon {
        background-color: #fdf6ec;
        color: #e6a23c;
      }
    }

    .result-descriptions {
      max-width: 800px;
      margin: 0 auto;
    }
  }

  .credit-card {
    margin-bottom: 20px;

    .credit-content {
      .credit-header {
        display: flex;
        align-items: center;
        gap: 12px;
        margin-bottom: 16px;

        .credit-label {
          font-size: 14px;
          color: #606266;
          font-weight: 500;
        }
      }

      .credit-body {
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
      }
    }
  }

  .fail-reason-card {
    margin-bottom: 20px;

    .card-header {
      display: flex;
      align-items: center;
      gap: 8px;
      font-weight: 600;
    }

    .suggestions {
      margin-top: 20px;

      h4 {
        margin: 0 0 12px 0;
        color: #303133;
      }

      ul {
        margin: 0;
        padding-left: 20px;

        li {
          margin-bottom: 8px;
          color: #606266;
        }
      }
    }
  }

  .card-header {
    font-weight: 600;
  }

  .timeline-content {
    .timeline-title {
      font-size: 16px;
      font-weight: 600;
      color: #303133;
      margin-bottom: 8px;
    }

    .timeline-desc {
      font-size: 14px;
      color: #606266;
    }
  }
}
</style>

