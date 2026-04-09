package com.university.sms.leave.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.university.sms.leave.entity.LeaveRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 请假申请 Mapper
 */
@Mapper
public interface LeaveRequestMapper extends BaseMapper<LeaveRequest> {
    
    /**
     * 查询学生的请假记录
     */
    @Select("SELECT * FROM lev_leave_request WHERE student_id = #{studentId} ORDER BY created_at DESC")
    List<LeaveRequest> selectByStudentId(@Param("studentId") Long studentId);
    
    /**
     * 查询待审批的请假（辅导员）
     */
    @Select("SELECT lr.*, lr.is_urgent AS urgent FROM lev_leave_request lr " +
            "INNER JOIN stu_student s ON lr.student_id = s.id " +
            "WHERE s.counselor_id = #{counselorId} AND lr.status = 'PENDING' " +
            "ORDER BY lr.is_urgent DESC, lr.created_at ASC")
    List<LeaveRequest> selectPendingByCounselor(@Param("counselorId") Long counselorId);
    
    /**
     * 查询所有待审批的请假（管理员）
     */
    @Select("SELECT *, is_urgent AS urgent FROM lev_leave_request WHERE status IN ('PENDING', 'COUNSELOR_APPROVED') ORDER BY is_urgent DESC, created_at ASC")
    List<LeaveRequest> selectAllPending();
    
    /**
     * 查询待院系领导审批的请假
     */
    @Select("SELECT *, is_urgent AS urgent FROM lev_leave_request WHERE status = 'COUNSELOR_APPROVED' ORDER BY is_urgent DESC, created_at ASC")
    List<LeaveRequest> selectPendingForDepartmentHead();
    
    /**
     * 查询未按时销假的记录
     */
    @Select("SELECT *, is_urgent AS urgent FROM lev_leave_request WHERE status = 'APPROVED' AND end_date < CURDATE() AND return_date IS NULL")
    List<LeaveRequest> selectOverdueReturns();

    /**
     * 统计区间内请假总数
     */
    @Select("""
            SELECT COUNT(*) FROM lev_leave_request
            WHERE 1 = 1
              AND (#{startDate} IS NULL OR created_at >= #{startDate})
              AND (#{endDate} IS NULL OR created_at <= #{endDate})
            """)
    Long countByPeriod(@Param("startDate") java.time.LocalDateTime startDate,
                       @Param("endDate") java.time.LocalDateTime endDate);

    /**
     * 统计区间内各状态数量（待审批 = PENDING + COUNSELOR_APPROVED，与审批列表一致）
     */
    @Select("""
            SELECT
              SUM(CASE WHEN status IN ('PENDING', 'COUNSELOR_APPROVED') THEN 1 ELSE 0 END) AS pendingCount,
              SUM(CASE WHEN status = 'APPROVED' THEN 1 ELSE 0 END) AS approvedCount,
              SUM(CASE WHEN status = 'REJECTED' THEN 1 ELSE 0 END) AS rejectedCount,
              SUM(CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END) AS completedCount,
              SUM(CASE WHEN is_urgent = 1 THEN 1 ELSE 0 END) AS urgentCount
            FROM lev_leave_request
            WHERE 1 = 1
              AND (#{startDate} IS NULL OR created_at >= #{startDate})
              AND (#{endDate} IS NULL OR created_at <= #{endDate})
            """)
    java.util.Map<String, Long> countStatusByPeriod(@Param("startDate") java.time.LocalDateTime startDate,
                                                    @Param("endDate") java.time.LocalDateTime endDate);

    /**
     * 统计区间内总请假天数
     */
    @Select("""
            SELECT COALESCE(SUM(duration), 0) FROM lev_leave_request
            WHERE 1 = 1
              AND (#{startDate} IS NULL OR created_at >= #{startDate})
              AND (#{endDate} IS NULL OR created_at <= #{endDate})
            """)
    java.math.BigDecimal sumDurationByPeriod(@Param("startDate") java.time.LocalDateTime startDate,
                                             @Param("endDate") java.time.LocalDateTime endDate);

    /**
     * 按类型统计
     */
    @Select("""
            SELECT leave_type       AS leaveType,
                   COUNT(*)         AS leaveCount,
                   COALESCE(SUM(duration), 0) AS totalDays
            FROM lev_leave_request
            WHERE 1 = 1
              AND (#{startDate} IS NULL OR created_at >= #{startDate})
              AND (#{endDate} IS NULL OR created_at <= #{endDate})
            GROUP BY leave_type
            """)
    java.util.List<java.util.Map<String, Object>> statsByType(@Param("startDate") java.time.LocalDateTime startDate,
                                                              @Param("endDate") java.time.LocalDateTime endDate);

    /**
     * 按院系统计
     */
    @Select("""
            SELECT s.department                  AS department,
                   COUNT(*)                      AS leaveCount,
                   COUNT(DISTINCT lr.student_id) AS studentCount,
                   COALESCE(SUM(lr.duration), 0) AS totalDays
            FROM lev_leave_request lr
              INNER JOIN stu_student s ON lr.student_id = s.id
            WHERE 1 = 1
              AND (#{startDate} IS NULL OR lr.created_at >= #{startDate})
              AND (#{endDate} IS NULL OR lr.created_at <= #{endDate})
            GROUP BY s.department
            """)
    java.util.List<java.util.Map<String, Object>> statsByDepartment(@Param("startDate") java.time.LocalDateTime startDate,
                                                                    @Param("endDate") java.time.LocalDateTime endDate);

    /**
     * 今日在请假的学生人数（缺勤关联）
     */
    @Select("""
            SELECT COUNT(DISTINCT student_id) FROM lev_leave_request
            WHERE status = 'APPROVED'
              AND start_date <= CURDATE()
              AND end_date >= CURDATE()
            """)
    Long countTodayOnLeave();
}
