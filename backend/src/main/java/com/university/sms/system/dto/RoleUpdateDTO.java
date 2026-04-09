package com.university.sms.system.dto;

import lombok.Data;

/**
 * 角色更新 DTO
 */
@Data
public class RoleUpdateDTO {
    private String roleName;
    private String description;
    private Integer sortOrder;
    private Integer status;
}
