<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { request } from '@/api/request'

const router = useRouter()
const authStore = useAuthStore()

const isCollapsed = ref(false)
const activeMenu = computed(() => router.currentRoute.value.path)

// 判断用户角色
const isStudent = computed(() => authStore.roles.includes('STUDENT'))
const isTeacher = computed(() => authStore.roles.includes('TEACHER'))
const isCounselor = computed(() => authStore.roles.includes('COUNSELOR'))
const isAdmin = computed(() => authStore.roles.includes('ADMIN') || authStore.roles.includes('ACADEMIC_AFFAIRS'))
const canApproveLeave = computed(() => isCounselor.value || authStore.roles.includes('DEPARTMENT_HEAD') || isAdmin.value)
const canApproveStatusChange = computed(() => isCounselor.value || isAdmin.value)

// 基于权限判断菜单显示
const canViewStudentList = computed(() => authStore.hasPermission('student:list') || !isStudent.value)

// 修改密码对话框
const passwordDialogVisible = ref(false)
const passwordLoading = ref(false)
const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const toggleSidebar = () => {
  isCollapsed.value = !isCollapsed.value
}

// 个人中心
const handleProfile = () => {
  router.push('/student/info')
}

// 打开修改密码对话框
const handleChangePassword = () => {
  passwordForm.value = {
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  }
  passwordDialogVisible.value = true
}

// 提交修改密码
const submitChangePassword = async () => {
  if (!passwordForm.value.oldPassword || !passwordForm.value.newPassword || !passwordForm.value.confirmPassword) {
    ElMessage.warning('请填写完整信息')
    return
  }
  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    ElMessage.warning('两次输入的新密码不一致')
    return
  }
  if (passwordForm.value.newPassword.length < 6) {
    ElMessage.warning('新密码长度不能少于6位')
    return
  }
  
  try {
    passwordLoading.value = true
    await request.post('/system/users/change-password', {
      oldPassword: passwordForm.value.oldPassword,
      newPassword: passwordForm.value.newPassword
    })
    ElMessage.success('密码修改成功，请重新登录')
    passwordDialogVisible.value = false
    authStore.logout()
    router.push('/login')
  } catch (error) {
    // 错误已在 request 拦截器中处理
  } finally {
    passwordLoading.value = false
  }
}

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    authStore.logout()
    router.push('/login')
  } catch {
    // 取消退出
  }
}
</script>

<template>
  <el-container class="main-layout">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapsed ? '64px' : '220px'" class="sidebar">
      <div class="logo">
        <span v-if="!isCollapsed">学生管理系统</span>
        <span v-else>SMS</span>
      </div>
      
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapsed"
        :collapse-transition="false"
        router
        class="sidebar-menu"
      >
        <el-menu-item index="/dashboard">
          <el-icon><HomeFilled /></el-icon>
          <template #title>首页</template>
        </el-menu-item>
        
        <el-sub-menu index="/student">
          <template #title>
            <el-icon><User /></el-icon>
            <span>学生信息</span>
          </template>
          <el-menu-item v-if="isStudent" index="/student/info">个人信息</el-menu-item>
          <el-menu-item v-if="canViewStudentList" index="/student/list">学生列表</el-menu-item>
          <el-menu-item v-if="isStudent" index="/student/status-change/apply">学籍异动申请</el-menu-item>
          <el-menu-item v-if="isStudent" index="/student/status-change/list">异动申请记录</el-menu-item>
          <el-menu-item v-if="canApproveStatusChange" index="/student/status-change/approval">学籍异动审批</el-menu-item>
          <el-menu-item index="/student/certificate">学籍证明</el-menu-item>
        </el-sub-menu>
        
        <el-sub-menu index="/course">
          <template #title>
            <el-icon><Reading /></el-icon>
            <span>选课管理</span>
          </template>
          <el-menu-item index="/course/list">课程列表</el-menu-item>
          <el-menu-item v-if="isStudent" index="/course/schedule">我的课表</el-menu-item>
          <el-menu-item v-if="isAdmin" index="/course/lottery">选课抽签</el-menu-item>
        </el-sub-menu>
        
        <el-sub-menu index="/grade">
          <template #title>
            <el-icon><Document /></el-icon>
            <span>成绩管理</span>
          </template>
          <el-menu-item v-if="isStudent" index="/grade/query">成绩查询</el-menu-item>
          <el-menu-item v-if="isTeacher || isAdmin" index="/grade/entry">成绩录入</el-menu-item>
        </el-sub-menu>
        
        <el-sub-menu index="/leave">
          <template #title>
            <el-icon><Calendar /></el-icon>
            <span>请销假</span>
          </template>
          <el-menu-item v-if="isStudent" index="/leave/apply">请假申请</el-menu-item>
          <el-menu-item v-if="isStudent" index="/leave/list">请假记录</el-menu-item>
          <el-menu-item v-if="canApproveLeave" index="/leave/approval">请假审批</el-menu-item>
          <el-menu-item v-if="canApproveLeave" index="/leave/stats">请假统计分析</el-menu-item>
        </el-sub-menu>
        
        <el-sub-menu index="/notification">
          <template #title>
            <el-icon><Bell /></el-icon>
            <span>消息通知</span>
          </template>
          <el-menu-item index="/notification/my">我的通知</el-menu-item>
          <el-menu-item v-if="isAdmin" index="/notification/template">通知模板管理</el-menu-item>
          <el-menu-item v-if="isAdmin" index="/notification/manage">通知发送管理</el-menu-item>
        </el-sub-menu>
        
        <el-sub-menu v-if="isAdmin" index="/system">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>系统管理</span>
          </template>
          <el-menu-item index="/system/user">用户管理</el-menu-item>
          <el-menu-item index="/system/role">角色管理</el-menu-item>
          <el-menu-item index="/system/config">系统配置</el-menu-item>
          <el-menu-item index="/system/data">数据管理</el-menu-item>
          <el-menu-item index="/system/audit">审计日志</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>
    
    <el-container class="main-container">
      <!-- 顶部导航 -->
      <el-header class="header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="toggleSidebar">
            <Fold v-if="!isCollapsed" />
            <Expand v-else />
          </el-icon>
        </div>
        
        <div class="header-right">
          <el-dropdown trigger="click">
            <div class="user-info">
              <el-avatar :size="32" icon="UserFilled" />
              <span class="username">{{ authStore.realName || authStore.username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="handleProfile">个人中心</el-dropdown-item>
                <el-dropdown-item @click="handleChangePassword">修改密码</el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      
      <!-- 主内容区 -->
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
    
    <!-- 修改密码对话框 -->
    <el-dialog v-model="passwordDialogVisible" title="修改密码" width="400px">
      <el-form :model="passwordForm" label-width="80px">
        <el-form-item label="原密码">
          <el-input v-model="passwordForm.oldPassword" type="password" placeholder="请输入原密码" show-password />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码" show-password />
        </el-form-item>
        <el-form-item label="确认密码">
          <el-input v-model="passwordForm.confirmPassword" type="password" placeholder="请再次输入新密码" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="passwordLoading" @click="submitChangePassword">确定</el-button>
      </template>
    </el-dialog>
  </el-container>
</template>

<style lang="scss" scoped>
.main-layout {
  width: 100%;
  height: 100vh;
}

.sidebar {
  background-color: #001529;
  transition: width 0.3s;
  overflow: hidden;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.15);
  z-index: 10;
  
  .logo {
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    font-size: 18px;
    font-weight: 600;
    background-color: #002140;
    letter-spacing: 2px;
    white-space: nowrap;
    overflow: hidden;
  }
  
  .sidebar-menu {
    border-right: none;
    background-color: #001529;
    
    :deep(.el-menu-item),
    :deep(.el-sub-menu__title) {
      color: rgba(255, 255, 255, 0.65);
      transition: all 0.2s;
      
      &:hover {
        color: #fff;
        background-color: #000c17;
      }
    }
    
    :deep(.el-menu-item.is-active) {
      color: #fff;
      background-color: #1890ff;
    }
    
    :deep(.el-sub-menu.is-opened > .el-sub-menu__title) {
      color: #fff;
    }
    
    :deep(.el-sub-menu .el-menu) {
      background-color: #000c17;
    }
    
    :deep(.el-sub-menu .el-menu .el-menu-item) {
      background-color: #000c17;
      padding-left: 48px !important;
      
      &:hover {
        color: #fff;
        background-color: #001529;
      }
      
      &.is-active {
        color: #fff !important;
        background-color: #1890ff !important;
      }
    }

    :deep(.el-menu--inline .el-menu-item.is-active) {
      color: #fff !important;
      background-color: #1890ff !important;
    }
  }
}

.main-container {
  flex-direction: column;
  overflow: hidden;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  background-color: #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  z-index: 5;
  
  .header-left {
    display: flex;
    align-items: center;
    
    .collapse-btn {
      font-size: 20px;
      cursor: pointer;
      color: #606266;
      padding: 4px;
      border-radius: 4px;
      transition: all 0.2s;
      
      &:hover {
        color: #1890ff;
        background-color: #f0f2f5;
      }
    }
  }
  
  .header-right {
    .user-info {
      display: flex;
      align-items: center;
      cursor: pointer;
      padding: 4px 8px;
      border-radius: 6px;
      transition: background-color 0.2s;
      
      &:hover {
        background-color: #f5f7fa;
      }
      
      .username {
        margin: 0 8px;
        color: #606266;
        font-size: 14px;
      }
    }
  }
}

.main-content {
  padding: 20px;
  background-color: #f0f2f5;
  overflow-y: auto;
  flex: 1;
  
  :deep(.el-card) {
    border-radius: 8px;
    border: none;
    box-shadow: 0 1px 6px rgba(0, 0, 0, 0.05);
    margin-bottom: 16px;
  }
  
  :deep(.el-table) {
    width: 100% !important;
  }
  
  :deep(.search-card) {
    margin-bottom: 16px;
  }
  
  :deep(.el-pagination) {
    margin-top: 16px;
    justify-content: flex-end;
  }
  
  :deep(.toolbar) {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-weight: 600;
  }
  
  :deep(.card-header) {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-weight: 600;
  }
}
</style>
