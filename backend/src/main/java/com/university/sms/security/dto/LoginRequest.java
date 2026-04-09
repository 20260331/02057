package com.university.sms.security.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录请求 DTO
 */
@Data
public class LoginRequest {
    
    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    private String username;
    
    /**
     * 前端使用后端公钥加密后的密码（Base64 编码密文）
     */
    @NotBlank(message = "密码不能为空")
    private String encryptedPassword;
}
