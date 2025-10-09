<template>
  <div class="exam-student-assignment">
    <!-- 操作按钮 -->
    <div class="action-bar">
      <el-button 
        type="primary" 
        @click="handleAddStudent"
        :disabled="examStatus !== 'DRAFT' || !hasRooms"
      >
        <el-icon><Plus /></el-icon>
        分配学生
      </el-button>
      <el-button 
        type="success" 
        @click="handleAutoAssign"
        :disabled="examStatus !== 'DRAFT' || !hasRooms"
      >
        <el-icon><MagicStick /></el-icon>
        自动分配
      </el-button>
      <el-alert
        v-if="!hasRooms"
        title="请先创建考场，才能分配学生"
        type="warning"
        :closable="false"
        style="margin-left: 20px"
      />
    </div>

    <!-- 按考场分组显示 -->
    <div v-if="roomList.length > 0" class="room-groups">
      <el-collapse v-model="activeRooms" accordion>
        <el-collapse-item 
          v-for="room in roomList" 
          :key="room.id" 
          :name="room.id"
        >
          <template #title>
            <div class="room-header">
              <span class="room-name">{{ room.roomName }} ({{ room.location }})</span>
              <div class="room-stats">
                <el-tag type="info" size="small">
                  {{ room.assignedCount || 0 }} / {{ room.capacity }}
                </el-tag>
                <el-progress 
                  :percentage="getRoomPercentage(room)" 
                  :stroke-width="6"
                  :show-text="false"
                  style="width: 100px; margin-left: 10px"
                />
              </div>
            </div>
          </template>

          <!-- 考场学生列表 -->
          <el-table 
            :data="room.students || []" 
            border
            size="small"
          >
            <el-table-column prop="student.studentNo" label="学号" width="120" />
            <el-table-column prop="student.name" label="姓名" width="100" />
            <el-table-column prop="seatNumber" label="座位号" width="100" />
            <el-table-column label="专业" min-width="150">
              <template #default="{ row }">
                {{ row.student.major?.name || '-' }}
              </template>
            </el-table-column>
            <el-table-column label="班级" width="120">
              <template #default="{ row }">
                {{ row.student.className || '-' }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100" fixed="right">
              <template #default="{ row }">
                <el-button 
                  link 
                  type="danger" 
                  @click="handleRemoveStudent(room, row)"
                  :disabled="examStatus !== 'DRAFT'"
                >
                  移除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-collapse-item>
      </el-collapse>
    </div>

    <!-- 分配学生对话框 -->
    <el-dialog
      v-model="assignDialogVisible"
      title="分配学生到考场"
      width="700px"
    >
      <el-form label-width="100px">
        <el-form-item label="选择考场">
          <el-select v-model="selectedRoomId" placeholder="请选择考场" style="width: 100%">
            <el-option
              v-for="room in availableRooms"
              :key="room.id"
              :label="`${room.roomName} (剩余: ${room.capacity - (room.assignedCount || 0)})`"
              :value="room.id"
              :disabled="room.assignedCount >= room.capacity"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="选择学生">
          <el-select
            v-model="selectedStudentId"
            placeholder="请选择学生"
            filterable
            style="width: 100%"
          >
            <el-option
              v-for="student in unassignedStudents"
              :key="student.id"
              :label="`${student.studentNo} - ${student.name}`"
              :value="student.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="座位号">
          <el-input v-model="seatNumber" placeholder="如：A01" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAssignSubmit" :loading="assignLoading">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, MagicStick } from '@element-plus/icons-vue'
import {
  getExamRooms,
  getExamRoomStudents,
  assignStudentToRoom,
  removeStudentFromRoom,
  autoAssignStudents,
  getCourseStudents
} from '@/api/exam'

const props = defineProps({
  examId: {
    type: Number,
    required: true
  },
  examStatus: {
    type: String,
    required: true
  }
})

const emit = defineEmits(['refresh'])

// 数据
const roomList = ref([])
const activeRooms = ref(null)
const allStudents = ref([])

// 分配对话框
const assignDialogVisible = ref(false)
const selectedRoomId = ref(null)
const selectedStudentId = ref(null)
const seatNumber = ref('')
const assignLoading = ref(false)

// ==================== 计算属性 ====================

const hasRooms = computed(() => roomList.value.length > 0)

const availableRooms = computed(() => {
  return roomList.value.filter(room => (room.assignedCount || 0) < room.capacity)
})

const unassignedStudents = computed(() => {
  const assignedStudentIds = new Set()
  roomList.value.forEach(room => {
    if (room.students) {
      room.students.forEach(s => assignedStudentIds.add(s.student.id))
    }
  })
  return allStudents.value.filter(s => !assignedStudentIds.has(s.id))
})

// ==================== 工具函数 ====================

const getRoomPercentage = (room) => {
  if (room.capacity === 0) return 0
  return Math.round(((room.assignedCount || 0) / room.capacity) * 100)
}

// ==================== 数据加载 ====================

const fetchRoomList = async () => {
  try {
    const res = await getExamRooms(props.examId)
    roomList.value = res.data

    // 加载每个考场的学生
    for (const room of roomList.value) {
      const studentsRes = await getExamRoomStudents(room.id)
      room.students = studentsRes.data
    }
  } catch (error) {
    console.error('获取考场列表失败:', error)
    ElMessage.error('获取考场列表失败')
  }
}

const fetchCourseStudents = async () => {
  try {
    const res = await getCourseStudents(props.examId)
    allStudents.value = res.data
  } catch (error) {
    console.error('获取课程学生失败:', error)
  }
}

// ==================== 事件处理 ====================

const handleAddStudent = () => {
  selectedRoomId.value = null
  selectedStudentId.value = null
  seatNumber.value = ''
  assignDialogVisible.value = true
}

const handleAssignSubmit = async () => {
  if (!selectedRoomId.value) {
    ElMessage.warning('请选择考场')
    return
  }
  if (!selectedStudentId.value) {
    ElMessage.warning('请选择学生')
    return
  }
  if (!seatNumber.value) {
    ElMessage.warning('请输入座位号')
    return
  }

  try {
    assignLoading.value = true
    await assignStudentToRoom(selectedRoomId.value, {
      studentId: selectedStudentId.value,
      seatNumber: seatNumber.value
    })
    ElMessage.success('学生分配成功')
    assignDialogVisible.value = false
    await fetchRoomList()
    emit('refresh')
  } catch (error) {
    console.error('分配失败:', error)
    ElMessage.error(error.message || '分配失败')
  } finally {
    assignLoading.value = false
  }
}

const handleRemoveStudent = async (room, student) => {
  try {
    await ElMessageBox.confirm(
      `确定要将学生"${student.student.name}"从考场"${room.roomName}"移除吗？`,
      '移除学生',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await removeStudentFromRoom(room.id, student.student.id)
    ElMessage.success('学生移除成功')
    await fetchRoomList()
    emit('refresh')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('移除失败:', error)
      ElMessage.error(error.message || '移除失败')
    }
  }
}

const handleAutoAssign = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要自动分配学生到各个考场吗？系统将按学号顺序自动分配座位。',
      '自动分配',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await autoAssignStudents(props.examId)
    ElMessage.success('学生自动分配成功')
    await fetchRoomList()
    emit('refresh')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('自动分配失败:', error)
      ElMessage.error(error.message || '自动分配失败')
    }
  }
}

// ==================== 生命周期 ====================

onMounted(async () => {
  await fetchRoomList()
  await fetchCourseStudents()
})
</script>

<style scoped>
.exam-student-assignment {
  padding: 20px 0;
}

.action-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
}

.room-groups {
  margin-top: 20px;
}

.room-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  padding-right: 20px;
}

.room-name {
  font-weight: 600;
  font-size: 14px;
}

.room-stats {
  display: flex;
  align-items: center;
  gap: 10px;
}
</style>
