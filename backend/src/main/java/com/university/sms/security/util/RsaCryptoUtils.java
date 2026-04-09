package com.university.sms.security.util;

import com.university.sms.common.exception.BusinessException;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.spec.MGF1ParameterSpec;
import java.util.Base64;

/**
 * RSA-OAEP 加解密工具（服务启动时生成一次密钥对）
 */
@Component
public class RsaCryptoUtils {

    private static final String ALGORITHM = "RSA";
    private static final String OAEP_TRANSFORMATION = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
    private static final OAEPParameterSpec OAEP_SHA256_SPEC =
            new OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT);

    private final KeyPair keyPair = generateKeyPair();

    public String getPublicKeyBase64() {
        return Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
    }

    public String decryptFromBase64(String encryptedPasswordBase64) {
        try {
            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedPasswordBase64);
            Cipher cipher = Cipher.getInstance(OAEP_TRANSFORMATION);
            // 显式指定 OAEP 参数，避免不同 JCE 提供者默认 MGF1 摘要不一致（常见为 SHA-1）
            cipher.init(Cipher.DECRYPT_MODE, keyPair.getPrivate(), OAEP_SHA256_SPEC);
            byte[] plainBytes = cipher.doFinal(encryptedBytes);
            return new String(plainBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new BusinessException("密码解密失败，请刷新页面后重试");
        }
    }

    private KeyPair generateKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance(ALGORITHM);
            generator.initialize(2048);
            return generator.generateKeyPair();
        } catch (Exception e) {
            throw new IllegalStateException("RSA 密钥对初始化失败", e);
        }
    }
}
