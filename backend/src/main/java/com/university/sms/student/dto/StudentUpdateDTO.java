package com.university.sms.student.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;

/**
 * 学生信息修改 DTO（辅导员使用）
 */
@Data
public class StudentUpdateDTO {
    
    /**
     * 联系电话
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
    
    /**
     * 邮箱
     */
    @Email(message = "邮箱格式不正确")
    private String email;
    
    /**
     * 家庭住址
     */
    private String address;
    
    /**
     * 班级
     */
    private String classNo;
    
    /**
     * 预计毕业日期
     */
    private LocalDate graduationDate;
}
