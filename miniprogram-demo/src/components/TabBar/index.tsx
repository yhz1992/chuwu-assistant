import { View, Text } from '@tarojs/components'
import { useCallback } from 'react'
import Icon, { type IconName } from '../Icon'
import './index.less'

interface TabItem {
  text: string
  icon: IconName
  /** 选中态背景色，同时影响文字色 */
  color: string
}

const TAB_LIST: TabItem[] = [
  { text: '首页', icon: 'home', color: '#FF69B4' },   // Pink
  { text: '收藏', icon: 'heart', color: '#98FF98' },   // Mint
  { text: '出物', icon: 'tag', color: '#FFBC99' },     // Peach
  { text: '我的', icon: 'user', color: '#000000' },    // Black
]

const INACTIVE_COLOR = '#999999'

interface TabBarProps {
  current: number
  onChange: (index: number) => void
}

export default function TabBar({ current, onChange }: TabBarProps) {
  const handleSwitch = useCallback(
    (index: number) => {
      if (index === current) return
      onChange(index)
    },
    [current, onChange],
  )

  return (
    <View className='tab-bar'>
      {TAB_LIST.map((item, index) => {
        const isActive = index === current
        return (
          <View
            key={item.text}
            className='tab-bar__item'
            onClick={() => handleSwitch(index)}
          >
            <View
              className={`tab-bar__icon ${isActive ? 'is-active' : ''}`}
              style={isActive ? { background: item.color } : undefined}
            >
              <Icon
                name={item.icon}
                size={48}
                color={isActive ? '#FFFFFF' : INACTIVE_COLOR}
              />
            </View>
            <Text
              className={`tab-bar__label ${isActive ? 'is-active' : ''}`}
              style={isActive ? { color: item.color } : undefined}
            >
              {item.text}
            </Text>
          </View>
        )
      })}
    </View>
  )
}
