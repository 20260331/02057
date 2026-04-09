package com.university.sms.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.university.sms.common.exception.BusinessException;
import com.university.sms.system.dto.*;
import com.university.sms.system.entity.Notification;
import com.university.sms.system.entity.NotificationRead;
import com.university.sms.system.entity.NotificationTemplate;
import com.university.sms.system.mapper.NotificationMapper;
import com.university.sms.system.mapper.NotificationReadMapper;
import com.university.sms.system.mapper.NotificationTemplateMapper;
import com.university.sms.system.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationTemplateMapper templateMapper;
    private final NotificationMapper notificationMapper;
    private final NotificationReadMapper readMapper;
    private final ObjectMapper objectMapper;

    private static final Pattern VAR_PATTERN = Pattern.compile("\\$\\{(\\w+)}");

    // ===== 模板管理 =====

    @Override
    public List<NotificationTemplateDTO> getAllTemplates() {
        List<NotificationTemplate> templates = templateMapper.selectList(
                new LambdaQueryWrapper<NotificationTemplate>().orderByAsc(NotificationTemplate::getCategory));
        return templates.stream().map(this::toTemplateDTO).toList();
    }

    @Override
    public List<NotificationTemplateDTO> getTemplatesByCategory(String category) {
        List<NotificationTemplate> templates = templateMapper.selectByCategory(category);
        return templates.stream().map(this::toTemplateDTO).toList();
    }

    @Override
    public NotificationTemplateDTO getTemplateById(Long id) {
        NotificationTemplate template = templateMapper.selectById(id);
        if (template == null) {
            throw new BusinessException("模板不存在");
        }
        return toTemplateDTO(template);
    }

    @Override
    @Transactional
    public void createTemplate(NotificationTemplateSaveDTO dto, Long operatorId) {
        NotificationTemplate existing = templateMapper.selectByCode(dto.getTemplateCode());
        if (existing != null) {
            throw new BusinessException("模板编码已存在: " + dto.getTemplateCode());
        }

        NotificationTemplate template = new NotificationTemplate();
        template.setTemplateCode(dto.getTemplateCode());
        template.setTemplateName(dto.getTemplateName());
        template.setCategory(dto.getCategory());
        template.setSubject(dto.getSubject());
        template.setContent(dto.getContent());
        template.setVariables(toJsonArray(dto.getVariables()));
        template.setChannel(dto.getChannel() != null ? dto.getChannel() : "SITE");
        template.setStatus(1);
        template.setIsSystem(0);
        template.setCreatedBy(operatorId);

        templateMapper.insert(template);
        log.info("创建通知模板: {} ({})", dto.getTemplateName(), dto.getTemplateCode());
    }

    @Override
    @Transactional
    public void updateTemplate(Long id, NotificationTemplateSaveDTO dto) {
        NotificationTemplate template = templateMapper.selectById(id);
        if (template == null) {
            throw new BusinessException("模板不存在");
        }

        NotificationTemplate codeCheck = templateMapper.selectByCode(dto.getTemplateCode());
        if (codeCheck != null && !codeCheck.getId().equals(id)) {
            throw new BusinessException("模板编码已被其他模板使用");
        }

        template.setTemplateCode(dto.getTemplateCode());
        template.setTemplateName(dto.getTemplateName());
        template.setCategory(dto.getCategory());
        template.setSubject(dto.getSubject());
        template.setContent(dto.getContent());
        template.setVariables(toJsonArray(dto.getVariables()));
        if (dto.getChannel() != null) {
            template.setChannel(dto.getChannel());
        }

        templateMapper.updateById(template);
        log.info("更新通知模板: id={}", id);
    }

    @Override
    @Transactional
    public void deleteTemplate(Long id) {
        NotificationTemplate template = templateMapper.selectById(id);
        if (template == null) {
            throw new BusinessException("模板不存在");
        }
        if (template.getIsSystem() != null && template.getIsSystem() == 1) {
            throw new BusinessException("系统内置模板不允许删除");
        }
        templateMapper.deleteById(id);
        log.info("删除通知模板: id={}", id);
    }

    @Override
    @Transactional
    public void toggleTemplateStatus(Long id) {
        NotificationTemplate template = templateMapper.selectById(id);
        if (template == null) {
            throw new BusinessException("模板不存在");
        }
        template.setStatus(template.getStatus() == 1 ? 0 : 1);
        templateMapper.updateById(template);
    }

    // ===== 通知发送 =====

    @Override
    @Transactional
    public void sendNotification(NotificationSendDTO dto, Long senderId, String senderName) {
        String title;
        String content;
        String category;
        Long templateId = null;

        if (dto.getTemplateId() != null) {
            NotificationTemplate template = templateMapper.selectById(dto.getTemplateId());
            if (template == null) {
                throw new BusinessException("通知模板不存在");
            }
            if (template.getStatus() != 1) {
                throw new BusinessException("通知模板已禁用");
            }
            templateId = template.getId();
            category = template.getCategory();

            Map<String, String> vars = dto.getVariables() != null ? dto.getVariables() : Collections.emptyMap();
            title = replaceVariables(template.getSubject(), vars);
            content = replaceVariables(template.getContent(), vars);
        } else {
            if (dto.getTitle() == null || dto.getTitle().isBlank()) {
                throw new BusinessException("自定义通知标题不能为空");
            }
            if (dto.getContent() == null || dto.getContent().isBlank()) {
                throw new BusinessException("自定义通知内容不能为空");
            }
            title = dto.getTitle();
            content = dto.getContent();
            category = dto.getCategory() != null ? dto.getCategory() : "SYSTEM";
        }

        Notification notification = new Notification();
        notification.setTemplateId(templateId);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setCategory(category);
        notification.setPriority(dto.getPriority() != null ? dto.getPriority() : Notification.PRIORITY_NORMAL);
        notification.setTargetType(dto.getTargetType());
        notification.setTargetValue(dto.getTargetValue());
        notification.setSenderId(senderId);
        notification.setSenderName(senderName);
        notification.setSendTime(LocalDateTime.now());
        notification.setStatus(Notification.STATUS_SENT);

        notificationMapper.insert(notification);
        log.info("发送通知: [{}] 目标: {}={}", title, dto.getTargetType(), dto.getTargetValue());
    }

    @Override
    public List<NotificationDTO> getAllNotifications() {
        List<Notification> list = notificationMapper.selectList(
                new LambdaQueryWrapper<Notification>().orderByDesc(Notification::getSendTime));
        return list.stream().map(n -> toNotificationDTO(n, null)).toList();
    }

    @Override
    public NotificationDTO getNotificationDetail(Long id, Long userId) {
        Notification notification = notificationMapper.selectById(id);
        if (notification == null) {
            throw new BusinessException("通知不存在");
        }

        if (userId != null) {
            markAsRead(id, userId);
        }

        NotificationRead readRecord = userId != null ? readMapper.selectByNotificationAndUser(id, userId) : null;
        return toNotificationDTO(notification, readRecord);
    }

    @Override
    @Transactional
    public void deleteNotification(Long id) {
        notificationMapper.deleteById(id);
        readMapper.delete(new LambdaQueryWrapper<NotificationRead>()
                .eq(NotificationRead::getNotificationId, id));
    }

    // ===== 用户通知 =====

    @Override
    public List<NotificationDTO> getMyNotifications(Long userId) {
        List<Notification> notifications = notificationMapper.selectByTargetUser(userId);
        return notifications.stream().map(n -> {
            NotificationRead readRecord = readMapper.selectByNotificationAndUser(n.getId(), userId);
            return toNotificationDTO(n, readRecord);
        }).toList();
    }

    @Override
    public int getUnreadCount(Long userId) {
        return readMapper.countUnread(userId);
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        NotificationRead existing = readMapper.selectByNotificationAndUser(notificationId, userId);
        if (existing == null) {
            NotificationRead record = new NotificationRead();
            record.setNotificationId(notificationId);
            record.setUserId(userId);
            record.setIsRead(1);
            record.setReadTime(LocalDateTime.now());
            readMapper.insert(record);
        } else if (existing.getIsRead() == 0) {
            existing.setIsRead(1);
            existing.setReadTime(LocalDateTime.now());
            readMapper.updateById(existing);
        }
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> notifications = notificationMapper.selectByTargetUser(userId);
        for (Notification n : notifications) {
            markAsRead(n.getId(), userId);
        }
    }

    // ===== 私有方法 =====

    private NotificationTemplateDTO toTemplateDTO(NotificationTemplate entity) {
        NotificationTemplateDTO dto = new NotificationTemplateDTO();
        dto.setId(entity.getId());
        dto.setTemplateCode(entity.getTemplateCode());
        dto.setTemplateName(entity.getTemplateName());
        dto.setCategory(entity.getCategory());
        dto.setCategoryName(NotificationTemplateDTO.getCategoryName(entity.getCategory()));
        dto.setSubject(entity.getSubject());
        dto.setContent(entity.getContent());
        dto.setChannel(entity.getChannel());
        dto.setChannelName(NotificationTemplateDTO.getChannelName(entity.getChannel()));
        dto.setStatus(entity.getStatus());
        dto.setIsSystem(entity.getIsSystem());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setVariables(parseJsonArray(entity.getVariables()));
        return dto;
    }

    private NotificationDTO toNotificationDTO(Notification entity, NotificationRead readRecord) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(entity.getId());
        dto.setTemplateId(entity.getTemplateId());
        dto.setTitle(entity.getTitle());
        dto.setContent(entity.getContent());
        dto.setCategory(entity.getCategory());
        dto.setCategoryName(NotificationTemplateDTO.getCategoryName(entity.getCategory()));
        dto.setPriority(entity.getPriority());
        dto.setPriorityName(NotificationDTO.getPriorityName(entity.getPriority()));
        dto.setTargetType(entity.getTargetType());
        dto.setTargetTypeName(NotificationDTO.getTargetTypeName(entity.getTargetType()));
        dto.setTargetValue(entity.getTargetValue());
        dto.setSenderId(entity.getSenderId());
        dto.setSenderName(entity.getSenderName());
        dto.setSendTime(entity.getSendTime());
        dto.setStatus(entity.getStatus());

        if (readRecord != null) {
            dto.setIsRead(readRecord.getIsRead() == 1);
            dto.setReadTime(readRecord.getReadTime());
        } else {
            dto.setIsRead(false);
        }

        if (entity.getTemplateId() != null) {
            NotificationTemplate tpl = templateMapper.selectById(entity.getTemplateId());
            if (tpl != null) {
                dto.setTemplateName(tpl.getTemplateName());
            }
        }

        return dto;
    }

    private String replaceVariables(String template, Map<String, String> variables) {
        if (template == null || variables == null || variables.isEmpty()) {
            return template;
        }
        Matcher matcher = VAR_PATTERN.matcher(template);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String varName = matcher.group(1);
            String replacement = variables.getOrDefault(varName, "${" + varName + "}");
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private List<String> parseJsonArray(String json) {
        if (json == null || json.isBlank()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            log.warn("解析变量列表失败: {}", json, e);
            return new ArrayList<>();
        }
    }

    private String toJsonArray(List<String> list) {
        if (list == null || list.isEmpty()) {
            return "[]";
        }
        try {
            return objectMapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }
}
