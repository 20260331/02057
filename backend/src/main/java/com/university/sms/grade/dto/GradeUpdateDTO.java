package com.university.sms.grade.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 成绩修改 DTO
 */
@Data
public class GradeUpdateDTO {
    
    @NotNull(message = "分数不能为空")
    private BigDecimal score;
    
    @NotBlank(message = "修改原因不能为空")
    private String reason;
}
