package com.university.sms.system.dto;

import lombok.Data;

import java.util.List;

/**
 * 分配角色请求DTO
 */
@Data
public class AssignRolesDTO {
    
    /**
     * 角色ID列表
     */
    private List<Long> roleIds;
}
