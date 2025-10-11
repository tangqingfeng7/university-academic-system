<template>
  <div class="tuition-bills-page">
    <!-- 页面头部 -->
    <div class="page-header animate-fade-in-down">
      <div class="header-content">
        <h1 class="page-title">学费账单管理</h1>
        <p class="page-subtitle">管理学生学费账单和缴费情况</p>
      </div>
      <div class="header-actions">
        <el-button 
          type="success" 
          :icon="DocumentAdd"
          size="large"
          @click="batchGenerateDialogVisible = true"
        >
          批量生成账单
        </el-button>
        <el-button 
          type="primary" 
          :icon="Download"
          size="large"
          @click="handleExport"
        >
          导出账单
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
          <el-form-item label="学号">
            <el-input 
              v-model="queryForm.studentNo" 
              placeholder="学号"
              clearable
              style="width: 150px;"
            />
          </el-form-item>
          <el-form-item label="姓名">
            <el-input 
              v-model="queryForm.studentName" 
              placeholder="学生姓名"
              clearable
              style="width: 150px;"
            />
          </el-form-item>
          <el-form-item label="状态">
            <el-select 
              v-model="queryForm.status" 
              placeholder="缴费状态"
              clearable
              style="width: 120px;"
            >
              <el-option label="未缴费" value="UNPAID" />
              <el-option label="部分缴费" value="PARTIAL" />
              <el-option label="已缴费" value="PAID" />
              <el-option label="已逾期" value="OVERDUE" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :icon="Search" @click="fetchBills">查询</el-button>
            <el-button :icon="Refresh" @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-section animate-fade-in-up" style="animation-delay: 0.2s;">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content">
              <div class="stat-icon" style="background: #409EFF;">
                <el-icon :size="24"><Document /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ stats.totalBills }}</div>
                <div class="stat-label">总账单数</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content">
              <div class="stat-icon" style="background: #67C23A;">
                <el-icon :size="24"><Check /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ stats.paidBills }}</div>
                <div class="stat-label">已缴费</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content">
              <div class="stat-icon" style="background: #F56C6C;">
                <el-icon :size="24"><Warning /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ stats.unpaidBills }}</div>
                <div class="stat-label">待缴费</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content">
              <div class="stat-icon" style="background: #E6A23C;">
                <el-icon :size="24"><Money /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">¥{{ stats.totalOutstanding }}</div>
                <div class="stat-label">欠费总额</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 账单列表 -->
    <div class="bills-section animate-fade-in-up" style="animation-delay: 0.3s;">
      <el-card shadow="hover">
        <el-table 
          :data="bills" 
          v-loading="loading"
          stripe
          border
          style="width: 100%"
        >
          <el-table-column prop="studentNo" label="学号" width="120" />
          <el-table-column prop="studentName" label="姓名" width="100" />
          <el-table-column prop="majorName" label="专业" width="150" />
          <el-table-column prop="academicYear" label="学年" width="120" align="center" />
          <el-table-column prop="totalAmount" label="应缴金额" width="120" align="right">
            <template #default="{ row }">
              ¥{{ row.totalAmount.toFixed(2) }}
            </template>
          </el-table-column>
          <el-table-column prop="paidAmount" label="已缴金额" width="120" align="right">
            <template #default="{ row }">
              ¥{{ row.paidAmount.toFixed(2) }}
            </template>
          </el-table-column>
          <el-table-column prop="outstandingAmount" label="欠费金额" width="120" align="right">
            <template #default="{ row }">
              <span :class="{ 'amount-danger': row.outstandingAmount > 0 }">
                ¥{{ row.outstandingAmount.toFixed(2) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="dueDate" label="截止日期" width="120" align="center">
            <template #default="{ row }">
              <span :class="{ 'date-danger': row.isOverdue && row.status !== 'PAID' }">
                {{ formatDate(row.dueDate) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="getBillStatusType(row.status)">
                {{ row.statusDescription }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right" align="center">
            <template #default="{ row }">
              <el-button type="primary" link :icon="View" @click="handleView(row)">
                查看
              </el-button>
              <el-button 
                type="warning" 
                link 
                :icon="Bell"
                :disabled="row.status === 'PAID'"
                @click="handleSendReminder(row)"
              >
                催缴
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination-wrapper">
          <el-pagination
            v-model:current-page="pagination.page"
            v-model:page-size="pagination.size"
            :total="pagination.total"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="fetchBills"
            @current-change="fetchBills"
          />
        </div>
      </el-card>
    </div>

    <!-- 批量生成账单对话框 -->
    <el-dialog
      v-model="batchGenerateDialogVisible"
      title="批量生成学费账单"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="generateFormRef"
        :model="generateForm"
        :rules="generateRules"
        label-width="100px"
      >
        <el-alert 
          type="info" 
          :closable="false"
          show-icon
          style="margin-bottom: 20px;"
        >
          <template #title>
            批量生成说明
          </template>
          系统将根据学费标准为指定学年的所有在校学生自动生成学费账单。
        </el-alert>

        <el-form-item label="学年" prop="academicYear">
          <el-input 
            v-model="generateForm.academicYear" 
            placeholder="如：2024-2025"
          />
        </el-form-item>

        <el-form-item label="截止日期" prop="dueDate">
          <el-date-picker
            v-model="generateForm.dueDate"
            type="date"
            placeholder="选择截止日期"
            style="width: 100%;"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="batchGenerateDialogVisible = false">取消</el-button>
        <el-button 
          type="primary" 
          :loading="generating"
          @click="handleBatchGenerate"
        >
          开始生成
        </el-button>
      </template>
    </el-dialog>

    <!-- 账单详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="账单详情"
      width="700px"
      :close-on-click-modal="false"
    >
      <div v-if="currentBill" class="bill-detail">
        <el-descriptions :column="2" border>
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
          <el-descriptions-item label="学年">
            {{ currentBill.academicYear }}
          </el-descriptions-item>
          <el-descriptions-item label="入学年份">
            {{ currentBill.enrollmentYear }}
          </el-descriptions-item>
          <el-descriptions-item label="学费">
            ¥{{ currentBill.tuitionFee?.toFixed(2) }}
          </el-descriptions-item>
          <el-descriptions-item label="住宿费">
            ¥{{ currentBill.accommodationFee?.toFixed(2) }}
          </el-descriptions-item>
          <el-descriptions-item label="教材费">
            ¥{{ currentBill.textbookFee?.toFixed(2) }}
          </el-descriptions-item>
          <el-descriptions-item label="其他费用" v-if="currentBill.otherFees">
            ¥{{ currentBill.otherFees?.toFixed(2) }}
          </el-descriptions-item>
          <el-descriptions-item label="应缴总额">
            <span class="amount-total">¥{{ currentBill.totalAmount?.toFixed(2) }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="已缴金额">
            <span class="amount-paid">¥{{ currentBill.paidAmount?.toFixed(2) }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="欠费金额">
            <span class="amount-outstanding">¥{{ currentBill.outstandingAmount?.toFixed(2) }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getBillStatusType(currentBill.status)">
              {{ currentBill.statusDescription }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="截止日期">
            {{ formatDate(currentBill.dueDate) }}
          </el-descriptions-item>
          <el-descriptions-item label="生成时间">
            {{ formatDateTime(currentBill.generatedAt) }}
          </el-descriptions-item>
        </el-descriptions>
      </div>

      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button 
          type="warning"
          :disabled="currentBill?.status === 'PAID'"
          @click="handleSendReminder(currentBill)"
        >
          发送催缴通知
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  DocumentAdd, Download, Search, Refresh, View, Bell,
  Document, Check, Warning, Money
} from '@element-plus/icons-vue'
import { 
  getAllBills,
  batchGenerateBills,
  sendPaymentReminder,
  exportBillList
} from '@/api/tuition'

// 数据
const bills = ref([])
const loading = ref(false)
const batchGenerateDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const generating = ref(false)
const currentBill = ref(null)

const queryForm = reactive({
  academicYear: '',
  studentNo: '',
  studentName: '',
  status: null
})

const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

const generateFormRef = ref(null)
const generateForm = reactive({
  academicYear: '',
  dueDate: null
})

// 统计数据
const stats = computed(() => {
  return {
    totalBills: bills.value.length,
    paidBills: bills.value.filter(b => b.status === 'PAID').length,
    unpaidBills: bills.value.filter(b => b.status !== 'PAID').length,
    totalOutstanding: bills.value.reduce((sum, b) => sum + b.outstandingAmount, 0).toFixed(2)
  }
})

// 表单验证规则
const generateRules = {
  academicYear: [
    { required: true, message: '请输入学年', trigger: 'blur' },
    { pattern: /^\d{4}-\d{4}$/, message: '格式为：2024-2025', trigger: 'blur' }
  ],
  dueDate: [
    { required: true, message: '请选择截止日期', trigger: 'change' }
  ]
}

// 获取账单列表
const fetchBills = async () => {
  try {
    loading.value = true
    const params = {
      ...queryForm,
      page: pagination.page - 1,
      size: pagination.size
    }
    const res = await getAllBills(params)
    bills.value = res.data.content || []
    pagination.total = res.data.totalElements || 0
  } catch (error) {
    console.error('获取账单列表失败:', error)
    ElMessage.error('获取账单列表失败')
  } finally {
    loading.value = false
  }
}

// 重置查询
const handleReset = () => {
  Object.assign(queryForm, {
    academicYear: '',
    studentNo: '',
    studentName: '',
    status: null
  })
  pagination.page = 1
  fetchBills()
}

// 批量生成账单
const handleBatchGenerate = async () => {
  try {
    await generateFormRef.value.validate()

    await ElMessageBox.confirm(
      `确认为${generateForm.academicYear}学年的所有在校学生生成学费账单吗？`,
      '确认生成',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    generating.value = true

    await batchGenerateBills({
      academicYear: generateForm.academicYear,
      dueDate: formatDateForAPI(generateForm.dueDate)
    })

    ElMessage.success('账单生成成功')
    batchGenerateDialogVisible.value = false
    fetchBills()
  } catch (error) {
    if (error !== 'cancel' && error !== false) {
      console.error('生成账单失败:', error)
      ElMessage.error(error.message || '生成失败')
    }
  } finally {
    generating.value = false
  }
}

// 查看详情
const handleView = (row) => {
  currentBill.value = row
  detailDialogVisible.value = true
}

// 发送催缴通知
const handleSendReminder = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确认向学生${row.studentName}发送催缴通知吗？`,
      '确认操作',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await sendPaymentReminder(row.id)
    ElMessage.success('催缴通知已发送')
    detailDialogVisible.value = false
  } catch (error) {
    if (error !== 'cancel') {
      console.error('发送通知失败:', error)
      ElMessage.error('发送失败')
    }
  }
}

// 导出账单
const handleExport = async () => {
  try {
    const res = await exportBillList(queryForm)
    
    // 创建下载链接
    const url = window.URL.createObjectURL(new Blob([res]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', `学费账单_${new Date().getTime()}.xlsx`)
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
const formatDateTime = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN')
}

// 格式化日期为API格式
const formatDateForAPI = (date) => {
  if (!date) return null
  const d = new Date(date)
  return d.toISOString().split('T')[0]
}

// 初始化
onMounted(() => {
  fetchBills()
})
</script>

<style scoped lang="scss">
.tuition-bills-page {
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

  .stats-section {
    margin-bottom: 20px;

    .stat-card {
      .stat-content {
        display: flex;
        align-items: center;
        gap: 16px;

        .stat-icon {
          width: 56px;
          height: 56px;
          display: flex;
          align-items: center;
          justify-content: center;
          border-radius: 12px;
          color: white;
        }

        .stat-info {
          .stat-value {
            font-size: 24px;
            font-weight: 600;
            color: #303133;
            margin-bottom: 4px;
          }

          .stat-label {
            font-size: 14px;
            color: #909399;
          }
        }
      }
    }
  }

  .bills-section {
    .amount-danger {
      color: #F56C6C;
      font-weight: 600;
    }

    .date-danger {
      color: #F56C6C;
    }
  }

  .pagination-wrapper {
    display: flex;
    justify-content: flex-end;
    margin-top: 20px;
  }

  .bill-detail {
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

