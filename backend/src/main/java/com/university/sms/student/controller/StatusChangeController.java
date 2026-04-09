package com.university.sms.student.controller;

import com.university.sms.common.response.Result;
import com.university.sms.security.annotation.RequirePermission;
import com.university.sms.student.dto.*;
import com.university.sms.student.service.StatusChangeService;
import com.university.sms.student.service.StudentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 学籍异动控制器
 */
@RestController
@RequestMapping("/status-changes")
@RequiredArgsConstructor
public class StatusChangeController {

    private final StatusChangeService statusChangeService;
    private final StudentService studentService;

    /**
     * 学生提交学籍异动申请
     */
    @PostMapping
    public Result<Long> submitApplication(@Valid @RequestBody StatusChangeApplicationDTO dto,
                                          HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        StudentDTO student = studentService.getByUserIdOrNull(userId);
        if (student == null) {
            return Result.error(400, "只有学生才能提交学籍异动申请");
        }
        Long applicationId = statusChangeService.submitApplication(student.getId(), dto);
        return Result.success(applicationId);
    }

    /**
     * 获取申请详情
     */
    @GetMapping("/{id}")
    public Result<StatusChangeInfoDTO> getApplicationById(@PathVariable Long id) {
        StatusChangeInfoDTO info = statusChangeService.getApplicationById(id);
        return Result.success(info);
    }

    /**
     * 获取我的学籍异动申请记录
     */
    @GetMapping("/my")
    public Result<List<StatusChangeInfoDTO>> getMyApplications(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        StudentDTO student = studentService.getByUserIdOrNull(userId);
        if (student == null) {
            return Result.error(400, "只有学生才能查看学籍异动申请");
        }
        List<StatusChangeInfoDTO> list = statusChangeService.getStudentApplications(student.getId());
        return Result.success(list);
    }

    /**
     * 获取待审批列表（辅导员）
     */
    @GetMapping("/pending/counselor")
    @RequirePermission("student:status:approve")
    public Result<List<StatusChangeInfoDTO>> getPendingForCounselor(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) request.getAttribute("roles");

        if (roles != null && (roles.contains("ADMIN") || roles.contains("ACADEMIC_AFFAIRS"))) {
            List<StatusChangeInfoDTO> list = statusChangeService.getAllPending();
            return Result.success(list);
        }

        List<StatusChangeInfoDTO> list = statusChangeService.getPendingForCounselor(userId);
        return Result.success(list);
    }

    /**
     * 获取待教务处审批列表（辅导员已批的二级审批）
     */
    @GetMapping("/pending/academic")
    @RequirePermission("student:status:approve")
    public Result<List<StatusChangeInfoDTO>> getPendingForAcademicAffairs() {
        List<StatusChangeInfoDTO> list = statusChangeService.getPendingForAcademicAffairs();
        return Result.success(list);
    }

    /**
     * 审批学籍异动申请
     */
    @PutMapping("/{id}/approve")
    @RequirePermission("student:status:approve")
    public Result<Void> approveApplication(@PathVariable Long id,
                                           @Valid @RequestBody StatusChangeApprovalDTO dto,
                                           HttpServletRequest request) {
        Long approverId = (Long) request.getAttribute("userId");
        String approverName = (String) request.getAttribute("username");
        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) request.getAttribute("roles");

        String approverRole = "COUNSELOR";
        if (roles != null && (roles.contains("ADMIN") || roles.contains("ACADEMIC_AFFAIRS"))) {
            approverRole = "ACADEMIC_AFFAIRS";
        } else if (roles != null && roles.contains("DEPARTMENT_HEAD")) {
            approverRole = "DEPARTMENT_HEAD";
        }

        statusChangeService.approveApplication(id, dto, approverId, approverName, approverRole);
        return Result.success();
    }

    /**
     * 学生取消申请
     */
    @PutMapping("/{id}/cancel")
    public Result<Void> cancelApplication(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        StudentDTO student = studentService.getByUserIdOrNull(userId);
        if (student == null) {
            return Result.error(400, "只有学生才能取消申请");
        }
        statusChangeService.cancelApplication(id, student.getId());
        return Result.success();
    }
}
