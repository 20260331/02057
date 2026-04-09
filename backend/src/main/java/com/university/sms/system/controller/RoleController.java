package com.university.sms.system.controller;

import com.university.sms.common.response.Result;
import com.university.sms.security.annotation.RequirePermission;
import com.university.sms.system.dto.RoleDTO;
import com.university.sms.system.dto.RoleUpdateDTO;
import com.university.sms.system.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/system/roles")
@RequiredArgsConstructor
public class RoleController {
    
    private final RoleService roleService;
    
    /**
     * 查询所有角色
     */
    @GetMapping
    @RequirePermission("system:user:manage")
    public Result<List<RoleDTO>> queryRoles() {
        List<RoleDTO> roles = roleService.queryAllRoles();
        return Result.success(roles);
    }
    
    /**
     * 获取角色详情
     */
    @GetMapping("/{id}")
    @RequirePermission("system:user:manage")
    public Result<RoleDTO> getRoleById(@PathVariable Long id) {
        RoleDTO role = roleService.getRoleById(id);
        return Result.success(role);
    }
    
    /**
     * 更新角色
     */
    @PutMapping("/{id}")
    @RequirePermission("system:user:manage")
    public Result<Void> updateRole(@PathVariable Long id, @RequestBody RoleUpdateDTO dto) {
        roleService.updateRole(id, dto);
        return Result.success();
    }
    
    /**
     * 获取角色的权限ID列表
     */
    @GetMapping("/{id}/permissions")
    @RequirePermission("system:user:manage")
    public Result<List<Long>> getRolePermissions(@PathVariable Long id) {
        List<Long> permissionIds = roleService.getRolePermissionIds(id);
        return Result.success(permissionIds);
    }
    
    /**
     * 配置角色权限
     */
    @PostMapping("/{id}/permissions")
    @RequirePermission("system:user:manage")
    public Result<Void> assignPermissions(@PathVariable Long id, @RequestBody List<Long> permissionIds) {
        log.info("接收到权限配置请求，角色ID: {}, 权限IDs: {}", id, permissionIds);
        if (permissionIds == null || permissionIds.isEmpty()) {
            log.warn("权限ID列表为空");
        }
        roleService.assignPermissions(id, permissionIds);
        return Result.success();
    }
    
    /**
     * 获取所有权限列表
     */
    @GetMapping("/permissions/all")
    @RequirePermission("system:user:manage")
    public Result<List<RoleDTO.PermissionDTO>> getAllPermissions() {
        List<RoleDTO.PermissionDTO> permissions = roleService.getAllPermissions();
        return Result.success(permissions);
    }
}
