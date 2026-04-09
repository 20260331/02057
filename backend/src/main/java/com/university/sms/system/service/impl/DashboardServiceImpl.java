package com.university.sms.system.service.impl;

import com.university.sms.system.dto.DashboardStatsDTO;
import com.university.sms.system.mapper.DashboardMapper;
import com.university.sms.system.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 首页统计服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {
    
    private final DashboardMapper dashboardMapper;
    
    @Override
    public DashboardStatsDTO getStats() {
        DashboardStatsDTO stats = new DashboardStatsDTO();
        
        // 学生总数（在籍学生）
        stats.setStudentCount(dashboardMapper.countStudents());
        
        // 开设课程数（当前学期）
        stats.setCourseCount(dashboardMapper.countCourses());
        
        // 待审批成绩数
        stats.setPendingGradeCount(dashboardMapper.countPendingGrades());
        
        // 待审批请假数
        stats.setPendingLeaveCount(dashboardMapper.countPendingLeaves());
        
        // 请假中人数
        stats.setOnLeaveCount(dashboardMapper.countOnLeave());
        
        return stats;
    }
}
