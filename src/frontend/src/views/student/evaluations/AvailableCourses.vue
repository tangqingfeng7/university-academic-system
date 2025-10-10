<template>
  <div class="available-courses-page">
    <!-- 页面头部 -->
    <div class="page-header animate-fade-in-down">
      <div class="header-content">
        <h1 class="page-title">可评价课程</h1>
        <p class="page-subtitle">对已修课程进行评价，帮助提升教学质量</p>
      </div>
    </div>

    <!-- 学期选择 -->
    <div class="filter-section animate-fade-in-up" style="animation-delay: 0.1s;">
      <el-select
        v-model="selectedSemester"
        placeholder="选择学期"
        size="large"
        @change="loadAvailableCourses"
        style="width: 200px"
      >
        <el-option
          v-for="semester in semesters"
          :key="semester.id"
          :label="semester.academicYear + ' ' + getSemesterTypeName(semester.semesterType)"
          :value="semester.id"
        />
      </el-select>
    </div>

    <!-- 课程列表 -->
    <div class="courses-grid animate-fade-in-up" style="animation-delay: 0.2s;">
      <el-empty 
        v-if="!loading && courses.length === 0"
        description="暂无可评价的课程"
      >
        <el-button type="primary" @click="$router.push('/student/evaluations')">
          查看已评价课程
        </el-button>
      </el-empty>

      <div 
        v-else
        v-loading="loading"
        class="course-cards"
      >
        <div 
          v-for="course in courses" 
          :key="course.offeringId"
          class="course-card"
        >
          <div class="course-header">
            <div class="course-info">
              <h3 class="course-name">{{ course.courseName }}</h3>
              <p class="course-code">{{ course.courseNo }}</p>
            </div>
          </div>
          
          <div class="course-details">
            <div class="detail-item">
              <el-icon><User /></el-icon>
              <span>{{ course.teacherName }}</span>
            </div>
            <div class="detail-item">
              <el-icon><Clock /></el-icon>
              <span>{{ course.semesterName }}</span>
            </div>
          </div>

          <div class="course-actions">
            <el-button 
              type="primary" 
              :icon="Edit"
              @click="handleEvaluate(course)"
              size="large"
            >
              立即评价
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 评价对话框 -->
    <EvaluationDialog
      v-model="dialogVisible"
      :offering="currentOffering"
      @success="handleEvaluationSuccess"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { User, Clock, Location, Edit } from '@element-plus/icons-vue'
import { getAvailableCoursesForEvaluation } from '@/api/evaluation'
import { getAllSemesters } from '@/api/semester'
import EvaluationDialog from './components/EvaluationDialog.vue'

const loading = ref(false)
const courses = ref([])
const semesters = ref([])
const selectedSemester = ref(null)
const dialogVisible = ref(false)
const currentOffering = ref(null)

// 获取学期列表
const loadSemesters = async () => {
  try {
    const res = await getAllSemesters()
    if (res.code === 200) {
      semesters.value = res.data
      // 默认选择最新学期
      if (semesters.value.length > 0) {
        const activeSemester = semesters.value.find(s => s.active)
        selectedSemester.value = activeSemester ? activeSemester.id : semesters.value[0].id
        loadAvailableCourses()
      }
    }
  } catch (error) {
    ElMessage.error('加载学期列表失败')
  }
}

// 加载可评价课程
const loadAvailableCourses = async () => {
  if (!selectedSemester.value) return
  
  loading.value = true
  try {
    const res = await getAvailableCoursesForEvaluation(selectedSemester.value)
    if (res.code === 200) {
      courses.value = res.data || []
    }
  } catch (error) {
    ElMessage.error('加载可评价课程失败')
  } finally {
    loading.value = false
  }
}

// 根据学期ID获取学期名称
const getSemesterName = (semesterId) => {
  const semester = semesters.value.find(s => s.id === semesterId)
  return semester ? `${semester.academicYear} ${getSemesterTypeName(semester.semesterType)}` : ''
}

// 评价课程
const handleEvaluate = (course) => {
  // 转换数据格式以适配对话框
  currentOffering.value = {
    id: course.offeringId,
    course: {
      name: course.courseName,
      courseNo: course.courseNo
    },
    teacher: {
      id: course.teacherId,
      name: course.teacherName
    }
  }
  dialogVisible.value = true
}

// 评价成功
const handleEvaluationSuccess = () => {
  // 子组件已经显示了成功消息，这里只需要刷新数据
  loadAvailableCourses()
}

// 获取学期类型名称
const getSemesterTypeName = (type) => {
  return type === 1 ? '春季学期' : '秋季学期'
}

onMounted(() => {
  loadSemesters()
})
</script>

<style scoped>
.available-courses-page {
  padding: 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.header-content {
  flex: 1;
}

.page-title {
  font-size: 28px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 8px 0;
}

.page-subtitle {
  font-size: 14px;
  color: #666;
  margin: 0;
}

.filter-section {
  margin-bottom: 24px;
}

.courses-grid {
  min-height: 400px;
}

.course-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
}

.course-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
}

.course-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.12);
}

.course-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.course-info {
  flex: 1;
}

.course-name {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 8px 0;
}

.course-code {
  font-size: 14px;
  color: #666;
  margin: 0;
}

.course-details {
  margin-bottom: 20px;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 0;
  color: #666;
  font-size: 14px;
}

.detail-item .el-icon {
  font-size: 16px;
  color: #409eff;
}

.course-actions {
  display: flex;
  justify-content: flex-end;
}

.course-actions .el-button {
  width: 100%;
}

/* 动画 */
@keyframes fade-in-down {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes fade-in-up {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.animate-fade-in-down {
  animation: fade-in-down 0.5s ease-out;
}

.animate-fade-in-up {
  animation: fade-in-up 0.5s ease-out;
}
</style>

