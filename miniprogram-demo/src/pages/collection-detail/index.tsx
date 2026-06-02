import { useState, useEffect } from 'react'
import { View, Text, Image, Swiper, SwiperItem, ScrollView } from '@tarojs/components'
import Taro, { useRouter } from '@tarojs/taro'
import { collectionApi } from '../../services/api'
import StatusTag from '../../components/StatusTag'
import Icon from '../../components/Icon'
import { getImageUrl } from '../../utils/image'
import './index.less'

interface DetailItem {
  id: string; name: string; images: string[]; coverImage: string
  itemType: string; workName: string; characterName: string
  purchasePrice: number; quantity: number; purchaseChannel: string
  purchaseDate: string; status: string; note: string
  isForSale: boolean; salePrice: number; flawNote: string
  shippingRule: string; bargainRule: string; bundleRule: string; createdAt: string
}

const TYPE_TO_LABEL: Record<string, string> = {
  badge: '吧唧', standee: '立牌', card: '小卡', shikishi: '色纸',
  keychain: '挂件', plush: '娃娃', doll_clothes: '娃衣', figure: '手办',
  tcg: '卡牌', other: '其他',
}

function parseImages(raw: any): string[] {
  if (!raw) return []
  if (Array.isArray(raw)) return raw
  if (typeof raw === 'string') { try { return JSON.parse(raw) } catch { return [] } }
  return []
}

export default function CollectionDetail() {
  const { params } = useRouter()
  const id = params?.id

  const [item, setItem] = useState<DetailItem | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    if (!id) { setError('参数错误'); setLoading(false); return }
    setLoading(true); setError('')
    collectionApi.getDetail(id)
      .then((data: any) => {
        if (!data || !data.id) { setError('物品不存在'); return }
        const { images: rawImages, ...rest } = data
        setItem({
          ...rest,
          images: parseImages(rawImages),
          note: rest.note || '', workName: rest.workName || '',
          characterName: rest.characterName || '', purchaseChannel: rest.purchaseChannel || '',
          purchaseDate: rest.purchaseDate || '', flawNote: rest.flawNote || '',
          bundleRule: rest.bundleRule || '',
        })
      })
      .catch((err: any) => { console.error('加载详情失败:', err); setError(err.message || '加载失败') })
      .finally(() => setLoading(false))
  }, [id])

  const handleImagePreview = (index: number) => {
    if (!item?.images?.length) return
    Taro.previewImage({ current: item.images[index], urls: item.images })
  }

  const handleEdit = () => {
    if (!id) return
    Taro.navigateTo({ url: `/pages/collection-add/index?id=${id}` })
  }

  const handleAddToSaleList = () => {
    if (!item) return
    Taro.setStorageSync('pendingSaleIds', [item.id])
    Taro.eventCenter.trigger('switchTab', 2)
    Taro.navigateBack()
  }

  const handleMarkAsSold = () => {
    if (!item) return
    Taro.showModal({
      title: '确认标记',
      content: '确定要将该物品标记为「已出物」吗？',
      success: async (res) => {
        if (res.confirm) {
          try {
            await collectionApi.update(item.id, { status: 'sold' } as any)
            Taro.showToast({ title: '已标记为已出', icon: 'success' })
            setItem((prev) => (prev ? { ...prev, status: 'sold' } : prev))
          } catch (err: any) {
            Taro.showToast({ title: err.message || '操作失败', icon: 'none' })
          }
        }
      },
    })
  }

  const handleMore = () => {
    Taro.showActionSheet({
      itemList: ['分享收藏', '复制一份', '删除'],
    }).then((res) => {
      switch (res.tapIndex) {
        case 0: Taro.setClipboardData({ data: `来看看我的收藏品：${item?.name}` }).then(() => Taro.showToast({ title: '已复制到剪贴板', icon: 'success' })); break
        case 1: handleDuplicate(); break
        case 2: handleDelete(); break
      }
    }).catch(() => {})
  }

  const handleDuplicate = async () => {
    if (!item) return
    try {
      await collectionApi.create({ name: item.name + '(副本)', itemType: item.itemType, status: item.status } as any)
      Taro.showToast({ title: '已复制一份', icon: 'success' })
    } catch (err: any) {
      Taro.showToast({ title: err.message || '复制失败', icon: 'none' })
    }
  }

  const handleDelete = () => {
    if (!item) return
    Taro.showModal({
      title: '确认删除',
      content: `确定要删除「${item.name}」吗？删除后不可恢复。`,
      success: async (res) => {
        if (res.confirm) {
          try {
            await collectionApi.delete(item.id)
            Taro.showToast({ title: '删除成功', icon: 'success' })
            setTimeout(() => Taro.navigateBack(), 800)
          } catch (err: any) {
            Taro.showToast({ title: err.message || '删除失败', icon: 'none' })
          }
        }
      },
    })
  }

  if (loading) {
    return <View className='collection-detail-loading'><Text>加载中...</Text></View>
  }

  if (error || !item) {
    return (
      <View className='collection-detail-loading'>
        <Text>{error || '物品不存在'}</Text>
        <View className='collection-detail-retry' onClick={() => Taro.navigateBack()}>
          <Text>返回</Text>
        </View>
      </View>
    )
  }

  const typeLabel = TYPE_TO_LABEL[item.itemType] || item.itemType || ''
  const images = Array.isArray(item.images) ? item.images : []
  const shippingLabel = item.shippingRule === 'included' ? '包邮' : item.shippingRule === 'conditional' ? '满额包邮' : '不包邮'
  const bargainLabel = item.bargainRule === 'bargain' ? '可小刀' : item.bargainRule === 'bundle_first' ? '打包优先' : '不刀'

  return (
    <View className='collection-detail'>
      <ScrollView className='collection-detail-content' scrollY enhanced showScrollbar={false}>
        {/* 图片轮播 */}
        {images.length > 0 ? (
          <View className='collection-detail-swiper-wrap'>
            <Swiper className='collection-detail-swiper' indicatorColor='rgba(255,255,255,0.4)' indicatorActiveColor='#fff' indicatorDots autoplay={false} circular>
              {images.map((url, index) => (
                <SwiperItem key={index} className='collection-detail-swiper-item'>
                  <Image className='collection-detail-swiper-image' src={getImageUrl(url)} mode='aspectFit' onClick={() => handleImagePreview(index)} />
                </SwiperItem>
              ))}
            </Swiper>
            <Text className='collection-detail-swiper-count'>{images.length}张</Text>
          </View>
        ) : (
          <View className='collection-detail-swiper-placeholder'>
            <Icon name='image' size={64} color='#BFBFBF' />
            <Text className='collection-detail-swiper-placeholder-text'>暂无图片</Text>
          </View>
        )}

        {/* 基础信息 */}
        <View className='collection-detail-card'>
          <View className='collection-detail-card-header'>
            <Text className='collection-detail-name'>{item.name}</Text>
            <StatusTag status={item.status as any} />
          </View>

          <View className='collection-detail-info-list'>
            <InfoRow label='作品' value={item.workName} />
            <InfoRow label='角色' value={item.characterName} />
            <InfoRow label='类型' value={typeLabel} />
            {item.purchasePrice != null && <InfoRow label='入手价' value={`¥${item.purchasePrice}`} cls='price' />}
            <InfoRow label='数量' value={String(item.quantity)} />
            <InfoRow label='入手渠道' value={item.purchaseChannel} />
            <InfoRow label='入手日期' value={item.purchaseDate} />
            <InfoRow label='备注' value={item.note} column />
          </View>
        </View>

        {/* 出物信息 */}
        {(item.isForSale || item.salePrice != null) && (
          <View className='collection-detail-card'>
            <Text className='collection-detail-card-title'>出物信息</Text>
            <View className='collection-detail-info-list'>
              {item.salePrice != null && <InfoRow label='出物价' value={`¥${item.salePrice}`} cls='price' />}
              {item.isForSale && (
                <>
                  <InfoRow label='包邮' value={shippingLabel} />
                  <InfoRow label='小刀' value={bargainLabel} />
                  <InfoRow label='瑕疵说明' value={item.flawNote} />
                  <InfoRow label='捆绑规则' value={item.bundleRule} />
                </>
              )}
            </View>
          </View>
        )}

        <View className='collection-detail-spacer' />
      </ScrollView>

      {/* 底部操作栏 */}
      <View className='collection-detail-actions'>
        <View className='collection-detail-actions-btn' onClick={handleEdit}>
          <Icon name='edit' size={28} color='#666' />
          <Text className='collection-detail-actions-btn-text'>编辑</Text>
        </View>
        <View className='collection-detail-actions-btn collection-detail-actions-btn--primary' onClick={handleAddToSaleList}>
          <Icon name='document' size={28} color='#fff' />
          <Text className='collection-detail-actions-btn-text'>加入出物清单</Text>
        </View>
        {item.status !== 'sold' && (
          <View className='collection-detail-actions-btn collection-detail-actions-btn--success' onClick={handleMarkAsSold}>
            <Icon name='check' size={28} color='#fff' />
            <Text className='collection-detail-actions-btn-text'>标记已出</Text>
          </View>
        )}
        <View className='collection-detail-actions-btn' onClick={handleMore}>
          <Icon name='more' size={28} color='#666' />
        </View>
      </View>
    </View>
  )
}

function InfoRow({ label, value, cls, column }: { label: string; value: string; cls?: string; column?: boolean }) {
  if (!value) return null
  return (
    <View className={`collection-detail-info-row${column ? ' collection-detail-info-row--column' : ''}`}>
      <Text className='collection-detail-info-label'>{label}</Text>
      <Text className={`collection-detail-info-value${cls ? ` collection-detail-info-value--${cls}` : ''}`}>{value}</Text>
    </View>
  )
}
