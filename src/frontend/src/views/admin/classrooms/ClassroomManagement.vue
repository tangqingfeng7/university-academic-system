<template>
  <div class="classroom-management-container">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="queryParams">
        <el-form-item label="教室编号">
          <el-input
            v-model="queryParams.roomNo"
            placeholder="请输入教室编号"
            clearable
            style="width: 180px"
            @clear="handleSearch"
            @keyup.enter="handleSearch"
          />
        </el-form-item>

        <el-form-item label="楼栋">
          <el-input
            v-model="queryParams.building"
            placeholder="请输入楼栋"
            clearable
            style="width: 150px"
            @clear="handleSearch"
            @keyup.enter="handleSearch"
          />
        </el-form-item>

        <el-form-item label="教室类型">
          <el-select
            v-model="queryParams.type"
            placeholder="请选择类型"
            clearable
            style="width: 150px"
            @change="handleSearch"
          >
            <el-option label="普通教室" value="LECTURE" />
            <el-option label="实验室" value="LAB" />
            <el-option label="多媒体教室" value="MULTIMEDIA" />
            <el-option label="会议室" value="CONFERENCE" />
          </el-select>
        </el-form-item>

        <el-form-item label="状态">
          <el-select
            v-model="queryParams.status"
            placeholder="请选择状态"
            clearable
            style="width: 120px"
            @change="handleSearch"
          >
            <el-option label="可用" value="AVAILABLE" />
            <el-option label="维修中" value="MAINTENANCE" />
            <el-option label="停用" value="DISABLED" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 教室列表 -->
    <el-card style="margin-top: 20px">
      <template #header>
        <div class="card-header">
          <span class="card-title">教室列表</span>
          <div>
            <el-button
              type="danger"
              :disabled="selectedIds.length === 0"
              @click="handleBatchDelete"
            >
              <el-icon><Delete /></el-icon>
              批量删除
            </el-button>
            <el-button type="primary" @click="handleCreate">
              <el-icon><Plus /></el-icon>
              添加教室
            </el-button>
          </div>
        </div>
      </template>

      <el-table
        v-loading="loading"
        :data="classroomList"
        stripe
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column type="index" label="序号" width="60" />
        
        <el-table-column prop="roomNo" label="教室编号" width="120" sortable />
        
        <el-table-column prop="building" label="楼栋" width="120" sortable />
        
        <el-table-column prop="capacity" label="容量" width="100" align="center" sortable>
          <template #default="{ row }">
            <el-tag type="info">{{ row.capacity }}人</el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="type" label="教室类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getTypeTag(row.type)">
              {{ getTypeName(row.type) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="equipment" label="设备信息" min-width="200">
          <template #default="{ row }">
            <span v-if="row.equipment">{{ formatEquipment(row.equipment) }}</span>
            <span v-else class="text-muted">无特殊设备</span>
          </template>
        </el-table-column>

        <el-table-column prop="status" label="状态" width="120" align="center">
          <template #default="{ row }">
            <el-select
              :model-value="row.status"
              size="small"
              @change="(val) => handleStatusChange(row, val)"
            >
              <el-option label="可用" value="AVAILABLE" />
              <el-option label="维修中" value="MAINTENANCE" />
              <el-option label="停用" value="DISABLED" />
            </el-select>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="320" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              @click="handleViewUsage(row)"
            >
              <el-icon><Calendar /></el-icon>
              使用情况
            </el-button>
            <el-button
              type="success"
              size="small"
              @click="handleEdit(row)"
            >
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button
              type="danger"
              size="small"
              @click="handleDelete(row)"
            >
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 教室表单对话框 -->
    <el-dialog
      v-model="formDialogVisible"
      :title="isEdit ? '编辑教室' : '添加教室'"
      width="600px"
      @close="handleCloseFormDialog"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="教室编号" prop="roomNo">
          <el-input
            v-model="form.roomNo"
            placeholder="如：A101"
            maxlength="20"
          />
        </el-form-item>

        <el-form-item label="楼栋" prop="building">
          <el-input
            v-model="form.building"
            placeholder="如：A栋教学楼"
            maxlength="50"
          />
        </el-form-item>

        <el-form-item label="容量" prop="capacity">
          <el-input-number
            v-model="form.capacity"
            :min="1"
            :max="500"
            placeholder="容量"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="教室类型" prop="type">
          <el-select
            v-model="form.type"
            placeholder="请选择类型"
            style="width: 100%"
          >
            <el-option label="普通教室" value="LECTURE" />
            <el-option label="实验室" value="LAB" />
            <el-option label="多媒体教室" value="MULTIMEDIA" />
            <el-option label="会议室" value="CONFERENCE" />
          </el-select>
        </el-form-item>

        <el-form-item label="设备信息">
          <el-checkbox-group v-model="equipmentList">
            <el-checkbox label="投影仪" />
            <el-checkbox label="电脑" />
            <el-checkbox label="音响" />
            <el-checkbox label="白板" />
            <el-checkbox label="空调" />
            <el-checkbox label="麦克风" />
          </el-checkbox-group>
        </el-form-item>

        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio label="AVAILABLE">可用</el-radio>
            <el-radio label="MAINTENANCE">维修中</el-radio>
            <el-radio label="DISABLED">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="formDialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          :loading="submitting"
          @click="handleSubmit"
        >
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import {
  getClassroomList,
  createClassroom,
  updateClassroom,
  deleteClassroom,
  batchDeleteClassrooms,
  updateClassroomStatus
} from '@/api/adminClassroom'

const router = useRouter()

// 查询参数
const queryParams = reactive({
  roomNo: '',
  building: '',
  type: '',
  status: ''
})

// 分页参数
const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

// 数据
const loading = ref(false)
const classroomList = ref([])
const selectedIds = ref([])

// 表单对话框
const formDialogVisible = ref(false)
const formRef = ref(null)
const submitting = ref(false)
const isEdit = ref(false)
const currentId = ref(null)

const form = reactive({
  roomNo: '',
  building: '',
  capacity: null,
  type: '',
  status: 'AVAILABLE'
})

const equipmentList = ref([])

// 表单验证规则
const rules = {
  roomNo: [
    { required: true, message: '请输入教室编号', trigger: 'blur' },
    { max: 20, message: '教室编号不能超过20个字符', trigger: 'blur' }
  ],
  building: [
    { required: true, message: '请输入楼栋', trigger: 'blur' },
    { max: 50, message: '楼栋不能超过50个字符', trigger: 'blur' }
  ],
  capacity: [
    { required: true, message: '请输入容量', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择教室类型', trigger: 'change' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
}

// 获取教室列表
const fetchClassroomList = async () => {
  loading.value = true
  try {
    const params = {
      ...queryParams,
      page: pagination.page - 1,
      size: pagination.size
    }
    const response = await getClassroomList(params)
    classroomList.value = response.data.content
    pagination.total = response.data.totalElements
  } catch (error) {
    ElMessage.error(error.message || '获取教室列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  fetchClassroomList()
}

// 重置
const handleReset = () => {
  Object.keys(queryParams).forEach(key => {
    queryParams[key] = ''
  })
  handleSearch()
}

// 分页大小改变
const handleSizeChange = (size) => {
  pagination.size = size
  pagination.page = 1
  fetchClassroomList()
}

// 页码改变
const handlePageChange = (page) => {
  pagination.page = page
  fetchClassroomList()
}

// 选择改变
const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map(item => item.id)
}

// 添加教室
const handleCreate = () => {
  isEdit.value = false
  currentId.value = null
  resetForm()
  formDialogVisible.value = true
}

// 编辑教室
const handleEdit = (classroom) => {
  isEdit.value = true
  currentId.value = classroom.id
  
  form.roomNo = classroom.roomNo
  form.building = classroom.building
  form.capacity = classroom.capacity
  form.type = classroom.type
  form.status = classroom.status
  
  // 解析设备信息
  if (classroom.equipment) {
    try {
      const equipmentObj = JSON.parse(classroom.equipment)
      equipmentList.value = Object.keys(equipmentObj).filter(key => equipmentObj[key])
    } catch {
      equipmentList.value = []
    }
  } else {
    equipmentList.value = []
  }
  
  formDialogVisible.value = true
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      // 构建设备信息对象
      const equipmentObj = {}
      const allEquipment = ['投影仪', '电脑', '音响', '白板', '空调', '麦克风']
      allEquipment.forEach(item => {
        equipmentObj[item] = equipmentList.value.includes(item)
      })
      
      const data = {
        ...form,
        equipment: JSON.stringify(equipmentObj)
      }
      
      if (isEdit.value) {
        await updateClassroom(currentId.value, data)
        ElMessage.success('教室更新成功')
      } else {
        await createClassroom(data)
        ElMessage.success('教室创建成功')
      }
      
      formDialogVisible.value = false
      fetchClassroomList()
    } catch (error) {
      ElMessage.error(error.message || '操作失败')
    } finally {
      submitting.value = false
    }
  })
}

// 删除教室
const handleDelete = (classroom) => {
  ElMessageBox.confirm(
    `确认删除教室 ${classroom.roomNo} 吗？删除后将无法恢复。`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await deleteClassroom(classroom.id)
      ElMessage.success('删除成功')
      fetchClassroomList()
    } catch (error) {
      ElMessage.error(error.message || '删除失败')
    }
  }).catch(() => {})
}

// 批量删除
const handleBatchDelete = () => {
  ElMessageBox.confirm(
    `确认删除选中的 ${selectedIds.value.length} 个教室吗？删除后将无法恢复。`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await batchDeleteClassrooms(selectedIds.value)
      ElMessage.success('批量删除成功')
      selectedIds.value = []
      fetchClassroomList()
    } catch (error) {
      ElMessage.error(error.message || '批量删除失败')
    }
  }).catch(() => {})
}

// 状态改变
const handleStatusChange = async (classroom, newStatus) => {
  try {
    await updateClassroomStatus(classroom.id, newStatus)
    ElMessage.success('状态更新成功')
    fetchClassroomList()
  } catch (error) {
    ElMessage.error(error.message || '状态更新失败')
  }
}

// 查看使用情况
const handleViewUsage = (classroom) => {
  router.push(`/admin/classrooms/${classroom.id}/usage`)
}

// 关闭表单对话框
const handleCloseFormDialog = () => {
  resetForm()
}

// 重置表单
const resetForm = () => {
  form.roomNo = ''
  form.building = ''
  form.capacity = null
  form.type = ''
  form.status = 'AVAILABLE'
  equipmentList.value = []
  formRef.value?.clearValidate()
}

// 格式化设备信息
const formatEquipment = (equipment) => {
  if (!equipment) return '无'
  try {
    const equipmentObj = JSON.parse(equipment)
    return Object.entries(equipmentObj)
      .filter(([, value]) => value)
      .map(([key]) => key)
      .join('、')
  } catch {
    return equipment
  }
}

// 获取类型标签
const getTypeTag = (type) => {
  const tags = {
    LECTURE: 'info',
    LAB: 'warning',
    MULTIMEDIA: 'success',
    CONFERENCE: 'danger'
  }
  return tags[type] || 'info'
}

// 获取类型名称
const getTypeName = (type) => {
  const names = {
    LECTURE: '普通教室',
    LAB: '实验室',
    MULTIMEDIA: '多媒体教室',
    CONFERENCE: '会议室'
  }
  return names[type] || type
}

// 初始化
fetchClassroomList()
</script>

<style scoped>
.classroom-management-container {
  padding: 20px;
}

.search-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 18px;
  font-weight: 600;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.text-muted {
  color: #909399;
  font-size: 14px;
}
</style>

