<template>
  <div class="approval-detail-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <el-button :icon="ArrowLeft" @click="goBack">返回列表</el-button>
      <div class="header-title">
        <h1>奖学金申请审批</h1>
        <el-tag :type="getStatusType(application.status)" size="large">
          {{ getStatusText(application.status) }}
        </el-tag>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-wrapper">
      <el-skeleton :rows="10" animated />
    </div>

    <!-- 内容区域 -->
    <div v-else class="content-wrapper">
      <el-row :gutter="20">
        <!-- 左侧主要内容 -->
        <el-col :xs="24" :lg="16">
          <!-- 学生信息卡片 -->
          <el-card shadow="never" class="info-card">
            <template #header>
              <div class="card-header">
                <el-icon><User /></el-icon>
                <span>学生信息</span>
              </div>
            </template>

            <div class="student-profile">
              <el-avatar :size="80" :src="application.student?.avatar" class="profile-avatar">
                {{ application.student?.name?.charAt(0) }}
              </el-avatar>
              <div class="profile-info">
                <h2 class="profile-name">{{ application.student?.name }}</h2>
                <div class="profile-meta">
                  <el-tag>{{ application.student?.studentNo }}</el-tag>
                  <el-tag v-if="application.student?.majorName" type="info">
                    {{ application.student.majorName }}
                  </el-tag>
                  <el-tag v-if="application.student?.departmentName" type="warning">
                    {{ application.student.departmentName }}
                  </el-tag>
                </div>
              </div>
            </div>

            <el-divider />

            <el-descriptions :column="2" border>
              <el-descriptions-item label="年级">
                {{ getStudentGrade(application.student) }}
              </el-descriptions-item>
              <el-descriptions-item label="班级">
                {{ application.student?.className || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="联系电话">
                {{ application.student?.phone || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="电子邮箱">
                {{ application.student?.email || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="入学年份">
                {{ application.student?.enrollmentYear || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="学生状态">
                <el-tag :type="getStudentStatusType(application.student?.status)" size="small">
                  {{ getStudentStatusText(application.student?.status) }}
                </el-tag>
              </el-descriptions-item>
            </el-descriptions>
          </el-card>

          <!-- 奖学金信息卡片 -->
          <el-card shadow="never" class="info-card">
            <template #header>
              <div class="card-header">
                <el-icon><Medal /></el-icon>
                <span>申请奖学金</span>
              </div>
            </template>

            <div class="scholarship-info">
              <div class="scholarship-main">
                <h3 class="scholarship-name">{{ application.scholarship?.name }}</h3>
                <div class="scholarship-tags">
                  <el-tag :type="getLevelType(application.scholarship?.level)" size="large">
                    {{ getLevelText(application.scholarship?.level) }}
                  </el-tag>
                  <el-tag type="warning" size="large">
                    ¥{{ application.scholarship?.amount }}
                  </el-tag>
                </div>
              </div>

              <el-divider />

              <div class="scholarship-detail">
                <p><strong>申请学年：</strong>{{ application.academicYear }}</p>
                <p><strong>名额限制：</strong>{{ application.scholarship?.quota }}人</p>
                <p><strong>奖学金描述：</strong></p>
                <p v-if="application.scholarship?.description" class="description">
                  {{ application.scholarship.description }}
                </p>
                <p v-else class="description empty">暂无描述</p>
              </div>

              <el-divider />

              <div class="scholarship-requirements">
                <p><strong>申请要求：</strong></p>
                <ul v-if="application.scholarship?.minGpa || application.scholarship?.minCredits">
                  <li v-if="application.scholarship?.minGpa">
                    最低GPA要求：{{ application.scholarship.minGpa }}
                  </li>
                  <li v-if="application.scholarship?.minCredits">
                    最低学分要求：{{ application.scholarship.minCredits }}
                  </li>
                </ul>
                <p v-else class="empty">暂无特殊要求</p>
              </div>
            </div>
          </el-card>

          <!-- 学业成绩卡片 -->
          <el-card shadow="never" class="info-card">
            <template #header>
              <div class="card-header">
                <el-icon><TrendCharts /></el-icon>
                <span>学业成绩</span>
              </div>
            </template>

            <div class="grades-section">
              <el-row :gutter="20">
                <el-col :span="8">
                  <div class="grade-item">
                    <div class="grade-label">GPA</div>
                    <div class="grade-value" :class="getGpaClass(application.gpa)">
                      {{ application.gpa?.toFixed(2) }}
                    </div>
                    <el-progress 
                      :percentage="(application.gpa / 4) * 100" 
                      :color="getGpaColor(application.gpa)"
                      :show-text="false"
                    />
                  </div>
                </el-col>

                <el-col :span="8">
                  <div class="grade-item">
                    <div class="grade-label">总学分</div>
                    <div class="grade-value primary">
                      {{ application.totalCredits }}
                    </div>
                    <el-tag size="small">
                      {{ application.totalCredits >= (application.scholarship?.minCredits || 0) ? '符合要求' : '不符合' }}
                    </el-tag>
                  </div>
                </el-col>

                <el-col :span="8">
                  <div class="grade-item">
                    <div class="grade-label">综合得分</div>
                    <div class="grade-value success">
                      {{ application.comprehensiveScore || '-' }}
                    </div>
                    <el-rate 
                      v-if="application.comprehensiveScore"
                      :model-value="application.comprehensiveScore / 20"
                      disabled
                      show-score
                    />
                  </div>
                </el-col>
              </el-row>

              <el-divider />

              <div class="grade-comparison">
                <h4>成绩对比</h4>
                <el-row :gutter="16">
                  <el-col :span="12">
                    <div class="comparison-item">
                      <span class="label">最低GPA要求：</span>
                      <span class="value">{{ application.scholarship?.minGpa || '-' }}</span>
                    </div>
                  </el-col>
                  <el-col :span="12">
                    <div class="comparison-item">
                      <span class="label">学生GPA：</span>
                      <span class="value" :class="meetsGpaRequirement ? 'success' : 'danger'">
                        {{ application.gpa?.toFixed(2) }}
                        <el-icon v-if="meetsGpaRequirement"><CircleCheck /></el-icon>
                        <el-icon v-else><CircleClose /></el-icon>
                      </span>
                    </div>
                  </el-col>
                </el-row>

                <el-row :gutter="16">
                  <el-col :span="12">
                    <div class="comparison-item">
                      <span class="label">最低学分要求：</span>
                      <span class="value">{{ application.scholarship?.minCredits || '-' }}</span>
                    </div>
                  </el-col>
                  <el-col :span="12">
                    <div class="comparison-item">
                      <span class="label">学生学分：</span>
                      <span class="value" :class="meetsCreditsRequirement ? 'success' : 'danger'">
                        {{ application.totalCredits }}
                        <el-icon v-if="meetsCreditsRequirement"><CircleCheck /></el-icon>
                        <el-icon v-else><CircleClose /></el-icon>
                      </span>
                    </div>
                  </el-col>
                </el-row>
              </div>
            </div>
          </el-card>

          <!-- 个人陈述卡片 -->
          <el-card shadow="never" class="info-card">
            <template #header>
              <div class="card-header">
                <el-icon><Document /></el-icon>
                <span>个人陈述</span>
              </div>
            </template>

            <div class="personal-statement">
              <p v-if="application.personalStatement" class="statement-content">
                {{ application.personalStatement }}
              </p>
              <el-empty v-else description="未填写个人陈述" :image-size="100" />
            </div>
          </el-card>

          <!-- 证明材料卡片 -->
          <el-card v-if="application.attachmentUrl" shadow="never" class="info-card">
            <template #header>
              <div class="card-header">
                <el-icon><Paperclip /></el-icon>
                <span>证明材料</span>
              </div>
            </template>

            <div class="attachments">
              <el-link 
                :href="application.attachmentUrl" 
                target="_blank"
                type="primary"
                :icon="Download"
              >
                查看或下载证明材料
              </el-link>
            </div>
          </el-card>

          <!-- 审批历史卡片 -->
          <el-card v-if="approvalHistory.length > 0" shadow="never" class="info-card">
            <template #header>
              <div class="card-header">
                <el-icon><Clock /></el-icon>
                <span>审批历史</span>
              </div>
            </template>

            <el-timeline>
              <el-timeline-item
                v-for="(record, index) in approvalHistory"
                :key="index"
                :timestamp="formatDate(record.approvedAt)"
                placement="top"
                :type="getApprovalTimelineType(record.action)"
                :icon="getApprovalTimelineIcon(record.action)"
              >
                <el-card shadow="hover">
                  <div class="history-item">
                    <div class="history-header">
                      <span class="history-approver">{{ record.approver?.name }}</span>
                      <el-tag :type="getApprovalActionType(record.action)" size="small">
                        {{ getApprovalActionText(record.action) }}
                      </el-tag>
                    </div>
                    <div class="history-level">
                      审批级别：{{ getApprovalLevelText(record.approvalLevel) }}
                    </div>
                    <div v-if="record.comment" class="history-comment">
                      <strong>审批意见：</strong>
                      <p>{{ record.comment }}</p>
                    </div>
                  </div>
                </el-card>
              </el-timeline-item>
            </el-timeline>
          </el-card>
        </el-col>

        <!-- 右侧操作栏 -->
        <el-col :xs="24" :lg="8">
          <!-- 快速信息卡片 -->
          <el-card shadow="never" class="info-card sticky-card">
            <template #header>
              <div class="card-header">
                <el-icon><InfoFilled /></el-icon>
                <span>申请信息</span>
              </div>
            </template>

            <div class="quick-info">
              <div class="info-row">
                <span class="info-label">申请ID：</span>
                <span class="info-value">{{ application.id }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">申请时间：</span>
                <span class="info-value">{{ formatDate(application.submittedAt) }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">当前状态：</span>
                <el-tag :type="getStatusType(application.status)">
                  {{ getStatusText(application.status) }}
                </el-tag>
              </div>
              <div class="info-row">
                <span class="info-label">符合要求：</span>
                <el-tag :type="meetsAllRequirements ? 'success' : 'warning'">
                  {{ meetsAllRequirements ? '是' : '否' }}
                </el-tag>
              </div>
            </div>

            <el-divider />

            <!-- 审批操作 -->
            <div v-if="canApprove" class="approval-actions">
              <h4 class="action-title">审批操作</h4>
              
              <el-form :model="approvalForm" label-position="top">
                <el-form-item label="审批意见">
                  <el-input
                    v-model="approvalForm.comment"
                    type="textarea"
                    :rows="3"
                    placeholder="请填写审批意见..."
                    maxlength="500"
                    show-word-limit
                  />
                </el-form-item>

                <el-space direction="vertical" :size="10" style="width: 100%">
                  <el-button
                    type="success"
                    size="large"
                    :icon="Checked"
                    :loading="approving"
                    @click="handleApprove('APPROVE')"
                    style="width: 100%"
                  >
                    审批通过
                  </el-button>

                  <el-button
                    type="danger"
                    size="large"
                    :icon="Close"
                    :loading="approving"
                    @click="handleApprove('REJECT')"
                    style="width: 100%"
                  >
                    审批拒绝
                  </el-button>

                  <el-button
                    type="warning"
                    size="large"
                    :icon="RefreshRight"
                    :loading="approving"
                    @click="handleApprove('RETURN')"
                    style="width: 100%"
                  >
                    退回修改
                  </el-button>
                </el-space>
              </el-form>
            </div>

            <!-- 查看模式提示 -->
            <div v-else class="view-mode-tip">
              <el-alert
                :title="getViewModeTipTitle()"
                type="info"
                :closable="false"
              />
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowLeft,
  User,
  Medal,
  TrendCharts,
  Document,
  Paperclip,
  Clock,
  InfoFilled,
  Checked,
  Close,
  RefreshRight,
  Download,
  Link,
  CircleCheck,
  CircleClose
} from '@element-plus/icons-vue'
import { 
  getApproverApplicationDetail, 
  approveApplication, 
  getApprovalHistory 
} from '@/api/scholarship'
import { formatDate } from '@/utils/date'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(true)
const approving = ref(false)
const application = ref({})
const approvalHistory = ref([])

const approvalForm = reactive({
  comment: ''
})

// 计算属性
const canApprove = computed(() => {
  const role = userStore.userInfo?.role
  const status = application.value.status
  
  if (role === 'TEACHER') {
    return status === 'PENDING' || status === 'COUNSELOR_APPROVED'
  }
  
  if (role === 'COUNSELOR' && status === 'PENDING') {
    return true
  }
  
  if (role === 'DEPARTMENT_ADMIN' && status === 'COUNSELOR_APPROVED') {
    return true
  }
  
  return false
})

const meetsGpaRequirement = computed(() => {
  const minGpa = application.value.scholarship?.minGpa
  if (!minGpa) return true
  return application.value.gpa >= minGpa
})

const meetsCreditsRequirement = computed(() => {
  const minCredits = application.value.scholarship?.minCredits
  if (!minCredits) return true
  return application.value.totalCredits >= minCredits
})

const meetsAllRequirements = computed(() => {
  return meetsGpaRequirement.value && meetsCreditsRequirement.value
})

// 获取申请详情
const fetchApplicationDetail = async () => {
  try {
    loading.value = true
    const res = await getApproverApplicationDetail(route.params.id)
    application.value = res.data
  } catch (error) {
    console.error('获取申请详情失败:', error)
    ElMessage.error('获取申请详情失败')
    goBack()
  } finally {
    loading.value = false
  }
}

// 获取审批历史
const fetchApprovalHistory = async () => {
  try {
    const res = await getApprovalHistory(route.params.id)
    approvalHistory.value = res.data || []
  } catch (error) {
    console.error('获取审批历史失败:', error)
    // 获取审批历史失败不阻止页面显示
  }
}

// 审批操作
const handleApprove = async (action) => {
  if (action === 'REJECT' && !approvalForm.comment) {
    ElMessage.warning('拒绝申请时必须填写理由')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确认${getApprovalActionText(action)}该申请吗？`,
      '审批确认',
      {
        type: 'warning',
        confirmButtonText: '确认',
        cancelButtonText: '取消'
      }
    )

    approving.value = true
    await approveApplication(application.value.id, {
      action,
      comment: approvalForm.comment
    })

    ElMessage.success(`${getApprovalActionText(action)}成功`)
    
    // 刷新数据
    await fetchApplicationDetail()
    await fetchApprovalHistory()
    
    // 清空表单
    approvalForm.comment = ''
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '操作失败')
    }
  } finally {
    approving.value = false
  }
}

// 返回列表
const goBack = () => {
  router.push({ name: 'TeacherScholarshipApprovals' })
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

// 获取GPA类名
const getGpaClass = (gpa) => {
  if (gpa >= 3.7) return 'success'
  if (gpa >= 3.0) return 'primary'
  if (gpa >= 2.5) return 'warning'
  return 'danger'
}

// 获取GPA颜色
const getGpaColor = (gpa) => {
  if (gpa >= 3.7) return '#67c23a'
  if (gpa >= 3.0) return '#409eff'
  if (gpa >= 2.5) return '#e6a23c'
  return '#f56c6c'
}

// 获取审批动作类型
const getApprovalActionType = (action) => {
  const types = {
    APPROVE: 'success',
    REJECT: 'danger',
    RETURN: 'warning'
  }
  return types[action] || 'info'
}

// 获取审批动作文本
const getApprovalActionText = (action) => {
  const texts = {
    APPROVE: '通过',
    REJECT: '拒绝',
    RETURN: '退回'
  }
  return texts[action] || action
}

// 获取审批级别文本
const getApprovalLevelText = (level) => {
  const texts = {
    1: '辅导员审批',
    2: '院系审批',
    3: '教务处审批'
  }
  return texts[level] || `第${level}级审批`
}

// 获取时间轴类型
const getApprovalTimelineType = (action) => {
  const types = {
    APPROVE: 'success',
    REJECT: 'danger',
    RETURN: 'warning'
  }
  return types[action] || 'primary'
}

// 获取时间轴图标
const getApprovalTimelineIcon = (action) => {
  const icons = {
    APPROVE: CircleCheck,
    REJECT: CircleClose,
    RETURN: RefreshRight
  }
  return icons[action] || Clock
}

// 获取查看模式提示
const getViewModeTipTitle = () => {
  const status = application.value.status
  if (status === 'DEPT_APPROVED') {
    return '该申请已通过所有审批'
  }
  if (status === 'REJECTED') {
    return '该申请已被拒绝'
  }
  return '您暂无权限审批该申请'
}

// 计算学生年级
const getStudentGrade = (student) => {
  if (!student || !student.enrollmentYear) return '-'
  const currentYear = new Date().getFullYear()
  const grade = currentYear - student.enrollmentYear + 1
  if (grade < 1 || grade > 6) return '-'
  return `${grade}`
}

// 获取学生状态类型
const getStudentStatusType = (status) => {
  const types = {
    ACTIVE: 'success',
    SUSPENDED: 'warning',
    GRADUATED: 'info',
    WITHDRAWN: 'danger'
  }
  return types[status] || 'info'
}

// 获取学生状态文本
const getStudentStatusText = (status) => {
  const texts = {
    ACTIVE: '在读',
    SUSPENDED: '休学',
    GRADUATED: '已毕业',
    WITHDRAWN: '退学'
  }
  return texts[status] || status || '-'
}

onMounted(() => {
  fetchApplicationDetail()
  fetchApprovalHistory()
})
</script>

<style scoped>
.approval-detail-container {
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}

/* 页面头部 */
.page-header {
  background: white;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.header-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 15px;
}

.header-title h1 {
  margin: 0;
  font-size: 24px;
  color: #303133;
}

/* 内容区域 */
.content-wrapper {
  display: flex;
  gap: 20px;
}

.info-card {
  margin-bottom: 20px;
  border-radius: 12px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  font-size: 16px;
  color: #303133;
}

/* 学生资料 */
.student-profile {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 20px;
}

.profile-avatar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.profile-info {
  flex: 1;
}

.profile-name {
  margin: 0 0 10px 0;
  font-size: 24px;
  color: #303133;
}

.profile-meta {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

/* 奖学金信息 */
.scholarship-info {
  line-height: 1.8;
}

.scholarship-main {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.scholarship-name {
  margin: 0;
  font-size: 20px;
  color: #303133;
}

.scholarship-tags {
  display: flex;
  gap: 10px;
}

.scholarship-detail p,
.scholarship-requirements p {
  margin: 10px 0;
  color: #606266;
}

.description {
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
  color: #606266;
}

.description.empty,
.scholarship-requirements .empty {
  color: #909399;
  font-style: italic;
}

.scholarship-requirements ul {
  margin: 10px 0;
  padding-left: 20px;
  color: #606266;
}

/* 学业成绩 */
.grades-section {
  line-height: 1.8;
}

.grade-item {
  text-align: center;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 12px;
}

.grade-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 10px;
}

.grade-value {
  font-size: 32px;
  font-weight: bold;
  margin-bottom: 10px;
}

.grade-value.success {
  color: #67c23a;
}

.grade-value.primary {
  color: #409eff;
}

.grade-value.warning {
  color: #e6a23c;
}

.grade-value.danger {
  color: #f56c6c;
}

.grade-comparison {
  margin-top: 20px;
}

.grade-comparison h4 {
  margin: 0 0 15px 0;
  color: #303133;
}

.comparison-item {
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
  margin-bottom: 10px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.comparison-item .label {
  color: #606266;
}

.comparison-item .value {
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 5px;
}

.comparison-item .value.success {
  color: #67c23a;
}

.comparison-item .value.danger {
  color: #f56c6c;
}

/* 个人陈述 */
.personal-statement {
  line-height: 1.8;
}

.statement-content {
  margin: 0;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
  color: #606266;
  white-space: pre-wrap;
}

/* 附件 */
.attachments {
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
}

/* 审批历史 */
.history-item {
  line-height: 1.6;
}

.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.history-approver {
  font-weight: 600;
  color: #303133;
}

.history-level {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.history-comment {
  margin-top: 12px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
}

.history-comment p {
  margin: 8px 0 0 0;
  color: #606266;
}

/* 右侧栏 */
.sticky-card {
  position: sticky;
  top: 20px;
  max-height: calc(100vh - 100px);
  overflow-y: auto;
  margin-bottom: 20px;
  scrollbar-width: thin;
  scrollbar-color: #dcdfe6 transparent;
}

.sticky-card::-webkit-scrollbar {
  width: 6px;
}

.sticky-card::-webkit-scrollbar-track {
  background: transparent;
}

.sticky-card::-webkit-scrollbar-thumb {
  background-color: #dcdfe6;
  border-radius: 3px;
}

.sticky-card::-webkit-scrollbar-thumb:hover {
  background-color: #c0c4cc;
}

.quick-info {
  line-height: 1.8;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.info-label {
  color: #606266;
  font-size: 14px;
}

.info-value {
  color: #303133;
  font-weight: 500;
  flex: 1;
  text-align: right;
  word-break: break-all;
}

/* 审批操作 */
.approval-actions {
  margin-top: 0;
}

.action-title {
  margin: 0 0 16px 0;
  font-size: 16px;
  color: #303133;
}

.view-mode-tip {
  margin-top: 0;
}

/* 加载状态 */
.loading-wrapper {
  background: white;
  border-radius: 12px;
  padding: 20px;
}

/* 响应式 */
@media (max-width: 992px) {
  .sticky-card {
    position: static;
  }
}

@media (max-width: 768px) {
  .approval-detail-container {
    padding: 10px;
  }

  .student-profile {
    flex-direction: column;
    text-align: center;
  }

  .scholarship-main {
    flex-direction: column;
    gap: 15px;
    text-align: center;
  }

  .grade-item {
    margin-bottom: 15px;
  }
}
</style>
