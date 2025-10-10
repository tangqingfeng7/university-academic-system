<template>
  <div class="status-change-detail">
    <el-card v-loading="loading">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span style="font-weight: 600;">学籍异动详情</span>
          <el-button @click="goBack">返回</el-button>
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

      <!-- 审批历史 -->
      <div v-if="detail && detail.approvalHistory" style="margin-top: 20px;">
        <h3>审批历史</h3>
        <el-timeline style="margin-top: 15px;">
          <el-timeline-item
            v-for="item in detail.approvalHistory"
            :key="item.id"
            :timestamp="item.approvedAt"
            placement="top"
          >
            <p>{{ getLevelText(item.approvalLevel) }} - {{ item.approverName }}</p>
            <p>审批结果：<el-tag size="small">{{ item.action }}</el-tag></p>
            <p v-if="item.comment">审批意见：{{ item.comment }}</p>
          </el-timeline-item>
        </el-timeline>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getAdminApplicationDetail } from '@/api/statusChange'

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

