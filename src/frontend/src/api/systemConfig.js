import request from '@/utils/request'

/**
 * 系统配置API
 */

// 获取所有配置
export function getAllConfigs() {
  return request({
    url: '/admin/configs',
    method: 'get'
  })
}

// 获取常用配置
export function getCommonConfigs() {
  return request({
    url: '/admin/configs/common',
    method: 'get'
  })
}

// 根据键获取配置
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

// 创建配置
export function createConfig(data) {
  return request({
    url: '/admin/configs',
    method: 'post',
    data
  })
}

// 删除配置
export function deleteConfig(key) {
  return request({
    url: `/admin/configs/${key}`,
    method: 'delete'
  })
}

// 刷新配置缓存
export function refreshConfigCache() {
  return request({
    url: '/admin/configs/refresh',
    method: 'post'
  })
}

