package com.university.sms.security.service;

import com.university.sms.system.entity.User;
import com.university.sms.system.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户详情服务实现
 * 加载用户信息和权限
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    
    private final UserMapper userMapper;
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 查询用户
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }
        
        // 查询用户角色
        List<String> roles = userMapper.selectRoleCodesByUserId(user.getId());
        
        // 查询用户权限
        List<String> permissions = userMapper.selectPermissionCodesByUserId(user.getId());
        
        // 构建权限列表
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        
        // 添加角色权限 (ROLE_ 前缀)
        roles.forEach(role -> 
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role)));
        
        // 添加具体权限
        permissions.forEach(permission -> 
            authorities.add(new SimpleGrantedAuthority(permission)));
        
        // 返回 Spring Security 的 UserDetails
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.getStatus() == User.STATUS_ENABLED,  // enabled
                true,  // accountNonExpired
                true,  // credentialsNonExpired
                user.getLockTime() == null,  // accountNonLocked
                authorities
        );
    }
    
    /**
     * 加载用户角色列表
     */
    public List<String> loadUserRoles(Long userId) {
        return userMapper.selectRoleCodesByUserId(userId);
    }
    
    /**
     * 加载用户权限列表
     */
    public List<String> loadUserPermissions(Long userId) {
        return userMapper.selectPermissionCodesByUserId(userId);
    }
}
