<template>
  <div class="graduates">
    <el-card class="header-card">
      <div class="header-content">
        <div>
          <h2>毕业生名单</h2>
          <p class="subtitle">查看和导出通过毕业审核的学生名单</p>
        </div>
        <el-space>
          <el-button type="success" @click="handleExport" :loading="exporting">
            <el-icon><Download /></el-icon>
            导出名单
          </el-button>
          <el-button type="info" @click="loadStatistics">
            <el-icon><DataAnalysis /></el-icon>
            查看统计
          </el-button>
        </el-space>
      </div>
    </el-card>

    <!-- 查询条件 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="毕业年份">
          <el-date-picker
            v-model="searchForm.year"
            type="year"
            placeholder="选择年份"
            value-format="YYYY"
            clearable
          />
        </el-form-item>
        <el-form-item label="院系">
          <el-select 
            v-model="searchForm.departmentId" 
            placeholder="请选择院系" 
            clearable
            style="width: 200px"
          >
            <el-option
              v-for="dept in departments"
              :key="dept.id"
              :label="dept.name"
              :value="dept.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="专业">
          <el-select 
            v-model="searchForm.majorId" 
            placeholder="请选择专业" 
            clearable 
            filterable
            style="width: 200px"
          >
            <el-option
              v-for="major in filteredMajors"
              :key="major.id"
              :label="major.name"
              :value="major.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadGraduates">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button @click="resetSearch">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 毕业生列表 -->
    <el-card>
      <template #header>
        <div class="card-header">
          <span>毕业生列表</span>
          <el-tag type="success" size="large">
            总计: {{ graduates.length }} 人
          </el-tag>
        </div>
      </template>

      <el-table
        v-loading="loading"
        :data="graduates"
        border
        stripe
        style="width: 100%"
      >
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="studentNo" label="学号" width="120" />
        <el-table-column prop="studentName" label="姓名" width="100" />
        <el-table-column prop="majorName" label="专业" min-width="150" />
        <el-table-column prop="className" label="班级" width="120" />
        <el-table-column prop="enrollmentYear" label="入学年份" width="100" align="center" />
        <el-table-column label="学分情况" width="280">
          <template #default="{ row }">
            <div class="credit-info">
              <el-tag type="primary" size="small">总: {{ row.totalCredits }}</el-tag>
              <el-tag type="danger" size="small">必: {{ row.requiredCredits }}</el-tag>
              <el-tag type="warning" size="small">选: {{ row.electiveCredits }}</el-tag>
              <el-tag type="success" size="small">实: {{ row.practicalCredits }}</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="auditedAt" label="审核通过时间" width="160" />
      </el-table>

      <el-empty v-if="!loading && graduates.length === 0" description="暂无毕业生数据" />
    </el-card>

    <!-- 统计对话框 -->
    <el-dialog
      v-model="statisticsVisible"
      title="毕业生统计"
      width="800px"
    >
      <div v-if="statistics" class="statistics-content">
        <el-row :gutter="20" style="margin-bottom: 20px">
          <el-col :span="8">
            <el-statistic title="毕业生总数" :value="statistics.totalCount">
              <template #suffix>人</template>
            </el-statistic>
          </el-col>
          <el-col :span="8">
            <el-statistic title="平均总学分" :value="statistics.averageTotalCredits" :precision="1">
              <template #suffix>分</template>
            </el-statistic>
          </el-col>
          <el-col :span="8">
            <el-statistic title="平均必修学分" :value="statistics.averageRequiredCredits" :precision="1">
              <template #suffix>分</template>
            </el-statistic>
          </el-col>
        </el-row>

        <el-divider>各专业毕业生分布</el-divider>

        <el-table
          :data="statistics.majorDistribution"
          border
          style="width: 100%"
        >
          <el-table-column prop="majorName" label="专业" min-width="150" />
          <el-table-column prop="count" label="毕业人数" width="120" align="center">
            <template #default="{ row }">
              <el-tag type="success">{{ row.count }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="占比" width="150" align="center">
            <template #default="{ row }">
              {{ ((row.count / statistics.totalCount) * 100).toFixed(1) }}%
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Download, DataAnalysis } from '@element-plus/icons-vue'
import request from '@/utils/request'

const loading = ref(false)
const exporting = ref(false)
const statisticsVisible = ref(false)

const graduates = ref([])
const departments = ref([])
const majors = ref([])
const statistics = ref(null)

const searchForm = reactive({
  year: new Date().getFullYear().toString(),
  departmentId: null,
  majorId: null
})

// 过滤专业
const filteredMajors = computed(() => {
  if (!searchForm.departmentId) return majors.value
  return majors.value.filter(m => m.departmentId === searchForm.departmentId)
})

// 加载院系列表
const loadDepartments = async () => {
  try {
    const response = await request.get('/admin/departments/all')
    departments.value = response.data || []
  } catch (error) {
    console.error('加载院系列表失败:', error)
  }
}

// 加载专业列表
const loadMajors = async () => {
  try {
    const response = await request.get('/admin/majors/all')
    majors.value = response.data || []
  } catch (error) {
    console.error('加载专业列表失败:', error)
  }
}

// 加载毕业生列表
const loadGraduates = async () => {
  if (!searchForm.year) {
    ElMessage.warning('请选择毕业年份')
    return
  }

  loading.value = true
  try {
    const params = { year: searchForm.year }
    const response = await request.get('/admin/graduation/graduates', { params })
    graduates.value = response.data || []

    // 前端过滤院系和专业
    let filtered = graduates.value
    if (searchForm.departmentId) {
      filtered = filtered.filter(g => {
        const major = majors.value.find(m => m.name === g.majorName)
        return major && major.departmentId === searchForm.departmentId
      })
    }
    if (searchForm.majorId) {
      const major = majors.value.find(m => m.id === searchForm.majorId)
      if (major) {
        filtered = filtered.filter(g => g.majorName === major.name)
      }
    }
    graduates.value = filtered
  } catch (error) {
    ElMessage.error(error.message || '加载毕业生列表失败')
  } finally {
    loading.value = false
  }
}

// 重置搜索
const resetSearch = () => {
  searchForm.year = new Date().getFullYear().toString()
  searchForm.departmentId = null
  searchForm.majorId = null
  loadGraduates()
}

// 导出名单
const handleExport = async () => {
  if (!searchForm.year) {
    ElMessage.warning('请选择毕业年份')
    return
  }

  exporting.value = true
  try {
    const response = await request.get(`/admin/graduation/graduates/export`, {
      params: { year: searchForm.year },
      responseType: 'blob'
    })

    // 创建下载链接
    const url = window.URL.createObjectURL(new Blob([response]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', `毕业生名单_${searchForm.year}.xlsx`)
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)

    ElMessage.success('导出成功')
  } catch (error) {
    ElMessage.error(error.message || '导出失败')
  } finally {
    exporting.value = false
  }
}

// 加载统计信息
const loadStatistics = async () => {
  if (!searchForm.year) {
    ElMessage.warning('请选择毕业年份')
    return
  }

  try {
    const response = await request.get('/admin/graduation/graduates/statistics', {
      params: { year: searchForm.year }
    })
    statistics.value = response.data
    statisticsVisible.value = true
  } catch (error) {
    ElMessage.error(error.message || '加载统计信息失败')
  }
}

onMounted(() => {
  loadDepartments()
  loadMajors()
  loadGraduates()
})
</script>

<style scoped lang="scss">
.graduates {
  .header-card {
    margin-bottom: 20px;

    .header-content {
      display: flex;
      justify-content: space-between;
      align-items: center;

      h2 {
        margin: 0 0 8px;
        font-size: 24px;
        color: #303133;
      }

      .subtitle {
        margin: 0;
        color: #909399;
        font-size: 14px;
      }
    }
  }

  .search-card {
    margin-bottom: 20px;
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .credit-info {
    display: flex;
    gap: 8px;
  }

  .statistics-content {
    :deep(.el-statistic__content) {
      font-size: 32px;
    }
  }
}
</style>

