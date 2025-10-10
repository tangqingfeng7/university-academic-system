import request from '@/utils/request'

/**
 * 教室资源管理相关API（管理员端）
 */

// 获取教室列表（分页）
export function getClassroomList(params) {
  return request({
    url: '/admin/classrooms',
    method: 'get',
    params
  })
}

// 获取教室详情
export function getClassroomById(id) {
  return request({
    url: `/admin/classrooms/${id}`,
    method: 'get'
  })
}

// 创建教室
export function createClassroom(data) {
  return request({
    url: '/admin/classrooms',
    method: 'post',
    data
  })
}

// 更新教室
export function updateClassroom(id, data) {
  return request({
    url: `/admin/classrooms/${id}`,
    method: 'put',
    data
  })
}

// 删除教室（软删除）
export function deleteClassroom(id) {
  return request({
    url: `/admin/classrooms/${id}`,
    method: 'delete'
  })
}

// 批量删除教室
export function batchDeleteClassrooms(ids) {
  return request({
    url: '/admin/classrooms/batch-delete',
    method: 'delete',
    data: ids
  })
}

// 更新教室状态
export function updateClassroomStatus(id, status) {
  return request({
    url: `/admin/classrooms/${id}/status`,
    method: 'put',
    params: { status }
  })
}

// 获取教室使用情况
export function getClassroomUsage(id, date) {
  return request({
    url: `/admin/classrooms/${id}/usage`,
    method: 'get',
    params: { date }
  })
}

// 获取教室周课表
export function getClassroomSchedule(id, startDate) {
  return request({
    url: `/admin/classrooms/${id}/schedule`,
    method: 'get',
    params: { startDate }
  })
}

/**
 * 教室借用管理相关API（管理员端）
 */

// 获取所有借用申请列表（分页）
export function getAllBookings(params) {
  return request({
    url: '/admin/classrooms/bookings',
    method: 'get',
    params
  })
}

// 获取借用申请详情
export function getBookingById(id) {
  return request({
    url: `/admin/classrooms/bookings/${id}`,
    method: 'get'
  })
}

// 审批借用申请
export function approveBooking(id, data) {
  return request({
    url: `/admin/classrooms/bookings/${id}/approve`,
    method: 'put',
    data
  })
}

// 批量审批
export function batchApproveBookings(data) {
  return request({
    url: '/admin/classrooms/bookings/batch-approve',
    method: 'put',
    data
  })
}

/**
 * 教室统计相关API
 */

// 获取教室使用率
export function getClassroomUtilization(id, params) {
  return request({
    url: `/admin/classrooms/${id}/utilization`,
    method: 'get',
    params
  })
}

// 获取使用率报告
export function getUtilizationReport(params) {
  return request({
    url: '/admin/classrooms/statistics/report',
    method: 'get',
    params
  })
}

// 获取使用率异常教室
export function getAbnormalClassrooms(params) {
  return request({
    url: '/admin/classrooms/statistics/abnormal',
    method: 'get',
    params
  })
}

// 获取使用率排名（最高和最低）
export function getUtilizationRanking(params) {
  return request({
    url: '/admin/classrooms/statistics/top',
    method: 'get',
    params
  })
}

// 导出使用率报告
export function exportUtilizationReport(params) {
  return request({
    url: '/admin/classrooms/statistics/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}

