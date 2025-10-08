import request from '@/utils/request'

/**
 * 学生仪表盘相关API
 */

// 获取学生仪表盘统计数据
export function getStudentDashboardStatistics() {
  return request({
    url: '/student/dashboard/statistics',
    method: 'get'
  })
}

