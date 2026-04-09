package com.university.sms.leave.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 请假申请实体
 */
@Data
@TableName("lev_leave_request")
public class LeaveRequest {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 学生ID
     */
    private Long studentId;
    
    /**
     * 请假类型: SICK-病假, PERSONAL-事假, OFFICIAL-公假
     */
    private String leaveType;
    
    /**
     * 开始日期
     */
    private LocalDate startDate;
    
    /**
     * 结束日期
     */
    private LocalDate endDate;
    
    /**
     * 请假时长（天）
     */
    private BigDecimal duration;
    
    /**
     * 请假原因
     */
    private String reason;
    
    /**
     * 状态: PENDING-待审批, COUNSELOR_APPROVED-辅导员已批, APPROVED-已批准, REJECTED-已拒绝, COMPLETED-已销假
     */
    private String status;
    
    /**
     * 是否紧急
     */
    @TableField("is_urgent")
    private Boolean urgent;
    
    /**
     * 销假日期
     */
    private LocalDate returnDate;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    
    /**
     * 状态常量
     */
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_COUNSELOR_APPROVED = "COUNSELOR_APPROVED";
    public static final String STATUS_APPROVED = "APPROVED";
    public static final String STATUS_REJECTED = "REJECTED";
    public static final String STATUS_COMPLETED = "COMPLETED";
    
    /**
     * 请假类型常量
     */
    public static final String TYPE_SICK = "SICK";
    public static final String TYPE_PERSONAL = "PERSONAL";
    public static final String TYPE_OFFICIAL = "OFFICIAL";
}
