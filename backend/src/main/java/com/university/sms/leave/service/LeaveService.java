package com.university.sms.leave.service;

import com.university.sms.leave.dto.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 请假服务接口
 */
public interface LeaveService {
    
    /**
     * 提交请假申请
     */
    Long submitLeave(Long studentId, LeaveRequestDTO dto);
    
    /**
     * 获取请假详情
     */
    LeaveInfoDTO getLeaveById(Long id);

    /**
     * 获取请假详情（带学生身份验证）
     * @param id 请假ID
     * @param studentId 学生ID（用于验证所有权）
     */
    LeaveInfoDTO getLeaveById(Long id, Long studentId);
    
    /**
     * 获取学生的请假记录
     */
    List<LeaveInfoDTO> getStudentLeaves(Long studentId);
    
    /**
     * 获取待审批列表（辅导员）
     */
    List<LeaveInfoDTO> getPendingForCounselor(Long counselorId);

    /**
     * 获取紧急待审批列表（辅导员-快速通道）
     */
    List<LeaveInfoDTO> getUrgentPendingForCounselor(Long counselorId);
    
    /**
     * 获取所有待审批列表（管理员）
     */
    List<LeaveInfoDTO> getAllPending();
    
    /**
     * 获取待审批列表（院系领导）
     */
    List<LeaveInfoDTO> getPendingForDepartmentHead();

    /**
     * 获取紧急待审批列表（院系领导-快速通道）
     */
    List<LeaveInfoDTO> getUrgentPendingForDepartmentHead();
    
    /**
     * 审批请假
     */
    void approveLeave(Long leaveId, LeaveApprovalDTO dto, Long approverId, String approverName, String approverRole);
    
    /**
     * 销假
     */
    void registerReturn(Long leaveId, LocalDate returnDate);

    /**
     * 销假（带学生身份验证）
     * @param leaveId 请假ID
     * @param returnDate 销假日期
     * @param studentId 学生ID（用于验证所有权）
     */
    void registerReturn(Long leaveId, LocalDate returnDate, Long studentId);
    
    /**
     * 获取未按时销假的记录
     */
    List<LeaveInfoDTO> getOverdueReturns();

    /**
     * 请假统计与分析
     *
     * @param startDate 统计开始日期（可选）
     * @param endDate   统计结束日期（可选）
     */
    LeaveStatisticsDTO getStatistics(LocalDate startDate, LocalDate endDate);
}
