import { request } from './request'
import type { PageQuery, PageResult } from './types'

// 系统用户
export interface SystemUser {
  id: number
  username: string
  realName: string
  phone: string
  email: string
  roles: string[]
  status: number
  statusText: string
  lastLoginTime: string
}

// 用户查询参数
export interface UserQuery extends PageQuery {
  username?: string
  realName?: string
  status?: number
}

// 用户创建数据
export interface UserCreateData {
  username: string
  realName: string
  phone?: string
  email?: string
}

// 系统角色
export interface SystemRole {
  id: number
  roleCode: string
  roleName: string
  description: string
  status: number
}

// 系统配置
export interface SystemConfig {
  id: number
  configKey: string
  configValue: string
  configType: string
  configName: string
  description: string
}

// 备份信息
export interface BackupInfo {
  fileName: string
  fileSize: string
  createdAt: string
}

// 数据字典项
export interface DictItem {
  code: string
  name: string
  description?: string
  isSystem?: number
  values: { value: string; label: string; cssClass?: string }[]
}

// 字典类型
export interface DictTypeItem {
  id: number
  dictCode: string
  dictName: string
  description: string
  isSystem: number
  status: number
  sortOrder: number
}

// 字典项
export interface DictValueItem {
  id: number
  dictTypeId: number
  dictCode: string
  itemValue: string
  itemLabel: string
  description: string
  cssClass: string
  status: number
  sortOrder: number
}

/**
 * 系统管理 API
 */
export const systemApi = {
  // 用户管理
  queryUsers(query: UserQuery) {
    return request.get<PageResult<SystemUser>>('/system/users', { params: query })
  },

  createUser(data: UserCreateData) {
    return request.post('/system/users', data)
  },

  updateUser(id: number, data: Partial<UserCreateData>) {
    return request.put(`/system/users/${id}`, data)
  },

  deleteUser(id: number) {
    return request.delete(`/system/users/${id}`)
  },

  resetPassword(id: number) {
    return request.post(`/system/users/${id}/reset-password`)
  },

  assignRoles(userId: number, roleIds: number[]) {
    return request.post(`/system/users/${userId}/roles`, { roleIds })
  },

  // 角色管理
  queryRoles() {
    return request.get<SystemRole[]>('/system/roles')
  },

  getRoleById(id: number) {
    return request.get<SystemRole>(`/system/roles/${id}`)
  },

  createRole(data: Partial<SystemRole>) {
    return request.post('/system/roles', data)
  },

  updateRole(id: number, data: Partial<SystemRole>) {
    return request.put(`/system/roles/${id}`, data)
  },

  deleteRole(id: number) {
    return request.delete(`/system/roles/${id}`)
  },

  getRolePermissions(roleId: number) {
    return request.get<number[]>(`/system/roles/${roleId}/permissions`)
  },

  updateRolePermissions(roleId: number, permissionIds: number[]) {
    return request.put(`/system/roles/${roleId}/permissions`, { permissionIds })
  },

  getAllPermissions() {
    return request.get<{ id: number; permissionCode: string; permissionName: string; resource: string }[]>('/system/permissions')
  }
}

/**
 * 系统配置 API
 */
export const systemConfigApi = {
  // 获取所有配置
  getAllConfigs() {
    return request.get<SystemConfig[]>('/system/config')
  },

  // 根据类型获取配置
  getConfigsByType(configType: string) {
    return request.get<SystemConfig[]>(`/system/config/type/${configType}`)
  },

  // 获取单个配置值
  getConfigValue(configKey: string) {
    return request.get<string>(`/system/config/${configKey}`)
  },

  // 更新单个配置
  updateConfig(configKey: string, value: string) {
    return request.put(`/system/config/${configKey}`, { value })
  },

  // 批量更新配置
  batchUpdateConfigs(configs: Record<string, string>) {
    return request.put('/system/config/batch', configs)
  },

  // 获取当前学期
  getCurrentSemester() {
    return request.get<string>('/system/config/current-semester')
  }
}

/**
 * 数据管理 API
 */
export const dataManageApi = {
  // 获取备份列表
  getBackupList() {
    return request.get<BackupInfo[]>('/system/data/backups')
  },

  // 创建备份
  createBackup() {
    return request.post<{ fileName: string; message: string }>('/system/data/backup')
  },

  // 删除备份
  deleteBackup(fileName: string) {
    return request.delete(`/system/data/backup/${fileName}`)
  },

  // 恢复备份（异步：立即返回，后台执行）
  restoreBackup(fileName: string) {
    return request.post(`/system/data/backup/restore/${fileName}`)
  },

  // 查询恢复任务状态（用于轮询）
  getRestoreStatus() {
    return request.get<{ status: string; message: string }>('/system/data/backup/restore-status')
  },

  // 获取数据字典
  getDataDictionary() {
    return request.get<DictItem[]>('/system/data/dictionary')
  },

  // 获取字典类型列表
  getDictTypes() {
    return request.get<DictTypeItem[]>('/system/data/dictionary/types')
  },

  // 创建字典类型
  createDictType(data: Partial<DictTypeItem>) {
    return request.post<DictTypeItem>('/system/data/dictionary/types', data)
  },

  // 更新字典类型
  updateDictType(id: number, data: Partial<DictTypeItem>) {
    return request.put(`/system/data/dictionary/types/${id}`, data)
  },

  // 删除字典类型
  deleteDictType(id: number) {
    return request.delete(`/system/data/dictionary/types/${id}`)
  },

  // 获取字典项列表
  getDictItems(dictCode: string) {
    return request.get<DictValueItem[]>(`/system/data/dictionary/items/${dictCode}`)
  },

  // 创建字典项
  createDictItem(data: Partial<DictValueItem>) {
    return request.post<DictValueItem>('/system/data/dictionary/items', data)
  },

  // 更新字典项
  updateDictItem(id: number, data: Partial<DictValueItem>) {
    return request.put(`/system/data/dictionary/items/${id}`, data)
  },

  // 删除字典项
  deleteDictItem(id: number) {
    return request.delete(`/system/data/dictionary/items/${id}`)
  },

  // 导出数据
  exportData(type: string) {
    return request.post<{ message: string; downloadUrl?: string }>(`/system/data/export/${type}`)
  }
}
