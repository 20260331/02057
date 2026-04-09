package com.university.sms.student.service.impl;

import com.university.sms.common.exception.BusinessException;
import com.university.sms.student.dto.*;
import com.university.sms.student.entity.StatusChangeApplication;
import com.university.sms.student.entity.StatusChangeApproval;
import com.university.sms.student.entity.Student;
import com.university.sms.student.entity.StudentChangeLog;
import com.university.sms.student.enums.AcademicStatus;
import com.university.sms.student.mapper.StatusChangeApplicationMapper;
import com.university.sms.student.mapper.StatusChangeApprovalMapper;
import com.university.sms.student.mapper.StudentChangeLogMapper;
import com.university.sms.student.mapper.StudentMapper;
import com.university.sms.student.service.StatusChangeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatusChangeServiceImpl implements StatusChangeService {

    private final StatusChangeApplicationMapper applicationMapper;
    private final StatusChangeApprovalMapper approvalMapper;
    private final StudentMapper studentMapper;
    private final StudentChangeLogMapper changeLogMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long submitApplication(Long studentId, StatusChangeApplicationDTO dto) {
        Student student = studentMapper.selectById(studentId);
        if (student == null) {
            throw new BusinessException("学生不存在");
        }

        // 校验状态流转
        AcademicStatus current = AcademicStatus.fromCode(student.getAcademicStatus());
        AcademicStatus target = AcademicStatus.fromCode(dto.getTargetStatus());
        if (!current.canTransitionTo(target)) {
            throw new BusinessException("不允许从 " + current.getDescription() + " 变更为 " + target.getDescription());
        }

        // 检查是否有进行中的申请
        int activeCount = applicationMapper.countActiveByStudentId(studentId);
        if (activeCount > 0) {
            throw new BusinessException("您已有进行中的学籍异动申请，请等待审批完成或取消后再提交");
        }

        StatusChangeApplication application = new StatusChangeApplication();
        application.setStudentId(studentId);
        application.setCurrentStatus(student.getAcademicStatus());
        application.setTargetStatus(dto.getTargetStatus());
        application.setReason(dto.getReason());
        application.setEffectiveDate(dto.getEffectiveDate());
        application.setStatus(StatusChangeApplication.STATUS_PENDING);
        application.setExecuted(false);
        application.setCreatedAt(LocalDateTime.now());

        applicationMapper.insert(application);
        log.info("学生 {} 提交学籍异动申请: {} -> {}", studentId, current.getDescription(), target.getDescription());
        return application.getId();
    }

    @Override
    public StatusChangeInfoDTO getApplicationById(Long id) {
        StatusChangeApplication app = applicationMapper.selectById(id);
        if (app == null) {
            throw new BusinessException("申请记录不存在");
        }
        return toDTO(app);
    }

    @Override
    public List<StatusChangeInfoDTO> getStudentApplications(Long studentId) {
        List<StatusChangeApplication> apps = applicationMapper.selectByStudentId(studentId);
        return apps.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<StatusChangeInfoDTO> getPendingForCounselor(Long counselorId) {
        List<StatusChangeApplication> apps = applicationMapper.selectPendingByCounselor(counselorId);
        return apps.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<StatusChangeInfoDTO> getAllPending() {
        List<StatusChangeApplication> apps = applicationMapper.selectAllPending();
        return apps.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<StatusChangeInfoDTO> getPendingForAcademicAffairs() {
        List<StatusChangeApplication> apps = applicationMapper.selectPendingForAcademicAffairs();
        return apps.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveApplication(Long applicationId, StatusChangeApprovalDTO dto,
                                   Long approverId, String approverName, String approverRole) {
        StatusChangeApplication app = applicationMapper.selectById(applicationId);
        if (app == null) {
            throw new BusinessException("申请记录不存在");
        }

        // 校验审批状态
        if ("COUNSELOR".equals(approverRole)) {
            if (!StatusChangeApplication.STATUS_PENDING.equals(app.getStatus())) {
                throw new BusinessException("该申请当前状态不允许辅导员审批");
            }
        } else {
            // 教务处/管理员：可审批 PENDING 或 COUNSELOR_APPROVED
            if (!StatusChangeApplication.STATUS_PENDING.equals(app.getStatus())
                    && !StatusChangeApplication.STATUS_COUNSELOR_APPROVED.equals(app.getStatus())) {
                throw new BusinessException("该申请当前状态不允许审批");
            }
        }

        // 记录审批
        StatusChangeApproval approval = new StatusChangeApproval();
        approval.setApplicationId(applicationId);
        approval.setApproverId(approverId);
        approval.setApproverName(approverName);
        approval.setApproverRole(approverRole);
        approval.setApprovalOrder(StatusChangeApplication.STATUS_PENDING.equals(app.getStatus()) ? 1 : 2);
        approval.setAction(dto.getApproved() ? "APPROVE" : "REJECT");
        approval.setComment(dto.getComment());
        approval.setCreatedAt(LocalDateTime.now());
        approvalMapper.insert(approval);

        if (!dto.getApproved()) {
            app.setStatus(StatusChangeApplication.STATUS_REJECTED);
        } else if ("COUNSELOR".equals(approverRole)) {
            if (app.needsEscalation()) {
                // 退学/转学 -> 需要教务处进一步审批
                app.setStatus(StatusChangeApplication.STATUS_COUNSELOR_APPROVED);
            } else {
                // 休学/复学 -> 辅导员直接审批通过
                app.setStatus(StatusChangeApplication.STATUS_APPROVED);
                app.setApprovedAt(LocalDateTime.now());
                executeStatusChange(app, approverId, approverName);
            }
        } else {
            // 教务处/管理员审批通过
            app.setStatus(StatusChangeApplication.STATUS_APPROVED);
            app.setApprovedAt(LocalDateTime.now());
            executeStatusChange(app, approverId, approverName);
        }

        applicationMapper.updateById(app);
        log.info("学籍异动申请 {} 审批完成，结果: {}", applicationId, dto.getApproved() ? "批准" : "拒绝");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelApplication(Long applicationId, Long studentId) {
        StatusChangeApplication app = applicationMapper.selectById(applicationId);
        if (app == null) {
            throw new BusinessException("申请记录不存在");
        }
        if (!app.getStudentId().equals(studentId)) {
            throw new BusinessException("只能取消自己的申请");
        }
        if (!StatusChangeApplication.STATUS_PENDING.equals(app.getStatus())) {
            throw new BusinessException("只能取消待审批状态的申请");
        }

        app.setStatus(StatusChangeApplication.STATUS_CANCELLED);
        applicationMapper.updateById(app);
        log.info("学生 {} 取消学籍异动申请 {}", studentId, applicationId);
    }

    /**
     * 审批通过后执行实际的学籍状态变更
     */
    private void executeStatusChange(StatusChangeApplication app, Long operatorId, String operatorName) {
        Student student = studentMapper.selectById(app.getStudentId());
        if (student == null) return;

        AcademicStatus current = AcademicStatus.fromCode(student.getAcademicStatus());
        AcademicStatus target = AcademicStatus.fromCode(app.getTargetStatus());

        // 写入变更日志
        StudentChangeLog changeLog = new StudentChangeLog();
        changeLog.setStudentId(app.getStudentId());
        changeLog.setFieldName("academicStatus");
        changeLog.setFieldLabel("学籍状态");
        changeLog.setOldValue(current.getDescription());
        changeLog.setNewValue(target.getDescription());
        changeLog.setChangeType(StudentChangeLog.TYPE_STATUS_CHANGE);
        changeLog.setReason(app.getReason());
        changeLog.setOperatorId(operatorId);
        changeLog.setOperatorName(operatorName);
        changeLog.setCreatedAt(LocalDateTime.now());
        changeLogMapper.insert(changeLog);

        // 更新学生学籍状态
        student.setAcademicStatus(app.getTargetStatus());
        studentMapper.updateById(student);

        app.setExecuted(true);
        log.info("学生 {} 学籍状态已变更: {} -> {}", app.getStudentId(), current.getDescription(), target.getDescription());
    }

    private StatusChangeInfoDTO toDTO(StatusChangeApplication app) {
        StatusChangeInfoDTO dto = new StatusChangeInfoDTO();
        dto.setId(app.getId());
        dto.setStudentId(app.getStudentId());
        dto.setCurrentStatus(app.getCurrentStatus());
        dto.setCurrentStatusText(StatusChangeInfoDTO.getAcademicStatusText(app.getCurrentStatus()));
        dto.setTargetStatus(app.getTargetStatus());
        dto.setTargetStatusText(StatusChangeInfoDTO.getAcademicStatusText(app.getTargetStatus()));
        dto.setReason(app.getReason());
        dto.setEffectiveDate(app.getEffectiveDate());
        dto.setStatus(app.getStatus());
        dto.setStatusText(StatusChangeInfoDTO.getApplicationStatusText(app.getStatus()));
        dto.setExecuted(app.getExecuted());
        dto.setApprovedAt(app.getApprovedAt());
        dto.setCreatedAt(app.getCreatedAt());

        // 填充学生信息
        Student student = studentMapper.selectById(app.getStudentId());
        if (student != null) {
            dto.setStudentNo(student.getStudentNo());
            dto.setStudentName(student.getName());
            dto.setDepartment(student.getDepartment());
            dto.setClassNo(student.getClassNo());
        }

        // 填充审批历史
        List<StatusChangeApproval> approvals = approvalMapper.selectByApplicationId(app.getId());
        dto.setApprovalHistory(approvals.stream().map(a -> {
            StatusChangeInfoDTO.ApprovalRecord record = new StatusChangeInfoDTO.ApprovalRecord();
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
}
