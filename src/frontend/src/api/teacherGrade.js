import request from '@/utils/request'

/**
 * 查询开课班级的学生成绩列表
 */
export function getOfferingGrades(offeringId) {
  return request({
    url: `/teacher/grades/offering/${offeringId}`,
    method: 'get'
  })
}

/**
 * 录入单个学生成绩
 */
export function inputGrade(courseSelectionId, data) {
  return request({
    url: `/teacher/grades/${courseSelectionId}`,
    method: 'put',
    data
  })
}

/**
 * 批量录入成绩
 */
export function batchInputGrades(gradeList) {
  return request({
    url: '/teacher/grades/batch',
    method: 'post',
    data: gradeList
  })
}

/**
 * 提交成绩
 */
export function submitGrades(offeringId) {
  return request({
    url: `/teacher/grades/offering/${offeringId}/submit`,
    method: 'put'
  })
}

/**
 * 发布成绩
 */
export function publishGrades(offeringId) {
  return request({
    url: `/teacher/grades/offering/${offeringId}/publish`,
    method: 'put'
  })
}

/**
 * 批量导入成绩（Excel）
 */
export function importGrades(offeringId, file) {
  const formData = new FormData()
  formData.append('file', file)
  
  return request({
    url: `/teacher/grades/offering/${offeringId}/import`,
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

