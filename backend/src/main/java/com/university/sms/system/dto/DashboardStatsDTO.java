package com.university.sms.system.dto;

import lombok.Data;

/**
 * 首页统计数据 DTO
 */
@Data
public class DashboardStatsDTO {
    /**
     * 学生总数
     */
    private Long studentCount;
    
    /**
     * 开设课程数
     */
    private Long courseCount;
    
    /**
     * 待审批成绩数
     */
    private Long pendingGradeCount;
    
    /**
     * 待审批请假数
     */
    private Long pendingLeaveCount;
    
    /**
     * 请假中人数
     */
    private Long onLeaveCount;
}
