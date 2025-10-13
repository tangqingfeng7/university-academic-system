<template>
  <div class="class-manage">
    <div class="page-header">
      <h2>班级管理</h2>
      <div class="header-actions">
        <el-button @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
          返回专业管理
        </el-button>
        <el-button type="success" @click="handleAutoAssign">
          <el-icon><MagicStick /></el-icon>
          自动分配辅导员
        </el-button>
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新建班级
        </el-button>
      </div>
    </div>

    <el-card>
      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-select
          v-model="queryParams.majorId"
          placeholder="选择专业"
          clearable
          filterable
          style="width: 200px"
          @change="fetchClassList"
        >
          <el-option
            v-for="major in majorOptions"
            :key="major.id"
            :label="major.name"
            :value="major.id"
          />
        </el-select>
        <el-select
          v-model="queryParams.enrollmentYear"
          placeholder="选择入学年份"
          clearable
          style="width: 150px"
          @change="fetchClassList"
        >
          <el-option
            v-for="year in yearOptions"
            :key="year"
            :label="year + '级'"
            :value="year"
          />
        </el-select>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>

      <el-table
        v-loading="loading"
        :data="classList.content"
        stripe
        border
      >
        <el-table-column prop="classCode" label="班级代码" width="120" />
        <el-table-column prop="className" label="班级名称" min-width="200" />
        <el-table-column prop="majorName" label="所属专业" width="180" />
        <el-table-column prop="departmentName" label="所属院系" width="150" />
        <el-table-column prop="enrollmentYear" label="入学年份" width="100">
          <template #default="{ row }">
            {{ row.enrollmentYear }}级
          </template>
        </el-table-column>
        <el-table-column prop="capacity" label="班级容量" width="100" />
        <el-table-column prop="counselorName" label="辅导员" width="120" />
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button 
              type="info" 
              size="small" 
              plain
              @click="handleViewStudents(row)"
            >
              查看学生
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
          :total="classList.totalElements"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="fetchClassList"
          @size-change="fetchClassList"
        />
      </div>
    </el-card>

    <!-- 新建/编辑班级对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="resetForm"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="班级代码" prop="classCode">
          <el-input 
            v-model="formData.classCode" 
            placeholder="如：2024CS01"
            :disabled="!!editingId"
          />
        </el-form-item>

        <el-form-item label="班级名称" prop="className">
          <el-input 
            v-model="formData.className" 
            placeholder="如：计算机科学与技术2024级1班"
          />
        </el-form-item>

        <el-form-item label="所属专业" prop="majorId">
          <el-select
            v-model="formData.majorId"
            placeholder="请选择专业"
            filterable
            style="width: 100%"
          >
            <el-option
              v-for="major in majorOptions"
              :key="major.id"
              :label="`${major.name} (${major.departmentName})`"
              :value="major.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="入学年份" prop="enrollmentYear">
          <el-select
            v-model="formData.enrollmentYear"
            placeholder="请选择入学年份"
            style="width: 100%"
          >
            <el-option
              v-for="year in yearOptions"
              :key="year"
              :label="year + '级'"
              :value="year"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="班级容量" prop="capacity">
          <el-input-number 
            v-model="formData.capacity" 
            :min="1"
            :max="200"
            placeholder="班级人数上限"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="辅导员" prop="counselorId">
          <el-select
            v-model="formData.counselorId"
            placeholder="请选择辅导员（可选）"
            clearable
            filterable
            style="width: 100%"
          >
            <el-option
              v-for="teacher in teacherOptions"
              :key="teacher.id"
              :label="teacher.name"
              :value="teacher.userId"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="备注" prop="remarks">
          <el-input 
            v-model="formData.remarks" 
            type="textarea"
            :rows="3"
            placeholder="备注信息"
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

    <!-- 查看学生对话框 -->
    <el-dialog
      v-model="studentDialogVisible"
      :title="`${currentClass?.className} - 学生列表`"
      width="800px"
    >
      <el-table
        v-loading="studentLoading"
        :data="studentList"
        stripe
        border
        max-height="500"
      >
        <el-table-column prop="studentNo" label="学号" width="120" />
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column prop="gender" label="性别" width="80" />
        <el-table-column prop="phone" label="联系电话" width="130" />
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.status === 'ACTIVE'" type="success">在读</el-tag>
            <el-tag v-else-if="row.status === 'SUSPENDED'" type="warning">休学</el-tag>
            <el-tag v-else-if="row.status === 'WITHDRAWN'" type="info">退学</el-tag>
            <el-tag v-else-if="row.status === 'GRADUATED'" type="primary">已毕业</el-tag>
            <el-tag v-else type="info">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="studentList.length === 0" style="text-align: center; padding: 40px; color: #999;">
        该班级暂无学生
      </div>

      <template #footer>
        <span>共 {{ studentList.length }} 名学生</span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, ArrowLeft, MagicStick } from '@element-plus/icons-vue'
import {
  getClassList,
  createClass,
  updateClass,
  deleteClass,
  checkClassCode,
  getClassStudents,
  autoAssignCounselors,
  autoAssignCounselorsConditional
} from '@/api/class'
import { getAllMajors } from '@/api/major'
import { getTeacherList } from '@/api/teacher'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const classList = ref({
  content: [],
  totalElements: 0,
  totalPages: 0
})
const majorOptions = ref([])
const teacherOptions = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const submitLoading = ref(false)
const formRef = ref(null)
const editingId = ref(null)

// 学生列表相关
const studentDialogVisible = ref(false)
const studentLoading = ref(false)
const studentList = ref([])
const currentClass = ref(null)

const queryParams = reactive({
  page: 0,
  size: 10,
  sortBy: 'enrollmentYear',
  direction: 'DESC',
  majorId: null,
  enrollmentYear: null
})

const formData = ref({
  classCode: '',
  className: '',
  majorId: null,
  enrollmentYear: null,
  capacity: 40,
  counselorId: null,
  remarks: ''
})

// 生成近10年的年份选项
const currentYear = new Date().getFullYear()
const yearOptions = Array.from({ length: 10 }, (_, i) => currentYear - i)

const validateClassCode = async (rule, value, callback) => {
  if (!value) {
    return callback(new Error('请输入班级代码'))
  }
  
  // 编辑时不检查
  if (editingId.value) {
    return callback()
  }
  
  try {
    const { data } = await checkClassCode(value)
    if (data.exists) {
      return callback(new Error('班级代码已存在'))
    }
    callback()
  } catch (error) {
    callback()
  }
}

const formRules = {
  classCode: [
    { required: true, validator: validateClassCode, trigger: 'blur' }
  ],
  className: [
    { required: true, message: '请输入班级名称', trigger: 'blur' }
  ],
  majorId: [
    { required: true, message: '请选择所属专业', trigger: 'change' }
  ],
  enrollmentYear: [
    { required: true, message: '请选择入学年份', trigger: 'change' }
  ],
  capacity: [
    { required: true, message: '请输入班级容量', trigger: 'blur' }
  ]
}

// 返回专业管理
const goBack = () => {
  router.push('/admin/majors')
}

// 查看学生列表
const handleViewStudents = async (row) => {
  currentClass.value = row
  studentDialogVisible.value = true
  studentLoading.value = true
  
  try {
    const { data } = await getClassStudents(row.id)
    studentList.value = data || []
  } catch (error) {
    console.error('获取学生列表失败:', error)
    ElMessage.error('获取学生列表失败')
  } finally {
    studentLoading.value = false
  }
}

// 获取专业列表
const fetchMajorList = async () => {
  try {
    const { data } = await getAllMajors()
    majorOptions.value = data || []
  } catch (error) {
    console.error('获取专业列表失败:', error)
  }
}

// 获取教师列表
const fetchTeacherList = async () => {
  try {
    const { data } = await getTeacherList({ page: 0, size: 1000 })
    teacherOptions.value = data.content || []
  } catch (error) {
    console.error('获取教师列表失败:', error)
  }
}

// 获取班级列表
const fetchClassList = async () => {
  try {
    loading.value = true
    const params = {
      ...queryParams,
      majorId: queryParams.majorId || undefined,
      enrollmentYear: queryParams.enrollmentYear || undefined
    }
    const { data } = await getClassList(params)
    classList.value = data
  } catch (error) {
    console.error('获取班级列表失败:', error)
    ElMessage.error('获取班级列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  queryParams.page = 0
  fetchClassList()
}

// 重置搜索
const handleReset = () => {
  queryParams.majorId = null
  queryParams.enrollmentYear = null
  queryParams.page = 0
  fetchClassList()
}

// 新建班级
const handleAdd = () => {
  dialogTitle.value = '新建班级'
  editingId.value = null
  dialogVisible.value = true
}

// 编辑班级
const handleEdit = (row) => {
  dialogTitle.value = '编辑班级'
  editingId.value = row.id
  formData.value = {
    classCode: row.classCode,
    className: row.className,
    majorId: row.majorId,
    enrollmentYear: row.enrollmentYear,
    capacity: row.capacity,
    counselorId: row.counselorId,
    remarks: row.remarks
  }
  dialogVisible.value = true
}

// 删除班级
const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定要删除班级"${row.className}"吗？删除后该班级的学生将无法关联到该班级。`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await deleteClass(row.id)
      ElMessage.success('删除成功')
      fetchClassList()
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
      await updateClass(editingId.value, formData.value)
      ElMessage.success('更新成功')
    } else {
      await createClass(formData.value)
      ElMessage.success('创建成功')
    }
    
    dialogVisible.value = false
    fetchClassList()
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
    classCode: '',
    className: '',
    majorId: null,
    enrollmentYear: null,
    capacity: 40,
    counselorId: null,
    remarks: ''
  }
  editingId.value = null
}

// 自动分配辅导员
const handleAutoAssign = () => {
  ElMessageBox.confirm(
    '是否为所有未分配辅导员的班级自动分配？系统将根据负载均衡原则，为每个院系的班级分配该院系的教师作为辅导员。',
    '自动分配辅导员',
    {
      confirmButtonText: '全局分配',
      cancelButtonText: '取消',
      type: 'info',
      distinguishCancelAndClose: true,
      showCancelButton: true,
      showClose: true,
      beforeClose: (action, instance, done) => {
        if (action === 'confirm') {
          performAutoAssign(null, null)
          done()
        } else if (action === 'cancel') {
          done()
        } else {
          done()
        }
      }
    }
  ).then(() => {
    // 全局分配
  }).catch(() => {
    // 取消或关闭，询问是否按条件分配
    if (queryParams.majorId || queryParams.enrollmentYear) {
      ElMessageBox.confirm(
        '是否仅为当前筛选条件的班级自动分配辅导员？',
        '条件分配',
        {
          confirmButtonText: '条件分配',
          cancelButtonText: '取消',
          type: 'info'
        }
      ).then(() => {
        performAutoAssign(queryParams.majorId, queryParams.enrollmentYear)
      }).catch(() => {})
    }
  })
}

// 执行自动分配
const performAutoAssign = async (majorId, enrollmentYear) => {
  const loadingInstance = ElMessage({
    message: '正在自动分配辅导员...',
    type: 'info',
    duration: 0
  })
  
  try {
    let result
    if (majorId || enrollmentYear) {
      // 条件分配
      const { data } = await autoAssignCounselorsConditional({ majorId, enrollmentYear })
      result = data
    } else {
      // 全局分配
      const { data } = await autoAssignCounselors()
      result = data
    }
    
    loadingInstance.close()
    
    ElMessageBox.alert(
      `<div style="line-height: 1.8;">
        <p><strong>分配完成</strong></p>
        <p>待分配班级数：<strong>${result.totalClasses}</strong></p>
        <p>成功分配班级数：<strong style="color: #67C23A;">${result.assignedCount}</strong></p>
        <p>未分配班级数：<strong style="color: #E6A23C;">${result.unassignedCount}</strong></p>
        ${result.unassignedCount > 0 ? '<p style="color: #E6A23C; font-size: 12px;">未分配的班级可能是因为对应院系没有可用的教师</p>' : ''}
      </div>`,
      '分配结果',
      {
        dangerouslyUseHTMLString: true,
        confirmButtonText: '确定',
        callback: () => {
          fetchClassList()
        }
      }
    )
  } catch (error) {
    loadingInstance.close()
    console.error('自动分配失败:', error)
    ElMessage.error(error.response?.data?.message || '自动分配失败')
  }
}

onMounted(() => {
  // 如果从专业管理页面传入了专业ID，则预设筛选条件
  if (route.query.majorId) {
    queryParams.majorId = parseInt(route.query.majorId)
  }
  
  fetchMajorList()
  fetchTeacherList()
  fetchClassList()
})
</script>

<style scoped>
.class-manage {
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

.header-actions {
  display: flex;
  gap: 10px;
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

