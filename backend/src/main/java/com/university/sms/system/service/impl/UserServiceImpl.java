package com.university.sms.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.university.sms.common.exception.BusinessException;
import com.university.sms.common.response.PageResult;
import com.university.sms.system.dto.*;
import com.university.sms.system.entity.User;
import com.university.sms.system.entity.UserRole;
import com.university.sms.system.mapper.UserMapper;
import com.university.sms.system.mapper.UserRoleMapper;
import com.university.sms.system.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    
    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final PasswordEncoder passwordEncoder;
    
    private static final String DEFAULT_PASSWORD = "123456";
    
    @Override
    public PageResult<UserDTO> queryUsers(UserQueryDTO query) {
        Page<User> page = new Page<>(query.getPage(), query.getSize());
        
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (query.getUsername() != null && !query.getUsername().isEmpty()) {
            wrapper.like(User::getUsername, query.getUsername());
        }
        if (query.getRealName() != null && !query.getRealName().isEmpty()) {
            wrapper.like(User::getRealName, query.getRealName());
        }
        if (query.getStatus() != null) {
            wrapper.eq(User::getStatus, query.getStatus());
        }
        wrapper.orderByDesc(User::getCreatedAt);
        
        page = userMapper.selectPage(page, wrapper);
        
        List<UserDTO> records = page.getRecords().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        
        return new PageResult<>(page.getCurrent(), page.getSize(), page.getTotal(), records);
    }
    
    @Override
    public UserDTO getUserById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return toDTO(user);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createUser(UserCreateDTO dto) {
        // 检查用户名是否存在
        User existing = userMapper.selectByUsername(dto.getUsername());
        if (existing != null) {
            throw new BusinessException("用户名已存在");
        }
        
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
        user.setRealName(dto.getRealName());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setStatus(User.STATUS_ENABLED);
        user.setLoginFailCount(0);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setDeleted(0);
        
        userMapper.insert(user);
        
        // 分配角色
        if (dto.getRoleIds() != null && !dto.getRoleIds().isEmpty()) {
            assignRoles(user.getId(), dto.getRoleIds());
        }
        
        return user.getId();
    }
    
    @Override
    public void updateUser(Long id, UserUpdateDTO dto) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        if (dto.getRealName() != null) user.setRealName(dto.getRealName());
        if (dto.getPhone() != null) user.setPhone(dto.getPhone());
        if (dto.getEmail() != null) user.setEmail(dto.getEmail());
        if (dto.getStatus() != null) user.setStatus(dto.getStatus());
        
        userMapper.updateById(user);
    }
    
    @Override
    public void deleteUser(Long id) {
        userMapper.deleteById(id);
    }
    
    @Override
    public void resetPassword(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        user.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
        user.setLoginFailCount(0);
        user.setLockTime(null);
        userMapper.updateById(user);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignRoles(Long userId, List<Long> roleIds) {
        // 删除原有角色
        userRoleMapper.deleteByUserId(userId);
        
        // 添加新角色
        for (Long roleId : roleIds) {
            UserRole userRole = new UserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);
            userRole.setCreatedAt(LocalDateTime.now());
            userRoleMapper.insert(userRole);
        }
    }
    
    @Override
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 验证原密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("原密码错误");
        }
        
        // 更新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
    }
    
    private UserDTO toDTO(User user) {
        List<String> roles = userMapper.selectRoleCodesByUserId(user.getId());
        return UserDTO.fromUser(user, roles);
    }
}
