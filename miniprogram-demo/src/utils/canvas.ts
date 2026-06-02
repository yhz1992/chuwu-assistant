/**
 * Canvas 2D 生成出物长图工具
 * 至少实现"简洁表格款"模板
 */

import Taro from '@tarojs/taro'

export interface SaleListImageData {
  title: string
  items: Array<{
    name: string
    image: string
    price: number
    originalPrice?: number
    note?: string
    characterName?: string
    workName?: string
    status?: string
  }>
}

export interface GenerateOptions {
  watermark?: boolean
  nickname?: string
}

const CANVAS_WIDTH = 750
const ROW_HEIGHT = 110
const MAX_ROWS_PER_PAGE = 25
const PADDING = 40
const TITLE_HEIGHT = 120
const HEADER_HEIGHT = 80

/**
 * 绘制圆角矩形
 */
function roundRect(
  ctx: Taro.CanvasContext,
  x: number,
  y: number,
  w: number,
  h: number,
  r: number,
) {
  ctx.beginPath()
  ctx.moveTo(x + r, y)
  ctx.arcTo(x + w, y, x + w, y + h, r)
  ctx.arcTo(x + w, y + h, x, y + h, r)
  ctx.arcTo(x, y + h, x, y, r)
  ctx.arcTo(x, y, x + w, y, r)
  ctx.closePath()
}

/**
 * 绘制文本（支持截断）
 */
function drawText(
  ctx: Taro.CanvasContext,
  text: string,
  x: number,
  y: number,
  maxWidth: number,
  fontSize: number,
  color: string = '#222222',
  bold: boolean = false,
) {
  ctx.font = `${bold ? 'bold ' : ''}${fontSize}px sans-serif`
  ctx.fillStyle = color
  ctx.textBaseline = 'middle'

  // 简单截断
  let displayText = text
  const textWidth = ctx.measureText(text).width
  if (textWidth > maxWidth) {
    let chars = text.split('')
    let w = 0
    let cutIndex = chars.length
    for (let i = 0; i < chars.length; i++) {
      w += ctx.measureText(chars[i]).width
      if (w > maxWidth - fontSize) {
        cutIndex = i
        break
      }
    }
    displayText = text.substring(0, Math.max(1, cutIndex)) + '...'
  }

  ctx.fillText(displayText, x, y)
}

/**
 * 生成简洁表格款出物长图
 */
async function generateSimpleTable(
  canvasId: string,
  data: SaleListImageData,
  options?: GenerateOptions,
): Promise<string> {
  const dpr = Taro.getSystemInfoSync().pixelRatio
  const canvasWidth = CANVAS_WIDTH
  const totalItems = data.items.length
  const itemsPerPage = MAX_ROWS_PER_PAGE
  const totalPages = Math.ceil(totalItems / itemsPerPage)

  // 计算内容高度
  const contentHeight =
    TITLE_HEIGHT +
    HEADER_HEIGHT +
    Math.min(totalItems, itemsPerPage) * ROW_HEIGHT +
    PADDING * 2

  // 水印高度
  const watermarkHeight = options?.watermark ? 50 : 0
  const canvasHeight = contentHeight + watermarkHeight

  // 获取 Canvas 节点
  const query = Taro.createSelectorQuery()
  const canvasNode = await new Promise<any>((resolve, reject) => {
    query
      .select(`#${canvasId}`)
      .fields({ node: true, size: true })
      .exec((res) => {
        if (res && res[0] && res[0].node) {
          resolve(res[0].node)
        } else {
          reject(new Error('Canvas 节点未找到'))
        }
      })
  })

  const canvas = canvasNode as any
  const ctx = canvas.getContext('2d')

  // 设置 Canvas 尺寸
  canvas.width = canvasWidth * dpr
  canvas.height = canvasHeight * dpr
  ctx.scale(dpr, dpr)

  // 白色背景
  ctx.fillStyle = '#FFFFFF'
  ctx.fillRect(0, 0, canvasWidth, canvasHeight)

  // ----- 表头背景 -----
  ctx.fillStyle = '#FFF0F6'
  roundRect(ctx, 0, 0, canvasWidth, TITLE_HEIGHT + HEADER_HEIGHT, 0)
  ctx.fill()

  // ----- 标题 -----
  const titleY = PADDING + 20
  drawText(ctx, data.title, PADDING, titleY, canvasWidth - PADDING * 2, 32, '#222222', true)

  // 日期
  const now = new Date()
  const dateStr = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`
  ctx.font = '22px sans-serif'
  ctx.fillStyle = '#999999'
  ctx.textBaseline = 'middle'
  ctx.fillText(dateStr, PADDING, titleY + 40)

  // 共 N 件
  ctx.font = '22px sans-serif'
  ctx.fillStyle = '#FF8AB7'
  ctx.textBaseline = 'middle'
  ctx.fillText(`共 ${totalItems} 件`, canvasWidth - PADDING - 80, titleY + 40)

  // ----- 表头行 -----
  const headerY = TITLE_HEIGHT + 10
  ctx.font = '24px sans-serif'
  ctx.fillStyle = '#666666'
  ctx.textBaseline = 'middle'

  // 序号
  ctx.fillText('序号', PADDING, headerY + HEADER_HEIGHT / 2)
  // 商品名
  ctx.fillText('商品', PADDING + 60, headerY + HEADER_HEIGHT / 2)
  // 价格
  ctx.fillText('价格', canvasWidth - PADDING - 120, headerY + HEADER_HEIGHT / 2)
  // 备注
  ctx.fillText('备注', canvasWidth - PADDING - 40, headerY + HEADER_HEIGHT / 2)

  // 分隔线
  ctx.strokeStyle = '#F0E6EC'
  ctx.lineWidth = 2
  ctx.beginPath()
  ctx.moveTo(PADDING, headerY + HEADER_HEIGHT)
  ctx.lineTo(canvasWidth - PADDING, headerY + HEADER_HEIGHT)
  ctx.stroke()

  // ----- 数据行 -----
  const itemsToDraw = data.items.slice(0, itemsPerPage)
  for (let i = 0; i < itemsToDraw.length; i++) {
    const item = itemsToDraw[i]
    const rowY = headerY + HEADER_HEIGHT + i * ROW_HEIGHT

    // 行背景交替
    if (i % 2 === 1) {
      ctx.fillStyle = '#FFF8FB'
      ctx.fillRect(PADDING, rowY, canvasWidth - PADDING * 2, ROW_HEIGHT)
    }

    // 序号
    ctx.font = '24px sans-serif'
    ctx.fillStyle = '#999999'
    ctx.textBaseline = 'middle'
    ctx.fillText(String(i + 1), PADDING, rowY + ROW_HEIGHT / 2)

    // 封面图（88x88，圆角12）
    const imgX = PADDING + 50
    const imgY = rowY + (ROW_HEIGHT - 88) / 2
    const imgSize = 88
    const imgRadius = 12

    if (item.image) {
      try {
        const imgInfo = await Taro.getImageInfo({ src: item.image })
        // 绘制圆角图片
        roundRect(ctx, imgX, imgY, imgSize, imgSize, imgRadius)
        ctx.save()
        ctx.clip()
        ctx.drawImage(imgInfo.path, imgX, imgY, imgSize, imgSize)
        ctx.restore()
      } catch {
        // 如果图片加载失败，绘制占位背景
        ctx.fillStyle = '#FFE4F0'
        roundRect(ctx, imgX, imgY, imgSize, imgSize, imgRadius)
        ctx.fill()
      }
    } else {
      // 无图片，绘制占位背景
      ctx.fillStyle = '#FFE4F0'
      roundRect(ctx, imgX, imgY, imgSize, imgSize, imgRadius)
      ctx.fill()
    }

    // 商品名称（26px）
    drawText(
      ctx,
      item.name || '-',
      imgX + imgSize + 16,
      rowY + ROW_HEIGHT / 2,
      canvasWidth - (imgX + imgSize + 16 + 200),
      26,
      '#222222',
    )

    // 价格（28px red）
    const priceText = `¥${item.price || 0}`
    ctx.font = 'bold 28px sans-serif'
    ctx.fillStyle = '#FF6B6B'
    ctx.textBaseline = 'middle'
    ctx.fillText(
      priceText,
      canvasWidth - PADDING - 200,
      rowY + ROW_HEIGHT / 2 - 10,
    )

    // 原价（20px 删除线）
    if (item.originalPrice && item.originalPrice > item.price) {
      const originalPriceText = `¥${item.originalPrice}`
      ctx.font = '20px sans-serif'
      ctx.fillStyle = '#999999'
      ctx.textBaseline = 'middle'
      const origX = canvasWidth - PADDING - 200
      ctx.fillText(originalPriceText, origX, rowY + ROW_HEIGHT / 2 + 18)

      // 删除线
      const origWidth = ctx.measureText(originalPriceText).width
      ctx.strokeStyle = '#999999'
      ctx.lineWidth = 1
      ctx.beginPath()
      ctx.moveTo(origX, rowY + ROW_HEIGHT / 2 + 18)
      ctx.lineTo(origX + origWidth, rowY + ROW_HEIGHT / 2 + 18)
      ctx.stroke()
    }

    // 备注
    if (item.note) {
      drawText(
        ctx,
        item.note,
        canvasWidth - PADDING - 170,
        rowY + ROW_HEIGHT / 2,
        130,
        20,
        '#999999',
      )
    }

    // 分隔线
    ctx.strokeStyle = '#EEEEEE'
    ctx.lineWidth = 1
    ctx.beginPath()
    ctx.moveTo(PADDING, rowY + ROW_HEIGHT)
    ctx.lineTo(canvasWidth - PADDING, rowY + ROW_HEIGHT)
    ctx.stroke()
  }

  // ----- 水印 -----
  if (options?.watermark && options?.nickname) {
    ctx.font = '18px sans-serif'
    ctx.fillStyle = 'rgba(0, 0, 0, 0.4)'
    ctx.textBaseline = 'bottom'
    ctx.textAlign = 'right'
    const watermarkText = `出物小助手 · @${options.nickname}`
    ctx.fillText(watermarkText, canvasWidth - PADDING, canvasHeight - 10)
    ctx.textAlign = 'left'
  }

  // ----- 生成临时文件 -----
  return new Promise((resolve, reject) => {
    Taro.canvasToTempFilePath(
      {
        canvas: canvas,
        canvasId: canvasId,
        width: canvasWidth * dpr,
        height: canvasHeight * dpr,
        destWidth: canvasWidth * dpr,
        destHeight: canvasHeight * dpr,
        fileType: 'png',
        quality: 1,
      },
      (res) => {
        if (res.tempFilePath) {
          resolve(res.tempFilePath)
        } else {
          reject(new Error('生成图片失败'))
        }
      },
    )
  })
}

/**
 * 生成出物清单长图
 */
export async function generateSaleListImage(
  canvasId: string,
  saleListData: SaleListImageData,
  template: 'simple' | 'card' | 'wall' = 'simple',
  options?: GenerateOptions,
): Promise<string> {
  if (template === 'simple') {
    return generateSimpleTable(canvasId, saleListData, options)
  }

  // card 和 wall 模板暂回退到 simple
  console.warn(`模板 "${template}" 尚未实现，回退到 "simple" 模板`)
  return generateSimpleTable(canvasId, saleListData, options)
}

export default {
  generateSaleListImage,
}
