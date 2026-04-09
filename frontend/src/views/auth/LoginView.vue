<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import type { FormInstance, FormRules } from 'element-plus'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const formRef = ref<FormInstance>()
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: '',
  rememberMe: false
})

const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 50, message: '用户名长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 50, message: '密码长度在 6 到 50 个字符', trigger: 'blur' }
  ]
}

// 初始化时检查记住我状态
onMounted(() => {
  if (authStore.rememberMe) {
    loginForm.rememberMe = true
  }
})

const handleLogin = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  
  loading.value = true
  
  try {
    await authStore.login({
      username: loginForm.username,
      password: loginForm.password,
      rememberMe: loginForm.rememberMe
    })
    
    ElMessage.success('登录成功，欢迎回来！')
    
    // 跳转到重定向页面或首页
    const redirect = route.query.redirect as string
    router.push(redirect || '/')
  } catch (error: unknown) {
    // 错误已在 request 拦截器中处理，这里不再重复提示
  } finally {
    loading.value = false
  }
}

// 回车键登录
const handleKeyUp = (e: KeyboardEvent) => {
  if (e.key === 'Enter') {
    handleLogin()
  }
}
</script>

<template>
  <div class="login-container">
    <!-- 背景装饰 -->
    <div class="login-bg">
      <div class="bg-shape bg-shape-1"></div>
      <div class="bg-shape bg-shape-2"></div>
      <div class="bg-shape bg-shape-3"></div>
    </div>
    
    <!-- 登录卡片 -->
    <div class="login-card">
      <!-- Logo 和标题 -->
      <div class="login-header">
        <div class="logo-wrapper">
          <div class="logo-icon">
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M12 3L1 9L12 15L21 10.09V17H23V9L12 3Z" fill="currentColor"/>
              <path d="M5 13.18V17.18L12 21L19 17.18V13.18L12 17L5 13.18Z" fill="currentColor"/>
            </svg>
          </div>
        </div>
        <h1 class="login-title">学生信息管理系统</h1>
        <p class="login-subtitle">Student Management System</p>
      </div>
      
      <!-- 登录表单 -->
      <el-form
        ref="formRef"
        :model="loginForm"
        :rules="rules"
        class="login-form"
        size="large"
        @keyup="handleKeyUp"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            :prefix-icon="User"
            clearable
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            :prefix-icon="Lock"
            show-password
            clearable
          />
        </el-form-item>
        
        <el-form-item class="remember-row">
          <el-checkbox v-model="loginForm.rememberMe">记住我</el-checkbox>
        </el-form-item>
        
        <el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            class="login-button"
            @click="handleLogin"
          >
            <span v-if="!loading">登 录</span>
            <span v-else>登录中...</span>
          </el-button>
        </el-form-item>
      </el-form>
      
      <!-- 底部信息 -->
      <div class="login-footer">
        <p class="copyright">© 2024 高校学生信息管理系统</p>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.login-container {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  min-height: 100vh;
  overflow: hidden;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

// 背景装饰
.login-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  overflow: hidden;
  pointer-events: none;
}

.bg-shape {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  animation: float 6s ease-in-out infinite;
}

.bg-shape-1 {
  top: -100px;
  left: -100px;
  width: 400px;
  height: 400px;
  animation-delay: 0s;
}

.bg-shape-2 {
  top: 50%;
  right: -150px;
  width: 500px;
  height: 500px;
  animation-delay: 2s;
}

.bg-shape-3 {
  bottom: -200px;
  left: 30%;
  width: 600px;
  height: 600px;
  animation-delay: 4s;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0) rotate(0deg);
  }
  50% {
    transform: translateY(-20px) rotate(5deg);
  }
}

// 登录卡片
.login-card {
  position: relative;
  z-index: 10;
  width: 420px;
  padding: 48px 40px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
  backdrop-filter: blur(10px);
}

// 头部
.login-header {
  margin-bottom: 36px;
  text-align: center;
}

.logo-wrapper {
  display: flex;
  justify-content: center;
  margin-bottom: 16px;
}

.logo-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 64px;
  height: 64px;
  color: #fff;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.4);
  
  svg {
    width: 36px;
    height: 36px;
  }
}

.login-title {
  margin-bottom: 8px;
  font-size: 26px;
  font-weight: 600;
  color: #303133;
  letter-spacing: 1px;
}

.login-subtitle {
  font-size: 14px;
  color: #909399;
  letter-spacing: 0.5px;
}

// 表单
.login-form {
  :deep(.el-form-item) {
    margin-bottom: 24px;
  }
  
  :deep(.el-input__wrapper) {
    padding: 4px 12px;
    border-radius: 8px;
    box-shadow: 0 0 0 1px #dcdfe6 inset;
    transition: all 0.3s;
    
    &:hover {
      box-shadow: 0 0 0 1px #c0c4cc inset;
    }
    
    &.is-focus {
      box-shadow: 0 0 0 1px #667eea inset;
    }
  }
  
  :deep(.el-input__inner) {
    height: 44px;
    font-size: 15px;
  }
  
  :deep(.el-input__prefix) {
    font-size: 18px;
    color: #909399;
  }
}

.remember-row {
  margin-bottom: 16px !important;
  
  :deep(.el-form-item__content) {
    justify-content: space-between;
  }
  
  :deep(.el-checkbox__label) {
    color: #606266;
  }
}

.login-button {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 500;
  letter-spacing: 4px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  border-radius: 8px;
  transition: all 0.3s;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 20px rgba(102, 126, 234, 0.4);
  }
  
  &:active {
    transform: translateY(0);
  }
  
  &.is-loading {
    opacity: 0.8;
  }
}

// 底部
.login-footer {
  margin-top: 32px;
  text-align: center;
}

.copyright {
  font-size: 12px;
  color: #c0c4cc;
}

// 响应式
@media (max-width: 480px) {
  .login-card {
    width: 90%;
    max-width: 380px;
    padding: 32px 24px;
  }
  
  .login-title {
    font-size: 22px;
  }
  
  .logo-icon {
    width: 56px;
    height: 56px;
    
    svg {
      width: 32px;
      height: 32px;
    }
  }
}
</style>
