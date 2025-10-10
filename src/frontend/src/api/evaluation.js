import request from '@/utils/request'

/**
 * 学生评价API
 */

/**
 * 获取可评价的课程列表
 */
export function getAvailableCoursesForEvaluation(semesterId) {
  return request({
    url: '/student/evaluations/available',
    method: 'get',
    params: { semesterId }
  })
}

/**
 * 提交课程评价
 */
export function submitCourseEvaluation(data) {
  return request({
    url: '/student/evaluations/course',
    method: 'post',
    data
  })
}

/**
 * 修改课程评价
 */
export function updateCourseEvaluation(evaluationId, data) {
  return request({
    url: `/student/evaluations/course/${evaluationId}`,
    method: 'put',
    data
  })
}

/**
 * 获取我的课程评价列表
 */
export function getMyCourseEvaluations(params) {
  return request({
    url: '/student/evaluations/course',
    method: 'get',
    params
  })
}

/**
 * 提交教师评价
 */
export function submitTeacherEvaluation(data) {
  return request({
    url: '/student/evaluations/teacher',
    method: 'post',
    data
  })
}

/**
 * 修改教师评价
 */
export function updateTeacherEvaluation(evaluationId, data) {
  return request({
    url: `/student/evaluations/teacher/${evaluationId}`,
    method: 'put',
    data
  })
}

/**
 * 获取我的教师评价列表
 */
export function getMyTeacherEvaluations(params) {
  return request({
    url: '/student/evaluations/teacher',
    method: 'get',
    params
  })
}

/**
 * 教师评价API
 */

/**
 * 获取教师评价统计
 */
export function getTeacherStatistics(params) {
  return request({
    url: '/teacher/evaluations/statistics',
    method: 'get',
    params
  })
}

/**
 * 获取教师的评价列表
 */
export function getMyTeacherEvaluationList(params) {
  return request({
    url: '/teacher/evaluations',
    method: 'get',
    params
  })
}

/**
 * 获取课程的评价列表
 */
export function getCourseEvaluationsByOffering(offeringId) {
  return request({
    url: `/teacher/evaluations/course/${offeringId}`,
    method: 'get'
  })
}

/**
 * 管理员API - 评价周期管理
 */

/**
 * 获取当前活跃评价周期
 */
export function getActivePeriod() {
  return request({
    url: '/admin/evaluations/periods/active',
    method: 'get'
  })
}

/**
 * 获取所有评价周期
 */
export function getAllPeriods() {
  return request({
    url: '/admin/evaluations/periods',
    method: 'get'
  })
}

/**
 * 创建评价周期
 */
export function createPeriod(data) {
  return request({
    url: '/admin/evaluations/periods',
    method: 'post',
    data
  })
}

/**
 * 更新评价周期
 */
export function updatePeriod(id, data) {
  return request({
    url: `/admin/evaluations/periods/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除评价周期
 */
export function deletePeriod(id) {
  return request({
    url: `/admin/evaluations/periods/${id}`,
    method: 'delete'
  })
}

/**
 * 切换评价周期状态
 */
export function togglePeriod(id, active) {
  return request({
    url: `/admin/evaluations/periods/${id}/toggle`,
    method: 'put',
    params: { active }
  })
}

/**
 * 管理员API - 评价数据查询
 */

/**
 * 查询课程评价列表
 */
export function getCourseEvaluations(params) {
  return request({
    url: '/admin/evaluations/course',
    method: 'get',
    params
  })
}

/**
 * 查询教师评价列表
 */
export function getTeacherEvaluations(params) {
  return request({
    url: '/admin/evaluations/teacher',
    method: 'get',
    params
  })
}

/**
 * 获取课程评价统计
 */
export function getCourseEvaluationStatistics(offeringId) {
  return request({
    url: `/admin/evaluations/course/${offeringId}/statistics`,
    method: 'get'
  })
}

/**
 * 获取教师评价统计（管理员）
 */
export function getTeacherEvaluationStatistics(teacherId, params) {
  return request({
    url: `/admin/evaluations/teacher/${teacherId}/statistics`,
    method: 'get',
    params
  })
}

/**
 * 生成教学质量报告
 */
export function generateQualityReport(semesterId) {
  return request({
    url: `/admin/evaluations/report/${semesterId}`,
    method: 'get'
  })
}

/**
 * 导出评价数据
 */
export function exportEvaluations(params) {
  return request({
    url: '/admin/evaluations/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}

