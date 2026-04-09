package com.university.sms.course.dto;

import com.university.sms.common.dto.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 课程查询条件 DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CourseQueryDTO extends PageQuery {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 关键词（课程名称、编号、教师）
     */
    private String keyword;
    
    /**
     * 课程编号
     */
    private String courseCode;
    
    /**
     * 课程名称
     */
    private String name;
    
    /**
     * 教师姓名
     */
    private String teacherName;
    
    /**
     * 课程类别
     */
    private String category;
    
    /**
     * 学期
     */
    private String semester;
    
    /**
     * 开课院系
     */
    private String department;
    
    /**
     * 教师ID
     */
    private Long teacherId;
    
    /**
     * 状态
     */
    private Integer status;
    
    /**
     * 是否只显示有余量的课程
     */
    private Boolean hasCapacity;
}
