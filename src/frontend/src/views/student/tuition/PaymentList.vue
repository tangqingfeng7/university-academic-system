<template>
  <div class="payment-list-page">
    <!-- 页面头部 -->
    <div class="page-header animate-fade-in-down">
      <div class="header-content">
        <h1 class="page-title">缴费记录</h1>
        <p class="page-subtitle">查看你的所有学费缴纳记录</p>
      </div>
    </div>

    <!-- 缴费记录列表 -->
    <div class="payments-section animate-fade-in-up" style="animation-delay: 0.1s;">
      <el-card shadow="hover" v-loading="loading">
        <template #header>
          <div class="card-header">
            <span class="card-title">缴费记录</span>
            <span class="record-count">共 {{ payments.length }} 条记录</span>
          </div>
        </template>

        <el-empty 
          v-if="payments.length === 0 && !loading"
          description="暂无缴费记录"
        />

        <el-timeline v-else>
          <el-timeline-item
            v-for="payment in payments"
            :key="payment.id"
            :timestamp="formatDateTime(payment.paidAt || payment.createdAt)"
            placement="top"
            :color="getPaymentStatusColor(payment.status)"
          >
            <el-card shadow="hover" class="payment-card">
              <div class="payment-header">
                <div class="payment-title">
                  <h4>{{ payment.academicYear }} 学年学费</h4>
                  <el-tag 
                    :type="getPaymentStatusType(payment.status)"
                    size="large"
                  >
                    {{ payment.statusDescription }}
                  </el-tag>
                </div>
                <div class="payment-amount">
                  ¥{{ payment.amount.toFixed(2) }}
                </div>
              </div>

              <div class="payment-body">
                <el-descriptions :column="2" border size="small">
                  <el-descriptions-item label="缴费单号">
                    <el-text copyable>{{ payment.paymentNo }}</el-text>
                  </el-descriptions-item>
                  <el-descriptions-item label="支付方式">
                    {{ payment.methodDescription }}
                  </el-descriptions-item>
                  <el-descriptions-item label="缴费时间">
                    {{ formatDateTime(payment.paidAt) }}
                  </el-descriptions-item>
                  <el-descriptions-item label="交易号" v-if="payment.transactionId">
                    <el-text copyable>{{ payment.transactionId }}</el-text>
                  </el-descriptions-item>
                  <el-descriptions-item label="备注" :span="2" v-if="payment.remark">
                    {{ payment.remark }}
                  </el-descriptions-item>
                </el-descriptions>
              </div>

              <div class="payment-footer">
                <el-button 
                  type="primary" 
                  size="small"
                  :icon="Download"
                  :loading="downloadingReceipt[payment.id]"
                  @click="handleDownloadReceipt(payment)"
                  v-if="payment.status === 'SUCCESS'"
                >
                  下载电子收据
                </el-button>
                <el-button 
                  type="warning" 
                  size="small"
                  @click="handleRefund(payment)"
                  v-if="payment.status === 'SUCCESS'"
                >
                  申请退费
                </el-button>
              </div>
            </el-card>
          </el-timeline-item>
        </el-timeline>
      </el-card>
    </div>

    <!-- 退费申请对话框 -->
    <el-dialog
      v-model="refundDialogVisible"
      title="申请退费"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="refundFormRef"
        :model="refundForm"
        :rules="refundRules"
        label-width="100px"
      >
        <el-alert
          type="info"
          :closable="false"
          show-icon
          style="margin-bottom: 20px;"
        >
          <template #title>
            退费说明
          </template>
          退费申请提交后需要经过财务审核和管理员审批，请耐心等待。退费成功后，款项将原路退回。
        </el-alert>

        <el-form-item label="缴费单号">
          <el-input :value="currentPayment?.paymentNo" disabled />
        </el-form-item>

        <el-form-item label="缴费金额">
          <el-input :value="`¥${currentPayment?.amount.toFixed(2)}`" disabled />
        </el-form-item>

        <el-form-item label="退费金额" prop="refundAmount">
          <el-input-number
            v-model="refundForm.refundAmount"
            :min="0.01"
            :max="currentPayment?.amount"
            :precision="2"
            :step="100"
            controls-position="right"
            style="width: 100%;"
          />
          <div class="form-tips">
            <el-button 
              link 
              type="primary"
              @click="refundForm.refundAmount = currentPayment?.amount"
            >
              全额退费
            </el-button>
          </div>
        </el-form-item>

        <el-form-item label="退费类型" prop="refundType">
          <el-radio-group v-model="refundForm.refundType">
            <el-radio value="FULL">全额退费</el-radio>
            <el-radio value="PARTIAL">部分退费</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="退费方式" prop="refundMethod">
          <el-radio-group v-model="refundForm.refundMethod">
            <el-radio value="ORIGINAL">原路退回</el-radio>
            <el-radio value="BANK_TRANSFER">银行转账</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item 
          label="银行账号" 
          prop="bankAccount"
          v-if="refundForm.refundMethod === 'BANK_TRANSFER'"
        >
          <el-input
            v-model="refundForm.bankAccount"
            placeholder="请输入银行卡号"
            maxlength="19"
          />
        </el-form-item>

        <el-form-item label="退费原因" prop="reason">
          <el-input
            v-model="refundForm.reason"
            type="textarea"
            :rows="4"
            placeholder="请详细说明退费原因"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="证明材料">
          <el-upload
            ref="uploadRef"
            :auto-upload="false"
            :limit="1"
            accept=".jpg,.jpeg,.png,.pdf"
            :on-change="handleFileChange"
          >
            <template #trigger>
              <el-button :icon="Upload">选择文件</el-button>
            </template>
            <template #tip>
              <div class="el-upload__tip">
                支持 jpg/png/pdf 格式，文件大小不超过 5MB
              </div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="refundDialogVisible = false">取消</el-button>
        <el-button 
          type="primary" 
          :loading="submittingRefund"
          @click="handleSubmitRefund"
        >
          提交申请
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Download, Upload
} from '@element-plus/icons-vue'
import { 
  getStudentPayments, 
  downloadReceipt,
  submitRefundApplication 
} from '@/api/tuition'

const router = useRouter()

// 数据
const payments = ref([])
const loading = ref(false)
const downloadingReceipt = ref({})
const refundDialogVisible = ref(false)
const currentPayment = ref(null)
const submittingRefund = ref(false)

const refundFormRef = ref(null)
const uploadRef = ref(null)
const refundForm = reactive({
  paymentId: null,
  refundAmount: 0,
  reason: '',
  refundType: 'FULL',
  refundMethod: 'ORIGINAL',
  bankAccount: '',
  attachment: null
})

// 表单验证规则
const refundRules = {
  refundAmount: [
    { required: true, message: '请输入退费金额', trigger: 'blur' }
  ],
  refundType: [
    { required: true, message: '请选择退费类型', trigger: 'change' }
  ],
  refundMethod: [
    { required: true, message: '请选择退费方式', trigger: 'change' }
  ],
  bankAccount: [
    { 
      validator: (rule, value, callback) => {
        if (refundForm.refundMethod === 'BANK_TRANSFER' && !value) {
          callback(new Error('请输入银行账号'))
        } else {
          callback()
        }
      }, 
      trigger: 'blur' 
    }
  ],
  reason: [
    { required: true, message: '请填写退费原因', trigger: 'blur' },
    { min: 10, message: '退费原因至少10个字符', trigger: 'blur' }
  ]
}

// 获取缴费记录
const fetchPayments = async () => {
  try {
    loading.value = true
    const res = await getStudentPayments()
    payments.value = res.data || []
  } catch (error) {
    console.error('获取缴费记录失败:', error)
    ElMessage.error('获取缴费记录失败')
  } finally {
    loading.value = false
  }
}

// 下载电子收据
const handleDownloadReceipt = async (payment) => {
  try {
    downloadingReceipt.value[payment.id] = true
    const res = await downloadReceipt(payment.id)
    
    // 创建下载链接
    const url = window.URL.createObjectURL(new Blob([res]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', `收据_${payment.paymentNo}.pdf`)
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    
    ElMessage.success('下载成功')
  } catch (error) {
    console.error('下载收据失败:', error)
    ElMessage.error('下载收据失败')
  } finally {
    downloadingReceipt.value[payment.id] = false
  }
}

// 处理退费申请
const handleRefund = (payment) => {
  currentPayment.value = payment
  refundForm.paymentId = payment.id
  refundForm.refundAmount = payment.amount
  refundForm.reason = ''
  refundForm.refundType = 'FULL'
  refundForm.refundMethod = 'ORIGINAL'
  refundForm.bankAccount = ''
  refundForm.attachment = null
  refundDialogVisible.value = true
}

// 处理文件选择
const handleFileChange = (file) => {
  // 验证文件大小
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isLt5M) {
    ElMessage.error('文件大小不能超过 5MB!')
    uploadRef.value.clearFiles()
    return
  }
  refundForm.attachment = file.raw
}

// 提交退费申请
const handleSubmitRefund = async () => {
  try {
    // 表单验证
    await refundFormRef.value.validate()

    // 确认提交
    await ElMessageBox.confirm(
      `确认申请退费 ¥${refundForm.refundAmount.toFixed(2)} 吗？`,
      '确认提交',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    submittingRefund.value = true

    // 提交申请
    await submitRefundApplication(refundForm)

    ElMessage.success('退费申请提交成功，请等待审批')
    refundDialogVisible.value = false

    // 跳转到退费申请列表
    router.push('/student/tuition/refund-applications')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('提交退费申请失败:', error)
      ElMessage.error(error.message || '提交失败，请重试')
    }
  } finally {
    submittingRefund.value = false
  }
}

// 获取支付状态颜色
const getPaymentStatusColor = (status) => {
  const colorMap = {
    'PENDING': '#909399',
    'SUCCESS': '#67C23A',
    'FAILED': '#F56C6C',
    'REFUNDED': '#E6A23C'
  }
  return colorMap[status] || '#909399'
}

// 获取支付状态类型
const getPaymentStatusType = (status) => {
  const typeMap = {
    'PENDING': 'info',
    'SUCCESS': 'success',
    'FAILED': 'danger',
    'REFUNDED': 'warning'
  }
  return typeMap[status] || 'info'
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
  fetchPayments()
})
</script>

<style scoped lang="scss">
.payment-list-page {
  padding: 20px;
  max-width: 1000px;
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

.payments-section {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .card-title {
      font-size: 18px;
      font-weight: 600;
      color: #303133;
    }

    .record-count {
      font-size: 14px;
      color: #909399;
    }
  }

  .payment-card {
    margin-top: 16px;

    .payment-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: 16px;

      .payment-title {
        h4 {
          font-size: 16px;
          font-weight: 600;
          color: #303133;
          margin: 0 0 8px 0;
        }
      }

      .payment-amount {
        font-size: 24px;
        font-weight: 600;
        color: #409EFF;
      }
    }

    .payment-body {
      margin-bottom: 16px;
    }

    .payment-footer {
      display: flex;
      gap: 12px;
      justify-content: flex-end;
      padding-top: 16px;
      border-top: 1px solid #E4E7ED;
    }
  }
}

.form-tips {
  margin-top: 8px;
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

