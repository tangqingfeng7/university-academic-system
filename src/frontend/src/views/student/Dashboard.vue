<template>
  <div class="student-dashboard">
    <!-- 欢迎信息 -->
    <el-row :gutter="20" style="margin-bottom: 20px;">
      <el-col :span="24">
        <el-card class="welcome-card">
          <div class="welcome-content">
            <div class="welcome-text">
              <h2>{{ stats.greeting }}，{{ stats.studentName }}！</h2>
              <p class="date">{{ formatDate(stats.currentDate) }}</p>
              <p class="info">
                学号：{{ stats.studentNo }} | 专业：{{ stats.major }} | 班级：{{ stats.className }} | 入学年份：{{ stats.enrollmentYear }}
              </p>
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
              <div class="stat-value">{{ stats.currentCourses }}</div>
              <div class="stat-label">本学期课程</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card stat-success" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon :size="40"><Medal /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.gpa.toFixed(2) }}</div>
              <div class="stat-label">平均绩点</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card stat-warning" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon :size="40"><TrophyBase /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalCredits.toFixed(1) }}</div>
              <div class="stat-label">已获学分</div>
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

    <!-- 学业进展 -->
    <el-row :gutter="20" style="margin-bottom: 20px;">
      <el-col :span="24">
        <el-card class="progress-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">
                <el-icon><TrendCharts /></el-icon>
                学业进展
              </span>
            </div>
          </template>
          <div class="progress-content">
            <div class="progress-item">
              <div class="progress-label">
                <span>已完成课程</span>
                <span class="progress-value">{{ stats.completedCourses }} 门</span>
              </div>
              <el-progress 
                :percentage="Math.min(100, (stats.completedCourses / 40) * 100)" 
                :stroke-width="20"
                :color="progressColors"
              />
            </div>
            <div class="progress-item">
              <div class="progress-label">
                <span>已获学分</span>
                <span class="progress-value">{{ stats.totalCredits.toFixed(1) }} / 150</span>
              </div>
              <el-progress 
                :percentage="Math.min(100, (stats.totalCredits / 150) * 100)" 
                :stroke-width="20"
                :color="progressColors"
              />
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
                @click="goTo('/student/course-selection')"
                class="action-btn"
              >
                选课中心
              </el-button>
            </div>
            <div class="action-item">
              <el-button
                type="success"
                :icon="Document"
                @click="goTo('/student/grades')"
                class="action-btn"
              >
                成绩查询
              </el-button>
            </div>
            <div class="action-item">
              <el-button
                type="warning"
                :icon="Calendar"
                @click="goTo('/student/schedule')"
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
import { 
  UserFilled, Reading, Medal, TrophyBase, Calendar, 
  Grid, TrendCharts, Document
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

const progressColors = [
  { color: '#f56c6c', percentage: 20 },
  { color: '#e6a23c', percentage: 40 },
  { color: '#5cb87a', percentage: 60 },
  { color: '#1989fa', percentage: 80 },
  { color: '#6f7ad3', percentage: 100 },
]

// 获取统计数据
const fetchStatistics = async () => {
  try {
    loading.value = true
    const res = await getStudentDashboardStatistics()
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
.student-dashboard {
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

  .progress-card {
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

    .progress-content {
      display: flex;
      flex-direction: column;
      gap: 30px;

      .progress-item {
        .progress-label {
          display: flex;
          justify-content: space-between;
          margin-bottom: 12px;
          font-size: 15px;
          font-weight: 500;

          .progress-value {
            color: #409eff;
          }
        }
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
  .student-dashboard {
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
