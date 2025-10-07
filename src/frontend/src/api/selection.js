import request from '@/utils/request'

/**
 * 选课相关API
 */

// 获取可选课程列表
export function getAvailableOfferings(params) {
  return request({
    url: '/student/offerings',
    method: 'get',
    params
  })
}

// 获取已选课程
export function getSelectedCourses() {
  return request({
    url: '/student/selections',
    method: 'get'
  })
}

// 选课
export function selectCourse(data) {
  return request({
    url: '/student/selections',
    method: 'post',
    data
  })
}

// 退课
export function dropCourse(selectionId) {
  return request({
    url: `/student/selections/${selectionId}`,
    method: 'delete'
  })
}

// 获取课表
export function getSchedule() {
  return request({
    url: '/student/schedule',
    method: 'get'
  })
}

