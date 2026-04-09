// 用户信息
export interface UserInfo {
  id: number
  username: string
  realName: string
  phone?: string
  email?: string
  roles: string[]
  avatar?: string
}

// 登录请求
export interface LoginRequest {
  username: string
  encryptedPassword: string
}

export interface EncryptionKeyResponse {
  algorithm: string
  publicKey: string
}

// 登录响应
export interface LoginResponse {
  token: string
  refreshToken: string
  userInfo: UserInfo
  permissions: string[]
}

// 分页查询参数
export interface PageQuery {
  page: number
  size: number
  [key: string]: unknown
}

// 分页响应
export interface PageResult<T> {
  records: T[]
  total: number
  page: number
  size: number
  pages: number
}

// 学生信息
export interface StudentInfo {
  id: number
  studentNo: string
  name: string
  gender: string
  birthDate: string
  idNumber: string
  phone: string
  email: string
  department: string
  major: string
  classNo: string
  enrollmentDate: string
  academicStatus: AcademicStatus
  counselorId: number
}

// 学籍状态
export enum AcademicStatus {
  ENROLLED = 'ENROLLED',
  SUSPENDED = 'SUSPENDED',
  WITHDRAWN = 'WITHDRAWN',
  GRADUATED = 'GRADUATED',
  TRANSFERRED = 'TRANSFERRED'
}

// 课程信息
export interface CourseInfo {
  id: number
  courseCode: string
  name: string
  credits: number
  teacherName: string
  schedule: string
  location: string
  capacity: number
  enrolledCount: number
  availableCapacity: number
  prerequisites: string[]
  description: string
  semester: string
  category: string
}

// 成绩信息
export interface GradeInfo {
  id: number
  studentId?: number
  courseCode: string
  courseName: string
  credits: number
  score: number
  letterGrade: string
  gradePoints: number
  semester: string
  status: GradeStatus
}

// 成绩状态
export enum GradeStatus {
  DRAFT = 'DRAFT',
  SUBMITTED = 'SUBMITTED',
  APPROVED = 'APPROVED'
}

// GPA 信息
export interface GPAInfo {
  cumulativeGPA: number
  totalCredits: number
  earnedCredits: number
  semesterGPAs: SemesterGPA[]
}

export interface SemesterGPA {
  semester: string
  gpa: number
}

// 请假类型
export enum LeaveType {
  SICK = 'SICK',
  PERSONAL = 'PERSONAL',
  OFFICIAL = 'OFFICIAL'
}

// 请假状态
export enum LeaveStatus {
  PENDING = 'PENDING',
  COUNSELOR_APPROVED = 'COUNSELOR_APPROVED',
  APPROVED = 'APPROVED',
  REJECTED = 'REJECTED',
  COMPLETED = 'COMPLETED'
}

// 请假信息
export interface LeaveInfo {
  id: number
  studentId: number
  studentName: string
  // 后端字段为 leaveType / leaveTypeText
  leaveType: LeaveType
  leaveTypeText?: string
  startDate: string
  endDate: string
  duration: number
  reason: string
  status: LeaveStatus
  statusText?: string
  urgent?: boolean
  approvalHistory: ApprovalRecord[]
  attachments: string[]
  createdAt: string
  returnDate?: string
}

// 审批记录
export interface ApprovalRecord {
  id: number
  approverId: number
  approverName: string
  approverRole?: string
  approved: boolean
  comment: string
  createdAt: string
}
