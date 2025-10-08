<template>
  <div class="grade-list-page">
    <!-- 页面头部 -->
    <div class="page-header animate-fade-in-down">
      <div class="header-content">
        <h1 class="page-title">成绩</h1>
        <p class="page-subtitle">查看你的课程成绩和学业表现</p>
      </div>
      <el-button 
        type="primary" 
        :icon="Download" 
        size="large"
        :loading="exportLoading"
        @click="handleExport"
      >
        导出成绩单
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
        <div class="stat-label">课程总数</div>
        <div class="stat-value">{{ statistics.totalCourses || 0 }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">已通过</div>
        <div class="stat-value stat-success">{{ statistics.passedCourses || 0 }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">总学分</div>
        <div class="stat-value">{{ statistics.totalCredits || 0 }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">平均绩点</div>
        <div class="stat-value stat-primary">{{ formatGPA(statistics.gpa || statistics.overallGPA) }}</div>
      </div>
    </div>

    <!-- 成绩列表 -->
    <div class="grade-table-container animate-fade-in-up" style="animation-delay: 0.3s;">
      <el-table
        v-loading="loading"
        :data="gradeList"
        style="width: 100%"
      >
        <el-table-column prop="semesterName" label="学期" width="160" show-overflow-tooltip />
        
        <el-table-column prop="courseNo" label="课程编号" width="120" />
        
        <el-table-column prop="courseName" label="课程名称" min-width="180" show-overflow-tooltip />
        
        <el-table-column prop="courseType" label="类型" width="80" align="center">
          <template #default="{ row }">
            <span class="course-type-badge" :class="getCourseTypeClass(row.courseType)">
              {{ getCourseTypeName(row.courseType) }}
            </span>
          </template>
        </el-table-column>

        <el-table-column prop="credits" label="学分" width="80" align="center">
          <template #default="{ row }">
            <span class="score-text">{{ row.credits }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="regularScore" label="平时" width="80" align="center">
          <template #default="{ row }">
            <span class="score-text">{{ formatScore(row.regularScore) }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="midtermScore" label="期中" width="80" align="center">
          <template #default="{ row }">
            <span class="score-text">{{ formatScore(row.midtermScore) }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="finalScore" label="期末" width="80" align="center">
          <template #default="{ row }">
            <span class="score-text">{{ formatScore(row.finalScore) }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="totalScore" label="总评" width="80" align="center">
          <template #default="{ row }">
            <span :class="['score-text', 'score-total', getScoreClass(row.totalScore)]">
              {{ formatScore(row.totalScore) }}
            </span>
          </template>
        </el-table-column>

        <el-table-column prop="gradePoint" label="绩点" width="80" align="center">
          <template #default="{ row }">
            <span class="score-text">{{ formatGradePoint(row.gradePoint) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <span class="status-badge" :class="row.passed ? 'status-pass' : 'status-fail'">
              {{ row.passed ? '通过' : '未通过' }}
            </span>
          </template>
        </el-table-column>

        <el-table-column prop="teacherName" label="授课教师" width="120" />
      </el-table>

      <!-- 空状态 -->
      <el-empty
        v-if="!loading && gradeList.length === 0"
        description="暂无成绩记录"
        :image-size="160"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Download } from '@element-plus/icons-vue'
import { 
  getMyGrades, 
  getSemesterGrades, 
  getSemesterStatistics,
  getTranscriptStatistics,
  exportTranscript 
} from '@/api/grade'
import { getAllSemesters } from '@/api/semester'

// 数据
const loading = ref(false)
const exportLoading = ref(false)
const gradeList = ref([])
const semesters = ref([])
const selectedSemester = ref(null)
const statistics = ref({
  totalCourses: 0,
  passedCourses: 0,
  totalCredits: 0,
  passedCredits: 0,
  gpa: 0,
  overallGPA: 0
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

// 获取成绩列表
const fetchGradeList = async () => {
  loading.value = true
  try {
    let res
    if (selectedSemester.value) {
      res = await getSemesterGrades(selectedSemester.value)
    } else {
      res = await getMyGrades()
    }
    gradeList.value = res.data || []
  } catch (error) {
    console.error('获取成绩列表失败:', error)
    ElMessage.error('获取成绩列表失败')
  } finally {
    loading.value = false
  }
}

// 获取统计信息
const fetchStatistics = async () => {
  try {
    let res
    if (selectedSemester.value) {
      res = await getSemesterStatistics(selectedSemester.value)
    } else {
      res = await getTranscriptStatistics()
    }
    statistics.value = res.data || {}
  } catch (error) {
    console.error('获取统计信息失败:', error)
  }
}

// 学期切换
const handleSemesterChange = () => {
  fetchGradeList()
  fetchStatistics()
}

// 导出成绩单
const handleExport = async () => {
  exportLoading.value = true
  try {
    const blob = await exportTranscript()
    
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `成绩单_${new Date().getTime()}.pdf`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    
    ElMessage.success('成绩单导出成功')
  } catch (error) {
    console.error('导出成绩单失败:', error)
    ElMessage.error('导出成绩单失败')
  } finally {
    exportLoading.value = false
  }
}

// 格式化学期类型
const getSemesterTypeName = (type) => {
  const typeMap = {
    1: '春季',
    2: '秋季'
  }
  return typeMap[type] || '未知'
}

// 格式化课程类型
const getCourseTypeName = (type) => {
  const typeMap = {
    REQUIRED: '必修',
    ELECTIVE: '选修',
    PUBLIC: '公共'
  }
  return typeMap[type] || type
}

// 课程类型标签颜色
const getCourseTypeTag = (type) => {
  const tagMap = {
    REQUIRED: 'danger',
    ELECTIVE: 'warning',
    PUBLIC: 'info'
  }
  return tagMap[type] || ''
}

// 课程类型样式类
const getCourseTypeClass = (type) => {
  const classMap = {
    REQUIRED: 'type-required',
    ELECTIVE: 'type-elective',
    PUBLIC: 'type-public'
  }
  return classMap[type] || ''
}

// 格式化分数
const formatScore = (score) => {
  return score != null ? score.toFixed(1) : '-'
}

// 格式化绩点
const formatGradePoint = (point) => {
  return point != null ? point.toFixed(2) : '-'
}

// 格式化GPA
const formatGPA = (gpa) => {
  if (gpa == null) return '0.00'
  return typeof gpa === 'number' ? gpa.toFixed(2) : parseFloat(gpa).toFixed(2)
}

// 分数样式
const getScoreClass = (score) => {
  if (score == null) return ''
  if (score >= 90) return 'score-excellent'
  if (score >= 80) return 'score-good'
  if (score >= 60) return 'score-pass'
  return 'score-fail'
}

// 页面加载
onMounted(() => {
  fetchSemesters()
  fetchGradeList()
  fetchStatistics()
})
</script>

<style scoped lang="scss">
.grade-list-page {
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

    &.stat-primary {
      color: var(--primary-color);
    }
  }

  // ===================================
  // 成绩表格
  // ===================================

  .grade-table-container {
    background: var(--bg-primary);
    border-radius: var(--radius-lg);
    overflow: hidden;

    :deep(.el-table) {
      background: transparent;

      // 表格单元格内边距
      .el-table__cell {
        padding: 14px 12px;
      }
    }
  }

  .score-text {
    font-size: 14px;
    color: var(--text-primary);
    font-weight: 500;
    font-variant-numeric: tabular-nums;
  }

  .score-total {
    font-weight: 600;
    font-size: 15px;
  }

  .score-excellent {
    color: var(--success-color);
  }

  .score-good {
    color: var(--primary-color);
  }

  .score-pass {
    color: var(--warning-color);
  }

  .score-fail {
    color: var(--error-color);
  }

  // 确保标签不显示多余的点
  :deep(.el-tag) {
    &::before,
    &::after {
      content: none !important;
      display: none !important;
    }
  }

  // 课程类型徽章样式
  .course-type-badge {
    display: inline-block;
    padding: 3px 10px;
    border-radius: 12px;
    font-size: 12px;
    font-weight: 500;
    line-height: 1.5;
    
    &.type-required {
      background: rgba(255, 59, 48, 0.1);
      color: var(--error-color);
    }
    
    &.type-elective {
      background: rgba(255, 149, 0, 0.1);
      color: var(--warning-color);
    }
    
    &.type-public {
      background: rgba(142, 142, 147, 0.1);
      color: var(--text-secondary);
    }
  }

  // 状态徽章样式
  .status-badge {
    display: inline-block;
    padding: 3px 10px;
    border-radius: 12px;
    font-size: 12px;
    font-weight: 500;
    line-height: 1.5;
    
    &.status-pass {
      background: rgba(52, 199, 89, 0.1);
      color: var(--success-color);
    }
    
    &.status-fail {
      background: rgba(255, 59, 48, 0.1);
      color: var(--error-color);
    }
  }
}
</style>
