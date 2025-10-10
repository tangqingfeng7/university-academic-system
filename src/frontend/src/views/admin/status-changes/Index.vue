<template>
  <div class="apple-status-change">
    <!-- 顶部标题栏 -->
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">学籍异动管理</h1>
        <p class="page-subtitle">Student Status Change Management</p>
      </div>
      <button class="export-btn" @click="handleExport">
        <svg class="icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
          <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4M7 10l5 5 5-5M12 15V3"/>
        </svg>
        <span>导出记录</span>
      </button>
    </div>

    <!-- 搜索筛选卡片 -->
    <div class="search-card">
      <div class="search-grid">
        <div class="search-item">
          <label class="search-label">学生姓名</label>
          <input 
            v-model="queryForm.studentName" 
            type="text" 
            class="search-input" 
            placeholder="输入姓名搜索"
          />
        </div>
        <div class="search-item">
          <label class="search-label">学号</label>
          <input 
            v-model="queryForm.studentNo" 
            type="text" 
            class="search-input" 
            placeholder="输入学号搜索"
          />
        </div>
        <div class="search-item">
          <label class="search-label">异动类型</label>
          <select v-model="queryForm.type" class="search-select">
            <option value="">全部类型</option>
            <option value="SUSPENSION">休学</option>
            <option value="RESUMPTION">复学</option>
            <option value="TRANSFER">转专业</option>
            <option value="WITHDRAWAL">退学</option>
          </select>
        </div>
        <div class="search-item">
          <label class="search-label">审批状态</label>
          <select v-model="queryForm.status" class="search-select">
            <option value="">全部状态</option>
            <option value="PENDING">待审批</option>
            <option value="APPROVED">已批准</option>
            <option value="REJECTED">已拒绝</option>
            <option value="CANCELLED">已取消</option>
          </select>
        </div>
      </div>
      <div class="search-actions">
        <button class="action-btn primary" @click="handleQuery">
          <svg class="icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
            <circle cx="11" cy="11" r="8"/>
            <path d="m21 21-4.35-4.35"/>
          </svg>
          <span>搜索</span>
        </button>
        <button class="action-btn secondary" @click="handleReset">
          <svg class="icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
            <path d="M3 12a9 9 0 0 1 9-9 9.75 9.75 0 0 1 6.74 2.74L21 8"/>
            <path d="M21 3v5h-5"/>
          </svg>
          <span>重置</span>
        </button>
      </div>
    </div>

    <!-- 数据统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon pending">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor">
            <circle cx="12" cy="12" r="10"/>
            <polyline points="12 6 12 12 16 14"/>
          </svg>
        </div>
        <div class="stat-info">
          <p class="stat-label">待审批</p>
          <h3 class="stat-value">{{ stats.pending }}</h3>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon approved">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor">
            <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
            <polyline points="22 4 12 14.01 9 11.01"/>
          </svg>
        </div>
        <div class="stat-info">
          <p class="stat-label">已批准</p>
          <h3 class="stat-value">{{ stats.approved }}</h3>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon rejected">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor">
            <circle cx="12" cy="12" r="10"/>
            <line x1="15" y1="9" x2="9" y2="15"/>
            <line x1="9" y1="9" x2="15" y2="15"/>
          </svg>
        </div>
        <div class="stat-info">
          <p class="stat-label">已拒绝</p>
          <h3 class="stat-value">{{ stats.rejected }}</h3>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon total">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor">
            <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
            <circle cx="9" cy="7" r="4"/>
            <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
            <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
          </svg>
        </div>
        <div class="stat-info">
          <p class="stat-label">总申请</p>
          <h3 class="stat-value">{{ total }}</h3>
        </div>
      </div>
    </div>

    <!-- 数据卡片列表 -->
    <div v-loading="loading" class="card-list">
      <div v-if="tableData.length === 0" class="empty-state">
        <svg class="empty-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
          <circle cx="12" cy="12" r="10"/>
          <path d="M12 8v4"/>
          <circle cx="12" cy="16" r="0.5" fill="currentColor"/>
        </svg>
        <p class="empty-text">暂无数据</p>
      </div>
      
      <div
        v-for="item in tableData"
        :key="item.id"
        class="data-card"
        @click="viewDetail(item.id)"
      >
        <div class="card-header">
          <div class="card-title-group">
            <span class="card-id">#{{ item.id }}</span>
            <span :class="['status-badge', getStatusClass(item.status)]">
              {{ getStatusText(item.status) }}
            </span>
          </div>
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

        <div class="card-body">
          <div class="student-info">
            <div class="avatar">
              {{ item.studentName?.charAt(0) || 'S' }}
            </div>
            <div class="info-text">
              <h4 class="student-name">{{ item.studentName }}</h4>
              <p class="student-no">学号: {{ item.studentNo }}</p>
            </div>
        </div>
          
          <div class="card-details">
            <div class="detail-item">
              <svg class="detail-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
                <line x1="16" y1="2" x2="16" y2="6"/>
                <line x1="8" y1="2" x2="8" y2="6"/>
                <line x1="3" y1="10" x2="21" y2="10"/>
              </svg>
              <span class="detail-text">{{ item.createdAt }}</span>
            </div>
            <div class="detail-item">
              <svg class="detail-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                <circle cx="12" cy="7" r="4"/>
              </svg>
              <span class="detail-text">{{ item.majorName || '未知专业' }}</span>
            </div>
          </div>
        </div>

        <div class="card-footer">
          <button class="view-btn">
            <span>查看详情</span>
            <svg class="arrow-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
              <path d="M5 12h14M12 5l7 7-7 7"/>
            </svg>
          </button>
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
import { ref, reactive, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getAllApplications, exportRecords } from '@/api/statusChange'

const router = useRouter()

const queryForm = reactive({
  studentName: '',
  studentNo: '',
  type: '',
  status: ''
})

const tableData = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(12)
const total = ref(0)

const stats = reactive({
  pending: 0,
  approved: 0,
  rejected: 0,
  cancelled: 0
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

const getStatusClass = (status) => {
  const map = {
    'PENDING': 'pending',
    'APPROVED': 'approved',
    'REJECTED': 'rejected',
    'CANCELLED': 'cancelled'
  }
  return map[status] || ''
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

const handleQuery = () => {
  currentPage.value = 1
  fetchData()
}

const handleReset = () => {
  Object.keys(queryForm).forEach(key => {
    queryForm[key] = ''
  })
  handleQuery()
}

const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      ...queryForm,
      page: currentPage.value - 1,
      size: pageSize.value
    }
    const res = await getAllApplications(params)
    tableData.value = res.data.content || []
    total.value = res.data.totalElements || 0
    
    // 更新统计数据
    stats.pending = tableData.value.filter(item => item.status === 'PENDING').length
    stats.approved = tableData.value.filter(item => item.status === 'APPROVED').length
    stats.rejected = tableData.value.filter(item => item.status === 'REJECTED').length
  } catch (error) {
    ElMessage.error('查询失败')
  } finally {
    loading.value = false
  }
}

const viewDetail = (id) => {
  router.push(`/admin/status-changes/${id}`)
}

const handleExport = async () => {
  try {
    const blob = await exportRecords(queryForm)
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `学籍异动记录_${new Date().getTime()}.xlsx`
    link.click()
    ElMessage.success('导出成功')
  } catch (error) {
    ElMessage.error('导出失败')
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.apple-status-change {
  min-height: 100vh;
  background: #fafafa;
  padding: 48px 64px;
}

/* 页面标题 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
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

.export-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 9px 18px;
  background: #ffffff;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  color: #000000;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.15s ease;
}

.export-btn:hover {
  background: #f5f5f5;
  border-color: #d0d0d0;
}

.export-btn .icon {
  width: 18px;
  height: 18px;
  stroke-width: 2;
}

/* 搜索卡片 */
.search-card {
  background: #ffffff;
  border-radius: 8px;
  padding: 24px;
  margin-bottom: 24px;
  border: 1px solid #e8e8e8;
}

.search-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
}

.search-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.search-label {
  font-size: 13px;
  font-weight: 500;
  color: #86868b;
  letter-spacing: 0.3px;
}

.search-input,
.search-select {
  padding: 8px 12px;
  border: 1px solid #d0d0d0;
  border-radius: 4px;
  font-size: 13px;
  color: #000000;
  background: #ffffff;
  transition: border-color 0.15s ease;
  outline: none;
}

.search-input:focus,
.search-select:focus {
  border-color: #000000;
}

.search-input::placeholder {
  color: #c7c7cc;
}

.search-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 9px 18px;
  border-radius: 4px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.15s ease;
  border: none;
  outline: none;
}

.action-btn .icon {
  width: 16px;
  height: 16px;
  stroke-width: 2;
}

.action-btn.primary {
  background: #000000;
  color: #ffffff;
}

.action-btn.primary:hover {
  background: #333333;
}

.action-btn.secondary {
  background: #f0f0f0;
  color: #000000;
  border: 1px solid #e0e0e0;
}

.action-btn.secondary:hover {
  background: #e8e8e8;
}

/* 统计卡片 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 20px;
  background: #ffffff;
  border-radius: 6px;
  border: 1px solid #e8e8e8;
  transition: border-color 0.15s ease;
}

.stat-card:hover {
  border-color: #d0d0d0;
}

.stat-icon {
  display: none;
}

.stat-info {
  flex: 1;
}

.stat-label {
  font-size: 12px;
  color: #666666;
  margin: 0;
  font-weight: 500;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #000000;
  margin: 0;
  letter-spacing: -0.5px;
}

/* 数据卡片列表 */
.card-list {
  display: flex;
  flex-direction: column;
  gap: 1px;
  background: #e8e8e8;
  border-radius: 6px;
  overflow: hidden;
  margin-bottom: 24px;
}

.data-card {
  background: #ffffff;
  padding: 20px 24px;
  transition: background-color 0.15s ease;
  cursor: pointer;
}

.data-card:hover {
  background: #fafafa;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.card-title-group {
  display: flex;
  align-items: center;
  gap: 12px;
}

.card-id {
  font-size: 13px;
  font-weight: 500;
  color: #666666;
  font-family: 'Consolas', Monaco, monospace;
}

.status-badge {
  padding: 2px 8px;
  border-radius: 3px;
  font-size: 11px;
  font-weight: 500;
  letter-spacing: 0;
}

.status-badge.pending {
  background: #fff4e6;
  color: #d48806;
  border: 1px solid #ffd591;
}

.status-badge.approved {
  background: #f6ffed;
  color: #389e0d;
  border: 1px solid #b7eb8f;
}

.status-badge.rejected {
  background: #fff1f0;
  color: #cf1322;
  border: 1px solid #ffccc7;
}

.status-badge.cancelled {
  background: #fafafa;
  color: #8c8c8c;
  border: 1px solid #d9d9d9;
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

.student-info {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.avatar {
  display: none;
}

.info-text {
  flex: 1;
}

.student-name {
  font-size: 14px;
  font-weight: 500;
  color: #000000;
  margin: 0 0 2px 0;
}

.student-no {
  font-size: 12px;
  color: #666666;
  margin: 0;
  font-family: 'Consolas', Monaco, monospace;
}

.card-details {
  display: flex;
  align-items: center;
  gap: 16px;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

.detail-icon {
  width: 14px;
  height: 14px;
  stroke-width: 2;
  color: #999999;
  flex-shrink: 0;
}

.detail-text {
  font-size: 12px;
  color: #666666;
}

.card-footer {
  display: none;
}

.view-btn {
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
  color: #999999;
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
  .apple-status-change {
    padding: 24px;
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .search-grid {
    grid-template-columns: 1fr;
  }

  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .card-details {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
}
</style>

