import { request } from './request'
import type { EncryptionKeyResponse, LoginRequest, LoginResponse, UserInfo } from './types'

/**
 * 认证相关 API
 */
export const authApi = {
  /**
   * 用户登录
   */
  login(data: LoginRequest) {
    return request.post<LoginResponse>('/auth/login', data)
  },

  /**
   * 获取登录加密公钥
   */
  getLoginEncryptionKey() {
    return request.get<EncryptionKeyResponse>('/auth/login/encryption-key')
  },

  /**
   * 用户登出
   */
  logout() {
    return request.post<void>('/auth/logout')
  },

  /**
   * 刷新 token
   */
  refreshToken(refreshToken: string) {
    return request.post<{ token: string; refreshToken: string }>('/auth/refresh', { refreshToken })
  },

  /**
   * 获取当前用户信息
   */
  getCurrentUser() {
    return request.get<UserInfo>('/auth/current')
  },

  /**
   * 获取当前用户权限
   */
  getCurrentPermissions() {
    return request.get<string[]>('/auth/permissions')
  },

  /**
   * 修改密码
   */
  changePassword(data: { oldPassword: string; newPassword: string }) {
    return request.put<void>('/auth/password', data)
  }
}
