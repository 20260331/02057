package com.university.sms.security.service.impl;

import com.university.sms.common.exception.BusinessException;
import com.university.sms.common.util.MaskUtils;
import com.university.sms.security.dto.EncryptionKeyResponse;
import com.university.sms.security.dto.LoginRequest;
import com.university.sms.security.dto.LoginResponse;
import com.university.sms.security.service.AuthService;
import com.university.sms.security.service.UserDetailsServiceImpl;
import com.university.sms.security.util.JwtUtils;
import com.university.sms.security.util.RsaCryptoUtils;
import com.university.sms.system.entity.User;
import com.university.sms.system.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 认证服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    
    private final UserMapper userMapper;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;
    private final UserDetailsServiceImpl userDetailsService;
    private final RsaCryptoUtils rsaCryptoUtils;
    
    @Value("${app.security.max-login-attempts:5}")
    private int maxLoginAttempts;
    
    @Value("${app.security.lock-duration-minutes:30}")
    private int lockDurationMinutes;
    
    @Override
    @Transactional
    public LoginResponse login(LoginRequest request, String clientIp) {
        String username = request.getUsername();
        String plainPassword = rsaCryptoUtils.decryptFromBase64(request.getEncryptedPassword());
        
        // 查询用户
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }
        
        // 检查用户状态
        if (user.getStatus() == User.STATUS_DISABLED) {
            throw new BusinessException("账户已被禁用，请联系管理员");
        }
        
        // 检查账户是否被锁定
        if (isAccountLocked(user)) {
            throw new BusinessException("账户已被锁定，请" + lockDurationMinutes + "分钟后再试");
        }
        
        // 验证密码
        if (!passwordEncoder.matches(plainPassword, user.getPassword())) {
            handleLoginFailure(user);
            throw new BusinessException("用户名或密码错误");
        }
        
        // 登录成功，更新登录信息
        userMapper.updateLastLoginInfo(user.getId(), LocalDateTime.now(), clientIp);
        
        // 获取用户角色和权限
        List<String> roles = userDetailsService.loadUserRoles(user.getId());
        List<String> permissions = userDetailsService.loadUserPermissions(user.getId());
        
        // 生成 token
        String accessToken = jwtUtils.generateAccessToken(user.getId(), user.getUsername(), roles);
        String refreshToken = jwtUtils.generateRefreshToken(user.getId(), user.getUsername());
        
        log.info("用户登录成功: {}, IP: {}", username, clientIp);
        
        return buildLoginResponse(user, roles, permissions, accessToken, refreshToken);
    }
    
    @Override
    public void logout(String token) {
        // JWT 是无状态的，登出只需要客户端删除 token
        // 如果需要服务端登出，可以将 token 加入黑名单（使用 Redis）
        log.info("用户登出");
    }
    
    @Override
    public LoginResponse refreshToken(String refreshToken) {
        // 验证刷新令牌
        if (!jwtUtils.validateToken(refreshToken) || !jwtUtils.isRefreshToken(refreshToken)) {
            throw new BusinessException("无效的刷新令牌");
        }
        
        Long userId = jwtUtils.getUserId(refreshToken);
        String username = jwtUtils.getUsername(refreshToken);
        
        // 查询用户
        User user = userMapper.selectByUsername(username);
        if (user == null || user.getStatus() == User.STATUS_DISABLED) {
            throw new BusinessException("用户不存在或已被禁用");
        }
        
        // 获取用户角色和权限
        List<String> roles = userDetailsService.loadUserRoles(userId);
        List<String> permissions = userDetailsService.loadUserPermissions(userId);
        
        // 生成新的 token
        String newAccessToken = jwtUtils.generateAccessToken(userId, username, roles);
        String newRefreshToken = jwtUtils.generateRefreshToken(userId, username);
        
        return buildLoginResponse(user, roles, permissions, newAccessToken, newRefreshToken);
    }
    
    @Override
    public LoginResponse getCurrentUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        List<String> roles = userDetailsService.loadUserRoles(userId);
        List<String> permissions = userDetailsService.loadUserPermissions(userId);
        
        return buildLoginResponse(user, roles, permissions, null, null);
    }
    
    @Override
    public List<String> getUserPermissions(Long userId) {
        return userDetailsService.loadUserPermissions(userId);
    }

    @Override
    public EncryptionKeyResponse getLoginEncryptionKey() {
        return new EncryptionKeyResponse("RSA-OAEP-256", rsaCryptoUtils.getPublicKeyBase64());
    }
    
    /**
     * 检查账户是否被锁定
     */
    private boolean isAccountLocked(User user) {
        if (user.getLockTime() == null) {
            return false;
        }
        LocalDateTime unlockTime = user.getLockTime().plusMinutes(lockDurationMinutes);
        return LocalDateTime.now().isBefore(unlockTime);
    }
    
    /**
     * 处理登录失败
     */
    private void handleLoginFailure(User user) {
        int failCount = user.getLoginFailCount() + 1;
        LocalDateTime lockTime = null;
        
        if (failCount >= maxLoginAttempts) {
            lockTime = LocalDateTime.now();
            log.warn("用户 {} 登录失败次数过多，账户已锁定", user.getUsername());
        }
        
        userMapper.updateLoginFailInfo(user.getId(), failCount, lockTime);
    }
    
    /**
     * 构建登录响应
     */
    private LoginResponse buildLoginResponse(User user, List<String> roles, 
                                             List<String> permissions,
                                             String accessToken, String refreshToken) {
        LoginResponse.UserInfo userInfo = LoginResponse.UserInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .phone(MaskUtils.maskPhone(user.getPhone()))
                .email(MaskUtils.maskEmail(user.getEmail()))
                .avatar(user.getAvatar())
                .roles(roles)
                .build();
        
        return LoginResponse.builder()
                .token(accessToken)
                .refreshToken(refreshToken)
                .userInfo(userInfo)
                .permissions(permissions)
                .build();
    }
}
