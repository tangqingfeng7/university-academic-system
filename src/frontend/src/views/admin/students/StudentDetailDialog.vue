<template>
  <el-dialog
    v-model="visible"
    title="学生详细信息"
    width="800px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <div v-if="student" class="student-detail">
      <!-- 基本信息 -->
      <el-descriptions title="基本信息" :column="2" border>
        <el-descriptions-item label="学号">
          <el-tag>{{ student.studentNo }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="姓名">
          <strong>{{ student.name }}</strong>
        </el-descriptions-item>
        <el-descriptions-item label="性别">
          <el-tag :type="student.gender === 'MALE' ? 'primary' : 'danger'">
            {{ student.gender === 'MALE' ? '男' : '女' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="出生日期">
          {{ student.birthDate || '未填写' }}
        </el-descriptions-item>
        <el-descriptions-item label="入学年份">
          {{ student.enrollmentYear }}级
        </el-descriptions-item>
        <el-descriptions-item label="专业">
          {{ student.majorName }}
        </el-descriptions-item>
        <el-descriptions-item label="院系">
          {{ student.departmentName }}
        </el-descriptions-item>
        <el-descriptions-item label="班级">
          {{ student.className || '未分配' }}
        </el-descriptions-item>
      </el-descriptions>

      <!-- 联系信息 -->
      <el-descriptions title="联系信息" :column="2" border style="margin-top: 20px">
        <el-descriptions-item label="联系电话">
          {{ student.phone || '未填写' }}
        </el-descriptions-item>
        <el-descriptions-item label="用户名">
          {{ student.username }}
        </el-descriptions-item>
      </el-descriptions>

      <!-- 统计信息 -->
      <div class="statistics-section">
        <h3>学业统计</h3>
        <el-row :gutter="20">
          <el-col :span="8">
            <div class="stat-card">
              <div class="stat-icon" style="background: #409eff">
                <el-icon :size="24"><Reading /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-label">已选课程</div>
                <div class="stat-value">{{ statistics.courseCount || 0 }}</div>
              </div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="stat-card">
              <div class="stat-icon" style="background: #67c23a">
                <el-icon :size="24"><DocumentChecked /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-label">已修学分</div>
                <div class="stat-value">{{ statistics.credits || 0 }}</div>
              </div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="stat-card">
              <div class="stat-icon" style="background: #e6a23c">
                <el-icon :size="24"><TrophyBase /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-label">平均绩点</div>
                <div class="stat-value">{{ statistics.gpa || '0.00' }}</div>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>
    </div>

    <template #footer>
      <el-button @click="handleClose">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch } from 'vue'
import { Reading, DocumentChecked, TrophyBase } from '@element-plus/icons-vue'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  student: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['update:modelValue'])

const visible = ref(props.modelValue)
const statistics = ref({
  courseCount: 0,
  credits: 0,
  gpa: '0.00'
})

watch(() => props.modelValue, (val) => {
  visible.value = val
  if (val && props.student) {
    loadStatistics()
  }
})

watch(visible, (val) => {
  emit('update:modelValue', val)
})

const loadStatistics = async () => {
  // TODO: 从API加载学生统计信息
  // 这里先使用模拟数据
  statistics.value = {
    courseCount: Math.floor(Math.random() * 20),
    credits: Math.floor(Math.random() * 100),
    gpa: (Math.random() * 2 + 2).toFixed(2)
  }
}

const handleClose = () => {
  visible.value = false
}
</script>

<style scoped lang="scss">
.student-detail {
  :deep(.el-descriptions__title) {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 12px;
  }

  .statistics-section {
    margin-top: 30px;

    h3 {
      font-size: 16px;
      font-weight: 600;
      color: #303133;
      margin-bottom: 16px;
    }

    .stat-card {
      display: flex;
      align-items: center;
      padding: 20px;
      background: white;
      border-radius: 12px;
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
      transition: all 0.3s;

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
      }

      .stat-icon {
        width: 48px;
        height: 48px;
        border-radius: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        color: white;
        margin-right: 16px;
      }

      .stat-content {
        flex: 1;

        .stat-label {
          font-size: 14px;
          color: #909399;
          margin-bottom: 4px;
        }

        .stat-value {
          font-size: 24px;
          font-weight: 600;
          color: #303133;
        }
      }
    }
  }
}
</style>

