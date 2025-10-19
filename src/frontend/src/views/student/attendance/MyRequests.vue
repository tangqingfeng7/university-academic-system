<template>
  <div class="my-requests">
    <el-card>
      <template #header>
        <span>我的申请</span>
      </template>

      <el-tabs v-model="activeTab" @tab-change="loadRequests">
        <el-tab-pane label="待审批" name="PENDING" />
        <el-tab-pane label="已批准" name="APPROVED" />
        <el-tab-pane label="已拒绝" name="REJECTED" />
      </el-tabs>

      <el-table :data="requests" v-loading="loading">
        <el-table-column label="申请类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.requestType === 'MAKEUP' ? 'success' : 'warning'">
              {{ row.requestType === 'MAKEUP' ? '补签' : '申诉' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="课程">
          <template #default="{ row }">
            {{ row.attendanceDetail?.attendanceRecord?.offering?.course?.name }}
          </template>
        </el-table-column>
        <el-table-column label="考勤日期" width="120">
          <template #default="{ row }">
            {{ row.attendanceDetail?.attendanceRecord?.attendanceDate }}
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="申请原因" min-width="200" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="提交时间" width="160" />
        <el-table-column label="审批意见" min-width="150">
          <template #default="{ row }">
            {{ row.approvalComment || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button 
              v-if="row.status === 'PENDING'"
              type="danger" 
              size="small"
              @click="handleCancel(row)">
              取消
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getStudentRequests, cancelRequest } from '@/api/attendance'

const loading = ref(false)
const activeTab = ref('PENDING')
const requests = ref([])

onMounted(() => {
  loadRequests()
})

const loadRequests = async () => {
  try {
    loading.value = true
    const status = activeTab.value
    const res = await getStudentRequests(status)
    requests.value = res.data || []
  } catch (error) {
    ElMessage.error('加载申请列表失败')
  } finally {
    loading.value = false
  }
}

const handleCancel = async (row) => {
  try {
    await ElMessageBox.confirm('确定要取消此申请吗？', '提示', {
      type: 'warning'
    })
    
    await cancelRequest(row.id)
    ElMessage.success('申请已取消')
    loadRequests()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '取消失败')
    }
  }
}

const getStatusType = (status) => {
  const types = {
    PENDING: 'warning',
    APPROVED: 'success',
    REJECTED: 'danger'
  }
  return types[status] || ''
}

const getStatusText = (status) => {
  const texts = {
    PENDING: '待审批',
    APPROVED: '已批准',
    REJECTED: '已拒绝'
  }
  return texts[status] || status
}
</script>

<style scoped>
.ml-1 {
  margin-left: 4px;
}
</style>

