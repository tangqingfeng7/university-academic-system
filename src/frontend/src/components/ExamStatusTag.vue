<template>
  <el-tag :type="tagType" :effect="effect" :size="size">
    {{ statusText }}
  </el-tag>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  status: {
    type: String,
    required: true
  },
  statusDescription: {
    type: String,
    default: ''
  },
  size: {
    type: String,
    default: 'default'
  },
  effect: {
    type: String,
    default: 'light'
  }
})

// 状态对应的标签类型
const statusTypeMap = {
  DRAFT: 'info',
  PUBLISHED: 'success',
  IN_PROGRESS: 'warning',
  FINISHED: '',
  CANCELLED: 'danger'
}

// 状态对应的中文文本（备用）
const statusTextMap = {
  DRAFT: '草稿',
  PUBLISHED: '已发布',
  IN_PROGRESS: '进行中',
  FINISHED: '已结束',
  CANCELLED: '已取消'
}

const tagType = computed(() => {
  return statusTypeMap[props.status] || 'info'
})

const statusText = computed(() => {
  return props.statusDescription || statusTextMap[props.status] || props.status
})
</script>

<style scoped>
/* 可选：添加自定义样式 */
</style>

