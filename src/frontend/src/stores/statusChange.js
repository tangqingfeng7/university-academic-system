import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import {
  getMyApplications,
  getPendingApprovals,
  getAllApplications,
  getStatistics
} from '@/api/statusChange'

export const useStatusChangeStore = defineStore('statusChange', () => {
  // 状态
  const myApplications = ref([]) // 我的申请列表
  const pendingApprovals = ref([]) // 待审批列表
  const allApplications = ref([]) // 所有申请列表（管理员）
  const statistics = ref({}) // 统计数据
  const pendingCount = ref(0) // 待审批数量
  const loading = ref(false) // 加载状态

  // 计算属性
  const hasPendingApprovals = computed(() => pendingCount.value > 0)

  // 获取我的申请列表
  const fetchMyApplications = async (params = {}) => {
    loading.value = true
    try {
      const res = await getMyApplications(params)
      myApplications.value = res.data.content || res.data
      return res.data
    } catch (error) {
      console.error('获取我的申请列表失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  // 获取待审批列表
  const fetchPendingApprovals = async (params = {}) => {
    loading.value = true
    try {
      const res = await getPendingApprovals(params)
      pendingApprovals.value = res.data.content || res.data
      // 更新待审批数量
      pendingCount.value = res.data.totalElements || pendingApprovals.value.length
      return res.data
    } catch (error) {
      console.error('获取待审批列表失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  // 获取所有申请列表（管理员）
  const fetchAllApplications = async (params = {}) => {
    loading.value = true
    try {
      const res = await getAllApplications(params)
      allApplications.value = res.data.content || res.data
      return res.data
    } catch (error) {
      console.error('获取所有申请列表失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  // 获取统计数据
  const fetchStatistics = async (params = {}) => {
    try {
      const res = await getStatistics(params)
      statistics.value = res.data
      return res.data
    } catch (error) {
      console.error('获取统计数据失败:', error)
      throw error
    }
  }

  // 刷新待审批数量（用于徽章显示）
  const refreshPendingCount = async () => {
    try {
      const res = await getPendingApprovals({ page: 0, size: 1 })
      pendingCount.value = res.data.totalElements || 0
    } catch (error) {
      console.error('刷新待审批数量失败:', error)
    }
  }

  // 清空数据
  const clearData = () => {
    myApplications.value = []
    pendingApprovals.value = []
    allApplications.value = []
    statistics.value = {}
    pendingCount.value = 0
  }

  return {
    // 状态
    myApplications,
    pendingApprovals,
    allApplications,
    statistics,
    pendingCount,
    loading,

    // 计算属性
    hasPendingApprovals,

    // 方法
    fetchMyApplications,
    fetchPendingApprovals,
    fetchAllApplications,
    fetchStatistics,
    refreshPendingCount,
    clearData
  }
})

