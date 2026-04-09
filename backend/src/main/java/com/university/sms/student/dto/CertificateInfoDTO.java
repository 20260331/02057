package com.university.sms.student.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 证明记录信息 DTO
 */
@Data
public class CertificateInfoDTO {

    private Long id;
    private Long studentId;
    private String studentNo;
    private String studentName;
    private String department;
    private String certNo;
    private String certType;
    private String certTypeText;
    private String certTitle;
    private String certContent;
    private String purpose;
    private Integer copies;
    private String status;
    private String statusText;
    private String generatedByName;
    private LocalDateTime revokedAt;
    private String revokeReason;
    private LocalDateTime createdAt;

    public static String getCertTypeText(String type) {
        if (type == null) return "";
        return switch (type) {
            case "ENROLLMENT" -> "学籍证明";
            case "ATTENDANCE" -> "在读证明";
            case "GRADUATION" -> "毕业证明";
            case "TRANSCRIPT" -> "成绩证明";
            case "STATUS_CHANGE" -> "学籍异动证明";
            default -> type;
        };
    }

    public static String getStatusText(String status) {
        if (status == null) return "";
        return switch (status) {
            case "VALID" -> "有效";
            case "REVOKED" -> "已作废";
            default -> status;
        };
    }
}
