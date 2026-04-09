package com.university.sms.security.controller;

import com.university.sms.common.response.Result;
import com.university.sms.security.dto.EncryptionKeyResponse;
import com.university.sms.security.dto.LoginRequest;
import com.university.sms.security.dto.LoginResponse;
import com.university.sms.security.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;

    /**
     * 获取登录加密公钥
     */
    @GetMapping("/login/encryption-key")
    public Result<EncryptionKeyResponse> getLoginEncryptionKey() {
        return Result.success(authService.getLoginEncryptionKey());
    }
    
    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request,
                                       HttpServletRequest httpRequest) {
        String clientIp = getClientIp(httpRequest);
        LoginResponse response = authService.login(request, clientIp);
        return Result.success(response);
    }
    
    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader(value = "Authorization", required = false) String authorization) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7);
            authService.logout(token);
        }
        return Result.success();
    }
    
    /**
     * 刷新令牌
     */
    @PostMapping("/refresh")
    public Result<LoginResponse> refresh(@RequestBody RefreshTokenRequest request) {
        LoginResponse response = authService.refreshToken(request.getRefreshToken());
        return Result.success(response);
    }
    
    /**
     * 获取当前用户信息
     */
    @GetMapping("/current")
    public Result<LoginResponse> getCurrentUser(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error("未登录");
        }
        LoginResponse response = authService.getCurrentUser(userId);
        return Result.success(response);
    }
    
    /**
     * 获取当前用户权限列表（实时从数据库获取）
     */
    @GetMapping("/permissions")
    public Result<java.util.List<String>> getCurrentPermissions(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error("未登录");
        }
        java.util.List<String> permissions = authService.getUserPermissions(userId);
        return Result.success(permissions);
    }
    
    /**
     * 获取客户端 IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个代理时，取第一个 IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
    
    /**
     * 刷新令牌请求
     */
    @lombok.Data
    public static class RefreshTokenRequest {
        private String refreshToken;
    }
}
