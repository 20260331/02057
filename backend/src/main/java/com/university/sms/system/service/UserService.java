package com.university.sms.system.service;

import com.university.sms.common.response.PageResult;
import com.university.sms.system.dto.*;

import java.util.List;

/**
 * 用户服务接口
 */
public interface UserService {
    
    /**
     * 分页查询用户
     */
    PageResult<UserDTO> queryUsers(UserQueryDTO query);
    
    /**
     * 获取用户详情
     */
    UserDTO getUserById(Long id);
    
    /**
     * 创建用户
     */
    Long createUser(UserCreateDTO dto);
    
    /**
     * 更新用户
     */
    void updateUser(Long id, UserUpdateDTO dto);
    
    /**
     * 删除用户
     */
    void deleteUser(Long id);
    
    /**
     * 重置密码
     */
    void resetPassword(Long id);
    
    /**
     * 分配角色
     */
    void assignRoles(Long userId, List<Long> roleIds);
    
    /**
     * 修改密码
     */
    void changePassword(Long userId, String oldPassword, String newPassword);
}
