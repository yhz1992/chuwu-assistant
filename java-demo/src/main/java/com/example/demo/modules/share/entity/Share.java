package com.example.demo.modules.share.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 分享记录实体
 * 对应数据库表 shares
 */
@Data
@TableName("shares")
public class Share {

    /** 分享 ID，格式：shr_ + 雪花ID */
    @TableId
    private String id;

    /** 关联的出售清单 ID */
    private String saleListId;

    /** 分享用户 ID */
    private String userId;

    /** 是否公开 */
    private Boolean isPublic;

    /** 浏览次数 */
    private Integer viewCount;

    /** 撤销分享时间 */
    private LocalDateTime revokedAt;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}
