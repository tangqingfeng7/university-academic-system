<template>
  <div class="scheduling-constraints">
    <el-page-header title="排课管理" content="排课约束设置" />

    <!-- 操作栏 -->
    <div class="action-bar">
      <el-button type="primary" @click="handleCreate">
        <el-icon><Plus /></el-icon>
        添加约束
      </el-button>
      <el-button @click="loadConstraints">
        <el-icon><Refresh /></el-icon>
        刷新
      </el-button>
    </div>

    <!-- 约束列表 -->
    <el-card shadow="hover" v-loading="loading">
      <el-table :data="constraints" stripe>
        <el-table-column prop="name" label="约束名称" min-width="180" />
        <el-table-column label="类型" width="100" align="center">
          <template #default="{ row }">
            <span :class="['constraint-type', row.type === 'HARD' ? 'type-hard' : 'type-soft']">
              {{ row.type === 'HARD' ? '硬约束' : '软约束' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="weight" label="权重" width="80" align="center" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.active"
              @change="handleToggleActive(row)"
              :loading="row.switching"
            />
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column label="操作" width="180" fixed="right" align="center">
          <template #default="{ row }">
            <el-button text size="small" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button text size="small" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="constraints.length === 0" class="empty-state">
        <el-empty description="暂无约束数据" />
      </div>
    </el-card>

    <!-- 添加/编辑约束对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="约束名称" prop="name">
          <el-input
            v-model="form.name"
            placeholder="请输入约束名称"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="约束类型" prop="type">
          <el-radio-group v-model="form.type">
            <el-radio label="HARD">硬约束</el-radio>
            <el-radio label="SOFT">软约束</el-radio>
          </el-radio-group>
          <div class="form-tip">
            硬约束：必须满足的约束条件<br/>
            软约束：尽量满足的约束条件
          </div>
        </el-form-item>

        <el-form-item label="权重" prop="weight" v-if="form.type === 'SOFT'">
          <el-input-number
            v-model="form.weight"
            :min="1"
            :max="100"
            placeholder="请输入权重"
          />
          <span class="unit-label">权重越高，优先级越高</span>
        </el-form-item>

        <el-form-item label="约束描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="4"
            placeholder="请输入约束描述"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="启用状态" prop="active">
          <el-switch v-model="form.active" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh } from '@element-plus/icons-vue'
import {
  getConstraints,
  createConstraint,
  updateConstraint,
  deleteConstraint
} from '@/api/scheduling'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const constraints = ref([])

const form = reactive({
  id: null,
  name: '',
  type: 'SOFT',
  weight: 50,
  description: '',
  active: true
})

const rules = {
  name: [
    { required: true, message: '请输入约束名称', trigger: 'blur' },
    { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择约束类型', trigger: 'change' }
  ],
  weight: [
    { required: true, message: '请输入权重', trigger: 'blur' },
    { type: 'number', min: 1, max: 100, message: '权重应在1-100之间', trigger: 'blur' }
  ],
  description: [
    { max: 500, message: '描述不能超过500个字符', trigger: 'blur' }
  ]
}

const dialogTitle = computed(() => isEdit.value ? '编辑约束' : '添加约束')

// 加载约束列表
const loadConstraints = async () => {
  loading.value = true
  try {
    const res = await getConstraints()
    constraints.value = res.data.map(item => ({
      ...item,
      switching: false
    }))
  } catch (error) {
    ElMessage.error(error.message || '加载约束列表失败')
  } finally {
    loading.value = false
  }
}

// 打开创建对话框
const handleCreate = () => {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

// 打开编辑对话框
const handleEdit = (row) => {
  isEdit.value = true
  form.id = row.id
  form.name = row.name
  form.type = row.type
  form.weight = row.weight
  form.description = row.description
  form.active = row.active
  dialogVisible.value = true
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    submitting.value = true
    try {
      const data = {
        name: form.name,
        type: form.type,
        weight: form.type === 'HARD' ? null : form.weight,
        description: form.description,
        active: form.active
      }

      if (isEdit.value) {
        await updateConstraint(form.id, data)
        ElMessage.success('更新约束成功')
      } else {
        await createConstraint(data)
        ElMessage.success('添加约束成功')
      }

      dialogVisible.value = false
      await loadConstraints()
    } catch (error) {
      ElMessage.error(error.message || '操作失败')
    } finally {
      submitting.value = false
    }
  })
}

// 切换启用状态
const handleToggleActive = async (row) => {
  row.switching = true
  try {
    const data = {
      active: row.active
    }
    await updateConstraint(row.id, data)
    ElMessage.success(`已${row.active ? '启用' : '禁用'}约束`)
  } catch (error) {
    row.active = !row.active // 回滚状态
    ElMessage.error(error.message || '操作失败')
  } finally {
    row.switching = false
  }
}

// 删除约束
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除约束"${row.name}"吗？此操作不可恢复。`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    loading.value = true
    await deleteConstraint(row.id)
    ElMessage.success('删除成功')
    await loadConstraints()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  } finally {
    loading.value = false
  }
}

// 重置表单
const resetForm = () => {
  form.id = null
  form.name = ''
  form.type = 'SOFT'
  form.weight = 50
  form.description = ''
  form.active = true
}

// 关闭对话框
const handleDialogClose = () => {
  formRef.value?.resetFields()
}

onMounted(() => {
  loadConstraints()
})
</script>

<style scoped lang="scss">
.scheduling-constraints {
  padding: 20px;

  :deep(.el-page-header) {
    margin-bottom: 20px;
  }
}

.action-bar {
  margin-bottom: 16px;
  display: flex;
  gap: 12px;
}

.empty-state {
  padding: 40px 0;
}

.form-tip {
  margin-top: 4px;
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
}

.unit-label {
  margin-left: 8px;
  font-size: 12px;
  color: #909399;
}

/* 苹果风格约束类型 */
.constraint-type {
  font-size: 13px;
  font-weight: 500;
  padding: 2px 0;
  display: inline-block;
}

.type-hard {
  color: #ff3b30;
}

.type-soft {
  color: #ff9500;
}
</style>

