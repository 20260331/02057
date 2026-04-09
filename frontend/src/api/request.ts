import axios, { type AxiosInstance, type AxiosRequestConfig, type AxiosResponse, type InternalAxiosRequestConfig } from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import router from '@/router'

// 响应数据结构
export interface ApiResponse<T = unknown> {
  code: number
  message: string
  data: T
}

// 创建 axios 实例
const service: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
service.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const authStore = useAuthStore()
    
    // 添加 token 到请求头
    if (authStore.token) {
      config.headers.Authorization = `Bearer ${authStore.token}`
    }
    
    return config
  },
  (error) => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    // 如果是 blob 类型（文件下载），直接返回
    if (response.config.responseType === 'blob') {
      return response
    }
    
    const res = response.data
    
    // 业务状态码判断
    if (res.code !== 200) {
      // 关闭之前的消息，避免重复
      ElMessage.closeAll()
      ElMessage.error(res.message || '请求失败')
      
      // 特定错误码处理
      if (res.code === 401) {
        handleUnauthorized()
      }
      
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    
    return response
  },
  (error) => {
    console.error('响应错误:', error)
    
    // 关闭之前的消息，避免重复
    ElMessage.closeAll()
    
    if (error.response) {
      const { status, data } = error.response
      
      switch (status) {
        case 401:
          handleUnauthorized()
          break
        case 403:
          ElMessage.error(data?.message || '没有权限访问该资源')
          break
        case 404:
          ElMessage.error('请求的资源不存在')
          break
        case 500:
          ElMessage.error(data?.message || '服务器内部错误')
          break
        default:
          ElMessage.error(data?.message || `请求失败 (${status})`)
      }
    } else if (error.code === 'ECONNABORTED') {
      ElMessage.error('请求超时，请稍后重试')
    } else {
      ElMessage.error('网络连接失败，请检查网络')
    }
    
    return Promise.reject(error)
  }
)

// 处理未授权
let isRefreshing = false
function handleUnauthorized() {
  if (isRefreshing) return
  
  isRefreshing = true
  const authStore = useAuthStore()
  
  ElMessageBox.confirm('登录状态已过期，请重新登录', '提示', {
    confirmButtonText: '重新登录',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    authStore.logout()
    router.push({ name: 'Login' })
  }).finally(() => {
    isRefreshing = false
  })
}

// 封装请求方法
export const request = {
  get<T = unknown>(url: string, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
    return service.get(url, config).then(res => res.data)
  },
  
  post<T = unknown>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
    return service.post(url, data, config).then(res => res.data)
  },
  
  put<T = unknown>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
    return service.put(url, data, config).then(res => res.data)
  },
  
  delete<T = unknown>(url: string, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
    return service.delete(url, config).then(res => res.data)
  },
  
  // 文件上传
  upload<T = unknown>(url: string, file: File, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
    const formData = new FormData()
    formData.append('file', file)
    
    return service.post(url, formData, {
      ...config,
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    }).then(res => res.data)
  },
  
  // 文件下载
  download(url: string, filename: string, config?: AxiosRequestConfig): Promise<void> {
    return service.get(url, {
      ...config,
      responseType: 'blob'
    }).then(res => {
      const blob = new Blob([res.data])
      const link = document.createElement('a')
      link.href = URL.createObjectURL(blob)
      link.download = filename
      link.click()
      URL.revokeObjectURL(link.href)
    })
  }
}

export default service
