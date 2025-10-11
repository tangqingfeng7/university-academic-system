<template>
  <div class="graduation-requirements">
    <el-card class="header-card">
      <div class="header-content">
        <div>
          <h2>毕业要求管理</h2>
          <p class="subtitle">设置和管理各专业的毕业学分要求</p>
        </div>
        <el-button type="primary" @click="showCreateDialog">
          <el-icon><Plus /></el-icon>
          添加毕业要求
        </el-button>
      </div>
    </el-card>

    <!-- 查询条件 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="专业">
          <el-select v-model="searchForm.majorId" placeholder="请选择专业" clearable filterable>
            <el-option
              v-for="major in majors"
              :key="major.id"
              :label="major.name"
              :value="major.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="入学年份">
          <el-date-picker
            v-model="searchForm.enrollmentYear"
            type="year"
            placeholder="选择年份"
            value-format="YYYY"
            clearable
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadRequirements">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button @click="resetSearch">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 要求列表 -->
    <el-card>
      <el-table
        v-loading="loading"
        :data="requirements"
        border
        stripe
        style="width: 100%"
      >
        <el-table-column prop="majorName" label="专业" min-width="150" />
        <el-table-column prop="enrollmentYear" label="入学年份" width="120" align="center" />
        <el-table-column prop="totalCreditsRequired" label="总学分" width="100" align="center">
          <template #default="{ row }">
            <el-tag type="primary">{{ row.totalCreditsRequired }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="requiredCreditsRequired" label="必修学分" width="100" align="center">
          <template #default="{ row }">
            <el-tag type="danger">{{ row.requiredCreditsRequired }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="electiveCreditsRequired" label="选修学分" width="100" align="center">
          <template #default="{ row }">
            <el-tag type="warning">{{ row.electiveCreditsRequired }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="practicalCreditsRequired" label="实践学分" width="100" align="center">
          <template #default="{ row }">
            <el-tag type="success">{{ row.practicalCreditsRequired }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="showEditDialog(row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button link type="danger" @click="handleDelete(row)">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && requirements.length === 0" description="暂无数据" />
    </el-card>

    <!-- 创建/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑毕业要求' : '添加毕业要求'"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
      >
        <el-form-item label="专业" prop="majorId">
          <el-select
            v-model="form.majorId"
            placeholder="请选择专业"
            filterable
            :disabled="isEdit"
            style="width: 100%"
          >
            <el-option
              v-for="major in majors"
              :key="major.id"
              :label="`${major.name} (${major.code})`"
              :value="major.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="入学年份" prop="enrollmentYear">
          <el-date-picker
            v-model="form.enrollmentYear"
            type="year"
            placeholder="选择入学年份"
            value-format="YYYY"
            :disabled="isEdit"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="总学分要求" prop="totalCreditsRequired">
          <el-input-number
            v-model="form.totalCreditsRequired"
            :min="0"
            :max="300"
            :precision="1"
            controls-position="right"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="必修学分要求" prop="requiredCreditsRequired">
          <el-input-number
            v-model="form.requiredCreditsRequired"
            :min="0"
            :max="200"
            :precision="1"
            controls-position="right"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="选修学分要求" prop="electiveCreditsRequired">
          <el-input-number
            v-model="form.electiveCreditsRequired"
            :min="0"
            :max="100"
            :precision="1"
            controls-position="right"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="实践学分要求" prop="practicalCreditsRequired">
          <el-input-number
            v-model="form.practicalCreditsRequired"
            :min="0"
            :max="100"
            :precision="1"
            controls-position="right"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="附加要求">
          <el-input
            v-model="form.additionalRequirements"
            type="textarea"
            :rows="3"
            placeholder="请输入其他毕业要求，如：CET-4、体育达标等"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh, Edit, Delete } from '@element-plus/icons-vue'
import request from '@/utils/request'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)

const requirements = ref([])
const majors = ref([])

const searchForm = reactive({
  majorId: null,
  enrollmentYear: null
})

const form = reactive({
  id: null,
  majorId: null,
  enrollmentYear: null,
  totalCreditsRequired: 150,
  requiredCreditsRequired: 100,
  electiveCreditsRequired: 40,
  practicalCreditsRequired: 10,
  additionalRequirements: ''
})

const rules = {
  majorId: [{ required: true, message: '请选择专业', trigger: 'change' }],
  enrollmentYear: [{ required: true, message: '请选择入学年份', trigger: 'change' }],
  totalCreditsRequired: [{ required: true, message: '请输入总学分要求', trigger: 'blur' }],
  requiredCreditsRequired: [{ required: true, message: '请输入必修学分要求', trigger: 'blur' }],
  electiveCreditsRequired: [{ required: true, message: '请输入选修学分要求', trigger: 'blur' }],
  practicalCreditsRequired: [{ required: true, message: '请输入实践学分要求', trigger: 'blur' }]
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

// 加载毕业要求列表
const loadRequirements = async () => {
  loading.value = true
  try {
    let url = '/admin/graduation/requirements'
    const params = {}
    if (searchForm.majorId) params.majorId = searchForm.majorId
    if (searchForm.enrollmentYear) params.enrollmentYear = searchForm.enrollmentYear

    const response = await request.get(url, { params })
    requirements.value = response.data || []
  } catch (error) {
    ElMessage.error(error.message || '加载毕业要求失败')
  } finally {
    loading.value = false
  }
}

// 重置搜索
const resetSearch = () => {
  searchForm.majorId = null
  searchForm.enrollmentYear = null
  loadRequirements()
}

// 显示创建对话框
const showCreateDialog = () => {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

// 显示编辑对话框
const showEditDialog = (row) => {
  isEdit.value = true
  Object.assign(form, row)
  dialogVisible.value = true
}

// 重置表单
const resetForm = () => {
  form.id = null
  form.majorId = null
  form.enrollmentYear = null
  form.totalCreditsRequired = 150
  form.requiredCreditsRequired = 100
  form.electiveCreditsRequired = 40
  form.practicalCreditsRequired = 10
  form.additionalRequirements = ''
  formRef.value?.clearValidate()
}

// 提交表单
const handleSubmit = async () => {
  const valid = await formRef.value?.validate()
  if (!valid) return

  submitting.value = true
  try {
    if (isEdit.value) {
      await request.put(`/admin/graduation/requirements/${form.id}`, form)
      ElMessage.success('更新成功')
    } else {
      await request.post('/admin/graduation/requirements', form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadRequirements()
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

// 删除
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除 ${row.majorName} ${row.enrollmentYear}级 的毕业要求吗？`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await request.delete(`/admin/graduation/requirements/${row.id}`)
    ElMessage.success('删除成功')
    loadRequirements()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

onMounted(() => {
  loadMajors()
  loadRequirements()
})
</script>

<style scoped lang="scss">
.graduation-requirements {
  .header-card {
    margin-bottom: 20px;

    .header-content {
      display: flex;
      justify-content: space-between;
      align-items: center;

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
  }

  .search-card {
    margin-bottom: 20px;
  }
}
</style>

