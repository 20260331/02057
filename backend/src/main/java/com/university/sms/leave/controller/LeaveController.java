package com.university.sms.leave.controller;

import com.university.sms.common.response.Result;
import com.university.sms.leave.dto.*;
import com.university.sms.leave.service.LeaveService;
import com.university.sms.security.annotation.RequirePermission;
import com.university.sms.student.dto.StudentDTO;
import com.university.sms.student.service.StudentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 请假控制器
 */
@RestController
@RequestMapping("/leaves")
@RequiredArgsConstructor
public class LeaveController {
    
    private final LeaveService leaveService;
    private final StudentService studentService;
    
    /**
     * 提交请假申请
     */
    @PostMapping
    public Result<Long> submitLeave(@Valid @RequestBody LeaveRequestDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        StudentDTO student = studentService.getByUserIdOrNull(userId);
        
        if (student == null) {
            return Result.error(400, "只有学生才能提交请假申请");
        }
        
        Long leaveId = leaveService.submitLeave(student.getId(), dto);
        return Result.success(leaveId);
    }
    
    /**
     * 获取请假详情
     */
    @GetMapping("/{id}")
    public Result<LeaveInfoDTO> getLeaveById(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) request.getAttribute("roles");
        
        LeaveInfoDTO leave = leaveService.getLeaveById(id);
        
        // 权限校验：学生只能看自己的，管理员/审批人可以看所有
        StudentDTO student = studentService.getByUserIdOrNull(userId);
        boolean isAdminOrApprover = roles != null && (roles.contains("ADMIN") || roles.contains("ACADEMIC_AFFAIRS") || roles.contains("COUNSELOR") || roles.contains("DEPARTMENT_HEAD"));
        
        if (!isAdminOrApprover && (student == null || !student.getId().equals(leave.getStudentId()))) {
            return Result.error(403, "无权查看他人的请假记录");
        }
        
        return Result.success(leave);
    }
    
    /**
     * 获取我的请假记录
     */
    @GetMapping("/my")
    public Result<List<LeaveInfoDTO>> getMyLeaves(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        StudentDTO student = studentService.getByUserIdOrNull(userId);
        
        if (student == null) {
            return Result.error(400, "只有学生才能查看请假记录");
        }
        
        List<LeaveInfoDTO> leaves = leaveService.getStudentLeaves(student.getId());
        return Result.success(leaves);
    }
    
    /**
     * 获取待审批列表（辅导员）
     */
    @GetMapping("/pending/counselor")
    @RequirePermission("leave:approve")
    public Result<List<LeaveInfoDTO>> getPendingForCounselor(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) request.getAttribute("roles");
        
        // 管理员可以看到所有待审批请假
        if (roles != null && (roles.contains("ADMIN") || roles.contains("ACADEMIC_AFFAIRS"))) {
            List<LeaveInfoDTO> leaves = leaveService.getAllPending();
            return Result.success(leaves);
        }
        
        // 辅导员只能看到自己负责学生的待审批请假
        List<LeaveInfoDTO> leaves = leaveService.getPendingForCounselor(userId);
        return Result.success(leaves);
    }

    /**
     * 获取紧急待审批列表（辅导员-快速通道）
     */
    @GetMapping("/pending/counselor/urgent")
    @RequirePermission("leave:approve")
    public Result<List<LeaveInfoDTO>> getUrgentPendingForCounselor(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) request.getAttribute("roles");

        // 管理员/教务处可以看到所有紧急待审批请假
        if (roles != null && (roles.contains("ADMIN") || roles.contains("ACADEMIC_AFFAIRS"))) {
            List<LeaveInfoDTO> leaves = leaveService.getAllPending().stream()
                    .filter(l -> Boolean.TRUE.equals(l.getUrgent()))
                    .toList();
            return Result.success(leaves);
        }

        // 辅导员只能看到自己负责学生的紧急待审批请假
        List<LeaveInfoDTO> leaves = leaveService.getUrgentPendingForCounselor(userId);
        return Result.success(leaves);
    }
    
    /**
     * 获取待审批列表（院系领导）
     */
    @GetMapping("/pending/department")
    @RequirePermission("leave:approve:department")
    public Result<List<LeaveInfoDTO>> getPendingForDepartmentHead() {
        List<LeaveInfoDTO> leaves = leaveService.getPendingForDepartmentHead();
        return Result.success(leaves);
    }

    /**
     * 获取紧急待审批列表（院系领导-快速通道）
     */
    @GetMapping("/pending/department/urgent")
    @RequirePermission("leave:approve:department")
    public Result<List<LeaveInfoDTO>> getUrgentPendingForDepartmentHead() {
        List<LeaveInfoDTO> leaves = leaveService.getUrgentPendingForDepartmentHead();
        return Result.success(leaves);
    }
    
    /**
     * 审批请假
     */
    @PutMapping("/{id}/approve")
    @RequirePermission("leave:approve")
    public Result<Void> approveLeave(@PathVariable Long id,
                                     @Valid @RequestBody LeaveApprovalDTO dto,
                                     HttpServletRequest request) {
        Long approverId = (Long) request.getAttribute("userId");
        String approverName = (String) request.getAttribute("username");
        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) request.getAttribute("roles");
        
        String approverRole = "COUNSELOR";
        if (roles != null && roles.contains("DEPARTMENT_HEAD")) {
            approverRole = "DEPARTMENT_HEAD";
        }
        
        leaveService.approveLeave(id, dto, approverId, approverName, approverRole);
        return Result.success();
    }
    
    /**
     * 销假
     */
    @PutMapping("/{id}/return")
    public Result<Void> registerReturn(@PathVariable Long id, @RequestBody ReturnDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) request.getAttribute("roles");
        
        LeaveInfoDTO leave = leaveService.getLeaveById(id);
        
        // 权限校验：学生只能销自己的假，管理员/审批人可以销所有人的假
        StudentDTO student = studentService.getByUserIdOrNull(userId);
        boolean isAdminOrApprover = roles != null && (roles.contains("ADMIN") || roles.contains("ACADEMIC_AFFAIRS") || roles.contains("COUNSELOR") || roles.contains("DEPARTMENT_HEAD"));
        
        if (!isAdminOrApprover && (student == null || !student.getId().equals(leave.getStudentId()))) {
            return Result.error(403, "无权对他人的请假记录进行销假操作");
        }
        
        leaveService.registerReturn(id, dto.getReturnDate());
        return Result.success();
    }

    /**
     * 请假统计与分析
     */
    @GetMapping("/statistics")
    @RequirePermission("leave:approve")
    public Result<LeaveStatisticsDTO> getStatistics(@RequestParam(required = false) LocalDate startDate,
                                                    @RequestParam(required = false) LocalDate endDate) {
        LeaveStatisticsDTO stats = leaveService.getStatistics(startDate, endDate);
        return Result.success(stats);
    }
    
    /**
     * 销假请求 DTO
     */
    @lombok.Data
    public static class ReturnDTO {
        private LocalDate returnDate;
    }
}
