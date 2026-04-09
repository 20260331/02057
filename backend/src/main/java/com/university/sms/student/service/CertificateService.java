package com.university.sms.student.service;

import com.university.sms.student.dto.CertificateInfoDTO;
import com.university.sms.student.dto.CertificateRequestDTO;

import java.util.List;

/**
 * 学籍证明服务接口
 */
public interface CertificateService {

    /**
     * 生成学籍证明
     */
    CertificateInfoDTO generateCertificate(Long studentId, CertificateRequestDTO dto,
                                            Long operatorId, String operatorName);

    /**
     * 获取证明详情
     */
    CertificateInfoDTO getCertificateById(Long id);

    /**
     * 根据证明编号查询（用于验证）
     */
    CertificateInfoDTO getCertificateByCertNo(String certNo);

    /**
     * 获取学生的证明记录列表
     */
    List<CertificateInfoDTO> getStudentCertificates(Long studentId);

    /**
     * 获取所有证明记录（管理员）
     */
    List<CertificateInfoDTO> getRecentCertificates(int limit);

    /**
     * 作废证明
     */
    void revokeCertificate(Long id, String reason, Long operatorId);

    /**
     * 获取可用的证明类型列表（基于学生当前状态）
     */
    List<String> getAvailableCertTypes(Long studentId);
}
