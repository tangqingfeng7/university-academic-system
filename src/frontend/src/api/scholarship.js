import request from '@/utils/request'

// ==================== 学生端API ====================

/**
 * 查询可申请的奖学金列表
 */
export function getAvailableScholarships() {
  return request({
    url: '/student/scholarships/available',
    method: 'get'
  })
}

/**
 * 查询奖学金详情
 */
export function getScholarshipDetail(id) {
  return request({
    url: `/student/scholarships/${id}`,
    method: 'get'
  })
}

/**
 * 验证申请资格
 */
export function checkEligibility(scholarshipId) {
  return request({
    url: `/student/scholarships/${scholarshipId}/eligibility`,
    method: 'get'
  })
}

/**
 * 提交申请
 */
export function submitApplication(data) {
  return request({
    url: '/student/scholarship-applications',
    method: 'post',
    data
  })
}

/**
 * 查询我的申请列表
 */
export function getMyApplications() {
  return request({
    url: '/student/scholarship-applications',
    method: 'get'
  })
}

/**
 * 查询申请详情
 */
export function getApplicationDetail(id) {
  return request({
    url: `/student/scholarship-applications/${id}`,
    method: 'get'
  })
}

/**
 * 撤销申请
 */
export function cancelApplication(id) {
  return request({
    url: `/student/scholarship-applications/${id}`,
    method: 'delete'
  })
}

/**
 * 查询审批流程
 */
export function getApprovalFlow(id) {
  return request({
    url: `/student/scholarship-applications/${id}/approval-flow`,
    method: 'get'
  })
}

/**
 * 查询我的获奖记录
 */
export function getMyAwards() {
  return request({
    url: '/student/scholarship-awards',
    method: 'get'
  })
}

// ==================== 管理员端API ====================

/**
 * 创建奖学金
 */
export function createScholarship(data) {
  return request({
    url: '/admin/scholarships',
    method: 'post',
    data
  })
}

/**
 * 更新奖学金
 */
export function updateScholarship(id, data) {
  return request({
    url: `/admin/scholarships/${id}`,
    method: 'put',
    data
  })
}

/**
 * 查询奖学金列表
 */
export function getScholarships(params) {
  return request({
    url: '/admin/scholarships',
    method: 'get',
    params
  })
}

/**
 * 启用/禁用奖学金
 */
export function toggleActive(id, active) {
  return request({
    url: `/admin/scholarships/${id}/active`,
    method: 'put',
    params: { active }
  })
}

/**
 * 删除奖学金
 */
export function deleteScholarship(id) {
  return request({
    url: `/admin/scholarships/${id}`,
    method: 'delete'
  })
}

/**
 * 开启申请
 */
export function openApplication(id, data) {
  return request({
    url: `/admin/scholarships/${id}/open`,
    method: 'post',
    data
  })
}

/**
 * 自动评定
 */
export function autoEvaluate(data) {
  return request({
    url: '/admin/scholarships/evaluate',
    method: 'post',
    data
  })
}

/**
 * 查询申请列表
 */
export function getApplications(params) {
  return request({
    url: '/admin/scholarship-applications',
    method: 'get',
    params
  })
}

/**
 * 公示获奖名单
 */
export function publishAwards(data) {
  return request({
    url: '/admin/scholarship-awards/publish',
    method: 'post',
    data
  })
}

/**
 * 查询获奖名单
 */
export function getAwards(params) {
  return request({
    url: '/admin/scholarship-awards',
    method: 'get',
    params
  })
}

/**
 * 导出获奖名单
 */
export function exportAwards(params) {
  return request({
    url: '/admin/scholarship-awards/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}

/**
 * 获奖分布统计
 */
export function getDistribution(academicYear) {
  return request({
    url: '/admin/scholarship-statistics/distribution',
    method: 'get',
    params: { academicYear }
  })
}

/**
 * 按专业统计
 */
export function getStatsByMajor(academicYear, departmentId) {
  return request({
    url: '/admin/scholarship-statistics/by-major',
    method: 'get',
    params: { academicYear, departmentId }
  })
}

/**
 * 按年级统计
 */
export function getStatsByGrade(academicYear) {
  return request({
    url: '/admin/scholarship-statistics/by-grade',
    method: 'get',
    params: { academicYear }
  })
}

/**
 * 生成统计报告
 */
export function generateReport(academicYear) {
  return request({
    url: '/admin/scholarship-statistics/report',
    method: 'get',
    params: { academicYear },
    responseType: 'blob'
  })
}

/**
 * 年度对比统计
 */
export function getYearlyComparison(startYear, endYear) {
  return request({
    url: '/admin/scholarship-statistics/yearly-comparison',
    method: 'get',
    params: { startYear, endYear }
  })
}

// ==================== 审批人端API ====================

/**
 * 查询奖学金列表（用于筛选）
 */
export function getApproverScholarships(params) {
  return request({
    url: '/approver/scholarships',
    method: 'get',
    params
  })
}

/**
 * 查询待审批申请
 */
export function getPendingApplications(params) {
  return request({
    url: '/approver/scholarship-applications/pending',
    method: 'get',
    params
  })
}

/**
 * 查询申请详情（审批人）
 */
export function getApproverApplicationDetail(id) {
  return request({
    url: `/approver/scholarship-applications/${id}`,
    method: 'get'
  })
}

/**
 * 检查是否可以审批
 */
export function checkCanApprove(id) {
  return request({
    url: `/approver/scholarship-applications/${id}/can-approve`,
    method: 'get'
  })
}

/**
 * 查询审批流程
 */
export function getApproverApprovalFlow(id) {
  return request({
    url: `/approver/scholarship-applications/${id}/approval-flow`,
    method: 'get'
  })
}

/**
 * 查询审批历史
 */
export function getApproverApprovalHistory(id) {
  return request({
    url: `/approver/scholarship-applications/${id}/approval-history`,
    method: 'get'
  })
}

/**
 * 辅导员审核
 */
export function counselorApprove(id, data) {
  return request({
    url: `/approver/scholarship-applications/${id}/counselor-approve`,
    method: 'put',
    data
  })
}

/**
 * 院系审批
 */
export function departmentApprove(id, data) {
  return request({
    url: `/approver/scholarship-applications/${id}/department-approve`,
    method: 'put',
    data
  })
}

/**
 * 统一审批接口（教师端）
 */
export function approveApplication(id, data) {
  return request({
    url: `/approver/scholarship-applications/${id}/approve`,
    method: 'put',
    data
  })
}

/**
 * 查询审批历史（教师端）
 */
export function getApprovalHistory(id) {
  return request({
    url: `/approver/scholarship-applications/${id}/approval-history`,
    method: 'get'
  })
}

