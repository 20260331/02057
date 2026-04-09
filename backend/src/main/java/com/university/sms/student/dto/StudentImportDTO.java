package com.university.sms.student.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * 学生导入 DTO
 */
@Data
public class StudentImportDTO {
    
    /**
     * 行号（用于错误报告）
     */
    private Integer rowNum;
    
    /**
     * 学号
     */
    private String studentNo;
    
    /**
     * 姓名
     */
    private String name;
    
    /**
     * 性别: M-男, F-女
     */
    private String gender;
    
    /**
     * 出生日期
     */
    private LocalDate birthDate;
    
    /**
     * 身份证号
     */
    private String idNumber;
    
    /**
     * 联系电话
     */
    private String phone;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 家庭住址
     */
    private String address;
    
    /**
     * 院系
     */
    private String department;
    
    /**
     * 专业
     */
    private String major;
    
    /**
     * 班级
     */
    private String classNo;
    
    /**
     * 入学日期
     */
    private LocalDate enrollmentDate;
    
    /**
     * 预计毕业日期
     */
    private LocalDate graduationDate;
    
    /**
     * 辅导员工号
     */
    private String counselorNo;
}
