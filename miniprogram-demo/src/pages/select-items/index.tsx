import { useEffect, useState, useCallback } from 'react'
import { View, Text, Input } from '@tarojs/components'
import Taro from '@tarojs/taro'
import { collectionApi, CollectionItem } from '../../services/api'
import CollectionCard from '../../components/CollectionCard'
import EmptyState from '../../components/EmptyState'
import Loading from '../../components/Loading'
import './index.less'

export default function SelectItems() {
  const [loading, setLoading] = useState(true)
  const [list, setList] = useState<CollectionItem[]>([])
  const [selectedIds, setSelectedIds] = useState<Set<string>>(new Set())
  const [keyword, setKeyword] = useState('')
  const [page, setPage] = useState(1)
  const [hasMore, setHasMore] = useState(true)

  const PAGE_SIZE = 20

  useEffect(() => {
    fetchList(1)
  }, [])

  const fetchList = async (pageNum: number, append = false) => {
    if (!append) setLoading(true)
    try {
      const params: any = {
        page: pageNum,
        pageSize: PAGE_SIZE,
      }
      if (keyword) params.keyword = keyword
      const res = await collectionApi.getList(params)
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

  const handleSearch = useCallback(() => {
    fetchList(1)
  }, [keyword])

  const handleToggleSelect = useCallback((id: string) => {
    setSelectedIds((prev) => {
      const next = new Set(prev)
      if (next.has(id)) {
        next.delete(id)
      } else {
        next.add(id)
      }
      return next
    })
  }, [])

  const handleNext = useCallback(() => {
    if (selectedIds.size === 0) {
      Taro.showToast({ title: '请先选择至少一件收藏', icon: 'none' })
      return
    }
    const ids = Array.from(selectedIds)
    Taro.navigateTo({
      url: `/pages/edit-sale-info/index?ids=${ids.join(',')}`,
    })
  }, [selectedIds])

  const handleLoadMore = useCallback(() => {
    if (!hasMore || loading) return
    fetchList(page + 1, true)
  }, [page, hasMore, loading])

  return (
    <View className='select-items'>
      {/* 搜索框 */}
      <View className='select-items-search'>
        <Input
          className='select-items-search-input'
          placeholder='搜索收藏品...'
          value={keyword}
          onInput={(e) => setKeyword(e.detail.value)}
          onConfirm={handleSearch}
          confirmType='search'
        />
        <Text className='select-items-search-btn' onClick={handleSearch}>
          搜索
        </Text>
      </View>

      {/* 收藏列表 */}
      {loading && list.length === 0 ? (
        <Loading text='加载中...' />
      ) : list.length === 0 ? (
        <EmptyState text='还没有收藏品' />
      ) : (
        <View className='select-items-list'>
          {list.map((item) => (
            <View
              key={item.id}
              className='select-items-card-wrap'
              onClick={() => handleToggleSelect(item.id)}
            >
              <CollectionCard
                item={item}
                selectable
                selected={selectedIds.has(item.id)}
              />
            </View>
          ))}
        </View>
      )}

      {/* 加载更多 */}
      {!loading && hasMore && list.length > 0 && (
        <View className='select-items-loadmore' onClick={handleLoadMore}>
          <Text className='select-items-loadmore-text'>点击加载更多</Text>
        </View>
      )}

      {/* 底部固定栏 */}
      <View className='select-items-footer'>
        <View className='select-items-footer-bar'>
          <Text className='select-items-footer-count'>
            已选 <Text className='select-items-footer-num'>{selectedIds.size}</Text> 件
          </Text>
          <View className='select-items-footer-btn' onClick={handleNext}>
            <Text className='select-items-footer-btn-text'>下一步</Text>
          </View>
        </View>
      </View>
    </View>
  )
}
