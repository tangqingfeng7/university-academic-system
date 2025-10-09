<template>
  <div class="exam-invigilator-manager">
    <!-- 操作按钮 -->
    <div class="action-bar">
      <el-button 
        type="primary" 
        @click="handleAddInvigilator"
        :disabled="examStatus !== 'DRAFT' || !hasRooms"
      >
        <el-icon><Plus /></el-icon>
        添加监考
      </el-button>
      <el-alert
        v-if="!hasRooms"
        title="请先创建考场，才能安排监考"
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
              <div class="invigilator-stats">
                <el-tag 
                  v-if="room.invigilators && room.invigilators.length > 0"
                  type="success" 
                  size="small"
                >
                  已安排 {{ room.invigilators.length }} 位监考
                </el-tag>
                <el-tag v-else type="warning" size="small">
                  未安排监考
                </el-tag>
              </div>
            </div>
          </template>

          <!-- 考场监考列表 -->
          <el-table 
            :data="room.invigilators || []" 
            border
            size="small"
          >
            <el-table-column prop="teacher.teacherNo" label="教师编号" width="120" />
            <el-table-column prop="teacher.name" label="教师姓名" width="120" />
            <el-table-column label="监考类型" width="120">
              <template #default="{ row }">
                <el-tag :type="row.type === 'CHIEF' ? 'primary' : 'info'">
                  {{ row.type === 'CHIEF' ? '主监考' : '副监考' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="院系" min-width="150">
              <template #default="{ row }">
                {{ row.teacher.department?.name || '-' }}
              </template>
            </el-table-column>
            <el-table-column label="联系电话" width="130">
              <template #default="{ row }">
                {{ row.teacher.phone || '-' }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100" fixed="right">
              <template #default="{ row }">
                <el-button 
                  link 
                  type="danger" 
                  @click="handleRemoveInvigilator(room, row)"
                  :disabled="examStatus !== 'DRAFT'"
                >
                  移除
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <!-- 添加监考按钮 -->
          <div style="margin-top: 10px" v-if="examStatus === 'DRAFT'">
            <el-button 
              type="primary" 
              size="small" 
              @click="handleAddToRoom(room)"
            >
              <el-icon><Plus /></el-icon>
              为此考场添加监考
            </el-button>
          </div>
        </el-collapse-item>
      </el-collapse>
    </div>

    <!-- 添加监考对话框 -->
    <el-dialog
      v-model="invigilatorDialogVisible"
      title="添加监考教师"
      width="600px"
    >
      <el-form label-width="100px">
        <el-form-item label="选择考场" v-if="!selectedRoom">
          <el-select v-model="selectedRoomId" placeholder="请选择考场" style="width: 100%">
            <el-option
              v-for="room in roomList"
              :key="room.id"
              :label="`${room.roomName} (${room.location})`"
              :value="room.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="考场" v-else>
          <el-tag>{{ selectedRoom.roomName }} ({{ selectedRoom.location }})</el-tag>
        </el-form-item>
        <el-form-item label="选择教师">
          <el-select
            v-model="selectedTeacherId"
            placeholder="请选择教师"
            filterable
            style="width: 100%"
          >
            <el-option
              v-for="teacher in teacherList"
              :key="teacher.id"
              :label="`${teacher.teacherNo} - ${teacher.name} (${teacher.department?.name || ''})`"
              :value="teacher.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="监考类型">
          <el-radio-group v-model="invigilatorType">
            <el-radio label="CHIEF">主监考</el-radio>
            <el-radio label="ASSISTANT">副监考</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="invigilatorDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleInvigilatorSubmit" :loading="invigilatorLoading">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import {
  getExamRooms,
  getExamRoomInvigilators,
  addInvigilator,
  removeInvigilator
} from '@/api/exam'
import { getAllTeachers } from '@/api/teacher'

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
const teacherList = ref([])

// 添加监考对话框
const invigilatorDialogVisible = ref(false)
const selectedRoomId = ref(null)
const selectedRoom = ref(null)
const selectedTeacherId = ref(null)
const invigilatorType = ref('CHIEF')
const invigilatorLoading = ref(false)

// ==================== 计算属性 ====================

const hasRooms = computed(() => roomList.value.length > 0)

// ==================== 数据加载 ====================

const fetchRoomList = async () => {
  try {
    const res = await getExamRooms(props.examId)
    roomList.value = res.data

    // 加载每个考场的监考安排
    for (const room of roomList.value) {
      const invigilatorsRes = await getExamRoomInvigilators(room.id)
      room.invigilators = invigilatorsRes.data
    }
  } catch (error) {
    console.error('获取考场列表失败:', error)
    ElMessage.error('获取考场列表失败')
  }
}

const fetchTeacherList = async () => {
  try {
    const res = await getAllTeachers()
    teacherList.value = res.data
  } catch (error) {
    console.error('获取教师列表失败:', error)
  }
}

// ==================== 事件处理 ====================

const handleAddInvigilator = () => {
  selectedRoom.value = null
  selectedRoomId.value = null
  selectedTeacherId.value = null
  invigilatorType.value = 'CHIEF'
  invigilatorDialogVisible.value = true
}

const handleAddToRoom = (room) => {
  selectedRoom.value = room
  selectedRoomId.value = room.id
  selectedTeacherId.value = null
  invigilatorType.value = 'CHIEF'
  invigilatorDialogVisible.value = true
}

const handleInvigilatorSubmit = async () => {
  const roomId = selectedRoomId.value
  if (!roomId) {
    ElMessage.warning('请选择考场')
    return
  }
  if (!selectedTeacherId.value) {
    ElMessage.warning('请选择教师')
    return
  }

  try {
    invigilatorLoading.value = true
    await addInvigilator(roomId, {
      teacherId: selectedTeacherId.value,
      type: invigilatorType.value
    })
    ElMessage.success('监考教师添加成功')
    invigilatorDialogVisible.value = false
    await fetchRoomList()
    emit('refresh')
  } catch (error) {
    console.error('添加失败:', error)
    ElMessage.error(error.message || '添加失败')
  } finally {
    invigilatorLoading.value = false
  }
}

const handleRemoveInvigilator = async (room, invigilator) => {
  try {
    await ElMessageBox.confirm(
      `确定要将教师"${invigilator.teacher.name}"从考场"${room.roomName}"的监考安排中移除吗？`,
      '移除监考',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await removeInvigilator(invigilator.id)
    ElMessage.success('监考教师移除成功')
    await fetchRoomList()
    emit('refresh')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('移除失败:', error)
      ElMessage.error(error.message || '移除失败')
    }
  }
}

// ==================== 生命周期 ====================

onMounted(async () => {
  await fetchRoomList()
  await fetchTeacherList()
})
</script>

<style scoped>
.exam-invigilator-manager {
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

.invigilator-stats {
  display: flex;
  align-items: center;
  gap: 10px;
}
</style>

