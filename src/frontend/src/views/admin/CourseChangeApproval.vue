<template>
  <div class="course-change-approval-container">
    <el-card class="header-card">
      <div class="header-content">
        <h2 class="title">
          <el-icon class="title-icon"><DocumentChecked /></el-icon>
          调课审批
        </h2>
        <el-radio-group v-model="filterStatus" @change="handleFilterChange" class="filter-radio-group">
          <el-radio-button label="">全部</el-radio-button>
          <el-radio-button label="PENDING">待审批</el-radio-button>
          <el-radio-button label="APPROVED">已通过</el-radio-button>
          <el-radio-button label="REJECTED">已拒绝</el-radio-button>
        </el-radio-group>
      </div>
    </el-card>

    <el-card class="list-card">
      <el-table :data="requestList" v-loading="loading" style="width: 100%">
        <el-table-column prop="teacherName" label="申请教师" width="120" />
        <el-table-column prop="courseName" label="课程名称" width="180" />
        <el-table-column label="原时间安排" width="200">
          <template #default="{ row }">
            <div class="schedule-text">{{ formatSchedule(row.originalSchedule) }}</div>
          </template>
        </el-table-column>
        <el-table-column label="新时间安排" width="200">
          <template #default="{ row }">
            <div class="schedule-text">{{ formatSchedule(row.newSchedule) }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="调课原因" show-overflow-tooltip min-width="200" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="申请时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'PENDING'"
              type="primary"
              size="small"
              @click="handleApprove(row)">
              审批
            </el-button>
            <el-button
              v-else
              type="info"
              size="small"
              @click="handleViewDetail(row)">
              查看
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
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
    </el-card>

    <!-- 审批对话框 -->
    <el-dialog
      v-model="approvalDialogVisible"
      title="调课审批"
      width="700px"
      :close-on-click-modal="false">
      <div v-if="currentRequest" class="approval-detail">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="申请教师">{{ currentRequest.teacherName }}</el-descriptions-item>
          <el-descriptions-item label="课程名称">{{ currentRequest.courseName }}</el-descriptions-item>
          <el-descriptions-item label="原时间安排">
            <div class="schedule-text">{{ formatSchedule(currentRequest.originalSchedule) }}</div>
          </el-descriptions-item>
          <el-descriptions-item label="新时间安排">
            <div class="schedule-text">{{ formatSchedule(currentRequest.newSchedule) }}</div>
          </el-descriptions-item>
          <el-descriptions-item label="调课原因">
            <div style="white-space: pre-wrap;">{{ currentRequest.reason }}</div>
          </el-descriptions-item>
          <el-descriptions-item label="申请时间">
            {{ formatDateTime(currentRequest.createdAt) }}
          </el-descriptions-item>
        </el-descriptions>

        <el-divider />

        <el-form ref="approvalFormRef" :model="approvalForm" :rules="approvalRules" label-width="100px">
          <el-form-item label="审批结果" prop="approved">
            <el-radio-group v-model="approvalForm.approved">
              <el-radio :label="true">通过</el-radio>
              <el-radio :label="false">拒绝</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="审批意见" prop="comment">
            <el-input
              v-model="approvalForm.comment"
              type="textarea"
              :rows="4"
              placeholder="请输入审批意见"
            />
          </el-form-item>
        </el-form>
      </div>

      <template #footer>
        <el-button @click="approvalDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitApproval" :loading="submitting">提交审批</el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="调课申请详情"
      width="700px">
      <div v-if="currentRequest" class="detail-content">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="申请教师">{{ currentRequest.teacherName }}</el-descriptions-item>
          <el-descriptions-item label="课程名称">{{ currentRequest.courseName }}</el-descriptions-item>
          <el-descriptions-item label="原时间安排">
            <div class="schedule-text">{{ formatSchedule(currentRequest.originalSchedule) }}</div>
          </el-descriptions-item>
          <el-descriptions-item label="新时间安排">
            <div class="schedule-text">{{ formatSchedule(currentRequest.newSchedule) }}</div>
          </el-descriptions-item>
          <el-descriptions-item label="调课原因">
            <div style="white-space: pre-wrap;">{{ currentRequest.reason }}</div>
          </el-descriptions-item>
          <el-descriptions-item label="申请时间">
            {{ formatDateTime(currentRequest.createdAt) }}
          </el-descriptions-item>
          <el-descriptions-item label="审批状态">
            <el-tag :type="getStatusType(currentRequest.status)">
              {{ getStatusText(currentRequest.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="审批人">{{ currentRequest.approverName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="审批意见">
            <div style="white-space: pre-wrap;">{{ currentRequest.approvalComment || '-' }}</div>
          </el-descriptions-item>
          <el-descriptions-item label="审批时间">
            {{ currentRequest.approvalTime ? formatDateTime(currentRequest.approvalTime) : '-' }}
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { DocumentChecked } from '@element-plus/icons-vue'
import { getAllCourseChanges, approveCourseChange } from '@/api/courseChange'

const loading = ref(false)
const requestList = ref([])
const filterStatus = ref('')
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const approvalDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const approvalFormRef = ref(null)
const submitting = ref(false)
const currentRequest = ref(null)

const approvalForm = reactive({
  approved: true,
  comment: ''
})

const approvalRules = {
  approved: [{ required: true, message: '请选择审批结果', trigger: 'change' }],
  comment: [{ required: true, message: '请输入审批意见', trigger: 'blur' }]
}

// 加载调课申请列表
const loadRequestList = async () => {
  loading.value = true
  try {
    const res = await getAllCourseChanges({
      page: pagination.page - 1,
      size: pagination.size,
      status: filterStatus.value || undefined
    })
    requestList.value = res.data.content
    pagination.total = res.data.totalElements
  } catch (error) {
    console.error('加载调课申请列表失败:', error)
    ElMessage.error('加载调课申请列表失败')
  } finally {
    loading.value = false
  }
}

// 过滤状态变化
const handleFilterChange = () => {
  pagination.page = 1
  loadRequestList()
}

// 审批
const handleApprove = (row) => {
  currentRequest.value = row
  approvalForm.approved = true
  approvalForm.comment = ''
  approvalDialogVisible.value = true
}

// 查看详情
const handleViewDetail = (row) => {
  currentRequest.value = row
  detailDialogVisible.value = true
}

// 提交审批
const handleSubmitApproval = async () => {
  if (!approvalFormRef.value) return
  
  await approvalFormRef.value.validate(async (valid) => {
    if (!valid) return

    const action = approvalForm.approved ? '通过' : '拒绝'
    
    try {
      await ElMessageBox.confirm(
        `确定要${action}这个调课申请吗？`,
        '确认',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )
    } catch {
      return
    }

    submitting.value = true
    try {
      await approveCourseChange(currentRequest.value.id, {
        approved: approvalForm.approved,
        comment: approvalForm.comment
      })
      
      ElMessage.success(`调课申请${action}成功`)
      approvalDialogVisible.value = false
      loadRequestList()
    } catch (error) {
      console.error('审批失败:', error)
      ElMessage.error(error.message || '审批失败')
    } finally {
      submitting.value = false
    }
  })
}

// 格式化时间安排
const formatSchedule = (scheduleJson) => {
  if (!scheduleJson) return ''
  try {
    const schedule = JSON.parse(scheduleJson)
    const dayNames = ['', '周一', '周二', '周三', '周四', '周五', '周六', '周日']
    return schedule.map(s => {
      const day = dayNames[s.day] || ''
      const location = s.location || ''
      return `${day} 第${s.period}节 ${location}`
    }).join('; ')
  } catch (e) {
    return scheduleJson
  }
}

// 格式化日期时间
const formatDateTime = (datetime) => {
  if (!datetime) return ''
  return new Date(datetime).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 获取状态类型
const getStatusType = (status) => {
  const types = {
    'PENDING': 'warning',
    'APPROVED': 'success',
    'REJECTED': 'danger'
  }
  return types[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const texts = {
    'PENDING': '待审批',
    'APPROVED': '已通过',
    'REJECTED': '已拒绝'
  }
  return texts[status] || status
}

const handleSizeChange = () => {
  pagination.page = 1
  loadRequestList()
}

const handleCurrentChange = () => {
  loadRequestList()
}

onMounted(() => {
  loadRequestList()
})
</script>

<style scoped lang="scss">
.course-change-approval-container {
  padding: 24px;
}

.header-card {
  margin-bottom: 24px;
  
  :deep(.el-card__body) {
    padding: 24px;
  }
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title {
  display: flex;
  align-items: center;
  font-size: 22px;
  font-weight: bold;
  color: #303133;
  margin: 0;
}

.title-icon {
  margin-right: 12px;
  font-size: 26px;
  color: #409EFF;
}

.filter-radio-group {
  :deep(.el-radio-button__inner) {
    padding: 12px 24px;
    font-size: 15px;
    font-weight: 500;
  }
  
  :deep(.el-radio-button) {
    &:not(:last-child) {
      margin-right: 8px;
    }
  }
}

.list-card {
  margin-top: 24px;
  
  :deep(.el-card__body) {
    padding: 24px;
  }
  
  // 增加表格行高和单元格内边距
  :deep(.el-table) {
    th.el-table__cell {
      background-color: #f5f7fa;
      padding: 16px 0;
      font-weight: 600;
    }
    
    td.el-table__cell {
      padding: 18px 0;
    }
  }
}

.schedule-text {
  font-size: 14px;
  line-height: 1.8;
  padding: 4px 0;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid #EBEEF5;
}

.approval-detail {
  padding: 16px 0;
  
  :deep(.el-descriptions) {
    .el-descriptions__label {
      width: 120px;
      font-weight: 600;
    }
    
    .el-descriptions__cell {
      padding: 16px 12px;
    }
  }
  
  :deep(.el-divider) {
    margin: 24px 0;
  }
  
  :deep(.el-form) {
    margin-top: 8px;
    
    .el-form-item {
      margin-bottom: 24px;
    }
  }
}

.detail-content {
  padding: 16px 0;
  
  :deep(.el-descriptions) {
    .el-descriptions__label {
      width: 120px;
      font-weight: 600;
    }
    
    .el-descriptions__cell {
      padding: 16px 12px;
    }
  }
}
</style>

