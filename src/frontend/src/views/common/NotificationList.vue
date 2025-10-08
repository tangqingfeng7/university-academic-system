<template>
  <div class="notification-list-container">
    <!-- 通知列表 -->
    <div v-loading="loading" class="notification-list">
      <div
        v-for="(notification, index) in notificationList"
        :key="notification.id"
        class="notification-card animate-fade-in-up"
        :style="{ 'animation-delay': `${index * 0.05}s` }"
        :class="{ 'unread': !notification.read }"
        @click="handleView(notification)"
      >
        <div class="notification-header">
          <div class="notification-meta">
            <el-badge 
              :is-dot="!notification.read" 
              type="primary"
              :offset="[4, 0]"
            >
              <el-tag 
                :type="getTypeStyle(notification.type)" 
                size="small"
              >
                {{ notification.typeDescription }}
              </el-tag>
            </el-badge>
          </div>
          <el-tag 
            v-if="notification.read"
            type="info" 
            size="small"
          >
            已读
          </el-tag>
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
      </div>

      <!-- 空状态 -->
      <el-empty
        v-if="!loading && notificationList.length === 0"
        description="暂无通知"
        :image-size="160"
      />
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
      @close="handleClose"
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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { User, Clock } from '@element-plus/icons-vue'
import { 
  getMyNotifications, 
  getNotificationDetail, 
  markAsRead 
} from '@/api/notification'

// 数据
const loading = ref(false)
const notificationList = ref([])
const detailDialogVisible = ref(false)
const currentNotification = ref(null)

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
      size: pagination.value.size
    }
    
    const res = await getMyNotifications(params)
    notificationList.value = res.data.content || []
    pagination.value.total = res.data.totalElements || 0
  } catch (error) {
    console.error('获取通知列表失败:', error)
    ElMessage.error('获取通知列表失败')
  } finally {
    loading.value = false
  }
}

// 查看通知详情
const handleView = async (row) => {
  try {
    const res = await getNotificationDetail(row.id)
    currentNotification.value = res.data
    detailDialogVisible.value = true
    
    // 如果未读，标记为已读
    if (!row.read) {
      await markAsRead(row.id)
      row.read = true
    }
  } catch (error) {
    console.error('获取通知详情失败:', error)
    ElMessage.error('获取通知详情失败')
  }
}

// 对话框关闭
const handleClose = () => {
  detailDialogVisible.value = false
  currentNotification.value = null
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

// 暴露方法给父组件
defineExpose({
  fetchNotifications
})
</script>

<style scoped lang="scss">
.notification-list-container {
  padding: var(--spacing-lg) 0;

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

    &.unread {
      border-color: var(--primary-lighter);
      background: linear-gradient(
        to right,
        rgba(0, 122, 255, 0.02) 0%,
        transparent 100%
      );
    }

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

  .notification-title {
    font-size: 17px;
    font-weight: 600;
    color: var(--text-primary);
    margin: 0 0 16px;
    letter-spacing: -0.01em;
  }

  .notification-info {
    display: flex;
    gap: 20px;
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
