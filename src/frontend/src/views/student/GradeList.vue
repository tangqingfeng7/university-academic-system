<template>
  <div class="grade-list-container">
    <el-card class="search-card">
      <el-form :inline="true">
        <el-form-item label="学期筛选">
          <el-select
            v-model="selectedSemester"
            placeholder="请选择学期（默认显示全部）"
            clearable
            @change="handleSemesterChange"
            style="width: 250px"
          >
            <el-option label="全部学期" :value="null" />
            <el-option
              v-for="semester in semesters"
              :key="semester.id"
              :label="semester.academicYear + ' ' + getSemesterTypeName(semester.semesterType)"
              :value="semester.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 统计卡片 -->
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-item">
            <div class="stat-label">总课程数</div>
            <div class="stat-value">{{ statistics.totalCourses || 0 }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-item">
            <div class="stat-label">已通过课程</div>
            <div class="stat-value success">{{ statistics.passedCourses || 0 }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-item">
            <div class="stat-label">总学分</div>
            <div class="stat-value">{{ statistics.totalCredits || 0 }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-item">
            <div class="stat-label">平均绩点</div>
            <div class="stat-value primary">{{ formatGPA(statistics.gpa || statistics.overallGPA) }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 成绩列表 -->
    <el-card style="margin-top: 20px">
      <template #header>
        <div class="card-header">
          <span>成绩列表</span>
          <el-button type="primary" @click="handleExport" :loading="exportLoading">
            <el-icon><Download /></el-icon>
            导出成绩单
          </el-button>
        </div>
      </template>

      <el-table
        v-loading="loading"
        :data="gradeList"
        stripe
        style="width: 100%"
      >
        <el-table-column type="index" label="序号" width="60" />
        
        <el-table-column prop="semesterName" label="学期" width="150">
          <template #default="{ row }">
            {{ row.semesterName }}
          </template>
        </el-table-column>

        <el-table-column prop="courseNo" label="课程编号" width="120" />
        
        <el-table-column prop="courseName" label="课程名称" min-width="200" />
        
        <el-table-column prop="courseType" label="课程类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getCourseTypeTag(row.courseType)">
              {{ getCourseTypeName(row.courseType) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="credits" label="学分" width="80" align="center" />

        <el-table-column prop="regularScore" label="平时成绩" width="100" align="center">
          <template #default="{ row }">
            {{ formatScore(row.regularScore) }}
          </template>
        </el-table-column>

        <el-table-column prop="midtermScore" label="期中成绩" width="100" align="center">
          <template #default="{ row }">
            {{ formatScore(row.midtermScore) }}
          </template>
        </el-table-column>

        <el-table-column prop="finalScore" label="期末成绩" width="100" align="center">
          <template #default="{ row }">
            {{ formatScore(row.finalScore) }}
          </template>
        </el-table-column>

        <el-table-column prop="totalScore" label="总评成绩" width="100" align="center">
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
            <el-tag :type="row.passed ? 'success' : 'danger'">
              {{ row.passed ? '通过' : '不通过' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="teacherName" label="授课教师" width="120" />
      </el-table>

      <div v-if="gradeList.length === 0 && !loading" class="empty-text">
        暂无成绩记录
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
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
      // 获取指定学期成绩
      res = await getSemesterGrades(selectedSemester.value)
    } else {
      // 获取所有成绩
      res = await getMyGrades()
    }
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
    let res
    if (selectedSemester.value) {
      // 获取学期统计
      res = await getSemesterStatistics(selectedSemester.value)
    } else {
      // 获取总统计
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

// 格式化学期类型
const getSemesterTypeName = (type) => {
  const typeMap = {
    1: '春季学期',
    2: '秋季学期'
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

<style scoped>
.grade-list-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stat-card {
  text-align: center;
}

.stat-item {
  padding: 10px 0;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}

.stat-value.success {
  color: #67C23A;
}

.stat-value.primary {
  color: #409EFF;
}

.empty-text {
  text-align: center;
  padding: 40px 0;
  color: #909399;
  font-size: 14px;
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

