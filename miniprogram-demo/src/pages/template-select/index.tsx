import { useState, useCallback } from 'react'
import { View, Text, Image } from '@tarojs/components'
import Taro from '@tarojs/taro'
import { saleListApi } from '../../services/api'
import Icon from '../../components/Icon'
import Toast from '../../components/Toast'
import './index.less'

interface TemplateOption {
  id: string
  name: string
  description: string
  scene: string
  isComingSoon?: boolean
}

const TEMPLATES: TemplateOption[] = [
  {
    id: 'simple',
    name: '简洁表格款',
    description: '清晰表格布局，展示商品名称、价格和备注',
    scene: '适合商品较多的清单',
  },
  {
    id: 'card',
    name: '卡片展示款',
    description: '卡片式布局，每个商品独立展示，图文并茂',
    scene: '适合想要精美展示的场景',
    isComingSoon: true,
  },
  {
    id: 'wall',
    name: '图片墙款',
    description: '图片为主，文字为辅，视觉冲击力强',
    scene: '适合图片精美的商品',
    isComingSoon: true,
  },
]

export default function TemplateSelect() {
  const [selectedId, setSelectedId] = useState('simple')
  const [generating, setGenerating] = useState(false)
  const [toastVisible, setToastVisible] = useState(false)
  const [toastText, setToastText] = useState('')

  const showToast = (text: string) => {
    setToastText(text)
    setToastVisible(true)
  }

  const handleSelect = useCallback((template: TemplateOption) => {
    if (template.isComingSoon) {
      showToast('该模板即将开放，敬请期待')
      return
    }
    setSelectedId(template.id)
  }, [])

  const handlePrev = useCallback(() => {
    Taro.navigateBack()
  }, [])

  const handleGenerate = useCallback(async () => {
    // 获取之前保存的出物草稿数据中的清单ID
    const draftData = Taro.getStorageSync('saleDraftData')
    if (!draftData) {
      showToast('请先编辑出物信息')
      return
    }

    setGenerating(true)
    try {
      // 先创建清单获取ID
      const ids = draftData.items.map((item: any) => item.id)
      const created = await saleListApi.create({
        title: draftData.title,
        collectionIds: ids,
      })

      // 调用生成接口
      const result = await saleListApi.generate(created.id, {
        templateId: selectedId,
        title: draftData.title,
        showPrice: true,
        showWatermark: true,
      })

      // 跳转到生成结果页
      Taro.navigateTo({
        url: `/pages/generate-result/index?id=${created.id}`,
      })
    } catch (err: any) {
      Taro.showToast({ title: err.message || '生成失败', icon: 'none' })
    } finally {
      setGenerating(false)
    }
  }, [selectedId])

  return (
    <View className='template-select'>
      <Text className='template-select-desc'>选择分享模板，生成精美的出物清单图片</Text>

      {/* 模板卡片列表 */}
      <View className='template-select-list'>
        {TEMPLATES.map((template) => {
          const isSelected = selectedId === template.id && !template.isComingSoon
          return (
            <View
              key={template.id}
              className={`template-select-card${isSelected ? ' template-select-card--selected' : ''}${template.isComingSoon ? ' template-select-card--disabled' : ''}`}
              onClick={() => handleSelect(template)}
            >
              {/* 预览占位 */}
              <View className='template-select-preview'>
                <View className='template-select-preview-placeholder'>
                  <Icon name={template.id === 'simple' ? 'document' : template.id === 'card' ? 'grid' : 'image'} size={48} color='#4F6EF7' />
                  <Text className='template-select-preview-name'>{template.name}</Text>
                </View>
                {isSelected && (
                  <View className='template-select-selected-badge'>
                    <Icon name='check' size={22} color='#fff' />
                  </View>
                )}
                {template.isComingSoon && (
                  <View className='template-select-coming-soon'>
                    <Text className='template-select-coming-soon-text'>即将开放</Text>
                  </View>
                )}
              </View>
              <View className='template-select-card-info'>
                <Text className='template-select-card-name'>{template.name}</Text>
                <Text className='template-select-card-desc'>{template.description}</Text>
                <Text className='template-select-card-scene'>适用：{template.scene}</Text>
              </View>
            </View>
          )
        })}
      </View>

      {/* 底部按钮 */}
      <View className='template-select-actions'>
        <View className='template-select-btn template-select-btn--secondary' onClick={handlePrev}>
          <Text className='template-select-btn-text'>上一步</Text>
        </View>
        <View
          className={`template-select-btn template-select-btn--primary${generating ? ' template-select-btn--loading' : ''}`}
          onClick={handleGenerate}
        >
          <Text className='template-select-btn-text'>
            {generating ? '生成中...' : '生成长图'}
          </Text>
        </View>
      </View>

      <Toast visible={toastVisible} text={toastText} onClose={() => setToastVisible(false)} />
    </View>
  )
}
