<template>
  <div class="exam-time-display">
    <div v-if="!compact" class="time-block">
      <div class="time-label">考试时间</div>
      <div class="time-value">
        <el-icon><Clock /></el-icon>
        <span>{{ formattedExamTime }}</span>
      </div>
    </div>
    <div v-if="!compact && duration" class="time-block">
      <div class="time-label">考试时长</div>
      <div class="time-value">
        <el-icon><Timer /></el-icon>
        <span>{{ durationText }}</span>
      </div>
    </div>
    <div v-if="!compact && endTime" class="time-block">
      <div class="time-label">结束时间</div>
      <div class="time-value">
        <el-icon><Clock /></el-icon>
        <span>{{ formattedEndTime }}</span>
      </div>
    </div>
    
    <!-- 紧凑模式 -->
    <div v-if="compact" class="compact-mode">
      <el-icon><Clock /></el-icon>
      <span>{{ formattedExamTime }}</span>
      <el-divider direction="vertical" v-if="duration" />
      <span v-if="duration">{{ durationText }}</span>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Clock, Timer } from '@element-plus/icons-vue'

const props = defineProps({
  examTime: {
    type: String,
    required: true
  },
  duration: {
    type: Number,
    default: null
  },
  endTime: {
    type: String,
    default: ''
  },
  compact: {
    type: Boolean,
    default: false
  }
})

// 格式化考试时间
const formattedExamTime = computed(() => {
  if (!props.examTime) return '--'
  
  const date = new Date(props.examTime)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  
  if (props.compact) {
    return `${month}-${day} ${hours}:${minutes}`
  }
  return `${year}-${month}-${day} ${hours}:${minutes}`
})

// 格式化结束时间
const formattedEndTime = computed(() => {
  if (!props.endTime) return '--'
  
  const date = new Date(props.endTime)
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  
  return `${hours}:${minutes}`
})

// 时长文本
const durationText = computed(() => {
  if (!props.duration) return ''
  
  const hours = Math.floor(props.duration / 60)
  const minutes = props.duration % 60
  
  if (hours > 0 && minutes > 0) {
    return `${hours}小时${minutes}分钟`
  } else if (hours > 0) {
    return `${hours}小时`
  } else {
    return `${minutes}分钟`
  }
})
</script>

<style scoped>
.exam-time-display {
  display: flex;
  gap: 20px;
  align-items: center;
}

.time-block {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.time-label {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.time-value {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: var(--el-text-color-primary);
}

.time-value .el-icon {
  color: var(--el-color-primary);
}

.compact-mode {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.compact-mode .el-icon {
  color: var(--el-color-primary);
}

.compact-mode .el-divider {
  margin: 0 4px;
}
</style>

