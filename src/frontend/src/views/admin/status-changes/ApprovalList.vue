<template>
  <div class="apple-approval-list">
    <!-- 顶部标题栏 -->
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">学籍异动审批</h1>
        <p class="page-subtitle">Pending Approval Applications</p>
      </div>
      <div class="header-badge">
        <span class="badge-number">{{ total }}</span>
        <span class="badge-text">待审批</span>
      </div>
    </div>

    <!-- 待审批卡片列表 -->
    <div v-loading="loading" class="approval-cards">
      <div v-if="tableData.length === 0" class="empty-state">
        <svg class="empty-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
          <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
          <polyline points="22 4 12 14.01 9 11.01"/>
        </svg>
        <p class="empty-text">暂无待审批申请</p>
        <p class="empty-subtext">所有申请都已处理完毕</p>
      </div>

      <div
        v-for="item in tableData"
        :key="item.id"
        class="approval-card"
        @click="handleApprove(item.id)"
      >
        <!-- 卡片左侧指示条 -->
        <div :class="['card-indicator', getTypeClass(item.type)]"></div>
        
        <!-- 卡片主体内容 -->
        <div class="card-content">
          <div class="card-header">
            <div class="header-left">
              <div class="student-avatar">
                {{ item.studentName?.charAt(0) || 'S' }}
              </div>
              <div class="student-info">
                <h3 class="student-name">{{ item.studentName }}</h3>
                <p class="student-meta">
                  <svg class="meta-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                    <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                    <circle cx="12" cy="7" r="4"/>
                  </svg>
                  <span>{{ item.studentNo }}</span>
                </p>
              </div>
            </div>
            <div class="header-right">
              <div :class="['type-badge', getTypeClass(item.type)]">
                <svg class="badge-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                  <path v-if="item.type === 'SUSPENSION'" d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2M9 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8zM23 21v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75"/>
                  <path v-else-if="item.type === 'RESUMPTION'" d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/>
                  <path v-else-if="item.type === 'TRANSFER'" d="M21 2v6h-6M3 12a9 9 0 0 1 15-6.7L21 8M3 22v-6h6M21 12a9 9 0 0 1-15 6.7L3 16"/>
                  <path v-else d="M18 6L6 18M6 6l12 12"/>
                </svg>
                <span>{{ getTypeText(item.type) }}</span>
              </div>
            </div>
          </div>

          <div class="card-body">
            <div class="info-grid">
              <div class="info-item">
                <svg class="info-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                  <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                  <circle cx="12" cy="7" r="4"/>
                </svg>
                <div class="info-text">
                  <span class="info-label">专业</span>
                  <span class="info-value">{{ item.majorName || '未知' }}</span>
                </div>
              </div>
              <div class="info-item">
                <svg class="info-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                  <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
                  <line x1="16" y1="2" x2="16" y2="6"/>
                  <line x1="8" y1="2" x2="8" y2="6"/>
                  <line x1="3" y1="10" x2="21" y2="10"/>
                </svg>
                <div class="info-text">
                  <span class="info-label">提交时间</span>
                  <span class="info-value">{{ formatDate(item.createdAt) }}</span>
                </div>
              </div>
              <div class="info-item">
                <svg class="info-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                  <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
                </svg>
                <div class="info-text">
                  <span class="info-label">审批级别</span>
                  <span class="info-value">{{ getLevelText(item.approvalLevel) }}</span>
                </div>
              </div>
              <div class="info-item">
                <svg class="info-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                  <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                  <polyline points="14 2 14 8 20 8"/>
                </svg>
                <div class="info-text">
                  <span class="info-label">申请编号</span>
                  <span class="info-value">#{{ item.id }}</span>
                </div>
              </div>
            </div>

            <!-- 申请理由预览 -->
            <div v-if="item.reason" class="reason-preview">
              <p class="reason-label">申请理由</p>
              <p class="reason-text">{{ item.reason }}</p>
            </div>
          </div>

          <div class="card-footer">
            <div class="footer-info">
              <svg class="clock-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                <circle cx="12" cy="12" r="10"/>
                <polyline points="12 6 12 12 16 14"/>
              </svg>
              <span>等待审批中</span>
            </div>
            <button class="approve-btn">
              <span>立即审批</span>
              <svg class="arrow-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                <path d="M5 12h14M12 5l7 7-7 7"/>
              </svg>
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 分页器 -->
    <div v-if="total > 0" class="pagination">
      <button 
        class="page-btn" 
        :disabled="currentPage === 1"
        @click="currentPage--; fetchData()"
      >
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor">
          <path d="M15 18l-6-6 6-6"/>
        </svg>
      </button>
      <div class="page-numbers">
        <span class="page-info">
          第 {{ currentPage }} 页，共 {{ Math.ceil(total / pageSize) }} 页
        </span>
      </div>
      <button 
        class="page-btn"
        :disabled="currentPage >= Math.ceil(total / pageSize)"
        @click="currentPage++; fetchData()"
      >
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor">
          <path d="M9 18l6-6-6-6"/>
        </svg>
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getPendingApprovals } from '@/api/statusChange'

const router = useRouter()

const tableData = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const getTypeText = (type) => {
  const map = {
    'SUSPENSION': '休学',
    'RESUMPTION': '复学',
    'TRANSFER': '转专业',
    'WITHDRAWAL': '退学'
  }
  return map[type] || type
}

const getLevelText = (level) => {
  const map = {
    1: '辅导员审批',
    2: '院系审批',
    3: '教务处审批'
  }
  return map[level] || `级别${level}`
}

const getTypeClass = (type) => {
  const map = {
    'SUSPENSION': 'type-suspension',
    'RESUMPTION': 'type-resumption',
    'TRANSFER': 'type-transfer',
    'WITHDRAWAL': 'type-withdrawal'
  }
  return map[type] || ''
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now - date
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  
  if (days === 0) return '今天'
  if (days === 1) return '昨天'
  if (days < 7) return `${days}天前`
  
  return dateStr.split(' ')[0]
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
  router.push(`/admin/status-changes/${id}`)
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.apple-approval-list {
  min-height: 100vh;
  background: #fafafa;
  padding: 48px 64px;
}

/* 页面标题 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 40px;
}

.header-content {
  flex: 1;
}

.page-title {
  font-size: 28px;
  font-weight: 600;
  color: #000000;
  margin: 0 0 6px 0;
  letter-spacing: -0.3px;
}

.page-subtitle {
  font-size: 13px;
  color: #666666;
  margin: 0;
  font-weight: 400;
  letter-spacing: 0;
}

.header-badge {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: #ffffff;
  border-radius: 4px;
  border: 1px solid #e8e8e8;
}

.badge-number {
  font-size: 18px;
  font-weight: 600;
  color: #000000;
  line-height: 1;
}

.badge-text {
  font-size: 12px;
  color: #666666;
  font-weight: 400;
}

/* 审批卡片列表 */
.approval-cards {
  display: flex;
  flex-direction: column;
  gap: 1px;
  background: #e8e8e8;
  border-radius: 6px;
  overflow: hidden;
  margin-bottom: 24px;
}

.approval-card {
  position: relative;
  background: #ffffff;
  transition: background-color 0.15s ease;
  cursor: pointer;
}

.approval-card:hover {
  background: #fafafa;
}

.card-indicator {
  display: none;
}

.card-content {
  padding: 20px 24px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.student-avatar {
  display: none;
}

.student-info {
  flex: 1;
}

.student-name {
  font-size: 14px;
  font-weight: 500;
  color: #000000;
  margin: 0 0 2px 0;
}

.student-meta {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #666666;
  margin: 0;
  font-family: 'Consolas', Monaco, monospace;
}

.meta-icon {
  display: none;
}

.header-right {
  flex-shrink: 0;
}

.type-badge {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 2px 8px;
  border-radius: 3px;
  font-size: 12px;
  font-weight: 500;
  background: #fafafa;
  color: #666666;
  border: 1px solid #e8e8e8;
}

.badge-icon {
  display: none;
}

.card-body {
  margin-bottom: 0;
}

.info-grid {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

.info-icon {
  width: 14px;
  height: 14px;
  stroke-width: 2;
  color: #999999;
  flex-shrink: 0;
}

.info-text {
  display: flex;
  align-items: center;
  gap: 4px;
}

.info-label {
  font-size: 12px;
  color: #999999;
  font-weight: 400;
}

.info-value {
  font-size: 12px;
  color: #666666;
  font-weight: 400;
}

.reason-preview {
  display: none;
}

.card-footer {
  display: none;
}

.footer-info {
  display: none;
}

.clock-icon {
  display: none;
}

.approve-btn {
  display: none;
}

.arrow-icon {
  display: none;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  background: #ffffff;
}

.empty-icon {
  width: 48px;
  height: 48px;
  color: #d0d0d0;
  stroke-width: 1.5;
  margin-bottom: 12px;
}

.empty-text {
  font-size: 13px;
  font-weight: 500;
  color: #000000;
  margin: 0 0 4px 0;
}

.empty-subtext {
  font-size: 12px;
  color: #999999;
  margin: 0;
}

/* 分页器 */
.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 20px;
}

.page-btn {
  width: 32px;
  height: 32px;
  border-radius: 4px;
  border: 1px solid #d0d0d0;
  background: #ffffff;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.15s ease;
}

.page-btn:not(:disabled):hover {
  background: #fafafa;
  border-color: #b0b0b0;
}

.page-btn:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}

.page-btn svg {
  width: 16px;
  height: 16px;
  stroke-width: 2;
  color: #000000;
}

.page-numbers {
  padding: 0 12px;
}

.page-info {
  font-size: 12px;
  color: #666666;
  font-weight: 400;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .apple-approval-list {
    padding: 24px;
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .header-badge {
    align-self: stretch;
  }

  .info-grid {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>

