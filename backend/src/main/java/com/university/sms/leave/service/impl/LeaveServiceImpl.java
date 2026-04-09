package com.university.sms.leave.service.impl;

import com.university.sms.common.exception.BusinessException;
import com.university.sms.leave.dto.*;
import com.university.sms.leave.entity.LeaveApproval;
import com.university.sms.leave.entity.LeaveRequest;
import com.university.sms.leave.mapper.LeaveApprovalMapper;
import com.university.sms.leave.mapper.LeaveRequestMapper;
import com.university.sms.leave.service.LeaveService;
import com.university.sms.student.entity.Student;
import com.university.sms.student.mapper.StudentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 请假服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LeaveServiceImpl implements LeaveService {
    
    private final LeaveRequestMapper leaveRequestMapper;
    private final LeaveApprovalMapper approvalMapper;
    private final StudentMapper studentMapper;
    
    // 需要院系领导审批的天数阈值
    private static final int ESCALATION_DAYS = 3;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long submitLeave(Long studentId, LeaveRequestDTO dto) {
        // 验证日期
        if (dto.getEndDate().isBefore(dto.getStartDate())) {
            throw new BusinessException("结束日期不能早于开始日期");
        }
        
        // 计算请假时长
        long days = ChronoUnit.DAYS.between(dto.getStartDate(), dto.getEndDate()) + 1;
        
        LeaveRequest leave = new LeaveRequest();
        leave.setStudentId(studentId);
        leave.setLeaveType(dto.getLeaveType());
        leave.setStartDate(dto.getStartDate());
        leave.setEndDate(dto.getEndDate());
        leave.setDuration(new BigDecimal(days));
        leave.setReason(dto.getReason());
        leave.setUrgent(dto.getUrgent());
        leave.setStatus(LeaveRequest.STATUS_PENDING);
        leave.setCreatedAt(LocalDateTime.now());
        
        leaveRequestMapper.insert(leave);
        
        log.info("学生 {} 提交请假申请，时长 {} 天", studentId, days);
        return leave.getId();
    }
    
    @Override
    public LeaveInfoDTO getLeaveById(Long id) {
        LeaveRequest leave = leaveRequestMapper.selectById(id);
        if (leave == null) {
            throw new BusinessException("请假记录不存在");
        }
        return toDTO(leave);
    }
    
    @Override
    public List<LeaveInfoDTO> getStudentLeaves(Long studentId) {
        List<LeaveRequest> leaves = leaveRequestMapper.selectByStudentId(studentId);
        return leaves.stream().map(this::toDTO).collect(Collectors.toList());
    }
    
    @Override
    public List<LeaveInfoDTO> getPendingForCounselor(Long counselorId) {
        List<LeaveRequest> leaves = leaveRequestMapper.selectPendingByCounselor(counselorId);
        log.info("DB pending leaves for counselor {}: {}",
                counselorId,
                leaves.stream()
                        .map(l -> String.format("id=%d,status=%s,urgent=%s",
                                l.getId(), l.getStatus(), String.valueOf(l.getUrgent())))
                        .toList());
        return leaves.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<LeaveInfoDTO> getUrgentPendingForCounselor(Long counselorId) {
        List<LeaveRequest> leaves = leaveRequestMapper.selectPendingByCounselor(counselorId);
        return leaves.stream()
                .filter(l -> Boolean.TRUE.equals(l.getUrgent()))
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<LeaveInfoDTO> getAllPending() {
        List<LeaveRequest> leaves = leaveRequestMapper.selectAllPending();
        log.info("DB all pending leaves (admin/AA): {}",
                leaves.stream()
                        .map(l -> String.format("id=%d,status=%s,urgent=%s",
                                l.getId(), l.getStatus(), String.valueOf(l.getUrgent())))
                        .toList());
        return leaves.stream().map(this::toDTO).collect(Collectors.toList());
    }
    
    @Override
    public List<LeaveInfoDTO> getPendingForDepartmentHead() {
        List<LeaveRequest> leaves = leaveRequestMapper.selectPendingForDepartmentHead();
        return leaves.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<LeaveInfoDTO> getUrgentPendingForDepartmentHead() {
        List<LeaveRequest> leaves = leaveRequestMapper.selectPendingForDepartmentHead();
        return leaves.stream()
                .filter(l -> Boolean.TRUE.equals(l.getUrgent()))
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveLeave(Long leaveId, LeaveApprovalDTO dto, Long approverId, String approverName, String approverRole) {
        LeaveRequest leave = leaveRequestMapper.selectById(leaveId);
        if (leave == null) {
            throw new BusinessException("请假记录不存在");
        }
        
        // 记录审批
        LeaveApproval approval = new LeaveApproval();
        approval.setLeaveRequestId(leaveId);
        approval.setApproverId(approverId);
        approval.setApproverName(approverName);
        approval.setApproverRole(approverRole);
        approval.setApprovalOrder(1); // 简化处理，实际应该查询当前审批顺序
        approval.setAction(dto.getApproved() ? "APPROVE" : "REJECT");
        approval.setComment(dto.getComment());
        approval.setCreatedAt(LocalDateTime.now());
        approvalMapper.insert(approval);
        
        // 更新请假状态
        if (!dto.getApproved()) {
            // 拒绝
            leave.setStatus(LeaveRequest.STATUS_REJECTED);
        } else if ("COUNSELOR".equals(approverRole)) {
            // 辅导员批准
            if (leave.getDuration().intValue() > ESCALATION_DAYS) {
                // 超过阈值，需要院系领导审批
                leave.setStatus(LeaveRequest.STATUS_COUNSELOR_APPROVED);
            } else {
                // 直接批准
                leave.setStatus(LeaveRequest.STATUS_APPROVED);
            }
        } else {
            // 院系领导批准
            leave.setStatus(LeaveRequest.STATUS_APPROVED);
        }
        
        leaveRequestMapper.updateById(leave);
        log.info("请假 {} 审批完成，结果: {}", leaveId, dto.getApproved() ? "批准" : "拒绝");
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void registerReturn(Long leaveId, LocalDate returnDate) {
        LeaveRequest leave = leaveRequestMapper.selectById(leaveId);
        if (leave == null) {
            throw new BusinessException("请假记录不存在");
        }
        
        if (!LeaveRequest.STATUS_APPROVED.equals(leave.getStatus())) {
            throw new BusinessException("只有已批准的请假才能销假");
        }
        
        leave.setReturnDate(returnDate);
        leave.setStatus(LeaveRequest.STATUS_COMPLETED);
        leaveRequestMapper.updateById(leave);
        
        log.info("请假 {} 已销假", leaveId);
    }
    
    @Override
    public List<LeaveInfoDTO> getOverdueReturns() {
        List<LeaveRequest> leaves = leaveRequestMapper.selectOverdueReturns();
        return leaves.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public LeaveStatisticsDTO getStatistics(LocalDate startDate, LocalDate endDate) {
        LeaveStatisticsDTO stats = new LeaveStatisticsDTO();

        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = endDate != null ? endDate.atTime(23, 59, 59) : null;

        Long total = leaveRequestMapper.countByPeriod(startDateTime, endDateTime);
        stats.setTotalLeaves(total != null ? total : 0L);

        Map<String, Long> statusCounts = leaveRequestMapper.countStatusByPeriod(startDateTime, endDateTime);
        stats.setPendingLeaves(nullSafeLong(statusCounts, "pendingCount", "pendingcount"));
        stats.setApprovedLeaves(nullSafeLong(statusCounts, "approvedCount", "approvedcount"));
        stats.setRejectedLeaves(nullSafeLong(statusCounts, "rejectedCount", "rejectedcount"));
        stats.setCompletedLeaves(nullSafeLong(statusCounts, "completedCount", "completedcount"));
        stats.setUrgentLeaves(nullSafeLong(statusCounts, "urgentCount", "urgentcount"));

        BigDecimal totalDaysSum = leaveRequestMapper.sumDurationByPeriod(startDateTime, endDateTime);
        stats.setTotalDays(totalDaysSum != null ? totalDaysSum : BigDecimal.ZERO);

        // 缺勤关联信息
        Long todayOnLeave = leaveRequestMapper.countTodayOnLeave();
        stats.setTodayOnLeaveCount(todayOnLeave != null ? todayOnLeave : 0L);
        stats.setOverdueReturnCount((long) getOverdueReturns().size());

        // 按类型统计（兼容 Map 键名大小写及空值）
        List<Map<String, Object>> typeRows = leaveRequestMapper.statsByType(startDateTime, endDateTime);
        List<LeaveStatisticsDTO.TypeStat> typeStats = (typeRows != null ? typeRows : List.<Map<String, Object>>of()).stream().map(row -> {
            LeaveStatisticsDTO.TypeStat t = new LeaveStatisticsDTO.TypeStat();
            String type = getMapString(row, "leaveType");
            t.setLeaveType(type);
            t.setLeaveTypeText(LeaveInfoDTO.getLeaveTypeText(type));
            t.setLeaveCount(getMapLong(row, "leaveCount"));
            t.setTotalDays(getMapBigDecimal(row, "totalDays"));
            return t;
        }).collect(Collectors.toList());
        stats.setByType(typeStats);

        // 按院系统计（兼容 Map 键名大小写及空值）
        List<Map<String, Object>> deptRows = leaveRequestMapper.statsByDepartment(startDateTime, endDateTime);
        List<LeaveStatisticsDTO.DepartmentStat> deptStats = (deptRows != null ? deptRows : List.<Map<String, Object>>of()).stream().map(row -> {
            LeaveStatisticsDTO.DepartmentStat d = new LeaveStatisticsDTO.DepartmentStat();
            d.setDepartment(getMapString(row, "department"));
            d.setLeaveCount(getMapLong(row, "leaveCount"));
            d.setStudentCount(getMapLong(row, "studentCount"));
            d.setTotalDays(getMapBigDecimal(row, "totalDays"));
            return d;
        }).collect(Collectors.toList());
        stats.setByDepartment(deptStats);

        return stats;
    }
    
    // ========== 私有方法 ==========
    
    private LeaveInfoDTO toDTO(LeaveRequest leave) {
        LeaveInfoDTO dto = new LeaveInfoDTO();
        dto.setId(leave.getId());
        dto.setStudentId(leave.getStudentId());
        dto.setLeaveType(leave.getLeaveType());
        dto.setLeaveTypeText(LeaveInfoDTO.getLeaveTypeText(leave.getLeaveType()));
        dto.setStartDate(leave.getStartDate());
        dto.setEndDate(leave.getEndDate());
        dto.setDuration(leave.getDuration());
        dto.setReason(leave.getReason());
        dto.setStatus(leave.getStatus());
        dto.setStatusText(LeaveInfoDTO.getStatusText(leave.getStatus()));
        dto.setUrgent(leave.getUrgent());
        dto.setReturnDate(leave.getReturnDate());
        dto.setCreatedAt(leave.getCreatedAt());
        
        // 填充学生信息
        Student student = studentMapper.selectById(leave.getStudentId());
        if (student != null) {
            dto.setStudentNo(student.getStudentNo());
            dto.setStudentName(student.getName());
            dto.setDepartment(student.getDepartment());
            dto.setClassNo(student.getClassNo());
        }
        
        // 填充审批历史
        List<LeaveApproval> approvals = approvalMapper.selectByLeaveRequestId(leave.getId());
        dto.setApprovalHistory(approvals.stream().map(a -> {
            LeaveInfoDTO.ApprovalRecord record = new LeaveInfoDTO.ApprovalRecord();
            record.setId(a.getId());
            record.setApproverId(a.getApproverId());
            record.setApproverName(a.getApproverName());
            record.setApproverRole(a.getApproverRole());
            record.setApproved(a.getApproved());
            record.setComment(a.getComment());
            record.setCreatedAt(a.getCreatedAt());
            return record;
        }).collect(Collectors.toList()));
        
        return dto;
    }

    /** 从 MyBatis Map 结果取字符串（兼容键名大小写） */
    private static String getMapString(Map<String, Object> row, String key) {
        Object v = getMapValue(row, key);
        return v != null ? v.toString() : "";
    }

    /** 从 MyBatis Map 结果取 Long（空值返回 0） */
    private static long getMapLong(Map<String, Object> row, String key) {
        Object v = getMapValue(row, key);
        if (v instanceof Number) {
            return ((Number) v).longValue();
        }
        return 0L;
    }

    /** 从 MyBatis Map 结果取 BigDecimal（空值返回 ZERO） */
    private static BigDecimal getMapBigDecimal(Map<String, Object> row, String key) {
        Object v = getMapValue(row, key);
        if (v instanceof BigDecimal) {
            return (BigDecimal) v;
        }
        if (v instanceof Number) {
            return BigDecimal.valueOf(((Number) v).doubleValue());
        }
        return BigDecimal.ZERO;
    }

    /** 兼容 MySQL 返回的列名大小写（camelCase / 首字母小写 / 全小写） */
    private static Object getMapValue(Map<String, Object> row, String key) {
        Object v = row.get(key);
        if (v != null) return v;
        if (key.length() > 0) {
            v = row.get(key.substring(0, 1).toLowerCase() + key.substring(1));
        }
        if (v != null) return v;
        v = row.get(key.toLowerCase());
        return v;
    }

    @SuppressWarnings("rawtypes")
    private static long nullSafeLong(Map map, String key1, String key2) {
        if (map == null) return 0L;
        Object v = map.get(key1);
        if (v == null) v = map.get(key2);
        if (v instanceof Number) return ((Number) v).longValue();
        return 0L;
    }
}
