<template>
  <div class="scholarship-apply">
    <el-card shadow="never" class="header-card">
      <h2 class="page-title">
        <el-icon><EditPen /></el-icon>
        提交奖学金申请
      </h2>
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ name: 'StudentScholarships' }">奖学金列表</el-breadcrumb-item>
        <el-breadcrumb-item>申请</el-breadcrumb-item>
      </el-breadcrumb>
    </el-card>

    <!-- 奖学金信息 -->
    <el-card v-if="scholarship" shadow="never" class="scholarship-info-card">
      <h3>奖学金信息</h3>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="奖学金名称">
          {{ scholarship.name }}
        </el-descriptions-item>
        <el-descriptions-item label="等级">
          <el-tag :type="getLevelType(scholarship.level)">
            {{ getLevelText(scholarship.level) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="奖金金额">
          <span class="amount">¥{{ scholarship.amount?.toLocaleString() }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="名额">
          {{ scholarship.quota }}人
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 申请表单 -->
    <el-card shadow="never">
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
        @submit.prevent="handleSubmit"
      >
        <el-form-item label="个人陈述" prop="personalStatement" required>
          <el-input
            v-model="form.personalStatement"
            type="textarea"
            :rows="8"
            placeholder="请详细描述您的个人情况、获奖经历、社会实践等，字数不少于200字"
            maxlength="2000"
            show-word-limit
          />
          <div class="form-tip">
            💡 提示：请真实描述您的学习成绩、综合素质、获奖情况、社会实践等，这将作为评审的重要参考
          </div>
        </el-form-item>

        <el-form-item label="证明材料" prop="attachmentUrl">
          <el-upload
            ref="uploadRef"
            class="upload-demo"
            :action="uploadAction"
            :headers="uploadHeaders"
            :on-success="handleUploadSuccess"
            :on-error="handleUploadError"
            :before-upload="beforeUpload"
            :file-list="fileList"
            :limit="5"
            accept=".pdf,.jpg,.jpeg,.png,.doc,.docx"
          >
            <el-button type="primary" :icon="Upload">上传文件</el-button>
            <template #tip>
              <div class="el-upload__tip">
                支持PDF、图片、Word文档，单个文件不超过10MB，最多5个文件
              </div>
            </template>
          </el-upload>
        </el-form-item>

        <el-form-item label="资格确认" prop="agree" required>
          <el-checkbox v-model="form.agree">
            我已阅读并理解申请要求，承诺所提供信息真实有效
          </el-checkbox>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            :loading="submitting"
            @click="handleSubmit"
          >
            提交申请
          </el-button>
          <el-button @click="handleCancel">
            取消
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { EditPen, Upload } from '@element-plus/icons-vue'
import { getScholarshipDetail, submitApplication } from '@/api/scholarship'
import { getToken } from '@/utils/auth'

const route = useRoute()
const router = useRouter()

const formRef = ref()
const uploadRef = ref()
const scholarship = ref(null)
const submitting = ref(false)
const fileList = ref([])

const form = reactive({
  scholarshipId: null,
  personalStatement: '',
  attachmentUrl: '',
  agree: false
})

const rules = {
  personalStatement: [
    { required: true, message: '请填写个人陈述', trigger: 'blur' },
    { min: 200, message: '个人陈述至少200字', trigger: 'blur' }
  ],
  agree: [
    { required: true, message: '请确认信息真实性', trigger: 'change' },
    {
      validator: (rule, value, callback) => {
        if (!value) {
          callback(new Error('请确认信息真实性'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ]
}

// 上传配置
const uploadAction = computed(() => {
  return `${import.meta.env.VITE_API_BASE_URL}/api/files/upload`
})

const uploadHeaders = computed(() => {
  return {
    Authorization: `Bearer ${getToken()}`
  }
})

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

// 文件上传前检查
const beforeUpload = (file) => {
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isLt10M) {
    ElMessage.error('文件大小不能超过 10MB!')
  }
  return isLt10M
}

// 上传成功
const handleUploadSuccess = (response, file, fileList) => {
  if (response.code === 200) {
    const urls = fileList
      .filter(f => f.response && f.response.code === 200)
      .map(f => f.response.data)
    form.attachmentUrl = urls.join(',')
    ElMessage.success('文件上传成功')
  } else {
    ElMessage.error(response.message || '文件上传失败')
  }
}

// 上传失败
const handleUploadError = (error) => {
  console.error('上传失败:', error)
  ElMessage.error('文件上传失败，请重试')
}

// 获取奖学金详情
const fetchScholarshipDetail = async () => {
  try {
    const res = await getScholarshipDetail(route.params.id)
    scholarship.value = res.data
    form.scholarshipId = res.data.id
  } catch (error) {
    console.error('获取奖学金详情失败:', error)
    ElMessage.error('获取奖学金详情失败')
    router.push({ name: 'StudentScholarships' })
  }
}

// 提交申请
const handleSubmit = async () => {
  try {
    await formRef.value.validate()

    await ElMessageBox.confirm(
      '确认提交申请？提交后将无法修改。',
      '确认提交',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    submitting.value = true

    const data = {
      scholarshipId: form.scholarshipId,
      personalStatement: form.personalStatement,
      attachmentUrl: form.attachmentUrl || null
    }

    await submitApplication(data)
    
    ElMessage.success('申请提交成功！')
    router.push({ name: 'StudentScholarshipApplications' })
  } catch (error) {
    if (error !== 'cancel') {
      console.error('提交申请失败:', error)
      ElMessage.error(error.message || '提交申请失败')
    }
  } finally {
    submitting.value = false
  }
}

// 取消
const handleCancel = () => {
  router.back()
}

onMounted(() => {
  if (route.params.id) {
    fetchScholarshipDetail()
  } else {
    ElMessage.error('缺少奖学金ID')
    router.push({ name: 'StudentScholarships' })
  }
})
</script>

<style scoped>
.scholarship-apply {
  padding: 20px;
}

.header-card {
  margin-bottom: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 0 0 10px 0;
  font-size: 24px;
  font-weight: bold;
}

.scholarship-info-card {
  margin-bottom: 20px;
}

.scholarship-info-card h3 {
  margin: 0 0 15px 0;
  font-size: 18px;
  font-weight: bold;
  color: #303133;
}

.amount {
  color: #f56c6c;
  font-size: 18px;
  font-weight: bold;
}

.form-tip {
  margin-top: 8px;
  padding: 10px;
  background: #ecf5ff;
  border-left: 4px solid #409eff;
  color: #606266;
  font-size: 13px;
  line-height: 1.6;
  border-radius: 4px;
}

.upload-demo {
  width: 100%;
}

:deep(.el-upload__tip) {
  margin-top: 8px;
  color: #909399;
  font-size: 12px;
}

:deep(.el-textarea__inner) {
  font-family: inherit;
}
</style>

