package com.university.sms.system.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class NotificationTemplateSaveDTO {

    @NotBlank(message = "模板编码不能为空")
    private String templateCode;

    @NotBlank(message = "模板名称不能为空")
    private String templateName;

    @NotBlank(message = "模板分类不能为空")
    private String category;

    @NotBlank(message = "通知标题模板不能为空")
    private String subject;

    @NotBlank(message = "通知内容模板不能为空")
    private String content;

    private List<String> variables;

    private String channel;
}
