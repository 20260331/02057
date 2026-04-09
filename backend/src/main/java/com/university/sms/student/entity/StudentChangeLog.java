package com.university.sms.student.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 学生信息变更日志实体
 */
@Data
@TableName("stu_student_change_log")
public class StudentChangeLog {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 学生ID
     */
    private Long studentId;
    
    /**
     * 变更字段名
     */
    private String fieldName;
    
    /**
     * 变更字段标签
     */
    private String fieldLabel;
    
    /**
     * 旧值
     */
    private String oldValue;
    
    /**
     * 新值
     */
    private String newValue;
    
    /**
     * 变更类型: UPDATE-修改, STATUS_CHANGE-状态变更
     */
    private String changeType;
    
    /**
     * 变更原因
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
    
    /**
     * 变更类型常量
     */
    public static final String TYPE_UPDATE = "UPDATE";
    public static final String TYPE_STATUS_CHANGE = "STATUS_CHANGE";
}
