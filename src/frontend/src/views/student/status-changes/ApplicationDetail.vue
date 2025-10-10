<template>
  <div class="application-detail">
    <el-card v-loading="loading">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span style="font-weight: 600;">申请详情</span>
          <el-button @click="goBack">返回</el-button>
        </div>
      </template>

      <el-descriptions v-if="detail" :column="2" border>
        <el-descriptions-item label="申请编号">{{ detail.id }}</el-descriptions-item>
        <el-descriptions-item label="异动类型">{{ getTypeText(detail.type) }}</el-descriptions-item>
        <el-descriptions-item label="审批状态">
          <el-tag :type="getStatusType(detail.status)">
            {{ getStatusText(detail.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="当前审批级别">
          {{ getLevelText(detail.approvalLevel) }}
        </el-descriptions-item>
        <el-descriptions-item label="提交时间">{{ detail.createdAt }}</el-descriptions-item>
        <el-descriptions-item v-if="detail.startDate" label="开始日期">{{ detail.startDate }}</el-descriptions-item>
        <el-descriptions-item v-if="detail.endDate" label="结束日期">{{ detail.endDate }}</el-descriptions-item>
        <el-descriptions-item v-if="detail.targetMajorName" label="目标专业">{{ detail.targetMajorName }}</el-descriptions-item>
        <el-descriptions-item label="异动原因" :span="2">{{ detail.reason }}</el-descriptions-item>
      </el-descriptions>

      <!-- 审批进度 -->
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
                  审批结果：<el-tag size="small">{{ getActionText(item.action) }}</el-tag>
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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getApplicationDetail } from '@/api/statusChange'

const router = useRouter()
const route = useRoute()

const detail = ref(null)
const loading = ref(false)

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

const getActionText = (action) => {
  const map = {
    'APPROVE': '批准',
    'REJECT': '拒绝',
    'RETURN': '退回'
  }
  return map[action] || action
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
    const res = await getApplicationDetail(route.params.id)
    detail.value = res.data
  } catch (error) {
    ElMessage.error('获取详情失败')
  } finally {
    loading.value = false
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
.application-detail {
  padding: 20px;
}
</style>

