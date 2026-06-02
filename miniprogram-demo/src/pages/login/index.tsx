import { useState, useCallback } from 'react'
import { View, Text, Button } from '@tarojs/components'
import Taro from '@tarojs/taro'
import { authApi } from '../../services/api'
import userStore from '../../stores/userStore'
import Icon from '../../components/Icon'
import { track, TrackEvent } from '../../utils/tracker'
import { isH5, generateMockCode } from '../../utils/platform'
import './index.less'

export default function Login() {
  const [agreed, setAgreed] = useState(false)
  const [loading, setLoading] = useState(false)

  const handleAgreementClick = useCallback(() => {
    setAgreed((prev) => !prev)
  }, [])

  const handleProtocolClick = useCallback((type: 'user' | 'privacy') => {
    const name = type === 'user' ? '用户协议' : '隐私政策'
    Taro.showToast({ title: `《${name}》页面开发中`, icon: 'none' })
  }, [])

  const handleWechatLogin = useCallback(async () => {
    if (!agreed) {
      Taro.showToast({ title: '请先阅读并同意用户协议和隐私政策', icon: 'none' })
      return
    }

    setLoading(true)

    try {
      let code = ''
      let nickname = '微信用户'
      let avatar = ''

      if (isH5) {
        // H5 浏览器模式：使用模拟登录
        code = generateMockCode()
        nickname = 'H5测试用户'
        avatar = ''
      } else {
        // 微信环境：走正常微信登录
        const loginRes = await Taro.login()
        if (!loginRes.code) {
          throw new Error('获取微信凭证失败')
        }
        code = loginRes.code

        try {
          const profileRes = await Taro.getUserProfile({ desc: '用于展示您的个人信息' })
          const rawUserInfo = (profileRes as any).userInfo || profileRes
          nickname = rawUserInfo.nickName || nickname
          avatar = rawUserInfo.avatarUrl || avatar
        } catch {
          // getUserProfile 被拒绝或低版本不可用
        }
      }

      const result = await authApi.wechatLogin({
        code,
        nickname,
        avatar,
      })

      userStore.setLogin(
        result.token,
        result.refreshToken,
        result.user,
      )

      track(TrackEvent.LOGIN_SUCCESS)

      Taro.reLaunch({ url: '/pages/index/index' })
    } catch (err: any) {
      Taro.showToast({
        title: err.message || '登录失败，请稍后重试',
        icon: 'none',
      })
    } finally {
      setLoading(false)
    }
  }, [agreed])

  const handleBrowseWithoutLogin = useCallback(() => {
    Taro.reLaunch({ url: '/pages/index/index' })
  }, [])

  return (
    <View className='login'>
      {/* 顶部 Logo 区域 */}
      <View className='login-header'>
        <View className='login-brand'>
          <Icon name='package' size={80} color='#4F6EF7' />
        </View>
        <Text className='login-title'>出物小助手</Text>
        <Text className='login-subtitle'>记录收藏，一键生成出物清单</Text>
      </View>

      {/* 中部卖点卡片 */}
      <View className='login-features'>
        <View className='login-feature-card'>
          <View className='login-feature-card-icon-wrap'>
            <Icon name='camera' size={36} color='#4F6EF7' />
          </View>
          <View className='login-feature-card-content'>
            <Text className='login-feature-card-title'>快速记录收藏</Text>
            <Text className='login-feature-card-desc'>
              记录谷子、小卡、娃、潮玩的图片、价格和状态
            </Text>
          </View>
        </View>

        <View className='login-feature-card'>
          <View className='login-feature-card-icon-wrap'>
            <Icon name='template' size={36} color='#4F6EF7' />
          </View>
          <View className='login-feature-card-content'>
            <Text className='login-feature-card-title'>一键生成出物图</Text>
            <Text className='login-feature-card-desc'>
              自动排版出物清单，适合闲鱼、小红书、微博和微信群
            </Text>
          </View>
        </View>

        <View className='login-feature-card'>
          <View className='login-feature-card-icon-wrap'>
            <Icon name='copy' size={36} color='#4F6EF7' />
          </View>
          <View className='login-feature-card-content'>
            <Text className='login-feature-card-title'>多平台文案复制</Text>
            <Text className='login-feature-card-desc'>
              自动生成不同平台的出物文案
            </Text>
          </View>
        </View>
      </View>

      {/* 底部操作区域 */}
      <View className='login-footer'>
        {/* 协议勾选 */}
        <View className='login-agreement' onClick={handleAgreementClick}>
          <View className={`login-agreement-checkbox ${agreed ? 'checked' : ''}`}>
            {agreed && <Icon name='check' size={24} color='#fff' />}
          </View>
          <Text className='login-agreement-text'>我已阅读并同意</Text>
          <Text className='login-agreement-link' onClick={(e) => { e.stopPropagation(); handleProtocolClick('user') }}>
            《用户协议》
          </Text>
          <Text className='login-agreement-link' onClick={(e) => { e.stopPropagation(); handleProtocolClick('privacy') }}>
            《隐私政策》
          </Text>
        </View>

        {/* 登录按钮 */}
        <Button
          className='login-btn-primary'
          onClick={handleWechatLogin}
          loading={loading}
          disabled={loading}
        >
          微信一键登录
        </Button>

        {/* 游客入口 */}
        <View className='login-browse' onClick={handleBrowseWithoutLogin}>
          <Text className='login-browse-text'>先随便看看</Text>
        </View>
      </View>
    </View>
  )
}
