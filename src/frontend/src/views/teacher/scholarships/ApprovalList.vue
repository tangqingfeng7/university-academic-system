<template>
  <div class="approval-list-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <h1 class="page-title">奖学金审批</h1>
      <el-button type="primary" :icon="Download" @click="handleExport">
        导出审批记录
      </el-button>
    </div>

    <!-- 统计卡片区域 -->
    <div class="stats-section">
      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :md="6">
          <div class="stat-card" @click="filterByStatus('PENDING')">
            <div class="stat-label">待我审批</div>
            <div class="stat-number">{{ statistics.pending }}</div>
          </div>
        </el-col>

        <el-col :xs="24" :sm="12" :md="6">
          <div class="stat-card" @click="filterByStatus('APPROVED')">
            <div class="stat-label">已通过</div>
            <div class="stat-number">{{ statistics.approved }}</div>
          </div>
        </el-col>

        <el-col :xs="24" :sm="12" :md="6">
          <div class="stat-card" @click="filterByStatus('REJECTED')">
            <div class="stat-label">已拒绝</div>
            <div class="stat-number">{{ statistics.rejected }}</div>
          </div>
        </el-col>

        <el-col :xs="24" :sm="12" :md="6">
          <div class="stat-card">
            <div class="stat-label">总申请数</div>
            <div class="stat-number">{{ statistics.total }}</div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 筛选和搜索区域 -->
    <el-card shadow="never" class="filter-card">
      <el-form :model="queryForm" label-width="80px">
        <el-row :gutter="16">
          <el-col :xs="24" :sm="12" :md="6">
            <el-form-item label="学年">
              <el-select 
                v-model="queryForm.academicYear" 
                placeholder="选择学年"
                clearable
                style="width: 100%"
              >
                <el-option
                  v-for="year in academicYears"
                  :key="year"
                  :label="year"
                  :value="year"
                />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :xs="24" :sm="12" :md="6">
            <el-form-item label="奖学金">
              <el-select 
                v-model="queryForm.scholarshipId" 
                placeholder="选择奖学金"
                clearable
                style="width: 100%"
              >
                <el-option
                  v-for="scholarship in scholarships"
                  :key="scholarship.id"
                  :label="scholarship.name"
                  :value="scholarship.id"
                />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :xs="24" :sm="12" :md="6">
            <el-form-item label="审批状态">
              <el-select 
                v-model="queryForm.status" 
                placeholder="选择状态"
                clearable
                style="width: 100%"
              >
                <el-option label="待审批" value="PENDING" />
                <el-option label="辅导员已通过" value="COUNSELOR_APPROVED" />
                <el-option label="院系已通过" value="DEPT_APPROVED" />
                <el-option label="已拒绝" value="REJECTED" />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :xs="24" :sm="12" :md="6">
            <el-form-item label="学生">
              <el-input
                v-model="queryForm.studentKeyword"
                placeholder="姓名或学号"
                clearable
                style="width: 100%"
              >
                <template #prefix>
                  <el-icon><Search /></el-icon>
                </template>
              </el-input>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row>
          <el-col :span="24" class="filter-actions">
            <el-button type="primary" :icon="Search" @click="handleQuery">
              查询
            </el-button>
            <el-button :icon="RefreshRight" @click="handleReset">
              重置
            </el-button>
            <el-button 
              v-if="selectedApplications.length > 0" 
              type="success"
              :icon="Checked"
              @click="handleBatchApprove"
            >
              批量通过 ({{ selectedApplications.length }})
            </el-button>
            <el-button 
              v-if="selectedApplications.length > 0" 
              type="danger"
              :icon="Close"
              @click="handleBatchReject"
            >
              批量拒绝 ({{ selectedApplications.length }})
            </el-button>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <!-- 申请列表 -->
    <el-card shadow="never" class="list-card">
      <!-- 列表视图切换 -->
      <div class="list-header">
        <div class="list-title">
          <span>申请列表</span>
          <el-tag v-if="pagination.total > 0" type="info" size="small">
            共 {{ pagination.total }} 条记录
          </el-tag>
        </div>
        <div class="view-switch">
          <el-radio-group v-model="viewMode" size="small">
            <el-radio-button value="table">
              <el-icon><Grid /></el-icon> 表格
            </el-radio-button>
            <el-radio-button value="card">
              <el-icon><List /></el-icon> 卡片
            </el-radio-button>
          </el-radio-group>
        </div>
      </div>

      <!-- 加载状态 -->
      <div v-if="loading" class="loading-wrapper">
        <el-skeleton :rows="8" animated />
      </div>

      <!-- 表格视图 -->
      <div v-else-if="viewMode === 'table' && applications.length > 0">
        <el-table 
          :data="applications" 
          stripe
          @selection-change="handleSelectionChange"
        >
          <el-table-column 
            type="selection" 
            width="55" 
            :selectable="canSelectRow"
          />

          <el-table-column label="学生信息" min-width="160" fixed>
            <template #default="{ row }">
              <div class="student-cell">
                <el-avatar 
                  :size="40" 
                  :src="row.student?.avatar"
                  class="student-avatar"
                >
                  {{ row.student?.name?.charAt(0) }}
                </el-avatar>
                <div class="student-info">
                  <div class="student-name">{{ row.student?.name }}</div>
                  <div class="student-meta">
                    <span>{{ row.student?.studentNo }}</span>
                    <el-divider direction="vertical" />
                    <span>{{ row.student?.majorName || '-' }}</span>
                  </div>
                </div>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="奖学金" min-width="180">
            <template #default="{ row }">
              <div class="scholarship-cell">
                <div class="scholarship-name">{{ row.scholarship?.name }}</div>
                <div class="scholarship-amount">
                  <el-tag type="warning" size="small">
                    ¥{{ row.scholarship?.amount }}
                  </el-tag>
                  <el-tag :type="getLevelType(row.scholarship?.level)" size="small">
                    {{ getLevelText(row.scholarship?.level) }}
                  </el-tag>
                </div>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="学年" prop="academicYear" width="110" align="center" />

          <el-table-column label="学业成绩" width="200" align="center">
            <template #default="{ row }">
              <div class="grade-cell">
                <div class="grade-item">
                  <span class="grade-label">GPA:</span>
                  <el-tag :type="getGpaType(row.gpa)" size="small">
                    {{ row.gpa?.toFixed(2) }}
                  </el-tag>
                </div>
                <div class="grade-item">
                  <span class="grade-label">学分:</span>
                  <span class="grade-value">{{ row.totalCredits }}</span>
                </div>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="综合得分" width="100" align="center">
            <template #default="{ row }">
              <div v-if="row.comprehensiveScore" class="score-badge">
                <el-progress 
                  type="circle" 
                  :percentage="row.comprehensiveScore" 
                  :width="50"
                  :stroke-width="5"
                />
              </div>
              <span v-else class="text-muted">未评定</span>
            </template>
          </el-table-column>

          <el-table-column label="审批状态" width="140" align="center">
            <template #default="{ row }">
              <el-tag 
                :type="getStatusType(row.status)" 
                effect="dark"
                class="status-tag"
              >
                <el-icon><component :is="getStatusIcon(row.status)" /></el-icon>
                {{ getStatusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column label="申请时间" width="160" align="center">
            <template #default="{ row }">
              <div class="time-cell">
                <div>{{ formatDate(row.submittedAt) }}</div>
                <div class="time-ago">{{ getTimeAgo(row.submittedAt) }}</div>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="操作" width="200" fixed="right" align="center">
            <template #default="{ row }">
              <div class="action-buttons">
                <el-button
                  type="primary"
                  size="small"
                  :icon="View"
                  @click="viewDetail(row)"
                >
                  查看
                </el-button>
                <el-button
                  v-if="canApprove(row)"
                  type="success"
                  size="small"
                  :icon="Checked"
                  @click="handleQuickApprove(row)"
                >
                  审批
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 卡片视图 -->
      <div v-else-if="viewMode === 'card' && applications.length > 0" class="card-view">
        <el-row :gutter="16">
          <el-col 
            v-for="app in applications" 
            :key="app.id"
            :xs="24" 
            :sm="12" 
            :lg="8"
            class="card-col"
          >
            <div 
              class="application-card"
              :class="{ 'can-approve': canApprove(app), 'selected': isSelected(app) }"
              @click="handleCardClick(app)"
            >
              <!-- 卡片头部 -->
              <div class="card-header">
                <div class="card-student">
                  <el-avatar :size="50" :src="app.student?.avatar">
                    {{ app.student?.name?.charAt(0) }}
                  </el-avatar>
                  <div class="card-student-info">
                    <div class="card-student-name">{{ app.student?.name }}</div>
                    <div class="card-student-no">{{ app.student?.studentNo }}</div>
                  </div>
                </div>
                <el-checkbox 
                  v-if="canApprove(app)"
                  :model-value="isSelected(app)"
                  @change="toggleSelection(app)"
                  @click.stop
                />
              </div>

              <!-- 奖学金信息 -->
              <div class="card-scholarship">
                <div class="card-scholarship-name">{{ app.scholarship?.name }}</div>
                <div class="card-scholarship-meta">
                  <el-tag type="warning" size="small">¥{{ app.scholarship?.amount }}</el-tag>
                  <el-tag :type="getLevelType(app.scholarship?.level)" size="small">
                    {{ getLevelText(app.scholarship?.level) }}
                  </el-tag>
                </div>
              </div>

              <!-- 成绩信息 -->
              <div class="card-grades">
                <div class="card-grade-item">
                  <span class="label">GPA</span>
                  <span class="value">{{ app.gpa?.toFixed(2) }}</span>
                </div>
                <div class="card-grade-item">
                  <span class="label">学分</span>
                  <span class="value">{{ app.totalCredits }}</span>
                </div>
                <div class="card-grade-item">
                  <span class="label">综合分</span>
                  <span class="value">{{ app.comprehensiveScore || '-' }}</span>
                </div>
              </div>

              <!-- 状态和时间 -->
              <div class="card-footer">
                <el-tag 
                  :type="getStatusType(app.status)" 
                  size="small"
                  effect="dark"
                >
                  {{ getStatusText(app.status) }}
                </el-tag>
                <span class="card-time">{{ getTimeAgo(app.submittedAt) }}</span>
              </div>

              <!-- 快速操作按钮 -->
              <div v-if="canApprove(app)" class="card-actions">
                <el-button 
                  type="success" 
                  size="small" 
                  :icon="Checked"
                  @click.stop="handleQuickApprove(app)"
                >
                  通过
                </el-button>
                <el-button 
                  type="danger" 
                  size="small" 
                  :icon="Close"
                  @click.stop="handleQuickReject(app)"
                >
                  拒绝
                </el-button>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>

      <!-- 空状态 -->
      <el-empty 
        v-else-if="!loading" 
        :description="getEmptyDescription()"
        :image-size="160"
      >
        <el-button type="primary" @click="handleReset">重置筛选</el-button>
      </el-empty>

      <!-- 分页 -->
      <div v-if="applications.length > 0" class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 30, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleQuery"
          @current-change="handleQuery"
        />
      </div>
    </el-card>

    <!-- 快速审批对话框 -->
    <el-dialog
      v-model="approvalDialog.visible"
      :title="approvalDialog.isApprove ? '审批通过' : '审批拒绝'"
      width="500px"
      @close="resetApprovalDialog"
    >
      <el-form :model="approvalDialog.form" label-width="80px">
        <el-form-item label="审批意见">
          <el-input
            v-model="approvalDialog.form.comment"
            type="textarea"
            :rows="4"
            :placeholder="approvalDialog.isApprove ? '请填写审批意见（选填）' : '请说明拒绝理由（必填）'"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="approvalDialog.visible = false">取消</el-button>
        <el-button 
          :type="approvalDialog.isApprove ? 'success' : 'danger'"
          :loading="approvalDialog.loading"
          @click="confirmApproval"
        >
          确认{{ approvalDialog.isApprove ? '通过' : '拒绝' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Checked,
  Clock,
  CircleCheck,
  CircleClose,
  Files,
  Search,
  RefreshRight,
  View,
  Download,
  Close,
  Grid,
  List,
  ArrowRight
} from '@element-plus/icons-vue'
import { getPendingApplications, approveApplication, getApproverScholarships } from '@/api/scholarship'
import { formatDate } from '@/utils/date'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

// 数据
const loading = ref(true)
const applications = ref([])
const scholarships = ref([])
const academicYears = ref([])
const selectedApplications = ref([])
const viewMode = ref('table')

// 查询表单
const queryForm = reactive({
  academicYear: '',
  scholarshipId: null,
  status: '',
  studentKeyword: ''
})

// 分页
const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

// 统计数据
const statistics = reactive({
  pending: 0,
  approved: 0,
  rejected: 0,
  total: 0
})

// 审批对话框
const approvalDialog = reactive({
  visible: false,
  loading: false,
  isApprove: true,
  application: null,
  form: {
    comment: ''
  }
})

// 计算统计数据
const updateStatistics = () => {
  statistics.pending = applications.value.filter(app => 
    canApprove(app)
  ).length
  statistics.approved = applications.value.filter(app => 
    app.status === 'DEPT_APPROVED' || app.status === 'COUNSELOR_APPROVED'
  ).length
  statistics.rejected = applications.value.filter(app => 
    app.status === 'REJECTED'
  ).length
  statistics.total = applications.value.length
}

// 获取申请列表
const fetchApplications = async () => {
  try {
    loading.value = true
    const params = {
      page: pagination.page - 1,
      size: pagination.size,
      ...queryForm
    }
    const res = await getPendingApplications(params)
    applications.value = res.data.content || []
    pagination.total = res.data.totalElements || 0
    updateStatistics()
  } catch (error) {
    console.error('获取申请列表失败:', error)
    ElMessage.error('获取申请列表失败')
  } finally {
    loading.value = false
  }
}

// 获取奖学金列表
const fetchScholarships = async () => {
  try {
    const res = await getApproverScholarships({ page: 0, size: 100 })
    scholarships.value = res.data.content || res.data || []
    
    // 生成学年列表
    const currentYear = new Date().getFullYear()
    const years = []
    for (let i = 0; i < 5; i++) {
      const year = currentYear - i
      years.push(`${year}-${year + 1}`)
    }
    academicYears.value = years
  } catch (error) {
    console.error('获取奖学金列表失败:', error)
  }
}

// 查询
const handleQuery = () => {
  pagination.page = 1
  fetchApplications()
}

// 重置
const handleReset = () => {
  Object.assign(queryForm, {
    academicYear: '',
    scholarshipId: null,
    status: '',
    studentKeyword: ''
  })
  handleQuery()
}

// 按状态筛选
const filterByStatus = (status) => {
  queryForm.status = queryForm.status === status ? '' : status
  handleQuery()
}

// 判断是否可以审批
const canApprove = (application) => {
  const role = userStore.userInfo?.role
  
  if (role === 'TEACHER') {
    return application.status === 'PENDING' || application.status === 'COUNSELOR_APPROVED'
  }
  
  if (role === 'COUNSELOR' && application.status === 'PENDING') {
    return true
  }
  
  if (role === 'DEPARTMENT_ADMIN' && application.status === 'COUNSELOR_APPROVED') {
    return true
  }
  
  return false
}

// 判断行是否可选
const canSelectRow = (row) => {
  return canApprove(row)
}

// 选择变更
const handleSelectionChange = (selection) => {
  selectedApplications.value = selection
}

// 判断是否选中
const isSelected = (app) => {
  return selectedApplications.value.some(item => item.id === app.id)
}

// 切换选中
const toggleSelection = (app) => {
  const index = selectedApplications.value.findIndex(item => item.id === app.id)
  if (index > -1) {
    selectedApplications.value.splice(index, 1)
  } else {
    selectedApplications.value.push(app)
  }
}

// 卡片点击
const handleCardClick = (app) => {
  viewDetail(app)
}

// 查看详情
const viewDetail = (application) => {
  router.push({
    name: 'TeacherScholarshipApproval',
    params: { id: application.id }
  })
}

// 快速通过
const handleQuickApprove = (application) => {
  approvalDialog.visible = true
  approvalDialog.isApprove = true
  approvalDialog.application = application
  approvalDialog.form.comment = ''
}

// 快速拒绝
const handleQuickReject = (application) => {
  approvalDialog.visible = true
  approvalDialog.isApprove = false
  approvalDialog.application = application
  approvalDialog.form.comment = ''
}

// 确认审批
const confirmApproval = async () => {
  if (!approvalDialog.isApprove && !approvalDialog.form.comment) {
    ElMessage.warning('请填写拒绝理由')
    return
  }

  try {
    approvalDialog.loading = true
    await approveApplication(approvalDialog.application.id, {
      action: approvalDialog.isApprove ? 'APPROVE' : 'REJECT',
      comment: approvalDialog.form.comment
    })
    
    ElMessage.success(`${approvalDialog.isApprove ? '通过' : '拒绝'}成功`)
    approvalDialog.visible = false
    fetchApplications()
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  } finally {
    approvalDialog.loading = false
  }
}

// 重置审批对话框
const resetApprovalDialog = () => {
  approvalDialog.form.comment = ''
  approvalDialog.application = null
}

// 批量通过
const handleBatchApprove = async () => {
  try {
    await ElMessageBox.confirm(
      `确认通过选中的 ${selectedApplications.value.length} 个申请？`,
      '批量审批',
      {
        type: 'warning',
        confirmButtonText: '确认通过',
        cancelButtonText: '取消'
      }
    )

    const promises = selectedApplications.value.map(app =>
      approveApplication(app.id, { action: 'APPROVE', comment: '批量审批通过' })
    )
    
    await Promise.all(promises)
    ElMessage.success('批量审批成功')
    selectedApplications.value = []
    fetchApplications()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量审批失败')
    }
  }
}

// 批量拒绝
const handleBatchReject = async () => {
  try {
    const { value } = await ElMessageBox.prompt(
      `确认拒绝选中的 ${selectedApplications.value.length} 个申请？请说明理由：`,
      '批量拒绝',
      {
        type: 'warning',
        confirmButtonText: '确认拒绝',
        cancelButtonText: '取消',
        inputPattern: /.+/,
        inputErrorMessage: '请输入拒绝理由'
      }
    )

    const promises = selectedApplications.value.map(app =>
      approveApplication(app.id, { action: 'REJECT', comment: value })
    )
    
    await Promise.all(promises)
    ElMessage.success('批量拒绝成功')
    selectedApplications.value = []
    fetchApplications()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量拒绝失败')
    }
  }
}

// 导出
const handleExport = () => {
  ElMessage.info('导出功能开发中...')
}

// 获取状态类型
const getStatusType = (status) => {
  const types = {
    PENDING: 'warning',
    COUNSELOR_APPROVED: 'primary',
    DEPT_APPROVED: 'success',
    REJECTED: 'danger'
  }
  return types[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const texts = {
    PENDING: '待审核',
    COUNSELOR_APPROVED: '辅导员已通过',
    DEPT_APPROVED: '已通过',
    REJECTED: '已拒绝'
  }
  return texts[status] || status
}

// 获取状态图标
const getStatusIcon = (status) => {
  const icons = {
    PENDING: Clock,
    COUNSELOR_APPROVED: CircleCheck,
    DEPT_APPROVED: CircleCheck,
    REJECTED: CircleClose
  }
  return icons[status] || Clock
}

// 获取等级类型
const getLevelType = (level) => {
  const types = {
    NATIONAL: 'danger',
    PROVINCIAL: 'warning',
    UNIVERSITY: 'primary',
    DEPARTMENT: 'info'
  }
  return types[level] || 'info'
}

// 获取等级文本
const getLevelText = (level) => {
  const texts = {
    NATIONAL: '国家级',
    PROVINCIAL: '省级',
    UNIVERSITY: '校级',
    DEPARTMENT: '院级'
  }
  return texts[level] || level
}

// 获取GPA类型
const getGpaType = (gpa) => {
  if (gpa >= 3.7) return 'success'
  if (gpa >= 3.0) return 'primary'
  if (gpa >= 2.5) return 'warning'
  return 'danger'
}

// 获取时间差
const getTimeAgo = (date) => {
  if (!date) return ''
  const now = new Date()
  const past = new Date(date)
  const diff = now - past
  
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)
  
  if (days > 0) return `${days}天前`
  if (hours > 0) return `${hours}小时前`
  if (minutes > 0) return `${minutes}分钟前`
  return '刚刚'
}

// 获取空状态描述
const getEmptyDescription = () => {
  if (queryForm.status || queryForm.scholarshipId || queryForm.academicYear || queryForm.studentKeyword) {
    return '没有符合条件的申请'
  }
  return '暂无待审批的申请'
}

onMounted(() => {
  fetchScholarships()
  fetchApplications()
})
</script>

<style scoped>
.approval-list-container {
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}

/* 页面头部 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px 0;
  margin-bottom: 20px;
}

.page-title {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #1a1a1a;
}

/* 统计卡片 */
.stats-section {
  margin-bottom: 20px;
}

.stat-card {
  background: white;
  border-radius: 8px;
  padding: 28px 24px;
  cursor: pointer;
  transition: all 0.2s ease;
  border: 1px solid #f0f0f0;
  height: 100%;
}

.stat-card:hover {
  border-color: #409eff;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.08);
}

.stat-label {
  font-size: 13px;
  color: #8c8c8c;
  margin-bottom: 12px;
  font-weight: 400;
  letter-spacing: 0.3px;
}

.stat-number {
  font-size: 36px;
  font-weight: 600;
  color: #262626;
  line-height: 1;
  font-variant-numeric: tabular-nums;
}

/* 筛选卡片 */
.filter-card {
  margin-bottom: 20px;
  border-radius: 12px;
}

.filter-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-start;
}

/* 列表卡片 */
.list-card {
  border-radius: 12px;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
}

.list-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

/* 表格视图 */
.student-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.student-avatar {
  flex-shrink: 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.student-info {
  flex: 1;
  min-width: 0;
}

.student-name {
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.student-meta {
  font-size: 12px;
  color: #909399;
  display: flex;
  align-items: center;
  gap: 4px;
}

.scholarship-cell {
  line-height: 1.6;
}

.scholarship-name {
  font-weight: 500;
  color: #303133;
  margin-bottom: 6px;
}

.scholarship-amount {
  display: flex;
  gap: 6px;
}

.grade-cell {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.grade-item {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.grade-label {
  font-size: 12px;
  color: #909399;
}

.grade-value {
  font-weight: 600;
  color: #303133;
}

.score-badge {
  display: flex;
  justify-content: center;
}

.status-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.time-cell {
  line-height: 1.6;
}

.time-ago {
  font-size: 12px;
  color: #909399;
}

.action-buttons {
  display: flex;
  gap: 8px;
  justify-content: center;
}

/* 卡片视图 */
.card-view {
  margin-top: 20px;
}

.card-col {
  margin-bottom: 16px;
}

.application-card {
  background: white;
  border: 2px solid #ebeef5;
  border-radius: 12px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  height: 100%;
}

.application-card:hover {
  border-color: #667eea;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.2);
  transform: translateY(-2px);
}

.application-card.can-approve:hover {
  border-color: #67c23a;
}

.application-card.selected {
  border-color: #409eff;
  background: #f0f9ff;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.card-student {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.card-student-info {
  flex: 1;
}

.card-student-name {
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.card-student-no {
  font-size: 12px;
  color: #909399;
}

.card-scholarship {
  margin-bottom: 16px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
}

.card-scholarship-name {
  font-weight: 500;
  color: #303133;
  margin-bottom: 8px;
}

.card-scholarship-meta {
  display: flex;
  gap: 8px;
}

.card-grades {
  display: flex;
  justify-content: space-around;
  margin-bottom: 16px;
  padding: 12px;
  background: #fafafa;
  border-radius: 8px;
}

.card-grade-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.card-grade-item .label {
  font-size: 12px;
  color: #909399;
}

.card-grade-item .value {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #ebeef5;
}

.card-time {
  font-size: 12px;
  color: #909399;
}

.card-actions {
  display: flex;
  gap: 8px;
  margin-top: 12px;
}

.card-actions .el-button {
  flex: 1;
}

/* 分页 */
.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

/* 加载状态 */
.loading-wrapper {
  padding: 20px;
}

/* 响应式 */
@media (max-width: 768px) {
  .approval-list-container {
    padding: 10px;
  }

  .page-header {
    padding: 20px;
  }

  .header-content {
    flex-direction: column;
    gap: 15px;
    align-items: flex-start;
  }

  .header-left {
    gap: 15px;
  }

  .header-icon {
    font-size: 36px;
  }

  .page-title {
    font-size: 22px;
  }

  .stats-section {
    grid-template-columns: repeat(2, 1fr);
  }

  .list-header {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }

  .filter-actions {
    flex-wrap: wrap;
  }
}

.text-muted {
  color: #909399;
}
</style>
