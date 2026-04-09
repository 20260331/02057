package com.university.sms.system.dto;

import com.university.sms.common.dto.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 审计日志查询 DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AuditLogQueryDTO extends PageQuery {
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 操作类型
     */
    private String operation;
    
    /**
     * 开始日期
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    
    /**
     * 结束日期
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
}
