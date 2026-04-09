package com.university.sms.course.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 课表项 DTO
 */
@Data
public class ScheduleDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 课程ID
     */
    private Long courseId;
    
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
    private BigDecimal credits;
    
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
     * 星期几 (1-7)
     */
    private Integer dayOfWeek;
    
    /**
     * 开始节次
     */
    private Integer startSection;
    
    /**
     * 结束节次
     */
    private Integer endSection;
}
