package com.university.sms.system.controller;

import com.university.sms.common.response.Result;
import com.university.sms.security.annotation.RequirePermission;
import com.university.sms.system.dto.SystemConfigDTO;
import com.university.sms.system.service.SystemConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 系统配置控制器
 */
@RestController
@RequestMapping("/system/config")
@RequiredArgsConstructor
public class SystemConfigController {
    
    private final SystemConfigService configService;
    
    /**
     * 获取所有配置
     */
    @GetMapping
    @RequirePermission("config:view")
    public Result<List<SystemConfigDTO>> getAllConfigs() {
        return Result.success(configService.getAllConfigs());
    }
    
    /**
     * 根据类型获取配置
     */
    @GetMapping("/type/{configType}")
    @RequirePermission("config:view")
    public Result<List<SystemConfigDTO>> getConfigsByType(@PathVariable String configType) {
        return Result.success(configService.getConfigsByType(configType));
    }
    
    /**
     * 获取单个配置值
     */
    @GetMapping("/{configKey}")
    public Result<String> getConfigValue(@PathVariable String configKey) {
        return Result.success(configService.getConfigValue(configKey));
    }
    
    /**
     * 更新单个配置
     */
    @PutMapping("/{configKey}")
    @RequirePermission("config:update")
    public Result<Void> updateConfig(@PathVariable String configKey, @RequestBody Map<String, String> body) {
        configService.updateConfig(configKey, body.get("value"));
        return Result.success();
    }
    
    /**
     * 批量更新配置
     */
    @PutMapping("/batch")
    @RequirePermission("config:update")
    public Result<Void> batchUpdateConfigs(@RequestBody Map<String, String> configs) {
        configService.batchUpdateConfigs(configs);
        return Result.success();
    }
    
    /**
     * 获取当前学期
     */
    @GetMapping("/current-semester")
    public Result<String> getCurrentSemester() {
        return Result.success(configService.getCurrentSemester());
    }
}
