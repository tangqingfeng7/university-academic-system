<template>
  <div class="teacher-dashboard">
    <!-- 欢迎区域 -->
    <div class="welcome-section animate-fade-in-down">
      <div class="welcome-content">
        <h1 class="welcome-title">{{ stats.greeting }}，{{ stats.teacherName }}老师</h1>
        <p class="welcome-date">{{ formatDate(stats.currentDate) }}</p>
        <div class="user-meta">
          <span class="meta-item">工号 {{ stats.teacherNo }}</span>
          <span class="meta-divider">·</span>
          <span class="meta-item">{{ stats.title }}</span>
          <span class="meta-divider">·</span>
          <span class="meta-item">{{ stats.department }}</span>
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
          <div class="stat-value">{{ stats.courseCount }}</div>
          <div class="stat-label">本学期课程</div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon stat-success">
          <el-icon :size="24"><User /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.studentCount }}</div>
          <div class="stat-label">授课学生</div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon stat-warning">
          <el-icon :size="24"><Edit /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.pendingGrades }}</div>
          <div class="stat-label">待录入成绩</div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon stat-primary">
          <el-icon :size="24"><Calendar /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.activeSemester?.name || '-' }}</div>
          <div class="stat-label">当前学期</div>
        </div>
      </div>
    </div>

    <!-- 快捷操作 -->
    <div class="quick-actions-section animate-fade-in-up" style="animation-delay: 0.2s;">
      <h2 class="section-title">快捷操作</h2>
      <div class="action-grid">
        <div class="action-card" @click="goTo('/teacher/courses')">
          <div class="action-icon">
            <el-icon :size="28"><Reading /></el-icon>
          </div>
          <div class="action-content">
            <div class="action-title">我的课程</div>
            <div class="action-desc">查看和管理课程</div>
          </div>
          <div class="action-arrow">
            <el-icon><ArrowRight /></el-icon>
          </div>
        </div>

        <div class="action-card" @click="goTo('/teacher/grades')">
          <div class="action-icon">
            <el-icon :size="28"><Edit /></el-icon>
          </div>
          <div class="action-content">
            <div class="action-title">成绩管理</div>
            <div class="action-desc">录入和管理成绩</div>
          </div>
          <div class="action-arrow">
            <el-icon><ArrowRight /></el-icon>
          </div>
        </div>

        <div class="action-card" @click="goTo('/teacher/schedule')">
          <div class="action-icon">
            <el-icon :size="28"><Calendar /></el-icon>
          </div>
          <div class="action-content">
            <div class="action-title">我的课表</div>
            <div class="action-desc">查看教学安排</div>
          </div>
          <div class="action-arrow">
            <el-icon><ArrowRight /></el-icon>
          </div>
        </div>

        <div class="action-card" @click="goTo('/teacher/notifications')">
          <div class="action-icon">
            <el-icon :size="28"><Bell /></el-icon>
          </div>
          <div class="action-content">
            <div class="action-title">通知管理</div>
            <div class="action-desc">发布课程通知</div>
          </div>
          <div class="action-arrow">
            <el-icon><ArrowRight /></el-icon>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  Reading, User, Edit, Calendar, Bell, ArrowRight
} from '@element-plus/icons-vue'
import { getTeacherDashboardStatistics } from '@/api/teacherDashboard'

const router = useRouter()

const stats = ref({
  greeting: '',
  teacherName: '',
  teacherNo: '',
  title: '',
  department: '',
  courseCount: 0,
  studentCount: 0,
  pendingGrades: 0,
  activeSemester: null,
  currentDate: ''
})

const loading = ref(false)

// 获取统计数据
const fetchStatistics = async () => {
  try {
    loading.value = true
    const res = await getTeacherDashboardStatistics()
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
const goTo = (path) => {
  router.push(path)
}

onMounted(() => {
  fetchStatistics()
})
</script>

<style scoped lang="scss">
.teacher-dashboard {
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
    min-width: 0;
  }

  .stat-value {
    font-size: 28px;
    font-weight: 600;
    color: var(--text-primary);
    margin-bottom: 4px;
    letter-spacing: -0.02em;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .stat-label {
    font-size: 14px;
    color: var(--text-secondary);
    font-weight: 500;
  }

  // ===================================
  // 快捷操作
  // ===================================

  .quick-actions-section {
    margin-bottom: var(--spacing-xl);
  }

  .section-title {
    font-size: 24px;
    font-weight: 600;
    color: var(--text-primary);
    margin: 0 0 var(--spacing-lg);
    letter-spacing: -0.02em;
  }

  .action-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;

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
}
</style>
