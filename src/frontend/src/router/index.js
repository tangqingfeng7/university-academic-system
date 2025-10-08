import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/login'
    },
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/Login.vue'),
      meta: { title: '登录' }
    },
    {
      path: '/admin',
      name: 'Admin',
      component: () => import('@/layouts/AdminLayout.vue'),
      meta: { 
        title: '管理端',
        requiresAuth: true,
        role: 'ADMIN'
      },
      children: [
        {
          path: '',
          redirect: '/admin/dashboard'
        },
        {
          path: 'dashboard',
          name: 'AdminDashboard',
          component: () => import('@/views/admin/Dashboard.vue'),
          meta: { title: '仪表盘' }
        },
        {
          path: 'students',
          name: 'AdminStudents',
          component: () => import('@/views/admin/students/List.vue'),
          meta: { title: '学生管理', keepAlive: true }
        },
        {
          path: 'teachers',
          name: 'AdminTeachers',
          component: () => import('@/views/admin/teachers/List.vue'),
          meta: { title: '教师管理', keepAlive: true }
        },
        {
          path: 'courses',
          name: 'AdminCourses',
          component: () => import('@/views/admin/courses/List.vue'),
          meta: { title: '课程管理', keepAlive: true }
        },
        {
          path: 'offerings',
          name: 'AdminOfferings',
          component: () => import('@/views/admin/offerings/List.vue'),
          meta: { title: '开课计划管理', keepAlive: true }
        },
        {
          path: 'semesters',
          name: 'AdminSemesters',
          component: () => import('@/views/admin/semesters/Index.vue'),
          meta: { title: '学期管理' }
        },
        {
          path: 'departments',
          name: 'AdminDepartments',
          component: () => import('@/views/admin/departments/Index.vue'),
          meta: { title: '院系管理' }
        },
        {
          path: 'majors',
          name: 'AdminMajors',
          component: () => import('@/views/admin/majors/Index.vue'),
          meta: { title: '专业管理' }
        },
        {
          path: 'system-config',
          name: 'AdminSystemConfig',
          component: () => import('@/views/admin/system/SystemConfig.vue'),
          meta: { title: '系统配置' }
        },
        {
          path: 'operation-log',
          name: 'AdminOperationLog',
          component: () => import('@/views/admin/system/OperationLog.vue'),
          meta: { title: '操作日志' }
        },
        {
          path: 'statistics',
          name: 'AdminStatistics',
          component: () => import('@/views/admin/statistics/Index.vue'),
          meta: { title: '统计报表' }
        },
        {
          path: 'notifications',
          name: 'AdminNotifications',
          component: () => import('@/views/admin/NotificationCenter.vue'),
          meta: { title: '通知管理' }
        },
        {
          path: 'leave-requests',
          name: 'AdminLeaveRequests',
          component: () => import('@/views/admin/LeaveRequests.vue'),
          meta: { title: '请假审批' }
        },
        {
          path: 'profile',
          name: 'AdminProfile',
          component: () => import('@/views/common/Profile.vue'),
          meta: { title: '个人信息' }
        }
      ]
    },
    {
      path: '/teacher',
      name: 'Teacher',
      component: () => import('@/layouts/TeacherLayout.vue'),
      meta: { 
        title: '教师端',
        requiresAuth: true,
        role: 'TEACHER'
      },
      children: [
        {
          path: '',
          redirect: '/teacher/dashboard'
        },
        {
          path: 'dashboard',
          name: 'TeacherDashboard',
          component: () => import('@/views/teacher/Dashboard.vue'),
          meta: { title: '教师工作台' }
        },
        {
          path: 'courses',
          name: 'TeacherCourses',
          component: () => import('@/views/teacher/Courses.vue'),
          meta: { title: '我的课程', keepAlive: true }
        },
        {
          path: 'grades/:id',
          name: 'TeacherGradeInput',
          component: () => import('@/views/teacher/GradeInput.vue'),
          meta: { title: '成绩录入' }
        },
        {
          path: 'schedule',
          name: 'TeacherSchedule',
          component: () => import('@/views/teacher/Schedule.vue'),
          meta: { title: '我的课表' }
        },
        {
          path: 'notifications',
          name: 'TeacherNotifications',
          component: () => import('@/views/teacher/NotificationCenter.vue'),
          meta: { title: '通知管理' }
        },
        {
          path: 'leave-requests',
          name: 'TeacherLeaveRequests',
          component: () => import('@/views/teacher/LeaveRequests.vue'),
          meta: { title: '请假申请' }
        },
        {
          path: 'profile',
          name: 'TeacherProfile',
          component: () => import('@/views/common/Profile.vue'),
          meta: { title: '个人信息' }
        }
      ]
    },
    {
      path: '/student',
      name: 'Student',
      component: () => import('@/layouts/StudentLayout.vue'),
      meta: { 
        title: '学生端',
        requiresAuth: true,
        role: 'STUDENT'
      },
      children: [
        {
          path: '',
          redirect: '/student/dashboard'
        },
        {
          path: 'dashboard',
          name: 'StudentDashboard',
          component: () => import('@/views/student/Dashboard.vue'),
          meta: { title: '学生工作台' }
        },
        {
          path: 'course-selection',
          name: 'StudentCourseSelection',
          component: () => import('@/views/student/CourseSelection.vue'),
          meta: { title: '选课中心', keepAlive: true }
        },
        {
          path: 'schedule',
          name: 'StudentSchedule',
          component: () => import('@/views/student/Schedule.vue'),
          meta: { title: '我的课表', keepAlive: true }
        },
        {
          path: 'grades',
          name: 'StudentGrades',
          component: () => import('@/views/student/GradeList.vue'),
          meta: { title: '成绩查询', keepAlive: true }
        },
        {
          path: 'transcript',
          name: 'StudentTranscript',
          component: () => import('@/views/student/Transcript.vue'),
          meta: { title: '成绩单' }
        },
        {
          path: 'notifications',
          name: 'StudentNotifications',
          component: () => import('@/views/student/NotificationList.vue'),
          meta: { title: '通知公告' }
        },
        {
          path: 'leave-requests',
          name: 'StudentLeaveRequests',
          component: () => import('@/views/student/LeaveRequests.vue'),
          meta: { title: '请假申请' }
        },
        {
          path: 'profile',
          name: 'StudentProfile',
          component: () => import('@/views/common/Profile.vue'),
          meta: { title: '个人信息' }
        }
      ]
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'NotFound',
      component: () => import('@/views/NotFound.vue'),
      meta: { title: '页面不存在' }
    }
  ]
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  
  // 设置页面标题
  document.title = to.meta.title 
    ? `${to.meta.title} - 大学教务管理系统` 
    : '大学教务管理系统'

  // 检查是否需要认证
  if (to.meta.requiresAuth) {
    if (!userStore.token) {
      // 未登录，跳转到登录页
      next('/login')
      return
    }

    // 检查角色权限
    if (to.meta.role && userStore.userInfo?.role !== to.meta.role) {
      // 权限不足，跳转到对应角色的首页
      const roleRoutes = {
        'ADMIN': '/admin',
        'TEACHER': '/teacher',
        'STUDENT': '/student'
      }
      next(roleRoutes[userStore.userInfo?.role] || '/login')
      return
    }
  }

  // 已登录用户访问登录页，重定向到对应的首页
  if (to.path === '/login' && userStore.token && userStore.userInfo) {
    const roleRoutes = {
      'ADMIN': '/admin',
      'TEACHER': '/teacher',
      'STUDENT': '/student'
    }
    next(roleRoutes[userStore.userInfo.role] || '/login')
    return
  }

  next()
})

export default router

