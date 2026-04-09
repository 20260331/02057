package com.university.sms.student.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 学籍证明记录实体
 */
@Data
@TableName("stu_certificate_record")
public class CertificateRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long studentId;

    private String certNo;

    private String certType;

    private String certTitle;

    private String certContent;

    private String purpose;

    private Integer copies;

    private String status;

    private Long generatedBy;

    private String generatedByName;

    private LocalDateTime revokedAt;

    private String revokeReason;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    public static final String STATUS_VALID = "VALID";
    public static final String STATUS_REVOKED = "REVOKED";
}
