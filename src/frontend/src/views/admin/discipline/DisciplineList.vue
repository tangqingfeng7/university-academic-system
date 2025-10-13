<template>
  <div class="discipline-list">
    <el-card shadow="never" class="header-card">
      <h2>处分管理</h2>
      <p class="subtitle">管理学生违纪处分记录</p>
    </el-card>

    <!-- 搜索栏 -->
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="queryForm" @submit.prevent="handleQuery">
        <el-form-item label="学号/姓名">
          <el-input v-model="queryForm.keyword" placeholder="请输入学号或姓名" clearable style="width: 200px" />
        </el-form-item>
        
        <el-form-item label="处分类型">
          <el-select v-model="queryForm.disciplineType" placeholder="全部" clearable style="width: 150px">
            <el-option label="警告" value="WARNING" />
            <el-option label="严重警告" value="SERIOUS_WARNING" />
            <el-option label="记过" value="DEMERIT" />
            <el-option label="记大过" value="SERIOUS_DEMERIT" />
            <el-option label="留校察看" value="PROBATION" />
            <el-option label="开除学籍" value="EXPULSION" />
          </el-select>
        </el-form-item>

        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="有效" value="ACTIVE" />
            <el-option label="已解除" value="REMOVED" />
            <el-option label="申诉中" value="APPEALING" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="success" @click="handleAdd">新增处分</el-button>
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
        
        <el-table-column prop="status" label="状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ row.statusDescription }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="approvalStatus" label="审批状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="getApprovalStatusType(row.approvalStatus)">
              {{ row.approvalStatusDescription }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="260" align="center" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <div class="action-item" @click="handleView(row)">
                <el-icon class="action-icon"><View /></el-icon>
                <span class="action-text">查看</span>
              </div>
              <div 
                v-if="row.approvalStatus === 'PENDING'" 
                class="action-item action-approve"
                @click="handleApprove(row)"
              >
                <el-icon class="action-icon"><Check /></el-icon>
                <span class="action-text">审批</span>
              </div>
              <div 
                v-if="row.status === 'ACTIVE' && row.canRemove && row.approvalStatus === 'APPROVED'" 
                class="action-item action-remove"
                @click="handleRemove(row)"
              >
                <el-icon class="action-icon"><CircleClose /></el-icon>
                <span class="action-text">解除</span>
              </div>
              <div class="action-item action-delete" @click="handleDelete(row)">
                <el-icon class="action-icon"><Delete /></el-icon>
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

    <!-- 审批对话框 -->
    <el-dialog
      v-model="approveDialogVisible"
      title="审批处分"
      width="500px"
    >
      <el-alert
        type="info"
        :closable="false"
        style="margin-bottom: 20px"
      >
        <p><strong>学生：</strong>{{ approveData.studentName }} ({{ approveData.studentNo }})</p>
        <p><strong>处分类型：</strong>{{ approveData.disciplineTypeDescription }}</p>
        <p><strong>处分原因：</strong>{{ approveData.reason }}</p>
      </el-alert>

      <el-form :model="approveForm" :rules="approveRules" ref="approveFormRef" label-width="100px">
        <el-form-item label="审批结果" prop="approved">
          <el-radio-group v-model="approveForm.approved">
            <el-radio :label="true">批准</el-radio>
            <el-radio :label="false">拒绝</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="审批意见" prop="comment">
          <el-input
            v-model="approveForm.comment"
            type="textarea"
            :rows="4"
            placeholder="请输入审批意见（选填）"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="approveDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="approveLoading" @click="handleSubmitApprove">
          提交
        </el-button>
      </template>
    </el-dialog>

    <!-- 查看处分详情对话框 -->
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
        <el-descriptions-item v-if="viewData.status === 'REMOVED'" label="解除人" :span="2">
          {{ viewData.removedByName }}
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

    <!-- 新增/编辑处分对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="学生" prop="studentId">
          <el-input
            v-model="form.studentNo"
            placeholder="请输入学号搜索学生"
            clearable
            @input="handleSearchStudent"
          >
            <template #append>
              <el-button :icon="Search" @click="handleSearchStudent" />
            </template>
          </el-input>
          <div v-if="form.studentName" class="student-info">
            已选择：{{ form.studentName }}（{{ form.studentNo }}）
          </div>
        </el-form-item>

        <el-form-item label="处分类型" prop="disciplineType">
          <el-select v-model="form.disciplineType" placeholder="请选择处分类型" style="width: 100%">
            <el-option label="警告" value="WARNING" />
            <el-option label="严重警告" value="SERIOUS_WARNING" />
            <el-option label="记过" value="DEMERIT" />
            <el-option label="记大过" value="SERIOUS_DEMERIT" />
            <el-option label="留校察看" value="PROBATION" />
            <el-option label="开除学籍" value="EXPULSION" />
          </el-select>
        </el-form-item>

        <el-form-item label="违纪日期" prop="occurrenceDate">
          <el-date-picker
            v-model="form.occurrenceDate"
            type="date"
            placeholder="选择违纪发生日期"
            style="width: 100%"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>

        <el-form-item label="处分日期" prop="punishmentDate">
          <el-date-picker
            v-model="form.punishmentDate"
            type="date"
            placeholder="选择处分日期"
            style="width: 100%"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>

        <el-form-item label="处分原因" prop="reason">
          <el-input
            v-model="form.reason"
            type="textarea"
            :rows="4"
            placeholder="请输入处分原因"
          />
        </el-form-item>

        <el-form-item label="详细描述">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入详细描述（可选）"
          />
        </el-form-item>

        <el-form-item label="是否可解除">
          <el-switch v-model="form.canRemove" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 学生搜索对话框 -->
    <el-dialog
      v-model="studentSearchVisible"
      title="选择学生"
      width="800px"
    >
      <el-input
        v-model="studentKeyword"
        placeholder="输入学号或姓名搜索"
        clearable
        @input="handleStudentSearch"
        style="margin-bottom: 16px"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>

      <el-table
        v-loading="studentLoading"
        :data="studentList"
        @row-click="handleSelectStudent"
        highlight-current-row
        style="cursor: pointer"
      >
        <el-table-column prop="studentNo" label="学号" width="120" />
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column prop="majorName" label="专业" min-width="150" show-overflow-tooltip />
        <el-table-column prop="className" label="班级" width="100" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, View, Check, CircleClose, Delete } from '@element-plus/icons-vue'
import { getDisciplines, deleteDiscipline, removeDiscipline, createDiscipline, approveDiscipline } from '@/api/discipline'
import { getStudentList } from '@/api/student'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

// 数据
const loading = ref(false)
const tableData = ref([])

// 查看详情相关
const viewDialogVisible = ref(false)
const viewData = ref({})

// 审批相关
const approveDialogVisible = ref(false)
const approveData = ref({})
const approveLoading = ref(false)
const approveFormRef = ref(null)
const approveForm = reactive({
  approved: true,
  comment: ''
})
const approveRules = {
  approved: [
    { required: true, message: '请选择审批结果', trigger: 'change' }
  ]
}

// 对话框
const dialogVisible = ref(false)
const dialogTitle = ref('新增处分')
const submitLoading = ref(false)
const formRef = ref(null)

// 表单
const form = reactive({
  studentId: null,
  studentNo: '',
  studentName: '',
  disciplineType: '',
  occurrenceDate: '',
  punishmentDate: '',
  reason: '',
  description: '',
  canRemove: true
})

// 表单验证
const formRules = {
  studentId: [
    { required: true, message: '请选择学生', trigger: 'change' }
  ],
  disciplineType: [
    { required: true, message: '请选择处分类型', trigger: 'change' }
  ],
  occurrenceDate: [
    { required: true, message: '请选择违纪日期', trigger: 'change' }
  ],
  punishmentDate: [
    { required: true, message: '请选择处分日期', trigger: 'change' }
  ],
  reason: [
    { required: true, message: '请输入处分原因', trigger: 'blur' },
    { min: 5, message: '处分原因至少5个字', trigger: 'blur' }
  ]
}

// 学生搜索
const studentSearchVisible = ref(false)
const studentKeyword = ref('')
const studentList = ref([])
const studentLoading = ref(false)

// 查询表单
const queryForm = reactive({
  keyword: '',
  disciplineType: '',
  status: ''
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
      size: pagination.size
    }
    
    // 只添加非空的查询参数
    if (queryForm.keyword) params.keyword = queryForm.keyword
    if (queryForm.disciplineType) params.disciplineType = queryForm.disciplineType
    if (queryForm.status) params.status = queryForm.status
    
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
  Object.assign(queryForm, {
    keyword: '',
    disciplineType: '',
    status: ''
  })
  handleQuery()
}

// 新增
const handleAdd = () => {
  dialogTitle.value = '新增处分'
  resetForm()
  dialogVisible.value = true
}

// 重置表单
const resetForm = () => {
  Object.assign(form, {
    studentId: null,
    studentNo: '',
    studentName: '',
    disciplineType: '',
    occurrenceDate: '',
    punishmentDate: '',
    reason: '',
    description: '',
    canRemove: true
  })
  if (formRef.value) {
    formRef.value.clearValidate()
  }
}

// 对话框关闭
const handleDialogClose = () => {
  resetForm()
}

// 搜索学生
const handleSearchStudent = () => {
  studentSearchVisible.value = true
  handleStudentSearch()
}

// 学生搜索查询
const handleStudentSearch = async () => {
  if (!studentKeyword.value) {
    studentList.value = []
    return
  }

  studentLoading.value = true
  try {
    const res = await getStudentList({
      keyword: studentKeyword.value,
      page: 0,
      size: 20
    })
    studentList.value = res.data.content || []
  } catch (error) {
    console.error('搜索学生失败:', error)
    ElMessage.error('搜索学生失败')
  } finally {
    studentLoading.value = false
  }
}

// 选择学生
const handleSelectStudent = (row) => {
  form.studentId = row.id
  form.studentNo = row.studentNo
  form.studentName = row.name
  studentSearchVisible.value = false
  studentKeyword.value = ''
  studentList.value = []
}

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    
    submitLoading.value = true
    
    const data = {
      studentId: form.studentId,
      disciplineType: form.disciplineType,
      occurrenceDate: form.occurrenceDate,
      punishmentDate: form.punishmentDate,
      reason: form.reason,
      description: form.description,
      canRemove: form.canRemove,
      reporterId: userStore.userInfo.userId
    }
    
    await createDiscipline(data)
    
    ElMessage.success('处分记录创建成功')
    dialogVisible.value = false
    fetchDisciplines()
  } catch (error) {
    if (error !== false) {
      console.error('创建处分失败:', error)
      // 显示详细错误信息
      if (error.reason && Array.isArray(error.reason)) {
        error.reason.forEach(msg => ElMessage.error(msg))
      } else if (error.message) {
        ElMessage.error(error.message)
      } else {
        ElMessage.error('创建处分失败')
      }
    }
  } finally {
    submitLoading.value = false
  }
}

// 查看
const handleView = (row) => {
  viewData.value = { ...row }
  viewDialogVisible.value = true
}

// 审批
const handleApprove = (row) => {
  approveData.value = { ...row }
  approveForm.approved = true
  approveForm.comment = ''
  approveDialogVisible.value = true
}

// 提交审批
const handleSubmitApprove = async () => {
  try {
    await approveFormRef.value.validate()
    
    approveLoading.value = true
    
    await approveDiscipline(approveData.value.id, approveForm.approved, approveForm.comment)
    
    const message = approveForm.approved ? '处分已批准' : '处分已拒绝'
    ElMessage.success(message)
    approveDialogVisible.value = false
    fetchDisciplines()
  } catch (error) {
    if (error !== false) {
      console.error('审批失败:', error)
      ElMessage.error('审批失败')
    }
  } finally {
    approveLoading.value = false
  }
}

// 解除
const handleRemove = async (row) => {
  try {
    const { value: reason } = await ElMessageBox.prompt('请输入解除原因', '解除处分', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /.+/,
      inputErrorMessage: '解除原因不能为空'
    })

    await removeDiscipline(row.id, reason)
    ElMessage.success('处分已解除')
    fetchDisciplines()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('解除处分失败:', error)
      ElMessage.error('解除处分失败')
    }
  }
}

// 删除
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除此处分记录吗？', '确认删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await deleteDiscipline(row.id)
    ElMessage.success('删除成功')
    fetchDisciplines()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
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
.discipline-list {
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

.student-info {
  margin-top: 8px;
  padding: 8px 12px;
  background: #f0f9ff;
  border: 1px solid #bfdbfe;
  border-radius: 4px;
  color: #1e40af;
  font-size: 14px;
}

/* 操作按钮 - 苹果风格 */
.action-buttons {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  flex-wrap: wrap;
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

.action-approve {
  color: #67c23a;
}

.action-approve:hover {
  background: rgba(103, 194, 58, 0.06);
}

.action-remove {
  color: #e6a23c;
}

.action-remove:hover {
  background: rgba(230, 162, 60, 0.06);
}

.action-delete {
  color: #f56c6c;
  padding: 6px 8px;
}

.action-delete:hover {
  background: rgba(245, 108, 108, 0.06);
}

.action-delete .action-icon {
  font-size: 16px;
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

