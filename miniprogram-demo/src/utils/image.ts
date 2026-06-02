/** 图片服务基地址（开发环境） */
const IMAGE_BASE = 'http://localhost:8080'

/**
 * 将后端返回的相对图片路径转为完整可访问 URL
 * - 已经是完整 URL（http/https）→ 原样返回
 * - 以 /uploads/ 开头 → 拼接 IMAGE_BASE
 * - 空值 → 返回空字符串
 */
export function getImageUrl(path: string | null | undefined): string {
  if (!path) return ''
  if (path.startsWith('http://') || path.startsWith('https://')) return path
  if (path.startsWith('/uploads/')) return IMAGE_BASE + path
  return path
}
