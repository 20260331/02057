package com.university.sms.system.service;

import com.university.sms.common.response.PageResult;
import com.university.sms.system.dto.AuditLogDTO;
import com.university.sms.system.dto.AuditLogQueryDTO;

/**
 * 审计日志服务接口
 */
public interface AuditLogService {
    
    /**
     * 分页查询审计日志
     */
    PageResult<AuditLogDTO> queryLogs(AuditLogQueryDTO query);
}
