package com.university.sms.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.university.sms.system.entity.NotificationRead;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface NotificationReadMapper extends BaseMapper<NotificationRead> {

    @Select("SELECT * FROM sys_notification_read WHERE notification_id = #{notificationId} AND user_id = #{userId}")
    NotificationRead selectByNotificationAndUser(Long notificationId, Long userId);

    @Select("""
        SELECT COUNT(*) FROM sys_notification n
        WHERE n.status = 'SENT'
        AND (n.target_type = 'ALL'
             OR (n.target_type = 'USER' AND FIND_IN_SET(#{userId}, n.target_value))
             OR (n.target_type = 'ROLE' AND EXISTS (
                 SELECT 1 FROM sys_user_role ur
                 JOIN sys_role r ON ur.role_id = r.id
                 WHERE ur.user_id = #{userId}
                 AND FIND_IN_SET(r.role_code, n.target_value)
             )))
        AND NOT EXISTS (
            SELECT 1 FROM sys_notification_read nr
            WHERE nr.notification_id = n.id AND nr.user_id = #{userId} AND nr.is_read = 1
        )
        """)
    int countUnread(Long userId);
}
