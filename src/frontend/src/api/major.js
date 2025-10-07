import request from '@/utils/request'

/**
 * 专业API
 */

// 获取专业列表（分页）
export function getMajorList(params) {
  return request({
    url: '/admin/majors',
    method: 'get',
    params
  })
}

// 获取所有专业（不分页）
export function getAllMajors() {
  return request({
    url: '/admin/majors/all',
    method: 'get'
  })
}

// 根据院系ID获取专业列表
export function getMajorsByDepartment(departmentId) {
  return request({
    url: `/admin/majors/by-department/${departmentId}`,
    method: 'get'
  })
}

// 获取专业详情
export function getMajorById(id) {
  return request({
    url: `/admin/majors/${id}`,
    method: 'get'
  })
}

// 创建专业
export function createMajor(data) {
  return request({
    url: '/admin/majors',
    method: 'post',
    data
  })
}

// 更新专业
export function updateMajor(id, data) {
  return request({
    url: `/admin/majors/${id}`,
    method: 'put',
    data
  })
}

// 删除专业
export function deleteMajor(id) {
  return request({
    url: `/admin/majors/${id}`,
    method: 'delete'
  })
}

// 检查专业代码是否存在
export function checkMajorCode(code) {
  return request({
    url: '/admin/majors/check-code',
    method: 'get',
    params: { code }
  })
}

