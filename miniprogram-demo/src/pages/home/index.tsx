import { useState, useCallback, useEffect } from 'react'
import { View, Text, ScrollView } from '@tarojs/components'
import Taro, { useDidShow } from '@tarojs/taro'
import { homeApi, HomeOverview } from '../../services/api'
import CollectionCard from '../../components/CollectionCard'
import StatusTag from '../../components/StatusTag'
import EmptyState from '../../components/EmptyState'
import Loading from '../../components/Loading'
import Icon from '../../components/Icon'
import './index.less'

/** 快捷操作项 */
const QUICK_ACTIONS = [
  { key: 'add', label: '记一件谷子', icon: 'plus' as const, path: '/pages/collection-add/index' },
  { key: 'sale', label: '生成出物清单', icon: 'document' as const, path: '/pages/select-items/index' },
  { key: 'wish', label: '添加心愿', icon: 'star' as const, path: '/pages/wishlist-add/index' },
  { key: 'arrival', label: '查看待到货', icon: 'package' as const, path: '/pages/collections/index' },
]

function getGreeting(): string {
  const hour = new Date().getHours()
  if (hour < 6) return '凌晨好'
  if (hour < 12) return '早上好'
  if (hour < 14) return '中午好'
  if (hour < 18) return '下午好'
  return '晚上好'
}

export default function Home() {
  const [loading, setLoading] = useState(true)
  const [overview, setOverview] = useState<HomeOverview | null>(null)

  useDidShow(() => {
    fetchOverview()
  })

  // 作为子组件（非页面）时 useDidShow 不会触发，添加 useEffect 兜底
  useEffect(() => {
    fetchOverview()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  const fetchOverview = async () => {
    setLoading(true)
    try {
      const data = await homeApi.getOverview()
      setOverview(data)
    } catch (err: any) {
      console.error('获取首页概览失败', err)
      Taro.showToast({ title: err.message || '加载失败', icon: 'none' })
    } finally {
      setLoading(false)
    }
  }

  const handleSearchClick = useCallback(() => {
    Taro.eventCenter.trigger('switchTab', 1)
  }, [])

  const handleQuickAction = useCallback(
    (action: (typeof QUICK_ACTIONS)[number]) => {
      if (action.key === 'sale') {
        const total = overview?.stats.totalCollections ?? 0
        if (total === 0) {
          Taro.showModal({
            title: '提示',
            content: '还没有收藏品，先去记录一些吧',
            confirmText: '去记录',
            success: (res) => {
              if (res.confirm) {
                Taro.navigateTo({ url: '/pages/collection-add/index' })
              }
            },
          })
          return
        }
      }

      if (action.key === 'arrival') {
        Taro.eventCenter.trigger('switchTab', 1)
        return
      }

      Taro.navigateTo({ url: action.path })
    },
    [overview],
  )

  const handleCollectionPress = useCallback((id: string) => {
    Taro.navigateTo({ url: `/pages/collection-detail/index?id=${id}` })
  }, [])

  const handleSaleListPress = useCallback((id: string) => {
    Taro.navigateTo({ url: `/pages/generate-result/index?id=${id}` })
  }, [])

  if (loading) {
    return <Loading text='加载中...' />
  }

  const stats = overview?.stats ?? {
    totalCollections: 0,
    totalSaleLists: 0,
    totalSoldItems: 0,
    wishlistCount: 0,
  }
  const recentCollections = overview?.recentCollections ?? []
  const recentSaleLists = overview?.recentSaleLists ?? []

  const hasNoCollections = stats.totalCollections === 0

  return (
    <View className='home'>
      {/* 顶部区域 */}
      <View className='home-header'>
        <View className='home-header-top'>
          <Text className='home-greeting'>{getGreeting()}</Text>
          <Text className='home-greeting-sub'>今天也要好好整理收藏呀</Text>
        </View>
      </View>

      {/* 搜索框 */}
      <View className='home-search' onClick={handleSearchClick}>
        <Icon name='search' size={32} color='#999' />
        <Text className='home-search-placeholder'>搜索名称、角色、作品</Text>
      </View>

      {/* 数据卡片 */}
      <View className='home-stats'>
        <View className='home-stat-card'>
          <Text className='home-stat-value'>{stats.totalCollections}</Text>
          <Text className='home-stat-label'>收藏</Text>
        </View>
        <View className='home-stat-card'>
          <Text className='home-stat-value'>{stats.totalSaleLists}</Text>
          <Text className='home-stat-label'>出物清单</Text>
        </View>
        <View className='home-stat-card'>
          <Text className='home-stat-value'>{stats.wishlistCount}</Text>
          <Text className='home-stat-label'>心愿</Text>
        </View>
        <View className='home-stat-card'>
          <Text className='home-stat-value'>{stats.totalSoldItems}</Text>
          <Text className='home-stat-label'>已出物</Text>
        </View>
      </View>

      {/* 快捷操作 */}
      <View className='home-actions'>
        {QUICK_ACTIONS.map((action) => (
          <View
            key={action.key}
            className='home-action-btn'
            onClick={() => handleQuickAction(action)}
          >
            <View className='home-action-icon-wrap'>
              <Icon name={action.icon} size={40} color='#4F6EF7' />
            </View>
            <Text className='home-action-label'>{action.label}</Text>
          </View>
        ))}
      </View>

      {/* 空状态引导 */}
      {hasNoCollections && (
        <View className='home-empty-guide'>
          <View className='home-empty-guide-card'>
            <Icon name='package' size={64} color='#4F6EF7' />
            <Text className='home-empty-guide-title'>还没有记录收藏</Text>
            <Text className='home-empty-guide-desc'>
              点击上方"记一件谷子"开始记录你的收藏吧
            </Text>
          </View>
        </View>
      )}

      {/* 最近收藏横滑 */}
      {!hasNoCollections && (
        <View className='home-section'>
          <View className='home-section-header'>
            <Text className='home-section-title'>最近收藏</Text>
            <View
              className='home-section-more'
              onClick={() => Taro.eventCenter.trigger('switchTab', 1)}
            >
              <Text className='home-section-more-text'>查看全部</Text>
              <Icon name='chevron-right' size={28} color='#999' />
            </View>
          </View>
          <ScrollView
            className='home-collections-scroll'
            scrollX
            showScrollbar={false}
          >
            <View className='home-collections-row'>
              {recentCollections.map((item) => (
                <View
                  key={item.id}
                  className='home-collection-item'
                  onClick={() => handleCollectionPress(item.id)}
                >
                  <CollectionCard
                    item={
                      {
                        id: item.id,
                        name: item.name,
                        coverImage: item.coverImage,
                        images: item.coverImage ? [item.coverImage] : [],
                        workName: item.workName,
                        characterName: item.characterName,
                        status: item.status,
                        createdAt: '',
                        updatedAt: '',
                      } as any
                    }
                  />
                </View>
              ))}
            </View>
          </ScrollView>
        </View>
      )}

      {/* 最近出物清单 */}
      {!hasNoCollections && (
        <View className='home-section'>
          <View className='home-section-header'>
            <Text className='home-section-title'>最近出物清单</Text>
          </View>
          {recentSaleLists.length === 0 ? (
            <EmptyState text='还没有出物清单' />
          ) : (
            <View className='home-salelist-list'>
              {recentSaleLists.map((item) => (
                <View
                  key={item.id}
                  className='home-salelist-card'
                  onClick={() => handleSaleListPress(item.id)}
                >
                  <View className='home-salelist-card-top'>
                    <Text className='home-salelist-card-title' numberOfLines={1}>
                      {item.title}
                    </Text>
                    <StatusTag status={item.status as any} size='small' />
                  </View>
                  <View className='home-salelist-card-info'>
                    <Text className='home-salelist-card-count'>
                      共 {item.totalCount} 件
                    </Text>
                    <Text className='home-salelist-card-price'>
                      ¥{item.totalPrice}
                    </Text>
                  </View>
                  <Text className='home-salelist-card-time'>{item.createdAt}</Text>
                </View>
              ))}
            </View>
          )}
        </View>
      )}
    </View>
  )
}
