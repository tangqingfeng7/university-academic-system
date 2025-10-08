import request from '@/utils/request'

/**
 * 仪表盘相关API
 */

// 获取仪表盘统计数据
export function getDashboardStatistics() {
  return request({
    url: '/admin/dashboard/statistics',
    method: 'get'
  })
}

// 获取数据趋势
export function getDataTrends() {
  return request({
    url: '/admin/dashboard/trends',
    method: 'get'
  })
}

