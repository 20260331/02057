package com.university.sms.security.annotation;

import java.lang.annotation.*;

/**
 * 权限校验注解
 * 用于标注需要特定权限才能访问的方法
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePermission {
    
    /**
     * 需要的权限编码
     * 多个权限之间是 OR 关系，满足其一即可
     */
    String[] value();
    
    /**
     * 权限逻辑：AND 或 OR
     * AND: 需要同时拥有所有权限
     * OR: 拥有任一权限即可（默认）
     */
    Logical logical() default Logical.OR;
    
    /**
     * 权限逻辑枚举
     */
    enum Logical {
        AND, OR
    }
}
