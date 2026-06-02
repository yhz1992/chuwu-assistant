import { useEffect, useState, useCallback } from 'react'
import { View, Text, Image } from '@tarojs/components'
import Taro from '@tarojs/taro'
import { saleListApi, SaleListItem } from '../../services/api'
import StatusTag from '../../components/StatusTag'
import EmptyState from '../../components/EmptyState'
import Loading from '../../components/Loading'
import Icon from '../../components/Icon'
import './index.less'

export default function SaleList() {
  const [loading, setLoading] = useState(true)
  const [list, setList] = useState<SaleListItem[]>([])
  const [page, setPage] = useState(1)
  const [hasMore, setHasMore] = useState(true)
  const [showActionSheet, setShowActionSheet] = useState(false)
  const [selectedItem, setSelectedItem] = useState<SaleListItem | null>(null)

  const PAGE_SIZE = 10

  useEffect(() => {
    const pendingIds = Taro.getStorageSync('pendingSaleIds')
    if (pendingIds && Array.isArray(pendingIds) && pendingIds.length > 0) {
      Taro.removeStorageSync('pendingSaleIds')
      Taro.navigateTo({ url: `/pages/select-items/index?ids=${pendingIds.join(',')}` })
      return
    }
    fetchList(1, true)
  }, [])

  const fetchList = async (pageNum: number, showLoading = false) => {
    if (showLoading) setLoading(true)
    try {
      const res = await saleListApi.getList({ page: pageNum, pageSize: PAGE_SIZE })
      const records = Array.isArray(res) ? res : []
      if (pageNum === 1) {
        setList(records)
      } else {
        setList((prev) => [...prev, ...records])
      }
      setHasMore(records.length === PAGE_SIZE)
      setPage(pageNum)
    } catch (err: any) {
      Taro.showToast({ title: err.message || '加载失败', icon: 'none' })
    } finally {
      setLoading(false)
    }
  }

  const handleLoadMore = useCallback(() => {
    if (!hasMore || loading) return
    fetchList(page + 1)
  }, [page, hasMore, loading])

  const handleNewSaleList = useCallback(() => {
    Taro.navigateTo({ url: '/pages/select-items/index' })
  }, [])

  const handleFromForSale = useCallback(() => {
    Taro.navigateTo({ url: '/pages/select-items/index?from=sale' })
  }, [])

  const handleItemClick = useCallback((item: SaleListItem) => {
    Taro.navigateTo({ url: `/pages/generate-result/index?id=${item.id}` })
  }, [])

  const handleMoreClick = useCallback((e: any, item: SaleListItem) => {
    e.stopPropagation()
    setSelectedItem(item)
    setShowActionSheet(true)
  }, [])

  const handleEdit = useCallback(() => {
    if (!selectedItem) return
    setShowActionSheet(false)
    Taro.navigateTo({ url: `/pages/edit-sale-info/index?id=${selectedItem.id}` })
  }, [selectedItem])

  const handleDuplicate = useCallback(async () => {
    if (!selectedItem) return
    setShowActionSheet(false)
    try {
      await saleListApi.duplicate(selectedItem.id)
      Taro.showToast({ title: '复制成功', icon: 'success' })
      fetchList(1, true)
    } catch (err: any) {
      Taro.showToast({ title: err.message || '复制失败', icon: 'none' })
    }
  }, [selectedItem])

  const handleDelete = useCallback(() => {
    if (!selectedItem) return
    setShowActionSheet(false)
    Taro.showModal({
      title: '确认删除',
      content: `确定要删除"${selectedItem.title}"吗？此操作不可恢复。`,
      success: async (res) => {
        if (res.confirm) {
          try {
            await saleListApi.delete(selectedItem.id)
            Taro.showToast({ title: '删除成功', icon: 'success' })
            fetchList(1, true)
          } catch (err: any) {
            Taro.showToast({ title: err.message || '删除失败', icon: 'none' })
          }
        }
      },
    })
  }, [selectedItem])

  const handleRegenerate = useCallback(() => {
    if (!selectedItem) return
    setShowActionSheet(false)
    Taro.navigateTo({ url: `/pages/template-select/index?id=${selectedItem.id}` })
  }, [selectedItem])

  if (loading && list.length === 0) {
    return <Loading text='加载中...' />
  }

  return (
    <View className='sale-list'>
      {/* 顶部操作区 */}
      <View className='sale-list-actions'>
        <View className='sale-list-btn sale-list-btn--primary' onClick={handleNewSaleList}>
          <Icon name='plus' size={36} color='#fff' />
          <Text className='sale-list-btn-text'>新建出物清单</Text>
        </View>
        <View className='sale-list-btn sale-list-btn--secondary' onClick={handleFromForSale}>
          <Icon name='tag' size={36} color='#4F6EF7' />
          <Text className='sale-list-btn-text'>从待出物生成</Text>
        </View>
      </View>

      {/* 清单列表 */}
      {list.length === 0 ? (
        <View className='sale-list-empty-wrap'>
          <EmptyState text='还没有出物清单' />
        </View>
      ) : (
        <View className='sale-list-items'>
          {list.map((item) => (
            <View
              key={item.id}
              className='sale-list-card'
              onClick={() => handleItemClick(item)}
            >
              <View className='sale-list-card-body'>
                {item.coverImage ? (
                  <Image className='sale-list-card-cover' src={item.coverImage} mode='aspectFill' />
                ) : (
                  <View className='sale-list-card-cover-placeholder'>
                    <Icon name='document' size={40} color='#BFBFBF' />
                  </View>
                )}
                <View className='sale-list-card-info'>
                  <View className='sale-list-card-title-row'>
                    <Text className='sale-list-card-title' numberOfLines={1}>{item.title}</Text>
                    <StatusTag status={item.status as any} size='small' />
                  </View>
                  <Text className='sale-list-card-detail'>共 {item.totalCount} 件 · ¥{item.totalPrice}</Text>
                  <Text className='sale-list-card-time'>{item.createdAt}</Text>
                </View>
                <View className='sale-list-card-more' onClick={(e) => handleMoreClick(e, item)}>
                  <Icon name='more' size={28} color='#999' />
                </View>
              </View>
            </View>
          ))}
        </View>
      )}

      {/* 加载更多 */}
      {!loading && hasMore && list.length > 0 && (
        <View className='sale-list-loadmore' onClick={handleLoadMore}>
          <Text className='sale-list-loadmore-text'>点击加载更多</Text>
        </View>
      )}

      {/* 更多菜单 */}
      {showActionSheet && (
        <View className='sale-list-overlay' onClick={() => setShowActionSheet(false)}>
          <View className='sale-list-action-sheet' onClick={(e) => e.stopPropagation()}>
            <View className='sale-list-action-sheet-title'>
              <Text className='sale-list-action-sheet-title-text'>{selectedItem?.title}</Text>
            </View>
            <View className='sale-list-action-item' onClick={handleEdit}>
              <Icon name='edit' size={32} color='#333' />
              <Text className='sale-list-action-item-text'>编辑</Text>
            </View>
            <View className='sale-list-action-item' onClick={handleDuplicate}>
              <Icon name='copy' size={32} color='#333' />
              <Text className='sale-list-action-item-text'>复制清单</Text>
            </View>
            <View className='sale-list-action-item' onClick={handleRegenerate}>
              <Icon name='template' size={32} color='#333' />
              <Text className='sale-list-action-item-text'>重新生成</Text>
            </View>
            <View className='sale-list-action-item sale-list-action-item--danger' onClick={handleDelete}>
              <Icon name='delete' size={32} color='#FF3B30' />
              <Text className='sale-list-action-item-text'>删除</Text>
            </View>
            <View className='sale-list-action-item sale-list-action-item--cancel' onClick={() => setShowActionSheet(false)}>
              <Text className='sale-list-action-item-text'>取消</Text>
            </View>
          </View>
        </View>
      )}
    </View>
  )
}
