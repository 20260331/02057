package com.university.sms.course.enums;

import lombok.Getter;

/**
 * 课程类别枚举
 */
@Getter
public enum CourseCategory {
    
    REQUIRED("REQUIRED", "必修"),
    ELECTIVE("ELECTIVE", "选修"),
    GENERAL("GENERAL", "通识");
    
    private final String code;
    private final String description;
    
    CourseCategory(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public static CourseCategory fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (CourseCategory category : values()) {
            if (category.getCode().equals(code)) {
                return category;
            }
        }
        return null;
    }
}
