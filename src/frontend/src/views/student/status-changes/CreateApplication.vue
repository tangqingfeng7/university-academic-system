<template>
  <div class="create-application">
    <!-- 页面标题 -->
    <div class="page-header">
      <div>
        <h2 class="page-title">提交学籍异动申请</h2>
        <p class="page-subtitle">请如实填写申请信息，确保信息准确无误</p>
      </div>
      <el-button @click="goBack" style="height: 40px;">
        <svg style="width: 16px; height: 16px; margin-right: 6px;" viewBox="0 0 20 20" fill="currentColor">
          <path fill-rule="evenodd" d="M9.707 14.707a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414l4-4a1 1 0 011.414 1.414L7.414 9H15a1 1 0 110 2H7.414l2.293 2.293a1 1 0 010 1.414z" clip-rule="evenodd"/>
        </svg>
        返回
      </el-button>
    </div>

    <!-- 表单容器 -->
    <div class="form-container">
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        size="large"
      >
        <div class="form-section">
          <h3 class="section-title">基本信息</h3>
          
          <el-form-item label="异动类型" prop="type" required>
            <el-select v-model="form.type" placeholder="请选择异动类型" style="width: 100%;">
              <el-option label="休学" value="SUSPENSION" />
              <el-option label="复学" value="RESUMPTION" />
              <el-option label="转专业" value="TRANSFER" />
              <el-option label="退学" value="WITHDRAWAL" />
            </el-select>
          </el-form-item>

          <el-form-item label="异动原因" prop="reason" required>
            <el-input
              v-model="form.reason"
              type="textarea"
              :rows="6"
              placeholder="请详细说明异动原因（至少10字）"
              maxlength="500"
              show-word-limit
            />
          </el-form-item>
        </div>

        <div v-if="form.type === 'SUSPENSION'" class="form-section">
          <h3 class="section-title">休学时间</h3>
          
          <div class="date-range">
            <el-form-item label="开始日期" prop="startDate">
              <el-date-picker
                v-model="form.startDate"
                type="date"
                placeholder="选择开始日期"
                value-format="YYYY-MM-DD"
                style="width: 100%;"
              />
            </el-form-item>

            <el-form-item label="结束日期" prop="endDate">
              <el-date-picker
                v-model="form.endDate"
                type="date"
                placeholder="选择结束日期"
                value-format="YYYY-MM-DD"
                style="width: 100%;"
              />
            </el-form-item>
          </div>
        </div>

        <div v-if="form.type === 'TRANSFER'" class="form-section">
          <h3 class="section-title">转专业信息</h3>
          
          <el-form-item label="目标专业" prop="targetMajorId">
            <el-select v-model="form.targetMajorId" placeholder="请选择目标专业" style="width: 100%;">
              <el-option
                v-for="major in majors"
                :key="major.id"
                :label="major.name"
                :value="major.id"
              />
            </el-select>
          </el-form-item>
        </div>

        <div class="form-section">
          <h3 class="section-title">证明材料</h3>
          
          <el-form-item label="上传文件">
            <el-upload
              ref="uploadRef"
              :auto-upload="false"
              :limit="1"
              accept=".pdf,.jpg,.jpeg,.png"
              :file-list="fileList"
              :on-change="handleFileChange"
              class="upload-area"
            >
              <div class="upload-content">
                <svg class="upload-icon" viewBox="0 0 20 20" fill="currentColor">
                  <path fill-rule="evenodd" d="M3 17a1 1 0 011-1h12a1 1 0 110 2H4a1 1 0 01-1-1zM6.293 6.707a1 1 0 010-1.414l3-3a1 1 0 011.414 0l3 3a1 1 0 01-1.414 1.414L11 5.414V13a1 1 0 11-2 0V5.414L7.707 6.707a1 1 0 01-1.414 0z" clip-rule="evenodd"/>
                </svg>
                <p class="upload-text">点击或拖拽文件到这里上传</p>
                <p class="upload-hint">支持 PDF、JPG、PNG 格式，大小不超过 10MB</p>
              </div>
            </el-upload>
          </el-form-item>
        </div>

        <div class="form-actions">
          <el-button @click="goBack" size="large">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitting" size="large">
            提交申请
          </el-button>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createApplication } from '@/api/statusChange'
import { getAllMajorsPublic } from '@/api/major'

const router = useRouter()
const formRef = ref()
const uploadRef = ref()

const form = reactive({
  type: '',
  reason: '',
  startDate: '',
  endDate: '',
  targetMajorId: ''
})

const rules = {
  type: [{ required: true, message: '请选择异动类型', trigger: 'change' }],
  reason: [
    { required: true, message: '请输入异动原因', trigger: 'blur' },
    { min: 10, max: 500, message: '请输入10-500字的异动原因', trigger: 'blur' }
  ]
}

const majors = ref([])
const submitting = ref(false)
const fileList = ref([])
const uploadedFile = ref(null)

// 处理文件变更
const handleFileChange = (file) => {
  uploadedFile.value = file.raw
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()

    submitting.value = true

    const formData = new FormData()
    Object.keys(form).forEach(key => {
      if (form[key]) {
        formData.append(key, form[key])
      }
    })

    // 添加文件
    if (uploadedFile.value) {
      formData.append('attachment', uploadedFile.value)
    }

    await createApplication(formData)
    ElMessage.success('申请提交成功')
    router.push('/student/status-changes')
  } catch (error) {
    console.error('提交失败:', error)
    if (error !== 'cancel') {
      ElMessage.error(error.message || '提交失败')
    }
  } finally {
    submitting.value = false
  }
}

const goBack = () => {
  router.back()
}

const fetchMajors = async () => {
  try {
    const { data } = await getAllMajorsPublic()
    majors.value = data || []
  } catch (error) {
    console.error('获取专业列表失败:', error)
    ElMessage.error('获取专业列表失败')
    majors.value = []
  }
}

onMounted(() => {
  fetchMajors()
})
</script>

<style scoped>
.create-application {
  padding: 24px;
  max-width: 900px;
  margin: 0 auto;
}

/* 页面标题 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
}

.page-title {
  font-size: 28px;
  font-weight: 600;
  color: #1d1d1f;
  margin: 0 0 4px 0;
  letter-spacing: -0.02em;
}

.page-subtitle {
  font-size: 14px;
  color: #86868b;
  margin: 0;
}

/* 表单容器 */
.form-container {
  background: #ffffff;
  border: 1px solid #e5e5e5;
  border-radius: 12px;
  padding: 32px;
}

.form-section {
  margin-bottom: 32px;
}

.form-section:last-of-type {
  margin-bottom: 0;
}

.section-title {
  font-size: 17px;
  font-weight: 600;
  color: #1d1d1f;
  margin: 0 0 20px 0;
  padding-bottom: 12px;
  border-bottom: 1px solid #e5e5e5;
}

/* 日期范围 */
.date-range {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

/* 上传区域 */
.upload-area {
  width: 100%;
}

.upload-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  border: 2px dashed #e5e5e5;
  border-radius: 8px;
  background: #fafafa;
  transition: all 0.2s ease;
  cursor: pointer;
}

.upload-content:hover {
  border-color: #d2d2d7;
  background: #f5f5f7;
}

.upload-icon {
  width: 48px;
  height: 48px;
  color: #86868b;
  margin-bottom: 12px;
}

.upload-text {
  font-size: 15px;
  color: #1d1d1f;
  font-weight: 500;
  margin: 0 0 4px 0;
}

.upload-hint {
  font-size: 13px;
  color: #86868b;
  margin: 0;
}

/* 表单操作按钮 */
.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid #e5e5e5;
}

/* Element Plus 表单样式覆盖 */
:deep(.el-form-item__label) {
  font-size: 14px;
  font-weight: 500;
  color: #1d1d1f;
  margin-bottom: 8px;
}

:deep(.el-input__wrapper) {
  border-radius: 8px;
  border: 1px solid #e5e5e5;
  box-shadow: none;
  transition: all 0.2s ease;
}

:deep(.el-input__wrapper:hover) {
  border-color: #d2d2d7;
}

:deep(.el-input__wrapper.is-focus) {
  border-color: #0071e3;
  box-shadow: 0 0 0 3px rgba(0, 113, 227, 0.1);
}

:deep(.el-textarea__inner) {
  border-radius: 8px;
  border: 1px solid #e5e5e5;
  box-shadow: none;
  transition: all 0.2s ease;
  font-family: inherit;
}

:deep(.el-textarea__inner:hover) {
  border-color: #d2d2d7;
}

:deep(.el-textarea__inner:focus) {
  border-color: #0071e3;
  box-shadow: 0 0 0 3px rgba(0, 113, 227, 0.1);
}

:deep(.el-select .el-input__wrapper) {
  border-radius: 8px;
}

/* 响应式 */
@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  
  .date-range {
    grid-template-columns: 1fr;
  }
  
  .form-container {
    padding: 24px;
  }
}
</style>

