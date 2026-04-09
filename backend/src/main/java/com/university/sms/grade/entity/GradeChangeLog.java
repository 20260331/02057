package com.university.sms.grade.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 成绩变更日志实体
 */
@Data
@TableName("grd_grade_change_log")
public class GradeChangeLog {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 成绩ID
     */
    private Long gradeId;
    
    /**
     * 学生ID
     */
    private Long studentId;
    
    /**
     * 课程ID
     */
    private Long courseId;
    
    /**
     * 原分数
     */
    private BigDecimal oldScore;
    
    /**
     * 新分数
     */
    private BigDecimal newScore;
    
    /**
     * 修改原因
     */
    @TableField("change_reason")
    private String reason;
    
    /**
     * 操作人ID
     */
    private Long operatorId;
    
    /**
     * 操作人姓名
     */
    private String operatorName;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
