package com.university.sms.common.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.io.Serializable;

/**
 * 分页查询参数封装类
 */
@Data
public class PageQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 默认页码
     */
    private static final int DEFAULT_PAGE = 1;

    /**
     * 默认每页大小
     */
    private static final int DEFAULT_SIZE = 10;

    /**
     * 最大每页大小
     */
    private static final int MAX_SIZE = 100;

    /**
     * 当前页码
     */
    @Min(value = 1, message = "页码最小为1")
    private Integer page = DEFAULT_PAGE;

    /**
     * 每页大小
     */
    @Min(value = 1, message = "每页大小最小为1")
    @Max(value = MAX_SIZE, message = "每页大小最大为100")
    private Integer size = DEFAULT_SIZE;

    /**
     * 排序字段
     */
    private String orderBy;

    /**
     * 排序方向：asc/desc
     */
    private String orderDirection = "desc";

    /**
     * 获取偏移量
     */
    public long getOffset() {
        return (long) (page - 1) * size;
    }

    /**
     * 是否升序
     */
    public boolean isAsc() {
        return "asc".equalsIgnoreCase(orderDirection);
    }
}
