package com.university.sms.student.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 学生信息实体
 */
@Data
@TableName("stu_student")
public class Student {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 关联用户ID
     */
    private Long userId;
    
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
     * 学籍状态
     */
    private String academicStatus;
    
    /**
     * 辅导员ID
     */
    private Long counselorId;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    
    /**
     * 逻辑删除标记
     */
    @TableLogic
    private Integer deleted;
}
