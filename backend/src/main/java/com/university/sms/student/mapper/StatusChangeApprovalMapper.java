package com.university.sms.student.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.university.sms.student.entity.StatusChangeApproval;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 学籍异动审批记录 Mapper
 */
@Mapper
public interface StatusChangeApprovalMapper extends BaseMapper<StatusChangeApproval> {

    @Select("SELECT * FROM stu_status_change_approval WHERE application_id = #{applicationId} ORDER BY created_at ASC")
    List<StatusChangeApproval> selectByApplicationId(@Param("applicationId") Long applicationId);
}
