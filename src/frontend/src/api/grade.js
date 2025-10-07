import request from '@/utils/request'

/**
 * 获取学生所有成绩
 */
export function getMyGrades() {
  return request({
    url: '/student/grades',
    method: 'get'
  })
}

/**
 * 获取指定学期成绩
 */
export function getSemesterGrades(semesterId) {
  return request({
    url: `/student/grades/semester/${semesterId}`,
    method: 'get'
  })
}

/**
 * 获取学期成绩统计
 */
export function getSemesterStatistics(semesterId) {
  return request({
    url: `/student/grades/semester/${semesterId}/statistics`,
    method: 'get'
  })
}

/**
 * 获取总成绩单统计
 */
export function getTranscriptStatistics() {
  return request({
    url: '/student/grades/transcript/statistics',
    method: 'get'
  })
}

/**
 * 导出成绩单PDF
 */
export function exportTranscript() {
  return request({
    url: '/student/grades/transcript/export',
    method: 'get',
    responseType: 'blob'
  })
}
