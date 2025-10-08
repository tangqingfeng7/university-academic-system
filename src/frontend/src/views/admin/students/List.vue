<template>
  <div class="student-management">
    <!-- 页面标题和快捷操作 -->
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">
          <el-icon class="title-icon"><User /></el-icon>
          学生管理
        </h2>
        <el-tag class="student-count" type="primary" effect="dark">
          共 {{ total }} 名学生
        </el-tag>
      </div>
      <div class="header-right">
        <el-button type="primary" :icon="Plus" @click="handleAdd">
          添加学生
        </el-button>
        <el-button type="success" :icon="Upload" @click="handleImport">
          批量导入
        </el-button>
        <el-button type="warning" :icon="Download" @click="handleExport">
          导出数据
        </el-button>
      </div>
    </div>

    <!-- 搜索筛选区域 -->
    <el-card class="search-card" shadow="hover">
      <div class="search-container">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-input
              v-model="searchParams.keyword"
              placeholder="搜索学号/姓名"
              :prefix-icon="Search"
              clearable
              @keyup.enter="handleSearch"
              @clear="handleSearch"
            />
          </el-col>
          <el-col :span="5">
            <el-select
              v-model="searchParams.majorId"
              placeholder="选择专业"
              clearable
              filterable
              @change="handleSearch"
            >
              <el-option
                v-for="major in majorList"
                :key="major.id"
                :label="major.name"
                :value="major.id"
              />
            </el-select>
          </el-col>
          <el-col :span="5">
            <el-select
              v-model="searchParams.className"
              placeholder="选择班级"
              clearable
              filterable
              @change="handleSearch"
            >
              <el-option
                v-for="className in classList"
                :key="className"
                :label="className"
                :value="className"
              />
            </el-select>
          </el-col>
          <el-col :span="5">
            <el-select
              v-model="searchParams.enrollmentYear"
              placeholder="入学年份"
              clearable
              @change="handleSearch"
            >
              <el-option
                v-for="year in enrollmentYears"
                :key="year"
                :label="`${year}级`"
                :value="year"
              />
            </el-select>
          </el-col>
          <el-col :span="3">
            <el-button type="primary" :icon="Search" @click="handleSearch">
              搜索
            </el-button>
          </el-col>
        </el-row>
      </div>
    </el-card>

    <!-- 学生列表 - 卡片视图 -->
    <div v-loading="loading" class="student-grid">
      <transition-group name="card-list">
        <el-card
          v-for="student in tableData"
          :key="student.id"
          class="student-card"
          shadow="hover"
          :body-style="{ padding: '0' }"
        >
          <div class="card-content">
            <!-- 学生头像和基本信息 -->
            <div class="student-header">
              <el-avatar
                :size="80"
                :icon="UserFilled"
                :style="{
                  backgroundColor: student.gender === 'MALE' ? '#409eff' : '#f56c6c'
                }"
              />
              <div class="student-info">
                <h3 class="student-name">
                  {{ student.name }}
                  <el-tag
                    :type="student.gender === 'MALE' ? '' : 'danger'"
                    size="small"
                    effect="plain"
                  >
                    {{ student.gender === 'MALE' ? '男' : '女' }}
                  </el-tag>
                </h3>
                <div class="student-no">学号：{{ student.studentNo }}</div>
              </div>
            </div>

            <!-- 详细信息 -->
            <div class="student-details">
              <div class="detail-item">
                <el-icon class="detail-icon"><School /></el-icon>
                <span class="detail-label">专业：</span>
                <span class="detail-value">{{ student.majorName }}</span>
              </div>
              <div class="detail-item">
                <el-icon class="detail-icon"><Reading /></el-icon>
                <span class="detail-label">班级：</span>
                <span class="detail-value">{{ student.className || '未分配' }}</span>
              </div>
              <div class="detail-item">
                <el-icon class="detail-icon"><Calendar /></el-icon>
                <span class="detail-label">入学年份：</span>
                <span class="detail-value">{{ student.enrollmentYear }}级</span>
              </div>
              <div class="detail-item">
                <el-icon class="detail-icon"><Phone /></el-icon>
                <span class="detail-label">联系电话：</span>
                <span class="detail-value">{{ student.phone || '未填写' }}</span>
              </div>
            </div>

            <!-- 操作按钮 -->
            <div class="card-actions">
              <el-button
                type="primary"
                link
                :icon="View"
                @click="handleViewDetail(student)"
              >
                详情
              </el-button>
              <el-button
                type="success"
                link
                :icon="Edit"
                @click="handleEdit(student)"
              >
                编辑
              </el-button>
              <el-button
                type="danger"
                link
                :icon="Delete"
                @click="handleDelete(student)"
              >
                删除
              </el-button>
            </div>
          </div>
        </el-card>
      </transition-group>

      <!-- 空状态 -->
      <el-empty
        v-if="!loading && tableData.length === 0"
        description="暂无学生数据"
        :image-size="200"
      >
        <el-button type="primary" @click="handleAdd">添加第一个学生</el-button>
      </el-empty>
    </div>

    <!-- 分页 -->
    <div v-if="total > 0" class="pagination-container">
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :page-sizes="[12, 24, 36, 48]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        background
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </div>

    <!-- 学生表单对话框 -->
    <student-form
      v-model="formVisible"
      :student-id="currentStudentId"
      :mode="formMode"
      @success="handleFormSuccess"
    />

    <!-- 学生详情对话框 -->
    <student-detail-dialog
      v-model="detailVisible"
      :student="currentStudent"
    />

    <!-- 导入对话框 -->
    <el-dialog
      v-model="importVisible"
      title="批量导入学生"
      width="600px"
      :close-on-click-modal="false"
    >
      <div class="import-container">
        <el-alert
          title="导入说明"
          type="info"
          :closable="false"
          show-icon
          style="margin-bottom: 20px"
        >
          <template #default>
            <p>1. 请下载导入模板，按照模板格式填写学生信息</p>
            <p>2. 支持 .xlsx、.xls 格式的Excel文件</p>
            <p>3. 学号必须唯一，重复的学号会导致导入失败</p>
          </template>
        </el-alert>

        <div class="download-template">
          <el-button type="primary" :icon="Download" @click="downloadTemplate">
            下载导入模板
          </el-button>
        </div>

        <el-divider />

        <file-upload
          ref="uploadRef"
          :action="`${baseURL}/api/admin/students/import`"
          :limit="1"
          accept=".xlsx,.xls"
          :on-success="handleImportSuccess"
          :on-error="handleImportError"
        />
      </div>
      <template #footer>
        <el-button @click="importVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus,
  Upload,
  Download,
  Search,
  User,
  UserFilled,
  Edit,
  Delete,
  View,
  School,
  Reading,
  Calendar,
  Phone
} from '@element-plus/icons-vue'
import StudentForm from './StudentForm.vue'
import StudentDetailDialog from './StudentDetailDialog.vue'
import FileUpload from '@/components/FileUpload.vue'
import {
  getStudentList,
  deleteStudent,
  exportStudents,
  getAllClasses
} from '@/api/student'
import { getAllMajors } from '@/api/system'

const baseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

// 数据
const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const majorList = ref([])
const classList = ref([])

// 搜索参数
const searchParams = reactive({
  keyword: '',
  majorId: null,
  className: null,
  enrollmentYear: null
})

// 分页参数
const pagination = reactive({
  page: 1,
  size: 12
})

// 表单相关
const formVisible = ref(false)
const formMode = ref('add')
const currentStudentId = ref(null)

// 详情相关
const detailVisible = ref(false)
const currentStudent = ref(null)

// 导入相关
const importVisible = ref(false)
const uploadRef = ref(null)

// 计算入学年份选项
const enrollmentYears = computed(() => {
  const currentYear = new Date().getFullYear()
  const years = []
  for (let i = 0; i < 10; i++) {
    years.push(currentYear - i)
  }
  return years
})

// 加载学生列表
const loadStudentList = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page - 1,
      size: pagination.size,
      ...searchParams
    }

    // 移除空值
    Object.keys(params).forEach(key => {
      if (params[key] === null || params[key] === '') {
        delete params[key]
      }
    })

    const res = await getStudentList(params)
    tableData.value = res.data.content || []
    total.value = res.data.totalElements || 0
  } catch (error) {
    console.error('加载学生列表失败:', error)
    ElMessage.error('加载学生列表失败')
  } finally {
    loading.value = false
  }
}

// 加载专业列表
const loadMajorList = async () => {
  try {
    const res = await getAllMajors()
    majorList.value = res.data || []
  } catch (error) {
    console.error('加载专业列表失败:', error)
  }
}

// 加载班级列表
const loadClassList = async () => {
  try {
    const res = await getAllClasses()
    classList.value = res.data || []
  } catch (error) {
    console.error('加载班级列表失败:', error)
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  loadStudentList()
}

// 分页
const handlePageChange = () => {
  loadStudentList()
}

const handleSizeChange = () => {
  pagination.page = 1
  loadStudentList()
}

// 添加学生
const handleAdd = () => {
  formMode.value = 'add'
  currentStudentId.value = null
  formVisible.value = true
}

// 编辑学生
const handleEdit = (student) => {
  formMode.value = 'edit'
  currentStudentId.value = student.id
  formVisible.value = true
}

// 查看详情
const handleViewDetail = (student) => {
  currentStudent.value = student
  detailVisible.value = true
}

// 删除学生
const handleDelete = (student) => {
  ElMessageBox.confirm(
    `确定要删除学生 "${student.name}" (${student.studentNo}) 吗？删除后数据将无法恢复！`,
    '确认删除',
    {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning',
      buttonSize: 'default'
    }
  ).then(async () => {
    try {
      await deleteStudent(student.id)
      ElMessage.success('删除成功')
      loadStudentList()
    } catch (error) {
      console.error('删除失败:', error)
    }
  }).catch(() => {
    // 取消删除
  })
}

// 表单提交成功
const handleFormSuccess = () => {
  loadStudentList()
  loadClassList()
}

// 导入
const handleImport = () => {
  importVisible.value = true
}

const handleImportSuccess = (response) => {
  if (response.code === 200) {
    ElMessage.success(`导入成功！共导入 ${response.data?.successCount || 0} 名学生`)
    importVisible.value = false
    loadStudentList()
    loadClassList()
  } else {
    ElMessage.error(response.message || '导入失败')
  }
}

const handleImportError = () => {
  ElMessage.error('导入失败，请检查文件格式')
}

// 下载模板
const downloadTemplate = () => {
  const link = document.createElement('a')
  link.href = `${baseURL}/api/admin/students/template`
  link.download = '学生导入模板.xlsx'
  link.click()
}

// 导出
const handleExport = async () => {
  try {
    const blob = await exportStudents(searchParams)
    const url = window.URL.createObjectURL(new Blob([blob]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', `学生数据_${new Date().getTime()}.xlsx`)
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  }
}

// 初始化
onMounted(() => {
  loadStudentList()
  loadMajorList()
  loadClassList()
})
</script>

<style scoped lang="scss">
.student-management {
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);

  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    .header-left {
      display: flex;
      align-items: center;
      gap: 16px;

      .page-title {
        margin: 0;
        font-size: 24px;
        font-weight: 600;
        color: #303133;
        display: flex;
        align-items: center;
        gap: 8px;

        .title-icon {
          font-size: 28px;
          color: #409eff;
        }
      }

      .student-count {
        font-size: 14px;
        padding: 6px 16px;
        border-radius: 20px;
      }
    }

    .header-right {
      display: flex;
      gap: 12px;
    }
  }

  .search-card {
    margin-bottom: 20px;
    border-radius: 12px;
    border: none;

    :deep(.el-card__body) {
      padding: 20px;
    }

    .search-container {
      .el-input,
      .el-select {
        width: 100%;
      }
    }
  }

  .student-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(380px, 1fr));
    gap: 20px;
    margin-bottom: 20px;
    min-height: 400px;

    .student-card {
      border-radius: 12px;
      border: none;
      transition: all 0.3s ease;
      cursor: pointer;

      &:hover {
        transform: translateY(-4px);
        box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
      }

      .card-content {
        padding: 24px;

        .student-header {
          display: flex;
          align-items: center;
          gap: 16px;
          margin-bottom: 20px;
          padding-bottom: 16px;
          border-bottom: 1px solid #ebeef5;

          .student-info {
            flex: 1;

            .student-name {
              margin: 0 0 8px 0;
              font-size: 18px;
              font-weight: 600;
              color: #303133;
              display: flex;
              align-items: center;
              gap: 8px;
            }

            .student-no {
              font-size: 14px;
              color: #909399;
            }
          }
        }

        .student-details {
          margin-bottom: 16px;

          .detail-item {
            display: flex;
            align-items: center;
            padding: 8px 0;
            font-size: 14px;

            .detail-icon {
              color: #409eff;
              margin-right: 8px;
              font-size: 16px;
            }

            .detail-label {
              color: #606266;
              margin-right: 4px;
            }

            .detail-value {
              color: #303133;
              font-weight: 500;
            }
          }
        }

        .card-actions {
          display: flex;
          justify-content: space-around;
          padding-top: 16px;
          border-top: 1px solid #ebeef5;
        }
      }
    }
  }

  .pagination-container {
    display: flex;
    justify-content: center;
    padding: 20px 0;
    background: white;
    border-radius: 12px;
  }

  .import-container {
    .download-template {
      text-align: center;
      margin: 20px 0;
    }

    :deep(.el-alert__description) p {
      margin: 4px 0;
      line-height: 1.6;
    }
  }
}

// 卡片列表动画
.card-list-enter-active,
.card-list-leave-active {
  transition: all 0.3s ease;
}

.card-list-enter-from {
  opacity: 0;
  transform: translateY(20px);
}

.card-list-leave-to {
  opacity: 0;
  transform: scale(0.9);
}
</style>
