package com.university.sms.leave.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 请假信息 DTO
 */
@Data
public class LeaveInfoDTO {
    
    private Long id;
    private Long studentId;
    private String studentNo;
    private String studentName;
    private String department;
    private String classNo;
    private String leaveType;
    private String leaveTypeText;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal duration;
    private String reason;
    private String status;
    private String statusText;
    private Boolean urgent;
    private LocalDate returnDate;
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
    
    public static String getLeaveTypeText(String type) {
        if (type == null) return "";
        return switch (type) {
            case "SICK" -> "病假";
            case "PERSONAL" -> "事假";
            case "OFFICIAL" -> "公假";
            default -> type;
        };
    }
    
    public static String getStatusText(String status) {
        if (status == null) return "";
        return switch (status) {
            case "PENDING" -> "待审批";
            case "COUNSELOR_APPROVED" -> "辅导员已批";
            case "APPROVED" -> "已批准";
            case "REJECTED" -> "已拒绝";
            case "COMPLETED" -> "已销假";
            default -> status;
        };
    }
}
