package com.university.sms.grade.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * GPA 信息 DTO
 */
@Data
public class GPAInfoDTO {
    
    /**
     * 累计 GPA
     */
    private BigDecimal cumulativeGPA;
    
    /**
     * 总学分
     */
    private BigDecimal totalCredits;
    
    /**
     * 已获学分
     */
    private BigDecimal earnedCredits;
    
    /**
     * 各学期 GPA
     */
    private List<SemesterGPA> semesterGPAs;
    
    @Data
    public static class SemesterGPA {
        private String semester;
        private BigDecimal gpa;
        private BigDecimal credits;
    }
}
