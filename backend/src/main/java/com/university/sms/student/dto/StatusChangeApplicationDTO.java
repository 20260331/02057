package com.university.sms.student.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * 学籍异动申请 DTO
 */
@Data
public class StatusChangeApplicationDTO {

    @NotBlank(message = "目标状态不能为空")
    private String targetStatus;

    @NotBlank(message = "申请原因不能为空")
    private String reason;

    @NotNull(message = "期望生效日期不能为空")
    private LocalDate effectiveDate;
}
