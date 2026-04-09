package com.university.sms.leave.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 请假统计与分析 DTO
 */
@Data
public class LeaveStatisticsDTO {

    /**
     * 统计区间内请假总数
     */
    private Long totalLeaves;

    private Long pendingLeaves;
    private Long approvedLeaves;
    private Long rejectedLeaves;
    private Long completedLeaves;
    private Long urgentLeaves;

    /**
     * 统计区间内总请假天数
     */
    private BigDecimal totalDays;

    /**
     * 今日在请假中的学生人数（缺勤关联）
     */
    private Long todayOnLeaveCount;

    /**
     * 未按时销假的请假记录数（缺勤风险）
     */
    private Long overdueReturnCount;

    /**
     * 按请假类型统计
     */
    private List<TypeStat> byType;

    /**
     * 按院系统计
     */
    private List<DepartmentStat> byDepartment;

    @Data
    public static class TypeStat {
        private String leaveType;
        private String leaveTypeText;
        private Long leaveCount;
        private BigDecimal totalDays;
    }

    @Data
    public static class DepartmentStat {
        private String department;
        private Long leaveCount;
        private Long studentCount;
        private BigDecimal totalDays;
    }
}

