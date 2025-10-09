<template>
  <div class="invigilation-list-page">
    <!-- 页面头部 -->
    <div class="page-header animate-fade-in-down">
      <div class="header-content">
        <h1 class="page-title">监考任务</h1>
        <p class="page-subtitle">查看您的监考任务安排</p>
      </div>
      <el-button 
        type="primary" 
        :icon="Download" 
        size="large"
        :loading="exportLoading"
        @click="handleExport"
      >
        导出监考安排
      </el-button>
    </div>

    <!-- 筛选器 -->
    <div class="filter-section animate-fade-in-up" style="animation-delay: 0.1s;">
      <el-select
        v-model="selectedSemester"
        placeholder="全部学期"
        clearable
        size="large"
        @change="handleSemesterChange"
        style="width: 200px"
      >
        <el-option label="全部学期" :value="null" />
        <el-option
          v-for="semester in semesters"
          :key="semester.id"
          :label="semester.academicYear + ' ' + getSemesterTypeName(semester.semesterType)"
          :value="semester.id"
        />
      </el-select>
    </div>

    <!-- 统计卡片 -->
    <div 
      class="stats-grid animate-fade-in-up" 
      style="animation-delay: 0.2s;"
      v-loading="loading"
    >
      <div class="stat-card">
        <div class="stat-label">监考总数</div>
        <div class="stat-value">{{ invigilationList.length || 0 }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">主监考</div>
        <div class="stat-value stat-primary">{{ chiefCount }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">副监考</div>
        <div class="stat-value stat-info">{{ assistantCount }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">待执行</div>
        <div class="stat-value stat-warning">{{ upcomingCount }}</div>
      </div>
    </div>

    <!-- 监考任务列表 -->
    <div class="invigilation-table-container animate-fade-in-up" style="animation-delay: 0.3s;">
      <el-table
        v-loading="loading"
        :data="invigilationList"
        style="width: 100%"
        :default-sort="{ prop: 'examTime', order: 'ascending' }"
        @row-click="handleRowClick"
        row-class-name="clickable-row"
      >
        <el-table-column prop="examName" label="考试名称" min-width="200" show-overflow-tooltip />
        
        <el-table-column prop="courseName" label="课程" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="course-info">
              <div class="course-name">{{ row.courseName }}</div>
              <div class="course-no">{{ row.courseNo }}</div>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="type" label="考试类型" width="120" align="center">
          <template #default="{ row }">
            <ExamTypeTag :type="row.type" :type-description="row.typeDescription" />
          </template>
        </el-table-column>

        <el-table-column prop="examTime" label="考试时间" width="180" sortable>
          <template #default="{ row }">
            <div class="time-info">
              <el-icon><Clock /></el-icon>
              <span>{{ formatExamTime(row.examTime) }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="duration" label="时长" width="100" align="center">
          <template #default="{ row }">
            <span class="duration-text">{{ formatDuration(row.duration) }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="roomName" label="考场" width="150" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="location-info">
              <el-icon><Location /></el-icon>
              <span>{{ row.roomName }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="invigilatorType" label="监考类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.invigilatorType === 'CHIEF' ? 'danger' : 'info'" size="small">
              {{ row.invigilatorType === 'CHIEF' ? '主监考' : '副监考' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <ExamStatusTag :status="row.examStatus" :status-description="row.statusDescription" />
          </template>
        </el-table-column>

        <el-table-column label="操作" width="100" align="center" fixed="right">
          <template #default="{ row }">
            <el-button 
              type="primary" 
              link 
              @click.stop="handleViewDetail(row)"
            >
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 空状态 -->
      <el-empty
        v-if="!loading && invigilationList.length === 0"
        description="暂无监考任务"
        :image-size="160"
      />
    </div>

    <!-- 监考详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="监考详情"
      width="700px"
      :close-on-click-modal="false"
    >
      <div v-if="currentInvigilation" class="invigilation-detail">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="考试名称">
            {{ currentInvigilation.examName }}
          </el-descriptions-item>
          <el-descriptions-item label="课程信息">
            {{ currentInvigilation.courseName }} ({{ currentInvigilation.courseNo }})
          </el-descriptions-item>
          <el-descriptions-item label="考试类型">
            <ExamTypeTag :type="currentInvigilation.examType" :type-description="currentInvigilation.examTypeDescription" />
          </el-descriptions-item>
          <el-descriptions-item label="考试时间">
            {{ formatExamTimeDetail(currentInvigilation.examTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="考试时长">
            {{ formatDuration(currentInvigilation.duration) }}
          </el-descriptions-item>
          <el-descriptions-item label="考场">
            {{ currentInvigilation.roomName }}
          </el-descriptions-item>
          <el-descriptions-item label="考场地点">
            {{ currentInvigilation.location }}
          </el-descriptions-item>
          <el-descriptions-item label="监考类型">
            <el-tag :type="currentInvigilation.invigilatorType === 'CHIEF' ? 'danger' : 'info'">
              {{ currentInvigilation.invigilatorType === 'CHIEF' ? '主监考' : '副监考' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="考场容量">
            {{ currentInvigilation.studentCount || 0 }}/{{ currentInvigilation.roomCapacity || 0 }} 人
          </el-descriptions-item>
          <el-descriptions-item label="考试状态">
            <ExamStatusTag :status="currentInvigilation.examStatus" :status-description="currentInvigilation.statusDescription" />
          </el-descriptions-item>
        </el-descriptions>

        <el-alert
          v-if="currentInvigilation.invigilatorType === 'CHIEF'"
          type="warning"
          :closable="false"
          style="margin-top: 16px;"
        >
          <template #title>
            <div style="display: flex; align-items: center; gap: 8px;">
              <el-icon><InfoFilled /></el-icon>
              <span>主监考职责</span>
            </div>
          </template>
          作为主监考，您需要负责考场的整体管理，包括考前准备、考试组织和突发情况处理。
        </el-alert>
      </div>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Download, Clock, Location, InfoFilled } from '@element-plus/icons-vue'
import { getMyInvigilation, downloadMyInvigilationSchedule } from '@/api/exam'
import { getAllSemesters } from '@/api/semester'
import ExamStatusTag from '@/components/ExamStatusTag.vue'
import ExamTypeTag from '@/components/ExamTypeTag.vue'

const router = useRouter()

// 数据
const loading = ref(false)
const exportLoading = ref(false)
const invigilationList = ref([])
const semesters = ref([])
const selectedSemester = ref(null)
const detailDialogVisible = ref(false)
const currentInvigilation = ref(null)

// 统计数据
const chiefCount = computed(() => {
  return invigilationList.value.filter(item => item.invigilatorType === 'CHIEF').length
})

const assistantCount = computed(() => {
  return invigilationList.value.filter(item => item.invigilatorType === 'ASSISTANT').length
})

const upcomingCount = computed(() => {
  return invigilationList.value.filter(item => ['PUBLISHED', 'IN_PROGRESS'].includes(item.examStatus)).length
})

// 获取学期列表
const fetchSemesters = async () => {
  try {
    const res = await getAllSemesters()
    semesters.value = res.data || []
  } catch (error) {
    console.error('获取学期列表失败:', error)
  }
}

// 获取监考任务列表
const fetchInvigilationList = async () => {
  loading.value = true
  try {
    const params = {}
    if (selectedSemester.value) {
      params.semesterId = selectedSemester.value
    }
    const res = await getMyInvigilation(params)
    invigilationList.value = res.data || []
  } catch (error) {
    console.error('获取监考任务失败:', error)
    ElMessage.error('获取监考任务失败')
  } finally {
    loading.value = false
  }
}

// 学期切换
const handleSemesterChange = () => {
  fetchInvigilationList()
}

// 导出监考安排
const handleExport = async () => {
  exportLoading.value = true
  try {
    await downloadMyInvigilationSchedule()
    ElMessage.success('监考安排导出成功')
  } catch (error) {
    console.error('导出监考安排失败:', error)
    ElMessage.error('导出监考安排失败')
  } finally {
    exportLoading.value = false
  }
}

// 查看详情
const handleViewDetail = (row) => {
  currentInvigilation.value = row
  detailDialogVisible.value = true
}

// 行点击事件
const handleRowClick = (row) => {
  handleViewDetail(row)
}

// 格式化学期类型
const getSemesterTypeName = (type) => {
  const typeMap = {
    1: '春季',
    2: '秋季'
  }
  return typeMap[type] || '未知'
}

// 格式化考试时间（简洁版）
const formatExamTime = (time) => {
  if (!time) return '-'
  const date = new Date(time)
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${month}-${day} ${hours}:${minutes}`
}

// 格式化考试时间（详细版）
const formatExamTimeDetail = (time) => {
  if (!time) return '-'
  const date = new Date(time)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const weekDays = ['日', '一', '二', '三', '四', '五', '六']
  const weekDay = weekDays[date.getDay()]
  return `${year}年${month}月${day}日 ${hours}:${minutes} 星期${weekDay}`
}

// 格式化时长
const formatDuration = (minutes) => {
  if (!minutes) return '-'
  const hours = Math.floor(minutes / 60)
  const mins = minutes % 60
  if (hours > 0 && mins > 0) {
    return `${hours}小时${mins}分钟`
  } else if (hours > 0) {
    return `${hours}小时`
  } else {
    return `${mins}分钟`
  }
}

// 页面加载
onMounted(() => {
  fetchSemesters()
  fetchInvigilationList()
})
</script>

<style scoped lang="scss">
.invigilation-list-page {
  max-width: 1400px;
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
  // 筛选器
  // ===================================

  .filter-section {
    margin-bottom: var(--spacing-xl);
  }

  // ===================================
  // 统计卡片
  // ===================================

  .stats-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 16px;
    margin-bottom: var(--spacing-2xl);

    @media (max-width: 1024px) {
      grid-template-columns: repeat(2, 1fr);
    }

    @media (max-width: 640px) {
      grid-template-columns: 1fr;
    }
  }

  .stat-card {
    background: var(--bg-primary);
    border-radius: var(--radius-lg);
    padding: 24px;
    border: 1px solid var(--border-light);
    transition: all var(--transition-base);

    &:hover {
      box-shadow: var(--shadow-sm);
    }
  }

  .stat-label {
    font-size: 14px;
    color: var(--text-secondary);
    margin-bottom: 8px;
    font-weight: 500;
  }

  .stat-value {
    font-size: 32px;
    font-weight: 600;
    color: var(--text-primary);
    letter-spacing: -0.02em;

    &.stat-primary {
      color: var(--error-color);
    }

    &.stat-info {
      color: var(--info-color);
    }

    &.stat-warning {
      color: var(--warning-color);
    }
  }

  // ===================================
  // 监考任务表格
  // ===================================

  .invigilation-table-container {
    background: var(--bg-primary);
    border-radius: var(--radius-lg);
    overflow: hidden;

    :deep(.el-table) {
      background: transparent;

      .el-table__cell {
        padding: 14px 12px;
      }

      .clickable-row {
        cursor: pointer;
        
        &:hover {
          background-color: var(--el-fill-color-light);
        }
      }
    }
  }

  .course-info {
    .course-name {
      font-size: 14px;
      font-weight: 500;
      color: var(--text-primary);
    }
    
    .course-no {
      font-size: 12px;
      color: var(--text-secondary);
      margin-top: 2px;
    }
  }

  .time-info {
    display: flex;
    align-items: center;
    gap: 6px;
    
    .el-icon {
      color: var(--el-color-primary);
    }
  }

  .duration-text {
    font-size: 14px;
    color: var(--text-primary);
    font-weight: 500;
  }

  .location-info {
    display: flex;
    align-items: center;
    gap: 6px;
    
    .el-icon {
      color: var(--el-color-primary);
    }
  }

  // ===================================
  // 监考详情对话框
  // ===================================

  .invigilation-detail {
    :deep(.el-descriptions) {
      .el-descriptions__label {
        width: 120px;
        font-weight: 500;
      }
    }
  }
}
</style>

