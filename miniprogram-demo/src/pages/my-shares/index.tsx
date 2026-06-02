import { useState, useEffect, useCallback } from 'react'
import { View, Text } from '@tarojs/components'
import Taro from '@tarojs/taro'
import { saleListApi, SaleListItem } from '../../services/api'
import EmptyState from '../../components/EmptyState'
import './index.less'

export default function MyShares() {
  const [list, setList] = useState<SaleListItem[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    fetchShares()
  }, [])

  const fetchShares = async () => {
    setLoading(true)
    try {
      const res = await saleListApi.getList({ status: 'shared' })
      setList(Array.isArray(res) ? res : [])
    } catch (err: any) {
      Taro.showToast({ title: err.message || '加载失败', icon: 'none' })
    } finally {
      setLoading(false)
    }
  }

  // ===== 查看分享页 =====
  const handleViewShare = useCallback((item: SaleListItem) => {
    if (item.shareUrl) {
      // 如果有分享链接，跳转到分享页
      Taro.navigateTo({ url: `/pages/sale-share/index?id=${item.id}` })
    } else {
      Taro.showToast({ title: '暂无分享链接', icon: 'none' })
    }
  }, [])

  // ===== 关闭分享 =====
  const handleCloseShare = useCallback(
    (item: SaleListItem) => {
      Taro.showModal({
        title: '确认关闭',
        content: `关闭后「${item.title}」将不再对外可见，确定吗？`,
        success: async (res) => {
          if (res.confirm) {
            try {
              await saleListApi.updateShareStatus(item.id, { shared: false })
              Taro.showToast({ title: '已关闭分享', icon: 'success' })
              // 从列表中移除
              setList((prev) => prev.filter((i) => i.id !== item.id))
            } catch (err: any) {
              Taro.showToast({ title: err.message || '操作失败', icon: 'none' })
            }
          }
        },
      })
    },
    [],
  )

  // ===== 格式化时间 =====
  const formatTime = (timeStr: string): string => {
    if (!timeStr) return ''
    try {
      const d = new Date(timeStr)
      const year = d.getFullYear()
      const month = String(d.getMonth() + 1).padStart(2, '0')
      const day = String(d.getDate()).padStart(2, '0')
      const hour = String(d.getHours()).padStart(2, '0')
      const minute = String(d.getMinutes()).padStart(2, '0')
      return `${year}-${month}-${day} ${hour}:${minute}`
    } catch {
      return timeStr
    }
  }

  if (loading) {
    return (
      <View className='my-shares'>
        <View className='my-shares-loading'>
          <Text>加载中...</Text>
        </View>
      </View>
    )
  }

  if (list.length === 0) {
    return (
      <View className='my-shares'>
        <EmptyState text='还没有分享过的清单' />
      </View>
    )
  }

  return (
    <View className='my-shares'>
      <View className='my-shares-list'>
        {list.map((item) => (
          <View key={item.id} className='my-shares-card'>
            <View className='my-shares-card-top'>
              <Text className='my-shares-card-title' numberOfLines={1}>
                {item.title}
              </Text>
              <View className='my-shares-card-status'>
                <Text className='my-shares-card-status-text'>已分享</Text>
              </View>
            </View>

            <View className='my-shares-card-stats'>
              <View className='my-shares-card-stat'>
                <Text className='my-shares-card-stat-value'>{item.totalCount}</Text>
                <Text className='my-shares-card-stat-label'>商品数</Text>
              </View>
              <View className='my-shares-card-stat'>
                <Text className='my-shares-card-stat-value'>{item.totalPrice}</Text>
                <Text className='my-shares-card-stat-label'>总价(¥)</Text>
              </View>
              <View className='my-shares-card-stat'>
                <Text className='my-shares-card-stat-value'>-</Text>
                <Text className='my-shares-card-stat-label'>PV</Text>
              </View>
            </View>

            <Text className='my-shares-card-time'>
              分享时间：{formatTime(item.createdAt)}
            </Text>

            <View className='my-shares-card-actions'>
              <View
                className='my-shares-card-btn my-shares-card-btn--primary'
                onClick={() => handleViewShare(item)}
              >
                <Text className='my-shares-card-btn-text'>查看分享页</Text>
              </View>
              <View
                className='my-shares-card-btn my-shares-card-btn--secondary'
                onClick={() => handleCloseShare(item)}
              >
                <Text className='my-shares-card-btn-text'>关闭分享</Text>
              </View>
            </View>
          </View>
        ))}
      </View>
    </View>
  )
}
