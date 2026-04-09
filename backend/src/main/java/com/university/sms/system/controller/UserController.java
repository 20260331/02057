package com.university.sms.system.controller;

import com.university.sms.common.response.PageResult;
import com.university.sms.common.response.Result;
import com.university.sms.security.annotation.RequirePermission;
import com.university.sms.security.util.SecurityUtils;
import com.university.sms.system.dto.*;
import com.university.sms.system.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理控制器
 */
@RestController
@RequestMapping("/system/users")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    
    /**
     * 分页查询用户
     */
    @GetMapping
    @RequirePermission("system:user:manage")
    public Result<PageResult<UserDTO>> queryUsers(UserQueryDTO query) {
        PageResult<UserDTO> result = userService.queryUsers(query);
        return Result.success(result);
    }
    
    /**
     * 获取用户详情
     */
    @GetMapping("/{id}")
    @RequirePermission("system:user:manage")
    public Result<UserDTO> getUserById(@PathVariable Long id) {
        UserDTO user = userService.getUserById(id);
        return Result.success(user);
    }
    
    /**
     * 创建用户
     */
    @PostMapping
    @RequirePermission("system:user:manage")
    public Result<Long> createUser(@Valid @RequestBody UserCreateDTO dto) {
        Long id = userService.createUser(dto);
        return Result.success(id);
    }
    
    /**
     * 更新用户
     */
    @PutMapping("/{id}")
    @RequirePermission("system:user:manage")
    public Result<Void> updateUser(@PathVariable Long id, @RequestBody UserUpdateDTO dto) {
        userService.updateUser(id, dto);
        return Result.success();
    }
    
    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    @RequirePermission("system:user:manage")
    public Result<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success();
    }
    
    /**
     * 重置密码
     */
    @PostMapping("/{id}/reset-password")
    @RequirePermission("system:user:manage")
    public Result<Void> resetPassword(@PathVariable Long id) {
        userService.resetPassword(id);
        return Result.success();
    }
    
    /**
     * 分配角色
     */
    @PostMapping("/{id}/roles")
    @RequirePermission("system:user:manage")
    public Result<Void> assignRoles(@PathVariable Long id, @RequestBody AssignRolesDTO dto) {
        userService.assignRoles(id, dto.getRoleIds());
        return Result.success();
    }
    
    /**
     * 修改密码（当前登录用户）
     */
    @PostMapping("/change-password")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        userService.changePassword(userId, dto.getOldPassword(), dto.getNewPassword());
        return Result.success();
    }
}
