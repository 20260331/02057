package com.university.sms.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.university.sms.system.entity.NotificationTemplate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface NotificationTemplateMapper extends BaseMapper<NotificationTemplate> {

    @Select("SELECT * FROM sys_notification_template WHERE status = 1 ORDER BY category, sort_order")
    List<NotificationTemplate> selectAllActive();

    @Select("SELECT * FROM sys_notification_template WHERE category = #{category} AND status = 1")
    List<NotificationTemplate> selectByCategory(String category);

    @Select("SELECT * FROM sys_notification_template WHERE template_code = #{code}")
    NotificationTemplate selectByCode(String code);
}
