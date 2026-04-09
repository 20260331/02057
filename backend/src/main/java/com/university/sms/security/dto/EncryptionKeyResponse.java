package com.university.sms.security.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 登录加密公钥响应
 */
@Data
@AllArgsConstructor
public class EncryptionKeyResponse {
    private String algorithm;
    private String publicKey;
}
