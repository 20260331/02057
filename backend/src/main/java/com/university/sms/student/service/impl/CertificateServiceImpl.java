package com.university.sms.student.service.impl;

import com.university.sms.common.exception.BusinessException;
import com.university.sms.grade.dto.GradeDTO;
import com.university.sms.grade.service.GradeService;
import com.university.sms.student.dto.CertificateInfoDTO;
import com.university.sms.student.dto.CertificateRequestDTO;
import com.university.sms.student.entity.CertificateRecord;
import com.university.sms.student.entity.Student;
import com.university.sms.student.enums.AcademicStatus;
import com.university.sms.student.enums.CertificateType;
import com.university.sms.student.mapper.CertificateRecordMapper;
import com.university.sms.student.mapper.StudentMapper;
import com.university.sms.student.service.CertificateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CertificateServiceImpl implements CertificateService {

    private final CertificateRecordMapper certificateMapper;
    private final StudentMapper studentMapper;
    private final GradeService gradeService;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy年MM月dd日");
    private static final String SCHOOL_NAME = "XX大学";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CertificateInfoDTO generateCertificate(Long studentId, CertificateRequestDTO dto,
                                                   Long operatorId, String operatorName) {
        Student student = studentMapper.selectById(studentId);
        if (student == null) {
            throw new BusinessException("学生不存在");
        }

        CertificateType certType = CertificateType.fromCode(dto.getCertType());
        validateCertType(student, certType);

        String certNo = generateCertNo(certType);
        String content = buildCertContent(student, certType, certNo);

        CertificateRecord record = new CertificateRecord();
        record.setStudentId(studentId);
        record.setCertNo(certNo);
        record.setCertType(dto.getCertType());
        record.setCertTitle(certType.getDescription());
        record.setCertContent(content);
        record.setPurpose(dto.getPurpose());
        record.setCopies(dto.getCopies() != null ? dto.getCopies() : 1);
        record.setStatus(CertificateRecord.STATUS_VALID);
        record.setGeneratedBy(operatorId);
        record.setGeneratedByName(operatorName);
        record.setCreatedAt(LocalDateTime.now());

        certificateMapper.insert(record);
        log.info("为学生 {} 生成 {} ，编号: {}", studentId, certType.getDescription(), certNo);

        return toDTO(record, student);
    }

    @Override
    public CertificateInfoDTO getCertificateById(Long id) {
        CertificateRecord record = certificateMapper.selectById(id);
        if (record == null) {
            throw new BusinessException("证明记录不存在");
        }
        Student student = studentMapper.selectById(record.getStudentId());
        return toDTO(record, student);
    }

    @Override
    public CertificateInfoDTO getCertificateByCertNo(String certNo) {
        CertificateRecord record = certificateMapper.selectByCertNo(certNo);
        if (record == null) {
            throw new BusinessException("未找到对应证明，编号可能无效");
        }
        Student student = studentMapper.selectById(record.getStudentId());
        return toDTO(record, student);
    }

    @Override
    public List<CertificateInfoDTO> getStudentCertificates(Long studentId) {
        List<CertificateRecord> records = certificateMapper.selectByStudentId(studentId);
        return records.stream().map(r -> {
            Student student = studentMapper.selectById(r.getStudentId());
            return toDTO(r, student);
        }).collect(Collectors.toList());
    }

    @Override
    public List<CertificateInfoDTO> getRecentCertificates(int limit) {
        List<CertificateRecord> records = certificateMapper.selectRecent(limit);
        return records.stream().map(r -> {
            Student student = studentMapper.selectById(r.getStudentId());
            return toDTO(r, student);
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void revokeCertificate(Long id, String reason, Long operatorId) {
        CertificateRecord record = certificateMapper.selectById(id);
        if (record == null) {
            throw new BusinessException("证明记录不存在");
        }
        if (CertificateRecord.STATUS_REVOKED.equals(record.getStatus())) {
            throw new BusinessException("该证明已作废");
        }
        record.setStatus(CertificateRecord.STATUS_REVOKED);
        record.setRevokedAt(LocalDateTime.now());
        record.setRevokeReason(reason);
        certificateMapper.updateById(record);
        log.info("证明 {} 已被作废，原因: {}", record.getCertNo(), reason);
    }

    @Override
    public List<String> getAvailableCertTypes(Long studentId) {
        Student student = studentMapper.selectById(studentId);
        if (student == null) {
            throw new BusinessException("学生不存在");
        }
        List<String> types = new ArrayList<>();
        AcademicStatus status = AcademicStatus.fromCode(student.getAcademicStatus());

        types.add(CertificateType.ENROLLMENT.getCode());

        if (status == AcademicStatus.ENROLLED) {
            types.add(CertificateType.ATTENDANCE.getCode());
        }
        if (status == AcademicStatus.GRADUATED) {
            types.add(CertificateType.GRADUATION.getCode());
        }
        types.add(CertificateType.TRANSCRIPT.getCode());
        types.add(CertificateType.STATUS_CHANGE.getCode());

        return types;
    }

    // ========== 私有方法 ==========

    private void validateCertType(Student student, CertificateType certType) {
        AcademicStatus status = AcademicStatus.fromCode(student.getAcademicStatus());

        if (certType == CertificateType.ATTENDANCE && status != AcademicStatus.ENROLLED) {
            throw new BusinessException("只有在读学生才能开具在读证明");
        }
        if (certType == CertificateType.GRADUATION && status != AcademicStatus.GRADUATED) {
            throw new BusinessException("只有已毕业学生才能开具毕业证明");
        }
    }

    private String generateCertNo(CertificateType type) {
        String prefix = switch (type) {
            case ENROLLMENT -> "XJ";
            case ATTENDANCE -> "ZD";
            case GRADUATION -> "BY";
            case TRANSCRIPT -> "CJ";
            case STATUS_CHANGE -> "YD";
        };
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String uuid = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 8).toUpperCase();
        return prefix + dateStr + uuid;
    }

    private String buildCertContent(Student student, CertificateType type, String certNo) {
        AcademicStatus academicStatus = AcademicStatus.fromCode(student.getAcademicStatus());
        String today = LocalDate.now().format(DATE_FMT);

        return switch (type) {
            case ENROLLMENT -> buildEnrollmentCert(student, academicStatus, certNo, today);
            case ATTENDANCE -> buildAttendanceCert(student, certNo, today);
            case GRADUATION -> buildGraduationCert(student, certNo, today);
            case TRANSCRIPT -> buildTranscriptCert(student, certNo, today);
            case STATUS_CHANGE -> buildStatusChangeCert(student, academicStatus, certNo, today);
        };
    }

    private String buildEnrollmentCert(Student student, AcademicStatus status, String certNo, String today) {
        return String.format("""
                <div class="certificate">
                  <h1>学 籍 证 明</h1>
                  <p class="cert-no">编号：%s</p>
                  <div class="content">
                    <p>兹证明 <b>%s</b>，性别 %s，身份证号 %s，学号 <b>%s</b>，
                    系%s <b>%s</b> 学院 <b>%s</b> 专业学生，
                    于 %s 入学。</p>
                    <p>当前学籍状态为：<b>%s</b>。</p>
                    <p>特此证明。</p>
                  </div>
                  <div class="footer">
                    <p class="school">%s 教务处</p>
                    <p class="date">%s</p>
                    <p class="seal">（盖章有效）</p>
                  </div>
                </div>""",
                certNo, student.getName(),
                "M".equals(student.getGender()) ? "男" : "女",
                maskIdNumber(student.getIdNumber()),
                student.getStudentNo(), SCHOOL_NAME,
                student.getDepartment(), student.getMajor(),
                student.getEnrollmentDate().format(DATE_FMT),
                status.getDescription(),
                SCHOOL_NAME, today);
    }

    private String buildAttendanceCert(Student student, String certNo, String today) {
        return String.format("""
                <div class="certificate">
                  <h1>在 读 证 明</h1>
                  <p class="cert-no">编号：%s</p>
                  <div class="content">
                    <p>兹证明 <b>%s</b>，性别 %s，身份证号 %s，学号 <b>%s</b>，
                    系%s <b>%s</b> 学院 <b>%s</b> 专业 <b>%s</b> 班全日制在读学生，
                    于 %s 入学，学制四年，预计 %s 毕业。</p>
                    <p>该生目前在校学习，学籍状态正常。</p>
                    <p>特此证明。</p>
                  </div>
                  <div class="footer">
                    <p class="school">%s 教务处</p>
                    <p class="date">%s</p>
                    <p class="seal">（盖章有效）</p>
                  </div>
                </div>""",
                certNo, student.getName(),
                "M".equals(student.getGender()) ? "男" : "女",
                maskIdNumber(student.getIdNumber()),
                student.getStudentNo(), SCHOOL_NAME,
                student.getDepartment(), student.getMajor(), student.getClassNo(),
                student.getEnrollmentDate().format(DATE_FMT),
                student.getGraduationDate() != null
                        ? student.getGraduationDate().format(DATE_FMT)
                        : student.getEnrollmentDate().plusYears(4).format(DATE_FMT),
                SCHOOL_NAME, today);
    }

    private String buildGraduationCert(Student student, String certNo, String today) {
        return String.format("""
                <div class="certificate">
                  <h1>毕 业 证 明</h1>
                  <p class="cert-no">编号：%s</p>
                  <div class="content">
                    <p>兹证明 <b>%s</b>，性别 %s，身份证号 %s，学号 <b>%s</b>，
                    于 %s 进入%s <b>%s</b> 学院 <b>%s</b> 专业学习，
                    学制四年，已于 %s 完成全部学业并准予毕业。</p>
                    <p>特此证明。</p>
                  </div>
                  <div class="footer">
                    <p class="school">%s 教务处</p>
                    <p class="date">%s</p>
                    <p class="seal">（盖章有效）</p>
                  </div>
                </div>""",
                certNo, student.getName(),
                "M".equals(student.getGender()) ? "男" : "女",
                maskIdNumber(student.getIdNumber()),
                student.getStudentNo(),
                student.getEnrollmentDate().format(DATE_FMT),
                SCHOOL_NAME, student.getDepartment(), student.getMajor(),
                student.getGraduationDate() != null
                        ? student.getGraduationDate().format(DATE_FMT)
                        : "（日期待定）",
                SCHOOL_NAME, today);
    }

    private String buildTranscriptCert(Student student, String certNo, String today) {
        List<GradeDTO> grades = gradeService.getStudentGrades(student.getId(), null);
        List<GradeDTO> approvedGrades = grades.stream()
                .filter(g -> "APPROVED".equals(g.getStatus()))
                .toList();

        StringBuilder rows = new StringBuilder();
        for (GradeDTO g : approvedGrades) {
            rows.append(String.format(
                    "<tr><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td></tr>\n",
                    g.getSemester(), g.getCourseCode(), g.getCourseName(),
                    g.getCredits(), g.getScore() != null ? g.getScore().toString() : "-",
                    g.getLetterGrade() != null ? g.getLetterGrade() : "-"));
        }

        return String.format("""
                <div class="certificate">
                  <h1>成 绩 证 明</h1>
                  <p class="cert-no">编号：%s</p>
                  <div class="content">
                    <p>兹证明 <b>%s</b>，学号 <b>%s</b>，系%s %s 学院 %s 专业学生，
                    在校期间各科成绩如下：</p>
                    <table class="grade-table">
                      <thead>
                        <tr><th>学期</th><th>课程编号</th><th>课程名称</th><th>学分</th><th>成绩</th><th>等级</th></tr>
                      </thead>
                      <tbody>
                        %s
                      </tbody>
                    </table>
                    <p>以上成绩真实有效。</p>
                    <p>特此证明。</p>
                  </div>
                  <div class="footer">
                    <p class="school">%s 教务处</p>
                    <p class="date">%s</p>
                    <p class="seal">（盖章有效）</p>
                  </div>
                </div>""",
                certNo, student.getName(), student.getStudentNo(),
                SCHOOL_NAME, student.getDepartment(), student.getMajor(),
                rows.toString(),
                SCHOOL_NAME, today);
    }

    private String buildStatusChangeCert(Student student, AcademicStatus status, String certNo, String today) {
        return String.format("""
                <div class="certificate">
                  <h1>学籍异动证明</h1>
                  <p class="cert-no">编号：%s</p>
                  <div class="content">
                    <p>兹证明 <b>%s</b>，性别 %s，学号 <b>%s</b>，
                    系%s <b>%s</b> 学院 <b>%s</b> 专业学生，
                    于 %s 入学。</p>
                    <p>该生当前学籍状态为：<b>%s</b>。</p>
                    <p>特此证明。</p>
                  </div>
                  <div class="footer">
                    <p class="school">%s 教务处</p>
                    <p class="date">%s</p>
                    <p class="seal">（盖章有效）</p>
                  </div>
                </div>""",
                certNo, student.getName(),
                "M".equals(student.getGender()) ? "男" : "女",
                student.getStudentNo(), SCHOOL_NAME,
                student.getDepartment(), student.getMajor(),
                student.getEnrollmentDate().format(DATE_FMT),
                status.getDescription(),
                SCHOOL_NAME, today);
    }

    private String maskIdNumber(String idNumber) {
        if (idNumber == null || idNumber.length() < 8) return "***";
        return idNumber.substring(0, 4) + "**********" + idNumber.substring(idNumber.length() - 4);
    }

    private CertificateInfoDTO toDTO(CertificateRecord record, Student student) {
        CertificateInfoDTO dto = new CertificateInfoDTO();
        dto.setId(record.getId());
        dto.setStudentId(record.getStudentId());
        dto.setCertNo(record.getCertNo());
        dto.setCertType(record.getCertType());
        dto.setCertTypeText(CertificateInfoDTO.getCertTypeText(record.getCertType()));
        dto.setCertTitle(record.getCertTitle());
        dto.setCertContent(record.getCertContent());
        dto.setPurpose(record.getPurpose());
        dto.setCopies(record.getCopies());
        dto.setStatus(record.getStatus());
        dto.setStatusText(CertificateInfoDTO.getStatusText(record.getStatus()));
        dto.setGeneratedByName(record.getGeneratedByName());
        dto.setRevokedAt(record.getRevokedAt());
        dto.setRevokeReason(record.getRevokeReason());
        dto.setCreatedAt(record.getCreatedAt());

        if (student != null) {
            dto.setStudentNo(student.getStudentNo());
            dto.setStudentName(student.getName());
            dto.setDepartment(student.getDepartment());
        }

        return dto;
    }
}
