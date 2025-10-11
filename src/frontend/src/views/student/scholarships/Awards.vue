<template>
  <div class="scholarship-awards">
    <div class="page-header">
      <h1 class="page-title">我的获奖记录</h1>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-section">
      <el-row :gutter="20">
        <el-col :xs="24" :sm="8">
          <div class="stat-card">
            <div class="stat-label">获奖次数</div>
            <div class="stat-number">{{ awards.length }}</div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="8">
          <div class="stat-card">
            <div class="stat-label">累计金额</div>
            <div class="stat-number">¥{{ totalAmount.toLocaleString() }}</div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="8">
          <div class="stat-card">
            <div class="stat-label">最近学年</div>
            <div class="stat-number">{{ latestYear }}</div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="5" animated />
    </div>

    <!-- 获奖列表 -->
    <div v-else-if="awards.length > 0" class="awards-timeline">
      <el-timeline>
        <el-timeline-item
          v-for="award in awards"
          :key="award.id"
          :timestamp="formatDate(award.awardedAt)"
          placement="top"
          :color="getLevelColor(award.scholarshipLevel)"
        >
          <el-card shadow="hover" class="award-card">
            <div class="award-header">
              <div class="award-icon">
                <el-icon :size="40">
                  <el-icon v-if="award.scholarshipLevel === 'NATIONAL' || award.scholarshipLevel === '国家级'"><Trophy /></el-icon>
                  <el-icon v-else-if="award.scholarshipLevel === 'PROVINCIAL' || award.scholarshipLevel === '省级'"><Medal /></el-icon>
                  <el-icon v-else><Star /></el-icon>
                </el-icon>
              </div>
              <div class="award-info">
                <h3 class="award-name">{{ award.scholarshipName }}</h3>
                <div class="award-meta">
                  <el-tag :type="getLevelType(award.scholarshipLevel)" size="small">
                    {{ getLevelText(award.scholarshipLevel) }}
                  </el-tag>
                  <el-tag type="warning" size="small">
                    {{ award.academicYear }}学年
                  </el-tag>
                </div>
              </div>
              <div class="award-amount">
                <div class="amount-label">奖金金额</div>
                <div class="amount-value">¥{{ award.amount.toLocaleString() }}</div>
              </div>
            </div>

            <el-divider />

            <div class="award-details">
              <el-descriptions :column="2" size="small">
                <el-descriptions-item label="获奖时间">
                  {{ formatDate(award.awardedAt) }}
                </el-descriptions-item>
                <el-descriptions-item label="公示状态">
                  <el-tag v-if="award.published" type="success" size="small">
                    已公示
                  </el-tag>
                  <el-tag v-else type="info" size="small">
                    未公示
                  </el-tag>
                </el-descriptions-item>
              </el-descriptions>
            </div>

            <div class="award-footer">
              <el-button type="primary" link @click="viewAwardDetail(award)">
                查看详情
              </el-button>
              <el-button type="primary" link @click="downloadCertificate(award)">
                <el-icon><Download /></el-icon>
                下载证书
              </el-button>
            </div>
          </el-card>
        </el-timeline-item>
      </el-timeline>
    </div>

    <!-- 空状态 -->
    <el-empty v-else description="您还没有获奖记录">
      <el-button type="primary" @click="goToScholarships">
        去申请奖学金
      </el-button>
    </el-empty>

    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailVisible"
      title="获奖详情"
      width="600px"
      :close-on-click-modal="false"
    >
      <div v-if="currentAward" class="detail-content">
        <div class="detail-header">
          <div class="detail-icon">
            <el-icon :size="50">
              <Trophy />
            </el-icon>
          </div>
          <div>
            <h3>{{ currentAward.scholarshipName }}</h3>
            <p class="detail-subtitle">{{ currentAward.academicYear }}学年</p>
          </div>
        </div>

        <el-descriptions :column="2" border style="margin-top: 20px;">
          <el-descriptions-item label="奖学金等级">
            <el-tag :type="getLevelType(currentAward.scholarshipLevel)">
              {{ getLevelText(currentAward.scholarshipLevel) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="奖金金额">
            <span class="amount">¥{{ currentAward.amount.toLocaleString() }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="获奖时间" :span="2">
            {{ formatDate(currentAward.awardedAt) }}
          </el-descriptions-item>
          <el-descriptions-item label="公示状态" :span="2">
            <el-tag v-if="currentAward.published" type="success">
              已公示
            </el-tag>
            <el-tag v-else type="info">
              未公示
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="GPA" :span="1">
            {{ currentAward.gpa || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="综合得分" :span="1">
            {{ currentAward.comprehensiveScore || '-' }}
          </el-descriptions-item>
        </el-descriptions>

        <el-alert
          title="恭喜您获得此项荣誉！"
          type="success"
          :closable="false"
          show-icon
          style="margin-top: 20px;"
        />
      </div>

      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button type="primary" @click="downloadCertificate(currentAward)">
          下载证书
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Medal, Trophy, Star, Money, Download } from '@element-plus/icons-vue'
import { getMyAwards } from '@/api/scholarship'
import { formatDate } from '@/utils/date'

const router = useRouter()

const loading = ref(true)
const awards = ref([])
const detailVisible = ref(false)
const currentAward = ref(null)

// 计算总金额
const totalAmount = computed(() => {
  return awards.value.reduce((sum, award) => sum + (award.amount || 0), 0)
})

// 计算最近学年
const latestYear = computed(() => {
  if (awards.value.length === 0) return '-'
  const years = awards.value.map(a => a.academicYear).sort().reverse()
  return years[0]
})

// 获取等级类型（支持中文和英文）
const getLevelType = (level) => {
  if (!level) return 'info'
  const types = {
    NATIONAL: 'danger',
    PROVINCIAL: 'warning',
    UNIVERSITY: 'primary',
    DEPARTMENT: 'success',
    '国家级': 'danger',
    '省级': 'warning',
    '校级': 'primary',
    '院系级': 'success'
  }
  return types[level] || 'info'
}

// 获取等级文本（如果已经是中文则直接返回）
const getLevelText = (level) => {
  if (!level) return ''
  // 如果已经是中文，直接返回
  if (['国家级', '省级', '校级', '院系级'].includes(level)) {
    return level
  }
  // 否则转换
  const texts = {
    NATIONAL: '国家级',
    PROVINCIAL: '省级',
    UNIVERSITY: '校级',
    DEPARTMENT: '院系级'
  }
  return texts[level] || level
}

// 获取等级颜色（支持中文和英文）
const getLevelColor = (level) => {
  if (!level) return '#909399'
  const colors = {
    NATIONAL: '#f56c6c',
    PROVINCIAL: '#e6a23c',
    UNIVERSITY: '#409eff',
    DEPARTMENT: '#67c23a',
    '国家级': '#f56c6c',
    '省级': '#e6a23c',
    '校级': '#409eff',
    '院系级': '#67c23a'
  }
  return colors[level] || '#909399'
}

// 获取获奖记录
const fetchAwards = async () => {
  try {
    loading.value = true
    const res = await getMyAwards()
    awards.value = res.data || []
  } catch (error) {
    console.error('获取获奖记录失败:', error)
    ElMessage.error('获取获奖记录失败')
  } finally {
    loading.value = false
  }
}

// 查看详情
const viewAwardDetail = (award) => {
  currentAward.value = award
  detailVisible.value = true
}

// 下载证书
const downloadCertificate = (award) => {
  ElMessage.info('证书下载功能开发中...')
  // TODO: 实现证书下载功能
}

// 去申请奖学金
const goToScholarships = () => {
  router.push({ name: 'StudentScholarships' })
}

onMounted(() => {
  fetchAwards()
})
</script>

<style scoped>
.scholarship-awards {
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

.stats-section {
  margin-bottom: 20px;
}

.stat-card {
  background: white;
  border-radius: 8px;
  padding: 28px 24px;
  transition: all 0.2s ease;
  border: 1px solid #f0f0f0;
  height: 100%;
}

.stat-card:hover {
  border-color: #409eff;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.08);
}

.stat-label {
  font-size: 13px;
  color: #8c8c8c;
  margin-bottom: 12px;
  font-weight: 400;
  letter-spacing: 0.3px;
}

.stat-number {
  font-size: 36px;
  font-weight: 600;
  color: #262626;
  line-height: 1;
  font-variant-numeric: tabular-nums;
}

.loading-container {
  padding: 20px;
}

.awards-timeline {
  margin-top: 20px;
}

.award-card {
  transition: all 0.3s ease;
}

.award-card:hover {
  transform: translateX(5px);
}

.award-header {
  display: flex;
  align-items: center;
  gap: 15px;
}

.award-icon {
  flex-shrink: 0;
  width: 70px;
  height: 70px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.award-info {
  flex: 1;
}

.award-name {
  margin: 0 0 10px 0;
  font-size: 18px;
  font-weight: bold;
  color: #303133;
}

.award-meta {
  display: flex;
  gap: 8px;
}

.award-amount {
  text-align: right;
}

.amount-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 5px;
}

.amount-value {
  font-size: 24px;
  font-weight: bold;
  color: #f56c6c;
}

.award-footer {
  margin-top: 15px;
  display: flex;
  gap: 15px;
}

.detail-header {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 8px;
}

.detail-icon {
  flex-shrink: 0;
  width: 70px;
  height: 70px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
}

.detail-header h3 {
  margin: 0 0 5px 0;
  font-size: 20px;
  font-weight: bold;
}

.detail-subtitle {
  margin: 0;
  opacity: 0.9;
  font-size: 14px;
}

.detail-content .amount {
  color: #f56c6c;
  font-size: 18px;
  font-weight: bold;
}
</style>

