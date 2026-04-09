package com.university.sms.grade.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 成绩实体
 */
@Data
@TableName("grd_grade")
public class Grade {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 学生ID
     */
    private Long studentId;
    
    /**
     * 课程ID
     */
    private Long courseId;
    
    /**
     * 分数 (0-100)
     */
    private BigDecimal score;
    
    /**
     * 等级 (A/B/C/D/F)
     */
    private String letterGrade;
    
    /**
     * 绩点
     */
    private BigDecimal gradePoints;
    
    /**
     * 状态: DRAFT-草稿, SUBMITTED-已提交, APPROVED-已确认
     */
    private String status;
    
    /**
     * 录入人ID
     */
    private Long enteredBy;
    
    /**
     * 录入时间
     */
    private LocalDateTime enteredAt;
    
    /**
     * 审批人ID
     */
    private Long approvedBy;
    
    /**
     * 审批时间
     */
    private LocalDateTime approvedAt;
    
    /**
     * 学期
     */
    private String semester;
    
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
     * 成绩状态常量
     */
    public static final String STATUS_DRAFT = "DRAFT";
    public static final String STATUS_SUBMITTED = "SUBMITTED";
    public static final String STATUS_APPROVED = "APPROVED";
}
