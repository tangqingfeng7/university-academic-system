<template>
  <div class="config-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>考勤配置管理</span>
          <el-button type="primary" @click="handleSave">
            <el-icon><Check /></el-icon>
            保存配置
          </el-button>
        </div>
      </template>

      <el-form :model="configForm" label-width="200px" v-loading="loading">
        <el-divider content-position="left">签到时间配置</el-divider>
        
        <el-form-item label="二维码有效期（分钟）">
          <el-input-number v-model="configForm['attendance.qrcode.expire_minutes']" :min="1" :max="30" />
        </el-form-item>
        
        <el-form-item label="提前签到时间（分钟）">
          <el-input-number v-model="configForm['attendance.early_checkin_minutes']" :min="0" :max="30" />
        </el-form-item>
        
        <el-form-item label="迟到判定时间（分钟）">
          <el-input-number v-model="configForm['attendance.late_threshold_minutes']" :min="1" :max="30" />
        </el-form-item>
        
        <el-form-item label="最晚签到时间（分钟）">
          <el-input-number v-model="configForm['attendance.max_late_minutes']" :min="5" :max="60" />
        </el-form-item>

        <el-divider content-position="left">定位签到配置</el-divider>
        
        <el-form-item label="地理围栏半径（米）">
          <el-input-number v-model="configForm['attendance.location.geofence_radius']" :min="10" :max="1000" />
        </el-form-item>

        <el-divider content-position="left">预警配置</el-divider>
        
        <el-form-item label="旷课预警阈值（次）">
          <el-input-number v-model="configForm['attendance.warning.absent_threshold']" :min="1" :max="10" />
        </el-form-item>
        
        <el-form-item label="旷课比例预警阈值">
          <el-input-number 
            v-model="configForm['attendance.warning.absent_rate_threshold']" 
            :min="0.1" 
            :max="1" 
            :step="0.1" 
            :precision="2" />
        </el-form-item>
        
        <el-form-item label="课程出勤率预警阈值">
          <el-input-number 
            v-model="configForm['attendance.warning.low_attendance_rate']" 
            :min="0.1" 
            :max="1" 
            :step="0.1" 
            :precision="2" />
        </el-form-item>
        
        <el-form-item label="教师未考勤提醒（天）">
          <el-input-number v-model="configForm['attendance.warning.teacher_no_attendance_days']" :min="1" :max="30" />
        </el-form-item>

        <el-divider content-position="left">功能开关</el-divider>
        
        <el-form-item label="启用手动点名">
          <el-switch v-model="configForm['attendance.method.manual.enabled']" />
        </el-form-item>
        
        <el-form-item label="启用扫码签到">
          <el-switch v-model="configForm['attendance.method.qrcode.enabled']" />
        </el-form-item>
        
        <el-form-item label="启用定位签到">
          <el-switch v-model="configForm['attendance.method.location.enabled']" />
        </el-form-item>

        <el-divider content-position="left">缓存配置</el-divider>
        
        <el-form-item label="统计缓存过期时间（分钟）">
          <el-input-number v-model="configForm['attendance.cache.expire_minutes']" :min="5" :max="120" />
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Check } from '@element-plus/icons-vue'
import { getAttendanceConfigs, updateAttendanceConfigs } from '@/api/attendance'

const loading = ref(false)
const configForm = ref({})

onMounted(() => {
  loadConfigs()
})

const loadConfigs = async () => {
  try {
    loading.value = true
    const res = await getAttendanceConfigs()
    const configs = res.data || []
    
    // 转换为form格式
    configs.forEach(config => {
      let value = config.configValue
      
      // 根据类型转换
      if (config.configType === 'INTEGER') {
        value = parseInt(value)
      } else if (config.configType === 'DOUBLE') {
        value = parseFloat(value)
      } else if (config.configType === 'BOOLEAN') {
        value = value === 'true'
      }
      
      configForm.value[config.configKey] = value
    })
  } catch (error) {
    ElMessage.error('加载配置失败')
  } finally {
    loading.value = false
  }
}

const handleSave = async () => {
  try {
    loading.value = true
    
    // 转换为字符串格式
    const configs = {}
    Object.keys(configForm.value).forEach(key => {
      configs[key] = String(configForm.value[key])
    })
    
    await updateAttendanceConfigs(configs)
    ElMessage.success('配置保存成功')
  } catch (error) {
    ElMessage.error(error.message || '保存失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.ml-2 {
  margin-left: 8px;
}
</style>

