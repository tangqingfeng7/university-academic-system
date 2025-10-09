import request from '@/utils/request'

/**
 * 考试管理相关API
 */

// ==================== 管理员端 - 考试CRUD ====================

// 获取考试列表（分页）
export function getExamList(params) {
  return request({
    url: '/admin/exams',
    method: 'get',
    params
  })
}

// 获取考试详情
export function getExamById(id) {
  return request({
    url: `/admin/exams/${id}`,
    method: 'get'
  })
}

// 创建考试
export function createExam(data) {
  return request({
    url: '/admin/exams',
    method: 'post',
    data
  })
}

// 更新考试
export function updateExam(id, data) {
  return request({
    url: `/admin/exams/${id}`,
    method: 'put',
    data
  })
}

// 删除考试
export function deleteExam(id) {
  return request({
    url: `/admin/exams/${id}`,
    method: 'delete'
  })
}

// ==================== 管理员端 - 考试操作 ====================

// 发布考试
export function publishExam(id) {
  return request({
    url: `/admin/exams/${id}/publish`,
    method: 'post'
  })
}

// 取消考试
export function cancelExam(id) {
  return request({
    url: `/admin/exams/${id}/cancel`,
    method: 'post'
  })
}

// 检测考试冲突
export function detectExamConflicts(id) {
  return request({
    url: `/admin/exams/${id}/conflicts`,
    method: 'get'
  })
}

// 别名：获取考试冲突
export const getExamConflicts = detectExamConflicts

// ==================== 管理员端 - 考场管理 ====================

// 创建考场
export function createExamRoom(examId, data) {
  return request({
    url: `/admin/exams/${examId}/rooms`,
    method: 'post',
    data
  })
}

// 自动创建考场
export function autoCreateExamRooms(examId, data) {
  return request({
    url: `/admin/exams/${examId}/rooms/auto`,
    method: 'post',
    data
  })
}

// 获取考场列表
export function getExamRooms(examId) {
  return request({
    url: `/admin/exams/${examId}/rooms`,
    method: 'get'
  })
}

// 更新考场
export function updateExamRoom(roomId, data) {
  return request({
    url: `/admin/exams/rooms/${roomId}`,
    method: 'put',
    data
  })
}

// 删除考场
export function deleteExamRoom(roomId) {
  return request({
    url: `/admin/exams/rooms/${roomId}`,
    method: 'delete'
  })
}

// 获取考场学生列表
export function getRoomStudents(roomId) {
  return request({
    url: `/admin/exams/rooms/${roomId}/students`,
    method: 'get'
  })
}

// 别名：获取考场学生列表
export const getExamRoomStudents = getRoomStudents

// ==================== 管理员端 - 学生分配 ====================

// 获取课程学生列表（用于考试分配）
export function getCourseStudents(examId) {
  return request({
    url: `/admin/exams/${examId}/available-students`,
    method: 'get'
  })
}

// 分配学生到考场
export function assignStudent(roomId, studentId, seatNumber) {
  return request({
    url: `/admin/exams/rooms/${roomId}/students`,
    method: 'post',
    params: {
      studentId,
      seatNumber
    }
  })
}

// 从考场移除学生
export function removeStudent(roomId, studentId) {
  return request({
    url: `/admin/exams/rooms/${roomId}/students/${studentId}`,
    method: 'delete'
  })
}

// 别名：分配学生到考场
export const assignStudentToRoom = assignStudent

// 别名：从考场移除学生
export const removeStudentFromRoom = removeStudent

// 自动分配学生
export function autoAssignStudents(examId) {
  return request({
    url: `/admin/exams/${examId}/students/auto`,
    method: 'post'
  })
}

// ==================== 管理员端 - 监考管理 ====================

// 添加监考教师
export function addInvigilator(roomId, data) {
  return request({
    url: `/admin/exams/rooms/${roomId}/invigilators`,
    method: 'post',
    data
  })
}

// 移除监考教师
export function removeInvigilator(invigilatorId) {
  return request({
    url: `/admin/exams/invigilators/${invigilatorId}`,
    method: 'delete'
  })
}

// 获取考场监考列表
export function getRoomInvigilators(roomId) {
  return request({
    url: `/admin/exams/rooms/${roomId}/invigilators`,
    method: 'get'
  })
}

// 别名：获取考场监考列表
export const getExamRoomInvigilators = getRoomInvigilators

// ==================== 管理员端 - 数据导出 ====================

// 导出考试列表（Excel）
export function exportExamList(params) {
  return request({
    url: '/admin/exams/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}

// 导出考场安排（Excel）
export function exportExamRoomArrangement(examId) {
  return request({
    url: `/admin/exams/${examId}/export/rooms`,
    method: 'get',
    responseType: 'blob'
  })
}

// 导出监考安排（Excel）
export function exportInvigilatorArrangement(examId) {
  return request({
    url: `/admin/exams/${examId}/export/invigilators`,
    method: 'get',
    responseType: 'blob'
  })
}

// ==================== 管理员端 - 统计功能 ====================

// 获取考试统计数据
export function getExamStatistics(semesterId) {
  return request({
    url: '/admin/exams/statistics',
    method: 'get',
    params: { semesterId }
  })
}

// ==================== 学生端 - 考试查询 ====================

// 获取我的考试列表
export function getMyExams(params) {
  return request({
    url: '/student/exams',
    method: 'get',
    params
  })
}

// 获取考试详情（学生端）
export function getMyExamById(id) {
  return request({
    url: `/student/exams/${id}`,
    method: 'get'
  })
}

// 导出个人考试安排（PDF）
export function exportMyExamSchedule() {
  return request({
    url: '/student/exams/export',
    method: 'get',
    responseType: 'blob'
  })
}

// ==================== 教师端 - 课程考试查询 ====================

// 获取我任教课程的考试列表
export function getMyCourseExams(params) {
  return request({
    url: '/teacher/exams/courses',
    method: 'get',
    params
  })
}

// 获取课程考试详情（教师端）
export function getCourseExamById(id) {
  return request({
    url: `/teacher/exams/courses/${id}`,
    method: 'get'
  })
}

// 获取考试的考场列表（教师端）
export function getCourseExamRooms(id) {
  return request({
    url: `/teacher/exams/courses/${id}/rooms`,
    method: 'get'
  })
}

// ==================== 教师端 - 监考任务查询 ====================

// 获取我的监考任务列表
export function getMyInvigilation(params) {
  return request({
    url: '/teacher/exams/invigilation',
    method: 'get',
    params
  })
}

// 获取监考任务详情
export function getInvigilationById(id) {
  return request({
    url: `/teacher/exams/invigilation/${id}`,
    method: 'get'
  })
}

// 导出监考任务安排（PDF）
export function exportMyInvigilationSchedule() {
  return request({
    url: '/teacher/exams/invigilation/export',
    method: 'get',
    responseType: 'blob'
  })
}

// ==================== 工具函数 ====================

/**
 * 下载文件辅助函数
 * @param {Blob} blob - 文件数据
 * @param {string} filename - 文件名
 */
export function downloadFile(blob, filename) {
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.setAttribute('download', filename)
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  window.URL.revokeObjectURL(url)
}

/**
 * 导出考试列表并下载
 * @param {Object} params - 查询参数
 */
export async function downloadExamList(params) {
  const blob = await exportExamList(params)
  const timestamp = new Date().toISOString().replace(/[:.]/g, '-').substring(0, 19)
  downloadFile(blob, `考试列表_${timestamp}.xlsx`)
}

/**
 * 导出考场安排并下载
 * @param {number} examId - 考试ID
 */
export async function downloadExamRoomArrangement(examId) {
  const blob = await exportExamRoomArrangement(examId)
  const timestamp = new Date().toISOString().replace(/[:.]/g, '-').substring(0, 19)
  downloadFile(blob, `考场安排_${timestamp}.xlsx`)
}

/**
 * 导出监考安排并下载
 * @param {number} examId - 考试ID
 */
export async function downloadInvigilatorArrangement(examId) {
  const blob = await exportInvigilatorArrangement(examId)
  const timestamp = new Date().toISOString().replace(/[:.]/g, '-').substring(0, 19)
  downloadFile(blob, `监考安排_${timestamp}.xlsx`)
}

/**
 * 导出个人考试安排并下载
 */
export async function downloadMyExamSchedule() {
  const blob = await exportMyExamSchedule()
  const timestamp = new Date().toISOString().replace(/[:.]/g, '-').substring(0, 19)
  downloadFile(blob, `个人考试安排_${timestamp}.pdf`)
}

/**
 * 导出监考任务安排并下载
 */
export async function downloadMyInvigilationSchedule() {
  const blob = await exportMyInvigilationSchedule()
  const timestamp = new Date().toISOString().replace(/[:.]/g, '-').substring(0, 19)
  downloadFile(blob, `监考任务安排_${timestamp}.pdf`)
}

