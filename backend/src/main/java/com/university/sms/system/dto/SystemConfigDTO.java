package com.university.sms.system.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 系统配置 DTO
 */
@Data
public class SystemConfigDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private String configKey;
    private String configValue;
    private String configType;
    private String configName;
    private String description;
}
