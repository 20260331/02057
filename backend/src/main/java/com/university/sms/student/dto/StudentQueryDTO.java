package com.university.sms.student.dto;

import com.university.sms.common.dto.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 学生查询条件 DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class StudentQueryDTO extends PageQuery {
    
    /**
     * 学号
     */
    private String studentNo;
    
    /**
     * 姓名（模糊查询）
     */
    private String name;
    
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
     * 学籍状态
     */
    private String academicStatus;
    
    /**
     * 辅导员ID
     */
    private Long counselorId;
    
    /**
     * 入学年份
     */
    private Integer enrollmentYear;
}
