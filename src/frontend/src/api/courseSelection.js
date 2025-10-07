import request from '@/utils/request'

/**
 * 学生选课API
 */

// 获取可选课程列表
export function getAvailableOfferings() {
  return request({
    url: '/student/offerings',
    method: 'get'
  })
}

// 获取我的选课列表
export function getMySelections() {
  return request({
    url: '/student/selections',
    method: 'get'
  })
}

// 选课
export function selectCourse(offeringId) {
  return request({
    url: '/student/selections',
    method: 'post',
    data: { offeringId }
  })
}

// 退课
export function dropCourse(selectionId) {
  return request({
    url: `/student/selections/${selectionId}`,
    method: 'delete'
  })
}

// 获取选课统计信息
export function getSelectionStatistics() {
  return request({
    url: '/student/selections/statistics',
    method: 'get'
  })
}

