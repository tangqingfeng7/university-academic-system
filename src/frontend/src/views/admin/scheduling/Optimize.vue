<template>
  <div class="scheduling-optimize">
    <el-page-header @back="goBack" title="返回" content="智能排课" />

    <!-- 方案信息 -->
    <el-card shadow="hover" class="solution-card" v-loading="loading">
      <template #header>
        <div class="card-header">
          <span class="card-title">
            <el-icon><Tickets /></el-icon>
            方案信息
          </span>
          <el-tag :type="getStatusType(solution.status)" size="large">
            {{ getStatusText(solution.status) }}
          </el-tag>
        </div>
      </template>

      <el-descriptions :column="3" border v-if="solution.id">
        <el-descriptions-item label="方案名称">{{ solution.name }}</el-descriptions-item>
        <el-descriptions-item label="学期">{{ semesterName }}</el-descriptions-item>
        <el-descriptions-item label="质量分数">
          <span v-if="solution.qualityScore !== null">
            {{ solution.qualityScore.toFixed(2) }}
          </span>
          <span v-else class="empty-text">未评估</span>
        </el-descriptions-item>
        <el-descriptions-item label="冲突数">
          <el-tag v-if="solution.conflictCount > 0" type="danger" size="small">
            {{ solution.conflictCount }}
          </el-tag>
          <span v-else class="success-text">0</span>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">
          {{ formatDateTime(solution.createdAt) }}
        </el-descriptions-item>
        <el-descriptions-item label="生成时间">
          <span v-if="solution.generatedAt">
            {{ formatDateTime(solution.generatedAt) }}
          </span>
          <span v-else class="empty-text">未生成</span>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 排课配置 -->
    <el-card shadow="hover" class="config-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">
            <el-icon><Setting /></el-icon>
            排课配置
          </span>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="140px"
      >
        <el-form-item label="待排课程" prop="courseIds">
          <el-select
            v-model="form.courseIds"
            multiple
            filterable
            placeholder="选择待排课程（不选则自动排所有课程）"
            style="width: 100%"
          >
            <el-option
              v-for="course in availableCourses"
              :key="course.id"
              :label="`${course.courseName} - ${course.teacherName}`"
              :value="course.id"
            />
          </el-select>
          <div class="form-tip">
            留空将对所有未排课的课程进行智能排课
          </div>
        </el-form-item>

        <el-form-item label="最大迭代次数" prop="maxIterations">
          <el-input-number
            v-model="form.maxIterations"
            :min="100"
            :max="10000"
            :step="100"
            placeholder="请输入最大迭代次数"
          />
          <span class="unit-label">次（默认1000）</span>
          <div class="form-tip">
            迭代次数越多，排课质量可能越高，但耗时越长
          </div>
        </el-form-item>

        <el-form-item label="超时时间" prop="timeoutSeconds">
          <el-input-number
            v-model="form.timeoutSeconds"
            :min="10"
            :max="300"
            :step="10"
            placeholder="请输入超时时间"
          />
          <span class="unit-label">秒（默认60秒）</span>
          <div class="form-tip">
            超时后将返回当前最优解
          </div>
        </el-form-item>

        <el-form-item label="优化目标" prop="optimizeFor">
          <el-radio-group v-model="form.optimizeFor">
            <el-radio label="QUALITY">质量优先</el-radio>
            <el-radio label="SPEED">速度优先</el-radio>
            <el-radio label="BALANCED">平衡模式</el-radio>
          </el-radio-group>
          <div class="form-tip">
            质量优先：追求最优解<br/>
            速度优先：快速得到可行解<br/>
            平衡模式：兼顾质量和速度
          </div>
        </el-form-item>

        <el-form-item>
          <el-button 
            type="primary" 
            size="large"
            @click="handleExecute" 
            :loading="executing"
            :disabled="solution.status === 'OPTIMIZING'"
          >
            <el-icon><Lightning /></el-icon>
            开始智能排课
          </el-button>
          <el-button 
            size="large"
            @click="handlePreview"
            :disabled="!solution.generatedAt"
          >
            <el-icon><View /></el-icon>
            预览结果
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 排课进度 -->
    <el-card shadow="hover" class="progress-card" v-if="executing || result">
      <template #header>
        <div class="card-header">
          <span class="card-title">
            <el-icon><Promotion /></el-icon>
            排课进度
          </span>
        </div>
      </template>

      <div class="progress-content">
        <el-progress
          :percentage="progress"
          :status="progressStatus"
          :stroke-width="20"
        >
          <template #default="{ percentage }">
            <span class="progress-text">{{ percentage }}%</span>
          </template>
        </el-progress>

        <div class="progress-info" v-if="result">
          <el-alert
            :title="result.success ? '排课成功' : '排课失败'"
            :type="result.success ? 'success' : 'error'"
            :closable="false"
            class="result-alert"
          >
            <template #default>
              <div v-if="result.success">
                <p>✓ 已成功排课 {{ result.scheduledCount }} 门课程</p>
                <p>✓ 质量分数：{{ result.qualityScore?.toFixed(2) }}</p>
                <p v-if="result.conflicts?.length > 0">
                  ⚠ 检测到 {{ result.conflicts.length }} 个冲突
                </p>
              </div>
              <div v-else>
                <p>{{ result.message }}</p>
              </div>
            </template>
          </el-alert>

          <!-- 冲突列表 -->
          <el-collapse v-if="result.conflicts && result.conflicts.length > 0" class="conflict-collapse">
            <el-collapse-item title="查看冲突详情" name="conflicts">
              <el-table :data="result.conflicts" size="small">
                <el-table-column prop="type" label="冲突类型" width="120">
                  <template #default="{ row }">
                    <el-tag :type="row.severity === 'HIGH' ? 'danger' : 'warning'" size="small">
                      {{ row.type }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="description" label="描述" />
              </el-table>
            </el-collapse-item>
          </el-collapse>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Tickets, Setting, Lightning, View, Promotion } from '@element-plus/icons-vue'
import { useRouter, useRoute } from 'vue-router'
import {
  getSolution,
  executeScheduling
} from '@/api/scheduling'
import { getSemester } from '@/api/semester'
import { getCourseOfferings } from '@/api/offering'
import { formatDateTime } from '@/utils/date'

const router = useRouter()
const route = useRoute()
const formRef = ref(null)
const loading = ref(false)
const executing = ref(false)

const solution = ref({})
const semesterName = ref('')
const availableCourses = ref([])
const result = ref(null)
const progress = ref(0)

const form = reactive({
  courseIds: [],
  maxIterations: 1000,
  timeoutSeconds: 60,
  optimizeFor: 'BALANCED'
})

const rules = {
  maxIterations: [
    { required: true, message: '请输入最大迭代次数', trigger: 'blur' },
    { type: 'number', min: 100, max: 10000, message: '迭代次数应在100-10000之间', trigger: 'blur' }
  ],
  timeoutSeconds: [
    { required: true, message: '请输入超时时间', trigger: 'blur' },
    { type: 'number', min: 10, max: 300, message: '超时时间应在10-300秒之间', trigger: 'blur' }
  ],
  optimizeFor: [
    { required: true, message: '请选择优化目标', trigger: 'change' }
  ]
}

const progressStatus = computed(() => {
  if (result.value) {
    return result.value.success ? 'success' : 'exception'
  }
  return undefined
})

// 状态类型映射
const getStatusType = (status) => {
  const typeMap = {
    DRAFT: 'info',
    OPTIMIZING: 'warning',
    COMPLETED: 'success',
    APPLIED: 'primary'
  }
  return typeMap[status] || 'info'
}

// 状态文本映射
const getStatusText = (status) => {
  const textMap = {
    DRAFT: '草稿',
    OPTIMIZING: '排课中',
    COMPLETED: '已完成',
    APPLIED: '已应用'
  }
  return textMap[status] || status
}

// 加载方案信息
const loadSolution = async () => {
  loading.value = true
  try {
    const res = await getSolution(route.params.id)
    solution.value = res.data

    // 加载学期信息
    const semesterRes = await getSemester(solution.value.semesterId)
    semesterName.value = semesterRes.data.name

    // 加载课程列表
    await loadCourses(solution.value.semesterId)
  } catch (error) {
    ElMessage.error(error.message || '加载方案信息失败')
  } finally {
    loading.value = false
  }
}

// 加载课程列表
const loadCourses = async (semesterId) => {
  try {
    const res = await getCourseOfferings({ semesterId })
    availableCourses.value = res.data || []
  } catch (error) {
    console.error('加载课程列表失败:', error)
  }
}

// 执行智能排课
const handleExecute = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    executing.value = true
    progress.value = 0
    result.value = null

    // 模拟进度
    const progressInterval = setInterval(() => {
      if (progress.value < 90) {
        progress.value += 10
      }
    }, 1000)

    try {
      const requestData = {
        semesterId: solution.value.semesterId,
        solutionName: solution.value.name,
        courseIds: form.courseIds.length > 0 ? form.courseIds : null,
        maxIterations: form.maxIterations,
        timeoutSeconds: form.timeoutSeconds,
        optimizeFor: form.optimizeFor
      }

      const res = await executeScheduling(route.params.id, requestData)
      result.value = res.data
      progress.value = 100

      if (result.value.success) {
        ElMessage.success('智能排课完成')
        await loadSolution()
      } else {
        ElMessage.warning('排课未完全成功，请查看冲突详情')
      }
    } catch (error) {
      progress.value = 100
      result.value = {
        success: false,
        message: error.message || '排课执行失败'
      }
      ElMessage.error(error.message || '排课执行失败')
    } finally {
      clearInterval(progressInterval)
      executing.value = false
    }
  })
}

// 预览结果
const handlePreview = () => {
  router.push({
    name: 'AdminSchedulingPreview',
    params: { id: route.params.id }
  })
}

// 返回
const goBack = () => {
  router.push({ name: 'AdminSchedulingSolutions' })
}

onMounted(() => {
  if (route.params.id) {
    loadSolution()
  }
})
</script>

<style scoped lang="scss">
.scheduling-optimize {
  padding: 20px;

  :deep(.el-page-header) {
    margin-bottom: 20px;
  }
}

.solution-card,
.config-card,
.progress-card {
  margin-bottom: 20px;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .card-title {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 16px;
      font-weight: 600;
      color: #303133;
    }
  }
}

.empty-text {
  color: #909399;
  font-style: italic;
}

.success-text {
  color: #67c23a;
  font-weight: 500;
}

.form-tip {
  margin-top: 4px;
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
}

.unit-label {
  margin-left: 8px;
  font-size: 12px;
  color: #909399;
}

.progress-content {
  .progress-text {
    font-size: 16px;
    font-weight: 600;
  }

  .progress-info {
    margin-top: 20px;

    .result-alert {
      margin-bottom: 16px;

      p {
        margin: 4px 0;
        line-height: 1.8;
      }
    }

    .conflict-collapse {
      margin-top: 16px;
    }
  }
}
</style>

