package com.example.demo.modules.auth.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体
 */
@Data
@TableName("users")
public class User {

    /** 用户 ID，格式：u_ + 雪花ID */
    @TableId
    private String id;

    /** 微信 openid */
    private String openid;

    /** 微信 unionid */
    private String unionid;

    /** 用户昵称 */
    private String nickname;

    /** 用户头像 URL */
    private String avatar;

    /** 会员等级 */
    private Integer membershipLevel;

    /** 会员过期时间 */
    private LocalDateTime membershipExpireAt;

    /** 违规次数 */
    private Integer violationCount;

    /** 状态：active-正常，banned-封禁 */
    private String status;

    /** 封禁截止时间（临时封禁） */
    private LocalDateTime bannedUntil;

    /** 软删除时间 */
    private LocalDateTime deletedAt;

    /** 乐观锁版本号 */
    @Version
    private Integer version;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}
