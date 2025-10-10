<template>
  <el-dialog
    v-model="visible"
    title="修改评价"
    width="600px"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="100px"
    >
      <!-- 课程信息 -->
      <div class="course-info-section">
        <h3>{{ evaluation?.courseName }}</h3>
        <p>教师：{{ evaluation?.teacherName }}</p>
      </div>

      <!-- 评分 -->
      <el-form-item label="评分" prop="rating">
        <el-rate
          v-model="form.rating"
          :texts="['很差', '较差', '一般', '良好', '优秀']"
          show-text
          size="large"
        />
      </el-form-item>

      <!-- 评论 -->
      <el-form-item label="评论" prop="comment">
        <el-input
          v-model="form.comment"
          type="textarea"
          :rows="6"
          placeholder="分享你的看法（选填）"
          maxlength="1000"
          show-word-limit
        />
      </el-form-item>

      <!-- 是否匿名 -->
      <el-form-item label="匿名评价">
        <el-switch
          v-model="form.anonymous"
          active-text="匿名"
          inactive-text="实名"
        />
        <div class="tip-text">
          <el-icon><InfoFilled /></el-icon>
          匿名评价时，教师无法看到你的姓名和学号
        </div>
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">
          保存修改
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { InfoFilled } from '@element-plus/icons-vue'
import { updateCourseEvaluation } from '@/api/evaluation'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  evaluation: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['update:modelValue', 'success'])

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const formRef = ref(null)
const submitting = ref(false)

const form = ref({
  rating: 0,
  comment: '',
  anonymous: true
})

const rules = {
  rating: [
    { required: true, message: '请进行评分', trigger: 'change' },
    { type: 'number', min: 1, max: 5, message: '评分必须在1-5之间', trigger: 'change' }
  ]
}

// 监听对话框打开
watch(() => props.modelValue, (val) => {
  if (val && props.evaluation) {
    form.value = {
      rating: props.evaluation.rating || 0,
      comment: props.evaluation.comment || '',
      anonymous: props.evaluation.anonymous !== undefined ? props.evaluation.anonymous : true
    }
    formRef.value?.clearValidate()
  }
})

// 提交修改
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()

    submitting.value = true

    await updateCourseEvaluation(props.evaluation.id, {
      rating: form.value.rating,
      comment: form.value.comment,
      anonymous: form.value.anonymous
    })

    ElMessage.success('评价修改成功')
    emit('success')
    handleClose()
  } catch (error) {
    if (error !== false) {
      ElMessage.error(error.message || '评价修改失败')
    }
  } finally {
    submitting.value = false
  }
}

// 关闭对话框
const handleClose = () => {
  visible.value = false
}
</script>

<style scoped>
.course-info-section {
  margin-bottom: 24px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
}

.course-info-section h3 {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 8px 0;
}

.course-info-section p {
  font-size: 14px;
  color: #666;
  margin: 0;
}

.tip-text {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
}

.tip-text .el-icon {
  font-size: 14px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

:deep(.el-rate) {
  height: 24px;
}

:deep(.el-rate__text) {
  font-size: 14px;
  margin-left: 12px;
}
</style>

