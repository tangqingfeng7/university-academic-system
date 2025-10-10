<template>
  <div class="admin-status-change">
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span style="font-weight: 600;">学籍异动管理</span>
          <el-button type="primary" @click="handleExport">导出记录</el-button>
        </div>
      </template>

      <!-- 查询表单 -->
      <el-form :inline="true" :model="queryForm">
        <el-form-item label="学生姓名">
          <el-input v-model="queryForm.studentName" placeholder="请输入" clearable />
        </el-form-item>
        <el-form-item label="学号">
          <el-input v-model="queryForm.studentNo" placeholder="请输入" clearable />
        </el-form-item>
        <el-form-item label="异动类型">
          <el-select v-model="queryForm.type" placeholder="全部" clearable>
            <el-option label="休学" value="SUSPENSION" />
            <el-option label="复学" value="RESUMPTION" />
            <el-option label="转专业" value="TRANSFER" />
            <el-option label="退学" value="WITHDRAWAL" />
          </el-select>
        </el-form-item>
        <el-form-item label="审批状态">
          <el-select v-model="queryForm.status" placeholder="全部" clearable>
            <el-option label="待审批" value="PENDING" />
            <el-option label="已批准" value="APPROVED" />
            <el-option label="已拒绝" value="REJECTED" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 数据表格 -->
      <el-table v-loading="loading" :data="tableData" style="width: 100%">
        <el-table-column prop="id" label="申请编号" width="100" />
        <el-table-column prop="studentName" label="学生姓名" width="120" />
        <el-table-column prop="studentNo" label="学号" width="140" />
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
        <el-table-column prop="createdAt" label="提交时间" width="180" />
        <el-table-column label="操作" fixed="right" width="120">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="viewDetail(row.id)">
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        layout="total, sizes, prev, pager, next"
        style="margin-top: 20px;"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getAllApplications, exportRecords } from '@/api/statusChange'

const router = useRouter()

const queryForm = reactive({
  studentName: '',
  studentNo: '',
  type: '',
  status: ''
})

const tableData = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

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

const handleQuery = () => {
  currentPage.value = 1
  fetchData()
}

const handleReset = () => {
  Object.keys(queryForm).forEach(key => {
    queryForm[key] = ''
  })
  handleQuery()
}

const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      ...queryForm,
      page: currentPage.value - 1,
      size: pageSize.value
    }
    const res = await getAllApplications(params)
    tableData.value = res.data.content || []
    total.value = res.data.totalElements || 0
  } catch (error) {
    ElMessage.error('查询失败')
  } finally {
    loading.value = false
  }
}

const viewDetail = (id) => {
  router.push(`/admin/status-changes/${id}`)
}

const handleExport = async () => {
  try {
    const blob = await exportRecords(queryForm)
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `学籍异动记录_${new Date().getTime()}.xlsx`
    link.click()
    ElMessage.success('导出成功')
  } catch (error) {
    ElMessage.error('导出失败')
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.admin-status-change {
  padding: 20px;
}
</style>

