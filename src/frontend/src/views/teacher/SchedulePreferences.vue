<template>
  <div class="schedule-preferences">
    <el-page-header @back="goBack" title="返回" content="排课偏好设置" />

    <el-card class="preference-card" shadow="hover" v-loading="loading">
      <template #header>
        <div class="card-header">
          <span class="card-title">
            <el-icon><Setting /></el-icon>
            我的排课偏好
          </span>
          <el-button 
            type="danger" 
            plain 
            size="small"
            v-if="hasPreference"
            @click="handleDelete"
          >
            清除偏好
          </el-button>
        </div>
      </template>

      <el-alert
        title="提示"
        type="info"
        :closable="false"
        class="info-alert"
      >
        <template #default>
          设置排课偏好后，系统在进行智能排课时会尽量满足您的偏好。注意：偏好设置仅作为软约束，不保证完全满足。
        </template>
      </el-alert>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="140px"
        class="preference-form"
      >
        <el-form-item label="偏好星期" prop="preferredDays">
          <el-checkbox-group v-model="form.preferredDays">
            <el-checkbox :label="1">星期一</el-checkbox>
            <el-checkbox :label="2">星期二</el-checkbox>
            <el-checkbox :label="3">星期三</el-checkbox>
            <el-checkbox :label="4">星期四</el-checkbox>
            <el-checkbox :label="5">星期五</el-checkbox>
          </el-checkbox-group>
          <div class="form-tip">选择您希望安排课程的星期</div>
        </el-form-item>

        <el-form-item label="偏好时段" prop="preferredTimeSlots">
          <el-checkbox-group v-model="form.preferredTimeSlots">
            <el-checkbox :label="1">第1-2节 (08:00-09:40)</el-checkbox>
            <el-checkbox :label="2">第3-4节 (10:00-11:40)</el-checkbox>
            <el-checkbox :label="3">第5-6节 (14:00-15:40)</el-checkbox>
            <el-checkbox :label="4">第7-8节 (16:00-17:40)</el-checkbox>
            <el-checkbox :label="5">第9-10节 (19:00-20:40)</el-checkbox>
          </el-checkbox-group>
          <div class="form-tip">选择您希望安排课程的时段</div>
        </el-form-item>

        <el-form-item label="每天最多课时" prop="maxDailyHours">
          <el-input-number
            v-model="form.maxDailyHours"
            :min="0"
            :max="12"
            :step="2"
            placeholder="请输入每天最多课时"
          />
          <span class="unit-label">节</span>
          <div class="form-tip">设置每天愿意承担的最大课时数（0表示不限制）</div>
        </el-form-item>

        <el-form-item label="每周最多课时" prop="maxWeeklyHours">
          <el-input-number
            v-model="form.maxWeeklyHours"
            :min="0"
            :max="40"
            :step="2"
            placeholder="请输入每周最多课时"
          />
          <span class="unit-label">节</span>
          <div class="form-tip">设置每周愿意承担的最大课时数（0表示不限制）</div>
        </el-form-item>

        <el-form-item label="备注说明" prop="notes">
          <el-input
            v-model="form.notes"
            type="textarea"
            :rows="4"
            placeholder="可以填写其他特殊说明，如特定时间段不可用等"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">
            <el-icon><Check /></el-icon>
            保存偏好
          </el-button>
          <el-button @click="handleReset">
            <el-icon><RefreshLeft /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 偏好预览 -->
    <el-card class="preview-card" shadow="hover" v-if="hasPreference">
      <template #header>
        <div class="card-header">
          <span class="card-title">
            <el-icon><View /></el-icon>
            当前偏好预览
          </span>
        </div>
      </template>

      <div class="preference-preview">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="偏好星期">
            <el-tag 
              v-for="day in preferredDaysDisplay" 
              :key="day" 
              type="primary" 
              size="small"
              class="day-tag"
            >
              {{ day }}
            </el-tag>
            <span v-if="preferredDaysDisplay.length === 0" class="empty-text">未设置</span>
          </el-descriptions-item>

          <el-descriptions-item label="偏好时段">
            <el-tag 
              v-for="slot in preferredTimeSlotsDisplay" 
              :key="slot" 
              type="success" 
              size="small"
              class="slot-tag"
            >
              {{ slot }}
            </el-tag>
            <span v-if="preferredTimeSlotsDisplay.length === 0" class="empty-text">未设置</span>
          </el-descriptions-item>

          <el-descriptions-item label="每天最多课时">
            <span class="value-text">
              {{ form.maxDailyHours || 0 }} 节
              <span v-if="!form.maxDailyHours" class="empty-text">（不限制）</span>
            </span>
          </el-descriptions-item>

          <el-descriptions-item label="每周最多课时">
            <span class="value-text">
              {{ form.maxWeeklyHours || 0 }} 节
              <span v-if="!form.maxWeeklyHours" class="empty-text">（不限制）</span>
            </span>
          </el-descriptions-item>

          <el-descriptions-item label="备注说明" :span="2">
            {{ form.notes || '无' }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Setting, Check, RefreshLeft, View } from '@element-plus/icons-vue'
import { 
  getMyPreference, 
  setTeacherPreference, 
  updateTeacherPreference,
  deleteMyPreference 
} from '@/api/scheduling'
import { useRouter } from 'vue-router'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const submitting = ref(false)
const hasPreference = ref(false)

// 星期映射
const dayMap = {
  1: '星期一',
  2: '星期二',
  3: '星期三',
  4: '星期四',
  5: '星期五'
}

// 时段映射
const slotMap = {
  1: '第1-2节',
  2: '第3-4节',
  3: '第5-6节',
  4: '第7-8节',
  5: '第9-10节'
}

const form = reactive({
  preferredDays: [],
  preferredTimeSlots: [],
  maxDailyHours: 0,
  maxWeeklyHours: 0,
  notes: ''
})

const rules = {
  preferredDays: [
    { type: 'array', message: '请选择偏好星期', trigger: 'change' }
  ],
  preferredTimeSlots: [
    { type: 'array', message: '请选择偏好时段', trigger: 'change' }
  ],
  maxDailyHours: [
    { type: 'number', min: 0, max: 12, message: '每天课时应在0-12之间', trigger: 'blur' }
  ],
  maxWeeklyHours: [
    { type: 'number', min: 0, max: 40, message: '每周课时应在0-40之间', trigger: 'blur' }
  ]
}

// 计算显示文本
const preferredDaysDisplay = computed(() => {
  return form.preferredDays.map(day => dayMap[day]).filter(Boolean)
})

const preferredTimeSlotsDisplay = computed(() => {
  return form.preferredTimeSlots.map(slot => slotMap[slot]).filter(Boolean)
})

// 加载偏好数据
const loadPreference = async () => {
  loading.value = true
  try {
    const res = await getMyPreference()
    if (res.data) {
      hasPreference.value = true
      form.preferredDays = res.data.preferredDaysList || []
      form.preferredTimeSlots = res.data.preferredTimeSlotsList || []
      form.maxDailyHours = res.data.maxDailyHours || 0
      form.maxWeeklyHours = res.data.maxWeeklyHours || 0
      form.notes = res.data.notes || ''
    }
  } catch (error) {
    // 如果404说明没有设置偏好，不提示错误
    if (error.response?.status !== 404) {
      ElMessage.error(error.message || '加载偏好失败')
    }
  } finally {
    loading.value = false
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      const data = {
        preferredDays: form.preferredDays,
        preferredTimeSlots: form.preferredTimeSlots,
        maxDailyHours: form.maxDailyHours || null,
        maxWeeklyHours: form.maxWeeklyHours || null,
        notes: form.notes || null
      }

      if (hasPreference.value) {
        await updateTeacherPreference(data)
        ElMessage.success('更新偏好成功')
      } else {
        await setTeacherPreference(data)
        ElMessage.success('设置偏好成功')
        hasPreference.value = true
      }
      
      await loadPreference()
    } catch (error) {
      ElMessage.error(error.message || '保存偏好失败')
    } finally {
      submitting.value = false
    }
  })
}

// 重置表单
const handleReset = () => {
  if (hasPreference.value) {
    loadPreference()
  } else {
    formRef.value?.resetFields()
  }
}

// 删除偏好
const handleDelete = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要清除所有排课偏好吗？此操作不可恢复。',
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    loading.value = true
    await deleteMyPreference()
    ElMessage.success('清除偏好成功')
    hasPreference.value = false
    formRef.value?.resetFields()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '清除偏好失败')
    }
  } finally {
    loading.value = false
  }
}

// 返回
const goBack = () => {
  // 返回到教师工作台
  router.push({ name: 'TeacherDashboard' })
}

onMounted(() => {
  loadPreference()
})
</script>

<style scoped lang="scss">
.schedule-preferences {
  padding: 20px;

  :deep(.el-page-header) {
    margin-bottom: 20px;
  }
}

.preference-card,
.preview-card {
  margin-bottom: 20px;
  animation: fadeIn 0.5s;

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

.info-alert {
  margin-bottom: 24px;
}

.preference-form {
  .form-tip {
    margin-top: 4px;
    font-size: 12px;
    color: #909399;
  }

  .unit-label {
    margin-left: 8px;
    color: #606266;
  }

  :deep(.el-checkbox) {
    margin-right: 20px;
    margin-bottom: 12px;
  }
}

.preference-preview {
  .day-tag,
  .slot-tag {
    margin-right: 8px;
    margin-bottom: 4px;
  }

  .empty-text {
    color: #909399;
    font-style: italic;
  }

  .value-text {
    font-weight: 500;
    color: #303133;

    .empty-text {
      font-weight: normal;
      margin-left: 4px;
    }
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>

