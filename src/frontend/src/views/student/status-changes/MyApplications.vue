<template>
  <div class="my-applications">
    <!-- 页面标题 -->
    <div class="page-header">
      <div>
        <h2 class="page-title">我的学籍异动</h2>
        <p class="page-subtitle">查看和管理您的学籍异动申请</p>
      </div>
      <el-button type="primary" @click="goToCreate" style="height: 40px;">
        <svg style="width: 16px; height: 16px; margin-right: 6px;" viewBox="0 0 20 20" fill="currentColor">
          <path d="M10 5a1 1 0 011 1v3h3a1 1 0 110 2h-3v3a1 1 0 11-2 0v-3H6a1 1 0 110-2h3V6a1 1 0 011-1z"/>
        </svg>
        提交新申请
      </el-button>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-label">全部申请</div>
        <div class="stat-value">{{ tableData.length }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">待审批</div>
        <div class="stat-value stat-warning">{{ pendingCount }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">已批准</div>
        <div class="stat-value stat-success">{{ approvedCount }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">已拒绝</div>
        <div class="stat-value stat-danger">{{ rejectedCount }}</div>
      </div>
    </div>

    <!-- 申请列表 -->
    <div v-loading="loading" class="applications-container">
      <div v-if="tableData.length === 0" class="empty-state">
        <svg class="empty-icon" viewBox="0 0 64 64" fill="none">
          <circle cx="32" cy="32" r="28" stroke="#E5E7EB" stroke-width="2"/>
          <path d="M32 20v24M20 32h24" stroke="#9CA3AF" stroke-width="2" stroke-linecap="round"/>
        </svg>
        <p class="empty-text">暂无申请记录</p>
        <el-button type="primary" @click="goToCreate" style="margin-top: 16px;">提交新申请</el-button>
      </div>

      <div v-else class="application-list">
        <div 
          v-for="item in tableData" 
          :key="item.id" 
          class="application-item"
          @click="viewDetail(item.id)"
        >
          <div class="application-header">
            <div class="application-title">
              <span class="type-badge" :class="`type-${item.type}`">{{ getTypeText(item.type) }}</span>
              <span class="application-id">#{{ item.id }}</span>
            </div>
            <div class="status-badge" :class="`status-${item.status}`">
              {{ getStatusText(item.status) }}
            </div>
          </div>

          <div class="application-body">
            <div class="info-row">
              <div class="info-item">
                <span class="info-label">审批进度</span>
                <span class="info-value">{{ getLevelText(item.approvalLevel) }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">提交时间</span>
                <span class="info-value">{{ item.createdAt }}</span>
              </div>
            </div>
          </div>

          <div class="application-footer">
            <el-button type="primary" text @click.stop="viewDetail(item.id)">
              查看详情
              <svg style="width: 14px; height: 14px; margin-left: 4px;" viewBox="0 0 20 20" fill="currentColor">
                <path fill-rule="evenodd" d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 011.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z" clip-rule="evenodd"/>
              </svg>
            </el-button>
            <el-button 
              v-if="item.status === 'PENDING'" 
              type="danger" 
              text 
              @click.stop="handleCancel(item.id)"
            >
              撤销申请
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMyApplications, cancelApplication } from '@/api/statusChange'

const router = useRouter()

const tableData = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

const pendingCount = computed(() => 
  tableData.value.filter(item => item.status === 'PENDING').length
)

const approvedCount = computed(() => 
  tableData.value.filter(item => item.status === 'APPROVED').length
)

const rejectedCount = computed(() => 
  tableData.value.filter(item => item.status === 'REJECTED').length
)

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

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getMyApplications()
    // 后端返回的是List，不是分页对象
    const data = Array.isArray(res.data) ? res.data : (res.data.content || [])
    tableData.value = data
    total.value = data.length
  } catch (error) {
    console.error('查询异动申请失败:', error)
    ElMessage.error('查询失败')
  } finally {
    loading.value = false
  }
}

const viewDetail = (id) => {
  router.push(`/student/status-changes/${id}`)
}

const goToCreate = () => {
  router.push('/student/status-changes/create')
}

const handleCancel = async (id) => {
  try {
    await ElMessageBox.confirm('确认撤销此申请？撤销后将无法恢复。', '确认撤销', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await cancelApplication(id)
    ElMessage.success('申请已撤销')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '撤销失败')
    }
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.my-applications {
  padding: 24px;
  max-width: 1400px;
  margin: 0 auto;
}

/* 页面标题 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
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

/* 统计卡片 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  background: #ffffff;
  border: 1px solid #e5e5e5;
  border-radius: 8px;
  padding: 20px;
  transition: all 0.2s ease;
}

.stat-card:hover {
  border-color: #d2d2d7;
  transform: translateY(-1px);
}

.stat-label {
  font-size: 13px;
  color: #86868b;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 32px;
  font-weight: 600;
  color: #1d1d1f;
  line-height: 1;
}

.stat-value.stat-warning {
  color: #f5a623;
}

.stat-value.stat-success {
  color: #34c759;
}

.stat-value.stat-danger {
  color: #ff3b30;
}

/* 申请列表容器 */
.applications-container {
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

/* 申请列表 */
.application-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.application-item {
  background: #fafafa;
  border: 1px solid #e5e5e5;
  border-radius: 8px;
  padding: 20px;
  transition: all 0.2s ease;
  cursor: pointer;
}

.application-item:hover {
  background: #f5f5f7;
  border-color: #d2d2d7;
}

.application-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.application-title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.type-badge {
  display: inline-flex;
  align-items: center;
  padding: 4px 12px;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 500;
  background: #f5f5f7;
  color: #1d1d1f;
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

.application-id {
  font-size: 13px;
  color: #86868b;
  font-weight: 500;
}

.status-badge {
  padding: 4px 12px;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 500;
}

.status-badge.status-PENDING {
  background: #fff3cd;
  color: #856404;
}

.status-badge.status-APPROVED {
  background: #d4edda;
  color: #155724;
}

.status-badge.status-REJECTED {
  background: #f8d7da;
  color: #721c24;
}

.status-badge.status-CANCELLED {
  background: #e2e3e5;
  color: #383d41;
}

.application-body {
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

.application-footer {
  display: flex;
  gap: 12px;
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
@media (max-width: 1200px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .info-row {
    grid-template-columns: 1fr;
  }
  
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
}
</style>

