/**
 * 日期格式化工具函数
 */

/**
 * 格式化日期时间
 * @param {string|Date} date - 日期
 * @param {string} format - 格式，默认 'YYYY-MM-DD HH:mm:ss'
 * @returns {string} 格式化后的日期字符串
 */
export function formatDate(date, format = 'YYYY-MM-DD HH:mm:ss') {
  if (!date) return '-'
  
  const d = new Date(date)
  if (isNaN(d.getTime())) return '-'
  
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hour = String(d.getHours()).padStart(2, '0')
  const minute = String(d.getMinutes()).padStart(2, '0')
  const second = String(d.getSeconds()).padStart(2, '0')
  
  return format
    .replace('YYYY', year)
    .replace('MM', month)
    .replace('DD', day)
    .replace('HH', hour)
    .replace('mm', minute)
    .replace('ss', second)
}

/**
 * 格式化为日期（不包含时间）
 * @param {string|Date} date - 日期
 * @returns {string} 格式化后的日期字符串
 */
export function formatDateOnly(date) {
  return formatDate(date, 'YYYY-MM-DD')
}

/**
 * 格式化为时间（不包含日期）
 * @param {string|Date} date - 日期
 * @returns {string} 格式化后的时间字符串
 */
export function formatTimeOnly(date) {
  return formatDate(date, 'HH:mm:ss')
}

/**
 * 格式化为相对时间
 * @param {string|Date} date - 日期
 * @returns {string} 相对时间字符串
 */
export function formatRelativeTime(date) {
  if (!date) return '-'
  
  const d = new Date(date)
  if (isNaN(d.getTime())) return '-'
  
  const now = new Date()
  const diff = now - d
  const seconds = Math.floor(diff / 1000)
  const minutes = Math.floor(seconds / 60)
  const hours = Math.floor(minutes / 60)
  const days = Math.floor(hours / 24)
  
  if (seconds < 60) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  
  return formatDateOnly(date)
}

/**
 * 获取日期范围
 * @param {string} range - 范围类型: 'today', 'week', 'month', 'year'
 * @returns {Array} [开始日期, 结束日期]
 */
export function getDateRange(range) {
  const now = new Date()
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  
  switch (range) {
    case 'today':
      return [today, new Date(today.getTime() + 86400000 - 1)]
    
    case 'week': {
      const weekStart = new Date(today)
      weekStart.setDate(today.getDate() - today.getDay())
      const weekEnd = new Date(weekStart.getTime() + 7 * 86400000 - 1)
      return [weekStart, weekEnd]
    }
    
    case 'month': {
      const monthStart = new Date(today.getFullYear(), today.getMonth(), 1)
      const monthEnd = new Date(today.getFullYear(), today.getMonth() + 1, 0, 23, 59, 59)
      return [monthStart, monthEnd]
    }
    
    case 'year': {
      const yearStart = new Date(today.getFullYear(), 0, 1)
      const yearEnd = new Date(today.getFullYear(), 11, 31, 23, 59, 59)
      return [yearStart, yearEnd]
    }
    
    default:
      return [today, today]
  }
}

/**
 * 判断是否为今天
 * @param {string|Date} date - 日期
 * @returns {boolean}
 */
export function isToday(date) {
  if (!date) return false
  
  const d = new Date(date)
  const today = new Date()
  
  return d.getFullYear() === today.getFullYear() &&
         d.getMonth() === today.getMonth() &&
         d.getDate() === today.getDate()
}

/**
 * 判断日期是否过期
 * @param {string|Date} date - 日期
 * @returns {boolean}
 */
export function isExpired(date) {
  if (!date) return false
  return new Date(date) < new Date()
}

