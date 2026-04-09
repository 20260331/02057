package com.university.sms.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.university.sms.system.entity.AuditLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 审计日志 Mapper 接口
 */
@Mapper
public interface AuditLogMapper extends BaseMapper<AuditLog> {
}
