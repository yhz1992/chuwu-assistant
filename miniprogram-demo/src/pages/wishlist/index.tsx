import { useState, useEffect, useRef, useCallback } from 'react'
import {
  View,
  Text,
  ScrollView,
  Input,
  Image,
} from '@tarojs/components'
import Taro, { usePullDownRefresh, useReachBottom } from '@tarojs/taro'
import {
  wishlistApi,
  WishlistItem,
  WishlistListParams,
} from '../../services/api'
import EmptyState from '../../components/EmptyState'
import Icon from '../../components/Icon'
import Toast from '../../components/Toast'
import './index.less'

const STATUS_TABS = [
  { label: '全部', value: '' },
  { label: '想买', value: 'wish' },
  { label: '已入', value: 'bought' },
  { label: '暂不买', value: 'postponed' },
] as const

const PRIORITY_MAP: Record<string, { label: string; className: string }> = {
  normal: { label: '一般想买', className: 'priority-normal' },
  high: { label: '很想买', className: 'priority-high' },
  must: { label: '必入', className: 'priority-must' },
}

const STATUS_MAP: Record<string, string> = {
  wish: '想买', bought: '已入', postponed: '暂不买',
}

export default function Wishlist() {
  const [list, setList] = useState<WishlistItem[]>([])
  const [loading, setLoading] = useState(true)
  const [refreshing, setRefreshing] = useState(false)
  const [loadingMore, setLoadingMore] = useState(false)
  const [page, setPage] = useState(1)
  const [hasMore, setHasMore] = useState(true)
  const pageSize = 20

  const [keyword, setKeyword] = useState('')
  const [activeStatus, setActiveStatus] = useState('')

  const [toastVisible, setToastVisible] = useState(false)
  const [toastText, setToastText] = useState('')

  const searchTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null)
  const loadedRef = useRef(false)

  const showToast = (text: string) => {
    setToastText(text)
    setToastVisible(true)
  }

  const buildParams = useCallback(
    (p: number): WishlistListParams => {
      const params: WishlistListParams = { page: p, pageSize }
      if (keyword.trim()) params.keyword = keyword.trim()
      if (activeStatus) params.status = activeStatus
      return params
    },
    [keyword, activeStatus],
  )

  const fetchList = useCallback(
    async (p: number, isRefresh = false) => {
      try {
        const params = buildParams(p)
        const res = await wishlistApi.getList(params)
        const records = Array.isArray(res) ? res : []
        if (isRefresh || p === 1) {
          setList(records)
        } else {
          setList((prev) => [...prev, ...records])
        }
        setHasMore(records.length >= pageSize)
        setPage(p)
      } catch (err: any) {
        Taro.showToast({ title: err.message || '加载失败', icon: 'none' })
      }
    },
    [buildParams],
  )

  useEffect(() => {
    if (loadedRef.current) return
    loadedRef.current = true
    setLoading(true)
    fetchList(1).finally(() => setLoading(false))
  }, [fetchList])

  useEffect(() => {
    if (!loadedRef.current) return
    setLoading(true)
    setList([])
    setPage(1)
    setHasMore(true)
    fetchList(1).finally(() => setLoading(false))
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [activeStatus])

  usePullDownRefresh(() => {
    setRefreshing(true)
    fetchList(1, true).finally(() => {
      setRefreshing(false)
      Taro.stopPullDownRefresh()
    })
  })

  useReachBottom(() => {
    if (loadingMore || !hasMore) return
    setLoadingMore(true)
    fetchList(page + 1).finally(() => setLoadingMore(false))
  })

  const handleSearchInput = (value: string) => {
    setKeyword(value)
    if (searchTimerRef.current) clearTimeout(searchTimerRef.current)
    searchTimerRef.current = setTimeout(() => {
      setLoading(true)
      setList([])
      setPage(1)
      setHasMore(true)
      fetchList(1, true).finally(() => setLoading(false))
    }, 300)
  }

  const handleStatusFilter = (status: string) => {
    setActiveStatus(status)
  }

  const handleAdd = () => {
    Taro.navigateTo({ url: '/pages/wishlist-add/index' })
  }

  const handleEdit = (item: WishlistItem) => {
    Taro.navigateTo({ url: `/pages/wishlist-add/index?id=${item.id}` })
  }

  const handleMarkAsBought = (item: WishlistItem) => {
    Taro.showModal({
      title: '标记已入',
      content: `确认已入手「${item.name}」？将转为收藏品`,
      success: async (res) => {
        if (res.confirm) {
          try {
            const result = await wishlistApi.convertToCollection(item.id, {
              name: item.name,
              images: item.image ? [item.image] : undefined,
            })
            Taro.showToast({ title: '已转为收藏', icon: 'success' })
            setList((prev) => prev.filter((i) => i.id !== item.id))
            if (result && result.id) {
              Taro.navigateTo({ url: `/pages/collection-add/index?id=${result.id}` })
            } else {
              Taro.navigateTo({ url: `/pages/collection-add/index?name=${encodeURIComponent(item.name)}` })
            }
          } catch (err: any) {
            Taro.showToast({ title: err.message || '转换失败', icon: 'none' })
          }
        }
      },
    })
  }

  const handleDelete = (item: WishlistItem) => {
    Taro.showModal({
      title: '确认删除',
      content: `确定要删除「${item.name}」吗？`,
      success: async (res) => {
        if (res.confirm) {
          try {
            await wishlistApi.delete(item.id)
            Taro.showToast({ title: '删除成功', icon: 'success' })
            setList((prev) => prev.filter((i) => i.id !== item.id))
          } catch (err: any) {
            Taro.showToast({ title: err.message || '删除失败', icon: 'none' })
          }
        }
      },
    })
  }

  const handleItemAction = (item: WishlistItem) => {
    const actions = ['编辑', '标记已入', '删除']
    if (item.status === 'bought') {
      actions.splice(1, 1)
    }
    Taro.showActionSheet({ itemList: actions })
      .then((res) => {
        switch (res.tapIndex) {
          case 0: handleEdit(item); break
          case 1:
            if (item.status === 'bought') { handleDelete(item) }
            else { handleMarkAsBought(item) }
            break
          case 2: handleDelete(item); break
        }
      })
      .catch(() => {})
  }

  const extractExtraInfo = (item: WishlistItem) => {
    if (!item.description) return {}
    try {
      const parsed = JSON.parse(item.description)
      if (parsed && typeof parsed === 'object') {
        return {
          workName: parsed.workName || '',
          characterName: parsed.characterName || '',
          category: parsed.category || '',
          notes: parsed.notes || '',
        }
      }
    } catch { /* 非 JSON 格式 */ }
    return {}
  }

  const renderCard = (item: WishlistItem) => {
    const extra = extractExtraInfo(item)
    const subtitle = [extra.characterName, extra.workName].filter(Boolean).join(' / ')
    const priorityConfig = PRIORITY_MAP[item.priority]

    return (
      <View key={item.id} className='wishlist-card' onClick={() => handleItemAction(item)}>
        <View className='wishlist-card-image-wrap'>
          {item.image ? (
            <Image className='wishlist-card-image' src={item.image} mode='aspectFill' />
          ) : (
            <View className='wishlist-card-image-placeholder'>
              <Icon name='star' size={44} color='#BFBFBF' />
            </View>
          )}
        </View>
        <View className='wishlist-card-info'>
          <View className='wishlist-card-header'>
            <Text className='wishlist-card-name' numberOfLines={1}>{item.name}</Text>
            {item.status && (
              <View className={`wishlist-card-status wishlist-card-status--${item.status}`}>
                <Text className='wishlist-card-status-text'>{STATUS_MAP[item.status] || item.status}</Text>
              </View>
            )}
          </View>
          {subtitle && (
            <Text className='wishlist-card-subtitle' numberOfLines={1}>{subtitle}</Text>
          )}
          <View className='wishlist-card-footer'>
            {item.expectedPrice != null && (
              <Text className='wishlist-card-price'>目标价 ¥{item.expectedPrice}</Text>
            )}
            {priorityConfig && (
              <View className={`wishlist-card-priority ${priorityConfig.className}`}>
                <Text className='wishlist-card-priority-text'>{priorityConfig.label}</Text>
              </View>
            )}
          </View>
          {extra.notes && (
            <Text className='wishlist-card-notes' numberOfLines={1}>{extra.notes}</Text>
          )}
        </View>
        <View className='wishlist-card-more'>
          <Icon name='chevron-right' size={28} color='#BFBFBF' />
        </View>
      </View>
    )
  }

  const renderContent = () => {
    if (loading && list.length === 0) {
      return <View className='wishlist-loading'><Text>加载中...</Text></View>
    }
    if (!loading && list.length === 0) {
      return <EmptyState text='还没有心愿物品，去添加一个吧' icon='star' />
    }
    return (
      <View className='wishlist-list'>
        {list.map(renderCard)}
        {loadingMore && <View className='wishlist-loading-more'><Text>加载更多...</Text></View>}
        {!hasMore && list.length > 0 && <View className='wishlist-no-more'><Text>没有更多了</Text></View>}
      </View>
    )
  }

  return (
    <View className='wishlist'>
      {/* 搜索栏 */}
      <View className='wishlist-search-bar'>
        <View className='wishlist-search-input-wrap'>
          <Icon name='search' size={28} color='#BFBFBF' />
          <Input className='wishlist-search-input' placeholder='搜索心愿物品' value={keyword}
            onInput={(e) => handleSearchInput(String(e.detail.value))} confirmType='search' />
          {keyword && (
            <View className='wishlist-search-clear' onClick={() => handleSearchInput('')}>
              <Icon name='close' size={28} color='#999' />
            </View>
          )}
        </View>
        <View className='wishlist-search-add' onClick={handleAdd}>
          <Icon name='plus' size={40} color='#fff' />
        </View>
      </View>

      {/* 筛选标签 */}
      <View className='wishlist-filter-bar'>
        <ScrollView className='wishlist-filter-tabs' scrollX showScrollbar={false}>
          {STATUS_TABS.map((tab) => (
            <View
              key={tab.value}
              className={`wishlist-filter-tab${activeStatus === tab.value ? ' wishlist-filter-tab--active' : ''}`}
              onClick={() => handleStatusFilter(tab.value)}
            >
              <Text className={`wishlist-filter-tab-text${activeStatus === tab.value ? ' wishlist-filter-tab-text--active' : ''}`}>
                {tab.label}
              </Text>
            </View>
          ))}
        </ScrollView>
      </View>

      {renderContent()}

      <Toast visible={toastVisible} text={toastText} onClose={() => setToastVisible(false)} />
    </View>
  )
}
