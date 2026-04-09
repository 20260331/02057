package com.university.sms.course.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 课程名单项 DTO
 */
@Data
public class CourseRosterDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 学生ID
     */
    private Long studentId;
    
    /**
     * 学号
     */
    private String studentNo;
    
    /**
     * 姓名
     */
    private String studentName;
    
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
     * 选课时间
     */
    private LocalDateTime selectedAt;
}
