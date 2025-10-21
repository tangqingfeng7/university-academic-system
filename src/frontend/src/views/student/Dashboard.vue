<template>
  <div class="student-dashboard">
    <!-- 欢迎区域 -->
    <div class="welcome-section animate-fade-in-down">
      <div class="welcome-content">
        <h1 class="welcome-title">{{ stats.greeting }}，{{ stats.studentName }}</h1>
        <p class="welcome-date">{{ formatDate(stats.currentDate) }}</p>
        <div class="user-meta">
          <span class="meta-item">{{ stats.studentNo }}</span>
          <span class="meta-divider">·</span>
          <span class="meta-item">{{ stats.major }}</span>
          <span class="meta-divider">·</span>
          <span class="meta-item">{{ stats.className }}</span>
        </div>
      </div>
    </div>

    <!-- 统计卡片网格 -->
    <div class="stats-grid animate-fade-in-up" style="animation-delay: 0.1s;" v-loading="loading">
      <div class="stat-card">
        <div class="stat-icon">
          <el-icon :size="24"><Reading /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.currentCourses }}</div>
          <div class="stat-label">本学期课程</div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon stat-success">
          <el-icon :size="24"><Medal /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.gpa.toFixed(2) }}</div>
          <div class="stat-label">平均绩点</div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon stat-warning">
          <el-icon :size="24"><TrophyBase /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.totalCredits.toFixed(1) }}</div>
          <div class="stat-label">已获学分</div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon stat-primary">
          <el-icon :size="24"><Document /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.completedCourses }}</div>
          <div class="stat-label">已完成课程</div>
        </div>
      </div>
    </div>

    <!-- 学业进展 -->
    <div class="progress-section animate-fade-in-up" style="animation-delay: 0.2s;">
      <h2 class="section-title">学业进展</h2>
      <div class="progress-cards">
        <div class="progress-card">
          <div class="progress-header">
            <span class="progress-label">课程完成度</span>
            <span class="progress-value">{{ stats.completedCourses }} / 40 门</span>
          </div>
          <div class="progress-bar">
            <div 
              class="progress-fill"
              :style="{ width: Math.min(100, (stats.completedCourses / 40) * 100) + '%' }"
            ></div>
          </div>
          <div class="progress-percentage">
            {{ Math.min(100, Math.round((stats.completedCourses / 40) * 100)) }}%
          </div>
        </div>

        <div class="progress-card">
          <div class="progress-header">
            <span class="progress-label">学分获取</span>
            <span class="progress-value">{{ stats.totalCredits.toFixed(1) }} / 150</span>
          </div>
          <div class="progress-bar">
            <div 
              class="progress-fill"
              :style="{ width: Math.min(100, (stats.totalCredits / 150) * 100) + '%' }"
            ></div>
          </div>
          <div class="progress-percentage">
            {{ Math.min(100, Math.round((stats.totalCredits / 150) * 100)) }}%
          </div>
        </div>
      </div>
    </div>

    <!-- 快捷操作 -->
    <div class="quick-actions-section animate-fade-in-up" style="animation-delay: 0.3s;">
      <h2 class="section-title">快捷操作</h2>
      <div class="action-grid">
        <div class="action-card" @click="goTo('/student/course-selection')">
          <div class="action-icon">
            <el-icon :size="28"><Reading /></el-icon>
          </div>
          <div class="action-content">
            <div class="action-title">选课中心</div>
            <div class="action-desc">查看和选择课程</div>
          </div>
          <div class="action-arrow">
            <el-icon><ArrowRight /></el-icon>
          </div>
        </div>

        <div class="action-card" @click="goTo('/student/courses')">
          <div class="action-icon">
            <el-icon :size="28"><Notebook /></el-icon>
          </div>
          <div class="action-content">
            <div class="action-title">我的课程</div>
            <div class="action-desc">查看已选课程</div>
          </div>
          <div class="action-arrow">
            <el-icon><ArrowRight /></el-icon>
          </div>
        </div>

        <div class="action-card" @click="goTo('/student/schedule')">
          <div class="action-icon">
            <el-icon :size="28"><Calendar /></el-icon>
          </div>
          <div class="action-content">
            <div class="action-title">我的课表</div>
            <div class="action-desc">查看课程安排</div>
          </div>
          <div class="action-arrow">
            <el-icon><ArrowRight /></el-icon>
          </div>
        </div>

        <div class="action-card" @click="goTo('/student/grades')">
          <div class="action-icon">
            <el-icon :size="28"><Document /></el-icon>
          </div>
          <div class="action-content">
            <div class="action-title">成绩查询</div>
            <div class="action-desc">查看课程成绩</div>
          </div>
          <div class="action-arrow">
            <el-icon><ArrowRight /></el-icon>
          </div>
        </div>

        <div class="action-card" @click="goTo('/student/exams')">
          <div class="action-icon">
            <el-icon :size="28"><EditPen /></el-icon>
          </div>
          <div class="action-content">
            <div class="action-title">考试安排</div>
            <div class="action-desc">查看考试信息</div>
          </div>
          <div class="action-arrow">
            <el-icon><ArrowRight /></el-icon>
          </div>
        </div>

        <div class="action-card" @click="goTo('/student/attendance')">
          <div class="action-icon">
            <el-icon :size="28"><Clock /></el-icon>
          </div>
          <div class="action-content">
            <div class="action-title">考勤记录</div>
            <div class="action-desc">查看出勤情况</div>
          </div>
          <div class="action-arrow">
            <el-icon><ArrowRight /></el-icon>
          </div>
        </div>

        <div class="action-card" @click="goTo('/student/tuition')">
          <div class="action-icon">
            <el-icon :size="28"><Money /></el-icon>
          </div>
          <div class="action-content">
            <div class="action-title">学费缴纳</div>
            <div class="action-desc">查看缴费情况</div>
          </div>
          <div class="action-arrow">
            <el-icon><ArrowRight /></el-icon>
          </div>
        </div>

        <div class="action-card" @click="goTo('/student/scholarships')">
          <div class="action-icon">
            <el-icon :size="28"><Trophy /></el-icon>
          </div>
          <div class="action-content">
            <div class="action-title">奖学金</div>
            <div class="action-desc">申请和查看奖学金</div>
          </div>
          <div class="action-arrow">
            <el-icon><ArrowRight /></el-icon>
          </div>
        </div>
      </div>
    </div>

    <!-- 日历视图 -->
    <div class="calendar-section animate-fade-in-up" style="animation-delay: 0.4s;">
      <h2 class="section-title">日程安排</h2>
      <div class="calendar-wrapper">
        <el-calendar>
          <template #date-cell="{ data }">
            <div class="calendar-day">
              <div class="day-number">{{ data.day.split('-').slice(2).join('') }}</div>
              <div class="day-events" v-if="getEventsForDate(data.day).length > 0">
                <div 
                  v-for="(event, index) in getEventsForDate(data.day).slice(0, 2)" 
                  :key="index"
                  class="event-item"
                  :class="event.type"
                >
                  <span class="event-text">{{ event.title }}</span>
                </div>
                <span v-if="getEventsForDate(data.day).length > 2" class="more-indicator">
                  +{{ getEventsForDate(data.day).length - 2 }}
                </span>
              </div>
            </div>
          </template>
        </el-calendar>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  Reading, Medal, TrophyBase, Document, Calendar, 
  ArrowRight, Tickets
} from '@element-plus/icons-vue'
import { getStudentDashboardStatistics } from '@/api/studentDashboard'

const router = useRouter()

const stats = ref({
  greeting: '',
  studentName: '',
  studentNo: '',
  major: '',
  className: '',
  enrollmentYear: '',
  currentCourses: 0,
  gpa: 0,
  totalCredits: 0,
  completedCourses: 0,
  activeSemester: null,
  currentDate: ''
})

const loading = ref(false)

// 获取统计数据
const fetchStatistics = async () => {
  try {
    loading.value = true
    const res = await getStudentDashboardStatistics()
    stats.value = { ...stats.value, ...res.data }
  } catch (error) {
    console.error('获取统计数据失败:', error)
    ElMessage.error('获取统计数据失败')
  } finally {
    loading.value = false
  }
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const weekDays = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const weekDay = weekDays[date.getDay()]
  return `${year}年${month}月${day}日 ${weekDay}`
}

// 跳转到指定页面
// 日历事件数据
const calendarEvents = ref([])

// 获取指定日期的事件
const getEventsForDate = (date) => {
  return calendarEvents.value.filter(event => {
    const eventDate = new Date(event.date).toISOString().split('T')[0]
    return eventDate === date
  })
}

// 加载日历事件
const loadCalendarEvents = async () => {
  try {
    const response = await fetch('/api/student/dashboard/calendar-events', {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })
    const result = await response.json()
    if (result.success && result.data) {
      calendarEvents.value = result.data
    }
  } catch (error) {
    console.error('加载日历事件失败:', error)
  }
}

const goTo = (path) => {
  router.push(path)
}

onMounted(() => {
  fetchStatistics()
  loadCalendarEvents()
})
</script>

<style scoped lang="scss">
.student-dashboard {
  max-width: 1200px;
  margin: 0 auto;
  padding: var(--spacing-3xl) var(--spacing-xl);

  // ===================================
  // 欢迎区域
  // ===================================

  .welcome-section {
    margin-bottom: var(--spacing-3xl);
  }

  .welcome-title {
    font-size: 40px;
    font-weight: 600;
    color: var(--text-primary);
    margin: 0 0 12px;
    letter-spacing: -0.03em;
  }

  .welcome-date {
    font-size: 17px;
    color: var(--text-secondary);
    margin: 0 0 12px;
  }

  .user-meta {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 15px;
    color: var(--text-secondary);
  }

  .meta-divider {
    color: var(--text-tertiary);
  }

  // ===================================
  // 统计卡片
  // ===================================

  .stats-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 16px;
    margin-bottom: var(--spacing-3xl);

    @media (max-width: 1024px) {
      grid-template-columns: repeat(2, 1fr);
    }

    @media (max-width: 640px) {
      grid-template-columns: 1fr;
    }
  }

  .stat-card {
    background: var(--bg-primary);
    border-radius: var(--radius-xl);
    padding: 24px;
    border: 1px solid var(--border-light);
    display: flex;
    align-items: center;
    gap: 16px;
    transition: all var(--transition-base);

    &:hover {
      box-shadow: var(--shadow-sm);
    }
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
    flex-shrink: 0;

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
    font-size: 28px;
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
  // 学业进展
  // ===================================

  .progress-section {
    margin-bottom: var(--spacing-3xl);
  }

  .section-title {
    font-size: 24px;
    font-weight: 600;
    color: var(--text-primary);
    margin: 0 0 var(--spacing-lg);
    letter-spacing: -0.02em;
  }

  .progress-cards {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;

    @media (max-width: 768px) {
      grid-template-columns: 1fr;
    }
  }

  .progress-card {
    background: var(--bg-primary);
    border-radius: var(--radius-lg);
    padding: 24px;
    border: 1px solid var(--border-light);
  }

  .progress-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
  }

  .progress-label {
    font-size: 15px;
    font-weight: 500;
    color: var(--text-primary);
  }

  .progress-value {
    font-size: 15px;
    font-weight: 600;
    color: var(--text-secondary);
  }

  .progress-bar {
    height: 8px;
    background: var(--bg-secondary);
    border-radius: var(--radius-full);
    overflow: hidden;
    margin-bottom: 12px;
  }

  .progress-fill {
    height: 100%;
    background: var(--primary-color);
    border-radius: var(--radius-full);
    transition: width var(--transition-smooth);
  }

  .progress-percentage {
    font-size: 14px;
    color: var(--text-tertiary);
    text-align: right;
  }

  // ===================================
  // 快捷操作
  // ===================================

  .quick-actions-section {
    margin-bottom: var(--spacing-xl);
  }

  .action-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 12px;

    @media (max-width: 1024px) {
      grid-template-columns: repeat(2, 1fr);
    }

    @media (max-width: 768px) {
      grid-template-columns: 1fr;
    }
  }

  .action-card {
    background: var(--bg-primary);
    border-radius: var(--radius-lg);
    padding: 20px;
    border: 1px solid var(--border-light);
    display: flex;
    align-items: center;
    gap: 16px;
    cursor: pointer;
    transition: all var(--transition-base);

    &:hover {
      box-shadow: var(--shadow-card);
      border-color: var(--border-color);
    }

    &:active {
      transform: scale(0.98);
    }
  }

  .action-icon {
    width: 48px;
    height: 48px;
    border-radius: var(--radius-md);
    background: var(--bg-secondary);
    display: flex;
    align-items: center;
    justify-content: center;
    color: var(--text-secondary);
    flex-shrink: 0;
  }

  .action-content {
    flex: 1;
  }

  .action-title {
    font-size: 17px;
    font-weight: 600;
    color: var(--text-primary);
    margin-bottom: 4px;
    letter-spacing: -0.01em;
  }

  .action-desc {
    font-size: 14px;
    color: var(--text-secondary);
  }

  .action-arrow {
    color: var(--text-tertiary);
    font-size: 20px;
    flex-shrink: 0;
  }

  // ===================================
  // 日历样式
  // ===================================

  .calendar-section {
    margin-bottom: var(--spacing-xl);
  }

  .calendar-wrapper {
    background: var(--bg-primary);
    border-radius: var(--radius-lg);
    padding: 20px;
    border: 1px solid var(--border-light);

    :deep(.el-calendar) {
      background: transparent;
      
      .el-calendar__header {
        padding: 12px 16px;
        border-bottom: 1px solid var(--border-light);
        
        .el-calendar__title {
          font-size: 16px;
          font-weight: 600;
          color: var(--text-primary);
        }
      }
      
      .el-calendar__body {
        padding: 12px 0;
      }
      
      .el-calendar-table {
        thead th {
          padding: 10px 0;
          font-weight: 600;
          color: var(--text-secondary);
          font-size: 13px;
        }
        
        .el-calendar-day {
          height: 90px;
          padding: 6px;
          
          &:hover {
            background: var(--bg-secondary);
          }
        }
      }
    }
    
    .calendar-day {
      height: 100%;
      display: flex;
      flex-direction: column;
      gap: 4px;
      
      .day-number {
        font-size: 14px;
        font-weight: 500;
        color: var(--text-primary);
      }
      
      .day-events {
        display: flex;
        flex-direction: column;
        gap: 2px;
        flex: 1;
        
        .event-item {
          font-size: 11px;
          padding: 2px 6px;
          border-radius: 3px;
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
          
          &.course {
            background: #e1f3ff;
            color: #409eff;
          }
          
          &.exam {
            background: #ffe1e1;
            color: #f56c6c;
          }
          
          .event-text {
            font-size: 11px;
          }
        }
        
        .more-indicator {
          font-size: 10px;
          color: var(--text-tertiary);
          padding: 2px;
        }
      }
    }
    
    :deep(.is-today) {
      .day-number {
        color: var(--primary-color);
        font-weight: 700;
      }
    }
  }
}
</style>
