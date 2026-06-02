import { useState, useCallback, useMemo, useEffect } from 'react'
import { View, Text, Input, Textarea, ScrollView } from '@tarojs/components'
import Taro from '@tarojs/taro'
import { collectionApi, saleListApi, CollectionItem } from '../../services/api'
import Loading from '../../components/Loading'
import './index.less'

interface SaleItemEdit {
  id: string
  name: string
  image: string
  price: string
  quantity: string
  defects: string
  freeShipping: boolean
  negotiable: boolean
  bundleRules: string
}

function formatDate(): string {
  const now = new Date()
  const y = now.getFullYear()
  const m = String(now.getMonth() + 1).padStart(2, '0')
  const d = String(now.getDate()).padStart(2, '0')
  return `${y}${m}${d}`
}

export default function EditSaleInfo() {
  const [loading, setLoading] = useState(false)
  const [title, setTitle] = useState(`我的出物清单${formatDate()}`)
  const [description, setDescription] = useState('')
  const [items, setItems] = useState<SaleItemEdit[]>([])
  const [batchFreeShipping, setBatchFreeShipping] = useState<boolean | null>(null)
  const [batchNegotiable, setBatchNegotiable] = useState<boolean | null>(null)
  const [batchNote, setBatchNote] = useState('')
  const [batchTradePlatform, setBatchTradePlatform] = useState('')
  const [tradePlatform, setTradePlatform] = useState('闲鱼')
  const [canSplit, setCanSplit] = useState(false)
  const [contactInfo, setContactInfo] = useState('')
  const [otherRules, setOtherRules] = useState('')

  const params = Taro.getCurrentInstance().router?.params as
    | { ids?: string; id?: string }
    | undefined

  useEffect(() => {
    loadCollections()
  }, [])

  const loadCollections = async () => {
    setLoading(true)
    try {
      if (params?.id) {
        // 编辑已有清单
        const detail = await saleListApi.getDetail(params.id)
        setTitle(detail.title)
        const editItems: SaleItemEdit[] = (detail.collections || []).map((c) => ({
          id: c.id,
          name: c.name,
          image: c.coverImage || c.images?.[0] || '',
          price: String(c.saleInfo?.salePrice ?? ''),
          quantity: '1',
          defects: c.saleInfo?.defects ?? '',
          freeShipping: c.saleInfo?.freeShipping ?? false,
          negotiable: c.saleInfo?.negotiable ?? false,
          bundleRules: c.saleInfo?.bundleRules ?? '',
        }))
        setItems(editItems)
      } else if (params?.ids) {
        // 从选中收藏创建
        const ids = params.ids.split(',')
        const results = await Promise.all(
          ids.map((id) => collectionApi.getDetail(id)),
        )
        const editItems: SaleItemEdit[] = results.map((c) => ({
          id: c.id,
          name: c.name,
          image: c.coverImage || c.images?.[0] || '',
          price: '',
          quantity: String(c.quantity ?? 1),
          defects: '',
          freeShipping: false,
          negotiable: false,
          bundleRules: '',
        }))
        setItems(editItems)
      }
    } catch (err: any) {
      Taro.showToast({ title: err.message || '加载失败', icon: 'none' })
    } finally {
      setLoading(false)
    }
  }

  const totalPrice = useMemo(() => {
    return items.reduce((sum, item) => {
      const p = parseFloat(item.price) || 0
      const q = parseInt(item.quantity) || 1
      return sum + p * q
    }, 0)
  }, [items])

  const updateItem = useCallback(
    (index: number, field: keyof SaleItemEdit, value: any) => {
      setItems((prev) => {
        const next = [...prev]
        next[index] = { ...next[index], [field]: value }
        return next
      })
    },
    [],
  )

  const applyBatchFreeShipping = useCallback(
    (value: boolean) => {
      setBatchFreeShipping(value)
      setItems((prev) =>
        prev.map((item) => ({ ...item, freeShipping: value })),
      )
    },
    [],
  )

  const applyBatchNegotiable = useCallback(
    (value: boolean) => {
      setBatchNegotiable(value)
      setItems((prev) =>
        prev.map((item) => ({ ...item, negotiable: value })),
      )
    },
    [],
  )

  const applyBatchNote = useCallback(() => {
    if (!batchNote.trim()) return
    setItems((prev) =>
      prev.map((item) => ({
        ...item,
        defects: item.defects
          ? `${item.defects}; ${batchNote.trim()}`
          : batchNote.trim(),
      })),
    )
    setBatchNote('')
    Taro.showToast({ title: '已追加备注', icon: 'success' })
  }, [batchNote])

  const applyBatchTradePlatform = useCallback(() => {
    if (!batchTradePlatform.trim()) return
    setTradePlatform(batchTradePlatform.trim())
    setBatchTradePlatform('')
    Taro.showToast({ title: '交易方式已更新', icon: 'success' })
  }, [batchTradePlatform])

  const handleSaveDraft = useCallback(async () => {
    try {
      const ids = items.map((item) => item.id)
      await saleListApi.create({
        title,
        collectionIds: ids,
      })
      Taro.showToast({ title: '已保存草稿', icon: 'success' })
      setTimeout(() => {
        Taro.navigateBack()
      }, 1500)
    } catch (err: any) {
      Taro.showToast({ title: err.message || '保存失败', icon: 'none' })
    }
  }, [title, items])

  const handleNext = useCallback(() => {
    // 校验：至少所有商品有价格
    const missingPrice = items.some(
      (item) => !item.price || parseFloat(item.price) <= 0,
    )
    if (missingPrice) {
      Taro.showToast({ title: '请为所有商品填写价格', icon: 'none' })
      return
    }

    // 携带数据到模板选择页（用全局数据或storage）
    const saleData = {
      title,
      description,
      items: items.map((item) => ({
        id: item.id,
        name: item.name,
        image: item.image,
        price: parseFloat(item.price),
        quantity: parseInt(item.quantity) || 1,
        totalPrice: (parseFloat(item.price) || 0) * (parseInt(item.quantity) || 1),
        defects: item.defects,
        freeShipping: item.freeShipping,
        negotiable: item.negotiable,
        bundleRules: item.bundleRules,
      })),
      totalPrice,
      tradePlatform,
      canSplit,
      contactInfo,
      otherRules,
    }

    Taro.setStorageSync('saleDraftData', saleData)
    Taro.navigateTo({ url: '/pages/template-select/index' })
  }, [title, description, items, totalPrice, tradePlatform, canSplit, contactInfo, otherRules])

  if (loading) {
    return <Loading text='加载中...' />
  }

  return (
    <View className='edit-sale-info'>
      <ScrollView className='edit-sale-info-scroll' scrollY>
        {/* 清单基本信息 */}
        <View className='edit-sale-info-section'>
          <Text className='edit-sale-info-section-title'>清单信息</Text>
          <View className='edit-sale-info-field'>
            <Text className='edit-sale-info-label'>清单名称</Text>
            <Input
              className='edit-sale-info-input'
              placeholder='输入清单名称'
              value={title}
              onInput={(e) => setTitle(e.detail.value)}
              maxlength={50}
            />
          </View>
          <View className='edit-sale-info-field'>
            <Text className='edit-sale-info-label'>清单说明</Text>
            <Textarea
              className='edit-sale-info-textarea'
              placeholder='选填，输入清单说明'
              value={description}
              onInput={(e) => setDescription(e.detail.value)}
              maxlength={200}
            />
          </View>
        </View>

        {/* 批量设置 */}
        <View className='edit-sale-info-section'>
          <Text className='edit-sale-info-section-title'>批量设置</Text>
          <View className='edit-sale-info-batch-row'>
            <Text className='edit-sale-info-batch-label'>包邮</Text>
            <View className='edit-sale-info-batch-actions'>
              <Text
                className={`edit-sale-info-batch-btn${batchFreeShipping === true ? ' edit-sale-info-batch-btn--active' : ''}`}
                onClick={() => applyBatchFreeShipping(true)}
              >
                全部包邮
              </Text>
              <Text
                className={`edit-sale-info-batch-btn${batchFreeShipping === false ? ' edit-sale-info-batch-btn--active' : ''}`}
                onClick={() => applyBatchFreeShipping(false)}
              >
                全部不包邮
              </Text>
            </View>
          </View>
          <View className='edit-sale-info-batch-row'>
            <Text className='edit-sale-info-batch-label'>小刀</Text>
            <View className='edit-sale-info-batch-actions'>
              <Text
                className={`edit-sale-info-batch-btn${batchNegotiable === true ? ' edit-sale-info-batch-btn--active' : ''}`}
                onClick={() => applyBatchNegotiable(true)}
              >
                全部不刀
              </Text>
              <Text
                className={`edit-sale-info-batch-btn${batchNegotiable === false ? ' edit-sale-info-batch-btn--active' : ''}`}
                onClick={() => applyBatchNegotiable(false)}
              >
                可小刀
              </Text>
            </View>
          </View>
          <View className='edit-sale-info-batch-row'>
            <Text className='edit-sale-info-batch-label'>备注</Text>
            <View className='edit-sale-info-batch-actions'>
              <Input
                className='edit-sale-info-batch-input'
                placeholder='追加备注到所有商品'
                value={batchNote}
                onInput={(e) => setBatchNote(e.detail.value)}
              />
              <Text className='edit-sale-info-batch-confirm' onClick={applyBatchNote}>
                追加
              </Text>
            </View>
          </View>
          <View className='edit-sale-info-batch-row'>
            <Text className='edit-sale-info-batch-label'>交易方式</Text>
            <View className='edit-sale-info-batch-actions'>
              <Input
                className='edit-sale-info-batch-input'
                placeholder='设置默认交易平台'
                value={batchTradePlatform}
                onInput={(e) => setBatchTradePlatform(e.detail.value)}
              />
              <Text className='edit-sale-info-batch-confirm' onClick={applyBatchTradePlatform}>
                设置
              </Text>
            </View>
          </View>
        </View>

        {/* 商品列表 */}
        <View className='edit-sale-info-section'>
          <View className='edit-sale-info-section-header'>
            <Text className='edit-sale-info-section-title'>
              商品列表（{items.length} 件）
            </Text>
            <Text className='edit-sale-info-total'>
              合计：<Text className='edit-sale-info-total-price'>¥{totalPrice}</Text>
            </Text>
          </View>
          {items.map((item, index) => (
            <View key={item.id} className='edit-sale-info-item'>
              <View className='edit-sale-info-item-header'>
                <Text className='edit-sale-info-item-index'>{index + 1}.</Text>
                <Text className='edit-sale-info-item-name' numberOfLines={1}>
                  {item.name}
                </Text>
              </View>
              <View className='edit-sale-info-item-fields'>
                <View className='edit-sale-info-item-row'>
                  <Text className='edit-sale-info-item-label'>出物价</Text>
                  <View className='edit-sale-info-item-input-wrap'>
                    <Text className='edit-sale-info-item-currency'>¥</Text>
                    <Input
                      className='edit-sale-info-item-input'
                      type='digit'
                      placeholder='0.00'
                      value={item.price}
                      onInput={(e) => updateItem(index, 'price', e.detail.value)}
                    />
                  </View>
                </View>
                <View className='edit-sale-info-item-row'>
                  <Text className='edit-sale-info-item-label'>数量</Text>
                  <Input
                    className='edit-sale-info-item-input-sm'
                    type='number'
                    placeholder='1'
                    value={item.quantity}
                    onInput={(e) => updateItem(index, 'quantity', e.detail.value)}
                  />
                </View>
                <View className='edit-sale-info-item-row'>
                  <Text className='edit-sale-info-item-label'>瑕疵说明</Text>
                  <Input
                    className='edit-sale-info-item-input'
                    placeholder='选填'
                    value={item.defects}
                    onInput={(e) => updateItem(index, 'defects', e.detail.value)}
                  />
                </View>
                <View className='edit-sale-info-item-row'>
                  <Text className='edit-sale-info-item-label'>包邮</Text>
                  <Text
                    className={`edit-sale-info-item-tag${item.freeShipping ? ' edit-sale-info-item-tag--active' : ''}`}
                    onClick={() => updateItem(index, 'freeShipping', !item.freeShipping)}
                  >
                    {item.freeShipping ? '包邮' : '不包邮'}
                  </Text>
                  <Text className='edit-sale-info-item-label edit-sale-info-item-label--second'>
                    小刀
                  </Text>
                  <Text
                    className={`edit-sale-info-item-tag${!item.negotiable ? ' edit-sale-info-item-tag--active' : ''}`}
                    onClick={() => updateItem(index, 'negotiable', !item.negotiable)}
                  >
                    {item.negotiable ? '可小刀' : '不刀'}
                  </Text>
                </View>
                <View className='edit-sale-info-item-row'>
                  <Text className='edit-sale-info-item-label'>捆绑规则</Text>
                  <Input
                    className='edit-sale-info-item-input'
                    placeholder='选填，如：满2件包邮'
                    value={item.bundleRules}
                    onInput={(e) => updateItem(index, 'bundleRules', e.detail.value)}
                  />
                </View>
              </View>
            </View>
          ))}
        </View>

        {/* 交易规则 */}
        <View className='edit-sale-info-section'>
          <Text className='edit-sale-info-section-title'>交易规则</Text>
          <View className='edit-sale-info-field'>
            <Text className='edit-sale-info-label'>交易平台</Text>
            <View className='edit-sale-info-tag-group'>
              {['闲鱼', '微信', '微博', '其他'].map((platform) => (
                <Text
                  key={platform}
                  className={`edit-sale-info-tag${tradePlatform === platform ? ' edit-sale-info-tag--active' : ''}`}
                  onClick={() => setTradePlatform(platform)}
                >
                  {platform}
                </Text>
              ))}
            </View>
          </View>
          <View className='edit-sale-info-field'>
            <Text className='edit-sale-info-label'>可拆售</Text>
            <Text
              className={`edit-sale-info-tag${canSplit ? ' edit-sale-info-tag--active' : ''}`}
              onClick={() => setCanSplit(!canSplit)}
            >
              {canSplit ? '可拆售' : '不拆售'}
            </Text>
          </View>
          <View className='edit-sale-info-field'>
            <Text className='edit-sale-info-label'>联系方式</Text>
            <Input
              className='edit-sale-info-input'
              placeholder='选填，如：闲鱼搜xxx'
              value={contactInfo}
              onInput={(e) => setContactInfo(e.detail.value)}
            />
          </View>
          <View className='edit-sale-info-field'>
            <Text className='edit-sale-info-label'>其他规则</Text>
            <Textarea
              className='edit-sale-info-textarea'
              placeholder='选填，其他交易规则说明'
              value={otherRules}
              onInput={(e) => setOtherRules(e.detail.value)}
              maxlength={300}
            />
          </View>
        </View>

        {/* 底部按钮区 */}
        <View className='edit-sale-info-actions'>
          <View className='edit-sale-info-action-btn edit-sale-info-action-btn--secondary' onClick={handleSaveDraft}>
            <Text className='edit-sale-info-action-btn-text'>保存草稿</Text>
          </View>
          <View className='edit-sale-info-action-btn edit-sale-info-action-btn--primary' onClick={handleNext}>
            <Text className='edit-sale-info-action-btn-text'>下一步</Text>
          </View>
        </View>
      </ScrollView>
    </View>
  )
}
