package com.university.sms.leave.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.university.sms.leave.entity.LeaveApproval;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 请假审批记录 Mapper
 */
@Mapper
public interface LeaveApprovalMapper extends BaseMapper<LeaveApproval> {
    
    /**
     * 查询请假的审批记录
     */
    @Select("SELECT * FROM lev_leave_approval WHERE leave_request_id = #{leaveRequestId} ORDER BY created_at ASC")
    List<LeaveApproval> selectByLeaveRequestId(@Param("leaveRequestId") Long leaveRequestId);
}
