package com.university.sms.student.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.university.sms.student.entity.StatusChangeApplication;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 学籍异动申请 Mapper
 */
@Mapper
public interface StatusChangeApplicationMapper extends BaseMapper<StatusChangeApplication> {

    @Select("SELECT * FROM stu_status_change_application WHERE student_id = #{studentId} ORDER BY created_at DESC")
    List<StatusChangeApplication> selectByStudentId(@Param("studentId") Long studentId);

    /**
     * 查询待辅导员审批的申请（通过学生关联辅导员）
     */
    @Select("SELECT a.* FROM stu_status_change_application a " +
            "INNER JOIN stu_student s ON a.student_id = s.id " +
            "WHERE s.counselor_id = #{counselorId} AND a.status = 'PENDING' " +
            "ORDER BY a.created_at ASC")
    List<StatusChangeApplication> selectPendingByCounselor(@Param("counselorId") Long counselorId);

    @Select("SELECT * FROM stu_status_change_application WHERE status IN ('PENDING', 'COUNSELOR_APPROVED') ORDER BY created_at ASC")
    List<StatusChangeApplication> selectAllPending();

    /**
     * 查询待教务处审批的申请（辅导员已批，需要二级审批）
     */
    @Select("SELECT * FROM stu_status_change_application WHERE status = 'COUNSELOR_APPROVED' ORDER BY created_at ASC")
    List<StatusChangeApplication> selectPendingForAcademicAffairs();

    /**
     * 检查学生是否有进行中的申请
     */
    @Select("SELECT COUNT(*) FROM stu_status_change_application WHERE student_id = #{studentId} AND status IN ('PENDING', 'COUNSELOR_APPROVED')")
    int countActiveByStudentId(@Param("studentId") Long studentId);
}
