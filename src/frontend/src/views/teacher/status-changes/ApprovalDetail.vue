<template>
  <div class="approval-detail">
    <el-card v-loading="loading">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span style="font-weight: 600;">审批处理</span>
          <el-button @click="goBack">返回</el-button>
        </div>
      </template>

      <el-descriptions v-if="detail" :column="2" border>
        <el-descriptions-item label="申请编号">{{ detail.id }}</el-descriptions-item>
        <el-descriptions-item label="学生姓名">{{ detail.studentName }}</el-descriptions-item>
        <el-descriptions-item label="学号">{{ detail.studentNo }}</el-descriptions-item>
        <el-descriptions-item label="异动类型">{{ getTypeText(detail.type) }}</el-descriptions-item>
        <el-descriptions-item label="提交时间">{{ detail.createdAt }}</el-descriptions-item>
        <el-descriptions-item label="异动原因" :span="2">{{ detail.reason }}</el-descriptions-item>
      </el-descriptions>

      <!-- 审批操作 -->
      <div v-if="detail && detail.status === 'PENDING'" style="margin-top: 30px;">
        <h3>审批操作</h3>
        <el-form :model="approvalForm" label-width="100px" style="margin-top: 15px;">
          <el-form-item label="审批结果" required>
            <el-radio-group v-model="approvalForm.action">
              <el-radio label="APPROVE">批准</el-radio>
              <el-radio label="REJECT">拒绝</el-radio>
              <el-radio label="RETURN">退回</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="审批意见">
            <el-input
              v-model="approvalForm.comment"
              type="textarea"
              :rows="4"
              placeholder="请输入审批意见"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSubmit">提交审批</el-button>
            <el-button @click="goBack">取消</el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getApprovalDetail, approveApplication } from '@/api/statusChange'

const router = useRouter()
const route = useRoute()

const detail = ref(null)
const loading = ref(false)

const approvalForm = reactive({
  action: 'APPROVE',
  comment: ''
})

const getTypeText = (type) => {
  const map = {
    'SUSPENSION': '休学',
    'RESUMPTION': '复学',
    'TRANSFER': '转专业',
    'WITHDRAWAL': '退学'
  }
  return map[type] || type
}

const fetchDetail = async () => {
  loading.value = true
  try {
    const res = await getApprovalDetail(route.params.id)
    detail.value = res.data
  } catch (error) {
    ElMessage.error('获取详情失败')
  } finally {
    loading.value = false
  }
}

const handleSubmit = async () => {
  try {
    await ElMessageBox.confirm('确认提交审批结果？', '确认操作', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await approveApplication(route.params.id, approvalForm)
    ElMessage.success('审批提交成功')
    router.back()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '审批提交失败')
    }
  }
}

const goBack = () => {
  router.back()
}

onMounted(() => {
  fetchDetail()
})
</script>

<style scoped>
.approval-detail {
  padding: 20px;
}
</style>

