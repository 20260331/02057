package com.university.sms.course.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 选课结果 DTO
 */
@Data
public class CourseSelectionResultDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 是否成功
     */
    private Boolean success;
    
    /**
     * 消息
     */
    private String message;
    
    /**
     * 选课记录ID
     */
    private Long selectionId;

    /**
     * 选课状态（SELECTED / LOTTERY_PENDING）
     */
    private String status;
    
    /**
     * 冲突的课程列表
     */
    private List<CourseDTO> conflictCourses;
    
    /**
     * 缺少的先修课程列表
     */
    private List<String> missingPrerequisites;
    
    /**
     * 创建成功结果
     */
    public static CourseSelectionResultDTO success(Long selectionId) {
        CourseSelectionResultDTO result = new CourseSelectionResultDTO();
        result.setSuccess(true);
        result.setMessage("选课成功");
        result.setSelectionId(selectionId);
        return result;
    }
    
    /**
     * 创建失败结果
     */
    public static CourseSelectionResultDTO fail(String message) {
        CourseSelectionResultDTO result = new CourseSelectionResultDTO();
        result.setSuccess(false);
        result.setMessage(message);
        return result;
    }
    
    /**
     * 创建时间冲突结果
     */
    public static CourseSelectionResultDTO timeConflict(List<CourseDTO> conflictCourses) {
        CourseSelectionResultDTO result = new CourseSelectionResultDTO();
        result.setSuccess(false);
        result.setMessage("选课失败：与已选课程时间冲突");
        result.setConflictCourses(conflictCourses);
        return result;
    }
    
    /**
     * 创建先修课程不满足结果
     */
    public static CourseSelectionResultDTO prerequisiteNotMet(List<String> missingPrerequisites) {
        CourseSelectionResultDTO result = new CourseSelectionResultDTO();
        result.setSuccess(false);
        result.setMessage("选课失败：先修课程要求未满足");
        result.setMissingPrerequisites(missingPrerequisites);
        return result;
    }
}
