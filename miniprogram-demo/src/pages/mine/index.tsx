import { useEffect, useState, useCallback } from 'react'
import { View, Text, Image } from '@tarojs/components'
import Taro from '@tarojs/taro'
import { userApi, UserMeResponse } from '../../services/api'
import userStore from '../../stores/userStore'
import Loading from '../../components/Loading'
import Icon from '../../components/Icon'
import './index.less'

interface MenuItem {
  key: string
  label: string
  icon: import('../../components/Icon').IconName
  path?: string
  badge?: string
  action?: () => void
}

export default function Mine() {
  const [loading, setLoading] = useState(true)
  const [userData, setUserData] = useState<UserMeResponse | null>(null)

  useEffect(() => {
    loadUserData()
  }, [])

  const loadUserData = async () => {
    setLoading(true)
    try {
      const data = await userApi.getMe()
      setUserData(data)
    } catch {
      setUserData(null)
    } finally {
      setLoading(false)
    }
  }

  const handleLogin = useCallback(() => {
    Taro.navigateTo({ url: '/pages/login/index' })
  }, [])

  const user = userData || userStore.getUserInfo()
  const stats = userData?.stats

  const menuItems: MenuItem[] = [
    {
      key: 'templates',
      label: '模板中心',
      icon: 'template',
      path: '/pages/template-select/index',
    },
    {
      key: 'shares',
      label: '我的分享',
      icon: 'share',
      path: '/pages/my-shares/index',
    },
    {
      key: 'export',
      label: '数据导出',
      icon: 'download',
      badge: '即将开放',
    },
    {
      key: 'feedback',
      label: '意见反馈',
      icon: 'chat',
      path: '/pages/feedback/index',
    },
    {
      key: 'manual',
      label: '使用说明',
      icon: 'help',
    },
    {
      key: 'about',
      label: '关于我们',
      icon: 'info',
    },
  ]

  const handleMenuClick = useCallback((item: MenuItem) => {
    if (item.action) {
      item.action()
      return
    }
    if (item.path) {
      Taro.navigateTo({ url: item.path })
    } else {
      Taro.showToast({ title: item.badge || '即将开放', icon: 'none' })
    }
  }, [])

  if (loading) {
    return <Loading text='加载中...' />
  }

  const version = 'v1.0.0'

  return (
    <View className='mine'>
      {/* 用户信息 */}
      <View className='mine-profile' onClick={user ? undefined : handleLogin}>
        <View className='mine-avatar-wrap'>
          {user?.avatar ? (
            <Image className='mine-avatar' src={user.avatar} mode='aspectFill' />
          ) : (
            <View className='mine-avatar-placeholder'>
              <Icon name='user' size={52} color='#BFBFBF' />
            </View>
          )}
        </View>
        <Text className='mine-nickname'>
          {user?.nickname || '点击登录'}
        </Text>
        {user?.nickname && (
          <Text className='mine-uid'>共同记录美好收藏</Text>
        )}
      </View>

      {/* 数据统计卡片 */}
      {stats && (
        <View className='mine-stats'>
          <View className='mine-stat-item'>
            <Text className='mine-stat-value'>{stats.collectionCount}</Text>
            <Text className='mine-stat-label'>收藏总数</Text>
          </View>
          <View className='mine-stat-item'>
            <Text className='mine-stat-value'>{stats.saleListCount}</Text>
            <Text className='mine-stat-label'>出物清单</Text>
          </View>
          <View className='mine-stat-item'>
            <Text className='mine-stat-value'>{stats.wishlistCount}</Text>
            <Text className='mine-stat-label'>心愿数量</Text>
          </View>
          <View className='mine-stat-item'>
            <Text className='mine-stat-value'>{stats.soldCount}</Text>
            <Text className='mine-stat-label'>已出物</Text>
          </View>
        </View>
      )}

      {/* 功能入口 */}
      <View className='mine-menu'>
        {menuItems.map((item) => (
          <View
            key={item.key}
            className='mine-menu-item'
            onClick={() => handleMenuClick(item)}
          >
            <View className='mine-menu-item-left'>
              <View className='mine-menu-item-icon-wrap'>
                <Icon name={item.icon} size={36} color='#4F6EF7' />
              </View>
              <Text className='mine-menu-item-label'>{item.label}</Text>
            </View>
            <View className='mine-menu-item-right'>
              {item.badge && (
                <Text className='mine-menu-item-badge'>{item.badge}</Text>
              )}
              <Icon name='chevron-right' size={28} color='#BFBFBF' />
            </View>
          </View>
        ))}
      </View>

      {/* 版本号 */}
      <View className='mine-version'>
        <Text className='mine-version-text'>出物小助手 {version}</Text>
      </View>
    </View>
  )
}
