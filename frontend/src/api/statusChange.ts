import { request } from './request'

// 学籍异动申请
export interface StatusChangeApplicationData {
  targetStatus: string
  reason: string
  effectiveDate: string
}

// 审批请求
export interface StatusChangeApprovalData {
  approved: boolean
  comment: string
}

// 审批记录
export interface StatusChangeApprovalRecord {
  id: number
  approverId: number
  approverName: string
  approverRole: string
  approved: boolean
  comment: string
  createdAt: string
}

// 学籍异动申请信息
export interface StatusChangeInfo {
  id: number
  studentId: number
  studentNo: string
  studentName: string
  department: string
  classNo: string
  currentStatus: string
  currentStatusText: string
  targetStatus: string
  targetStatusText: string
  reason: string
  effectiveDate: string
  status: string
  statusText: string
  executed: boolean
  approvedAt: string
  createdAt: string
  approvalHistory: StatusChangeApprovalRecord[]
}

/**
 * 学籍异动相关 API
 */
export const statusChangeApi = {
  submitApplication(data: StatusChangeApplicationData) {
    return request.post<number>('/status-changes', data)
  },

  getApplicationDetail(id: number) {
    return request.get<StatusChangeInfo>(`/status-changes/${id}`)
  },

  getMyApplications() {
    return request.get<StatusChangeInfo[]>('/status-changes/my')
  },

  getPendingForCounselor() {
    return request.get<StatusChangeInfo[]>('/status-changes/pending/counselor')
  },

  getPendingForAcademicAffairs() {
    return request.get<StatusChangeInfo[]>('/status-changes/pending/academic')
  },

  approveApplication(id: number, data: StatusChangeApprovalData) {
    return request.put<void>(`/status-changes/${id}/approve`, data)
  },

  cancelApplication(id: number) {
    return request.put<void>(`/status-changes/${id}/cancel`)
  }
}
