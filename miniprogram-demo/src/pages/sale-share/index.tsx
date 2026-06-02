import { useEffect, useState } from 'react'
import { View, Text, Image, ScrollView } from '@tarojs/components'
import Taro from '@tarojs/taro'
import { shareApi, ShareDetail } from '../../services/api'
import Loading from '../../components/Loading'
import EmptyState from '../../components/EmptyState'
import Icon from '../../components/Icon'
import './index.less'

export default function SaleShare() {
  const [loading, setLoading] = useState(true)
  const [share, setShare] = useState<ShareDetail | null>(null)

  const params = Taro.getCurrentInstance().router?.params as
    | { shareId?: string; id?: string }
    | undefined

  useEffect(() => {
    const shareId = params?.shareId || params?.id
    if (shareId) {
      loadShare(shareId)
    } else {
      setLoading(false)
      Taro.showToast({ title: '参数错误', icon: 'none' })
    }
  }, [params?.shareId, params?.id])

  const loadShare = async (shareId: string) => {
    setLoading(true)
    try {
      const data = await shareApi.getShareDetail(shareId)
      setShare(data)
    } catch (err: any) {
      Taro.showToast({ title: err.message || '加载失败', icon: 'none' })
    } finally {
      setLoading(false)
    }
  }

  const handleCreateOwn = () => {
    Taro.navigateTo({ url: '/pages/select-items/index' })
  }

  if (loading) {
    return <Loading text='加载中...' />
  }

  if (!share) {
    return <EmptyState text='分享内容不存在' />
  }

  const totalPrice = (share.items || []).reduce((sum, item) => {
    return sum + (item.saleInfo?.salePrice || 0)
  }, 0)

  return (
    <View className='sale-share'>
      <ScrollView className='sale-share-scroll' scrollY>
        {/* 头部信息 */}
        <View className='sale-share-header'>
          <Text className='sale-share-title'>{share.title || '出物清单'}</Text>
          {share.createdAt && (
            <Text className='sale-share-time'>
              发布于：{share.createdAt}
            </Text>
          )}
        </View>

        {/* 清单图片 */}
        {share.image && (
          <View className='sale-share-image-wrap'>
            <Image
              className='sale-share-image'
              src={share.image}
              mode='widthFix'
            />
          </View>
        )}

        {/* 商品列表 */}
        {(!share.image || true) && (
          <View className='sale-share-items'>
            <Text className='sale-share-items-title'>
              共 {(share.items || []).length} 件商品
              {totalPrice > 0 && (
                <Text className='sale-share-total-price'>
                  {' '}合计 ¥{totalPrice}
                </Text>
              )}
            </Text>
            {(share.items || []).map((item, index) => (
              <View key={item.id || index} className='sale-share-item'>
                <View className='sale-share-item-image-wrap'>
                  {item.coverImage || item.images?.[0] ? (
                    <Image
                      className='sale-share-item-image'
                      src={item.coverImage || item.images![0]}
                      mode='aspectFill'
                    />
                  ) : (
                    <View className='sale-share-item-image-placeholder'>
                      <Icon name='image' size={40} color='#BFBFBF' />
                    </View>
                  )}
                </View>
                <View className='sale-share-item-info'>
                  <Text className='sale-share-item-name' numberOfLines={1}>
                    {item.name}
                  </Text>
                  {item.characterName && (
                    <Text className='sale-share-item-char' numberOfLines={1}>
                      {item.characterName}
                    </Text>
                  )}
                  <View className='sale-share-item-footer'>
                    <Text className='sale-share-item-price'>
                      ¥{item.saleInfo?.salePrice || 0}
                    </Text>
                    {item.saleInfo?.freeShipping && (
                      <Text className='sale-share-item-badge'>包邮</Text>
                    )}
                  </View>
                </View>
              </View>
            ))}
          </View>
        )}

        {/* 免责声明 */}
        <View className='sale-share-disclaimer'>
          <Text className='sale-share-disclaimer-text'>
            免责声明：本清单由用户自行生成，内容真实性由发布者负责。平台仅提供工具支持，不参与任何交易。
          </Text>
        </View>

        {/* 底部按钮 */}
        <View className='sale-share-actions'>
          <View className='sale-share-btn' onClick={handleCreateOwn}>
            <Text className='sale-share-btn-text'>我也要生成出物清单</Text>
          </View>
        </View>
      </ScrollView>
    </View>
  )
}
