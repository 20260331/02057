package com.university.sms.system.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * 用户创建 DTO
 */
@Data
public class UserCreateDTO {
    @NotBlank(message = "用户名不能为空")
    private String username;
    
    @NotBlank(message = "真实姓名不能为空")
    private String realName;
    
    private String phone;
    private String email;
    private List<Long> roleIds;
}
