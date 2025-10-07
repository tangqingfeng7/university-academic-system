<template>
  <div class="department-manage">
    <div class="page-header">
      <h2>院系管理</h2>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新建院系
      </el-button>
    </div>

    <el-card>
      <el-table
        v-loading="loading"
        :data="departmentList.content"
        stripe
        border
      >
        <el-table-column prop="code" label="院系代码" width="150" />
        <el-table-column prop="name" label="院系名称" min-width="200" />
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button 
              type="primary" 
              size="small" 
              plain
              @click="handleEdit(row)"
            >
              编辑
            </el-button>
            <el-button 
              type="danger" 
              size="small" 
              plain
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.size"
          :total="departmentList.totalElements"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="fetchDepartmentList"
          @size-change="fetchDepartmentList"
        />
      </div>
    </el-card>

    <!-- 新建/编辑院系对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
      @close="resetForm"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="院系代码" prop="code">
          <el-input 
            v-model="formData.code" 
            placeholder="请输入院系代码"
            :disabled="!!editingId"
          />
        </el-form-item>

        <el-form-item label="院系名称" prop="name">
          <el-input 
            v-model="formData.name" 
            placeholder="请输入院系名称"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button 
          type="primary" 
          :loading="submitLoading"
          @click="handleSubmit"
        >
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import {
  getDepartmentList,
  createDepartment,
  updateDepartment,
  deleteDepartment,
  checkDepartmentCode
} from '@/api/department'

const loading = ref(false)
const departmentList = ref({
  content: [],
  totalElements: 0,
  totalPages: 0
})
const dialogVisible = ref(false)
const dialogTitle = ref('')
const submitLoading = ref(false)
const formRef = ref(null)
const editingId = ref(null)

const queryParams = reactive({
  page: 0,
  size: 10,
  sortBy: 'id',
  direction: 'ASC'
})

const formData = ref({
  code: '',
  name: ''
})

const validateCode = async (rule, value, callback) => {
  if (!value) {
    return callback(new Error('请输入院系代码'))
  }
  
  // 编辑时不检查
  if (editingId.value) {
    return callback()
  }
  
  try {
    const { data } = await checkDepartmentCode(value)
    if (data.exists) {
      return callback(new Error('院系代码已存在'))
    }
    callback()
  } catch (error) {
    callback()
  }
}

const formRules = {
  code: [
    { required: true, validator: validateCode, trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入院系名称', trigger: 'blur' }
  ]
}

// 获取院系列表
const fetchDepartmentList = async () => {
  try {
    loading.value = true
    const { data } = await getDepartmentList(queryParams)
    departmentList.value = data
  } catch (error) {
    console.error('获取院系列表失败:', error)
    ElMessage.error('获取院系列表失败')
  } finally {
    loading.value = false
  }
}

// 新建院系
const handleAdd = () => {
  dialogTitle.value = '新建院系'
  editingId.value = null
  dialogVisible.value = true
}

// 编辑院系
const handleEdit = (row) => {
  dialogTitle.value = '编辑院系'
  editingId.value = row.id
  formData.value = {
    code: row.code,
    name: row.name
  }
  dialogVisible.value = true
}

// 删除院系
const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定要删除院系"${row.name}"吗？删除后该院系下的所有专业也将被删除。`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await deleteDepartment(row.id)
      ElMessage.success('删除成功')
      fetchDepartmentList()
    } catch (error) {
      console.error('删除失败:', error)
      ElMessage.error(error.response?.data?.message || '删除失败')
    }
  }).catch(() => {})
}

// 提交表单
const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  try {
    submitLoading.value = true
    
    if (editingId.value) {
      await updateDepartment(editingId.value, formData.value)
      ElMessage.success('更新成功')
    } else {
      await createDepartment(formData.value)
      ElMessage.success('创建成功')
    }
    
    dialogVisible.value = false
    fetchDepartmentList()
  } catch (error) {
    console.error('提交失败:', error)
    ElMessage.error(error.response?.data?.message || '提交失败')
  } finally {
    submitLoading.value = false
  }
}

// 重置表单
const resetForm = () => {
  formRef.value?.resetFields()
  formData.value = {
    code: '',
    name: ''
  }
  editingId.value = null
}

onMounted(() => {
  fetchDepartmentList()
})
</script>

<style scoped>
.department-manage {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>

