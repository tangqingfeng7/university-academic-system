<template>
  <div class="course-exam-list-page">
    <!-- 页面头部 -->
    <div class="page-header animate-fade-in-down">
      <div class="header-content">
        <h1 class="page-title">课程考试</h1>
        <p class="page-subtitle">查看您任教课程的考试安排</p>
      </div>
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
        <div class="stat-label">考试总数</div>
        <div class="stat-value">{{ examList.length || 0 }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">待发布</div>
        <div class="stat-value stat-warning">{{ draftCount }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">已发布</div>
        <div class="stat-value stat-success">{{ publishedCount }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">已结束</div>
        <div class="stat-value">{{ finishedCount }}</div>
      </div>
    </div>

    <!-- 考试列表 -->
    <div class="exam-table-container animate-fade-in-up" style="animation-delay: 0.3s;">
      <el-table
        v-loading="loading"
        :data="examList"
        style="width: 100%"
        :default-sort="{ prop: 'examTime', order: 'ascending' }"
        @row-click="handleRowClick"
        row-class-name="clickable-row"
      >
        <el-table-column prop="courseName" label="课程名称" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="course-info">
              <div class="course-name">{{ row.courseName }}</div>
              <div class="course-no">{{ row.courseNo }}</div>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="examName" label="考试名称" min-width="180" show-overflow-tooltip />
        
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

        <el-table-column prop="roomCount" label="考场数" width="100" align="center">
          <template #default="{ row }">
            <el-tag type="info" size="small">{{ row.roomCount || 0 }} 个</el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="studentCount" label="学生数" width="100" align="center">
          <template #default="{ row }">
            <el-tag type="primary" size="small">{{ row.studentCount || 0 }} 人</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <ExamStatusTag :status="row.status" :status-description="row.statusDescription" />
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
        v-if="!loading && examList.length === 0"
        description="暂无课程考试"
        :image-size="160"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Clock } from '@element-plus/icons-vue'
import { getMyCourseExams } from '@/api/exam'
import { getAllSemesters } from '@/api/semester'
import ExamStatusTag from '@/components/ExamStatusTag.vue'
import ExamTypeTag from '@/components/ExamTypeTag.vue'

const router = useRouter()

// 数据
const loading = ref(false)
const examList = ref([])
const semesters = ref([])
const selectedSemester = ref(null)

// 统计数据
const draftCount = computed(() => {
  return examList.value.filter(exam => exam.status === 'DRAFT').length
})

const publishedCount = computed(() => {
  return examList.value.filter(exam => ['PUBLISHED', 'IN_PROGRESS'].includes(exam.status)).length
})

const finishedCount = computed(() => {
  return examList.value.filter(exam => exam.status === 'FINISHED').length
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

// 获取考试列表
const fetchExamList = async () => {
  loading.value = true
  try {
    const params = {}
    if (selectedSemester.value) {
      params.semesterId = selectedSemester.value
    }
    const res = await getMyCourseExams(params)
    examList.value = res.data || []
  } catch (error) {
    console.error('获取考试列表失败:', error)
    ElMessage.error('获取考试列表失败')
  } finally {
    loading.value = false
  }
}

// 学期切换
const handleSemesterChange = () => {
  fetchExamList()
}

// 查看详情
const handleViewDetail = (row) => {
  router.push(`/teacher/exams/courses/${row.id}`)
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

// 格式化考试时间
const formatExamTime = (time) => {
  if (!time) return '-'
  const date = new Date(time)
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${month}-${day} ${hours}:${minutes}`
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
  fetchExamList()
})
</script>

<style scoped lang="scss">
.course-exam-list-page {
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

    &.stat-success {
      color: var(--success-color);
    }

    &.stat-warning {
      color: var(--warning-color);
    }
  }

  // ===================================
  // 考试表格
  // ===================================

  .exam-table-container {
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
}
</style>

