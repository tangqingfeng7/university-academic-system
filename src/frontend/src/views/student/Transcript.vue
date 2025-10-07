<template>
  <div class="transcript-container">
    <!-- 标题和导出按钮 -->
    <el-card class="header-card">
      <div class="transcript-header">
        <div>
          <h2>学生成绩单</h2>
          <p class="subtitle">Student Transcript</p>
        </div>
        <el-button type="primary" @click="handleExport" :loading="exportLoading" size="large">
          <el-icon><Download /></el-icon>
          导出成绩单PDF
        </el-button>
      </div>
    </el-card>

    <!-- 总体统计卡片 -->
    <el-card class="statistics-card" v-loading="loading">
      <template #header>
        <span class="card-title">总体统计</span>
      </template>
      
      <el-row :gutter="30">
        <el-col :span="6">
          <div class="stat-box">
            <div class="stat-icon" style="background: #ecf5ff">
              <el-icon :size="30" color="#409EFF"><Document /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-label">总课程数</div>
              <div class="stat-value">{{ statistics.totalCourses || 0 }}</div>
            </div>
          </div>
        </el-col>
        
        <el-col :span="6">
          <div class="stat-box">
            <div class="stat-icon" style="background: #f0f9ff">
              <el-icon :size="30" color="#67C23A"><CircleCheck /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-label">已通过课程</div>
              <div class="stat-value success">{{ statistics.passedCourses || 0 }}</div>
            </div>
          </div>
        </el-col>
        
        <el-col :span="6">
          <div class="stat-box">
            <div class="stat-icon" style="background: #fef0f0">
              <el-icon :size="30" color="#E6A23C"><Star /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-label">总学分 / 已获得学分</div>
              <div class="stat-value">{{ statistics.passedCredits || 0 }} / {{ statistics.totalCredits || 0 }}</div>
            </div>
          </div>
        </el-col>
        
        <el-col :span="6">
          <div class="stat-box">
            <div class="stat-icon" style="background: #f4f4f5">
              <el-icon :size="30" color="#909399"><TrendCharts /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-label">总平均绩点 (GPA)</div>
              <div class="stat-value primary">{{ formatGPA(statistics.overallGPA) }}</div>
            </div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 按学期展示成绩 -->
    <div v-for="semester in semesterGrades" :key="semester.semesterId" class="semester-section">
      <el-card>
        <template #header>
          <div class="semester-header">
            <span class="semester-title">
              {{ semester.semesterName }}
            </span>
            <div class="semester-stats">
              <el-tag type="info" size="small">课程数: {{ semester.courses.length }}</el-tag>
              <el-tag type="success" size="small" style="margin-left: 10px">
                学分: {{ semester.totalCredits }}
              </el-tag>
              <el-tag type="warning" size="small" style="margin-left: 10px">
                平均分: {{ semester.avgScore.toFixed(1) }}
              </el-tag>
            </div>
          </div>
        </template>

        <el-table
          :data="semester.courses"
          stripe
          style="width: 100%"
        >
          <el-table-column type="index" label="序号" width="60" />
          
          <el-table-column prop="courseNo" label="课程编号" width="120" />
          
          <el-table-column prop="courseName" label="课程名称" min-width="180" />
          
          <el-table-column prop="courseType" label="类型" width="100">
            <template #default="{ row }">
              <el-tag :type="getCourseTypeTag(row.courseType)" size="small">
                {{ getCourseTypeName(row.courseType) }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column prop="credits" label="学分" width="80" align="center" />

          <el-table-column prop="regularScore" label="平时" width="80" align="center">
            <template #default="{ row }">
              {{ formatScore(row.regularScore) }}
            </template>
          </el-table-column>

          <el-table-column prop="midtermScore" label="期中" width="80" align="center">
            <template #default="{ row }">
              {{ formatScore(row.midtermScore) }}
            </template>
          </el-table-column>

          <el-table-column prop="finalScore" label="期末" width="80" align="center">
            <template #default="{ row }">
              {{ formatScore(row.finalScore) }}
            </template>
          </el-table-column>

          <el-table-column prop="totalScore" label="总评" width="80" align="center">
            <template #default="{ row }">
              <span :class="getScoreClass(row.totalScore)">
                {{ formatScore(row.totalScore) }}
              </span>
            </template>
          </el-table-column>

          <el-table-column prop="gradePoint" label="绩点" width="80" align="center">
            <template #default="{ row }">
              {{ formatGradePoint(row.gradePoint) }}
            </template>
          </el-table-column>

          <el-table-column label="状态" width="80" align="center">
            <template #default="{ row }">
              <el-tag :type="row.passed ? 'success' : 'danger'" size="small">
                {{ row.passed ? '通过' : '未通过' }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column prop="teacherName" label="授课教师" width="120" />
        </el-table>
      </el-card>
    </div>

    <!-- 空状态 -->
    <el-empty v-if="semesterGrades.length === 0 && !loading" description="暂无成绩记录" />
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
    ElMessage.error('获取成绩列表失败: ' + (error.message || '未知错误'))
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
    
    // 创建下载链接
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
    ElMessage.error('导出成绩单失败: ' + (error.message || '未知错误'))
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

<style scoped>
.transcript-container {
  padding: 20px;
}

.header-card {
  margin-bottom: 20px;
}

.transcript-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.transcript-header h2 {
  margin: 0 0 5px 0;
  font-size: 28px;
  color: #303133;
}

.subtitle {
  margin: 0;
  font-size: 14px;
  color: #909399;
  font-style: italic;
}

.statistics-card {
  margin-bottom: 30px;
}

.card-title {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
}

.stat-box {
  display: flex;
  align-items: center;
  padding: 15px;
  border-radius: 8px;
  background: #f5f7fa;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
}

.stat-content {
  flex: 1;
}

.stat-label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 26px;
  font-weight: bold;
  color: #303133;
}

.stat-value.success {
  color: #67C23A;
}

.stat-value.primary {
  color: #409EFF;
}

.semester-section {
  margin-bottom: 20px;
}

.semester-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.semester-title {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
}

.semester-stats {
  display: flex;
  align-items: center;
}

.score-excellent {
  color: #67C23A;
  font-weight: bold;
}

.score-good {
  color: #409EFF;
  font-weight: bold;
}

.score-pass {
  color: #E6A23C;
}

.score-fail {
  color: #F56C6C;
  font-weight: bold;
}
</style>

