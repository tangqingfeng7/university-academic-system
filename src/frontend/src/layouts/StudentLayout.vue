<template>
  <div class="layout-container">
    <el-container>
      <el-aside width="200px">
        <div class="logo">学生端</div>
        <el-menu
          :default-active="activeMenu"
          router
          background-color="#545c64"
          text-color="#bfcbd9"
          active-text-color="#409eff"
        >
          <el-menu-item index="/student/dashboard">
            <el-icon><House /></el-icon>
            <span>工作台</span>
          </el-menu-item>
          <el-menu-item index="/student/course-selection">
            <el-icon><Document /></el-icon>
            <span>选课中心</span>
          </el-menu-item>
          <el-menu-item index="/student/schedule">
            <el-icon><Calendar /></el-icon>
            <span>我的课表</span>
          </el-menu-item>
          <el-menu-item index="/student/grades">
            <el-icon><Document /></el-icon>
            <span>成绩查询</span>
          </el-menu-item>
          <el-menu-item index="/student/transcript">
            <el-icon><Document /></el-icon>
            <span>成绩单</span>
          </el-menu-item>
          <el-menu-item index="/student/notifications">
            <el-icon><Bell /></el-icon>
            <span>通知公告</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      <el-container>
        <el-header>
          <div class="header-content">
            <span>大学教务管理系统</span>
            <div class="header-actions">
              <NotificationBell />
              <el-dropdown>
                <span class="user-info">
                  <el-icon><User /></el-icon>
                  学生
                </span>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item @click="router.push('/student/profile')">个人信息</el-dropdown-item>
                    <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
        </el-header>
        <el-main>
          <router-view v-slot="{ Component, route }">
            <keep-alive>
              <component :is="Component" v-if="route.meta.keepAlive" :key="route.name" />
            </keep-alive>
            <component :is="Component" v-if="!route.meta.keepAlive" :key="route.name" />
          </router-view>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { House, User, Document, Calendar, Bell } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import NotificationBell from '@/components/NotificationBell.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped lang="scss">
.layout-container {
  width: 100%;
  height: 100%;

  .el-container {
    height: 100%;
  }

  .el-aside {
    background-color: #545c64;
  }

  .logo {
    height: 60px;
    line-height: 60px;
    text-align: center;
    font-size: 20px;
    font-weight: bold;
    color: white;
    background-color: #4a5360;
  }

  .el-header {
    display: flex;
    align-items: center;
    background-color: white;
    box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  }

  .header-content {
    display: flex;
    align-items: center;
    justify-content: space-between;
    width: 100%;
  }

  .header-actions {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  .user-info {
    display: flex;
    align-items: center;
    cursor: pointer;

    .el-icon {
      margin-right: 5px;
    }
  }

  .el-main {
    background-color: #f0f2f5;
    padding: 20px;
  }
}
</style>

