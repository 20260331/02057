package com.university.sms.security.aspect;

import com.university.sms.common.exception.BusinessException;
import com.university.sms.security.annotation.RequirePermission;
import com.university.sms.security.service.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * 权限校验 AOP 切面
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class PermissionAspect {
    
    private final UserDetailsServiceImpl userDetailsService;
    
    @Around("@annotation(com.university.sms.security.annotation.RequirePermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取当前认证信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(401, "未登录或登录已过期");
        }
        
        // 获取用户ID
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof Long)) {
            throw new BusinessException(401, "无效的认证信息");
        }
        Long userId = (Long) principal;
        
        // 获取方法上的权限注解
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RequirePermission requirePermission = method.getAnnotation(RequirePermission.class);
        
        if (requirePermission == null) {
            return joinPoint.proceed();
        }
        
        // 获取需要的权限
        String[] requiredPermissions = requirePermission.value();
        RequirePermission.Logical logical = requirePermission.logical();
        
        // 获取用户权限
        List<String> userPermissions = userDetailsService.loadUserPermissions(userId);
        List<String> userRoles = userDetailsService.loadUserRoles(userId);
        
        log.info("用户 {} 角色: {}, 权限: {}, 需要权限: {}", userId, userRoles, userPermissions, Arrays.toString(requiredPermissions));
        
        // 检查是否为管理员（管理员拥有所有权限）
        if (userRoles.contains("ADMIN")) {
            return joinPoint.proceed();
        }
        
        // 校验权限
        boolean hasPermission = checkPermissions(requiredPermissions, userPermissions, logical);
        
        if (!hasPermission) {
            log.warn("用户 {} 权限不足，需要权限: {}, 拥有权限: {}", 
                    userId, Arrays.toString(requiredPermissions), userPermissions);
            throw new BusinessException(403, "权限不足");
        }
        
        return joinPoint.proceed();
    }
    
    /**
     * 检查权限
     */
    private boolean checkPermissions(String[] requiredPermissions, 
                                     List<String> userPermissions,
                                     RequirePermission.Logical logical) {
        if (requiredPermissions == null || requiredPermissions.length == 0) {
            return true;
        }
        
        if (logical == RequirePermission.Logical.AND) {
            // AND 逻辑：需要拥有所有权限
            return Arrays.stream(requiredPermissions)
                    .allMatch(userPermissions::contains);
        } else {
            // OR 逻辑：拥有任一权限即可
            return Arrays.stream(requiredPermissions)
                    .anyMatch(userPermissions::contains);
        }
    }
}
