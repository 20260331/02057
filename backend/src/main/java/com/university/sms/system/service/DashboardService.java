package com.university.sms.system.service;

import com.university.sms.system.dto.DashboardStatsDTO;

/**
 * 首页统计服务接口
 */
public interface DashboardService {
    
    /**
     * 获取首页统计数据
     */
    DashboardStatsDTO getStats();
}
