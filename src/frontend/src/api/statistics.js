import request from '@/utils/request'

/**
 * 统计报表API
 */

// 获取学生统计数据
export function getStudentStatistics() {
  return request({
    url: '/admin/statistics/students',
    method: 'get'
  })
}

// 获取课程统计数据
export function getCourseStatistics(params) {
  return request({
    url: '/admin/statistics/courses',
    method: 'get',
    params
  })
}

// 获取成绩统计数据
export function getGradeStatistics(params) {
  return request({
    url: '/admin/statistics/grades',
    method: 'get',
    params
  })
}

// 获取教师工作量统计
export function getTeacherWorkload(params) {
  return request({
    url: '/admin/statistics/teachers',
    method: 'get',
    params
  })
}

// 导出学生统计报表
export function exportStudentStatistics() {
  return request({
    url: '/admin/statistics/students/export',
    method: 'get',
    responseType: 'blob'
  })
}

// 导出课程统计报表
export function exportCourseStatistics(params) {
  return request({
    url: '/admin/statistics/courses/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}

// 导出成绩统计报表
export function exportGradeStatistics(params) {
  return request({
    url: '/admin/statistics/grades/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}

// 导出教师工作量统计报表
export function exportTeacherWorkload(params) {
  return request({
    url: '/admin/statistics/teachers/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}
