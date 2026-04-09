package com.university.sms.course.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 选课记录实体
 */
@Data
@TableName("crs_course_selection")
public class CourseSelection {
    
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
     * 状态: SELECTED-已选, WITHDRAWN-已退, LOTTERY_PENDING-待抽签, LOTTERY_FAILED-抽签未中
     */
    private String status;
    
    /**
     * 选课时间
     */
    private LocalDateTime selectedAt;
    
    /**
     * 退课时间
     */
    private LocalDateTime withdrawnAt;
    
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
}
