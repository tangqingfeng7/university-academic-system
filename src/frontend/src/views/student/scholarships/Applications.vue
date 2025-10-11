<template>
  <div class="my-applications">
    <div class="page-header">
      <h1 class="page-title">我的申请</h1>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="5" animated />
    </div>

    <!-- 申请列表 -->
    <div v-else-if="applications.length > 0" class="applications-list">
      <el-card
        v-for="app in applications"
        :key="app.id"
        shadow="hover"
        class="application-card"
      >
        <div class="card-header">
          <div class="header-left">
            <h3 class="scholarship-name">{{ app.scholarship?.name }}</h3>
            <el-tag :type="getStatusType(app.status)" size="small">
              {{ getStatusText(app.status) }}
            </el-tag>
          </div>
          <div class="header-right">
            <el-button
              type="primary"
              link
              @click="viewDetail(app)"
            >
              查看详情
            </el-button>
            <el-button
              v-if="app.status === 'PENDING'"
              type="danger"
              link
              @click="handleCancel(app)"
            >
              撤销申请
            </el-button>
          </div>
        </div>

        <el-divider />

        <div class="card-body">
          <el-row :gutter="20">
            <el-col :span="12">
              <div class="info-item">
                <label>申请时间：</label>
                <span>{{ formatDate(app.submittedAt) }}</span>
              </div>
              <div class="info-item">
                <label>奖金金额：</label>
                <span class="amount">¥{{ app.scholarship?.amount?.toLocaleString() }}</span>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="info-item">
                <label>学年GPA：</label>
                <span>{{ app.gpa }}</span>
              </div>
              <div class="info-item">
                <label>总学分：</label>
                <span>{{ app.totalCredits }}</span>
              </div>
            </el-col>
          </el-row>

          <div v-if="app.comprehensiveScore" class="info-item">
            <label>综合得分：</label>
            <el-progress
              :percentage="app.comprehensiveScore"
              :color="getScoreColor(app.comprehensiveScore)"
            />
          </div>
        </div>

        <!-- 审批流程 -->
        <el-divider />
        <div class="approval-timeline">
          <h4>审批进度</h4>
          <el-steps :active="getActiveStep(app.status)" align-center>
            <el-step title="提交申请" :icon="EditPen" />
            <el-step title="辅导员审核" :icon="User" />
            <el-step title="院系审批" :icon="OfficeBuilding" />
            <el-step
              :title="app.status === 'REJECTED' ? '已拒绝' : '已通过'"
              :icon="app.status === 'REJECTED' ? CloseBold : Select"
              :status="app.status === 'REJECTED' ? 'error' : undefined"
            />
          </el-steps>
        </div>
      </el-card>
    </div>

    <!-- 空状态 -->
    <el-empty v-else description="您还没有提交过申请">
      <el-button type="primary" @click="goToScholarships">
        去申请奖学金
      </el-button>
    </el-empty>

    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailVisible"
      title="申请详情"
      width="800px"
      :close-on-click-modal="false"
    >
      <div v-if="currentApplication" class="detail-content">
        <h3>基本信息</h3>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="奖学金名称">
            {{ currentApplication.scholarship?.name }}
          </el-descriptions-item>
          <el-descriptions-item label="申请状态">
            <el-tag :type="getStatusType(currentApplication.status)">
              {{ getStatusText(currentApplication.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="申请时间" :span="2">
            {{ formatDate(currentApplication.submittedAt) }}
          </el-descriptions-item>
          <el-descriptions-item label="GPA">
            {{ currentApplication.gpa }}
          </el-descriptions-item>
          <el-descriptions-item label="总学分">
            {{ currentApplication.totalCredits }}
          </el-descriptions-item>
          <el-descriptions-item label="综合得分" :span="2">
            <span v-if="currentApplication.comprehensiveScore">
              {{ currentApplication.comprehensiveScore }}分
            </span>
            <span v-else class="text-muted">待评定</span>
          </el-descriptions-item>
        </el-descriptions>

        <h3 style="margin-top: 20px;">个人陈述</h3>
        <div class="personal-statement">
          {{ currentApplication.personalStatement }}
        </div>

        <h3 v-if="currentApplication.attachmentUrl" style="margin-top: 20px;">证明材料</h3>
        <div v-if="currentApplication.attachmentUrl" class="attachments">
          <el-link
            v-for="(url, index) in currentApplication.attachmentUrl.split(',')"
            :key="index"
            :href="url"
            target="_blank"
            type="primary"
            :icon="Download"
          >
            附件{{ index + 1 }}
          </el-link>
        </div>

        <!-- 审批历史 -->
        <h3 style="margin-top: 20px;">审批历史</h3>
        <el-timeline v-if="approvalHistory.length > 0">
          <el-timeline-item
            v-for="(record, index) in approvalHistory"
            :key="index"
            :timestamp="formatDate(record.approvedAt)"
            placement="top"
            :type="record.action === 'APPROVE' ? 'success' : 'danger'"
          >
            <el-card>
              <h4>
                {{ record.approvalLevelDescription }} - {{ record.approverName }}
                <el-tag 
                  :type="record.action === 'APPROVE' ? 'success' : 'danger'" 
                  size="small"
                  style="margin-left: 10px;"
                >
                  {{ record.actionDescription }}
                </el-tag>
              </h4>
              <p v-if="record.comment" style="margin-top: 10px; color: #606266;">
                <strong>审批意见：</strong>{{ record.comment }}
              </p>
              <p v-else style="margin-top: 10px; color: #909399;">
                无审批意见
              </p>
            </el-card>
          </el-timeline-item>
        </el-timeline>
        <el-empty v-else description="暂无审批记录" :image-size="80" />
      </div>

      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Document,
  EditPen,
  User,
  OfficeBuilding,
  Select,
  CloseBold,
  Download
} from '@element-plus/icons-vue'
import { getMyApplications, cancelApplication, getApprovalFlow } from '@/api/scholarship'
import { formatDate } from '@/utils/date'

const router = useRouter()

const loading = ref(true)
const applications = ref([])
const detailVisible = ref(false)
const currentApplication = ref(null)
const approvalHistory = ref([])

// 获取我的申请列表
const fetchApplications = async () => {
  try {
    loading.value = true
    const res = await getMyApplications()
    applications.value = res.data || []
  } catch (error) {
    console.error('获取申请列表失败:', error)
    ElMessage.error('获取申请列表失败')
  } finally {
    loading.value = false
  }
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

// 获取得分颜色
const getScoreColor = (score) => {
  if (score >= 90) return '#67c23a'
  if (score >= 80) return '#409eff'
  if (score >= 70) return '#e6a23c'
  return '#f56c6c'
}

// 获取当前步骤
const getActiveStep = (status) => {
  const steps = {
    PENDING: 1,
    COUNSELOR_APPROVED: 2,
    DEPT_APPROVED: 3,
    REJECTED: 3
  }
  return steps[status] || 0
}

// 查看详情
const viewDetail = async (app) => {
  currentApplication.value = app
  detailVisible.value = true

  // 获取审批历史
  try {
    const res = await getApprovalFlow(app.id)
    approvalHistory.value = res.data || []
  } catch (error) {
    console.error('获取审批历史失败:', error)
    approvalHistory.value = []
  }
}

// 撤销申请
const handleCancel = async (app) => {
  try {
    await ElMessageBox.confirm(
      '确定要撤销该申请吗？撤销后将无法恢复。',
      '确认撤销',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await cancelApplication(app.id)
    ElMessage.success('申请已撤销')
    fetchApplications()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('撤销申请失败:', error)
      ElMessage.error('撤销申请失败')
    }
  }
}

// 去申请奖学金
const goToScholarships = () => {
  router.push({ name: 'StudentScholarships' })
}

onMounted(() => {
  fetchApplications()
})
</script>

<style scoped>
.my-applications {
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.page-header {
  padding: 24px 0;
  margin-bottom: 20px;
}

.page-title {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #1a1a1a;
}

.loading-container {
  padding: 20px;
}

.applications-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.application-card {
  transition: all 0.3s ease;
}

.application-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.header-left {
  flex: 1;
}

.scholarship-name {
  margin: 0 0 10px 0;
  font-size: 18px;
  font-weight: bold;
  color: #303133;
}

.header-right {
  display: flex;
  gap: 10px;
}

.card-body {
  margin: 15px 0;
}

.info-item {
  display: flex;
  margin-bottom: 12px;
}

.info-item label {
  color: #909399;
  font-size: 14px;
  min-width: 100px;
}

.info-item span {
  flex: 1;
  font-weight: 500;
  color: #303133;
}

.amount {
  color: #f56c6c;
  font-size: 16px;
  font-weight: bold;
}

.approval-timeline {
  margin-top: 15px;
}

.approval-timeline h4 {
  margin: 0 0 15px 0;
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.detail-content h3 {
  margin: 0 0 15px 0;
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.personal-statement {
  padding: 15px;
  background: #f5f7fa;
  border-radius: 8px;
  line-height: 1.8;
  color: #606266;
  white-space: pre-wrap;
}

.attachments {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.text-muted {
  color: #909399;
  font-style: italic;
}

:deep(.el-progress__text) {
  font-weight: bold;
}
</style>

