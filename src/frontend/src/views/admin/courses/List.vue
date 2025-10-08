<template>
  <div class="course-list-container">
    <el-card class="search-card">
      <search-form
        :fields="searchFields"
        @search="handleSearch"
        @reset="handleReset"
      >
        <template #extra-buttons>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            添加课程
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
        :page="queryParams.page"
        :size="queryParams.size"
        @page-change="handlePageChange"
        @size-change="handleSizeChange"
      >
        <template #type="{ row }">
          <el-tag :type="getCourseTypeTag(row.type)">
            {{ row.typeDescription }}
          </el-tag>
        </template>
        <template #operation="{ row }">
          <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button link type="success" @click="handlePrerequisites(row)">先修课程</el-button>
          <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </data-table>
    </el-card>

    <!-- 添加/编辑课程对话框 -->
    <course-form
      v-model="formVisible"
      :course-id="currentCourseId"
      :mode="formMode"
      @success="handleFormSuccess"
    />

    <!-- 先修课程对话框 -->
    <prerequisite-dialog
      v-model="prerequisiteVisible"
      :course-id="currentCourseId"
      :course-name="currentCourseName"
      @success="handlePrerequisiteSuccess"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import DataTable from '@/components/DataTable.vue'
import SearchForm from '@/components/SearchForm.vue'
import CourseForm from './CourseForm.vue'
import PrerequisiteDialog from './PrerequisiteDialog.vue'
import { 
  getCourseList, 
  deleteCourse
} from '@/api/course'
import { getAllDepartments } from '@/api/system'

// 搜索条件
const searchFields = ref([
  {
    prop: 'courseNo',
    label: '课程编号',
    type: 'input',
    placeholder: '请输入课程编号'
  },
  {
    prop: 'name',
    label: '课程名称',
    type: 'input',
    placeholder: '请输入课程名称'
  },
  {
    prop: 'departmentId',
    label: '开课院系',
    type: 'select',
    placeholder: '请选择院系',
    options: []
  },
  {
    prop: 'type',
    label: '课程类型',
    type: 'select',
    placeholder: '请选择类型',
    options: [
      { label: '必修课', value: 'REQUIRED' },
      { label: '选修课', value: 'ELECTIVE' },
      { label: '公共课', value: 'PUBLIC' }
    ]
  }
])

// 表格列配置
const columns = [
  { prop: 'courseNo', label: '课程编号', width: 120 },
  { prop: 'name', label: '课程名称', width: 180 },
  { prop: 'credits', label: '学分', width: 80 },
  { prop: 'hours', label: '学时', width: 80 },
  { prop: 'type', label: '课程类型', width: 100, slot: 'type' },
  { prop: 'departmentName', label: '开课院系', width: 150 },
  { prop: 'description', label: '课程简介', minWidth: 200, showOverflowTooltip: true },
  { prop: 'operation', label: '操作', width: 220, fixed: 'right', slot: 'operation' }
]

// 表格数据
const tableData = ref([])
const total = ref(0)
const loading = ref(false)

// 查询参数
const queryParams = reactive({
  page: 1,
  size: 10,
  keyword: '',
  departmentId: null,
  type: null
})

// 表单相关
const formVisible = ref(false)
const formMode = ref('add') // add | edit
const currentCourseId = ref(null)

// 先修课程相关
const prerequisiteVisible = ref(false)
const currentCourseName = ref('')

// 获取课程列表
const fetchCourseList = async () => {
  loading.value = true
  try {
    const params = {
      page: queryParams.page - 1, // 后端从0开始
      size: queryParams.size
    }
    
    if (queryParams.keyword) params.keyword = queryParams.keyword
    if (queryParams.departmentId) params.departmentId = queryParams.departmentId
    if (queryParams.type) params.type = queryParams.type
    
    const res = await getCourseList(params)
    tableData.value = res.data.content
    total.value = res.data.totalElements
  } catch (error) {
    console.error('获取课程列表失败:', error)
    ElMessage.error('获取课程列表失败')
  } finally {
    loading.value = false
  }
}

// 获取院系列表（用于搜索下拉）
const fetchDepartmentList = async () => {
  try {
    const res = await getAllDepartments()
    const departmentField = searchFields.value.find(f => f.prop === 'departmentId')
    if (departmentField) {
      departmentField.options = res.data.map(dept => ({
        label: dept.name,
        value: dept.id
      }))
    }
  } catch (error) {
    console.error('获取院系列表失败:', error)
  }
}

// 搜索
const handleSearch = (searchData) => {
  // 将课程编号或名称合并为keyword
  const { courseNo, name, departmentId, type } = searchData
  let keyword = ''
  if (courseNo) keyword = courseNo
  else if (name) keyword = name
  
  queryParams.keyword = keyword
  queryParams.departmentId = departmentId || null
  queryParams.type = type || null
  queryParams.page = 1
  fetchCourseList()
}

// 重置
const handleReset = () => {
  queryParams.page = 1
  queryParams.size = 10
  queryParams.keyword = ''
  queryParams.departmentId = null
  queryParams.type = null
  fetchCourseList()
}

// 分页
const handlePageChange = (pageInfo) => {
  if (typeof pageInfo === 'object') {
    queryParams.page = pageInfo.page
    queryParams.size = pageInfo.size
  } else {
    queryParams.page = pageInfo
  }
  fetchCourseList()
}

const handleSizeChange = (size) => {
  queryParams.size = size
  queryParams.page = 1
  fetchCourseList()
}

// 添加课程
const handleAdd = () => {
  formMode.value = 'add'
  currentCourseId.value = null
  formVisible.value = true
}

// 编辑课程
const handleEdit = (row) => {
  formMode.value = 'edit'
  currentCourseId.value = row.id
  formVisible.value = true
}

// 设置先修课程
const handlePrerequisites = (row) => {
  currentCourseId.value = row.id
  currentCourseName.value = row.name
  prerequisiteVisible.value = true
}

// 删除课程
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除课程 ${row.name}（${row.courseNo}）吗？`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await deleteCourse(row.id)
    ElMessage.success('删除成功')
    fetchCourseList()
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
  fetchCourseList()
}

// 先修课程设置成功
const handlePrerequisiteSuccess = () => {
  prerequisiteVisible.value = false
}

// 获取课程类型标签颜色
const getCourseTypeTag = (type) => {
  const typeMap = {
    'REQUIRED': 'danger',
    'ELECTIVE': 'success',
    'PUBLIC': 'warning'
  }
  return typeMap[type] || ''
}

onMounted(() => {
  fetchDepartmentList()
  fetchCourseList()
})
</script>

<style scoped lang="scss">
.course-list-container {
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
}
</style>

