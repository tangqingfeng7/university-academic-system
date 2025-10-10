<template>
  <div class="booking-list-container">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="queryParams">
        <el-form-item label="教室编号">
          <el-input
            v-model="queryParams.roomNo"
            placeholder="请输入教室编号"
            clearable
            style="width: 180px"
            @clear="handleSearch"
            @keyup.enter="handleSearch"
          />
        </el-form-item>

        <el-form-item label="状态">
          <el-select
            v-model="queryParams.status"
            placeholder="请选择状态"
            clearable
            style="width: 150px"
            @change="handleSearch"
          >
            <el-option label="待审批" value="PENDING" />
            <el-option label="已批准" value="APPROVED" />
            <el-option label="已拒绝" value="REJECTED" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
        </el-form-item>

        <el-form-item label="申请时间">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            style="width: 260px"
            @change="handleSearch"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 申请列表 -->
    <el-card style="margin-top: 20px">
      <template #header>
        <div class="card-header">
          <span class="card-title">我的申请列表</span>
          <el-button type="primary" @click="handleCreate">
            <el-icon><Plus /></el-icon>
            新建申请
          </el-button>
        </div>
      </template>

      <!-- 统计信息 -->
      <div class="statistics">
        <el-row :gutter="16">
          <el-col :span="6">
            <el-statistic title="全部申请" :value="statistics.total" />
          </el-col>
          <el-col :span="6">
            <el-statistic title="待审批" :value="statistics.pending">
              <template #suffix>
                <el-icon color="#E6A23C"><Clock /></el-icon>
              </template>
            </el-statistic>
          </el-col>
          <el-col :span="6">
            <el-statistic title="已批准" :value="statistics.approved">
              <template #suffix>
                <el-icon color="#67C23A"><Check /></el-icon>
              </template>
            </el-statistic>
          </el-col>
          <el-col :span="6">
            <el-statistic title="已拒绝" :value="statistics.rejected">
              <template #suffix>
                <el-icon color="#F56C6C"><Close /></el-icon>
              </template>
            </el-statistic>
          </el-col>
        </el-row>
      </div>

      <el-divider />

      <el-table
        v-loading="loading"
        :data="bookingList"
        stripe
        style="width: 100%"
      >
        <el-table-column type="index" label="序号" width="60" />
        
        <el-table-column label="教室" width="120">
          <template #default="{ row }">
            <div>{{ row.classroomNo || '-' }}</div>
            <div style="font-size: 12px; color: #909399">{{ row.classroom?.building || '-' }}</div>
          </template>
        </el-table-column>

        <el-table-column prop="startTime" label="借用时间" min-width="180">
          <template #default="{ row }">
            <div>
              <el-icon><Calendar /></el-icon>
              {{ formatDate(row.startTime) }}
            </div>
            <div style="font-size: 12px; color: #909399; margin-top: 4px">
              <el-icon><Clock /></el-icon>
              {{ formatTimeRange(row.startTime, row.endTime) }}
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="purpose" label="借用目的" min-width="180">
          <template #default="{ row }">
            <el-tooltip :content="row.purpose" placement="top" :disabled="row.purpose.length < 30">
              <span>{{ truncate(row.purpose, 30) }}</span>
            </el-tooltip>
          </template>
        </el-table-column>

        <el-table-column prop="expectedAttendees" label="预计人数" width="100" align="center">
          <template #default="{ row }">
            <el-tag type="info">{{ row.expectedAttendees }}人</el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusTag(row.status)">
              {{ getStatusName(row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="createdAt" label="申请时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              @click="handleViewDetail(row)"
            >
              <el-icon><View /></el-icon>
              详情
            </el-button>
            <el-button
              v-if="row.status === 'PENDING'"
              type="danger"
              size="small"
              @click="handleCancel(row)"
            >
              <el-icon><Close /></el-icon>
              取消
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 申请详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="申请详情"
      width="700px"
      @close="handleCloseDetailDialog"
    >
      <div v-if="currentBooking" v-loading="detailLoading">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="教室">
            {{ currentBooking.classroomName || currentBooking.classroomNo || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="容量">
            {{ currentBooking.classroom?.capacity || '-' }}人
          </el-descriptions-item>
          <el-descriptions-item label="借用目的" :span="2">
            {{ currentBooking.purpose }}
          </el-descriptions-item>
          <el-descriptions-item label="预计人数">
            {{ currentBooking.expectedAttendees }}人
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusTag(currentBooking.status)">
              {{ getStatusName(currentBooking.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="开始时间" :span="2">
            {{ formatDateTime(currentBooking.startTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="结束时间" :span="2">
            {{ formatDateTime(currentBooking.endTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="申请时间" :span="2">
            {{ formatDateTime(currentBooking.createdAt) }}
          </el-descriptions-item>
          <el-descriptions-item v-if="currentBooking.remarks" label="备注" :span="2">
            {{ currentBooking.remarks }}
          </el-descriptions-item>
          <el-descriptions-item
            v-if="currentBooking.status === 'APPROVED' || currentBooking.status === 'REJECTED'"
            label="审批人"
          >
            {{ currentBooking.approverName || '未知' }}
          </el-descriptions-item>
          <el-descriptions-item
            v-if="currentBooking.status === 'APPROVED' || currentBooking.status === 'REJECTED'"
            label="审批时间"
          >
            {{ formatDateTime(currentBooking.approvedAt) }}
          </el-descriptions-item>
          <el-descriptions-item
            v-if="currentBooking.approvalComment"
            label="审批意见"
            :span="2"
          >
            {{ currentBooking.approvalComment }}
          </el-descriptions-item>
        </el-descriptions>
      </div>

      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button
          v-if="currentBooking && currentBooking.status === 'PENDING'"
          type="danger"
          @click="handleCancelInDialog"
        >
          取消申请
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { getMyBookings, getBookingById, cancelBooking } from '@/api/classroom'

const router = useRouter()

// 查询参数
const queryParams = reactive({
  roomNo: '',
  status: '',
  startDate: '',
  endDate: ''
})

// 日期范围
const dateRange = ref([])

// 分页参数
const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

// 数据
const loading = ref(false)
const bookingList = ref([])

// 统计数据
const statistics = computed(() => {
  return {
    total: bookingList.value.length,
    pending: bookingList.value.filter(b => b.status === 'PENDING').length,
    approved: bookingList.value.filter(b => b.status === 'APPROVED').length,
    rejected: bookingList.value.filter(b => b.status === 'REJECTED').length
  }
})

// 详情对话框
const detailDialogVisible = ref(false)
const detailLoading = ref(false)
const currentBooking = ref(null)

// 获取申请列表
const fetchBookingList = async () => {
  loading.value = true
  try {
    const params = {
      ...queryParams,
      page: pagination.page - 1,
      size: pagination.size
    }
    
    // 处理日期范围
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = formatDate(dateRange.value[0])
      params.endDate = formatDate(dateRange.value[1])
    }
    
    const response = await getMyBookings(params)
    bookingList.value = response.data.content
    pagination.total = response.data.totalElements
  } catch (error) {
    ElMessage.error(error.message || '获取申请列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  fetchBookingList()
}

// 重置
const handleReset = () => {
  Object.keys(queryParams).forEach(key => {
    queryParams[key] = ''
  })
  dateRange.value = []
  handleSearch()
}

// 分页大小改变
const handleSizeChange = (size) => {
  pagination.size = size
  pagination.page = 1
  fetchBookingList()
}

// 页码改变
const handlePageChange = (page) => {
  pagination.page = page
  fetchBookingList()
}

// 新建申请
const handleCreate = () => {
  router.push('/teacher/classroom-bookings/create')
}

// 查看详情
const handleViewDetail = async (booking) => {
  currentBooking.value = booking
  detailDialogVisible.value = true
  
  // 获取完整详情
  detailLoading.value = true
  try {
    const response = await getBookingById(booking.id)
    currentBooking.value = response.data
  } catch (error) {
    ElMessage.error(error.message || '获取申请详情失败')
  } finally {
    detailLoading.value = false
  }
}

// 取消申请
const handleCancel = (booking) => {
  ElMessageBox.confirm(
    '确认取消该借用申请吗？取消后将无法恢复。',
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await cancelBooking(booking.id)
      ElMessage.success('申请已取消')
      fetchBookingList()
    } catch (error) {
      ElMessage.error(error.message || '取消申请失败')
    }
  }).catch(() => {
    // 取消操作
  })
}

// 在对话框中取消申请
const handleCancelInDialog = () => {
  handleCancel(currentBooking.value)
  detailDialogVisible.value = false
}

// 关闭详情对话框
const handleCloseDetailDialog = () => {
  currentBooking.value = null
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return ''
  const d = new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

// 格式化日期时间
const formatDateTime = (datetime) => {
  if (!datetime) return ''
  const d = new Date(datetime)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hour = String(d.getHours()).padStart(2, '0')
  const minute = String(d.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hour}:${minute}`
}

// 格式化时间范围
const formatTimeRange = (startTime, endTime) => {
  const start = new Date(startTime)
  const end = new Date(endTime)
  const startHour = String(start.getHours()).padStart(2, '0')
  const startMinute = String(start.getMinutes()).padStart(2, '0')
  const endHour = String(end.getHours()).padStart(2, '0')
  const endMinute = String(end.getMinutes()).padStart(2, '0')
  return `${startHour}:${startMinute} - ${endHour}:${endMinute}`
}

// 截断文本
const truncate = (text, length) => {
  if (!text) return ''
  return text.length > length ? text.substring(0, length) + '...' : text
}

// 获取状态标签
const getStatusTag = (status) => {
  const tags = {
    PENDING: 'warning',
    APPROVED: 'success',
    REJECTED: 'danger',
    CANCELLED: 'info'
  }
  return tags[status] || 'info'
}

// 获取状态名称
const getStatusName = (status) => {
  const names = {
    PENDING: '待审批',
    APPROVED: '已批准',
    REJECTED: '已拒绝',
    CANCELLED: '已取消'
  }
  return names[status] || status
}

onMounted(() => {
  fetchBookingList()
})
</script>

<style scoped>
.booking-list-container {
  padding: 20px;
}

.search-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 18px;
  font-weight: 600;
}

.statistics {
  padding: 20px 0;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

:deep(.el-statistic__head) {
  color: #909399;
  font-size: 14px;
}

:deep(.el-statistic__number) {
  font-size: 24px;
  font-weight: 600;
}
</style>

