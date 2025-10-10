<template>
  <div class="classroom-list-container">
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

        <el-form-item label="楼栋">
          <el-input
            v-model="queryParams.building"
            placeholder="请输入楼栋"
            clearable
            style="width: 150px"
            @clear="handleSearch"
            @keyup.enter="handleSearch"
          />
        </el-form-item>

        <el-form-item label="教室类型">
          <el-select
            v-model="queryParams.type"
            placeholder="请选择类型"
            clearable
            style="width: 150px"
            @change="handleSearch"
          >
            <el-option label="普通教室" value="LECTURE" />
            <el-option label="实验室" value="LAB" />
            <el-option label="多媒体教室" value="MULTIMEDIA" />
            <el-option label="会议室" value="CONFERENCE" />
          </el-select>
        </el-form-item>

        <el-form-item label="最小容量">
          <el-input-number
            v-model="queryParams.minCapacity"
            :min="0"
            :max="500"
            placeholder="最小容量"
            style="width: 120px"
          />
        </el-form-item>

        <el-form-item label="最大容量">
          <el-input-number
            v-model="queryParams.maxCapacity"
            :min="0"
            :max="500"
            placeholder="最大容量"
            style="width: 120px"
          />
        </el-form-item>

        <el-form-item label="状态">
          <el-select
            v-model="queryParams.status"
            placeholder="请选择状态"
            clearable
            style="width: 120px"
            @change="handleSearch"
          >
            <el-option label="可用" value="AVAILABLE" />
            <el-option label="维修中" value="MAINTENANCE" />
            <el-option label="停用" value="DISABLED" />
          </el-select>
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

    <!-- 教室列表 -->
    <el-card style="margin-top: 20px">
      <template #header>
        <div class="card-header">
          <span class="card-title">教室列表</span>
          <el-button type="primary" @click="handleApplyBooking">
            <el-icon><Plus /></el-icon>
            申请借用教室
          </el-button>
        </div>
      </template>

      <el-table
        v-loading="loading"
        :data="classroomList"
        stripe
        style="width: 100%"
      >
        <el-table-column type="index" label="序号" width="60" />
        
        <el-table-column prop="roomNo" label="教室编号" width="120" />
        
        <el-table-column prop="building" label="楼栋" width="120" />
        
        <el-table-column prop="capacity" label="容量" width="80" align="center">
          <template #default="{ row }">
            <el-tag type="info">{{ row.capacity }}人</el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="type" label="教室类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getTypeTag(row.type)">
              {{ getTypeName(row.type) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="equipment" label="设备信息" min-width="200">
          <template #default="{ row }">
            <span v-if="row.equipment">{{ formatEquipment(row.equipment) }}</span>
            <span v-else class="text-muted">无特殊设备</span>
          </template>
        </el-table-column>

        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusTag(row.status)">
              {{ getStatusName(row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              @click="handleViewSchedule(row)"
            >
              <el-icon><Calendar /></el-icon>
              查看课表
            </el-button>
            <el-button
              type="success"
              size="small"
              :disabled="row.status !== 'AVAILABLE'"
              @click="handleBookClassroom(row)"
            >
              <el-icon><Document /></el-icon>
              申请借用
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

    <!-- 教室课表对话框 -->
    <el-dialog
      v-model="scheduleDialogVisible"
      title="教室课表"
      width="80%"
      @close="handleCloseScheduleDialog"
    >
      <div v-loading="scheduleLoading">
        <el-form :inline="true" style="margin-bottom: 20px">
          <el-form-item label="选择周">
            <el-date-picker
              v-model="selectedWeekStart"
              type="week"
              format="YYYY年 第ww周"
              placeholder="选择周"
              @change="handleWeekChange"
              style="width: 250px"
            />
          </el-form-item>
        </el-form>

        <div v-if="currentClassroom" class="schedule-info">
          <el-descriptions :column="4" border>
            <el-descriptions-item label="教室编号">
              {{ currentClassroom.roomNo }}
            </el-descriptions-item>
            <el-descriptions-item label="楼栋">
              {{ currentClassroom.building }}
            </el-descriptions-item>
            <el-descriptions-item label="容量">
              {{ currentClassroom.capacity }}人
            </el-descriptions-item>
            <el-descriptions-item label="类型">
              <el-tag :type="getTypeTag(currentClassroom.type)">
                {{ getTypeName(currentClassroom.type) }}
              </el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 课表表格 -->
        <el-table
          :data="scheduleData"
          border
          style="margin-top: 20px; width: 100%"
          :header-cell-style="{ background: '#f5f7fa' }"
        >
          <el-table-column prop="timeSlot" label="时间段" width="120" fixed />
          <el-table-column
            v-for="day in weekDays"
            :key="day.value"
            :label="day.label"
            min-width="150"
          >
            <template #default="{ row }">
              <div v-if="row[day.value]" class="schedule-cell">
                <div class="usage-item" v-for="(item, index) in row[day.value]" :key="index">
                  <el-tag :type="getUsageTypeTag(item.type)" size="small" style="width: 100%; margin-bottom: 4px">
                    {{ getUsageTypeName(item.type) }}
                  </el-tag>
                  <div class="usage-title">{{ item.title }}</div>
                  <div class="usage-time">{{ item.timeRange }}</div>
                </div>
              </div>
              <span v-else class="text-muted">空闲</span>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <template #footer>
        <el-button @click="scheduleDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { getClassroomList, getClassroomSchedule } from '@/api/classroom'

const router = useRouter()

// 查询参数
const queryParams = reactive({
  roomNo: '',
  building: '',
  type: '',
  minCapacity: null,
  maxCapacity: null,
  status: 'AVAILABLE'
})

// 分页参数
const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

// 数据
const loading = ref(false)
const classroomList = ref([])

// 课表对话框
const scheduleDialogVisible = ref(false)
const scheduleLoading = ref(false)
const currentClassroom = ref(null)
const selectedWeekStart = ref(null)
const scheduleData = ref([])

// 星期配置
const weekDays = [
  { label: '周一', value: 'monday' },
  { label: '周二', value: 'tuesday' },
  { label: '周三', value: 'wednesday' },
  { label: '周四', value: 'thursday' },
  { label: '周五', value: 'friday' },
  { label: '周六', value: 'saturday' },
  { label: '周日', value: 'sunday' }
]

// 获取教室列表
const fetchClassroomList = async () => {
  loading.value = true
  try {
    const params = {
      roomNo: queryParams.roomNo || undefined,
      building: queryParams.building || undefined,
      type: queryParams.type || undefined,
      minCapacity: queryParams.minCapacity || undefined,
      maxCapacity: queryParams.maxCapacity || undefined,
      status: queryParams.status || undefined,
      page: pagination.page - 1,
      size: pagination.size
    }
    const response = await getClassroomList(params)
    classroomList.value = response.data.content
    pagination.total = response.data.totalElements
  } catch (error) {
    ElMessage.error(error.message || '获取教室列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  fetchClassroomList()
}

// 重置
const handleReset = () => {
  Object.keys(queryParams).forEach(key => {
    if (key === 'status') {
      queryParams[key] = 'AVAILABLE'
    } else if (key === 'minCapacity' || key === 'maxCapacity') {
      queryParams[key] = null
    } else {
      queryParams[key] = ''
    }
  })
  handleSearch()
}

// 分页大小改变
const handleSizeChange = (size) => {
  pagination.size = size
  pagination.page = 1
  fetchClassroomList()
}

// 页码改变
const handlePageChange = (page) => {
  pagination.page = page
  fetchClassroomList()
}

// 跳转到借用申请页
const handleApplyBooking = () => {
  router.push('/teacher/classroom-bookings/create')
}

// 申请借用教室
const handleBookClassroom = (classroom) => {
  router.push({
    path: '/teacher/classroom-bookings/create',
    query: { classroomId: classroom.id }
  })
}

// 查看教室课表
const handleViewSchedule = async (classroom) => {
  currentClassroom.value = classroom
  scheduleDialogVisible.value = true
  
  // 默认显示本周
  const now = new Date()
  const day = now.getDay()
  const monday = new Date(now)
  monday.setDate(now.getDate() - (day === 0 ? 6 : day - 1))
  selectedWeekStart.value = monday
  
  await loadSchedule()
}

// 周改变
const handleWeekChange = () => {
  loadSchedule()
}

// 加载课表
const loadSchedule = async () => {
  if (!currentClassroom.value || !selectedWeekStart.value) return
  
  scheduleLoading.value = true
  try {
    const startDate = formatDate(selectedWeekStart.value)
    const response = await getClassroomSchedule(currentClassroom.value.id, startDate)
    
    // 转换课表数据
    scheduleData.value = transformScheduleData(response.data)
  } catch (error) {
    ElMessage.error(error.message || '获取教室课表失败')
  } finally {
    scheduleLoading.value = false
  }
}

// 转换课表数据
const transformScheduleData = (data) => {
  const timeSlots = [
    { slot: '第1-2节', start: '08:00', end: '09:40' },
    { slot: '第3-4节', start: '10:00', end: '11:40' },
    { slot: '第5-6节', start: '14:00', end: '15:40' },
    { slot: '第7-8节', start: '16:00', end: '17:40' },
    { slot: '第9-10节', start: '19:00', end: '20:40' }
  ]
  
  return timeSlots.map(slot => {
    const row = { timeSlot: `${slot.slot}\n${slot.start}-${slot.end}` }
    weekDays.forEach(day => {
      row[day.value] = []
    })
    
    // 填充课表数据
    data.days?.forEach(dayData => {
      const dayKey = weekDays[dayData.dayOfWeek - 1]?.value
      if (dayKey) {
        dayData.usages?.forEach(usage => {
          // 判断使用记录是否在当前时间段
          if (isInTimeSlot(usage.startTime, usage.endTime, slot.start, slot.end)) {
            row[dayKey].push({
              type: usage.type,
              title: usage.title,
              timeRange: `${usage.startTime}-${usage.endTime}`
            })
          }
        })
      }
    })
    
    return row
  })
}

// 判断时间是否在时间段内
const isInTimeSlot = (startTime, endTime, slotStart, slotEnd) => {
  return startTime < slotEnd && endTime > slotStart
}

// 关闭课表对话框
const handleCloseScheduleDialog = () => {
  currentClassroom.value = null
  selectedWeekStart.value = null
  scheduleData.value = []
}

// 格式化日期
const formatDate = (date) => {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

// 格式化设备信息
const formatEquipment = (equipment) => {
  if (!equipment) return '无'
  try {
    const equipmentObj = JSON.parse(equipment)
    return Object.entries(equipmentObj)
      .filter(([, value]) => value)
      .map(([key]) => key)
      .join('、')
  } catch {
    return equipment
  }
}

// 获取类型标签
const getTypeTag = (type) => {
  const tags = {
    LECTURE: 'info',
    LAB: 'warning',
    MULTIMEDIA: 'success',
    CONFERENCE: 'danger'
  }
  return tags[type] || 'info'
}

// 获取类型名称
const getTypeName = (type) => {
  const names = {
    LECTURE: '普通教室',
    LAB: '实验室',
    MULTIMEDIA: '多媒体教室',
    CONFERENCE: '会议室'
  }
  return names[type] || type
}

// 获取状态标签
const getStatusTag = (status) => {
  const tags = {
    AVAILABLE: 'success',
    MAINTENANCE: 'warning',
    DISABLED: 'danger'
  }
  return tags[status] || 'info'
}

// 获取状态名称
const getStatusName = (status) => {
  const names = {
    AVAILABLE: '可用',
    MAINTENANCE: '维修中',
    DISABLED: '停用'
  }
  return names[status] || status
}

// 获取使用类型标签
const getUsageTypeTag = (type) => {
  const tags = {
    COURSE: 'primary',
    EXAM: 'danger',
    BOOKING: 'success',
    OTHER: 'info'
  }
  return tags[type] || 'info'
}

// 获取使用类型名称
const getUsageTypeName = (type) => {
  const names = {
    COURSE: '上课',
    EXAM: '考试',
    BOOKING: '借用',
    OTHER: '其他'
  }
  return names[type] || type
}

onMounted(() => {
  fetchClassroomList()
})
</script>

<style scoped>
.classroom-list-container {
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

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.text-muted {
  color: #909399;
  font-size: 14px;
}

.schedule-info {
  margin-bottom: 20px;
}

.schedule-cell {
  padding: 4px;
}

.usage-item {
  margin-bottom: 8px;
}

.usage-item:last-child {
  margin-bottom: 0;
}

.usage-title {
  font-size: 12px;
  color: #303133;
  margin: 2px 0;
  font-weight: 500;
}

.usage-time {
  font-size: 11px;
  color: #909399;
}

:deep(.el-table__cell) {
  padding: 8px 0;
}
</style>

