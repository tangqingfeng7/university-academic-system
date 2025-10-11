<template>
  <div class="refund-applications-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>退费申请管理</span>
        </div>
      </template>

      <!-- 搜索筛选 -->
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="学号">
          <el-input v-model="queryForm.studentNo" placeholder="请输入学号" clearable style="width: 150px" />
        </el-form-item>

        <el-form-item label="姓名">
          <el-input v-model="queryForm.studentName" placeholder="请输入姓名" clearable style="width: 150px" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 数据表格 -->
      <el-table
        v-loading="loading"
        :data="tableData"
        border
        stripe
        style="width: 100%"
      >
        <el-table-column type="index" label="序号" width="60" align="center" />
        
        <el-table-column prop="studentNo" label="学号" width="120" align="center" />
        
        <el-table-column prop="studentName" label="姓名" width="100" align="center" />
        
        <el-table-column prop="refundAmount" label="退费金额" width="120" align="center">
          <template #default="{ row }">
            <span style="color: #f56c6c; font-weight: bold">¥{{ row.refundAmount.toFixed(2) }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="refundType" label="退费类型" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getRefundTypeTag(row.refundType)">
              {{ getRefundTypeLabel(row.refundType) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="refundMethod" label="退费方式" width="120" align="center">
          <template #default="{ row }">
            {{ getRefundMethodLabel(row.refundMethod) }}
          </template>
        </el-table-column>

        <el-table-column prop="reason" label="退费原因" min-width="200" show-overflow-tooltip />

        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="createdAt" label="申请时间" width="160" align="center">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleViewDetail(row)">
              详情
            </el-button>
            <el-button 
              v-if="row.status === 'PENDING'" 
              link 
              type="success" 
              size="small" 
              @click="handleApprove(row)"
            >
              审批
            </el-button>
            <el-button 
              v-if="row.status === 'APPROVED'" 
              link 
              type="warning" 
              size="small" 
              @click="handleExecute(row)"
            >
              执行退费
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="fetchData"
        @current-change="fetchData"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="退费申请详情"
      width="800px"
    >
      <el-descriptions :column="2" border v-if="currentApplication">
        <el-descriptions-item label="申请编号">{{ currentApplication.id }}</el-descriptions-item>
        <el-descriptions-item label="申请状态">
          <el-tag :type="getStatusType(currentApplication.status)">
            {{ getStatusLabel(currentApplication.status) }}
          </el-tag>
        </el-descriptions-item>

        <el-descriptions-item label="学号">{{ currentApplication.studentNo }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ currentApplication.studentName }}</el-descriptions-item>

        <el-descriptions-item label="退费金额">
          <span style="color: #f56c6c; font-weight: bold; font-size: 16px">
            ¥{{ currentApplication.refundAmount?.toFixed(2) }}
          </span>
        </el-descriptions-item>
        <el-descriptions-item label="原缴费金额">
          ¥{{ currentApplication.originalAmount?.toFixed(2) }}
        </el-descriptions-item>

        <el-descriptions-item label="退费类型">
          <el-tag :type="getRefundTypeTag(currentApplication.refundType)">
            {{ getRefundTypeLabel(currentApplication.refundType) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="退费方式">
          {{ getRefundMethodLabel(currentApplication.refundMethod) }}
        </el-descriptions-item>

        <el-descriptions-item label="银行账号" v-if="currentApplication.bankAccount">
          {{ currentApplication.bankAccount }}
        </el-descriptions-item>
        <el-descriptions-item label="申请时间">
          {{ formatDateTime(currentApplication.createdAt) }}
        </el-descriptions-item>

        <el-descriptions-item label="退费原因" :span="2">
          {{ currentApplication.reason }}
        </el-descriptions-item>

        <el-descriptions-item label="证明材料" :span="2" v-if="currentApplication.attachmentUrl">
          <el-link type="primary" :href="currentApplication.attachmentUrl" target="_blank">
            查看附件
          </el-link>
        </el-descriptions-item>
      </el-descriptions>

      <!-- 审批历史 -->
      <el-divider content-position="left">审批历史</el-divider>
      <el-timeline v-if="currentApplication?.approvalHistory && currentApplication.approvalHistory.length > 0">
        <el-timeline-item
          v-for="approval in currentApplication.approvalHistory"
          :key="approval.id"
          :timestamp="formatDateTime(approval.approvedAt)"
          placement="top"
        >
          <el-card>
            <p>
              <el-tag :type="approval.action === 'APPROVE' ? 'success' : 'danger'" size="small">
                {{ approval.action === 'APPROVE' ? '批准' : '拒绝' }}
              </el-tag>
              <span style="margin-left: 10px">审批人：{{ approval.approverName }}</span>
            </p>
            <p style="margin-top: 8px">审批意见：{{ approval.comment }}</p>
          </el-card>
        </el-timeline-item>
      </el-timeline>
      <el-empty v-else description="暂无审批记录" :image-size="80" />

      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 审批对话框 -->
    <el-dialog
      v-model="approvalDialogVisible"
      title="审批退费申请"
      width="500px"
    >
      <el-form :model="approvalForm" :rules="approvalRules" ref="approvalFormRef" label-width="100px">
        <el-form-item label="审批结果" prop="action">
          <el-radio-group v-model="approvalForm.action">
            <el-radio label="APPROVE">批准</el-radio>
            <el-radio label="REJECT">拒绝</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="审批意见" prop="comment">
          <el-input
            v-model="approvalForm.comment"
            type="textarea"
            :rows="4"
            placeholder="请输入审批意见"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="approvalDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitApproval" :loading="submitting">
          提交审批
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import { 
  getRefundApplications, 
  getAdminRefundApplicationDetail,
  approveRefundApplication,
  executeRefund 
} from '@/api/tuition'

// 搜索表单
const queryForm = reactive({
  studentNo: '',
  studentName: ''
})

// 表格数据
const loading = ref(false)
const tableData = ref([])
const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

// 详情对话框
const detailDialogVisible = ref(false)
const currentApplication = ref(null)

// 审批对话框
const approvalDialogVisible = ref(false)
const approvalFormRef = ref(null)
const approvalForm = reactive({
  action: 'APPROVE',
  comment: ''
})
const approvalRules = {
  action: [{ required: true, message: '请选择审批结果', trigger: 'change' }],
  comment: [{ required: true, message: '请输入审批意见', trigger: 'blur' }]
}
const submitting = ref(false)

// 获取数据
const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      ...queryForm,
      page: pagination.page - 1,
      size: pagination.size
    }
    const res = await getRefundApplications(params)
    tableData.value = res.data.content
    pagination.total = res.data.totalElements
  } catch (error) {
    console.error('获取退费申请列表失败:', error)
    ElMessage.error('获取退费申请列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  fetchData()
}

// 重置
const handleReset = () => {
  queryForm.studentNo = ''
  queryForm.studentName = ''
  pagination.page = 1
  fetchData()
}

// 查看详情
const handleViewDetail = async (row) => {
  try {
    const res = await getAdminRefundApplicationDetail(row.id)
    currentApplication.value = res.data
    detailDialogVisible.value = true
  } catch (error) {
    console.error('获取退费申请详情失败:', error)
    ElMessage.error('获取退费申请详情失败')
  }
}

// 审批
const handleApprove = async (row) => {
  currentApplication.value = row
  approvalForm.action = 'APPROVE'
  approvalForm.comment = ''
  approvalDialogVisible.value = true
}

// 提交审批
const handleSubmitApproval = async () => {
  if (!approvalFormRef.value) return
  
  await approvalFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      await approveRefundApplication(currentApplication.value.id, approvalForm)
      ElMessage.success('审批成功')
      approvalDialogVisible.value = false
      fetchData()
    } catch (error) {
      console.error('审批失败:', error)
      ElMessage.error(error.message || '审批失败')
    } finally {
      submitting.value = false
    }
  })
}

// 执行退费
const handleExecute = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确认对学生 ${row.studentName}（${row.studentNo}）执行退费 ¥${row.refundAmount.toFixed(2)} 吗？`,
      '执行退费确认',
      {
        confirmButtonText: '确认执行',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await executeRefund(row.id)
    ElMessage.success('退费执行成功')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('执行退费失败:', error)
      ElMessage.error(error.message || '执行退费失败')
    }
  }
}

// 状态标签
const getStatusType = (status) => {
  const typeMap = {
    PENDING: 'warning',
    APPROVED: 'success',
    REJECTED: 'danger',
    EXECUTED: 'info',
    CANCELLED: 'info'
  }
  return typeMap[status] || ''
}

const getStatusLabel = (status) => {
  const labelMap = {
    PENDING: '待审批',
    APPROVED: '已批准',
    REJECTED: '已拒绝',
    EXECUTED: '已退费',
    CANCELLED: '已取消'
  }
  return labelMap[status] || status
}

// 退费类型
const getRefundTypeTag = (type) => {
  const tagMap = {
    FULL: 'danger',
    PARTIAL: 'warning',
    OVERPAYMENT: 'success'
  }
  return tagMap[type] || ''
}

const getRefundTypeLabel = (type) => {
  const labelMap = {
    FULL: '全额退费',
    PARTIAL: '部分退费',
    OVERPAYMENT: '多缴退费'
  }
  return labelMap[type] || type
}

// 退费方式
const getRefundMethodLabel = (method) => {
  const labelMap = {
    ORIGINAL: '原路退回',
    BANK_CARD: '银行卡',
    ALIPAY: '支付宝',
    WECHAT: '微信',
    CASH: '现金'
  }
  return labelMap[method] || method
}

// 格式化时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.refund-applications-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
  font-size: 16px;
}

.search-form {
  margin-bottom: 20px;
}
</style>

