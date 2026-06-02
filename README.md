# 出物小助手 (Chuwu Assistant)

面向谷圈、小卡、娃圈、潮玩等收藏用户的微信小程序。帮助用户记录收藏、整理心愿单、一键生成可分享的出物长图和交易文案。

## 项目结构

```
v1/
├── java-demo/               # 后端服务 (Spring Boot 3.5)
│   ├── src/main/java/       # Java 源代码
│   │   └── com/example/demo/
│   │       ├── common/      # 公共模块（配置/拦截器/异常/工具）
│   │       └── modules/     # 业务模块
│   │           ├── auth/        # 认证（微信登录/JWT）
│   │           ├── collection/  # 收藏管理
│   │           ├── event/       # 事件埋点
│   │           ├── feedback/    # 用户反馈
│   │           ├── home/        # 首页概览
│   │           ├── salelist/    # 出物清单
│   │           ├── share/       # 分享
│   │           ├── template/    # 模板中心
│   │           ├── upload/      # 图片上传
│   │           ├── user/        # 用户信息
│   │           └── wishlist/    # 心愿单
│   └── src/main/resources/
│       ├── application.yml  # 应用配置
│       └── db/              # 数据库初始化 SQL
├── miniprogram-demo/        # 前端小程序 (Taro 4 + React 18)
│   ├── src/
│   │   ├── pages/           # 17 个页面
│   │   ├── components/      # 公共组件
│   │   ├── services/        # API 接口 + 请求封装
│   │   ├── stores/          # 状态管理
│   │   ├── styles/          # 全局样式
│   │   └── utils/           # 工具函数
│   └── config/              # Taro 构建配置
├── 谷圈收藏与出物小程序mvp需求文档.md   # MVP 需求文档
├── 出物小助手-全链路使用手册.md        # 全链路使用手册
└── BUG_REPORT.md                        # 测试问题报告
```

## 技术栈

### 后端

| 技术 | 版本 | 用途 |
|------|------|------|
| Java | 17 | 运行语言 |
| Spring Boot | 3.5.0 | Web 框架 |
| MyBatis-Plus | 3.5.10.1 | ORM |
| H2 Database | - | 开发数据库 (MySQL 兼容模式) |
| MySQL Connector | - | 生产数据库驱动 |
| Redis | - | 缓存 + 分布式锁 (Redisson) |
| JWT (jjwt) | 0.12.6 | 用户认证 |
| Knife4j | 4.5.0 | API 文档 |
| Hutool | 5.8.34 | 工具库 |
| Lombok | - | 代码精简 |

### 前端

| 技术 | 版本 | 用途 |
|------|------|------|
| Taro | 4.2.0 | 跨端小程序框架 |
| React | 18.x | UI 框架 |
| TypeScript | 5.4.5 | 类型安全 |
| Vite | 4.x | H5 构建 |
| Less | 4.2.0 | CSS 预处理器 |

## 本地开发

### 环境要求

- **JDK 17** 或更高版本
- **Maven 3.8** 或更高版本
- **Node.js 18** 或更高版本
- **Redis** (可选，开发模式下可使用内嵌实现或跳过)

### 1. 启动后端

```bash
cd java-demo

# 安装依赖并启动（默认端口 8080）
./mvnw spring-boot:run

# 或者先编译再运行
./mvnw clean package -DskipTests
java -jar target/demo-0.0.1-SNAPSHOT.jar
```

启动后访问：
- **API 服务**: http://localhost:8080
- **API 文档 (Knife4j)** : http://localhost:8080/doc.html
- **H2 控制台**: http://localhost:8080/h2-console

> 开发模式下无需安装 MySQL，使用 H2 文件数据库（数据文件位于 `java-demo/data/`）。
> 微信登录默认使用模拟模式（`wechat.dev-mode: true`），无需真实微信 AppID。

### 2. 启动前端

```bash
cd miniprogram-demo

# 安装依赖
npm install

# 启动 H5 开发模式（浏览器预览，默认端口 3000）
npm run dev:h5

# 启动微信小程序开发模式（需要微信开发者工具）
npm run dev:weapp
```

前端开发环境变量在 `.env.development` 中配置，默认 API 地址指向 `http://localhost:8080`。

### 3. 前端构建

```bash
# 构建 H5 版本
npm run build:h5

# 构建微信小程序版本
npm run build:weapp

# 支持其他平台：swan / alipay / tt / rn / qq / jd / harmony-hybrid
```

## 数据库

项目使用 H2 数据库进行开发，表结构定义在 `java-demo/src/main/resources/db/schema-h2.sql`。

### 核心表

| 表名 | 说明 |
|------|------|
| `users` | 用户表 |
| `collection_items` | 收藏/藏品表 |
| `sale_lists` | 出物清单表 |
| `sale_list_items` | 出物清单商品表 |
| `wishlist_items` | 心愿单表 |
| `templates` | 模板表 |
| `shares` | 分享记录表 |
| `feedbacks` | 用户反馈表 |
| `event_logs` | 事件埋点表 |

## API 概览

所有 API 前缀为 `/api/v1/`，除登录和分享页外均需 JWT 认证（Header: `Authorization: Bearer <token>`）。

| 模块 | 端点 | 说明 |
|------|------|------|
| Auth | `POST /auth/wechat-login` | 微信登录 |
| Auth | `POST /auth/refresh` | 刷新 Token |
| User | `GET/PUT /user/me` | 当前用户信息 |
| Home | `GET /home/overview` | 首页概览 |
| Upload | `POST /upload/image` | 图片上传 |
| Collection | `GET/POST /collections` | 收藏列表/新增 |
| Collection | `GET/PUT/DELETE /collections/{id}` | 收藏详情/更新/删除 |
| SaleList | `GET/POST /sale-lists` | 出物清单列表/创建 |
| SaleList | `GET/PUT/DELETE /sale-lists/{id}` | 清单详情/更新/删除 |
| SaleList | `POST /sale-lists/{id}/generate` | 生成分享图 |
| SaleList | `POST /sale-lists/{id}/duplicate` | 复制清单 |
| Template | `GET /templates` | 模板列表 |
| Share | `GET /share/{shareId}` | 分享页（无需登录） |
| Wishlist | `GET/POST /wishlist` | 心愿单列表/新增 |
| Wishlist | `PUT/DELETE /wishlist/{id}` | 心愿单更新/删除 |
| Wishlist | `POST /wishlist/{id}/convert` | 心愿单转收藏 |
| Feedback | `POST /feedback` | 提交反馈 |
| Event | `POST /events/track` | 埋点上报 |

完整 API 文档可在启动后端后访问 http://localhost:8080/doc.html 查看。

## 架构说明

- **认证流程**: 微信登录获取 code → 后端换取 openid → 签发 JWT → 前端存储 token
- **图片存储**: 开发模式下存储在本地 `uploads/` 目录，生产环境应接入 OSS
- **长图生成**: 前端使用 Canvas API 在客户端生成出物长图
- **分享传播**: 生成清单后创建分享记录，通过 shareId 访问分享页

## 相关文档

- [MVP 需求文档](谷圈收藏与出物小程序mvp需求文档.md)
- [全链路使用手册](出物小助手-全链路使用手册.md)
- [Bug 报告](BUG_REPORT.md)
