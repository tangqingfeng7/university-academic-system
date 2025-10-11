<template>
  <div class="application-management">
    <el-card shadow="never" class="header-card">
      <div class="header-content">
        <div>
          <h2 class="page-title">
            <el-icon><Document /></el-icon>
            申请管理
          </h2>
          <p class="page-subtitle">查询和管理奖学金申请</p>
        </div>
        <el-button type="success" :icon="MagicStick" @click="handleEvaluate">
          自动评定
        </el-button>
      </div>
    </el-card>

    <!-- 筛选栏 -->
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" :model="queryForm" @submit.prevent="handleQuery">
        <el-form-item label="学年">
          <el-select v-model="queryForm.academicYear" placeholder="全部学年" clearable>
            <el-option
              v-for="year in academicYears"
              :key="year"
              :label="year"
              :value="year"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="奖学金">
          <el-select v-model="queryForm.scholarshipId" placeholder="全部奖学金" clearable>
            <el-option
              v-for="scholarship in scholarships"
              :key="scholarship.id"
              :label="scholarship.name"
              :value="scholarship.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="全部状态" clearable>
            <el-option label="待审核" value="PENDING" />
            <el-option label="辅导员已通过" value="COUNSELOR_APPROVED" />
            <el-option label="已通过" value="DEPT_APPROVED" />
            <el-option label="已拒绝" value="REJECTED" />
          </el-select>
        </el-form-item>

        <el-form-item label="学生姓名">
          <el-input
            v-model="queryForm.studentName"
            placeholder="请输入学生姓名"
            clearable
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleQuery">
            查询
          </el-button>
          <el-button :icon="RefreshRight" @click="handleReset">
            重置
          </el-button>
          <el-button :icon="Download" @click="handleExport">
            导出
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="5" animated />
    </div>

    <!-- 申请列表 -->
    <el-card v-else shadow="never">
      <el-table :data="applications" stripe>
        <el-table-column label="学生信息" min-width="150">
          <template #default="{ row }">
            <div class="student-info">
              <div class="student-name">{{ row.student?.name }}</div>
              <div class="student-no">{{ row.student?.studentNo }}</div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="专业" prop="student.major.name" min-width="120" />

        <el-table-column label="奖学金" prop="scholarship.name" min-width="150" />

        <el-table-column label="学年" prop="academicYear" width="120" />

        <el-table-column label="GPA" prop="gpa" width="80" align="center" />

        <el-table-column label="学分" prop="totalCredits" width="80" align="center" />

        <el-table-column label="综合得分" width="100" align="center">
          <template #default="{ row }">
            <span v-if="row.comprehensiveScore" class="score">
              {{ row.comprehensiveScore }}
            </span>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="申请时间" width="160">
          <template #default="{ row }">
            {{ formatDate(row.submittedAt) }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              link
              :icon="View"
              @click="handleView(row)"
            >
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleQuery"
          @current-change="handleQuery"
        />
      </div>
    </el-card>

    <!-- 申请详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="申请详情"
      width="800px"
      :close-on-click-modal="false"
    >
      <div v-if="currentApplication" class="application-detail">
        <!-- 学生信息 -->
        <el-card shadow="never" class="section-card">
          <template #header>
            <div class="card-header">
              <span>学生信息</span>
            </div>
          </template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="学号">
              {{ currentApplication.student?.studentNo }}
            </el-descriptions-item>
            <el-descriptions-item label="姓名">
              {{ currentApplication.student?.name }}
            </el-descriptions-item>
            <el-descriptions-item label="院系">
              {{ currentApplication.student?.departmentName }}
            </el-descriptions-item>
            <el-descriptions-item label="专业">
              {{ currentApplication.student?.majorName }}
            </el-descriptions-item>
            <el-descriptions-item label="年级">
              {{ currentApplication.student?.grade }}级
            </el-descriptions-item>
            <el-descriptions-item label="班级">
              {{ currentApplication.student?.className }}
            </el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- 奖学金信息 -->
        <el-card shadow="never" class="section-card">
          <template #header>
            <div class="card-header">
              <span>奖学金信息</span>
            </div>
          </template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="奖学金名称">
              {{ currentApplication.scholarship?.name }}
            </el-descriptions-item>
            <el-descriptions-item label="奖学金等级">
              {{ currentApplication.scholarship?.levelDescription }}
            </el-descriptions-item>
            <el-descriptions-item label="奖学金金额">
              ¥{{ currentApplication.scholarship?.amount }}
            </el-descriptions-item>
            <el-descriptions-item label="学年">
              {{ currentApplication.academicYear }}
            </el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- 学业信息 -->
        <el-card shadow="never" class="section-card">
          <template #header>
            <div class="card-header">
              <span>学业信息</span>
            </div>
          </template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="GPA">
              <span class="score-highlight">{{ currentApplication.gpa }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="总学分">
              {{ currentApplication.totalCredits }}
            </el-descriptions-item>
            <el-descriptions-item label="综合得分" :span="2">
              <span class="score-highlight">{{ currentApplication.comprehensiveScore }}</span>
            </el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- 申请材料 -->
        <el-card shadow="never" class="section-card">
          <template #header>
            <div class="card-header">
              <span>申请材料</span>
            </div>
          </template>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="个人陈述">
              <div class="statement-content">
                {{ currentApplication.personalStatement || '无' }}
              </div>
            </el-descriptions-item>
            <el-descriptions-item label="附件材料">
              <a 
                v-if="currentApplication.attachmentUrl" 
                :href="currentApplication.attachmentUrl" 
                target="_blank"
                class="attachment-link"
              >
                <el-icon><Document /></el-icon>
                查看附件
              </a>
              <span v-else class="text-muted">无附件</span>
            </el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- 审批信息 -->
        <el-card shadow="never" class="section-card">
          <template #header>
            <div class="card-header">
              <span>审批信息</span>
            </div>
          </template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="申请状态">
              <el-tag :type="getStatusType(currentApplication.status)">
                {{ currentApplication.statusDescription }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="提交时间">
              {{ formatDate(currentApplication.submittedAt) }}
            </el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- 审批操作 -->
        <el-card 
          v-if="canApprove(currentApplication)" 
          shadow="never" 
          class="section-card approval-actions"
        >
          <template #header>
            <div class="card-header">
              <span>审批操作</span>
            </div>
          </template>
          <el-form label-width="100px">
            <el-form-item label="审批意见">
              <el-input
                v-model="approvalComment"
                type="textarea"
                :rows="4"
                placeholder="请输入审批意见（选填）"
              />
            </el-form-item>
            <el-form-item>
              <el-button
                type="success"
                :loading="approving"
                @click="handleApprove('APPROVE')"
              >
                批准
              </el-button>
              <el-button
                type="danger"
                :loading="approving"
                @click="handleApprove('REJECT')"
              >
                拒绝
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </div>

      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 自动评定对话框 -->
    <el-dialog
      v-model="evaluateDialogVisible"
      title="自动评定"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="evaluateFormRef"
        :model="evaluateForm"
        :rules="evaluateRules"
        label-width="120px"
      >
        <el-form-item label="奖学金" prop="scholarshipId" required>
          <el-select
            v-model="evaluateForm.scholarshipId"
            placeholder="请选择奖学金"
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

        <el-form-item label="学年" prop="academicYear" required>
          <el-select
            v-model="evaluateForm.academicYear"
            placeholder="请选择学年"
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

        <el-alert
          title="自动评定说明"
          type="info"
          :closable="false"
          style="margin-top: 10px;"
        >
          <p>系统将根据以下规则自动计算综合得分：</p>
          <ul>
            <li>学业成绩占70%（GPA * 25）</li>
            <li>综合素质占30%（获奖、社会实践等）</li>
          </ul>
          <p>评定完成后，将按得分排序生成推荐名单。</p>
        </el-alert>
      </el-form>

      <template #footer>
        <el-button @click="evaluateDialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          :loading="evaluating"
          @click="handleEvaluateSubmit"
        >
          开始评定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Document,
  MagicStick,
  Search,
  RefreshRight,
  Download,
  View
} from '@element-plus/icons-vue'
import {
  getApplications,
  getScholarships,
  autoEvaluate,
  departmentApprove
} from '@/api/scholarship'
import { formatDate } from '@/utils/date'

const router = useRouter()

const loading = ref(true)
const evaluating = ref(false)
const approving = ref(false)
const applications = ref([])
const scholarships = ref([])
const academicYears = ref([])
const evaluateDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const currentApplication = ref(null)
const approvalComment = ref('')
const evaluateFormRef = ref()

const queryForm = reactive({
  academicYear: '',
  scholarshipId: null,
  status: '',
  studentName: ''
})

const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

const evaluateForm = reactive({
  scholarshipId: null,
  academicYear: ''
})

const evaluateRules = {
  scholarshipId: [
    { required: true, message: '请选择奖学金', trigger: 'change' }
  ],
  academicYear: [
    { required: true, message: '请选择学年', trigger: 'change' }
  ]
}

// 获取申请列表
const fetchApplications = async () => {
  try {
    loading.value = true
    // 过滤掉空值参数
    const filteredQuery = Object.entries(queryForm).reduce((acc, [key, value]) => {
      if (value !== '' && value !== null && value !== undefined) {
        acc[key] = value
      }
      return acc
    }, {})
    
    const params = {
      page: pagination.page - 1,
      size: pagination.size,
      ...filteredQuery
    }
    const res = await getApplications(params)
    applications.value = res.data.content || []
    pagination.total = res.data.totalElements || 0
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
    const res = await getScholarships({ page: 0, size: 100 })
    scholarships.value = res.data.content || []
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
  queryForm.academicYear = ''
  queryForm.scholarshipId = null
  queryForm.status = ''
  queryForm.studentName = ''
  handleQuery()
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

// 查看详情
const handleView = (application) => {
  currentApplication.value = application
  approvalComment.value = ''
  detailDialogVisible.value = true
}

// 判断是否可以审批
const canApprove = (application) => {
  if (!application) return false
  // 管理员可以审批"辅导员已批准"状态的申请
  return application.status === 'COUNSELOR_APPROVED'
}

// 处理审批
const handleApprove = async (action) => {
  if (!currentApplication.value) return
  
  try {
    approving.value = true
    
    const request = {
      action: action,
      comment: approvalComment.value || undefined
    }
    
    await departmentApprove(currentApplication.value.id, request)
    
    ElMessage.success(action === 'APPROVE' ? '审批通过' : '已拒绝申请')
    
    // 关闭对话框
    detailDialogVisible.value = false
    
    // 刷新列表
    await fetchApplications()
  } catch (error) {
    console.error('审批失败:', error)
    ElMessage.error(error.response?.data?.message || '审批失败，请稍后重试')
  } finally {
    approving.value = false
  }
}

// 自动评定
const handleEvaluate = () => {
  evaluateForm.scholarshipId = null
  evaluateForm.academicYear = academicYears.value[0] || ''
  evaluateDialogVisible.value = true
}

// 提交自动评定
const handleEvaluateSubmit = async () => {
  try {
    await evaluateFormRef.value.validate()
    evaluating.value = true

    await autoEvaluate(evaluateForm)
    ElMessage.success('自动评定完成！')
    evaluateDialogVisible.value = false
    fetchApplications()
  } catch (error) {
    console.error('自动评定失败:', error)
    ElMessage.error(error.message || '自动评定失败')
  } finally {
    evaluating.value = false
  }
}

onMounted(() => {
  // 生成学年列表
  const currentYear = new Date().getFullYear()
  const years = []
  for (let i = 0; i < 5; i++) {
    const year = currentYear - i
    years.push(`${year}-${year + 1}`)
  }
  academicYears.value = years

  fetchScholarships()
  fetchApplications()
})
</script>

<style scoped>
.application-management {
  padding: 20px;
}

.header-card {
  margin-bottom: 20px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 0 0 10px 0;
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.page-subtitle {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.filter-card {
  margin-bottom: 20px;
}

.filter-card :deep(.el-form) {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
}

.filter-card :deep(.el-form-item) {
  margin-bottom: 0;
}

.filter-card :deep(.el-select) {
  width: 200px;
}

.filter-card :deep(.el-input) {
  width: 200px;
}

.loading-container {
  padding: 20px;
}

.student-info {
  line-height: 1.6;
}

.student-name {
  font-weight: 500;
  color: #303133;
}

.student-no {
  font-size: 12px;
  color: #909399;
}

.score {
  font-weight: bold;
  color: #409eff;
}

.text-muted {
  color: #909399;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

/* 详情对话框样式 */
.application-detail {
  max-height: 600px;
  overflow-y: auto;
}

.section-card {
  margin-bottom: 20px;
  border: 1px solid #e4e7ed;
}

.section-card:last-child {
  margin-bottom: 0;
}

.card-header {
  font-weight: 600;
  font-size: 16px;
  color: #303133;
}

.score-highlight {
  font-weight: bold;
  font-size: 16px;
  color: #409eff;
}

.statement-content {
  max-height: 200px;
  overflow-y: auto;
  padding: 8px;
  background-color: #f5f7fa;
  border-radius: 4px;
  line-height: 1.8;
  white-space: pre-wrap;
  word-break: break-word;
}

.attachment-link {
  color: #409eff;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  gap: 5px;
}

.attachment-link:hover {
  color: #66b1ff;
}

.approval-actions {
  border: 2px solid #409eff;
  background-color: #ecf5ff;
}

.approval-actions .card-header {
  color: #409eff;
}
</style>

