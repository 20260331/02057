package com.university.sms.student.enums;

import lombok.Getter;

/**
 * 学籍状态枚举
 */
@Getter
public enum AcademicStatus {
    
    ENROLLED("ENROLLED", "在读"),
    SUSPENDED("SUSPENDED", "休学"),
    WITHDRAWN("WITHDRAWN", "退学"),
    GRADUATED("GRADUATED", "毕业"),
    TRANSFERRED("TRANSFERRED", "转学");
    
    private final String code;
    private final String description;
    
    AcademicStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    /**
     * 根据编码获取枚举
     */
    public static AcademicStatus fromCode(String code) {
        for (AcademicStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的学籍状态: " + code);
    }
    
    /**
     * 检查状态变更是否合法
     */
    public boolean canTransitionTo(AcademicStatus target) {
        if (this == target) {
            return false;
        }
        // 已毕业或已退学的学生不能再变更状态
        if (this == GRADUATED || this == WITHDRAWN) {
            return false;
        }
        // 休学只能恢复为在读或退学
        if (this == SUSPENDED) {
            return target == ENROLLED || target == WITHDRAWN;
        }
        return true;
    }
}
