<template>
  <div class="attendance-records">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>我的考勤记录</span>
          <el-button type="primary" @click="showCheckinDialog = true">
            <el-icon><LocationFilled /></el-icon>
            签到
          </el-button>
        </div>
      </template>

      <!-- 课程筛选 -->
      <el-form :inline="true" class="mb-4">
        <el-form-item label="课程">
          <el-select 
            v-model="queryParams.offeringId" 
            placeholder="请选择课程" 
            clearable 
            @change="loadRecords"
            style="width: 300px">
            <el-option 
              v-for="offering in offerings"
              :key="offering.id"
              :label="offering.course?.name"
              :value="offering.id" />
          </el-select>
        </el-form-item>
      </el-form>

      <!-- 无课程提示 -->
      <el-empty 
        v-if="offerings.length === 0" 
        description="您还没有选课，请先去选课中心选择课程"
        :image-size="200" />

      <!-- 无考勤记录提示 -->
      <template v-else-if="!queryParams.offeringId">
        <el-empty description="请选择课程查看考勤记录" :image-size="200" />
      </template>

      <!-- 考勤统计卡片 -->
      <template v-else>
        <el-row :gutter="16" class="mb-4" v-if="statistics">
          <el-col :span="6">
            <el-statistic title="总课次" :value="statistics.totalClasses" />
          </el-col>
          <el-col :span="6">
            <el-statistic title="出勤次数" :value="statistics.presentCount" />
          </el-col>
          <el-col :span="6">
            <el-statistic title="旷课次数" :value="statistics.absentCount" />
          </el-col>
          <el-col :span="6">
            <el-statistic title="出勤率" :value="statistics.attendanceRate" suffix="%" />
          </el-col>
        </el-row>

        <!-- 考勤记录表格 -->
        <el-table :data="records" v-loading="loading">
          <el-table-column label="课程" min-width="200">
            <template #default="{ row }">
              {{ row.attendanceRecord?.courseName || '-' }}
            </template>
          </el-table-column>
          <el-table-column label="考勤日期" width="120">
            <template #default="{ row }">
              {{ row.attendanceRecord?.attendanceDate || '-' }}
            </template>
          </el-table-column>
          <el-table-column label="考勤时间" width="100">
            <template #default="{ row }">
              {{ row.attendanceRecord?.attendanceTime || '-' }}
            </template>
          </el-table-column>
          <el-table-column label="考勤状态" width="120">
            <template #default="{ row }">
              <el-tag :type="getStatusTagType(row.status)">
                {{ getStatusText(row.status) }}
              </el-tag>
              <el-tag v-if="row.isMakeup" type="warning" size="small" class="ml-1">
                补签
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="checkinTime" label="签到时间" width="160" />
          <el-table-column prop="remarks" label="备注" min-width="150" />
          <el-table-column label="操作" width="180" fixed="right">
            <template #default="{ row }">
              <el-button 
                v-if="row.status === 'ABSENT' && !hasRequest(row.id)"
                type="primary" 
                size="small"
                @click="showAppealDialog(row, 'MAKEUP')">
                补签申请
              </el-button>
              <el-button 
                v-if="row.status !== 'PRESENT' && !hasRequest(row.id)"
                type="warning" 
                size="small"
                @click="showAppealDialog(row, 'APPEAL')">
                申诉
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </template>
    </el-card>

    <!-- 签到对话框 -->
    <el-dialog v-model="showCheckinDialog" title="签到" width="500px">
      <el-tabs v-model="checkinType">
        <el-tab-pane label="扫码签到" name="qrcode">
          <el-form label-width="100px">
            <el-form-item label="二维码令牌">
              <el-input v-model="qrcodeForm.qrToken" placeholder="请输入或扫描二维码" />
            </el-form-item>
          </el-form>
          <div class="text-center">
            <el-button type="primary" @click="handleQRCodeCheckin">确认签到</el-button>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="定位签到" name="location">
          <el-form label-width="100px">
            <el-form-item label="考勤记录">
              <el-input v-model="locationForm.recordId" placeholder="请输入考勤记录ID" />
            </el-form-item>
            <el-form-item label="当前位置">
              <el-button @click="getLocation">获取位置</el-button>
              <div v-if="locationForm.latitude" class="mt-2 text-secondary">
                纬度: {{ locationForm.latitude }}, 经度: {{ locationForm.longitude }}
              </div>
            </el-form-item>
          </el-form>
          <div class="text-center">
            <el-button type="primary" @click="handleLocationCheckin">确认签到</el-button>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-dialog>

    <!-- 申请对话框 -->
    <el-dialog v-model="showAppealDialogVisible" :title="appealDialogTitle" width="500px">
      <el-form :model="appealForm" label-width="100px">
        <el-form-item label="申请原因" required>
          <el-input 
            v-model="appealForm.reason" 
            type="textarea" 
            :rows="4"
            placeholder="请详细说明申请原因" />
        </el-form-item>
        <el-form-item label="附件">
          <el-input v-model="appealForm.attachmentUrl" placeholder="附件URL（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAppealDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAppealRequest">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { LocationFilled } from '@element-plus/icons-vue'
import {
  getStudentAttendanceRecords,
  getStudentStatistics,
  qrCodeCheckin,
  locationCheckin,
  submitMakeupRequest,
  submitAppeal,
  getStudentRequests
} from '@/api/attendance'
import { getSelectedCourses } from '@/api/selection'

const loading = ref(false)
const records = ref([])
const statistics = ref(null)
const offerings = ref([])
const myRequests = ref([])
const queryParams = ref({
  offeringId: null
})

const showCheckinDialog = ref(false)
const checkinType = ref('qrcode')
const qrcodeForm = ref({
  qrToken: ''
})
const locationForm = ref({
  recordId: null,
  latitude: null,
  longitude: null
})

const showAppealDialogVisible = ref(false)
const appealDialogTitle = ref('')
const appealType = ref('')
const currentDetail = ref(null)
const appealForm = ref({
  detailId: null,
  reason: '',
  attachmentUrl: ''
})

onMounted(() => {
  loadOfferings()
  loadMyRequests()
})

const loadOfferings = async () => {
  try {
    const res = await getSelectedCourses()
    offerings.value = res.data || []
    
    // 如果有课程，自动选择第一个并加载考勤记录
    if (offerings.value.length > 0) {
      queryParams.value.offeringId = offerings.value[0].id
      loadRecords()
    }
  } catch (error) {
    console.error('加载课程失败', error)
  }
}

const loadRecords = async () => {
  if (!queryParams.value.offeringId) {
    records.value = []
    statistics.value = null
    return
  }

  try {
    loading.value = true
    const [recordsRes, statsRes] = await Promise.all([
      getStudentAttendanceRecords(queryParams.value.offeringId),
      getStudentStatistics(queryParams.value.offeringId)
    ])
    
    records.value = recordsRes.data || []
    statistics.value = statsRes.data || null
  } catch (error) {
    ElMessage.error('加载考勤记录失败')
  } finally {
    loading.value = false
  }
}

const loadMyRequests = async () => {
  try {
    const res = await getStudentRequests()
    myRequests.value = res.data || []
  } catch (error) {
    console.error('加载申请列表失败', error)
  }
}

const hasRequest = (detailId) => {
  return myRequests.value.some(req => 
    req.attendanceDetail?.id === detailId && 
    (req.status === 'PENDING' || req.status === 'APPROVED')
  )
}

const handleQRCodeCheckin = async () => {
  try {
    await qrCodeCheckin({ qrToken: qrcodeForm.value.qrToken })
    ElMessage.success('签到成功')
    showCheckinDialog.value = false
    qrcodeForm.value.qrToken = ''
    loadRecords()
  } catch (error) {
    ElMessage.error(error.message || '签到失败')
  }
}

const getLocation = () => {
  if (navigator.geolocation) {
    navigator.geolocation.getCurrentPosition(
      (position) => {
        locationForm.value.latitude = position.coords.latitude
        locationForm.value.longitude = position.coords.longitude
        ElMessage.success('位置获取成功')
      },
      () => {
        ElMessage.error('位置获取失败')
      }
    )
  } else {
    ElMessage.error('浏览器不支持定位功能')
  }
}

const handleLocationCheckin = async () => {
  try {
    await locationCheckin(locationForm.value)
    ElMessage.success('签到成功')
    showCheckinDialog.value = false
    loadRecords()
  } catch (error) {
    ElMessage.error(error.message || '签到失败')
  }
}

const showAppealDialog = (row, type) => {
  currentDetail.value = row
  appealType.value = type
  appealDialogTitle.value = type === 'MAKEUP' ? '补签申请' : '考勤申诉'
  appealForm.value = {
    detailId: row.id,
    reason: '',
    attachmentUrl: ''
  }
  showAppealDialogVisible.value = true
}

const submitAppealRequest = async () => {
  if (!appealForm.value.reason.trim()) {
    ElMessage.warning('请填写申请原因')
    return
  }

  try {
    if (appealType.value === 'MAKEUP') {
      await submitMakeupRequest(appealForm.value)
    } else {
      await submitAppeal(appealForm.value)
    }
    
    ElMessage.success('申请已提交')
    showAppealDialogVisible.value = false
    loadMyRequests()
  } catch (error) {
    ElMessage.error(error.message || '提交失败')
  }
}

const getStatusTagType = (status) => {
  const types = {
    PRESENT: 'success',
    LATE: 'warning',
    EARLY_LEAVE: 'warning',
    LEAVE: 'info',
    ABSENT: 'danger'
  }
  return types[status] || ''
}

const getStatusText = (status) => {
  const texts = {
    PRESENT: '出勤',
    LATE: '迟到',
    EARLY_LEAVE: '早退',
    LEAVE: '请假',
    ABSENT: '旷课'
  }
  return texts[status] || status
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

.ml-1 {
  margin-left: 4px;
}

.mt-2 {
  margin-top: 8px;
}

.text-secondary {
  color: #909399;
  font-size: 14px;
}

.text-center {
  text-align: center;
}
</style>

