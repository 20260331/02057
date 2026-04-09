package com.university.sms.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.university.sms.system.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户 Mapper 接口
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
    
    /**
     * 根据用户名查询用户
     */
    @Select("SELECT * FROM sys_user WHERE username = #{username} AND deleted = 0")
    User selectByUsername(@Param("username") String username);
    
    /**
     * 查询用户的角色编码列表
     */
    @Select("SELECT r.role_code FROM sys_role r " +
            "INNER JOIN sys_user_role ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND r.status = 1")
    List<String> selectRoleCodesByUserId(@Param("userId") Long userId);
    
    /**
     * 查询用户的权限编码列表
     */
    @Select("SELECT DISTINCT p.permission_code FROM sys_permission p " +
            "INNER JOIN sys_role_permission rp ON p.id = rp.permission_id " +
            "INNER JOIN sys_user_role ur ON rp.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId}")
    List<String> selectPermissionCodesByUserId(@Param("userId") Long userId);
    
    /**
     * 更新登录失败次数
     */
    @Update("UPDATE sys_user SET login_fail_count = #{failCount}, " +
            "lock_time = #{lockTime} WHERE id = #{userId}")
    int updateLoginFailInfo(@Param("userId") Long userId, 
                           @Param("failCount") Integer failCount,
                           @Param("lockTime") LocalDateTime lockTime);
    
    /**
     * 更新最后登录信息
     */
    @Update("UPDATE sys_user SET last_login_time = #{loginTime}, " +
            "last_login_ip = #{loginIp}, login_fail_count = 0, lock_time = NULL " +
            "WHERE id = #{userId}")
    int updateLastLoginInfo(@Param("userId") Long userId,
                           @Param("loginTime") LocalDateTime loginTime,
                           @Param("loginIp") String loginIp);
}
