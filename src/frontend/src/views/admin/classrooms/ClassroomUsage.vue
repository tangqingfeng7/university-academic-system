<template>
  <div class="classroom-usage-container">
    <!-- 教室信息卡片 -->
    <el-card v-loading="classroomLoading">
      <template #header>
        <div class="card-header">
          <span class="card-title">教室信息</span>
          <el-button @click="handleBack">
            <el-icon><Back /></el-icon>
            返回
          </el-button>
        </div>
      </template>

      <el-descriptions v-if="classroom" :column="4" border>
        <el-descriptions-item label="教室编号">
          {{ classroom.roomNo }}
        </el-descriptions-item>
        <el-descriptions-item label="楼栋">
          {{ classroom.building }}
        </el-descriptions-item>
        <el-descriptions-item label="容量">
          {{ classroom.capacity }}人
        </el-descriptions-item>
        <el-descriptions-item label="类型">
          <el-tag :type="getTypeTag(classroom.type)">
            {{ getTypeName(classroom.type) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="设备" :span="3">
          {{ formatEquipment(classroom.equipment) }}
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusTag(classroom.status)">
            {{ getStatusName(classroom.status) }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 使用情况查询 -->
    <el-card style="margin-top: 20px">
      <template #header>
        <div class="card-header">
          <span class="card-title">使用情况</span>
          <el-radio-group v-model="viewMode" @change="handleViewModeChange">
            <el-radio-button label="day">按天查看</el-radio-button>
            <el-radio-button label="week">按周查看</el-radio-button>
          </el-radio-group>
        </div>
      </template>

      <!-- 日期选择 -->
      <el-form :inline="true" style="margin-bottom: 20px">
        <el-form-item v-if="viewMode === 'day'" label="选择日期">
          <el-date-picker
            v-model="selectedDate"
            type="date"
            format="YYYY-MM-DD"
            placeholder="选择日期"
            @change="handleDateChange"
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item v-else label="选择周">
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

      <!-- 按天查看 -->
      <div v-if="viewMode === 'day'" v-loading="usageLoading">
        <el-timeline v-if="usageData.length > 0">
          <el-timeline-item
            v-for="(item, index) in usageData"
            :key="index"
            :timestamp="`${item.startTime} - ${item.endTime}`"
            placement="top"
            :type="getUsageTypeColor(item.type)"
          >
            <el-card>
              <div class="usage-item-header">
                <el-tag :type="getUsageTypeTag(item.type)">
                  {{ getUsageTypeName(item.type) }}
                </el-tag>
                <span class="usage-title">{{ item.title }}</span>
              </div>
              <p v-if="item.description" class="usage-description">
                {{ item.description }}
              </p>
            </el-card>
          </el-timeline-item>
        </el-timeline>
        <el-empty v-else description="当天无使用记录" />
      </div>

      <!-- 按周查看 -->
      <div v-if="viewMode === 'week'" v-loading="scheduleLoading">
        <el-table
          :data="scheduleData"
          border
          style="width: 100%"
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
              <div v-if="row[day.value] && row[day.value].length > 0" class="schedule-cell">
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
    </el-card>

    <!-- 使用率统计 -->
    <el-card style="margin-top: 20px">
      <template #header>
        <span class="card-title">使用率统计</span>
      </template>

      <el-form :inline="true" style="margin-bottom: 20px">
        <el-form-item label="统计周期">
          <el-date-picker
            v-model="utilizationDateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            @change="handleUtilizationDateChange"
            style="width: 300px"
          />
        </el-form-item>
      </el-form>

      <div v-loading="utilizationLoading">
        <el-row :gutter="20" v-if="utilization">
          <el-col :span="6">
            <el-statistic title="总使用时长" :value="utilization.totalUsageMinutes">
              <template #suffix>分钟</template>
            </el-statistic>
          </el-col>
          <el-col :span="6">
            <el-statistic title="总可用时长" :value="utilization.totalAvailableMinutes">
              <template #suffix>分钟</template>
            </el-statistic>
          </el-col>
          <el-col :span="6">
            <el-statistic title="使用率" :value="utilization.utilizationRate">
              <template #suffix>%</template>
            </el-statistic>
          </el-col>
          <el-col :span="6">
            <el-statistic title="课程使用次数" :value="utilization.courseCount">
              <template #suffix>次</template>
            </el-statistic>
          </el-col>
        </el-row>

        <el-divider />

        <el-descriptions :column="2" border v-if="utilization">
          <el-descriptions-item label="考试使用次数">
            {{ utilization.examCount }}次
          </el-descriptions-item>
          <el-descriptions-item label="借用次数">
            {{ utilization.bookingCount }}次
          </el-descriptions-item>
          <el-descriptions-item label="统计开始日期">
            {{ formatDate(utilization.startDate) }}
          </el-descriptions-item>
          <el-descriptions-item label="统计结束日期">
            {{ formatDate(utilization.endDate) }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter, useRoute } from 'vue-router'
import {
  getClassroomById,
  getClassroomUsage,
  getClassroomSchedule,
  getClassroomUtilization
} from '@/api/adminClassroom'

const router = useRouter()
const route = useRoute()

// 教室ID
const classroomId = ref(route.params.id)

// 数据
const classroomLoading = ref(false)
const classroom = ref(null)

// 视图模式
const viewMode = ref('day')
const selectedDate = ref(new Date())
const selectedWeekStart = ref(new Date())

// 使用情况数据
const usageLoading = ref(false)
const usageData = ref([])

// 课表数据
const scheduleLoading = ref(false)
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

// 使用率统计
const utilizationLoading = ref(false)
const utilization = ref(null)
const utilizationDateRange = ref([])

// 获取教室信息
const fetchClassroomInfo = async () => {
  classroomLoading.value = true
  try {
    const response = await getClassroomById(classroomId.value)
    classroom.value = response.data
  } catch (error) {
    ElMessage.error(error.message || '获取教室信息失败')
  } finally {
    classroomLoading.value = false
  }
}

// 视图模式改变
const handleViewModeChange = (mode) => {
  if (mode === 'day') {
    loadUsageData()
  } else {
    loadScheduleData()
  }
}

// 日期改变
const handleDateChange = () => {
  loadUsageData()
}

// 周改变
const handleWeekChange = () => {
  loadScheduleData()
}

// 加载使用数据
const loadUsageData = async () => {
  usageLoading.value = true
  try {
    const date = formatDate(selectedDate.value)
    const response = await getClassroomUsage(classroomId.value, date)
    usageData.value = response.data.usages || []
  } catch (error) {
    ElMessage.error(error.message || '获取使用情况失败')
  } finally {
    usageLoading.value = false
  }
}

// 加载课表数据
const loadScheduleData = async () => {
  scheduleLoading.value = true
  try {
    const startDate = formatDate(selectedWeekStart.value)
    const response = await getClassroomSchedule(classroomId.value, startDate)
    scheduleData.value = transformScheduleData(response.data)
  } catch (error) {
    ElMessage.error(error.message || '获取课表失败')
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

// 使用率日期改变
const handleUtilizationDateChange = () => {
  if (utilizationDateRange.value && utilizationDateRange.value.length === 2) {
    loadUtilizationData()
  }
}

// 加载使用率数据
const loadUtilizationData = async () => {
  if (!utilizationDateRange.value || utilizationDateRange.value.length !== 2) return
  
  utilizationLoading.value = true
  try {
    const params = {
      startDate: formatDate(utilizationDateRange.value[0]),
      endDate: formatDate(utilizationDateRange.value[1])
    }
    const response = await getClassroomUtilization(classroomId.value, params)
    utilization.value = response.data
  } catch (error) {
    ElMessage.error(error.message || '获取使用率统计失败')
  } finally {
    utilizationLoading.value = false
  }
}

// 返回
const handleBack = () => {
  router.back()
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

// 格式化设备信息
const formatEquipment = (equipment) => {
  if (!equipment) return '无特殊设备'
  try {
    const equipmentObj = JSON.parse(equipment)
    const items = Object.entries(equipmentObj)
      .filter(([, value]) => value)
      .map(([key]) => key)
    return items.length > 0 ? items.join('、') : '无特殊设备'
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

// 获取使用类型颜色
const getUsageTypeColor = (type) => {
  const colors = {
    COURSE: 'primary',
    EXAM: 'danger',
    BOOKING: 'success',
    OTHER: 'info'
  }
  return colors[type] || 'info'
}

onMounted(() => {
  fetchClassroomInfo()
  loadUsageData()
  
  // 默认显示本周
  const now = new Date()
  const day = now.getDay()
  const monday = new Date(now)
  monday.setDate(now.getDate() - (day === 0 ? 6 : day - 1))
  selectedWeekStart.value = monday
  
  // 设置默认使用率统计周期（最近7天）
  const endDate = new Date()
  const startDate = new Date()
  startDate.setDate(startDate.getDate() - 7)
  utilizationDateRange.value = [startDate, endDate]
  loadUtilizationData()
})
</script>

<style scoped>
.classroom-usage-container {
  padding: 20px;
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

.text-muted {
  color: #909399;
  font-size: 14px;
}

.usage-item-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.usage-title {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.usage-description {
  color: #606266;
  font-size: 14px;
  margin: 8px 0 0 0;
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

.usage-item .usage-title {
  font-size: 12px;
  color: #303133;
  margin: 2px 0;
  font-weight: 500;
}

.usage-time {
  font-size: 11px;
  color: #909399;
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

