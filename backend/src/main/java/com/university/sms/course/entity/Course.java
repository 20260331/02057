package com.university.sms.course.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 课程实体
 */
@Data
@TableName("crs_course")
public class Course {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 课程编号
     */
    private String courseCode;
    
    /**
     * 课程名称
     */
    private String name;
    
    /**
     * 学分
     */
    private BigDecimal credits;
    
    /**
     * 授课教师ID
     */
    private Long teacherId;
    
    /**
     * 授课教师姓名
     */
    private String teacherName;
    
    /**
     * 上课时间(如: 周一1-2节)
     */
    private String schedule;
    
    /**
     * 上课地点
     */
    private String location;
    
    /**
     * 课程容量
     */
    private Integer capacity;
    
    /**
     * 已选人数
     */
    private Integer enrolledCount;
    
    /**
     * 学期(如: 2024-2025-1)
     */
    private String semester;
    
    /**
     * 课程类别: REQUIRED-必修, ELECTIVE-选修, GENERAL-通识
     */
    private String category;
    
    /**
     * 开课院系
     */
    private String department;
    
    /**
     * 课程描述
     */
    private String description;
    
    /**
     * 教学大纲
     */
    private String syllabus;
    
    /**
     * 状态: 0-停开, 1-正常
     */
    private Integer status;
    
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
    
    /**
     * 计算可选容量
     */
    public Integer getAvailableCapacity() {
        if (capacity == null || enrolledCount == null) {
            return 0;
        }
        return Math.max(0, capacity - enrolledCount);
    }
    
    /**
     * 课程状态常量
     */
    public static final int STATUS_CLOSED = 0;
    public static final int STATUS_OPEN = 1;
}
