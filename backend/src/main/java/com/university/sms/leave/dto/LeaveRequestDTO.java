package com.university.sms.leave.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * 请假申请 DTO
 */
@Data
public class LeaveRequestDTO {
    
    @NotBlank(message = "请假类型不能为空")
    private String leaveType;
    
    @NotNull(message = "开始日期不能为空")
    private LocalDate startDate;
    
    @NotNull(message = "结束日期不能为空")
    private LocalDate endDate;
    
    @NotBlank(message = "请假原因不能为空")
    private String reason;
    
    /**
     * 是否紧急
     */
    private Boolean urgent = false;
}
