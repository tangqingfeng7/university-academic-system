<template>
  <div class="report-discipline">
    <el-card shadow="never" class="header-card">
      <h2>处分上报</h2>
      <p class="subtitle">上报学生违纪处分记录</p>
    </el-card>

    <!-- 上报表单 -->
    <el-card shadow="never">
      <el-form
        ref="formRef"
        :model="form"
        :rules="formRules"
        label-width="120px"
        style="max-width: 800px"
      >
        <el-form-item label="学生" prop="studentId">
          <el-input
            v-model="form.studentName"
            placeholder="点击选择学生"
            readonly
            @click="handleSearchStudent"
          >
            <template #append>
              <el-button :icon="Search" @click="handleSearchStudent">选择</el-button>
            </template>
          </el-input>
          <div v-if="form.studentId" class="student-info">
            <span>学号：{{ form.studentNo }}</span>
            <span style="margin-left: 20px">专业：{{ form.majorName }}</span>
            <span style="margin-left: 20px">班级：{{ form.className }}</span>
          </div>
        </el-form-item>

        <el-form-item label="处分类型" prop="disciplineType">
          <el-select v-model="form.disciplineType" placeholder="请选择处分类型" style="width: 100%">
            <el-option label="警告" value="WARNING" />
            <el-option label="严重警告" value="SERIOUS_WARNING" />
            <el-option label="记过" value="DEMERIT" />
            <el-option label="记大过" value="SERIOUS_DEMERIT" />
            <el-option label="留校察看" value="PROBATION" />
            <el-option label="开除学籍" value="EXPULSION" />
          </el-select>
        </el-form-item>

        <el-form-item label="违纪日期" prop="occurrenceDate">
          <el-date-picker
            v-model="form.occurrenceDate"
            type="date"
            placeholder="请选择违纪日期"
            style="width: 100%"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>

        <el-form-item label="建议处分日期" prop="punishmentDate">
          <el-date-picker
            v-model="form.punishmentDate"
            type="date"
            placeholder="请选择处分日期"
            style="width: 100%"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>

        <el-form-item label="处分原因" prop="reason">
          <el-input
            v-model="form.reason"
            type="textarea"
            :rows="4"
            placeholder="请详细描述违纪原因"
          />
        </el-form-item>

        <el-form-item label="详细描述">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="6"
            placeholder="请详细描述违纪经过、情节等（选填）"
          />
        </el-form-item>

        <el-form-item label="是否可解除" prop="canRemove">
          <el-switch v-model="form.canRemove" />
          <span style="margin-left: 10px; color: #909399; font-size: 12px">
            是否允许后续解除该处分
          </span>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
            提交上报
          </el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 选择学生对话框 -->
    <el-dialog
      v-model="studentSearchVisible"
      title="选择学生"
      width="800px"
    >
      <el-form :inline="true" style="margin-bottom: 20px">
        <el-form-item label="学号/姓名">
          <el-input
            v-model="studentKeyword"
            placeholder="请输入学号或姓名"
            clearable
            @keyup.enter="handleStudentSearch"
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleStudentSearch">搜索</el-button>
        </el-form-item>
      </el-form>

      <el-table
        v-loading="studentLoading"
        :data="studentList"
        border
        stripe
        max-height="400px"
      >
        <el-table-column prop="studentNo" label="学号" width="120" />
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column prop="majorName" label="专业" min-width="150" show-overflow-tooltip />
        <el-table-column prop="className" label="班级" width="120" />
        <el-table-column label="操作" width="100" align="center">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleSelectStudent(row)">
              选择
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { createDiscipline } from '@/api/discipline'
import { getStudentListForTeacher } from '@/api/student'

// 表单
const formRef = ref(null)
const form = reactive({
  studentId: null,
  studentNo: '',
  studentName: '',
  majorName: '',
  className: '',
  disciplineType: '',
  occurrenceDate: '',
  punishmentDate: '',
  reason: '',
  description: '',
  canRemove: true
})

// 表单验证
const formRules = {
  studentId: [
    { required: true, message: '请选择学生', trigger: 'change' }
  ],
  disciplineType: [
    { required: true, message: '请选择处分类型', trigger: 'change' }
  ],
  occurrenceDate: [
    { required: true, message: '请选择违纪日期', trigger: 'change' }
  ],
  punishmentDate: [
    { required: true, message: '请选择处分日期', trigger: 'change' }
  ],
  reason: [
    { required: true, message: '请输入处分原因', trigger: 'blur' },
    { min: 5, message: '处分原因至少5个字', trigger: 'blur' }
  ]
}

// 学生搜索
const studentSearchVisible = ref(false)
const studentKeyword = ref('')
const studentList = ref([])
const studentLoading = ref(false)

// 提交状态
const submitLoading = ref(false)

// 打开学生搜索
const handleSearchStudent = () => {
  studentSearchVisible.value = true
  studentKeyword.value = ''
  studentList.value = []
}

// 搜索学生
const handleStudentSearch = async () => {
  if (!studentKeyword.value) {
    studentList.value = []
    return
  }

  studentLoading.value = true
  try {
    const res = await getStudentListForTeacher({
      keyword: studentKeyword.value,
      page: 0,
      size: 20
    })
    studentList.value = res.data.content || []
  } catch (error) {
    console.error('搜索学生失败:', error)
    ElMessage.error('搜索学生失败')
  } finally {
    studentLoading.value = false
  }
}

// 选择学生
const handleSelectStudent = (row) => {
  form.studentId = row.id
  form.studentNo = row.studentNo
  form.studentName = row.name
  form.majorName = row.majorName
  form.className = row.className
  studentSearchVisible.value = false
}

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    
    submitLoading.value = true
    
    const data = {
      studentId: form.studentId,
      disciplineType: form.disciplineType,
      occurrenceDate: form.occurrenceDate,
      punishmentDate: form.punishmentDate,
      reason: form.reason,
      description: form.description,
      canRemove: form.canRemove
    }
    
    await createDiscipline(data)
    
    ElMessage.success('处分上报成功，等待管理员审批')
    handleReset()
  } catch (error) {
    if (error !== false) {
      console.error('提交失败:', error)
      // 显示详细错误信息
      if (error.reason && Array.isArray(error.reason)) {
        error.reason.forEach(msg => ElMessage.error(msg))
      } else if (error.data && error.data.message) {
        ElMessage.error(error.data.message)
      } else if (error.message) {
        ElMessage.error(error.message)
      } else {
        ElMessage.error('提交失败')
      }
    }
  } finally {
    submitLoading.value = false
  }
}

// 重置表单
const handleReset = () => {
  form.studentId = null
  form.studentNo = ''
  form.studentName = ''
  form.majorName = ''
  form.className = ''
  form.disciplineType = ''
  form.occurrenceDate = ''
  form.punishmentDate = ''
  form.reason = ''
  form.description = ''
  form.canRemove = true
  formRef.value?.resetFields()
}
</script>

<style scoped>
.report-discipline {
  padding: 20px;
}

.header-card {
  margin-bottom: 20px;
}

.header-card h2 {
  margin: 0 0 8px 0;
  font-size: 24px;
  color: #303133;
}

.subtitle {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.student-info {
  margin-top: 8px;
  padding: 8px 12px;
  background: #f0f9ff;
  border: 1px solid #bfdbfe;
  border-radius: 4px;
  color: #1e40af;
  font-size: 14px;
}
</style>

