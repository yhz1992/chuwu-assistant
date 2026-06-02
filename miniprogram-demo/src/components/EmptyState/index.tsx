import { View, Text } from '@tarojs/components'
import Icon from '../Icon'
import type { IconName } from '../Icon'
import './index.less'

interface EmptyStateProps {
  text?: string
  icon?: IconName
  subText?: string
}

export default function EmptyState({ text = '暂无数据', icon = 'package', subText }: EmptyStateProps) {
  return (
    <View className='empty-state'>
      <View className='empty-state-icon-wrap'>
        <Icon name={icon} size={80} color='#D0D5DD' />
      </View>
      <Text className='empty-state-text'>{text}</Text>
      {subText && <Text className='empty-state-sub-text'>{subText}</Text>}
    </View>
  )
}
