/**
 * 工具函数集合
 */

/**
 * 日期格式化
 * @param date 日期对象或字符串
 * @param format 格式化模板，默认 YYYY-MM-DD
 */
export function formatDate(date: Date | string, format = 'YYYY-MM-DD'): string {
  const d = typeof date === 'string' ? new Date(date) : date
  
  if (isNaN(d.getTime())) {
    return ''
  }
  
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hours = String(d.getHours()).padStart(2, '0')
  const minutes = String(d.getMinutes()).padStart(2, '0')
  const seconds = String(d.getSeconds()).padStart(2, '0')
  
  return format
    .replace('YYYY', String(year))
    .replace('MM', month)
    .replace('DD', day)
    .replace('HH', hours)
    .replace('mm', minutes)
    .replace('ss', seconds)
}

/**
 * 日期时间格式化
 */
export function formatDateTime(date: Date | string): string {
  return formatDate(date, 'YYYY-MM-DD HH:mm:ss')
}

/**
 * 手机号脱敏
 * @param phone 手机号
 */
export function maskPhone(phone: string): string {
  if (!phone || phone.length < 7) return phone
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

/**
 * 身份证号脱敏
 * @param idNumber 身份证号
 */
export function maskIdNumber(idNumber: string): string {
  if (!idNumber || idNumber.length < 8) return idNumber
  return idNumber.replace(/^(.{4}).*(.{4})$/, '$1**********$2')
}

/**
 * 邮箱脱敏
 * @param email 邮箱
 */
export function maskEmail(email: string): string {
  if (!email || !email.includes('@')) return email
  const [name, domain] = email.split('@')
  const maskedName = name.length > 2 
    ? name.substring(0, 2) + '***' 
    : name + '***'
  return `${maskedName}@${domain}`
}

/**
 * 防抖函数
 */
export function debounce<T extends (...args: unknown[]) => unknown>(
  fn: T,
  delay = 300
): (...args: Parameters<T>) => void {
  let timer: ReturnType<typeof setTimeout> | null = null
  
  return function (this: unknown, ...args: Parameters<T>) {
    if (timer) clearTimeout(timer)
    timer = setTimeout(() => {
      fn.apply(this, args)
    }, delay)
  }
}

/**
 * 节流函数
 */
export function throttle<T extends (...args: unknown[]) => unknown>(
  fn: T,
  delay = 300
): (...args: Parameters<T>) => void {
  let lastTime = 0
  
  return function (this: unknown, ...args: Parameters<T>) {
    const now = Date.now()
    if (now - lastTime >= delay) {
      lastTime = now
      fn.apply(this, args)
    }
  }
}

/**
 * 深拷贝
 */
export function deepClone<T>(obj: T): T {
  if (obj === null || typeof obj !== 'object') {
    return obj
  }
  
  if (obj instanceof Date) {
    return new Date(obj.getTime()) as T
  }
  
  if (Array.isArray(obj)) {
    return obj.map(item => deepClone(item)) as T
  }
  
  const cloned = {} as T
  for (const key in obj) {
    if (Object.prototype.hasOwnProperty.call(obj, key)) {
      cloned[key] = deepClone(obj[key])
    }
  }
  
  return cloned
}

/**
 * 生成唯一 ID
 */
export function generateId(): string {
  return Date.now().toString(36) + Math.random().toString(36).substring(2)
}

/**
 * 文件大小格式化
 */
export function formatFileSize(bytes: number): string {
  if (bytes === 0) return '0 B'
  
  const units = ['B', 'KB', 'MB', 'GB', 'TB']
  const k = 1024
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + units[i]
}

/**
 * 学籍状态映射
 */
export const academicStatusMap: Record<string, string> = {
  ENROLLED: '在读',
  SUSPENDED: '休学',
  WITHDRAWN: '退学',
  GRADUATED: '毕业',
  TRANSFERRED: '转学'
}

/**
 * 请假类型映射
 */
export const leaveTypeMap: Record<string, string> = {
  SICK: '病假',
  PERSONAL: '事假',
  OFFICIAL: '公假'
}

/**
 * 请假状态映射
 */
export const leaveStatusMap: Record<string, string> = {
  PENDING: '待审批',
  COUNSELOR_APPROVED: '辅导员已批',
  APPROVED: '已批准',
  REJECTED: '已拒绝',
  COMPLETED: '已销假'
}

/**
 * 成绩状态映射
 */
export const gradeStatusMap: Record<string, string> = {
  DRAFT: '草稿',
  SUBMITTED: '已提交',
  APPROVED: '已确认'
}
