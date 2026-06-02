import { useState, useEffect } from 'react'
import {
  View,
  Text,
  Input,
  Textarea,
  Picker,
  ScrollView,
} from '@tarojs/components'
import Taro from '@tarojs/taro'
import { wishlistApi } from '../../services/api'
import ImageUploader from '../../components/ImageUploader'
import Icon from '../../components/Icon'
import { track, TrackEvent } from '../../utils/tracker'
import './index.less'

const CATEGORY_OPTIONS = [
  '吧唧', '立牌', '小卡', '色纸', '挂件', '娃娃', '娃衣', '手办', '卡牌', '其他',
]

const PRIORITY_OPTIONS = [
  { label: '一般想买', value: 'normal' },
  { label: '很想买', value: 'high' },
  { label: '必入', value: 'must' },
]

const STATUS_OPTIONS = [
  { label: '想买', value: 'wish' },
  { label: '已入', value: 'bought' },
  { label: '暂不买', value: 'postponed' },
]

interface FormState {
  image: string
  name: string
  workName: string
  characterName: string
  category: string
  expectedPrice: string
  priority: string
  status: string
  notes: string
}

function getDefaultForm(): FormState {
  return {
    image: '', name: '', workName: '', characterName: '', category: '',
    expectedPrice: '', priority: 'normal', status: 'wish', notes: '',
  }
}

function parseDescription(desc?: string): Partial<FormState> {
  if (!desc) return {}
  try {
    const parsed = JSON.parse(desc)
    if (parsed && typeof parsed === 'object') {
      return {
        workName: parsed.workName || '',
        characterName: parsed.characterName || '',
        category: parsed.category || '',
        notes: parsed.notes || '',
      }
    }
  } catch {
    return { notes: desc }
  }
  return {}
}

function buildDescription(extra: { workName: string; characterName: string; category: string; notes: string }): string {
  const hasWork = extra.workName || extra.characterName || extra.category || extra.notes
  if (!hasWork) return ''
  return JSON.stringify({
    workName: extra.workName || undefined,
    characterName: extra.characterName || undefined,
    category: extra.category || undefined,
    notes: extra.notes || undefined,
  })
}

export default function WishlistAdd() {
  const router = Taro.getCurrentInstance().router
  const editId = router?.params?.id || null
  const isEdit = !!editId

  const [form, setForm] = useState<FormState>(getDefaultForm)
  const [saving, setSaving] = useState(false)
  const [loading, setLoading] = useState(isEdit)

  const updateField = <K extends keyof FormState>(key: K, value: FormState[K]) => {
    setForm((prev) => ({ ...prev, [key]: value }))
  }

  useEffect(() => {
    Taro.setNavigationBarTitle({ title: isEdit ? '编辑心愿' : '新增心愿' })
  }, [isEdit])

  useEffect(() => {
    if (!editId) return
    setLoading(true)
    wishlistApi.getList({ page: 1, pageSize: 100 })
      .then((res) => {
        const item = (Array.isArray(res) ? res : []).find((r: any) => r.id === editId)
        if (!item) {
          Taro.showToast({ title: '心愿不存在', icon: 'none' })
          Taro.navigateBack()
          return
        }
        const extra = parseDescription(item.description)
        setForm({
          image: item.image || '',
          name: item.name || '',
          workName: extra.workName || '',
          characterName: extra.characterName || '',
          category: extra.category || '',
          expectedPrice: item.expectedPrice != null ? String(item.expectedPrice) : '',
          priority: item.priority || 'normal',
          status: item.status || 'wish',
          notes: extra.notes || '',
        })
      })
      .catch((err) => {
        Taro.showToast({ title: err.message || '加载失败', icon: 'none' })
        Taro.navigateBack()
      })
      .finally(() => setLoading(false))
  }, [editId])

  const validate = (): boolean => {
    if (!form.name.trim()) { Taro.showToast({ title: '请输入心愿物品名称', icon: 'none' }); return false }
    if (form.expectedPrice && parseFloat(form.expectedPrice) < 0) {
      Taro.showToast({ title: '目标价不能为负数', icon: 'none' }); return false
    }
    return true
  }

  const buildSubmitData = () => {
    const description = buildDescription({
      workName: form.workName.trim(),
      characterName: form.characterName.trim(),
      category: form.category,
      notes: form.notes.trim(),
    })
    return {
      name: form.name.trim(),
      description: description || undefined,
      image: form.image || undefined,
      expectedPrice: form.expectedPrice ? parseFloat(form.expectedPrice) : undefined,
      priority: form.priority || undefined,
      status: form.status || undefined,
    }
  }

  const handleSave = async () => {
    if (!validate()) return
    setSaving(true)
    try {
      const data = buildSubmitData()
      if (isEdit && editId) {
        await wishlistApi.update(editId, data)
        Taro.showToast({ title: '更新成功', icon: 'success' })
        Taro.navigateBack()
      } else {
        await wishlistApi.create(data)
        Taro.showToast({ title: '新增成功', icon: 'success' })
        track(TrackEvent.WISHLIST_CREATE, { name: data.name })
        Taro.navigateBack()
      }
    } catch (err: any) {
      Taro.showToast({ title: err.message || '保存失败', icon: 'none' })
    } finally { setSaving(false) }
  }

  const handleSaveAndConvert = async () => {
    if (!validate()) return
    setSaving(true)
    try {
      const data = buildSubmitData()
      let wishId = editId
      if (!wishId) {
        const created = await wishlistApi.create(data)
        wishId = created.id
        track(TrackEvent.WISHLIST_CREATE, { name: data.name })
      }
      if (!wishId) { Taro.showToast({ title: '操作失败', icon: 'none' }); return }
      const result = await wishlistApi.convertToCollection(wishId, {
        name: data.name,
        images: data.image ? [data.image] : undefined,
        workName: form.workName.trim() || undefined,
        characterName: form.characterName.trim() || undefined,
      })
      Taro.showToast({ title: '已转为收藏', icon: 'success' })
      if (result && result.id) {
        Taro.navigateTo({ url: `/pages/collection-add/index?id=${result.id}` })
      } else {
        Taro.navigateTo({ url: '/pages/collection-add/index' })
      }
    } catch (err: any) {
      Taro.showToast({ title: err.message || '转换失败', icon: 'none' })
    } finally { setSaving(false) }
  }

  if (loading) {
    return <View className='wishlist-add-loading'><Text>加载中...</Text></View>
  }

  return (
    <View className='wishlist-add'>
      <ScrollView className='wishlist-add-content' scrollY enhanced showScrollbar={false}>
        {/* 图片上传 */}
        <View className='wishlist-add-section'>
          <Text className='wishlist-add-section-title'>图片</Text>
          <ImageUploader images={form.image ? [form.image] : []} onChange={(urls) => updateField('image', urls[0] || '')} maxCount={1} />
        </View>

        {/* 基本信息 */}
        <View className='wishlist-add-section'>
          <Text className='wishlist-add-section-title'>基本信息</Text>

          <View className='wishlist-add-field'>
            <Text className='wishlist-add-label required'>名称</Text>
            <Input className='wishlist-add-input' placeholder='输入心愿物品名称' value={form.name}
              onInput={(e) => updateField('name', String(e.detail.value))} />
          </View>

          <View className='wishlist-add-field'>
            <Text className='wishlist-add-label'>作品</Text>
            <Input className='wishlist-add-input' placeholder='输入作品名' value={form.workName}
              onInput={(e) => updateField('workName', String(e.detail.value))} />
          </View>

          <View className='wishlist-add-field'>
            <Text className='wishlist-add-label'>角色</Text>
            <Input className='wishlist-add-input' placeholder='输入角色名' value={form.characterName}
              onInput={(e) => updateField('characterName', String(e.detail.value))} />
          </View>

          <View className='wishlist-add-field'>
            <Text className='wishlist-add-label'>类型</Text>
            <Picker mode='selector' range={CATEGORY_OPTIONS}
              value={form.category ? CATEGORY_OPTIONS.indexOf(form.category) : -1}
              onChange={(e) => updateField('category', CATEGORY_OPTIONS[parseInt(String(e.detail.value), 10)])}>
              <View className={`wishlist-add-picker${!form.category ? ' wishlist-add-picker--empty' : ''}`}>
                <Text>{form.category || '请选择类型'}</Text>
                <Icon name='chevron-right' size={28} color='#BFBFBF' />
              </View>
            </Picker>
          </View>

          <View className='wishlist-add-field'>
            <Text className='wishlist-add-label'>目标价</Text>
            <View className='wishlist-add-input-suffix'>
              <Input className='wishlist-add-input' placeholder='0.00' type='digit' value={form.expectedPrice}
                onInput={(e) => updateField('expectedPrice', String(e.detail.value))} />
              <Text className='wishlist-add-suffix'>元</Text>
            </View>
          </View>

          <View className='wishlist-add-field'>
            <Text className='wishlist-add-label'>想买程度</Text>
            <Picker mode='selector' range={PRIORITY_OPTIONS.map((o) => o.label)}
              value={PRIORITY_OPTIONS.findIndex((o) => o.value === form.priority)}
              onChange={(e) => { const idx = parseInt(String(e.detail.value), 10); updateField('priority', PRIORITY_OPTIONS[idx].value) }}>
              <View className='wishlist-add-picker'>
                <Text>{PRIORITY_OPTIONS.find((o) => o.value === form.priority)?.label || '请选择'}</Text>
                <Icon name='chevron-right' size={28} color='#BFBFBF' />
              </View>
            </Picker>
          </View>

          <View className='wishlist-add-field'>
            <Text className='wishlist-add-label'>状态</Text>
            <Picker mode='selector' range={STATUS_OPTIONS.map((o) => o.label)}
              value={STATUS_OPTIONS.findIndex((o) => o.value === form.status)}
              onChange={(e) => { const idx = parseInt(String(e.detail.value), 10); updateField('status', STATUS_OPTIONS[idx].value) }}>
              <View className='wishlist-add-picker'>
                <Text>{STATUS_OPTIONS.find((o) => o.value === form.status)?.label || '请选择'}</Text>
                <Icon name='chevron-right' size={28} color='#BFBFBF' />
              </View>
            </Picker>
          </View>

          <View className='wishlist-add-field'>
            <Text className='wishlist-add-label'>备注</Text>
            <Textarea className='wishlist-add-textarea' placeholder='输入备注信息...' value={form.notes}
              onInput={(e) => updateField('notes', String(e.detail.value))} />
          </View>
        </View>

        {/* 底部占位 */}
        <View className='wishlist-add-spacer' />
      </ScrollView>

      {/* 底部按钮 — 固定在页面底部 */}
      <View className='wishlist-add-bottom'>
        <View className={`wishlist-add-bottom-btn wishlist-add-bottom-btn--primary${saving ? ' wishlist-add-bottom-btn--disabled' : ''}`} onClick={handleSave}>
          <Text className='wishlist-add-bottom-btn-text'>{saving ? '保存中...' : '保存'}</Text>
        </View>
        <View className={`wishlist-add-bottom-btn wishlist-add-bottom-btn--secondary${saving ? ' wishlist-add-bottom-btn--disabled' : ''}`} onClick={handleSaveAndConvert}>
          <Text className='wishlist-add-bottom-btn-text'>{saving ? '保存中...' : '保存并转为收藏'}</Text>
        </View>
      </View>
    </View>
  )
}
