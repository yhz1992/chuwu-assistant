import { View, Text } from '@tarojs/components'
import { useCallback } from 'react'
import Icon, { type IconName } from '../Icon'
import './index.less'

interface TabItem {
  text: string
  icon: IconName
}

const TAB_LIST: TabItem[] = [
  { text: '首页', icon: 'home' },
  { text: '收藏', icon: 'heart' },
  { text: '出物', icon: 'tag' },
  { text: '我的', icon: 'user' },
]

const ACTIVE_COLOR = '#4F6EF7'
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
            <View className={`tab-bar__icon ${isActive ? 'is-active' : ''}`}>
              <Icon
                name={item.icon}
                size={48}
                color={isActive ? ACTIVE_COLOR : INACTIVE_COLOR}
              />
            </View>
            <Text className={`tab-bar__label ${isActive ? 'is-active' : ''}`}>
              {item.text}
            </Text>
          </View>
        )
      })}
    </View>
  )
}
