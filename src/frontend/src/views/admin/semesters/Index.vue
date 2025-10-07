<template>
  <div class="semester-manage">
    <div class="page-header">
      <h2>学期管理</h2>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新建学期
      </el-button>
    </div>

    <el-card>
      <el-table
        v-loading="loading"
        :data="semesterList"
        stripe
        border
      >
        <el-table-column prop="academicYear" label="学年" width="120" />
        <el-table-column label="学期" width="100">
          <template #default="{ row }">
            {{ row.semesterType === 1 ? '春季' : '秋季' }}
          </template>
        </el-table-column>
        <el-table-column prop="startDate" label="开始日期" width="120" />
        <el-table-column prop="endDate" label="结束日期" width="120" />
        <el-table-column label="选课时间" width="240">
          <template #default="{ row }">
            {{ row.courseSelectionStart }} 至 {{ row.courseSelectionEnd }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.active" type="success">当前学期</el-tag>
            <el-tag v-else type="info">非活动</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button 
              v-if="!row.active" 
              type="primary" 
              size="small" 
              @click="handleActivate(row)"
            >
              设为当前
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
    </el-card>

    <!-- 新建/编辑学期对话框 -->
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
        label-width="120px"
      >
        <el-form-item label="学年" prop="academicYear">
          <el-input 
            v-model="formData.academicYear" 
            placeholder="例如：2024-2025"
          />
        </el-form-item>

        <el-form-item label="学期类型" prop="semesterType">
          <el-radio-group v-model="formData.semesterType">
            <el-radio :label="1">春季</el-radio>
            <el-radio :label="2">秋季</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="开始日期" prop="startDate">
          <el-date-picker
            v-model="formData.startDate"
            type="date"
            placeholder="选择日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="结束日期" prop="endDate">
          <el-date-picker
            v-model="formData.endDate"
            type="date"
            placeholder="选择日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="选课开始时间" prop="courseSelectionStart">
          <el-date-picker
            v-model="formData.courseSelectionStart"
            type="datetime"
            placeholder="选择日期时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="选课结束时间" prop="courseSelectionEnd">
          <el-date-picker
            v-model="formData.courseSelectionEnd"
            type="datetime"
            placeholder="选择日期时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
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
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import {
  getAllSemesters,
  createSemester,
  updateSemester,
  deleteSemester,
  activateSemester
} from '@/api/semester'

const loading = ref(false)
const semesterList = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const submitLoading = ref(false)
const formRef = ref(null)
const editingId = ref(null)

const formData = ref({
  academicYear: '',
  semesterType: 1,
  startDate: '',
  endDate: '',
  courseSelectionStart: '',
  courseSelectionEnd: ''
})

const formRules = {
  academicYear: [
    { required: true, message: '请输入学年', trigger: 'blur' },
    { pattern: /^\d{4}-\d{4}$/, message: '学年格式不正确，例如：2024-2025', trigger: 'blur' }
  ],
  semesterType: [
    { required: true, message: '请选择学期类型', trigger: 'change' }
  ],
  startDate: [
    { required: true, message: '请选择开始日期', trigger: 'change' }
  ],
  endDate: [
    { required: true, message: '请选择结束日期', trigger: 'change' }
  ],
  courseSelectionStart: [
    { required: true, message: '请选择选课开始时间', trigger: 'change' }
  ],
  courseSelectionEnd: [
    { required: true, message: '请选择选课结束时间', trigger: 'change' }
  ]
}

// 获取学期列表
const fetchSemesterList = async () => {
  try {
    loading.value = true
    const { data } = await getAllSemesters()
    semesterList.value = data || []
  } catch (error) {
    console.error('获取学期列表失败:', error)
    ElMessage.error('获取学期列表失败')
  } finally {
    loading.value = false
  }
}

// 新建学期
const handleAdd = () => {
  dialogTitle.value = '新建学期'
  editingId.value = null
  dialogVisible.value = true
}

// 编辑学期
const handleEdit = (row) => {
  dialogTitle.value = '编辑学期'
  editingId.value = row.id
  formData.value = {
    academicYear: row.academicYear,
    semesterType: row.semesterType,
    startDate: row.startDate,
    endDate: row.endDate,
    courseSelectionStart: row.courseSelectionStart,
    courseSelectionEnd: row.courseSelectionEnd
  }
  dialogVisible.value = true
}

// 删除学期
const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定要删除学期"${row.academicYear} ${row.semesterType === 1 ? '春季' : '秋季'}"吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await deleteSemester(row.id)
      ElMessage.success('删除成功')
      fetchSemesterList()
    } catch (error) {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

// 设置为当前学期
const handleActivate = (row) => {
  ElMessageBox.confirm(
    `确定要将"${row.academicYear} ${row.semesterType === 1 ? '春季' : '秋季'}"设为当前学期吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await activateSemester(row.id)
      ElMessage.success('设置成功')
      fetchSemesterList()
    } catch (error) {
      console.error('设置失败:', error)
      ElMessage.error('设置失败')
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
      await updateSemester(editingId.value, formData.value)
      ElMessage.success('更新成功')
    } else {
      await createSemester(formData.value)
      ElMessage.success('创建成功')
    }
    
    dialogVisible.value = false
    fetchSemesterList()
  } catch (error) {
    console.error('提交失败:', error)
    ElMessage.error('提交失败')
  } finally {
    submitLoading.value = false
  }
}

// 重置表单
const resetForm = () => {
  formRef.value?.resetFields()
  formData.value = {
    academicYear: '',
    semesterType: 1,
    startDate: '',
    endDate: '',
    courseSelectionStart: '',
    courseSelectionEnd: ''
  }
  editingId.value = null
}

onMounted(() => {
  fetchSemesterList()
})
</script>

<style scoped>
.semester-manage {
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
</style>

