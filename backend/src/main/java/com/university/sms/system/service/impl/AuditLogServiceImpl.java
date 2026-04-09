package com.university.sms.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.university.sms.common.response.PageResult;
import com.university.sms.system.dto.AuditLogDTO;
import com.university.sms.system.dto.AuditLogQueryDTO;
import com.university.sms.system.entity.AuditLog;
import com.university.sms.system.mapper.AuditLogMapper;
import com.university.sms.system.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 审计日志服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {
    
    private final AuditLogMapper auditLogMapper;
    
    @Override
    public PageResult<AuditLogDTO> queryLogs(AuditLogQueryDTO query) {
        Page<AuditLog> page = new Page<>(query.getPage(), query.getSize());
        
        LambdaQueryWrapper<AuditLog> wrapper = new LambdaQueryWrapper<>();
        
        // 用户名模糊查询
        if (query.getUsername() != null && !query.getUsername().isEmpty()) {
            wrapper.like(AuditLog::getUsername, query.getUsername());
        }
        
        // 操作类型模糊查询
        if (query.getOperation() != null && !query.getOperation().isEmpty()) {
            wrapper.like(AuditLog::getOperation, query.getOperation());
        }
        
        // 时间范围查询
        if (query.getStartDate() != null) {
            wrapper.ge(AuditLog::getCreatedAt, query.getStartDate().atStartOfDay());
        }
        if (query.getEndDate() != null) {
            wrapper.le(AuditLog::getCreatedAt, query.getEndDate().atTime(LocalTime.MAX));
        }
        
        // 按时间倒序
        wrapper.orderByDesc(AuditLog::getCreatedAt);
        
        page = auditLogMapper.selectPage(page, wrapper);
        
        List<AuditLogDTO> records = page.getRecords().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        
        return new PageResult<>(page.getCurrent(), page.getSize(), page.getTotal(), records);
    }
    
    private AuditLogDTO toDTO(AuditLog log) {
        AuditLogDTO dto = new AuditLogDTO();
        dto.setId(log.getId());
        dto.setUserId(log.getUserId());
        dto.setUsername(log.getUsername());
        dto.setModule(log.getModule());
        dto.setOperation(log.getOperation());
        dto.setMethod(log.getMethod());
        dto.setRequestUrl(log.getRequestUrl());
        dto.setRequestMethod(log.getRequestMethod());
        dto.setIpAddress(log.getIpAddress());
        dto.setExecutionTime(log.getExecutionTime());
        dto.setStatus(log.getStatus());
        dto.setCreatedAt(log.getCreatedAt());
        return dto;
    }
}
