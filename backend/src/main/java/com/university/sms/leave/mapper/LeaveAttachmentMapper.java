package com.university.sms.leave.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.university.sms.leave.entity.LeaveAttachment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface LeaveAttachmentMapper extends BaseMapper<LeaveAttachment> {
    
    @Select("SELECT * FROM lev_leave_attachment WHERE leave_request_id = #{leaveRequestId}")
    List<LeaveAttachment> selectByLeaveRequestId(@Param("leaveRequestId") Long leaveRequestId);
}
