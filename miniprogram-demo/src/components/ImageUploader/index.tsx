import { useState } from 'react'
import { View, Text, Image } from '@tarojs/components'
import Taro from '@tarojs/taro'
import { uploadApi } from '../../services/api'
import Icon from '../Icon'
import { isH5, chooseImageH5 } from '../../utils/platform'
import './index.less'

interface ImageUploaderProps {
  images: string[]
  onChange: (urls: string[]) => void
  maxCount?: number
}

export default function ImageUploader({
  images,
  onChange,
  maxCount = 9,
}: ImageUploaderProps) {
  const [uploading, setUploading] = useState(false)

  const remaining = maxCount - images.length

  const handleUpload = async () => {
    if (remaining <= 0 || uploading) return

    setUploading(true)
    Taro.showLoading({ title: '上传中...', mask: true })

    try {
      if (isH5) {
        // H5 浏览器：使用 FileReader 读取本地图片，转 base64 作为 URL
        const base64Urls = await chooseImageH5(remaining)
        if (base64Urls.length === 0) return
        onChange([...images, ...base64Urls])
      } else {
        // 微信环境：使用原生选择 + 上传到服务器
        const res = await Taro.chooseMedia({
          count: remaining,
          mediaType: ['image'],
          sourceType: ['album', 'camera'],
        })

        const tempFiles = res.tempFiles || []
        if (tempFiles.length === 0) return

        const results = await Promise.all(
          tempFiles.map((file) =>
            uploadApi.uploadImage(file.tempFilePath, 'collection'),
          ),
        )

        const newUrls = results.map((r) => r.url)
        onChange([...images, ...newUrls])
      }
    } catch (err: any) {
      if (err.errMsg?.includes('cancel') || err.errMsg?.includes('fail cancel') || (isH5 && err.message === 'cancel')) {
        return
      }
      Taro.showToast({ title: err.message || '上传失败，请重试', icon: 'none' })
    } finally {
      setUploading(false)
      Taro.hideLoading()
    }
  }

  const handleDelete = (index: number) => {
    const newImages = images.filter((_, i) => i !== index)
    onChange(newImages)
  }

  const handlePreview = (index: number) => {
    Taro.previewImage({
      current: images[index],
      urls: images,
    })
  }

  return (
    <View className='image-uploader'>
      <View className='image-uploader-grid'>
        {images.map((url, index) => (
          <View key={url} className='image-uploader-item'>
            <Image
              className='image-uploader-image'
              src={url}
              mode='aspectFill'
              onClick={() => handlePreview(index)}
            />
            {index === 0 && <Text className='image-uploader-cover-badge'>封面</Text>}
            <View
              className='image-uploader-delete'
              onClick={(e) => {
                e.stopPropagation()
                handleDelete(index)
              }}
            >
              <Icon name='close' size={20} color='#fff' />
            </View>
          </View>
        ))}
        {remaining > 0 && (
          <View
            className={`image-uploader-upload-btn${uploading ? ' image-uploader-upload-btn--disabled' : ''}`}
            onClick={handleUpload}
          >
            {uploading ? (
              <Text className='image-uploader-loading'>上传中...</Text>
            ) : (
              <>
                <Icon name='plus' size={40} color='#999' />
                <Text className='image-uploader-hint'>
                  {images.length}/{maxCount}
                </Text>
              </>
            )}
          </View>
        )}
      </View>
    </View>
  )
}
