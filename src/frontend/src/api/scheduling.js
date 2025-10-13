/**
 * 排课优化模块API
 */
import request from '@/utils/request'

// ==================== 教师端API ====================

/**
 * 设置排课偏好
 */
export function setTeacherPreference(data) {
  return request({
    url: '/teacher/preferences',
    method: 'post',
    data
  })
}

/**
 * 更新排课偏好
 */
export function updateTeacherPreference(data) {
  return request({
    url: '/teacher/preferences',
    method: 'put',
    data
  })
}

/**
 * 查询我的排课偏好
 */
export function getMyPreference() {
  return request({
    url: '/teacher/preferences',
    method: 'get'
  })
}

/**
 * 删除排课偏好
 */
export function deleteMyPreference() {
  return request({
    url: '/teacher/preferences',
    method: 'delete'
  })
}

// ==================== 管理员端API - 排课约束 ====================

/**
 * 添加排课约束
 */
export function createConstraint(data) {
  return request({
    url: '/admin/scheduling/constraints',
    method: 'post',
    data
  })
}

/**
 * 更新排课约束
 */
export function updateConstraint(id, data) {
  return request({
    url: `/admin/scheduling/constraints/${id}`,
    method: 'put',
    data
  })
}

/**
 * 查询排课约束列表
 */
export function getConstraints() {
  return request({
    url: '/admin/scheduling/constraints',
    method: 'get'
  })
}

/**
 * 查询活跃的约束列表
 */
export function getActiveConstraints() {
  return request({
    url: '/admin/scheduling/constraints/active',
    method: 'get'
  })
}

/**
 * 查询约束详情
 */
export function getConstraint(id) {
  return request({
    url: `/admin/scheduling/constraints/${id}`,
    method: 'get'
  })
}

/**
 * 删除排课约束
 */
export function deleteConstraint(id) {
  return request({
    url: `/admin/scheduling/constraints/${id}`,
    method: 'delete'
  })
}

// ==================== 管理员端API - 排课方案 ====================

/**
 * 创建排课方案
 */
export function createSolution(data) {
  return request({
    url: '/admin/scheduling/solutions',
    method: 'post',
    data
  })
}

/**
 * 执行智能排课
 */
export function executeScheduling(solutionId, data) {
  return request({
    url: `/admin/scheduling/solutions/${solutionId}/optimize`,
    method: 'post',
    data
  })
}

/**
 * 手动调整排课
 */
export function adjustSchedule(solutionId, data) {
  return request({
    url: `/admin/scheduling/solutions/${solutionId}/adjust`,
    method: 'put',
    data
  })
}

/**
 * 应用排课方案
 */
export function applySolution(solutionId, data = {}) {
  return request({
    url: `/admin/scheduling/solutions/${solutionId}/apply`,
    method: 'post',
    data
  })
}

/**
 * 查询排课方案详情
 */
export function getSolution(solutionId) {
  return request({
    url: `/admin/scheduling/solutions/${solutionId}`,
    method: 'get'
  })
}

/**
 * 查询排课方案结果
 */
export function getSolutionResult(solutionId) {
  return request({
    url: `/admin/scheduling/solutions/${solutionId}/result`,
    method: 'get'
  })
}

/**
 * 检测方案冲突
 */
export function detectConflicts(solutionId) {
  return request({
    url: `/admin/scheduling/solutions/${solutionId}/conflicts`,
    method: 'get'
  })
}

/**
 * 查询学期的所有排课方案
 */
export function getSolutionsBySemester(params) {
  return request({
    url: '/admin/scheduling/solutions',
    method: 'get',
    params
  })
}

/**
 * 删除排课方案
 */
export function deleteSolution(solutionId) {
  return request({
    url: `/admin/scheduling/solutions/${solutionId}`,
    method: 'delete'
  })
}

/**
 * 比较两个排课方案
 */
export function compareSolutions(solutionId1, solutionId2) {
  return request({
    url: '/admin/scheduling/solutions/compare',
    method: 'get',
    params: { solutionId1, solutionId2 }
  })
}

/**
 * 评估排课方案质量
 */
export function evaluateSolution(solutionId) {
  return request({
    url: `/admin/scheduling/solutions/${solutionId}/evaluate`,
    method: 'get'
  })
}

/**
 * 导出课表
 */
export function exportSchedule(solutionId, format = 'excel') {
  return request({
    url: `/admin/scheduling/solutions/${solutionId}/export`,
    method: 'get',
    params: { format },
    responseType: 'blob'
  })
}

// ==================== 管理员端API - 教师偏好 ====================

/**
 * 查询教师的排课偏好
 */
export function getTeacherPreference(teacherId) {
  return request({
    url: `/admin/scheduling/preferences/${teacherId}`,
    method: 'get'
  })
}

/**
 * 查询所有教师的排课偏好
 */
export function getAllPreferences() {
  return request({
    url: '/admin/scheduling/preferences',
    method: 'get'
  })
}

/**
 * 管理员代教师设置排课偏好
 */
export function setTeacherPreferenceByAdmin(teacherId, data) {
  return request({
    url: `/admin/scheduling/preferences/${teacherId}`,
    method: 'post',
    data
  })
}

