package com.university.sms.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

@Data
public class NotificationSendDTO {

    /** 模板ID（使用模板发送时填写；自定义发送时可为空） */
    private Long templateId;

    /** 自定义标题（不使用模板时必填） */
    private String title;

    /** 自定义内容（不使用模板时必填） */
    private String content;

    /** 通知分类 */
    private String category;

    /** 优先级: LOW, NORMAL, HIGH, URGENT */
    private String priority;

    /** 目标类型: ALL, ROLE, USER */
    @NotBlank(message = "目标类型不能为空")
    private String targetType;

    /** 目标值（逗号分隔的角色编码或用户ID） */
    private String targetValue;

    /** 模板变量替换（key -> value） */
    private Map<String, String> variables;
}
