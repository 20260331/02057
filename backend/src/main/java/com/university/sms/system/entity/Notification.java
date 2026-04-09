package com.university.sms.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_notification")
public class Notification {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long templateId;
    private String title;
    private String content;
    private String category;
    private String priority;
    private String targetType;
    private String targetValue;
    private Long senderId;
    private String senderName;
    private LocalDateTime sendTime;
    private String status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    public static final String STATUS_DRAFT = "DRAFT";
    public static final String STATUS_SENT = "SENT";

    public static final String TARGET_ALL = "ALL";
    public static final String TARGET_ROLE = "ROLE";
    public static final String TARGET_USER = "USER";

    public static final String PRIORITY_LOW = "LOW";
    public static final String PRIORITY_NORMAL = "NORMAL";
    public static final String PRIORITY_HIGH = "HIGH";
    public static final String PRIORITY_URGENT = "URGENT";
}
