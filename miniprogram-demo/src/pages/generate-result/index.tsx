import { useEffect, useState, useCallback } from 'react'
import { View, Text, Image, ScrollView } from '@tarojs/components'
import Taro from '@tarojs/taro'
import { saleListApi, SaleListItem } from '../../services/api'
import Loading from '../../components/Loading'
import Icon from '../../components/Icon'
import Toast from '../../components/Toast'
import { track, TrackEvent } from '../../utils/tracker'
import { isH5, downloadFileH5, copyToClipboardH5 } from '../../utils/platform'
import './index.less'

interface CopyTexts {
  xianyu: string
  xiaohongshu: string
  weibo: string
  wechat: string
}

const TEXT_TABS = [
  { key: 'xianyu', label: '闲鱼文案' },
  { key: 'xiaohongshu', label: '小红书文案' },
  { key: 'weibo', label: '微博文案' },
  { key: 'wechat', label: '微信群文案' },
]

export default function GenerateResult() {
  const [loading, setLoading] = useState(true)
  const [saleDetail, setSaleDetail] = useState<SaleListItem | null>(null)
  const [imageUrl, setImageUrl] = useState('')
  const [currentTab, setCurrentTab] = useState('xianyu')
  const [savingImage, setSavingImage] = useState(false)
  const [copying, setCopying] = useState(false)
  const [toastVisible, setToastVisible] = useState(false)
  const [toastText, setToastText] = useState('')

  const [texts, setTexts] = useState<CopyTexts>({
    xianyu: '',
    xiaohongshu: '',
    weibo: '',
    wechat: '',
  })

  const params = Taro.getCurrentInstance().router?.params as { id?: string } | undefined

  useEffect(() => {
    if (params?.id) {
      loadData(params.id)
    } else {
      setLoading(false)
      Taro.showToast({ title: '参数错误', icon: 'none' })
    }
  }, [params?.id])

  const loadData = async (id: string) => {
    setLoading(true)
    try {
      const detail = await saleListApi.getDetail(id)
      setSaleDetail(detail)

      // 如果已有分享图，使用它
      if (detail.shareImage) {
        setImageUrl(detail.shareImage)
      }

      // 尝试生成，如果还没有图片
      if (!detail.shareImage) {
        try {
          const result = await saleListApi.generate(id, {
            templateId: detail.templateId || 'simple',
            title: detail.title,
            showPrice: true,
            showWatermark: true,
          })
          if (result.imageUrl) {
            setImageUrl(result.imageUrl)
          }
          track(TrackEvent.SALE_LIST_GENERATE, { saleListId: id })
          // 构造文案
          setTexts(buildTexts(detail))
        } catch {
          // 生成失败时，用已有数据构造文案
          setTexts(buildTexts(detail))
        }
      } else {
        setTexts(buildTexts(detail))
      }
    } catch (err: any) {
      Taro.showToast({ title: err.message || '加载失败', icon: 'none' })
    } finally {
      setLoading(false)
    }
  }

  const buildTexts = (detail: SaleListItem): CopyTexts => {
    if (!detail) {
      return { xianyu: '', xiaohongshu: '', weibo: '', wechat: '' }
    }

    const itemsText = (detail.items || [])
      .map((c, i) => `${i + 1}. ${c.name}${c.price ? ` ¥${c.price}` : ''}`)
      .join('\n')

    const baseText = `【${detail.title}】\n共 ${detail.totalCount} 件，合计 ¥${detail.totalPrice}\n\n${itemsText}\n\n更多详情请点击查看~`

    return {
      xianyu: baseText + '\n#闲鱼 #出物',
      xiaohongshu: `${detail.title}\n\n${itemsText}\n\n总计：¥${detail.totalPrice}\n\n${detail.shareUrl || ''}\n\n#出物 #好物分享`,
      weibo: `【${detail.title}】\n共${detail.totalCount}件，总计¥${detail.totalPrice}\n\n${itemsText}\n\n${detail.shareUrl || ''}`,
      wechat: `${detail.title}\n共${detail.totalCount}件 | 合计¥${detail.totalPrice}\n\n${itemsText}\n\n${detail.shareUrl || ''}`,
    }
  }

  const showToast = (text: string) => {
    setToastText(text)
    setToastVisible(true)
  }

  const handleSaveImage = useCallback(async () => {
    if (!imageUrl) {
      showToast('暂无可用图片')
      return
    }

    setSavingImage(true)
    try {
      if (isH5) {
        // H5 浏览器：直接下载图片
        downloadFileH5(imageUrl, `${saleDetail?.title || '出物清单'}.png`)
        track(TrackEvent.IMAGE_SAVE, { saleListId: saleDetail?.id })
        showToast('图片已开始下载')
      } else {
        // 微信环境：保存到相册
        try {
          await Taro.authorize({ scope: 'scope.writePhotosAlbum' })
        } catch {
          await Taro.openSetting()
        }
        const downloadRes = await Taro.downloadFile({ url: imageUrl })
        await Taro.saveImageToPhotosAlbum({
          filePath: downloadRes.tempFilePath,
        })
        track(TrackEvent.IMAGE_SAVE, { saleListId: saleDetail?.id })
        showToast('已保存到相册')
      }
    } catch (err: any) {
      if (err.errMsg?.includes('cancel') || err.errMsg?.includes('deny')) {
        showToast('需要相册权限才能保存')
      } else {
        showToast(err.message || '保存失败')
      }
    } finally {
      setSavingImage(false)
    }
  }, [imageUrl, saleDetail?.title, saleDetail?.id])

  const handleShare = useCallback(() => {
    track(TrackEvent.SHARE_SALE_LIST, { saleListId: saleDetail?.id })
    if (isH5) {
      // H5: 使用 Web Share API
      if (navigator.share) {
        navigator.share({
          title: saleDetail?.title || '出物清单',
          text: `来看看我的出物清单：${saleDetail?.title}`,
          url: window.location.href,
        }).catch(() => {})
      } else {
        // 复制链接兜底
        copyToClipboardH5(window.location.href).then(() => {
          showToast('链接已复制，可分享给好友')
        })
      }
    } else {
      Taro.showShareMenu({
        withShareTicket: true,
        showShareItems: ['shareAppMessage', 'shareTimeline'],
      })
    }
  }, [saleDetail?.id, saleDetail?.title])

  const handleCopyText = useCallback(
    async (text: string) => {
      if (!text) {
        showToast('暂无可用文案')
        return
      }
      setCopying(true)
      try {
        if (isH5) {
          // H5: 使用剪贴板 API
          await copyToClipboardH5(text)
        } else {
          Taro.setClipboardData({
            data: text,
            fail: () => {
              throw new Error('复制失败')
            },
          })
        }
        track(TrackEvent.COPY_TEXT, { platform: currentTab })
        showToast('文案已复制')
      } catch {
        showToast('复制失败')
      } finally {
        setCopying(false)
      }
    },
    [currentTab],
  )

  const handleEdit = useCallback(() => {
    if (!saleDetail) return
    Taro.navigateTo({
      url: `/pages/edit-sale-info/index?id=${saleDetail.id}`,
    })
  }, [saleDetail])

  const handleChangeTemplate = useCallback(() => {
    if (!saleDetail) return
    Taro.navigateTo({
      url: `/pages/template-select/index?id=${saleDetail.id}`,
    })
  }, [saleDetail])

  if (loading) {
    return <Loading text='生成中...' />
  }

  const currentText = texts[currentTab as keyof CopyTexts]

  return (
    <View className='generate-result'>
      <ScrollView scrollY className='generate-result-scroll'>
        {/* 长图预览 */}
        <View className='generate-result-preview'>
          <Text className='generate-result-preview-title'>长图预览</Text>
          {imageUrl ? (
            <Image
              className='generate-result-preview-image'
              src={imageUrl}
              mode='widthFix'
            />
          ) : (
            <View className='generate-result-preview-placeholder'>
              <Text className='generate-result-preview-loading-text'>长图生成中...</Text>
            </View>
          )}
        </View>

        {/* 操作按钮组 */}
        <View className='generate-result-actions'>
          <View
            className={`generate-result-action-btn${savingImage ? ' generate-result-action-btn--disabled' : ''}`}
            onClick={handleSaveImage}
          >
            <Icon name='download' size={36} color='#000000' />
            <Text className='generate-result-action-label'>保存图片</Text>
          </View>
          <View className='generate-result-action-btn' onClick={handleShare}>
            <Icon name='share' size={36} color='#000000' />
            <Text className='generate-result-action-label'>分享给好友</Text>
          </View>
          <View
            className={`generate-result-action-btn${copying ? ' generate-result-action-btn--disabled' : ''}`}
            onClick={() => handleCopyText(currentText)}
          >
            <Icon name='copy' size={36} color='#000000' />
            <Text className='generate-result-action-label'>复制文案</Text>
          </View>
        </View>

        {/* 文案 Tab 区 */}
        <View className='generate-result-texts'>
          <View className='generate-result-tabs'>
            {TEXT_TABS.map((tab) => (
              <View
                key={tab.key}
                className={`generate-result-tab${currentTab === tab.key ? ' generate-result-tab--active' : ''}`}
                onClick={() => setCurrentTab(tab.key)}
              >
                <Text className='generate-result-tab-text'>{tab.label}</Text>
              </View>
            ))}
          </View>
          <View className='generate-result-text-content'>
            <Text className='generate-result-text'>{currentText || '暂无文案'}</Text>
            <View
              className='generate-result-copy-btn'
              onClick={() => handleCopyText(currentText)}
            >
              <Text className='generate-result-copy-btn-text'>复制</Text>
            </View>
          </View>
        </View>

        {/* 底部操作 */}
        <View className='generate-result-bottom'>
          <View className='generate-result-bottom-btn generate-result-bottom-btn--secondary' onClick={handleEdit}>
            <Text className='generate-result-bottom-btn-text'>重新编辑</Text>
          </View>
          <View className='generate-result-bottom-btn generate-result-bottom-btn--primary' onClick={handleChangeTemplate}>
            <Text className='generate-result-bottom-btn-text'>换模板</Text>
          </View>
        </View>
      </ScrollView>

      <Toast
        visible={toastVisible}
        text={toastText}
        onClose={() => setToastVisible(false)}
      />
    </View>
  )
}
