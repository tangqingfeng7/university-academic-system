import request from '@/utils/request'

/**
 * 操作日志API
 */

// 获取所有操作日志（分页）
export function getAllLogs(params) {
  return request({
    url: '/admin/logs',
    method: 'get',
    params
  })
}

// 条件搜索操作日志
export function searchLogs(params) {
  return request({
    url: '/admin/logs/search',
    method: 'get',
    params
  })
}

// 根据用户ID查询操作日志
export function getLogsByUserId(userId, params) {
  return request({
    url: `/admin/logs/user/${userId}`,
    method: 'get',
    params
  })
}

// 获取日志统计信息
export function getLogStatistics(params) {
  return request({
    url: '/admin/logs/statistics',
    method: 'get',
    params
  })
}

