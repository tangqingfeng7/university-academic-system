<template>
  <div class="attendance-statistics">
    <el-card>
      <template #header>
        <span>考勤统计分析</span>
      </template>

      <el-tabs v-model="activeTab">
        <el-tab-pane label="院系统计" name="department">
          <el-form :inline="true" class="mb-4">
            <el-form-item label="院系">
              <el-select 
                v-model="deptQuery.departmentId" 
                placeholder="请选择院系"
                style="width: 200px"
              >
                <el-option 
                  v-for="dept in departments"
                  :key="dept.id"
                  :label="dept.name"
                  :value="dept.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="开始日期">
              <el-date-picker 
                v-model="deptQuery.startDate" 
                type="date"
                style="width: 180px"
              />
            </el-form-item>
            <el-form-item label="结束日期">
              <el-date-picker 
                v-model="deptQuery.endDate" 
                type="date"
                style="width: 180px"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadDepartmentStats">查询</el-button>
              <el-button @click="exportDepartmentData">导出</el-button>
            </el-form-item>
          </el-form>

          <el-descriptions v-if="departmentStats" :column="3" border>
            <el-descriptions-item label="考勤次数">{{ departmentStats.totalRecords }}</el-descriptions-item>
            <el-descriptions-item label="总学生人次">{{ departmentStats.totalStudents }}</el-descriptions-item>
            <el-descriptions-item label="平均出勤率">{{ departmentStats.avgAttendanceRate }}%</el-descriptions-item>
            <el-descriptions-item label="出勤人次">{{ departmentStats.totalPresent }}</el-descriptions-item>
            <el-descriptions-item label="迟到人次">{{ departmentStats.totalLate }}</el-descriptions-item>
            <el-descriptions-item label="旷课人次">{{ departmentStats.totalAbsent }}</el-descriptions-item>
          </el-descriptions>
        </el-tab-pane>

        <el-tab-pane label="教师统计" name="teacher">
          <el-form :inline="true" class="mb-4">
            <el-form-item label="教师">
              <el-select 
                v-model="teacherQuery.teacherId" 
                placeholder="请选择教师"
                style="width: 200px"
              >
                <el-option 
                  v-for="teacher in teachers"
                  :key="teacher.id"
                  :label="teacher.name"
                  :value="teacher.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="开始日期">
              <el-date-picker 
                v-model="teacherQuery.startDate" 
                type="date"
                style="width: 180px"
              />
            </el-form-item>
            <el-form-item label="结束日期">
              <el-date-picker 
                v-model="teacherQuery.endDate" 
                type="date"
                style="width: 180px"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadTeacherStats">查询</el-button>
              <el-button @click="exportTeacherData">导出</el-button>
            </el-form-item>
          </el-form>

          <el-descriptions v-if="teacherStats" :column="2" border>
            <el-descriptions-item label="考勤次数">{{ teacherStats.totalRecords }}</el-descriptions-item>
            <el-descriptions-item label="平均出勤率">{{ teacherStats.avgAttendanceRate }}%</el-descriptions-item>
          </el-descriptions>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getDepartmentStatistics,
  getTeacherStatistics,
  exportDepartmentStatistics,
  exportTeacherStatistics
} from '@/api/attendance'
import { getAllDepartments } from '@/api/department'
import { getAllTeachers } from '@/api/teacher'
import { formatDateOnly } from '@/utils/date'

const activeTab = ref('department')
const departments = ref([])
const teachers = ref([])

const deptQuery = ref({
  departmentId: null,
  startDate: null,
  endDate: null
})

const teacherQuery = ref({
  teacherId: null,
  startDate: null,
  endDate: null
})

const departmentStats = ref(null)
const teacherStats = ref(null)

onMounted(() => {
  loadDepartments()
  loadTeachers()
  
  // 设置默认日期为最近30天
  const now = new Date()
  const monthAgo = new Date(now.getTime() - 30 * 24 * 60 * 60 * 1000)
  deptQuery.value.endDate = now
  deptQuery.value.startDate = monthAgo
  teacherQuery.value.endDate = now
  teacherQuery.value.startDate = monthAgo
})

const loadDepartments = async () => {
  try {
    const res = await getAllDepartments()
    departments.value = res.data || []
  } catch (error) {
    console.error('加载院系失败', error)
  }
}

const loadTeachers = async () => {
  try {
    const res = await getAllTeachers()
    teachers.value = res.data || []
  } catch (error) {
    console.error('加载教师失败', error)
  }
}

const loadDepartmentStats = async () => {
  if (!deptQuery.value.departmentId) {
    ElMessage.warning('请选择院系')
    return
  }
  if (!deptQuery.value.startDate || !deptQuery.value.endDate) {
    ElMessage.warning('请选择开始日期和结束日期')
    return
  }

  try {
    const params = {
      departmentId: deptQuery.value.departmentId,
      startDate: formatDateOnly(deptQuery.value.startDate),
      endDate: formatDateOnly(deptQuery.value.endDate)
    }
    const res = await getDepartmentStatistics(params)
    departmentStats.value = res.data || null
  } catch (error) {
    ElMessage.error('加载统计数据失败')
  }
}

const loadTeacherStats = async () => {
  if (!teacherQuery.value.teacherId) {
    ElMessage.warning('请选择教师')
    return
  }

  try {
    const params = {
      teacherId: teacherQuery.value.teacherId
    }
    // 只有当日期存在时才添加到参数中
    if (teacherQuery.value.startDate) {
      params.startDate = formatDateOnly(teacherQuery.value.startDate)
    }
    if (teacherQuery.value.endDate) {
      params.endDate = formatDateOnly(teacherQuery.value.endDate)
    }
    const res = await getTeacherStatistics(params)
    teacherStats.value = res.data || null
  } catch (error) {
    ElMessage.error('加载统计数据失败')
  }
}

const exportDepartmentData = async () => {
  if (!deptQuery.value.departmentId) {
    ElMessage.warning('请选择院系')
    return
  }
  if (!deptQuery.value.startDate || !deptQuery.value.endDate) {
    ElMessage.warning('请选择开始日期和结束日期')
    return
  }

  try {
    const params = {
      departmentId: deptQuery.value.departmentId,
      startDate: formatDateOnly(deptQuery.value.startDate),
      endDate: formatDateOnly(deptQuery.value.endDate)
    }
    const res = await exportDepartmentStatistics(params)
    downloadFile(res, '院系考勤统计.xlsx')
  } catch (error) {
    ElMessage.error('导出失败')
  }
}

const exportTeacherData = async () => {
  if (!teacherQuery.value.teacherId) {
    ElMessage.warning('请选择教师')
    return
  }
  if (!teacherQuery.value.startDate || !teacherQuery.value.endDate) {
    ElMessage.warning('请选择开始日期和结束日期')
    return
  }

  try {
    const params = {
      teacherId: teacherQuery.value.teacherId,
      startDate: formatDateOnly(teacherQuery.value.startDate),
      endDate: formatDateOnly(teacherQuery.value.endDate)
    }
    const res = await exportTeacherStatistics(params)
    downloadFile(res, '教师考勤统计.xlsx')
  } catch (error) {
    ElMessage.error('导出失败')
  }
}

const downloadFile = (data, filename) => {
  const url = window.URL.createObjectURL(new Blob([data]))
  const link = document.createElement('a')
  link.href = url
  link.setAttribute('download', filename)
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}
</script>

<style scoped>
.mb-4 {
  margin-bottom: 16px;
}

.attendance-statistics {
  padding: 24px;
}

.attendance-statistics :deep(.el-card) {
  overflow: visible;
}

.attendance-statistics :deep(.el-card__body) {
  overflow: visible;
}

.attendance-statistics :deep(.el-tabs__content) {
  overflow: visible;
}

.attendance-statistics :deep(.el-tab-pane) {
  overflow: visible;
}
</style>

