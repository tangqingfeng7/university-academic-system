import request from '@/utils/request'

/**
 * 教师仪表盘相关API
 */

// 获取教师仪表盘统计数据
export function getTeacherDashboardStatistics() {
  return request({
    url: '/teacher/dashboard/statistics',
    method: 'get'
  })
}

