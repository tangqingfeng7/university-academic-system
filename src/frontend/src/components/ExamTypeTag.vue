<template>
  <el-tag :type="tagType" :effect="effect" :size="size">
    {{ typeText }}
  </el-tag>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  type: {
    type: String,
    required: true
  },
  typeDescription: {
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

// 类型对应的标签类型
const typeTagMap = {
  MIDTERM: 'primary',
  FINAL: 'success',
  MAKEUP: 'warning',
  RETAKE: 'info'
}

// 类型对应的中文文本（备用）
const typeTextMap = {
  MIDTERM: '期中考试',
  FINAL: '期末考试',
  MAKEUP: '补考',
  RETAKE: '重修考试'
}

const tagType = computed(() => {
  return typeTagMap[props.type] || 'primary'
})

const typeText = computed(() => {
  return props.typeDescription || typeTextMap[props.type] || props.type
})
</script>

<style scoped>
/* 可选：添加自定义样式 */
</style>

