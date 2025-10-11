<template>
  <div class="scholarship-management">
    <el-card shadow="never" class="header-card">
      <div class="header-content">
        <div>
          <h2 class="page-title">
            <el-icon><Trophy /></el-icon>
            奖学金管理
          </h2>
          <p class="page-subtitle">管理奖学金类型和评定规则</p>
        </div>
        <el-button type="primary" :icon="Plus" @click="handleCreate">
          新建奖学金
        </el-button>
      </div>
    </el-card>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="5" animated />
    </div>

    <!-- 奖学金列表 -->
    <el-card v-else shadow="never">
      <el-table :data="scholarships" stripe>
        <el-table-column label="奖学金名称" prop="name" min-width="150" />

        <el-table-column label="等级" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getLevelType(row.level)">
              {{ getLevelText(row.level) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="金额" width="120" align="right">
          <template #default="{ row }">
            <span class="amount">¥{{ row.amount.toLocaleString() }}</span>
          </template>
        </el-table-column>

        <el-table-column label="名额" prop="quota" width="80" align="center" />

        <el-table-column label="最低GPA" width="100" align="center">
          <template #default="{ row }">
            {{ row.minGpa || '-' }}
          </template>
        </el-table-column>

        <el-table-column label="最低学分" width="100" align="center">
          <template #default="{ row }">
            {{ row.minCredits || '-' }}
          </template>
        </el-table-column>

        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.active"
              @change="handleToggleActive(row)"
            />
          </template>
        </el-table-column>

        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              link
              :icon="View"
              @click="handleView(row)"
            >
              查看
            </el-button>
            <el-button
              type="primary"
              link
              :icon="Edit"
              @click="handleEdit(row)"
            >
              编辑
            </el-button>
            <el-button
              type="danger"
              link
              :icon="Delete"
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchScholarships"
          @current-change="fetchScholarships"
        />
      </div>
    </el-card>

    <!-- 创建/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="800px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
      >
        <el-form-item label="奖学金名称" prop="name" required>
          <el-input v-model="form.name" placeholder="请输入奖学金名称" />
        </el-form-item>

        <el-form-item label="等级" prop="level" required>
          <el-select v-model="form.level" placeholder="请选择等级">
            <el-option label="国家级" value="NATIONAL" />
            <el-option label="省级" value="PROVINCIAL" />
            <el-option label="校级" value="UNIVERSITY" />
            <el-option label="院系级" value="DEPARTMENT" />
          </el-select>
        </el-form-item>

        <el-form-item label="奖金金额" prop="amount" required>
          <el-input-number
            v-model="form.amount"
            :min="0"
            :step="100"
            :precision="2"
            controls-position="right"
          />
          <span style="margin-left: 10px;">元</span>
        </el-form-item>

        <el-form-item label="名额" prop="quota" required>
          <el-input-number
            v-model="form.quota"
            :min="1"
            controls-position="right"
          />
          <span style="margin-left: 10px;">人</span>
        </el-form-item>

        <el-form-item label="最低GPA">
          <el-input-number
            v-model="form.minGpa"
            :min="0"
            :max="4"
            :step="0.1"
            :precision="2"
            controls-position="right"
          />
        </el-form-item>

        <el-form-item label="最低学分">
          <el-input-number
            v-model="form.minCredits"
            :min="0"
            :step="1"
            controls-position="right"
          />
        </el-form-item>

        <el-form-item label="描述">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="4"
            placeholder="请输入奖学金描述"
          />
        </el-form-item>

        <el-form-item label="其他要求">
          <el-input
            v-model="form.requirements"
            type="textarea"
            :rows="3"
            placeholder="请输入其他要求（可选）"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Trophy,
  Plus,
  View,
  Edit,
  Delete
} from '@element-plus/icons-vue'
import {
  getScholarships,
  createScholarship,
  updateScholarship,
  deleteScholarship,
  toggleActive
} from '@/api/scholarship'

const loading = ref(true)
const submitting = ref(false)
const scholarships = ref([])
const dialogVisible = ref(false)
const formRef = ref()
const currentScholarship = ref(null)

const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

const form = reactive({
  name: '',
  level: '',
  amount: 0,
  quota: 1,
  minGpa: null,
  minCredits: null,
  description: '',
  requirements: ''
})

const rules = {
  name: [
    { required: true, message: '请输入奖学金名称', trigger: 'blur' }
  ],
  level: [
    { required: true, message: '请选择等级', trigger: 'change' }
  ],
  amount: [
    { required: true, message: '请输入奖金金额', trigger: 'blur' }
  ],
  quota: [
    { required: true, message: '请输入名额', trigger: 'blur' }
  ]
}

const dialogTitle = computed(() => {
  return currentScholarship.value ? '编辑奖学金' : '新建奖学金'
})

// 获取奖学金列表
const fetchScholarships = async () => {
  try {
    loading.value = true
    const params = {
      page: pagination.page - 1,
      size: pagination.size
    }
    const res = await getScholarships(params)
    scholarships.value = res.data.content || []
    pagination.total = res.data.totalElements || 0
  } catch (error) {
    console.error('获取奖学金列表失败:', error)
    ElMessage.error('获取奖学金列表失败')
  } finally {
    loading.value = false
  }
}

// 获取等级类型
const getLevelType = (level) => {
  const types = {
    NATIONAL: 'danger',
    PROVINCIAL: 'warning',
    UNIVERSITY: 'primary',
    DEPARTMENT: 'success'
  }
  return types[level] || 'info'
}

// 获取等级文本
const getLevelText = (level) => {
  const texts = {
    NATIONAL: '国家级',
    PROVINCIAL: '省级',
    UNIVERSITY: '校级',
    DEPARTMENT: '院系级'
  }
  return texts[level] || level
}

// 新建
const handleCreate = () => {
  currentScholarship.value = null
  Object.assign(form, {
    name: '',
    level: '',
    amount: 0,
    quota: 1,
    minGpa: null,
    minCredits: null,
    description: '',
    requirements: ''
  })
  dialogVisible.value = true
}

// 查看
const handleView = (scholarship) => {
  ElMessageBox.alert(
    `
      <p><strong>名称：</strong>${scholarship.name}</p>
      <p><strong>等级：</strong>${getLevelText(scholarship.level)}</p>
      <p><strong>金额：</strong>¥${scholarship.amount.toLocaleString()}</p>
      <p><strong>名额：</strong>${scholarship.quota}人</p>
      <p><strong>最低GPA：</strong>${scholarship.minGpa || '无要求'}</p>
      <p><strong>最低学分：</strong>${scholarship.minCredits || '无要求'}</p>
      <p><strong>描述：</strong>${scholarship.description || '无'}</p>
      <p><strong>其他要求：</strong>${scholarship.requirements || '无'}</p>
    `,
    '奖学金详情',
    {
      dangerouslyUseHTMLString: true,
      confirmButtonText: '关闭'
    }
  )
}

// 编辑
const handleEdit = (scholarship) => {
  currentScholarship.value = scholarship
  Object.assign(form, {
    name: scholarship.name,
    level: scholarship.level,
    amount: scholarship.amount,
    quota: scholarship.quota,
    minGpa: scholarship.minGpa,
    minCredits: scholarship.minCredits,
    description: scholarship.description || '',
    requirements: scholarship.requirements || ''
  })
  dialogVisible.value = true
}

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    submitting.value = true

    const data = { ...form }
    
    if (currentScholarship.value) {
      await updateScholarship(currentScholarship.value.id, data)
      ElMessage.success('更新成功！')
    } else {
      await createScholarship(data)
      ElMessage.success('创建成功！')
    }

    dialogVisible.value = false
    fetchScholarships()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('提交失败:', error)
      ElMessage.error(error.message || '提交失败')
    }
  } finally {
    submitting.value = false
  }
}

// 启用/禁用
const handleToggleActive = async (scholarship) => {
  try {
    await toggleActive(scholarship.id, scholarship.active)
    ElMessage.success(scholarship.active ? '已启用' : '已禁用')
  } catch (error) {
    console.error('操作失败:', error)
    ElMessage.error('操作失败')
    scholarship.active = !scholarship.active
  }
}

// 删除
const handleDelete = async (scholarship) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除奖学金"${scholarship.name}"吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await deleteScholarship(scholarship.id)
    ElMessage.success('删除成功！')
    fetchScholarships()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  fetchScholarships()
})
</script>

<style scoped>
.scholarship-management {
  padding: 20px;
}

.header-card {
  margin-bottom: 20px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 0 0 10px 0;
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.page-subtitle {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.loading-container {
  padding: 20px;
}

.amount {
  color: #f56c6c;
  font-weight: bold;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>

