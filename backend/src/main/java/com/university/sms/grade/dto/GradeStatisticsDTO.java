package com.university.sms.grade.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 成绩统计 DTO
 */
@Data
public class GradeStatisticsDTO {
    
    /**
     * 课程ID
     */
    private Long courseId;
    
    /**
     * 课程名称
     */
    private String courseName;
    
    /**
     * 学生总数
     */
    private Integer totalStudents;
    
    /**
     * 平均分
     */
    private BigDecimal averageScore;
    
    /**
     * 最高分
     */
    private BigDecimal maxScore;
    
    /**
     * 最低分
     */
    private BigDecimal minScore;
    
    /**
     * 及格率
     */
    private BigDecimal passRate;
    
    /**
     * 优秀率 (>=90)
     */
    private BigDecimal excellentRate;
    
    /**
     * 成绩分布 (A/B/C/D/F -> 人数)
     */
    private Map<String, Integer> distribution;
}
