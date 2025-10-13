<template>
  <div class="course-credit-table">
    <el-table 
      :data="courses" 
      stripe
      :empty-text="emptyText"
    >
      <el-table-column 
        prop="courseName" 
        label="课程名称" 
        min-width="200"
      />
      <el-table-column 
        prop="courseNo" 
        label="课程代码" 
        width="120"
      />
      <el-table-column 
        label="课程类型" 
        width="100"
      >
        <template #default="{ row }">
          <el-tag 
            :type="getCourseTypeTag(row.courseType)" 
            size="small"
          >
            {{ getCourseTypeText(row.courseType) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column 
        prop="credits" 
        label="学分" 
        width="80"
        align="center"
      />
      <el-table-column 
        prop="totalScore" 
        label="成绩" 
        width="80"
        align="center"
      >
        <template #default="{ row }">
          <span :class="getScoreClass(row.totalScore)">
            {{ row.totalScore || '-' }}
          </span>
        </template>
      </el-table-column>
      <el-table-column 
        prop="gradePoint" 
        label="绩点" 
        width="80"
        align="center"
      >
        <template #default="{ row }">
          {{ row.gradePoint?.toFixed(2) || '-' }}
        </template>
      </el-table-column>
      <el-table-column 
        prop="semesterName" 
        label="修读学期" 
        width="120"
      />
      <el-table-column 
        label="状态" 
        width="80"
        align="center"
      >
        <template #default="{ row }">
          <el-tag 
            :type="row.totalScore >= 60 ? 'success' : 'danger'" 
            size="small"
          >
            {{ row.totalScore >= 60 ? '通过' : '未通过' }}
          </el-tag>
        </template>
      </el-table-column>
    </el-table>

    <!-- 统计信息 -->
    <div class="table-footer" v-if="courses && courses.length > 0">
      <el-descriptions :column="4" size="small" border>
        <el-descriptions-item label="课程数量">
          {{ courses.length }}
        </el-descriptions-item>
        <el-descriptions-item label="总学分">
          {{ totalCredits.toFixed(1) }}
        </el-descriptions-item>
        <el-descriptions-item label="平均成绩">
          {{ averageScore.toFixed(2) }}
        </el-descriptions-item>
        <el-descriptions-item label="平均绩点">
          {{ averageGradePoint.toFixed(2) }}
        </el-descriptions-item>
      </el-descriptions>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  courses: {
    type: Array,
    default: () => []
  }
})

const emptyText = computed(() => '暂无课程数据')

// 课程类型标签
const getCourseTypeTag = (type) => {
  const tagMap = {
    'REQUIRED': 'danger',
    'ELECTIVE': 'warning',
    'PUBLIC': 'info'
  }
  return tagMap[type] || ''
}

// 课程类型文本
const getCourseTypeText = (type) => {
  const textMap = {
    'REQUIRED': '必修',
    'ELECTIVE': '选修',
    'PUBLIC': '公共'
  }
  return textMap[type] || type
}

// 成绩样式
const getScoreClass = (score) => {
  if (!score) return ''
  if (score >= 90) return 'score-excellent'
  if (score >= 80) return 'score-good'
  if (score >= 70) return 'score-medium'
  if (score >= 60) return 'score-pass'
  return 'score-fail'
}

// 总学分
const totalCredits = computed(() => {
  if (!props.courses || props.courses.length === 0) return 0
  return props.courses.reduce((sum, course) => sum + (course.credits || 0), 0)
})

// 平均成绩
const averageScore = computed(() => {
  if (!props.courses || props.courses.length === 0) return 0
  const validScores = props.courses.filter(c => c.totalScore)
  if (validScores.length === 0) return 0
  const sum = validScores.reduce((total, course) => total + course.totalScore, 0)
  return sum / validScores.length
})

// 平均绩点
const averageGradePoint = computed(() => {
  if (!props.courses || props.courses.length === 0) return 0
  const validGradePoints = props.courses.filter(c => c.gradePoint)
  if (validGradePoints.length === 0) return 0
  const sum = validGradePoints.reduce((total, course) => total + course.gradePoint, 0)
  return sum / validGradePoints.length
})
</script>

<style scoped lang="scss">
.course-credit-table {
  .score-excellent {
    color: #67c23a;
    font-weight: bold;
  }

  .score-good {
    color: #409eff;
    font-weight: bold;
  }

  .score-medium {
    color: #e6a23c;
  }

  .score-pass {
    color: #909399;
  }

  .score-fail {
    color: #f56c6c;
    font-weight: bold;
  }

  .table-footer {
    margin-top: 20px;
  }
}
</style>

