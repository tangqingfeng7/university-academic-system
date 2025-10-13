<template>
  <div class="major-manage">
    <div class="page-header">
      <h2>专业管理</h2>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新建专业
      </el-button>
    </div>

    <el-card>
      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-select
          v-model="queryParams.departmentId"
          placeholder="选择院系"
          clearable
          filterable
          style="width: 200px"
          @change="fetchMajorList"
        >
          <el-option
            v-for="dept in departmentOptions"
            :key="dept.id"
            :label="dept.name"
            :value="dept.id"
          />
        </el-select>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>

      <el-table
        v-loading="loading"
        :data="majorList.content"
        stripe
        border
      >
        <el-table-column prop="code" label="专业代码" width="150" />
        <el-table-column prop="name" label="专业名称" min-width="200" />
        <el-table-column prop="departmentName" label="所属院系" width="200" />
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button 
              type="success" 
              size="small" 
              plain
              @click="handleClassManage(row)"
            >
              班级管理
            </el-button>
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
          :total="majorList.totalElements"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="fetchMajorList"
          @size-change="fetchMajorList"
        />
      </div>
    </el-card>

    <!-- 新建/编辑专业对话框 -->
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
        <el-form-item label="专业代码" prop="code">
          <el-input 
            v-model="formData.code" 
            placeholder="请输入专业代码"
            :disabled="!!editingId"
          />
        </el-form-item>

        <el-form-item label="专业名称" prop="name">
          <el-input 
            v-model="formData.name" 
            placeholder="请输入专业名称"
          />
        </el-form-item>

        <el-form-item label="所属院系" prop="departmentId">
          <el-select
            v-model="formData.departmentId"
            placeholder="请选择院系"
            filterable
            style="width: 100%"
          >
            <el-option
              v-for="dept in departmentOptions"
              :key="dept.id"
              :label="dept.name"
              :value="dept.id"
            />
          </el-select>
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
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import {
  getMajorList,
  createMajor,
  updateMajor,
  deleteMajor,
  checkMajorCode
} from '@/api/major'
import { getAllDepartments } from '@/api/department'

const router = useRouter()

const loading = ref(false)
const majorList = ref({
  content: [],
  totalElements: 0,
  totalPages: 0
})
const departmentOptions = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const submitLoading = ref(false)
const formRef = ref(null)
const editingId = ref(null)

const queryParams = reactive({
  page: 0,
  size: 10,
  sortBy: 'id',
  direction: 'ASC',
  departmentId: null
})

const formData = ref({
  code: '',
  name: '',
  departmentId: null
})

const validateCode = async (rule, value, callback) => {
  if (!value) {
    return callback(new Error('请输入专业代码'))
  }
  
  // 编辑时不检查
  if (editingId.value) {
    return callback()
  }
  
  try {
    const { data } = await checkMajorCode(value)
    if (data.exists) {
      return callback(new Error('专业代码已存在'))
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
    { required: true, message: '请输入专业名称', trigger: 'blur' }
  ],
  departmentId: [
    { required: true, message: '请选择所属院系', trigger: 'change' }
  ]
}

// 获取院系列表
const fetchDepartmentList = async () => {
  try {
    const { data } = await getAllDepartments()
    departmentOptions.value = data || []
  } catch (error) {
    console.error('获取院系列表失败:', error)
  }
}

// 获取专业列表
const fetchMajorList = async () => {
  try {
    loading.value = true
    const params = {
      ...queryParams,
      departmentId: queryParams.departmentId || undefined
    }
    const { data } = await getMajorList(params)
    majorList.value = data
  } catch (error) {
    console.error('获取专业列表失败:', error)
    ElMessage.error('获取专业列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  queryParams.page = 0
  fetchMajorList()
}

// 重置搜索
const handleReset = () => {
  queryParams.departmentId = null
  queryParams.page = 0
  fetchMajorList()
}

// 班级管理
const handleClassManage = (row) => {
  router.push({
    path: '/admin/classes',
    query: { majorId: row.id, majorName: row.name }
  })
}

// 新建专业
const handleAdd = () => {
  dialogTitle.value = '新建专业'
  editingId.value = null
  dialogVisible.value = true
}

// 编辑专业
const handleEdit = (row) => {
  dialogTitle.value = '编辑专业'
  editingId.value = row.id
  formData.value = {
    code: row.code,
    name: row.name,
    departmentId: row.departmentId
  }
  dialogVisible.value = true
}

// 删除专业
const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定要删除专业"${row.name}"吗？删除后该专业下的所有学生将无法关联到该专业。`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await deleteMajor(row.id)
      ElMessage.success('删除成功')
      fetchMajorList()
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
      await updateMajor(editingId.value, formData.value)
      ElMessage.success('更新成功')
    } else {
      await createMajor(formData.value)
      ElMessage.success('创建成功')
    }
    
    dialogVisible.value = false
    fetchMajorList()
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
    name: '',
    departmentId: null
  }
  editingId.value = null
}

onMounted(() => {
  fetchDepartmentList()
  fetchMajorList()
})
</script>

<style scoped>
.major-manage {
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

.search-bar {
  margin-bottom: 20px;
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>

