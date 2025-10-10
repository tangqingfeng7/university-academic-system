<template>
  <div class="approval-list">
    <!-- 页面标题 -->
    <div class="page-header">
      <div>
        <h2 class="page-title">学籍异动审批</h2>
        <p class="page-subtitle">处理学生的学籍异动申请</p>
      </div>
    </div>

    <!-- 待审批提示 -->
    <div v-if="pendingCount > 0" class="pending-alert">
      <svg class="alert-icon" viewBox="0 0 20 20" fill="currentColor">
        <path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clip-rule="evenodd"/>
      </svg>
      <span class="alert-text">您有 <strong>{{ pendingCount }}</strong> 条待审批申请</span>
    </div>

    <!-- 申请列表 -->
    <div v-loading="loading" class="approvals-container">
      <div v-if="tableData.length === 0" class="empty-state">
        <svg class="empty-icon" viewBox="0 0 64 64" fill="none">
          <circle cx="32" cy="32" r="28" stroke="#E5E7EB" stroke-width="2"/>
          <path d="M32 20v24M44 32H20" stroke="#9CA3AF" stroke-width="2" stroke-linecap="round"/>
        </svg>
        <p class="empty-text">暂无待审批申请</p>
      </div>

      <div v-else class="approval-items">
        <div 
          v-for="item in tableData" 
          :key="item.id" 
          class="approval-item"
          @click="handleApprove(item.id)"
        >
          <div class="approval-header">
            <div class="student-info">
              <div class="student-avatar">
                {{ item.studentName?.charAt(0) || '学' }}
              </div>
              <div class="student-details">
                <div class="student-name">{{ item.studentName }}</div>
                <div class="student-no">{{ item.studentNo }}</div>
              </div>
            </div>
            <div class="type-badge" :class="`type-${item.type}`">
              {{ getTypeText(item.type) }}
            </div>
          </div>

          <div class="approval-body">
            <div class="info-row">
              <div class="info-item">
                <span class="info-label">申请编号</span>
                <span class="info-value">#{{ item.id }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">提交时间</span>
                <span class="info-value">{{ item.createdAt }}</span>
              </div>
            </div>
          </div>

          <div class="approval-footer">
            <el-button type="primary" @click.stop="handleApprove(item.id)">
              立即审批
              <svg style="width: 14px; height: 14px; margin-left: 4px;" viewBox="0 0 20 20" fill="currentColor">
                <path fill-rule="evenodd" d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 011.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z" clip-rule="evenodd"/>
              </svg>
            </el-button>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div v-if="tableData.length > 0" class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next"
          background
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getPendingApprovals } from '@/api/statusChange'

const router = useRouter()

const tableData = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

const pendingCount = computed(() => total.value)

const getTypeText = (type) => {
  const map = {
    'SUSPENSION': '休学',
    'RESUMPTION': '复学',
    'TRANSFER': '转专业',
    'WITHDRAWAL': '退学'
  }
  return map[type] || type
}

const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value
    }
    const res = await getPendingApprovals(params)
    tableData.value = res.data.content || []
    total.value = res.data.totalElements || 0
  } catch (error) {
    ElMessage.error('查询失败')
  } finally {
    loading.value = false
  }
}

const handleApprove = (id) => {
  router.push(`/teacher/status-changes/${id}`)
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.approval-list {
  padding: 24px;
  max-width: 1400px;
  margin: 0 auto;
}

/* 页面标题 */
.page-header {
  margin-bottom: 24px;
}

.page-title {
  font-size: 28px;
  font-weight: 600;
  color: #1d1d1f;
  margin: 0 0 4px 0;
  letter-spacing: -0.02em;
}

.page-subtitle {
  font-size: 14px;
  color: #86868b;
  margin: 0;
}

/* 待审批提示 */
.pending-alert {
  display: flex;
  align-items: center;
  gap: 12px;
  background: #fff3cd;
  border: 1px solid #ffeaa7;
  border-radius: 8px;
  padding: 16px 20px;
  margin-bottom: 24px;
}

.alert-icon {
  width: 24px;
  height: 24px;
  color: #856404;
  flex-shrink: 0;
}

.alert-text {
  font-size: 14px;
  color: #856404;
}

.alert-text strong {
  font-weight: 600;
}

/* 审批列表容器 */
.approvals-container {
  background: #ffffff;
  border: 1px solid #e5e5e5;
  border-radius: 12px;
  padding: 24px;
  min-height: 400px;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
}

.empty-icon {
  width: 80px;
  height: 80px;
  margin-bottom: 16px;
  opacity: 0.5;
}

.empty-text {
  font-size: 15px;
  color: #86868b;
  margin: 0;
}

/* 审批列表 */
.approval-items {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.approval-item {
  background: #fafafa;
  border: 1px solid #e5e5e5;
  border-radius: 8px;
  padding: 20px;
  transition: all 0.2s ease;
  cursor: pointer;
}

.approval-item:hover {
  background: #f5f5f7;
  border-color: #d2d2d7;
}

.approval-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.student-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.student-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: 600;
  flex-shrink: 0;
}

.student-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.student-name {
  font-size: 16px;
  font-weight: 600;
  color: #1d1d1f;
}

.student-no {
  font-size: 13px;
  color: #86868b;
}

.type-badge {
  padding: 4px 12px;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 500;
}

.type-badge.type-SUSPENSION {
  background: #fff3cd;
  color: #856404;
}

.type-badge.type-RESUMPTION {
  background: #d1ecf1;
  color: #0c5460;
}

.type-badge.type-TRANSFER {
  background: #d4edda;
  color: #155724;
}

.type-badge.type-WITHDRAWAL {
  background: #f8d7da;
  color: #721c24;
}

.approval-body {
  margin-bottom: 16px;
}

.info-row {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-label {
  font-size: 13px;
  color: #86868b;
}

.info-value {
  font-size: 14px;
  color: #1d1d1f;
  font-weight: 500;
}

.approval-footer {
  display: flex;
  justify-content: flex-end;
  padding-top: 16px;
  border-top: 1px solid #e5e5e5;
}

/* 分页 */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid #e5e5e5;
}

/* 响应式 */
@media (max-width: 768px) {
  .info-row {
    grid-template-columns: 1fr;
  }
}
</style>

