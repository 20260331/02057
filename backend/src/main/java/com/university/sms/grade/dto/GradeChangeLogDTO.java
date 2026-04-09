package com.university.sms.grade.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 成绩修改记录 DTO
 */
@Data
public class GradeChangeLogDTO {
    private Long id;
    private Long gradeId;
    private Long studentId;
    private String studentNo;
    private String studentName;
    private Long courseId;
    private BigDecimal oldScore;
    private BigDecimal newScore;
    private String reason;
    private Long operatorId;
    private String operatorName;
    private LocalDateTime createdAt;
}
