package com.university.sms.student.enums;

import lombok.Getter;

/**
 * 学籍证明类型枚举
 */
@Getter
public enum CertificateType {

    ENROLLMENT("ENROLLMENT", "学籍证明"),
    ATTENDANCE("ATTENDANCE", "在读证明"),
    GRADUATION("GRADUATION", "毕业证明"),
    TRANSCRIPT("TRANSCRIPT", "成绩证明"),
    STATUS_CHANGE("STATUS_CHANGE", "学籍异动证明");

    private final String code;
    private final String description;

    CertificateType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static CertificateType fromCode(String code) {
        for (CertificateType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的证明类型: " + code);
    }
}
