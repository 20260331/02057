package com.university.sms.student.service;

import com.university.sms.student.dto.StatusChangeApplicationDTO;
import com.university.sms.student.dto.StatusChangeApprovalDTO;
import com.university.sms.student.dto.StatusChangeInfoDTO;

import java.util.List;

/**
 * 学籍异动服务接口
 */
public interface StatusChangeService {

    /**
     * 学生提交学籍异动申请
     */
    Long submitApplication(Long studentId, StatusChangeApplicationDTO dto);

    /**
     * 获取申请详情
     */
    StatusChangeInfoDTO getApplicationById(Long id);

    /**
     * 获取学生的申请记录
     */
    List<StatusChangeInfoDTO> getStudentApplications(Long studentId);

    /**
     * 获取待辅导员审批的申请列表
     */
    List<StatusChangeInfoDTO> getPendingForCounselor(Long counselorId);

    /**
     * 获取所有待审批的申请（管理员/教务处）
     */
    List<StatusChangeInfoDTO> getAllPending();

    /**
     * 获取待教务处审批的申请（辅导员已批，需二级审批）
     */
    List<StatusChangeInfoDTO> getPendingForAcademicAffairs();

    /**
     * 审批学籍异动申请
     */
    void approveApplication(Long applicationId, StatusChangeApprovalDTO dto,
                            Long approverId, String approverName, String approverRole);

    /**
     * 学生取消申请
     */
    void cancelApplication(Long applicationId, Long studentId);
}
