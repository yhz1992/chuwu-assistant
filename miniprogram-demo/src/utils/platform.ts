import Taro from '@tarojs/taro'

/** 是否运行在 H5 环境 */
export const isH5 = Taro.getEnv() === Taro.ENV_TYPE.WEB

/** 是否运行在微信小程序环境 */
export const isWeapp = Taro.getEnv() === Taro.ENV_TYPE.WEAPP

/** 是否运行在开发/浏览器模式 */
export const isDev = isH5 || process.env.NODE_ENV === 'development'

/** 生成模拟的微信 code（H5 开发模式使用） */
export function generateMockCode(): string {
  return 'h5_dev_' + Date.now() + '_' + Math.random().toString(36).slice(2, 10)
}

/** H5 环境选择图片 */
export async function chooseImageH5(count = 9): Promise<string[]> {
  return new Promise((resolve, reject) => {
    const input = document.createElement('input')
    input.type = 'file'
    input.accept = 'image/*'
    input.multiple = count > 1
    input.onchange = async () => {
      const files = Array.from(input.files || [])
      if (files.length === 0) {
        reject(new Error('cancel'))
        return
      }
      const urls = await Promise.all(
        files.slice(0, count).map((file) => {
          return new Promise<string>((res) => {
            const reader = new FileReader()
            reader.onload = () => res(reader.result as string)
            reader.readAsDataURL(file)
          })
        }),
      )
      resolve(urls)
    }
    input.click()
  })
}

/** H5 环境触发下载 */
export function downloadFileH5(url: string, filename?: string): void {
  const a = document.createElement('a')
  a.href = url
  a.download = filename || '出物清单.png'
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
}

/** H5 环境复制到剪贴板 */
export async function copyToClipboardH5(text: string): Promise<void> {
  if (navigator.clipboard && navigator.clipboard.writeText) {
    await navigator.clipboard.writeText(text)
  } else {
    const textarea = document.createElement('textarea')
    textarea.value = text
    textarea.style.position = 'fixed'
    textarea.style.opacity = '0'
    document.body.appendChild(textarea)
    textarea.select()
    document.execCommand('copy')
    document.body.removeChild(textarea)
  }
}
