import { request } from './request'
import type { LeaveInfo } from './types'

// 请假申请
export interface LeaveRequestData {
  leaveType: string
  startDate: string
  endDate: string
  reason: string
  urgent?: boolean
}

// 请假统计
export interface LeaveTypeStat {
  leaveType: string
  leaveTypeText: string
  leaveCount: number
  totalDays: number
}

export interface LeaveDepartmentStat {
  department: string
  leaveCount: number
  studentCount: number
  totalDays: number
}

export interface LeaveStatistics {
  totalLeaves: number
  pendingLeaves: number
  approvedLeaves: number
  rejectedLeaves: number
  completedLeaves: number
  urgentLeaves: number
  totalDays: number
  todayOnLeaveCount: number
  overdueReturnCount: number
  byType: LeaveTypeStat[]
  byDepartment: LeaveDepartmentStat[]
}

// 审批请求
export interface ApprovalData {
  approved: boolean
  comment: string
}

// 附件信息
export interface AttachmentInfo {
  id: number
  fileName: string
  fileSize: string
  fileType: string
  createdAt: string
}

/**
 * 请假相关 API
 */
export const leaveApi = {
  /**
   * 提交请假申请
   */
  submitLeave(data: LeaveRequestData) {
    return request.post<number>('/leaves', data)
  },
  
  /**
   * 获取请假详情
   */
  getLeaveDetail(id: number) {
    return request.get<LeaveInfo>(`/leaves/${id}`)
  },
  
  /**
   * 获取我的请假记录
   */
  getMyLeaves() {
    return request.get<LeaveInfo[]>('/leaves/my')
  },
  
  /**
   * 获取待审批列表（辅导员）
   */
  getPendingForCounselor() {
    return request.get<LeaveInfo[]>('/leaves/pending/counselor')
  },

  /**
   * 获取紧急待审批列表（辅导员-快速通道）
   */
  getUrgentPendingForCounselor() {
    return request.get<LeaveInfo[]>('/leaves/pending/counselor/urgent')
  },
  
  /**
   * 获取待审批列表（院系领导）
   */
  getPendingForDepartment() {
    return request.get<LeaveInfo[]>('/leaves/pending/department')
  },

  /**
   * 获取紧急待审批列表（院系领导-快速通道）
   */
  getUrgentPendingForDepartment() {
    return request.get<LeaveInfo[]>('/leaves/pending/department/urgent')
  },
  
  /**
   * 审批请假
   */
  approveLeave(id: number, data: ApprovalData) {
    return request.put<void>(`/leaves/${id}/approve`, data)
  },
  
  /**
   * 销假
   */
  returnFromLeave(id: number, returnDate: string) {
    return request.put<void>(`/leaves/${id}/return`, { returnDate })
  },
  
  /**
   * 获取请假附件列表
   */
  getAttachments(leaveId: number) {
    return request.get<AttachmentInfo[]>(`/files/leave/${leaveId}`)
  },
  
  /**
   * 删除附件
   */
  deleteAttachment(id: number) {
    return request.delete(`/files/${id}`)
  },

  /**
   * 请假统计与分析
   */
  getStatistics(params: { startDate?: string; endDate?: string }) {
    return request.get<LeaveStatistics>('/leaves/statistics', { params })
  }
}
