<template>
  <div class="scholarship-list">
    <div class="page-header">
      <h1 class="page-title">奖学金申请</h1>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="5" animated />
    </div>

    <!-- 奖学金列表 -->
    <div v-else-if="scholarships.length > 0" class="scholarship-grid">
      <el-card
        v-for="scholarship in scholarships"
        :key="scholarship.id"
        shadow="hover"
        class="scholarship-card"
        :class="{ 'inactive': !scholarship.active }"
      >
        <div class="scholarship-header">
          <div class="scholarship-icon">
            <el-icon :size="40">
              <el-icon v-if="scholarship.level === 'NATIONAL'"><Trophy /></el-icon>
              <el-icon v-else-if="scholarship.level === 'PROVINCIAL'"><Medal /></el-icon>
              <el-icon v-else><Star /></el-icon>
            </el-icon>
          </div>
          <div class="scholarship-info">
            <h3 class="scholarship-name">{{ scholarship.name }}</h3>
            <el-tag :type="getLevelType(scholarship.level)" size="small">
              {{ getLevelText(scholarship.level) }}
            </el-tag>
          </div>
        </div>

        <el-divider />

        <div class="scholarship-details">
          <div class="detail-item">
            <label>奖金金额：</label>
            <span class="amount">¥{{ scholarship.amount.toLocaleString() }}</span>
          </div>
          <div class="detail-item">
            <label>名额限制：</label>
            <span>{{ scholarship.quota }}人</span>
          </div>
          <div class="detail-item">
            <label>最低GPA：</label>
            <span>{{ scholarship.minGpa || '无要求' }}</span>
          </div>
          <div class="detail-item">
            <label>最低学分：</label>
            <span>{{ scholarship.minCredits || '无要求' }}</span>
          </div>
        </div>

        <div class="description">
          <p>{{ scholarship.description || '暂无描述' }}</p>
        </div>

        <div class="card-footer">
          <el-button
            type="primary"
            :disabled="!scholarship.active"
            @click="viewDetail(scholarship)"
          >
            查看详情
          </el-button>
          <el-button
            type="success"
            :disabled="!scholarship.active"
            @click="applyScholarship(scholarship)"
          >
            立即申请
          </el-button>
        </div>

        <div v-if="!scholarship.active" class="inactive-overlay">
          <el-tag type="info" size="large">未开放申请</el-tag>
        </div>
      </el-card>
    </div>

    <!-- 空状态 -->
    <el-empty v-else description="暂无可申请的奖学金" />

    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailVisible"
      title="奖学金详情"
      width="600px"
      :close-on-click-modal="false"
    >
      <div v-if="currentScholarship" class="detail-dialog">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="奖学金名称">
            {{ currentScholarship.name }}
          </el-descriptions-item>
          <el-descriptions-item label="等级">
            <el-tag :type="getLevelType(currentScholarship.level)">
              {{ getLevelText(currentScholarship.level) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="奖金金额">
            <span class="amount">¥{{ currentScholarship.amount.toLocaleString() }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="名额">
            {{ currentScholarship.quota }}人
          </el-descriptions-item>
          <el-descriptions-item label="最低GPA" :span="2">
            {{ currentScholarship.minGpa || '无要求' }}
          </el-descriptions-item>
          <el-descriptions-item label="最低学分" :span="2">
            {{ currentScholarship.minCredits || '无要求' }}
          </el-descriptions-item>
          <el-descriptions-item label="描述" :span="2">
            {{ currentScholarship.description || '暂无描述' }}
          </el-descriptions-item>
          <el-descriptions-item label="其他要求" :span="2">
            <div v-if="currentScholarship.requirements">
              {{ currentScholarship.requirements }}
            </div>
            <span v-else class="text-muted">无特殊要求</span>
          </el-descriptions-item>
        </el-descriptions>

        <el-alert
          v-if="eligibilityMessage"
          :title="eligibilityMessage"
          :type="isEligible ? 'success' : 'warning'"
          :closable="false"
          show-icon
          style="margin-top: 20px;"
        />
      </div>

      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button
          type="primary"
          :disabled="!currentScholarship?.active || !isEligible"
          @click="confirmApply"
        >
          立即申请
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Trophy, Medal, Star } from '@element-plus/icons-vue'
import { getAvailableScholarships, checkEligibility } from '@/api/scholarship'

const router = useRouter()

const loading = ref(true)
const scholarships = ref([])
const detailVisible = ref(false)
const currentScholarship = ref(null)
const isEligible = ref(false)
const eligibilityMessage = ref('')

// 获取可申请的奖学金列表
const fetchScholarships = async () => {
  try {
    loading.value = true
    const res = await getAvailableScholarships()
    scholarships.value = res.data || []
  } catch (error) {
    console.error('获取奖学金列表失败:', error)
    ElMessage.error('获取奖学金列表失败')
  } finally {
    loading.value = false
  }
}

// 获取等级类型
const getLevelType = (level) => {
  const types = {
    NATIONAL: 'danger',
    PROVINCIAL: 'warning',
    UNIVERSITY: 'primary',
    DEPARTMENT: 'success'
  }
  return types[level] || 'info'
}

// 获取等级文本
const getLevelText = (level) => {
  const texts = {
    NATIONAL: '国家级',
    PROVINCIAL: '省级',
    UNIVERSITY: '校级',
    DEPARTMENT: '院系级'
  }
  return texts[level] || level
}

// 查看详情
const viewDetail = async (scholarship) => {
  currentScholarship.value = scholarship
  detailVisible.value = true
  
  // 检查申请资格
  try {
    const res = await checkEligibility(scholarship.id)
    isEligible.value = res.data
    eligibilityMessage.value = isEligible.value 
      ? '您符合申请条件，可以提交申请'
      : '您暂不符合申请条件，请查看具体要求'
  } catch (error) {
    console.error('检查资格失败:', error)
    isEligible.value = false
    eligibilityMessage.value = '无法验证申请资格'
  }
}

// 申请奖学金
const applyScholarship = async (scholarship) => {
  try {
    // 先检查资格
    const res = await checkEligibility(scholarship.id)
    if (!res.data) {
      ElMessage.warning('您暂不符合申请条件')
      return
    }

    // 跳转到申请页面
    router.push({
      name: 'StudentScholarshipApply',
      params: { id: scholarship.id }
    })
  } catch (error) {
    console.error('检查资格失败:', error)
    ElMessage.error('检查资格失败')
  }
}

// 确认申请
const confirmApply = () => {
  detailVisible.value = false
  applyScholarship(currentScholarship.value)
}

onMounted(() => {
  fetchScholarships()
})
</script>

<style scoped>
.scholarship-list {
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.page-header {
  padding: 24px 0;
  margin-bottom: 20px;
}

.page-title {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #1a1a1a;
}

.loading-container {
  padding: 20px;
}

.scholarship-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
}

.scholarship-card {
  position: relative;
  transition: all 0.3s ease;
  overflow: hidden;
}

.scholarship-card:hover {
  transform: translateY(-5px);
}

.scholarship-card.inactive {
  opacity: 0.6;
}

.scholarship-header {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 15px;
}

.scholarship-icon {
  flex-shrink: 0;
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.scholarship-info {
  flex: 1;
}

.scholarship-name {
  margin: 0 0 8px 0;
  font-size: 18px;
  font-weight: bold;
  color: #303133;
}

.scholarship-details {
  margin: 15px 0;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}

.detail-item:last-child {
  border-bottom: none;
}

.detail-item label {
  color: #909399;
  font-size: 14px;
}

.detail-item span {
  font-weight: 500;
  color: #303133;
}

.amount {
  color: #f56c6c;
  font-size: 18px;
  font-weight: bold;
}

.description {
  margin: 15px 0;
  padding: 10px;
  background: #f5f7fa;
  border-radius: 8px;
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  min-height: 60px;
}

.card-footer {
  display: flex;
  gap: 10px;
  margin-top: 15px;
}

.card-footer .el-button {
  flex: 1;
}

.inactive-overlay {
  position: absolute;
  top: 0;
  right: 0;
  padding: 10px 20px;
  background: rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(2px);
}

.detail-dialog :deep(.el-descriptions__label) {
  width: 120px;
  font-weight: 500;
}

.text-muted {
  color: #909399;
  font-style: italic;
}
</style>

