<template>
  <div class="transcript-page">
    <!-- 页面头部 -->
    <div class="page-header animate-fade-in-down">
      <div class="header-content">
        <h1 class="page-title">成绩单</h1>
        <p class="page-subtitle">你的完整学业记录和总体表现</p>
      </div>
      <el-button 
        type="primary" 
        :icon="Download" 
        size="large"
        :loading="exportLoading"
        @click="handleExport"
      >
        导出 PDF
      </el-button>
    </div>

    <!-- 总体统计 -->
    <div 
      class="overall-stats animate-fade-in-up" 
      style="animation-delay: 0.1s;"
      v-loading="loading"
    >
      <div class="stats-grid">
        <div class="stat-item">
          <div class="stat-icon">
            <el-icon :size="24"><Document /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ statistics.totalCourses || 0 }}</div>
            <div class="stat-label">课程总数</div>
          </div>
        </div>
        
        <div class="stat-item">
          <div class="stat-icon stat-success">
            <el-icon :size="24"><CircleCheck /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ statistics.passedCourses || 0 }}</div>
            <div class="stat-label">已通过</div>
          </div>
        </div>
        
        <div class="stat-item">
          <div class="stat-icon stat-warning">
            <el-icon :size="24"><Star /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ statistics.passedCredits || 0 }} / {{ statistics.totalCredits || 0 }}</div>
            <div class="stat-label">学分</div>
          </div>
        </div>
        
        <div class="stat-item">
          <div class="stat-icon stat-primary">
            <el-icon :size="24"><TrendCharts /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ formatGPA(statistics.overallGPA) }}</div>
            <div class="stat-label">平均绩点 GPA</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 按学期展示成绩 -->
    <div class="semesters-section">
      <div
        v-for="(semester, index) in semesterGrades"
        :key="semester.semesterId"
        class="semester-block animate-fade-in-up"
        :style="{ 'animation-delay': `${0.2 + index * 0.1}s` }"
      >
        <div class="semester-header">
          <div class="semester-info">
            <h2 class="semester-title">{{ semester.semesterName }}</h2>
            <div class="semester-meta">
              <span class="meta-item">{{ semester.courses.length }} 门课程</span>
              <span class="meta-divider">·</span>
              <span class="meta-item">{{ semester.totalCredits }} 学分</span>
              <span class="meta-divider">·</span>
              <span class="meta-item">平均分 {{ semester.avgScore.toFixed(1) }}</span>
            </div>
          </div>
        </div>

        <div class="semester-courses">
          <div
            v-for="course in semester.courses"
            :key="course.id"
            class="course-item"
          >
            <div class="course-main">
              <div class="course-info">
                <div class="course-name">{{ course.courseName }}</div>
                <div class="course-details">
                  <span class="course-no">{{ course.courseNo }}</span>
                  <span class="detail-divider">·</span>
                  <el-tag :type="getCourseTypeTag(course.courseType)" size="small">
                    {{ getCourseTypeName(course.courseType) }}
                  </el-tag>
                  <span class="detail-divider">·</span>
                  <span class="course-credits">{{ course.credits }} 学分</span>
                  <span class="detail-divider">·</span>
                  <span class="course-teacher">{{ course.teacherName }}</span>
                </div>
              </div>
              
              <div class="course-scores">
                <div class="score-item">
                  <div class="score-label">平时</div>
                  <div class="score-value">{{ formatScore(course.regularScore) }}</div>
                </div>
                <div class="score-item">
                  <div class="score-label">期中</div>
                  <div class="score-value">{{ formatScore(course.midtermScore) }}</div>
                </div>
                <div class="score-item">
                  <div class="score-label">期末</div>
                  <div class="score-value">{{ formatScore(course.finalScore) }}</div>
                </div>
                <div class="score-item score-total-item">
                  <div class="score-label">总评</div>
                  <div :class="['score-value', 'score-total', getScoreClass(course.totalScore)]">
                    {{ formatScore(course.totalScore) }}
                  </div>
                </div>
                <div class="score-item">
                  <div class="score-label">绩点</div>
                  <div class="score-value">{{ formatGradePoint(course.gradePoint) }}</div>
                </div>
                <div class="course-status">
                  <el-tag :type="course.passed ? 'success' : 'danger'" size="small">
                    {{ course.passed ? '通过' : '未通过' }}
                  </el-tag>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <el-empty
      v-if="!loading && semesterGrades.length === 0"
      description="暂无成绩记录"
      :image-size="200"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Download, Document, CircleCheck, Star, TrendCharts } from '@element-plus/icons-vue'
import { getMyGrades, getTranscriptStatistics, exportTranscript } from '@/api/grade'

// 数据
const loading = ref(false)
const exportLoading = ref(false)
const gradeList = ref([])
const statistics = ref({
  totalCourses: 0,
  passedCourses: 0,
  totalCredits: 0,
  passedCredits: 0,
  overallGPA: 0
})

// 按学期分组的成绩
const semesterGrades = computed(() => {
  const semesterMap = new Map()
  
  gradeList.value.forEach(grade => {
    const semesterId = grade.semesterId
    if (!semesterMap.has(semesterId)) {
      semesterMap.set(semesterId, {
        semesterId: semesterId,
        semesterName: grade.semesterName,
        courses: [],
        totalCredits: 0,
        avgScore: 0
      })
    }
    
    const semester = semesterMap.get(semesterId)
    semester.courses.push(grade)
    semester.totalCredits += grade.credits
  })
  
  // 计算每个学期的平均分
  semesterMap.forEach(semester => {
    const totalScore = semester.courses.reduce((sum, course) => sum + (course.totalScore || 0), 0)
    semester.avgScore = semester.courses.length > 0 ? totalScore / semester.courses.length : 0
  })
  
  // 转换为数组并按学期排序（最新的在前）
  return Array.from(semesterMap.values()).sort((a, b) => {
    return b.semesterId - a.semesterId
  })
})

// 获取成绩列表
const fetchGradeList = async () => {
  loading.value = true
  try {
    const res = await getMyGrades()
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
    const res = await getTranscriptStatistics()
    statistics.value = res.data || {}
  } catch (error) {
    console.error('获取统计信息失败:', error)
  }
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
  fetchGradeList()
  fetchStatistics()
})
</script>

<style scoped lang="scss">
.transcript-page {
  max-width: 1200px;
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
  // 总体统计
  // ===================================

  .overall-stats {
    background: var(--bg-primary);
    border-radius: var(--radius-xl);
    padding: var(--spacing-2xl);
    border: 1px solid var(--border-light);
    margin-bottom: var(--spacing-2xl);
  }

  .stats-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: var(--spacing-xl);

    @media (max-width: 1024px) {
      grid-template-columns: repeat(2, 1fr);
    }

    @media (max-width: 640px) {
      grid-template-columns: 1fr;
    }
  }

  .stat-item {
    display: flex;
    align-items: center;
    gap: 16px;
  }

  .stat-icon {
    width: 56px;
    height: 56px;
    border-radius: var(--radius-md);
    background: var(--bg-secondary);
    display: flex;
    align-items: center;
    justify-content: center;
    color: var(--text-secondary);

    &.stat-success {
      background: rgba(52, 199, 89, 0.1);
      color: var(--success-color);
    }

    &.stat-warning {
      background: rgba(255, 149, 0, 0.1);
      color: var(--warning-color);
    }

    &.stat-primary {
      background: rgba(0, 122, 255, 0.1);
      color: var(--primary-color);
    }
  }

  .stat-content {
    flex: 1;
  }

  .stat-value {
    font-size: 24px;
    font-weight: 600;
    color: var(--text-primary);
    margin-bottom: 4px;
    letter-spacing: -0.02em;
  }

  .stat-label {
    font-size: 14px;
    color: var(--text-secondary);
    font-weight: 500;
  }

  // ===================================
  // 学期区块
  // ===================================

  .semesters-section {
    display: flex;
    flex-direction: column;
    gap: var(--spacing-xl);
  }

  .semester-block {
    background: var(--bg-primary);
    border-radius: var(--radius-xl);
    border: 1px solid var(--border-light);
    overflow: hidden;
  }

  .semester-header {
    padding: var(--spacing-xl);
    border-bottom: 1px solid var(--border-light);
    background: var(--bg-secondary);
  }

  .semester-title {
    font-size: 21px;
    font-weight: 600;
    color: var(--text-primary);
    margin: 0 0 8px;
    letter-spacing: -0.01em;
  }

  .semester-meta {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 14px;
    color: var(--text-secondary);
  }

  .meta-item {
    font-weight: 500;
  }

  .meta-divider {
    color: var(--text-tertiary);
  }

  // ===================================
  // 课程列表
  // ===================================

  .semester-courses {
    padding: var(--spacing-md);
  }

  .course-item {
    padding: var(--spacing-lg);
    border-radius: var(--radius-md);
    transition: background var(--transition-fast);

    &:hover {
      background: var(--bg-secondary);
    }

    & + .course-item {
      border-top: 1px solid var(--border-light);
    }
  }

  .course-main {
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: var(--spacing-xl);

    @media (max-width: 768px) {
      flex-direction: column;
      align-items: flex-start;
    }
  }

  .course-info {
    flex: 1;
  }

  .course-name {
    font-size: 17px;
    font-weight: 600;
    color: var(--text-primary);
    margin-bottom: 8px;
    letter-spacing: -0.01em;
  }

  .course-details {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 14px;
    color: var(--text-secondary);
    flex-wrap: wrap;
  }

  .course-no {
    font-family: 'SF Mono', 'Monaco', 'Courier New', monospace;
    font-size: 13px;
  }

  .detail-divider {
    color: var(--text-tertiary);
  }

  .course-scores {
    display: flex;
    align-items: center;
    gap: 20px;

    @media (max-width: 768px) {
      width: 100%;
      justify-content: space-between;
    }
  }

  .score-item {
    text-align: center;
    min-width: 50px;
  }

  .score-label {
    font-size: 12px;
    color: var(--text-tertiary);
    margin-bottom: 4px;
  }

  .score-value {
    font-size: 16px;
    font-weight: 600;
    color: var(--text-primary);
  }

  .score-total-item {
    .score-total {
      font-size: 18px;
    }
  }

  .course-status {
    min-width: 70px;
    text-align: center;
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
}
</style>
