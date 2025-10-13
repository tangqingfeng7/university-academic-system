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
          path: 'classes',
          name: 'AdminClasses',
          component: () => import('@/views/admin/classes/Index.vue'),
          meta: { title: '班级管理', keepAlive: true }
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
          path: 'course-change-approval',
          name: 'AdminCourseChangeApproval',
          component: () => import('@/views/admin/CourseChangeApproval.vue'),
          meta: { title: '调课审批' }
        },
        {
          path: 'exams',
          name: 'AdminExams',
          component: () => import('@/views/admin/exam/ExamList.vue'),
          meta: { title: '考试管理', keepAlive: true }
        },
        {
          path: 'exams/create',
          name: 'AdminExamCreate',
          component: () => import('@/views/admin/exam/ExamForm.vue'),
          meta: { title: '创建考试' }
        },
        {
          path: 'exams/edit/:id',
          name: 'AdminExamEdit',
          component: () => import('@/views/admin/exam/ExamForm.vue'),
          meta: { title: '编辑考试' }
        },
        {
          path: 'exams/detail/:id',
          name: 'AdminExamDetail',
          component: () => import('@/views/admin/exam/ExamDetail.vue'),
          meta: { title: '考试详情' }
        },
        {
          path: 'evaluations/periods',
          name: 'AdminEvaluationPeriods',
          component: () => import('@/views/admin/evaluations/PeriodManagement.vue'),
          meta: { title: '评价周期管理' }
        },
        {
          path: 'evaluations/data',
          name: 'AdminEvaluations',
          component: () => import('@/views/admin/evaluations/DataQuery.vue'),
          meta: { title: '评价数据查询', keepAlive: true }
        },
        {
          path: 'evaluations/report',
          name: 'AdminEvaluationReport',
          component: () => import('@/views/admin/evaluations/QualityReport.vue'),
          meta: { title: '教学质量报告' }
        },
        {
          path: 'classrooms',
          name: 'AdminClassrooms',
          component: () => import('@/views/admin/classrooms/ClassroomManagement.vue'),
          meta: { title: '教室管理', keepAlive: true }
        },
        {
          path: 'classrooms/:id/usage',
          name: 'AdminClassroomUsage',
          component: () => import('@/views/admin/classrooms/ClassroomUsage.vue'),
          meta: { title: '教室使用情况' }
        },
        {
          path: 'classroom-bookings',
          name: 'AdminClassroomBookings',
          component: () => import('@/views/admin/classrooms/BookingApproval.vue'),
          meta: { title: '借用审批', keepAlive: true }
        },
        {
          path: 'classrooms/statistics',
          name: 'AdminClassroomStatistics',
          component: () => import('@/views/admin/classrooms/UtilizationStatistics.vue'),
          meta: { title: '使用率统计' }
        },
        // ==================== 学籍异动管理 ====================
        {
          path: 'status-changes',
          name: 'AdminStatusChanges',
          component: () => import('@/views/admin/status-changes/Index.vue'),
          meta: { title: '学籍异动管理', keepAlive: true }
        },
        {
          path: 'status-changes/approvals',
          name: 'AdminStatusChangeApprovals',
          component: () => import('@/views/admin/status-changes/ApprovalList.vue'),
          meta: { title: '学籍异动审批', keepAlive: true }
        },
        {
          path: 'status-changes/:id',
          name: 'AdminStatusChangeDetail',
          component: () => import('@/views/admin/status-changes/Detail.vue'),
          meta: { title: '异动详情' }
        },
        // ==================== 学费缴纳管理 ====================
        {
          path: 'tuition/standards',
          name: 'AdminTuitionStandards',
          component: () => import('@/views/admin/tuition/Standards.vue'),
          meta: { title: '学费标准设置', keepAlive: true }
        },
        {
          path: 'tuition/bills',
          name: 'AdminTuitionBills',
          component: () => import('@/views/admin/tuition/Bills.vue'),
          meta: { title: '账单管理', keepAlive: true }
        },
        {
          path: 'tuition/payments/offline',
          name: 'AdminTuitionOfflinePayment',
          component: () => import('@/views/admin/tuition/OfflinePayment.vue'),
          meta: { title: '线下缴费录入' }
        },
        {
          path: 'tuition/statistics',
          name: 'AdminTuitionStatistics',
          component: () => import('@/views/admin/tuition/Statistics.vue'),
          meta: { title: '财务统计' }
        },
        {
          path: 'tuition/refund-applications',
          name: 'AdminRefundApplications',
          component: () => import('@/views/admin/tuition/RefundApplications.vue'),
          meta: { title: '退费审批', keepAlive: true }
        },
        
        // ==================== 奖学金评定管理 ====================
        {
          path: 'scholarships',
          name: 'AdminScholarships',
          component: () => import('@/views/admin/scholarships/ScholarshipManagement.vue'),
          meta: { title: '奖学金管理', keepAlive: true }
        },
        {
          path: 'scholarship-applications',
          name: 'AdminScholarshipApplications',
          component: () => import('@/views/admin/scholarships/ApplicationManagement.vue'),
          meta: { title: '申请管理', keepAlive: true }
        },
        {
          path: 'scholarship-awards',
          name: 'AdminScholarshipAwards',
          component: () => import('@/views/admin/scholarships/AwardManagement.vue'),
          meta: { title: '获奖管理', keepAlive: true }
        },
        {
          path: 'scholarship-statistics',
          name: 'AdminScholarshipStatistics',
          component: () => import('@/views/admin/scholarships/Statistics.vue'),
          meta: { title: '奖学金统计' }
        },
        
        // ==================== 毕业审核管理 ====================
        {
          path: 'graduation/requirements',
          name: 'AdminGraduationRequirements',
          component: () => import('@/views/admin/graduation/Requirements.vue'),
          meta: { title: '毕业要求设置', keepAlive: true }
        },
        {
          path: 'graduation/audit',
          name: 'AdminGraduationAudit',
          component: () => import('@/views/admin/graduation/BatchAudit.vue'),
          meta: { title: '批量毕业审核' }
        },
        {
          path: 'graduation/audits',
          name: 'AdminGraduationAudits',
          component: () => import('@/views/admin/graduation/Audits.vue'),
          meta: { title: '审核结果查询', keepAlive: true }
        },
        {
          path: 'graduation/graduates',
          name: 'AdminGraduationGraduates',
          component: () => import('@/views/admin/graduation/Graduates.vue'),
          meta: { title: '毕业生名单', keepAlive: true }
        },
        
        // ==================== 排课优化管理 ====================
        {
          path: 'scheduling/constraints',
          name: 'AdminSchedulingConstraints',
          component: () => import('@/views/admin/scheduling/Constraints.vue'),
          meta: { title: '排课约束设置', keepAlive: true }
        },
        {
          path: 'scheduling/solutions',
          name: 'AdminSchedulingSolutions',
          component: () => import('@/views/admin/scheduling/Solutions.vue'),
          meta: { title: '排课方案管理', keepAlive: true }
        },
        {
          path: 'scheduling/optimize/:id',
          name: 'AdminSchedulingOptimize',
          component: () => import('@/views/admin/scheduling/Optimize.vue'),
          meta: { title: '智能排课' }
        },
        {
          path: 'scheduling/preview/:id',
          name: 'AdminSchedulingPreview',
          component: () => import('@/views/admin/scheduling/Preview.vue'),
          meta: { title: '课表预览' }
        },
        // ==================== 处分管理 ====================
        {
          path: 'disciplines',
          name: 'AdminDisciplines',
          component: () => import('@/views/admin/discipline/DisciplineList.vue'),
          meta: { title: '处分管理', keepAlive: true }
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
          path: 'course-change-requests',
          name: 'TeacherCourseChangeRequests',
          component: () => import('@/views/teacher/CourseChangeRequests.vue'),
          meta: { title: '调课申请' }
        },
        {
          path: 'exams/courses',
          name: 'TeacherCourseExams',
          component: () => import('@/views/teacher/exam/CourseExamList.vue'),
          meta: { title: '课程考试', keepAlive: true }
        },
        {
          path: 'exams/courses/:id',
          name: 'TeacherCourseExamDetail',
          component: () => import('@/views/teacher/exam/CourseExamDetail.vue'),
          meta: { title: '考试详情' }
        },
        {
          path: 'scheduling/preferences',
          name: 'TeacherSchedulePreferences',
          component: () => import('@/views/teacher/SchedulePreferences.vue'),
          meta: { title: '排课偏好设置' }
        },
        {
          path: 'exams/invigilation',
          name: 'TeacherInvigilation',
          component: () => import('@/views/teacher/exam/InvigilationList.vue'),
          meta: { title: '监考任务', keepAlive: true }
        },
        {
          path: 'evaluations/statistics',
          name: 'TeacherEvaluationStatistics',
          component: () => import('@/views/teacher/evaluations/Statistics.vue'),
          meta: { title: '教学评价统计' }
        },
        {
          path: 'my-classes',
          name: 'TeacherMyClasses',
          component: () => import('@/views/teacher/MyClasses.vue'),
          meta: { title: '我的班级', keepAlive: true }
        },
        {
          path: 'evaluations/list',
          name: 'TeacherEvaluationList',
          component: () => import('@/views/teacher/evaluations/EvaluationList.vue'),
          meta: { title: '评价列表', keepAlive: true }
        },
        {
          path: 'classrooms',
          name: 'TeacherClassrooms',
          component: () => import('@/views/teacher/classrooms/ClassroomList.vue'),
          meta: { title: '教室查询', keepAlive: true }
        },
        {
          path: 'classroom-bookings',
          name: 'TeacherClassroomBookings',
          component: () => import('@/views/teacher/classrooms/BookingList.vue'),
          meta: { title: '我的申请', keepAlive: true }
        },
        {
          path: 'classroom-bookings/create',
          name: 'TeacherBookingCreate',
          component: () => import('@/views/teacher/classrooms/BookingCreate.vue'),
          meta: { title: '申请借用教室' }
        },
        // ==================== 学籍异动审批 ====================
        {
          path: 'status-changes/approvals',
          name: 'TeacherStatusChangeApprovals',
          component: () => import('@/views/teacher/status-changes/ApprovalList.vue'),
          meta: { title: '学籍异动审批', keepAlive: true }
        },
        {
          path: 'status-changes/:id',
          name: 'TeacherStatusChangeDetail',
          component: () => import('@/views/teacher/status-changes/ApprovalDetail.vue'),
          meta: { title: '审批处理' }
        },
        // ==================== 处分上报 ====================
        {
          path: 'discipline/report',
          name: 'TeacherReportDiscipline',
          component: () => import('@/views/teacher/discipline/ReportDiscipline.vue'),
          meta: { title: '上报处分' }
        },
        {
          path: 'discipline/my-reports',
          name: 'TeacherMyReports',
          component: () => import('@/views/teacher/discipline/MyReports.vue'),
          meta: { title: '上报记录', keepAlive: true }
        },
        // ==================== 奖学金审批 ====================
        {
          path: 'scholarship-approvals',
          name: 'TeacherScholarshipApprovals',
          component: () => import('@/views/teacher/scholarships/ApprovalList.vue'),
          meta: { title: '奖学金审批', keepAlive: true }
        },
        {
          path: 'scholarship-approvals/:id',
          name: 'TeacherScholarshipApproval',
          component: () => import('@/views/teacher/scholarships/ApprovalDetail.vue'),
          meta: { title: '审批详情' }
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
          path: 'exams',
          name: 'StudentExams',
          component: () => import('@/views/student/exam/ExamList.vue'),
          meta: { title: '考试安排', keepAlive: true }
        },
        {
          path: 'exams/:id',
          name: 'StudentExamDetail',
          component: () => import('@/views/student/exam/ExamDetail.vue'),
          meta: { title: '考试详情' }
        },
        {
          path: 'evaluations',
          name: 'StudentEvaluations',
          component: () => import('@/views/student/evaluations/MyEvaluations.vue'),
          meta: { title: '我的评价', keepAlive: true }
        },
        {
          path: 'evaluations/available',
          name: 'StudentAvailableEvaluations',
          component: () => import('@/views/student/evaluations/AvailableCourses.vue'),
          meta: { title: '可评价课程' }
        },
        // ==================== 学籍异动申请 ====================
        {
          path: 'status-changes',
          name: 'StudentStatusChanges',
          component: () => import('@/views/student/status-changes/MyApplications.vue'),
          meta: { title: '我的异动申请', keepAlive: true }
        },
        {
          path: 'status-changes/create',
          name: 'StudentStatusChangeCreate',
          component: () => import('@/views/student/status-changes/CreateApplication.vue'),
          meta: { title: '提交异动申请' }
        },
        {
          path: 'status-changes/:id',
          name: 'StudentStatusChangeDetail',
          component: () => import('@/views/student/status-changes/ApplicationDetail.vue'),
          meta: { title: '申请详情' }
        },
        // ==================== 毕业审核 ====================
        {
          path: 'graduation/progress',
          name: 'StudentGraduationProgress',
          component: () => import('@/views/student/graduation/Progress.vue'),
          meta: { title: '毕业进度' }
        },
        {
          path: 'graduation/credits',
          name: 'StudentGraduationCredits',
          component: () => import('@/views/student/graduation/Credits.vue'),
          meta: { title: '学分汇总', keepAlive: true }
        },
        {
          path: 'graduation/audit',
          name: 'StudentGraduationAudit',
          component: () => import('@/views/student/graduation/Audit.vue'),
          meta: { title: '审核结果' }
        },
        // ==================== 学费缴纳 ====================
        {
          path: 'tuition/bills',
          name: 'StudentTuitionBills',
          component: () => import('@/views/student/tuition/BillList.vue'),
          meta: { title: '学费账单', keepAlive: true }
        },
        {
          path: 'tuition/payment',
          name: 'StudentTuitionPayment',
          component: () => import('@/views/student/tuition/Payment.vue'),
          meta: { title: '学费缴纳' }
        },
        {
          path: 'tuition/payments',
          name: 'StudentTuitionPayments',
          component: () => import('@/views/student/tuition/PaymentList.vue'),
          meta: { title: '缴费记录', keepAlive: true }
        },
        {
          path: 'tuition/refund-applications',
          name: 'StudentRefundApplications',
          component: () => import('@/views/student/tuition/RefundApplications.vue'),
          meta: { title: '退费申请', keepAlive: true }
        },
        // ==================== 奖学金评定 ====================
        {
          path: 'scholarships',
          name: 'StudentScholarships',
          component: () => import('@/views/student/scholarships/Index.vue'),
          meta: { title: '奖学金申请', keepAlive: true }
        },
        {
          path: 'scholarships/apply/:id',
          name: 'StudentScholarshipApply',
          component: () => import('@/views/student/scholarships/Apply.vue'),
          meta: { title: '提交申请' }
        },
        {
          path: 'scholarship-applications',
          name: 'StudentScholarshipApplications',
          component: () => import('@/views/student/scholarships/Applications.vue'),
          meta: { title: '我的申请', keepAlive: true }
        },
        {
          path: 'scholarship-awards',
          name: 'StudentScholarshipAwards',
          component: () => import('@/views/student/scholarships/Awards.vue'),
          meta: { title: '获奖记录', keepAlive: true }
        },
        // ==================== 处分记录 ====================
        {
          path: 'disciplines',
          name: 'StudentDisciplines',
          component: () => import('@/views/student/discipline/MyDisciplines.vue'),
          meta: { title: '处分记录', keepAlive: true }
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

