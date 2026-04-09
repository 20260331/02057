package com.university.sms.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.university.sms.system.entity.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {

    @Select("SELECT * FROM sys_notification WHERE status = 'SENT' ORDER BY send_time DESC")
    List<Notification> selectAllSent();

    @Select("""
        SELECT n.* FROM sys_notification n
        WHERE n.status = 'SENT'
        AND (n.target_type = 'ALL'
             OR (n.target_type = 'USER' AND FIND_IN_SET(#{userId}, n.target_value))
             OR (n.target_type = 'ROLE' AND EXISTS (
                 SELECT 1 FROM sys_user_role ur
                 JOIN sys_role r ON ur.role_id = r.id
                 WHERE ur.user_id = #{userId}
                 AND FIND_IN_SET(r.role_code, n.target_value)
             )))
        ORDER BY n.send_time DESC
        """)
    List<Notification> selectByTargetUser(Long userId);
}
