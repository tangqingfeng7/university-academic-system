<template>
  <div class="exam-room-manager">
    <!-- 操作按钮 -->
    <div class="action-bar">
      <el-button 
        type="primary" 
        @click="handleAddRoom"
        :disabled="examStatus !== 'DRAFT'"
      >
        <el-icon><Plus /></el-icon>
        添加考场
      </el-button>
      <el-button 
        type="success" 
        @click="handleAutoCreate"
        :disabled="examStatus !== 'DRAFT'"
      >
        <el-icon><MagicStick /></el-icon>
        自动创建考场
      </el-button>
    </div>

    <!-- 考场列表 -->
    <el-table 
      :data="roomList" 
      border 
      v-loading="loading"
      style="margin-top: 20px"
    >
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="roomName" label="考场名称" width="150" />
      <el-table-column prop="location" label="考场地点" width="200" />
      <el-table-column label="容量" width="200">
        <template #default="{ row }">
          <room-capacity-progress 
            :capacity="row.capacity" 
            :assignedCount="row.assignedCount"
            :showText="true"
          />
        </template>
      </el-table-column>
      <el-table-column label="监考教师" min-width="200">
        <template #default="{ row }">
          <el-tag 
            v-for="inv in row.invigilators" 
            :key="inv.id"
            :type="inv.type === 'CHIEF' ? 'primary' : 'info'"
            size="small"
            style="margin-right: 5px"
          >
            {{ inv.teacher.name }} ({{ inv.type === 'CHIEF' ? '主监考' : '副监考' }})
          </el-tag>
          <span v-if="!row.invigilators || row.invigilators.length === 0" class="text-secondary">
            暂无监考
          </span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleViewStudents(row)">
            学生
          </el-button>
          <el-button link type="primary" @click="handleEditRoom(row)" :disabled="examStatus !== 'DRAFT'">
            编辑
          </el-button>
          <el-button link type="danger" @click="handleDeleteRoom(row)" :disabled="examStatus !== 'DRAFT'">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 添加/编辑考场对话框 -->
    <el-dialog
      v-model="roomDialogVisible"
      :title="roomDialogMode === 'add' ? '添加考场' : '编辑考场'"
      width="600px"
    >
      <el-form
        ref="roomFormRef"
        :model="roomForm"
        :rules="roomFormRules"
        label-width="100px"
      >
        <el-form-item label="考场名称" prop="roomName">
          <el-input v-model="roomForm.roomName" placeholder="如：教学楼A101" />
        </el-form-item>
        <el-form-item label="考场地点" prop="location">
          <el-input v-model="roomForm.location" placeholder="如：东区教学楼A栋1层" />
        </el-form-item>
        <el-form-item label="考场容量" prop="capacity">
          <el-input-number v-model="roomForm.capacity" :min="1" :max="500" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="roomDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleRoomSubmit" :loading="roomSubmitLoading">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 自动创建考场对话框 -->
    <el-dialog
      v-model="autoCreateDialogVisible"
      title="自动创建考场"
      width="500px"
    >
      <el-form label-width="120px">
        <el-form-item label="考场数量">
          <el-input-number v-model="autoCreateCount" :min="1" :max="20" />
        </el-form-item>
        <el-form-item label="每个考场容量">
          <el-input-number v-model="autoCreateCapacity" :min="10" :max="200" />
        </el-form-item>
        <el-form-item label="考场名称前缀">
          <el-input v-model="autoCreatePrefix" placeholder="如：教学楼A" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="autoCreateDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAutoCreateSubmit" :loading="autoCreateLoading">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 查看学生对话框 -->
    <el-dialog
      v-model="studentsDialogVisible"
      title="考场学生列表"
      width="800px"
    >
      <el-table :data="currentRoomStudents" border>
        <el-table-column prop="student.studentNo" label="学号" width="120" />
        <el-table-column prop="student.name" label="姓名" width="100" />
        <el-table-column prop="seatNumber" label="座位号" width="100" />
        <el-table-column label="专业" min-width="150">
          <template #default="{ row }">
            {{ row.student.major?.name || '-' }}
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, MagicStick } from '@element-plus/icons-vue'
import RoomCapacityProgress from '@/components/RoomCapacityProgress.vue'
import {
  getExamRooms,
  createExamRoom,
  updateExamRoom,
  deleteExamRoom,
  autoCreateExamRooms,
  getExamRoomStudents
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
const loading = ref(false)

// 考场对话框
const roomDialogVisible = ref(false)
const roomDialogMode = ref('add')
const roomFormRef = ref(null)
const roomForm = ref({
  roomName: '',
  location: '',
  capacity: 50
})
// 自定义验证器：验证考场容量
const validateCapacity = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入考场容量'))
  } else if (typeof value !== 'number') {
    callback(new Error('考场容量必须是数字'))
  } else if (value < 1) {
    callback(new Error('考场容量必须大于0'))
  } else if (value > 500) {
    callback(new Error('考场容量不能超过500'))
  } else {
    callback()
  }
}

const roomFormRules = {
  roomName: [
    { required: true, message: '请输入考场名称', trigger: 'blur' },
    { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  location: [
    { required: true, message: '请输入考场地点', trigger: 'blur' },
    { min: 2, max: 200, message: '长度在 2 到 200 个字符', trigger: 'blur' }
  ],
  capacity: [
    { required: true, message: '请输入考场容量', trigger: 'blur' },
    { validator: validateCapacity, trigger: 'blur' }
  ]
}
const roomSubmitLoading = ref(false)
const currentRoomId = ref(null)

// 自动创建对话框
const autoCreateDialogVisible = ref(false)
const autoCreateCount = ref(3)
const autoCreateCapacity = ref(50)
const autoCreatePrefix = ref('考场')
const autoCreateLoading = ref(false)

// 学生列表对话框
const studentsDialogVisible = ref(false)
const currentRoomStudents = ref([])

// ==================== 数据加载 ====================

const fetchRoomList = async () => {
  try {
    loading.value = true
    const res = await getExamRooms(props.examId)
    roomList.value = res.data
  } catch (error) {
    console.error('获取考场列表失败:', error)
    ElMessage.error('获取考场列表失败')
  } finally {
    loading.value = false
  }
}

// ==================== 事件处理 ====================

const handleAddRoom = () => {
  roomDialogMode.value = 'add'
  roomForm.value = {
    roomName: '',
    location: '',
    capacity: 50
  }
  currentRoomId.value = null
  roomDialogVisible.value = true
}

const handleEditRoom = (row) => {
  roomDialogMode.value = 'edit'
  roomForm.value = {
    roomName: row.roomName,
    location: row.location,
    capacity: row.capacity
  }
  currentRoomId.value = row.id
  roomDialogVisible.value = true
}

const handleRoomSubmit = async () => {
  try {
    await roomFormRef.value.validate()
    roomSubmitLoading.value = true

    if (roomDialogMode.value === 'add') {
      await createExamRoom({
        examId: props.examId,
        ...roomForm.value
      })
      ElMessage.success('考场添加成功')
    } else {
      await updateExamRoom(currentRoomId.value, roomForm.value)
      ElMessage.success('考场更新成功')
    }

    roomDialogVisible.value = false
    await fetchRoomList()
    emit('refresh')
  } catch (error) {
    if (error !== 'validation-failed') {
      console.error('操作失败:', error)
      ElMessage.error(error.message || '操作失败')
    }
  } finally {
    roomSubmitLoading.value = false
  }
}

const handleDeleteRoom = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除考场"${row.roomName}"吗？`,
      '删除考场',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await deleteExamRoom(row.id)
    ElMessage.success('考场删除成功')
    await fetchRoomList()
    emit('refresh')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error(error.message || '删除失败')
    }
  }
}

const handleAutoCreate = () => {
  autoCreateDialogVisible.value = true
}

const handleAutoCreateSubmit = async () => {
  try {
    autoCreateLoading.value = true
    
    const rooms = []
    for (let i = 1; i <= autoCreateCount.value; i++) {
      rooms.push({
        roomName: `${autoCreatePrefix.value}${i}`,
        location: `${autoCreatePrefix.value}${i}`,
        capacity: autoCreateCapacity.value
      })
    }

    await autoCreateExamRooms(props.examId, rooms)
    ElMessage.success('考场创建成功')
    autoCreateDialogVisible.value = false
    await fetchRoomList()
    emit('refresh')
  } catch (error) {
    console.error('自动创建失败:', error)
    ElMessage.error(error.message || '自动创建失败')
  } finally {
    autoCreateLoading.value = false
  }
}

const handleViewStudents = async (row) => {
  try {
    const res = await getExamRoomStudents(row.id)
    currentRoomStudents.value = res.data
    studentsDialogVisible.value = true
  } catch (error) {
    console.error('获取学生列表失败:', error)
    ElMessage.error('获取学生列表失败')
  }
}

// ==================== 生命周期 ====================

onMounted(() => {
  fetchRoomList()
})
</script>

<style scoped>
.exam-room-manager {
  padding: 20px 0;
}

.action-bar {
  display: flex;
  gap: 10px;
}

.text-secondary {
  color: #909399;
  font-size: 12px;
}
</style>

