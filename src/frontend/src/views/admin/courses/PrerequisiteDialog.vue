<template>
  <el-dialog
    v-model="dialogVisible"
    :title="`设置先修课程 - ${courseName}`"
    width="700px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <div class="prerequisite-container">
      <!-- 已设置的先修课程列表 -->
      <div class="current-prerequisites">
        <h4>当前先修课程</h4>
        <el-table :data="currentPrerequisites" border style="width: 100%">
          <el-table-column prop="courseNo" label="课程编号" width="120" />
          <el-table-column prop="name" label="课程名称" />
          <el-table-column prop="credits" label="学分" width="80" />
          <el-table-column label="操作" width="100">
            <template #default="{ row }">
              <el-button
                link
                type="danger"
                size="small"
                @click="handleRemove(row)"
              >
                移除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="currentPrerequisites.length === 0" description="暂无先修课程" />
      </div>

      <!-- 选择器 -->
      <div class="add-prerequisite">
        <h4>添加先修课程</h4>
        <el-select
          v-model="selectedCourseId"
          filterable
          placeholder="请选择课程"
          style="width: 100%"
          @change="handleAdd"
        >
          <el-option
            v-for="course in availableCourses"
            :key="course.id"
            :label="`${course.courseNo} - ${course.name}`"
            :value="course.id"
          />
        </el-select>
      </div>
    </div>

    <template #footer>
      <el-button @click="handleClose">关闭</el-button>
      <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
        保存
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  getPrerequisites,
  setPrerequisites,
  getAllCourses
} from '@/api/course'

const props = defineProps({
  modelValue: {
    type: Boolean,
    required: true
  },
  courseId: {
    type: Number,
    default: null
  },
  courseName: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['update:modelValue', 'success'])

const dialogVisible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const submitLoading = ref(false)
const currentPrerequisites = ref([]) // 当前已设置的先修课程
const allCourses = ref([]) // 所有课程列表
const selectedCourseId = ref(null) // 选中的课程ID

// 可用的课程列表（排除当前课程和已选的先修课程）
const availableCourses = computed(() => {
  return allCourses.value.filter(course => {
    // 排除当前课程
    if (course.id === props.courseId) return false
    // 排除已选的先修课程
    if (currentPrerequisites.value.some(p => p.id === course.id)) return false
    return true
  })
})

// 获取当前先修课程列表
const fetchPrerequisites = async () => {
  if (!props.courseId) return
  
  try {
    const res = await getPrerequisites(props.courseId)
    currentPrerequisites.value = res.data
  } catch (error) {
    console.error('获取先修课程失败:', error)
    ElMessage.error('获取先修课程失败')
  }
}

// 获取所有课程列表
const fetchAllCourses = async () => {
  try {
    const res = await getAllCourses()
    allCourses.value = res.data
  } catch (error) {
    console.error('获取课程列表失败:', error)
  }
}

// 添加先修课程
const handleAdd = (courseId) => {
  const course = allCourses.value.find(c => c.id === courseId)
  if (course) {
    currentPrerequisites.value.push(course)
    selectedCourseId.value = null
  }
}

// 移除先修课程
const handleRemove = (course) => {
  const index = currentPrerequisites.value.findIndex(p => p.id === course.id)
  if (index > -1) {
    currentPrerequisites.value.splice(index, 1)
  }
}

// 保存先修课程设置
const handleSubmit = async () => {
  try {
    submitLoading.value = true
    
    const prerequisiteIds = currentPrerequisites.value.map(p => p.id)
    // 后端期望接收List<Long>数组，直接传递数组而非对象
    await setPrerequisites(props.courseId, prerequisiteIds)
    
    ElMessage.success('保存成功')
    emit('success')
    handleClose()
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败')
  } finally {
    submitLoading.value = false
  }
}

// 关闭对话框
const handleClose = () => {
  currentPrerequisites.value = []
  allCourses.value = []
  selectedCourseId.value = null
  dialogVisible.value = false
}

// 监听对话框打开
watch(() => props.modelValue, async (val) => {
  if (val && props.courseId) {
    await fetchAllCourses()
    await fetchPrerequisites()
  }
})
</script>

<style scoped lang="scss">
.prerequisite-container {
  .current-prerequisites {
    margin-bottom: 20px;

    h4 {
      margin-bottom: 10px;
      color: #303133;
    }

    .el-table {
      margin-bottom: 10px;
    }
  }

  .add-prerequisite {
    h4 {
      margin-bottom: 10px;
      color: #303133;
    }
  }
}
</style>

