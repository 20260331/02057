package com.university.sms.system.service.impl;

import com.university.sms.common.exception.BusinessException;
import com.university.sms.system.dto.RoleDTO;
import com.university.sms.system.dto.RoleUpdateDTO;
import com.university.sms.system.entity.Permission;
import com.university.sms.system.entity.Role;
import com.university.sms.system.entity.RolePermission;
import com.university.sms.system.mapper.PermissionMapper;
import com.university.sms.system.mapper.RoleMapper;
import com.university.sms.system.mapper.RolePermissionMapper;
import com.university.sms.system.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {
    
    private final RoleMapper roleMapper;
    private final PermissionMapper permissionMapper;
    private final RolePermissionMapper rolePermissionMapper;
    
    @Override
    public List<RoleDTO> queryAllRoles() {
        List<Role> roles = roleMapper.selectList(null);
        return roles.stream().map(this::toDTO).collect(Collectors.toList());
    }
    
    @Override
    public RoleDTO getRoleById(Long id) {
        Role role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }
        return toDTO(role);
    }
    
    @Override
    public void updateRole(Long id, RoleUpdateDTO dto) {
        Role role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }
        
        if (dto.getRoleName() != null) role.setRoleName(dto.getRoleName());
        if (dto.getDescription() != null) role.setDescription(dto.getDescription());
        if (dto.getSortOrder() != null) role.setSortOrder(dto.getSortOrder());
        if (dto.getStatus() != null) role.setStatus(dto.getStatus());
        
        roleMapper.updateById(role);
    }
    
    @Override
    public List<Long> getRolePermissionIds(Long roleId) {
        return rolePermissionMapper.selectPermissionIdsByRoleId(roleId);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignPermissions(Long roleId, List<Long> permissionIds) {
        log.info("为角色 {} 分配权限: {}", roleId, permissionIds);
        
        // 验证角色存在
        Role role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }
        log.info("角色信息: {} - {}", role.getRoleCode(), role.getRoleName());
        
        // 删除原有权限
        int deleted = rolePermissionMapper.deleteByRoleId(roleId);
        log.info("删除角色 {} 的原有权限 {} 条", roleId, deleted);
        
        // 如果权限列表为空，直接返回
        if (permissionIds == null || permissionIds.isEmpty()) {
            log.info("权限列表为空，清空角色权限完成");
            return;
        }
        
        // 添加新权限
        for (Long permissionId : permissionIds) {
            RolePermission rp = new RolePermission();
            rp.setRoleId(roleId);
            rp.setPermissionId(permissionId);
            rp.setCreatedAt(LocalDateTime.now());
            int inserted = rolePermissionMapper.insert(rp);
            log.info("为角色 {} 添加权限 {}, 插入结果: {}, 新ID: {}", roleId, permissionId, inserted, rp.getId());
        }
        
        log.info("角色 {} 权限分配完成，共 {} 条", roleId, permissionIds.size());
    }
    
    @Override
    public List<RoleDTO.PermissionDTO> getAllPermissions() {
        List<Permission> permissions = permissionMapper.selectList(null);
        return permissions.stream().map(this::toPermissionDTO).collect(Collectors.toList());
    }
    
    private RoleDTO toDTO(Role role) {
        RoleDTO dto = new RoleDTO();
        dto.setId(role.getId());
        dto.setRoleCode(role.getRoleCode());
        dto.setRoleName(role.getRoleName());
        dto.setDescription(role.getDescription());
        dto.setSortOrder(role.getSortOrder());
        dto.setStatus(role.getStatus());
        return dto;
    }
    
    private RoleDTO.PermissionDTO toPermissionDTO(Permission permission) {
        RoleDTO.PermissionDTO dto = new RoleDTO.PermissionDTO();
        dto.setId(permission.getId());
        dto.setPermissionCode(permission.getPermissionCode());
        dto.setPermissionName(permission.getPermissionName());
        dto.setResource(permission.getResource());
        dto.setAction(permission.getAction());
        dto.setDescription(permission.getDescription());
        return dto;
    }
}
