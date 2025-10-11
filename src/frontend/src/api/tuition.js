import request from '@/utils/request'

/**
 * 学费缴纳相关API
 */

// ========== 学生端API ==========

// 查询学生的学费账单列表
export function getStudentBills() {
  return request({
    url: '/student/tuition/bills',
    method: 'get'
  })
}

// 查询账单详情
export function getBillDetail(billId) {
  return request({
    url: `/student/tuition/bills/${billId}`,
    method: 'get'
  })
}

// 在线缴费
export function createPayment(data) {
  return request({
    url: '/student/tuition/payments',
    method: 'post',
    data
  })
}

// 查询学生的缴费记录
export function getStudentPayments() {
  return request({
    url: '/student/tuition/payments',
    method: 'get'
  })
}

// 下载电子收据
export function downloadReceipt(paymentId) {
  return request({
    url: `/student/tuition/receipts/${paymentId}`,
    method: 'get',
    responseType: 'blob'
  })
}

// 提交退费申请
export function submitRefundApplication(data) {
  const formData = new FormData()
  formData.append('paymentId', data.paymentId)
  formData.append('refundAmount', data.refundAmount)
  formData.append('reason', data.reason)
  formData.append('refundType', data.refundType)
  formData.append('refundMethod', data.refundMethod)
  if (data.bankAccount) {
    formData.append('bankAccount', data.bankAccount)
  }
  if (data.attachment) {
    formData.append('attachment', data.attachment)
  }
  
  return request({
    url: '/student/tuition/refund-applications',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 查询学生的退费申请
export function getStudentRefundApplications() {
  return request({
    url: '/student/tuition/refund-applications',
    method: 'get'
  })
}

// 查询退费申请详情 - 学生端
export function getStudentRefundApplicationDetail(applicationId) {
  return request({
    url: `/student/tuition/refund-applications/${applicationId}`,
    method: 'get'
  })
}

// 取消退费申请
export function cancelRefundApplication(applicationId) {
  return request({
    url: `/student/tuition/refund-applications/${applicationId}`,
    method: 'delete'
  })
}

// ========== 管理员端API ==========

// 查询所有学费账单（分页）
export function getAllBills(params) {
  return request({
    url: '/admin/tuition/bills',
    method: 'get',
    params
  })
}

// 生成学费账单
export function generateBills(data) {
  return request({
    url: '/admin/tuition/bills/generate',
    method: 'post',
    data
  })
}

// 批量生成学费账单
export function batchGenerateBills(data) {
  return request({
    url: '/admin/tuition/bills/batch-generate',
    method: 'post',
    data
  })
}

// 录入线下缴费
export function recordOfflinePayment(billId, data) {
  return request({
    url: `/admin/tuition/payments/offline/${billId}`,
    method: 'post',
    data
  })
}

// 查询所有缴费记录（分页）
export function getAllPayments(params) {
  return request({
    url: '/admin/tuition/payments',
    method: 'get',
    params
  })
}

// 发送欠费提醒
export function sendPaymentReminder(billId) {
  return request({
    url: `/admin/tuition/reminders/${billId}`,
    method: 'post'
  })
}

// 批量发送欠费提醒
export function batchSendReminders(data) {
  return request({
    url: '/admin/tuition/reminders/batch',
    method: 'post',
    data
  })
}

// 获取缴费率统计
export function getPaymentRate(params) {
  return request({
    url: '/admin/tuition/statistics/payment-rate',
    method: 'get',
    params
  })
}

// 获取综合统计数据
export function getPaymentStatistics(params) {
  return request({
    url: '/admin/tuition/statistics',
    method: 'get',
    params
  })
}

// 生成财务报表
export function generateFinancialReport(params) {
  return request({
    url: '/admin/tuition/statistics/financial-report',
    method: 'get',
    params
  })
}

// 导出财务报表
export function exportFinancialReport(params) {
  return request({
    url: '/admin/tuition/statistics/export-report',
    method: 'get',
    params,
    responseType: 'blob'
  })
}

// 导出账单列表
export function exportBillList(params) {
  return request({
    url: '/admin/tuition/bills/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}

// 查询所有退费申请（分页）- 管理员
export function getRefundApplications(params) {
  return request({
    url: '/admin/tuition/refund-applications',
    method: 'get',
    params
  })
}

// 查询退费申请详情 - 管理员
export function getAdminRefundApplicationDetail(applicationId) {
  return request({
    url: `/admin/tuition/refund-applications/${applicationId}`,
    method: 'get'
  })
}

// 审批退费申请 - 管理员
export function approveRefundApplication(applicationId, data) {
  return request({
    url: `/admin/tuition/refund-applications/${applicationId}/approve`,
    method: 'post',
    data
  })
}

// 执行退费 - 管理员
export function executeRefund(applicationId) {
  return request({
    url: `/admin/tuition/refund-applications/${applicationId}/execute`,
    method: 'post'
  })
}

// ========== 学费标准管理 ==========

// 查询学费标准列表
export function getTuitionStandards(params) {
  return request({
    url: '/admin/tuition/standards',
    method: 'get',
    params
  })
}

// 设置学费标准
export function setTuitionStandard(data) {
  return request({
    url: '/admin/tuition/standards',
    method: 'post',
    data
  })
}

// 更新学费标准
export function updateTuitionStandard(id, data) {
  return request({
    url: `/admin/tuition/standards/${id}`,
    method: 'put',
    data
  })
}

// 查询学费标准详情
export function getTuitionStandardDetail(id) {
  return request({
    url: `/admin/tuition/standards/${id}`,
    method: 'get'
  })
}

