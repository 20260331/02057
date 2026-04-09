package com.university.sms.grade.service.impl;

import com.university.sms.common.exception.BusinessException;
import com.university.sms.course.entity.Course;
import com.university.sms.course.mapper.CourseMapper;
import com.university.sms.grade.dto.*;
import com.university.sms.grade.entity.Grade;
import com.university.sms.grade.entity.GradeChangeLog;
import com.university.sms.grade.mapper.GradeChangeLogMapper;
import com.university.sms.grade.mapper.GradeMapper;
import com.university.sms.grade.service.GradeService;
import com.university.sms.student.entity.Student;
import com.university.sms.student.mapper.StudentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 成绩服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GradeServiceImpl implements GradeService {
    
    private final GradeMapper gradeMapper;
    private final GradeChangeLogMapper changeLogMapper;
    private final CourseMapper courseMapper;
    private final StudentMapper studentMapper;
    
    @Override
    public List<GradeDTO> getStudentGrades(Long studentId, String semester) {
        List<Grade> grades;
        if (semester != null && !semester.isEmpty()) {
            grades = gradeMapper.selectByStudentAndSemester(studentId, semester);
        } else {
            grades = gradeMapper.selectByStudentId(studentId);
        }
        
        return grades.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<GradeDTO> getCourseGrades(Long courseId) {
        List<Grade> grades = gradeMapper.selectByCourseId(courseId);
        return grades.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchSaveGrades(BatchGradeDTO dto, Long operatorId) {
        log.info("批量保存成绩 - 课程ID: {}, 成绩数量: {}", dto.getCourseId(), dto.getGrades().size());
        
        Course course = courseMapper.selectById(dto.getCourseId());
        if (course == null) {
            throw new BusinessException("课程不存在");
        }
        
        log.info("课程信息 - 名称: {}, 学期: {}", course.getName(), course.getSemester());
        
        for (BatchGradeDTO.GradeItem item : dto.getGrades()) {
            log.info("处理学生成绩 - 学生ID: {}, 分数: {}", item.getStudentId(), item.getScore());
            
            // 验证分数范围
            if (item.getScore().compareTo(BigDecimal.ZERO) < 0 || 
                item.getScore().compareTo(new BigDecimal("100")) > 0) {
                throw new BusinessException("分数必须在 0-100 之间");
            }
            
            // 查找或创建成绩记录
            Grade grade = gradeMapper.selectByStudentAndCourse(item.getStudentId(), dto.getCourseId());
            boolean isNew = (grade == null);
            
            log.info("成绩记录 - 是否新建: {}", isNew);
            
            if (isNew) {
                grade = new Grade();
                grade.setStudentId(item.getStudentId());
                grade.setCourseId(dto.getCourseId());
                grade.setStatus(Grade.STATUS_DRAFT);
                grade.setSemester(course.getSemester());
                grade.setCreatedAt(LocalDateTime.now());
            }
            
            // 检查分数是否有变化
            boolean scoreChanged = grade.getScore() == null || 
                                   grade.getScore().compareTo(item.getScore()) != 0;
            
            grade.setScore(item.getScore());
            grade.setLetterGrade(calculateLetterGrade(item.getScore()));
            grade.setGradePoints(calculateGradePoints(item.getScore()));
            grade.setEnteredBy(operatorId);
            grade.setEnteredAt(LocalDateTime.now());
            
            // 如果分数有变化且当前状态不是草稿，重置为草稿状态（需要重新提交审核）
            if (scoreChanged && !Grade.STATUS_DRAFT.equals(grade.getStatus())) {
                grade.setStatus(Grade.STATUS_DRAFT);
            }
            
            if (isNew) {
                log.info("插入新成绩记录");
                gradeMapper.insert(grade);
                log.info("插入成功 - ID: {}", grade.getId());
            } else {
                log.info("更新成绩记录 - ID: {}", grade.getId());
                gradeMapper.updateById(grade);
            }
        }
        
        log.info("批量保存成绩完成");
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchSaveAndSubmitGrades(BatchGradeDTO dto, Long operatorId) {
        log.info("批量保存并提交成绩 - 课程ID: {}, 成绩数量: {}", dto.getCourseId(), dto.getGrades().size());
        
        Course course = courseMapper.selectById(dto.getCourseId());
        if (course == null) {
            throw new BusinessException("课程不存在");
        }
        
        log.info("课程信息 - 名称: {}, 学期: {}", course.getName(), course.getSemester());
        
        for (BatchGradeDTO.GradeItem item : dto.getGrades()) {
            log.info("处理学生成绩 - 学生ID: {}, 分数: {}", item.getStudentId(), item.getScore());
            
            // 验证分数范围
            if (item.getScore().compareTo(BigDecimal.ZERO) < 0 || 
                item.getScore().compareTo(new BigDecimal("100")) > 0) {
                throw new BusinessException("分数必须在 0-100 之间");
            }
            
            // 查找或创建成绩记录
            Grade grade = gradeMapper.selectByStudentAndCourse(item.getStudentId(), dto.getCourseId());
            boolean isNew = (grade == null);
            
            log.info("成绩记录 - 是否新建: {}", isNew);
            
            if (isNew) {
                grade = new Grade();
                grade.setStudentId(item.getStudentId());
                grade.setCourseId(dto.getCourseId());
                grade.setSemester(course.getSemester());
                grade.setCreatedAt(LocalDateTime.now());
            }
            
            grade.setScore(item.getScore());
            grade.setLetterGrade(calculateLetterGrade(item.getScore()));
            grade.setGradePoints(calculateGradePoints(item.getScore()));
            grade.setEnteredBy(operatorId);
            grade.setEnteredAt(LocalDateTime.now());
            // 直接设置为已提交状态
            grade.setStatus(Grade.STATUS_SUBMITTED);
            
            if (isNew) {
                log.info("插入新成绩记录并提交");
                gradeMapper.insert(grade);
                log.info("插入成功 - ID: {}", grade.getId());
            } else {
                log.info("更新成绩记录并提交 - ID: {}", grade.getId());
                gradeMapper.updateById(grade);
            }
        }
        
        log.info("批量保存并提交成绩完成");
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateGrade(Long gradeId, GradeUpdateDTO dto, Long operatorId, String operatorName) {
        Grade grade = gradeMapper.selectById(gradeId);
        if (grade == null) {
            throw new BusinessException("成绩记录不存在");
        }
        
        // 验证分数范围
        if (dto.getScore().compareTo(BigDecimal.ZERO) < 0 || 
            dto.getScore().compareTo(new BigDecimal("100")) > 0) {
            throw new BusinessException("分数必须在 0-100 之间");
        }
        
        // 记录变更日志
        GradeChangeLog log = new GradeChangeLog();
        log.setGradeId(gradeId);
        log.setStudentId(grade.getStudentId());
        log.setCourseId(grade.getCourseId());
        log.setOldScore(grade.getScore());
        log.setNewScore(dto.getScore());
        log.setReason(dto.getReason());
        log.setOperatorId(operatorId);
        log.setOperatorName(operatorName);
        log.setCreatedAt(LocalDateTime.now());
        changeLogMapper.insert(log);
        
        // 更新成绩
        grade.setScore(dto.getScore());
        grade.setLetterGrade(calculateLetterGrade(dto.getScore()));
        grade.setGradePoints(calculateGradePoints(dto.getScore()));
        grade.setEnteredBy(operatorId);
        grade.setEnteredAt(LocalDateTime.now());
        
        // 如果成绩已审批，调整后需要重新提交审核
        if (Grade.STATUS_APPROVED.equals(grade.getStatus())) {
            grade.setStatus(Grade.STATUS_SUBMITTED);
        }
        
        gradeMapper.updateById(grade);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitGrades(Long courseId, Long operatorId) {
        log.info("提交成绩审核 - 课程ID: {}, 操作人: {}", courseId, operatorId);
        
        List<Grade> grades = gradeMapper.selectByCourseId(courseId);
        log.info("查询到 {} 条成绩记录", grades.size());
        
        int submittedCount = 0;
        for (Grade grade : grades) {
            log.info("成绩记录 - ID: {}, 学生ID: {}, 状态: {}", grade.getId(), grade.getStudentId(), grade.getStatus());
            if (Grade.STATUS_DRAFT.equals(grade.getStatus())) {
                grade.setStatus(Grade.STATUS_SUBMITTED);
                gradeMapper.updateById(grade);
                submittedCount++;
                log.info("成绩 {} 状态更新为 SUBMITTED", grade.getId());
            }
        }
        
        log.info("提交完成，共提交 {} 条成绩", submittedCount);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveGrades(Long courseId, Long approverId) {
        List<Grade> grades = gradeMapper.selectPendingApproval(courseId);
        for (Grade grade : grades) {
            grade.setStatus(Grade.STATUS_APPROVED);
            grade.setApprovedBy(approverId);
            grade.setApprovedAt(LocalDateTime.now());
            gradeMapper.updateById(grade);
        }
    }

    @Override
    public GPAInfoDTO calculateGPA(Long studentId) {
        List<Grade> grades = gradeMapper.selectByStudentId(studentId);
        
        GPAInfoDTO gpaInfo = new GPAInfoDTO();
        
        BigDecimal totalCredits = BigDecimal.ZERO;
        BigDecimal totalPoints = BigDecimal.ZERO;
        BigDecimal earnedCredits = BigDecimal.ZERO;
        
        Map<String, List<Grade>> semesterGrades = new HashMap<>();
        
        for (Grade grade : grades) {
            if (!Grade.STATUS_APPROVED.equals(grade.getStatus())) continue;
            
            Course course = courseMapper.selectById(grade.getCourseId());
            if (course == null) continue;
            
            BigDecimal credits = course.getCredits();
            totalCredits = totalCredits.add(credits);
            totalPoints = totalPoints.add(credits.multiply(grade.getGradePoints()));
            
            if (grade.getScore().compareTo(new BigDecimal("60")) >= 0) {
                earnedCredits = earnedCredits.add(credits);
            }
            
            // 按学期分组
            semesterGrades.computeIfAbsent(course.getSemester(), k -> new ArrayList<>()).add(grade);
        }
        
        // 计算累计 GPA
        if (totalCredits.compareTo(BigDecimal.ZERO) > 0) {
            gpaInfo.setCumulativeGPA(totalPoints.divide(totalCredits, 2, RoundingMode.HALF_UP));
        } else {
            gpaInfo.setCumulativeGPA(BigDecimal.ZERO);
        }
        
        gpaInfo.setTotalCredits(totalCredits);
        gpaInfo.setEarnedCredits(earnedCredits);
        
        // 计算各学期 GPA
        List<GPAInfoDTO.SemesterGPA> semesterGPAs = new ArrayList<>();
        for (Map.Entry<String, List<Grade>> entry : semesterGrades.entrySet()) {
            GPAInfoDTO.SemesterGPA semGPA = new GPAInfoDTO.SemesterGPA();
            semGPA.setSemester(entry.getKey());
            
            BigDecimal semCredits = BigDecimal.ZERO;
            BigDecimal semPoints = BigDecimal.ZERO;
            
            for (Grade g : entry.getValue()) {
                Course c = courseMapper.selectById(g.getCourseId());
                if (c != null) {
                    semCredits = semCredits.add(c.getCredits());
                    semPoints = semPoints.add(c.getCredits().multiply(g.getGradePoints()));
                }
            }
            
            if (semCredits.compareTo(BigDecimal.ZERO) > 0) {
                semGPA.setGpa(semPoints.divide(semCredits, 2, RoundingMode.HALF_UP));
            } else {
                semGPA.setGpa(BigDecimal.ZERO);
            }
            semGPA.setCredits(semCredits);
            semesterGPAs.add(semGPA);
        }
        
        gpaInfo.setSemesterGPAs(semesterGPAs);
        return gpaInfo;
    }
    
    @Override
    public GradeStatisticsDTO getStatistics(Long courseId) {
        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new BusinessException("课程不存在");
        }
        
        List<Grade> grades = gradeMapper.selectByCourseId(courseId);
        List<Grade> approvedGrades = grades.stream()
                .filter(g -> Grade.STATUS_APPROVED.equals(g.getStatus()))
                .collect(Collectors.toList());
        
        GradeStatisticsDTO stats = new GradeStatisticsDTO();
        stats.setCourseId(courseId);
        stats.setCourseName(course.getName());
        stats.setTotalStudents(approvedGrades.size());
        
        if (!approvedGrades.isEmpty()) {
            BigDecimal sum = BigDecimal.ZERO;
            BigDecimal max = BigDecimal.ZERO;
            BigDecimal min = new BigDecimal("100");
            int passCount = 0;
            int excellentCount = 0;
            
            Map<String, Integer> distribution = new HashMap<>();
            
            for (Grade g : approvedGrades) {
                sum = sum.add(g.getScore());
                if (g.getScore().compareTo(max) > 0) max = g.getScore();
                if (g.getScore().compareTo(min) < 0) min = g.getScore();
                if (g.getScore().compareTo(new BigDecimal("60")) >= 0) passCount++;
                if (g.getScore().compareTo(new BigDecimal("90")) >= 0) excellentCount++;
                
                distribution.merge(g.getLetterGrade(), 1, Integer::sum);
            }
            
            stats.setAverageScore(sum.divide(new BigDecimal(approvedGrades.size()), 2, RoundingMode.HALF_UP));
            stats.setMaxScore(max);
            stats.setMinScore(min);
            stats.setPassRate(new BigDecimal(passCount * 100).divide(new BigDecimal(approvedGrades.size()), 2, RoundingMode.HALF_UP));
            stats.setExcellentRate(new BigDecimal(excellentCount * 100).divide(new BigDecimal(approvedGrades.size()), 2, RoundingMode.HALF_UP));
            stats.setDistribution(distribution);
        }
        
        return stats;
    }
    
    @Override
    public boolean hasFailingGrades(Long studentId) {
        List<Grade> grades = gradeMapper.selectByStudentId(studentId);
        return grades.stream()
                .anyMatch(g -> g.getScore() != null && g.getScore().compareTo(new BigDecimal("60")) < 0);
    }
    
    @Override
    public List<GradeChangeLogDTO> getChangeLogs(Long courseId) {
        List<GradeChangeLog> logs = changeLogMapper.selectByCourseId(courseId);
        return logs.stream().map(log -> {
            GradeChangeLogDTO dto = new GradeChangeLogDTO();
            dto.setId(log.getId());
            dto.setGradeId(log.getGradeId());
            dto.setStudentId(log.getStudentId());
            dto.setCourseId(log.getCourseId());
            dto.setOldScore(log.getOldScore());
            dto.setNewScore(log.getNewScore());
            dto.setReason(log.getReason());
            dto.setOperatorId(log.getOperatorId());
            dto.setOperatorName(log.getOperatorName());
            dto.setCreatedAt(log.getCreatedAt());
            
            // 填充学生信息
            Student student = studentMapper.selectById(log.getStudentId());
            if (student != null) {
                dto.setStudentNo(student.getStudentNo());
                dto.setStudentName(student.getName());
            }
            return dto;
        }).collect(Collectors.toList());
    }
    
    // ========== 私有方法 ==========
    
    private GradeDTO toDTO(Grade grade) {
        GradeDTO dto = new GradeDTO();
        dto.setId(grade.getId());
        dto.setStudentId(grade.getStudentId());
        dto.setCourseId(grade.getCourseId());
        dto.setScore(grade.getScore());
        dto.setLetterGrade(grade.getLetterGrade());
        dto.setGradePoints(grade.getGradePoints());
        dto.setStatus(grade.getStatus());
        dto.setStatusText(GradeDTO.getStatusText(grade.getStatus()));
        dto.setEnteredAt(grade.getEnteredAt());
        dto.setApprovedAt(grade.getApprovedAt());
        
        // 填充学生信息
        Student student = studentMapper.selectById(grade.getStudentId());
        if (student != null) {
            dto.setStudentNo(student.getStudentNo());
            dto.setStudentName(student.getName());
        }
        
        // 填充课程信息
        Course course = courseMapper.selectById(grade.getCourseId());
        if (course != null) {
            dto.setCourseCode(course.getCourseCode());
            dto.setCourseName(course.getName());
            dto.setCredits(course.getCredits());
            dto.setSemester(course.getSemester());
        }
        
        return dto;
    }
    
    @Override
    public TranscriptDTO generateTranscript(Long studentId) {
        Student student = studentMapper.selectById(studentId);
        if (student == null) {
            throw new BusinessException("学生不存在");
        }

        List<GradeDTO> allGrades = getStudentGrades(studentId, null);
        List<GradeDTO> approvedGrades = allGrades.stream()
                .filter(g -> "APPROVED".equals(g.getStatus()))
                .toList();

        GPAInfoDTO gpaInfo = calculateGPA(studentId);

        TranscriptDTO dto = new TranscriptDTO();
        dto.setStudentNo(student.getStudentNo());
        dto.setStudentName(student.getName());
        dto.setGender(student.getGender() != null ? ("M".equals(student.getGender()) ? "男" : "女") : "");
        dto.setDepartment(student.getDepartment());
        dto.setMajor(student.getMajor());
        dto.setClassNo(student.getClassNo());
        dto.setEnrollmentDate(student.getEnrollmentDate() != null ? student.getEnrollmentDate().toString() : "");
        dto.setAcademicStatus(student.getAcademicStatus());
        dto.setCumulativeGPA(gpaInfo.getCumulativeGPA());
        dto.setTotalCredits(gpaInfo.getTotalCredits());
        dto.setEarnedCredits(gpaInfo.getEarnedCredits());
        dto.setTotalCourses(approvedGrades.size());
        dto.setPassedCourses((int) approvedGrades.stream()
                .filter(g -> g.getScore() != null && g.getScore().compareTo(new BigDecimal("60")) >= 0)
                .count());
        dto.setSchoolName("XX大学");
        dto.setGeneratedDate(java.time.LocalDate.now().toString());

        // 按学期分组
        Map<String, List<GradeDTO>> bySemester = approvedGrades.stream()
                .collect(Collectors.groupingBy(
                        g -> g.getSemester() != null ? g.getSemester() : "未知学期",
                        LinkedHashMap::new,
                        Collectors.toList()));

        List<TranscriptDTO.SemesterBlock> semesterBlocks = new ArrayList<>();
        for (Map.Entry<String, List<GradeDTO>> entry : bySemester.entrySet()) {
            TranscriptDTO.SemesterBlock block = new TranscriptDTO.SemesterBlock();
            block.setSemester(entry.getKey());

            List<TranscriptDTO.CourseGrade> courseGrades = entry.getValue().stream().map(g -> {
                TranscriptDTO.CourseGrade cg = new TranscriptDTO.CourseGrade();
                cg.setCourseCode(g.getCourseCode());
                cg.setCourseName(g.getCourseName());
                cg.setCredits(g.getCredits());
                cg.setScore(g.getScore());
                cg.setLetterGrade(g.getLetterGrade());
                cg.setGradePoints(g.getGradePoints());
                return cg;
            }).toList();

            block.setCourses(courseGrades);

            BigDecimal semCredits = courseGrades.stream()
                    .map(TranscriptDTO.CourseGrade::getCredits)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            block.setSemesterCredits(semCredits);

            if (semCredits.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal weightedSum = courseGrades.stream()
                        .filter(cg -> cg.getGradePoints() != null && cg.getCredits() != null)
                        .map(cg -> cg.getGradePoints().multiply(cg.getCredits()))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                block.setSemesterGPA(weightedSum.divide(semCredits, 2, RoundingMode.HALF_UP));
            } else {
                block.setSemesterGPA(BigDecimal.ZERO);
            }

            semesterBlocks.add(block);
        }
        dto.setSemesters(semesterBlocks);

        dto.setHtmlContent(buildTranscriptHtml(dto));

        return dto;
    }

    private String buildTranscriptHtml(TranscriptDTO t) {
        StringBuilder semesterHtml = new StringBuilder();
        for (TranscriptDTO.SemesterBlock sem : t.getSemesters()) {
            semesterHtml.append(String.format("""
                <tr class="semester-header">
                  <td colspan="6">%s &nbsp;&nbsp; 学期GPA: %s &nbsp;&nbsp; 学期学分: %s</td>
                </tr>
                """, sem.getSemester(),
                    sem.getSemesterGPA() != null ? sem.getSemesterGPA().toPlainString() : "-",
                    sem.getSemesterCredits() != null ? sem.getSemesterCredits().toPlainString() : "0"));

            for (TranscriptDTO.CourseGrade cg : sem.getCourses()) {
                semesterHtml.append(String.format("""
                    <tr>
                      <td>%s</td><td>%s</td><td class="center">%s</td>
                      <td class="center">%s</td><td class="center">%s</td><td class="center">%s</td>
                    </tr>
                    """,
                        cg.getCourseCode(),
                        cg.getCourseName(),
                        cg.getCredits() != null ? cg.getCredits().toPlainString() : "-",
                        cg.getScore() != null ? cg.getScore().toPlainString() : "-",
                        cg.getLetterGrade() != null ? cg.getLetterGrade() : "-",
                        cg.getGradePoints() != null ? cg.getGradePoints().toPlainString() : "-"));
            }
        }

        return String.format("""
            <!DOCTYPE html>
            <html lang="zh-CN">
            <head>
            <meta charset="UTF-8">
            <title>学业成绩单 - %s</title>
            <style>
              @page { size: A4; margin: 15mm; }
              @media print { body { -webkit-print-color-adjust: exact; print-color-adjust: exact; } }
              body { font-family: "SimSun","STSong","Songti SC",serif; color: #333; margin: 0; padding: 20px; }
              .transcript { max-width: 800px; margin: 0 auto; }
              .header { text-align: center; border-bottom: 3px double #333; padding-bottom: 16px; margin-bottom: 20px; }
              .header h1 { font-size: 28px; letter-spacing: 8px; margin: 0 0 4px 0; }
              .header h2 { font-size: 20px; letter-spacing: 4px; margin: 0; font-weight: normal; }
              .header .en-name { font-family: "Times New Roman",serif; font-size: 14px; color: #666; margin-top: 4px; }
              .student-info { margin: 16px 0; }
              .student-info table { width: 100%%; border-collapse: collapse; }
              .student-info td { padding: 6px 8px; font-size: 14px; }
              .student-info .label { color: #666; width: 80px; }
              .student-info .value { font-weight: bold; }
              .grade-table { width: 100%%; border-collapse: collapse; margin: 16px 0; font-size: 13px; }
              .grade-table th { background: #1a3c6e; color: #fff; padding: 8px 10px; text-align: left; font-weight: normal; }
              .grade-table td { padding: 7px 10px; border-bottom: 1px solid #e0e0e0; }
              .grade-table .center { text-align: center; }
              .grade-table .semester-header { background: #f0f4f8; font-weight: bold; }
              .grade-table .semester-header td { border-bottom: 2px solid #c0c8d4; font-size: 14px; color: #1a3c6e; }
              .grade-table tbody tr:hover { background: #fafbfc; }
              .summary { margin: 20px 0; padding: 16px; background: #f8f9fb; border: 1px solid #e0e4ea; border-radius: 4px; }
              .summary table { width: 100%%; }
              .summary td { padding: 6px 12px; font-size: 14px; }
              .summary .gpa-highlight { font-size: 24px; font-weight: bold; color: #1a3c6e; }
              .footer { margin-top: 40px; text-align: right; font-size: 14px; }
              .footer .school { font-size: 16px; font-weight: bold; }
              .footer .seal { color: #c00; margin-top: 8px; }
              .footer .date { margin-top: 4px; color: #666; }
              .watermark { position: fixed; top: 50%%; left: 50%%; transform: translate(-50%%,-50%%) rotate(-30deg);
                font-size: 60px; color: rgba(0,0,0,0.03); pointer-events: none; letter-spacing: 20px; z-index: -1; }
            </style>
            </head>
            <body>
            <div class="watermark">%s</div>
            <div class="transcript">
              <div class="header">
                <h1>%s</h1>
                <h2>学 业 成 绩 单</h2>
                <div class="en-name">OFFICIAL ACADEMIC TRANSCRIPT</div>
              </div>

              <div class="student-info">
                <table>
                  <tr>
                    <td class="label">姓　　名</td><td class="value">%s</td>
                    <td class="label">学　　号</td><td class="value">%s</td>
                    <td class="label">性　　别</td><td class="value">%s</td>
                  </tr>
                  <tr>
                    <td class="label">学　　院</td><td class="value">%s</td>
                    <td class="label">专　　业</td><td class="value">%s</td>
                    <td class="label">班　　级</td><td class="value">%s</td>
                  </tr>
                  <tr>
                    <td class="label">入学日期</td><td class="value">%s</td>
                    <td class="label">学　　籍</td><td class="value" colspan="3">%s</td>
                  </tr>
                </table>
              </div>

              <table class="grade-table">
                <thead>
                  <tr>
                    <th style="width:110px">课程编号</th>
                    <th>课程名称</th>
                    <th class="center" style="width:60px">学分</th>
                    <th class="center" style="width:60px">成绩</th>
                    <th class="center" style="width:60px">等级</th>
                    <th class="center" style="width:60px">绩点</th>
                  </tr>
                </thead>
                <tbody>
                  %s
                </tbody>
              </table>

              <div class="summary">
                <table>
                  <tr>
                    <td>累计 GPA</td>
                    <td class="gpa-highlight">%s</td>
                    <td>总修学分</td>
                    <td><b>%s</b></td>
                    <td>已获学分</td>
                    <td><b>%s</b></td>
                    <td>课程总数</td>
                    <td><b>%d</b></td>
                  </tr>
                </table>
              </div>

              <div class="footer">
                <p class="school">%s 教务处</p>
                <p class="date">打印日期：%s</p>
                <p class="seal">（此成绩单由系统生成，加盖教务处公章后有效）</p>
              </div>
            </div>
            </body>
            </html>""",
                t.getStudentName(),
                t.getSchoolName(),
                t.getSchoolName(),
                t.getStudentName(), t.getStudentNo(), t.getGender(),
                t.getDepartment() != null ? t.getDepartment() : "", t.getMajor() != null ? t.getMajor() : "", t.getClassNo() != null ? t.getClassNo() : "",
                t.getEnrollmentDate(), t.getAcademicStatus() != null ? t.getAcademicStatus() : "在读",
                semesterHtml.toString(),
                t.getCumulativeGPA() != null ? t.getCumulativeGPA().toPlainString() : "0.00",
                t.getTotalCredits() != null ? t.getTotalCredits().toPlainString() : "0",
                t.getEarnedCredits() != null ? t.getEarnedCredits().toPlainString() : "0",
                t.getTotalCourses(),
                t.getSchoolName(), t.getGeneratedDate());
    }

    /**
     * 计算等级 (A/B/C/D/F)
     */
    private String calculateLetterGrade(BigDecimal score) {
        if (score.compareTo(new BigDecimal("90")) >= 0) return "A";
        if (score.compareTo(new BigDecimal("80")) >= 0) return "B";
        if (score.compareTo(new BigDecimal("70")) >= 0) return "C";
        if (score.compareTo(new BigDecimal("60")) >= 0) return "D";
        return "F";
    }
    
    /**
     * 计算绩点
     */
    private BigDecimal calculateGradePoints(BigDecimal score) {
        if (score.compareTo(new BigDecimal("90")) >= 0) return new BigDecimal("4.0");
        if (score.compareTo(new BigDecimal("80")) >= 0) return new BigDecimal("3.0");
        if (score.compareTo(new BigDecimal("70")) >= 0) return new BigDecimal("2.0");
        if (score.compareTo(new BigDecimal("60")) >= 0) return new BigDecimal("1.0");
        return BigDecimal.ZERO;
    }
}
