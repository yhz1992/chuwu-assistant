---
description: UI 视觉对比检查 — 对比设计稿与实现的差异，检查间距/颜色/字体/圆角等
argument-hint: [URL] [设计稿描述]
allowed-tools: Bash, Read, Write, Edit, Grep
---

# UI 视觉对比检查

对比网页实现与设计稿（或设计系统规范），发现并修复视觉差异。

## 使用方式

```bash
/visual-check localhost:3000 "导航栏高度48px，背景色#1a1a2e，菜单项间距24px"
/visual-check localhost:3000/login "登录卡片宽400px，圆角12px，阴影0 4px 24px rgba(0,0,0,.12)"
```

## 检查维度

### 1. 布局
- 元素位置是否正确（header/sidebar/content 等区域划分）
- 对齐方式（flex/grid 的对齐是否符合设计）
- 元素间距（gap, margin, padding）
- 容器宽度约束（max-width 是否正确）

### 2. 颜色
- 背景色、文字色、边框色
- Hover/Active/Focus/Disabled 状态色
- 暗色模式颜色（如适用）
- 渐变色方向和色值

### 3. 字体
- font-family（中英文字体是否正确）
- font-size（各级标题和正文）
- font-weight（粗体是否到位）
- line-height（行高是否合适）
- letter-spacing（字间距）
- text-align（对齐方式）

### 4. 边框与圆角
- border-width、border-style、border-color
- border-radius（四角是否一致或按设计差异化）
- outline（focus 状态）

### 5. 阴影与效果
- box-shadow（层级阴影是否正确）
- opacity（半透明元素）
- backdrop-filter/blur（毛玻璃效果）
- transition/animation（动效时长和缓动函数）

### 6. 图标与图片
- 图标大小和颜色
- 图标与文字的间距
- 图片的 object-fit 和圆角

## 工作流程

1. **打开页面** — 在浏览器中打开目标 URL
2. **读取样式** — 获取关键元素的 computed styles
3. **逐项对比** — 与设计稿描述或规范对比
4. **列出差异** — 标出每个差异项：元素名 → 期望值 vs 实际值
5. **修复代码** — 定位源码中的样式，逐个修复
6. **回浏览器验证** — 刷新页面确认修复
7. **输出报告** — 汇总已修复项和仍需关注项
