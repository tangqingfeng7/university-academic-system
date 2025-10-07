import request from '@/utils/request'

// 获取学生课表
export function getStudentSchedule(semesterId) {
  return request({
    url: `/student/schedule/semester/${semesterId}`,
    method: 'get'
  })
}

// 获取学生指定周次的课表
export function getStudentWeeklySchedule(semesterId, weekNumber) {
  return request({
    url: `/student/schedule/semester/${semesterId}/week/${weekNumber}`,
    method: 'get'
  })
}

// 导出课表（文本格式）
export function exportSchedule(semesterId) {
  return request({
    url: `/student/schedule/semester/${semesterId}/export`,
    method: 'get',
    responseType: 'blob'
  })
}

// 获取教师课表
export function getTeacherSchedule(semesterId) {
  return request({
    url: `/teacher/schedule/semester/${semesterId}`,
    method: 'get'
  })
}

// 获取教师指定周次的课表
export function getTeacherWeeklySchedule(semesterId, weekNumber) {
  return request({
    url: `/teacher/schedule/semester/${semesterId}/week/${weekNumber}`,
    method: 'get'
  })
}

