import request from '@/utils/request'

/**
 * 开课计划API
 */

// 获取开课计划列表（分页）
export function getOfferingList(params) {
  return request({
    url: '/admin/offerings',
    method: 'get',
    params
  })
}

// 获取开课计划详情
export function getOfferingById(id) {
  return request({
    url: `/admin/offerings/${id}`,
    method: 'get'
  })
}

// 创建开课计划
export function createOffering(data) {
  return request({
    url: '/admin/offerings',
    method: 'post',
    data
  })
}

// 更新开课计划
export function updateOffering(id, data) {
  return request({
    url: `/admin/offerings/${id}`,
    method: 'put',
    data
  })
}

// 删除开课计划
export function deleteOffering(id) {
  return request({
    url: `/admin/offerings/${id}`,
    method: 'delete'
  })
}

// 发布开课计划
export function publishOffering(id) {
  return request({
    url: `/admin/offerings/${id}/publish`,
    method: 'put'
  })
}

// 取消开课计划
export function cancelOffering(id) {
  return request({
    url: `/admin/offerings/${id}/cancel`,
    method: 'put'
  })
}

// 根据教师ID获取开课计划
export function getOfferingsByTeacher(teacherId) {
  return request({
    url: `/admin/offerings/by-teacher/${teacherId}`,
    method: 'get'
  })
}

// 获取开课计划统计信息
export function getOfferingStatistics() {
  return request({
    url: '/admin/offerings/statistics',
    method: 'get'
  })
}

