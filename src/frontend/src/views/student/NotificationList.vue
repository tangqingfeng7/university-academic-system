<template>
  <div class="notification-page">
    <!-- 页面头部 -->
    <div class="page-header animate-fade-in-down">
      <div class="header-content">
        <h1 class="page-title">通知</h1>
        <p class="page-subtitle">查看系统和课程通知</p>
      </div>
    </div>

    <!-- 通知列表 -->
    <CommonNotificationList />
  </div>
</template>

<script setup>
import CommonNotificationList from '@/views/common/NotificationList.vue'
</script>

<style scoped lang="scss">
.notification-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: var(--spacing-3xl) var(--spacing-xl);

  // ===================================
  // 页面头部
  // ===================================

  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: var(--spacing-2xl);
  }

  .header-content {
    flex: 1;
  }

  .page-title {
    font-size: 40px;
    font-weight: 600;
    color: var(--text-primary);
    margin: 0 0 8px;
    letter-spacing: -0.03em;
  }

  .page-subtitle {
    font-size: 17px;
    color: var(--text-secondary);
    margin: 0;
    font-weight: 400;
  }
}
</style>

