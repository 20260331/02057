package com.university.sms.system.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class NotificationTemplateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String templateCode;
    private String templateName;
    private String category;
    private String categoryName;
    private String subject;
    private String content;
    private List<String> variables;
    private String channel;
    private String channelName;
    private Integer status;
    private Integer isSystem;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static String getCategoryName(String category) {
        if (category == null) return "";
        return switch (category) {
            case "ACADEMIC" -> "教务通知";
            case "COURSE" -> "选课通知";
            case "GRADE" -> "成绩通知";
            case "LEAVE" -> "请假通知";
            case "SYSTEM" -> "系统通知";
            default -> category;
        };
    }

    public static String getChannelName(String channel) {
        if (channel == null) return "";
        return switch (channel) {
            case "SITE" -> "站内信";
            case "EMAIL" -> "邮件";
            case "ALL" -> "全渠道";
            default -> channel;
        };
    }
}
