<template>
  <div class="create-application">
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span style="font-weight: 600;">提交学籍异动申请</span>
          <el-button @click="goBack">返回</el-button>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
        style="max-width: 600px;"
      >
        <el-form-item label="异动类型" prop="type" required>
          <el-select v-model="form.type" placeholder="请选择异动类型">
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
            :rows="5"
            placeholder="请详细说明异动原因"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-form-item v-if="form.type === 'SUSPENSION'" label="开始日期" prop="startDate">
          <el-date-picker
            v-model="form.startDate"
            type="date"
            placeholder="选择开始日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>

        <el-form-item v-if="form.type === 'SUSPENSION'" label="结束日期" prop="endDate">
          <el-date-picker
            v-model="form.endDate"
            type="date"
            placeholder="选择结束日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>

        <el-form-item v-if="form.type === 'TRANSFER'" label="目标专业" prop="targetMajorId">
          <el-select v-model="form.targetMajorId" placeholder="请选择目标专业">
            <el-option
              v-for="major in majors"
              :key="major.id"
              :label="major.name"
              :value="major.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="证明材料">
          <el-upload
            ref="uploadRef"
            :auto-upload="false"
            :limit="1"
            accept=".pdf,.jpg,.jpeg,.png"
            :file-list="fileList"
            :on-change="handleFileChange"
          >
            <el-button>选择文件</el-button>
            <template #tip>
              <div style="color: #909399; font-size: 12px; margin-top: 7px;">
                支持上传 PDF、JPG、PNG 格式文件，大小不超过 10MB
              </div>
            </template>
          </el-upload>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">
            提交申请
          </el-button>
          <el-button @click="goBack">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createApplication } from '@/api/statusChange'

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
  // TODO: 获取专业列表
  majors.value = []
}

onMounted(() => {
  fetchMajors()
})
</script>

<style scoped>
.create-application {
  padding: 20px;
}
</style>

