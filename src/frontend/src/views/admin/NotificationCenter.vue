<template>
  <div class="notification-management">
    <!-- 页面头部 -->
    <div class="page-header animate-fade-in-down">
      <div class="header-content">
        <h1 class="page-title">通知</h1>
        <p class="page-subtitle">发布和管理系统通知</p>
      </div>
      <el-button 
        type="primary" 
        :icon="Plus" 
        size="large"
        @click="showPublishDialog"
      >
        发布通知
      </el-button>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar animate-fade-in-up" style="animation-delay: 0.1s;">
      <div class="filter-item">
        <el-select 
          v-model="filters.type" 
          placeholder="所有类型" 
          clearable
          @change="fetchNotifications"
        >
          <el-option label="系统通知" value="SYSTEM" />
          <el-option label="课程通知" value="COURSE" />
          <el-option label="成绩通知" value="GRADE" />
        </el-select>
      </div>
      <div class="filter-item">
        <el-select 
          v-model="filters.targetRole" 
          placeholder="所有角色" 
          clearable
          @change="fetchNotifications"
        >
          <el-option label="全部用户" value="ALL" />
          <el-option label="教师" value="TEACHER" />
          <el-option label="学生" value="STUDENT" />
        </el-select>
      </div>
      <div class="filter-item">
        <el-select 
          v-model="filters.active" 
          placeholder="所有状态" 
          clearable
          @change="fetchNotifications"
        >
          <el-option label="生效中" :value="true" />
          <el-option label="已停用" :value="false" />
        </el-select>
      </div>
    </div>

    <!-- 通知列表 -->
    <div v-loading="loading" class="notification-list">
      <div
        v-for="(notification, index) in notificationList"
        :key="notification.id"
        class="notification-card animate-fade-in-up"
        :style="{ 'animation-delay': `${0.2 + index * 0.05}s` }"
        @click="handleView(notification)"
      >
        <div class="notification-header">
          <div class="notification-meta">
            <el-tag 
              :type="getTypeStyle(notification.type)" 
              size="small"
            >
              {{ notification.typeDescription }}
            </el-tag>
            <span class="meta-divider">·</span>
            <span class="meta-text">{{ getRoleName(notification.targetRole) }}</span>
            <span class="meta-divider">·</span>
            <el-tag 
              :type="notification.active ? 'success' : 'info'" 
              size="small"
            >
              {{ notification.active ? '生效中' : '已停用' }}
            </el-tag>
          </div>
        </div>

        <h3 class="notification-title">{{ notification.title }}</h3>

        <div class="notification-info">
          <span class="info-item">
            <el-icon><User /></el-icon>
            {{ notification.publisherName }}
          </span>
          <span class="info-item">
            <el-icon><Clock /></el-icon>
            {{ formatDateTime(notification.publishTime) }}
          </span>
        </div>

        <div class="notification-actions" @click.stop>
          <el-button
            type="primary"
            size="default"
            plain
            @click="handleView(notification)"
          >
            查看详情
          </el-button>
          <el-button
            v-if="notification.active"
            type="danger"
            size="default"
            plain
            @click="handleDeactivate(notification)"
          >
            停用
          </el-button>
        </div>
      </div>

      <!-- 空状态 -->
      <el-empty
        v-if="!loading && notificationList.length === 0"
        description="还没有发布任何通知"
        :image-size="160"
      >
        <el-button type="primary" :icon="Plus" @click="showPublishDialog">
          发布第一条通知
        </el-button>
      </el-empty>
    </div>

    <!-- 分页 -->
    <div v-if="pagination.total > 0" class="pagination-wrapper">
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :page-sizes="[10, 20, 50]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 通知详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      :title="currentNotification?.title"
      width="700px"
    >
      <div v-if="currentNotification" class="notification-detail">
        <div class="detail-header">
          <div class="detail-tags">
            <el-tag 
              :type="getTypeStyle(currentNotification.type)" 
              size="large"
            >
              {{ currentNotification.typeDescription }}
            </el-tag>
            <el-tag 
              :type="currentNotification.active ? 'success' : 'info'" 
              size="large"
            >
              {{ currentNotification.active ? '生效中' : '已停用' }}
            </el-tag>
          </div>
          
          <div class="detail-meta">
            <div class="meta-row">
              <span class="meta-label">发布人</span>
              <span class="meta-value">{{ currentNotification.publisherName }}</span>
            </div>
            <div class="meta-row">
              <span class="meta-label">发布时间</span>
              <span class="meta-value">{{ formatDateTime(currentNotification.publishTime) }}</span>
            </div>
            <div class="meta-row">
              <span class="meta-label">目标角色</span>
              <span class="meta-value">{{ getRoleName(currentNotification.targetRole) }}</span>
            </div>
          </div>
        </div>

        <div class="detail-divider"></div>

        <div class="detail-content" v-html="formatContent(currentNotification.content)"></div>
      </div>

      <template #footer>
        <el-button size="large" @click="detailDialogVisible = false">
          关闭
        </el-button>
      </template>
    </el-dialog>

    <!-- 发布通知对话框 -->
    <NotificationForm 
      v-model="publishDialogVisible" 
      @success="handlePublishSuccess"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, User, Clock } from '@element-plus/icons-vue'
import { 
  getAllNotifications,
  getNotificationDetail,
  deactivateNotification 
} from '@/api/notification'
import NotificationForm from '@/components/NotificationForm.vue'

// 数据
const loading = ref(false)
const notificationList = ref([])
const detailDialogVisible = ref(false)
const publishDialogVisible = ref(false)
const currentNotification = ref(null)

// 筛选条件
const filters = reactive({
  type: null,
  targetRole: null,
  active: null
})

// 分页
const pagination = ref({
  page: 1,
  size: 20,
  total: 0
})

// 获取通知列表
const fetchNotifications = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.value.page - 1,
      size: pagination.value.size,
      ...Object.fromEntries(
        Object.entries(filters).filter(([_, value]) => value != null)
      )
    }
    
    const res = await getAllNotifications(params)
    notificationList.value = res.data.content || []
    pagination.value.total = res.data.totalElements || 0
  } catch (error) {
    console.error('获取通知列表失败:', error)
    ElMessage.error('获取通知列表失败')
  } finally {
    loading.value = false
  }
}

// 显示发布对话框
const showPublishDialog = () => {
  publishDialogVisible.value = true
}

// 发布成功
const handlePublishSuccess = () => {
  fetchNotifications()
}

// 查看通知详情
const handleView = async (row) => {
  try {
    const res = await getNotificationDetail(row.id)
    currentNotification.value = res.data
    detailDialogVisible.value = true
  } catch (error) {
    console.error('获取通知详情失败:', error)
    ElMessage.error('获取通知详情失败')
  }
}

// 停用通知
const handleDeactivate = async (row) => {
  try {
    await ElMessageBox.confirm(
      '确定要停用该通知吗？停用后将不再向用户展示。',
      '确认停用',
      {
        confirmButtonText: '停用',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await deactivateNotification(row.id)
    ElMessage.success('通知已停用')
    fetchNotifications()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('停用通知失败:', error)
      ElMessage.error('停用通知失败')
    }
  }
}

// 分页事件
const handleSizeChange = (size) => {
  pagination.value.size = size
  pagination.value.page = 1
  fetchNotifications()
}

const handleCurrentChange = (page) => {
  pagination.value.page = page
  fetchNotifications()
}

// 格式化时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  const date = new Date(dateTime)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}`
}

// 格式化内容
const formatContent = (content) => {
  if (!content) return ''
  return content.replace(/\n/g, '<br>')
}

// 类型样式
const getTypeStyle = (type) => {
  const styleMap = {
    SYSTEM: 'danger',
    COURSE: 'primary',
    GRADE: 'warning'
  }
  return styleMap[type] || 'info'
}

// 角色名称
const getRoleName = (role) => {
  const nameMap = {
    ALL: '全部用户',
    ADMIN: '管理员',
    TEACHER: '教师',
    STUDENT: '学生'
  }
  return nameMap[role] || role
}

// 页面加载
onMounted(() => {
  fetchNotifications()
})
</script>

<style scoped lang="scss">
.notification-management {
  max-width: 1200px;
  margin: 0 auto;
  padding: var(--spacing-3xl) var(--spacing-xl);

  // ===================================
  // 页面头部
  // ===================================

  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: var(--spacing-2xl);
  }

  .header-content {
    flex: 1;
  }

  .page-title {
    font-size: 40px;
    font-weight: 600;
    color: var(--text-primary);
    margin: 0 0 8px;
    letter-spacing: -0.03em;
  }

  .page-subtitle {
    font-size: 17px;
    color: var(--text-secondary);
    margin: 0;
    font-weight: 400;
  }

  // ===================================
  // 筛选栏
  // ===================================

  .filter-bar {
    display: flex;
    gap: 12px;
    margin-bottom: var(--spacing-lg);

    @media (max-width: 768px) {
      flex-wrap: wrap;
    }
  }

  .filter-item {
    min-width: 160px;

    @media (max-width: 768px) {
      flex: 1;
      min-width: 120px;
    }
  }

  // ===================================
  // 通知列表
  // ===================================

  .notification-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
    margin-bottom: var(--spacing-xl);
    min-height: 300px;
  }

  .notification-card {
    background: var(--bg-primary);
    border-radius: var(--radius-lg);
    padding: 24px;
    border: 1px solid var(--border-light);
    cursor: pointer;
    transition: all var(--transition-base);

    &:hover {
      box-shadow: var(--shadow-card);
      border-color: var(--border-color);
    }
  }

  .notification-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
  }

  .notification-meta {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 14px;
  }

  .meta-divider {
    color: var(--text-tertiary);
  }

  .meta-text {
    color: var(--text-secondary);
    font-weight: 500;
  }

  .notification-title {
    font-size: 19px;
    font-weight: 600;
    color: var(--text-primary);
    margin: 0 0 16px;
    letter-spacing: -0.01em;
  }

  .notification-info {
    display: flex;
    gap: 20px;
    margin-bottom: 16px;
  }

  .info-item {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 14px;
    color: var(--text-secondary);

    .el-icon {
      font-size: 16px;
    }
  }

  .notification-actions {
    display: flex;
    gap: 8px;
    padding-top: 16px;
    border-top: 1px solid var(--border-light);
  }

  // ===================================
  // 分页
  // ===================================

  .pagination-wrapper {
    display: flex;
    justify-content: center;
    margin-top: var(--spacing-xl);
  }

  // ===================================
  // 通知详情
  // ===================================

  .notification-detail {
    padding: var(--spacing-md) 0;
  }

  .detail-header {
    margin-bottom: var(--spacing-lg);
  }

  .detail-tags {
    display: flex;
    gap: 8px;
    margin-bottom: var(--spacing-lg);
  }

  .detail-meta {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  .meta-row {
    display: flex;
    align-items: center;
  }

  .meta-label {
    font-size: 14px;
    color: var(--text-tertiary);
    min-width: 80px;
  }

  .meta-value {
    font-size: 15px;
    color: var(--text-primary);
    font-weight: 500;
  }

  .detail-divider {
    height: 1px;
    background: var(--border-light);
    margin: var(--spacing-xl) 0;
  }

  .detail-content {
    padding: var(--spacing-lg);
    background: var(--bg-secondary);
    border-radius: var(--radius-md);
    line-height: 1.8;
    color: var(--text-primary);
    font-size: 15px;
    min-height: 200px;
    max-height: 500px;
    overflow-y: auto;

    :deep(br) {
      display: block;
      margin: 8px 0;
    }
  }
}
</style>
