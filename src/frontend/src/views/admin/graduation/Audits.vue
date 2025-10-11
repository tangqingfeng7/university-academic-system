<template>
  <div class="graduation-audits">
    <el-card class="header-card">
      <h2>审核结果查询</h2>
      <p class="subtitle">查看和管理学生毕业审核结果</p>
    </el-card>

    <!-- 查询条件 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="学号/姓名">
          <el-input
            v-model="searchForm.keyword"
            placeholder="请输入学号或姓名"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="审核年份">
          <el-date-picker
            v-model="searchForm.year"
            type="year"
            placeholder="选择年份"
            value-format="YYYY"
            clearable
          />
        </el-form-item>
        <el-form-item label="审核状态">
          <el-select 
            v-model="searchForm.status" 
            placeholder="请选择状态" 
            clearable
            style="width: 150px"
          >
            <el-option label="通过" value="PASS" />
            <el-option label="未通过" value="FAIL" />
            <el-option label="暂缓毕业" value="DEFERRED" />
            <el-option label="待审核" value="PENDING" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadAudits">
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

    <!-- 审核结果列表 -->
    <el-card>
      <el-table
        v-loading="loading"
        :data="audits"
        border
        stripe
        style="width: 100%"
      >
        <el-table-column prop="studentNo" label="学号" width="120" />
        <el-table-column prop="studentName" label="姓名" width="100" />
        <el-table-column prop="majorName" label="专业" min-width="150" />
        <el-table-column prop="auditYear" label="审核年份" width="100" align="center" />
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
        <el-table-column label="审核状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.status === 'PASS'" type="success">通过</el-tag>
            <el-tag v-else-if="row.status === 'FAIL'" type="danger">未通过</el-tag>
            <el-tag v-else-if="row.status === 'DEFERRED'" type="warning">暂缓</el-tag>
            <el-tag v-else type="info">待审核</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="auditedAt" label="审核时间" width="160" />
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="showDetail(row)">
              <el-icon><View /></el-icon>
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && audits.length === 0" description="暂无数据" />
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailVisible"
      title="审核详情"
      width="700px"
    >
      <div v-if="currentAudit" class="audit-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="学号">{{ currentAudit.studentNo }}</el-descriptions-item>
          <el-descriptions-item label="姓名">{{ currentAudit.studentName }}</el-descriptions-item>
          <el-descriptions-item label="专业" :span="2">{{ currentAudit.majorName }}</el-descriptions-item>
          <el-descriptions-item label="审核年份">{{ currentAudit.auditYear }}</el-descriptions-item>
          <el-descriptions-item label="审核状态">
            <el-tag v-if="currentAudit.status === 'PASS'" type="success">通过</el-tag>
            <el-tag v-else-if="currentAudit.status === 'FAIL'" type="danger">未通过</el-tag>
            <el-tag v-else-if="currentAudit.status === 'DEFERRED'" type="warning">暂缓</el-tag>
            <el-tag v-else type="info">待审核</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="总学分">{{ currentAudit.totalCredits }}</el-descriptions-item>
          <el-descriptions-item label="必修学分">{{ currentAudit.requiredCredits }}</el-descriptions-item>
          <el-descriptions-item label="选修学分">{{ currentAudit.electiveCredits }}</el-descriptions-item>
          <el-descriptions-item label="实践学分">{{ currentAudit.practicalCredits }}</el-descriptions-item>
          <el-descriptions-item label="审核人">{{ currentAudit.auditedByName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="审核时间">{{ currentAudit.auditedAt || '-' }}</el-descriptions-item>
          <el-descriptions-item v-if="currentAudit.failReason" label="不通过原因" :span="2">
            <el-text type="danger">{{ currentAudit.failReason }}</el-text>
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, View } from '@element-plus/icons-vue'
import request from '@/utils/request'

const loading = ref(false)
const detailVisible = ref(false)
const audits = ref([])
const currentAudit = ref(null)

const searchForm = reactive({
  keyword: '',
  year: null,
  status: null
})

// 加载审核结果列表
const loadAudits = async () => {
  loading.value = true
  try {
    const params = {}
    if (searchForm.year) params.year = searchForm.year
    if (searchForm.status) params.status = searchForm.status

    const response = await request.get('/admin/graduation/audits', { params })
    audits.value = response.data || []

    // 如果有关键词,前端过滤
    if (searchForm.keyword) {
      const keyword = searchForm.keyword.toLowerCase()
      audits.value = audits.value.filter(audit =>
        audit.studentNo?.toLowerCase().includes(keyword) ||
        audit.studentName?.toLowerCase().includes(keyword)
      )
    }
  } catch (error) {
    ElMessage.error(error.message || '加载审核结果失败')
  } finally {
    loading.value = false
  }
}

// 重置搜索
const resetSearch = () => {
  searchForm.keyword = ''
  searchForm.year = null
  searchForm.status = null
  loadAudits()
}

// 显示详情
const showDetail = (row) => {
  currentAudit.value = row
  detailVisible.value = true
}

onMounted(() => {
  loadAudits()
})
</script>

<style scoped lang="scss">
.graduation-audits {
  .header-card {
    margin-bottom: 20px;

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

  .search-card {
    margin-bottom: 20px;
  }

  .credit-info {
    display: flex;
    gap: 8px;
  }
}
</style>

