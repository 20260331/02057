package com.university.sms.security.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 登录响应 DTO
 */
@Data
@Builder
public class LoginResponse {
    
    /**
     * 访问令牌
     */
    private String token;
    
    /**
     * 刷新令牌
     */
    private String refreshToken;
    
    /**
     * 用户信息
     */
    private UserInfo userInfo;
    
    /**
     * 权限列表
     */
    private List<String> permissions;
    
    /**
     * 用户信息内部类
     */
    @Data
    @Builder
    public static class UserInfo {
        private Long id;
        private String username;
        private String realName;
        private String phone;
        private String email;
        private String avatar;
        private List<String> roles;
    }
}
