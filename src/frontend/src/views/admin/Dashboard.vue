<template>
  <div class="dashboard-container">
    <!-- 页面头部 -->
    <div class="page-header animate-fade-in-down">
      <div>
        <h1 class="page-title">总览</h1>
        <p class="page-subtitle">{{ currentTime }}</p>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="statistics-grid animate-fade-in-up" style="animation-delay: 0.1s;">
      <div class="stat-card">
        <div class="stat-header">
          <span class="stat-label">学生总数</span>
          <el-icon class="stat-icon" :size="20"><UserFilled /></el-icon>
        </div>
        <div class="stat-value">{{ statistics.studentCount }}</div>
        <div class="stat-extra">近30天 +{{ statistics.recentStudents }}</div>
      </div>

      <div class="stat-card">
        <div class="stat-header">
          <span class="stat-label">教师总数</span>
          <el-icon class="stat-icon" :size="20"><User /></el-icon>
        </div>
        <div class="stat-value">{{ statistics.teacherCount }}</div>
        <div class="stat-extra">近30天 +{{ statistics.recentTeachers }}</div>
      </div>

      <div class="stat-card">
        <div class="stat-header">
          <span class="stat-label">课程总数</span>
          <el-icon class="stat-icon" :size="20"><Reading /></el-icon>
        </div>
        <div class="stat-value">{{ statistics.courseCount }}</div>
        <div class="stat-extra">全校开设</div>
      </div>

      <div class="stat-card">
        <div class="stat-header">
          <span class="stat-label">本学期开课</span>
          <el-icon class="stat-icon" :size="20"><Tickets /></el-icon>
        </div>
        <div class="stat-value">{{ statistics.offeringCount }}</div>
        <div class="stat-extra" v-if="statistics.activeSemester">
          {{ statistics.activeSemester.name }}
        </div>
        <div class="stat-extra" v-else>未设置学期</div>
      </div>
    </div>

    <!-- 当前学期信息 -->
    <div class="content-section animate-fade-in-up" style="animation-delay: 0.2s;">
      <div class="section-header">
        <h2 class="section-title">当前学期</h2>
      </div>
      
      <div class="semester-card" v-if="statistics.activeSemester">
        <div class="semester-content">
          <div class="semester-main">
            <div class="semester-badge">活动中</div>
            <h3 class="semester-name">
              {{ statistics.activeSemester.name }}
              <span class="semester-type">{{ statistics.activeSemester.semesterType === 1 ? '春季学期' : '秋季学期' }}</span>
            </h3>
            <div class="semester-info">
              <div class="info-item">
                <span class="info-label">学年</span>
                <span class="info-value">{{ statistics.activeSemester.academicYear }}</span>
              </div>
              <div class="info-divider">·</div>
              <div class="info-item">
                <span class="info-label">开课数量</span>
                <span class="info-value">{{ statistics.offeringCount }} 门</span>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <el-empty v-else description="暂无活动学期" :image-size="120">
        <el-button type="primary" @click="goTo('/admin/semesters')">
          设置学期
        </el-button>
      </el-empty>
    </div>

    <!-- 快捷操作 -->
    <div class="content-section animate-fade-in-up" style="animation-delay: 0.3s;">
      <div class="section-header">
        <h2 class="section-title">快捷操作</h2>
      </div>
      
      <div class="quick-actions">
        <div class="action-item" @click="goTo('/admin/students')">
          <el-icon class="action-icon" :size="24"><Plus /></el-icon>
          <span class="action-label">添加学生</span>
        </div>
        <div class="action-item" @click="goTo('/admin/teachers')">
          <el-icon class="action-icon" :size="24"><Plus /></el-icon>
          <span class="action-label">添加教师</span>
        </div>
        <div class="action-item" @click="goTo('/admin/courses')">
          <el-icon class="action-icon" :size="24"><Plus /></el-icon>
          <span class="action-label">添加课程</span>
        </div>
        <div class="action-item" @click="goTo('/admin/offerings')">
          <el-icon class="action-icon" :size="24"><Plus /></el-icon>
          <span class="action-label">创建开课</span>
        </div>
        <div class="action-item" @click="goTo('/admin/semesters')">
          <el-icon class="action-icon" :size="24"><Setting /></el-icon>
          <span class="action-label">学期管理</span>
        </div>
        <div class="action-item" @click="goTo('/admin/statistics')">
          <el-icon class="action-icon" :size="24"><DocumentCopy /></el-icon>
          <span class="action-label">统计报表</span>
        </div>
      </div>
    </div>

    <!-- 系统通知 -->
    <div class="content-section animate-fade-in-up" style="animation-delay: 0.4s;">
      <div class="section-header">
        <h2 class="section-title">系统通知</h2>
        <el-button type="primary" link @click="goTo('/admin/notifications')">
          查看全部
        </el-button>
      </div>
      
      <el-empty description="暂无新通知" :image-size="100" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import {
  UserFilled, User, Reading, Tickets, Plus, Setting, DocumentCopy
} from '@element-plus/icons-vue'
import { getDashboardStatistics } from '@/api/dashboard'

const router = useRouter()

const statistics = ref({
  studentCount: 0,
  teacherCount: 0,
  courseCount: 0,
  offeringCount: 0,
  recentStudents: 0,
  recentTeachers: 0,
  activeSemester: null
})

const currentTime = computed(() => {
  const now = new Date()
  const hours = now.getHours()
  let greeting = '早上好'
  if (hours >= 12 && hours < 18) {
    greeting = '下午好'
  } else if (hours >= 18) {
    greeting = '晚上好'
  }
  const weekday = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
  return `${greeting}，今天是 ${now.getFullYear()}年${now.getMonth() + 1}月${now.getDate()}日 ${weekday[now.getDay()]}`
})

const loadStatistics = async () => {
  try {
    const res = await getDashboardStatistics()
    statistics.value = {
      studentCount: res.data.studentCount || 0,
      teacherCount: res.data.teacherCount || 0,
      courseCount: res.data.courseCount || 0,
      offeringCount: res.data.offeringCount || 0,
      recentStudents: res.data.recentStudents || 0,
      recentTeachers: res.data.recentTeachers || 0,
      activeSemester: res.data.activeSemester
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

const goTo = (path) => {
  router.push(path)
}

onMounted(() => {
  loadStatistics()
})
</script>

<style scoped lang="scss">
.dashboard-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: var(--spacing-3xl) var(--spacing-xl);

  // ===================================
  // 页面头部
  // ===================================

  .page-header {
    margin-bottom: var(--spacing-3xl);
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
  // 统计卡片
  // ===================================

  .statistics-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 16px;
    margin-bottom: var(--spacing-2xl);

    @media (max-width: 1024px) {
      grid-template-columns: repeat(2, 1fr);
    }

    @media (max-width: 640px) {
      grid-template-columns: repeat(1, 1fr);
    }
  }

  .stat-card {
    background: var(--bg-primary);
    border-radius: var(--radius-lg);
    padding: 24px;
    border: 1px solid var(--border-light);
    transition: all var(--transition-base);

    &:hover {
      box-shadow: var(--shadow-card);
      border-color: var(--border-color);
    }
  }

  .stat-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
  }

  .stat-label {
    font-size: 15px;
    color: var(--text-secondary);
    font-weight: 500;
  }

  .stat-icon {
    color: var(--text-tertiary);
  }

  .stat-value {
    font-size: 36px;
    font-weight: 600;
    color: var(--text-primary);
    line-height: 1;
    margin-bottom: 8px;
    letter-spacing: -0.02em;
  }

  .stat-extra {
    font-size: 14px;
    color: var(--text-tertiary);
  }

  // ===================================
  // 内容区域
  // ===================================

  .content-section {
    margin-bottom: var(--spacing-2xl);
  }

  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: var(--spacing-lg);
  }

  .section-title {
    font-size: 22px;
    font-weight: 600;
    color: var(--text-primary);
    margin: 0;
    letter-spacing: -0.01em;
  }

  // ===================================
  // 学期卡片
  // ===================================

  .semester-card {
    background: var(--bg-primary);
    border-radius: var(--radius-lg);
    padding: 28px;
    border: 1px solid var(--border-light);
  }

  .semester-content {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
  }

  .semester-main {
    flex: 1;
  }

  .semester-badge {
    display: inline-flex;
    padding: 4px 12px;
    background: rgba(0, 122, 255, 0.1);
    color: var(--primary-color);
    border-radius: var(--radius-full);
    font-size: 13px;
    font-weight: 500;
    margin-bottom: 12px;
  }

  .semester-name {
    font-size: 24px;
    font-weight: 600;
    color: var(--text-primary);
    margin: 0 0 16px;
    letter-spacing: -0.01em;
  }

  .semester-type {
    color: var(--text-secondary);
    font-weight: 500;
  }

  .semester-info {
    display: flex;
    align-items: center;
    gap: 16px;
  }

  .info-item {
    display: flex;
    flex-direction: column;
    gap: 4px;
  }

  .info-label {
    font-size: 13px;
    color: var(--text-tertiary);
  }

  .info-value {
    font-size: 15px;
    color: var(--text-primary);
    font-weight: 500;
  }

  .info-divider {
    color: var(--text-tertiary);
  }

  // ===================================
  // 快捷操作
  // ===================================

  .quick-actions {
    display: grid;
    grid-template-columns: repeat(6, 1fr);
    gap: 12px;

    @media (max-width: 1024px) {
      grid-template-columns: repeat(3, 1fr);
    }

    @media (max-width: 640px) {
      grid-template-columns: repeat(2, 1fr);
    }
  }

  .action-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 24px 16px;
    background: var(--bg-primary);
    border: 1px solid var(--border-light);
    border-radius: var(--radius-lg);
    cursor: pointer;
    transition: all var(--transition-base);
    gap: 12px;

    &:hover {
      background: var(--bg-secondary);
      border-color: var(--border-color);
    }

    &:active {
      transform: scale(0.98);
    }
  }

  .action-icon {
    color: var(--primary-color);
  }

  .action-label {
    font-size: 14px;
    color: var(--text-primary);
    font-weight: 500;
  }
}
</style>
