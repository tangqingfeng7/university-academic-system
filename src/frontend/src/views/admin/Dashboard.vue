<template>
  <div class="dashboard-container">
    <!-- 欢迎区域 -->
    <div class="welcome-section">
      <el-card shadow="hover">
        <div class="welcome-content">
          <div class="welcome-text">
            <h2>欢迎使用大学教务管理系统</h2>
            <p>{{ currentTime }}</p>
          </div>
          <el-icon class="welcome-icon" :size="80"><Van /></el-icon>
        </div>
      </el-card>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="statistics-row">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card stat-card-primary">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon :size="40"><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">学生总数</div>
              <div class="stat-value">{{ statistics.studentCount }}</div>
              <div class="stat-extra">
                <el-icon><TrendCharts /></el-icon>
                近30天新增 {{ statistics.recentStudents }}
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card shadow="hover" class="stat-card stat-card-success">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon :size="40"><UserFilled /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">教师总数</div>
              <div class="stat-value">{{ statistics.teacherCount }}</div>
              <div class="stat-extra">
                <el-icon><TrendCharts /></el-icon>
                近30天新增 {{ statistics.recentTeachers }}
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card shadow="hover" class="stat-card stat-card-warning">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon :size="40"><Reading /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">课程总数</div>
              <div class="stat-value">{{ statistics.courseCount }}</div>
              <div class="stat-extra">
                <el-icon><DocumentChecked /></el-icon>
                全校开设课程
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card shadow="hover" class="stat-card stat-card-info">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon :size="40"><Tickets /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">本学期开课</div>
              <div class="stat-value">{{ statistics.offeringCount }}</div>
              <div class="stat-extra" v-if="statistics.activeSemester">
                <el-icon><Calendar /></el-icon>
                {{ statistics.activeSemester.name }}
              </div>
              <div class="stat-extra" v-else>
                <el-icon><WarningFilled /></el-icon>
                未设置活动学期
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 当前学期信息 -->
    <el-row :gutter="20" class="content-row">
      <el-col :span="12">
        <el-card shadow="hover" class="semester-card">
          <template #header>
            <div class="card-header">
              <span>
                <el-icon><Calendar /></el-icon>
                当前学期信息
              </span>
            </div>
          </template>
          <div v-if="statistics.activeSemester" class="semester-info">
            <el-descriptions :column="1" border>
              <el-descriptions-item label="学期名称">
                <el-tag type="primary" size="large">
                  {{ statistics.activeSemester.name }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="学年">
                {{ statistics.activeSemester.academicYear }}
              </el-descriptions-item>
              <el-descriptions-item label="学期类型">
                <el-tag :type="statistics.activeSemester.semesterType === 1 ? 'success' : 'warning'">
                  {{ statistics.activeSemester.semesterType === 1 ? '春季学期' : '秋季学期' }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="开课数量">
                {{ statistics.offeringCount }} 门课程
              </el-descriptions-item>
            </el-descriptions>
          </div>
          <el-empty v-else description="暂无活动学期" :image-size="100" />
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card shadow="hover" class="quick-actions-card">
          <template #header>
            <div class="card-header">
              <span>
                <el-icon><Grid /></el-icon>
                快捷操作
              </span>
            </div>
          </template>
          <div class="quick-actions">
            <div class="action-item">
              <el-button
                type="primary"
                :icon="Plus"
                @click="goTo('/admin/students')"
                class="action-btn"
              >
                添加学生
              </el-button>
            </div>
            <div class="action-item">
              <el-button
                type="success"
                :icon="Plus"
                @click="goTo('/admin/teachers')"
                class="action-btn"
              >
                添加教师
              </el-button>
            </div>
            <div class="action-item">
              <el-button
                type="warning"
                :icon="Plus"
                @click="goTo('/admin/courses')"
                class="action-btn"
              >
                添加课程
              </el-button>
            </div>
            <div class="action-item">
              <el-button
                type="info"
                :icon="Plus"
                @click="goTo('/admin/offerings')"
                class="action-btn"
              >
                创建开课
              </el-button>
            </div>
            <div class="action-item">
              <el-button
                type="primary"
                :icon="Setting"
                @click="goTo('/admin/semesters')"
                class="action-btn"
              >
                学期管理
              </el-button>
            </div>
            <div class="action-item">
              <el-button
                type="success"
                :icon="DocumentCopy"
                @click="goTo('/admin/statistics')"
                class="action-btn"
              >
                统计报表
              </el-button>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 最近通知 -->
    <el-row :gutter="20" class="content-row">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>
                <el-icon><Bell /></el-icon>
                系统通知
              </span>
              <el-button
                type="primary"
                link
                @click="goTo('/admin/notifications')"
              >
                查看更多 <el-icon><ArrowRight /></el-icon>
              </el-button>
            </div>
          </template>
          <el-empty description="暂无新通知" :image-size="80" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import {
  User,
  UserFilled,
  Reading,
  Tickets,
  Calendar,
  Grid,
  Plus,
  Setting,
  DocumentCopy,
  Bell,
  ArrowRight,
  Van,
  TrendCharts,
  DocumentChecked,
  WarningFilled
} from '@element-plus/icons-vue'
import { getDashboardStatistics } from '@/api/dashboard'

const router = useRouter()

// 数据
const statistics = ref({
  studentCount: 0,
  teacherCount: 0,
  courseCount: 0,
  offeringCount: 0,
  recentStudents: 0,
  recentTeachers: 0,
  activeSemester: null
})

// 当前时间
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
  return `${greeting}！今天是 ${now.getFullYear()}年${now.getMonth() + 1}月${now.getDate()}日 ${weekday[now.getDay()]}`
})

// 加载统计数据
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

// 跳转页面
const goTo = (path) => {
  router.push(path)
}

// 初始化
onMounted(() => {
  loadStatistics()
})
</script>

<style scoped lang="scss">
.dashboard-container {
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);

  .welcome-section {
    margin-bottom: 20px;

    .welcome-content {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .welcome-text {
        h2 {
          margin: 0 0 8px 0;
          font-size: 24px;
          font-weight: 600;
          color: #303133;
        }

        p {
          margin: 0;
          font-size: 14px;
          color: #909399;
        }
      }

      .welcome-icon {
        color: #409eff;
        opacity: 0.6;
      }
    }
  }

  .statistics-row {
    margin-bottom: 20px;

    .stat-card {
      border-radius: 12px;
      border: none;
      transition: all 0.3s;

      &:hover {
        transform: translateY(-4px);
      }

      :deep(.el-card__body) {
        padding: 24px;
      }

      .stat-content {
        display: flex;
        align-items: center;
        gap: 20px;

        .stat-icon {
          width: 80px;
          height: 80px;
          border-radius: 16px;
          display: flex;
          align-items: center;
          justify-content: center;
          color: white;
        }

        .stat-info {
          flex: 1;

          .stat-label {
            font-size: 14px;
            color: #909399;
            margin-bottom: 8px;
          }

          .stat-value {
            font-size: 32px;
            font-weight: 600;
            color: #303133;
            margin-bottom: 8px;
          }

          .stat-extra {
            font-size: 12px;
            color: #909399;
            display: flex;
            align-items: center;
            gap: 4px;
          }
        }
      }

      &.stat-card-primary .stat-icon {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      }

      &.stat-card-success .stat-icon {
        background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
      }

      &.stat-card-warning .stat-icon {
        background: linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%);
      }

      &.stat-card-info .stat-icon {
        background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
      }
    }
  }

  .content-row {
    margin-bottom: 20px;

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      font-weight: 600;
      font-size: 16px;

      span {
        display: flex;
        align-items: center;
        gap: 8px;
      }
    }

    .semester-card {
      border-radius: 12px;
      border: none;

      .semester-info {
        :deep(.el-descriptions__label) {
          font-weight: 500;
        }
      }
    }

    .quick-actions-card {
      border-radius: 12px;
      border: none;

      .quick-actions {
        display: grid;
        grid-template-columns: repeat(3, 1fr);
        gap: 16px;

        .action-item {
          display: flex;
          
          .action-btn {
            width: 100%;
            height: 52px;
            font-size: 15px;
            font-weight: 500;
            border-radius: 8px;
          }
        }
      }
    }
  }
}
</style>
