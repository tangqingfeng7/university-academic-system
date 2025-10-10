<template>
  <div class="evaluation-data-query-page">
    <!-- 页面头部 -->
    <div class="page-header animate-fade-in-down">
      <div class="header-content">
        <h1 class="page-title">评价数据查询</h1>
        <p class="page-subtitle">查看和管理所有教学评价数据</p>
      </div>
      <el-button 
        type="primary" 
        :icon="Download"
        size="large"
        :loading="exporting"
        @click="handleExport"
      >
        导出数据
      </el-button>
    </div>

    <!-- 筛选器 -->
    <div class="filter-section animate-fade-in-up" style="animation-delay: 0.1s;">
      <el-form :inline="true" :model="filterForm" class="filter-form">
        <el-form-item label="评价类型">
          <el-select 
            v-model="filterForm.type" 
            placeholder="全部"
            style="width: 150px"
            @change="handleFilterChange"
          >
            <el-option label="全部" value="" />
            <el-option label="课程评价" value="course" />
            <el-option label="教师评价" value="teacher" />
          </el-select>
        </el-form-item>

        <el-form-item label="学期">
          <el-select 
            v-model="filterForm.semesterId" 
            placeholder="选择学期"
            clearable
            style="width: 200px"
            @change="handleFilterChange"
          >
            <el-option
              v-for="semester in semesters"
              :key="semester.id"
              :label="semester.academicYear + ' ' + getSemesterTypeName(semester.semesterType)"
              :value="semester.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="状态">
          <el-select 
            v-model="filterForm.flagged" 
            placeholder="全部"
            clearable
            style="width: 150px"
            @change="handleFilterChange"
          >
            <el-option label="全部" value="" />
            <el-option label="正常" :value="false" />
            <el-option label="已标记" :value="true" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :icon="Search" @click="loadEvaluations">
            查询
          </el-button>
          <el-button :icon="Refresh" @click="handleReset">
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 标签页 -->
    <div class="tabs-section animate-fade-in-up" style="animation-delay: 0.2s;">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <!-- 课程评价 -->
        <el-tab-pane label="课程评价" name="course">
          <div v-loading="loading" class="evaluations-table">
            <el-table
              :data="courseEvaluations"
              style="width: 100%"
              :default-sort="{ prop: 'createdAt', order: 'descending' }"
            >
              <el-table-column prop="courseName" label="课程名称" min-width="150" />
              <el-table-column prop="teacherName" label="教师" width="100" />
              <el-table-column prop="studentName" label="学生" width="120">
                <template #default="{ row }">
                  {{ row.anonymous ? '匿名学生' : row.studentName }}
                </template>
              </el-table-column>
              <el-table-column prop="rating" label="评分" width="120" align="center">
                <template #default="{ row }">
                  <el-rate :model-value="row.rating" disabled show-score />
                </template>
              </el-table-column>
              <el-table-column prop="anonymous" label="匿名" width="80" align="center">
                <template #default="{ row }">
                  <el-tag :type="row.anonymous ? 'info' : 'success'" size="small">
                    {{ row.anonymous ? '匿名' : '实名' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="flagged" label="状态" width="90" align="center">
                <template #default="{ row }">
                  <el-tag :type="row.flagged ? 'warning' : 'success'" size="small">
                    {{ row.flagged ? '已标记' : '正常' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="createdAt" label="评价时间" width="180">
                <template #default="{ row }">
                  {{ formatDateTime(row.createdAt) }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="120" fixed="right">
                <template #default="{ row }">
                  <el-button type="primary" text @click="handleViewDetail(row, 'course')">
                    查看详情
                  </el-button>
                </template>
              </el-table-column>
            </el-table>

            <!-- 分页 -->
            <div class="pagination-section">
              <el-pagination
                v-model:current-page="coursePage"
                v-model:page-size="pageSize"
                :total="courseTotal"
                layout="total, sizes, prev, pager, next, jumper"
                :page-sizes="[10, 20, 50, 100]"
                @current-change="loadCourseEvaluations"
                @size-change="loadCourseEvaluations"
              />
            </div>
          </div>
        </el-tab-pane>

        <!-- 教师评价 -->
        <el-tab-pane label="教师评价" name="teacher">
          <div v-loading="loading" class="evaluations-table">
            <el-table
              :data="teacherEvaluations"
              style="width: 100%"
              :default-sort="{ prop: 'createdAt', order: 'descending' }"
            >
              <el-table-column prop="teacherName" label="教师" width="100" />
              <el-table-column prop="courseName" label="课程名称" min-width="150" />
              <el-table-column prop="studentName" label="学生" width="120">
                <template #default="{ row }">
                  {{ row.anonymous ? '匿名学生' : row.studentName }}
                </template>
              </el-table-column>
              <el-table-column label="评分详情" width="300">
                <template #default="{ row }">
                  <div class="rating-details">
                    <span>能力: {{ row.teachingRating }}</span>
                    <span>态度: {{ row.attitudeRating }}</span>
                    <span>内容: {{ row.contentRating }}</span>
                    <span class="avg">平均: {{ calculateAvg(row) }}</span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="anonymous" label="匿名" width="80" align="center">
                <template #default="{ row }">
                  <el-tag :type="row.anonymous ? 'info' : 'success'" size="small">
                    {{ row.anonymous ? '匿名' : '实名' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="flagged" label="状态" width="90" align="center">
                <template #default="{ row }">
                  <el-tag :type="row.flagged ? 'warning' : 'success'" size="small">
                    {{ row.flagged ? '已标记' : '正常' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="createdAt" label="评价时间" width="180">
                <template #default="{ row }">
                  {{ formatDateTime(row.createdAt) }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="120" fixed="right">
                <template #default="{ row }">
                  <el-button type="primary" text @click="handleViewDetail(row, 'teacher')">
                    查看详情
                  </el-button>
                </template>
              </el-table-column>
            </el-table>

            <!-- 分页 -->
            <div class="pagination-section">
              <el-pagination
                v-model:current-page="teacherPage"
                v-model:page-size="pageSize"
                :total="teacherTotal"
                layout="total, sizes, prev, pager, next, jumper"
                :page-sizes="[10, 20, 50, 100]"
                @current-change="loadTeacherEvaluations"
                @size-change="loadTeacherEvaluations"
              />
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      :title="detailDialogTitle"
      width="700px"
    >
      <div v-if="currentDetail" class="detail-content">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="课程名称" :span="2">
            {{ currentDetail.courseName }}
          </el-descriptions-item>
          <el-descriptions-item label="教师">
            {{ currentDetail.teacherName }}
          </el-descriptions-item>
          <el-descriptions-item label="学期">
            {{ currentDetail.semesterName }}
          </el-descriptions-item>
          <el-descriptions-item label="学生" :span="2">
            {{ currentDetail.anonymous ? '匿名学生' : currentDetail.studentName }}
          </el-descriptions-item>
          
          <template v-if="detailType === 'course'">
            <el-descriptions-item label="课程评分" :span="2">
              <el-rate :model-value="currentDetail.rating" disabled show-score />
            </el-descriptions-item>
          </template>
          
          <template v-if="detailType === 'teacher'">
            <el-descriptions-item label="教学能力">
              <el-rate :model-value="currentDetail.teachingRating" disabled />
            </el-descriptions-item>
            <el-descriptions-item label="教学态度">
              <el-rate :model-value="currentDetail.attitudeRating" disabled />
            </el-descriptions-item>
            <el-descriptions-item label="教学内容" :span="2">
              <el-rate :model-value="currentDetail.contentRating" disabled />
            </el-descriptions-item>
          </template>
          
          <el-descriptions-item label="匿名评价">
            <el-tag :type="currentDetail.anonymous ? 'info' : 'success'">
              {{ currentDetail.anonymous ? '是' : '否' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="currentDetail.flagged ? 'warning' : 'success'">
              {{ currentDetail.flagged ? '已标记' : '正常' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="评价时间" :span="2">
            {{ formatDateTime(currentDetail.createdAt) }}
          </el-descriptions-item>
        </el-descriptions>

        <div v-if="currentDetail.comment" class="comment-section">
          <h4>评价内容</h4>
          <div class="comment-box">
            {{ currentDetail.comment }}
          </div>
        </div>

        <div v-if="currentDetail.flagged && currentDetail.moderationNote" class="moderation-section">
          <h4>审核提示</h4>
          <el-alert
            :title="currentDetail.moderationNote"
            type="warning"
            :closable="false"
          />
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Download, Search, Refresh } from '@element-plus/icons-vue'
import { getCourseEvaluations, getTeacherEvaluations, exportEvaluations } from '@/api/evaluation'
import { getAllSemesters } from '@/api/semester'

const loading = ref(false)
const exporting = ref(false)
const activeTab = ref('course')
const semesters = ref([])

const filterForm = ref({
  type: '',
  semesterId: null,
  flagged: ''
})

const courseEvaluations = ref([])
const teacherEvaluations = ref([])
const coursePage = ref(1)
const teacherPage = ref(1)
const pageSize = ref(20)
const courseTotal = ref(0)
const teacherTotal = ref(0)

const detailDialogVisible = ref(false)
const detailDialogTitle = ref('')
const detailType = ref('')
const currentDetail = ref(null)

// 加载学期列表
const loadSemesters = async () => {
  try {
    const res = await getAllSemesters()
    if (res.code === 200) {
      semesters.value = res.data || []
    }
  } catch (error) {
    ElMessage.error('加载学期列表失败')
  }
}

// 加载课程评价
const loadCourseEvaluations = async () => {
  loading.value = true
  try {
    const res = await getCourseEvaluations({
      semesterId: filterForm.value.semesterId,
      page: coursePage.value - 1,
      size: pageSize.value
    })
    if (res.code === 200) {
      let data = res.data.content || []
      // 前端过滤flagged状态
      if (filterForm.value.flagged !== '') {
        data = data.filter(item => item.flagged === filterForm.value.flagged)
      }
      courseEvaluations.value = data
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
    const res = await getTeacherEvaluations({
      semesterId: filterForm.value.semesterId,
      page: teacherPage.value - 1,
      size: pageSize.value
    })
    if (res.code === 200) {
      let data = res.data.content || []
      // 前端过滤flagged状态
      if (filterForm.value.flagged !== '') {
        data = data.filter(item => item.flagged === filterForm.value.flagged)
      }
      teacherEvaluations.value = data
      teacherTotal.value = res.data.totalElements || 0
    }
  } catch (error) {
    ElMessage.error('加载教师评价失败')
  } finally {
    loading.value = false
  }
}

// 加载评价数据
const loadEvaluations = () => {
  if (activeTab.value === 'course') {
    loadCourseEvaluations()
  } else {
    loadTeacherEvaluations()
  }
}

// 标签页切换
const handleTabChange = (tabName) => {
  activeTab.value = tabName
  loadEvaluations()
}

// 筛选变更
const handleFilterChange = () => {
  coursePage.value = 1
  teacherPage.value = 1
  loadEvaluations()
}

// 重置筛选
const handleReset = () => {
  filterForm.value = {
    type: '',
    semesterId: null,
    flagged: ''
  }
  handleFilterChange()
}

// 查看详情
const handleViewDetail = (row, type) => {
  currentDetail.value = row
  detailType.value = type
  detailDialogTitle.value = type === 'course' ? '课程评价详情' : '教师评价详情'
  detailDialogVisible.value = true
}

// 导出数据
const handleExport = async () => {
  try {
    exporting.value = true
    const res = await exportEvaluations({
      type: activeTab.value,
      semesterId: filterForm.value.semesterId
    })
    
    // 创建下载链接
    const blob = new Blob([res], { 
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' 
    })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `评价数据_${activeTab.value}_${new Date().getTime()}.xlsx`
    link.click()
    window.URL.revokeObjectURL(url)
    
    ElMessage.success('导出成功')
  } catch (error) {
    ElMessage.error('导出失败')
  } finally {
    exporting.value = false
  }
}

// 计算平均分
const calculateAvg = (row) => {
  const avg = (row.teachingRating + row.attitudeRating + row.contentRating) / 3
  return avg.toFixed(1)
}

// 获取学期类型名称
const getSemesterTypeName = (type) => {
  return type === 1 ? '春季学期' : '秋季学期'
}

// 格式化日期时间
const formatDateTime = (datetime) => {
  if (!datetime) return ''
  const date = new Date(datetime)
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
  loadCourseEvaluations()
})
</script>

<style scoped>
.evaluation-data-query-page {
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
  background: white;
  border-radius: 12px;
  padding: 20px 24px;
  margin-bottom: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.filter-form {
  margin-bottom: 0;
}

.tabs-section {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.evaluations-table {
  min-height: 500px;
}

.rating-details {
  display: flex;
  gap: 16px;
  font-size: 13px;
}

.rating-details .avg {
  font-weight: 600;
  color: #409eff;
}

.pagination-section {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

.detail-content {
  padding: 8px 0;
}

.comment-section,
.moderation-section {
  margin-top: 24px;
}

.comment-section h4,
.moderation-section h4 {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 12px 0;
}

.comment-box {
  padding: 16px;
  background: #f5f7fa;
  border-radius: 6px;
  font-size: 14px;
  color: #606266;
  line-height: 1.8;
  white-space: pre-wrap;
  word-break: break-word;
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

