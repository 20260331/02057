import { request } from './request'

export interface CertificateRequest {
  certType: string
  purpose?: string
  copies?: number
}

export interface CertificateInfo {
  id: number
  studentId: number
  studentNo: string
  studentName: string
  department: string
  certNo: string
  certType: string
  certTypeText: string
  certTitle: string
  certContent: string
  purpose: string
  copies: number
  status: string
  statusText: string
  generatedByName: string
  revokedAt: string
  revokeReason: string
  createdAt: string
}

export const certTypeOptions = [
  { value: 'ENROLLMENT', label: '学籍证明' },
  { value: 'ATTENDANCE', label: '在读证明' },
  { value: 'GRADUATION', label: '毕业证明' },
  { value: 'TRANSCRIPT', label: '成绩证明' },
  { value: 'STATUS_CHANGE', label: '学籍异动证明' }
]

export const certificateApi = {
  generate(data: CertificateRequest) {
    return request.post<CertificateInfo>('/certificates', data)
  },

  generateForStudent(studentId: number, data: CertificateRequest) {
    return request.post<CertificateInfo>(`/certificates/student/${studentId}`, data)
  },

  getById(id: number) {
    return request.get<CertificateInfo>(`/certificates/${id}`)
  },

  verify(certNo: string) {
    return request.get<CertificateInfo>(`/certificates/verify/${certNo}`)
  },

  getMyCertificates() {
    return request.get<CertificateInfo[]>('/certificates/my')
  },

  getStudentCertificates(studentId: number) {
    return request.get<CertificateInfo[]>(`/certificates/student/${studentId}`)
  },

  getRecentCertificates(limit = 100) {
    return request.get<CertificateInfo[]>('/certificates/recent', { params: { limit } })
  },

  getAvailableTypes() {
    return request.get<string[]>('/certificates/types')
  },

  revoke(id: number, reason: string) {
    return request.put<void>(`/certificates/${id}/revoke`, { reason })
  }
}
