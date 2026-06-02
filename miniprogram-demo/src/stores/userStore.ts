import Taro from '@tarojs/taro'

export interface UserInfo {
  id: string
  nickname: string
  avatar: string
}

interface UserState {
  token: string | null
  refreshToken: string | null
  userInfo: UserInfo | null
}

const STORAGE_KEYS = {
  TOKEN: 'token',
  REFRESH_TOKEN: 'refreshToken',
  USER_INFO: 'userInfo',
}

/**
 * 简易状态管理 - 用户模块
 *
 * 基于模块级变量 + Taro 持久化存储。
 * 组件内如需响应式更新，可通过 useUser hook 读取当前快照。
 */
const userStore = {
  /** 获取当前 token */
  getToken(): string | null {
    return Taro.getStorageSync(STORAGE_KEYS.TOKEN) || null
  },

  /** 获取 refresh token */
  getRefreshToken(): string | null {
    return Taro.getStorageSync(STORAGE_KEYS.REFRESH_TOKEN) || null
  },

  /** 获取用户信息 */
  getUserInfo(): UserInfo | null {
    const raw = Taro.getStorageSync(STORAGE_KEYS.USER_INFO)
    return raw || null
  },

  /** 获取完整状态快照 */
  getState(): UserState {
    return {
      token: this.getToken(),
      refreshToken: this.getRefreshToken(),
      userInfo: this.getUserInfo(),
    }
  },

  /** 是否已登录 */
  isLogin(): boolean {
    return !!this.getToken()
  },

  /** 设置登录态 */
  setLogin(token: string, refreshToken: string, userInfo: UserInfo): void {
    Taro.setStorageSync(STORAGE_KEYS.TOKEN, token)
    Taro.setStorageSync(STORAGE_KEYS.REFRESH_TOKEN, refreshToken)
    Taro.setStorageSync(STORAGE_KEYS.USER_INFO, userInfo)
  },

  /** 更新用户信息 */
  setUserInfo(userInfo: UserInfo): void {
    Taro.setStorageSync(STORAGE_KEYS.USER_INFO, userInfo)
  },

  /** 登出 - 清除所有登录态 */
  logout(): void {
    Taro.removeStorageSync(STORAGE_KEYS.TOKEN)
    Taro.removeStorageSync(STORAGE_KEYS.REFRESH_TOKEN)
    Taro.removeStorageSync(STORAGE_KEYS.USER_INFO)
  },
}

export default userStore
