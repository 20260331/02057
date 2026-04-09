package com.university.sms.system.service;

import com.university.sms.system.dto.SystemConfigDTO;

import java.util.List;
import java.util.Map;

/**
 * 系统配置服务接口
 */
public interface SystemConfigService {
    
    /**
     * 获取所有配置
     */
    List<SystemConfigDTO> getAllConfigs();
    
    /**
     * 根据类型获取配置
     */
    List<SystemConfigDTO> getConfigsByType(String configType);
    
    /**
     * 获取配置值
     */
    String getConfigValue(String configKey);
    
    /**
     * 获取配置值，带默认值
     */
    String getConfigValue(String configKey, String defaultValue);
    
    /**
     * 更新配置
     */
    void updateConfig(String configKey, String configValue);
    
    /**
     * 批量更新配置
     */
    void batchUpdateConfigs(Map<String, String> configs);
    
    /**
     * 获取当前学期
     */
    String getCurrentSemester();
}
