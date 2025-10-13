<template>
  <div class="my-reports">
    <el-card shadow="never" class="header-card">
      <h2>我的上报记录</h2>
      <p class="subtitle">查看我上报的处分记录及审批状态</p>
    </el-card>

    <!-- 筛选栏 -->
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="queryForm" @submit.prevent="handleQuery">
        <el-form-item label="学号/姓名">
          <el-input v-model="queryForm.keyword" placeholder="请输入学号或姓名" clearable style="width: 200px" />
        </el-form-item>
        
        <el-form-item label="审批状态">
          <el-select v-model="queryForm.approvalStatus" placeholder="全部" clearable style="width: 120px">
            <el-option label="待审批" value="PENDING" />
            <el-option label="已批准" value="APPROVED" />
            <el-option label="已拒绝" value="REJECTED" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 处分列表 -->
    <el-card shadow="never">
      <el-table v-loading="loading" :data="tableData" border stripe>
        <el-table-column prop="studentNo" label="学号" width="120" />
        <el-table-column prop="studentName" label="姓名" width="100" />
        <el-table-column prop="majorName" label="专业" min-width="150" show-overflow-tooltip />
        <el-table-column prop="disciplineTypeDescription" label="处分类型" width="120" />
        <el-table-column prop="reason" label="处分原因" min-width="200" show-overflow-tooltip />
        <el-table-column prop="punishmentDate" label="处分日期" width="120" />
        
        <el-table-column prop="approvalStatus" label="审批状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="getApprovalStatusType(row.approvalStatus)">
              {{ row.approvalStatusDescription }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <div class="action-item" @click="handleView(row)">
                <el-icon class="action-icon"><View /></el-icon>
                <span class="action-text">查看</span>
              </div>
              <div 
                v-if="row.approvalStatus === 'PENDING'" 
                class="action-item action-edit"
                @click="handleEdit(row)"
              >
                <el-icon class="action-icon"><Edit /></el-icon>
                <span class="action-text">编辑</span>
              </div>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 查看详情对话框 -->
    <el-dialog
      v-model="viewDialogVisible"
      title="处分详情"
      width="700px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="学号">{{ viewData.studentNo }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ viewData.studentName }}</el-descriptions-item>
        <el-descriptions-item label="专业" :span="2">{{ viewData.majorName }}</el-descriptions-item>
        <el-descriptions-item label="班级" :span="2">{{ viewData.className }}</el-descriptions-item>
        
        <el-descriptions-item label="处分类型" :span="2">
          <el-tag :type="getDisciplineTypeColor(viewData.disciplineType)">
            {{ viewData.disciplineTypeDescription }}
          </el-tag>
        </el-descriptions-item>
        
        <el-descriptions-item label="处分原因" :span="2">{{ viewData.reason }}</el-descriptions-item>
        <el-descriptions-item label="详细说明" :span="2">{{ viewData.description || '-' }}</el-descriptions-item>
        
        <el-descriptions-item label="发生日期">{{ viewData.occurrenceDate }}</el-descriptions-item>
        <el-descriptions-item label="处分日期">{{ viewData.punishmentDate }}</el-descriptions-item>
        
        <el-descriptions-item label="处分状态" :span="2">
          <el-tag :type="getStatusType(viewData.status)">
            {{ viewData.statusDescription }}
          </el-tag>
        </el-descriptions-item>

        <el-descriptions-item label="审批状态" :span="2">
          <el-tag :type="getApprovalStatusType(viewData.approvalStatus)">
            {{ viewData.approvalStatusDescription }}
          </el-tag>
        </el-descriptions-item>
        
        <el-descriptions-item label="可否解除" :span="2">
          <el-tag :type="viewData.canRemove ? 'success' : 'info'">
            {{ viewData.canRemove ? '是' : '否' }}
          </el-tag>
        </el-descriptions-item>
        
        <el-descriptions-item v-if="viewData.status === 'REMOVED'" label="解除日期">
          {{ viewData.removedDate }}
        </el-descriptions-item>
        <el-descriptions-item v-if="viewData.status === 'REMOVED'" label="解除原因">
          {{ viewData.removedReason }}
        </el-descriptions-item>
        
        <el-descriptions-item label="上报人">{{ viewData.reporterName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="审批人">{{ viewData.approverName || '-' }}</el-descriptions-item>
        
        <el-descriptions-item v-if="viewData.approvalComment" label="审批意见" :span="2">
          {{ viewData.approvalComment }}
        </el-descriptions-item>
        
        <el-descriptions-item v-if="viewData.attachmentUrl" label="附件" :span="2">
          <el-link :href="viewData.attachmentUrl" type="primary" target="_blank">查看附件</el-link>
        </el-descriptions-item>
        
        <el-descriptions-item label="创建时间">{{ viewData.createdAt }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ viewData.updatedAt }}</el-descriptions-item>
      </el-descriptions>
      
      <template #footer>
        <el-button @click="viewDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { View, Edit } from '@element-plus/icons-vue'
import { getDisciplines } from '@/api/discipline'
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'

const userStore = useUserStore()
const router = useRouter()

// 数据
const loading = ref(false)
const tableData = ref([])

// 查看详情
const viewDialogVisible = ref(false)
const viewData = ref({})

// 查询表单
const queryForm = reactive({
  keyword: '',
  approvalStatus: ''
})

// 分页
const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

// 获取处分列表
const fetchDisciplines = async () => {
  loading.value = true
  try {
    // 过滤掉空值参数
    const params = {
      page: pagination.page - 1,
      size: pagination.size,
      reporterId: userStore.userInfo?.id // 只查询当前用户上报的记录
    }
    
    // 只添加非空的查询参数
    if (queryForm.keyword) params.keyword = queryForm.keyword
    if (queryForm.approvalStatus) params.approvalStatus = queryForm.approvalStatus
    
    const res = await getDisciplines(params)
    tableData.value = res.data.content || []
    pagination.total = res.data.totalElements || 0
  } catch (error) {
    console.error('获取处分列表失败:', error)
    ElMessage.error('获取处分列表失败')
  } finally {
    loading.value = false
  }
}

// 查询
const handleQuery = () => {
  pagination.page = 1
  fetchDisciplines()
}

// 重置
const handleReset = () => {
  queryForm.keyword = ''
  queryForm.approvalStatus = ''
  pagination.page = 1
  fetchDisciplines()
}

// 查看
const handleView = (row) => {
  viewData.value = { ...row }
  viewDialogVisible.value = true
}

// 编辑
const handleEdit = (row) => {
  // TODO: 跳转到编辑页面或打开编辑对话框
  ElMessage.info('编辑功能开发中')
}

// 状态标签类型
const getStatusType = (status) => {
  const typeMap = {
    ACTIVE: 'danger',
    REMOVED: 'success',
    APPEALING: 'warning'
  }
  return typeMap[status] || 'info'
}

// 处分类型标签颜色
const getDisciplineTypeColor = (type) => {
  const colorMap = {
    WARNING: 'warning',
    SERIOUS_WARNING: 'warning',
    DEMERIT: 'danger',
    SERIOUS_DEMERIT: 'danger',
    PROBATION: 'danger',
    EXPULSION: 'danger'
  }
  return colorMap[type] || 'info'
}

// 审批状态标签类型
const getApprovalStatusType = (status) => {
  const typeMap = {
    PENDING: 'warning',
    APPROVED: 'success',
    REJECTED: 'danger'
  }
  return typeMap[status] || 'info'
}

// 分页变化
const handlePageChange = (page) => {
  pagination.page = page
  fetchDisciplines()
}

const handleSizeChange = (size) => {
  pagination.size = size
  pagination.page = 1
  fetchDisciplines()
}

// 页面加载
onMounted(() => {
  fetchDisciplines()
})
</script>

<style scoped>
.my-reports {
  padding: 20px;
}

.header-card {
  margin-bottom: 20px;
}

.header-card h2 {
  margin: 0 0 8px 0;
  font-size: 24px;
  color: #303133;
}

.subtitle {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.search-card {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

/* 操作按钮 - 苹果风格 */
.action-buttons {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}

.action-item {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  background: transparent;
  color: #409eff;
  font-size: 13px;
  user-select: none;
}

.action-item:hover {
  background: rgba(64, 158, 255, 0.06);
  transform: translateY(-1px);
}

.action-item:active {
  transform: translateY(0);
}

.action-icon {
  font-size: 15px;
  transition: transform 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

.action-item:hover .action-icon {
  transform: scale(1.1);
}

.action-text {
  font-weight: 500;
  letter-spacing: 0.2px;
}

.action-edit {
  color: #67c23a;
}

.action-edit:hover {
  background: rgba(103, 194, 58, 0.06);
}

/* 表格优化 */
:deep(.el-table) {
  font-size: 13px;
}

:deep(.el-table th) {
  background-color: #fafafa;
  font-weight: 600;
  color: #303133;
}

:deep(.el-table td) {
  padding: 12px 0;
}

:deep(.el-tag) {
  border-radius: 4px;
  font-weight: 500;
  border: none;
  padding: 0 8px;
  height: 24px;
  line-height: 24px;
  font-size: 12px;
}
</style>

