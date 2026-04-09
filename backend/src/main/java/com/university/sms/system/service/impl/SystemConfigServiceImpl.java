package com.university.sms.system.service.impl;

import com.university.sms.common.exception.BusinessException;
import com.university.sms.system.dto.SystemConfigDTO;
import com.university.sms.system.entity.SystemConfig;
import com.university.sms.system.mapper.SystemConfigMapper;
import com.university.sms.system.service.SystemConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 系统配置服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SystemConfigServiceImpl implements SystemConfigService {
    
    private final SystemConfigMapper configMapper;
    
    @Override
    public List<SystemConfigDTO> getAllConfigs() {
        return configMapper.selectAllConfigs().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<SystemConfigDTO> getConfigsByType(String configType) {
        return configMapper.selectByType(configType).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public String getConfigValue(String configKey) {
        SystemConfig config = configMapper.selectByKey(configKey);
        return config != null ? config.getConfigValue() : null;
    }
    
    @Override
    public String getConfigValue(String configKey, String defaultValue) {
        String value = getConfigValue(configKey);
        return value != null ? value : defaultValue;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateConfig(String configKey, String configValue) {
        SystemConfig config = configMapper.selectByKey(configKey);
        if (config == null) {
            throw new BusinessException("配置项不存在: " + configKey);
        }
        config.setConfigValue(configValue);
        configMapper.updateById(config);
        log.info("更新系统配置: {} = {}", configKey, configValue);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdateConfigs(Map<String, String> configs) {
        for (Map.Entry<String, String> entry : configs.entrySet()) {
            SystemConfig config = configMapper.selectByKey(entry.getKey());
            if (config != null) {
                config.setConfigValue(entry.getValue());
                configMapper.updateById(config);
            }
        }
        log.info("批量更新系统配置: {} 项", configs.size());
    }
    
    @Override
    public String getCurrentSemester() {
        return getConfigValue("current_semester", "2024-2025-1");
    }
    
    private SystemConfigDTO toDTO(SystemConfig config) {
        SystemConfigDTO dto = new SystemConfigDTO();
        dto.setId(config.getId());
        dto.setConfigKey(config.getConfigKey());
        dto.setConfigValue(config.getConfigValue());
        dto.setConfigType(config.getConfigType());
        dto.setConfigName(config.getConfigName());
        dto.setDescription(config.getDescription());
        return dto;
    }
}
