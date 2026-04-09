package com.university.sms.student.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 学籍异动审批记录实体
 */
@Data
@TableName("stu_status_change_approval")
public class StatusChangeApproval {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long applicationId;

    private Long approverId;

    private String approverName;

    private String approverRole;

    private Integer approvalOrder;

    /**
     * APPROVE / REJECT
     */
    private String action;

    private String comment;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(exist = false)
    private Boolean approved;

    public Boolean getApproved() {
        return "APPROVE".equals(action);
    }

    public void setApproved(Boolean approved) {
        this.action = approved != null && approved ? "APPROVE" : "REJECT";
    }
}
