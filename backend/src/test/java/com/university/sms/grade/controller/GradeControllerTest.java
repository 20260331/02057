package com.university.sms.grade.controller;

import com.university.sms.common.response.Result;
import com.university.sms.grade.dto.GPAInfoDTO;
import com.university.sms.grade.dto.GradeDTO;
import com.university.sms.grade.dto.TranscriptDTO;
import com.university.sms.grade.service.GradeService;
import com.university.sms.student.dto.StudentDTO;
import com.university.sms.student.service.StudentService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GradeController 单元测试")
class GradeControllerTest {

    @Mock private GradeService gradeService;
    @Mock private StudentService studentService;
    @Mock private HttpServletRequest request;

    @InjectMocks
    private GradeController controller;

    private StudentDTO student;

    @BeforeEach
    void setUp() {
        student = new StudentDTO();
        student.setId(1L);
        student.setStudentNo("2024001");
    }

    @Nested
    @DisplayName("getMyGrades - 查询我的成绩")
    class GetMyGradesTests {

        @Test
        @DisplayName("正常返回成绩列表")
        void shouldReturnGradeList() {
            when(request.getAttribute("userId")).thenReturn(100L);
            when(studentService.getByUserId(100L)).thenReturn(student);

            GradeDTO g = new GradeDTO();
            g.setCourseName("高等数学");
            g.setScore(new BigDecimal("90"));
            when(gradeService.getStudentGrades(1L, null)).thenReturn(List.of(g));

            Result<List<GradeDTO>> result = controller.getMyGrades(null, request);

            assertThat(result.getCode()).isEqualTo(200);
            assertThat(result.getData()).hasSize(1);
            assertThat(result.getData().get(0).getCourseName()).isEqualTo("高等数学");
        }

        @Test
        @DisplayName("按学期筛选")
        void shouldFilterBySemester() {
            when(request.getAttribute("userId")).thenReturn(100L);
            when(studentService.getByUserId(100L)).thenReturn(student);
            when(gradeService.getStudentGrades(1L, "2024-2025-1")).thenReturn(List.of());

            controller.getMyGrades("2024-2025-1", request);

            verify(gradeService).getStudentGrades(1L, "2024-2025-1");
        }
    }

    @Nested
    @DisplayName("getMyGPA - 查询我的 GPA")
    class GetMyGPATests {

        @Test
        @DisplayName("正常返回 GPA 信息")
        void shouldReturnGPAInfo() {
            when(request.getAttribute("userId")).thenReturn(100L);
            when(studentService.getByUserId(100L)).thenReturn(student);

            GPAInfoDTO gpa = new GPAInfoDTO();
            gpa.setCumulativeGPA(new BigDecimal("3.57"));
            gpa.setTotalCredits(new BigDecimal("20"));
            when(gradeService.calculateGPA(1L)).thenReturn(gpa);

            Result<GPAInfoDTO> result = controller.getMyGPA(request);

            assertThat(result.getCode()).isEqualTo(200);
            assertThat(result.getData().getCumulativeGPA()).isEqualByComparingTo(new BigDecimal("3.57"));
        }
    }

    @Nested
    @DisplayName("getMyTranscript - 获取成绩单")
    class GetTranscriptTests {

        @Test
        @DisplayName("正常返回成绩单")
        void shouldReturnTranscript() {
            when(request.getAttribute("userId")).thenReturn(100L);
            when(studentService.getByUserId(100L)).thenReturn(student);

            TranscriptDTO transcript = new TranscriptDTO();
            transcript.setStudentName("张三");
            transcript.setHtmlContent("<html>transcript</html>");
            when(gradeService.generateTranscript(1L)).thenReturn(transcript);

            Result<TranscriptDTO> result = controller.getMyTranscript(request);

            assertThat(result.getCode()).isEqualTo(200);
            assertThat(result.getData().getStudentName()).isEqualTo("张三");
            assertThat(result.getData().getHtmlContent()).contains("transcript");
        }
    }

    @Nested
    @DisplayName("getStudentGrades - 管理员查询学生成绩")
    class GetStudentGradesTests {

        @Test
        @DisplayName("按 studentId 查询")
        void shouldQueryByStudentId() {
            GradeDTO g = new GradeDTO();
            g.setCourseName("物理");
            when(gradeService.getStudentGrades(5L, "2024-2025-1")).thenReturn(List.of(g));

            Result<List<GradeDTO>> result = controller.getStudentGrades(5L, "2024-2025-1");

            assertThat(result.getCode()).isEqualTo(200);
            assertThat(result.getData()).hasSize(1);
        }
    }
}
