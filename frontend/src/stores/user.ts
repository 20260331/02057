/**
 * 用户 Store - 作为 auth store 的别名
 * 提供用户信息相关的便捷访问
 */
import { useAuthStore } from './auth'

// 导出 useUserStore 作为 useAuthStore 的别名
export const useUserStore = useAuthStore
