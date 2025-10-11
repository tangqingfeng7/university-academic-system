<template>
  <div class="refund-applications-page">
    <!-- 页面头部 -->
    <div class="page-header animate-fade-in-down">
      <div class="header-content">
        <h1 class="page-title">退费申请</h1>
        <p class="page-subtitle">查看和管理你的退费申请</p>
      </div>
    </div>

    <!-- 退费申请列表 -->
    <div class="applications-section animate-fade-in-up" style="animation-delay: 0.1s;">
      <el-card shadow="hover" v-loading="loading">
        <el-empty 
          v-if="applications.length === 0 && !loading"
          description="暂无退费申请"
        >
          <el-button type="primary" @click="goToPayments">
            查看缴费记录
          </el-button>
        </el-empty>

        <div v-else class="application-cards">
          <div 
            v-for="application in applications" 
            :key="application.id"
            class="application-card"
          >
            <div class="card-header">
              <div class="card-title">
                <h3>退费申请 - {{ application.paymentNo }}</h3>
                <el-tag 
                  :type="getStatusType(application.status)"
                  size="large"
                >
                  {{ application.statusDescription }}
                </el-tag>
              </div>
              <div class="card-meta">
                <span>提交时间：{{ formatDateTime(application.submittedAt) }}</span>
              </div>
            </div>

            <div class="card-body">
              <el-descriptions :column="2" border size="small">
                <el-descriptions-item label="原缴费金额">
                  ¥{{ application.paymentAmount.toFixed(2) }}
                </el-descriptions-item>
                <el-descriptions-item label="退费金额">
                  <span class="refund-amount">
                    ¥{{ application.refundAmount.toFixed(2) }}
                  </span>
                </el-descriptions-item>
                <el-descriptions-item label="退费类型">
                  {{ application.refundTypeDescription }}
                </el-descriptions-item>
                <el-descriptions-item label="退费方式">
                  {{ application.refundMethodDescription }}
                </el-descriptions-item>
                <el-descriptions-item label="退费原因" :span="2">
                  {{ application.reason }}
                </el-descriptions-item>
                <el-descriptions-item 
                  label="当前审批级别" 
                  v-if="application.status === 'PENDING'"
                >
                  {{ getApprovalLevelText(application.approvalLevel) }}
                </el-descriptions-item>
                <el-descriptions-item 
                  label="审批完成时间" 
                  v-if="application.approvedAt"
                >
                  {{ formatDateTime(application.approvedAt) }}
                </el-descriptions-item>
                <el-descriptions-item 
                  label="退费时间" 
                  v-if="application.refundedAt"
                >
                  {{ formatDateTime(application.refundedAt) }}
                </el-descriptions-item>
                <el-descriptions-item 
                  label="退费交易号" 
                  v-if="application.refundTransactionId"
                >
                  <el-text copyable>{{ application.refundTransactionId }}</el-text>
                </el-descriptions-item>
              </el-descriptions>

              <!-- 审批历史 -->
              <div class="approval-history" v-if="application.approvalHistory && application.approvalHistory.length > 0">
                <h4>审批历史</h4>
                <el-timeline>
                  <el-timeline-item
                    v-for="approval in application.approvalHistory"
                    :key="approval.id"
                    :timestamp="formatDateTime(approval.approvedAt)"
                    :color="getApprovalColor(approval.action)"
                  >
                    <div class="approval-item">
                      <div class="approval-title">
                        <strong>{{ approval.approvalLevelDescription }}</strong>
                        <el-tag :type="getApprovalTagType(approval.action)" size="small">
                          {{ approval.actionDescription }}
                        </el-tag>
                      </div>
                      <div class="approval-content">
                        <p>审批人：{{ approval.approverName }}</p>
                        <p v-if="approval.comment">审批意见：{{ approval.comment }}</p>
                      </div>
                    </div>
                  </el-timeline-item>
                </el-timeline>
              </div>
            </div>

            <div class="card-footer">
              <el-button 
                type="primary" 
                @click="viewDetail(application)"
              >
                查看详情
              </el-button>
              <el-button 
                type="danger" 
                :disabled="application.status !== 'PENDING'"
                @click="handleCancel(application)"
              >
                取消申请
              </el-button>
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="退费申请详情"
      width="700px"
      :close-on-click-modal="false"
    >
      <div v-if="currentApplication" class="application-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="申请ID">
            {{ currentApplication.id }}
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(currentApplication.status)">
              {{ currentApplication.statusDescription }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="学号">
            {{ currentApplication.studentNo }}
          </el-descriptions-item>
          <el-descriptions-item label="姓名">
            {{ currentApplication.studentName }}
          </el-descriptions-item>
          <el-descriptions-item label="缴费单号">
            {{ currentApplication.paymentNo }}
          </el-descriptions-item>
          <el-descriptions-item label="原缴费金额">
            ¥{{ currentApplication.paymentAmount.toFixed(2) }}
          </el-descriptions-item>
          <el-descriptions-item label="退费金额">
            <span class="refund-amount-large">
              ¥{{ currentApplication.refundAmount.toFixed(2) }}
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="退费类型">
            {{ currentApplication.refundTypeDescription }}
          </el-descriptions-item>
          <el-descriptions-item label="退费方式" :span="2">
            {{ currentApplication.refundMethodDescription }}
          </el-descriptions-item>
          <el-descriptions-item label="退费原因" :span="2">
            {{ currentApplication.reason }}
          </el-descriptions-item>
          <el-descriptions-item label="提交时间">
            {{ formatDateTime(currentApplication.submittedAt) }}
          </el-descriptions-item>
          <el-descriptions-item 
            label="审批完成时间" 
            v-if="currentApplication.approvedAt"
          >
            {{ formatDateTime(currentApplication.approvedAt) }}
          </el-descriptions-item>
          <el-descriptions-item 
            label="退费时间" 
            v-if="currentApplication.refundedAt"
          >
            {{ formatDateTime(currentApplication.refundedAt) }}
          </el-descriptions-item>
          <el-descriptions-item 
            label="退费交易号" 
            v-if="currentApplication.refundTransactionId"
            :span="2"
          >
            <el-text copyable>{{ currentApplication.refundTransactionId }}</el-text>
          </el-descriptions-item>
        </el-descriptions>
      </div>

      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  getStudentRefundApplications, 
  getStudentRefundApplicationDetail,
  cancelRefundApplication 
} from '@/api/tuition'

const router = useRouter()

// 数据
const applications = ref([])
const loading = ref(false)
const detailDialogVisible = ref(false)
const currentApplication = ref(null)

// 获取退费申请列表
const fetchApplications = async () => {
  try {
    loading.value = true
    const res = await getStudentRefundApplications()
    applications.value = res.data || []
  } catch (error) {
    console.error('获取退费申请列表失败:', error)
    ElMessage.error('获取退费申请列表失败')
  } finally {
    loading.value = false
  }
}

// 查看详情
const viewDetail = async (application) => {
  try {
    const res = await getStudentRefundApplicationDetail(application.id)
    currentApplication.value = res.data
    detailDialogVisible.value = true
  } catch (error) {
    console.error('获取退费申请详情失败:', error)
    ElMessage.error('获取详情失败')
  }
}

// 取消申请
const handleCancel = async (application) => {
  try {
    await ElMessageBox.confirm(
      '确认取消该退费申请吗？',
      '确认取消',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await cancelRefundApplication(application.id)
    ElMessage.success('已取消退费申请')
    fetchApplications()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消退费申请失败:', error)
      ElMessage.error('取消失败')
    }
  }
}

// 跳转到缴费记录
const goToPayments = () => {
  router.push('/student/tuition/payments')
}

// 获取状态类型
const getStatusType = (status) => {
  const typeMap = {
    'PENDING': 'warning',
    'APPROVED': 'success',
    'REJECTED': 'danger',
    'CANCELLED': 'info'
  }
  return typeMap[status] || 'info'
}

// 获取审批级别文本
const getApprovalLevelText = (level) => {
  const textMap = {
    1: '财务审核',
    2: '管理员审批'
  }
  return textMap[level] || '未知级别'
}

// 获取审批颜色
const getApprovalColor = (action) => {
  const colorMap = {
    'APPROVE': '#67C23A',
    'REJECT': '#F56C6C',
    'RETURN': '#E6A23C'
  }
  return colorMap[action] || '#909399'
}

// 获取审批标签类型
const getApprovalTagType = (action) => {
  const typeMap = {
    'APPROVE': 'success',
    'REJECT': 'danger',
    'RETURN': 'warning'
  }
  return typeMap[action] || 'info'
}

// 格式化日期时间
const formatDateTime = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 初始化
onMounted(() => {
  fetchApplications()
})
</script>

<style scoped lang="scss">
.refund-applications-page {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 24px;

  .header-content {
    .page-title {
      font-size: 28px;
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
}

.applications-section {
  .application-cards {
    display: flex;
    flex-direction: column;
    gap: 20px;
  }

  .application-card {
    border: 2px solid #E4E7ED;
    border-radius: 12px;
    padding: 24px;
    transition: all 0.3s;

    &:hover {
      border-color: #409EFF;
      box-shadow: 0 4px 12px rgba(64, 158, 255, 0.15);
    }

    .card-header {
      margin-bottom: 20px;

      .card-title {
        display: flex;
        align-items: center;
        justify-content: space-between;
        margin-bottom: 12px;

        h3 {
          font-size: 18px;
          font-weight: 600;
          color: #303133;
          margin: 0;
        }
      }

      .card-meta {
        color: #909399;
        font-size: 14px;
      }
    }

    .card-body {
      margin-bottom: 20px;

      .refund-amount {
        font-size: 18px;
        font-weight: 600;
        color: #F56C6C;
      }

      .approval-history {
        margin-top: 24px;

        h4 {
          font-size: 16px;
          font-weight: 600;
          color: #303133;
          margin: 0 0 16px 0;
        }

        .approval-item {
          .approval-title {
            display: flex;
            align-items: center;
            gap: 12px;
            margin-bottom: 8px;
          }

          .approval-content {
            p {
              margin: 4px 0;
              color: #606266;
              font-size: 14px;
            }
          }
        }
      }
    }

    .card-footer {
      display: flex;
      gap: 12px;
      padding-top: 16px;
      border-top: 1px solid #E4E7ED;
    }
  }
}

.application-detail {
  .refund-amount-large {
    font-size: 20px;
    font-weight: 600;
    color: #F56C6C;
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

