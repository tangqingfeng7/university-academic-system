<template>
  <div class="student-list-container">
    <el-card class="search-card">
      <search-form
        :fields="searchFields"
        @search="handleSearch"
        @reset="handleReset"
      >
        <template #extra-buttons>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            添加学生
          </el-button>
          <el-button type="success" @click="handleImport">
            <el-icon><Upload /></el-icon>
            导入学生
          </el-button>
          <el-button type="warning" @click="handleExport">
            <el-icon><Download /></el-icon>
            导出学生
          </el-button>
        </template>
      </search-form>
    </el-card>

    <el-card class="table-card">
      <data-table
        :columns="columns"
        :data="tableData"
        :total="total"
        :loading="loading"
        @page-change="handlePageChange"
        @size-change="handleSizeChange"
      >
        <template #gender="{ row }">
          <el-tag :type="row.gender === 'MALE' ? 'primary' : 'danger'">
            {{ row.gender === 'MALE' ? '男' : '女' }}
          </el-tag>
        </template>
        <template #operation="{ row }">
          <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </data-table>
    </el-card>

    <!-- 添加/编辑学生对话框 -->
    <student-form
      v-model="formVisible"
      :student-id="currentStudentId"
      :mode="formMode"
      @success="handleFormSuccess"
    />

    <!-- 导入对话框 -->
    <el-dialog v-model="importVisible" title="导入学生" width="500px">
      <file-upload
        ref="uploadRef"
        :action="`${baseURL}/api/admin/students/import`"
        :limit="1"
        accept=".xlsx,.xls"
        :on-success="handleImportSuccess"
        :on-error="handleImportError"
      >
        <template #tip>
          <div class="upload-tip">
            <p>支持 .xlsx、.xls 格式的Excel文件</p>
            <p>
              <el-link type="primary" @click="downloadTemplate">下载导入模板</el-link>
            </p>
          </div>
        </template>
      </file-upload>
      <template #footer>
        <el-button @click="importVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Upload, Download } from '@element-plus/icons-vue'
import DataTable from '@/components/DataTable.vue'
import SearchForm from '@/components/SearchForm.vue'
import FileUpload from '@/components/FileUpload.vue'
import StudentForm from './StudentForm.vue'
import { 
  getStudentList, 
  deleteStudent, 
  exportStudents,
  getAllClasses
} from '@/api/student'
import { getAllMajors } from '@/api/system'
import { useDebounceFn } from '@/utils/debounce'

const baseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

// 搜索条件
const searchFields = ref([
  {
    prop: 'studentNo',
    label: '学号',
    type: 'input',
    placeholder: '请输入学号'
  },
  {
    prop: 'name',
    label: '姓名',
    type: 'input',
    placeholder: '请输入姓名'
  },
  {
    prop: 'majorId',
    label: '专业',
    type: 'select',
    placeholder: '请选择专业',
    options: []
  },
  {
    prop: 'className',
    label: '班级',
    type: 'select',
    placeholder: '请选择班级',
    options: []
  }
])

// 表格列配置
const columns = [
  { prop: 'studentNo', label: '学号', width: 120 },
  { prop: 'name', label: '姓名', width: 100 },
  { prop: 'gender', label: '性别', width: 80, slot: 'gender' },
  { prop: 'birthDate', label: '出生日期', width: 120 },
  { prop: 'enrollmentYear', label: '入学年份', width: 100 },
  { prop: 'majorName', label: '专业', width: 150 },
  { prop: 'className', label: '班级', width: 120 },
  { prop: 'phone', label: '联系电话', width: 130 },
  { prop: 'operation', label: '操作', width: 150, fixed: 'right', slot: 'operation' }
]

// 表格数据
const tableData = ref([])
const total = ref(0)
const loading = ref(false)

// 查询参数
const queryParams = reactive({
  page: 1,
  size: 10,
  studentNo: '',
  name: '',
  majorId: null,
  className: ''
})

// 表单相关
const formVisible = ref(false)
const formMode = ref('add') // add | edit
const currentStudentId = ref(null)

// 导入相关
const importVisible = ref(false)
const uploadRef = ref(null)

// 获取学生列表
const fetchStudentList = async () => {
  loading.value = true
  try {
    const params = {
      page: queryParams.page - 1, // 后端从0开始
      size: queryParams.size
    }
    
    // 合并学号和姓名为keyword参数
    let keyword = ''
    if (queryParams.studentNo) keyword = queryParams.studentNo
    else if (queryParams.name) keyword = queryParams.name
    
    if (keyword) params.keyword = keyword
    if (queryParams.majorId) params.majorId = queryParams.majorId
    if (queryParams.className) params.className = queryParams.className
    
    const res = await getStudentList(params)
    tableData.value = res.data.content
    total.value = res.data.totalElements
  } catch (error) {
    console.error('获取学生列表失败:', error)
    ElMessage.error('获取学生列表失败')
  } finally {
    loading.value = false
  }
}

// 获取专业列表（用于搜索下拉）
const fetchMajorList = async () => {
  try {
    const res = await getAllMajors()
    const majorField = searchFields.value.find(f => f.prop === 'majorId')
    if (majorField) {
      majorField.options = res.data.map(major => ({
        label: major.name,
        value: major.id
      }))
    }
  } catch (error) {
    console.error('获取专业列表失败:', error)
  }
}

// 获取班级列表（用于搜索下拉）
const fetchClassList = async () => {
  try {
    const res = await getAllClasses()
    const classField = searchFields.value.find(f => f.prop === 'className')
    if (classField) {
      classField.options = res.data.map(className => ({
        label: className,
        value: className
      }))
    }
  } catch (error) {
    console.error('获取班级列表失败:', error)
  }
}

// 搜索（使用防抖优化）
const handleSearchInternal = (searchData) => {
  // 将学号或姓名合并为keyword
  const { studentNo, name, majorId, className } = searchData
  queryParams.studentNo = studentNo || ''
  queryParams.name = name || ''
  queryParams.majorId = majorId || null
  queryParams.className = className || ''
  queryParams.page = 1
  fetchStudentList()
}

// 使用防抖包装搜索函数
const handleSearch = useDebounceFn(handleSearchInternal, 500)

// 重置
const handleReset = () => {
  queryParams.page = 1
  queryParams.size = 10
  queryParams.studentNo = ''
  queryParams.name = ''
  queryParams.majorId = null
  queryParams.className = ''
  fetchStudentList()
}

// 分页
const handlePageChange = (pageInfo) => {
  if (typeof pageInfo === 'object') {
    queryParams.page = pageInfo.page
    queryParams.size = pageInfo.size
  } else {
    queryParams.page = pageInfo
  }
  fetchStudentList()
}

const handleSizeChange = (size) => {
  queryParams.size = size
  queryParams.page = 1
  fetchStudentList()
}

// 添加学生
const handleAdd = () => {
  formMode.value = 'add'
  currentStudentId.value = null
  formVisible.value = true
}

// 编辑学生
const handleEdit = (row) => {
  formMode.value = 'edit'
  currentStudentId.value = row.id
  formVisible.value = true
}

// 删除学生
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除学生 ${row.name}（${row.studentNo}）吗？`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await deleteStudent(row.id)
    ElMessage.success('删除成功')
    fetchStudentList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 表单提交成功
const handleFormSuccess = () => {
  formVisible.value = false
  fetchStudentList()
}

// 导入
const handleImport = () => {
  importVisible.value = true
}

const handleImportSuccess = (response) => {
  ElMessage.success(`导入成功！成功: ${response.data.successCount}, 失败: ${response.data.failureCount}`)
  if (response.data.errors && response.data.errors.length > 0) {
    console.log('导入错误:', response.data.errors)
  }
  importVisible.value = false
  fetchStudentList()
}

const handleImportError = (error) => {
  console.error('导入失败:', error)
  ElMessage.error('导入失败，请检查文件格式')
}

const downloadTemplate = () => {
  // 这里可以下载一个模板文件
  window.open(`${baseURL}/api/admin/students/template`, '_blank')
}

// 导出
const handleExport = async () => {
  try {
    const res = await exportStudents()
    
    // 创建blob对象
    const blob = new Blob([res], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    })
    
    // 创建下载链接
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `学生信息_${new Date().getTime()}.xlsx`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  }
}

onMounted(() => {
  fetchMajorList()
  fetchClassList()
  fetchStudentList()
})
</script>

<style scoped lang="scss">
.student-list-container {
  padding: 20px;
  min-height: calc(100vh - 60px);

  .search-card {
    margin-bottom: 20px;
  }

  .table-card {
    :deep(.el-table) {
      min-height: 400px;
    }
  }

  .upload-tip {
    margin-top: 10px;
    color: #909399;
    font-size: 12px;

    p {
      margin: 5px 0;
    }
  }
}
</style>

