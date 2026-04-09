package com.university.sms.student.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.university.sms.student.entity.CertificateRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 学籍证明记录 Mapper
 */
@Mapper
public interface CertificateRecordMapper extends BaseMapper<CertificateRecord> {

    @Select("SELECT * FROM stu_certificate_record WHERE student_id = #{studentId} ORDER BY created_at DESC")
    List<CertificateRecord> selectByStudentId(@Param("studentId") Long studentId);

    @Select("SELECT * FROM stu_certificate_record WHERE cert_no = #{certNo}")
    CertificateRecord selectByCertNo(@Param("certNo") String certNo);

    @Select("SELECT * FROM stu_certificate_record ORDER BY created_at DESC LIMIT #{limit}")
    List<CertificateRecord> selectRecent(@Param("limit") int limit);
}
