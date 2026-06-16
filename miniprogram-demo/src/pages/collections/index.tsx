import { useState, useEffect, useRef, useCallback } from 'react'
import {
  View,
  Text,
  ScrollView,
  Input,
  Picker,
} from '@tarojs/components'
import Taro, { useDidShow, usePullDownRefresh, useReachBottom } from '@tarojs/taro'
import {
  collectionApi,
  CollectionItem,
  CollectionListParams,
} from '../../services/api'
import CollectionCard from '../../components/CollectionCard'
import EmptyState from '../../components/EmptyState'
import Icon from '../../components/Icon'
import './index.less'

/** 状态筛选标签 */
const STATUS_TABS = [
  { label: '全部', value: '' },
  { label: '已到货', value: 'arrived' },
  { label: '预售', value: 'preorder' },
  { label: '待补款', value: 'pending_payment' },
  { label: '待发货', value: 'pending_shipment' },
  { label: '待出物', value: 'for_sale' },
  { label: '已出物', value: 'sold' },
] as const

/** 排序选项 */
const SORT_OPTIONS = [
  { label: '最近添加', sortBy: 'createdAt' as const, sortOrder: 'desc' as const },
  { label: '最近更新', sortBy: 'updatedAt' as const, sortOrder: 'desc' as const },
  { label: '价格升序', sortBy: 'purchasePrice' as const, sortOrder: 'asc' as const },
  { label: '价格降序', sortBy: 'purchasePrice' as const, sortOrder: 'desc' as const },
]

/** 收藏品类型选项 */
const CATEGORY_OPTIONS = [
  '吧唧',
  '立牌',
  '小卡',
  '色纸',
  '挂件',
  '娃娃',
  '娃衣',
  '手办',
  '卡牌',
  '其他',
]

/** 中文 → 英文映射 */
const CATEGORY_TO_TYPE: Record<string, string> = {
  '吧唧': 'badge',
  '立牌': 'standee',
  '小卡': 'card',
  '色纸': 'shikishi',
  '挂件': 'keychain',
  '娃娃': 'plush',
  '娃衣': 'doll_clothes',
  '手办': 'figure',
  '卡牌': 'tcg',
  '其他': 'other',
}

/** 批量操作菜单 */
const BATCH_ACTIONS = ['加入出物清单', '修改状态', '删除']

/** 批量状态修改选项 */
const BATCH_STATUS_OPTIONS = [
  { label: '已到货', value: 'arrived' },
  { label: '预售', value: 'preorder' },
  { label: '待补款', value: 'pending_payment' },
  { label: '待发货', value: 'pending_shipment' },
  { label: '待出物', value: 'for_sale' },
  { label: '已出物', value: 'sold' },
  { label: '不出', value: 'not_for_sale' },
]

export default function Collections() {
  // ---- 列表数据 ----
  const [list, setList] = useState<CollectionItem[]>([])
  const [loading, setLoading] = useState(true)
  const [refreshing, setRefreshing] = useState(false)
  const [loadingMore, setLoadingMore] = useState(false)
  const [page, setPage] = useState(1)
  const [hasMore, setHasMore] = useState(true)
  const pageSize = 20

  // ---- 搜索 & 筛选 ----
  const [keyword, setKeyword] = useState('')
  const [activeStatus, setActiveStatus] = useState('')
  const [showAdvancedFilter, setShowAdvancedFilter] = useState(false)
  const [filterCategory, setFilterCategory] = useState('')
  const [filterWorkName, setFilterWorkName] = useState('')
  const [filterCharacterName, setFilterCharacterName] = useState('')
  const [sortBy, setSortBy] = useState<string>('createdAt')
  const [sortOrder, setSortOrder] = useState<'asc' | 'desc'>('desc')

  // ---- 批量模式 ----
  const [batchMode, setBatchMode] = useState(false)
  const [selectedIds, setSelectedIds] = useState<Set<string>>(new Set())

  // ---- refs ----
  const searchTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null)
  const loadedRef = useRef(false)
  const showedRef = useRef(false)

  /** 构建查询参数 */
  const buildParams = useCallback(
    (p: number): CollectionListParams => {
      const params: CollectionListParams = {
        page: p,
        pageSize,
        sortBy,
        sortOrder,
      }
      if (keyword.trim()) params.keyword = keyword.trim()
      if (activeStatus) params.status = activeStatus
      if (filterCategory) params.itemType = CATEGORY_TO_TYPE[filterCategory]
      if (filterWorkName) params.workName = filterWorkName
      if (filterCharacterName) params.characterName = filterCharacterName
      return params
    },
    [keyword, activeStatus, filterCategory, filterWorkName, filterCharacterName, sortBy, sortOrder],
  )

  /** 获取列表数据 */
  const fetchList = useCallback(
    async (p: number, isRefresh = false) => {
      try {
        const params = buildParams(p)
        const res = await collectionApi.getList(params)
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

  /** 初始加载 */
  useEffect(() => {
    if (loadedRef.current) return
    loadedRef.current = true
    setLoading(true)
    fetchList(1).finally(() => setLoading(false))
  }, [fetchList])

  /** 页面显示时重新加载（tab 切换等场景） */
  useDidShow(() => {
    if (!showedRef.current) {
      showedRef.current = true
      return
    }
    setList([])
    setPage(1)
    setHasMore(true)
    fetchList(1, true)
  })

  /** 筛选/排序变化时重新加载 */
  useEffect(() => {
    if (!loadedRef.current) return
    setLoading(true)
    setList([])
    setPage(1)
    setHasMore(true)
    fetchList(1).finally(() => setLoading(false))
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [activeStatus, filterCategory, filterWorkName, filterCharacterName, sortBy, sortOrder])

  // ---- 下拉刷新 ----
  usePullDownRefresh(() => {
    setRefreshing(true)
    fetchList(1, true).finally(() => {
      setRefreshing(false)
      Taro.stopPullDownRefresh()
    })
  })

  // ---- 触底加载更多 ----
  useReachBottom(() => {
    if (loadingMore || !hasMore) return
    setLoadingMore(true)
    fetchList(page + 1).finally(() => setLoadingMore(false))
  })

  // ---- 搜索 ----
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

  // ---- 状态筛选 ----
  const handleStatusFilter = (status: string) => {
    setActiveStatus(status)
  }

  // ---- 排序选择 ----
  const handleSort = () => {
    Taro.showActionSheet({
      itemList: SORT_OPTIONS.map((o) => o.label),
    }).then((res) => {
      const option = SORT_OPTIONS[res.tapIndex]
      if (option) {
        setSortBy(option.sortBy)
        setSortOrder(option.sortOrder)
      }
    }).catch(() => {})
  }

  // ---- 卡片点击 ----
  const handleCardPress = (id: string) => {
    if (batchMode) {
      toggleSelect(id)
      return
    }
    Taro.navigateTo({ url: `/pages/collection-detail/index?id=${id}` })
  }

  const handleCardLongPress = (id: string) => {
    if (batchMode) return
    setBatchMode(true)
    setSelectedIds(new Set([id]))
  }

  // ---- 批量选择 ----
  const toggleSelect = (id: string) => {
    const next = new Set(selectedIds)
    if (next.has(id)) {
      next.delete(id)
    } else {
      next.add(id)
    }
    setSelectedIds(next)
    if (next.size === 0) {
      setBatchMode(false)
    }
  }

  const exitBatchMode = () => {
    setBatchMode(false)
    setSelectedIds(new Set())
  }

  // ---- 批量操作 ----
  const handleBatchAction = () => {
    if (selectedIds.size === 0) {
      Taro.showToast({ title: '请选择收藏品', icon: 'none' })
      return
    }

    Taro.showActionSheet({
      itemList: BATCH_ACTIONS,
    }).then((res) => {
      const action = BATCH_ACTIONS[res.tapIndex]
      switch (action) {
        case '加入出物清单':
          handleBatchAddToSaleList()
          break
        case '修改状态':
          handleBatchChangeStatus()
          break
        case '删除':
          handleBatchDelete()
          break
      }
    }).catch(() => {})
  }

  const handleBatchAddToSaleList = () => {
    const ids = Array.from(selectedIds)
    Taro.navigateTo({
      url: `/pages/sale-list/index?collectionIds=${ids.join(',')}`,
    })
    exitBatchMode()
  }

  const handleBatchChangeStatus = () => {
    const statusList = BATCH_STATUS_OPTIONS.map((o) => o.label)
    Taro.showActionSheet({ itemList: statusList })
      .then(async (res) => {
        const option = BATCH_STATUS_OPTIONS[res.tapIndex]
        if (!option) return
        try {
          await collectionApi.batchUpdateStatus(Array.from(selectedIds), option.value)
          Taro.showToast({ title: '更新成功', icon: 'success' })
          setList([])
          setPage(1)
          setHasMore(true)
          await fetchList(1, true)
          exitBatchMode()
        } catch (err: any) {
          Taro.showToast({ title: err.message || '更新失败', icon: 'none' })
        }
      })
      .catch(() => {})
  }

  const handleBatchDelete = () => {
    Taro.showModal({
      title: '确认删除',
      content: `确定要删除选中的 ${selectedIds.size} 件收藏品吗？`,
      success: async (modalRes) => {
        if (modalRes.confirm) {
          try {
            await collectionApi.batchDelete(Array.from(selectedIds))
            Taro.showToast({ title: '删除成功', icon: 'success' })
            setList([])
            setPage(1)
            setHasMore(true)
            await fetchList(1, true)
            exitBatchMode()
          } catch (err: any) {
            Taro.showToast({ title: err.message || '删除失败', icon: 'none' })
          }
        }
      },
    })
  }

  // ---- 单品操作 ----
  const handleItemMore = (item: CollectionItem) => {
    Taro.showActionSheet({
      itemList: ['编辑', '加入出物清单', '标记为待出物', '复制一份', '删除'],
    }).then((res) => {
      switch (res.tapIndex) {
        case 0:
          Taro.navigateTo({ url: `/pages/collection-add/index?id=${item.id}` })
          break
        case 1:
          handleAddToSaleList(item)
          break
        case 2:
          handleMarkAsForSale(item)
          break
        case 3:
          handleDuplicate(item)
          break
        case 4:
          handleDeleteItem(item)
          break
      }
    }).catch(() => {})
  }

  const handleAddToSaleList = (item: CollectionItem) => {
    Taro.showModal({
      title: '加入出物清单',
      placeholderText: '请输入出物价（可选）',
      content: '',
      editable: true,
      success: async (res) => {
        if (res.confirm) {
          const price = res.content ? parseFloat(res.content) : undefined
          Taro.navigateTo({
            url: `/pages/sale-list/index?collectionIds=${item.id}&price=${price || ''}`,
          })
        }
      },
    })
  }

  const handleMarkAsForSale = async (item: CollectionItem) => {
    try {
      await collectionApi.update(item.id, { status: 'for_sale' })
      Taro.showToast({ title: '已标记为待出物', icon: 'success' })
      setList((prev) =>
        prev.map((i) => (i.id === item.id ? { ...i, status: 'for_sale' } : i)),
      )
    } catch (err: any) {
      Taro.showToast({ title: err.message || '操作失败', icon: 'none' })
    }
  }

  const handleDuplicate = async (item: CollectionItem) => {
    try {
      const { id, createdAt, updatedAt, ...rest } = item
      await collectionApi.create(rest)
      Taro.showToast({ title: '已复制一份', icon: 'success' })
      setList([])
      setPage(1)
      setHasMore(true)
      await fetchList(1, true)
    } catch (err: any) {
      Taro.showToast({ title: err.message || '复制失败', icon: 'none' })
    }
  }

  const handleDeleteItem = (item: CollectionItem) => {
    Taro.showModal({
      title: '确认删除',
      content: `确定要删除「${item.name}」吗？`,
      success: async (res) => {
        if (res.confirm) {
          try {
            await collectionApi.delete(item.id)
            Taro.showToast({ title: '删除成功', icon: 'success' })
            setList((prev) => prev.filter((i) => i.id !== item.id))
          } catch (err: any) {
            Taro.showToast({ title: err.message || '删除失败', icon: 'none' })
          }
        }
      },
    })
  }

  // ---- 导航 ----
  const handleAdd = () => {
    Taro.navigateTo({ url: '/pages/collection-add/index' })
  }

  // ---- 高级筛选 ----
  const clearAdvancedFilter = () => {
    setFilterCategory('')
    setFilterWorkName('')
    setFilterCharacterName('')
    setShowAdvancedFilter(false)
  }

  // ---- 渲染函数 ----
  const renderGrid = () => {
    if (loading && list.length === 0) {
      return (
        <View className='collections-loading'>
          <Text>加载中...</Text>
        </View>
      )
    }

    if (!loading && list.length === 0) {
      return <EmptyState text='还没有收藏品，去添加一个吧' />
    }

    return (
      <View className='collections-grid'>
        {list.map((item) => (
          <View key={item.id} className='collections-grid-item'>
            <CollectionCard
              item={item}
              onPress={() => handleCardPress(item.id)}
              onLongPress={() => handleCardLongPress(item.id)}
              selectable={batchMode}
              selected={selectedIds.has(item.id)}
            />
            {!batchMode && (
              <View
                className='collections-item-more'
                onClick={() => handleItemMore(item)}
                catchMove
              >
                <Icon name='more' size={28} color='#fff' />
              </View>
            )}
          </View>
        ))}
        {loadingMore && (
          <View className='collections-loading-more'>
            <Text>加载更多...</Text>
          </View>
        )}
        {!hasMore && list.length > 0 && (
          <View className='collections-no-more'>
            <Text>没有更多了</Text>
          </View>
        )}
      </View>
    )
  }

  return (
    <View className='collections'>
      {/* 搜索栏 */}
      <View className='collections-search-bar'>
        <View className='collections-search-input-wrap'>
          <Icon name='search' size={28} color='#BFBFBF' />
          <Input
            className='collections-search-input'
            placeholder='搜索收藏品名称、作品、角色'
            value={keyword}
            onInput={(e) => handleSearchInput(String(e.detail.value))}
            confirmType='search'
          />
          {keyword && (
            <View
              className='collections-search-clear'
              onClick={() => handleSearchInput('')}
            >
              <Icon name='close' size={28} color='#999' />
            </View>
          )}
        </View>
        <View className='collections-search-add' onClick={handleAdd}>
          <Icon name='plus' size={40} color='#fff' />
        </View>
      </View>

      {/* 筛选标签 */}
      <View className='collections-filter-bar'>
        <ScrollView
          className='collections-filter-tabs'
          scrollX
          showScrollbar={false}
        >
          {STATUS_TABS.map((tab) => (
            <View
              key={tab.value}
              className={`collections-filter-tab${activeStatus === tab.value ? ' collections-filter-tab--active' : ''}`}
              onClick={() => handleStatusFilter(tab.value)}
            >
              <Text
                className={`collections-filter-tab-text${activeStatus === tab.value ? ' collections-filter-tab-text--active' : ''}`}
              >
                {tab.label}
              </Text>
            </View>
          ))}
        </ScrollView>

        {/* 排序 & 高级筛选 */}
        <View className='collections-filter-actions'>
          <View className='collections-filter-action' onClick={handleSort}>
            <Icon name='sort' size={24} color='#666' />
            <Text className='collections-filter-action-text'>排序</Text>
          </View>
          <View
            className={`collections-filter-action${showAdvancedFilter ? ' collections-filter-action--active' : ''}`}
            onClick={() => setShowAdvancedFilter(!showAdvancedFilter)}
          >
            <Icon name='filter' size={24} color={showAdvancedFilter ? '#000000' : '#666666'} />
            <Text className='collections-filter-action-text'>筛选</Text>
          </View>
        </View>
      </View>

      {/* 高级筛选面板 */}
      {showAdvancedFilter && (
        <View className='collections-advanced-filter'>
          <View className='collections-advanced-filter-row'>
            <Text className='collections-advanced-filter-label'>类型</Text>
            <Picker
              mode='selector'
              range={['全部', ...CATEGORY_OPTIONS]}
              value={filterCategory ? CATEGORY_OPTIONS.indexOf(filterCategory) + 1 : 0}
              onChange={(e) => {
                const idx = parseInt(String(e.detail.value), 10)
                setFilterCategory(idx === 0 ? '' : CATEGORY_OPTIONS[idx - 1])
              }}
            >
              <View className='collections-advanced-filter-value'>
                <Text>{filterCategory || '全部'}</Text>
              </View>
            </Picker>
          </View>
          <View className='collections-advanced-filter-row'>
            <Text className='collections-advanced-filter-label'>作品</Text>
            <Input
              className='collections-advanced-filter-input'
              placeholder='输入作品名'
              value={filterWorkName}
              onInput={(e) => setFilterWorkName(String(e.detail.value))}
            />
          </View>
          <View className='collections-advanced-filter-row'>
            <Text className='collections-advanced-filter-label'>角色</Text>
            <Input
              className='collections-advanced-filter-input'
              placeholder='输入角色名'
              value={filterCharacterName}
              onInput={(e) => setFilterCharacterName(String(e.detail.value))}
            />
          </View>
          <View className='collections-advanced-filter-btns'>
            <View className='collections-advanced-filter-btn' onClick={clearAdvancedFilter}>
              <Text>重置</Text>
            </View>
            <View
              className='collections-advanced-filter-btn collections-advanced-filter-btn--primary'
              onClick={() => setShowAdvancedFilter(false)}
            >
              <Text>确定</Text>
            </View>
          </View>
        </View>
      )}

      {/* 列表内容 */}
      {renderGrid()}

      {/* 批量操作栏 */}
      {batchMode && (
        <View className='collections-batch-bar'>
          <View className='collections-batch-bar-left'>
            <Text className='collections-batch-bar-count'>
              已选 {selectedIds.size} 件
            </Text>
          </View>
          <View className='collections-batch-bar-actions'>
            <View
              className='collections-batch-bar-btn'
              onClick={handleBatchAction}
            >
              <Text className='collections-batch-bar-btn-text'>批量操作</Text>
            </View>
            <View
              className='collections-batch-bar-btn collections-batch-bar-btn--cancel'
              onClick={exitBatchMode}
            >
              <Text className='collections-batch-bar-btn-text'>取消</Text>
            </View>
          </View>
        </View>
      )}
    </View>
  )
}
