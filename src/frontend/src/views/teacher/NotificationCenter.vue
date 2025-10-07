<template>
  <div class="notification-center">
    <el-card>
      <template #header>
        <div class="card-header">
          <span class="card-title">
            <el-icon><Bell /></el-icon>
            通知管理
          </span>
          <el-button type="primary" @click="publishDialogVisible = true">
            <el-icon><Plus /></el-icon>
            发布课程通知
          </el-button>
        </div>
      </template>

      <!-- 复用通用通知列表组件 -->
      <CommonNotificationList ref="notificationListRef" />
    </el-card>

    <!-- 发布通知对话框 -->
    <NotificationForm 
      v-model="publishDialogVisible" 
      @success="handlePublishSuccess"
    />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { Bell, Plus } from '@element-plus/icons-vue'
import CommonNotificationList from '@/views/common/NotificationList.vue'
import NotificationForm from '@/components/NotificationForm.vue'

const publishDialogVisible = ref(false)
const notificationListRef = ref(null)

// 发布成功后刷新列表
const handlePublishSuccess = () => {
  // 通过ref调用子组件的刷新方法
  if (notificationListRef.value && notificationListRef.value.fetchNotifications) {
    notificationListRef.value.fetchNotifications()
  }
}
</script>

<style scoped>
.notification-center {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  display: flex;
  align-items: center;
  font-size: 18px;
  font-weight: bold;
  color: #303133;
}

.card-title .el-icon {
  margin-right: 8px;
  font-size: 20px;
  color: #409EFF;
}
</style>

