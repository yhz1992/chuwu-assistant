import { useState, useEffect, useCallback } from 'react'
import { View, Text, Image, ScrollView } from '@tarojs/components'
import Taro from '@tarojs/taro'
import { templateApi, TemplateItem } from '../../services/api'
import EmptyState from '../../components/EmptyState'
import Icon from '../../components/Icon'
import Toast from '../../components/Toast'
import './index.less'

/** 分类标签 */
const CATEGORY_TABS = [
  { label: '全部', value: '' },
  { label: '简洁', value: 'simple' },
  { label: '可爱', value: 'cute' },
  { label: '小红书风', value: 'xiaohongshu' },
  { label: '表格', value: 'table' },
  { label: '图片墙', value: 'wall' },
] as const

/** 模板分类标签映射 */
const CATEGORY_LABEL_MAP: Record<string, string> = {
  simple: '简洁',
  cute: '可爱',
  xiaohongshu: '小红书风',
  table: '表格',
  wall: '图片墙',
}

export default function TemplateCenter() {
  const [list, setList] = useState<TemplateItem[]>([])
  const [loading, setLoading] = useState(true)
  const [activeCategory, setActiveCategory] = useState('')
  const [toastVisible, setToastVisible] = useState(false)
  const [toastText, setToastText] = useState('')

  const showToast = (text: string) => {
    setToastText(text)
    setToastVisible(true)
  }

  // ===== 获取模板列表 =====
  useEffect(() => {
    fetchTemplates()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  const fetchTemplates = async () => {
    setLoading(true)
    try {
      const res = await templateApi.getList()
      setList(Array.isArray(res) ? res : [])
    } catch (err: any) {
      Taro.showToast({ title: err.message || '加载失败', icon: 'none' })
    } finally {
      setLoading(false)
    }
  }

  // ===== 筛选后的模板 =====
  const filteredList = activeCategory
    ? list.filter((item) => item.category === activeCategory)
    : list

  // ===== 选择模板 =====
  const handleSelect = (template: TemplateItem) => {
    Taro.navigateTo({
      url: `/pages/select-items/index?templateId=${template.id}`,
    })
  }

  // ===== 收藏模板 =====
  const handleFavorite = (template: TemplateItem) => {
    showToast('模板收藏功能即将开放，敬请期待')
  }

  // ===== 渲染 =====
  if (loading) {
    return (
      <View className='template-center'>
        <View className='template-center-loading'>
          <Text>加载中...</Text>
        </View>
      </View>
    )
  }

  return (
    <View className='template-center'>
      {/* 分类标签 */}
      <View className='template-center-tabs'>
        <ScrollView
          className='template-center-tabs-scroll'
          scrollX
          showScrollbar={false}
        >
          {CATEGORY_TABS.map((tab) => (
            <View
              key={tab.value}
              className={`template-center-tab${activeCategory === tab.value ? ' template-center-tab--active' : ''}`}
              onClick={() => setActiveCategory(tab.value)}
            >
              <Text
                className={`template-center-tab-text${activeCategory === tab.value ? ' template-center-tab-text--active' : ''}`}
              >
                {tab.label}
              </Text>
            </View>
          ))}
        </ScrollView>
      </View>

      {/* 模板列表 */}
      {filteredList.length === 0 ? (
        <EmptyState text='暂无可用模板' />
      ) : (
        <View className='template-center-grid'>
          {filteredList.map((template) => {
            const categoryLabel = CATEGORY_LABEL_MAP[template.category] || template.category
            return (
              <View
                key={template.id}
                className='template-center-card'
                onClick={() => handleSelect(template)}
              >
                {/* 预览图占位 */}
                <View className='template-center-card-preview'>
                  {template.preview ? (
                    <Image
                      className='template-center-card-image'
                      src={template.preview}
                      mode='aspectFill'
                    />
                  ) : (
                    <View className='template-center-card-placeholder'>
                      <Icon name='template' size={48} color='#000000' />
                      <Text className='template-center-card-placeholder-name'>
                        {template.name}
                      </Text>
                    </View>
                  )}
                  {template.isRecommended && (
                    <View className='template-center-card-badge'>
                      <Text className='template-center-card-badge-text'>推荐</Text>
                    </View>
                  )}
                </View>

                {/* 信息 */}
                <View className='template-center-card-info'>
                  <Text className='template-center-card-name' numberOfLines={1}>
                    {template.name}
                  </Text>
                  {categoryLabel && (
                    <View className='template-center-card-tag'>
                      <Text className='template-center-card-tag-text'>{categoryLabel}</Text>
                    </View>
                  )}
                </View>

                {/* 操作 */}
                <View className='template-center-card-actions'>
                  <View
                    className='template-center-card-btn template-center-card-btn--primary'
                    onClick={(e) => {
                      e.stopPropagation()
                      handleSelect(template)
                    }}
                  >
                    <Text className='template-center-card-btn-text'>使用此模板</Text>
                  </View>
                  <View
                    className='template-center-card-btn template-center-card-btn--secondary'
                    onClick={(e) => {
                      e.stopPropagation()
                      handleFavorite(template)
                    }}
                  >
                    <Text className='template-center-card-btn-text'>收藏</Text>
                  </View>
                </View>
              </View>
            )
          })}
        </View>
      )}

      <Toast
        visible={toastVisible}
        text={toastText}
        onClose={() => setToastVisible(false)}
      />
    </View>
  )
}
