<template>
  <div class="attendance-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>考勤管理</span>
          <el-button type="primary" @click="showStartDialog = true">
            <el-icon><Plus /></el-icon>
            开始考勤
          </el-button>
        </div>
      </template>

      <el-table :data="attendanceList" v-loading="loading">
        <el-table-column prop="attendanceDate" label="考勤日期" width="120" />
        <el-table-column prop="attendanceTime" label="考勤时间" width="100" />
        <el-table-column prop="courseName" label="课程" min-width="150" />
        <el-table-column label="考勤方式" width="120">
          <template #default="{ row }">
            <el-tag :type="getMethodType(row.method)">
              {{ getMethodText(row.method) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="totalStudents" label="应到" width="80" />
        <el-table-column prop="presentCount" label="实到" width="80" />
        <el-table-column label="出勤率" width="100">
          <template #default="{ row }">
            <span v-if="row.attendanceRate !== null">
              {{ row.attendanceRate }}%
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button 
              v-if="row.status === 'IN_PROGRESS'" 
              type="primary" 
              size="small"
              @click="continueAttendance(row)">
              继续考勤
            </el-button>
            <el-button 
              type="info" 
              size="small"
              @click="viewDetails(row)">
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 开始考勤对话框 -->
    <el-dialog v-model="showStartDialog" title="开始考勤" width="500px">
      <el-form :model="startForm" label-width="100px">
        <el-form-item label="课程">
          <el-select v-model="startForm.offeringId" placeholder="请选择课程">
            <el-option 
              v-for="offering in offerings" 
              :key="offering.id"
              :label="offering.course?.name"
              :value="offering.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="考勤方式">
          <el-radio-group v-model="startForm.method">
            <el-radio label="MANUAL">手动点名</el-radio>
            <el-radio label="QRCODE">扫码签到</el-radio>
            <el-radio label="LOCATION">定位签到</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showStartDialog = false">取消</el-button>
        <el-button type="primary" @click="handleStartAttendance">开始</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { startAttendance, getTeacherAttendanceList } from '@/api/attendance'
import { getMyCourses } from '@/api/teacherCourse'

const router = useRouter()
const loading = ref(false)
const attendanceList = ref([])
const offerings = ref([])
const showStartDialog = ref(false)
const startForm = ref({
  offeringId: null,
  method: 'MANUAL'
})

onMounted(() => {
  loadAttendanceList()
  loadOfferings()
})

const loadAttendanceList = async () => {
  try {
    loading.value = true
    const res = await getTeacherAttendanceList()
    attendanceList.value = res.data || []
  } catch (error) {
    ElMessage.error('加载考勤列表失败')
  } finally {
    loading.value = false
  }
}

const loadOfferings = async () => {
  try {
    const res = await getMyCourses()
    // 确保只取需要的字段
    offerings.value = (res.data || []).map(item => ({
      id: item.id,
      course: {
        name: item.course?.name
      }
    }))
  } catch (error) {
    console.error('加载课程列表失败', error)
  }
}

const handleStartAttendance = async () => {
  try {
    const res = await startAttendance(startForm.value)
    ElMessage.success('考勤已开始')
    showStartDialog.value = false
    
    // 跳转到考勤详情页面
    router.push({
      name: 'TeacherAttendanceDetail',
      params: { id: res.data.id }
    })
  } catch (error) {
    ElMessage.error(error.message || '开始考勤失败')
  }
}

const continueAttendance = (row) => {
  router.push({
    name: 'TeacherAttendanceDetail',
    params: { id: row.id }
  })
}

const viewDetails = (row) => {
  router.push({
    name: 'TeacherAttendanceDetail',
    params: { id: row.id }
  })
}

const getMethodType = (method) => {
  const types = {
    MANUAL: 'info',
    QRCODE: 'success',
    LOCATION: 'warning'
  }
  return types[method] || ''
}

const getMethodText = (method) => {
  const texts = {
    MANUAL: '手动点名',
    QRCODE: '扫码签到',
    LOCATION: '定位签到'
  }
  return texts[method] || method
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
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>

