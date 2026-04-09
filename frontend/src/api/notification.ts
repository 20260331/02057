import { request } from './request'

// 通知模板
export interface NotificationTemplate {
  id: number
  templateCode: string
  templateName: string
  category: string
  categoryName: string
  subject: string
  content: string
  variables: string[]
  channel: string
  channelName: string
  status: number
  isSystem: number
  createdAt: string
  updatedAt: string
}

// 通知模板保存
export interface NotificationTemplateSave {
  templateCode: string
  templateName: string
  category: string
  subject: string
  content: string
  variables: string[]
  channel: string
}

// 通知记录
export interface NotificationInfo {
  id: number
  templateId: number | null
  templateName: string
  title: string
  content: string
  category: string
  categoryName: string
  priority: string
  priorityName: string
  targetType: string
  targetTypeName: string
  targetValue: string
  senderId: number
  senderName: string
  sendTime: string
  status: string
  isRead: boolean
  readTime: string | null
}

// 通知发送
export interface NotificationSend {
  templateId?: number
  title?: string
  content?: string
  category?: string
  priority?: string
  targetType: string
  targetValue?: string
  variables?: Record<string, string>
}

export const notificationApi = {
  // ===== 模板管理 =====
  getTemplates() {
    return request.get<NotificationTemplate[]>('/notifications/templates')
  },

  getTemplatesByCategory(category: string) {
    return request.get<NotificationTemplate[]>(`/notifications/templates/category/${category}`)
  },

  getTemplateById(id: number) {
    return request.get<NotificationTemplate>(`/notifications/templates/${id}`)
  },

  createTemplate(data: NotificationTemplateSave) {
    return request.post<void>('/notifications/templates', data)
  },

  updateTemplate(id: number, data: NotificationTemplateSave) {
    return request.put<void>(`/notifications/templates/${id}`, data)
  },

  deleteTemplate(id: number) {
    return request.delete<void>(`/notifications/templates/${id}`)
  },

  toggleTemplateStatus(id: number) {
    return request.post<void>(`/notifications/templates/${id}/toggle`)
  },

  // ===== 通知发送与管理 =====
  sendNotification(data: NotificationSend) {
    return request.post<void>('/notifications/send', data)
  },

  getAllNotifications() {
    return request.get<NotificationInfo[]>('/notifications/manage/list')
  },

  deleteNotification(id: number) {
    return request.delete<void>(`/notifications/${id}`)
  },

  // ===== 我的通知 =====
  getMyNotifications() {
    return request.get<NotificationInfo[]>('/notifications/my')
  },

  getUnreadCount() {
    return request.get<{ count: number }>('/notifications/my/unread-count')
  },

  getNotificationDetail(id: number) {
    return request.get<NotificationInfo>(`/notifications/${id}`)
  },

  markAsRead(id: number) {
    return request.post<void>(`/notifications/${id}/read`)
  },

  markAllAsRead() {
    return request.post<void>('/notifications/my/read-all')
  }
}
