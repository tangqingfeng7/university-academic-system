<template>
  <div class="period-management-page">
    <!-- 页面头部 -->
    <div class="page-header animate-fade-in-down">
      <div class="header-content">
        <h1 class="page-title">评价周期管理</h1>
        <p class="page-subtitle">管理教学评价的开放时间和周期</p>
      </div>
      <el-button 
        type="primary" 
        :icon="Plus"
        size="large"
        @click="handleCreate"
      >
        创建评价周期
      </el-button>
    </div>

    <!-- 周期列表 -->
    <div class="periods-container animate-fade-in-up" style="animation-delay: 0.1s;">
      <div v-loading="loading" class="periods-list">
        <el-empty 
          v-if="periods.length === 0 && !loading"
          description="暂无评价周期"
        >
          <el-button type="primary" @click="handleCreate">
            创建第一个评价周期
          </el-button>
        </el-empty>

        <div v-else class="period-cards">
          <div 
            v-for="period in periods" 
            :key="period.id"
            class="period-card"
            :class="{ 'period-active': period.isActive }"
          >
            <div class="card-header">
              <div class="period-info">
                <h3>{{ period.semesterName || '未知学期' }}</h3>
                <p class="period-desc">{{ period.description || '暂无描述' }}</p>
              </div>
              <div class="status-badge">
                <el-tag 
                  :type="getStatusType(period)" 
                  size="large"
                  effect="dark"
                >
                  {{ getStatusText(period) }}
                </el-tag>
              </div>
            </div>

            <div class="card-body">
              <div class="time-info">
                <div class="time-item">
                  <el-icon><Calendar /></el-icon>
                  <div class="time-content">
                    <span class="label">开始时间</span>
                    <span class="value">{{ formatDateTime(period.startTime) }}</span>
                  </div>
                </div>
                <div class="time-item">
                  <el-icon><Calendar /></el-icon>
                  <div class="time-content">
                    <span class="label">结束时间</span>
                    <span class="value">{{ formatDateTime(period.endTime) }}</span>
                  </div>
                </div>
              </div>

              <div class="progress-bar">
                <el-progress 
                  :percentage="getProgress(period)" 
                  :status="getProgressStatus(period)"
                  :stroke-width="8"
                />
              </div>
            </div>

            <div class="card-footer">
              <div class="meta-info">
                <span class="created-time">
                  创建于 {{ formatDate(period.createdAt) }}
                </span>
              </div>
              <div class="actions">
                <el-button
                  v-if="!period.isActive && !isExpired(period)"
                  type="success"
                  size="small"
                  :icon="VideoPlay"
                  @click="handleToggle(period, true)"
                >
                  开启
                </el-button>
                <el-button
                  v-if="period.isActive"
                  type="warning"
                  size="small"
                  :icon="VideoPause"
                  @click="handleToggle(period, false)"
                >
                  关闭
                </el-button>
                <el-button
                  type="primary"
                  size="small"
                  :icon="Edit"
                  @click="handleEdit(period)"
                  :disabled="period.isActive"
                >
                  编辑
                </el-button>
                <el-button
                  type="danger"
                  size="small"
                  :icon="Delete"
                  @click="handleDelete(period)"
                  :disabled="period.isActive"
                >
                  删除
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 创建/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="学期" prop="semesterId">
          <el-select
            v-model="form.semesterId"
            placeholder="请选择学期"
            style="width: 100%"
            :disabled="!!editingId"
          >
            <el-option
              v-for="semester in semesters"
              :key="semester.id"
              :label="semester.academicYear + ' ' + getSemesterTypeName(semester.semesterType)"
              :value="semester.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker
            v-model="form.startTime"
            type="datetime"
            placeholder="选择开始时间"
            style="width: 100%"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>

        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker
            v-model="form.endTime"
            type="datetime"
            placeholder="选择结束时间"
            style="width: 100%"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>

        <el-form-item label="描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="4"
            placeholder="请输入评价周期描述（选填）"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">
            确定
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Plus, 
  Edit, 
  Delete, 
  Calendar, 
  VideoPlay, 
  VideoPause 
} from '@element-plus/icons-vue'
import { 
  getAllPeriods, 
  createPeriod, 
  updatePeriod, 
  deletePeriod, 
  togglePeriod 
} from '@/api/evaluation'
import { getAllSemesters } from '@/api/semester'

const loading = ref(false)
const periods = ref([])
const semesters = ref([])
const dialogVisible = ref(false)
const submitting = ref(false)
const editingId = ref(null)
const formRef = ref(null)

const form = ref({
  semesterId: null,
  startTime: '',
  endTime: '',
  description: ''
})

const rules = {
  semesterId: [
    { required: true, message: '请选择学期', trigger: 'change' }
  ],
  startTime: [
    { required: true, message: '请选择开始时间', trigger: 'change' }
  ],
  endTime: [
    { required: true, message: '请选择结束时间', trigger: 'change' }
  ]
}

const dialogTitle = computed(() => {
  return editingId.value ? '编辑评价周期' : '创建评价周期'
})

// 加载评价周期列表
const loadPeriods = async () => {
  loading.value = true
  try {
    const res = await getAllPeriods()
    if (res.code === 200) {
      periods.value = res.data || []
    }
  } catch (error) {
    ElMessage.error('加载评价周期失败')
  } finally {
    loading.value = false
  }
}

// 加载学期列表
const loadSemesters = async () => {
  try {
    const res = await getAllSemesters()
    if (res.code === 200) {
      semesters.value = res.data || []
    }
  } catch (error) {
    ElMessage.error('加载学期列表失败')
  }
}

// 获取状态类型
const getStatusType = (period) => {
  if (period.isActive) return 'success'
  if (isExpired(period)) return 'info'
  return 'warning'
}

// 获取状态文本
const getStatusText = (period) => {
  if (period.isActive) return '进行中'
  if (isExpired(period)) return '已结束'
  return '未开始'
}

// 判断是否已过期
const isExpired = (period) => {
  return new Date(period.endTime) < new Date()
}

// 获取进度
const getProgress = (period) => {
  const now = new Date()
  const start = new Date(period.startTime)
  const end = new Date(period.endTime)
  
  if (now < start) return 0
  if (now > end) return 100
  
  const total = end - start
  const elapsed = now - start
  return Math.round((elapsed / total) * 100)
}

// 获取进度状态
const getProgressStatus = (period) => {
  if (period.isActive) return 'success'
  if (isExpired(period)) return 'info'
  return 'warning'
}

// 创建周期
const handleCreate = () => {
  editingId.value = null
  form.value = {
    semesterId: null,
    startTime: '',
    endTime: '',
    description: ''
  }
  dialogVisible.value = true
}

// 编辑周期
const handleEdit = (period) => {
  editingId.value = period.id
  form.value = {
    semesterId: period.semesterId,
    startTime: period.startTime,
    endTime: period.endTime,
    description: period.description || ''
  }
  dialogVisible.value = true
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()

    submitting.value = true

    if (editingId.value) {
      await updatePeriod(editingId.value, form.value)
      ElMessage.success('更新成功')
    } else {
      await createPeriod(form.value)
      ElMessage.success('创建成功')
    }

    dialogVisible.value = false
    loadPeriods()
  } catch (error) {
    if (error !== false) {
      ElMessage.error(error.message || '操作失败')
    }
  } finally {
    submitting.value = false
  }
}

// 切换状态
const handleToggle = async (period, active) => {
  try {
    const action = active ? '开启' : '关闭'
    await ElMessageBox.confirm(
      `确定要${action}该评价周期吗？`,
      '确认操作',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await togglePeriod(period.id, active)
    ElMessage.success(`${action}成功`)
    loadPeriods()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

// 删除周期
const handleDelete = async (period) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除该评价周期吗？此操作不可恢复。',
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'error'
      }
    )

    await deletePeriod(period.id)
    ElMessage.success('删除成功')
    loadPeriods()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 关闭对话框
const handleDialogClose = () => {
  formRef.value?.clearValidate()
}

// 获取学期类型名称
const getSemesterTypeName = (type) => {
  return type === 1 ? '春季学期' : '秋季学期'
}

// 格式化日期时间
const formatDateTime = (datetime) => {
  if (!datetime) return ''
  const date = new Date(datetime)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 格式化日期
const formatDate = (datetime) => {
  if (!datetime) return ''
  const date = new Date(datetime)
  return date.toLocaleDateString('zh-CN')
}

onMounted(() => {
  loadSemesters()
  loadPeriods()
})
</script>

<style scoped>
.period-management-page {
  padding: 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.header-content {
  flex: 1;
}

.page-title {
  font-size: 28px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 8px 0;
}

.page-subtitle {
  font-size: 14px;
  color: #666;
  margin: 0;
}

.periods-container {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.periods-list {
  min-height: 400px;
}

.period-cards {
  display: grid;
  gap: 20px;
}

.period-card {
  border: 2px solid #e4e7ed;
  border-radius: 12px;
  padding: 24px;
  transition: all 0.3s ease;
  background: white;
}

.period-card:hover {
  border-color: #409eff;
  box-shadow: 0 4px 16px rgba(64, 158, 255, 0.15);
  transform: translateY(-2px);
}

.period-active {
  border-color: #67c23a;
  background: linear-gradient(135deg, #f6ffed 0%, #ffffff 100%);
}

.period-active:hover {
  border-color: #67c23a;
  box-shadow: 0 4px 16px rgba(103, 194, 58, 0.25);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 2px solid #f0f2f5;
}

.period-info {
  flex: 1;
}

.period-info h3 {
  font-size: 20px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 8px 0;
}

.period-desc {
  font-size: 14px;
  color: #666;
  margin: 0;
  line-height: 1.6;
}

.status-badge {
  flex-shrink: 0;
}

.card-body {
  margin-bottom: 20px;
}

.time-info {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  margin-bottom: 16px;
}

.time-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
}

.time-item .el-icon {
  font-size: 24px;
  color: #409eff;
}

.time-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.time-content .label {
  font-size: 12px;
  color: #909399;
}

.time-content .value {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.progress-bar {
  margin-top: 12px;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 20px;
  border-top: 1px solid #e4e7ed;
}

.meta-info {
  flex: 1;
}

.created-time {
  font-size: 12px;
  color: #909399;
}

.actions {
  display: flex;
  gap: 8px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* 动画 */
@keyframes fade-in-down {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes fade-in-up {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.animate-fade-in-down {
  animation: fade-in-down 0.5s ease-out;
}

.animate-fade-in-up {
  animation: fade-in-up 0.5s ease-out;
}
</style>

