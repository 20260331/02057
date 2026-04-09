package com.university.sms.leave.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 请假审批 DTO
 */
@Data
public class LeaveApprovalDTO {
    
    @NotNull(message = "审批结果不能为空")
    private Boolean approved;
    
    @NotBlank(message = "审批意见不能为空")
    private String comment;
}
