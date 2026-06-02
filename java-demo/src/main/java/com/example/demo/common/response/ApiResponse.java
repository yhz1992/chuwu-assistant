package com.example.demo.common.response;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 统一响应体
 *
 * @param <T> 数据类型
 */
@Slf4j
@Data
public class ApiResponse<T> {

    /** 状态码 */
    private int code;

    /** 提示信息 */
    private String message;

    /** 响应数据 */
    private T data;

    protected ApiResponse() {
    }

    protected ApiResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 成功响应
     *
     * @param data 响应数据
     * @param <T>  数据类型
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }

    /**
     * 成功响应（自定义消息）
     *
     * @param message 自定义消息
     * @param data    响应数据
     * @param <T>     数据类型
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(ResultCode.SUCCESS.getCode(), message, data);
    }

    /**
     * 失败响应
     *
     * @param code    错误码
     * @param message 错误信息
     * @param <T>     数据类型
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> fail(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }

    /**
     * 失败响应（使用 ResultCode）
     *
     * @param resultCode 错误码枚举
     * @param <T>        数据类型
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> fail(ResultCode resultCode) {
        return new ApiResponse<>(resultCode.getCode(), resultCode.getMessage(), null);
    }

    /**
     * 失败响应（自定义消息覆盖枚举默认消息）
     *
     * @param resultCode 错误码枚举
     * @param message    自定义错误信息
     * @param <T>        数据类型
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> fail(ResultCode resultCode, String message) {
        return new ApiResponse<>(resultCode.getCode(), message, null);
    }
}
