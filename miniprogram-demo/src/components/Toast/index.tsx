import { View, Text } from '@tarojs/components'
import { useEffect, useState } from 'react'
import './index.less'

export type ToastType = 'success' | 'error' | 'warning' | 'info'

interface ToastProps {
  visible: boolean
  text: string
  type?: ToastType
  duration?: number
  onClose?: () => void
}

export default function Toast({
  visible,
  text,
  type = 'info',
  duration = 2000,
  onClose,
}: ToastProps) {
  const [show, setShow] = useState(visible)

  useEffect(() => {
    setShow(visible)
  }, [visible])

  useEffect(() => {
    if (show && duration > 0) {
      const timer = setTimeout(() => {
        setShow(false)
        onClose?.()
      }, duration)
      return () => clearTimeout(timer)
    }
  }, [show, duration, onClose])

  if (!show) return null

  return (
    <View className='toast-overlay'>
      <View className={`toast toast--${type}`}>
        <Text className='toast-text'>{text}</Text>
      </View>
    </View>
  )
}
