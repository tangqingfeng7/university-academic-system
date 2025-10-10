<template>
  <div class="status-change-detail">
    <el-card v-loading="loading">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span style="font-weight: 600;">学籍异动详情</span>
          <div style="display: flex; gap: 12px;">
            <el-button 
              v-if="detail && detail.status === 'PENDING'" 
              type="success" 
              @click="showApprovalDialog('APPROVE')"
            >
              批准
            </el-button>
            <el-button 
              v-if="detail && detail.status === 'PENDING'" 
              type="danger" 
              @click="showApprovalDialog('REJECT')"
            >
              拒绝
            </el-button>
            <el-button @click="goBack">返回</el-button>
          </div>
        </div>
      </template>

      <el-descriptions v-if="detail" :column="2" border>
        <el-descriptions-item label="申请编号">{{ detail.id }}</el-descriptions-item>
        <el-descriptions-item label="学生姓名">{{ detail.studentName }}</el-descriptions-item>
        <el-descriptions-item label="学号">{{ detail.studentNo }}</el-descriptions-item>
        <el-descriptions-item label="异动类型">{{ getTypeText(detail.type) }}</el-descriptions-item>
        <el-descriptions-item label="审批状态">
          <el-tag :type="getStatusType(detail.status)">
            {{ getStatusText(detail.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="当前审批级别">{{ getLevelText(detail.approvalLevel) }}</el-descriptions-item>
        <el-descriptions-item label="提交时间">{{ detail.createdAt }}</el-descriptions-item>
        <el-descriptions-item label="异动原因" :span="2">{{ detail.reason }}</el-descriptions-item>
      </el-descriptions>

      <!-- 审批进度和历史 -->
      <div v-if="detail" style="margin-top: 30px;">
        <h3>审批进度</h3>
        <el-steps :active="getActiveStep()" finish-status="success" style="margin-top: 20px;">
          <el-step title="辅导员审批" />
          <el-step title="院系审批" />
          <el-step title="教务处审批" />
        </el-steps>

        <!-- 审批历史 -->
        <div v-if="detail.approvalHistory && detail.approvalHistory.length > 0" style="margin-top: 30px;">
          <h4>审批记录</h4>
          <el-timeline style="margin-top: 15px;">
            <el-timeline-item
              v-for="item in detail.approvalHistory"
              :key="item.id"
              :timestamp="item.approvedAt"
              placement="top"
            >
              <div>
                <div>{{ getLevelText(item.approvalLevel) }} - {{ item.approverName }}</div>
                <div style="margin-top: 5px;">
                  审批结果：<el-tag size="small">{{ item.action }}</el-tag>
                </div>
                <div v-if="item.comment" style="margin-top: 5px; color: #606266;">
                  审批意见：{{ item.comment }}
                </div>
              </div>
            </el-timeline-item>
          </el-timeline>
        </div>
        <div v-else style="margin-top: 30px; color: #909399; text-align: center;">
          <el-empty description="暂无审批记录" :image-size="60" />
        </div>
      </div>
    </el-card>

    <!-- 审批对话框 -->
    <el-dialog
      v-model="approvalDialogVisible"
      :title="approvalAction === 'APPROVE' ? '批准申请' : '拒绝申请'"
      width="500px"
    >
      <el-form :model="approvalForm" label-width="80px">
        <el-form-item label="审批意见">
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
        <el-button @click="approvalDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitApproval" :loading="submitting">
          确认{{ approvalAction === 'APPROVE' ? '批准' : '拒绝' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getAdminApplicationDetail, adminApproveApplication } from '@/api/statusChange'

const router = useRouter()
const route = useRoute()

const detail = ref(null)
const loading = ref(false)
const approvalDialogVisible = ref(false)
const approvalAction = ref('')
const submitting = ref(false)

const approvalForm = reactive({
  comment: ''
})

const getTypeText = (type) => {
  const map = {
    'SUSPENSION': '休学',
    'RESUMPTION': '复学',
    'TRANSFER': '转专业',
    'WITHDRAWAL': '退学'
  }
  return map[type] || type
}

const getStatusText = (status) => {
  const map = {
    'PENDING': '待审批',
    'APPROVED': '已批准',
    'REJECTED': '已拒绝',
    'CANCELLED': '已取消'
  }
  return map[status] || status
}

const getStatusType = (status) => {
  const map = {
    'PENDING': 'warning',
    'APPROVED': 'success',
    'REJECTED': 'danger',
    'CANCELLED': 'info'
  }
  return map[status] || ''
}

const getLevelText = (level) => {
  const map = {
    1: '辅导员审批',
    2: '院系审批',
    3: '教务处审批'
  }
  return map[level] || `级别${level}`
}

const getActiveStep = () => {
  if (!detail.value) return 0
  
  // 如果已经批准或拒绝，显示所有步骤完成
  if (detail.value.status === 'APPROVED' || detail.value.status === 'REJECTED') {
    return 3
  }
  
  // 如果有审批历史，根据最高审批级别显示
  if (detail.value.approvalHistory && detail.value.approvalHistory.length > 0) {
    const maxLevel = Math.max(...detail.value.approvalHistory.map(item => item.approvalLevel))
    return maxLevel
  }
  
  // 否则根据当前审批级别显示（当前级别还未完成，所以减1）
  return detail.value.approvalLevel - 1
}

const fetchDetail = async () => {
  loading.value = true
  try {
    const res = await getAdminApplicationDetail(route.params.id)
    detail.value = res.data
  } catch (error) {
    ElMessage.error('获取详情失败')
  } finally {
    loading.value = false
  }
}

const showApprovalDialog = (action) => {
  approvalAction.value = action
  approvalForm.comment = ''
  approvalDialogVisible.value = true
}

const submitApproval = async () => {
  try {
    submitting.value = true
    
    await adminApproveApplication(route.params.id, {
      action: approvalAction.value,
      comment: approvalForm.comment
    })
    
    ElMessage.success('审批成功')
    approvalDialogVisible.value = false
    
    // 重新加载详情
    await fetchDetail()
  } catch (error) {
    console.error('审批失败:', error)
    ElMessage.error(error.message || '审批失败')
  } finally {
    submitting.value = false
  }
}

const goBack = () => {
  router.back()
}

onMounted(() => {
  fetchDetail()
})
</script>

<style scoped>
.status-change-detail {
  padding: 20px;
}
</style>

