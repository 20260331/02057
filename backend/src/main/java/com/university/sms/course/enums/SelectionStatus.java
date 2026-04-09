package com.university.sms.course.enums;

import lombok.Getter;

/**
 * 选课状态枚举
 */
@Getter
public enum SelectionStatus {
    
    SELECTED("SELECTED", "已选"),
    WITHDRAWN("WITHDRAWN", "已退"),
    LOTTERY_PENDING("LOTTERY_PENDING", "待抽签"),
    LOTTERY_FAILED("LOTTERY_FAILED", "抽签未中");
    
    private final String code;
    private final String description;
    
    SelectionStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public static SelectionStatus fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (SelectionStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}
