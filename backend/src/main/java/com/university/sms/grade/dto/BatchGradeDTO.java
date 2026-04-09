package com.university.sms.grade.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 批量成绩录入 DTO
 */
@Data
public class BatchGradeDTO {
    
    @NotNull(message = "课程ID不能为空")
    private Long courseId;
    
    private List<GradeItem> grades;
    
    @Data
    public static class GradeItem {
        @NotNull(message = "学生ID不能为空")
        private Long studentId;
        
        @NotNull(message = "分数不能为空")
        private BigDecimal score;
    }
}
