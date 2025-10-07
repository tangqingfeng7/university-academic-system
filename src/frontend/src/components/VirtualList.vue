<template>
  <div ref="containerRef" class="virtual-list" @scroll="handleScroll">
    <div :style="{ height: `${totalHeight}px`, position: 'relative' }">
      <div
        v-for="item in visibleData"
        :key="item[itemKey]"
        :style="{
          position: 'absolute',
          top: `${item._virtualTop}px`,
          width: '100%'
        }"
      >
        <slot :item="item"></slot>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'

const props = defineProps({
  // 数据列表
  data: {
    type: Array,
    required: true,
    default: () => []
  },
  // 每项高度（固定高度）
  itemHeight: {
    type: Number,
    required: true,
    default: 50
  },
  // 容器高度
  height: {
    type: Number,
    default: 400
  },
  // 唯一key字段
  itemKey: {
    type: String,
    default: 'id'
  },
  // 缓冲数量（上下各渲染的额外项数）
  buffer: {
    type: Number,
    default: 5
  }
})

const containerRef = ref(null)
const scrollTop = ref(0)

// 总高度
const totalHeight = computed(() => props.data.length * props.itemHeight)

// 可见区域起始索引
const startIndex = computed(() => {
  const index = Math.floor(scrollTop.value / props.itemHeight) - props.buffer
  return Math.max(0, index)
})

// 可见区域结束索引
const endIndex = computed(() => {
  const index = Math.ceil((scrollTop.value + props.height) / props.itemHeight) + props.buffer
  return Math.min(props.data.length, index)
})

// 可见数据
const visibleData = computed(() => {
  return props.data.slice(startIndex.value, endIndex.value).map((item, index) => ({
    ...item,
    _virtualTop: (startIndex.value + index) * props.itemHeight
  }))
})

// 滚动处理
const handleScroll = (e) => {
  scrollTop.value = e.target.scrollTop
}

// 监听数据变化，重置滚动位置
watch(() => props.data, () => {
  if (containerRef.value) {
    containerRef.value.scrollTop = 0
    scrollTop.value = 0
  }
})

// 暴露方法：滚动到指定位置
const scrollToIndex = (index) => {
  if (containerRef.value) {
    const top = index * props.itemHeight
    containerRef.value.scrollTop = top
    scrollTop.value = top
  }
}

defineExpose({
  scrollToIndex
})
</script>

<style scoped>
.virtual-list {
  overflow-y: auto;
  overflow-x: hidden;
}
</style>

