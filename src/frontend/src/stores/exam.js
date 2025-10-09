import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

/**
 * 考试模块状态管理
 */
export const useExamStore = defineStore('exam', () => {
  // ==================== 状态 ====================
  
  // 选中的学期
  const selectedSemesterId = ref(null)
  
  // 考试列表缓存（管理端）
  const examListCache = ref({
    data: [],
    total: 0,
    page: 0,
    size: 20,
    filters: {
      semesterId: null,
      courseId: null,
      type: null,
      status: null,
      keyword: null
    },
    timestamp: null
  })
  
  // 学生考试列表缓存
  const studentExamCache = ref({
    data: [],
    semesterId: null,
    timestamp: null
  })
  
  // 教师课程考试列表缓存
  const teacherExamCache = ref({
    data: [],
    semesterId: null,
    timestamp: null
  })
  
  // 教师监考任务列表缓存
  const invigilationCache = ref({
    data: [],
    semesterId: null,
    timestamp: null
  })
  
  // 考试详情缓存
  const examDetailCache = ref(new Map())
  
  // 考场列表缓存（按考试ID）
  const examRoomsCache = ref(new Map())
  
  // 缓存过期时间（5分钟）
  const CACHE_EXPIRE_TIME = 5 * 60 * 1000
  
  // ==================== 计算属性 ====================
  
  // 检查管理端考试列表缓存是否有效
  const isExamListCacheValid = computed(() => {
    if (!examListCache.value.timestamp) return false
    return Date.now() - examListCache.value.timestamp < CACHE_EXPIRE_TIME
  })
  
  // 检查学生考试缓存是否有效
  const isStudentExamCacheValid = computed(() => {
    if (!studentExamCache.value.timestamp) return false
    return Date.now() - studentExamCache.value.timestamp < CACHE_EXPIRE_TIME
  })
  
  // 检查教师考试缓存是否有效
  const isTeacherExamCacheValid = computed(() => {
    if (!teacherExamCache.value.timestamp) return false
    return Date.now() - teacherExamCache.value.timestamp < CACHE_EXPIRE_TIME
  })
  
  // 检查监考任务缓存是否有效
  const isInvigilationCacheValid = computed(() => {
    if (!invigilationCache.value.timestamp) return false
    return Date.now() - invigilationCache.value.timestamp < CACHE_EXPIRE_TIME
  })
  
  // ==================== 方法 ====================
  
  /**
   * 设置选中的学期
   */
  const setSelectedSemester = (semesterId) => {
    selectedSemesterId.value = semesterId
  }
  
  /**
   * 更新管理端考试列表缓存
   */
  const updateExamListCache = (data, total, page, size, filters = {}) => {
    examListCache.value = {
      data,
      total,
      page,
      size,
      filters: { ...filters },
      timestamp: Date.now()
    }
  }
  
  /**
   * 更新学生考试列表缓存
   */
  const updateStudentExamCache = (data, semesterId = null) => {
    studentExamCache.value = {
      data,
      semesterId,
      timestamp: Date.now()
    }
  }
  
  /**
   * 更新教师考试列表缓存
   */
  const updateTeacherExamCache = (data, semesterId = null) => {
    teacherExamCache.value = {
      data,
      semesterId,
      timestamp: Date.now()
    }
  }
  
  /**
   * 更新监考任务列表缓存
   */
  const updateInvigilationCache = (data, semesterId = null) => {
    invigilationCache.value = {
      data,
      semesterId,
      timestamp: Date.now()
    }
  }
  
  /**
   * 更新考试详情缓存
   */
  const updateExamDetailCache = (examId, detail) => {
    examDetailCache.value.set(examId, {
      data: detail,
      timestamp: Date.now()
    })
  }
  
  /**
   * 获取考试详情缓存
   */
  const getExamDetailCache = (examId) => {
    const cache = examDetailCache.value.get(examId)
    if (!cache) return null
    
    // 检查缓存是否过期
    if (Date.now() - cache.timestamp > CACHE_EXPIRE_TIME) {
      examDetailCache.value.delete(examId)
      return null
    }
    
    return cache.data
  }
  
  /**
   * 更新考场列表缓存
   */
  const updateExamRoomsCache = (examId, rooms) => {
    examRoomsCache.value.set(examId, {
      data: rooms,
      timestamp: Date.now()
    })
  }
  
  /**
   * 获取考场列表缓存
   */
  const getExamRoomsCache = (examId) => {
    const cache = examRoomsCache.value.get(examId)
    if (!cache) return null
    
    // 检查缓存是否过期
    if (Date.now() - cache.timestamp > CACHE_EXPIRE_TIME) {
      examRoomsCache.value.delete(examId)
      return null
    }
    
    return cache.data
  }
  
  /**
   * 清除所有缓存
   */
  const clearAllCache = () => {
    examListCache.value = {
      data: [],
      total: 0,
      page: 0,
      size: 20,
      filters: {},
      timestamp: null
    }
    studentExamCache.value = {
      data: [],
      semesterId: null,
      timestamp: null
    }
    teacherExamCache.value = {
      data: [],
      semesterId: null,
      timestamp: null
    }
    invigilationCache.value = {
      data: [],
      semesterId: null,
      timestamp: null
    }
    examDetailCache.value.clear()
    examRoomsCache.value.clear()
  }
  
  /**
   * 清除管理端考试列表缓存
   */
  const clearExamListCache = () => {
    examListCache.value = {
      data: [],
      total: 0,
      page: 0,
      size: 20,
      filters: {},
      timestamp: null
    }
  }
  
  /**
   * 清除学生考试缓存
   */
  const clearStudentExamCache = () => {
    studentExamCache.value = {
      data: [],
      semesterId: null,
      timestamp: null
    }
  }
  
  /**
   * 清除教师考试缓存
   */
  const clearTeacherExamCache = () => {
    teacherExamCache.value = {
      data: [],
      semesterId: null,
      timestamp: null
    }
  }
  
  /**
   * 清除监考任务缓存
   */
  const clearInvigilationCache = () => {
    invigilationCache.value = {
      data: [],
      semesterId: null,
      timestamp: null
    }
  }
  
  /**
   * 清除指定考试的详情缓存
   */
  const clearExamDetailCache = (examId) => {
    examDetailCache.value.delete(examId)
  }
  
  /**
   * 清除指定考试的考场缓存
   */
  const clearExamRoomsCache = (examId) => {
    examRoomsCache.value.delete(examId)
  }
  
  /**
   * 在考试列表中更新单个考试
   */
  const updateExamInList = (examId, updatedExam) => {
    // 更新管理端列表
    const index = examListCache.value.data.findIndex(e => e.id === examId)
    if (index !== -1) {
      examListCache.value.data[index] = { ...examListCache.value.data[index], ...updatedExam }
    }
    
    // 清除详情缓存
    clearExamDetailCache(examId)
  }
  
  /**
   * 从列表中删除考试
   */
  const removeExamFromList = (examId) => {
    // 从管理端列表删除
    examListCache.value.data = examListCache.value.data.filter(e => e.id !== examId)
    examListCache.value.total = Math.max(0, examListCache.value.total - 1)
    
    // 清除相关缓存
    clearExamDetailCache(examId)
    clearExamRoomsCache(examId)
  }
  
  return {
    // 状态
    selectedSemesterId,
    examListCache,
    studentExamCache,
    teacherExamCache,
    invigilationCache,
    examDetailCache,
    examRoomsCache,
    
    // 计算属性
    isExamListCacheValid,
    isStudentExamCacheValid,
    isTeacherExamCacheValid,
    isInvigilationCacheValid,
    
    // 方法
    setSelectedSemester,
    updateExamListCache,
    updateStudentExamCache,
    updateTeacherExamCache,
    updateInvigilationCache,
    updateExamDetailCache,
    getExamDetailCache,
    updateExamRoomsCache,
    getExamRoomsCache,
    clearAllCache,
    clearExamListCache,
    clearStudentExamCache,
    clearTeacherExamCache,
    clearInvigilationCache,
    clearExamDetailCache,
    clearExamRoomsCache,
    updateExamInList,
    removeExamFromList
  }
})

