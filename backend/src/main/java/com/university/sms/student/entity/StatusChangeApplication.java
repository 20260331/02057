package com.university.sms.student.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 学籍异动申请实体
 */
@Data
@TableName("stu_status_change_application")
public class StatusChangeApplication {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long studentId;

    private String currentStatus;

    private String targetStatus;

    private String reason;

    private LocalDate effectiveDate;

    /**
     * PENDING / COUNSELOR_APPROVED / APPROVED / REJECTED / CANCELLED
     */
    private String status;

    private Long currentApproverId;

    private LocalDateTime approvedAt;

    @TableField("executed")
    private Boolean executed;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_COUNSELOR_APPROVED = "COUNSELOR_APPROVED";
    public static final String STATUS_APPROVED = "APPROVED";
    public static final String STATUS_REJECTED = "REJECTED";
    public static final String STATUS_CANCELLED = "CANCELLED";

    /**
     * 退学、转学需要教务处二级审批
     */
    public boolean needsEscalation() {
        return "WITHDRAWN".equals(targetStatus) || "TRANSFERRED".equals(targetStatus);
    }
}
