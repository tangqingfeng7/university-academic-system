<template>
  <div class="my-evaluations-page">
    <!-- 页面头部 -->
    <div class="page-header animate-fade-in-down">
      <div class="header-content">
        <h1 class="page-title">我的评价</h1>
        <p class="page-subtitle">查看和管理你提交的课程评价</p>
      </div>
      <el-button 
        type="primary" 
        :icon="Plus"
        size="large"
        @click="$router.push('/student/evaluations/available')"
      >
        评价课程
      </el-button>
    </div>

    <!-- 标签页 -->
    <div class="tabs-section animate-fade-in-up" style="animation-delay: 0.1s;">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="课程评价" name="course">
          <!-- 课程评价列表 -->
          <div v-loading="loading" class="evaluations-list">
            <el-empty 
              v-if="courseEvaluations.length === 0 && !loading"
              description="暂无课程评价"
            >
              <el-button type="primary" @click="$router.push('/student/evaluations/available')">
                去评价
              </el-button>
            </el-empty>

            <div v-else class="evaluation-cards">
              <div 
                v-for="evaluation in courseEvaluations" 
                :key="evaluation.id"
                class="evaluation-card"
              >
                <div class="card-header">
                  <div class="course-info">
                    <h3>{{ evaluation.courseName }}</h3>
                    <span class="course-code">{{ evaluation.courseNo }}</span>
                  </div>
                  <el-tag :type="evaluation.flagged ? 'warning' : 'success'">
                    {{ evaluation.flagged ? '已标记' : evaluation.statusDescription }}
                  </el-tag>
                </div>

                <div class="card-body">
                  <div class="info-row">
                    <span class="label">教师：</span>
                    <span>{{ evaluation.teacherName }}</span>
                  </div>
                  <div class="info-row">
                    <span class="label">学期：</span>
                    <span>{{ evaluation.semesterName }}</span>
                  </div>
                  <div class="info-row">
                    <span class="label">评分：</span>
                    <el-rate v-model="evaluation.rating" disabled show-score />
                  </div>
                  <div v-if="evaluation.comment" class="comment-section">
                    <span class="label">评论：</span>
                    <p class="comment-text">{{ evaluation.comment }}</p>
                  </div>
                  <div class="info-row">
                    <span class="label">匿名：</span>
                    <el-tag :type="evaluation.anonymous ? 'info' : 'success'" size="small">
                      {{ evaluation.anonymous ? '匿名' : '实名' }}
                    </el-tag>
                  </div>
                  <div v-if="evaluation.flagged && evaluation.moderationNote" class="warning-section">
                    <el-alert
                      type="warning"
                      :closable="false"
                      :title="evaluation.moderationNote"
                    />
                  </div>
                </div>

                <div class="card-footer">
                  <span class="time-text">{{ formatTime(evaluation.createdAt) }}</span>
                  <el-button 
                    v-if="canEdit"
                    text 
                    type="primary"
                    :icon="Edit"
                    @click="handleEdit(evaluation)"
                  >
                    修改
                  </el-button>
                </div>
              </div>
            </div>

            <!-- 分页 -->
            <div v-if="courseTotal > 0" class="pagination-section">
              <el-pagination
                v-model:current-page="coursePage"
                v-model:page-size="pageSize"
                :total="courseTotal"
                layout="total, prev, pager, next"
                @current-change="loadCourseEvaluations"
              />
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="教师评价" name="teacher">
          <!-- 教师评价列表 -->
          <div v-loading="loading" class="evaluations-list">
            <el-empty 
              v-if="teacherEvaluations.length === 0 && !loading"
              description="暂无教师评价"
            />

            <div v-else class="evaluation-cards">
              <div 
                v-for="evaluation in teacherEvaluations" 
                :key="evaluation.id"
                class="evaluation-card"
              >
                <div class="card-header">
                  <div class="course-info">
                    <h3>{{ evaluation.teacherName }}</h3>
                    <span class="course-code">{{ evaluation.courseName }}</span>
                  </div>
                  <el-tag :type="evaluation.flagged ? 'warning' : 'success'">
                    {{ evaluation.flagged ? '已标记' : evaluation.statusDescription }}
                  </el-tag>
                </div>

                <div class="card-body">
                  <div class="info-row">
                    <span class="label">课程：</span>
                    <span>{{ evaluation.courseName }}</span>
                  </div>
                  <div class="rating-grid">
                    <div class="rating-item">
                      <span class="rating-label">教学能力</span>
                      <el-rate v-model="evaluation.teachingRating" disabled />
                    </div>
                    <div class="rating-item">
                      <span class="rating-label">教学态度</span>
                      <el-rate v-model="evaluation.attitudeRating" disabled />
                    </div>
                    <div class="rating-item">
                      <span class="rating-label">教学内容</span>
                      <el-rate v-model="evaluation.contentRating" disabled />
                    </div>
                  </div>
                  <div v-if="evaluation.comment" class="comment-section">
                    <span class="label">评论：</span>
                    <p class="comment-text">{{ evaluation.comment }}</p>
                  </div>
                  <div class="info-row">
                    <span class="label">匿名：</span>
                    <el-tag :type="evaluation.anonymous ? 'info' : 'success'" size="small">
                      {{ evaluation.anonymous ? '匿名' : '实名' }}
                    </el-tag>
                  </div>
                  <div v-if="evaluation.flagged && evaluation.moderationNote" class="warning-section">
                    <el-alert
                      type="warning"
                      :closable="false"
                      :title="evaluation.moderationNote"
                    />
                  </div>
                </div>

                <div class="card-footer">
                  <span class="time-text">{{ formatTime(evaluation.createdAt) }}</span>
                </div>
              </div>
            </div>

            <!-- 分页 -->
            <div v-if="teacherTotal > 0" class="pagination-section">
              <el-pagination
                v-model:current-page="teacherPage"
                v-model:page-size="pageSize"
                :total="teacherTotal"
                layout="total, prev, pager, next"
                @current-change="loadTeacherEvaluations"
              />
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 编辑对话框 -->
    <EditEvaluationDialog
      v-model="editDialogVisible"
      :evaluation="currentEvaluation"
      @success="handleEditSuccess"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Edit } from '@element-plus/icons-vue'
import { getMyCourseEvaluations, getMyTeacherEvaluations, getActivePeriod } from '@/api/evaluation'
import EditEvaluationDialog from './components/EditEvaluationDialog.vue'

const loading = ref(false)
const activeTab = ref('course')
const coursePage = ref(1)
const teacherPage = ref(1)
const pageSize = ref(10)
const courseTotal = ref(0)
const teacherTotal = ref(0)
const courseEvaluations = ref([])
const teacherEvaluations = ref([])
const editDialogVisible = ref(false)
const currentEvaluation = ref(null)
const activePeriod = ref(null)

// 是否可编辑（评价周期内）
const canEdit = computed(() => {
  return activePeriod.value && activePeriod.value.active
})

// 加载活跃评价周期
const loadActivePeriod = async () => {
  try {
    const res = await getActivePeriod()
    if (res.code === 200) {
      activePeriod.value = res.data
    }
  } catch (error) {
    // 忽略错误，不影响页面显示
  }
}

// 加载课程评价
const loadCourseEvaluations = async () => {
  loading.value = true
  try {
    const res = await getMyCourseEvaluations({
      page: coursePage.value - 1,
      size: pageSize.value
    })
    if (res.code === 200) {
      courseEvaluations.value = res.data.content || []
      courseTotal.value = res.data.totalElements || 0
    }
  } catch (error) {
    ElMessage.error('加载课程评价失败')
  } finally {
    loading.value = false
  }
}

// 加载教师评价
const loadTeacherEvaluations = async () => {
  loading.value = true
  try {
    const res = await getMyTeacherEvaluations({
      page: teacherPage.value - 1,
      size: pageSize.value
    })
    if (res.code === 200) {
      teacherEvaluations.value = res.data.content || []
      teacherTotal.value = res.data.totalElements || 0
    }
  } catch (error) {
    ElMessage.error('加载教师评价失败')
  } finally {
    loading.value = false
  }
}

// 标签页切换
const handleTabChange = (tabName) => {
  if (tabName === 'course') {
    loadCourseEvaluations()
  } else {
    loadTeacherEvaluations()
  }
}

// 编辑评价
const handleEdit = (evaluation) => {
  currentEvaluation.value = evaluation
  editDialogVisible.value = true
}

// 编辑成功
const handleEditSuccess = () => {
  ElMessage.success('评价修改成功')
  if (activeTab.value === 'course') {
    loadCourseEvaluations()
  } else {
    loadTeacherEvaluations()
  }
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
  loadActivePeriod()
  loadCourseEvaluations()
})
</script>

<style scoped>
.my-evaluations-page {
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

.tabs-section {
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
  gap: 16px;
}

.evaluation-card {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 20px;
  transition: all 0.3s ease;
}

.evaluation-card:hover {
  border-color: #409eff;
  box-shadow: 0 2px 12px rgba(64, 158, 255, 0.12);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e4e7ed;
}

.course-info h3 {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 4px 0;
}

.course-code {
  font-size: 12px;
  color: #909399;
}

.card-body {
  margin-bottom: 16px;
}

.info-row {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
  font-size: 14px;
}

.label {
  color: #909399;
  margin-right: 8px;
  min-width: 60px;
}

.rating-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin: 16px 0;
}

.rating-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.rating-label {
  font-size: 12px;
  color: #909399;
}

.comment-section {
  margin-top: 12px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 6px;
}

.comment-text {
  margin: 8px 0 0 0;
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}

.warning-section {
  margin-top: 12px;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16px;
  border-top: 1px solid #e4e7ed;
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

