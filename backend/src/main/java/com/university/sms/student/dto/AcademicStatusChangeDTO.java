package com.university.sms.student.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * 学籍状态变更 DTO
 */
@Data
public class AcademicStatusChangeDTO {
    
    /**
     * 目标状态
     */
    @NotBlank(message = "目标状态不能为空")
    private String targetStatus;
    
    /**
     * 变更原因
     */
    @NotBlank(message = "变更原因不能为空")
    private String reason;
    
    /**
     * 生效日期
     */
    @NotNull(message = "生效日期不能为空")
    private LocalDate effectiveDate;
}
