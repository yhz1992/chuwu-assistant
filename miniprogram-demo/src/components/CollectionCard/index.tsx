import { View, Text, Image } from '@tarojs/components'
import StatusTag from '../StatusTag'
import Icon from '../Icon'
import { CollectionItem } from '../../services/api'
import { getImageUrl } from '../../utils/image'
import './index.less'

interface CollectionCardProps {
  item: CollectionItem
  onPress?: () => void
  onLongPress?: () => void
  selectable?: boolean
  selected?: boolean
}

export default function CollectionCard({
  item,
  onPress,
  onLongPress,
  selectable = false,
  selected = false,
}: CollectionCardProps) {
  const imageUrl = getImageUrl(item.coverImage || (Array.isArray(item.images) ? item.images[0] : ''))
  const subtitle = [item.characterName, item.workName].filter(Boolean).join(' / ')

  return (
    <View
      className={`collection-card${selected ? ' collection-card--selected' : ''}`}
      onClick={onPress}
      onLongPress={onLongPress}
    >
      <View className='collection-card-image-wrap'>
        {imageUrl ? (
          <Image className='collection-card-image' src={imageUrl} mode='aspectFill' />
        ) : (
          <View className='collection-card-image-placeholder'>
            <Icon name='image' size={48} color='#BFBFBF' />
          </View>
        )}
        {selectable && (
          <View
            className={`collection-card-check${selected ? ' collection-card-check--checked' : ''}`}
          >
            {selected && <Icon name='check' size={20} color='#fff' />}
          </View>
        )}
      </View>
      <View className='collection-card-info'>
        <Text className='collection-card-name' numberOfLines={1}>
          {item.name}
        </Text>
        {subtitle && (
          <Text className='collection-card-subtitle' numberOfLines={1}>
            {subtitle}
          </Text>
        )}
        <View className='collection-card-footer'>
          {item.purchasePrice != null && (
            <Text className='collection-card-price'>¥{item.purchasePrice}</Text>
          )}
          <StatusTag status={item.status as any} size='small' />
        </View>
      </View>
    </View>
  )
}
