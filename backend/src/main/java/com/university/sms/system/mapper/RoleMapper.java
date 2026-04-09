package com.university.sms.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.university.sms.system.entity.Role;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色 Mapper 接口
 */
@Mapper
public interface RoleMapper extends BaseMapper<Role> {
}
