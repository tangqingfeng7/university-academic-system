<template>
  <div class="offline-payment-page">
    <!-- 页面头部 -->
    <div class="page-header animate-fade-in-down">
      <div class="header-content">
        <h1 class="page-title">线下缴费录入</h1>
        <p class="page-subtitle">录入银行转账、现金缴费等线下缴费记录</p>
      </div>
    </div>

    <!-- 录入表单 -->
    <div class="payment-form-section animate-fade-in-up" style="animation-delay: 0.1s;">
      <el-card shadow="hover">
        <template #header>
          <div class="card-header">
            <span class="header-title">缴费信息录入</span>
            <el-button 
              type="text" 
              @click="handleReset"
              :icon="Refresh"
            >
              重置
            </el-button>
          </div>
        </template>

        <el-form
          ref="formRef"
          :model="form"
          :rules="formRules"
          label-width="120px"
          size="large"
        >
          <!-- 学生信息查询 -->
          <el-form-item label="学号" prop="studentNo">
            <el-input 
              v-model="form.studentNo" 
              placeholder="请输入学号"
              style="width: 300px;"
              @blur="handleSearchStudent"
            >
              <template #append>
                <el-button 
                  :icon="Search"
                  :loading="searching"
                  @click="handleSearchStudent"
                >
                  查询
                </el-button>
              </template>
            </el-input>
          </el-form-item>

          <!-- 学生信息显示 -->
          <el-form-item label="学生姓名" v-if="student">
            <el-input 
              :value="student.name"
              disabled
              style="width: 300px;"
            />
          </el-form-item>

          <el-form-item label="专业" v-if="student">
            <el-input 
              :value="`${student.majorName} (${student.departmentName})`"
              disabled
              style="width: 400px;"
            />
          </el-form-item>

          <!-- 账单选择 -->
          <el-form-item label="学费账单" prop="billId" v-if="bills.length > 0">
            <el-select 
              v-model="form.billId" 
              placeholder="请选择账单"
              style="width: 500px;"
              @change="handleBillChange"
            >
              <el-option
                v-for="bill in bills"
                :key="bill.id"
                :label="`${bill.academicYear} 学年 - 应缴：¥${bill.totalAmount.toFixed(2)} - 已缴：¥${bill.paidAmount.toFixed(2)} - 欠费：¥${bill.outstandingAmount.toFixed(2)}`"
                :value="bill.id"
              />
            </el-select>
          </el-form-item>

          <!-- 账单详情 -->
          <el-form-item label="账单详情" v-if="selectedBill">
            <el-descriptions :column="3" border size="small" style="width: 600px;">
              <el-descriptions-item label="应缴金额">
                ¥{{ selectedBill.totalAmount.toFixed(2) }}
              </el-descriptions-item>
              <el-descriptions-item label="已缴金额">
                ¥{{ selectedBill.paidAmount.toFixed(2) }}
              </el-descriptions-item>
              <el-descriptions-item label="欠费金额">
                <span class="amount-outstanding">
                  ¥{{ selectedBill.outstandingAmount.toFixed(2) }}
                </span>
              </el-descriptions-item>
            </el-descriptions>
          </el-form-item>

          <!-- 缴费信息 -->
          <el-divider content-position="left">缴费信息</el-divider>

          <el-form-item label="缴费金额" prop="amount">
            <el-input-number
              v-model="form.amount"
              :min="0.01"
              :max="selectedBill?.outstandingAmount"
              :precision="2"
              :step="100"
              controls-position="right"
              style="width: 300px;"
            />
            <el-button 
              type="text" 
              v-if="selectedBill"
              @click="form.amount = selectedBill.outstandingAmount"
              style="margin-left: 12px;"
            >
              全额缴清
            </el-button>
          </el-form-item>

          <el-form-item label="缴费方式" prop="method">
            <el-radio-group v-model="form.method">
              <el-radio label="BANK_TRANSFER">银行转账</el-radio>
              <el-radio label="CASH">现金</el-radio>
            </el-radio-group>
          </el-form-item>

          <el-form-item label="交易流水号" prop="transactionId">
            <el-input 
              v-model="form.transactionId" 
              placeholder="银行交易流水号或收据编号"
              style="width: 400px;"
            />
          </el-form-item>

          <el-form-item label="缴费日期" prop="paidAt">
            <el-date-picker
              v-model="form.paidAt"
              type="datetime"
              placeholder="选择缴费时间"
              style="width: 300px;"
            />
          </el-form-item>

          <el-form-item label="备注">
            <el-input 
              v-model="form.remark" 
              type="textarea"
              :rows="3"
              placeholder="备注信息（选填）"
              style="width: 500px;"
            />
          </el-form-item>

          <el-form-item>
            <el-button 
              type="primary" 
              size="large"
              :loading="submitting"
              :disabled="!selectedBill"
              @click="handleSubmit"
            >
              确认录入
            </el-button>
            <el-button size="large" @click="handleReset">
              重置表单
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>

    <!-- 最近录入记录 -->
    <div class="recent-records-section animate-fade-in-up" style="animation-delay: 0.2s;">
      <el-card shadow="hover">
        <template #header>
          <div class="card-header">
            <span class="header-title">最近录入记录</span>
            <el-button 
              type="text" 
              @click="fetchRecentPayments"
              :icon="Refresh"
            >
              刷新
            </el-button>
          </div>
        </template>

        <el-table 
          :data="recentPayments" 
          v-loading="loadingRecords"
          stripe
        >
          <el-table-column prop="paymentNo" label="缴费单号" width="180" />
          <el-table-column prop="studentNo" label="学号" width="120" />
          <el-table-column prop="studentName" label="姓名" width="100" />
          <el-table-column prop="academicYear" label="学年" width="120" align="center" />
          <el-table-column prop="amount" label="缴费金额" width="120" align="right">
            <template #default="{ row }">
              ¥{{ row.amount.toFixed(2) }}
            </template>
          </el-table-column>
          <el-table-column prop="method" label="缴费方式" width="100" align="center">
            <template #default="{ row }">
              <el-tag size="small">{{ row.methodDescription }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="transactionId" label="交易流水号" width="180" />
          <el-table-column prop="paidAt" label="缴费时间" width="160" align="center">
            <template #default="{ row }">
              {{ formatDateTime(row.paidAt) }}
            </template>
          </el-table-column>
          <el-table-column prop="operatorName" label="录入人" width="100" />
        </el-table>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import { 
  getStudentBills,
  recordOfflinePayment,
  getAllPayments
} from '@/api/tuition'
import { getStudentByNo } from '@/api/student'

// 数据
const formRef = ref(null)
const student = ref(null)
const bills = ref([])
const selectedBill = ref(null)
const recentPayments = ref([])
const searching = ref(false)
const submitting = ref(false)
const loadingRecords = ref(false)

const form = reactive({
  studentNo: '',
  billId: null,
  amount: null,
  method: 'BANK_TRANSFER',
  transactionId: '',
  paidAt: new Date(),
  remark: ''
})

// 表单验证规则
const formRules = {
  studentNo: [
    { required: true, message: '请输入学号', trigger: 'blur' }
  ],
  billId: [
    { required: true, message: '请选择账单', trigger: 'change' }
  ],
  amount: [
    { required: true, message: '请输入缴费金额', trigger: 'blur' }
  ],
  method: [
    { required: true, message: '请选择缴费方式', trigger: 'change' }
  ],
  transactionId: [
    { required: true, message: '请输入交易流水号', trigger: 'blur' }
  ],
  paidAt: [
    { required: true, message: '请选择缴费时间', trigger: 'change' }
  ]
}

// 查询学生信息
const handleSearchStudent = async () => {
  if (!form.studentNo.trim()) {
    ElMessage.warning('请输入学号')
    return
  }

  try {
    searching.value = true

    // 查询学生信息
    const studentRes = await getStudentByNo(form.studentNo)
    student.value = studentRes.data

    // 查询学生账单
    const billsRes = await getStudentBills(student.value.id)
    bills.value = billsRes.data.filter(b => b.status !== 'PAID')

    if (bills.value.length === 0) {
      ElMessage.warning('该学生没有待缴费的账单')
    }
  } catch (error) {
    console.error('查询失败:', error)
    ElMessage.error(error.message || '查询学生信息失败')
    student.value = null
    bills.value = []
  } finally {
    searching.value = false
  }
}

// 账单选择变化
const handleBillChange = (billId) => {
  selectedBill.value = bills.value.find(b => b.id === billId)
  if (selectedBill.value) {
    form.amount = selectedBill.value.outstandingAmount
  }
}

// 提交录入
const handleSubmit = async () => {
  try {
    await formRef.value.validate()

    await ElMessageBox.confirm(
      `确认录入该笔缴费记录吗？\n学生：${student.value.name} (${form.studentNo})\n金额：¥${form.amount.toFixed(2)}`,
      '确认录入',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    submitting.value = true

    await recordOfflinePayment(form.billId, {
      amount: form.amount,
      method: form.method,
      transactionId: form.transactionId,
      paidAt: form.paidAt.toISOString()
    })

    ElMessage.success('缴费记录录入成功')
    handleReset()
    fetchRecentPayments()
  } catch (error) {
    if (error !== 'cancel' && error !== false) {
      console.error('录入失败:', error)
      ElMessage.error(error.message || '录入失败')
    }
  } finally {
    submitting.value = false
  }
}

// 重置表单
const handleReset = () => {
  Object.assign(form, {
    studentNo: '',
    billId: null,
    amount: null,
    method: 'BANK_TRANSFER',
    transactionId: '',
    paidAt: new Date(),
    remark: ''
  })
  student.value = null
  bills.value = []
  selectedBill.value = null
  formRef.value?.clearValidate()
}

// 获取最近录入记录
const fetchRecentPayments = async () => {
  try {
    loadingRecords.value = true
    const res = await getAllPayments({
      page: 0,
      size: 10
    })
    recentPayments.value = res.data.content || []
  } catch (error) {
    console.error('获取记录失败:', error)
  } finally {
    loadingRecords.value = false
  }
}

// 格式化日期时间
const formatDateTime = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN')
}

// 初始化
fetchRecentPayments()
</script>

<style scoped lang="scss">
.offline-payment-page {
  padding: 20px;

  .page-header {
    margin-bottom: 24px;

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
  }

  .payment-form-section {
    margin-bottom: 24px;

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .header-title {
        font-size: 16px;
        font-weight: 600;
      }
    }

    .amount-outstanding {
      color: #F56C6C;
      font-weight: 600;
    }
  }

  .recent-records-section {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .header-title {
        font-size: 16px;
        font-weight: 600;
      }
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

