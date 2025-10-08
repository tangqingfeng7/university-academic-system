import request from '@/utils/request'

/**
 * 学生管理相关API
 */

// 获取学生列表（分页）
export function getStudentList(params) {
  return request({
    url: '/admin/students',
    method: 'get',
    params
  })
}

// 获取所有学生（不分页）
export function getAllStudents() {
  return request({
    url: '/admin/students/all',
    method: 'get'
  })
}

// 根据专业获取学生
export function getStudentsByMajor(majorId) {
  return request({
    url: `/admin/students/by-major/${majorId}`,
    method: 'get'
  })
}

// 获取所有班级列表（去重）
export function getAllClasses() {
  return request({
    url: '/admin/students/classes',
    method: 'get'
  })
}

// 获取学生详情
export function getStudentById(id) {
  return request({
    url: `/admin/students/${id}`,
    method: 'get'
  })
}

// 创建学生
export function createStudent(data) {
  return request({
    url: '/admin/students',
    method: 'post',
    data
  })
}

// 更新学生信息
export function updateStudent(id, data) {
  return request({
    url: `/admin/students/${id}`,
    method: 'put',
    data
  })
}

// 删除学生
export function deleteStudent(id) {
  return request({
    url: `/admin/students/${id}`,
    method: 'delete'
  })
}

// 检查学号是否存在
export function checkStudentNo(studentNo) {
  return request({
    url: '/admin/students/check-student-no',
    method: 'get',
    params: { studentNo }
  })
}

// 获取学生统计信息
export function getStudentStatistics() {
  return request({
    url: '/admin/students/statistics',
    method: 'get'
  })
}

// 导入学生（Excel）
export function importStudents(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/admin/students/import',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 导出学生（Excel）
export function exportStudents(params) {
  return request({
    url: '/admin/students/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}

