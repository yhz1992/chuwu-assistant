import { useState } from 'react'
import {
  View,
  Text,
  Textarea,
  Input,
  Picker,
  ScrollView,
} from '@tarojs/components'
import Taro from '@tarojs/taro'
import { feedbackApi } from '../../services/api'
import ImageUploader from '../../components/ImageUploader'
import { track, TrackEvent } from '../../utils/tracker'
import './index.less'

/** 反馈类型选项 */
const FEEDBACK_TYPES = [
  '功能建议',
  'Bug 反馈',
  '模板建议',
  '其他',
]

export default function Feedback() {
  const [type, setType] = useState('')
  const [content, setContent] = useState('')
  const [contact, setContact] = useState('')
  const [images, setImages] = useState<string[]>([])
  const [submitting, setSubmitting] = useState(false)

  // ===== 提交 =====
  const handleSubmit = async () => {
    // 校验
    if (!type) {
      Taro.showToast({ title: '请选择反馈类型', icon: 'none' })
      return
    }
    if (!content.trim()) {
      Taro.showToast({ title: '请输入反馈内容', icon: 'none' })
      return
    }

    setSubmitting(true)
    try {
      await feedbackApi.submit({
        type,
        content: content.trim(),
        images: images.length > 0 ? images : undefined,
        contact: contact.trim() || undefined,
      })
      Taro.showToast({ title: '感谢反馈，我们会认真查看', icon: 'success' })
      track(TrackEvent.FEEDBACK_SUBMIT, { type })
      Taro.navigateBack()
    } catch (err: any) {
      Taro.showToast({ title: err.message || '提交失败，请重试', icon: 'none' })
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <View className='feedback'>
      <ScrollView className='feedback-content' scrollY enhanced showScrollbar={false}>
        {/* ===== 反馈类型 ===== */}
        <View className='feedback-section'>
          <Text className='feedback-section-title'>反馈类型</Text>
          <Picker
            mode='selector'
            range={FEEDBACK_TYPES}
            value={type ? FEEDBACK_TYPES.indexOf(type) : -1}
            onChange={(e) => setType(FEEDBACK_TYPES[parseInt(String(e.detail.value), 10)])}
          >
            <View className={`feedback-picker${!type ? ' feedback-picker--empty' : ''}`}>
              <Text>{type || '请选择反馈类型'}</Text>
            </View>
          </Picker>
        </View>

        {/* ===== 反馈内容 ===== */}
        <View className='feedback-section'>
          <Text className='feedback-section-title'>反馈内容</Text>
          <Textarea
            className='feedback-textarea'
            placeholder='请详细描述你的问题或建议...'
            value={content}
            onInput={(e) => setContent(String(e.detail.value))}
          />
          <Text className='feedback-count'>{content.length}</Text>
        </View>

        {/* ===== 上传截图 ===== */}
        <View className='feedback-section'>
          <Text className='feedback-section-title'>上传截图（选填）</Text>
          <Text className='feedback-section-desc'>最多上传 3 张截图，帮助我们更快定位问题</Text>
          <ImageUploader
            images={images}
            onChange={setImages}
            maxCount={3}
          />
        </View>

        {/* ===== 联系方式 ===== */}
        <View className='feedback-section'>
          <Text className='feedback-section-title'>联系方式（选填）</Text>
          <Input
            className='feedback-input'
            placeholder='微信 / QQ / 手机号'
            value={contact}
            onInput={(e) => setContact(String(e.detail.value))}
          />
        </View>

        {/* ===== 提交按钮 ===== */}
        <View className='feedback-bottom'>
          <View
            className={`feedback-submit-btn${submitting ? ' feedback-submit-btn--disabled' : ''}`}
            onClick={handleSubmit}
          >
            <Text className='feedback-submit-btn-text'>
              {submitting ? '提交中...' : '提交反馈'}
            </Text>
          </View>
        </View>
      </ScrollView>
    </View>
  )
}
