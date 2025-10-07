<template>
  <div class="operation-log">
    <div class="page-header">
      <h2>操作日志</h2>
    </div>

    <el-card>
      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-input
          v-model="queryParams.username"
          placeholder="用户名"
          clearable
          style="width: 200px"
        />
        <el-select
          v-model="queryParams.status"
          placeholder="操作状态"
          clearable
          style="width: 150px"
        >
          <el-option label="成功" value="SUCCESS" />
          <el-option label="失败" value="FAILURE" />
        </el-select>
        <el-date-picker
          v-model="dateRange"
          type="datetimerange"
          range-separator="至"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
          format="YYYY-MM-DD HH:mm:ss"
          value-format="YYYY-MM-DD HH:mm:ss"
          style="width: 380px"
        />
        <el-button type="primary" @click="handleSearch">
          <el-icon><Search /></el-icon>
          查询
        </el-button>
        <el-button @click="handleReset">
          <el-icon><RefreshLeft /></el-icon>
          重置
        </el-button>
      </div>

      <!-- 统计信息 -->
      <div v-if="statistics" class="statistics-bar">
        <el-descriptions :column="4" border>
          <el-descriptions-item label="总操作数">
            {{ statistics.totalOperations || 0 }}
          </el-descriptions-item>
          <el-descriptions-item label="成功次数">
            <span class="success-text">{{ statistics.successCount || 0 }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="失败次数">
            <span class="error-text">{{ statistics.failureCount || 0 }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="成功率">
            {{ statistics.successRate || '0%' }}
          </el-descriptions-item>
        </el-descriptions>
      </div>

      <!-- 日志表格 -->
      <el-table
        v-loading="loading"
        :data="logList.content"
        stripe
        border
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="operation" label="操作" min-width="150" />
        <el-table-column prop="method" label="请求方法" min-width="200" show-overflow-tooltip />
        <el-table-column label="参数" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <el-tooltip
              v-if="row.params"
              :content="row.params"
              placement="top"
            >
              <span class="params-text">{{ row.params }}</span>
            </el-tooltip>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="ip" label="IP地址" width="140" />
        <el-table-column label="执行时长" width="100">
          <template #default="{ row }">
            {{ row.executionTime }}ms
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.status === 'SUCCESS'" type="success" size="small">
              成功
            </el-tag>
            <el-tag v-else type="danger" size="small">
              失败
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="错误信息" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <el-tooltip
              v-if="row.errorMsg"
              :content="row.errorMsg"
              placement="top"
            >
              <span class="error-text">{{ row.errorMsg }}</span>
            </el-tooltip>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="操作时间" width="180" />
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.size"
          :total="logList.totalElements"
          :page-sizes="[20, 50, 100, 200]"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="fetchLogList"
          @size-change="fetchLogList"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, RefreshLeft } from '@element-plus/icons-vue'
import { searchLogs, getLogStatistics } from '@/api/operationLog'

const loading = ref(false)
const logList = ref({
  content: [],
  totalElements: 0,
  totalPages: 0
})
const statistics = ref(null)
const dateRange = ref([])

const queryParams = reactive({
  page: 0,
  size: 20,
  username: '',
  status: '',
  startTime: '',
  endTime: ''
})

// 获取日志列表
const fetchLogList = async () => {
  try {
    loading.value = true
    
    const params = {
      page: queryParams.page,
      size: queryParams.size,
      username: queryParams.username || undefined,
      status: queryParams.status || undefined,
      startTime: queryParams.startTime || undefined,
      endTime: queryParams.endTime || undefined
    }
    
    const { data } = await searchLogs(params)
    logList.value = data
  } catch (error) {
    console.error('获取日志列表失败:', error)
    ElMessage.error('获取日志列表失败')
  } finally {
    loading.value = false
  }
}

// 获取统计信息
const fetchStatistics = async () => {
  try {
    const params = {
      startTime: queryParams.startTime || undefined,
      endTime: queryParams.endTime || undefined
    }
    
    const { data } = await getLogStatistics(params)
    statistics.value = data
  } catch (error) {
    console.error('获取统计信息失败:', error)
  }
}

// 搜索
const handleSearch = () => {
  // 处理时间范围
  if (dateRange.value && dateRange.value.length === 2) {
    queryParams.startTime = dateRange.value[0]
    queryParams.endTime = dateRange.value[1]
  } else {
    queryParams.startTime = ''
    queryParams.endTime = ''
  }
  
  queryParams.page = 0
  fetchLogList()
  fetchStatistics()
}

// 重置
const handleReset = () => {
  queryParams.username = ''
  queryParams.status = ''
  queryParams.startTime = ''
  queryParams.endTime = ''
  dateRange.value = []
  queryParams.page = 0
  fetchLogList()
  fetchStatistics()
}

onMounted(() => {
  fetchLogList()
  fetchStatistics()
})
</script>

<style scoped>
.operation-log {
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

.search-bar {
  margin-bottom: 20px;
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.statistics-bar {
  margin-bottom: 20px;
}

.success-text {
  color: #67c23a;
  font-weight: 600;
}

.error-text {
  color: #f56c6c;
  font-weight: 600;
}

.params-text {
  display: inline-block;
  max-width: 180px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>

