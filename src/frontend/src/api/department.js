import request from '@/utils/request'

/**
 * 院系API
 */

// 获取院系列表（分页）
export function getDepartmentList(params) {
  return request({
    url: '/admin/departments',
    method: 'get',
    params
  })
}

// 获取所有院系（不分页）
export function getAllDepartments() {
  return request({
    url: '/admin/departments/all',
    method: 'get'
  })
}

// 获取院系详情
export function getDepartmentById(id) {
  return request({
    url: `/admin/departments/${id}`,
    method: 'get'
  })
}

// 创建院系
export function createDepartment(data) {
  return request({
    url: '/admin/departments',
    method: 'post',
    data
  })
}

// 更新院系
export function updateDepartment(id, data) {
  return request({
    url: `/admin/departments/${id}`,
    method: 'put',
    data
  })
}

// 删除院系
export function deleteDepartment(id) {
  return request({
    url: `/admin/departments/${id}`,
    method: 'delete'
  })
}

// 检查院系代码是否存在
export function checkDepartmentCode(code) {
  return request({
    url: '/admin/departments/check-code',
    method: 'get',
    params: { code }
  })
}

