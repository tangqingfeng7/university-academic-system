<template>
  <div class="bill-list-page">
    <!-- 页面头部 -->
    <div class="page-header animate-fade-in-down">
      <div class="header-content">
        <h1 class="page-title">学费账单</h1>
        <p class="page-subtitle">查看和管理你的学费账单</p>
      </div>
    </div>

    <!-- 筛选和统计 -->
    <div class="filter-section animate-fade-in-up" style="animation-delay: 0.1s;">
      <el-card shadow="hover">
        <div class="stats-row">
          <div class="stat-item">
            <span class="stat-label">总账单</span>
            <span class="stat-value">{{ billStats.totalBills }}</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">待缴费</span>
            <span class="stat-value text-danger">{{ billStats.unpaidBills }}</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">已缴费</span>
            <span class="stat-value text-success">{{ billStats.paidBills }}</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">欠费金额</span>
            <span class="stat-value text-danger">¥{{ billStats.totalOutstanding }}</span>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 账单列表 -->
    <div class="bills-section animate-fade-in-up" style="animation-delay: 0.2s;">
      <el-card shadow="hover" v-loading="loading">
        <el-empty 
          v-if="bills.length === 0 && !loading"
          description="暂无学费账单"
        />

        <div v-else class="bill-cards">
          <div 
            v-for="bill in bills" 
            :key="bill.id"
            class="bill-card"
            :class="{ 'overdue': bill.isOverdue }"
          >
            <div class="bill-header">
              <div class="bill-title">
                <h3>{{ bill.academicYear }} 学年学费</h3>
                <el-tag 
                  :type="getBillStatusType(bill.status)"
                  size="large"
                >
                  {{ bill.statusDescription }}
                </el-tag>
              </div>
              <div class="bill-meta">
                <span class="meta-item">
                  <el-icon><Calendar /></el-icon>
                  生成时间：{{ formatDate(bill.generatedAt) }}
                </span>
                <span class="meta-item">
                  <el-icon><Clock /></el-icon>
                  截止日期：{{ formatDate(bill.dueDate) }}
                </span>
              </div>
            </div>

            <div class="bill-body">
              <div class="fee-breakdown">
                <div class="fee-item">
                  <span class="fee-label">学费</span>
                  <span class="fee-value">¥{{ bill.tuitionFee.toFixed(2) }}</span>
                </div>
                <div class="fee-item">
                  <span class="fee-label">住宿费</span>
                  <span class="fee-value">¥{{ bill.accommodationFee.toFixed(2) }}</span>
                </div>
                <div class="fee-item">
                  <span class="fee-label">教材费</span>
                  <span class="fee-value">¥{{ bill.textbookFee.toFixed(2) }}</span>
                </div>
                <div class="fee-item" v-if="bill.otherFees">
                  <span class="fee-label">其他费用</span>
                  <span class="fee-value">¥{{ bill.otherFees.toFixed(2) }}</span>
                </div>
              </div>

              <el-divider />

              <div class="amount-summary">
                <div class="summary-row">
                  <span class="summary-label">应缴总额：</span>
                  <span class="summary-value total">¥{{ bill.totalAmount.toFixed(2) }}</span>
                </div>
                <div class="summary-row">
                  <span class="summary-label">已缴金额：</span>
                  <span class="summary-value paid">¥{{ bill.paidAmount.toFixed(2) }}</span>
                </div>
                <div class="summary-row">
                  <span class="summary-label">欠费金额：</span>
                  <span class="summary-value outstanding">¥{{ bill.outstandingAmount.toFixed(2) }}</span>
                </div>
              </div>
            </div>

            <div class="bill-footer">
              <el-button 
                type="primary" 
                size="large"
                :disabled="bill.status === 'PAID'"
                @click="goToPayment(bill)"
              >
                <el-icon><CreditCard /></el-icon>
                立即缴费
              </el-button>
              <el-button size="large" @click="viewBillDetail(bill)">
                查看详情
              </el-button>
            </div>

            <!-- 逾期提示 -->
            <div v-if="bill.isOverdue && bill.status !== 'PAID'" class="overdue-warning">
              <el-icon><Warning /></el-icon>
              账单已逾期，请尽快缴费
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 账单详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="账单详情"
      width="600px"
      :close-on-click-modal="false"
    >
      <div v-if="currentBill" class="bill-detail">
        <div class="detail-section">
          <h4>基本信息</h4>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="学年">
              {{ currentBill.academicYear }}
            </el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="getBillStatusType(currentBill.status)">
                {{ currentBill.statusDescription }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="学号">
              {{ currentBill.studentNo }}
            </el-descriptions-item>
            <el-descriptions-item label="姓名">
              {{ currentBill.studentName }}
            </el-descriptions-item>
            <el-descriptions-item label="专业">
              {{ currentBill.majorName }}
            </el-descriptions-item>
            <el-descriptions-item label="院系">
              {{ currentBill.departmentName }}
            </el-descriptions-item>
            <el-descriptions-item label="生成时间">
              {{ formatDate(currentBill.generatedAt) }}
            </el-descriptions-item>
            <el-descriptions-item label="截止日期">
              {{ formatDate(currentBill.dueDate) }}
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="detail-section">
          <h4>费用明细</h4>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="学费">
              ¥{{ currentBill.tuitionFee.toFixed(2) }}
            </el-descriptions-item>
            <el-descriptions-item label="住宿费">
              ¥{{ currentBill.accommodationFee.toFixed(2) }}
            </el-descriptions-item>
            <el-descriptions-item label="教材费">
              ¥{{ currentBill.textbookFee.toFixed(2) }}
            </el-descriptions-item>
            <el-descriptions-item v-if="currentBill.otherFees" label="其他费用">
              ¥{{ currentBill.otherFees.toFixed(2) }}
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="detail-section">
          <h4>缴费情况</h4>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="应缴总额">
              <span class="amount-total">¥{{ currentBill.totalAmount.toFixed(2) }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="已缴金额">
              <span class="amount-paid">¥{{ currentBill.paidAmount.toFixed(2) }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="欠费金额">
              <span class="amount-outstanding">¥{{ currentBill.outstandingAmount.toFixed(2) }}</span>
            </el-descriptions-item>
          </el-descriptions>
        </div>
      </div>

      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button 
          type="primary" 
          :disabled="currentBill?.status === 'PAID'"
          @click="goToPayment(currentBill)"
        >
          立即缴费
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  Calendar, Clock, CreditCard, Warning
} from '@element-plus/icons-vue'
import { getStudentBills } from '@/api/tuition'

const router = useRouter()

// 数据
const bills = ref([])
const loading = ref(false)
const detailDialogVisible = ref(false)
const currentBill = ref(null)

// 统计数据
const billStats = computed(() => {
  return {
    totalBills: bills.value.length,
    unpaidBills: bills.value.filter(b => b.status !== 'PAID').length,
    paidBills: bills.value.filter(b => b.status === 'PAID').length,
    totalOutstanding: bills.value.reduce((sum, b) => sum + b.outstandingAmount, 0).toFixed(2)
  }
})

// 获取账单列表
const fetchBills = async () => {
  try {
    loading.value = true
    const res = await getStudentBills()
    bills.value = res.data || []
  } catch (error) {
    console.error('获取账单列表失败:', error)
    ElMessage.error('获取账单列表失败')
  } finally {
    loading.value = false
  }
}

// 查看账单详情
const viewBillDetail = (bill) => {
  currentBill.value = bill
  detailDialogVisible.value = true
}

// 跳转到缴费页面
const goToPayment = (bill) => {
  if (bill.status === 'PAID') {
    ElMessage.warning('该账单已支付')
    return
  }
  detailDialogVisible.value = false
  router.push({
    path: '/student/tuition/payment',
    query: { billId: bill.id }
  })
}

// 获取账单状态类型
const getBillStatusType = (status) => {
  const typeMap = {
    'UNPAID': 'danger',
    'PARTIAL': 'warning',
    'PAID': 'success',
    'OVERDUE': 'danger'
  }
  return typeMap[status] || 'info'
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
}

// 初始化
onMounted(() => {
  fetchBills()
})
</script>

<style scoped lang="scss">
.bill-list-page {
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

.filter-section {
  margin-bottom: 24px;

  .stats-row {
    display: flex;
    justify-content: space-around;
    align-items: center;

    .stat-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 8px;

      .stat-label {
        font-size: 14px;
        color: #909399;
      }

      .stat-value {
        font-size: 24px;
        font-weight: 600;
        color: #303133;

        &.text-danger {
          color: #F56C6C;
        }

        &.text-success {
          color: #67C23A;
        }
      }
    }
  }
}

.bills-section {
  .bill-cards {
    display: flex;
    flex-direction: column;
    gap: 20px;
  }

  .bill-card {
    border: 2px solid #E4E7ED;
    border-radius: 12px;
    padding: 24px;
    transition: all 0.3s;
    position: relative;
    overflow: hidden;

    &:hover {
      border-color: #409EFF;
      box-shadow: 0 4px 12px rgba(64, 158, 255, 0.15);
    }

    &.overdue {
      border-color: #F56C6C;
      background-color: #FEF0F0;
    }

    .bill-header {
      margin-bottom: 20px;

      .bill-title {
        display: flex;
        align-items: center;
        justify-content: space-between;
        margin-bottom: 12px;

        h3 {
          font-size: 20px;
          font-weight: 600;
          color: #303133;
          margin: 0;
        }
      }

      .bill-meta {
        display: flex;
        gap: 24px;
        color: #909399;
        font-size: 14px;

        .meta-item {
          display: flex;
          align-items: center;
          gap: 6px;
        }
      }
    }

    .bill-body {
      .fee-breakdown {
        display: grid;
        grid-template-columns: repeat(4, 1fr);
        gap: 16px;
        margin-bottom: 20px;

        .fee-item {
          display: flex;
          flex-direction: column;
          gap: 8px;
          padding: 12px;
          background-color: #F5F7FA;
          border-radius: 8px;

          .fee-label {
            font-size: 13px;
            color: #909399;
          }

          .fee-value {
            font-size: 18px;
            font-weight: 600;
            color: #303133;
          }
        }
      }

      .amount-summary {
        display: flex;
        flex-direction: column;
        gap: 12px;

        .summary-row {
          display: flex;
          justify-content: space-between;
          align-items: center;

          .summary-label {
            font-size: 15px;
            color: #606266;
          }

          .summary-value {
            font-size: 18px;
            font-weight: 600;

            &.total {
              color: #303133;
            }

            &.paid {
              color: #67C23A;
            }

            &.outstanding {
              color: #F56C6C;
              font-size: 20px;
            }
          }
        }
      }
    }

    .bill-footer {
      display: flex;
      gap: 12px;
      margin-top: 20px;
    }

    .overdue-warning {
      position: absolute;
      top: 12px;
      right: 12px;
      display: flex;
      align-items: center;
      gap: 6px;
      padding: 6px 12px;
      background-color: #FEF0F0;
      color: #F56C6C;
      border-radius: 4px;
      font-size: 13px;
      font-weight: 500;
    }
  }
}

.bill-detail {
  .detail-section {
    margin-bottom: 24px;

    h4 {
      font-size: 16px;
      font-weight: 600;
      color: #303133;
      margin: 0 0 12px 0;
    }

    .amount-total {
      font-size: 18px;
      font-weight: 600;
      color: #303133;
    }

    .amount-paid {
      font-size: 18px;
      font-weight: 600;
      color: #67C23A;
    }

    .amount-outstanding {
      font-size: 18px;
      font-weight: 600;
      color: #F56C6C;
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

