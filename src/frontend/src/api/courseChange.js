import request from '@/utils/request'

/**
 * 教师创建调课申请
 */
export function createCourseChange(data) {
  return request({
    url: '/teacher/course-change-requests',
    method: 'post',
    data
  })
}

/**
 * 教师查询自己的调课申请列表
 */
export function getTeacherCourseChanges(params) {
  return request({
    url: '/teacher/course-change-requests',
    method: 'get',
    params
  })
}

/**
 * 管理员查询所有调课申请列表
 */
export function getAllCourseChanges(params) {
  return request({
    url: '/admin/course-change-requests',
    method: 'get',
    params
  })
}

/**
 * 获取调课申请详情
 */
export function getCourseChangeDetail(id) {
  return request({
    url: `/course-change-requests/${id}`,
    method: 'get'
  })
}

/**
 * 管理员审批调课申请
 */
export function approveCourseChange(id, data) {
  return request({
    url: `/admin/course-change-requests/${id}/approve`,
    method: 'put',
    data
  })
}

/**
 * 统计待审批数量
 */
export function countPendingCourseChanges() {
  return request({
    url: '/admin/course-change-requests/pending/count',
    method: 'get'
  })
}

