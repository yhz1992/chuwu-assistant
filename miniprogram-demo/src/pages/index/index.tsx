import { View } from '@tarojs/components'
import { useState, useEffect } from 'react'
import Taro from '@tarojs/taro'

import TabBar from '../../components/TabBar'
import Home from '../home/index'
import Collections from '../collections/index'
import SaleList from '../sale-list/index'
import Mine from '../mine/index'

import './index.less'

const TAB_PANELS = [Home, Collections, SaleList, Mine]

export default function MainPage() {
  const [activeTab, setActiveTab] = useState(0)

  useEffect(() => {
    const handler = (index: number) => {
      setActiveTab(index)
    }
    Taro.eventCenter.on('switchTab', handler)
    return () => {
      Taro.eventCenter.off('switchTab', handler)
    }
  }, [])

  return (
    <View className='main-page'>
      {TAB_PANELS.map((Panel, index) => (
        <View
          key={index}
          className='main-page__panel'
          style={{ display: activeTab === index ? 'block' : 'none' }}
        >
          <Panel />
        </View>
      ))}
      <TabBar current={activeTab} onChange={setActiveTab} />
    </View>
  )
}
