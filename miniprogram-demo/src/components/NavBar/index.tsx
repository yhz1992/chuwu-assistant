import { View, Text } from '@tarojs/components'
import Taro from '@tarojs/taro'
import Icon from '../Icon'
import './index.less'

interface NavBarProps {
  title: string
  showBack?: boolean
  onBack?: () => void
  rightContent?: React.ReactNode
}

export default function NavBar({ title, showBack = false, onBack, rightContent }: NavBarProps) {
  const handleBack = () => {
    if (onBack) {
      onBack()
    } else {
      Taro.navigateBack()
    }
  }

  return (
    <View className='navbar'>
      <View className='navbar-left'>
        {showBack && (
          <View className='navbar-back-btn' onClick={handleBack}>
            <Icon name='chevron-right' size={40} color='#1A1A1A' style={{ transform: 'rotate(180deg)' }} />
          </View>
        )}
      </View>
      <Text className='navbar-title'>{title}</Text>
      <View className='navbar-right'>{rightContent}</View>
    </View>
  )
}
