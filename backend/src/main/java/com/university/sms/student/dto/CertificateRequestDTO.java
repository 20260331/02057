package com.university.sms.student.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 证明申请请求 DTO
 */
@Data
public class CertificateRequestDTO {

    @NotBlank(message = "证明类型不能为空")
    private String certType;

    private String purpose;

    @Min(value = 1, message = "份数至少为1")
    @Max(value = 10, message = "单次最多申请10份")
    private Integer copies = 1;
}
