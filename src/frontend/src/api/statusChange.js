import request from '@/utils/request'

/**
 * 学籍异动管理 API
 */

// ==================== 学生端 API ====================

/**
 * 提交异动申请
 * @param {FormData} data - 包含申请数据和附件的FormData
 */
export function createApplication(data) {
  return request({
    url: '/student/status-changes',
    method: 'post',
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    data
  })
}

/**
 * 查询我的异动申请
 * @param {Object} params - 查询参数
 */
export function getMyApplications(params) {
  return request({
    url: '/student/status-changes',
    method: 'get',
    params
  })
}

/**
 * 查询异动申请详情
 * @param {number} id - 申请ID
 */
export function getApplicationDetail(id) {
  return request({
    url: `/student/status-changes/${id}`,
    method: 'get'
  })
}

/**
 * 撤销异动申请
 * @param {number} id - 申请ID
 */
export function cancelApplication(id) {
  return request({
    url: `/student/status-changes/${id}`,
    method: 'delete'
  })
}

/**
 * 查询我的异动历史
 */
export function getMyHistory() {
  return request({
    url: '/student/status-changes/history',
    method: 'get'
  })
}

// ==================== 审批人端 API ====================

/**
 * 查询待审批申请
 * @param {Object} params - 查询参数
 */
export function getPendingApprovals(params) {
  return request({
    url: '/approver/status-changes',
    method: 'get',
    params
  })
}

/**
 * 获取申请详情（用于审批）
 * @param {number} id - 申请ID
 */
export function getApprovalDetail(id) {
  return request({
    url: `/approver/status-changes/${id}`,
    method: 'get'
  })
}

/**
 * 审批申请（批准、拒绝、退回）
 * @param {number} id - 申请ID
 * @param {Object} data - 审批数据 { action, comment }
 */
export function approveApplication(id, data) {
  return request({
    url: `/approver/status-changes/${id}/approve`,
    method: 'post',
    data
  })
}

/**
 * 查询我的审批统计
 */
export function getApprovalStatistics() {
  return request({
    url: '/approver/status-changes/statistics',
    method: 'get'
  })
}

// ==================== 管理员端 API ====================

/**
 * 查询所有异动申请（多条件查询）
 * @param {Object} params - 查询参数
 */
export function getAllApplications(params) {
  return request({
    url: '/admin/status-changes',
    method: 'get',
    params
  })
}

/**
 * 查询申请详情（管理员视图）
 * @param {number} id - 申请ID
 */
export function getAdminApplicationDetail(id) {
  return request({
    url: `/admin/status-changes/${id}`,
    method: 'get'
  })
}

/**
 * 查询超期未审批的申请
 */
export function getOverdueApplications() {
  return request({
    url: '/admin/status-changes/overdue',
    method: 'get'
  })
}

/**
 * 获取统计数据
 * @param {Object} params - 查询参数 { startDate, endDate }
 */
export function getStatistics(params) {
  return request({
    url: '/admin/status-changes/statistics',
    method: 'get',
    params
  })
}

/**
 * 按类型统计
 * @param {Object} params - 查询参数
 */
export function getStatisticsByType(params) {
  return request({
    url: '/admin/status-changes/statistics/by-type',
    method: 'get',
    params
  })
}

/**
 * 按状态统计
 * @param {Object} params - 查询参数
 */
export function getStatisticsByStatus(params) {
  return request({
    url: '/admin/status-changes/statistics/by-status',
    method: 'get',
    params
  })
}

/**
 * 按月份统计
 * @param {Object} params - 查询参数
 */
export function getStatisticsByMonth(params) {
  return request({
    url: '/admin/status-changes/statistics/by-month',
    method: 'get',
    params
  })
}

/**
 * 转专业统计（按目标专业）
 * @param {Object} params - 查询参数
 */
export function getTransferStatistics(params) {
  return request({
    url: '/admin/status-changes/statistics/transfers',
    method: 'get',
    params
  })
}

/**
 * 导出异动记录（Excel）
 * @param {Object} params - 查询参数
 */
export function exportRecords(params) {
  return request({
    url: '/admin/status-changes/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}

/**
 * 导出统计报告（Excel）
 * @param {Object} params - 查询参数
 */
export function exportReport(params) {
  return request({
    url: '/admin/status-changes/export/report',
    method: 'get',
    params,
    responseType: 'blob'
  })
}

