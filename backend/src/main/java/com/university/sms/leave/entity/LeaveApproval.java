package com.university.sms.leave.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 请假审批记录实体
 */
@Data
@TableName("lev_leave_approval")
public class LeaveApproval {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 请假申请ID
     */
    private Long leaveRequestId;
    
    /**
     * 审批人ID
     */
    private Long approverId;
    
    /**
     * 审批人姓名
     */
    private String approverName;
    
    /**
     * 审批人角色
     */
    private String approverRole;
    
    /**
     * 审批顺序
     */
    private Integer approvalOrder;
    
    /**
     * 审批动作: APPROVE-批准, REJECT-拒绝
     */
    private String action;
    
    /**
     * 审批意见
     */
    private String comment;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    // 便捷方法
    @TableField(exist = false)
    private Boolean approved;
    
    public Boolean getApproved() {
        return "APPROVE".equals(action);
    }
    
    public void setApproved(Boolean approved) {
        this.action = approved != null && approved ? "APPROVE" : "REJECT";
    }
}
