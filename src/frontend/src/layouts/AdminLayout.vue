<template>
  <div class="layout-container">
    <el-container>
      <!-- 侧边栏 - Apple 风格 -->
      <el-aside :width="isCollapse ? '80px' : '240px'" class="apple-sidebar">
        <div class="sidebar-header">
          <div class="logo-wrapper" :class="{ collapsed: isCollapse }">
            <div class="logo-icon">
              <el-icon :size="24"><Platform /></el-icon>
            </div>
            <transition name="fade">
              <span v-if="!isCollapse" class="logo-text">管理后台</span>
            </transition>
          </div>
        </div>

        <el-scrollbar class="sidebar-scroll">
          <el-menu
            :default-active="activeMenu"
            :collapse="isCollapse"
            router
            :collapse-transition="false"
            class="sidebar-menu"
          >
            <el-menu-item index="/admin/dashboard">
              <el-icon><TrendCharts /></el-icon>
              <template #title>仪表盘</template>
            </el-menu-item>

            <el-menu-item index="/admin/students">
              <el-icon><UserFilled /></el-icon>
              <template #title>学生管理</template>
            </el-menu-item>

            <el-menu-item index="/admin/teachers">
              <el-icon><User /></el-icon>
              <template #title>教师管理</template>
            </el-menu-item>

            <el-menu-item index="/admin/courses">
              <el-icon><Reading /></el-icon>
              <template #title>课程管理</template>
            </el-menu-item>

            <el-menu-item index="/admin/offerings">
              <el-icon><Calendar /></el-icon>
              <template #title>开课计划</template>
            </el-menu-item>

            <el-menu-item index="/admin/semesters">
              <el-icon><Clock /></el-icon>
              <template #title>学期管理</template>
            </el-menu-item>

            <el-menu-item index="/admin/departments">
              <el-icon><OfficeBuilding /></el-icon>
              <template #title>院系管理</template>
            </el-menu-item>

            <el-menu-item index="/admin/majors">
              <el-icon><School /></el-icon>
              <template #title>专业管理</template>
            </el-menu-item>

            <el-menu-item index="/admin/system-config">
              <el-icon><Setting /></el-icon>
              <template #title>系统配置</template>
            </el-menu-item>

            <el-menu-item index="/admin/operation-log">
              <el-icon><Document /></el-icon>
              <template #title>操作日志</template>
            </el-menu-item>

            <el-menu-item index="/admin/statistics">
              <el-icon><DataAnalysis /></el-icon>
              <template #title>统计报表</template>
            </el-menu-item>

            <el-menu-item index="/admin/notifications">
              <el-icon><Bell /></el-icon>
              <template #title>通知管理</template>
            </el-menu-item>
          </el-menu>
        </el-scrollbar>
      </el-aside>

      <el-container class="main-container">
        <!-- 顶部导航 - Apple 风格 -->
        <el-header class="apple-header">
          <div class="header-content">
            <div class="header-left">
              <el-button 
                :icon="isCollapse ? Expand : Fold" 
                circle 
                @click="toggleCollapse"
              />
            </div>

            <div class="header-right">
              <NotificationBell />
              
              <el-dropdown class="user-dropdown" @command="handleCommand">
                <div class="user-info">
                  <el-avatar :size="32" class="user-avatar">
                    <el-icon :size="18"><User /></el-icon>
                  </el-avatar>
                  <span class="user-name">{{ userStore.userInfo?.name || userStore.userInfo?.username || '管理员' }}</span>
                </div>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="profile">
                      <el-icon><User /></el-icon>
                      <span>个人信息</span>
                    </el-dropdown-item>
                    <el-dropdown-item command="password">
                      <el-icon><Lock /></el-icon>
                      <span>修改密码</span>
                    </el-dropdown-item>
                    <el-dropdown-item divided command="logout">
                      <el-icon><SwitchButton /></el-icon>
                      <span>退出登录</span>
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
        </el-header>

        <!-- 主内容区 -->
        <el-main class="apple-main">
          <transition name="fade-transform" mode="out-in">
            <router-view v-slot="{ Component, route }">
              <keep-alive>
                <component :is="Component" v-if="route.meta.keepAlive" :key="route.name" />
              </keep-alive>
              <component :is="Component" v-if="!route.meta.keepAlive" :key="route.name" />
            </router-view>
          </transition>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  Platform, TrendCharts, UserFilled, User, Reading, Calendar, Clock, 
  OfficeBuilding, School, Setting, Document, DataAnalysis, Bell,
  Expand, Fold, Lock, SwitchButton
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import NotificationBell from '@/components/NotificationBell.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const isCollapse = ref(false)
const activeMenu = computed(() => route.path)

const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
}

const handleCommand = async (command) => {
  if (command === 'profile') {
    router.push('/admin/profile')
  } else if (command === 'password') {
    // 打开修改密码对话框
  } else if (command === 'logout') {
    await userStore.logout()
    router.push('/login')
  }
}
</script>

<style scoped lang="scss">
.layout-container {
  width: 100%;
  height: 100%;
  background: var(--bg-primary);

  .el-container {
    height: 100%;
  }
}

// ===================================
// 侧边栏 - Apple 风格
// ===================================

.apple-sidebar {
  background: var(--bg-primary);
  border-right: 1px solid var(--border-light);
  transition: width var(--transition-base);
  overflow: hidden;
}

.sidebar-header {
  padding: 24px 20px;
  border-bottom: 1px solid var(--border-light);
}

.logo-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
  transition: all var(--transition-base);

  &.collapsed {
    justify-content: center;
  }
}

.logo-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  background: var(--primary-color);
  border-radius: var(--radius-md);
  color: white;
  flex-shrink: 0;
}

.logo-text {
  font-size: 17px;
  font-weight: 600;
  color: var(--text-primary);
  letter-spacing: -0.01em;
}

.sidebar-scroll {
  height: calc(100% - 88px);
}

.sidebar-menu {
  border: none;
  background: transparent;
  padding: 8px 12px;

  :deep(.el-menu-item) {
    height: 40px;
    line-height: 40px;
    margin-bottom: 2px;
    border-radius: var(--radius-md);
    color: var(--text-primary);
    transition: all var(--transition-fast);
    font-size: 15px;
    font-weight: 500;

    &:hover {
      background: var(--bg-secondary);
    }

    &.is-active {
      background: var(--primary-color);
      color: white;

      .el-icon {
        color: white;
      }
    }

    .el-icon {
      font-size: 18px;
      color: var(--text-secondary);
      transition: color var(--transition-fast);
    }
  }
}

// ===================================
// 顶部导航 - Apple 风格
// ===================================

.main-container {
  display: flex;
  flex-direction: column;
}

.apple-header {
  height: 64px;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: var(--backdrop-blur);
  border-bottom: 1px solid var(--border-light);
  padding: 0 24px;
  z-index: 10;
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 100%;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-dropdown {
  cursor: pointer;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 6px 12px;
  border-radius: var(--radius-full);
  transition: all var(--transition-fast);

  &:hover {
    background: var(--bg-tertiary);
  }
}

.user-avatar {
  background: var(--primary-color);
  color: white;
}

.user-name {
  font-size: 15px;
  font-weight: 500;
  color: var(--text-primary);
}

// ===================================
// 主内容区
// ===================================

.apple-main {
  padding: 0;
  background: var(--bg-secondary);
  min-height: calc(100vh - 64px);
}

// ===================================
// 过渡动画
// ===================================

.fade-enter-active,
.fade-leave-active {
  transition: opacity var(--transition-base);
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: all var(--transition-base);
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-10px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(10px);
}

// ===================================
// 下拉菜单样式
// ===================================

:deep(.el-dropdown-menu__item) {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  font-size: 15px;
  color: var(--text-primary);

  .el-icon {
    font-size: 16px;
    color: var(--text-secondary);
  }

  &:hover {
    background: var(--bg-secondary);

    .el-icon {
      color: var(--primary-color);
    }
  }
}

// ===================================
// 响应式设计
// ===================================

@media (max-width: 768px) {
  .apple-header {
    padding: 0 16px;
  }

  .user-name {
    display: none;
  }
}
</style>
