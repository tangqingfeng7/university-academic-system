<template>
  <div class="scheduling-preview">
    <el-page-header @back="goBack" title="返回" content="课表预览" />

    <!-- 方案信息栏 -->
    <el-card shadow="hover" class="info-card" v-loading="loading">
      <div class="info-header">
        <div class="info-title">
          <h3>{{ solution.name }}</h3>
          <el-tag :type="getStatusType(solution.status)" size="large">
            {{ getStatusText(solution.status) }}
          </el-tag>
        </div>
        <div class="info-actions">
          <el-button @click="handleExport('excel')">
            <el-icon><Download /></el-icon>
            导出Excel
          </el-button>
          <el-button @click="handleExport('pdf')">
            <el-icon><Download /></el-icon>
            导出PDF
          </el-button>
        </div>
      </div>

      <el-descriptions :column="4" class="info-details">
        <el-descriptions-item label="质量分数">
          {{ solution.qualityScore?.toFixed(2) || '--' }}
        </el-descriptions-item>
        <el-descriptions-item label="冲突数">
          <el-tag v-if="solution.conflictCount > 0" type="danger" size="small">
            {{ solution.conflictCount }}
          </el-tag>
          <span v-else class="success-text">0</span>
        </el-descriptions-item>
        <el-descriptions-item label="已排课程">
          {{ scheduleItems.length }}
        </el-descriptions-item>
        <el-descriptions-item label="生成时间">
          {{ formatDateTime(solution.generatedAt) }}
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 视图切换和筛选 -->
    <el-card shadow="hover" class="filter-card">
      <el-form :inline="true" :model="filterForm">
        <el-form-item label="视图类型">
          <el-radio-group v-model="viewType" @change="loadScheduleResult">
            <el-radio-button label="grid">网格视图</el-radio-button>
            <el-radio-button label="list">列表视图</el-radio-button>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="筛选维度" v-if="viewType === 'grid'">
          <el-select v-model="filterForm.dimension" @change="loadScheduleResult" style="width: 150px">
            <el-option label="按教室" value="classroom" />
            <el-option label="按教师" value="teacher" />
          </el-select>
        </el-form-item>

        <el-form-item v-if="viewType === 'grid'">
          <el-select 
            v-model="filterForm.targetId" 
            :placeholder="`选择${filterForm.dimension === 'classroom' ? '教室' : '教师'}`"
            filterable
            clearable
            @change="filterSchedule"
            style="width: 200px"
          >
            <el-option
              v-for="item in filterOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleDetectConflicts">
            <el-icon><Warning /></el-icon>
            检测冲突
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 网格视图 -->
    <el-card shadow="hover" class="schedule-card" v-if="viewType === 'grid'" v-loading="scheduleLoading">
      <div class="schedule-grid">
        <table class="schedule-table">
          <thead>
            <tr>
              <th class="time-column">时间/星期</th>
              <th v-for="day in weekDays" :key="day.value">{{ day.label }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="slot in timeSlots" :key="slot.value">
              <td class="time-column">
                <div class="time-label">{{ slot.label }}</div>
                <div class="time-range">{{ slot.range }}</div>
              </td>
              <td 
                v-for="day in weekDays" 
                :key="day.value"
                class="schedule-cell"
                :class="{ 'has-course': getCellCourse(day.value, slot.value) }"
              >
                <div 
                  v-if="getCellCourse(day.value, slot.value)"
                  class="course-block"
                  :class="{ 'conflict': isConflict(day.value, slot.value) }"
                  @click="handleCourseClick(getCellCourse(day.value, slot.value))"
                >
                  <div class="course-name">{{ getCellCourse(day.value, slot.value).courseName }}</div>
                  <div class="course-info">
                    <div>{{ getCellCourse(day.value, slot.value).teacherName }}</div>
                    <div>{{ getCellCourse(day.value, slot.value).classroomName }}</div>
                  </div>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </el-card>

    <!-- 列表视图 -->
    <el-card shadow="hover" class="schedule-card" v-if="viewType === 'list'" v-loading="scheduleLoading">
      <el-table :data="displayItems" stripe max-height="600">
        <el-table-column prop="courseName" label="课程名称" min-width="150" />
        <el-table-column prop="teacherName" label="授课教师" width="120" />
        <el-table-column prop="classroomName" label="教室" width="120" />
        <el-table-column label="上课时间" min-width="200">
          <template #default="{ row }">
            {{ formatScheduleTime(row) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <span v-if="row.hasConflict" class="status-text status-conflict">冲突</span>
            <span v-else class="status-text status-normal">正常</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right" align="center">
          <template #default="{ row }">
            <el-button text size="small" @click="handleAdjust(row)">
              调整
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 手动调整对话框 -->
    <el-dialog
      v-model="adjustDialogVisible"
      title="手动调整排课"
      width="600px"
      @close="handleAdjustDialogClose"
    >
      <el-form
        ref="adjustFormRef"
        :model="adjustForm"
        :rules="adjustRules"
        label-width="100px"
        v-if="currentCourse"
      >
        <el-form-item label="课程">
          <el-input :value="currentCourse.courseName" disabled />
        </el-form-item>

        <el-form-item label="教师">
          <el-input :value="currentCourse.teacherName" disabled />
        </el-form-item>

        <el-form-item label="新教室" prop="newClassroomId">
          <el-select 
            v-model="adjustForm.newClassroomId" 
            placeholder="选择教室"
            filterable
            style="width: 100%"
          >
            <el-option
              v-for="classroom in availableClassrooms"
              :key="classroom.id"
              :label="`${classroom.roomNo} (${classroom.capacity}人)`"
              :value="classroom.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="新时间" prop="newTimeSlots">
          <el-select v-model="adjustForm.newDayOfWeek" placeholder="星期" style="width: 48%; margin-right: 4%">
            <el-option v-for="day in weekDays" :key="day.value" :label="day.label" :value="day.value" />
          </el-select>
          <el-select v-model="adjustForm.newTimeSlot" placeholder="时段" style="width: 48%">
            <el-option v-for="slot in timeSlots" :key="slot.value" :label="slot.label" :value="slot.value" />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="adjustDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitAdjust" :loading="adjusting">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 冲突详情对话框 -->
    <el-dialog
      v-model="conflictDialogVisible"
      title="冲突详情"
      width="700px"
    >
      <el-table :data="conflicts" stripe>
        <el-table-column prop="type" label="冲突类型" width="150">
          <template #default="{ row }">
            <el-tag :type="row.severity === 'HIGH' ? 'danger' : 'warning'" size="small">
              {{ row.type }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Download, Warning } from '@element-plus/icons-vue'
import { useRouter, useRoute } from 'vue-router'
import {
  getSolution,
  getSolutionResult,
  detectConflicts,
  adjustSchedule,
  exportSchedule
} from '@/api/scheduling'
import { getClassrooms } from '@/api/classroom'
import { formatDateTime } from '@/utils/date'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const scheduleLoading = ref(false)
const adjusting = ref(false)
const adjustDialogVisible = ref(false)
const conflictDialogVisible = ref(false)
const adjustFormRef = ref(null)

const solution = ref({})
const scheduleResult = ref(null)
const scheduleItems = ref([])
const filteredItems = ref([])
const conflicts = ref([])
const availableClassrooms = ref([])
const currentCourse = ref(null)
const viewType = ref('grid')

const weekDays = [
  { value: 1, label: '星期一' },
  { value: 2, label: '星期二' },
  { value: 3, label: '星期三' },
  { value: 4, label: '星期四' },
  { value: 5, label: '星期五' },
  { value: 6, label: '星期六' },
  { value: 7, label: '星期日' }
]

const timeSlots = [
  { value: 1, label: '第1节', range: '08:20-09:00' },
  { value: 2, label: '第2节', range: '09:05-09:45' },
  { value: 3, label: '第3节', range: '10:05-10:45' },
  { value: 4, label: '第4节', range: '10:50-11:30' },
  { value: 5, label: '第5节', range: '11:35-12:15' },
  { value: 6, label: '第6节', range: '14:30-15:10' },
  { value: 7, label: '第7节', range: '15:15-15:55' },
  { value: 8, label: '第8节', range: '16:15-16:55' },
  { value: 9, label: '第9节', range: '17:00-17:40' },
  { value: 10, label: '第10节', range: '17:45-18:25' },
  { value: 11, label: '第11节', range: '20:15-20:55' },
  { value: 12, label: '第12节', range: '21:00-21:40' }
]

const filterForm = reactive({
  dimension: 'classroom',
  targetId: null
})

const adjustForm = reactive({
  newClassroomId: null,
  newDayOfWeek: null,
  newTimeSlot: null
})

const adjustRules = {
  newClassroomId: [
    { required: true, message: '请选择教室', trigger: 'change' }
  ],
  newTimeSlots: [
    { required: true, message: '请选择新时间', trigger: 'change' }
  ]
}

// 显示的项目（用于列表视图）
const displayItems = computed(() => {
  return filteredItems.value.length > 0 ? filteredItems.value : scheduleItems.value
})

// 筛选选项
const filterOptions = computed(() => {
  if (filterForm.dimension === 'classroom') {
    const classrooms = new Map()
    scheduleItems.value.forEach(item => {
      if (item.classroomId && item.classroomName) {
        classrooms.set(item.classroomId, { id: item.classroomId, name: item.classroomName })
      }
    })
    return Array.from(classrooms.values())
  } else {
    const teachers = new Map()
    scheduleItems.value.forEach(item => {
      if (item.teacherId && item.teacherName) {
        teachers.set(item.teacherId, { id: item.teacherId, name: item.teacherName })
      }
    })
    return Array.from(teachers.values())
  }
})

// 状态映射
const getStatusType = (status) => {
  const typeMap = {
    DRAFT: 'info',
    OPTIMIZING: 'warning',
    COMPLETED: 'success',
    APPLIED: 'primary'
  }
  return typeMap[status] || 'info'
}

const getStatusText = (status) => {
  const textMap = {
    DRAFT: '草稿',
    OPTIMIZING: '排课中',
    COMPLETED: '已完成',
    APPLIED: '已应用'
  }
  return textMap[status] || status
}

// 获取单元格课程
const getCellCourse = (day, slot) => {
  const items = filteredItems.value.length > 0 ? filteredItems.value : scheduleItems.value
  return items.find(item => 
    item.timeSlots?.some(ts => ts.dayOfWeek === day && ts.timeSlot === slot)
  )
}

// 检查是否有冲突
const isConflict = (day, slot) => {
  return conflicts.value.some(conflict => 
    conflict.description?.includes(`星期${day}`) && 
    conflict.description?.includes(`第${slot}`)
  )
}

// 格式化排课时间
const formatScheduleTime = (row) => {
  if (!row.timeSlots || row.timeSlots.length === 0) return '--'
  return row.timeSlots.map(ts => {
    const day = weekDays.find(d => d.value === ts.dayOfWeek)?.label || ''
    const slot = timeSlots.find(s => s.value === ts.timeSlot)?.label || ''
    return `${day} ${slot}`
  }).join(', ')
}

// 加载方案信息
const loadSolution = async () => {
  loading.value = true
  try {
    const res = await getSolution(route.params.id)
    solution.value = res.data
  } catch (error) {
    ElMessage.error(error.message || '加载方案信息失败')
  } finally {
    loading.value = false
  }
}

// 加载排课结果
const loadScheduleResult = async () => {
  scheduleLoading.value = true
  try {
    const res = await getSolutionResult(route.params.id)
    scheduleResult.value = res.data
    scheduleItems.value = res.data.scheduledItems || []
    filteredItems.value = []
    filterForm.targetId = null
  } catch (error) {
    ElMessage.error(error.message || '加载排课结果失败')
  } finally {
    scheduleLoading.value = false
  }
}

// 筛选排课
const filterSchedule = () => {
  if (!filterForm.targetId) {
    filteredItems.value = []
    return
  }

  if (filterForm.dimension === 'classroom') {
    filteredItems.value = scheduleItems.value.filter(item => item.classroomId === filterForm.targetId)
  } else {
    filteredItems.value = scheduleItems.value.filter(item => item.teacherId === filterForm.targetId)
  }
}

// 检测冲突
const handleDetectConflicts = async () => {
  scheduleLoading.value = true
  try {
    const res = await detectConflicts(route.params.id)
    conflicts.value = res.data || []
    if (conflicts.value.length === 0) {
      ElMessage.success('未检测到冲突')
    } else {
      conflictDialogVisible.value = true
      ElMessage.warning(`检测到 ${conflicts.value.length} 个冲突`)
    }
  } catch (error) {
    ElMessage.error(error.message || '检测冲突失败')
  } finally {
    scheduleLoading.value = false
  }
}

// 点击课程
const handleCourseClick = (course) => {
  handleAdjust(course)
}

// 调整排课
const handleAdjust = async (course) => {
  currentCourse.value = course
  adjustForm.newClassroomId = course.classroomId
  adjustForm.newDayOfWeek = course.timeSlots?.[0]?.dayOfWeek || null
  adjustForm.newTimeSlot = course.timeSlots?.[0]?.timeSlot || null
  
  // 加载可用教室
  try {
    const res = await getClassrooms()
    availableClassrooms.value = res.data || []
  } catch (error) {
    console.error('加载教室列表失败:', error)
  }
  
  adjustDialogVisible.value = true
}

// 提交调整
const handleSubmitAdjust = async () => {
  if (!adjustFormRef.value) return

  await adjustFormRef.value.validate(async (valid) => {
    if (!valid) return

    adjusting.value = true
    try {
      const data = {
        courseOfferingId: currentCourse.value.courseOfferingId,
        newClassroomId: adjustForm.newClassroomId,
        newTimeSlots: [{
          dayOfWeek: adjustForm.newDayOfWeek,
          timeSlot: adjustForm.newTimeSlot
        }]
      }

      await adjustSchedule(route.params.id, data)
      ElMessage.success('调整成功')
      adjustDialogVisible.value = false
      await loadScheduleResult()
    } catch (error) {
      ElMessage.error(error.message || '调整失败')
    } finally {
      adjusting.value = false
    }
  })
}

// 导出课表
const handleExport = async (format) => {
  try {
    const blob = await exportSchedule(route.params.id, format)
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `schedule_${solution.value.name}.${format === 'pdf' ? 'pdf' : 'xlsx'}`
    link.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (error) {
    ElMessage.error(error.message || '导出失败')
  }
}

// 关闭调整对话框
const handleAdjustDialogClose = () => {
  adjustFormRef.value?.resetFields()
  currentCourse.value = null
}

// 返回
const goBack = () => {
  router.push({ name: 'AdminSchedulingSolutions' })
}

onMounted(async () => {
  if (route.params.id) {
    await loadSolution()
    await loadScheduleResult()
  }
})
</script>

<style scoped lang="scss">
.scheduling-preview {
  padding: 20px;

  :deep(.el-page-header) {
    margin-bottom: 20px;
  }
}

.info-card {
  margin-bottom: 16px;

  .info-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;

    .info-title {
      display: flex;
      align-items: center;
      gap: 16px;

      h3 {
        margin: 0;
        font-size: 20px;
        font-weight: 600;
      }
    }
  }
}

.filter-card {
  margin-bottom: 16px;

  :deep(.el-form--inline .el-form-item) {
    margin-bottom: 0;
  }
}

.schedule-card {
  .schedule-grid {
    overflow-x: auto;
  }

  .schedule-table {
    width: 100%;
    border-collapse: collapse;
    min-width: 1000px;

    th,
    td {
      border: 1px solid #ebeef5;
      padding: 8px;
      text-align: center;
    }

    th {
      background-color: #f5f7fa;
      font-weight: 600;
      color: #606266;
    }

    .time-column {
      width: 120px;
      background-color: #fafafa;

      .time-label {
        font-weight: 600;
        color: #303133;
      }

      .time-range {
        font-size: 12px;
        color: #909399;
        margin-top: 4px;
      }
    }

    .schedule-cell {
      height: 100px;
      padding: 4px;
      vertical-align: top;

      &.has-course {
        background-color: #f0f9ff;
      }

      .course-block {
        height: 100%;
        padding: 8px;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        border-radius: 4px;
        color: white;
        cursor: pointer;
        transition: all 0.3s;

        &:hover {
          transform: scale(1.05);
          box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
        }

        &.conflict {
          background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
        }

        .course-name {
          font-weight: 600;
          font-size: 14px;
          margin-bottom: 8px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }

        .course-info {
          font-size: 12px;
          opacity: 0.9;
          line-height: 1.6;

          div {
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }
        }
      }
    }
  }
}

.success-text {
  color: #67c23a;
  font-weight: 500;
}

/* 苹果风格状态文字 */
.status-text {
  font-size: 13px;
  font-weight: 500;
  padding: 2px 8px;
  border-radius: 4px;
}

.status-normal {
  color: #34c759;
}

.status-conflict {
  color: #ff3b30;
}
</style>

