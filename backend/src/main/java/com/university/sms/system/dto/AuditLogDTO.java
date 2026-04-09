package com.university.sms.system.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 审计日志 DTO
 */
@Data
public class AuditLogDTO {
    private Long id;
    private Long userId;
    private String username;
    private String module;
    private String operation;
    private String method;
    private String requestUrl;
    private String requestMethod;
    private String ipAddress;
    private Long executionTime;
    private Integer status;
    private LocalDateTime createdAt;
}
