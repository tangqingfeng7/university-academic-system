import request from '@/utils/request'

/**
 * 处分管理API
 */

// ==================== 管理员端API ====================

/**
 * 创建处分记录
 */
export function createDiscipline(data) {
  return request({
    url: '/admin/disciplines',
    method: 'post',
    data
  })
}

/**
 * 查询处分列表
 */
export function getDisciplines(params) {
  return request({
    url: '/admin/disciplines',
    method: 'get',
    params
  })
}

/**
 * 解除处分
 */
export function removeDiscipline(id, reason) {
  return request({
    url: `/admin/disciplines/${id}/remove`,
    method: 'post',
    params: { reason }
  })
}

/**
 * 删除处分
 */
export function deleteDiscipline(id) {
  return request({
    url: `/admin/disciplines/${id}`,
    method: 'delete'
  })
}

/**
 * 审批处分
 */
export function approveDiscipline(id, approved, comment) {
  return request({
    url: `/admin/disciplines/${id}/approve`,
    method: 'post',
    params: { approved, comment }
  })
}

// ==================== 学生端API ====================

/**
 * 查询我的处分
 */
export function getMyDisciplines() {
  return request({
    url: '/student/disciplines',
    method: 'get'
  })
}

/**
 * 提交申诉
 */
export function createAppeal(data) {
  return request({
    url: '/student/discipline-appeals',
    method: 'post',
    data
  })
}

/**
 * 查询我的申诉
 */
export function getMyAppeals() {
  return request({
    url: '/student/discipline-appeals',
    method: 'get'
  })
}

// ==================== 申诉审核API ====================

/**
 * 查询待审核的申诉
 */
export function getPendingAppeals(params) {
  return request({
    url: '/admin/discipline-appeals',
    method: 'get',
    params
  })
}

/**
 * 审核申诉
 */
export function reviewAppeal(id, data) {
  return request({
    url: `/admin/discipline-appeals/${id}/review`,
    method: 'post',
    data
  })
}

