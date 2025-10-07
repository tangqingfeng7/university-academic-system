import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAppStore = defineStore('app', () => {
  const sidebar = ref({
    opened: true,
    withoutAnimation: false
  })
  
  const currentSemester = ref(null)
  const notifications = ref([])
  const unreadCount = ref(0)

  // 切换侧边栏
  const toggleSidebar = () => {
    sidebar.value.opened = !sidebar.value.opened
    sidebar.value.withoutAnimation = false
  }

  // 关闭侧边栏
  const closeSidebar = (withoutAnimation = false) => {
    sidebar.value.opened = false
    sidebar.value.withoutAnimation = withoutAnimation
  }

  // 设置当前学期
  const setCurrentSemester = (semester) => {
    currentSemester.value = semester
  }

  // 设置通知
  const setNotifications = (list) => {
    notifications.value = list
    unreadCount.value = list.filter(n => !n.read).length
  }

  // 添加通知
  const addNotification = (notification) => {
    notifications.value.unshift(notification)
    if (!notification.read) {
      unreadCount.value++
    }
  }

  // 标记通知已读
  const markNotificationRead = (id) => {
    const notification = notifications.value.find(n => n.id === id)
    if (notification && !notification.read) {
      notification.read = true
      unreadCount.value = Math.max(0, unreadCount.value - 1)
    }
  }

  return {
    sidebar,
    currentSemester,
    notifications,
    unreadCount,
    toggleSidebar,
    closeSidebar,
    setCurrentSemester,
    setNotifications,
    addNotification,
    markNotificationRead
  }
})

