<template>
  <div class="teacher-dashboard">
    <!-- 欢迎信息 -->
    <el-row :gutter="20" style="margin-bottom: 20px;">
      <el-col :span="24">
        <el-card class="welcome-card">
          <div class="welcome-content">
            <div class="welcome-text">
              <h2>{{ stats.greeting }}，{{ stats.teacherName }}老师！</h2>
              <p class="date">{{ formatDate(stats.currentDate) }}</p>
              <p class="info">工号：{{ stats.teacherNo }} | 职称：{{ stats.title }} | 院系：{{ stats.department }}</p>
            </div>
            <div class="welcome-icon">
              <el-icon :size="80" color="#409eff"><UserFilled /></el-icon>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 统计卡片 -->
    <el-row :gutter="20" style="margin-bottom: 20px;">
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card stat-primary" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon :size="40"><Reading /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.courseCount }}</div>
              <div class="stat-label">本学期课程</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card stat-success" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon :size="40"><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.studentCount }}</div>
              <div class="stat-label">授课学生数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card stat-warning" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon :size="40"><Edit /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.pendingGrades }}</div>
              <div class="stat-label">待批改成绩</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card stat-info" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon :size="40"><Calendar /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.activeSemester?.name || '-' }}</div>
              <div class="stat-label">当前学期</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 快捷操作 -->
    <el-row :gutter="20">
      <el-col :span="24">
        <el-card class="quick-actions-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">
                <el-icon><Grid /></el-icon>
                快捷操作
              </span>
            </div>
          </template>
          <div class="quick-actions">
            <div class="action-item">
              <el-button
                type="primary"
                :icon="Reading"
                @click="goTo('/teacher/courses')"
                class="action-btn"
              >
                课程与成绩
              </el-button>
            </div>
            <div class="action-item">
              <el-button
                type="success"
                :icon="Bell"
                @click="goTo('/teacher/notifications')"
                class="action-btn"
              >
                通知管理
              </el-button>
            </div>
            <div class="action-item">
              <el-button
                type="warning"
                :icon="Calendar"
                @click="goTo('/teacher/schedule')"
                class="action-btn"
              >
                我的课表
              </el-button>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { UserFilled, Reading, User, Bell, Calendar, Grid } from '@element-plus/icons-vue'
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
    // 从响应中提取data字段
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
  padding: 20px;

  .welcome-card {
    border-radius: 12px;
    border: none;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;

    :deep(.el-card__body) {
      padding: 30px;
    }

    .welcome-content {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .welcome-text {
        flex: 1;

        h2 {
          margin: 0 0 10px 0;
          font-size: 28px;
          font-weight: 600;
        }

        .date {
          margin: 0 0 10px 0;
          font-size: 14px;
          opacity: 0.9;
        }

        .info {
          margin: 0;
          font-size: 14px;
          opacity: 0.85;
        }
      }

      .welcome-icon {
        opacity: 0.3;
      }
    }
  }

  .stat-card {
    border-radius: 12px;
    border: none;
    margin-bottom: 20px;
    transition: all 0.3s;

    &:hover {
      transform: translateY(-5px);
    }

    :deep(.el-card__body) {
      padding: 20px;
    }

    .stat-content {
      display: flex;
      align-items: center;
      gap: 15px;

      .stat-icon {
        width: 60px;
        height: 60px;
        border-radius: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 30px;
      }

      .stat-info {
        flex: 1;

        .stat-value {
          font-size: 28px;
          font-weight: bold;
          margin-bottom: 5px;
          line-height: 1;
        }

        .stat-label {
          font-size: 14px;
          color: #909399;
        }
      }
    }

    &.stat-primary {
      background: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%);

      .stat-icon {
        background: rgba(25, 118, 210, 0.1);
        color: #1976d2;
      }

      .stat-value {
        color: #1976d2;
      }
    }

    &.stat-success {
      background: linear-gradient(135deg, #e8f5e9 0%, #c8e6c9 100%);

      .stat-icon {
        background: rgba(56, 142, 60, 0.1);
        color: #388e3c;
      }

      .stat-value {
        color: #388e3c;
      }
    }

    &.stat-warning {
      background: linear-gradient(135deg, #fff3e0 0%, #ffe0b2 100%);

      .stat-icon {
        background: rgba(245, 124, 0, 0.1);
        color: #f57c00;
      }

      .stat-value {
        color: #f57c00;
      }
    }

    &.stat-info {
      background: linear-gradient(135deg, #e0f2f1 0%, #b2dfdb 100%);

      .stat-icon {
        background: rgba(0, 121, 107, 0.1);
        color: #00796b;
      }

      .stat-value {
        color: #00796b;
        font-size: 16px;
      }
    }
  }

  .quick-actions-card {
    border-radius: 12px;
    border: none;

    .card-header {
      .card-title {
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: 16px;
        font-weight: 600;
      }
    }

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

@media (max-width: 768px) {
  .teacher-dashboard {
    padding: 10px;

    .welcome-card {
      :deep(.el-card__body) {
        padding: 20px;
      }

      .welcome-content {
        flex-direction: column;
        text-align: center;

        .welcome-text h2 {
          font-size: 22px;
        }

        .welcome-icon {
          margin-top: 15px;
        }
      }
    }

    .quick-actions-card .quick-actions {
      grid-template-columns: 1fr;
    }
  }
}
</style>
