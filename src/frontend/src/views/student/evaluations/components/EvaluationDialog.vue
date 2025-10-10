<template>
  <el-dialog
    v-model="visible"
    title="课程评价"
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
        <h3>{{ offering?.course?.name }}</h3>
        <p>教师：{{ offering?.teacher?.name }}</p>
      </div>

      <!-- 课程评分 -->
      <el-form-item label="课程评分" prop="rating">
        <el-rate
          v-model="form.rating"
          :texts="['很差', '较差', '一般', '良好', '优秀']"
          show-text
          size="large"
        />
      </el-form-item>

      <!-- 教师评分 -->
      <el-divider content-position="left">教师评价</el-divider>

      <el-form-item label="教学能力" prop="teachingRating">
        <el-rate
          v-model="form.teachingRating"
          :texts="['很差', '较差', '一般', '良好', '优秀']"
          show-text
        />
      </el-form-item>

      <el-form-item label="教学态度" prop="attitudeRating">
        <el-rate
          v-model="form.attitudeRating"
          :texts="['很差', '较差', '一般', '良好', '优秀']"
          show-text
        />
      </el-form-item>

      <el-form-item label="教学内容" prop="contentRating">
        <el-rate
          v-model="form.contentRating"
          :texts="['很差', '较差', '一般', '良好', '优秀']"
          show-text
        />
      </el-form-item>

      <!-- 课程评论 -->
      <el-form-item label="课程评论" prop="courseComment">
        <el-input
          v-model="form.courseComment"
          type="textarea"
          :rows="4"
          placeholder="分享你对这门课程的看法（选填）"
          maxlength="1000"
          show-word-limit
        />
      </el-form-item>

      <!-- 教师评论 -->
      <el-form-item label="教师评论" prop="teacherComment">
        <el-input
          v-model="form.teacherComment"
          type="textarea"
          :rows="4"
          placeholder="分享你对任课教师的看法（选填）"
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
          提交评价
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { InfoFilled } from '@element-plus/icons-vue'
import { submitCourseEvaluation, submitTeacherEvaluation } from '@/api/evaluation'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  offering: {
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
  courseComment: '',
  teachingRating: 0,
  attitudeRating: 0,
  contentRating: 0,
  teacherComment: '',
  anonymous: true
})

const rules = {
  rating: [
    { required: true, message: '请对课程进行评分', trigger: 'change' },
    { type: 'number', min: 1, max: 5, message: '评分必须在1-5之间', trigger: 'change' }
  ],
  teachingRating: [
    { required: true, message: '请对教学能力进行评分', trigger: 'change' },
    { type: 'number', min: 1, max: 5, message: '评分必须在1-5之间', trigger: 'change' }
  ],
  attitudeRating: [
    { required: true, message: '请对教学态度进行评分', trigger: 'change' },
    { type: 'number', min: 1, max: 5, message: '评分必须在1-5之间', trigger: 'change' }
  ],
  contentRating: [
    { required: true, message: '请对教学内容进行评分', trigger: 'change' },
    { type: 'number', min: 1, max: 5, message: '评分必须在1-5之间', trigger: 'change' }
  ]
}

// 监听对话框打开
watch(() => props.modelValue, (val) => {
  if (val) {
    resetForm()
  }
})

// 重置表单
const resetForm = () => {
  form.value = {
    rating: 0,
    courseComment: '',
    teachingRating: 0,
    attitudeRating: 0,
    contentRating: 0,
    teacherComment: '',
    anonymous: true
  }
  formRef.value?.clearValidate()
}

// 提交评价
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()

    submitting.value = true

    // 提交课程评价
    const courseEvaluationData = {
      offeringId: props.offering.id,
      rating: form.value.rating,
      comment: form.value.courseComment,
      anonymous: form.value.anonymous
    }

    await submitCourseEvaluation(courseEvaluationData)

    // 提交教师评价
    const teacherEvaluationData = {
      teacherId: props.offering.teacher.id,
      offeringId: props.offering.id,
      teachingRating: form.value.teachingRating,
      attitudeRating: form.value.attitudeRating,
      contentRating: form.value.contentRating,
      comment: form.value.teacherComment,
      anonymous: form.value.anonymous
    }

    await submitTeacherEvaluation(teacherEvaluationData)

    ElMessage.success('评价提交成功')
    emit('success')
    handleClose()
  } catch (error) {
    if (error !== false) {
      ElMessage.error(error.message || '评价提交失败')
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

