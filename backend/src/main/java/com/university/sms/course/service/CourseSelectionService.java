package com.university.sms.course.service;

import com.university.sms.course.dto.*;

import java.util.List;

/**
 * 选课服务接口
 */
public interface CourseSelectionService {
    
    /**
     * 选课
     */
    CourseSelectionResultDTO selectCourse(Long studentId, Long courseId);
    
    /**
     * 退课
     */
    void withdrawCourse(Long studentId, Long courseId);
    
    /**
     * 检查时间冲突
     */
    List<CourseDTO> checkTimeConflict(Long studentId, Long courseId);
    
    /**
     * 检查先修课程
     */
    List<String> checkPrerequisites(Long studentId, Long courseId);
    
    /**
     * 获取学生课表
     */
    List<ScheduleDTO> getStudentSchedule(Long studentId);
    
    /**
     * 获取课程学生名单
     */
    List<CourseRosterDTO> getCourseRoster(Long courseId);
    
    /**
     * 获取学生选课记录
     */
    List<CourseSelectionDTO> getStudentSelections(Long studentId);
    
    /**
     * 对单门课程执行抽签
     */
    void executeLottery(Long courseId);

    /**
     * 对所有需要抽签的课程批量执行抽签
     */
    int executeLotteryAll();

    /**
     * 获取需要抽签的课程列表（有LOTTERY_PENDING记录的课程）
     */
    List<LotteryCourseDTO> getLotteryPendingCourses();

    /**
     * 获取当前选课阶段代码
     */
    String getSelectionPhase();

    /**
     * 获取选课阶段完整信息（含各阶段时间窗口、规则等）
     */
    SelectionPhaseDTO getPhaseInfo();
}
