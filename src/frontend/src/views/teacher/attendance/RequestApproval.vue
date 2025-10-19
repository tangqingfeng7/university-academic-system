<template>
  <div class="request-approval">
    <el-card>
      <template #header>
        <span>考勤申请审批</span>
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
        <el-table-column label="学生">
          <template #default="{ row }">
            {{ row.student?.name }} ({{ row.student?.studentNo }})
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
        <el-table-column prop="createdAt" label="提交时间" width="160" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === 'PENDING'">
              <el-button type="success" size="small" @click="handleApprove(row)">
                批准
              </el-button>
              <el-button type="danger" size="small" @click="handleReject(row)">
                拒绝
              </el-button>
            </template>
            <el-button type="info" size="small" @click="viewDetail(row)">
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 审批对话框 -->
    <el-dialog v-model="showApprovalDialog" :title="approvalTitle" width="500px">
      <el-form :model="approvalForm" label-width="100px">
        <el-form-item label="审批意见">
          <el-input 
            v-model="approvalForm.comment" 
            type="textarea" 
            :rows="4"
            placeholder="请输入审批意见" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showApprovalDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmApproval">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getAllRequests, approveRequest, rejectRequest } from '@/api/attendance'

const loading = ref(false)
const activeTab = ref('PENDING')
const requests = ref([])
const showApprovalDialog = ref(false)
const approvalTitle = ref('')
const approvalType = ref('') // 'approve' or 'reject'
const currentRequest = ref(null)
const approvalForm = ref({
  comment: ''
})

onMounted(() => {
  loadRequests()
})

const loadRequests = async () => {
  try {
    loading.value = true
    const status = activeTab.value === 'PENDING' ? 'PENDING' : activeTab.value
    const res = await getAllRequests(status)
    requests.value = res.data || []
  } catch (error) {
    ElMessage.error('加载申请列表失败')
  } finally {
    loading.value = false
  }
}

const handleApprove = (row) => {
  currentRequest.value = row
  approvalType.value = 'approve'
  approvalTitle.value = '批准申请'
  approvalForm.value.comment = ''
  showApprovalDialog.value = true
}

const handleReject = (row) => {
  currentRequest.value = row
  approvalType.value = 'reject'
  approvalTitle.value = '拒绝申请'
  approvalForm.value.comment = ''
  showApprovalDialog.value = true
}

const confirmApproval = async () => {
  try {
    if (approvalType.value === 'approve') {
      await approveRequest(currentRequest.value.id, approvalForm.value)
      ElMessage.success('申请已批准')
    } else {
      await rejectRequest(currentRequest.value.id, approvalForm.value)
      ElMessage.success('申请已拒绝')
    }
    
    showApprovalDialog.value = false
    loadRequests()
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  }
}

const viewDetail = (row) => {
  // TODO: 显示详情对话框
  console.log('查看详情', row)
}
</script>

<style scoped>
.mt-4 {
  margin-top: 16px;
}

.ml-2 {
  margin-left: 8px;
}
</style>

