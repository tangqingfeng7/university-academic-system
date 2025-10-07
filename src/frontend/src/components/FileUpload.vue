<template>
  <div class="file-upload">
    <el-upload
      ref="uploadRef"
      :action="action"
      :headers="headers"
      :data="data"
      :multiple="multiple"
      :accept="accept"
      :limit="limit"
      :file-list="fileList"
      :auto-upload="autoUpload"
      :show-file-list="showFileList"
      :drag="drag"
      :before-upload="handleBeforeUpload"
      :on-success="handleSuccess"
      :on-error="handleError"
      :on-exceed="handleExceed"
      :on-remove="handleRemove"
      v-bind="$attrs"
    >
      <template v-if="drag">
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">
          将文件拖到此处，或<em>点击上传</em>
        </div>
        <div v-if="tip" class="el-upload__tip">{{ tip }}</div>
      </template>
      <template v-else>
        <el-button :type="buttonType" :icon="UploadFilled">
          {{ buttonText }}
        </el-button>
        <div v-if="tip" class="el-upload__tip">{{ tip }}</div>
      </template>
    </el-upload>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const props = defineProps({
  // 上传地址
  action: {
    type: String,
    default: '#'
  },
  // 额外的上传数据
  data: {
    type: Object,
    default: () => ({})
  },
  // 是否支持多选
  multiple: {
    type: Boolean,
    default: false
  },
  // 接受的文件类型
  accept: {
    type: String,
    default: ''
  },
  // 最大上传文件数
  limit: {
    type: Number,
    default: undefined
  },
  // 文件大小限制（MB）
  maxSize: {
    type: Number,
    default: 10
  },
  // 是否自动上传
  autoUpload: {
    type: Boolean,
    default: true
  },
  // 是否显示文件列表
  showFileList: {
    type: Boolean,
    default: true
  },
  // 是否启用拖拽上传
  drag: {
    type: Boolean,
    default: false
  },
  // 按钮文本
  buttonText: {
    type: String,
    default: '选择文件'
  },
  // 按钮类型
  buttonType: {
    type: String,
    default: 'primary'
  },
  // 提示文本
  tip: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['success', 'error', 'remove', 'exceed'])

const userStore = useUserStore()
const uploadRef = ref(null)
const fileList = ref([])

// 请求头（添加token）
const headers = computed(() => ({
  Authorization: `Bearer ${userStore.token}`
}))

// 上传前校验
const handleBeforeUpload = (file) => {
  // 文件大小校验
  const isLt = file.size / 1024 / 1024 < props.maxSize
  if (!isLt) {
    ElMessage.error(`文件大小不能超过 ${props.maxSize}MB!`)
    return false
  }

  // 文件类型校验
  if (props.accept) {
    const acceptTypes = props.accept.split(',').map(type => type.trim())
    const fileType = file.name.substring(file.name.lastIndexOf('.'))
    const isAccept = acceptTypes.some(type => {
      if (type.startsWith('.')) {
        return fileType === type
      }
      return file.type.includes(type)
    })
    
    if (!isAccept) {
      ElMessage.error(`只能上传 ${props.accept} 格式的文件!`)
      return false
    }
  }

  return true
}

// 上传成功
const handleSuccess = (response, file, fileList) => {
  ElMessage.success('上传成功')
  emit('success', response, file, fileList)
}

// 上传失败
const handleError = (error, file, fileList) => {
  ElMessage.error('上传失败，请重试')
  emit('error', error, file, fileList)
}

// 文件超出个数限制
const handleExceed = (files, fileList) => {
  ElMessage.warning(`最多只能上传 ${props.limit} 个文件`)
  emit('exceed', files, fileList)
}

// 移除文件
const handleRemove = (file, fileList) => {
  emit('remove', file, fileList)
}

// 手动上传
const submit = () => {
  uploadRef.value?.submit()
}

// 清空文件列表
const clearFiles = () => {
  uploadRef.value?.clearFiles()
}

// 暴露方法
defineExpose({
  submit,
  clearFiles
})
</script>

<style scoped lang="scss">
.file-upload {
  :deep(.el-upload__tip) {
    margin-top: 8px;
    font-size: 12px;
    color: #606266;
  }

  :deep(.el-upload-dragger) {
    padding: 30px;
  }
}
</style>

