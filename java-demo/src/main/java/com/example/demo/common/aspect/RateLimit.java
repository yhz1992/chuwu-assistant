package com.example.demo.common.aspect;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 限流注解
 * 配合 RateLimiterAspect 实现对接口的限流控制
 *
 * <p>使用示例：
 * <pre>
 * &#64;RateLimit(key = "sendSms", limit = 10, window = 60)
 * &#64;PostMapping("/send-sms")
 * public ApiResponse&lt;Void&gt; sendSms() { ... }
 * </pre>
 * 上述表示 60 秒内最多允许 10 次请求
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {

    /**
     * 限流键名，用于构造 Redis key
     * 最终 key 格式为 "rate_limit:{key}:{userId或IP}"
     */
    String key() default "default";

    /**
     * 时间窗口内允许的最大请求数
     */
    long limit() default 60;

    /**
     * 时间窗口大小（单位：秒）
     */
    long window() default 60;

    /**
     * 超出限流后的提示信息
     */
    String message() default "操作过于频繁，请稍后再试";
}
