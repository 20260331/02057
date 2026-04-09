import { request } from './request'
import type { PageQuery, PageResult, StudentInfo } from './types'

// 学生查询参数
export interface StudentQuery extends PageQuery {
  studentNo?: string
  name?: string
  department?: string
  major?: string
  classNo?: string
  academicStatus?: string
  counselorId?: number
  enrollmentYear?: number
}

// 更新联系方式
export interface UpdateContactRequest {
  phone?: string
  email?: string
  address?: string
}

// 更新学生信息
export interface UpdateStudentRequest {
  phone?: string
  email?: string
  address?: string
  classNo?: string
  graduationDate?: string
}

// 学籍状态变更
export interface AcademicStatusChangeRequest {
  targetStatus: string
  reason: string
  effectiveDate: string
}

// 导入结果
export interface ImportResult {
  totalCount: number
  successCount: number
  failCount: number
  errors: Array<{
    rowNum: number
    field: string
    message: string
  }>
}

/**
 * 学生相关 API
 */
export const studentApi = {
  /**
   * 获取当前登录学生信息
   */
  getCurrentStudent() {
    return request.get<StudentInfo>('/students/me')
  },

  /**
   * 根据ID获取学生信息
   */
  getById(id: number) {
    return request.get<StudentInfo>(`/students/${id}`)
  },

  /**
   * 根据学号获取学生信息
   */
  getByStudentNo(studentNo: string) {
    return request.get<StudentInfo>(`/students/no/${studentNo}`)
  },

  /**
   * 分页查询学生列表
   */
  queryPage(query: StudentQuery) {
    return request.get<PageResult<StudentInfo>>('/students', { params: query })
  },

  /**
   * 辅导员查询所辖学生
   */
  queryByCounselor(query: StudentQuery) {
    return request.get<PageResult<StudentInfo>>('/students/counselor', { params: query })
  },

  /**
   * 学生更新自己的联系方式
   */
  updateMyContact(data: UpdateContactRequest) {
    return request.put<void>('/students/me/contact', data)
  },

  /**
   * 更新学生联系方式
   */
  updateContact(id: number, data: UpdateContactRequest) {
    return request.put<void>(`/students/${id}/contact`, data)
  },

  /**
   * 更新学生信息
   */
  updateStudent(id: number, data: UpdateStudentRequest) {
    return request.put<void>(`/students/${id}`, data)
  },

  /**
   * 变更学籍状态
   */
  changeAcademicStatus(id: number, data: AcademicStatusChangeRequest) {
    return request.put<void>(`/students/${id}/status`, data)
  },

  /**
   * 下载导入模板
   */
  downloadImportTemplate() {
    return request.download('/students/import/template', '学生导入模板.xlsx')
  },

  /**
   * 导入学生数据
   */
  importStudents(file: File) {
    return request.upload<ImportResult>('/students/import', file)
  },

  /**
   * 导出学生数据
   */
  exportStudents(query: StudentQuery) {
    return request.download('/students/export', '学生信息.xlsx', { params: query })
  },

  /**
   * 生成学籍证明
   */
  generateCertificate(id: number) {
    return request.get<string>(`/students/${id}/certificate`)
  },

  /**
   * 获取变更日志
   */
  getChangeLogs(id: number) {
    return request.get<Array<{
      id: number
      fieldLabel: string
      oldValue: string
      newValue: string
      changeType: string
      reason: string
      operatorName: string
      createdAt: string
    }>>(`/students/${id}/logs`)
  }
}
