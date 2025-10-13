<template>
  <div class="scheduling-solutions">
    <el-page-header title="排课管理" content="排课方案管理" />

    <!-- 筛选栏 -->
    <el-card shadow="hover" class="filter-card">
      <el-form :inline="true" :model="filterForm">
        <el-form-item label="学期">
          <el-select 
            v-model="filterForm.semesterId" 
            placeholder="选择学期"
            style="width: 200px"
            @change="loadSolutions"
          >
            <el-option
              v-for="semester in semesters"
              :key="semester.id"
              :label="semester.name"
              :value="semester.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleCreate">
            <el-icon><Plus /></el-icon>
            创建方案
          </el-button>
          <el-button @click="loadSolutions">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 方案列表 -->
    <el-card shadow="hover" v-loading="loading" class="list-card">
      <el-table :data="solutions" stripe>
        <el-table-column prop="name" label="方案名称" min-width="180" />
        <el-table-column label="状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="质量分数" width="100" align="center">
          <template #default="{ row }">
            <span v-if="row.qualityScore !== null">
              {{ row.qualityScore.toFixed(2) }}
            </span>
            <span v-else class="empty-text">--</span>
          </template>
        </el-table-column>
        <el-table-column prop="conflictCount" label="冲突数" width="90" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.conflictCount > 0" type="danger" size="small">
              {{ row.conflictCount }}
            </el-tag>
            <span v-else class="success-text">0</span>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="180" align="center">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="应用时间" width="180" align="center">
          <template #default="{ row }">
            <span v-if="row.appliedAt">{{ formatDateTime(row.appliedAt) }}</span>
            <span v-else class="empty-text">未应用</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="300" fixed="right" align="center">
          <template #default="{ row }">
            <el-button 
              text 
              size="small" 
              @click="handleOptimize(row)"
              :disabled="row.status === 'OPTIMIZING'"
            >
              智能排课
            </el-button>
            <el-button 
              text 
              size="small" 
              @click="handlePreview(row)"
              :disabled="row.status === 'DRAFT'"
            >
              预览
            </el-button>
            <el-button 
              text 
              size="small" 
              @click="handleApply(row)"
              :disabled="row.status !== 'COMPLETED'"
            >
              应用
            </el-button>
            <el-button 
              text 
              size="small" 
              @click="handleDelete(row)"
              :disabled="row.status === 'APPLIED'"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="solutions.length === 0" class="empty-state">
        <el-empty description="暂无方案数据" />
      </div>

      <!-- 分页 -->
      <div class="pagination" v-if="total > 0">
        <el-pagination
          v-model:current-page="filterForm.page"
          v-model:page-size="filterForm.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadSolutions"
          @current-change="loadSolutions"
        />
      </div>
    </el-card>

    <!-- 创建方案对话框 -->
    <el-dialog
      v-model="createDialogVisible"
      title="创建排课方案"
      width="500px"
      @close="handleCreateDialogClose"
    >
      <el-form
        ref="createFormRef"
        :model="createForm"
        :rules="createRules"
        label-width="100px"
      >
        <el-form-item label="学期" prop="semesterId">
          <el-select 
            v-model="createForm.semesterId" 
            placeholder="请选择学期"
            style="width: 100%"
          >
            <el-option
              v-for="semester in semesters"
              :key="semester.id"
              :label="semester.name"
              :value="semester.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="方案名称" prop="name">
          <el-input
            v-model="createForm.name"
            placeholder="请输入方案名称"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitCreate" :loading="creating">
          创建
        </el-button>
      </template>
    </el-dialog>

    <!-- 应用方案对话框 -->
    <el-dialog
      v-model="applyDialogVisible"
      title="应用排课方案"
      width="500px"
    >
      <el-alert
        title="注意"
        type="warning"
        :closable="false"
        class="apply-alert"
      >
        应用方案将把排课结果写入课程开课表，请确认操作无误。
      </el-alert>

      <el-form :model="applyForm" label-width="120px" class="apply-form">
        <el-form-item label="强制应用">
          <el-switch v-model="applyForm.force" />
          <div class="form-tip">忽略冲突检查，强制应用方案</div>
        </el-form-item>

        <el-form-item label="覆盖现有排课">
          <el-switch v-model="applyForm.overwrite" />
          <div class="form-tip">覆盖已有的排课数据</div>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="applyDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleConfirmApply" :loading="applying">
          确定应用
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import {
  getSolutionsBySemester,
  createSolution,
  deleteSolution,
  applySolution
} from '@/api/scheduling'
import { getSemesters } from '@/api/semester'
import { formatDateTime } from '@/utils/date'

const router = useRouter()
const loading = ref(false)
const creating = ref(false)
const applying = ref(false)
const createDialogVisible = ref(false)
const applyDialogVisible = ref(false)
const createFormRef = ref(null)

const semesters = ref([])
const solutions = ref([])
const total = ref(0)
const currentSolution = ref(null)

const filterForm = reactive({
  semesterId: null,
  page: 1,
  size: 20
})

const createForm = reactive({
  semesterId: null,
  name: ''
})

const createRules = {
  semesterId: [
    { required: true, message: '请选择学期', trigger: 'change' }
  ],
  name: [
    { required: true, message: '请输入方案名称', trigger: 'blur' },
    { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' }
  ]
}

const applyForm = reactive({
  force: false,
  overwrite: false
})

// 状态类型映射
const getStatusType = (status) => {
  const typeMap = {
    DRAFT: 'info',
    OPTIMIZING: 'warning',
    COMPLETED: 'success',
    APPLIED: 'primary'
  }
  return typeMap[status] || 'info'
}

// 状态文本映射
const getStatusText = (status) => {
  const textMap = {
    DRAFT: '草稿',
    OPTIMIZING: '排课中',
    COMPLETED: '已完成',
    APPLIED: '已应用'
  }
  return textMap[status] || status
}

// 加载学期列表
const loadSemesters = async () => {
  try {
    const res = await getSemesters()
    semesters.value = res.data || []
    if (semesters.value.length > 0 && !filterForm.semesterId) {
      // 默认选择第一个学期
      filterForm.semesterId = semesters.value[0].id
    }
  } catch (error) {
    ElMessage.error(error.message || '加载学期列表失败')
  }
}

// 加载方案列表
const loadSolutions = async () => {
  if (!filterForm.semesterId) {
    solutions.value = []
    total.value = 0
    return
  }

  loading.value = true
  try {
    const params = {
      semesterId: filterForm.semesterId,
      page: filterForm.page - 1,
      size: filterForm.size
    }
    const res = await getSolutionsBySemester(params)
    solutions.value = res.data.content || []
    total.value = res.data.totalElements || 0
  } catch (error) {
    ElMessage.error(error.message || '加载方案列表失败')
  } finally {
    loading.value = false
  }
}

// 打开创建对话框
const handleCreate = () => {
  createForm.semesterId = filterForm.semesterId
  createForm.name = ''
  createDialogVisible.value = true
}

// 提交创建
const handleSubmitCreate = async () => {
  if (!createFormRef.value) return

  await createFormRef.value.validate(async (valid) => {
    if (!valid) return

    creating.value = true
    try {
      await createSolution(createForm)
      ElMessage.success('创建方案成功')
      createDialogVisible.value = false
      await loadSolutions()
    } catch (error) {
      ElMessage.error(error.message || '创建方案失败')
    } finally {
      creating.value = false
    }
  })
}

// 智能排课
const handleOptimize = (row) => {
  router.push({
    name: 'AdminSchedulingOptimize',
    params: { id: row.id }
  })
}

// 预览方案
const handlePreview = (row) => {
  router.push({
    name: 'AdminSchedulingPreview',
    params: { id: row.id }
  })
}

// 应用方案
const handleApply = (row) => {
  currentSolution.value = row
  applyForm.force = false
  applyForm.overwrite = false
  applyDialogVisible.value = true
}

// 确认应用
const handleConfirmApply = async () => {
  if (!currentSolution.value) return

  applying.value = true
  try {
    await applySolution(currentSolution.value.id, applyForm)
    ElMessage.success('应用方案成功')
    applyDialogVisible.value = false
    await loadSolutions()
  } catch (error) {
    ElMessage.error(error.message || '应用方案失败')
  } finally {
    applying.value = false
  }
}

// 删除方案
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除方案"${row.name}"吗？此操作不可恢复。`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    loading.value = true
    await deleteSolution(row.id)
    ElMessage.success('删除成功')
    await loadSolutions()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  } finally {
    loading.value = false
  }
}

// 关闭创建对话框
const handleCreateDialogClose = () => {
  createFormRef.value?.resetFields()
}

onMounted(async () => {
  await loadSemesters()
  await loadSolutions()
})
</script>

<style scoped lang="scss">
.scheduling-solutions {
  padding: 20px;

  :deep(.el-page-header) {
    margin-bottom: 20px;
  }
}

.filter-card {
  margin-bottom: 16px;

  :deep(.el-form--inline .el-form-item) {
    margin-bottom: 0;
  }
}

.list-card {
  .empty-state {
    padding: 40px 0;
  }

  .empty-text {
    color: #909399;
    font-style: italic;
  }

  .success-text {
    color: #67c23a;
    font-weight: 500;
  }
}

.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.apply-alert {
  margin-bottom: 20px;
}

.apply-form {
  .form-tip {
    margin-top: 4px;
    font-size: 12px;
    color: #909399;
  }
}
</style>

