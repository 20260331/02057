package com.university.sms.course.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 抽签课程信息 DTO
 */
@Data
public class LotteryCourseDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long courseId;
    private String courseCode;
    private String courseName;
    private BigDecimal credits;
    private String teacherName;
    private String schedule;
    private String location;
    private String semester;
    private String category;
    private Integer capacity;
    private Integer enrolledCount;
    /** 待抽签人数 */
    private Integer pendingCount;
    /** 是否已执行过抽签 */
    private Boolean lotteryExecuted;
}
