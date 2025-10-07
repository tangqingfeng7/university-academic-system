<template>
  <div class="system-config">
    <div class="page-header">
      <h2>系统配置</h2>
      <div class="header-actions">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新建配置
        </el-button>
        <el-button type="success" @click="handleRefreshCache">
          <el-icon><Refresh /></el-icon>
          刷新缓存
        </el-button>
        <el-button type="primary" plain @click="handleBatchSave">
          <el-icon><Check /></el-icon>
          批量保存
        </el-button>
      </div>
    </div>

    <el-card>
      <el-table
        v-loading="loading"
        :data="configList"
        stripe
        border
      >
        <el-table-column prop="configKey" label="配置键" width="250" />
        <el-table-column label="配置值" min-width="200">
          <template #default="{ row }">
            <el-input
              v-model="row.configValue"
              placeholder="请输入配置值"
              :disabled="!row.editing"
            />
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="300" />
        <el-table-column prop="updatedAt" label="更新时间" width="180" />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="!row.editing"
              type="primary"
              size="small"
              plain
              @click="handleEdit(row)"
            >
              编辑
            </el-button>
            <template v-else>
              <el-button
                type="success"
                size="small"
                @click="handleSave(row)"
              >
                保存
              </el-button>
              <el-button
                size="small"
                @click="handleCancel(row)"
              >
                取消
              </el-button>
            </template>
            <el-button
              type="danger"
              size="small"
              plain
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新建配置对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="新建配置"
      width="500px"
      @close="resetForm"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="配置键" prop="configKey">
          <el-input
            v-model="formData.configKey"
            placeholder="请输入配置键（例如：MAX_CREDITS_PER_SEMESTER）"
          />
        </el-form-item>

        <el-form-item label="配置值" prop="configValue">
          <el-input
            v-model="formData.configValue"
            placeholder="请输入配置值"
          />
        </el-form-item>

        <el-form-item label="描述" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="请输入配置描述"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          :loading="submitLoading"
          @click="handleSubmit"
        >
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh, Check } from '@element-plus/icons-vue'
import {
  getAllConfigs,
  createConfig,
  updateConfig,
  deleteConfig,
  refreshConfigCache,
  batchUpdateConfigs
} from '@/api/systemConfig'

const loading = ref(false)
const configList = ref([])
const dialogVisible = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)

// 保存原始配置值用于取消编辑
const originalConfigs = ref({})

const formData = ref({
  configKey: '',
  configValue: '',
  description: ''
})

const formRules = {
  configKey: [
    { required: true, message: '请输入配置键', trigger: 'blur' }
  ],
  configValue: [
    { required: true, message: '请输入配置值', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入配置描述', trigger: 'blur' }
  ]
}

// 获取配置列表
const fetchConfigList = async () => {
  try {
    loading.value = true
    const { data } = await getAllConfigs()
    configList.value = (data || []).map(config => ({
      ...config,
      editing: false
    }))
    // 保存原始值
    configList.value.forEach(config => {
      originalConfigs.value[config.id] = config.configValue
    })
  } catch (error) {
    console.error('获取配置列表失败:', error)
    ElMessage.error('获取配置列表失败')
  } finally {
    loading.value = false
  }
}

// 编辑配置
const handleEdit = (row) => {
  row.editing = true
}

// 保存配置
const handleSave = async (row) => {
  try {
    await updateConfig({
      configKey: row.configKey,
      configValue: row.configValue,
      description: row.description
    })
    ElMessage.success('保存成功')
    row.editing = false
    originalConfigs.value[row.id] = row.configValue
    fetchConfigList() // 刷新列表获取更新时间
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败')
  }
}

// 取消编辑
const handleCancel = (row) => {
  row.configValue = originalConfigs.value[row.id]
  row.editing = false
}

// 批量保存
const handleBatchSave = async () => {
  const editedConfigs = configList.value.filter(config => config.editing)
  
  if (editedConfigs.length === 0) {
    ElMessage.warning('没有需要保存的配置')
    return
  }

  try {
    const updates = editedConfigs.map(config => ({
      configKey: config.configKey,
      configValue: config.configValue,
      description: config.description
    }))
    
    await batchUpdateConfigs(updates)
    ElMessage.success('批量保存成功')
    
    // 退出编辑状态
    editedConfigs.forEach(config => {
      config.editing = false
      originalConfigs.value[config.id] = config.configValue
    })
    
    fetchConfigList()
  } catch (error) {
    console.error('批量保存失败:', error)
    ElMessage.error('批量保存失败')
  }
}

// 新建配置
const handleAdd = () => {
  dialogVisible.value = true
}

// 删除配置
const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定要删除配置"${row.configKey}"吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await deleteConfig(row.configKey)
      ElMessage.success('删除成功')
      fetchConfigList()
    } catch (error) {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

// 刷新缓存
const handleRefreshCache = async () => {
  try {
    await refreshConfigCache()
    ElMessage.success('缓存刷新成功')
  } catch (error) {
    console.error('刷新缓存失败:', error)
    ElMessage.error('刷新缓存失败')
  }
}

// 提交表单
const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  try {
    submitLoading.value = true
    await createConfig(formData.value)
    ElMessage.success('创建成功')
    dialogVisible.value = false
    fetchConfigList()
  } catch (error) {
    console.error('创建失败:', error)
    ElMessage.error(error.response?.data?.message || '创建失败')
  } finally {
    submitLoading.value = false
  }
}

// 重置表单
const resetForm = () => {
  formRef.value?.resetFields()
  formData.value = {
    configKey: '',
    configValue: '',
    description: ''
  }
}

onMounted(() => {
  fetchConfigList()
})
</script>

<style scoped>
.system-config {
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

.header-actions {
  display: flex;
  gap: 10px;
}
</style>

