package com.university.sms.security.service;

import com.university.sms.security.dto.LoginRequest;
import com.university.sms.security.dto.LoginResponse;
import com.university.sms.security.dto.EncryptionKeyResponse;

import java.util.List;

/**
 * 认证服务接口
 */
public interface AuthService {
    
    /**
     * 用户登录
     * @param request 登录请求
     * @param clientIp 客户端IP
     * @return 登录响应
     */
    LoginResponse login(LoginRequest request, String clientIp);
    
    /**
     * 用户登出
     * @param token 访问令牌
     */
    void logout(String token);
    
    /**
     * 刷新令牌
     * @param refreshToken 刷新令牌
     * @return 新的登录响应
     */
    LoginResponse refreshToken(String refreshToken);
    
    /**
     * 获取当前用户信息
     * @param userId 用户ID
     * @return 登录响应（包含用户信息）
     */
    LoginResponse getCurrentUser(Long userId);
    
    /**
     * 获取用户权限列表（实时从数据库获取）
     * @param userId 用户ID
     * @return 权限编码列表
     */
    List<String> getUserPermissions(Long userId);

    /**
     * 获取登录密码加密公钥
     */
    EncryptionKeyResponse getLoginEncryptionKey();
}
