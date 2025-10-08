<template>
  <div class="leave-requests-page">
    <!-- 页面头部 -->
    <div class="page-header animate-fade-in-down">
      <div class="header-content">
        <h1 class="page-title">请假申请</h1>
        <p class="page-subtitle">管理我的请假申请</p>
      </div>
      <el-button 
        type="primary" 
        :icon="Plus" 
        size="large"
        @click="showCreateDialog"
      >
        申请请假
      </el-button>
    </div>

    <!-- 请假列表 -->
    <div 
      v-loading="loading" 
      class="leave-list animate-fade-in-up" 
      style="animation-delay: 0.1s;"
    >
      <div
        v-for="(leave, index) in leaveList"
        :key="leave.id"
        class="leave-card"
        :style="{ 'animation-delay': `${0.15 + index * 0.05}s` }"
      >
        <div class="leave-header">
          <div class="leave-meta">
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
              type="danger"
              size="small"
              plain
              @click="handleCancel(leave.id)"
            >
              取消申请
            </el-button>
          </div>
        </div>

        <div class="leave-info">
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
        description="还没有请假申请"
        :image-size="160"
      >
        <el-button type="primary" :icon="Plus" @click="showCreateDialog">
          申请请假
        </el-button>
      </el-empty>
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

    <!-- 申请请假对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="申请请假"
      width="650px"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="90px"
      >
        <el-form-item label="请假类型" prop="leaveType">
          <el-select v-model="form.leaveType" placeholder="选择请假类型" size="large" style="width: 100%">
            <el-option label="病假" value="SICK" />
            <el-option label="事假" value="PERSONAL" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>

        <el-form-item label="开始日期" prop="startDate">
          <el-date-picker
            v-model="form.startDate"
            type="date"
            placeholder="选择开始日期"
            size="large"
            style="width: 100%"
            :disabled-date="disabledDate"
          />
        </el-form-item>

        <el-form-item label="结束日期" prop="endDate">
          <el-date-picker
            v-model="form.endDate"
            type="date"
            placeholder="选择结束日期"
            size="large"
            style="width: 100%"
            :disabled-date="disabledDate"
          />
        </el-form-item>

        <el-form-item label="请假原因" prop="reason">
          <el-input
            v-model="form.reason"
            type="textarea"
            :rows="6"
            placeholder="请输入请假原因"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button size="large" @click="dialogVisible = false">取消</el-button>
          <el-button
            type="primary"
            size="large"
            :loading="submitting"
            @click="handleSubmit"
          >
            {{ submitting ? '提交中...' : '提交申请' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import {
  getStudentLeaveRequests,
  createStudentLeaveRequest,
  cancelStudentLeaveRequest
} from '@/api/leaveRequest'

// 数据
const loading = ref(false)
const submitting = ref(false)
const leaveList = ref([])
const dialogVisible = ref(false)
const formRef = ref(null)

// 分页
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 表单数据
const form = reactive({
  leaveType: '',
  startDate: '',
  endDate: '',
  reason: ''
})

// 表单验证规则
const rules = {
  leaveType: [{ required: true, message: '请选择请假类型', trigger: 'change' }],
  startDate: [{ required: true, message: '请选择开始日期', trigger: 'change' }],
  endDate: [{ required: true, message: '请选择结束日期', trigger: 'change' }],
  reason: [
    { required: true, message: '请输入请假原因', trigger: 'blur' },
    { min: 10, message: '请假原因至少10个字', trigger: 'blur' }
  ]
}

// 获取请假列表
const fetchLeaveRequests = async () => {
  loading.value = true
  try {
    const res = await getStudentLeaveRequests({
      page: pagination.page - 1,
      size: pagination.size
    })
    leaveList.value = res.data.content || []
    pagination.total = res.data.totalElements || 0
  } catch (error) {
    console.error('获取请假列表失败:', error)
    ElMessage.error('获取请假列表失败')
  } finally {
    loading.value = false
  }
}

// 显示创建对话框
const showCreateDialog = () => {
  dialogVisible.value = true
  resetForm()
}

// 提交申请
const handleSubmit = () => {
  formRef.value?.validate(async (valid) => {
    if (!valid) return

    submitting.value = true
    try {
      await createStudentLeaveRequest(form)
      ElMessage.success('请假申请提交成功')
      dialogVisible.value = false
      fetchLeaveRequests()
    } catch (error) {
      console.error('提交请假申请失败:', error)
      ElMessage.error(error.message || '提交请假申请失败')
    } finally {
      submitting.value = false
    }
  })
}

// 取消申请
const handleCancel = async (id) => {
  try {
    await ElMessageBox.confirm(
      '确定要取消这条请假申请吗？',
      '确认取消',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await cancelStudentLeaveRequest(id)
    ElMessage.success('已取消请假申请')
    fetchLeaveRequests()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消请假申请失败:', error)
      ElMessage.error('取消请假申请失败')
    }
  }
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

// 禁用过去的日期
const disabledDate = (time) => {
  return time.getTime() < Date.now() - 8.64e7
}

// 重置表单
const resetForm = () => {
  form.leaveType = ''
  form.startDate = ''
  form.endDate = ''
  form.reason = ''
  formRef.value?.clearValidate()
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
.leave-requests-page {
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
  }

  .meta-divider {
    color: var(--text-tertiary);
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

  :deep(.el-date-picker) {
    width: 100%;
  }

  :deep(.el-textarea__inner) {
    font-family: var(--font-system);
  }
}
</style>

