import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { UserInfo, LoginRequest } from '@/api/types'
import { authApi } from '@/api/auth'
import router from '@/router'
import { rsaEncryptPasswordOAEP } from '@/utils/crypto'

// Token 刷新定时器
let refreshTimer: ReturnType<typeof setTimeout> | null = null

// Token 过期时间（毫秒），默认 25 分钟（假设 token 有效期 30 分钟）
const TOKEN_REFRESH_INTERVAL = 25 * 60 * 1000

export const useAuthStore = defineStore('auth', () => {
  // 状态
  const token = ref<string>(localStorage.getItem('token') || '')
  const refreshToken = ref<string>(localStorage.getItem('refreshToken') || '')
  const userInfo = ref<UserInfo | null>(JSON.parse(localStorage.getItem('userInfo') || 'null'))
  const permissions = ref<string[]>(JSON.parse(localStorage.getItem('permissions') || '[]'))
  const rememberMe = ref<boolean>(localStorage.getItem('rememberMe') === 'true')

  // 计算属性
  const isAuthenticated = computed(() => !!token.value)
  const username = computed(() => userInfo.value?.username || '')
  const realName = computed(() => userInfo.value?.realName || '')
  const roles = computed(() => userInfo.value?.roles || [])

  // 设置 Token
  function setToken(newToken: string, newRefreshToken?: string) {
    token.value = newToken
    localStorage.setItem('token', newToken)
    
    if (newRefreshToken) {
      refreshToken.value = newRefreshToken
      localStorage.setItem('refreshToken', newRefreshToken)
    }
    
    // 启动自动刷新
    startTokenRefresh()
  }

  // 设置用户信息
  function setUserInfo(info: UserInfo) {
    userInfo.value = info
    localStorage.setItem('userInfo', JSON.stringify(info))
  }

  // 设置权限
  function setPermissions(perms: string[]) {
    permissions.value = perms
    localStorage.setItem('permissions', JSON.stringify(perms))
  }

  // 设置记住我
  function setRememberMe(value: boolean) {
    rememberMe.value = value
    localStorage.setItem('rememberMe', String(value))
  }

  // 检查权限
  function hasPermission(permission: string): boolean {
    return permissions.value.includes(permission) || permissions.value.includes('*')
  }

  // 检查是否有任一权限
  function hasAnyPermission(perms: string[]): boolean {
    return perms.some(p => hasPermission(p))
  }

  // 检查角色
  function hasRole(role: string): boolean {
    return roles.value.includes(role)
  }

  // 检查是否有任一角色
  function hasAnyRole(roleList: string[]): boolean {
    return roleList.some(r => hasRole(r))
  }

  // 刷新权限（从服务器获取最新权限）
  async function refreshPermissions() {
    if (!token.value) return
    
    try {
      const res = await authApi.getCurrentPermissions()
      if (res.code === 200 && res.data) {
        setPermissions(res.data)
      }
    } catch (error) {
      console.error('刷新权限失败:', error)
    }
  }

  // 登录
  async function login(loginData: { username: string; password: string; rememberMe?: boolean }) {
    const { rememberMe: remember = false, username, password } = loginData
    const keyRes = await authApi.getLoginEncryptionKey()
    if (keyRes.code !== 200 || !keyRes.data?.publicKey) {
      throw new Error(keyRes.message || '获取登录加密公钥失败')
    }

    const encryptedPassword = await rsaEncryptPasswordOAEP(keyRes.data.publicKey, password)
    const credentials: LoginRequest = { username, encryptedPassword }
    const res = await authApi.login(credentials)
    
    if (res.code === 200 && res.data) {
      const { token: newToken, refreshToken: newRefreshToken, userInfo: info, permissions: perms } = res.data
      
      setToken(newToken, newRefreshToken)
      setUserInfo(info)
      setPermissions(perms)
      setRememberMe(remember)
      
      return res.data
    }
    
    throw new Error(res.message || '登录失败')
  }

  // 登出
  async function logout() {
    try {
      // 调用后端登出接口
      if (token.value) {
        await authApi.logout().catch(() => {
          // 忽略登出接口错误
        })
      }
    } finally {
      // 清除本地状态
      clearAuthState()
    }
  }

  // 清除认证状态
  function clearAuthState() {
    // 停止自动刷新
    stopTokenRefresh()
    
    // 清除状态
    token.value = ''
    refreshToken.value = ''
    userInfo.value = null
    permissions.value = []
    
    // 清除本地存储
    localStorage.removeItem('token')
    localStorage.removeItem('refreshToken')
    localStorage.removeItem('userInfo')
    localStorage.removeItem('permissions')
    
    // 如果不记住我，清除记住我状态
    if (!rememberMe.value) {
      localStorage.removeItem('rememberMe')
    }
  }

  // 刷新 Token
  async function doRefreshToken(): Promise<boolean> {
    if (!refreshToken.value) {
      return false
    }
    
    try {
      const res = await authApi.refreshToken(refreshToken.value)
      
      if (res.code === 200 && res.data) {
        setToken(res.data.token, res.data.refreshToken)
        return true
      }
      
      return false
    } catch (error) {
      console.error('Token 刷新失败:', error)
      return false
    }
  }

  // 启动 Token 自动刷新
  function startTokenRefresh() {
    stopTokenRefresh()
    
    if (token.value && refreshToken.value) {
      refreshTimer = setTimeout(async () => {
        const success = await doRefreshToken()
        if (!success) {
          // 刷新失败，跳转登录页
          clearAuthState()
          router.push({ name: 'Login' })
        }
      }, TOKEN_REFRESH_INTERVAL)
    }
  }

  // 停止 Token 自动刷新
  function stopTokenRefresh() {
    if (refreshTimer) {
      clearTimeout(refreshTimer)
      refreshTimer = null
    }
  }

  // 初始化时启动自动刷新，并刷新权限
  if (token.value && refreshToken.value) {
    startTokenRefresh()
    // 页面加载时刷新权限
    refreshPermissions()
  }

  return {
    // 状态
    token,
    refreshToken,
    userInfo,
    permissions,
    rememberMe,
    // 计算属性
    isAuthenticated,
    username,
    realName,
    roles,
    // 方法
    setToken,
    setUserInfo,
    setPermissions,
    setRememberMe,
    hasPermission,
    hasAnyPermission,
    hasRole,
    hasAnyRole,
    refreshPermissions,
    login,
    logout,
    clearAuthState,
    doRefreshToken,
    startTokenRefresh,
    stopTokenRefresh
  }
})
