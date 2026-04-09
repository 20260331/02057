package com.university.sms.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_notification_read")
public class NotificationRead {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long notificationId;
    private Long userId;
    private Integer isRead;
    private LocalDateTime readTime;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
