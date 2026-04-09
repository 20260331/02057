package com.university.sms.leave.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 请假附件实体
 */
@Data
@TableName("lev_leave_attachment")
public class LeaveAttachment {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long leaveRequestId;
    private String fileName;
    private String filePath;
    private Long fileSize;
    private String fileType;
    private Long uploadedBy;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
