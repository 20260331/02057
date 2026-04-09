package com.university.sms.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 数据字典类型实体
 */
@Data
@TableName("sys_dict_type")
public class DictType {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String dictCode;
    private String dictName;
    private String description;
    private Integer isSystem;
    private Integer status;
    private Integer sortOrder;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
