<template>
  <div class="course-change-container">
    <el-card class="header-card">
      <div class="header-content">
        <h2 class="title">
          <el-icon class="title-icon"><Calendar /></el-icon>
          调课申请
        </h2>
        <el-button type="primary" @click="showApplyDialog">
          <el-icon><Plus /></el-icon>
          申请调课
        </el-button>
      </div>
    </el-card>

    <el-card class="list-card">
      <el-table :data="requestList" v-loading="loading" style="width: 100%">
        <el-table-column prop="courseName" label="课程名称" width="200" />
        <el-table-column label="原时间安排" width="200">
          <template #default="{ row }">
            <div class="schedule-text">{{ formatSchedule(row.originalSchedule) }}</div>
          </template>
        </el-table-column>
        <el-table-column label="新时间安排" width="200">
          <template #default="{ row }">
            <div class="schedule-text">{{ formatSchedule(row.newSchedule) }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="调课原因" show-overflow-tooltip />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="审批信息" width="250">
          <template #default="{ row }">
            <div v-if="row.status !== 'PENDING'" class="approval-info">
              <div>审批人：{{ row.approverName }}</div>
              <div>意见：{{ row.approvalComment }}</div>
              <div>时间：{{ formatDateTime(row.approvalTime) }}</div>
            </div>
            <span v-else class="pending-text">待审批</span>
          </template>
        </el-table-column>
        <el-table-column label="申请时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              @click="handleViewDetail(row)">
              查看
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="调课申请详情"
      width="700px">
      <div v-if="currentRequest" class="detail-content">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="课程名称">{{ currentRequest.courseName }}</el-descriptions-item>
          <el-descriptions-item label="原时间安排">
            <div class="schedule-text">{{ formatSchedule(currentRequest.originalSchedule) }}</div>
          </el-descriptions-item>
          <el-descriptions-item label="新时间安排">
            <div class="schedule-text">{{ formatSchedule(currentRequest.newSchedule) }}</div>
          </el-descriptions-item>
          <el-descriptions-item label="调课原因">
            <div style="white-space: pre-wrap;">{{ currentRequest.reason }}</div>
          </el-descriptions-item>
          <el-descriptions-item label="申请时间">
            {{ formatDateTime(currentRequest.createdAt) }}
          </el-descriptions-item>
          <el-descriptions-item label="审批状态">
            <el-tag :type="getStatusType(currentRequest.status)">
              {{ getStatusText(currentRequest.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="审批人" v-if="currentRequest.status !== 'PENDING'">
            {{ currentRequest.approverName || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="审批意见" v-if="currentRequest.status !== 'PENDING'">
            <div style="white-space: pre-wrap;">{{ currentRequest.approvalComment || '-' }}</div>
          </el-descriptions-item>
          <el-descriptions-item label="审批时间" v-if="currentRequest.status !== 'PENDING'">
            {{ currentRequest.approvalTime ? formatDateTime(currentRequest.approvalTime) : '-' }}
          </el-descriptions-item>
        </el-descriptions>
      </div>

      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 申请调课对话框 -->
    <el-dialog
      v-model="applyDialogVisible"
      title="申请调课"
      width="700px"
      :close-on-click-modal="false">
      <el-form
        ref="applyFormRef"
        :model="applyForm"
        :rules="applyRules"
        label-width="100px">
        <el-form-item label="选择课程" prop="offeringId">
          <el-select v-model="applyForm.offeringId" placeholder="请选择课程" style="width: 100%">
            <el-option
              v-for="offering in myOfferings"
              :key="offering.id"
              :label="`${offering.courseName} - ${offering.semesterName}`"
              :value="offering.id"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="新时间安排" prop="newSchedule">
          <div class="schedule-builder">
            <div v-for="(schedule, index) in scheduleItems" :key="index" class="schedule-item">
              <el-select v-model="schedule.day" placeholder="星期" style="width: 120px">
                <el-option label="周一" :value="1" />
                <el-option label="周二" :value="2" />
                <el-option label="周三" :value="3" />
                <el-option label="周四" :value="4" />
                <el-option label="周五" :value="5" />
                <el-option label="周六" :value="6" />
                <el-option label="周日" :value="7" />
              </el-select>
              <el-select v-model="schedule.period" placeholder="节次" style="width: 150px">
                <el-option label="第1-2节" value="1-2" />
                <el-option label="第3-4节" value="3-4" />
                <el-option label="第5-6节" value="5-6" />
                <el-option label="第7-8节" value="7-8" />
                <el-option label="第9-10节" value="9-10" />
                <el-option label="第11-12节" value="11-12" />
              </el-select>
              <el-input
                v-model="schedule.location"
                placeholder="上课地点"
                style="width: 200px"
              />
              <el-button 
                type="danger" 
                :icon="Delete" 
                circle 
                size="small"
                @click="removeSchedule(index)"
                v-if="scheduleItems.length > 1"
              />
            </div>
            <el-button type="primary" :icon="Plus" @click="addSchedule">添加时间</el-button>
          </div>
        </el-form-item>

        <el-form-item label="调课原因" prop="reason">
          <el-input
            v-model="applyForm.reason"
            type="textarea"
            :rows="4"
            placeholder="请输入调课原因"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="applyDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitApply" :loading="submitting">提交申请</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Calendar, Plus, Delete } from '@element-plus/icons-vue'
import { getTeacherCourseChanges, createCourseChange } from '@/api/courseChange'
import { getTeacherSchedule } from '@/api/schedule'
import { getActiveSemester } from '@/api/semester'

const loading = ref(false)
const requestList = ref([])
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const applyDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const applyFormRef = ref(null)
const submitting = ref(false)
const myOfferings = ref([])
const currentRequest = ref(null)
const scheduleItems = ref([{
  day: null,
  period: null,
  location: '',
  weeks: []
}])

const applyForm = reactive({
  offeringId: null,
  newSchedule: '',
  reason: ''
})

const applyRules = {
  offeringId: [{ required: true, message: '请选择课程', trigger: 'change' }],
  reason: [{ required: true, message: '请输入调课原因', trigger: 'blur' }]
}

// 加载调课申请列表
const loadRequestList = async () => {
  loading.value = true
  try {
    const res = await getTeacherCourseChanges({
      page: pagination.page - 1,
      size: pagination.size
    })
    requestList.value = res.data.content
    pagination.total = res.data.totalElements
  } catch (error) {
    console.error('加载调课申请列表失败:', error)
    ElMessage.error('加载调课申请列表失败')
  } finally {
    loading.value = false
  }
}

// 加载我的课程
const loadMyOfferings = async () => {
  try {
    const semesterRes = await getActiveSemester()
    if (!semesterRes.data) return

    const scheduleRes = await getTeacherSchedule(semesterRes.data.id)
    const offerings = new Map()
    
    scheduleRes.data.items.forEach(item => {
      if (!offerings.has(item.offeringId)) {
        offerings.set(item.offeringId, {
          id: item.offeringId,
          courseName: item.courseName,
          semesterName: scheduleRes.data.semesterName
        })
      }
    })
    
    myOfferings.value = Array.from(offerings.values())
  } catch (error) {
    console.error('加载课程列表失败:', error)
  }
}

// 显示申请对话框
const showApplyDialog = () => {
  loadMyOfferings()
  applyDialogVisible.value = true
}

// 查看详情
const handleViewDetail = (row) => {
  currentRequest.value = row
  detailDialogVisible.value = true
}

// 添加时间安排
const addSchedule = () => {
  scheduleItems.value.push({
    day: null,
    period: null,
    location: '',
    weeks: Array.from({length: 16}, (_, i) => i + 1)
  })
}

// 删除时间安排
const removeSchedule = (index) => {
  scheduleItems.value.splice(index, 1)
}

// 提交申请
const handleSubmitApply = async () => {
  if (!applyFormRef.value) return
  
  await applyFormRef.value.validate(async (valid) => {
    if (!valid) return

    // 构建新时间安排JSON
    const newSchedule = scheduleItems.value
      .filter(s => s.day && s.period)
      .map(s => ({
        day: s.day,
        period: s.period,
        location: s.location || '',
        weeks: (s.weeks && s.weeks.length > 0) ? s.weeks : Array.from({length: 16}, (_, i) => i + 1)
      }))

    if (newSchedule.length === 0) {
      ElMessage.warning('请至少添加一个时间安排')
      return
    }

    submitting.value = true
    try {
      await createCourseChange({
        offeringId: applyForm.offeringId,
        newSchedule: JSON.stringify(newSchedule),
        reason: applyForm.reason
      })
      
      ElMessage.success('调课申请提交成功')
      applyDialogVisible.value = false
      resetApplyForm()
      loadRequestList()
    } catch (error) {
      console.error('提交调课申请失败:', error)
      ElMessage.error(error.message || '提交调课申请失败')
    } finally {
      submitting.value = false
    }
  })
}

// 重置申请表单
const resetApplyForm = () => {
  applyForm.offeringId = null
  applyForm.reason = ''
  scheduleItems.value = [{
    day: null,
    period: null,
    location: '',
    weeks: Array.from({length: 16}, (_, i) => i + 1)
  }]
  applyFormRef.value?.resetFields()
}

// 格式化时间安排
const formatSchedule = (scheduleJson) => {
  if (!scheduleJson) return ''
  try {
    const schedule = JSON.parse(scheduleJson)
    const dayNames = ['', '周一', '周二', '周三', '周四', '周五', '周六', '周日']
    return schedule.map(s => {
      const day = dayNames[s.day] || ''
      const location = s.location || ''
      return `${day} 第${s.period}节 ${location}`
    }).join('; ')
  } catch (e) {
    return scheduleJson
  }
}

// 格式化日期时间
const formatDateTime = (datetime) => {
  if (!datetime) return ''
  return new Date(datetime).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 获取状态类型
const getStatusType = (status) => {
  const types = {
    'PENDING': 'warning',
    'APPROVED': 'success',
    'REJECTED': 'danger'
  }
  return types[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const texts = {
    'PENDING': '待审批',
    'APPROVED': '已通过',
    'REJECTED': '已拒绝'
  }
  return texts[status] || status
}

const handleSizeChange = () => {
  pagination.page = 1
  loadRequestList()
}

const handleCurrentChange = () => {
  loadRequestList()
}

onMounted(() => {
  loadRequestList()
})
</script>

<style scoped lang="scss">
.course-change-container {
  padding: 20px;
}

.header-card {
  margin-bottom: 20px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title {
  display: flex;
  align-items: center;
  font-size: 20px;
  font-weight: bold;
  color: #303133;
  margin: 0;
}

.title-icon {
  margin-right: 10px;
  font-size: 24px;
  color: #409EFF;
}

.list-card {
  margin-top: 20px;
}

.schedule-text {
  font-size: 13px;
  line-height: 1.6;
}

.approval-info {
  font-size: 13px;
  line-height: 1.6;
  color: #606266;
}

.pending-text {
  color: #E6A23C;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.schedule-builder {
  width: 100%;
}

.schedule-item {
  display: flex;
  gap: 10px;
  margin-bottom: 10px;
  align-items: center;
}

.detail-content {
  padding: 16px 0;
  
  :deep(.el-descriptions) {
    .el-descriptions__label {
      width: 120px;
      font-weight: 600;
    }
    
    .el-descriptions__cell {
      padding: 16px 12px;
    }
  }
}
</style>

