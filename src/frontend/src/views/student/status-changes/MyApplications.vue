<template>
  <div class="my-applications">
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span style="font-weight: 600;">我的学籍异动申请</span>
          <el-button type="primary" @click="goToCreate">提交新申请</el-button>
        </div>
      </template>

      <!-- 统计卡片 -->
      <el-row :gutter="20" style="margin-bottom: 20px;">
        <el-col :span="6">
          <el-statistic title="全部申请" :value="tableData.length" />
        </el-col>
        <el-col :span="6">
          <el-statistic title="待审批" :value="pendingCount" />
        </el-col>
        <el-col :span="6">
          <el-statistic title="已批准" :value="approvedCount" />
        </el-col>
        <el-col :span="6">
          <el-statistic title="已拒绝" :value="rejectedCount" />
        </el-col>
      </el-row>

      <el-divider />

      <!-- 数据表格 -->
      <el-table v-loading="loading" :data="tableData">
        <el-table-column prop="id" label="申请编号" width="100" />
        <el-table-column label="异动类型" width="120">
          <template #default="{ row }">
            <el-tag>{{ getTypeText(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="审批状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="当前审批级别" width="140">
          <template #default="{ row }">
            {{ getLevelText(row.approvalLevel) }}
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="提交时间" width="180" />
        <el-table-column label="操作" fixed="right" width="200">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="viewDetail(row.id)">
              查看详情
            </el-button>
            <el-button
              v-if="row.status === 'PENDING'"
              type="danger"
              link
              size="small"
              @click="handleCancel(row.id)"
            >
              撤销
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="tableData.length === 0" description="暂无申请记录">
        <el-button type="primary" @click="goToCreate">提交新申请</el-button>
      </el-empty>

      <el-pagination
        v-if="tableData.length > 0"
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next"
        style="margin-top: 20px;"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMyApplications, cancelApplication } from '@/api/statusChange'

const router = useRouter()

const tableData = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

const pendingCount = computed(() => 
  tableData.value.filter(item => item.status === 'PENDING').length
)

const approvedCount = computed(() => 
  tableData.value.filter(item => item.status === 'APPROVED').length
)

const rejectedCount = computed(() => 
  tableData.value.filter(item => item.status === 'REJECTED').length
)

const getTypeText = (type) => {
  const map = {
    'SUSPENSION': '休学',
    'RESUMPTION': '复学',
    'TRANSFER': '转专业',
    'WITHDRAWAL': '退学'
  }
  return map[type] || type
}

const getStatusText = (status) => {
  const map = {
    'PENDING': '待审批',
    'APPROVED': '已批准',
    'REJECTED': '已拒绝',
    'CANCELLED': '已取消'
  }
  return map[status] || status
}

const getStatusType = (status) => {
  const map = {
    'PENDING': 'warning',
    'APPROVED': 'success',
    'REJECTED': 'danger',
    'CANCELLED': 'info'
  }
  return map[status] || ''
}

const getLevelText = (level) => {
  const map = {
    1: '辅导员审批',
    2: '院系审批',
    3: '教务处审批'
  }
  return map[level] || `级别${level}`
}

const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value
    }
    const res = await getMyApplications(params)
    tableData.value = res.data.content || []
    total.value = res.data.totalElements || 0
  } catch (error) {
    ElMessage.error('查询失败')
  } finally {
    loading.value = false
  }
}

const viewDetail = (id) => {
  router.push(`/student/status-changes/${id}`)
}

const goToCreate = () => {
  router.push('/student/status-changes/create')
}

const handleCancel = async (id) => {
  try {
    await ElMessageBox.confirm('确认撤销此申请？撤销后将无法恢复。', '确认撤销', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await cancelApplication(id)
    ElMessage.success('申请已撤销')
    fetchData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '撤销失败')
    }
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.my-applications {
  padding: 20px;
}
</style>

