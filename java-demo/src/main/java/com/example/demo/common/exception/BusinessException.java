package com.example.demo.common.exception;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * 业务异常，含错误码和错误信息
 */
@Slf4j
@Getter
public class BusinessException extends RuntimeException {

    /** 业务错误码 */
    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 从 ResultCode 枚举构造业务异常
     *
     * @param resultCode 错误码枚举
     */
    public BusinessException(com.example.demo.common.response.ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    /**
     * 从 ResultCode 枚举构造业务异常，自定义错误信息覆盖枚举默认消息
     *
     * @param resultCode 错误码枚举
     * @param message    自定义错误信息
     */
    public BusinessException(com.example.demo.common.response.ResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
    }
}
