<template>
  <div class="data-table">
    <el-table
      :data="data"
      :loading="loading"
      :height="height"
      :max-height="maxHeight"
      :stripe="stripe"
      :border="border"
      v-bind="$attrs"
      @selection-change="handleSelectionChange"
      @sort-change="handleSortChange"
    >
      <!-- 选择列 -->
      <el-table-column
        v-if="showSelection"
        type="selection"
        width="55"
        align="center"
      />

      <!-- 序号列 -->
      <el-table-column
        v-if="showIndex"
        type="index"
        label="序号"
        width="60"
        align="center"
        :index="indexMethod"
      />

      <!-- 动态列 -->
      <el-table-column
        v-for="column in columns"
        :key="column.prop"
        :prop="column.prop"
        :label="column.label"
        :width="column.width"
        :min-width="column.minWidth"
        :align="column.align || 'left'"
        :sortable="column.sortable"
        :fixed="column.fixed"
        :show-overflow-tooltip="column.showOverflowTooltip !== false"
      >
        <template v-if="column.slot" #default="scope">
          <slot :name="column.slot" :row="scope.row" :index="scope.$index" />
        </template>
        <template v-else-if="column.formatter" #default="scope">
          {{ column.formatter(scope.row, column) }}
        </template>
      </el-table-column>

      <!-- 操作列 -->
      <el-table-column
        v-if="$slots.actions"
        label="操作"
        :width="actionsWidth"
        :fixed="actionsFixed"
        align="center"
      >
        <template #default="scope">
          <slot name="actions" :row="scope.row" :index="scope.$index" />
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div v-if="showPagination" class="pagination-container">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="pageSizes"
        :total="total"
        :layout="paginationLayout"
        :background="true"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'

const props = defineProps({
  // 表格数据
  data: {
    type: Array,
    default: () => []
  },
  // 列配置
  columns: {
    type: Array,
    default: () => []
  },
  // 是否加载中
  loading: {
    type: Boolean,
    default: false
  },
  // 表格高度
  height: {
    type: [String, Number],
    default: undefined
  },
  // 表格最大高度
  maxHeight: {
    type: [String, Number],
    default: undefined
  },
  // 是否显示斑马纹
  stripe: {
    type: Boolean,
    default: true
  },
  // 是否显示边框
  border: {
    type: Boolean,
    default: true
  },
  // 是否显示选择列
  showSelection: {
    type: Boolean,
    default: false
  },
  // 是否显示序号列
  showIndex: {
    type: Boolean,
    default: false
  },
  // 是否显示分页
  showPagination: {
    type: Boolean,
    default: true
  },
  // 当前页
  page: {
    type: Number,
    default: 1
  },
  // 每页条数
  size: {
    type: Number,
    default: 10
  },
  // 总条数
  total: {
    type: Number,
    default: 0
  },
  // 每页条数选项
  pageSizes: {
    type: Array,
    default: () => [10, 20, 50, 100]
  },
  // 分页布局
  paginationLayout: {
    type: String,
    default: 'total, sizes, prev, pager, next, jumper'
  },
  // 操作列宽度
  actionsWidth: {
    type: [String, Number],
    default: 200
  },
  // 操作列固定
  actionsFixed: {
    type: [String, Boolean],
    default: 'right'
  }
})

const emit = defineEmits([
  'selection-change',
  'sort-change',
  'page-change',
  'size-change'
])

const currentPage = ref(props.page)
const pageSize = ref(props.size)

// 监听props.page的变化，同步到currentPage
watch(() => props.page, (newVal) => {
  currentPage.value = newVal
})

// 监听props.size的变化，同步到pageSize
watch(() => props.size, (newVal) => {
  pageSize.value = newVal
})

// 序号计算
const indexMethod = (index) => {
  return (currentPage.value - 1) * pageSize.value + index + 1
}

// 选择变化
const handleSelectionChange = (selection) => {
  emit('selection-change', selection)
}

// 排序变化
const handleSortChange = ({ prop, order }) => {
  emit('sort-change', { prop, order })
}

// 每页条数变化
const handleSizeChange = (size) => {
  pageSize.value = size
  emit('size-change', size)
  emit('page-change', { page: currentPage.value, size: pageSize.value })
}

// 当前页变化
const handleCurrentChange = (page) => {
  currentPage.value = page
  emit('page-change', { page: currentPage.value, size: pageSize.value })
}
</script>

<style scoped lang="scss">
.data-table {
  .pagination-container {
    display: flex;
    justify-content: flex-end;
    margin-top: 20px;
  }
}
</style>

