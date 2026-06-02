import { View, Text } from '@tarojs/components'
import './index.less'

/** 收藏品状态 */
export type CollectionStatus =
  | 'arrived'
  | 'preorder'
  | 'pending_payment'
  | 'pending_shipment'
  | 'pending_receipt'
  | 'for_sale'
  | 'sold'
  | 'not_for_sale'

/** 出物清单状态 */
export type SaleListStatus = 'draft' | 'generated' | 'shared'

/** 出物商品状态 */
export type SaleItemStatus = 'available' | 'sold' | 'reserved'

interface StatusTagProps {
  status: CollectionStatus | SaleListStatus | SaleItemStatus
  size?: 'small' | 'normal'
}

const COLLECTION_STATUS_MAP: Record<CollectionStatus, { label: string; className: string }> = {
  arrived: { label: '已到货', className: 'arrived' },
  preorder: { label: '预售', className: 'preorder' },
  pending_payment: { label: '待补款', className: 'pending-payment' },
  pending_shipment: { label: '待发货', className: 'pending-shipment' },
  pending_receipt: { label: '待收货', className: 'pending-receipt' },
  for_sale: { label: '待出物', className: 'for-sale' },
  sold: { label: '已出物', className: 'sold' },
  not_for_sale: { label: '不出', className: 'not-for-sale' },
}

const SALE_LIST_STATUS_MAP: Record<SaleListStatus, { label: string; className: string }> = {
  draft: { label: '草稿', className: 'sale-draft' },
  generated: { label: '已生成', className: 'sale-generated' },
  shared: { label: '已分享', className: 'sale-shared' },
}

const SALE_ITEM_STATUS_MAP: Record<SaleItemStatus, { label: string; className: string }> = {
  available: { label: '可出', className: 'for-sale' },
  sold: { label: '已出', className: 'sold' },
  reserved: { label: '暂挂', className: 'pending-payment' },
}

const ALL_MAP = {
  ...COLLECTION_STATUS_MAP,
  ...SALE_LIST_STATUS_MAP,
  ...SALE_ITEM_STATUS_MAP,
}

export default function StatusTag({ status, size = 'normal' }: StatusTagProps) {
  const config = (ALL_MAP as Record<string, { label: string; className: string }>)[status]
  if (!config) return null

  return (
    <View className={`status-tag status-tag--${config.className} status-tag--${size}`}>
      <Text className='status-tag-text'>{config.label}</Text>
    </View>
  )
}
