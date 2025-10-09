<template>
  <div class="exam-list-container">
    <el-card class="search-card">
      <search-form
        :fields="searchFields"
        @search="handleSearch"
        @reset="handleReset"
      >
        <template #extra-buttons>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            创建考试
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
        <!-- 考试类型插槽 -->
        <template #type="{ row }">
          <exam-type-tag :type="row.type" :typeDescription="row.typeDescription" />
        </template>

        <!-- 考试时间插槽 -->
        <template #examTime="{ row }">
          <exam-time-display 
            :examTime="row.examTime" 
            :duration="row.duration" 
            compact 
          />
        </template>

        <!-- 考试状态插槽 -->
        <template #status="{ row }">
          <exam-status-tag :status="row.status" :statusDescription="row.statusDescription" />
        </template>

        <!-- 考场情况插槽 -->
        <template #rooms="{ row }">
          <div class="room-info">
            <div class="room-count">
              <el-icon><OfficeBuilding /></el-icon>
              <span>{{ row.totalRooms || 0 }} 个考场</span>
            </div>
            <div class="student-count">
              <el-icon><User /></el-icon>
              <span>{{ row.totalStudents || 0 }} 人</span>
            </div>
          </div>
        </template>

        <!-- 操作按钮插槽 -->
        <template #operation="{ row }">
          <el-button 
            link 
            type="primary" 
            @click="handleView(row)"
          >
            详情
          </el-button>
          <el-button 
            link 
            type="primary" 
            @click="handleEdit(row)"
            v-if="row.status === 'DRAFT'"
          >
            编辑
          </el-button>
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
          <el-button 
            link 
            type="danger" 
            @click="handleDelete(row)"
            v-if="row.status === 'DRAFT'"
          >
            删除
          </el-button>
          <el-button 
            link 
            type="info" 
            @click="handleExport(row)"
            v-if="row.status !== 'DRAFT'"
          >
            导出
          </el-button>
        </template>
      </data-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onActivated } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, OfficeBuilding, User } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import DataTable from '@/components/DataTable.vue'
import SearchForm from '@/components/SearchForm.vue'
import ExamStatusTag from '@/components/ExamStatusTag.vue'
import ExamTypeTag from '@/components/ExamTypeTag.vue'
import ExamTimeDisplay from '@/components/ExamTimeDisplay.vue'
import { useDebounceFn } from '@/utils/debounce'
import { useExamStore } from '@/stores/exam'
import { 
  getExamList, 
  deleteExam,
  publishExam,
  cancelExam,
  exportExamList,
  downloadExamList
} from '@/api/exam'
import { getAllSemesters } from '@/api/semester'

const router = useRouter()
const examStore = useExamStore()

// 搜索条件配置
const searchFields = ref([
  {
    prop: 'semesterId',
    label: '学期',
    type: 'select',
    placeholder: '请选择学期',
    options: []
  },
  {
    prop: 'courseNo',
    label: '课程编号',
    type: 'input',
    placeholder: '请输入课程编号'
  },
  {
    prop: 'courseName',
    label: '课程名称',
    type: 'input',
    placeholder: '请输入课程名称'
  },
  {
    prop: 'type',
    label: '考试类型',
    type: 'select',
    placeholder: '请选择类型',
    options: [
      { label: '期中考试', value: 'MIDTERM' },
      { label: '期末考试', value: 'FINAL' },
      { label: '补考', value: 'MAKEUP' },
      { label: '重修考试', value: 'RETAKE' }
    ]
  },
  {
    prop: 'status',
    label: '考试状态',
    type: 'select',
    placeholder: '请选择状态',
    options: [
      { label: '草稿', value: 'DRAFT' },
      { label: '已发布', value: 'PUBLISHED' },
      { label: '进行中', value: 'IN_PROGRESS' },
      { label: '已结束', value: 'FINISHED' },
      { label: '已取消', value: 'CANCELLED' }
    ]
  }
])

// 表格列定义
const columns = ref([
  { prop: 'id', label: 'ID', width: '80' },
  { prop: 'name', label: '考试名称', minWidth: '180' },
  { prop: 'type', label: '类型', width: '120', slot: 'type' },
  { 
    prop: 'courseOffering.course.name', 
    label: '课程', 
    minWidth: '160',
    formatter: (row) => row.courseOffering?.course?.name || '-'
  },
  { 
    prop: 'courseOffering.semester.semesterName', 
    label: '学期', 
    width: '150',
    formatter: (row) => row.courseOffering?.semester?.semesterName || '-'
  },
  { prop: 'examTime', label: '考试时间', width: '200', slot: 'examTime' },
  { prop: 'totalScore', label: '总分', width: '80' },
  { prop: 'rooms', label: '考场情况', width: '140', slot: 'rooms' },
  { prop: 'status', label: '状态', width: '100', slot: 'status' },
  { prop: 'operation', label: '操作', width: '280', slot: 'operation', fixed: 'right' }
])

// 表格数据
const tableData = ref([])
const total = ref(0)
const loading = ref(false)

// 查询参数
const queryParams = reactive({
  page: 1,
  size: 10,
  semesterId: null,
  courseNo: null,
  courseName: null,
  type: null,
  status: null,
  sortBy: 'examTime',
  sortDirection: 'ASC'
})

// ==================== 数据加载 ====================

// 获取考试列表
const fetchExamList = async () => {
  loading.value = true
  try {
    // 确保 page 和 size 是有效的数字
    const page = parseInt(queryParams.page) || 1
    const size = parseInt(queryParams.size) || 10
    
    const params = {
      page: page - 1, // 后端从0开始
      size: size,
      sortBy: queryParams.sortBy,
      sortDirection: queryParams.sortDirection
    }
    
    if (queryParams.semesterId) params.semesterId = queryParams.semesterId
    if (queryParams.courseNo) params.courseNo = queryParams.courseNo
    if (queryParams.courseName) params.courseName = queryParams.courseName
    if (queryParams.type) params.type = queryParams.type
    if (queryParams.status) params.status = queryParams.status
    
    const res = await getExamList(params)
    tableData.value = res.data.content
    total.value = res.data.totalElements
    
    // 更新缓存
    examStore.updateExamListCache(
      res.data.content,
      res.data.totalElements,
      page,
      size,
      {
        semesterId: queryParams.semesterId,
        courseNo: queryParams.courseNo,
        courseName: queryParams.courseName,
        type: queryParams.type,
        status: queryParams.status
      }
    )
  } catch (error) {
    console.error('获取考试列表失败:', error)
    ElMessage.error('获取考试列表失败')
  } finally {
    loading.value = false
  }
}

// 防抖搜索
const debouncedFetchExamList = useDebounceFn(fetchExamList, 500)

// 获取学期列表
const fetchSemesterList = async () => {
  try {
    const res = await getAllSemesters()
    const semesterField = searchFields.value.find(f => f.prop === 'semesterId')
    if (semesterField) {
      semesterField.options = res.data.map(semester => ({
        label: semester.semesterName,
        value: semester.id
      }))
    }
  } catch (error) {
    console.error('获取学期列表失败:', error)
  }
}

// ==================== 搜索和分页 ====================

// 搜索（使用防抖）
const handleSearch = (searchData) => {
  queryParams.semesterId = searchData.semesterId || null
  queryParams.courseNo = searchData.courseNo || null
  queryParams.courseName = searchData.courseName || null
  queryParams.type = searchData.type || null
  queryParams.status = searchData.status || null
  queryParams.page = 1
  fetchExamList()
}

// 重置
const handleReset = () => {
  queryParams.semesterId = null
  queryParams.courseNo = null
  queryParams.courseName = null
  queryParams.type = null
  queryParams.status = null
  queryParams.page = 1
  fetchExamList()
}

// 分页改变
const handlePageChange = (pageInfo) => {
  // DataTable 发射的是对象 { page, size }
  const page = typeof pageInfo === 'object' ? pageInfo.page : pageInfo
  queryParams.page = parseInt(page) || 1
  fetchExamList()
}

// 每页条数改变
const handleSizeChange = (sizeInfo) => {
  // DataTable 发射的是对象 { page, size }
  const size = typeof sizeInfo === 'object' ? sizeInfo.size : sizeInfo
  queryParams.size = parseInt(size) || 10
  queryParams.page = 1
  fetchExamList()
}

// ==================== 操作按钮 ====================

// 创建考试
const handleAdd = () => {
  router.push('/admin/exams/create')
}

// 查看详情
const handleView = (row) => {
  router.push(`/admin/exams/detail/${row.id}`)
}

// 编辑考试
const handleEdit = (row) => {
  router.push(`/admin/exams/edit/${row.id}`)
}

// 发布考试
const handlePublish = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要发布考试"${row.name}"吗？发布后将通知所有相关学生和教师。`,
      '发布考试',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await publishExam(row.id)
    ElMessage.success('考试发布成功')
    fetchExamList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('发布考试失败:', error)
      ElMessage.error(error.message || '发布考试失败')
    }
  }
}

// 取消考试
const handleCancel = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要取消考试"${row.name}"吗？取消后将通知所有相关人员。`,
      '取消考试',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await cancelExam(row.id)
    ElMessage.success('考试已取消')
    fetchExamList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消考试失败:', error)
      ElMessage.error(error.message || '取消考试失败')
    }
  }
}

// 删除考试
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除考试"${row.name}"吗？此操作不可恢复。`,
      '删除考试',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'danger'
      }
    )
    
    await deleteExam(row.id)
    ElMessage.success('考试删除成功')
    fetchExamList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除考试失败:', error)
      ElMessage.error(error.message || '删除考试失败')
    }
  }
}

// 导出考试安排
const handleExport = async (row) => {
  try {
    ElMessage.info('正在生成导出文件...')
    await downloadExamList([row.id])
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error(error.message || '导出失败')
  }
}

// ==================== 生命周期 ====================

onMounted(() => {
  fetchSemesterList()
  
  // 首次加载时尝试从缓存恢复
  if (examStore.isExamListCacheValid) {
    const cache = examStore.examListCache
    tableData.value = cache.data
    total.value = cache.total
    queryParams.page = parseInt(cache.page) || 1
    queryParams.size = parseInt(cache.size) || 10
    Object.assign(queryParams, cache.filters)
  } else {
    fetchExamList()
  }
})

// Keep-alive组件激活时
onActivated(() => {
  // 如果缓存有效，使用缓存数据
  if (examStore.isExamListCacheValid) {
    const cache = examStore.examListCache
    tableData.value = cache.data
    total.value = cache.total
  } else {
    // 缓存失效，重新加载
    fetchExamList()
  }
})
</script>

<style scoped>
.exam-list-container {
  padding: 20px;
}

.search-card {
  margin-bottom: 20px;
}

.table-card {
  margin-bottom: 20px;
}

.room-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.room-count,
.student-count {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
}

.room-count .el-icon,
.student-count .el-icon {
  color: var(--el-color-primary);
}
</style>

