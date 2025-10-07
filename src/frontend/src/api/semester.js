import request from '@/utils/request'

/**
 * 学期API
 */

// 获取所有学期
export function getAllSemesters() {
  return request({
    url: '/admin/semesters',
    method: 'get'
  })
}

// 获取当前活动学期
export function getActiveSemester() {
  return request({
    url: '/admin/semesters/active',
    method: 'get'
  })
}

// 获取学期详情
export function getSemesterById(id) {
  return request({
    url: `/admin/semesters/${id}`,
    method: 'get'
  })
}

// 创建学期
export function createSemester(data) {
  return request({
    url: '/admin/semesters',
    method: 'post',
    data
  })
}

// 更新学期
export function updateSemester(id, data) {
  return request({
    url: `/admin/semesters/${id}`,
    method: 'put',
    data
  })
}

// 删除学期
export function deleteSemester(id) {
  return request({
    url: `/admin/semesters/${id}`,
    method: 'delete'
  })
}

// 设置活动学期
export function activateSemester(id) {
  return request({
    url: `/admin/semesters/${id}/activate`,
    method: 'put'
  })
}

// 获取学期统计信息
export function getSemesterStatistics() {
  return request({
    url: '/admin/semesters/statistics',
    method: 'get'
  })
}

