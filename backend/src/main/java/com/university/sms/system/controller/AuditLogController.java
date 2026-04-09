package com.university.sms.system.controller;

import com.university.sms.common.response.PageResult;
import com.university.sms.common.response.Result;
import com.university.sms.security.annotation.RequirePermission;
import com.university.sms.system.dto.AuditLogDTO;
import com.university.sms.system.dto.AuditLogQueryDTO;
import com.university.sms.system.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 审计日志控制器
 */
@RestController
@RequestMapping("/system/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {
    
    private final AuditLogService auditLogService;
    
    /**
     * 分页查询审计日志
     */
    @GetMapping
    @RequirePermission("system:user:manage")
    public Result<PageResult<AuditLogDTO>> queryLogs(AuditLogQueryDTO query) {
        PageResult<AuditLogDTO> result = auditLogService.queryLogs(query);
        return Result.success(result);
    }
}
