<template>
  <div class="leave-requests-management">
    <!-- 页面头部 -->
    <div class="page-header animate-fade-in-down">
      <div class="header-content">
        <h1 class="page-title">请假审批</h1>
        <p class="page-subtitle">审批和管理请假申请</p>
      </div>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar animate-fade-in-up" style="animation-delay: 0.1s;">
      <div class="filter-item">
        <el-select
          v-model="filters.status"
          placeholder="所有状态"
          clearable
          style="width: 100%"
          @change="fetchLeaveRequests"
        >
          <el-option label="待审批" value="PENDING" />
          <el-option label="已批准" value="APPROVED" />
          <el-option label="已拒绝" value="REJECTED" />
          <el-option label="已取消" value="CANCELLED" />
        </el-select>
      </div>
      <div class="filter-item">
        <el-select
          v-model="filters.applicantType"
          placeholder="所有类型"
          clearable
          style="width: 100%"
          @change="fetchLeaveRequests"
        >
          <el-option label="学生" value="STUDENT" />
          <el-option label="教师" value="TEACHER" />
        </el-select>
      </div>
      <div class="filter-item">
        <el-input
          v-model="filters.keyword"
          placeholder="搜索姓名或学号/工号"
          clearable
          @clear="fetchLeaveRequests"
          @keyup.enter="fetchLeaveRequests"
        >
          <template #append>
            <el-button :icon="Search" @click="fetchLeaveRequests" />
          </template>
        </el-input>
      </div>
    </div>

    <!-- 请假列表 -->
    <div
      v-loading="loading"
      class="leave-list animate-fade-in-up"
      style="animation-delay: 0.2s;"
    >
      <div
        v-for="(leave, index) in leaveList"
        :key="leave.id"
        class="leave-card"
        :style="{ 'animation-delay': `${0.25 + index * 0.05}s` }"
      >
        <div class="leave-header">
          <div class="leave-meta">
            <el-tag :type="getApplicantTypeStyle(leave.applicantType)" size="small">
              {{ leave.applicantTypeDescription }}
            </el-tag>
            <span class="meta-divider">·</span>
            <el-tag :type="getTypeStyle(leave.leaveType)" size="small">
              {{ leave.leaveTypeDescription }}
            </el-tag>
            <span class="meta-divider">·</span>
            <el-tag :type="getStatusStyle(leave.status)" size="small">
              {{ leave.statusDescription }}
            </el-tag>
          </div>
          <div class="leave-actions" v-if="leave.status === 'PENDING'">
            <el-button
              type="success"
              size="small"
              plain
              @click="handleApprove(leave, 'APPROVED')"
            >
              批准
            </el-button>
            <el-button
              type="danger"
              size="small"
              plain
              @click="handleApprove(leave, 'REJECTED')"
            >
              拒绝
            </el-button>
          </div>
        </div>

        <div class="leave-info">
          <div class="info-row">
            <span class="label">申请人</span>
            <span class="value">{{ leave.applicantName }} ({{ leave.applicantNo }})</span>
          </div>
          <div class="info-row">
            <span class="label">请假时间</span>
            <span class="value">
              {{ formatDate(leave.startDate) }} 至 {{ formatDate(leave.endDate) }}
              （{{ leave.days }} 天）
            </span>
          </div>
          <div class="info-row">
            <span class="label">请假原因</span>
            <span class="value reason-text">{{ leave.reason }}</span>
          </div>
          <div class="info-row" v-if="leave.approverName">
            <span class="label">审批人</span>
            <span class="value">{{ leave.approverName }}</span>
          </div>
          <div class="info-row" v-if="leave.approvalComment">
            <span class="label">审批意见</span>
            <span class="value">{{ leave.approvalComment }}</span>
          </div>
          <div class="info-row" v-if="leave.approvalTime">
            <span class="label">审批时间</span>
            <span class="value">{{ formatDateTime(leave.approvalTime) }}</span>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <el-empty
        v-if="!loading && leaveList.length === 0"
        description="暂无请假申请"
        :image-size="160"
      />
    </div>

    <!-- 分页 -->
    <div v-if="pagination.total > 0" class="pagination-wrapper">
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :page-sizes="[10, 20, 50]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 审批对话框 -->
    <el-dialog
      v-model="approvalDialogVisible"
      :title="approvalForm.status === 'APPROVED' ? '批准请假' : '拒绝请假'"
      width="600px"
    >
      <div class="approval-info">
        <div class="info-item">
          <span class="label">申请人：</span>
          <span class="value">{{ currentLeave?.applicantName }}</span>
        </div>
        <div class="info-item">
          <span class="label">请假时间：</span>
          <span class="value">
            {{ formatDate(currentLeave?.startDate) }} 至 
            {{ formatDate(currentLeave?.endDate) }}
            （{{ currentLeave?.days }} 天）
          </span>
        </div>
        <div class="info-item">
          <span class="label">请假原因：</span>
          <span class="value">{{ currentLeave?.reason }}</span>
        </div>
      </div>

      <el-form
        ref="approvalFormRef"
        :model="approvalForm"
        :rules="approvalRules"
        label-width="90px"
        style="margin-top: 20px"
      >
        <el-form-item label="审批意见" prop="comment">
          <el-input
            v-model="approvalForm.comment"
            type="textarea"
            :rows="4"
            placeholder="请输入审批意见"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button size="large" @click="approvalDialogVisible = false">取消</el-button>
          <el-button
            :type="approvalForm.status === 'APPROVED' ? 'success' : 'danger'"
            size="large"
            :loading="submitting"
            @click="handleSubmitApproval"
          >
            {{ submitting ? '提交中...' : '确认' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import {
  getAllLeaveRequests,
  approveLeaveRequest
} from '@/api/leaveRequest'

// 数据
const loading = ref(false)
const submitting = ref(false)
const leaveList = ref([])
const approvalDialogVisible = ref(false)
const currentLeave = ref(null)
const approvalFormRef = ref(null)

// 筛选条件
const filters = reactive({
  status: '',
  applicantType: '',
  keyword: ''
})

// 分页
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 审批表单
const approvalForm = reactive({
  status: '',
  comment: ''
})

// 审批表单验证规则
const approvalRules = {
  comment: [
    { required: true, message: '请输入审批意见', trigger: 'blur' },
    { min: 2, message: '审批意见至少2个字', trigger: 'blur' }
  ]
}

// 获取请假列表
const fetchLeaveRequests = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page - 1,
      size: pagination.size,
      ...Object.fromEntries(
        Object.entries(filters).filter(([_, value]) => value !== '')
      )
    }

    const res = await getAllLeaveRequests(params)
    leaveList.value = res.data.content || []
    pagination.total = res.data.totalElements || 0
  } catch (error) {
    console.error('获取请假列表失败:', error)
    ElMessage.error('获取请假列表失败')
  } finally {
    loading.value = false
  }
}

// 显示审批对话框
const handleApprove = (leave, status) => {
  currentLeave.value = leave
  approvalForm.status = status
  approvalForm.comment = ''
  approvalDialogVisible.value = true
  approvalFormRef.value?.clearValidate()
}

// 提交审批
const handleSubmitApproval = () => {
  approvalFormRef.value?.validate(async (valid) => {
    if (!valid) return

    submitting.value = true
    try {
      await approveLeaveRequest(currentLeave.value.id, approvalForm)
      ElMessage.success('审批成功')
      approvalDialogVisible.value = false
      fetchLeaveRequests()
    } catch (error) {
      console.error('审批失败:', error)
      ElMessage.error(error.message || '审批失败')
    } finally {
      submitting.value = false
    }
  })
}

// 分页事件
const handleSizeChange = (size) => {
  pagination.size = size
  pagination.page = 1
  fetchLeaveRequests()
}

const handleCurrentChange = (page) => {
  pagination.page = page
  fetchLeaveRequests()
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

// 格式化日期时间
const formatDateTime = (dateTimeStr) => {
  if (!dateTimeStr) return '-'
  const date = new Date(dateTimeStr)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}`
}

// 申请人类型样式
const getApplicantTypeStyle = (type) => {
  return type === 'STUDENT' ? 'primary' : 'success'
}

// 类型样式
const getTypeStyle = (type) => {
  const styleMap = {
    SICK: 'danger',
    PERSONAL: 'warning',
    OTHER: 'info'
  }
  return styleMap[type] || ''
}

// 状态样式
const getStatusStyle = (status) => {
  const styleMap = {
    PENDING: 'warning',
    APPROVED: 'success',
    REJECTED: 'danger',
    CANCELLED: 'info'
  }
  return styleMap[status] || ''
}

// 页面加载
onMounted(() => {
  fetchLeaveRequests()
})
</script>

<style scoped lang="scss">
.leave-requests-management {
  max-width: 1200px;
  margin: 0 auto;
  padding: var(--spacing-3xl) var(--spacing-xl);

  // 页面头部
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: var(--spacing-2xl);
  }

  .header-content {
    flex: 1;
  }

  .page-title {
    font-size: 40px;
    font-weight: 600;
    color: var(--text-primary);
    margin: 0 0 8px;
    letter-spacing: -0.03em;
  }

  .page-subtitle {
    font-size: 17px;
    color: var(--text-secondary);
    margin: 0;
    font-weight: 400;
  }

  // 筛选栏
  .filter-bar {
    display: flex;
    gap: 12px;
    margin-bottom: var(--spacing-lg);

    @media (max-width: 768px) {
      flex-wrap: wrap;
    }
  }

  .filter-item {
    min-width: 200px;

    @media (max-width: 768px) {
      flex: 1;
      min-width: 160px;
    }
  }

  // 请假列表
  .leave-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
    margin-bottom: var(--spacing-xl);
    min-height: 300px;
  }

  .leave-card {
    background: var(--bg-primary);
    border-radius: var(--radius-lg);
    padding: 24px;
    border: 1px solid var(--border-light);
    transition: all var(--transition-base);

    &:hover {
      box-shadow: var(--shadow-sm);
    }
  }

  .leave-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
  }

  .leave-meta {
    display: flex;
    align-items: center;
    gap: 8px;
    flex-wrap: wrap;
  }

  .meta-divider {
    color: var(--text-tertiary);
  }

  .leave-actions {
    display: flex;
    gap: 8px;
  }

  .leave-info {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  .info-row {
    display: flex;
    gap: 16px;

    .label {
      font-size: 14px;
      color: var(--text-tertiary);
      min-width: 70px;
      flex-shrink: 0;
    }

    .value {
      font-size: 15px;
      color: var(--text-primary);
      flex: 1;
    }

    .reason-text {
      line-height: 1.6;
    }
  }

  // 分页
  .pagination-wrapper {
    display: flex;
    justify-content: center;
    margin-top: var(--spacing-xl);
  }

  // 审批信息
  .approval-info {
    padding: 16px;
    background: var(--bg-secondary);
    border-radius: var(--radius-md);
    margin-bottom: 20px;

    .info-item {
      display: flex;
      gap: 12px;
      margin-bottom: 12px;

      &:last-child {
        margin-bottom: 0;
      }

      .label {
        font-size: 14px;
        color: var(--text-tertiary);
        min-width: 80px;
        flex-shrink: 0;
      }

      .value {
        font-size: 15px;
        color: var(--text-primary);
        flex: 1;
      }
    }
  }

  // 对话框
  .dialog-footer {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
  }

  :deep(.el-form-item__label) {
    font-weight: 500;
  }

  :deep(.el-form-item__content) {
    flex: 1;
  }

  :deep(.el-select) {
    width: 100%;
  }

  :deep(.el-textarea__inner) {
    font-family: var(--font-system);
  }
}
</style>

