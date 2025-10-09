import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import router from '@/router'

// 创建axios实例
const request = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json;charset=UTF-8'
  }
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    const userStore = useUserStore()
    
    // 添加token到请求头
    if (userStore.token) {
      config.headers.Authorization = `Bearer ${userStore.token}`
    }
    
    return config
  },
  error => {
    console.error('请求错误：', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    // 如果是文件下载（blob类型），直接返回数据
    if (response.config.responseType === 'blob') {
      return response.data
    }
    
    const res = response.data
    
    // 如果返回的状态码不是200，说明接口有问题
    if (res.code !== 200) {
      // 根据业务错误码显示友好的错误信息
      const errorMessage = getBusinessErrorMessage(res.code, res.message)
      ElMessage.error(errorMessage)
      
      // 401: 未授权，需要重新登录
      if (res.code === 401) {
        const userStore = useUserStore()
        userStore.logout()
        router.push('/login')
      }
      
      return Promise.reject(new Error(errorMessage))
    }
    
    return res
  },
  error => {
    console.error('响应错误：', error)
    
    let message = '网络错误，请稍后重试'
    
    if (error.response) {
      const status = error.response.status
      const data = error.response.data
      
      switch (status) {
        case 400:
          // 业务异常，显示后端返回的具体错误信息
          message = data?.message || '请求参数错误'
          break
        case 401:
          message = '未授权，请重新登录'
          const userStore = useUserStore()
          userStore.logout()
          router.push('/login')
          break
        case 403:
          message = '权限不足，拒绝访问'
          break
        case 404:
          message = '请求的资源不存在'
          break
        case 409:
          // 冲突错误（如时间冲突）
          message = data?.message || '操作冲突，请检查后重试'
          break
        case 500:
          message = '服务器内部错误，请联系管理员'
          break
        case 502:
          message = '网关错误，请稍后重试'
          break
        case 503:
          message = '服务暂时不可用，请稍后重试'
          break
        case 504:
          message = '网关超时，请稍后重试'
          break
        default:
          message = data?.message || `请求失败 (${status})`
      }
    } else if (error.code === 'ECONNABORTED') {
      message = '请求超时，请稍后重试'
    } else if (error.code === 'ERR_NETWORK') {
      message = '网络连接失败，请检查网络设置'
    } else if (!window.navigator.onLine) {
      message = '网络已断开，请检查网络连接'
    }
    
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

/**
 * 根据业务错误码返回友好的错误信息
 * @param {number} code - 错误码
 * @param {string} defaultMessage - 默认错误信息
 * @returns {string} 友好的错误信息
 */
function getBusinessErrorMessage(code, defaultMessage) {
  const errorMessages = {
    // 考试相关错误
    404: '资源不存在',
    400: defaultMessage || '请求参数错误',
    
    // 时间冲突相关
    'EXAM_TIME_CONFLICT': '考试时间冲突，请选择其他时间',
    'STUDENT_EXAM_CONFLICT': '学生在该时间已有其他考试',
    'TEACHER_INVIGILATION_CONFLICT': '教师在该时间已有监考任务',
    'EXAM_ROOM_TIME_CONFLICT': '考场在该时间已被占用',
    
    // 状态相关错误
    'EXAM_ALREADY_PUBLISHED': '考试已发布，无法修改',
    'EXAM_ALREADY_STARTED': '考试已开始，无法取消',
    'EXAM_NO_ROOMS': '考试没有考场，无法发布',
    
    // 考场相关错误
    'EXAM_ROOM_FULL': '考场已满，无法继续分配',
    'EXAM_ROOM_HAS_STUDENTS': '考场已有学生，无法删除',
    
    // 学生分配相关
    'STUDENT_NOT_ENROLLED': '学生未选修该课程',
    'STUDENT_ALREADY_ASSIGNED': '学生已分配到考场',
  }
  
  return errorMessages[code] || errorMessages[defaultMessage] || defaultMessage || '操作失败'
}

export default request

