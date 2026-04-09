package com.university.sms.system.controller;

import com.university.sms.common.response.Result;
import com.university.sms.security.annotation.RequirePermission;
import com.university.sms.system.dto.*;
import com.university.sms.system.service.NotificationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // ===== 模板管理（管理员） =====

    @GetMapping("/templates")
    @RequirePermission("system:manage")
    public Result<List<NotificationTemplateDTO>> getAllTemplates() {
        return Result.success(notificationService.getAllTemplates());
    }

    @GetMapping("/templates/category/{category}")
    @RequirePermission("system:manage")
    public Result<List<NotificationTemplateDTO>> getTemplatesByCategory(@PathVariable String category) {
        return Result.success(notificationService.getTemplatesByCategory(category));
    }

    @GetMapping("/templates/{id}")
    @RequirePermission("system:manage")
    public Result<NotificationTemplateDTO> getTemplateById(@PathVariable Long id) {
        return Result.success(notificationService.getTemplateById(id));
    }

    @PostMapping("/templates")
    @RequirePermission("system:manage")
    public Result<Void> createTemplate(@Valid @RequestBody NotificationTemplateSaveDTO dto,
                                       HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        notificationService.createTemplate(dto, userId);
        return Result.success();
    }

    @PutMapping("/templates/{id}")
    @RequirePermission("system:manage")
    public Result<Void> updateTemplate(@PathVariable Long id,
                                       @Valid @RequestBody NotificationTemplateSaveDTO dto) {
        notificationService.updateTemplate(id, dto);
        return Result.success();
    }

    @DeleteMapping("/templates/{id}")
    @RequirePermission("system:manage")
    public Result<Void> deleteTemplate(@PathVariable Long id) {
        notificationService.deleteTemplate(id);
        return Result.success();
    }

    @PostMapping("/templates/{id}/toggle")
    @RequirePermission("system:manage")
    public Result<Void> toggleTemplateStatus(@PathVariable Long id) {
        notificationService.toggleTemplateStatus(id);
        return Result.success();
    }

    // ===== 通知发送与管理（管理员） =====

    @PostMapping("/send")
    @RequirePermission("system:manage")
    public Result<Void> sendNotification(@Valid @RequestBody NotificationSendDTO dto,
                                         HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String username = (String) request.getAttribute("username");
        notificationService.sendNotification(dto, userId, username);
        return Result.success();
    }

    @GetMapping("/manage/list")
    @RequirePermission("system:manage")
    public Result<List<NotificationDTO>> getAllNotifications() {
        return Result.success(notificationService.getAllNotifications());
    }

    @DeleteMapping("/{id}")
    @RequirePermission("system:manage")
    public Result<Void> deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return Result.success();
    }

    // ===== 我的通知（所有登录用户） =====

    @GetMapping("/my")
    public Result<List<NotificationDTO>> getMyNotifications(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(notificationService.getMyNotifications(userId));
    }

    @GetMapping("/my/unread-count")
    public Result<Map<String, Integer>> getUnreadCount(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        int count = notificationService.getUnreadCount(userId);
        return Result.success(Map.of("count", count));
    }

    @GetMapping("/{id}")
    public Result<NotificationDTO> getNotificationDetail(@PathVariable Long id,
                                                          HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(notificationService.getNotificationDetail(id, userId));
    }

    @PostMapping("/{id}/read")
    public Result<Void> markAsRead(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        notificationService.markAsRead(id, userId);
        return Result.success();
    }

    @PostMapping("/my/read-all")
    public Result<Void> markAllAsRead(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        notificationService.markAllAsRead(userId);
        return Result.success();
    }
}
