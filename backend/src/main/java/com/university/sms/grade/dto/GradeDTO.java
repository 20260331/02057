package com.university.sms.grade.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 成绩 DTO
 */
@Data
public class GradeDTO {
    
    private Long id;
    private Long studentId;
    private String studentNo;
    private String studentName;
    private Long courseId;
    private String courseCode;
    private String courseName;
    private BigDecimal credits;
    private BigDecimal score;
    private String letterGrade;
    private BigDecimal gradePoints;
    private String semester;
    private String status;
    private String statusText;
    private LocalDateTime enteredAt;
    private LocalDateTime approvedAt;
    
    public static String getStatusText(String status) {
        if (status == null) return "";
        return switch (status) {
            case "DRAFT" -> "草稿";
            case "SUBMITTED" -> "已提交";
            case "APPROVED" -> "已确认";
            default -> status;
        };
    }
}
