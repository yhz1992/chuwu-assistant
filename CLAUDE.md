# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概览

出物小助手 — 面向谷圈/小卡/娃圈/潮玩收藏用户的微信小程序全栈项目。

- **后端**: `java-demo/`，Spring Boot 3.5 + MyBatis-Plus 3.5 + H2 (开发) / MySQL (生产) + JWT
- **前端**: `miniprogram-demo/`，Taro 4 + React 18 + TypeScript + Less，支持微信/H5/支付宝等多端

## 常用命令

### 后端 (`java-demo/`)

```bash
# 运行开发服务器（端口 8080，H2 文件数据库，模拟微信登录）
mvn spring-boot:run

# 运行所有测试
mvn test -DargLine="-Djava.io.tmpdir=$TMPDIR"

# 运行单个测试类
mvn test -DargLine="-Djava.io.tmpdir=$TMPDIR" -Dtest=AuthServiceTest

# 编译检查
mvn compile
```

测试必须加 `-DargLine="-Djava.io.tmpdir=$TMPDIR"`，否则 surefire 在沙箱中创建临时目录会失败。

API 文档: http://localhost:8080/doc.html (Knife4j)
H2 控制台: http://localhost:8080/h2-console (JDBC URL: `jdbc:h2:file:./data/chuwu_assistant`，用户名 `sa`，空密码)

### 前端 (`miniprogram-demo/`)

```bash
npm install

# H5 开发模式（浏览器预览）
npm run dev:h5

# 微信小程序开发模式
npm run dev:weapp

# 构建
npm run build:h5       # H5
npm run build:weapp    # 微信小程序
```

前端无测试。ESLint/Stylelint 规则已配置但需手动运行。

## 架构要点

### 后端分层

```
controller → service → mapper (MyBatis-Plus BaseMapper)
                ↓
           entity (对应数据库表)
           dto (请求/响应)
```

公共层 (`common/`) 提供：
- `AuthInterceptor` — JWT 拦截器，从 Header 取 `Authorization: Bearer <token>`，将 userId 写入 `UserContext`
- `RateLimiterAspect` — `@RateLimit` 注解切面（依赖 Redisson，开发模式无 Redis 时不会触发）
- `GlobalExceptionHandler` — 统一异常处理，`BusinessException` → `ApiResponse` 错误码
- `JwtUtils` — 生成/解析 access token 和 refresh token
- `SnowflakeIdUtils` — 带前缀的雪花 ID（如 `ci_xxx`, `sl_xxx`, `u_xxx`）

### JSON 字段映射注意事项

数据库存储 JSON 的字段（如 `collection_items.images`, `sale_lists.trade_rule`）在实体层是 `String` 类型，**不是** `List`/`Map`。在 Service 层使用 `JSONUtil.toJsonStr()` 写入、`objectMapper.readValue()` 或 `JSONUtil.toList()` 读取。`@TableField(typeHandler = JacksonTypeHandler.class)` 仅在特定实体（如 `SaleList.generatedPages`）上使用。

前后端字段名通过 `@JsonProperty` 映射（如 `@JsonProperty("showWatermark") private Boolean watermark`）。后端 Java 用驼峰，JSON 序列化后 Spring 默认也是驼峰，但 MyBatis-Plus 的 `map-underscore-to-camel-case: true` 处理数据库下划线到实体驼峰的转换。

### 认证流程

1. 前端调用 `/api/v1/auth/wechat-login`，传入微信 `code`
2. 开发模式 (`wechat.dev-mode: true`) 下用 `"dev_" + code` 作为模拟 openid，跳过真实微信 API
3. 后端查 `users` 表，不存在则创建
4. 签发 access token (7天) + refresh token (30天)，refresh token 存 Redis
5. 前端请求时在 Header 携带 `Authorization: Bearer <token>`

分享页 `/api/v1/share/{shareId}` 无需认证。

### 前端 API 层

`src/services/request.ts` 封装了 Taro 请求，自动注入 token、处理 401 刷新、统一错误提示。`src/services/api.ts` 定义所有接口的 TypeScript 类型和调用函数。

### 已知问题

- 前端无测试框架。测试需通过浏览器自动化或微信开发者工具手动验证。
- 上传功能在 H5 和微信小程序端行为不同（文件选择/保存图片），Taro API 做了平台适配。
- `miniprogram-demo/.git` 存在但无提交记录。整个项目尚未纳入版本控制。
