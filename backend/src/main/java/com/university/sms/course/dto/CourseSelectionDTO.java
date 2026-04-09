package com.university.sms.course.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 选课 DTO
 */
@Data
public class CourseSelectionDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 选课记录ID
     */
    private Long id;
    
    /**
     * 学生ID
     */
    @NotNull(message = "学生ID不能为空")
    private Long studentId;
    
    /**
     * 课程ID
     */
    @NotNull(message = "课程ID不能为空")
    private Long courseId;
    
    /**
     * 学生学号
     */
    private String studentNo;
    
    /**
     * 学生姓名
     */
    private String studentName;
    
    /**
     * 课程编号
     */
    private String courseCode;
    
    /**
     * 课程名称
     */
    private String courseName;
    
    /**
     * 学分
     */
    private java.math.BigDecimal credits;
    
    /**
     * 教师姓名
     */
    private String teacherName;
    
    /**
     * 上课时间
     */
    private String schedule;
    
    /**
     * 上课地点
     */
    private String location;
    
    /**
     * 状态
     */
    private String status;
    
    /**
     * 状态描述
     */
    private String statusDesc;
    
    /**
     * 选课时间
     */
    private LocalDateTime selectedAt;
    
    /**
     * 退课时间
     */
    private LocalDateTime withdrawnAt;
}
