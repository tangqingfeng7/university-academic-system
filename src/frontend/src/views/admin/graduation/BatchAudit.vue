<template>
  <div class="batch-audit">
    <el-card class="header-card">
      <h2>批量毕业审核</h2>
      <p class="subtitle">对满足条件的学生进行批量毕业审核</p>
    </el-card>

    <!-- 筛选条件 -->
    <el-card class="filter-card">
      <el-form :inline="true" :model="filterForm">
        <el-form-item label="院系">
          <el-select 
            v-model="filterForm.departmentId" 
            placeholder="请选择院系" 
            clearable
            style="width: 200px"
          >
            <el-option
              v-for="dept in departments"
              :key="dept.id"
              :label="dept.name"
              :value="dept.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="专业">
          <el-select 
            v-model="filterForm.majorId" 
            placeholder="请选择专业" 
            clearable 
            filterable
            style="width: 200px"
          >
            <el-option
              v-for="major in filteredMajors"
              :key="major.id"
              :label="major.name"
              :value="major.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="入学年份">
          <el-date-picker
            v-model="filterForm.enrollmentYear"
            type="year"
            placeholder="选择年份"
            value-format="YYYY"
            clearable
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadStudents">
            <el-icon><Search /></el-icon>
            查询学生
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 学生列表 -->
    <el-card>
      <template #header>
        <div class="card-header">
          <span>待审核学生列表</span>
          <el-button
            type="primary"
            :disabled="selectedStudents.length === 0"
            @click="handleBatchAudit"
          >
            <el-icon><Check /></el-icon>
            批量审核 ({{ selectedStudents.length }})
          </el-button>
        </div>
      </template>

      <el-table
        ref="tableRef"
        v-loading="loading"
        :data="students"
        border
        stripe
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="studentNo" label="学号" width="120" />
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column prop="majorName" label="专业" min-width="150" />
        <el-table-column prop="className" label="班级" width="120" />
        <el-table-column prop="enrollmentYear" label="入学年份" width="100" align="center" />
        <el-table-column label="学分情况" width="300">
          <template #default="{ row }">
            <div class="credit-info">
              <el-tag type="primary" size="small">总: {{ row.totalCredits || 0 }}</el-tag>
              <el-tag type="danger" size="small">必: {{ row.requiredCredits || 0 }}</el-tag>
              <el-tag type="warning" size="small">选: {{ row.electiveCredits || 0 }}</el-tag>
              <el-tag type="success" size="small">实: {{ row.practicalCredits || 0 }}</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleSingleAudit(row)">
              执行审核
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && students.length === 0" description="暂无符合条件的学生" />

      <el-pagination
        v-if="total > 0"
        v-model:current-page="page"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadStudents"
        @current-change="loadStudents"
        style="margin-top: 20px; justify-content: center"
      />
    </el-card>

    <!-- 审核进度对话框 -->
    <el-dialog
      v-model="progressVisible"
      title="批量审核进度"
      width="600px"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      :show-close="!auditing"
    >
      <div class="progress-content">
        <el-progress
          :percentage="progressPercentage"
          :status="auditComplete ? 'success' : undefined"
        />
        <div class="progress-text">
          {{ progressText }}
        </div>
        
        <el-descriptions v-if="auditResult" :column="2" border style="margin-top: 20px">
          <el-descriptions-item label="总数">{{ auditResult.totalCount }}</el-descriptions-item>
          <el-descriptions-item label="成功">{{ auditResult.successCount }}</el-descriptions-item>
          <el-descriptions-item label="通过">
            <el-tag type="success">{{ auditResult.passCount }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="未通过">
            <el-tag type="danger">{{ auditResult.notPassCount }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="暂缓">
            <el-tag type="warning">{{ auditResult.deferredCount }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="失败">
            <el-tag type="info">{{ auditResult.failCount }}</el-tag>
          </el-descriptions-item>
        </el-descriptions>

        <el-alert
          v-if="auditResult && auditResult.errors && auditResult.errors.length > 0"
          title="错误详情"
          type="error"
          :closable="false"
          style="margin-top: 20px"
        >
          <div v-for="(error, index) in auditResult.errors" :key="index">
            {{ error }}
          </div>
        </el-alert>
      </div>

      <template #footer>
        <el-button v-if="auditComplete" type="primary" @click="handleAuditComplete">
          完成
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Check } from '@element-plus/icons-vue'
import request from '@/utils/request'

const loading = ref(false)
const auditing = ref(false)
const progressVisible = ref(false)
const auditComplete = ref(false)
const tableRef = ref(null)

const students = ref([])
const departments = ref([])
const majors = ref([])
const selectedStudents = ref([])
const auditResult = ref(null)

const page = ref(1)
const pageSize = ref(20)
const total = ref(0)

const filterForm = reactive({
  departmentId: null,
  majorId: null,
  enrollmentYear: null
})

const progressPercentage = ref(0)
const progressText = ref('')

// 过滤专业
const filteredMajors = computed(() => {
  if (!filterForm.departmentId) return majors.value
  return majors.value.filter(m => m.departmentId === filterForm.departmentId)
})

// 加载院系列表
const loadDepartments = async () => {
  try {
    const response = await request.get('/admin/departments/all')
    departments.value = response.data || []
  } catch (error) {
    console.error('加载院系列表失败:', error)
  }
}

// 加载专业列表
const loadMajors = async () => {
  try {
    const response = await request.get('/admin/majors/all')
    majors.value = response.data || []
  } catch (error) {
    console.error('加载专业列表失败:', error)
  }
}

// 加载学生列表
const loadStudents = async () => {
  loading.value = true
  try {
    const params = {
      page: page.value - 1,
      size: pageSize.value,
      ...filterForm
    }
    
    const response = await request.get('/admin/students', { params })
    students.value = response.data.content || []
    total.value = response.data.totalElements || 0
  } catch (error) {
    ElMessage.error(error.message || '加载学生列表失败')
  } finally {
    loading.value = false
  }
}

// 选择变更
const handleSelectionChange = (selection) => {
  selectedStudents.value = selection
}

// 单个审核
const handleSingleAudit = async (row) => {
  auditing.value = true
  progressVisible.value = true
  auditComplete.value = false
  progressPercentage.value = 0
  progressText.value = '正在审核...'
  auditResult.value = null

  try {
    const response = await request.post(`/admin/graduation/audit/${row.id}`)
    
    progressPercentage.value = 100
    progressText.value = '审核完成'
    auditComplete.value = true
    
    // 构造单个结果
    auditResult.value = {
      totalCount: 1,
      successCount: 1,
      failCount: 0,
      passCount: response.data.status === 'PASS' ? 1 : 0,
      notPassCount: response.data.status === 'FAIL' ? 1 : 0,
      deferredCount: response.data.status === 'DEFERRED' ? 1 : 0,
      errors: []
    }

    ElMessage.success('审核完成')
    loadStudents()
  } catch (error) {
    progressPercentage.value = 100
    progressText.value = '审核失败'
    auditComplete.value = true
    auditResult.value = {
      totalCount: 1,
      successCount: 0,
      failCount: 1,
      errors: [error.message || '审核失败']
    }
    ElMessage.error(error.message || '审核失败')
  } finally {
    auditing.value = false
  }
}

// 批量审核
const handleBatchAudit = async () => {
  if (selectedStudents.value.length === 0) {
    ElMessage.warning('请选择要审核的学生')
    return
  }

  auditing.value = true
  progressVisible.value = true
  auditComplete.value = false
  progressPercentage.value = 0
  progressText.value = `正在审核 ${selectedStudents.value.length} 名学生...`
  auditResult.value = null

  try {
    const studentIds = selectedStudents.value.map(s => s.id)
    const response = await request.post('/admin/graduation/audit/batch', studentIds)
    
    auditResult.value = response.data
    progressPercentage.value = 100
    progressText.value = '批量审核完成'
    auditComplete.value = true
    
    ElMessage.success('批量审核完成')
    
    // 清除选择
    tableRef.value?.clearSelection()
    loadStudents()
  } catch (error) {
    progressPercentage.value = 100
    progressText.value = '批量审核失败'
    auditComplete.value = true
    ElMessage.error(error.message || '批量审核失败')
  } finally {
    auditing.value = false
  }
}

// 完成
const handleAuditComplete = () => {
  progressVisible.value = false
  auditResult.value = null
}

onMounted(() => {
  loadDepartments()
  loadMajors()
})
</script>

<style scoped lang="scss">
.batch-audit {
  .header-card {
    margin-bottom: 20px;

    h2 {
      margin: 0 0 8px;
      font-size: 24px;
      color: #303133;
    }

    .subtitle {
      margin: 0;
      color: #909399;
      font-size: 14px;
    }
  }

  .filter-card {
    margin-bottom: 20px;
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .credit-info {
    display: flex;
    gap: 8px;
  }

  .progress-content {
    .progress-text {
      text-align: center;
      margin-top: 16px;
      font-size: 14px;
      color: #606266;
    }
  }
}
</style>

