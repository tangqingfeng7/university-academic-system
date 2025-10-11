<template>
  <div class="tuition-standards-page">
    <!-- 页面头部 -->
    <div class="page-header animate-fade-in-down">
      <div class="header-content">
        <h1 class="page-title">学费标准设置</h1>
        <p class="page-subtitle">管理不同专业和年级的学费标准</p>
      </div>
      <el-button 
        type="primary" 
        :icon="Plus"
        size="large"
        @click="handleCreate"
      >
        新增学费标准
      </el-button>
    </div>

    <!-- 筛选区域 -->
    <div class="filter-section animate-fade-in-up" style="animation-delay: 0.1s;">
      <el-card shadow="hover">
        <el-form :inline="true" :model="queryForm">
          <el-form-item label="学年">
            <el-input 
              v-model="queryForm.academicYear" 
              placeholder="如：2024-2025"
              clearable
              style="width: 160px;"
            />
          </el-form-item>
          <el-form-item label="年级">
            <el-select 
              v-model="queryForm.gradeLevel" 
              placeholder="选择年级"
              clearable
              style="width: 120px;"
            >
              <el-option label="一年级" :value="1" />
              <el-option label="二年级" :value="2" />
              <el-option label="三年级" :value="3" />
              <el-option label="四年级" :value="4" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select 
              v-model="queryForm.active" 
              placeholder="选择状态"
              clearable
              style="width: 120px;"
            >
              <el-option label="启用" :value="true" />
              <el-option label="停用" :value="false" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :icon="Search" @click="fetchStandards">查询</el-button>
            <el-button :icon="Refresh" @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>

    <!-- 学费标准列表 -->
    <div class="standards-section animate-fade-in-up" style="animation-delay: 0.2s;">
      <el-card shadow="hover">
        <el-table 
          :data="standards" 
          v-loading="loading"
          stripe
          border
          :show-overflow-tooltip="false"
          style="width: 100%"
        >
          <el-table-column prop="majorName" label="专业" min-width="180" />
          <el-table-column prop="departmentName" label="院系" min-width="140" />
          <el-table-column prop="academicYear" label="学年" width="120" align="center" />
          <el-table-column prop="gradeLevel" label="年级" width="90" align="center">
            <template #default="{ row }">
              {{ row.gradeLevel }}年级
            </template>
          </el-table-column>
          <el-table-column prop="tuitionFee" label="学费" width="120" align="right">
            <template #default="{ row }">
              ¥{{ row.tuitionFee.toFixed(2) }}
            </template>
          </el-table-column>
          <el-table-column prop="accommodationFee" label="住宿费" width="120" align="right">
            <template #default="{ row }">
              ¥{{ row.accommodationFee.toFixed(2) }}
            </template>
          </el-table-column>
          <el-table-column prop="textbookFee" label="教材费" width="120" align="right">
            <template #default="{ row }">
              ¥{{ row.textbookFee.toFixed(2) }}
            </template>
          </el-table-column>
          <el-table-column prop="totalFee" label="合计" width="140" align="right">
            <template #default="{ row }">
              <span class="total-amount">¥{{ row.totalFee.toFixed(2) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="active" label="状态" width="90" align="center" :show-overflow-tooltip="false">
            <template #default="{ row }">
              <el-tag :type="row.active ? 'success' : 'info'">
                {{ row.active ? '启用' : '停用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="140" align="center" :show-overflow-tooltip="false">
            <template #default="{ row }">
              <div class="action-buttons">
                <el-button text :icon="Edit" size="small" @click="handleEdit(row)" class="action-btn edit-btn">
                  编辑
                </el-button>
                <el-button 
                  text
                  size="small"
                  @click="handleToggleStatus(row)"
                  :class="['action-btn', row.active ? 'disable-btn' : 'enable-btn']"
                >
                  {{ row.active ? '停用' : '启用' }}
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination-wrapper">
          <el-pagination
            v-model:current-page="pagination.page"
            v-model:page-size="pagination.size"
            :total="pagination.total"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="fetchStandards"
            @current-change="fetchStandards"
          />
        </div>
      </el-card>
    </div>

    <!-- 编辑/新增对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="700px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="formRules"
        label-width="120px"
      >
        <el-form-item label="专业" prop="majorId">
          <el-select 
            v-model="form.majorId" 
            placeholder="请选择专业"
            filterable
            style="width: 100%;"
          >
            <el-option
              v-for="major in majors"
              :key="major.id"
              :label="`${major.name} (${major.departmentName})`"
              :value="major.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="学年" prop="academicYear">
          <el-input 
            v-model="form.academicYear" 
            placeholder="如：2024-2025"
          />
        </el-form-item>

        <el-form-item label="年级" prop="gradeLevel">
          <el-select 
            v-model="form.gradeLevel" 
            placeholder="请选择年级"
            style="width: 100%;"
          >
            <el-option label="一年级" :value="1" />
            <el-option label="二年级" :value="2" />
            <el-option label="三年级" :value="3" />
            <el-option label="四年级" :value="4" />
          </el-select>
        </el-form-item>

        <el-form-item label="学费" prop="tuitionFee">
          <el-input-number
            v-model="form.tuitionFee"
            :min="0"
            :precision="2"
            :step="100"
            controls-position="right"
            style="width: 100%;"
          />
        </el-form-item>

        <el-form-item label="住宿费" prop="accommodationFee">
          <el-input-number
            v-model="form.accommodationFee"
            :min="0"
            :precision="2"
            :step="100"
            controls-position="right"
            style="width: 100%;"
          />
        </el-form-item>

        <el-form-item label="教材费" prop="textbookFee">
          <el-input-number
            v-model="form.textbookFee"
            :min="0"
            :precision="2"
            :step="100"
            controls-position="right"
            style="width: 100%;"
          />
        </el-form-item>

        <el-form-item label="其他费用" prop="otherFees">
          <el-input-number
            v-model="form.otherFees"
            :min="0"
            :precision="2"
            :step="100"
            controls-position="right"
            style="width: 100%;"
          />
        </el-form-item>

        <el-form-item label="合计">
          <el-input 
            :value="`¥${totalFee.toFixed(2)}`" 
            disabled
          />
        </el-form-item>

        <el-form-item label="状态">
          <el-switch 
            v-model="form.active"
            active-text="启用"
            inactive-text="停用"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
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
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Plus, Search, Refresh, Edit
} from '@element-plus/icons-vue'
import { 
  getTuitionStandards, 
  setTuitionStandard,
  updateTuitionStandard 
} from '@/api/tuition'
import { getAllMajors } from '@/api/major'

// 数据
const standards = ref([])
const majors = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const submitting = ref(false)
const currentRow = ref(null)

const queryForm = reactive({
  academicYear: '',
  gradeLevel: null,
  active: null
})

const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

const formRef = ref(null)
const form = reactive({
  majorId: null,
  academicYear: '',
  gradeLevel: null,
  tuitionFee: 0,
  accommodationFee: 0,
  textbookFee: 0,
  otherFees: 0,
  active: true
})

// 计算总费用
const totalFee = computed(() => {
  return (form.tuitionFee || 0) + 
         (form.accommodationFee || 0) + 
         (form.textbookFee || 0) + 
         (form.otherFees || 0)
})

// 表单验证规则
const formRules = {
  majorId: [
    { required: true, message: '请选择专业', trigger: 'change' }
  ],
  academicYear: [
    { required: true, message: '请输入学年', trigger: 'blur' },
    { pattern: /^\d{4}-\d{4}$/, message: '格式为：2024-2025', trigger: 'blur' }
  ],
  gradeLevel: [
    { required: true, message: '请选择年级', trigger: 'change' }
  ],
  tuitionFee: [
    { required: true, message: '请输入学费', trigger: 'blur' }
  ],
  accommodationFee: [
    { required: true, message: '请输入住宿费', trigger: 'blur' }
  ],
  textbookFee: [
    { required: true, message: '请输入教材费', trigger: 'blur' }
  ]
}

// 获取学费标准列表
const fetchStandards = async () => {
  try {
    loading.value = true
    const params = {
      ...queryForm,
      page: pagination.page - 1,
      size: pagination.size
    }
    const res = await getTuitionStandards(params)
    standards.value = res.data.content || []
    pagination.total = res.data.totalElements || 0
  } catch (error) {
    console.error('获取学费标准失败:', error)
    ElMessage.error('获取学费标准失败')
  } finally {
    loading.value = false
  }
}

// 获取专业列表
const fetchMajors = async () => {
  try {
    const res = await getAllMajors()
    majors.value = res.data || []
  } catch (error) {
    console.error('获取专业列表失败:', error)
  }
}

// 重置查询
const handleReset = () => {
  queryForm.academicYear = ''
  queryForm.gradeLevel = null
  queryForm.active = null
  pagination.page = 1
  fetchStandards()
}

// 新增
const handleCreate = () => {
  currentRow.value = null
  dialogTitle.value = '新增学费标准'
  resetForm()
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row) => {
  currentRow.value = row
  dialogTitle.value = '编辑学费标准'
  Object.assign(form, {
    majorId: row.majorId,
    academicYear: row.academicYear,
    gradeLevel: row.gradeLevel,
    tuitionFee: row.tuitionFee,
    accommodationFee: row.accommodationFee,
    textbookFee: row.textbookFee,
    otherFees: row.otherFees || 0,
    active: row.active
  })
  dialogVisible.value = true
}

// 提交
const handleSubmit = async () => {
  try {
    await formRef.value.validate()

    submitting.value = true

    if (currentRow.value) {
      // 更新
      await updateTuitionStandard(currentRow.value.id, form)
      ElMessage.success('更新成功')
    } else {
      // 新增
      await setTuitionStandard(form)
      ElMessage.success('新增成功')
    }

    dialogVisible.value = false
    fetchStandards()
  } catch (error) {
    if (error !== false) {
      console.error('提交失败:', error)
      ElMessage.error(error.message || '操作失败')
    }
  } finally {
    submitting.value = false
  }
}

// 切换状态
const handleToggleStatus = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确认${row.active ? '停用' : '启用'}该学费标准吗？`,
      '确认操作',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await updateTuitionStandard(row.id, {
      ...row,
      active: !row.active
    })

    ElMessage.success('操作成功')
    fetchStandards()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('操作失败:', error)
      ElMessage.error('操作失败')
    }
  }
}

// 重置表单
const resetForm = () => {
  Object.assign(form, {
    majorId: null,
    academicYear: '',
    gradeLevel: null,
    tuitionFee: 0,
    accommodationFee: 0,
    textbookFee: 0,
    otherFees: 0,
    active: true
  })
  formRef.value?.clearValidate()
}

// 初始化
onMounted(() => {
  fetchStandards()
  fetchMajors()
})
</script>

<style scoped lang="scss">
.tuition-standards-page {
  padding: 20px;

  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 20px;

    .header-content {
      .page-title {
        font-size: 24px;
        font-weight: 600;
        color: #303133;
        margin: 0 0 8px 0;
      }

      .page-subtitle {
        font-size: 14px;
        color: #909399;
        margin: 0;
      }
    }
  }

  .filter-section {
    margin-bottom: 20px;
  }

  .standards-section {
    .total-amount {
      font-weight: 600;
      color: #409EFF;
    }

    .action-buttons {
      display: flex;
      gap: 4px;
      justify-content: center;
      align-items: center;
      flex-wrap: nowrap;

      .action-btn {
        padding: 4px 12px;
        font-size: 13px;
        font-weight: 500;
        border-radius: 4px;
        transition: all 0.2s ease;
        
        &:hover {
          transform: translateY(-1px);
        }

        &.edit-btn {
          color: #606266;
          
          &:hover {
            color: #409EFF;
            background: rgba(64, 158, 255, 0.1);
          }
        }

        &.disable-btn {
          color: #909399;
          
          &:hover {
            color: #F56C6C;
            background: rgba(245, 108, 108, 0.1);
          }
        }

        &.enable-btn {
          color: #909399;
          
          &:hover {
            color: #67C23A;
            background: rgba(103, 194, 58, 0.1);
          }
        }
      }
    }
  }

  
  :deep(.el-table__expand-icon) {
    display: none !important;
  }

  :deep(.el-table__placeholder) {
    display: none !important;
  }

  :deep(.el-table__cell) {
    .cell {
      overflow: visible !important;
      text-overflow: clip !important;
      white-space: normal !important;
    }
  }

  :deep(.el-table__fixed-right) {
    display: none !important;
  }

  :deep(.el-icon) {
    &.el-icon--right {
      display: none !important;
    }
  }

 
  :deep(.el-dropdown) {
    display: none !important;
  }

  :deep(.el-table__column) {
    &:last-child {
      .cell::after {
        content: none !important;
      }
    }
  }

  .pagination-wrapper {
    display: flex;
    justify-content: flex-end;
    margin-top: 20px;
  }
}

// 动画
@keyframes fade-in-down {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes fade-in-up {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.animate-fade-in-down {
  animation: fade-in-down 0.5s ease-out;
}

.animate-fade-in-up {
  animation: fade-in-up 0.5s ease-out;
}
</style>

