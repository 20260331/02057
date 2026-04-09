import { createRouter, createWebHistory, type RouteRecordRaw, type RouteLocationNormalized } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

// 路由元信息类型扩展
declare module 'vue-router' {
  interface RouteMeta {
    title?: string
    icon?: string
    requiresAuth?: boolean
    permissions?: string[]
    roles?: string[]
  }
}

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/LoginView.vue'),
    meta: { 
      title: '登录',
      requiresAuth: false 
    }
  },
  {
    path: '/',
    name: 'Layout',
    component: () => import('@/views/layout/MainLayout.vue'),
    redirect: '/dashboard',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/DashboardView.vue'),
        meta: { title: '首页', icon: 'HomeFilled' }
      },
      // 学生信息模块
      {
        path: 'student',
        name: 'Student',
        meta: { title: '学生信息', icon: 'User' },
        children: [
          {
            path: 'info',
            name: 'StudentInfo',
            component: () => import('@/views/student/StudentInfoView.vue'),
            meta: { title: '个人信息' }
          },
          {
            path: 'list',
            name: 'StudentList',
            component: () => import('@/views/student/StudentListView.vue'),
            meta: { title: '学生列表', permissions: ['student:view', 'student:update'] }
          },
          {
            path: 'status-change/apply',
            name: 'StatusChangeApply',
            component: () => import('@/views/student/StatusChangeApplyView.vue'),
            meta: { title: '学籍异动申请' }
          },
          {
            path: 'status-change/list',
            name: 'StatusChangeList',
            component: () => import('@/views/student/StatusChangeListView.vue'),
            meta: { title: '异动申请记录' }
          },
          {
            path: 'status-change/approval',
            name: 'StatusChangeApproval',
            component: () => import('@/views/student/StatusChangeApprovalView.vue'),
            meta: { title: '学籍异动审批', permissions: ['student:status:approve'] }
          },
          {
            path: 'certificate',
            name: 'Certificate',
            component: () => import('@/views/student/CertificateView.vue'),
            meta: { title: '学籍证明' }
          }
        ]
      },
      // 选课模块
      {
        path: 'course',
        name: 'Course',
        meta: { title: '选课管理', icon: 'Reading' },
        children: [
          {
            path: 'list',
            name: 'CourseList',
            component: () => import('@/views/course/CourseListView.vue'),
            meta: { title: '课程列表' }
          },
          {
            path: 'schedule',
            name: 'Schedule',
            component: () => import('@/views/course/ScheduleView.vue'),
            meta: { title: '我的课表' }
          },
          {
            path: 'lottery',
            name: 'LotteryManage',
            component: () => import('@/views/course/LotteryManageView.vue'),
            meta: { title: '选课抽签', permissions: ['course:manage'] }
          }
        ]
      },
      // 成绩模块
      {
        path: 'grade',
        name: 'Grade',
        meta: { title: '成绩管理', icon: 'Document' },
        children: [
          {
            path: 'query',
            name: 'GradeQuery',
            component: () => import('@/views/grade/GradeQueryView.vue'),
            meta: { title: '成绩查询' }
          },
          {
            path: 'entry',
            name: 'GradeEntry',
            component: () => import('@/views/grade/GradeEntryView.vue'),
            meta: { title: '成绩录入', permissions: ['grade:entry'] }
          }
        ]
      },
      // 请假模块
      {
        path: 'leave',
        name: 'Leave',
        meta: { title: '请销假', icon: 'Calendar' },
        children: [
          {
            path: 'apply',
            name: 'LeaveApply',
            component: () => import('@/views/leave/LeaveApplyView.vue'),
            meta: { title: '请假申请' }
          },
          {
            path: 'list',
            name: 'LeaveList',
            component: () => import('@/views/leave/LeaveListView.vue'),
            meta: { title: '请假记录' }
          },
          {
            path: 'approval',
            name: 'LeaveApproval',
            component: () => import('@/views/leave/LeaveApprovalView.vue'),
            meta: { title: '请假审批', permissions: ['leave:approve'] }
          },
          {
            path: 'stats',
            name: 'LeaveStats',
            component: () => import('@/views/leave/LeaveStatsView.vue'),
            meta: { title: '请假统计分析', permissions: ['leave:approve'] }
          }
        ]
      },
      // 消息通知模块
      {
        path: 'notification',
        name: 'Notification',
        meta: { title: '消息通知', icon: 'Bell' },
        children: [
          {
            path: 'my',
            name: 'MyNotification',
            component: () => import('@/views/notification/MyNotificationView.vue'),
            meta: { title: '我的通知' }
          },
          {
            path: 'template',
            name: 'NotificationTemplate',
            component: () => import('@/views/system/NotificationTemplateView.vue'),
            meta: { title: '通知模板管理', permissions: ['system:manage'] }
          },
          {
            path: 'manage',
            name: 'NotificationManage',
            component: () => import('@/views/system/NotificationManageView.vue'),
            meta: { title: '通知发送管理', permissions: ['system:manage'] }
          }
        ]
      },
      // 系统管理模块
      {
        path: 'system',
        name: 'System',
        meta: { title: '系统管理', icon: 'Setting', permissions: ['system:manage'] },
        children: [
          {
            path: 'user',
            name: 'UserManage',
            component: () => import('@/views/system/UserManageView.vue'),
            meta: { title: '用户管理' }
          },
          {
            path: 'role',
            name: 'RoleManage',
            component: () => import('@/views/system/RoleManageView.vue'),
            meta: { title: '角色管理' }
          },
          {
            path: 'config',
            name: 'SystemConfig',
            component: () => import('@/views/system/SystemConfigView.vue'),
            meta: { title: '系统配置' }
          },
          {
            path: 'data',
            name: 'DataManage',
            component: () => import('@/views/system/DataManageView.vue'),
            meta: { title: '数据管理' }
          },
          {
            path: 'audit',
            name: 'AuditLog',
            component: () => import('@/views/system/AuditLogView.vue'),
            meta: { title: '审计日志' }
          }
        ]
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/NotFoundView.vue'),
    meta: { title: '页面未找到', requiresAuth: false }
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

/**
 * 检查路由权限
 */
function checkPermission(to: RouteLocationNormalized, authStore: ReturnType<typeof useAuthStore>): boolean {
  // 管理员拥有所有权限
  if (authStore.hasAnyRole(['ADMIN', 'ACADEMIC_AFFAIRS'])) {
    return true
  }
  
  const { permissions, roles } = to.meta
  
  // 检查权限
  if (permissions && permissions.length > 0) {
    if (!authStore.hasAnyPermission(permissions)) {
      return false
    }
  }
  
  // 检查角色
  if (roles && roles.length > 0) {
    if (!authStore.hasAnyRole(roles)) {
      return false
    }
  }
  
  return true
}

/**
 * 设置页面标题
 */
function setPageTitle(to: RouteLocationNormalized) {
  const baseTitle = '学生信息管理系统'
  const pageTitle = to.meta.title
  document.title = pageTitle ? `${pageTitle} - ${baseTitle}` : baseTitle
}

// 路由守卫
router.beforeEach((to, _from, next) => {
  const authStore = useAuthStore()
  
  // 设置页面标题
  setPageTitle(to)
  
  // 不需要认证的页面直接放行
  if (to.meta.requiresAuth === false) {
    // 已登录用户访问登录页，重定向到首页
    if (to.name === 'Login' && authStore.isAuthenticated) {
      next({ name: 'Dashboard' })
      return
    }
    next()
    return
  }
  
  // 需要认证但未登录，跳转登录页
  if (!authStore.isAuthenticated) {
    next({ 
      name: 'Login', 
      query: { redirect: to.fullPath } 
    })
    return
  }
  
  // 检查权限
  if (!checkPermission(to, authStore)) {
    ElMessage.closeAll()
    ElMessage.error({
      message: '您没有权限访问该页面',
      offset: 60
    })
    next(false)
    return
  }
  
  next()
})

// 路由后置守卫
router.afterEach(() => {
  // 滚动到页面顶部
  window.scrollTo(0, 0)
})

export default router
