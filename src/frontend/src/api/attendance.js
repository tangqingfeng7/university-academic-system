import request from '@/utils/request'

// ========== 教师端API ==========

/**
 * 开始考勤
 */
export function startAttendance(data) {
  return request({
    url: '/teacher/attendance/start',
    method: 'post',
    data
  })
}

/**
 * 记录学生考勤
 */
export function recordAttendance(recordId, data) {
  return request({
    url: `/teacher/attendance/${recordId}/record`,
    method: 'post',
    data
  })
}

/**
 * 提交考勤
 */
export function submitAttendance(recordId) {
  return request({
    url: `/teacher/attendance/${recordId}/submit`,
    method: 'put'
  })
}

/**
 * 取消考勤
 */
export function cancelAttendance(recordId) {
  return request({
    url: `/teacher/attendance/${recordId}/cancel`,
    method: 'put'
  })
}

/**
 * 更新考勤明细
 */
export function updateAttendanceDetail(detailId, data) {
  return request({
    url: `/teacher/attendance/detail/${detailId}`,
    method: 'put',
    data
  })
}

/**
 * 获取考勤详情
 */
export function getAttendanceRecord(recordId) {
  return request({
    url: `/teacher/attendance/${recordId}`,
    method: 'get'
  })
}

/**
 * 获取考勤明细列表
 */
export function getAttendanceDetails(recordId) {
  return request({
    url: `/teacher/attendance/${recordId}/details`,
    method: 'get'
  })
}

/**
 * 获取教师考勤列表
 */
export function getTeacherAttendanceList() {
  return request({
    url: '/teacher/attendance/list',
    method: 'get'
  })
}

/**
 * 获取课程考勤记录
 */
export function getOfferingAttendance(offeringId) {
  return request({
    url: `/teacher/attendance/offering/${offeringId}`,
    method: 'get'
  })
}

/**
 * 获取课程统计
 */
export function getCourseStatistics(offeringId) {
  return request({
    url: `/teacher/attendance/statistics/course/${offeringId}`,
    method: 'get'
  })
}

/**
 * 获取出勤趋势
 */
export function getAttendanceTrend(offeringId) {
  return request({
    url: `/teacher/attendance/statistics/trend/${offeringId}`,
    method: 'get'
  })
}

/**
 * 生成签到二维码
 */
export function generateQRCode(recordId) {
  return request({
    url: '/teacher/attendance/qrcode/generate',
    method: 'post',
    params: { recordId }
  })
}

/**
 * 刷新二维码
 */
export function refreshQRCode(recordId) {
  return request({
    url: '/teacher/attendance/qrcode/refresh',
    method: 'post',
    params: { recordId }
  })
}

/**
 * 设置地理围栏
 */
export function setGeofence(data) {
  return request({
    url: '/teacher/attendance/location/set',
    method: 'post',
    data
  })
}

/**
 * 获取待审批申请列表
 */
export function getPendingRequests() {
  return request({
    url: '/teacher/attendance/requests',
    method: 'get'
  })
}

/**
 * 获取所有申请列表
 */
export function getAllRequests(status) {
  return request({
    url: '/teacher/attendance/requests/all',
    method: 'get',
    params: { status }
  })
}

/**
 * 批准申请
 */
export function approveRequest(requestId, data) {
  return request({
    url: `/teacher/attendance/requests/${requestId}/approve`,
    method: 'post',
    data
  })
}

/**
 * 拒绝申请
 */
export function rejectRequest(requestId, data) {
  return request({
    url: `/teacher/attendance/requests/${requestId}/reject`,
    method: 'post',
    data
  })
}

/**
 * 导出课程考勤
 */
export function exportAttendance(offeringId) {
  return request({
    url: '/teacher/attendance/export',
    method: 'post',
    params: { offeringId },
    responseType: 'blob'
  })
}

// ========== 学生端API ==========

/**
 * 扫码签到
 */
export function qrCodeCheckin(data) {
  return request({
    url: '/student/attendance/checkin/qrcode',
    method: 'post',
    data
  })
}

/**
 * 定位签到
 */
export function locationCheckin(data) {
  return request({
    url: '/student/attendance/checkin/location',
    method: 'post',
    data
  })
}

/**
 * 获取学生考勤记录
 */
export function getStudentAttendanceRecords(offeringId) {
  return request({
    url: '/student/attendance/records',
    method: 'get',
    params: { offeringId }
  })
}

/**
 * 获取学生考勤统计
 */
export function getStudentStatistics(offeringId) {
  return request({
    url: '/student/attendance/statistics',
    method: 'get',
    params: { offeringId }
  })
}

/**
 * 获取学期统计
 */
export function getStudentSemesterStatistics(semesterId) {
  return request({
    url: '/student/attendance/statistics/semester',
    method: 'get',
    params: { semesterId }
  })
}

/**
 * 提交补签申请
 */
export function submitMakeupRequest(data) {
  return request({
    url: '/student/attendance/makeup',
    method: 'post',
    data
  })
}

/**
 * 提交考勤申诉
 */
export function submitAppeal(data) {
  return request({
    url: '/student/attendance/appeal',
    method: 'post',
    data
  })
}

/**
 * 获取学生申请列表
 */
export function getStudentRequests(status) {
  return request({
    url: '/student/attendance/requests',
    method: 'get',
    params: { status }
  })
}

/**
 * 取消申请
 */
export function cancelRequest(requestId) {
  return request({
    url: `/student/attendance/requests/${requestId}`,
    method: 'delete'
  })
}

// ========== 管理员端API ==========

/**
 * 获取院系统计
 */
export function getDepartmentStatistics(params) {
  return request({
    url: '/admin/attendance/statistics/department',
    method: 'get',
    params
  })
}

/**
 * 获取教师统计
 */
export function getTeacherStatistics(params) {
  return request({
    url: '/admin/attendance/statistics/teacher',
    method: 'get',
    params
  })
}

/**
 * 获取预警列表
 */
export function getWarnings(status) {
  return request({
    url: '/admin/attendance/warnings',
    method: 'get',
    params: { status }
  })
}

/**
 * 处理预警
 */
export function handleWarning(warningId, comment) {
  return request({
    url: `/admin/attendance/warnings/${warningId}/handle`,
    method: 'post',
    params: { comment }
  })
}

/**
 * 忽略预警
 */
export function ignoreWarning(warningId) {
  return request({
    url: `/admin/attendance/warnings/${warningId}/ignore`,
    method: 'post'
  })
}

/**
 * 手动执行预警检测
 */
export function executeWarningCheck() {
  return request({
    url: '/admin/attendance/warnings/check',
    method: 'post'
  })
}

/**
 * 获取考勤配置
 */
export function getAttendanceConfigs() {
  return request({
    url: '/admin/attendance/config',
    method: 'get'
  })
}

/**
 * 更新配置
 */
export function updateAttendanceConfig(key, value) {
  return request({
    url: `/admin/attendance/config/${key}`,
    method: 'put',
    params: { value }
  })
}

/**
 * 批量更新配置
 */
export function updateAttendanceConfigs(data) {
  return request({
    url: '/admin/attendance/config',
    method: 'put',
    data
  })
}

/**
 * 导出院系统计
 */
export function exportDepartmentStatistics(params) {
  return request({
    url: '/admin/attendance/export/department',
    method: 'post',
    params,
    responseType: 'blob'
  })
}

/**
 * 导出教师统计
 */
export function exportTeacherStatistics(params) {
  return request({
    url: '/admin/attendance/export/teacher',
    method: 'post',
    params,
    responseType: 'blob'
  })
}

/**
 * 生成月度报告
 */
export function generateMonthlyReport(params) {
  return request({
    url: '/admin/attendance/report/monthly',
    method: 'post',
    params,
    responseType: 'blob'
  })
}

