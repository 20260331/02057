package com.university.sms.grade.service;

import com.university.sms.grade.dto.*;

import java.util.List;

/**
 * 成绩服务接口
 */
public interface GradeService {
    
    /**
     * 查询学生成绩
     */
    List<GradeDTO> getStudentGrades(Long studentId, String semester);
    
    /**
     * 查询课程成绩
     */
    List<GradeDTO> getCourseGrades(Long courseId);
    
    /**
     * 批量录入成绩
     */
    void batchSaveGrades(BatchGradeDTO dto, Long operatorId);
    
    /**
     * 批量录入并提交成绩（一步完成保存和提交）
     */
    void batchSaveAndSubmitGrades(BatchGradeDTO dto, Long operatorId);
    
    /**
     * 修改成绩
     */
    void updateGrade(Long gradeId, GradeUpdateDTO dto, Long operatorId, String operatorName);
    
    /**
     * 提交成绩（锁定）
     */
    void submitGrades(Long courseId, Long operatorId);
    
    /**
     * 审批成绩
     */
    void approveGrades(Long courseId, Long approverId);
    
    /**
     * 计算学生 GPA
     */
    GPAInfoDTO calculateGPA(Long studentId);
    
    /**
     * 获取课程成绩统计
     */
    GradeStatisticsDTO getStatistics(Long courseId);
    
    /**
     * 获取课程成绩修改记录
     */
    List<GradeChangeLogDTO> getChangeLogs(Long courseId);
    
    /**
     * 检查学生是否有不及格成绩
     */
    boolean hasFailingGrades(Long studentId);

    /**
     * 生成学生正式成绩单（含结构化数据 + HTML 文档）
     */
    TranscriptDTO generateTranscript(Long studentId);
}
