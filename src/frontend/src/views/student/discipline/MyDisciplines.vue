<template>
  <div class="my-disciplines">
    <el-card shadow="never" class="header-card">
      <h2>我的处分记录</h2>
      <p class="subtitle">查看个人违纪处分信息和申诉状态</p>
    </el-card>

    <el-card v-loading="loading" shadow="never">
      <el-empty v-if="!loading && disciplines.length === 0" description="暂无处分记录" />

      <div v-else class="discipline-list">
        <div v-for="discipline in disciplines" :key="discipline.id" class="discipline-item">
          <div class="discipline-header">
            <div class="discipline-type">
              <el-tag :type="getTypeTag(discipline.disciplineType)" size="large">
                {{ discipline.disciplineTypeDescription }}
              </el-tag>
            </div>
            <div class="discipline-status">
              <el-tag :type="getStatusType(discipline.status)">
                {{ discipline.statusDescription }}
              </el-tag>
            </div>
          </div>

          <div class="discipline-content">
            <div class="info-row">
              <label>处分原因：</label>
              <span>{{ discipline.reason }}</span>
            </div>
            
            <div v-if="discipline.description" class="info-row">
              <label>详细描述：</label>
              <span>{{ discipline.description }}</span>
            </div>

            <div class="info-row">
              <label>违纪日期：</label>
              <span>{{ discipline.occurrenceDate }}</span>
            </div>

            <div class="info-row">
              <label>处分日期：</label>
              <span>{{ discipline.punishmentDate }}</span>
            </div>

            <div v-if="discipline.status === 'REMOVED'" class="info-row">
              <label>解除日期：</label>
              <span>{{ discipline.removedDate }}</span>
            </div>

            <div v-if="discipline.status === 'REMOVED' && discipline.removedReason" class="info-row">
              <label>解除原因：</label>
              <span>{{ discipline.removedReason }}</span>
            </div>
          </div>

          <div class="discipline-actions">
            <el-button type="info" size="small" @click="handleView(discipline)">
              查看详情
            </el-button>
            <el-button v-if="discipline.status === 'ACTIVE'" type="primary" size="small" @click="handleAppeal(discipline)">
              提交申诉
            </el-button>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 查看详情对话框 -->
    <el-dialog
      v-model="viewDialogVisible"
      title="处分详情"
      width="700px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="学号">{{ viewData.studentNo }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ viewData.studentName }}</el-descriptions-item>
        <el-descriptions-item label="专业" :span="2">{{ viewData.majorName }}</el-descriptions-item>
        <el-descriptions-item label="班级" :span="2">{{ viewData.className }}</el-descriptions-item>
        
        <el-descriptions-item label="处分类型" :span="2">
          <el-tag :type="getTypeTag(viewData.disciplineType)">
            {{ viewData.disciplineTypeDescription }}
          </el-tag>
        </el-descriptions-item>
        
        <el-descriptions-item label="处分原因" :span="2">{{ viewData.reason }}</el-descriptions-item>
        <el-descriptions-item label="详细说明" :span="2">{{ viewData.description || '-' }}</el-descriptions-item>
        
        <el-descriptions-item label="发生日期">{{ viewData.occurrenceDate }}</el-descriptions-item>
        <el-descriptions-item label="处分日期">{{ viewData.punishmentDate }}</el-descriptions-item>
        
        <el-descriptions-item label="处分状态" :span="2">
          <el-tag :type="getStatusType(viewData.status)">
            {{ viewData.statusDescription }}
          </el-tag>
        </el-descriptions-item>

        <el-descriptions-item label="审批状态" :span="2">
          <el-tag :type="getApprovalStatusType(viewData.approvalStatus)">
            {{ viewData.approvalStatusDescription }}
          </el-tag>
        </el-descriptions-item>
        
        <el-descriptions-item label="可否解除" :span="2">
          <el-tag :type="viewData.canRemove ? 'success' : 'info'">
            {{ viewData.canRemove ? '是' : '否' }}
          </el-tag>
        </el-descriptions-item>
        
        <el-descriptions-item v-if="viewData.status === 'REMOVED'" label="解除日期">
          {{ viewData.removedDate }}
        </el-descriptions-item>
        <el-descriptions-item v-if="viewData.status === 'REMOVED'" label="解除原因">
          {{ viewData.removedReason }}
        </el-descriptions-item>
        
        <el-descriptions-item label="上报人">{{ viewData.reporterName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="审批人">{{ viewData.approverName || '-' }}</el-descriptions-item>
        
        <el-descriptions-item v-if="viewData.approvalComment" label="审批意见" :span="2">
          {{ viewData.approvalComment }}
        </el-descriptions-item>
        
        <el-descriptions-item v-if="viewData.attachmentUrl" label="附件" :span="2">
          <el-link :href="viewData.attachmentUrl" type="primary" target="_blank">查看附件</el-link>
        </el-descriptions-item>
        
        <el-descriptions-item label="创建时间">{{ viewData.createdAt }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ viewData.updatedAt }}</el-descriptions-item>
      </el-descriptions>
      
      <template #footer>
        <el-button @click="viewDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 申诉对话框 -->
    <el-dialog v-model="appealDialog.visible" title="提交申诉" width="600px">
      <el-form :model="appealDialog.form" :rules="appealRules" ref="appealFormRef" label-width="100px">
        <el-form-item label="申诉理由" prop="appealReason">
          <el-input
            v-model="appealDialog.form.appealReason"
            type="textarea"
            :rows="6"
            placeholder="请详细说明申诉理由"
          />
        </el-form-item>

        <el-form-item label="证据材料">
          <el-input
            v-model="appealDialog.form.evidence"
            type="textarea"
            :rows="4"
            placeholder="请提供相关证据（可选）"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="appealDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="appealDialog.loading" @click="handleSubmitAppeal">
          提交
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMyDisciplines, createAppeal } from '@/api/discipline'

// 数据
const loading = ref(false)
const disciplines = ref([])

// 查看详情
const viewDialogVisible = ref(false)
const viewData = ref({})

// 申诉对话框
const appealDialog = reactive({
  visible: false,
  loading: false,
  disciplineId: null,
  form: {
    appealReason: '',
    evidence: ''
  }
})

const appealFormRef = ref(null)

// 表单验证规则
const appealRules = {
  appealReason: [
    { required: true, message: '请输入申诉理由', trigger: 'blur' },
    { min: 10, message: '申诉理由至少10个字', trigger: 'blur' }
  ]
}

// 获取处分列表
const fetchDisciplines = async () => {
  loading.value = true
  try {
    const res = await getMyDisciplines()
    disciplines.value = res.data || []
  } catch (error) {
    console.error('获取处分列表失败:', error)
    ElMessage.error('获取处分列表失败')
  } finally {
    loading.value = false
  }
}

// 查看详情
const handleView = (discipline) => {
  viewData.value = { ...discipline }
  viewDialogVisible.value = true
}

// 提交申诉
const handleAppeal = (discipline) => {
  appealDialog.disciplineId = discipline.id
  appealDialog.form.appealReason = ''
  appealDialog.form.evidence = ''
  appealDialog.visible = true
}

// 提交申诉表单
const handleSubmitAppeal = async () => {
  try {
    await appealFormRef.value.validate()
    
    appealDialog.loading = true
    
    await createAppeal({
      disciplineId: appealDialog.disciplineId,
      appealReason: appealDialog.form.appealReason,
      evidence: appealDialog.form.evidence
    })
    
    ElMessage.success('申诉已提交')
    appealDialog.visible = false
    fetchDisciplines()
  } catch (error) {
    if (error !== false) {
      console.error('提交申诉失败:', error)
      ElMessage.error('提交申诉失败')
    }
  } finally {
    appealDialog.loading = false
  }
}

// 处分类型标签
const getTypeTag = (type) => {
  const tagMap = {
    WARNING: 'warning',
    SERIOUS_WARNING: 'warning',
    DEMERIT: 'danger',
    SERIOUS_DEMERIT: 'danger',
    PROBATION: 'danger',
    EXPULSION: 'danger'
  }
  return tagMap[type] || 'info'
}

// 状态标签类型
const getStatusType = (status) => {
  const typeMap = {
    ACTIVE: 'danger',
    REMOVED: 'success',
    APPEALING: 'warning'
  }
  return typeMap[status] || 'info'
}

// 审批状态标签类型
const getApprovalStatusType = (status) => {
  const typeMap = {
    PENDING: 'warning',
    APPROVED: 'success',
    REJECTED: 'danger'
  }
  return typeMap[status] || 'info'
}

// 页面加载
onMounted(() => {
  fetchDisciplines()
})
</script>

<style scoped>
.my-disciplines {
  padding: 20px;
}

.header-card {
  margin-bottom: 20px;
}

.header-card h2 {
  margin: 0 0 8px 0;
  font-size: 24px;
  color: #303133;
}

.subtitle {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.discipline-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.discipline-item {
  border: 1px solid #EBEEF5;
  border-radius: 8px;
  padding: 20px;
  background: #F9FAFC;
}

.discipline-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.discipline-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.info-row {
  display: flex;
  font-size: 14px;
}

.info-row label {
  color: #909399;
  width: 100px;
  flex-shrink: 0;
}

.info-row span {
  color: #303133;
  flex: 1;
}

.discipline-actions {
  margin-top: 16px;
  text-align: right;
}
</style>

