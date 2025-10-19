<template>
  <div class="attendance-warnings">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>考勤预警管理</span>
          <el-button type="primary" @click="executeCheck">
            <el-icon><Refresh /></el-icon>
            手动检测
          </el-button>
        </div>
      </template>

      <el-tabs v-model="activeTab" @tab-change="loadWarnings">
        <el-tab-pane label="待处理" name="PENDING">
          <template #label>
            待处理 
            <el-badge v-if="pendingCount > 0" :value="pendingCount" class="ml-2" />
          </template>
        </el-tab-pane>
        <el-tab-pane label="已处理" name="HANDLED" />
        <el-tab-pane label="已忽略" name="IGNORED" />
      </el-tabs>

      <el-table :data="warnings" v-loading="loading">
        <el-table-column label="预警类型" width="150">
          <template #default="{ row }">
            <el-tag :type="getWarningTypeColor(row.warningType)">
              {{ getWarningTypeText(row.warningType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="预警级别" width="100">
          <template #default="{ row }">
            <el-tag :type="getLevelColor(row.warningLevel)">
              {{ getLevelText(row.warningLevel) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="targetName" label="目标" width="120" />
        <el-table-column label="课程" width="150">
          <template #default="{ row }">
            {{ row.offering?.course?.name || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="warningMessage" label="预警消息" min-width="300" show-overflow-tooltip />
        <el-table-column prop="createdAt" label="创建时间" width="160" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === 'PENDING'">
              <el-button type="success" size="small" @click="handleWarning(row, 'handle')">
                处理
              </el-button>
              <el-button type="info" size="small" @click="handleWarning(row, 'ignore')">
                忽略
              </el-button>
            </template>
            <span v-else class="text-secondary">{{ row.handleComment || '-' }}</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 处理预警对话框 -->
    <el-dialog v-model="showHandleDialog" title="处理预警" width="500px">
      <el-form :model="handleForm" label-width="100px">
        <el-form-item label="处理意见">
          <el-input 
            v-model="handleForm.comment" 
            type="textarea" 
            :rows="4"
            placeholder="请输入处理意见" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showHandleDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmHandle">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import {
  getWarnings,
  handleWarning as handleWarningApi,
  ignoreWarning,
  executeWarningCheck
} from '@/api/attendance'

const loading = ref(false)
const activeTab = ref('PENDING')
const warnings = ref([])
const showHandleDialog = ref(false)
const currentWarning = ref(null)
const handleForm = ref({
  comment: ''
})

const pendingCount = computed(() => {
  return warnings.value.filter(w => w.status === 'PENDING').length
})

onMounted(() => {
  loadWarnings()
})

const loadWarnings = async () => {
  try {
    loading.value = true
    const status = activeTab.value
    const res = await getWarnings(status)
    warnings.value = res.data || []
  } catch (error) {
    ElMessage.error('加载预警列表失败')
  } finally {
    loading.value = false
  }
}

const handleWarning = async (row, action) => {
  currentWarning.value = row
  
  if (action === 'handle') {
    handleForm.value.comment = ''
    showHandleDialog.value = true
  } else {
    try {
      await ElMessageBox.confirm('确定要忽略此预警吗？', '提示', {
        type: 'warning'
      })
      
      await ignoreWarning(row.id)
      ElMessage.success('预警已忽略')
      loadWarnings()
    } catch (error) {
      if (error !== 'cancel') {
        ElMessage.error(error.message || '操作失败')
      }
    }
  }
}

const confirmHandle = async () => {
  try {
    await handleWarningApi(currentWarning.value.id, handleForm.value.comment)
    ElMessage.success('预警已处理')
    showHandleDialog.value = false
    loadWarnings()
  } catch (error) {
    ElMessage.error(error.message || '处理失败')
  }
}

const executeCheck = async () => {
  try {
    loading.value = true
    await executeWarningCheck()
    ElMessage.success('预警检测已执行')
    loadWarnings()
  } catch (error) {
    ElMessage.error('检测失败')
  } finally {
    loading.value = false
  }
}

const getWarningTypeText = (type) => {
  const texts = {
    STUDENT_ABSENT: '学生旷课',
    COURSE_LOW_RATE: '出勤率低',
    TEACHER_NO_ATTENDANCE: '教师未考勤'
  }
  return texts[type] || type
}

const getWarningTypeColor = (type) => {
  const colors = {
    STUDENT_ABSENT: 'danger',
    COURSE_LOW_RATE: 'warning',
    TEACHER_NO_ATTENDANCE: 'info'
  }
  return colors[type] || ''
}

const getLevelText = (level) => {
  const texts = {
    1: '轻度',
    2: '中度',
    3: '严重'
  }
  return texts[level] || level
}

const getLevelColor = (level) => {
  const colors = {
    1: 'info',
    2: 'warning',
    3: 'danger'
  }
  return colors[level] || ''
}
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.mb-4 {
  margin-bottom: 16px;
}

.ml-2 {
  margin-left: 8px;
}

.text-secondary {
  color: #909399;
}
</style>

