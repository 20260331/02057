<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { User, Reading, Document, Calendar, Edit, Tickets, DataBoard, Bell } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { request } from '@/api/request'

const router = useRouter()
const authStore = useAuthStore()

const isStudent = computed(() => authStore.roles.includes('STUDENT'))
const isTeacher = computed(() => authStore.roles.includes('TEACHER'))
const isAdmin = computed(() => authStore.roles.includes('ADMIN') || authStore.roles.includes('ACADEMIC_AFFAIRS'))

interface DashboardStats {
  studentCount: number
  courseCount: number
  pendingGradeCount: number
  pendingLeaveCount: number
  onLeaveCount: number
}

const stats = ref<DashboardStats>({
  studentCount: 0,
  courseCount: 0,
  pendingGradeCount: 0,
  pendingLeaveCount: 0,
  onLeaveCount: 0
})

const loading = ref(false)

const loadStats = async () => {
  loading.value = true
  try {
    const res = await request.get<DashboardStats>('/dashboard/stats')
    if (res.code === 200) {
      stats.value = res.data
    }
  } catch (error) {
    console.error('加载统计数据失败', error)
  } finally {
    loading.value = false
  }
}

const quickLinks = computed(() => {
  const links = []
  if (isStudent.value) {
    links.push(
      { title: '个人信息', icon: User, path: '/student/info', color: '#1890ff' },
      { title: '课程列表', icon: Reading, path: '/course/list', color: '#52c41a' },
      { title: '成绩查询', icon: Document, path: '/grade/query', color: '#722ed1' },
      { title: '请假申请', icon: Edit, path: '/leave/apply', color: '#faad14' },
      { title: '我的课表', icon: Tickets, path: '/course/schedule', color: '#13c2c2' },
      { title: '我的通知', icon: Bell, path: '/notification/my', color: '#eb2f96' },
    )
  } else if (isTeacher.value) {
    links.push(
      { title: '成绩录入', icon: Edit, path: '/grade/entry', color: '#722ed1' },
      { title: '课程列表', icon: Reading, path: '/course/list', color: '#52c41a' },
      { title: '我的通知', icon: Bell, path: '/notification/my', color: '#eb2f96' },
    )
  } else if (isAdmin.value) {
    links.push(
      { title: '学生列表', icon: User, path: '/student/list', color: '#1890ff' },
      { title: '课程管理', icon: Reading, path: '/course/list', color: '#52c41a' },
      { title: '成绩管理', icon: Document, path: '/grade/entry', color: '#722ed1' },
      { title: '请假审批', icon: Calendar, path: '/leave/approval', color: '#faad14' },
      { title: '用户管理', icon: DataBoard, path: '/system/user', color: '#13c2c2' },
      { title: '通知管理', icon: Bell, path: '/notification/manage', color: '#eb2f96' },
    )
  }
  return links
})

const navigateTo = (path: string) => {
  router.push(path)
}

const currentTime = ref('')
const updateTime = () => {
  const now = new Date()
  currentTime.value = now.toLocaleString('zh-CN', {
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit', second: '2-digit'
  })
}

onMounted(() => {
  loadStats()
  updateTime()
  setInterval(updateTime, 1000)
})
</script>

<template>
  <div class="dashboard">
    <div class="welcome-card">
      <div class="welcome-left">
        <h2>欢迎回来，{{ authStore.realName || authStore.username }}</h2>
        <p>高校学生信息管理系统</p>
      </div>
      <div class="welcome-right">
        <span class="time-display">{{ currentTime }}</span>
      </div>
    </div>
    
    <el-row :gutter="16" class="stat-cards">
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card" v-loading="loading">
          <div class="stat-icon" style="background-color: #1890ff;">
            <el-icon><User /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.studentCount }}</div>
            <div class="stat-label">学生总数</div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card" v-loading="loading">
          <div class="stat-icon" style="background-color: #52c41a;">
            <el-icon><Reading /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.courseCount }}</div>
            <div class="stat-label">开设课程</div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card" v-loading="loading">
          <div class="stat-icon" style="background-color: #faad14;">
            <el-icon><Document /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.pendingLeaveCount }}</div>
            <div class="stat-label">待审批请假</div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card" v-loading="loading">
          <div class="stat-icon" style="background-color: #ff4d4f;">
            <el-icon><Calendar /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.onLeaveCount }}</div>
            <div class="stat-label">请假中</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="content-row">
      <el-col :xs="24" :md="16">
        <el-card class="quick-links-card">
          <template #header>
            <span class="section-title">快捷入口</span>
          </template>
          <el-row :gutter="16">
            <el-col
              v-for="link in quickLinks"
              :key="link.path"
              :xs="12"
              :sm="8"
              :md="8"
            >
              <div class="quick-link-item" @click="navigateTo(link.path)">
                <div class="link-icon" :style="{ backgroundColor: link.color + '15', color: link.color }">
                  <el-icon :size="28"><component :is="link.icon" /></el-icon>
                </div>
                <span class="link-title">{{ link.title }}</span>
              </div>
            </el-col>
          </el-row>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :md="8">
        <el-card class="sys-info-card">
          <template #header>
            <span class="section-title">系统信息</span>
          </template>
          <div class="sys-info-list">
            <div class="sys-info-item">
              <span class="info-label">当前用户</span>
              <span class="info-value">{{ authStore.realName || authStore.username }}</span>
            </div>
            <div class="sys-info-item">
              <span class="info-label">用户角色</span>
              <span class="info-value">
                <el-tag
                  v-for="role in authStore.roles"
                  :key="role"
                  size="small"
                  style="margin-right: 4px"
                >{{ role }}</el-tag>
              </span>
            </div>
            <div class="sys-info-item">
              <span class="info-label">系统版本</span>
              <span class="info-value">v1.0.0</span>
            </div>
            <div class="sys-info-item">
              <span class="info-label">技术栈</span>
              <span class="info-value">Vue 3 + Spring Boot 3</span>
            </div>
            <div class="sys-info-item">
              <span class="info-label">数据库</span>
              <span class="info-value">MySQL 8.0</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style lang="scss" scoped>
.dashboard {
  .welcome-card {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 28px 32px;
    margin-bottom: 16px;
    background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
    border-radius: 8px;
    color: #fff;
    
    .welcome-left {
      h2 {
        margin: 0 0 6px 0;
        font-size: 22px;
        font-weight: 600;
      }
      
      p {
        margin: 0;
        opacity: 0.85;
        font-size: 14px;
      }
    }
    
    .welcome-right {
      .time-display {
        font-size: 15px;
        opacity: 0.9;
        font-variant-numeric: tabular-nums;
      }
    }
  }
  
  .stat-cards {
    margin-bottom: 16px;
    
    .el-col {
      margin-bottom: 0;
    }
  }
  
  .stat-card {
    :deep(.el-card__body) {
      display: flex;
      align-items: center;
      padding: 20px;
    }
    
    .stat-icon {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 52px;
      height: 52px;
      border-radius: 10px;
      color: #fff;
      font-size: 24px;
      margin-right: 16px;
      flex-shrink: 0;
    }
    
    .stat-info {
      .stat-value {
        font-size: 28px;
        font-weight: 600;
        color: #303133;
        line-height: 1.2;
      }
      
      .stat-label {
        font-size: 13px;
        color: #909399;
        margin-top: 4px;
      }
    }
  }
  
  .content-row {
    .el-col {
      margin-bottom: 0;
    }
  }
  
  .section-title {
    font-weight: 600;
    font-size: 15px;
    color: #303133;
  }
  
  .quick-links-card {
    height: 100%;
    
    .quick-link-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 20px 12px;
      margin-bottom: 12px;
      border-radius: 8px;
      cursor: pointer;
      transition: all 0.25s;
      
      &:hover {
        background-color: #f5f7fa;
        transform: translateY(-2px);
      }
      
      .link-icon {
        display: flex;
        align-items: center;
        justify-content: center;
        width: 56px;
        height: 56px;
        border-radius: 12px;
        margin-bottom: 10px;
      }
      
      .link-title {
        font-size: 13px;
        color: #606266;
        font-weight: 500;
      }
    }
  }
  
  .sys-info-card {
    height: 100%;
    
    .sys-info-list {
      .sys-info-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 12px 0;
        border-bottom: 1px solid #f0f0f0;
        
        &:last-child {
          border-bottom: none;
        }
        
        .info-label {
          color: #909399;
          font-size: 13px;
          flex-shrink: 0;
        }
        
        .info-value {
          color: #303133;
          font-size: 13px;
          text-align: right;
        }
      }
    }
  }
}

@media (max-width: 768px) {
  .dashboard {
    .welcome-card {
      flex-direction: column;
      align-items: flex-start;
      
      .welcome-right {
        margin-top: 8px;
      }
    }
    
    .stat-cards .el-col {
      margin-bottom: 12px;
    }
    
    .content-row .el-col {
      margin-bottom: 16px;
    }
  }
}
</style>
