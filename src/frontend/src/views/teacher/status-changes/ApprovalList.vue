<template>
  <div class="approval-list">
    <el-card>
      <template #header>
        <span style="font-weight: 600;">学籍异动审批</span>
      </template>

      <el-alert
        v-if="pendingCount > 0"
        :title="`您有 ${pendingCount} 条待审批申请`"
        type="warning"
        :closable="false"
        style="margin-bottom: 20px;"
      />

      <el-table v-loading="loading" :data="tableData">
        <el-table-column prop="id" label="申请编号" width="100" />
        <el-table-column prop="studentName" label="学生姓名" width="120" />
        <el-table-column prop="studentNo" label="学号" width="140" />
        <el-table-column label="异动类型" width="120">
          <template #default="{ row }">
            <el-tag>{{ getTypeText(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="提交时间" width="180" />
        <el-table-column label="操作" fixed="right" width="120">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleApprove(row.id)">
              审批
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
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
import { ElMessage } from 'element-plus'
import { getPendingApprovals } from '@/api/statusChange'

const router = useRouter()

const tableData = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

const pendingCount = computed(() => total.value)

const getTypeText = (type) => {
  const map = {
    'SUSPENSION': '休学',
    'RESUMPTION': '复学',
    'TRANSFER': '转专业',
    'WITHDRAWAL': '退学'
  }
  return map[type] || type
}

const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value
    }
    const res = await getPendingApprovals(params)
    tableData.value = res.data.content || []
    total.value = res.data.totalElements || 0
  } catch (error) {
    ElMessage.error('查询失败')
  } finally {
    loading.value = false
  }
}

const handleApprove = (id) => {
  router.push(`/teacher/status-changes/${id}`)
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.approval-list {
  padding: 20px;
}
</style>

