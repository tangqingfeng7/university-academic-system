<template>
  <div class="notification-center">
    <el-card>
      <template #header>
        <div class="card-header">
          <span class="card-title">
            <el-icon><Bell /></el-icon>
            通知管理
          </span>
          <el-button type="primary" @click="showPublishDialog">
            <el-icon><Plus /></el-icon>
            发布通知
          </el-button>
        </div>
      </template>

      <!-- 通知列表 -->
      <el-table
        v-loading="loading"
        :data="notificationList"
        style="width: 100%"
      >
        <el-table-column prop="title" label="标题" min-width="200" />

        <el-table-column prop="typeDescription" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getTypeTag(row.type)" size="small">
              {{ row.typeDescription }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="targetRole" label="目标角色" width="100">
          <template #default="{ row }">
            {{ getRoleName(row.targetRole) }}
          </template>
        </el-table-column>

        <el-table-column prop="publisherName" label="发布人" width="120" />

        <el-table-column prop="publishTime" label="发布时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.publishTime) }}
          </template>
        </el-table-column>

        <el-table-column prop="active" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.active ? 'success' : 'info'" size="small">
              {{ row.active ? '生效中' : '已停用' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              @click="handleView(row)"
            >
              查看
            </el-button>
            <el-button
              v-if="row.active"
              type="danger"
              size="small"
              @click="handleDeactivate(row)"
            >
              停用
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

    <!-- 发布通知对话框 -->
    <NotificationForm 
      v-model="publishDialogVisible" 
      @success="handlePublishSuccess"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Bell, Plus } from '@element-plus/icons-vue'
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
    
    const res = await getAllNotifications(params)
    notificationList.value = res.data.content || []
    pagination.value.total = res.data.totalElements || 0
  } catch (error) {
    console.error('获取通知列表失败:', error)
    ElMessage.error('获取通知列表失败: ' + (error.message || '未知错误'))
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
    ElMessage.error('获取通知详情失败: ' + (error.message || '未知错误'))
  }
}

// 停用通知
const handleDeactivate = async (row) => {
  try {
    await ElMessageBox.confirm(
      '确定要停用该通知吗？停用后将不再向用户展示。',
      '确认停用',
      {
        confirmButtonText: '确定',
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
      ElMessage.error('停用通知失败: ' + (error.message || '未知错误'))
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

