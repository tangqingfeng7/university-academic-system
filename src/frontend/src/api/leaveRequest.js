import request from '@/utils/request'

/**
 * 请假申请相关API
 */

// =============================================
// 学生端API
// =============================================

/**
 * 学生提交请假申请
 */
export function createStudentLeaveRequest(data) {
  return request({
    url: '/student/leave-requests',
    method: 'post',
    data
  })
}

/**
 * 学生查询我的请假申请列表
 */
export function getStudentLeaveRequests(params) {
  return request({
    url: '/student/leave-requests',
    method: 'get',
    params
  })
}

/**
 * 学生查询请假申请详情
 */
export function getStudentLeaveRequestById(id) {
  return request({
    url: `/student/leave-requests/${id}`,
    method: 'get'
  })
}

/**
 * 学生取消请假申请
 */
export function cancelStudentLeaveRequest(id) {
  return request({
    url: `/student/leave-requests/${id}`,
    method: 'delete'
  })
}

// =============================================
// 教师端API
// =============================================

/**
 * 教师提交请假申请
 */
export function createTeacherLeaveRequest(data) {
  return request({
    url: '/teacher/leave-requests',
    method: 'post',
    data
  })
}

/**
 * 教师查询我的请假申请列表
 */
export function getTeacherLeaveRequests(params) {
  return request({
    url: '/teacher/leave-requests',
    method: 'get',
    params
  })
}

/**
 * 教师查询请假申请详情
 */
export function getTeacherLeaveRequestById(id) {
  return request({
    url: `/teacher/leave-requests/${id}`,
    method: 'get'
  })
}

/**
 * 教师取消请假申请
 */
export function cancelTeacherLeaveRequest(id) {
  return request({
    url: `/teacher/leave-requests/${id}`,
    method: 'delete'
  })
}

// =============================================
// 管理端API
// =============================================

/**
 * 管理员查询所有请假申请列表
 */
export function getAllLeaveRequests(params) {
  return request({
    url: '/admin/leave-requests',
    method: 'get',
    params
  })
}

/**
 * 管理员查询请假申请详情
 */
export function getAdminLeaveRequestById(id) {
  return request({
    url: `/admin/leave-requests/${id}`,
    method: 'get'
  })
}

/**
 * 管理员审批请假申请
 */
export function approveLeaveRequest(id, data) {
  return request({
    url: `/admin/leave-requests/${id}/approve`,
    method: 'post',
    data
  })
}

/**
 * 管理员获取统计数据
 */
export function getLeaveRequestStatistics() {
  return request({
    url: '/admin/leave-requests/statistics',
    method: 'get'
  })
}

