package com.university.sms.system.dto;

import lombok.Data;

/**
 * 用户更新 DTO
 */
@Data
public class UserUpdateDTO {
    private String realName;
    private String phone;
    private String email;
    private Integer status;
}
