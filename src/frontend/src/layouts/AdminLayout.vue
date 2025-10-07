<template>
  <div class="layout-container">
    <el-container>
      <el-aside width="200px">
        <div class="logo">管理端</div>
        <el-menu
          :default-active="activeMenu"
          router
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409eff"
        >
          <el-menu-item index="/admin/dashboard">
            <el-icon><House /></el-icon>
            <span>仪表盘</span>
          </el-menu-item>
          <el-menu-item index="/admin/students">
            <el-icon><UserFilled /></el-icon>
            <span>学生管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/teachers">
            <el-icon><User /></el-icon>
            <span>教师管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/courses">
            <el-icon><Reading /></el-icon>
            <span>课程管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/offerings">
            <el-icon><Calendar /></el-icon>
            <span>开课计划</span>
          </el-menu-item>
          <el-menu-item index="/admin/semesters">
            <el-icon><Clock /></el-icon>
            <span>学期管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/departments">
            <el-icon><OfficeBuilding /></el-icon>
            <span>院系管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/majors">
            <el-icon><School /></el-icon>
            <span>专业管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/system-config">
            <el-icon><Setting /></el-icon>
            <span>系统配置</span>
          </el-menu-item>
          <el-menu-item index="/admin/operation-log">
            <el-icon><Document /></el-icon>
            <span>操作日志</span>
          </el-menu-item>
          <el-menu-item index="/admin/statistics">
            <el-icon><DataAnalysis /></el-icon>
            <span>统计报表</span>
          </el-menu-item>
          <el-menu-item index="/admin/notifications">
            <el-icon><Bell /></el-icon>
            <span>通知管理</span>
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
                  管理员
                </span>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item @click="router.push('/admin/profile')">个人信息</el-dropdown-item>
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
import { House, UserFilled, User, Reading, Calendar, Clock, OfficeBuilding, School, Setting, Document, DataAnalysis, Bell } from '@element-plus/icons-vue'
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
    background-color: #304156;
  }

  .logo {
    height: 60px;
    line-height: 60px;
    text-align: center;
    font-size: 20px;
    font-weight: bold;
    color: white;
    background-color: #2b3a4a;
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

