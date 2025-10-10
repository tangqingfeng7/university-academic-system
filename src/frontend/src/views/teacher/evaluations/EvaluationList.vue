<template>
  <div class="evaluation-list-page">
    <!-- 页面头部 -->
    <div class="page-header animate-fade-in-down">
      <div class="header-content">
        <h1 class="page-title">学生评价列表</h1>
        <p class="page-subtitle">查看学生对你的教学评价详情</p>
      </div>
      <el-button 
        type="primary" 
        :icon="ArrowLeft"
        @click="$router.back()"
      >
        返回统计
      </el-button>
    </div>

    <!-- 筛选器 -->
    <div class="filter-section animate-fade-in-up" style="animation-delay: 0.1s;">
      <el-select
        v-model="selectedSemester"
        placeholder="选择学期"
        clearable
        size="large"
        @change="loadEvaluations"
        style="width: 200px; margin-right: 12px;"
      >
        <el-option
          v-for="semester in semesters"
          :key="semester.id"
          :label="semester.academicYear + ' ' + getSemesterTypeName(semester.semesterType)"
          :value="semester.id"
        />
      </el-select>

      <el-select
        v-model="selectedCourse"
        placeholder="选择课程"
        clearable
        size="large"
        @change="loadEvaluations"
        style="width: 250px;"
      >
        <el-option
          v-for="course in myCourses"
          :key="course.id"
          :label="course.course.name"
          :value="course.id"
        />
      </el-select>
    </div>

    <!-- 评价列表 -->
    <div class="evaluations-container animate-fade-in-up" style="animation-delay: 0.2s;">
      <div v-loading="loading" class="evaluations-list">
        <el-empty 
          v-if="evaluations.length === 0 && !loading"
          description="暂无评价数据"
        />

        <div v-else class="evaluation-cards">
          <div 
            v-for="evaluation in evaluations" 
            :key="evaluation.id"
            class="evaluation-card"
          >
            <div class="card-header">
              <div class="course-info">
                <h3>{{ evaluation.courseName }}</h3>
                <div class="meta-info">
                  <span class="semester-tag">{{ evaluation.semesterName }}</span>
                  <el-tag 
                    :type="evaluation.flagged ? 'warning' : 'success'" 
                    size="small"
                  >
                    {{ evaluation.flagged ? '已标记' : '正常' }}
                  </el-tag>
                  <el-tag type="info" size="small">
                    {{ evaluation.anonymous ? '匿名' : '实名' }}
                  </el-tag>
                </div>
              </div>
              <div class="overall-rating">
                <div class="rating-value">{{ calculateOverallRating(evaluation).toFixed(1) }}</div>
                <div class="rating-stars">
                  <el-rate 
                    :model-value="calculateOverallRating(evaluation)" 
                    disabled 
                  />
                </div>
              </div>
            </div>

            <div class="card-body">
              <!-- 各维度评分 -->
              <div class="rating-grid">
                <div class="rating-item">
                  <div class="rating-label">
                    <el-icon><Management /></el-icon>
                    <span>教学能力</span>
                  </div>
                  <div class="rating-stars">
                    <el-rate :model-value="evaluation.teachingRating" disabled size="small" />
                    <span class="rating-score">{{ evaluation.teachingRating }}.0</span>
                  </div>
                </div>

                <div class="rating-item">
                  <div class="rating-label">
                    <el-icon><Checked /></el-icon>
                    <span>教学态度</span>
                  </div>
                  <div class="rating-stars">
                    <el-rate :model-value="evaluation.attitudeRating" disabled size="small" />
                    <span class="rating-score">{{ evaluation.attitudeRating }}.0</span>
                  </div>
                </div>

                <div class="rating-item">
                  <div class="rating-label">
                    <el-icon><Reading /></el-icon>
                    <span>教学内容</span>
                  </div>
                  <div class="rating-stars">
                    <el-rate :model-value="evaluation.contentRating" disabled size="small" />
                    <span class="rating-score">{{ evaluation.contentRating }}.0</span>
                  </div>
                </div>
              </div>

              <!-- 评价内容 -->
              <div v-if="evaluation.comment" class="comment-section">
                <div class="comment-header">
                  <el-icon><ChatLineRound /></el-icon>
                  <span>学生评论</span>
                </div>
                <p class="comment-text">{{ evaluation.comment }}</p>
              </div>

              <!-- 审核提示 -->
              <div v-if="evaluation.flagged && evaluation.moderationNote" class="warning-section">
                <el-alert
                  type="warning"
                  :closable="false"
                  :title="`审核提示：${evaluation.moderationNote}`"
                />
              </div>
            </div>

            <div class="card-footer">
              <span class="time-text">评价时间：{{ formatTime(evaluation.createdAt) }}</span>
            </div>
          </div>
        </div>

        <!-- 分页 -->
        <div v-if="total > 0" class="pagination-section">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :total="total"
            layout="total, prev, pager, next, jumper"
            @current-change="loadEvaluations"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  ArrowLeft, 
  Management, 
  Checked, 
  Reading, 
  ChatLineRound 
} from '@element-plus/icons-vue'
import { getMyTeacherEvaluationList } from '@/api/evaluation'
import { getAllSemesters } from '@/api/semester'
import { getMyCourses } from '@/api/teacherCourse'

const loading = ref(false)
const semesters = ref([])
const myCourses = ref([])
const selectedSemester = ref(null)
const selectedCourse = ref(null)
const evaluations = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 加载学期列表
const loadSemesters = async () => {
  try {
    const res = await getAllSemesters()
    if (res.code === 200) {
      semesters.value = res.data
    }
  } catch (error) {
    ElMessage.error('加载学期列表失败')
  }
}

// 加载我的课程
const loadMyCourses = async () => {
  try {
    const res = await getMyCourses()
    if (res.code === 200) {
      myCourses.value = res.data || []
    }
  } catch (error) {
    ElMessage.error('加载课程列表失败')
  }
}

// 加载评价列表
const loadEvaluations = async () => {
  loading.value = true
  try {
    const res = await getMyTeacherEvaluationList({
      semesterId: selectedSemester.value,
      page: currentPage.value - 1,
      size: pageSize.value
    })
    if (res.code === 200) {
      evaluations.value = res.data.content || []
      total.value = res.data.totalElements || 0
      
      // 如果选择了课程，进行前端过滤
      if (selectedCourse.value) {
        evaluations.value = evaluations.value.filter(
          e => e.courseOfferingId === selectedCourse.value
        )
      }
    }
  } catch (error) {
    ElMessage.error('加载评价列表失败')
  } finally {
    loading.value = false
  }
}

// 计算综合评分
const calculateOverallRating = (evaluation) => {
  return (
    (evaluation.teachingRating + evaluation.attitudeRating + evaluation.contentRating) / 3
  )
}

// 获取学期类型名称
const getSemesterTypeName = (type) => {
  return type === 1 ? '春季学期' : '秋季学期'
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleString('zh-CN', { 
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

onMounted(() => {
  loadSemesters()
  loadMyCourses()
  loadEvaluations()
})
</script>

<style scoped>
.evaluation-list-page {
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

.evaluations-container {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.evaluations-list {
  min-height: 400px;
}

.evaluation-cards {
  display: grid;
  gap: 20px;
  margin-bottom: 24px;
}

.evaluation-card {
  border: 1px solid #e4e7ed;
  border-radius: 12px;
  padding: 24px;
  transition: all 0.3s ease;
  background: white;
}

.evaluation-card:hover {
  border-color: #409eff;
  box-shadow: 0 4px 16px rgba(64, 158, 255, 0.15);
  transform: translateY(-2px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 2px solid #f0f2f5;
}

.course-info {
  flex: 1;
}

.course-info h3 {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 12px 0;
}

.meta-info {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.semester-tag {
  font-size: 12px;
  color: #909399;
  padding: 4px 12px;
  background: #f5f7fa;
  border-radius: 4px;
}

.overall-rating {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.rating-value {
  font-size: 36px;
  font-weight: 600;
  color: #1890ff;
  line-height: 1;
}

.card-body {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.rating-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
}

.rating-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
}

.rating-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

.rating-label .el-icon {
  font-size: 16px;
  color: #1890ff;
}

.rating-stars {
  display: flex;
  align-items: center;
  gap: 8px;
}

.rating-score {
  font-size: 14px;
  font-weight: 600;
  color: #1890ff;
}

.comment-section {
  padding: 16px;
  background: #f9fafb;
  border-left: 3px solid #1890ff;
  border-radius: 6px;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 12px;
  font-size: 14px;
  font-weight: 600;
  color: #606266;
}

.comment-header .el-icon {
  font-size: 16px;
  color: #1890ff;
}

.comment-text {
  margin: 0;
  font-size: 14px;
  color: #606266;
  line-height: 1.8;
  white-space: pre-wrap;
  word-break: break-word;
}

.warning-section {
  margin-top: 8px;
}

.card-footer {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #e4e7ed;
  display: flex;
  justify-content: flex-end;
}

.time-text {
  font-size: 12px;
  color: #909399;
}

.pagination-section {
  display: flex;
  justify-content: center;
  margin-top: 24px;
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

