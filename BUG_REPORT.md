# 出物小助手 - 全链路测试问题报告

## 测试环境
- 前端: Taro 4.x H5 + Vite，运行在 http://localhost:3000
- 后端: Spring Boot 3.5 + H2 数据库，运行在 http://localhost:8080
- 浏览器: Chromium 148 (Playwright headless)

## 已修复的问题

| # | 问题 | 状态 |
|---|------|------|
| F1 | 缺少 CORS 配置 (H5跨域请求被拒绝) | ✅ 已修复 |
| F2 | OPTIONS 预检请求被拦截器拒绝 | ✅ 已修复 |
| F3 | H5 模式登录不支持 (`Taro.login` 不可用) | ✅ 已修复 |
| F4 | H5 模式保存图片/分享不支持 | ✅ 已适配 |
| F5 | H5 模式图片选择不支持 | ✅ 已适配 |

## 待修复的问题

| # | 标题 | 严重程度 | 详情 |
|---|------|----------|------|
| B1 | 首页 API 字段名与前端不匹配 | 🔴 高 | 后端返回 `totalCollections`, `totalSaleLists` 等；前端期望 `collectionCount`, `forSaleCount` 等 |
| B2 | images 字段返回 JSON 字符串 | 🔴 高 | 收藏 API 返回的 `images` 是字符串 `"[...]"` 而非数组，前端无法直接使用 |
| B3 | 生成长图接口 500 错误 | 🔴 高 | `POST /sale-lists/{id}/generate` 返回 5001 错误 |
| B4 | 模板字段 JSON 序列化问题 | 🟡 中 | `config` 和 `tags` 返回字符串而非 JSON 对象 |
| B5 | 埋点参数名不匹配 | 🟡 中 | 前端发送 `{event: "..."}`，后端可能期望 `eventName` 或 `event_name` |
| B6 | 出物清单 `collections` 字段名 | 🟡 中 | 前端 `SaleListItem.collections`，后端返回 `items` |
| B7 | Icon 组件 CSS 遮罩点击问题 | 🟢 低 | `taro-image-core` 遮挡按钮点击，需检查 Icon 组件 pointer-events |
