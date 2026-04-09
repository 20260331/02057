package com.university.sms.system.service;

import com.university.sms.system.dto.*;

import java.util.List;

public interface NotificationService {

    // ===== 模板管理 =====

    List<NotificationTemplateDTO> getAllTemplates();

    List<NotificationTemplateDTO> getTemplatesByCategory(String category);

    NotificationTemplateDTO getTemplateById(Long id);

    void createTemplate(NotificationTemplateSaveDTO dto, Long operatorId);

    void updateTemplate(Long id, NotificationTemplateSaveDTO dto);

    void deleteTemplate(Long id);

    void toggleTemplateStatus(Long id);

    // ===== 通知发送与管理 =====

    void sendNotification(NotificationSendDTO dto, Long senderId, String senderName);

    List<NotificationDTO> getAllNotifications();

    NotificationDTO getNotificationDetail(Long id, Long userId);

    void deleteNotification(Long id);

    // ===== 用户通知（我的通知） =====

    List<NotificationDTO> getMyNotifications(Long userId);

    int getUnreadCount(Long userId);

    void markAsRead(Long notificationId, Long userId);

    void markAllAsRead(Long userId);
}
