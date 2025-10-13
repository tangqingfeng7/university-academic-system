<template>
  <div class="my-classes">
    <div class="page-header">
      <h2>我的班级</h2>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="statistics-cards">
      <el-col :span="8">
        <el-card shadow="hover">
          <div class="stat-item">
            <el-icon class="stat-icon" :size="40" color="#409EFF">
              <School />
            </el-icon>
            <div class="stat-content">
              <div class="stat-value">{{ statistics.totalClasses || 0 }}</div>
              <div class="stat-label">管理班级数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <div class="stat-item">
            <el-icon class="stat-icon" :size="40" color="#67C23A">
              <User />
            </el-icon>
            <div class="stat-content">
              <div class="stat-value">{{ statistics.totalStudents || 0 }}</div>
              <div class="stat-label">管理学生数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <div class="stat-item">
            <el-icon class="stat-icon" :size="40" color="#E6A23C">
              <Calendar />
            </el-icon>
            <div class="stat-content">
              <div class="stat-value">{{ yearCount }}</div>
              <div class="stat-label">涉及年级数</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 班级列表 -->
    <el-card class="class-list-card">
      <template #header>
        <div class="card-header">
          <span>班级列表</span>
          <el-button type="primary" @click="fetchData">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </template>

      <el-table
        v-loading="loading"
        :data="classList"
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
        <el-table-column label="当前人数" width="100">
          <template #default="{ row }">
            <el-tag>{{ getStudentCount(row.id) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button 
              type="primary" 
              size="small" 
              plain
              @click="handleViewStudents(row)"
            >
              <el-icon><View /></el-icon>
              查看学生
            </el-button>
            <el-button 
              type="info" 
              size="small" 
              plain
              @click="handleViewDetail(row)"
            >
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="classList.length === 0 && !loading" description="您还没有管理任何班级" />
    </el-card>

    <!-- 学生列表对话框 -->
    <el-dialog
      v-model="studentDialogVisible"
      :title="`${currentClass?.className} - 学生名单`"
      width="900px"
    >
      <el-table
        v-loading="studentLoading"
        :data="studentList"
        stripe
        border
        max-height="500"
      >
        <el-table-column type="index" label="序号" width="60" />
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

      <div v-if="studentList.length === 0 && !studentLoading" style="text-align: center; padding: 40px; color: #999;">
        该班级暂无学生
      </div>

      <template #footer>
        <div class="dialog-footer">
          <span>共 {{ studentList.length }} 名学生</span>
          <el-button @click="studentDialogVisible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 班级详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      :title="currentClass?.className"
      width="600px"
    >
      <el-descriptions :column="2" border v-if="currentClass">
        <el-descriptions-item label="班级代码">
          {{ currentClass.classCode }}
        </el-descriptions-item>
        <el-descriptions-item label="班级名称">
          {{ currentClass.className }}
        </el-descriptions-item>
        <el-descriptions-item label="所属专业">
          {{ currentClass.majorName }}
        </el-descriptions-item>
        <el-descriptions-item label="所属院系">
          {{ currentClass.departmentName }}
        </el-descriptions-item>
        <el-descriptions-item label="入学年份">
          {{ currentClass.enrollmentYear }}级
        </el-descriptions-item>
        <el-descriptions-item label="班级容量">
          {{ currentClass.capacity }}人
        </el-descriptions-item>
        <el-descriptions-item label="当前人数">
          {{ getStudentCount(currentClass.id) }}人
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">
          {{ currentClass.createdAt }}
        </el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">
          {{ currentClass.remarks || '无' }}
        </el-descriptions-item>
      </el-descriptions>

      <template #footer>
        <el-button type="primary" @click="detailDialogVisible = false">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  School, User, Calendar, Refresh, View 
} from '@element-plus/icons-vue'
import {
  getMyClasses,
  getMyClassStudents,
  getMyClassStatistics
} from '@/api/class'

const loading = ref(false)
const classList = ref([])
const statistics = ref({
  totalClasses: 0,
  totalStudents: 0,
  classesPerYear: {}
})

// 学生列表相关
const studentDialogVisible = ref(false)
const studentLoading = ref(false)
const studentList = ref([])
const currentClass = ref(null)

// 详情对话框
const detailDialogVisible = ref(false)

// 学生数量缓存
const studentCountMap = reactive({})

// 年级数量
const yearCount = computed(() => {
  return Object.keys(statistics.value.classesPerYear || {}).length
})

// 获取班级列表
const fetchClassList = async () => {
  try {
    loading.value = true
    const { data } = await getMyClasses()
    classList.value = data || []
    
    // 加载每个班级的学生数量
    for (const classItem of classList.value) {
      loadStudentCount(classItem.id)
    }
  } catch (error) {
    console.error('获取班级列表失败:', error)
    ElMessage.error('获取班级列表失败')
  } finally {
    loading.value = false
  }
}

// 获取统计信息
const fetchStatistics = async () => {
  try {
    const { data } = await getMyClassStatistics()
    statistics.value = data || {
      totalClasses: 0,
      totalStudents: 0,
      classesPerYear: {}
    }
  } catch (error) {
    console.error('获取统计信息失败:', error)
  }
}

// 加载班级学生数量
const loadStudentCount = async (classId) => {
  try {
    const { data } = await getMyClassStudents(classId)
    studentCountMap[classId] = (data || []).length
  } catch (error) {
    console.error('获取学生数量失败:', error)
    studentCountMap[classId] = 0
  }
}

// 获取学生数量
const getStudentCount = (classId) => {
  return studentCountMap[classId] || 0
}

// 查看学生列表
const handleViewStudents = async (row) => {
  currentClass.value = row
  studentDialogVisible.value = true
  studentLoading.value = true
  
  try {
    const { data } = await getMyClassStudents(row.id)
    studentList.value = data || []
  } catch (error) {
    console.error('获取学生列表失败:', error)
    ElMessage.error('获取学生列表失败')
  } finally {
    studentLoading.value = false
  }
}

// 查看详情
const handleViewDetail = (row) => {
  currentClass.value = row
  detailDialogVisible.value = true
}

// 刷新数据
const fetchData = () => {
  fetchClassList()
  fetchStatistics()
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.my-classes {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

.statistics-cards {
  margin-bottom: 20px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 20px;
}

.stat-icon {
  flex-shrink: 0;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.class-list-card {
  margin-top: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.dialog-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>

