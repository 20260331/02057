package com.university.sms.course.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 课程先修要求实体
 */
@Data
@TableName("crs_course_prerequisite")
public class CoursePrerequisite {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 课程ID
     */
    private Long courseId;
    
    /**
     * 先修课程ID
     */
    private Long prerequisiteCourseId;
    
    /**
     * 最低成绩要求
     */
    private BigDecimal minScore;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    /**
     * 默认最低成绩要求
     */
    public static final BigDecimal DEFAULT_MIN_SCORE = new BigDecimal("60.00");
}
