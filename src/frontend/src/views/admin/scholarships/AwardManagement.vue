<template>
  <div class="award-management">
    <el-card shadow="never" class="header-card">
      <div class="header-content">
        <div>
          <h2 class="page-title">
            <el-icon><Medal /></el-icon>
            获奖管理
          </h2>
          <p class="page-subtitle">管理和公示获奖名单</p>
        </div>
        <el-button type="success" :icon="Promotion" @click="handlePublish">
          公示获奖名单
        </el-button>
      </div>
    </el-card>

    <!-- 筛选栏 -->
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" :model="queryForm" @submit.prevent="handleQuery" class="filter-form">
        <el-form-item label="学年">
          <el-select v-model="queryForm.academicYear" placeholder="全部学年" clearable style="width: 180px">
            <el-option
              v-for="year in academicYears"
              :key="year"
              :label="year"
              :value="year"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="奖学金">
          <el-select v-model="queryForm.scholarshipId" placeholder="全部奖学金" clearable style="width: 200px">
            <el-option
              v-for="scholarship in scholarships"
              :key="scholarship.id"
              :label="scholarship.name"
              :value="scholarship.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="公示状态">
          <el-select v-model="queryForm.published" placeholder="全部" clearable style="width: 120px">
            <el-option label="已公示" :value="true" />
            <el-option label="未公示" :value="false" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleQuery">
            查询
          </el-button>
          <el-button :icon="RefreshRight" @click="handleReset">
            重置
          </el-button>
          <el-button :icon="Download" @click="handleExport">
            导出名单
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="5" animated />
    </div>

    <!-- 获奖列表 -->
    <el-card v-else shadow="never">
      <el-table :data="awards" stripe>
        <el-table-column label="学生信息" min-width="150">
          <template #default="{ row }">
            <div class="student-info">
              <div class="student-name">{{ row.studentName }}</div>
              <div class="student-no">{{ row.studentNo }}</div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="专业" prop="majorName" min-width="120" />

        <el-table-column label="年级" width="80" align="center">
          <template #default="{ row }">
            {{ row.grade }}级
          </template>
        </el-table-column>

        <el-table-column label="奖学金" prop="scholarshipName" min-width="150" />

        <el-table-column label="等级" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getLevelTypeByDesc(row.scholarshipLevel)">
              {{ row.scholarshipLevel }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="奖金金额" width="120" align="right">
          <template #default="{ row }">
            <span class="amount">¥{{ row.amount.toLocaleString() }}</span>
          </template>
        </el-table-column>

        <el-table-column label="学年" prop="academicYear" width="120" />

        <el-table-column label="获奖时间" width="160">
          <template #default="{ row }">
            {{ formatDate(row.awardedAt) }}
          </template>
        </el-table-column>

        <el-table-column label="公示状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.published" type="success">已公示</el-tag>
            <el-tag v-else type="info">未公示</el-tag>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleQuery"
          @current-change="handleQuery"
        />
      </div>
    </el-card>

    <!-- 公示对话框 -->
    <el-dialog
      v-model="publishDialogVisible"
      title="公示获奖名单"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="publishFormRef"
        :model="publishForm"
        :rules="publishRules"
        label-width="120px"
      >
        <el-form-item label="奖学金" prop="scholarshipId" required>
          <el-select
            v-model="publishForm.scholarshipId"
            placeholder="请选择奖学金"
            style="width: 100%"
          >
            <el-option
              v-for="scholarship in scholarships"
              :key="scholarship.id"
              :label="scholarship.name"
              :value="scholarship.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="学年" prop="academicYear" required>
          <el-select
            v-model="publishForm.academicYear"
            placeholder="请选择学年"
            style="width: 100%"
          >
            <el-option
              v-for="year in academicYears"
              :key="year"
              :label="year"
              :value="year"
            />
          </el-select>
        </el-form-item>

        <el-alert
          title="公示说明"
          type="warning"
          :closable="false"
          style="margin-top: 10px;"
        >
          <p>公示后将：</p>
          <ul>
            <li>标记为已公示状态</li>
            <li>发送通知给所有获奖学生</li>
            <li>生成公示公告</li>
          </ul>
          <p>请确认信息无误后再进行公示。</p>
        </el-alert>
      </el-form>

      <template #footer>
        <el-button @click="publishDialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          :loading="publishing"
          @click="handlePublishSubmit"
        >
          确认公示
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Medal,
  Promotion,
  Search,
  RefreshRight,
  Download
} from '@element-plus/icons-vue'
import {
  getAwards,
  getScholarships,
  publishAwards,
  exportAwards
} from '@/api/scholarship'
import { formatDate } from '@/utils/date'

const loading = ref(true)
const publishing = ref(false)
const awards = ref([])
const scholarships = ref([])
const academicYears = ref([])
const publishDialogVisible = ref(false)
const publishFormRef = ref()

const queryForm = reactive({
  academicYear: '',
  scholarshipId: null,
  published: null
})

const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

const publishForm = reactive({
  scholarshipId: null,
  academicYear: ''
})

const publishRules = {
  scholarshipId: [
    { required: true, message: '请选择奖学金', trigger: 'change' }
  ],
  academicYear: [
    { required: true, message: '请选择学年', trigger: 'change' }
  ]
}

// 获取获奖列表
const fetchAwards = async () => {
  try {
    loading.value = true
    // 过滤掉空值参数
    const filteredQuery = Object.entries(queryForm).reduce((acc, [key, value]) => {
      if (value !== '' && value !== null && value !== undefined) {
        acc[key] = value
      }
      return acc
    }, {})
    
    const params = {
      page: pagination.page - 1,
      size: pagination.size,
      ...filteredQuery
    }
    const res = await getAwards(params)
    awards.value = res.data.content || []
    pagination.total = res.data.totalElements || 0
  } catch (error) {
    console.error('获取获奖列表失败:', error)
    ElMessage.error('获取获奖列表失败')
  } finally {
    loading.value = false
  }
}

// 获取奖学金列表
const fetchScholarships = async () => {
  try {
    const res = await getScholarships({ page: 0, size: 100 })
    scholarships.value = res.data.content || []
  } catch (error) {
    console.error('获取奖学金列表失败:', error)
  }
}

// 查询
const handleQuery = () => {
  pagination.page = 1
  fetchAwards()
}

// 重置
const handleReset = () => {
  queryForm.academicYear = ''
  queryForm.scholarshipId = null
  queryForm.published = null
  handleQuery()
}

// 导出
const handleExport = async () => {
  try {
    if (!queryForm.academicYear && !queryForm.scholarshipId) {
      ElMessage.warning('请先选择学年或奖学金进行筛选')
      return
    }

    const params = { ...queryForm }
    const blob = await exportAwards(params)
    
    // 创建下载链接
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `获奖名单_${new Date().getTime()}.xlsx`
    link.click()
    window.URL.revokeObjectURL(url)
    
    ElMessage.success('导出成功！')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  }
}

// 获取等级类型
const getLevelType = (level) => {
  const types = {
    NATIONAL: 'danger',
    PROVINCIAL: 'warning',
    UNIVERSITY: 'primary',
    DEPARTMENT: 'success'
  }
  return types[level] || 'info'
}

// 根据等级描述获取类型（用于已经转换为中文的等级）
const getLevelTypeByDesc = (levelDesc) => {
  const types = {
    '国家级': 'danger',
    '省级': 'warning',
    '校级': 'primary',
    '院系级': 'success'
  }
  return types[levelDesc] || 'info'
}

// 获取等级文本
const getLevelText = (level) => {
  const texts = {
    NATIONAL: '国家级',
    PROVINCIAL: '省级',
    UNIVERSITY: '校级',
    DEPARTMENT: '院系级'
  }
  return texts[level] || level
}

// 公示
const handlePublish = () => {
  publishForm.scholarshipId = null
  publishForm.academicYear = academicYears.value[0] || ''
  publishDialogVisible.value = true
}

// 提交公示
const handlePublishSubmit = async () => {
  try {
    await publishFormRef.value.validate()

    await ElMessageBox.confirm(
      '确认公示该奖学金的获奖名单吗？公示后将无法撤销。',
      '确认公示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    publishing.value = true
    await publishAwards(publishForm)
    
    ElMessage.success('公示成功！')
    publishDialogVisible.value = false
    fetchAwards()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('公示失败:', error)
      ElMessage.error(error.message || '公示失败')
    }
  } finally {
    publishing.value = false
  }
}

onMounted(() => {
  // 生成学年列表
  const currentYear = new Date().getFullYear()
  const years = []
  for (let i = 0; i < 5; i++) {
    const year = currentYear - i
    years.push(`${year}-${year + 1}`)
  }
  academicYears.value = years

  fetchScholarships()
  fetchAwards()
})
</script>

<style scoped>
.award-management {
  padding: 20px;
}

.header-card {
  margin-bottom: 20px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 0 0 10px 0;
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.page-subtitle {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.filter-card {
  margin-bottom: 20px;
}

.filter-form {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.filter-form :deep(.el-form-item) {
  margin-bottom: 10px;
}

.loading-container {
  padding: 20px;
}

.student-info {
  line-height: 1.6;
}

.student-name {
  font-weight: 500;
  color: #303133;
}

.student-no {
  font-size: 12px;
  color: #909399;
}

.amount {
  color: #f56c6c;
  font-weight: bold;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>

