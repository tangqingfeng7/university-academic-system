<template>
  <div class="search-form">
    <el-form
      ref="formRef"
      :model="formData"
      :inline="inline"
      :label-width="labelWidth"
      class="search-form-content"
    >
      <el-form-item
        v-for="item in fields"
        :key="item.prop"
        :label="item.label"
        :prop="item.prop"
      >
        <!-- 输入框 -->
        <el-input
          v-if="item.type === 'input' || !item.type"
          v-model="formData[item.prop]"
          :placeholder="item.placeholder || `请输入${item.label}`"
          :clearable="item.clearable !== false"
          v-bind="item.attrs"
        />

        <!-- 选择器 -->
        <el-select
          v-else-if="item.type === 'select'"
          v-model="formData[item.prop]"
          :placeholder="item.placeholder || `请选择${item.label}`"
          :clearable="item.clearable !== false"
          v-bind="item.attrs"
        >
          <el-option
            v-for="option in item.options"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </el-select>

        <!-- 日期选择器 -->
        <el-date-picker
          v-else-if="item.type === 'date'"
          v-model="formData[item.prop]"
          type="date"
          :placeholder="item.placeholder || `请选择${item.label}`"
          :clearable="item.clearable !== false"
          v-bind="item.attrs"
        />

        <!-- 日期范围选择器 -->
        <el-date-picker
          v-else-if="item.type === 'daterange'"
          v-model="formData[item.prop]"
          type="daterange"
          range-separator="-"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          :clearable="item.clearable !== false"
          v-bind="item.attrs"
        />

        <!-- 时间选择器 -->
        <el-time-picker
          v-else-if="item.type === 'time'"
          v-model="formData[item.prop]"
          :placeholder="item.placeholder || `请选择${item.label}`"
          :clearable="item.clearable !== false"
          v-bind="item.attrs"
        />

        <!-- 数字输入框 -->
        <el-input-number
          v-else-if="item.type === 'number'"
          v-model="formData[item.prop]"
          :placeholder="item.placeholder || `请输入${item.label}`"
          v-bind="item.attrs"
        />

        <!-- 自定义插槽 -->
        <slot v-else-if="item.type === 'slot'" :name="item.prop" />
      </el-form-item>

      <el-form-item class="form-actions">
        <el-button type="primary" :icon="Search" @click="handleSearch">
          搜索
        </el-button>
        <el-button :icon="Refresh" @click="handleReset">
          重置
        </el-button>
        <slot name="actions" />
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { Search, Refresh } from '@element-plus/icons-vue'

const props = defineProps({
  // 表单字段配置
  fields: {
    type: Array,
    required: true
  },
  // 初始值
  modelValue: {
    type: Object,
    default: () => ({})
  },
  // 是否行内表单
  inline: {
    type: Boolean,
    default: true
  },
  // 标签宽度
  labelWidth: {
    type: String,
    default: '80px'
  }
})

const emit = defineEmits(['search', 'reset', 'update:modelValue'])

const formRef = ref(null)
const formData = reactive({})

// 初始化表单数据
const initFormData = () => {
  props.fields.forEach(field => {
    formData[field.prop] = props.modelValue[field.prop] || field.defaultValue || ''
  })
}

// 监听modelValue变化
watch(
  () => props.modelValue,
  (newVal) => {
    Object.assign(formData, newVal)
  },
  { immediate: true, deep: true }
)

// 初始化
initFormData()

// 搜索
const handleSearch = () => {
  emit('update:modelValue', { ...formData })
  emit('search', { ...formData })
}

// 重置
const handleReset = () => {
  formRef.value?.resetFields()
  initFormData()
  emit('update:modelValue', { ...formData })
  emit('reset', { ...formData })
}

// 暴露方法
defineExpose({
  resetFields: () => handleReset()
})
</script>

<style scoped lang="scss">
.search-form {
  padding: 20px;
  background: #fff;
  border-radius: 4px;
  margin-bottom: 20px;

  .search-form-content {
    .form-actions {
      margin-left: 20px;
    }
  }

  :deep(.el-form-item) {
    margin-bottom: 18px;
  }

  :deep(.el-input),
  :deep(.el-select),
  :deep(.el-date-editor) {
    width: 200px;
  }
}
</style>

