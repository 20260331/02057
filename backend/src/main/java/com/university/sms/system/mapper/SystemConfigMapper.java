package com.university.sms.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.university.sms.system.entity.SystemConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 系统配置 Mapper
 */
@Mapper
public interface SystemConfigMapper extends BaseMapper<SystemConfig> {
    
    /**
     * 根据配置键查询
     */
    @Select("SELECT * FROM sys_config WHERE config_key = #{configKey}")
    SystemConfig selectByKey(@Param("configKey") String configKey);
    
    /**
     * 根据配置类型查询
     */
    @Select("SELECT * FROM sys_config WHERE config_type = #{configType} ORDER BY sort_order")
    List<SystemConfig> selectByType(@Param("configType") String configType);
    
    /**
     * 查询所有配置
     */
    @Select("SELECT * FROM sys_config ORDER BY config_type, sort_order")
    List<SystemConfig> selectAllConfigs();
}
