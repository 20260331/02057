package com.university.sms.system.controller;

import com.university.sms.common.response.Result;
import com.university.sms.system.dto.DashboardStatsDTO;
import com.university.sms.system.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 首页统计控制器
 */
@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    
    private final DashboardService dashboardService;
    
    /**
     * 获取首页统计数据
     */
    @GetMapping("/stats")
    public Result<DashboardStatsDTO> getStats() {
        DashboardStatsDTO stats = dashboardService.getStats();
        return Result.success(stats);
    }
}
