<template>
  <div class="not-found">
    <el-result icon="warning" title="404" sub-title="抱歉，页面不存在">
      <template #extra>
        <el-button type="primary" @click="goHome">返回首页</el-button>
      </template>
    </el-result>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const goHome = () => {
  const roleRoutes = {
    'ADMIN': '/admin',
    'TEACHER': '/teacher',
    'STUDENT': '/student'
  }
  router.push(roleRoutes[userStore.userInfo?.role] || '/login')
}
</script>

<style scoped lang="scss">
.not-found {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
}
</style>

