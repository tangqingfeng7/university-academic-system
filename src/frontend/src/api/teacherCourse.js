import request from '@/utils/request'

/**
 * 获取教师授课列表
 */
export function getMyCourses(semesterId) {
  return request({
    url: '/teacher/courses',
    method: 'get',
    params: { semesterId }
  })
}

/**
 * 获取开课班级学生名单
 */
export function getCourseStudents(offeringId) {
  return request({
    url: `/teacher/courses/${offeringId}/students`,
    method: 'get'
  })
}

/**
 * 获取开课班级详情
 */
export function getCourseDetail(offeringId) {
  return request({
    url: `/teacher/courses/${offeringId}`,
    method: 'get'
  })
}

