package com.university.sms.system.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class NotificationDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long templateId;
    private String templateName;
    private String title;
    private String content;
    private String category;
    private String categoryName;
    private String priority;
    private String priorityName;
    private String targetType;
    private String targetTypeName;
    private String targetValue;
    private Long senderId;
    private String senderName;
    private LocalDateTime sendTime;
    private String status;
    private Boolean isRead;
    private LocalDateTime readTime;

    public static String getPriorityName(String priority) {
        if (priority == null) return "";
        return switch (priority) {
            case "LOW" -> "低";
            case "NORMAL" -> "普通";
            case "HIGH" -> "高";
            case "URGENT" -> "紧急";
            default -> priority;
        };
    }

    public static String getTargetTypeName(String targetType) {
        if (targetType == null) return "";
        return switch (targetType) {
            case "ALL" -> "全体用户";
            case "ROLE" -> "按角色";
            case "USER" -> "指定用户";
            default -> targetType;
        };
    }
}
