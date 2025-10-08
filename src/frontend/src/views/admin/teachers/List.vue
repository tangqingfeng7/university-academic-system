<template>
  <div class="teacher-list-container">
    <el-card class="search-card">
      <search-form
        :fields="searchFields"
        @search="handleSearch"
        @reset="handleReset"
      >
        <template #extra-buttons>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            添加教师
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

    <!-- 添加/编辑教师对话框 -->
    <teacher-form
      v-model="formVisible"
      :teacher-id="currentTeacherId"
      :mode="formMode"
      @success="handleFormSuccess"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import DataTable from '@/components/DataTable.vue'
import SearchForm from '@/components/SearchForm.vue'
import TeacherForm from './TeacherForm.vue'
import { 
  getTeacherList, 
  deleteTeacher,
  checkHasOfferings
} from '@/api/teacher'
import { getAllDepartments } from '@/api/system'
import { useDebounceFn } from '@/utils/debounce'

// 搜索条件
const searchFields = ref([
  {
    prop: 'teacherNo',
    label: '工号',
    type: 'input',
    placeholder: '请输入工号'
  },
  {
    prop: 'name',
    label: '姓名',
    type: 'input',
    placeholder: '请输入姓名'
  },
  {
    prop: 'departmentId',
    label: '院系',
    type: 'select',
    placeholder: '请选择院系',
    options: []
  },
  {
    prop: 'title',
    label: '职称',
    type: 'select',
    placeholder: '请选择职称',
    options: [
      { label: '教授', value: '教授' },
      { label: '副教授', value: '副教授' },
      { label: '讲师', value: '讲师' },
      { label: '助教', value: '助教' }
    ]
  }
])

// 表格列配置
const columns = [
  { prop: 'teacherNo', label: '工号', width: 120 },
  { prop: 'name', label: '姓名', width: 100 },
  { prop: 'gender', label: '性别', width: 80, slot: 'gender' },
  { prop: 'title', label: '职称', width: 120 },
  { prop: 'departmentName', label: '所属院系', width: 150 },
  { prop: 'phone', label: '联系电话', width: 130 },
  { prop: 'email', label: '邮箱', minWidth: 180 },
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
  keyword: '',
  departmentId: null
})

// 表单相关
const formVisible = ref(false)
const formMode = ref('add') // add | edit
const currentTeacherId = ref(null)

// 获取教师列表
const fetchTeacherList = async () => {
  loading.value = true
  try {
    const params = {
      page: queryParams.page - 1, // 后端从0开始
      size: queryParams.size
    }
    
    if (queryParams.keyword) params.keyword = queryParams.keyword
    if (queryParams.departmentId) params.departmentId = queryParams.departmentId
    
    const res = await getTeacherList(params)
    tableData.value = res.data.content
    total.value = res.data.totalElements
  } catch (error) {
    console.error('获取教师列表失败:', error)
    ElMessage.error('获取教师列表失败')
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

// 搜索（使用防抖优化）
const handleSearchInternal = (searchData) => {
  // 将搜索字段合并为keyword
  const { teacherNo, name, departmentId, title } = searchData
  let keyword = ''
  if (teacherNo) keyword = teacherNo
  else if (name) keyword = name
  else if (title) keyword = title
  
  queryParams.keyword = keyword
  queryParams.departmentId = departmentId || null
  queryParams.page = 1
  fetchTeacherList()
}

// 使用防抖包装搜索函数
const handleSearch = useDebounceFn(handleSearchInternal, 500)

// 重置
const handleReset = () => {
  queryParams.page = 1
  queryParams.size = 10
  queryParams.keyword = ''
  queryParams.departmentId = null
  fetchTeacherList()
}

// 分页
const handlePageChange = (pageInfo) => {
  if (typeof pageInfo === 'object') {
    queryParams.page = pageInfo.page
    queryParams.size = pageInfo.size
  } else {
    queryParams.page = pageInfo
  }
  fetchTeacherList()
}

const handleSizeChange = (size) => {
  queryParams.size = size
  queryParams.page = 1
  fetchTeacherList()
}

// 添加教师
const handleAdd = () => {
  formMode.value = 'add'
  currentTeacherId.value = null
  formVisible.value = true
}

// 编辑教师
const handleEdit = (row) => {
  formMode.value = 'edit'
  currentTeacherId.value = row.id
  formVisible.value = true
}

// 删除教师
const handleDelete = async (row) => {
  try {
    // 先检查是否有授课任务
    const checkRes = await checkHasOfferings(row.id)
    if (checkRes.data.hasOfferings) {
      await ElMessageBox.alert(
        `教师 ${row.name}（${row.teacherNo}）当前有授课任务，无法删除。请先取消该教师的所有授课任务。`,
        '无法删除',
        {
          confirmButtonText: '确定',
          type: 'warning'
        }
      )
      return
    }

    await ElMessageBox.confirm(
      `确定要删除教师 ${row.name}（${row.teacherNo}）吗？`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await deleteTeacher(row.id)
    ElMessage.success('删除成功')
    fetchTeacherList()
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
  fetchTeacherList()
}

onMounted(() => {
  fetchDepartmentList()
  fetchTeacherList()
})
</script>

<style scoped lang="scss">
.teacher-list-container {
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

