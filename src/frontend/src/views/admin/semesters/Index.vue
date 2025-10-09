<template>
  <div class="semester-management">
    <!-- 页面头部 - Apple 风格 -->
    <div class="page-header animate-fade-in-down">
      <div class="header-content">
        <h1 class="page-title">学期</h1>
        <p class="page-subtitle">管理学年学期和选课时间设置</p>
      </div>
      <el-button 
        type="primary" 
        :icon="Plus" 
        size="large"
        @click="handleAdd"
      >
        新建学期
      </el-button>
    </div>

    <!-- 当前活动学期横幅 - 简洁版 -->
    <div 
      v-if="activeSemester" 
      class="active-semester-card animate-fade-in-up"
      style="animation-delay: 0.1s;"
    >
      <div class="card-content">
        <div class="semester-badge">
          <el-icon><Select /></el-icon>
          <span>当前学期</span>
        </div>
        <h2 class="semester-name">{{ activeSemester.semesterNameWithWeek || activeSemester.semesterName }}</h2>
        <div class="semester-meta">
          <span class="meta-item">{{ activeSemester.startDate }} - {{ activeSemester.endDate }}</span>
          <span class="meta-divider">·</span>
          <span class="meta-item">选课时间：{{ formatDateTime(activeSemester.courseSelectionStart) }} - {{ formatDateTime(activeSemester.courseSelectionEnd) }}</span>
        </div>
        
        <!-- 进度条 -->
        <div class="progress-wrapper">
          <div class="progress-info">
            <span class="progress-label">学期进度</span>
            <span class="progress-value">{{ getSemesterProgress(activeSemester) }}%</span>
          </div>
          <div class="progress-bar">
            <div class="progress-fill" :style="{ width: getSemesterProgress(activeSemester) + '%' }"></div>
          </div>
        </div>
      </div>
    </div>

    <!-- 学期列表 -->
    <div v-loading="loading" class="semester-list">
      <div
        v-for="(semester, index) in sortedSemesterList"
        :key="semester.id"
        class="semester-item animate-fade-in-up"
        :style="{ 'animation-delay': `${0.2 + index * 0.05}s` }"
      >
        <div class="semester-main">
          <div class="semester-info">
            <div class="semester-header">
              <h3 class="semester-title">
                {{ semester.academicYear }}
                <span class="semester-type">{{ semester.semesterType === 1 ? '春季学期' : '秋季学期' }}</span>
                <span v-if="semester.active && semester.currentWeek" class="semester-week">第{{ semester.currentWeek }}周</span>
              </h3>
              <el-tag 
                v-if="semester.active" 
                type="primary"
                size="small"
              >
                活动中
              </el-tag>
              <el-tag 
                v-else-if="isUpcoming(semester)" 
                type="warning"
                size="small"
              >
                未来学期
              </el-tag>
              <el-tag 
                v-else 
                type="info"
                size="small"
              >
                已结束
              </el-tag>
            </div>
            
            <div class="semester-details">
              <div class="detail-item">
                <span class="detail-label">学期时间</span>
                <span class="detail-value">{{ semester.startDate }} 至 {{ semester.endDate }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">选课时间</span>
                <span class="detail-value">{{ formatDateTime(semester.courseSelectionStart) }} 至 {{ formatDateTime(semester.courseSelectionEnd) }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">选课状态</span>
                <span class="detail-value">
                  <el-tag 
                    :type="getCourseSelectionStatusType(semester)" 
                    size="small"
                  >
                    {{ semester.courseSelectionStatus || '未知' }}
                  </el-tag>
                  <el-switch
                    v-model="semester.courseSelectionEnabled"
                    style="margin-left: 10px"
                    :active-text="semester.courseSelectionEnabled ? '已启用' : '已关闭'"
                    @change="handleToggleCourseSelection(semester)"
                  />
                </span>
              </div>
            </div>
          </div>

          <div class="semester-actions">
            <el-button
              v-if="!semester.active"
              type="primary"
              size="default"
              plain
              @click="handleActivate(semester)"
            >
              设为当前
            </el-button>
            <el-button
              type="default"
              size="default"
              @click="handleEditCourseSelectionTime(semester)"
              :icon="Clock"
            >
              选课时间
            </el-button>
            <el-button
              type="default"
              size="default"
              @click="handleEdit(semester)"
              :disabled="semester.active"
            >
              编辑
            </el-button>
            <el-button
              type="danger"
              size="default"
              plain
              @click="handleDelete(semester)"
              :disabled="semester.active"
            >
              删除
            </el-button>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <el-empty
        v-if="!loading && semesterList.length === 0"
        description="还没有创建任何学期"
        :image-size="160"
      >
        <el-button type="primary" :icon="Plus" @click="handleAdd">
          创建第一个学期
        </el-button>
      </el-empty>
    </div>

    <!-- 新建/编辑学期对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="resetForm"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
        label-position="top"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="学年" prop="academicYear">
              <el-input 
                v-model="formData.academicYear" 
                placeholder="例如：2024-2025"
                size="large"
              />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="学期类型" prop="semesterType">
              <el-select v-model="formData.semesterType" size="large" style="width: 100%">
                <el-option :label="'春季学期'" :value="1" />
                <el-option :label="'秋季学期'" :value="2" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="开始日期" prop="startDate">
              <el-date-picker
                v-model="formData.startDate"
                type="date"
                placeholder="选择开始日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                size="large"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="结束日期" prop="endDate">
              <el-date-picker
                v-model="formData.endDate"
                type="date"
                placeholder="选择结束日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                size="large"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="选课开始时间" prop="courseSelectionStart">
              <el-date-picker
                v-model="formData.courseSelectionStart"
                type="datetime"
                placeholder="选择开始时间"
                format="YYYY-MM-DD HH:mm:ss"
                value-format="YYYY-MM-DD HH:mm:ss"
                size="large"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="选课结束时间" prop="courseSelectionEnd">
              <el-date-picker
                v-model="formData.courseSelectionEnd"
                type="datetime"
                placeholder="选择结束时间"
                format="YYYY-MM-DD HH:mm:ss"
                value-format="YYYY-MM-DD HH:mm:ss"
                size="large"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button size="large" @click="dialogVisible = false">
            取消
          </el-button>
          <el-button 
            type="primary" 
            size="large"
            :loading="submitLoading"
            @click="handleSubmit"
          >
            {{ editingId ? '保存' : '创建' }}
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 编辑选课时间对话框 -->
    <el-dialog
      v-model="courseSelectionTimeDialogVisible"
      title="编辑选课时间"
      width="500px"
    >
      <el-form label-width="120px">
        <el-form-item label="选课开始时间">
          <el-date-picker
            v-model="courseSelectionTimeForm.startTime"
            type="datetime"
            placeholder="选择开始时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="选课结束时间">
          <el-date-picker
            v-model="courseSelectionTimeForm.endTime"
            type="datetime"
            placeholder="选择结束时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="courseSelectionTimeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitCourseSelectionTime">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Select, Clock } from '@element-plus/icons-vue'
import {
  getSemesters,
  createSemester,
  updateSemester,
  deleteSemester,
  setActiveSemester,
  toggleCourseSelection,
  updateCourseSelectionTime
} from '@/api/system'

const loading = ref(false)
const semesterList = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const submitLoading = ref(false)
const formRef = ref(null)
const editingId = ref(null)

const formData = ref({
  academicYear: '',
  semesterType: 1,
  startDate: '',
  endDate: '',
  courseSelectionStart: '',
  courseSelectionEnd: ''
})

const formRules = {
  academicYear: [
    { required: true, message: '请输入学年', trigger: 'blur' },
    { pattern: /^\d{4}-\d{4}$/, message: '学年格式：YYYY-YYYY，例如：2024-2025', trigger: 'blur' }
  ],
  semesterType: [
    { required: true, message: '请选择学期类型', trigger: 'change' }
  ],
  startDate: [
    { required: true, message: '请选择开始日期', trigger: 'change' }
  ],
  endDate: [
    { required: true, message: '请选择结束日期', trigger: 'change' }
  ],
  courseSelectionStart: [
    { required: true, message: '请选择选课开始时间', trigger: 'change' }
  ],
  courseSelectionEnd: [
    { required: true, message: '请选择选课结束时间', trigger: 'change' }
  ]
}

// 计算属性
const activeSemester = computed(() => {
  return semesterList.value.find(s => s.active)
})

const sortedSemesterList = computed(() => {
  return [...semesterList.value]
    .sort((a, b) => new Date(b.startDate) - new Date(a.startDate))
})

// 方法
const isUpcoming = (semester) => {
  return new Date(semester.startDate) > new Date()
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return dateTime.replace('T', ' ').substring(0, 16)
}

const getSemesterProgress = (semester) => {
  const start = new Date(semester.startDate).getTime()
  const end = new Date(semester.endDate).getTime()
  const now = Date.now()
  
  if (now < start) return 0
  if (now > end) return 100
  
  return Math.round(((now - start) / (end - start)) * 100)
}

const fetchSemesterList = async () => {
  try {
    loading.value = true
    const { data } = await getSemesters()
    semesterList.value = data || []
  } catch (error) {
    console.error('获取学期列表失败:', error)
    ElMessage.error('获取学期列表失败')
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  dialogTitle.value = '新建学期'
  editingId.value = null
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑学期'
  editingId.value = row.id
  formData.value = {
    academicYear: row.academicYear,
    semesterType: row.semesterType,
    startDate: row.startDate,
    endDate: row.endDate,
    courseSelectionStart: row.courseSelectionStart,
    courseSelectionEnd: row.courseSelectionEnd
  }
  dialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定要删除学期"${row.academicYear} ${row.semesterType === 1 ? '春季' : '秋季'}"吗？`,
    '删除确认',
    {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await deleteSemester(row.id)
      ElMessage.success('删除成功')
      fetchSemesterList()
    } catch (error) {
      console.error('删除失败:', error)
      ElMessage.error(error.response?.data?.message || '删除失败')
    }
  }).catch(() => {})
}

const handleActivate = (row) => {
  ElMessageBox.confirm(
    `确定要将"${row.academicYear} ${row.semesterType === 1 ? '春季' : '秋季'}"设为当前活动学期吗？`,
    '设置确认',
    {
      confirmButtonText: '设置',
      cancelButtonText: '取消',
      type: 'info'
    }
  ).then(async () => {
    try {
      await setActiveSemester(row.id)
      ElMessage.success('设置成功')
      fetchSemesterList()
    } catch (error) {
      console.error('设置失败:', error)
      ElMessage.error(error.response?.data?.message || '设置失败')
    }
  }).catch(() => {})
}

// 获取选课状态标签类型
const getCourseSelectionStatusType = (semester) => {
  if (!semester.courseSelectionEnabled) return 'info'
  if (semester.courseSelectionAvailable) return 'success'
  if (semester.courseSelectionStatus === '选课未开始') return 'warning'
  return 'info'
}

// 切换选课功能
const handleToggleCourseSelection = async (semester) => {
  try {
    await toggleCourseSelection(semester.id, semester.courseSelectionEnabled)
    ElMessage.success(semester.courseSelectionEnabled ? '选课功能已开启' : '选课功能已关闭')
    fetchSemesterList()
  } catch (error) {
    // 恢复开关状态
    semester.courseSelectionEnabled = !semester.courseSelectionEnabled
    console.error('切换选课功能失败:', error)
    ElMessage.error(error.response?.data?.message || '操作失败')
  }
}

// 编辑选课时间对话框
const courseSelectionTimeDialogVisible = ref(false)
const courseSelectionTimeForm = ref({
  semesterId: null,
  startTime: '',
  endTime: ''
})

const handleEditCourseSelectionTime = (semester) => {
  courseSelectionTimeForm.value = {
    semesterId: semester.id,
    startTime: semester.courseSelectionStart,
    endTime: semester.courseSelectionEnd
  }
  courseSelectionTimeDialogVisible.value = true
}

const submitCourseSelectionTime = async () => {
  try {
    const { semesterId, startTime, endTime } = courseSelectionTimeForm.value
    await updateCourseSelectionTime(semesterId, startTime, endTime)
    ElMessage.success('选课时间更新成功')
    courseSelectionTimeDialogVisible.value = false
    fetchSemesterList()
  } catch (error) {
    console.error('更新选课时间失败:', error)
    ElMessage.error(error.response?.data?.message || '更新失败')
  }
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  try {
    submitLoading.value = true
    
    if (editingId.value) {
      await updateSemester(editingId.value, formData.value)
      ElMessage.success('更新成功')
    } else {
      await createSemester(formData.value)
      ElMessage.success('创建成功')
    }
    
    dialogVisible.value = false
    fetchSemesterList()
  } catch (error) {
    console.error('提交失败:', error)
    ElMessage.error(error.response?.data?.message || '提交失败')
  } finally {
    submitLoading.value = false
  }
}

const resetForm = () => {
  formRef.value?.resetFields()
  formData.value = {
    academicYear: '',
    semesterType: 1,
    startDate: '',
    endDate: '',
    courseSelectionStart: '',
    courseSelectionEnd: ''
  }
  editingId.value = null
}

onMounted(() => {
  fetchSemesterList()
})
</script>

<style scoped lang="scss">
.semester-management {
  max-width: 1200px;
  margin: 0 auto;
  padding: var(--spacing-3xl) var(--spacing-xl);

  // ===================================
  // 页面头部 - Apple 风格
  // ===================================

  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: var(--spacing-3xl);
  }

  .header-content {
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
  // 活动学期卡片 - 简洁版
  // ===================================

  .active-semester-card {
    background: var(--bg-primary);
    border-radius: var(--radius-xl);
    padding: var(--spacing-2xl);
    margin-bottom: var(--spacing-2xl);
    box-shadow: var(--shadow-card);
    border: 1px solid var(--border-light);
  }

  .semester-badge {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    padding: 6px 14px;
    background: rgba(0, 122, 255, 0.1);
    color: var(--primary-color);
    border-radius: var(--radius-full);
    font-size: 13px;
    font-weight: 500;
    margin-bottom: var(--spacing-md);
  }

  .semester-name {
    font-size: 28px;
    font-weight: 600;
    color: var(--text-primary);
    margin: 0 0 12px;
    letter-spacing: -0.02em;
  }

  .semester-meta {
    display: flex;
    align-items: center;
    gap: 12px;
    font-size: 15px;
    color: var(--text-secondary);
    margin-bottom: var(--spacing-xl);

    .meta-item {
      line-height: 1.5;
    }

    .meta-divider {
      color: var(--text-tertiary);
    }
  }

  .progress-wrapper {
    margin-top: var(--spacing-lg);
  }

  .progress-info {
    display: flex;
    justify-content: space-between;
    margin-bottom: 12px;
    font-size: 14px;
  }

  .progress-label {
    color: var(--text-secondary);
    font-weight: 500;
  }

  .progress-value {
    color: var(--text-primary);
    font-weight: 600;
  }

  .progress-bar {
    height: 6px;
    background: var(--bg-tertiary);
    border-radius: var(--radius-full);
    overflow: hidden;
  }

  .progress-fill {
    height: 100%;
    background: var(--primary-color);
    border-radius: var(--radius-full);
    transition: width var(--transition-smooth);
  }

  // ===================================
  // 学期列表
  // ===================================

  .semester-list {
    display: flex;
    flex-direction: column;
    gap: var(--spacing-md);
  }

  .semester-item {
    background: var(--bg-primary);
    border-radius: var(--radius-lg);
    border: 1px solid var(--border-light);
    transition: all var(--transition-base);

    &:hover {
      box-shadow: var(--shadow-card);
      border-color: var(--border-color);
    }
  }

  .semester-main {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: var(--spacing-lg) var(--spacing-xl);
    gap: var(--spacing-xl);

    @media (max-width: 768px) {
      flex-direction: column;
      align-items: stretch;
    }
  }

  .semester-info {
    flex: 1;
  }

  .semester-header {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: var(--spacing-md);
  }

  .semester-title {
    font-size: 19px;
    font-weight: 600;
    color: var(--text-primary);
    margin: 0;
    letter-spacing: -0.01em;
  }

  .semester-type {
    color: var(--text-secondary);
    font-weight: 500;
  }

  .semester-week {
    margin-left: 8px;
    padding: 2px 8px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    border-radius: 12px;
    font-size: 12px;
    font-weight: 600;
  }

  .semester-details {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }

  .detail-item {
    display: flex;
    gap: 12px;
    font-size: 14px;
  }

  .detail-label {
    color: var(--text-tertiary);
    min-width: 80px;
  }

  .detail-value {
    color: var(--text-secondary);
  }

  .semester-actions {
    display: flex;
    gap: 8px;
    flex-shrink: 0;

    @media (max-width: 768px) {
      justify-content: flex-end;
    }
  }

  // ===================================
  // 对话框样式
  // ===================================

  .dialog-footer {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
  }
}
</style>
