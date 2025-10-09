<template>
  <div class="exam-detail-page">
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
          <p class="page-subtitle">查看考试的详细信息</p>
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
              <span class="card-title">基本信息</span>
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

        <!-- 考试时间卡片 -->
        <el-card class="info-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span class="card-title">考试时间</span>
            </div>
          </template>
          
          <div class="time-info-container">
            <div class="time-item">
              <el-icon :size="24" class="time-icon"><Clock /></el-icon>
              <div class="time-content">
                <div class="time-label">开始时间</div>
                <div class="time-value">{{ formatExamTime(exam.examTime) }}</div>
              </div>
            </div>
            
            <el-divider direction="vertical" class="time-divider" />
            
            <div class="time-item">
              <el-icon :size="24" class="time-icon"><Timer /></el-icon>
              <div class="time-content">
                <div class="time-label">结束时间</div>
                <div class="time-value">{{ calculateEndTime(exam.examTime, exam.duration) }}</div>
              </div>
            </div>
          </div>
        </el-card>

        <!-- 考场信息卡片 -->
        <el-card 
          v-if="isExamPublished" 
          class="info-card" 
          shadow="never"
        >
          <template #header>
            <div class="card-header">
              <span class="card-title">考场信息</span>
            </div>
          </template>
          
          <div class="location-info">
            <div class="location-item">
              <el-icon :size="24" class="location-icon"><Location /></el-icon>
              <div class="location-content">
                <div class="location-label">考场</div>
                <div class="location-value">{{ exam.roomName || '未安排' }}</div>
              </div>
            </div>
            
            <div class="location-item">
              <el-icon :size="24" class="location-icon"><OfficeBuilding /></el-icon>
              <div class="location-content">
                <div class="location-label">地点</div>
                <div class="location-value">{{ exam.location || '未安排' }}</div>
              </div>
            </div>
            
            <div class="location-item">
              <el-icon :size="24" class="location-icon"><Postcard /></el-icon>
              <div class="location-content">
                <div class="location-label">座位号</div>
                <div class="location-value">
                  <span v-if="exam.seatNumber" class="seat-number">{{ exam.seatNumber }}</span>
                  <span v-else>未安排</span>
                </div>
              </div>
            </div>
          </div>
        </el-card>

        <!-- 未发布提示 -->
        <el-alert
          v-else
          title="考试尚未发布"
          type="info"
          description="考场和座位信息将在考试发布后显示"
          :closable="false"
          show-icon
        />

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

        <!-- 考试提醒 -->
        <el-card class="info-card tips-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span class="card-title">温馨提示</span>
            </div>
          </template>
          
          <div class="tips-content">
            <div class="tip-item">
              <el-icon class="tip-icon"><InfoFilled /></el-icon>
              <span>请提前15分钟到达考场，携带有效证件（学生证、身份证）</span>
            </div>
            <div class="tip-item">
              <el-icon class="tip-icon"><InfoFilled /></el-icon>
              <span>考试过程中请遵守考场纪律，不得作弊</span>
            </div>
            <div class="tip-item">
              <el-icon class="tip-icon"><InfoFilled /></el-icon>
              <span>考试结束后请按照监考老师的指示有序离场</span>
            </div>
          </div>
        </el-card>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  ArrowLeft, Clock, Timer, Location, OfficeBuilding, Postcard, InfoFilled 
} from '@element-plus/icons-vue'
import { getMyExamById } from '@/api/exam'
import ExamStatusTag from '@/components/ExamStatusTag.vue'
import ExamTypeTag from '@/components/ExamTypeTag.vue'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const exam = ref(null)

// 判断考试是否已发布
const isExamPublished = computed(() => {
  return exam.value && ['PUBLISHED', 'IN_PROGRESS', 'FINISHED'].includes(exam.value.status)
})

// 获取考试详情
const fetchExamDetail = async () => {
  loading.value = true
  try {
    const res = await getMyExamById(route.params.id)
    exam.value = res.data
  } catch (error) {
    console.error('获取考试详情失败:', error)
    ElMessage.error('获取考试详情失败')
    router.push('/student/exams')
  } finally {
    loading.value = false
  }
}

// 返回列表
const handleBack = () => {
  router.push('/student/exams')
}

// 格式化考试时间（详细版）
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

// 计算结束时间
const calculateEndTime = (startTime, duration) => {
  if (!startTime || !duration) return '-'
  const start = new Date(startTime)
  const end = new Date(start.getTime() + duration * 60000)
  const hours = String(end.getHours()).padStart(2, '0')
  const minutes = String(end.getMinutes()).padStart(2, '0')
  return `${hours}:${minutes}`
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
})
</script>

<style scoped lang="scss">
.exam-detail-page {
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
  // 考试时间
  // ===================================

  .time-info-container {
    display: flex;
    align-items: center;
    gap: 32px;

    @media (max-width: 768px) {
      flex-direction: column;
      align-items: stretch;
      gap: 24px;
    }
  }

  .time-item {
    flex: 1;
    display: flex;
    align-items: center;
    gap: 16px;
  }

  .time-icon {
    color: var(--primary-color);
    flex-shrink: 0;
  }

  .time-content {
    flex: 1;
  }

  .time-label {
    font-size: 14px;
    color: var(--text-secondary);
    margin-bottom: 4px;
  }

  .time-value {
    font-size: 18px;
    font-weight: 600;
    color: var(--text-primary);
  }

  .time-divider {
    height: 60px;
    
    @media (max-width: 768px) {
      display: none;
    }
  }

  // ===================================
  // 考场信息
  // ===================================

  .location-info {
    display: flex;
    flex-direction: column;
    gap: 24px;
  }

  .location-item {
    display: flex;
    align-items: center;
    gap: 16px;
    padding: 16px;
    background: var(--bg-secondary);
    border-radius: var(--radius-md);
  }

  .location-icon {
    color: var(--primary-color);
    flex-shrink: 0;
  }

  .location-content {
    flex: 1;
  }

  .location-label {
    font-size: 14px;
    color: var(--text-secondary);
    margin-bottom: 4px;
  }

  .location-value {
    font-size: 18px;
    font-weight: 600;
    color: var(--text-primary);
  }

  .seat-number {
    display: inline-block;
    padding: 6px 16px;
    background: rgba(64, 158, 255, 0.1);
    color: var(--el-color-primary);
    border-radius: 16px;
    font-size: 20px;
    font-weight: 600;
    font-family: 'Courier New', monospace;
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
  // 温馨提示
  // ===================================

  .tips-card {
    border-color: var(--info-color);
    background: rgba(144, 147, 153, 0.05);
  }

  .tips-content {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }

  .tip-item {
    display: flex;
    align-items: flex-start;
    gap: 12px;
    font-size: 15px;
    line-height: 1.6;
    color: var(--text-primary);
  }

  .tip-icon {
    color: var(--info-color);
    margin-top: 2px;
    flex-shrink: 0;
  }
}
</style>

