import { PropsWithChildren } from 'react'
import { useLaunch } from '@tarojs/taro'
import Taro from '@tarojs/taro'

import userStore from './stores/userStore'
import './app.less'

function App({ children }: PropsWithChildren<any>) {
  useLaunch((options) => {
    console.log('App launched.', options)

    // 优先判断：是否从分享链接进入
    const query = (options as any)?.query || {}
    const shareId = query?.shareId as string | undefined

    if (shareId) {
      // 分享页无需登录，直接跳转
      Taro.reLaunch({ url: `/pages/sale-share/index?shareId=${shareId}` })
      return
    }

    // 检查登录态
    const isLogin = userStore.isLogin()
    if (isLogin) {
      // 已登录则直接进入首页
      Taro.reLaunch({ url: '/pages/index/index' })
    } else {
      // 未登录跳转登录页
      Taro.reLaunch({ url: '/pages/login/index' })
    }
  })

  // children 是将要会渲染的页面
  return children
}

export default App
