import request from '@/utils/request'

/**
 * 教师管理相关API
 */

// 获取教师列表（分页）
export function getTeacherList(params) {
  return request({
    url: '/admin/teachers',
    method: 'get',
    params
  })
}

// 获取所有教师
export function getAllTeachers() {
  return request({
    url: '/admin/teachers/all',
    method: 'get'
  })
}

// 获取教师详情
export function getTeacherById(id) {
  return request({
    url: `/admin/teachers/${id}`,
    method: 'get'
  })
}

// 创建教师
export function createTeacher(data) {
  return request({
    url: '/admin/teachers',
    method: 'post',
    data
  })
}

// 更新教师信息
export function updateTeacher(id, data) {
  return request({
    url: `/admin/teachers/${id}`,
    method: 'put',
    data
  })
}

// 删除教师
export function deleteTeacher(id) {
  return request({
    url: `/admin/teachers/${id}`,
    method: 'delete'
  })
}

// 检查工号是否存在
export function checkTeacherNo(teacherNo) {
  return request({
    url: '/admin/teachers/check-teacher-no',
    method: 'get',
    params: { teacherNo }
  })
}

// 检查教师是否有授课任务
export function checkHasOfferings(id) {
  return request({
    url: `/admin/teachers/${id}/has-offerings`,
    method: 'get'
  })
}

// 根据院系获取教师列表
export function getTeachersByDepartment(departmentId) {
  return request({
    url: `/admin/teachers/by-department/${departmentId}`,
    method: 'get'
  })
}

