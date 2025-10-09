<template>
  <div class="course-selection">
    <el-tabs v-model="activeTab" @tab-click="handleTabClick">
      <!-- 可选课程 -->
      <el-tab-pane label="可选课程" name="available">
        <div class="tab-content">
          <!-- 统计信息 -->
          <el-alert
            v-if="statistics"
            :title="`已选课程：${statistics.totalCourses}门，共${statistics.totalCredits}学分`"
            type="info"
            :closable="false"
            style="margin-bottom: 20px"
          />

          <!-- 搜索栏 -->
          <div class="search-bar">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索课程名称或教师"
              clearable
              style="width: 300px"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </div>

          <!-- 空状态提示 -->
          <el-empty
            v-if="!loading && availableOfferings.length === 0"
            description="当前暂无可选课程"
          >
            <template #default>
              <el-alert
                title="提示"
                type="info"
                :closable="false"
              >
                <template #default>
                  <div>可能的原因：</div>
                  <ul style="margin: 10px 0; padding-left: 20px;">
                    <li>选课功能暂未开放</li>
                    <li>当前不在选课时间范围内</li>
                    <li>本学期暂无已发布的开课计划</li>
                  </ul>
                  <div style="color: #909399;">请联系管理员了解详情</div>
                </template>
              </el-alert>
            </template>
          </el-empty>

          <!-- 课程列表 -->
          <el-table
            v-if="availableOfferings.length > 0"
            v-loading="loading"
            :data="filteredOfferings"
            stripe
            border
          >
            <el-table-column prop="courseNo" label="课程编号" width="120" />
            <el-table-column prop="courseName" label="课程名称" min-width="200" />
            <el-table-column prop="teacherName" label="教师" width="120" />
            <el-table-column prop="credits" label="学分" width="80" align="center" />
            <el-table-column label="上课时间" min-width="200">
              <template #default="{ row }">
                <div v-if="row.scheduleText">{{ row.scheduleText }}</div>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column prop="location" label="上课地点" width="150" />
            <el-table-column label="名额" width="150" align="center">
              <template #default="{ row }">
                <el-tag :type="getCapacityTagType(row)">
                  {{ row.enrolled }}/{{ row.capacity }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="{ row }">
                <el-button
                  type="primary"
                  size="small"
                  :disabled="isCourseFull(row) || isAlreadySelected(row)"
                  @click="handleSelect(row)"
                >
                  {{ isAlreadySelected(row) ? '已选' : '选课' }}
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>

      <!-- 已选课程 -->
      <el-tab-pane label="已选课程" name="selected">
        <div class="tab-content">
          <el-table
            v-loading="selectionLoading"
            :data="selectedCourses"
            stripe
            border
          >
            <el-table-column prop="courseNo" label="课程编号" width="120" />
            <el-table-column prop="courseName" label="课程名称" min-width="200" />
            <el-table-column prop="teacherName" label="教师" width="120" />
            <el-table-column prop="credits" label="学分" width="80" align="center" />
            <el-table-column label="上课时间" min-width="200">
              <template #default="{ row }">
                <div v-if="row.scheduleText">{{ row.scheduleText }}</div>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column prop="location" label="上课地点" width="150" />
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag v-if="row.status === 'SELECTED'" type="success">已选</el-tag>
                <el-tag v-else-if="row.status === 'DROPPED'" type="info">已退</el-tag>
                <el-tag v-else type="warning">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="selectionTime" label="选课时间" width="180" />
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="{ row }">
                <el-button
                  v-if="row.status === 'SELECTED'"
                  type="danger"
                  size="small"
                  plain
                  @click="handleDrop(row)"
                >
                  退课
                </el-button>
                <span v-else>-</span>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import {
  getAvailableOfferings,
  getMySelections,
  selectCourse,
  dropCourse,
  getSelectionStatistics
} from '@/api/courseSelection'

const activeTab = ref('available')
const loading = ref(false)
const selectionLoading = ref(false)
const searchKeyword = ref('')
const availableOfferings = ref([])
const selectedCourses = ref([])
const statistics = ref(null)

// 解析课程时间
const parseSchedule = (scheduleJson) => {
  if (!scheduleJson) return ''
  
  try {
    const schedule = JSON.parse(scheduleJson)
    if (!Array.isArray(schedule) || schedule.length === 0) return ''
    
    const weekDays = ['', '周一', '周二', '周三', '周四', '周五', '周六', '周日']
    return schedule.map(item => {
      const day = weekDays[item.day] || ''
      const period = item.period || ''
      const weeks = Array.isArray(item.weeks) && item.weeks.length > 0 
        ? `第${item.weeks[0]}-${item.weeks[item.weeks.length - 1]}周` 
        : ''
      return `${day} ${period}节 ${weeks}`
    }).join('; ')
  } catch (error) {
    return ''
  }
}

// 过滤课程列表
const filteredOfferings = computed(() => {
  if (!searchKeyword.value) return availableOfferings.value
  
  const keyword = searchKeyword.value.toLowerCase()
  return availableOfferings.value.filter(offering => 
    offering.courseName?.toLowerCase().includes(keyword) ||
    offering.teacherName?.toLowerCase().includes(keyword) ||
    offering.courseNo?.toLowerCase().includes(keyword)
  )
})

// 获取容量标签类型
const getCapacityTagType = (offering) => {
  const rate = offering.enrolled / offering.capacity
  if (rate >= 1) return 'danger'
  if (rate >= 0.8) return 'warning'
  return 'success'
}

// 判断课程是否已满
const isCourseFull = (offering) => {
  return offering.enrolled >= offering.capacity
}

// 判断课程是否已选
const isAlreadySelected = (offering) => {
  return selectedCourses.value.some(
    sel => sel.offeringId === offering.id && sel.status === 'SELECTED'
  )
}

// 获取可选课程列表
const fetchAvailableOfferings = async () => {
  try {
    loading.value = true
    const { data } = await getAvailableOfferings()
    availableOfferings.value = (data || []).map(offering => ({
      ...offering,
      scheduleText: parseSchedule(offering.schedule)
    }))
  } catch (error) {
    console.error('获取可选课程失败:', error)
    ElMessage.error('获取可选课程失败')
  } finally {
    loading.value = false
  }
}

// 获取已选课程列表
const fetchSelectedCourses = async () => {
  try {
    selectionLoading.value = true
    const { data } = await getMySelections()
    selectedCourses.value = (data || []).map(selection => ({
      ...selection,
      scheduleText: parseSchedule(selection.schedule)
    }))
  } catch (error) {
    console.error('获取已选课程失败:', error)
    ElMessage.error('获取已选课程失败')
  } finally {
    selectionLoading.value = false
  }
}

// 获取统计信息
const fetchStatistics = async () => {
  try {
    const { data } = await getSelectionStatistics()
    statistics.value = data
  } catch (error) {
    console.error('获取统计信息失败:', error)
  }
}

// 选课
const handleSelect = (offering) => {
  ElMessageBox.confirm(
    `确定要选择《${offering.courseName}》吗？`,
    '确认选课',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await selectCourse(offering.id)
      ElMessage.success('选课成功')
      
      // 刷新数据
      await Promise.all([
        fetchAvailableOfferings(),
        fetchSelectedCourses(),
        fetchStatistics()
      ])
    } catch (error) {
      console.error('选课失败:', error)
      const errorMsg = error.response?.data?.message || '选课失败'
      ElMessage.error(errorMsg)
    }
  }).catch(() => {})
}

// 退课
const handleDrop = (selection) => {
  ElMessageBox.confirm(
    `确定要退选《${selection.courseName}》吗？退课后可能无法再次选择该课程。`,
    '确认退课',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await dropCourse(selection.id)
      ElMessage.success('退课成功')
      
      // 刷新数据
      await Promise.all([
        fetchAvailableOfferings(),
        fetchSelectedCourses(),
        fetchStatistics()
      ])
    } catch (error) {
      console.error('退课失败:', error)
      const errorMsg = error.response?.data?.message || '退课失败'
      ElMessage.error(errorMsg)
    }
  }).catch(() => {})
}

// 标签切换
const handleTabClick = (tab) => {
  if (tab.props.name === 'available') {
    fetchAvailableOfferings()
  } else if (tab.props.name === 'selected') {
    fetchSelectedCourses()
  }
}

onMounted(() => {
  fetchAvailableOfferings()
  fetchStatistics()
})
</script>

<style scoped>
.course-selection {
  padding: 20px;
}

.tab-content {
  margin-top: 20px;
}

.search-bar {
  margin-bottom: 20px;
}
</style>

