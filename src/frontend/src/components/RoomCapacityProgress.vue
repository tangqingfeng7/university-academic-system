<template>
  <div class="room-capacity-progress">
    <div v-if="showText" class="capacity-text">
      <span class="current">{{ assignedCount }}</span>
      <span class="separator">/</span>
      <span class="total">{{ capacity }}</span>
      <span v-if="showLabel" class="label">人</span>
    </div>
    
    <el-progress
      :percentage="percentage"
      :status="progressStatus"
      :stroke-width="strokeWidth"
      :show-text="showPercentage"
      :text-inside="textInside"
    />
    
    <div v-if="showRemaining" class="remaining-text">
      剩余 <span class="remaining-count">{{ remainingCapacity }}</span> 个座位
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  capacity: {
    type: Number,
    required: true
  },
  assignedCount: {
    type: Number,
    required: true
  },
  strokeWidth: {
    type: Number,
    default: 6
  },
  showText: {
    type: Boolean,
    default: true
  },
  showPercentage: {
    type: Boolean,
    default: false
  },
  showLabel: {
    type: Boolean,
    default: true
  },
  showRemaining: {
    type: Boolean,
    default: false
  },
  textInside: {
    type: Boolean,
    default: false
  }
})

// 计算百分比
const percentage = computed(() => {
  if (props.capacity === 0) return 0
  return Math.round((props.assignedCount / props.capacity) * 100)
})

// 剩余容量
const remainingCapacity = computed(() => {
  return Math.max(0, props.capacity - props.assignedCount)
})

// 进度条状态
const progressStatus = computed(() => {
  const percent = percentage.value
  if (percent >= 100) return 'exception'
  if (percent >= 90) return 'warning'
  if (percent >= 70) return ''
  return 'success'
})
</script>

<style scoped>
.room-capacity-progress {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.capacity-text {
  display: flex;
  align-items: baseline;
  gap: 4px;
  font-size: 14px;
}

.capacity-text .current {
  font-size: 18px;
  font-weight: 600;
  color: var(--el-color-primary);
}

.capacity-text .separator {
  color: var(--el-text-color-secondary);
}

.capacity-text .total {
  font-size: 16px;
  color: var(--el-text-color-regular);
}

.capacity-text .label {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-left: 2px;
}

.remaining-text {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.remaining-text .remaining-count {
  font-weight: 600;
  color: var(--el-color-success);
}
</style>

