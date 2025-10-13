<template>
  <div class="grade-input-page">
    <!-- 顶部操作栏 -->
    <div class="page-header">
      <div class="header-info">
        <h2>{{ courseInfo?.courseName || '成绩录入' }}</h2>
        <div class="meta-info" v-if="courseInfo">
          <span>课程编号：{{ courseInfo.courseNo }}</span>
          <el-divider direction="vertical" />
          <span>学期：{{ courseInfo.semesterName }}</span>
          <el-divider direction="vertical" />
          <span>选课人数：{{ gradeList.length }}</span>
        </div>
      </div>
      <div class="header-actions">
        <el-upload
          action=""
          :auto-upload="false"
          :on-change="handleImport"
          :show-file-list="false"
          accept=".xls,.xlsx"
        >
          <el-button type="success">
            <el-icon><Upload /></el-icon>
            导入成绩
          </el-button>
        </el-upload>
        <el-button type="primary" @click="handleBatchSave" :loading="saving">
          <el-icon><Select /></el-icon>
          批量保存
        </el-button>
        <el-button type="warning" @click="handleSubmit" :loading="submitting">
          <el-icon><Check /></el-icon>
          提交成绩
        </el-button>
        <el-button type="danger" @click="handlePublish" :loading="publishing">
          <el-icon><Promotion /></el-icon>
          发布成绩
        </el-button>
      </div>
    </div>

    <!-- 成绩表格 -->
    <div class="table-wrapper">
      <el-table
        v-loading="loading"
        :data="gradeList"
        stripe
        border
        height="calc(100vh - 240px)"
        style="width: 100%"
      >
        <el-table-column type="index" label="序号" width="60" align="center" fixed />
        
        <el-table-column prop="studentNo" label="学号" min-width="120" fixed />
        
        <el-table-column prop="studentName" label="姓名" min-width="100" fixed />
        
        <el-table-column prop="majorName" label="专业" min-width="180" show-overflow-tooltip />
        
        <el-table-column prop="className" label="班级" min-width="120" />

        <el-table-column label="平时成绩" min-width="130" align="center">
          <template #default="{ row }">
            <el-input-number
              v-model="row.regularScore"
              :min="0"
              :max="100"
              :precision="1"
              :controls="false"
              size="small"
              @change="calculateTotal(row)"
            />
          </template>
        </el-table-column>

        <el-table-column label="期中成绩" min-width="130" align="center">
          <template #default="{ row }">
            <el-input-number
              v-model="row.midtermScore"
              :min="0"
              :max="100"
              :precision="1"
              :controls="false"
              size="small"
              @change="calculateTotal(row)"
            />
          </template>
        </el-table-column>

        <el-table-column label="期末成绩" min-width="130" align="center">
          <template #default="{ row }">
            <el-input-number
              v-model="row.finalScore"
              :min="0"
              :max="100"
              :precision="1"
              :controls="false"
              size="small"
              @change="calculateTotal(row)"
            />
          </template>
        </el-table-column>

        <el-table-column label="总评成绩" min-width="110" align="center">
          <template #default="{ row }">
            <span :class="getScoreClass(row.totalScore)" class="score-text">
              {{ row.totalScore != null ? row.totalScore.toFixed(1) : '-' }}
            </span>
          </template>
        </el-table-column>

        <el-table-column label="绩点" min-width="90" align="center">
          <template #default="{ row }">
            <span class="gpa-text">
              {{ row.gradePoint != null ? row.gradePoint.toFixed(2) : '-' }}
            </span>
          </template>
        </el-table-column>

        <el-table-column label="状态" min-width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusTag(row.status)" size="small">
              {{ getStatusName(row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="100" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              @click="handleSave(row)"
              :loading="row.saving"
            >
              保存
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="gradeList.length === 0 && !loading" class="empty-state">
        <el-empty description="暂无学生数据" />
      </div>
    </div>

    <!-- 导入结果对话框 -->
    <el-dialog
      v-model="importDialogVisible"
      title="成绩导入结果"
      width="600px"
    >
      <div v-if="importResult">
        <el-alert
          :type="importResult.errorCount > 0 ? 'warning' : 'success'"
          :closable="false"
          show-icon
        >
          <template #title>
            导入完成：成功 {{ importResult.successCount }} 条，失败 {{ importResult.errorCount }} 条
          </template>
        </el-alert>

        <el-table
          v-if="importResult.errors && importResult.errors.length > 0"
          :data="importResult.errors"
          style="margin-top: 20px"
          max-height="400"
        >
          <el-table-column prop="row" label="行号" width="80" />
          <el-table-column prop="studentNo" label="学号" width="120" />
          <el-table-column prop="studentName" label="姓名" width="120" />
          <el-table-column prop="error" label="错误信息" />
        </el-table>
      </div>

      <template #footer>
        <el-button @click="importDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="handleRefresh">刷新列表</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Upload, Select, Check, Promotion } from '@element-plus/icons-vue'
import { 
  getOfferingGrades, 
  inputGrade, 
  batchInputGrades, 
  submitGrades, 
  publishGrades,
  importGrades 
} from '@/api/teacherGrade'
import { getCourseDetail } from '@/api/teacherCourse'

const route = useRoute()
const offeringId = ref(parseInt(route.params.id))

// 数据
const loading = ref(false)
const saving = ref(false)
const submitting = ref(false)
const publishing = ref(false)
const gradeList = ref([])
const courseInfo = ref(null)
const importDialogVisible = ref(false)
const importResult = ref(null)

// 获取课程信息
const fetchCourseInfo = async () => {
  try {
    const res = await getCourseDetail(offeringId.value)
    courseInfo.value = res.data
  } catch (error) {
    console.error('获取课程信息失败:', error)
  }
}

// 获取成绩列表
const fetchGradeList = async () => {
  loading.value = true
  try {
    const res = await getOfferingGrades(offeringId.value)
    gradeList.value = (res.data || []).map(grade => ({
      ...grade,
      saving: false
    }))
  } catch (error) {
    console.error('获取成绩列表失败:', error)
    ElMessage.error('获取成绩列表失败: ' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// 计算总评成绩（平时30% + 期中30% + 期末40%）
const calculateTotal = (row) => {
  const regular = row.regularScore || 0
  const midterm = row.midtermScore || 0
  const final = row.finalScore || 0
  
  row.totalScore = regular * 0.3 + midterm * 0.3 + final * 0.4
  
  // 计算绩点（简化算法）
  if (row.totalScore >= 90) row.gradePoint = 4.0
  else if (row.totalScore >= 85) row.gradePoint = 3.7
  else if (row.totalScore >= 82) row.gradePoint = 3.3
  else if (row.totalScore >= 78) row.gradePoint = 3.0
  else if (row.totalScore >= 75) row.gradePoint = 2.7
  else if (row.totalScore >= 72) row.gradePoint = 2.3
  else if (row.totalScore >= 68) row.gradePoint = 2.0
  else if (row.totalScore >= 64) row.gradePoint = 1.5
  else if (row.totalScore >= 60) row.gradePoint = 1.0
  else row.gradePoint = 0
}

// 保存单个成绩
const handleSave = async (row) => {
  row.saving = true
  try {
    const data = {
      regularScore: row.regularScore,
      midtermScore: row.midtermScore,
      finalScore: row.finalScore
    }
    
    await inputGrade(row.courseSelectionId, data)
    ElMessage.success('成绩保存成功')
    fetchGradeList()
  } catch (error) {
    console.error('保存成绩失败:', error)
    ElMessage.error('保存成绩失败: ' + (error.message || '未知错误'))
  } finally {
    row.saving = false
  }
}

// 批量保存
const handleBatchSave = async () => {
  saving.value = true
  try {
    const gradeData = gradeList.value.map(row => ({
      courseSelectionId: row.courseSelectionId,
      regularScore: row.regularScore,
      midtermScore: row.midtermScore,
      finalScore: row.finalScore
    }))
    
    await batchInputGrades(gradeData)
    ElMessage.success('批量保存成功')
    fetchGradeList()
  } catch (error) {
    console.error('批量保存失败:', error)
    ElMessage.error('批量保存失败: ' + (error.message || '未知错误'))
  } finally {
    saving.value = false
  }
}

// 提交成绩
const handleSubmit = async () => {
  try {
    await ElMessageBox.confirm(
      '提交后成绩将无法修改，确定要提交吗？',
      '确认提交',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    submitting.value = true
    await submitGrades(offeringId.value)
    ElMessage.success('成绩提交成功')
    fetchGradeList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('提交成绩失败:', error)
      ElMessage.error('提交成绩失败: ' + (error.message || '未知错误'))
    }
  } finally {
    submitting.value = false
  }
}

// 发布成绩
const handlePublish = async () => {
  try {
    await ElMessageBox.confirm(
      '发布后学生将能看到成绩，确定要发布吗？',
      '确认发布',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    publishing.value = true
    await publishGrades(offeringId.value)
    ElMessage.success('成绩发布成功')
    fetchGradeList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('发布成绩失败:', error)
      ElMessage.error('发布成绩失败: ' + (error.message || '未知错误'))
    }
  } finally {
    publishing.value = false
  }
}

// 导入成绩
const handleImport = async (file) => {
  try {
    const res = await importGrades(offeringId.value, file.raw)
    importResult.value = res.data
    importDialogVisible.value = true
    
    if (res.data.errorCount === 0) {
      ElMessage.success('成绩导入成功')
    } else {
      ElMessage.warning(`导入完成，成功${res.data.successCount}条，失败${res.data.errorCount}条`)
    }
  } catch (error) {
    console.error('导入成绩失败:', error)
    ElMessage.error('导入成绩失败: ' + (error.message || '未知错误'))
  }
}

// 刷新列表
const handleRefresh = () => {
  importDialogVisible.value = false
  fetchGradeList()
}

// 分数样式
const getScoreClass = (score) => {
  if (score == null) return ''
  if (score >= 90) return 'score-excellent'
  if (score >= 80) return 'score-good'
  if (score >= 60) return 'score-pass'
  return 'score-fail'
}

// 状态标签
const getStatusTag = (status) => {
  const tagMap = {
    DRAFT: 'info',
    SUBMITTED: 'warning',
    PUBLISHED: 'success'
  }
  return tagMap[status] || 'info'
}

// 状态名称
const getStatusName = (status) => {
  const nameMap = {
    DRAFT: '草稿',
    SUBMITTED: '已提交',
    PUBLISHED: '已发布'
  }
  return nameMap[status] || status
}

// 页面加载
onMounted(() => {
  fetchCourseInfo()
  fetchGradeList()
})
</script>

<style scoped>
.grade-input-page {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  flex-direction: column;
  background: var(--bg-secondary);
  overflow: hidden;
}

.page-header {
  background: white;
  padding: 20px 24px;
  border-bottom: 1px solid var(--border-light);
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-shrink: 0;
}

.header-info h2 {
  margin: 0 0 8px 0;
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
}

.meta-info {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: var(--text-secondary);
}

.header-actions {
  display: flex;
  gap: 12px;
}

.table-wrapper {
  flex: 1;
  padding: 20px 24px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  width: 100%;
  box-sizing: border-box;
}

.empty-state {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: white;
  border-radius: var(--radius-lg);
}

.score-text {
  font-weight: 600;
  font-size: 15px;
}

.score-excellent {
  color: #67C23A;
}

.score-good {
  color: #409EFF;
}

.score-pass {
  color: #E6A23C;
}

.score-fail {
  color: #F56C6C;
}

.gpa-text {
  font-weight: 500;
  color: var(--text-secondary);
}

:deep(.el-input-number) {
  width: 100px;
}

:deep(.el-input-number .el-input__inner) {
  text-align: center;
  padding: 0 8px;
}

:deep(.el-table) {
  border-radius: var(--radius-lg);
  overflow: hidden;
  width: 100% !important;
}

:deep(.el-table__header) {
  font-weight: 600;
}

:deep(.el-table__body) {
  font-size: 14px;
}

:deep(.el-table__header-wrapper),
:deep(.el-table__body-wrapper) {
  width: 100% !important;
}
</style>
