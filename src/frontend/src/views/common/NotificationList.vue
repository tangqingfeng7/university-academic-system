<template>
  <div class="notification-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span class="card-title">
            <el-icon><Bell /></el-icon>
            通知公告
          </span>
        </div>
      </template>

      <!-- 通知列表 -->
      <el-table
        v-loading="loading"
        :data="notificationList"
        style="width: 100%"
        @row-click="handleRowClick"
      >
        <el-table-column prop="title" label="标题" min-width="200">
          <template #default="{ row }">
            <div class="title-cell">
              <el-badge :is-dot="!row.read" type="primary">
                <span :class="{ 'unread-title': !row.read }">{{ row.title }}</span>
              </el-badge>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="typeDescription" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getTypeTag(row.type)" size="small">
              {{ row.typeDescription }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="publisherName" label="发布人" width="120" />

        <el-table-column prop="publishTime" label="发布时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.publishTime) }}
          </template>
        </el-table-column>

        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.read" type="info" size="small">已读</el-tag>
            <el-tag v-else type="success" size="small">未读</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              @click.stop="handleView(row)"
            >
              查看
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 通知详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      :title="currentNotification?.title"
      width="60%"
      :before-close="handleClose"
    >
      <div v-if="currentNotification" class="notification-detail">
        <div class="detail-meta">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="类型">
              <el-tag :type="getTypeTag(currentNotification.type)" size="small">
                {{ currentNotification.typeDescription }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="发布人">
              {{ currentNotification.publisherName }}
            </el-descriptions-item>
            <el-descriptions-item label="发布时间">
              {{ formatDateTime(currentNotification.publishTime) }}
            </el-descriptions-item>
            <el-descriptions-item label="目标角色">
              {{ getRoleName(currentNotification.targetRole) }}
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <el-divider />

        <div class="detail-content" v-html="formatContent(currentNotification.content)"></div>
      </div>

      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Bell } from '@element-plus/icons-vue'
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
    ElMessage.error('获取通知列表失败: ' + (error.message || '未知错误'))
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
    ElMessage.error('获取通知详情失败: ' + (error.message || '未知错误'))
  }
}

// 行点击事件
const handleRowClick = (row) => {
  handleView(row)
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

// 格式化内容（保留换行）
const formatContent = (content) => {
  if (!content) return ''
  return content.replace(/\n/g, '<br>')
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

// 角色名称
const getRoleName = (role) => {
  const nameMap = {
    ALL: '全部',
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

<style scoped>
.notification-container {
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

.title-cell {
  cursor: pointer;
}

.unread-title {
  font-weight: bold;
  color: #303133;
}

:deep(.el-table__row) {
  cursor: pointer;
}

:deep(.el-table__row:hover) {
  background-color: #f5f7fa;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.notification-detail {
  padding: 10px 0;
}

.detail-meta {
  margin-bottom: 20px;
}

.detail-content {
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 4px;
  line-height: 1.8;
  color: #606266;
  min-height: 200px;
  max-height: 500px;
  overflow-y: auto;
}
</style>

