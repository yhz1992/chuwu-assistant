-- ============================================
-- 出物小助手 - 数据库初始化脚本
-- 数据库: chuwu_assistant, 字符集: utf8mb4
-- ============================================

CREATE DATABASE IF NOT EXISTS chuwu_assistant
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE chuwu_assistant;

-- ============================================
-- 用户表
-- ============================================
CREATE TABLE `users` (
  `id`                   VARCHAR(32)  NOT NULL COMMENT '用户ID(u_+雪花ID)',
  `openid`               VARCHAR(64)  NOT NULL COMMENT '微信openid',
  `unionid`              VARCHAR(64)  DEFAULT NULL COMMENT '微信unionid',
  `nickname`             VARCHAR(64)  DEFAULT NULL COMMENT '昵称',
  `avatar`               VARCHAR(512) DEFAULT NULL COMMENT '头像URL',
  `membership_level`     TINYINT      DEFAULT 0 COMMENT '会员等级(0=免费)',
  `membership_expire_at` DATETIME     DEFAULT NULL COMMENT '会员过期时间',
  `violation_count`      INT          DEFAULT 0 COMMENT '90天滑动窗口违规次数',
  `status`               VARCHAR(16)  DEFAULT 'active' COMMENT 'active/banned',
  `banned_until`         DATETIME     DEFAULT NULL COMMENT '封禁截止时间',
  `deleted_at`           DATETIME     DEFAULT NULL COMMENT '软删',
  `version`              INT          DEFAULT 0 COMMENT '乐观锁',
  `created_at`           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_openid` (`openid`),
  KEY `idx_status` (`status`, `deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ============================================
-- 收藏表
-- ============================================
CREATE TABLE `collection_items` (
  `id`               VARCHAR(32)   NOT NULL COMMENT '收藏ID(ci_+雪花ID)',
  `user_id`          VARCHAR(32)   NOT NULL COMMENT '所属用户',
  `name`             VARCHAR(128)  NOT NULL COMMENT '商品名称',
  `images`           JSON          DEFAULT NULL COMMENT '图片URL数组(最多9张)',
  `cover_image`      VARCHAR(512)  DEFAULT NULL COMMENT '封面图URL',
  `work_name`        VARCHAR(128)  DEFAULT NULL COMMENT '作品名',
  `character_name`   VARCHAR(128)  DEFAULT NULL COMMENT '角色名',
  `item_type`        VARCHAR(32)   NOT NULL COMMENT '类型(badge/standee/card/shikishi/keychain/plush/doll_clothes/figure/tcg/other)',
  `purchase_price`   DECIMAL(10,2) DEFAULT NULL COMMENT '入手价(元)',
  `quantity`         INT           DEFAULT 1 COMMENT '数量',
  `purchase_channel` VARCHAR(64)   DEFAULT NULL COMMENT '入手渠道',
  `purchase_date`    DATE          DEFAULT NULL COMMENT '入手日期',
  `status`           VARCHAR(32)   NOT NULL DEFAULT 'arrived' COMMENT '状态(arrived/preorder/pending_payment/pending_shipment/pending_receipt/for_sale/sold/not_for_sale)',
  `note`             TEXT          DEFAULT NULL COMMENT '备注',
  `is_for_sale`      TINYINT(1)    DEFAULT 0 COMMENT '是否待出物',
  `sale_price`       DECIMAL(10,2) DEFAULT NULL COMMENT '出物价(元)',
  `flaw_note`        TEXT          DEFAULT NULL COMMENT '瑕疵说明',
  `shipping_rule`    VARCHAR(32)   DEFAULT NULL COMMENT '包邮规则(included/not_included/conditional)',
  `bargain_rule`     VARCHAR(32)   DEFAULT NULL COMMENT '小刀规则(bargain/no_bargain/bundle_first)',
  `bundle_rule`      VARCHAR(256)  DEFAULT NULL COMMENT '捆绑规则',
  `audit_status`     VARCHAR(16)   DEFAULT 'pending' COMMENT '审核状态(pending/passed/rejected)',
  `audit_message`    VARCHAR(512)  DEFAULT NULL COMMENT '审核失败原因',
  `custom_type_id`   VARCHAR(32)   DEFAULT NULL COMMENT '自定义类型ID',
  `reminder_at`      DATETIME      DEFAULT NULL COMMENT '提醒时间',
  `search_text`      VARCHAR(512)  DEFAULT NULL COMMENT '搜索索引(名称+作品+角色拼音)',
  `sort_index`       BIGINT        DEFAULT 0 COMMENT '拖拽排序',
  `deleted_at`       DATETIME      DEFAULT NULL COMMENT '软删',
  `version`          INT           DEFAULT 0 COMMENT '乐观锁',
  `created_at`       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_status` (`user_id`, `status`, `deleted_at`),
  KEY `idx_user_for_sale` (`user_id`, `is_for_sale`, `deleted_at`),
  KEY `idx_user_created` (`user_id`, `created_at` DESC),
  KEY `idx_item_type` (`item_type`),
  KEY `idx_audit` (`audit_status`, `created_at`),
  FULLTEXT KEY `ft_search` (`search_text`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收藏表';

-- ============================================
-- 出物清单表
-- ============================================
CREATE TABLE `sale_lists` (
  `id`               VARCHAR(32)   NOT NULL COMMENT '清单ID(sl_+雪花ID)',
  `user_id`          VARCHAR(32)   NOT NULL COMMENT '所属用户',
  `title`            VARCHAR(128)  DEFAULT NULL COMMENT '清单标题',
  `description`      TEXT          DEFAULT NULL COMMENT '清单说明',
  `template_id`      VARCHAR(32)   DEFAULT NULL COMMENT '模板ID',
  `status`           VARCHAR(16)   NOT NULL DEFAULT 'draft' COMMENT 'draft/generated/shared',
  `total_count`      INT           DEFAULT 0 COMMENT '商品数量',
  `total_price`      DECIMAL(10,2) DEFAULT 0 COMMENT '总价',
  `generated_image`  VARCHAR(512)  DEFAULT NULL COMMENT '生成图片URL',
  `generated_pages`  JSON          DEFAULT NULL COMMENT '多页长图URL数组',
  `share_id`         VARCHAR(32)   DEFAULT NULL COMMENT '分享ID',
  `trade_rule`       JSON          DEFAULT NULL COMMENT '交易规则汇总',
  `watermark`        TINYINT(1)    DEFAULT 1 COMMENT '是否带水印',
  `deleted_at`       DATETIME      DEFAULT NULL COMMENT '软删',
  `version`          INT           DEFAULT 0 COMMENT '乐观锁',
  `created_at`       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_status_created` (`user_id`, `status`, `created_at` DESC),
  UNIQUE KEY `uk_share` (`share_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='出物清单表';

-- ============================================
-- 出物清单商品表
-- ============================================
CREATE TABLE `sale_list_items` (
  `id`                    VARCHAR(32)   NOT NULL COMMENT '清单商品ID(sli_+雪花ID)',
  `sale_list_id`          VARCHAR(32)   NOT NULL COMMENT '所属清单',
  `collection_item_id`    VARCHAR(32)   DEFAULT NULL COMMENT '原始收藏ID',
  `collection_snapshot`   JSON          DEFAULT NULL COMMENT '收藏快照(加入清单时冻结)',
  `name`                  VARCHAR(128)  NOT NULL COMMENT '商品名称',
  `image`                 VARCHAR(512)  DEFAULT NULL COMMENT '商品图片',
  `price`                 DECIMAL(10,2) DEFAULT NULL COMMENT '出物价(元)',
  `quantity`              INT           DEFAULT 1 COMMENT '数量',
  `status`                VARCHAR(16)   DEFAULT 'available' COMMENT 'available/sold/reserved',
  `flaw_note`             TEXT          DEFAULT NULL COMMENT '瑕疵说明',
  `shipping_rule`         VARCHAR(32)   DEFAULT NULL COMMENT '包邮规则',
  `bargain_rule`          VARCHAR(32)   DEFAULT NULL COMMENT '小刀规则',
  `bundle_rule`           VARCHAR(256)  DEFAULT NULL COMMENT '捆绑规则',
  `note`                  TEXT          DEFAULT NULL COMMENT '备注',
  `sort_order`            INT           DEFAULT 0 COMMENT '排序',
  `sold_at`               DATETIME      DEFAULT NULL COMMENT '售出时间',
  `created_at`            DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`            DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_list_sort` (`sale_list_id`, `sort_order`),
  KEY `idx_collection` (`collection_item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='出物清单商品表';

-- ============================================
-- 心愿表
-- ============================================
CREATE TABLE `wishlist_items` (
  `id`              VARCHAR(32)   NOT NULL COMMENT '心愿ID(wi_+雪花ID)',
  `user_id`         VARCHAR(32)   NOT NULL COMMENT '所属用户',
  `name`            VARCHAR(128)  NOT NULL COMMENT '名称',
  `image`           VARCHAR(512)  DEFAULT NULL COMMENT '图片URL',
  `work_name`       VARCHAR(128)  DEFAULT NULL COMMENT '作品名',
  `character_name`  VARCHAR(128)  DEFAULT NULL COMMENT '角色名',
  `item_type`       VARCHAR(32)   DEFAULT NULL COMMENT '类型',
  `target_price`    DECIMAL(10,2) DEFAULT NULL COMMENT '目标价(元)',
  `desire_level`    VARCHAR(16)   DEFAULT 'normal' COMMENT 'normal/high/must_have',
  `status`          VARCHAR(16)   DEFAULT 'want' COMMENT 'want/bought/paused',
  `note`            TEXT          DEFAULT NULL COMMENT '备注',
  `deleted_at`      DATETIME      DEFAULT NULL COMMENT '软删',
  `version`         INT           DEFAULT 0 COMMENT '乐观锁',
  `created_at`      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_status` (`user_id`, `status`, `deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='心愿表';

-- ============================================
-- 模板表
-- ============================================
CREATE TABLE `templates` (
  `id`            VARCHAR(32)  NOT NULL COMMENT '模板ID(tpl_+雪花ID)',
  `name`          VARCHAR(64)  NOT NULL COMMENT '模板名称',
  `preview_image` VARCHAR(512) DEFAULT NULL COMMENT '预览图URL',
  `type`          VARCHAR(16)  NOT NULL COMMENT 'simple/card/wall',
  `is_premium`    TINYINT(1)   DEFAULT 0 COMMENT '是否高级模板',
  `is_active`     TINYINT(1)   DEFAULT 1 COMMENT '是否上架',
  `config`        JSON         DEFAULT NULL COMMENT '模板配置',
  `tags`          JSON         DEFAULT NULL COMMENT '标签数组',
  `description`   VARCHAR(256) DEFAULT NULL COMMENT '模板说明',
  `sort_order`    INT          DEFAULT 0 COMMENT '排序权重',
  `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_type_active` (`type`, `is_active`),
  KEY `idx_sort` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='模板表';

-- ============================================
-- 分享表
-- ============================================
CREATE TABLE `shares` (
  `id`           VARCHAR(32) NOT NULL COMMENT '分享ID(shr_+base62)',
  `sale_list_id` VARCHAR(32) NOT NULL COMMENT '关联清单',
  `user_id`      VARCHAR(32) NOT NULL COMMENT '所属用户',
  `is_public`    TINYINT(1)  DEFAULT 1 COMMENT '是否公开',
  `view_count`   INT         DEFAULT 0 COMMENT 'PV数',
  `revoked_at`   DATETIME    DEFAULT NULL COMMENT '撤回时间',
  `created_at`   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_sale_list` (`sale_list_id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分享表';

-- ============================================
-- 反馈表
-- ============================================
CREATE TABLE `feedbacks` (
  `id`           VARCHAR(32)  NOT NULL COMMENT '反馈ID(fb_+雪花ID)',
  `user_id`      VARCHAR(32)  DEFAULT NULL COMMENT '提交用户(游客可为空)',
  `type`         VARCHAR(16)  NOT NULL COMMENT 'feature/bug/template/other',
  `content`      TEXT         NOT NULL COMMENT '反馈内容',
  `contact`      VARCHAR(128) DEFAULT NULL COMMENT '联系方式',
  `images`       JSON         DEFAULT NULL COMMENT '截图URL数组',
  `status`       VARCHAR(16)  DEFAULT 'pending' COMMENT 'pending/processing/done',
  `handler_id`   VARCHAR(32)  DEFAULT NULL COMMENT '处理人',
  `handler_note` TEXT         DEFAULT NULL COMMENT '处理备注',
  `deleted_at`   DATETIME     DEFAULT NULL COMMENT '软删',
  `created_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_status_type` (`status`, `type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='反馈表';

-- ============================================
-- 事件埋点表
-- ============================================
CREATE TABLE `event_logs` (
  `id`          VARCHAR(32)  NOT NULL,
  `user_id`     VARCHAR(32)  DEFAULT NULL COMMENT '用户ID(游客为空)',
  `event_name`  VARCHAR(64)  NOT NULL COMMENT '事件名',
  `properties`  JSON         DEFAULT NULL COMMENT '事件属性',
  `client_info` JSON         DEFAULT NULL COMMENT '客户端信息(机型/系统/版本/网络)',
  `ip`          VARCHAR(64)  DEFAULT NULL COMMENT '客户端IP',
  `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_event_time` (`user_id`, `event_name`, `created_at`),
  KEY `idx_event_time` (`event_name`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='事件埋点表';

-- ============================================
-- 自定义类型表
-- ============================================
CREATE TABLE `custom_item_types` (
  `id`          VARCHAR(32) NOT NULL,
  `user_id`     VARCHAR(32) NOT NULL,
  `name`        VARCHAR(32) NOT NULL COMMENT '类型名(最多10字)',
  `usage_count` INT         DEFAULT 0 COMMENT '使用次数',
  `created_at`  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`),
  UNIQUE KEY `uk_user_name` (`user_id`, `name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='自定义类型表';

-- ============================================
-- 提醒表
-- ============================================
CREATE TABLE `reminders` (
  `id`          VARCHAR(32)  NOT NULL,
  `user_id`     VARCHAR(32)  NOT NULL,
  `target_type` VARCHAR(32)  NOT NULL COMMENT 'collection/sale_list',
  `target_id`   VARCHAR(32)  NOT NULL,
  `remind_at`   DATETIME     NOT NULL COMMENT '提醒时间',
  `status`      VARCHAR(16)  DEFAULT 'pending' COMMENT 'pending/sent/cancelled',
  `message`     VARCHAR(256) DEFAULT NULL COMMENT '提醒文案',
  `channel`     VARCHAR(32)  DEFAULT 'in_app' COMMENT 'wx_subscribe/in_app',
  `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_remind_status` (`remind_at`, `status`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='提醒表';

-- ============================================
-- 审核任务表
-- ============================================
CREATE TABLE `audit_tasks` (
  `id`            VARCHAR(32)  NOT NULL,
  `target_type`   VARCHAR(16)  NOT NULL COMMENT 'image/text',
  `target_table`  VARCHAR(32)  NOT NULL COMMENT 'collection_items/sale_lists/wishlist_items/feedbacks',
  `target_id`     VARCHAR(32)  NOT NULL,
  `user_id`       VARCHAR(32)  DEFAULT NULL,
  `content`       TEXT         DEFAULT NULL COMMENT '文本内容或图片URL',
  `auto_result`   VARCHAR(16)  DEFAULT NULL COMMENT 'auto_pass/auto_suspect/auto_fail',
  `manual_result` VARCHAR(16)  DEFAULT NULL COMMENT 'pass/fail',
  `handler_id`    VARCHAR(32)  DEFAULT NULL COMMENT '处理人(AdminUser.id)',
  `handled_at`    DATETIME     DEFAULT NULL,
  `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_status_created` (`manual_result`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审核任务表';

-- ============================================
-- 管理员表
-- ============================================
CREATE TABLE `admin_users` (
  `id`            VARCHAR(32)  NOT NULL,
  `username`      VARCHAR(64)  NOT NULL COMMENT '唯一用户名',
  `password_hash` VARCHAR(256) NOT NULL COMMENT 'bcrypt哈希',
  `role`          VARCHAR(16)  NOT NULL DEFAULT 'cs' COMMENT 'super_admin/ops/cs',
  `status`        VARCHAR(16)  DEFAULT 'active' COMMENT 'active/disabled',
  `last_login_at` DATETIME     DEFAULT NULL,
  `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员表';

-- ============================================
-- 种子数据：3个MVP模板
-- ============================================
INSERT INTO `templates` (`id`, `name`, `type`, `is_premium`, `is_active`, `config`, `tags`, `description`, `sort_order`)
VALUES
('tpl_001', '简洁表格款', 'simple', 0, 1,
 '{"layout":"table","columns":["image","name","price","note"],"maxItemsPerPage":25}',
 '["大量出物","信息清晰"]', '适合一次性整理大量出物，信息密度高', 1),
('tpl_002', '卡片展示款', 'card', 0, 1,
 '{"layout":"card","columns":2,"maxItemsPerPage":12,"cardWidth":343,"gap":16}',
 '["小红书","好看"]', '每件商品一张小卡片，适合小红书/朋友圈分享', 2),
('tpl_003', '图片墙款', 'wall', 0, 1,
 '{"layout":"grid","columns":3,"maxItemsPerPage":30,"imageSize":234,"gap":6}',
 '["图片多","视觉冲击"]', '强调图片展示，适合小卡、吧唧、立牌', 3);

-- ============================================
-- 插入默认管理员（密码: admin123，bcrypt）
-- ============================================
INSERT INTO `admin_users` (`id`, `username`, `password_hash`, `role`, `status`)
VALUES ('adm_001', 'admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'super_admin', 'active');
