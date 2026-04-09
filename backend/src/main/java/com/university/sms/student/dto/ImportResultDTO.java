package com.university.sms.student.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 导入结果 DTO
 */
@Data
public class ImportResultDTO {
    
    /**
     * 总行数
     */
    private int totalCount;
    
    /**
     * 成功数
     */
    private int successCount;
    
    /**
     * 失败数
     */
    private int failCount;
    
    /**
     * 错误列表
     */
    private List<ImportError> errors = new ArrayList<>();
    
    /**
     * 添加错误
     */
    public void addError(int rowNum, String field, String message) {
        errors.add(new ImportError(rowNum, field, message));
        failCount++;
    }
    
    /**
     * 增加成功数
     */
    public void incrementSuccess() {
        successCount++;
    }
    
    /**
     * 导入错误详情
     */
    @Data
    public static class ImportError {
        private int rowNum;
        private String field;
        private String message;
        
        public ImportError(int rowNum, String field, String message) {
            this.rowNum = rowNum;
            this.field = field;
            this.message = message;
        }
    }
}
