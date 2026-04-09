package com.university.sms.system.service;

import com.university.sms.system.dto.RoleDTO;
import com.university.sms.system.dto.RoleUpdateDTO;

import java.util.List;

/**
 * 角色服务接口
 */
public interface RoleService {
    
    /**
     * 查询所有角色
     */
    List<RoleDTO> queryAllRoles();
    
    /**
     * 获取角色详情
     */
    RoleDTO getRoleById(Long id);
    
    /**
     * 更新角色
     */
    void updateRole(Long id, RoleUpdateDTO dto);
    
    /**
     * 获取角色的权限ID列表
     */
    List<Long> getRolePermissionIds(Long roleId);
    
    /**
     * 配置角色权限
     */
    void assignPermissions(Long roleId, List<Long> permissionIds);
    
    /**
     * 获取所有权限列表
     */
    List<RoleDTO.PermissionDTO> getAllPermissions();
}
