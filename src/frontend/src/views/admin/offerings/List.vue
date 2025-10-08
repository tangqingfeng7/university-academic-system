<template>
  <div class="offering-list-container">
    <el-card class="search-card">
      <search-form
        :fields="searchFields"
        @search="handleSearch"
        @reset="handleReset"
      >
        <template #extra-buttons>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            添加开课计划
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
        <template #status="{ row }">
          <el-tag :type="getStatusTag(row.status)">
            {{ row.statusDescription }}
          </el-tag>
        </template>
        <template #enrollment="{ row }">
          <span>{{ row.enrolled || 0 }} / {{ row.capacity }}</span>
          <el-progress 
            :percentage="getEnrollmentPercentage(row)" 
            :stroke-width="6"
            :show-text="false"
            :color="getProgressColor(row)"
            style="margin-top: 4px;"
          />
        </template>
        <template #operation="{ row }">
          <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button 
            link 
            type="success" 
            @click="handlePublish(row)"
            v-if="row.status === 'DRAFT'"
          >
            发布
          </el-button>
          <el-button 
            link 
            type="warning" 
            @click="handleCancel(row)"
            v-if="row.status === 'PUBLISHED'"
          >
            取消
          </el-button>
          <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </data-table>
    </el-card>

    <!-- 添加/编辑开课计划对话框 -->
    <offering-form
      v-model="formVisible"
      :offering-id="currentOfferingId"
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
import OfferingForm from './OfferingForm.vue'
import { 
  getOfferingList, 
  deleteOffering,
  publishOffering,
  cancelOffering
} from '@/api/offering'
import { getAllSemesters } from '@/api/semester'
import { getAllCourses } from '@/api/course'
import { getAllTeachers } from '@/api/teacher'

// 搜索条件
const searchFields = ref([
  {
    prop: 'semesterId',
    label: '学期',
    type: 'select',
    placeholder: '请选择学期',
    options: []
  },
  {
    prop: 'courseId',
    label: '课程',
    type: 'select',
    placeholder: '请选择课程',
    options: [],
    filterable: true
  },
  {
    prop: 'teacherId',
    label: '教师',
    type: 'select',
    placeholder: '请选择教师',
    options: [],
    filterable: true
  }
])

// 表格列定义
const columns = ref([
  { prop: 'id', label: 'ID', width: '80' },
  { prop: 'semesterName', label: '学期', width: '150' },
  { prop: 'courseName', label: '课程', minWidth: '200' },
  { prop: 'teacherName', label: '授课教师', width: '120' },
  { prop: 'schedule', label: '上课时间', minWidth: '180' },
  { prop: 'location', label: '上课地点', width: '120' },
  { prop: 'enrollment', label: '选课人数', width: '140', slot: 'enrollment' },
  { prop: 'status', label: '状态', width: '100', slot: 'status' },
  { prop: 'operation', label: '操作', width: '220', slot: 'operation', fixed: 'right' }
])

// 数据
const tableData = ref([])
const total = ref(0)
const loading = ref(false)

// 查询参数
const queryParams = reactive({
  page: 1,
  size: 10,
  semesterId: null,
  courseId: null,
  teacherId: null
})

// 表单相关
const formVisible = ref(false)
const formMode = ref('add')
const currentOfferingId = ref(null)

// 获取开课计划列表
const fetchOfferingList = async () => {
  loading.value = true
  try {
    const params = {
      page: queryParams.page - 1,
      size: queryParams.size
    }
    
    if (queryParams.semesterId) params.semesterId = queryParams.semesterId
    if (queryParams.courseId) params.courseId = queryParams.courseId
    if (queryParams.teacherId) params.teacherId = queryParams.teacherId
    
    const res = await getOfferingList(params)
    tableData.value = res.data.content
    total.value = res.data.totalElements
  } catch (error) {
    console.error('获取开课计划列表失败:', error)
    ElMessage.error('获取开课计划列表失败')
  } finally {
    loading.value = false
  }
}

// 获取学期列表（用于搜索下拉）
const fetchSemesterList = async () => {
  try {
    const res = await getAllSemesters()
    const semesterField = searchFields.value.find(f => f.prop === 'semesterId')
    if (semesterField) {
      semesterField.options = res.data.map(semester => ({
        label: `${semester.academicYear} ${semester.semesterType === 1 ? '春季' : '秋季'}`,
        value: semester.id
      }))
    }
  } catch (error) {
    console.error('获取学期列表失败:', error)
  }
}

// 获取课程列表（用于搜索下拉）
const fetchCourseList = async () => {
  try {
    const res = await getAllCourses()
    const courseField = searchFields.value.find(f => f.prop === 'courseId')
    if (courseField) {
      courseField.options = res.data.map(course => ({
        label: `${course.courseNo} - ${course.name}`,
        value: course.id
      }))
    }
  } catch (error) {
    console.error('获取课程列表失败:', error)
  }
}

// 获取教师列表（用于搜索下拉）
const fetchTeacherList = async () => {
  try {
    const res = await getAllTeachers()
    const teacherField = searchFields.value.find(f => f.prop === 'teacherId')
    if (teacherField) {
      teacherField.options = res.data.map(teacher => ({
        label: `${teacher.teacherNo} - ${teacher.name}`,
        value: teacher.id
      }))
    }
  } catch (error) {
    console.error('获取教师列表失败:', error)
  }
}

// 搜索
const handleSearch = (searchData) => {
  queryParams.semesterId = searchData.semesterId || null
  queryParams.courseId = searchData.courseId || null
  queryParams.teacherId = searchData.teacherId || null
  queryParams.page = 1
  fetchOfferingList()
}

// 重置
const handleReset = () => {
  queryParams.page = 1
  queryParams.size = 10
  queryParams.semesterId = null
  queryParams.courseId = null
  queryParams.teacherId = null
  fetchOfferingList()
}

// 分页
const handlePageChange = (pageData) => {
  if (typeof pageData === 'object') {
    queryParams.page = pageData.page
    queryParams.size = pageData.size
  } else {
    queryParams.page = pageData
  }
  fetchOfferingList()
}

const handleSizeChange = (size) => {
  queryParams.size = size
  queryParams.page = 1
  fetchOfferingList()
}

// 添加
const handleAdd = () => {
  formMode.value = 'add'
  currentOfferingId.value = null
  formVisible.value = true
}

// 编辑
const handleEdit = (row) => {
  formMode.value = 'edit'
  currentOfferingId.value = row.id
  formVisible.value = true
}

// 删除
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除开课计划"${row.courseName}"吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await deleteOffering(row.id)
    ElMessage.success('删除成功')
    fetchOfferingList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除开课计划失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 发布
const handlePublish = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要发布开课计划"${row.courseName}"吗？发布后学生将可以选课。`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }
    )
    
    await publishOffering(row.id)
    ElMessage.success('发布成功')
    fetchOfferingList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('发布开课计划失败:', error)
      ElMessage.error('发布失败')
    }
  }
}

// 取消
const handleCancel = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要取消开课计划"${row.courseName}"吗？已选课的学生将被退课。`,
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await cancelOffering(row.id)
    ElMessage.success('取消成功')
    fetchOfferingList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消开课计划失败:', error)
      ElMessage.error('取消失败')
    }
  }
}

// 表单提交成功回调
const handleFormSuccess = () => {
  fetchOfferingList()
}

// 状态标签
const getStatusTag = (status) => {
  const statusMap = {
    'DRAFT': '',
    'PUBLISHED': 'success',
    'CANCELLED': 'info'
  }
  return statusMap[status] || ''
}

// 选课进度百分比
const getEnrollmentPercentage = (row) => {
  if (!row.capacity) return 0
  return Math.round((row.enrolled || 0) / row.capacity * 100)
}

// 进度条颜色
const getProgressColor = (row) => {
  const percentage = getEnrollmentPercentage(row)
  if (percentage >= 100) return '#f56c6c'
  if (percentage >= 80) return '#e6a23c'
  return '#67c23a'
}

// 初始化
onMounted(() => {
  fetchSemesterList()
  fetchCourseList()
  fetchTeacherList()
  fetchOfferingList()
})
</script>

<style scoped>
.offering-list-container {
  padding: 20px;
}

.search-card {
  margin-bottom: 20px;
}

.table-card {
  min-height: 400px;
}
</style>

