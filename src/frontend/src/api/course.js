import request from '@/utils/request'

/**
 * 课程管理相关API
 */

// 获取课程列表（分页）
export function getCourseList(params) {
  return request({
    url: '/admin/courses',
    method: 'get',
    params
  })
}

// 获取所有课程
export function getAllCourses() {
  return request({
    url: '/admin/courses/all',
    method: 'get'
  })
}

// 获取课程详情
export function getCourseById(id) {
  return request({
    url: `/admin/courses/${id}`,
    method: 'get'
  })
}

// 创建课程
export function createCourse(data) {
  return request({
    url: '/admin/courses',
    method: 'post',
    data
  })
}

// 更新课程信息
export function updateCourse(id, data) {
  return request({
    url: `/admin/courses/${id}`,
    method: 'put',
    data
  })
}

// 删除课程
export function deleteCourse(id) {
  return request({
    url: `/admin/courses/${id}`,
    method: 'delete'
  })
}

// 获取先修课程
export function getPrerequisites(courseId) {
  return request({
    url: `/admin/courses/${courseId}/prerequisites`,
    method: 'get'
  })
}

// 设置先修课程
export function setPrerequisites(courseId, data) {
  return request({
    url: `/admin/courses/${courseId}/prerequisites`,
    method: 'post',
    data
  })
}

// 检查课程编号是否存在
export function checkCourseNo(courseNo) {
  return request({
    url: '/admin/courses/check-course-no',
    method: 'get',
    params: { courseNo }
  })
}

// 根据院系获取课程列表
export function getCoursesByDepartment(departmentId) {
  return request({
    url: `/admin/courses/by-department/${departmentId}`,
    method: 'get'
  })
}

