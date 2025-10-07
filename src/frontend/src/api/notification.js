import request from '@/utils/request'

/**
 * 获取我的通知列表（分页）
 */
export function getMyNotifications(params) {
  return request({
    url: '/notifications',
    method: 'get',
    params
  })
}

/**
 * 获取通知详情
 */
export function getNotificationDetail(id) {
  return request({
    url: `/notifications/${id}`,
    method: 'get'
  })
}

/**
 * 标记通知为已读
 */
export function markAsRead(id) {
  return request({
    url: `/notifications/${id}/read`,
    method: 'put'
  })
}

/**
 * 获取未读通知数量
 */
export function getUnreadCount() {
  return request({
    url: '/notifications/unread/count',
    method: 'get'
  })
}

/**
 * 获取最新通知列表
 */
export function getLatestNotifications(limit = 5) {
  return request({
    url: '/notifications/latest',
    method: 'get',
    params: { limit }
  })
}

/**
 * 管理员发布系统通知
 */
export function publishAdminNotification(data) {
  return request({
    url: '/admin/notifications',
    method: 'post',
    data
  })
}

/**
 * 管理员查询所有通知
 */
export function getAllNotifications(params) {
  return request({
    url: '/admin/notifications',
    method: 'get',
    params
  })
}

/**
 * 管理员停用通知
 */
export function deactivateNotification(id) {
  return request({
    url: `/admin/notifications/${id}/deactivate`,
    method: 'put'
  })
}

/**
 * 教师发布课程通知
 */
export function publishTeacherNotification(data) {
  return request({
    url: '/teacher/notifications',
    method: 'post',
    data
  })
}
