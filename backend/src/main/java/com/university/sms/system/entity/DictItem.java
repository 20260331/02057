package com.university.sms.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 数据字典项实体
 */
@Data
@TableName("sys_dict_item")
public class DictItem {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long dictTypeId;
    private String dictCode;
    private String itemValue;
    private String itemLabel;
    private String description;
    private String cssClass;
    private Integer status;
    private Integer sortOrder;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
