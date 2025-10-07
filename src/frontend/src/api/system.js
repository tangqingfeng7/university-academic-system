import request from '@/utils/request'

/**
 * 系统管理相关API
 */

// ========== 学期管理 ==========

// 获取学期列表
export function getSemesters(params) {
  return request({
    url: '/admin/semesters',
    method: 'get',
    params
  })
}

// 获取当前学期
export function getCurrentSemester() {
  return request({
    url: '/admin/semesters/current',
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

// 设置当前学期
export function setActiveSemester(id) {
  return request({
    url: `/admin/semesters/${id}/activate`,
    method: 'put'
  })
}

// ========== 院系管理 ==========

// 获取院系列表（分页）
export function getDepartments(params) {
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

// ========== 专业管理 ==========

// 获取专业列表（分页）
export function getMajors(params) {
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

// ========== 系统配置 ==========

// 获取所有配置
export function getConfigs() {
  return request({
    url: '/admin/configs',
    method: 'get'
  })
}

// 获取单个配置
export function getConfigByKey(key) {
  return request({
    url: `/admin/configs/${key}`,
    method: 'get'
  })
}

// 更新配置
export function updateConfig(data) {
  return request({
    url: '/admin/configs',
    method: 'put',
    data
  })
}

// 批量更新配置
export function batchUpdateConfigs(data) {
  return request({
    url: '/admin/configs/batch',
    method: 'put',
    data
  })
}

// 刷新配置缓存
export function refreshConfigCache() {
  return request({
    url: '/admin/configs/refresh',
    method: 'post'
  })
}

// 获取公共配置
export function getPublicConfig(key) {
  return request({
    url: `/admin/configs/public/${key}`,
    method: 'get'
  })
}

// ========== 操作日志 ==========

// 获取操作日志
export function getOperationLogs(params) {
  return request({
    url: '/admin/logs',
    method: 'get',
    params
  })
}

// 搜索操作日志
export function searchOperationLogs(params) {
  return request({
    url: '/admin/logs/search',
    method: 'get',
    params
  })
}

// 获取日志统计
export function getLogStatistics(params) {
  return request({
    url: '/admin/logs/statistics',
    method: 'get',
    params
  })
}

