<template>
  <el-dropdown trigger="click" @command="handleCommand">
    <div class="notification-bell">
      <el-badge :value="unreadCount" :max="99" :hidden="unreadCount === 0">
        <el-icon :size="20"><Bell /></el-icon>
      </el-badge>
    </div>
    <template #dropdown>
      <el-dropdown-menu class="notification-dropdown">
        <div class="dropdown-header">
          <span>通知消息</span>
          <el-link type="primary" @click="goToNotificationList">查看全部</el-link>
        </div>
        <el-divider style="margin: 5px 0" />
        
        <div v-if="loading" class="dropdown-loading">
          <el-icon class="is-loading"><Loading /></el-icon>
          <span>加载中...</span>
        </div>

        <div v-else-if="notifications.length === 0" class="dropdown-empty">
          <el-icon><DocumentRemove /></el-icon>
          <span>暂无通知</span>
        </div>

        <template v-else>
          <el-dropdown-item
            v-for="notification in notifications"
            :key="notification.id"
            :command="notification.id"
            :divided="true"
          >
            <div class="notification-item">
              <div class="item-header">
                <el-badge :is-dot="!notification.read" type="primary">
                  <span class="item-title" :class="{ 'unread': !notification.read }">
                    {{ notification.title }}
                  </span>
                </el-badge>
                <el-tag :type="getTypeTag(notification.type)" size="small">
                  {{ notification.typeDescription }}
                </el-tag>
              </div>
              <div class="item-time">{{ formatTime(notification.publishTime) }}</div>
            </div>
          </el-dropdown-item>
        </template>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Bell, Loading, DocumentRemove } from '@element-plus/icons-vue'
import { getUnreadCount, getLatestNotifications, markAsRead } from '@/api/notification'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

// 数据
const loading = ref(false)
const unreadCount = ref(0)
const notifications = ref([])

// 获取未读数量
const fetchUnreadCount = async () => {
  try {
    const res = await getUnreadCount()
    unreadCount.value = res.data.count || 0
  } catch (error) {
    console.error('获取未读数量失败:', error)
  }
}

// 获取最新通知
const fetchLatestNotifications = async () => {
  loading.value = true
  try {
    const res = await getLatestNotifications(5)
    notifications.value = res.data || []
  } catch (error) {
    console.error('获取最新通知失败:', error)
  } finally {
    loading.value = false
  }
}

// 点击通知
const handleCommand = async (notificationId) => {
  const notification = notifications.value.find(n => n.id === notificationId)
  if (!notification) return

  // 如果未读，标记为已读
  if (!notification.read) {
    try {
      await markAsRead(notificationId)
      notification.read = true
      unreadCount.value = Math.max(0, unreadCount.value - 1)
    } catch (error) {
      console.error('标记已读失败:', error)
    }
  }

  // 跳转到通知列表页面
  goToNotificationList()
}

// 跳转到通知列表
const goToNotificationList = () => {
  const role = userStore.userInfo?.role?.toLowerCase()
  if (role === 'admin') {
    router.push('/admin/notifications')
  } else if (role === 'teacher') {
    router.push('/teacher/notifications')
  } else if (role === 'student') {
    router.push('/student/notifications')
  }
}

// 格式化时间
const formatTime = (dateTime) => {
  if (!dateTime) return ''
  
  const date = new Date(dateTime)
  const now = new Date()
  const diff = now - date
  
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)
  
  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${month}-${day}`
}

// 类型标签
const getTypeTag = (type) => {
  const tagMap = {
    SYSTEM: 'danger',
    COURSE: 'primary',
    GRADE: 'warning'
  }
  return tagMap[type] || 'info'
}

// 初始化
const init = () => {
  fetchUnreadCount()
  fetchLatestNotifications()
}

// 页面加载
onMounted(() => {
  init()
  
  // 每分钟刷新一次
  const timer = setInterval(() => {
    fetchUnreadCount()
  }, 60000)
  
  // 组件卸载时清除定时器
  return () => clearInterval(timer)
})

// 暴露刷新方法
defineExpose({
  refresh: init
})
</script>

<style scoped>
.notification-bell {
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  padding: 0 12px;
  height: 100%;
}

.notification-bell:hover {
  background-color: rgba(0, 0, 0, 0.05);
}

.notification-dropdown {
  width: 360px;
  max-height: 480px;
}

.dropdown-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px 8px;
  font-weight: bold;
  color: #303133;
}

.dropdown-loading,
.dropdown-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  color: #909399;
}

.dropdown-loading .el-icon {
  font-size: 24px;
  margin-bottom: 8px;
}

.dropdown-empty .el-icon {
  font-size: 48px;
  margin-bottom: 12px;
  color: #dcdfe6;
}

.notification-item {
  padding: 4px 0;
  width: 100%;
}

.item-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 6px;
}

.item-title {
  flex: 1;
  margin-right: 10px;
  font-size: 14px;
  color: #606266;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 240px;
}

.item-title.unread {
  font-weight: bold;
  color: #303133;
}

.item-time {
  font-size: 12px;
  color: #909399;
}

:deep(.el-dropdown-menu__item) {
  padding: 8px 16px;
  line-height: 1.5;
}

:deep(.el-dropdown-menu__item:hover) {
  background-color: #f5f7fa;
}
</style>

