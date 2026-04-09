package com.university.sms.system.dto;

import lombok.Data;

/**
 * 角色 DTO
 */
@Data
public class RoleDTO {
    private Long id;
    private String roleCode;
    private String roleName;
    private String description;
    private Integer sortOrder;
    private Integer status;
    
    /**
     * 权限 DTO
     */
    @Data
    public static class PermissionDTO {
        private Long id;
        private String permissionCode;
        private String permissionName;
        private String resource;
        private String action;
        private String description;
    }
}
