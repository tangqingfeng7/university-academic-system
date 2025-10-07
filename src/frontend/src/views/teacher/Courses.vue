<template>
  <div class="teacher-courses-container">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true">
        <el-form-item label="学期筛选">
          <el-select
            v-model="selectedSemester"
            placeholder="请选择学期（默认显示全部）"
            clearable
            @change="fetchCourseList"
            style="width: 250px"
          >
            <el-option label="全部学期" :value="null" />
            <el-option
              v-for="semester in semesters"
              :key="semester.id"
              :label="semester.academicYear + ' ' + getSemesterTypeName(semester.semesterType)"
              :value="semester.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 课程列表 -->
    <el-card style="margin-top: 20px">
      <template #header>
        <span class="card-title">我的授课班级</span>
      </template>

      <el-table
        v-loading="loading"
        :data="courseList"
        stripe
        style="width: 100%"
      >
        <el-table-column type="index" label="序号" width="60" />
        
        <el-table-column prop="semesterName" label="学期" width="150">
          <template #default="{ row }">
            {{ row.semesterName }}
          </template>
        </el-table-column>

        <el-table-column prop="courseNo" label="课程编号" width="120" />
        
        <el-table-column prop="courseName" label="课程名称" min-width="200" />
        
        <el-table-column prop="courseType" label="课程类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getCourseTypeTag(row.courseType)">
              {{ getCourseTypeName(row.courseType) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="schedule" label="上课时间" min-width="180">
          <template #default="{ row }">
            {{ formatSchedule(row.schedule) }}
          </template>
        </el-table-column>

        <el-table-column prop="location" label="上课地点" width="120" />

        <el-table-column label="选课人数" width="120" align="center">
          <template #default="{ row }">
            <el-tag type="info">
              {{ row.enrolled }} / {{ row.capacity }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusTag(row.status)">
              {{ getStatusName(row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              @click="handleViewStudents(row)"
            >
              <el-icon><User /></el-icon>
              学生名单
            </el-button>
            <el-button
              type="success"
              size="small"
              @click="handleGradeInput(row)"
            >
              <el-icon><Edit /></el-icon>
              成绩管理
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="courseList.length === 0 && !loading" class="empty-text">
        暂无授课班级
      </div>
    </el-card>

    <!-- 学生名单对话框 -->
    <el-dialog
      v-model="studentDialogVisible"
      title="班级学生名单"
      width="70%"
      :close-on-click-modal="false"
    >
      <div v-if="currentCourse" class="course-info">
        <p><strong>课程：</strong>{{ currentCourse.courseName }}</p>
        <p><strong>学期：</strong>{{ currentCourse.semesterName }}</p>
        <p><strong>选课人数：</strong>{{ studentList.length }}</p>
      </div>

      <el-table
        v-loading="studentLoading"
        :data="studentList"
        stripe
        max-height="500"
      >
        <el-table-column type="index" label="序号" width="60" />
        
        <el-table-column prop="studentNo" label="学号" width="120" />
        
        <el-table-column prop="name" label="姓名" width="120" />
        
        <el-table-column prop="gender" label="性别" width="80">
          <template #default="{ row }">
            {{ row.gender === 'MALE' ? '男' : '女' }}
          </template>
        </el-table-column>

        <el-table-column prop="majorName" label="专业" min-width="150" />
        
        <el-table-column prop="className" label="班级" width="120" />
        
        <el-table-column prop="phone" label="联系电话" width="130" />
      </el-table>

      <template #footer>
        <el-button @click="studentDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Edit } from '@element-plus/icons-vue'
import { getMyCourses, getCourseStudents } from '@/api/teacherCourse'
import { getAllSemesters } from '@/api/semester'

const router = useRouter()

// 数据
const loading = ref(false)
const studentLoading = ref(false)
const courseList = ref([])
const semesters = ref([])
const selectedSemester = ref(null)
const studentDialogVisible = ref(false)
const currentCourse = ref(null)
const studentList = ref([])

// 获取学期列表
const fetchSemesters = async () => {
  try {
    const res = await getAllSemesters()
    semesters.value = res.data || []
  } catch (error) {
    console.error('获取学期列表失败:', error)
  }
}

// 获取授课列表
const fetchCourseList = async () => {
  loading.value = true
  try {
    const res = await getMyCourses(selectedSemester.value)
    courseList.value = res.data || []
  } catch (error) {
    console.error('获取授课列表失败:', error)
    ElMessage.error('获取授课列表失败: ' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// 查看学生名单
const handleViewStudents = async (course) => {
  currentCourse.value = course
  studentDialogVisible.value = true
  studentLoading.value = true
  
  try {
    const res = await getCourseStudents(course.id)
    studentList.value = res.data || []
  } catch (error) {
    console.error('获取学生名单失败:', error)
    ElMessage.error('获取学生名单失败: ' + (error.message || '未知错误'))
  } finally {
    studentLoading.value = false
  }
}

// 格式化学期类型
const getSemesterTypeName = (type) => {
  const typeMap = {
    1: '春季学期',
    2: '秋季学期'
  }
  return typeMap[type] || '未知'
}

// 格式化课程类型
const getCourseTypeName = (type) => {
  const typeMap = {
    REQUIRED: '必修',
    ELECTIVE: '选修',
    PUBLIC: '公共'
  }
  return typeMap[type] || type
}

// 课程类型标签颜色
const getCourseTypeTag = (type) => {
  const tagMap = {
    REQUIRED: 'danger',
    ELECTIVE: 'warning',
    PUBLIC: 'info'
  }
  return tagMap[type] || ''
}

// 格式化课程表
const formatSchedule = (schedule) => {
  if (!schedule) return '-'
  
  try {
    const schedules = JSON.parse(schedule)
    if (!Array.isArray(schedules) || schedules.length === 0) return '-'
    
    const dayNames = ['', '周一', '周二', '周三', '周四', '周五', '周六', '周日']
    
    return schedules.map(s => {
      const dayName = dayNames[s.day] || `周${s.day}`
      const weeks = s.weeks ? `(${s.weeks})` : ''
      return `${dayName} 第${s.period}节 ${weeks}`
    }).join(', ')
  } catch (e) {
    return schedule
  }
}

// 状态标签颜色
const getStatusTag = (status) => {
  const tagMap = {
    DRAFT: 'info',
    PUBLISHED: 'success',
    CANCELLED: 'danger'
  }
  return tagMap[status] || ''
}

// 状态名称
const getStatusName = (status) => {
  const nameMap = {
    DRAFT: '草稿',
    PUBLISHED: '已发布',
    CANCELLED: '已取消'
  }
  return nameMap[status] || status
}

// 成绩管理
const handleGradeInput = (row) => {
  router.push({
    name: 'TeacherGradeInput',
    params: { id: row.id }
  })
}

// 页面加载
onMounted(() => {
  fetchSemesters()
  fetchCourseList()
})
</script>

<style scoped>
.teacher-courses-container {
  padding: 20px;
}

.card-title {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
}

.empty-text {
  text-align: center;
  padding: 40px 0;
  color: #909399;
  font-size: 14px;
}

.course-info {
  padding: 15px;
  background: #f5f7fa;
  border-radius: 4px;
  margin-bottom: 20px;
}

.course-info p {
  margin: 8px 0;
  color: #606266;
}

.course-info strong {
  color: #303133;
  margin-right: 10px;
}
</style>

