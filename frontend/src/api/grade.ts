import { request } from './request'
import type { GradeInfo, GPAInfo } from './types'

// 成绩单课程成绩
export interface TranscriptCourseGrade {
  courseCode: string
  courseName: string
  credits: number
  score: number
  letterGrade: string
  gradePoints: number
}

// 成绩单学期分组
export interface TranscriptSemester {
  semester: string
  semesterGPA: number
  semesterCredits: number
  courses: TranscriptCourseGrade[]
}

// 成绩单数据
export interface TranscriptData {
  studentNo: string
  studentName: string
  gender: string
  department: string
  major: string
  classNo: string
  enrollmentDate: string
  academicStatus: string
  cumulativeGPA: number
  totalCredits: number
  earnedCredits: number
  totalCourses: number
  passedCourses: number
  semesters: TranscriptSemester[]
  schoolName: string
  generatedDate: string
  htmlContent: string
}

// 批量成绩录入
export interface BatchGradeRequest {
  courseId: number
  grades: Array<{
    studentId: number
    score: number
  }>
}

// 成绩修改
export interface GradeUpdateRequest {
  score: number
  reason: string
}

// 成绩统计
export interface GradeStatistics {
  courseId: number
  courseName: string
  totalStudents: number
  averageScore: number
  maxScore: number
  minScore: number
  passRate: number
  excellentRate: number
  distribution: Record<string, number>
}

// 成绩修改记录
export interface GradeChangeLog {
  id: number
  gradeId: number
  studentId: number
  studentNo: string
  studentName: string
  courseId: number
  oldScore: number
  newScore: number
  reason: string
  operatorId: number
  operatorName: string
  createdAt: string
}

/**
 * 成绩相关 API
 */
export const gradeApi = {
  /**
   * 获取我的成绩
   */
  getMyGrades(semester?: string) {
    return request.get<GradeInfo[]>('/grades/my', { params: { semester } })
  },

  /**
   * 获取学生成绩
   */
  getStudentGrades(studentId: number, semester?: string) {
    return request.get<GradeInfo[]>(`/grades/student/${studentId}`, { params: { semester } })
  },

  /**
   * 获取课程成绩
   */
  getCourseGrades(courseId: number) {
    return request.get<GradeInfo[]>(`/grades/course/${courseId}`)
  },

  /**
   * 批量录入成绩
   */
  batchSaveGrades(data: BatchGradeRequest) {
    return request.post<void>('/grades/batch', data)
  },

  /**
   * 批量录入并提交成绩（一步完成保存和提交）
   */
  batchSaveAndSubmitGrades(data: BatchGradeRequest) {
    return request.post<void>('/grades/batch-submit', data)
  },

  /**
   * 修改成绩
   */
  updateGrade(id: number, data: GradeUpdateRequest) {
    return request.put<void>(`/grades/${id}`, data)
  },

  /**
   * 提交成绩
   */
  submitGrades(courseId: number) {
    return request.post<void>(`/grades/submit/${courseId}`)
  },

  /**
   * 审批成绩
   */
  approveGrades(courseId: number) {
    return request.post<void>(`/grades/approve/${courseId}`)
  },

  /**
   * 获取我的 GPA
   */
  getMyGPA() {
    return request.get<GPAInfo>('/grades/gpa')
  },

  /**
   * 获取学生 GPA
   */
  getStudentGPA(studentId: number) {
    return request.get<GPAInfo>(`/grades/gpa/${studentId}`)
  },

  /**
   * 获取课程成绩统计
   */
  getStatistics(courseId: number) {
    return request.get<GradeStatistics>(`/grades/statistics/${courseId}`)
  },

  /**
   * 获取课程成绩修改记录
   */
  getChangeLogs(courseId: number) {
    return request.get<GradeChangeLog[]>(`/grades/change-logs/${courseId}`)
  },

  /**
   * 获取我的成绩单
   */
  getMyTranscript() {
    return request.get<TranscriptData>('/grades/transcript')
  },

  /**
   * 获取指定学生的成绩单
   */
  getStudentTranscript(studentId: number) {
    return request.get<TranscriptData>(`/grades/transcript/${studentId}`)
  },

  /**
   * 导出我的成绩单 HTML 文件
   */
  exportMyTranscript() {
    return request.get('/grades/transcript/export', { responseType: 'blob' })
  },

  /**
   * 导出指定学生的成绩单 HTML 文件
   */
  exportStudentTranscript(studentId: number) {
    return request.get(`/grades/transcript/export/${studentId}`, { responseType: 'blob' })
  },

  /**
   * 查询我是否存在不及格成绩（成绩预警）
   */
  hasMyFailingGrades() {
    return request.get<boolean>('/grades/my/failing')
  }
}
