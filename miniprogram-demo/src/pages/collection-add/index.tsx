import { useState, useEffect } from 'react'
import {
  View,
  Text,
  Input,
  Textarea,
  Picker,
  Switch,
  ScrollView,
} from '@tarojs/components'
import Taro from '@tarojs/taro'
import { collectionApi } from '../../services/api'
import ImageUploader from '../../components/ImageUploader'
import Icon from '../../components/Icon'
import { track, TrackEvent } from '../../utils/tracker'
import './index.less'

/** 收藏品类型选项 */
const CATEGORY_OPTIONS = [
  '吧唧', '立牌', '小卡', '色纸', '挂件', '娃娃', '娃衣', '手办', '卡牌', '其他',
]

const CATEGORY_TO_ITEM_TYPE: Record<string, string> = {
  '吧唧': 'badge', '立牌': 'standee', '小卡': 'card', '色纸': 'shikishi',
  '挂件': 'keychain', '娃娃': 'plush', '娃衣': 'doll_clothes', '手办': 'figure',
  '卡牌': 'tcg', '其他': 'other',
}

const ITEM_TYPE_TO_CATEGORY: Record<string, string> = Object.fromEntries(
  Object.entries(CATEGORY_TO_ITEM_TYPE).map(([k, v]) => [v, k]),
)

const STATUS_OPTIONS = [
  { label: '已到货', value: 'arrived' },
  { label: '预售', value: 'preorder' },
  { label: '待补款', value: 'pending_payment' },
  { label: '待发货', value: 'pending_shipment' },
  { label: '待收货', value: 'pending_receipt' },
  { label: '待出物', value: 'for_sale' },
  { label: '已出物', value: 'sold' },
  { label: '不出', value: 'not_for_sale' },
]

function getTodayString(): string {
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

function loadHistory(key: string): string[] {
  try {
    const data = Taro.getStorageSync(key)
    return Array.isArray(data) ? data : []
  } catch { return [] }
}

function saveHistory(key: string, items: string[]) {
  try { Taro.setStorageSync(key, items.slice(0, 20)) } catch {}
}

interface FormState {
  images: string[]
  name: string
  workName: string
  characterName: string
  category: string
  purchasePrice: string
  quantity: number
  purchaseChannel: string
  purchaseDate: string
  status: string
  notes: string
  forSale: boolean
  salePrice: string
  defects: string
  freeShipping: boolean
  negotiable: boolean
  bundleRules: string
}

function getDefaultForm(): FormState {
  return {
    images: [], name: '', workName: '', characterName: '', category: '',
    purchasePrice: '', quantity: 1, purchaseChannel: '', purchaseDate: getTodayString(),
    status: '', notes: '', forSale: false, salePrice: '', defects: '',
    freeShipping: false, negotiable: false, bundleRules: '',
  }
}

export default function CollectionAdd() {
  const router = Taro.getCurrentInstance().router
  const editId = router?.params?.id || null
  const isEdit = !!editId

  const [form, setForm] = useState<FormState>(getDefaultForm)
  const [saving, setSaving] = useState(false)
  const [loading, setLoading] = useState(isEdit)
  const [showSaleInfo, setShowSaleInfo] = useState(false)
  const [workHistory] = useState<string[]>(() => loadHistory('collection_work_history'))
  const [characterHistory] = useState<string[]>(() => loadHistory('collection_character_history'))
  const [showWorkSuggestions, setShowWorkSuggestions] = useState(false)
  const [showCharacterSuggestions, setShowCharacterSuggestions] = useState(false)

  const updateField = <K extends keyof FormState>(key: K, value: FormState[K]) => {
    setForm((prev) => ({ ...prev, [key]: value }))
  }

  useEffect(() => {
    if (!editId) return
    setLoading(true)
    collectionApi.getDetail(editId)
      .then((item: any) => {
        setForm({
          images: (typeof item.images === 'string' ? JSON.parse(item.images || '[]') : item.images) || [],
          name: item.name || '', workName: item.workName || '', characterName: item.characterName || '',
          category: ITEM_TYPE_TO_CATEGORY[item.itemType] || '',
          purchasePrice: item.purchasePrice != null ? String(item.purchasePrice) : '',
          quantity: item.quantity || 1, purchaseChannel: item.purchaseChannel || '',
          purchaseDate: item.purchaseDate || getTodayString(),
          status: item.status || '', notes: item.note || '',
          forSale: item.isForSale ?? false,
          salePrice: item.salePrice != null ? String(item.salePrice) : '',
          defects: item.flawNote || '', freeShipping: item.shippingRule === 'included',
          negotiable: item.bargainRule === 'bargain', bundleRules: item.bundleRule || '',
        })
      })
      .catch((err) => {
        Taro.showToast({ title: err.message || '加载失败', icon: 'none' })
        Taro.navigateBack()
      })
      .finally(() => setLoading(false))
  }, [editId])

  useEffect(() => {
    Taro.setNavigationBarTitle({ title: isEdit ? '编辑收藏' : '新增收藏' })
  }, [isEdit])

  const validate = (): boolean => {
    if (!form.name.trim()) { Taro.showToast({ title: '请输入收藏品名称', icon: 'none' }); return false }
    if (!form.category) { Taro.showToast({ title: '请选择类型', icon: 'none' }); return false }
    if (!form.status) { Taro.showToast({ title: '请选择当前状态', icon: 'none' }); return false }
    if (form.purchasePrice && parseFloat(form.purchasePrice) < 0) {
      Taro.showToast({ title: '入手价不能为负数', icon: 'none' }); return false
    }
    return true
  }

  const buildSubmitData = () => ({
    name: form.name.trim(), images: form.images,
    itemType: form.category ? CATEGORY_TO_ITEM_TYPE[form.category] : undefined,
    workName: form.workName.trim() || undefined,
    characterName: form.characterName.trim() || undefined,
    purchasePrice: form.purchasePrice ? parseFloat(form.purchasePrice) : undefined,
    purchaseDate: form.purchaseDate || undefined,
    purchaseChannel: form.purchaseChannel.trim() || undefined,
    quantity: form.quantity, status: form.status || undefined,
    note: form.notes.trim() || undefined, isForSale: form.forSale || undefined,
    salePrice: form.salePrice ? parseFloat(form.salePrice) : undefined,
    flawNote: form.defects.trim() || undefined,
    shippingRule: form.freeShipping ? 'included' : 'not_included',
    bargainRule: form.negotiable ? 'bargain' : 'no_bargain',
    bundleRule: form.bundleRules.trim() || undefined,
  })

  const handleSave = async (continueAdd = false) => {
    if (!validate()) return
    if (form.workName.trim()) {
      const next = [form.workName.trim(), ...workHistory.filter((w) => w !== form.workName)]
      saveHistory('collection_work_history', next)
    }
    if (form.characterName.trim()) {
      const next = [form.characterName.trim(), ...characterHistory.filter((c) => c !== form.characterName)]
      saveHistory('collection_character_history', next)
    }
    setSaving(true)
    try {
      const data = buildSubmitData()
      if (isEdit && editId) {
        await collectionApi.update(editId, data)
        Taro.showToast({ title: '更新成功', icon: 'success' })
        Taro.navigateBack()
      } else {
        await collectionApi.create(data as any)
        Taro.showToast({ title: '新增成功', icon: 'success' })
        track(TrackEvent.COLLECTION_CREATE, { name: data.name })
        if (continueAdd) {
          setForm((prev) => ({ ...getDefaultForm(), workName: prev.workName, characterName: prev.characterName, category: prev.category }))
        } else {
          Taro.navigateBack()
        }
      }
    } catch (err: any) {
      Taro.showToast({ title: err.message || '保存失败', icon: 'none' })
    } finally { setSaving(false) }
  }

  if (loading) {
    return <View className='collection-add-loading'><Text>加载中...</Text></View>
  }

  return (
    <View className='collection-add'>
      <ScrollView className='collection-add-content' scrollY enhanced showScrollbar={false}>
        {/* 图片上传 */}
        <View className='collection-add-section'>
          <Text className='collection-add-section-title'>图片</Text>
          <ImageUploader images={form.images} onChange={(urls) => updateField('images', urls)} maxCount={9} />
        </View>

        {/* 基础信息 */}
        <View className='collection-add-section'>
          <Text className='collection-add-section-title'>基础信息</Text>

          <View className='collection-add-field'>
            <Text className='collection-add-label required'>名称</Text>
            <Input className='collection-add-input' placeholder='例如：A柄吧唧、生日立牌' value={form.name} onInput={(e) => updateField('name', String(e.detail.value))} />
          </View>

          <View className='collection-add-field'>
            <Text className='collection-add-label'>作品</Text>
            <View className='collection-add-input-wrap'>
              <Input className='collection-add-input' placeholder='输入作品名' value={form.workName}
                onInput={(e) => { updateField('workName', String(e.detail.value)); setShowWorkSuggestions(true) }}
                onFocus={() => setShowWorkSuggestions(true)}
                onBlur={() => setTimeout(() => setShowWorkSuggestions(false), 200)} />
              {workHistory.length > 0 && showWorkSuggestions && (
                <View className='collection-add-suggestions'>
                  {workHistory.map((w) => (
                    <Text key={w} className='collection-add-suggestion-item' onClick={() => { updateField('workName', w); setShowWorkSuggestions(false) }}>{w}</Text>
                  ))}
                </View>
              )}
            </View>
          </View>

          <View className='collection-add-field'>
            <Text className='collection-add-label'>角色</Text>
            <View className='collection-add-input-wrap'>
              <Input className='collection-add-input' placeholder='输入角色名' value={form.characterName}
                onInput={(e) => { updateField('characterName', String(e.detail.value)); setShowCharacterSuggestions(true) }}
                onFocus={() => setShowCharacterSuggestions(true)}
                onBlur={() => setTimeout(() => setShowCharacterSuggestions(false), 200)} />
              {characterHistory.length > 0 && showCharacterSuggestions && (
                <View className='collection-add-suggestions'>
                  {characterHistory.map((c) => (
                    <Text key={c} className='collection-add-suggestion-item' onClick={() => { updateField('characterName', c); setShowCharacterSuggestions(false) }}>{c}</Text>
                  ))}
                </View>
              )}
            </View>
          </View>

          <View className='collection-add-field'>
            <Text className='collection-add-label required'>类型</Text>
            <Picker mode='selector' range={CATEGORY_OPTIONS} value={form.category ? CATEGORY_OPTIONS.indexOf(form.category) : -1}
              onChange={(e) => updateField('category', CATEGORY_OPTIONS[parseInt(String(e.detail.value), 10)])}>
              <View className={`collection-add-picker${!form.category ? ' collection-add-picker--empty' : ''}`}>
                <Text>{form.category || '请选择类型'}</Text>
                <Icon name='chevron-right' size={28} color='#BFBFBF' />
              </View>
            </Picker>
          </View>

          <View className='collection-add-field'>
            <Text className='collection-add-label'>入手价</Text>
            <View className='collection-add-input-suffix'>
              <Input className='collection-add-input' placeholder='0.00' type='digit' value={form.purchasePrice} onInput={(e) => updateField('purchasePrice', String(e.detail.value))} />
              <Text className='collection-add-suffix'>元</Text>
            </View>
          </View>

          <View className='collection-add-field'>
            <Text className='collection-add-label'>数量</Text>
            <View className='collection-add-stepper'>
              <View className={`collection-add-stepper-btn${form.quantity <= 1 ? ' collection-add-stepper-btn--disabled' : ''}`}
                onClick={() => form.quantity > 1 && updateField('quantity', form.quantity - 1)}>
                <Text>-</Text>
              </View>
              <Text className='collection-add-stepper-value'>{form.quantity}</Text>
              <View className={`collection-add-stepper-btn${form.quantity >= 999 ? ' collection-add-stepper-btn--disabled' : ''}`}
                onClick={() => form.quantity < 999 && updateField('quantity', form.quantity + 1)}>
                <Text>+</Text>
              </View>
            </View>
          </View>

          <View className='collection-add-field'>
            <Text className='collection-add-label'>入手渠道</Text>
            <Input className='collection-add-input' placeholder='例如：淘宝、闲鱼、日本代购' value={form.purchaseChannel}
              onInput={(e) => updateField('purchaseChannel', String(e.detail.value))} />
          </View>

          <View className='collection-add-field'>
            <Text className='collection-add-label'>入手日期</Text>
            <Picker mode='date' value={form.purchaseDate} onChange={(e) => updateField('purchaseDate', String(e.detail.value))}>
              <View className='collection-add-picker'>
                <Text>{form.purchaseDate}</Text>
                <Icon name='chevron-right' size={28} color='#BFBFBF' />
              </View>
            </Picker>
          </View>

          <View className='collection-add-field'>
            <Text className='collection-add-label required'>当前状态</Text>
            <Picker mode='selector' range={STATUS_OPTIONS.map((o) => o.label)}
              value={form.status ? STATUS_OPTIONS.findIndex((o) => o.value === form.status) : -1}
              onChange={(e) => { const idx = parseInt(String(e.detail.value), 10); updateField('status', STATUS_OPTIONS[idx].value) }}>
              <View className={`collection-add-picker${!form.status ? ' collection-add-picker--empty' : ''}`}>
                <Text>{form.status ? STATUS_OPTIONS.find((o) => o.value === form.status)?.label || '请选择状态' : '请选择状态'}</Text>
                <Icon name='chevron-right' size={28} color='#BFBFBF' />
              </View>
            </Picker>
          </View>

          <View className='collection-add-field'>
            <Text className='collection-add-label'>备注</Text>
            <Textarea className='collection-add-textarea' placeholder='输入备注信息...' value={form.notes}
              onInput={(e) => updateField('notes', String(e.detail.value))} />
          </View>
        </View>

        {/* 出物信息 */}
        <View className='collection-add-section'>
          <View className='collection-add-section-header' onClick={() => setShowSaleInfo(!showSaleInfo)}>
            <Text className='collection-add-section-title'>出物信息</Text>
            <View className='collection-add-section-toggle'>
              <Text className='collection-add-section-toggle-text'>{showSaleInfo ? '收起' : '展开'}</Text>
              <Icon name={showSaleInfo ? 'chevron-down' : 'chevron-right'} size={28} color='#4F6EF7' />
            </View>
          </View>

          {showSaleInfo && (
            <>
              <View className='collection-add-field'>
                <Text className='collection-add-label'>准备出物</Text>
                <Switch checked={form.forSale} color='#4F6EF7' onChange={(e) => updateField('forSale', !!e.detail.value)} />
              </View>
              {form.forSale && (
                <>
                  <View className='collection-add-field'>
                    <Text className='collection-add-label'>出物价</Text>
                    <View className='collection-add-input-suffix'>
                      <Input className='collection-add-input' placeholder='0.00' type='digit' value={form.salePrice}
                        onInput={(e) => updateField('salePrice', String(e.detail.value))} />
                      <Text className='collection-add-suffix'>元</Text>
                    </View>
                  </View>
                  <View className='collection-add-field'>
                    <Text className='collection-add-label'>瑕疵说明</Text>
                    <Input className='collection-add-input' placeholder='如有瑕疵请说明' value={form.defects}
                      onInput={(e) => updateField('defects', String(e.detail.value))} />
                  </View>
                  <View className='collection-add-field'>
                    <Text className='collection-add-label'>包邮</Text>
                    <Switch checked={form.freeShipping} color='#4F6EF7' onChange={(e) => updateField('freeShipping', !!e.detail.value)} />
                  </View>
                  <View className='collection-add-field'>
                    <Text className='collection-add-label'>可小刀</Text>
                    <Switch checked={form.negotiable} color='#4F6EF7' onChange={(e) => updateField('negotiable', !!e.detail.value)} />
                  </View>
                  <View className='collection-add-field'>
                    <Text className='collection-add-label'>捆绑规则</Text>
                    <Input className='collection-add-input' placeholder='例如：满100包邮' value={form.bundleRules}
                      onInput={(e) => updateField('bundleRules', String(e.detail.value))} />
                  </View>
                </>
              )}
            </>
          )}
        </View>

        {/* 底部占位，防止内容被固定按钮遮挡 */}
        <View className='collection-add-spacer' />
      </ScrollView>

      {/* 底部按钮 — 固定在页面底部 */}
      <View className='collection-add-bottom'>
        <View className={`collection-add-bottom-btn collection-add-bottom-btn--primary${saving ? ' collection-add-bottom-btn--disabled' : ''}`}
          onClick={() => handleSave(false)}>
          <Text className='collection-add-bottom-btn-text'>{saving ? '保存中...' : '保存'}</Text>
        </View>
        {!isEdit && (
          <View className={`collection-add-bottom-btn collection-add-bottom-btn--secondary${saving ? ' collection-add-bottom-btn--disabled' : ''}`}
            onClick={() => handleSave(true)}>
            <Text className='collection-add-bottom-btn-text'>{saving ? '保存中...' : '保存并继续添加'}</Text>
          </View>
        )}
      </View>
    </View>
  )
}
