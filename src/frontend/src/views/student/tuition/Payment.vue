<template>
  <div class="payment-page">
    <!-- 页面头部 -->
    <div class="page-header animate-fade-in-down">
      <el-button 
        :icon="ArrowLeft" 
        @click="goBack"
      >
        返回
      </el-button>
      <div class="header-content">
        <h1 class="page-title">学费缴纳</h1>
        <p class="page-subtitle">安全、便捷的在线缴费服务</p>
      </div>
    </div>

    <div class="payment-container" v-loading="loading">
      <!-- 账单信息 -->
      <div class="bill-info-section animate-fade-in-up" style="animation-delay: 0.1s;">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span class="card-title">账单信息</span>
              <el-tag :type="getBillStatusType(billInfo.status)" size="large">
                {{ billInfo.statusDescription }}
              </el-tag>
            </div>
          </template>

          <el-descriptions :column="2" border>
            <el-descriptions-item label="学年">
              {{ billInfo.academicYear }}
            </el-descriptions-item>
            <el-descriptions-item label="截止日期">
              <span :class="{ 'text-danger': billInfo.isOverdue }">
                {{ formatDate(billInfo.dueDate) }}
              </span>
            </el-descriptions-item>
            <el-descriptions-item label="学号">
              {{ billInfo.studentNo }}
            </el-descriptions-item>
            <el-descriptions-item label="姓名">
              {{ billInfo.studentName }}
            </el-descriptions-item>
          </el-descriptions>

          <el-divider />

          <div class="fee-details">
            <h4>费用明细</h4>
            <div class="fee-list">
              <div class="fee-item">
                <span>学费</span>
                <span>¥{{ billInfo.tuitionFee?.toFixed(2) }}</span>
              </div>
              <div class="fee-item">
                <span>住宿费</span>
                <span>¥{{ billInfo.accommodationFee?.toFixed(2) }}</span>
              </div>
              <div class="fee-item">
                <span>教材费</span>
                <span>¥{{ billInfo.textbookFee?.toFixed(2) }}</span>
              </div>
              <div class="fee-item" v-if="billInfo.otherFees">
                <span>其他费用</span>
                <span>¥{{ billInfo.otherFees.toFixed(2) }}</span>
              </div>
            </div>

            <el-divider />

            <div class="fee-summary">
              <div class="summary-item">
                <span>应缴总额</span>
                <span class="total">¥{{ billInfo.totalAmount?.toFixed(2) }}</span>
              </div>
              <div class="summary-item">
                <span>已缴金额</span>
                <span class="paid">¥{{ billInfo.paidAmount?.toFixed(2) }}</span>
              </div>
              <div class="summary-item highlight">
                <span>本次应缴</span>
                <span class="outstanding">¥{{ billInfo.outstandingAmount?.toFixed(2) }}</span>
              </div>
            </div>
          </div>
        </el-card>
      </div>

      <!-- 缴费表单 -->
      <div class="payment-form-section animate-fade-in-up" style="animation-delay: 0.2s;">
        <el-card shadow="hover">
          <template #header>
            <span class="card-title">缴费信息</span>
          </template>

          <el-form 
            ref="paymentFormRef"
            :model="paymentForm"
            :rules="paymentRules"
            label-width="120px"
            label-position="left"
          >
            <el-form-item label="缴费金额" prop="amount">
              <el-input-number
                v-model="paymentForm.amount"
                :min="0.01"
                :max="billInfo.outstandingAmount"
                :precision="2"
                :step="100"
                controls-position="right"
                style="width: 100%;"
              />
              <div class="form-tips">
                <el-button 
                  link 
                  type="primary"
                  @click="paymentForm.amount = billInfo.outstandingAmount"
                >
                  一次性付清
                </el-button>
              </div>
            </el-form-item>

            <el-form-item label="支付方式" prop="method">
              <el-radio-group v-model="paymentForm.method">
                <el-radio-button value="ALIPAY">
                  <div class="payment-method">
                    <el-icon :size="24"><Wallet /></el-icon>
                    <span>支付宝</span>
                  </div>
                </el-radio-button>
                <el-radio-button value="WECHAT">
                  <div class="payment-method">
                    <el-icon :size="24"><ChatLineSquare /></el-icon>
                    <span>微信支付</span>
                  </div>
                </el-radio-button>
                <el-radio-button value="BANK_CARD">
                  <div class="payment-method">
                    <el-icon :size="24"><CreditCard /></el-icon>
                    <span>银行卡</span>
                  </div>
                </el-radio-button>
              </el-radio-group>
            </el-form-item>

            <el-form-item label="备注" prop="remark">
              <el-input
                v-model="paymentForm.remark"
                type="textarea"
                :rows="3"
                placeholder="选填，可以填写备注信息"
                maxlength="200"
                show-word-limit
              />
            </el-form-item>
          </el-form>

          <div class="payment-footer">
            <div class="amount-display">
              <span class="label">应付金额：</span>
              <span class="amount">¥{{ paymentForm.amount.toFixed(2) }}</span>
            </div>
            <el-button 
              type="primary" 
              size="large"
              :loading="paying"
              :disabled="billInfo.status === 'PAID'"
              @click="handlePay"
            >
              <el-icon><CreditCard /></el-icon>
              确认支付
            </el-button>
          </div>
        </el-card>
      </div>

      <!-- 安全提示 -->
      <div class="security-tips animate-fade-in-up" style="animation-delay: 0.3s;">
        <el-alert 
          type="info" 
          :closable="false"
          show-icon
        >
          <template #title>
            <div class="tips-title">
              <el-icon><InfoFilled /></el-icon>
              安全提示
            </div>
          </template>
          <ul class="tips-list">
            <li>请确认账单信息无误后再进行支付</li>
            <li>支付成功后会自动生成电子收据，可在"缴费记录"中查看和下载</li>
            <li>如支付过程中遇到问题，请联系财务处或教务处</li>
            <li>请勿重复支付，如有疑问请及时咨询</li>
          </ul>
        </el-alert>
      </div>
    </div>

    <!-- 支付成功对话框 -->
    <el-dialog
      v-model="successDialogVisible"
      title="支付成功"
      width="500px"
      :close-on-click-modal="false"
      :show-close="false"
    >
      <div class="success-content">
        <el-result 
          icon="success" 
          title="支付成功！"
          :sub-title="`您已成功支付 ¥${paymentForm.amount.toFixed(2)}`"
        >
          <template #extra>
            <div class="result-info">
              <p>支付单号：{{ paymentNo }}</p>
              <p>支付时间：{{ formatDateTime(new Date()) }}</p>
            </div>
          </template>
        </el-result>
      </div>

      <template #footer>
        <el-button @click="goToPayments">查看缴费记录</el-button>
        <el-button type="primary" @click="goToBills">返回账单列表</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  ArrowLeft, Wallet, ChatLineSquare, CreditCard, 
  InfoFilled
} from '@element-plus/icons-vue'
import { getBillDetail, createPayment } from '@/api/tuition'

const router = useRouter()
const route = useRoute()

// 数据
const billInfo = ref({})
const loading = ref(false)
const paying = ref(false)
const successDialogVisible = ref(false)
const paymentNo = ref('')

const paymentFormRef = ref(null)
const paymentForm = reactive({
  billId: null,
  amount: 0,
  method: 'ALIPAY',
  remark: ''
})

// 表单验证规则
const paymentRules = {
  amount: [
    { required: true, message: '请输入缴费金额', trigger: 'blur' },
    { 
      validator: (rule, value, callback) => {
        if (value <= 0) {
          callback(new Error('缴费金额必须大于0'))
        } else if (value > billInfo.value.outstandingAmount) {
          callback(new Error('缴费金额不能超过欠费金额'))
        } else {
          callback()
        }
      }, 
      trigger: 'change' 
    }
  ],
  method: [
    { required: true, message: '请选择支付方式', trigger: 'change' }
  ]
}

// 获取账单详情
const fetchBillDetail = async (billId) => {
  try {
    loading.value = true
    const res = await getBillDetail(billId)
    billInfo.value = res.data
    
    // 初始化缴费金额为欠费金额
    paymentForm.billId = billId
    paymentForm.amount = billInfo.value.outstandingAmount || 0
  } catch (error) {
    console.error('获取账单详情失败:', error)
    ElMessage.error('获取账单详情失败')
    goBack()
  } finally {
    loading.value = false
  }
}

// 处理支付
const handlePay = async () => {
  try {
    // 表单验证
    await paymentFormRef.value.validate()

    // 确认支付
    await ElMessageBox.confirm(
      `确认支付 ¥${paymentForm.amount.toFixed(2)} 吗？`,
      '确认支付',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    paying.value = true

    // 调用支付接口
    const res = await createPayment(paymentForm)
    paymentNo.value = res.data.paymentNo

    // 显示支付成功对话框
    successDialogVisible.value = true

    ElMessage.success('支付成功！')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('支付失败:', error)
      ElMessage.error(error.message || '支付失败，请重试')
    }
  } finally {
    paying.value = false
  }
}

// 返回
const goBack = () => {
  router.back()
}

// 跳转到缴费记录
const goToPayments = () => {
  successDialogVisible.value = false
  router.push('/student/tuition/payments')
}

// 跳转到账单列表
const goToBills = () => {
  successDialogVisible.value = false
  router.push('/student/tuition/bills')
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
  return date.toLocaleDateString('zh-CN')
}

// 格式化日期时间
const formatDateTime = (date) => {
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

// 初始化
onMounted(() => {
  const billId = route.query.billId
  if (!billId) {
    ElMessage.error('缺少账单ID参数')
    goBack()
    return
  }
  fetchBillDetail(billId)
})
</script>

<style scoped lang="scss">
.payment-page {
  padding: 20px;
  max-width: 900px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 24px;

  .header-content {
    margin-top: 16px;

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

.payment-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;

  .card-title {
    font-size: 18px;
    font-weight: 600;
    color: #303133;
  }
}

.bill-info-section {
  .fee-details {
    h4 {
      font-size: 16px;
      font-weight: 600;
      color: #303133;
      margin: 0 0 16px 0;
    }

    .fee-list {
      display: flex;
      flex-direction: column;
      gap: 12px;

      .fee-item {
        display: flex;
        justify-content: space-between;
        padding: 12px;
        background-color: #F5F7FA;
        border-radius: 6px;

        span:first-child {
          color: #606266;
        }

        span:last-child {
          font-weight: 600;
          color: #303133;
        }
      }
    }

    .fee-summary {
      display: flex;
      flex-direction: column;
      gap: 16px;

      .summary-item {
        display: flex;
        justify-content: space-between;
        align-items: center;

        &.highlight {
          padding: 16px;
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          border-radius: 8px;
          color: white;

          span {
            color: white !important;
            font-size: 20px;
            font-weight: 600;
          }
        }

        span:first-child {
          font-size: 15px;
          color: #606266;
        }

        span:last-child {
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
}

.payment-form-section {
  .form-tips {
    margin-top: 8px;
  }

  .payment-method {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    padding: 12px 20px;
  }

  .payment-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 24px;
    padding-top: 24px;
    border-top: 1px solid #E4E7ED;

    .amount-display {
      .label {
        font-size: 16px;
        color: #606266;
      }

      .amount {
        font-size: 28px;
        font-weight: 600;
        color: #F56C6C;
        margin-left: 12px;
      }
    }
  }
}

.security-tips {
  .tips-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-weight: 600;
  }

  .tips-list {
    margin: 12px 0 0 0;
    padding-left: 20px;
    
    li {
      margin-bottom: 8px;
      color: #606266;
      line-height: 1.6;
    }
  }
}

.success-content {
  .result-info {
    text-align: left;
    margin-top: 20px;

    p {
      margin: 8px 0;
      color: #606266;
      font-size: 14px;
    }
  }
}

.text-danger {
  color: #F56C6C;
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

