import Taro from '@tarojs/taro'

const BASE_URL = 'http://localhost:8080/api/v1'

interface RequestOptions {
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE' | 'PATCH'
  data?: any
  header?: any
  skipAuth?: boolean
  showLoading?: boolean
}

interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}

let refreshingPromise: Promise<boolean> | null = null

/**
 * 刷新 access token
 */
async function refreshAccessToken(): Promise<boolean> {
  const refreshToken = Taro.getStorageSync('refreshToken')
  if (!refreshToken) {
    return false
  }

  try {
    const res = await Taro.request<ApiResponse<{ accessToken: string; refreshToken: string }>>({
      url: `${BASE_URL}/auth/refresh`,
      method: 'POST',
      data: { refreshToken },
      header: { 'Content-Type': 'application/json' },
    })

    if (res.data.code === 0 && res.data.data) {
      const { accessToken: newAccessToken, refreshToken: newRefreshToken } = res.data.data
      Taro.setStorageSync('token', newAccessToken)
      Taro.setStorageSync('refreshToken', newRefreshToken)
      return true
    }

    return false
  } catch {
    return false
  }
}

const request = async <T>(url: string, options: RequestOptions = {}): Promise<T> => {
  const {
    method = 'GET',
    data,
    header = {},
    skipAuth = false,
    showLoading = false,
  } = options

  const headers: any = {
    'Content-Type': 'application/json',
    ...header,
  }

  // 添加 token
  if (!skipAuth) {
    const token = Taro.getStorageSync('token')
    if (token) {
      headers['Authorization'] = `Bearer ${token}`
    }
  }

  // 显示加载
  if (showLoading) {
    Taro.showLoading({ title: '加载中...', mask: true })
  }

  try {
    const res = await Taro.request<ApiResponse<T>>({
      url: `${BASE_URL}${url}`,
      method,
      data,
      header: headers,
    })

    if (showLoading) {
      Taro.hideLoading()
    }

    const { code, data: responseData } = res.data

    // token 过期，尝试刷新
    if (code === 1002) {
      if (!refreshingPromise) {
        refreshingPromise = refreshAccessToken()
      }

      const refreshed = await refreshingPromise
      refreshingPromise = null

      if (refreshed) {
        // 刷新成功，重发原请求
        const newToken = Taro.getStorageSync('token')
        headers['Authorization'] = `Bearer ${newToken}`

        const retryRes = await Taro.request<ApiResponse<T>>({
          url: `${BASE_URL}${url}`,
          method,
          data,
          header: headers,
        })

        if (retryRes.data.code === 0) {
          return retryRes.data.data as T
        }

        // 重试仍然失败
        if (retryRes.data.code === 1002 || retryRes.data.code === 1003) {
          clearLoginState()
          throw new Error(retryRes.data.message || '登录已过期，请重新登录')
        }

        throw new Error(retryRes.data.message || '请求失败')
      } else {
        // refresh 失败
        clearLoginState()
        throw new Error('登录已过期，请重新登录')
      }
    }

    // refresh token 过期
    if (code === 1003) {
      clearLoginState()
      throw new Error('登录已过期，请重新登录')
    }

    // 业务错误（所有非零、非 token 相关的错误码）
    if (code !== 0) {
      throw new Error(res.data.message || '请求失败')
    }

    return responseData as T
  } catch (err: any) {
    if (showLoading) {
      Taro.hideLoading()
    }

    // 如果是 Taro.request 抛出的网络错误
    if (err.errMsg || err.message?.includes('request:fail')) {
      Taro.showToast({ title: '网络异常，请稍后重试', icon: 'none' })
      throw new Error('网络异常，请稍后重试')
    }

    // 业务异常已在上面 throw，这里直接传播
    throw err
  }
}

function clearLoginState() {
  Taro.removeStorageSync('token')
  Taro.removeStorageSync('refreshToken')
  Taro.removeStorageSync('userInfo')
  Taro.showToast({ title: '登录已过期', icon: 'none' })
  setTimeout(() => {
    Taro.reLaunch({ url: '/pages/login/index' })
  }, 1500)
}

// ===== 便捷方法 =====
export function get<T>(url: string, params?: Record<string, any>, options?: RequestOptions): Promise<T> {
  return request<T>(url, {
    method: 'GET',
    data: params,
    ...options,
  })
}

export function post<T>(url: string, data?: any, options?: RequestOptions): Promise<T> {
  return request<T>(url, {
    method: 'POST',
    data,
    ...options,
  })
}

export function put<T>(url: string, data?: any, options?: RequestOptions): Promise<T> {
  return request<T>(url, {
    method: 'PUT',
    data,
    ...options,
  })
}

export function del<T>(url: string, options?: RequestOptions): Promise<T> {
  return request<T>(url, {
    method: 'DELETE',
    ...options,
  })
}

export function patch<T>(url: string, data?: any, options?: RequestOptions): Promise<T> {
  return request<T>(url, {
    method: 'PATCH',
    data,
    ...options,
  })
}

/**
 * 图片上传
 * @param url 上传地址
 * @param filePath 本地文件路径
 * @param scene 上传场景（区分是收藏图片还是出物图片）
 */
export function upload<T>(
  url: string,
  filePath: string,
  scene: 'collection' | 'sale',
): Promise<T> {
  return new Promise((resolve, reject) => {
    const token = Taro.getStorageSync('token')

    Taro.uploadFile({
      url: `${BASE_URL}${url}`,
      filePath,
      name: 'file',
      header: {
        Authorization: `Bearer ${token}`,
      },
      formData: {
        scene,
      },
      success: (res) => {
        try {
          const data: ApiResponse<T> = JSON.parse(res.data)
          if (data.code === 0) {
            resolve(data.data as T)
          } else {
            reject(new Error(data.message || '上传失败'))
          }
        } catch {
          reject(new Error('上传响应解析失败'))
        }
      },
      fail: (err) => {
        reject(new Error(err.errMsg || '上传失败'))
      },
    })
  })
}

export default request
