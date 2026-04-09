import { request } from './request'
import type { PageQuery, PageResult, CourseInfo } from './types'

// 课程查询参数
export interface CourseQuery extends PageQuery {
  keyword?: string
  semester?: string
  category?: string
  department?: string
  teacherId?: number
}

// 课程创建/更新数据
export interface CourseFormData {
  courseCode: string
  name: string
  credits: number
  teacherId?: number
  teacherName?: string
  schedule?: string
  location?: string
  capacity: number
  semester: string
  category: string
  department?: string
  description?: string
}

// 选课结果
export interface SelectionResult {
  success: boolean
  message: string
  status?: string
  conflictCourses?: CourseInfo[]
  missingPrerequisites?: string[]
}

// 选课阶段详情
export interface SelectionPhaseInfo {
  currentPhase: string
  currentPhaseName: string
  canSelect: boolean
  canWithdraw: boolean
  selectionMode: string
  lotteryEnabled: boolean
  preSelectionStart: string | null
  preSelectionEnd: string | null
  formalSelectionStart: string | null
  formalSelectionEnd: string | null
  adjustmentStart: string | null
  adjustmentEnd: string | null
  nextPhaseName: string | null
  nextPhaseStart: string | null
}

// 抽签课程信息
export interface LotteryCourse {
  courseId: number
  courseCode: string
  courseName: string
  credits: number
  teacherName: string
  schedule: string
  location: string
  semester: string
  category: string
  capacity: number
  enrolledCount: number
  pendingCount: number
  lotteryExecuted: boolean
}

// 选课记录
export interface CourseSelection {
  id: number
  studentId: number
  courseId: number
  courseCode: string
  courseName: string
  credits: number
  teacherName: string
  schedule: string
  location: string
  status: string
  selectedAt: string
}

// 课表项
export interface ScheduleItem {
  courseId: number
  courseCode: string
  courseName: string
  credits: number
  teacherName: string
  schedule: string
  location: string
  dayOfWeek: number
  startSection: number
  endSection: number
}

// 课程名单项
export interface RosterItem {
  studentId: number
  studentNo: string
  studentName: string
  department: string
  major: string
  classNo: string
  selectedAt: string
}

/**
 * 课程相关 API
 */
export const courseApi = {
  /**
   * 分页查询课程
   */
  queryCourses(query: CourseQuery) {
    return request.get<PageResult<CourseInfo>>('/courses', { params: query })
  },

  /**
   * 搜索课程
   */
  searchCourses(keyword: string, semester?: string) {
    return request.get<CourseInfo[]>('/courses/search', { params: { keyword, semester } })
  },

  /**
   * 获取课程详情
   */
  getCourseById(id: number) {
    return request.get<CourseInfo>(`/courses/${id}`)
  },

  /**
   * 创建课程
   */
  createCourse(data: CourseFormData) {
    return request.post<CourseInfo>('/courses', data)
  },

  /**
   * 更新课程
   */
  updateCourse(id: number, data: CourseFormData) {
    return request.put<CourseInfo>(`/courses/${id}`, data)
  },

  /**
   * 删除课程
   */
  deleteCourse(id: number) {
    return request.delete(`/courses/${id}`)
  },

  /**
   * 获取教师课程
   */
  getTeacherCourses() {
    return request.get<CourseInfo[]>('/courses/teacher')
  },

  /**
   * 选课
   */
  selectCourse(courseId: number) {
    return request.post<SelectionResult>('/course-selections', { courseId })
  },

  /**
   * 退课
   */
  withdrawCourse(courseId: number) {
    return request.delete<void>(`/course-selections/${courseId}`)
  },

  /**
   * 获取我的选课记录
   */
  getMySelections() {
    return request.get<CourseSelection[]>('/course-selections/my')
  },

  /**
   * 获取我的课表
   */
  getMySchedule() {
    return request.get<ScheduleItem[]>('/course-selections/schedule')
  },

  /**
   * 获取课程名单
   */
  getCourseRoster(courseId: number) {
    return request.get<RosterItem[]>(`/course-selections/roster/${courseId}`)
  },

  /**
   * 检查时间冲突
   */
  checkConflict(courseId: number) {
    return request.get<CourseInfo[]>('/course-selections/check-conflict', { params: { courseId } })
  },

  /**
   * 检查先修课程
   */
  checkPrerequisites(courseId: number) {
    return request.get<string[]>('/course-selections/check-prerequisites', { params: { courseId } })
  },

  /**
   * 获取当前选课阶段（简单版）
   */
  getSelectionPhase() {
    return request.get<{ phase: string }>('/course-selections/phase')
  },

  /**
   * 获取选课阶段完整信息
   */
  getPhaseInfo() {
    return request.get<SelectionPhaseInfo>('/course-selections/phase/info')
  },

  /**
   * 获取需要抽签的课程列表
   */
  getLotteryPendingCourses() {
    return request.get<LotteryCourse[]>('/course-selections/lottery/pending')
  },

  /**
   * 对单门课程执行抽签
   */
  executeLottery(courseId: number) {
    return request.post<void>(`/course-selections/lottery/${courseId}`)
  },

  /**
   * 批量执行所有课程抽签
   */
  executeLotteryAll() {
    return request.post<{ processedCount: number }>('/course-selections/lottery/all')
  }
}
