import request from '@/utils/request'

/**
 * 教室资源管理相关API（教师端）
 */

// 获取教室列表（分页）
export function getClassroomList(params) {
  return request({
    url: '/teacher/classrooms',
    method: 'get',
    params
  })
}

// 获取可用教室列表
export function getAvailableClassrooms(params) {
  return request({
    url: '/teacher/classrooms/available',
    method: 'get',
    params
  })
}

// 获取教室详情
export function getClassroomById(id) {
  return request({
    url: `/teacher/classrooms/${id}`,
    method: 'get'
  })
}

// 获取教室使用情况
export function getClassroomUsage(id, date) {
  return request({
    url: `/teacher/classrooms/${id}/usage`,
    method: 'get',
    params: { date }
  })
}

// 获取教室周课表
export function getClassroomSchedule(id, startDate) {
  return request({
    url: `/teacher/classrooms/${id}/schedule`,
    method: 'get',
    params: { startDate }
  })
}

/**
 * 教室借用管理相关API（教师端）
 */

// 提交教室借用申请
export function createBooking(data) {
  return request({
    url: '/teacher/classrooms/bookings',
    method: 'post',
    data
  })
}

// 获取我的借用申请列表（分页）
export function getMyBookings(params) {
  return request({
    url: '/teacher/classrooms/bookings',
    method: 'get',
    params
  })
}

// 获取借用申请详情
export function getBookingById(id) {
  return request({
    url: `/teacher/classrooms/bookings/${id}`,
    method: 'get'
  })
}

// 取消借用申请
export function cancelBooking(id) {
  return request({
    url: `/teacher/classrooms/bookings/${id}`,
    method: 'delete'
  })
}

// 检查教室时间冲突
export function checkConflict(params) {
  return request({
    url: '/teacher/classrooms/bookings/check-conflict',
    method: 'get',
    params
  })
}

