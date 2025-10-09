<template>
  <div class="course-exam-detail-page">
    <!-- 页面头部 -->
    <div class="page-header animate-fade-in-down">
      <div class="header-content">
        <el-button 
          :icon="ArrowLeft" 
          circle 
          @click="handleBack"
          class="back-button"
        />
        <div class="header-text">
          <h1 class="page-title">考试详情</h1>
          <p class="page-subtitle">查看考试详细信息和考场安排</p>
        </div>
      </div>
    </div>

    <!-- 考试详情 -->
    <div v-loading="loading" class="detail-container animate-fade-in-up" style="animation-delay: 0.1s;">
      <template v-if="exam">
        <!-- 考试基本信息卡片 -->
        <el-card class="info-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span class="card-title">考试信息</span>
              <ExamStatusTag 
                :status="exam.status" 
                :status-description="exam.statusDescription"
                size="large"
              />
            </div>
          </template>
          
          <div class="info-grid">
            <div class="info-item">
              <div class="info-label">考试名称</div>
              <div class="info-value">{{ exam.examName }}</div>
            </div>
            
            <div class="info-item">
              <div class="info-label">课程信息</div>
              <div class="info-value">
                <div>{{ exam.courseName }}</div>
                <div class="course-no">{{ exam.courseNo }}</div>
              </div>
            </div>
            
            <div class="info-item">
              <div class="info-label">考试类型</div>
              <div class="info-value">
                <ExamTypeTag 
                  :type="exam.type" 
                  :type-description="exam.typeDescription"
                  size="large"
                />
              </div>
            </div>
            
            <div class="info-item">
              <div class="info-label">考试时间</div>
              <div class="info-value">{{ formatExamTime(exam.examTime) }}</div>
            </div>
            
            <div class="info-item">
              <div class="info-label">考试时长</div>
              <div class="info-value">{{ formatDuration(exam.duration) }}</div>
            </div>
            
            <div class="info-item">
              <div class="info-label">考试总分</div>
              <div class="info-value">
                <span class="score-value">{{ exam.totalScore || 100 }}</span> 分
              </div>
            </div>
          </div>
        </el-card>

        <!-- 统计信息卡片 -->
        <el-card class="info-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span class="card-title">统计信息</span>
            </div>
          </template>
          
          <div class="stats-row">
            <div class="stat-item">
              <div class="stat-icon room-icon">
                <el-icon :size="24"><OfficeBuilding /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-label">考场总数</div>
                <div class="stat-value">{{ roomList.length }} 个</div>
              </div>
            </div>
            
            <div class="stat-item">
              <div class="stat-icon student-icon">
                <el-icon :size="24"><User /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-label">学生总数</div>
                <div class="stat-value">{{ totalStudents }} 人</div>
              </div>
            </div>
            
            <div class="stat-item">
              <div class="stat-icon capacity-icon">
                <el-icon :size="24"><DataLine /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-label">容量使用率</div>
                <div class="stat-value">{{ capacityRate }}%</div>
              </div>
            </div>
          </div>
        </el-card>

        <!-- 考场列表卡片 -->
        <el-card class="info-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span class="card-title">考场安排</span>
            </div>
          </template>
          
          <el-table
            v-loading="roomLoading"
            :data="roomList"
            style="width: 100%"
            @row-click="handleRoomClick"
            row-class-name="clickable-row"
          >
            <el-table-column prop="roomName" label="考场名称" width="150" />
            <el-table-column prop="location" label="考场地点" min-width="200" show-overflow-tooltip />
            <el-table-column label="容量" width="150" align="center">
              <template #default="{ row }">
                <div class="capacity-info">
                  <span>{{ row.assignedCount }}/{{ row.capacity }}</span>
                  <el-progress 
                    :percentage="Math.round(row.assignedCount / row.capacity * 100)" 
                    :show-text="false"
                    :stroke-width="6"
                    style="width: 60px; margin-left: 8px;"
                  />
                </div>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120" align="center">
              <template #default="{ row }">
                <el-button 
                  type="primary" 
                  link 
                  @click.stop="handleViewStudents(row)"
                >
                  查看学生
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <!-- 空状态 -->
          <el-empty
            v-if="!roomLoading && roomList.length === 0"
            description="暂无考场安排"
            :image-size="120"
          />
        </el-card>

        <!-- 考试说明卡片 -->
        <el-card 
          v-if="exam.description" 
          class="info-card" 
          shadow="never"
        >
          <template #header>
            <div class="card-header">
              <span class="card-title">考试说明</span>
            </div>
          </template>
          
          <div class="description-content">
            {{ exam.description }}
          </div>
        </el-card>
      </template>
    </div>

    <!-- 学生列表对话框 -->
    <el-dialog
      v-model="studentsDialogVisible"
      :title="`${currentRoom?.roomName} - 学生名单`"
      width="800px"
      :close-on-click-modal="false"
    >
      <el-table
        v-loading="studentsLoading"
        :data="studentList"
        style="width: 100%"
        max-height="400"
      >
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="studentNo" label="学号" width="120" />
        <el-table-column prop="studentName" label="姓名" width="120" />
        <el-table-column prop="seatNumber" label="座位号" width="100" align="center">
          <template #default="{ row }">
            <span class="seat-number">{{ row.seatNumber }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="major" label="专业" min-width="150" show-overflow-tooltip />
      </el-table>
      
      <template #footer>
        <el-button @click="studentsDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  ArrowLeft, OfficeBuilding, User, DataLine 
} from '@element-plus/icons-vue'
import { getCourseExamById, getCourseExamRooms, getRoomStudents } from '@/api/exam'
import ExamStatusTag from '@/components/ExamStatusTag.vue'
import ExamTypeTag from '@/components/ExamTypeTag.vue'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const roomLoading = ref(false)
const studentsLoading = ref(false)
const exam = ref(null)
const roomList = ref([])
const studentList = ref([])
const studentsDialogVisible = ref(false)
const currentRoom = ref(null)

// 统计数据
const totalStudents = computed(() => {
  return roomList.value.reduce((sum, room) => sum + (room.assignedCount || 0), 0)
})

const totalCapacity = computed(() => {
  return roomList.value.reduce((sum, room) => sum + (room.capacity || 0), 0)
})

const capacityRate = computed(() => {
  if (totalCapacity.value === 0) return 0
  return Math.round(totalStudents.value / totalCapacity.value * 100)
})

// 获取考试详情
const fetchExamDetail = async () => {
  loading.value = true
  try {
    const res = await getCourseExamById(route.params.id)
    exam.value = res.data
  } catch (error) {
    console.error('获取考试详情失败:', error)
    ElMessage.error('获取考试详情失败')
    router.push('/teacher/exams/courses')
  } finally {
    loading.value = false
  }
}

// 获取考场列表
const fetchRoomList = async () => {
  roomLoading.value = true
  try {
    const res = await getCourseExamRooms(route.params.id)
    roomList.value = res.data || []
  } catch (error) {
    console.error('获取考场列表失败:', error)
    ElMessage.error('获取考场列表失败')
  } finally {
    roomLoading.value = false
  }
}

// 查看学生列表
const handleViewStudents = async (room) => {
  currentRoom.value = room
  studentsDialogVisible.value = true
  studentsLoading.value = true
  
  try {
    const res = await getRoomStudents(room.id)
    studentList.value = res.data || []
  } catch (error) {
    console.error('获取学生列表失败:', error)
    ElMessage.error('获取学生列表失败')
  } finally {
    studentsLoading.value = false
  }
}

// 考场行点击
const handleRoomClick = (row) => {
  handleViewStudents(row)
}

// 返回列表
const handleBack = () => {
  router.push('/teacher/exams/courses')
}

// 格式化考试时间
const formatExamTime = (time) => {
  if (!time) return '-'
  const date = new Date(time)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const weekDays = ['日', '一', '二', '三', '四', '五', '六']
  const weekDay = weekDays[date.getDay()]
  return `${year}年${month}月${day}日 ${hours}:${minutes} 星期${weekDay}`
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
  fetchExamDetail()
  fetchRoomList()
})
</script>

<style scoped lang="scss">
.course-exam-detail-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: var(--spacing-3xl) var(--spacing-xl);

  // ===================================
  // 页面头部
  // ===================================

  .page-header {
    margin-bottom: var(--spacing-2xl);
  }

  .header-content {
    display: flex;
    align-items: flex-start;
    gap: 16px;
  }

  .back-button {
    margin-top: 8px;
  }

  .header-text {
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
  // 详情容器
  // ===================================

  .detail-container {
    display: flex;
    flex-direction: column;
    gap: 20px;
  }

  .info-card {
    border: 1px solid var(--border-light);
    border-radius: var(--radius-lg);

    :deep(.el-card__header) {
      border-bottom: 1px solid var(--border-light);
      padding: 20px 24px;
    }

    :deep(.el-card__body) {
      padding: 24px;
    }
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .card-title {
    font-size: 18px;
    font-weight: 600;
    color: var(--text-primary);
  }

  // ===================================
  // 基本信息网格
  // ===================================

  .info-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 24px;

    @media (max-width: 768px) {
      grid-template-columns: 1fr;
    }
  }

  .info-item {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }

  .info-label {
    font-size: 14px;
    color: var(--text-secondary);
    font-weight: 500;
  }

  .info-value {
    font-size: 16px;
    color: var(--text-primary);
    font-weight: 500;
  }

  .course-no {
    font-size: 13px;
    color: var(--text-secondary);
    margin-top: 4px;
  }

  .score-value {
    font-size: 24px;
    font-weight: 600;
    color: var(--primary-color);
  }

  // ===================================
  // 统计信息
  // ===================================

  .stats-row {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 24px;

    @media (max-width: 768px) {
      grid-template-columns: 1fr;
    }
  }

  .stat-item {
    display: flex;
    align-items: center;
    gap: 16px;
    padding: 16px;
    background: var(--bg-secondary);
    border-radius: var(--radius-md);
  }

  .stat-icon {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 48px;
    height: 48px;
    border-radius: var(--radius-md);
    flex-shrink: 0;

    &.room-icon {
      background: rgba(64, 158, 255, 0.1);
      color: var(--el-color-primary);
    }

    &.student-icon {
      background: rgba(103, 194, 58, 0.1);
      color: var(--el-color-success);
    }

    &.capacity-icon {
      background: rgba(230, 162, 60, 0.1);
      color: var(--el-color-warning);
    }
  }

  .stat-content {
    flex: 1;
  }

  .stat-label {
    font-size: 14px;
    color: var(--text-secondary);
    margin-bottom: 4px;
  }

  .stat-value {
    font-size: 20px;
    font-weight: 600;
    color: var(--text-primary);
  }

  // ===================================
  // 考场表格
  // ===================================

  :deep(.clickable-row) {
    cursor: pointer;
    
    &:hover {
      background-color: var(--el-fill-color-light);
    }
  }

  .capacity-info {
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: 500;
  }

  // ===================================
  // 考试说明
  // ===================================

  .description-content {
    font-size: 15px;
    line-height: 1.8;
    color: var(--text-primary);
    white-space: pre-wrap;
  }

  // ===================================
  // 学生列表对话框
  // ===================================

  .seat-number {
    display: inline-block;
    padding: 4px 12px;
    background: rgba(64, 158, 255, 0.1);
    color: var(--el-color-primary);
    border-radius: 12px;
    font-size: 14px;
    font-weight: 600;
    font-family: 'Courier New', monospace;
  }
}
</style>

