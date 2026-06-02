package com.example.demo.common.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 统一错误码枚举
 * 参照需求文档 §17.1 通用错误码 + §23 业务错误码
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    // ==================== 通用错误码（§17.1） ====================
    /** 成功 */
    SUCCESS(0, "success"),

    // -------- 17.1.2 认证授权类（01xx） --------
    /** 未登录或 token 已过期 */
    UNAUTHORIZED(1001, "未登录或登录已过期"),
    /** token 无效 */
    INVALID_TOKEN(1002, "token无效"),
    /** 微信登录失败 */
    WX_LOGIN_FAILED(1003, "微信登录失败"),
    /** Refresh token 无效或已过期 */
    REFRESH_TOKEN_INVALID(1003, "refresh token 无效或已过期"),
    /** 无权限访问 */
    FORBIDDEN(1005, "无权限访问"),

    // -------- 17.1.3 参数校验类（02xx） --------
    /** 参数错误 */
    PARAM_ERROR(2001, "参数错误"),
    /** 参数缺失 */
    PARAM_MISSING(2002, "缺少必要参数"),
    /** 参数格式错误 */
    PARAM_FORMAT_ERROR(2003, "参数格式错误"),
    /** 请求体不能为空 */
    REQUEST_BODY_EMPTY(2004, "请求体不能为空"),

    // -------- 17.1.4 资源类（03xx） --------
    /** 资源不存在 */
    NOT_FOUND(3001, "请求的资源不存在"),
    /** 资源已存在 */
    ALREADY_EXISTS(3002, "资源已存在"),
    /** 资源已被删除 */
    DELETED(3003, "资源已被删除"),

    // -------- 17.1.5 操作类（04xx） --------
    /** 操作频繁 */
    TOO_MANY_REQUESTS(4001, "操作过于频繁，请稍后再试"),
    /** 操作不支持 */
    UNSUPPORTED_OPERATION(4002, "不支持的操作"),
    /** 操作失败 */
    OPERATION_FAILED(4003, "操作失败"),

    // -------- 17.1.6 系统类（05xx） --------
    /** 服务器内部错误 */
    INTERNAL_ERROR(5001, "服务器内部错误"),
    /** 服务暂不可用 */
    SERVICE_UNAVAILABLE(5002, "服务暂不可用，请稍后再试"),
    /** 第三方服务异常 */
    THIRD_PARTY_ERROR(5003, "第三方服务异常"),
    /** 数据库异常 */
    DATABASE_ERROR(5004, "数据库操作异常"),

    // ==================== 业务错误码（§23） ====================

    // -------- 23.1 藏品管理（21xx） --------
    /** 藏品不存在 */
    COLLECTION_NOT_FOUND(2101, "藏品不存在"),
    /** 藏品已存在 */
    COLLECTION_ALREADY_EXISTS(2102, "藏品已添加过，请勿重复添加"),
    /** 藏品图片不能为空 */
    COLLECTION_IMAGE_EMPTY(2103, "请上传藏品图片"),
    /** 藏品名称不能为空 */
    COLLECTION_NAME_EMPTY(2104, "请输入藏品名称"),
    /** 藏品分类无效 */
    COLLECTION_INVALID_TYPE(2105, "无效的藏品分类"),

    // -------- 23.2 出售清单（22xx） --------
    /** 清单不存在 */
    SALE_LIST_NOT_FOUND(2201, "出售清单不存在"),
    /** 清单已生成无法修改 */
    SALE_LIST_ALREADY_GENERATED(2202, "清单已生成，无法修改"),
    /** 清单已分享无法撤销 */
    SALE_LIST_ALREADY_SHARED(2203, "清单已分享，无法撤销"),
    /** 清单名不能为空 */
    SALE_LIST_NAME_EMPTY(2204, "请输入清单名称"),
    /** 清单无商品 */
    SALE_LIST_EMPTY(2205, "清单中没有商品，请先添加商品"),

    // -------- 23.3 商品（23xx） --------
    /** 商品不存在 */
    SALE_ITEM_NOT_FOUND(2301, "商品不存在"),
    /** 商品已售出 */
    SALE_ITEM_SOLD(2302, "商品已售出"),
    /** 商品已下架 */
    SALE_ITEM_UNAVAILABLE(2303, "商品已下架"),
    /** 商品价格无效 */
    SALE_ITEM_INVALID_PRICE(2304, "商品价格无效"),
    /** 商品库存不足 */
    SALE_ITEM_OUT_OF_STOCK(2305, "商品库存不足"),

    // -------- 23.4 砍价规则（24xx） --------
    /** 砍价规则不能为空 */
    BARGAIN_RULE_EMPTY(2401, "请设置砍价规则"),
    /** 砍价金额不能为负 */
    BARGAIN_AMOUNT_INVALID(2402, "砍价金额无效"),

    // -------- 23.5 分享（25xx） --------
    /** 分享链接无效 */
    SHARE_LINK_INVALID(2501, "分享链接无效或已过期"),
    /** 分享不存在 */
    SHARE_NOT_FOUND(2502, "分享不存在"),
    /** 分享已过期 */
    SHARE_EXPIRED(2503, "分享已过期"),
    /** 对外分享链接生成失败 */
    SHARE_GENERATE_FAILED(2504, "对外分享链接生成失败"),

    // -------- 23.6 心愿单（26xx） --------
    /** 心愿单不存在 */
    WISHLIST_NOT_FOUND(2601, "心愿单不存在"),
    /** 心愿单已存在 */
    WISHLIST_ALREADY_EXISTS(2602, "该商品已在心愿单中"),
    /** 心愿单已达上限 */
    WISHLIST_LIMIT_EXCEEDED(2603, "心愿单已达上限"),
    /** 心愿单名称不能为空 */
    WISHLIST_NAME_EMPTY(2604, "请输入心愿单名称"),

    // -------- 23.7 用户（27xx） --------
    /** 用户不存在 */
    USER_NOT_FOUND(2701, "用户不存在"),
    /** 用户已被封禁 */
    USER_BANNED(2702, "账号已被封禁"),
    /** 用户信息获取失败 */
    USER_INFO_FAILED(2703, "用户信息获取失败"),
    /** 手机号格式错误 */
    USER_PHONE_INVALID(2704, "手机号格式错误"),

    // -------- 23.8 反馈（28xx） --------
    /** 反馈内容不能为空 */
    FEEDBACK_CONTENT_EMPTY(2801, "反馈内容不能为空"),
    /** 反馈提交过于频繁 */
    FEEDBACK_TOO_FREQUENT(2802, "反馈提交过于频繁，请稍后再试"),

    // -------- 23.9 模板（29xx） --------
    /** 模板不存在 */
    TEMPLATE_NOT_FOUND(2901, "模板不存在"),
    /** 模板名称不能为空 */
    TEMPLATE_NAME_EMPTY(2902, "请输入模板名称"),
    /** 模板名称已存在 */
    TEMPLATE_NAME_EXISTS(2903, "模板名称已存在"),
    ;

    /** 错误码 */
    private final int code;

    /** 错误信息 */
    private final String message;
}
