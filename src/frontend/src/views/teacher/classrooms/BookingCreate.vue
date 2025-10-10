<template>
  <div class="booking-create-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span class="card-title">教室借用申请</span>
          <el-button @click="handleBack">
            <el-icon><Back /></el-icon>
            返回
          </el-button>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
        style="max-width: 800px"
      >
        <el-form-item label="教室" prop="classroomId" required>
          <el-select
            v-model="form.classroomId"
            placeholder="请选择教室"
            filterable
            clearable
            style="width: 100%"
            @change="handleClassroomChange"
          >
            <el-option
              v-for="classroom in availableClassrooms"
              :key="classroom.id"
              :label="`${classroom.roomNo} - ${classroom.building} (${classroom.capacity}人)`"
              :value="classroom.id"
            >
              <div style="display: flex; justify-content: space-between; align-items: center">
                <span>{{ classroom.roomNo }} - {{ classroom.building }}</span>
                <el-tag :type="getTypeTag(classroom.type)" size="small">
                  {{ getTypeName(classroom.type) }}
                </el-tag>
              </div>
            </el-option>
          </el-select>
          <div v-if="selectedClassroom" class="classroom-info">
            <el-descriptions :column="3" size="small" style="margin-top: 10px">
              <el-descriptions-item label="容量">
                {{ selectedClassroom.capacity }}人
              </el-descriptions-item>
              <el-descriptions-item label="类型">
                {{ getTypeName(selectedClassroom.type) }}
              </el-descriptions-item>
              <el-descriptions-item label="状态">
                <el-tag :type="getStatusTag(selectedClassroom.status)" size="small">
                  {{ getStatusName(selectedClassroom.status) }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="设备" :span="3">
                {{ formatEquipment(selectedClassroom.equipment) }}
              </el-descriptions-item>
            </el-descriptions>
          </div>
        </el-form-item>

        <el-form-item label="借用时间" required>
          <el-col :span="11">
            <el-form-item prop="startTime">
              <el-date-picker
                v-model="form.startTime"
                type="datetime"
                placeholder="选择开始时间"
                style="width: 100%"
                :disabled-date="disabledDate"
                format="YYYY-MM-DD HH:mm"
                @change="handleTimeChange"
              />
            </el-form-item>
          </el-col>
          <el-col :span="2" style="text-align: center">-</el-col>
          <el-col :span="11">
            <el-form-item prop="endTime">
              <el-date-picker
                v-model="form.endTime"
                type="datetime"
                placeholder="选择结束时间"
                style="width: 100%"
                :disabled-date="disabledDate"
                format="YYYY-MM-DD HH:mm"
                @change="handleTimeChange"
              />
            </el-form-item>
          </el-col>
        </el-form-item>

        <!-- 冲突检测提示 -->
        <el-alert
          v-if="conflictCheckResult && conflictCheckResult.hasConflict"
          title="时间冲突提示"
          type="error"
          :closable="false"
          style="margin-bottom: 20px"
        >
          <div>该教室在所选时间段已被占用：</div>
          <ul style="margin: 10px 0 0 20px">
            <li v-for="(conflict, index) in conflictCheckResult.conflicts" :key="index">
              {{ formatConflict(conflict) }}
            </li>
          </ul>
        </el-alert>

        <el-alert
          v-else-if="conflictCheckResult && !conflictCheckResult.hasConflict"
          title="时间段可用"
          type="success"
          :closable="false"
          style="margin-bottom: 20px"
        />

        <el-form-item label="借用目的" prop="purpose" required>
          <el-input
            v-model="form.purpose"
            placeholder="请输入借用目的（如：举办学术讲座、组织学生活动等）"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="预计人数" prop="expectedAttendees">
          <el-input-number
            v-model="form.expectedAttendees"
            :min="1"
            :max="selectedClassroom ? selectedClassroom.capacity : 500"
            placeholder="预计人数"
            style="width: 200px"
          />
          <span v-if="selectedClassroom" style="margin-left: 10px; color: #909399">
            （教室容量：{{ selectedClassroom.capacity }}人）
          </span>
        </el-form-item>

        <el-form-item label="备注" prop="remarks">
          <el-input
            v-model="form.remarks"
            type="textarea"
            :rows="4"
            placeholder="请输入其他需要说明的信息"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            :loading="submitting"
            :disabled="!canSubmit"
            @click="handleSubmit"
          >
            <el-icon><Check /></el-icon>
            提交申请
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
          <el-button @click="handleBack">
            <el-icon><Back /></el-icon>
            取消
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter, useRoute } from 'vue-router'
import { getAvailableClassrooms, createBooking, checkConflict } from '@/api/classroom'

const router = useRouter()
const route = useRoute()

// 表单引用
const formRef = ref(null)

// 表单数据
const form = reactive({
  classroomId: null,
  startTime: null,
  endTime: null,
  purpose: '',
  expectedAttendees: null,
  remarks: ''
})

// 表单验证规则
const rules = {
  classroomId: [
    { required: true, message: '请选择教室', trigger: 'change' }
  ],
  startTime: [
    { required: true, message: '请选择开始时间', trigger: 'change' }
  ],
  endTime: [
    { required: true, message: '请选择结束时间', trigger: 'change' },
    {
      validator: (rule, value, callback) => {
        if (form.startTime && value && value <= form.startTime) {
          callback(new Error('结束时间必须晚于开始时间'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ],
  purpose: [
    { required: true, message: '请输入借用目的', trigger: 'blur' },
    { min: 5, max: 200, message: '借用目的长度在5到200个字符之间', trigger: 'blur' }
  ],
  expectedAttendees: [
    { required: true, message: '请输入预计人数', trigger: 'blur' }
  ]
}

// 数据
const availableClassrooms = ref([])
const selectedClassroom = ref(null)
const submitting = ref(false)
const conflictCheckResult = ref(null)
const checkingConflict = ref(false)

// 是否可以提交
const canSubmit = computed(() => {
  return form.classroomId &&
    form.startTime &&
    form.endTime &&
    form.purpose &&
    form.expectedAttendees &&
    (!conflictCheckResult.value || !conflictCheckResult.value.hasConflict)
})

// 获取可用教室列表
const fetchAvailableClassrooms = async () => {
  try {
    const response = await getAvailableClassrooms({
      status: 'AVAILABLE',
      page: 0,
      size: 1000
    })
    availableClassrooms.value = response.data.content || []
    
    // 如果URL中有classroomId参数，自动选中
    if (route.query.classroomId) {
      form.classroomId = parseInt(route.query.classroomId)
      handleClassroomChange(form.classroomId)
    }
  } catch (error) {
    ElMessage.error(error.message || '获取教室列表失败')
  }
}

// 教室改变
const handleClassroomChange = (classroomId) => {
  selectedClassroom.value = availableClassrooms.value.find(c => c.id === classroomId)
  
  // 重新检测冲突
  if (form.startTime && form.endTime) {
    checkTimeConflict()
  }
}

// 时间改变
const handleTimeChange = () => {
  conflictCheckResult.value = null
  if (form.classroomId && form.startTime && form.endTime && form.endTime > form.startTime) {
    checkTimeConflict()
  }
}

// 检测时间冲突
const checkTimeConflict = async () => {
  if (!form.classroomId || !form.startTime || !form.endTime) return
  
  checkingConflict.value = true
  try {
    const response = await checkConflict({
      classroomId: form.classroomId,
      startTime: formatDateTime(form.startTime),
      endTime: formatDateTime(form.endTime)
    })
    conflictCheckResult.value = response.data
  } catch (error) {
    ElMessage.error(error.message || '检测时间冲突失败')
  } finally {
    checkingConflict.value = false
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    // 再次检测冲突
    await checkTimeConflict()
    if (conflictCheckResult.value?.hasConflict) {
      ElMessage.warning('所选时间段有冲突，请重新选择')
      return
    }
    
    ElMessageBox.confirm(
      '确认提交教室借用申请？',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    ).then(async () => {
      submitting.value = true
      try {
        const data = {
          classroomId: form.classroomId,
          startTime: formatDateTime(form.startTime),
          endTime: formatDateTime(form.endTime),
          purpose: form.purpose,
          expectedAttendees: form.expectedAttendees,
          remarks: form.remarks || undefined
        }
        
        await createBooking(data)
        ElMessage.success('申请提交成功，请等待审批')
        router.push('/teacher/classroom-bookings')
      } catch (error) {
        ElMessage.error(error.message || '提交申请失败')
      } finally {
        submitting.value = false
      }
    }).catch(() => {
      // 取消操作
    })
  })
}

// 重置表单
const handleReset = () => {
  if (!formRef.value) return
  formRef.value.resetFields()
  selectedClassroom.value = null
  conflictCheckResult.value = null
}

// 返回
const handleBack = () => {
  router.back()
}

// 禁用过去的日期
const disabledDate = (date) => {
  return date < new Date(new Date().setHours(0, 0, 0, 0))
}

// 格式化日期时间
const formatDateTime = (date) => {
  if (!date) return ''
  const d = new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hour = String(d.getHours()).padStart(2, '0')
  const minute = String(d.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day}T${hour}:${minute}:00`
}

// 格式化冲突信息
const formatConflict = (conflict) => {
  const typeNames = {
    COURSE: '上课',
    EXAM: '考试',
    BOOKING: '借用',
    OTHER: '其他'
  }
  return `${typeNames[conflict.type] || conflict.type}: ${conflict.description} (${conflict.startTime} - ${conflict.endTime})`
}

// 格式化设备信息
const formatEquipment = (equipment) => {
  if (!equipment) return '无特殊设备'
  try {
    const equipmentObj = JSON.parse(equipment)
    const items = Object.entries(equipmentObj)
      .filter(([, value]) => value)
      .map(([key]) => key)
    return items.length > 0 ? items.join('、') : '无特殊设备'
  } catch {
    return equipment
  }
}

// 获取类型标签
const getTypeTag = (type) => {
  const tags = {
    LECTURE: 'info',
    LAB: 'warning',
    MULTIMEDIA: 'success',
    CONFERENCE: 'danger'
  }
  return tags[type] || 'info'
}

// 获取类型名称
const getTypeName = (type) => {
  const names = {
    LECTURE: '普通教室',
    LAB: '实验室',
    MULTIMEDIA: '多媒体教室',
    CONFERENCE: '会议室'
  }
  return names[type] || type
}

// 获取状态标签
const getStatusTag = (status) => {
  const tags = {
    AVAILABLE: 'success',
    MAINTENANCE: 'warning',
    DISABLED: 'danger'
  }
  return tags[status] || 'info'
}

// 获取状态名称
const getStatusName = (status) => {
  const names = {
    AVAILABLE: '可用',
    MAINTENANCE: '维修中',
    DISABLED: '停用'
  }
  return names[status] || status
}

onMounted(() => {
  fetchAvailableClassrooms()
})
</script>

<style scoped>
.booking-create-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 18px;
  font-weight: 600;
}

.classroom-info {
  margin-top: 10px;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

:deep(.el-select-dropdown__item) {
  height: auto;
  padding: 8px 20px;
}
</style>

