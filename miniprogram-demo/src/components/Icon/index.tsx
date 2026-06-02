import { View } from '@tarojs/components'
import { CSSProperties } from 'react'
import type { IconName } from './icons'
import './index.less'

export type { IconName }

interface IconProps {
  name: IconName
  size?: number       // rpx，默认 32
  color?: string      // 任意 CSS 颜色，默认 '#333333'
  className?: string
  style?: CSSProperties
}

/**
 * 图标组件
 *
 * 形状通过 CSS class `icon-{name}` 定义在 index.less 中（-webkit-mask-image），
 * 颜色和尺寸通过 inline style 控制。这是微信小程序的兼容方案：
 * 小程序的 WXSS 支持 -webkit-mask-image，但 inline style 不支持。
 */
export default function Icon({ name, size = 32, color = '#333333', className = '', style }: IconProps) {
  return (
    <View
      className={`icon icon-${name} ${className}`}
      style={{
        width: `${size}rpx`,
        height: `${size}rpx`,
        backgroundColor: color,
        ...style,
      }}
    />
  )
}
