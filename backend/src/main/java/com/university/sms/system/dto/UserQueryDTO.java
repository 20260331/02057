package com.university.sms.system.dto;

import com.university.sms.common.dto.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户查询 DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserQueryDTO extends PageQuery {
    private String username;
    private String realName;
    private Integer status;
}
