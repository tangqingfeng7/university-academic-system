<template>
  <div class="exam-detail-container">
    <!-- 面包屑导航 -->
    <el-breadcrumb separator="/" class="breadcrumb">
      <el-breadcrumb-item :to="{ path: '/admin' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item :to="{ path: '/admin/exam' }">考试管理</el-breadcrumb-item>
      <el-breadcrumb-item>考试详情</el-breadcrumb-item>
    </el-breadcrumb>

    <!-- 加载中 -->
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="10" animated />
    </div>

    <!-- 考试详情 -->
    <div v-else-if="exam">
      <!-- 基本信息卡片 -->
      <el-card class="info-card">
        <template #header>
          <div class="card-header">
            <div class="header-left">
              <span class="exam-name">{{ exam.name }}</span>
              <exam-status-tag 
                :status="exam.status" 
                :statusDescription="exam.statusDescription" 
                size="large"
              />
            </div>
            <div class="header-right">
              <!-- 操作按钮 -->
              <el-button 
                type="primary" 
                @click="handleEdit"
                v-if="exam.status === 'DRAFT'"
              >
                <el-icon><Edit /></el-icon>
                编辑
              </el-button>
              <el-button 
                type="success" 
                @click="handlePublish"
                v-if="exam.status === 'DRAFT'"
              >
                <el-icon><Promotion /></el-icon>
                发布考试
              </el-button>
              <el-button 
                type="warning" 
                @click="handleCancel"
                v-if="exam.status === 'PUBLISHED'"
              >
                <el-icon><CloseBold /></el-icon>
                取消考试
              </el-button>
              <el-button 
                type="info" 
                @click="handleCheckConflicts"
              >
                <el-icon><Warning /></el-icon>
                冲突检测
              </el-button>
              <el-button 
                type="primary" 
                @click="handleExport"
                v-if="exam.status !== 'DRAFT'"
              >
                <el-icon><Download /></el-icon>
                导出安排
              </el-button>
            </div>
          </div>
        </template>

        <!-- 考试基本信息 -->
        <el-descriptions :column="3" border>
          <el-descriptions-item label="考试名称" :span="2">
            {{ exam.name }}
          </el-descriptions-item>
          <el-descriptions-item label="考试类型">
            <exam-type-tag :type="exam.type" :typeDescription="exam.typeDescription" />
          </el-descriptions-item>

          <el-descriptions-item label="关联课程">
            {{ exam.courseOffering?.course?.courseNo }} - {{ exam.courseOffering?.course?.name }}
          </el-descriptions-item>
          <el-descriptions-item label="任课教师">
            {{ exam.courseOffering?.teacher?.name }}
          </el-descriptions-item>
          <el-descriptions-item label="所属学期">
            {{ exam.courseOffering?.semester?.semesterName }}
          </el-descriptions-item>

          <el-descriptions-item label="考试时间" :span="2">
            <exam-time-display 
              :examTime="exam.examTime" 
              :duration="exam.duration" 
            />
          </el-descriptions-item>
          <el-descriptions-item label="总分">
            {{ exam.totalScore }} 分
          </el-descriptions-item>

          <el-descriptions-item label="考场数量">
            <el-tag type="info">{{ exam.totalRooms || 0 }} 个考场</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="考试人数">
            <el-tag type="info">{{ exam.totalStudents || 0 }} 人</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ formatDateTime(exam.createdAt) }}
          </el-descriptions-item>

          <el-descriptions-item label="考试说明" :span="3" v-if="exam.description">
            {{ exam.description }}
          </el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- Tab页签 -->
      <el-card class="tabs-card">
        <el-tabs v-model="activeTab" @tab-change="handleTabChange">
          <!-- 考场管理 -->
          <el-tab-pane label="考场管理" name="rooms">
            <exam-room-manager 
              :exam-id="examId" 
              :exam-status="exam.status"
              @refresh="fetchExamDetail"
            />
          </el-tab-pane>

          <!-- 学生分配 -->
          <el-tab-pane label="学生分配" name="students">
            <exam-student-assignment 
              :exam-id="examId" 
              :exam-status="exam.status"
              @refresh="fetchExamDetail"
            />
          </el-tab-pane>

          <!-- 监考管理 -->
          <el-tab-pane label="监考管理" name="invigilators">
            <exam-invigilator-manager 
              :exam-id="examId" 
              :exam-status="exam.status"
              @refresh="fetchExamDetail"
            />
          </el-tab-pane>
        </el-tabs>
      </el-card>
    </div>

    <!-- 冲突检测对话框 -->
    <el-dialog
      v-model="conflictDialogVisible"
      title="考试冲突检测"
      width="800px"
    >
      <div v-if="conflictLoading" class="text-center">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span style="margin-left: 10px">检测中...</span>
      </div>
      <div v-else>
        <el-alert
          v-if="conflicts.length === 0"
          title="未发现冲突"
          type="success"
          description="该考试没有时间冲突问题"
          :closable="false"
        />
        <div v-else>
          <el-alert
            :title="`发现 ${conflicts.length} 个冲突`"
            type="warning"
            :closable="false"
            style="margin-bottom: 20px"
          />
          <el-table :data="conflicts" border>
            <el-table-column prop="studentNo" label="学号" width="120" />
            <el-table-column prop="studentName" label="姓名" width="100" />
            <el-table-column label="冲突考试" min-width="200">
              <template #default="{ row }">
                {{ row.conflictExamName }}
              </template>
            </el-table-column>
            <el-table-column label="冲突时间" width="180">
              <template #default="{ row }">
                {{ formatDateTime(row.conflictExamTime) }}
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
      <template #footer>
        <el-button @click="conflictDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Edit, 
  Promotion, 
  CloseBold, 
  Warning, 
  Download, 
  Loading 
} from '@element-plus/icons-vue'
import ExamStatusTag from '@/components/ExamStatusTag.vue'
import ExamTypeTag from '@/components/ExamTypeTag.vue'
import ExamTimeDisplay from '@/components/ExamTimeDisplay.vue'
import ExamRoomManager from './components/ExamRoomManager.vue'
import ExamStudentAssignment from './components/ExamStudentAssignment.vue'
import ExamInvigilatorManager from './components/ExamInvigilatorManager.vue'
import { 
  getExamById, 
  publishExam, 
  cancelExam,
  getExamConflicts,
  downloadExamList
} from '@/api/exam'

const route = useRoute()
const router = useRouter()

// 考试ID
const examId = computed(() => parseInt(route.params.id))

// 数据
const exam = ref(null)
const loading = ref(true)

// 当前激活的Tab
const activeTab = ref('rooms')

// 冲突检测
const conflictDialogVisible = ref(false)
const conflictLoading = ref(false)
const conflicts = ref([])

// ==================== 数据加载 ====================

// 获取考试详情
const fetchExamDetail = async () => {
  try {
    loading.value = true
    const res = await getExamById(examId.value)
    exam.value = res.data
  } catch (error) {
    console.error('获取考试详情失败:', error)
    ElMessage.error('获取考试详情失败')
    router.back()
  } finally {
    loading.value = false
  }
}

// ==================== 事件处理 ====================

// 编辑考试
const handleEdit = () => {
  router.push(`/admin/exam/${examId.value}/edit`)
}

// 发布考试
const handlePublish = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要发布考试"${exam.value.name}"吗？发布后将通知所有相关学生和教师。`,
      '发布考试',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await publishExam(examId.value)
    ElMessage.success('考试发布成功')
    await fetchExamDetail()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('发布考试失败:', error)
      ElMessage.error(error.message || '发布考试失败')
    }
  }
}

// 取消考试
const handleCancel = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要取消考试"${exam.value.name}"吗？取消后将通知所有相关人员。`,
      '取消考试',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await cancelExam(examId.value)
    ElMessage.success('考试已取消')
    await fetchExamDetail()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消考试失败:', error)
      ElMessage.error(error.message || '取消考试失败')
    }
  }
}

// 冲突检测
const handleCheckConflicts = async () => {
  conflictDialogVisible.value = true
  conflictLoading.value = true
  
  try {
    const res = await getExamConflicts(examId.value)
    conflicts.value = res.data || []
  } catch (error) {
    console.error('冲突检测失败:', error)
    ElMessage.error('冲突检测失败')
  } finally {
    conflictLoading.value = false
  }
}

// 导出安排
const handleExport = async () => {
  try {
    ElMessage.info('正在生成导出文件...')
    await downloadExamList([examId.value])
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error(error.message || '导出失败')
  }
}

// Tab切换
const handleTabChange = (tabName) => {
  console.log('切换到Tab:', tabName)
}

// ==================== 工具函数 ====================

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  const date = new Date(dateTime)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}`
}

// ==================== 生命周期 ====================

onMounted(() => {
  fetchExamDetail()
})
</script>

<style scoped>
.exam-detail-container {
  padding: 20px;
}

.breadcrumb {
  margin-bottom: 20px;
}

.loading-container {
  padding: 40px;
}

.info-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.exam-name {
  font-size: 20px;
  font-weight: 600;
}

.header-right {
  display: flex;
  gap: 10px;
}

.tabs-card {
  margin-bottom: 20px;
}

.text-center {
  text-align: center;
  padding: 40px;
  color: #909399;
}

:deep(.el-descriptions__label) {
  width: 120px;
  font-weight: 600;
}
</style>

