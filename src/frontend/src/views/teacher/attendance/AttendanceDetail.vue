<template>
  <div class="attendance-detail">
    <!-- 顶部导航 -->
    <div class="page-header">
      <div class="header-back" @click="$router.back()">
        <el-icon><ArrowLeft /></el-icon>
        <span>返回</span>
      </div>
    </div>

    <div class="detail-container" v-loading="loading">
      <!-- 课程标题卡片 -->
      <div class="course-card">
        <div class="course-header">
          <div class="course-info">
            <h1 class="course-title">{{ record.courseName }}</h1>
            <div class="course-meta">
              <span class="meta-item">{{ record.attendanceDate }}</span>
              <span class="meta-divider">·</span>
              <span class="meta-item">{{ record.attendanceTime }}</span>
              <span class="status-badge" :class="`status-${record.status}`">
                {{ getStatusText(record.status) }}
              </span>
            </div>
          </div>
          <div class="action-buttons" v-if="record.status === 'IN_PROGRESS'">
            <button 
              v-if="record.method === 'QRCODE'"
              class="btn btn-success"
              @click="handleGenerateQRCode">
              <el-icon><Picture /></el-icon>
              生成二维码
            </button>
            <button 
              v-if="record.method === 'LOCATION'"
              class="btn btn-warning"
              @click="showGeofenceDialog = true">
              <el-icon><Location /></el-icon>
              设置围栏
            </button>
            <button 
              class="btn btn-primary"
              @click="handleSubmit">
              <el-icon><Check /></el-icon>
              提交考勤
            </button>
            <button 
              class="btn btn-secondary"
              @click="handleCancel">
              <el-icon><Close /></el-icon>
              取消考勤
            </button>
          </div>
        </div>
      </div>

      <!-- 统计卡片 -->
      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-label">应到人数</div>
          <div class="stat-value">{{ record.totalStudents || 0 }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">实到人数</div>
          <div class="stat-value stat-success">{{ record.presentCount || 0 }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">缺勤人数</div>
          <div class="stat-value stat-danger">{{ record.absentCount || 0 }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">出勤率</div>
          <div class="stat-value">
            <span v-if="record.attendanceRate !== null">{{ record.attendanceRate }}%</span>
            <span v-else>-</span>
          </div>
        </div>
      </div>

      <!-- 学生列表 -->
      <div class="students-section">
        <div class="section-header">
          <h2 class="section-title">学生名单</h2>
          <span class="students-count">共 {{ details.length }} 人</span>
        </div>

        <div class="students-table">
          <div class="table-row table-header">
            <div class="col col-no">学号</div>
            <div class="col col-name">姓名</div>
            <div class="col col-class">班级</div>
            <div class="col col-status">考勤状态</div>
            <div class="col col-time">签到时间</div>
            <div class="col col-tag">标记</div>
            <div class="col col-remarks">备注</div>
          </div>
          
          <div v-for="(row, index) in details" :key="index" class="table-row table-data">
            <div class="col col-no">{{ row.studentNo }}</div>
            <div class="col col-name">
              <div class="student-avatar">{{ row.studentName?.charAt(0) || '?' }}</div>
              <span class="student-name">{{ row.studentName }}</span>
            </div>
            <div class="col col-class">{{ row.className }}</div>
            <div class="col col-status">
              <el-select 
                v-if="record.status === 'IN_PROGRESS' && record.method === 'MANUAL'"
                v-model="row.status" 
                @change="updateStatus(row)"
                class="status-select">
                <el-option label="出勤" value="PRESENT" />
                <el-option label="迟到" value="LATE" />
                <el-option label="早退" value="EARLY_LEAVE" />
                <el-option label="请假" value="LEAVE" />
                <el-option label="旷课" value="ABSENT" />
              </el-select>
              <span v-else class="status-badge-inline" :class="`status-${row.status}`">
                {{ getAttendanceStatusText(row.status) }}
              </span>
            </div>
            <div class="col col-time">
              <div v-if="row.checkinTime" class="checkin-time">
                <el-icon><Clock /></el-icon>
                {{ formatTime(row.checkinTime) }}
              </div>
              <span v-else class="text-muted">-</span>
            </div>
            <div class="col col-tag">
              <span v-if="row.isMakeup" class="makeup-tag">补签</span>
              <span v-else>-</span>
            </div>
            <div class="col col-remarks">{{ row.remarks || '-' }}</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 二维码对话框 -->
    <el-dialog v-model="showQRCodeDialog" title="扫码签到" width="500px">
      <div class="qrcode-container">
        <el-alert type="info" :closable="false" class="mb-4">
          <p>请学生在签到界面输入以下令牌进行签到</p>
        </el-alert>
        <div class="token-display">
          <el-input v-model="currentQRToken" readonly size="large">
            <template #append>
              <el-button @click="copyToken">复制</el-button>
            </template>
          </el-input>
        </div>
        <div class="qrcode-info">
          <p class="text-secondary">有效期：5分钟</p>
          <el-button type="primary" @click="handleRefreshQRCode">刷新令牌</el-button>
        </div>
      </div>
    </el-dialog>

    <!-- 地理围栏对话框 -->
    <el-dialog v-model="showGeofenceDialog" title="设置签到位置" width="500px">
      <el-form :model="geofenceForm" label-width="100px">
        <el-form-item label="纬度">
          <el-input-number v-model="geofenceForm.latitude" :precision="6" :step="0.0001" />
          <el-button @click="getCurrentLocation" class="ml-2">获取当前位置</el-button>
        </el-form-item>
        <el-form-item label="经度">
          <el-input-number v-model="geofenceForm.longitude" :precision="6" :step="0.0001" />
        </el-form-item>
        <el-form-item label="半径（米）">
          <el-input-number v-model="geofenceForm.radius" :min="10" :max="1000" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showGeofenceDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSetGeofence">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Clock, Picture, Location, Check, Close } from '@element-plus/icons-vue'
import {
  getAttendanceRecord,
  getAttendanceDetails,
  recordAttendance,
  submitAttendance,
  cancelAttendance,
  generateQRCode,
  refreshQRCode,
  setGeofence
} from '@/api/attendance'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const record = ref({})
const details = ref([])
const showQRCodeDialog = ref(false)
const showGeofenceDialog = ref(false)
const currentQRToken = ref('')

const geofenceForm = ref({
  recordId: null,
  latitude: 39.9042,
  longitude: 116.4074,
  radius: 100
})

onMounted(() => {
  loadData()
})

const loadData = async () => {
  try {
    loading.value = true
    const recordId = route.params.id
    
    const [recordRes, detailsRes] = await Promise.all([
      getAttendanceRecord(recordId),
      getAttendanceDetails(recordId)
    ])
    
    record.value = recordRes.data || {}
    details.value = detailsRes.data || []
    geofenceForm.value.recordId = recordId
  } catch (error) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const updateStatus = async (row) => {
  try {
    await recordAttendance(record.value.id, {
      studentId: row.studentId,
      status: row.status,
      remarks: row.remarks
    })
    ElMessage.success('考勤状态已更新')
  } catch (error) {
    ElMessage.error(error.message || '更新失败')
    loadData() // 重新加载数据
  }
}

const handleSubmit = async () => {
  try {
    await ElMessageBox.confirm('确定要提交考勤吗？提交后将无法修改。', '提示', {
      type: 'warning'
    })
    
    await submitAttendance(record.value.id)
    ElMessage.success('考勤已提交')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '提交失败')
    }
  }
}

const handleCancel = async () => {
  try {
    await ElMessageBox.confirm('确定要取消考勤吗？', '提示', {
      type: 'warning'
    })
    
    await cancelAttendance(record.value.id)
    ElMessage.success('考勤已取消')
    router.back()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '取消失败')
    }
  }
}

const handleGenerateQRCode = async () => {
  try {
    const res = await generateQRCode(record.value.id)
    currentQRToken.value = res.data.qrToken
    showQRCodeDialog.value = true
    ElMessage.success('二维码已生成，令牌：' + currentQRToken.value)
  } catch (error) {
    ElMessage.error(error.message || '生成二维码失败')
  }
}

const handleRefreshQRCode = async () => {
  try {
    const res = await refreshQRCode(record.value.id)
    currentQRToken.value = res.data.qrToken
    ElMessage.success('二维码已刷新，新令牌：' + currentQRToken.value)
  } catch (error) {
    ElMessage.error(error.message || '刷新失败')
  }
}

const copyToken = () => {
  navigator.clipboard.writeText(currentQRToken.value)
    .then(() => {
      ElMessage.success('令牌已复制')
    })
    .catch(() => {
      ElMessage.error('复制失败')
    })
}

const getCurrentLocation = () => {
  if (navigator.geolocation) {
    navigator.geolocation.getCurrentPosition(
      (position) => {
        geofenceForm.value.latitude = position.coords.latitude
        geofenceForm.value.longitude = position.coords.longitude
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

const handleSetGeofence = async () => {
  try {
    await setGeofence(geofenceForm.value)
    ElMessage.success('地理围栏设置成功')
    showGeofenceDialog.value = false
  } catch (error) {
    ElMessage.error(error.message || '设置失败')
  }
}

const getStatusType = (status) => {
  const types = {
    IN_PROGRESS: 'warning',
    SUBMITTED: 'success',
    CANCELLED: 'info'
  }
  return types[status] || ''
}

const getStatusText = (status) => {
  const texts = {
    IN_PROGRESS: '进行中',
    SUBMITTED: '已提交',
    CANCELLED: '已取消'
  }
  return texts[status] || status
}

const formatTime = (timeStr) => {
  if (!timeStr) return '-'
  try {
    const date = new Date(timeStr)
    return date.toLocaleString('zh-CN', {
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })
  } catch {
    return timeStr
  }
}

const getAttendanceStatusType = (status) => {
  const types = {
    PRESENT: 'success',
    LATE: 'warning',
    EARLY_LEAVE: 'warning',
    LEAVE: 'info',
    ABSENT: 'danger'
  }
  return types[status] || ''
}

const getAttendanceStatusText = (status) => {
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

<style scoped lang="scss">
.attendance-detail {
  min-height: 100vh;
  background: #f5f5f7;
  padding: 0;
}

/* ==================== 页面头部 ==================== */
.page-header {
  background: rgba(255, 255, 255, 0.72);
  backdrop-filter: saturate(180%) blur(20px);
  border-bottom: 1px solid rgba(0, 0, 0, 0.08);
  padding: 16px 40px;
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-back {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  font-size: 15px;
  color: #007aff;
  font-weight: 500;
  cursor: pointer;
  border-radius: 8px;
  transition: all 0.2s;

  &:hover {
    background: rgba(0, 122, 255, 0.08);
  }

  .el-icon {
    font-size: 18px;
  }
}

/* ==================== 主容器 ==================== */
.detail-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 32px 40px;
}

/* ==================== 课程卡片 ==================== */
.course-card {
  background: white;
  border-radius: 16px;
  padding: 32px 40px;
  margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.course-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 32px;
}

.course-title {
  font-size: 32px;
  font-weight: 700;
  color: #1d1d1f;
  letter-spacing: -0.6px;
  margin: 0 0 12px 0;
  line-height: 1.2;
}

.course-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  color: #86868b;
  font-weight: 400;
}

.meta-divider {
  color: #d2d2d7;
}

.status-badge {
  display: inline-flex;
  align-items: center;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 13px;
  font-weight: 600;
  margin-left: 4px;

  &.status-IN_PROGRESS {
    background: rgba(255, 149, 0, 0.1);
    color: #ff9500;
  }

  &.status-SUBMITTED {
    background: rgba(52, 199, 89, 0.1);
    color: #34c759;
  }

  &.status-CANCELLED {
    background: rgba(142, 142, 147, 0.1);
    color: #8e8e93;
  }
}

/* ==================== 按钮组 ==================== */
.action-buttons {
  display: flex;
  gap: 12px;
  flex-shrink: 0;
}

.btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 10px 20px;
  border-radius: 10px;
  font-size: 15px;
  font-weight: 600;
  border: none;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;

  .el-icon {
    font-size: 18px;
  }

  &:hover {
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  }

  &:active {
    transform: translateY(0);
  }

  &.btn-primary {
    background: #007aff;
    color: white;

    &:hover {
      background: #0051d5;
    }
  }

  &.btn-success {
    background: #34c759;
    color: white;

    &:hover {
      background: #28a745;
    }
  }

  &.btn-warning {
    background: #ff9500;
    color: white;

    &:hover {
      background: #e68600;
    }
  }

  &.btn-secondary {
    background: #f5f5f7;
    color: #1d1d1f;

    &:hover {
      background: #e5e5e7;
    }
  }
}

/* ==================== 统计卡片 ==================== */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  background: white;
  border: 1px solid #e5e5ea;
  border-radius: 12px;
  padding: 20px;
  transition: all 0.2s;

  &:hover {
    border-color: #d1d1d6;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  }
}

.stat-label {
  font-size: 13px;
  color: #86868b;
  font-weight: 500;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 32px;
  font-weight: 600;
  color: #1d1d1f;
  letter-spacing: -0.5px;
  line-height: 1;

  &.stat-success {
    color: #34c759;
  }

  &.stat-danger {
    color: #ff3b30;
  }
}

/* ==================== 学生列表 ==================== */
.students-section {
  background: white;
  border-radius: 16px;
  padding: 32px 40px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.section-title {
  font-size: 22px;
  font-weight: 700;
  color: #1d1d1f;
  letter-spacing: -0.4px;
  margin: 0;
}

.students-count {
  font-size: 14px;
  color: #86868b;
  font-weight: 500;
}

/* ==================== 自定义表格 ==================== */
.students-table {
  width: 100%;
}

.table-row {
  display: grid;
  grid-template-columns: 120px 180px 150px 140px 160px 80px 1fr;
  gap: 16px;
  align-items: center;
  padding: 16px 0;
  border-bottom: 1px solid #f5f5f7;
  transition: all 0.2s;

  &.table-header {
    border-bottom: 2px solid #e5e5ea;
    
    .col {
      font-size: 13px;
      font-weight: 600;
      color: #86868b;
      text-transform: uppercase;
      letter-spacing: 0.3px;
    }
  }

  &.table-data {
    &:hover {
      background: #fafafa;
      border-radius: 12px;
      padding-left: 16px;
      padding-right: 16px;
      margin: 0 -16px;
    }

    &:last-child {
      border-bottom: none;
    }
  }
}

.col {
  font-size: 15px;
  color: #1d1d1f;
}

.col-name {
  display: flex;
  align-items: center;
  gap: 12px;
}

.student-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
  flex-shrink: 0;
}

.student-name {
  font-weight: 600;
  color: #1d1d1f;
}

.status-badge-inline {
  display: inline-flex;
  align-items: center;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 13px;
  font-weight: 600;

  &.status-PRESENT {
    background: rgba(52, 199, 89, 0.1);
    color: #34c759;
  }

  &.status-LATE {
    background: rgba(255, 149, 0, 0.1);
    color: #ff9500;
  }

  &.status-EARLY_LEAVE {
    background: rgba(255, 149, 0, 0.1);
    color: #ff9500;
  }

  &.status-LEAVE {
    background: rgba(0, 122, 255, 0.1);
    color: #007aff;
  }

  &.status-ABSENT {
    background: rgba(255, 59, 48, 0.1);
    color: #ff3b30;
  }
}

.checkin-time {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #007aff;
  font-size: 14px;
  font-weight: 500;

  .el-icon {
    font-size: 16px;
  }
}

.text-muted {
  color: #d2d2d7;
}

.makeup-tag {
  display: inline-flex;
  padding: 3px 10px;
  border-radius: 10px;
  background: rgba(255, 149, 0, 0.1);
  color: #ff9500;
  font-size: 12px;
  font-weight: 600;
}

.status-select {
  width: 120px;
}

/* ==================== 响应式 ==================== */
@media (max-width: 1200px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .table-row {
    grid-template-columns: 100px 150px 120px 130px 140px 70px 1fr;
    gap: 12px;
  }
}

@media (max-width: 768px) {
  .detail-container {
    padding: 20px;
  }

  .course-card {
    padding: 24px;
  }

  .course-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .action-buttons {
    width: 100%;
    flex-wrap: wrap;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }

  .students-section {
    padding: 24px;
    overflow-x: auto;
  }
}

/* ==================== 对话框样式 ==================== */
.qrcode-container {
  text-align: center;
  padding: 20px;
}

.token-display {
  margin: 24px 0;
}

.qrcode-info {
  margin-top: 24px;

  p {
    margin: 10px 0;
    font-size: 14px;
    color: #86868b;
  }
}

.mb-4 {
  margin-bottom: 24px;
}

.ml-2 {
  margin-left: 8px;
}
</style>

