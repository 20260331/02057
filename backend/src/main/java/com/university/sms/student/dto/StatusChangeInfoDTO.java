package com.university.sms.student.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 学籍异动申请信息 DTO
 */
@Data
public class StatusChangeInfoDTO {

    private Long id;
    private Long studentId;
    private String studentNo;
    private String studentName;
    private String department;
    private String classNo;
    private String currentStatus;
    private String currentStatusText;
    private String targetStatus;
    private String targetStatusText;
    private String reason;
    private LocalDate effectiveDate;
    private String status;
    private String statusText;
    private Boolean executed;
    private LocalDateTime approvedAt;
    private LocalDateTime createdAt;
    private List<ApprovalRecord> approvalHistory;

    @Data
    public static class ApprovalRecord {
        private Long id;
        private Long approverId;
        private String approverName;
        private String approverRole;
        private Boolean approved;
        private String comment;
        private LocalDateTime createdAt;
    }

    public static String getAcademicStatusText(String status) {
        if (status == null) return "";
        return switch (status) {
            case "ENROLLED" -> "在读";
            case "SUSPENDED" -> "休学";
            case "WITHDRAWN" -> "退学";
            case "GRADUATED" -> "毕业";
            case "TRANSFERRED" -> "转学";
            default -> status;
        };
    }

    public static String getApplicationStatusText(String status) {
        if (status == null) return "";
        return switch (status) {
            case "PENDING" -> "待审批";
            case "COUNSELOR_APPROVED" -> "辅导员已批";
            case "APPROVED" -> "已通过";
            case "REJECTED" -> "已拒绝";
            case "CANCELLED" -> "已取消";
            default -> status;
        };
    }
}
