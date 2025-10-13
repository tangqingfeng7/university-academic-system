import request from '@/utils/request'

/**
 * 班级管理API
 */

// 获取班级列表（分页）
export function getClassList(params) {
  return request({
    url: '/admin/classes',
    method: 'get',
    params
  })
}

// 获取所有班级（不分页）
export function getAllClasses() {
  return request({
    url: '/admin/classes/all',
    method: 'get'
  })
}

// 根据专业ID获取班级列表
export function getClassesByMajor(majorId) {
  return request({
    url: `/admin/classes/by-major/${majorId}`,
    method: 'get'
  })
}

// 根据入学年份获取班级列表
export function getClassesByYear(enrollmentYear) {
  return request({
    url: `/admin/classes/by-year/${enrollmentYear}`,
    method: 'get'
  })
}

// 根据专业和入学年份获取班级列表
export function getClassesByMajorAndYear(majorId, enrollmentYear) {
  return request({
    url: '/admin/classes/by-major-year',
    method: 'get',
    params: { majorId, enrollmentYear }
  })
}

// 获取班级详情
export function getClassById(id) {
  return request({
    url: `/admin/classes/${id}`,
    method: 'get'
  })
}

// 创建班级
export function createClass(data) {
  return request({
    url: '/admin/classes',
    method: 'post',
    data
  })
}

// 更新班级
export function updateClass(id, data) {
  return request({
    url: `/admin/classes/${id}`,
    method: 'put',
    data
  })
}

// 删除班级
export function deleteClass(id) {
  return request({
    url: `/admin/classes/${id}`,
    method: 'delete'
  })
}

// 检查班级代码是否存在
export function checkClassCode(classCode) {
  return request({
    url: '/admin/classes/check-code',
    method: 'get',
    params: { classCode }
  })
}

// 获取班级的学生列表
export function getClassStudents(classId) {
  return request({
    url: `/admin/classes/${classId}/students`,
    method: 'get'
  })
}

// 自动分配辅导员（全局）
export function autoAssignCounselors() {
  return request({
    url: '/admin/classes/auto-assign-counselors',
    method: 'post'
  })
}

// 自动分配辅导员（按专业或年级）
export function autoAssignCounselorsConditional(params) {
  return request({
    url: '/admin/classes/auto-assign-counselors/conditional',
    method: 'post',
    params
  })
}

// ==================== 公共接口（所有用户可访问）====================

// 获取所有班级列表（公共接口）
export function getAllClassesPublic() {
  return request({
    url: '/common/classes',
    method: 'get'
  })
}

// 根据专业ID获取班级列表（公共接口）
export function getClassesByMajorPublic(majorId) {
  return request({
    url: `/common/classes/by-major/${majorId}`,
    method: 'get'
  })
}

// ==================== 教师端接口 ====================

// 查询我管理的班级列表
export function getMyClasses() {
  return request({
    url: '/teacher/classes/my-classes',
    method: 'get'
  })
}

// 查询我管理的某个班级详情
export function getMyClassById(id) {
  return request({
    url: `/teacher/classes/my-classes/${id}`,
    method: 'get'
  })
}

// 查询我管理的某个班级的学生列表
export function getMyClassStudents(id) {
  return request({
    url: `/teacher/classes/my-classes/${id}/students`,
    method: 'get'
  })
}

// 获取我管理的班级统计信息
export function getMyClassStatistics() {
  return request({
    url: '/teacher/classes/statistics',
    method: 'get'
  })
}

