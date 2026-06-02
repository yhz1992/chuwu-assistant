package com.example.demo.common.aspect;

import com.example.demo.common.response.ApiResponse;
import com.example.demo.common.response.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 基于 Redisson 的限流切面
 * 拦截带有 @RateLimit 注解的方法，进行请求限流控制
 */
@Slf4j
@Aspect
@Component
public class RateLimiterAspect {

    private final RedissonClient redissonClient;

    public RateLimiterAspect(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    /**
     * 环绕通知：执行限流检查
     * 若超过限流阈值，直接返回限流提示，不执行原方法
     */
    @Around("@annotation(com.example.demo.common.aspect.RateLimit)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        // 获取注解参数
        RateLimit rateLimit = method.getAnnotation(RateLimit.class);
        String key = rateLimit.key();
        long limit = rateLimit.limit();
        long window = rateLimit.window();
        String message = rateLimit.message();

        // 构造 Redis 限流 key
        String rateLimitKey = "rate_limit:" + key;

        // 获取 Redisson 限流器
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(rateLimitKey);

        // 初始化限流规则（仅在首次创建时生效）
        rateLimiter.trySetRate(RateType.OVERALL, limit, window, RateIntervalUnit.SECONDS);

        // 尝试获取许可
        boolean acquired = rateLimiter.tryAcquire(1);
        if (!acquired) {
            log.warn("接口限流触发: key={}, limit={}/{}, method={}", key, limit, window, method.getName());
            // 判断方法返回类型，兼容 ApiResponse 和直接返回
            Class<?> returnType = method.getReturnType();
            if (ApiResponse.class.isAssignableFrom(returnType)) {
                return ApiResponse.fail(ResultCode.TOO_MANY_REQUESTS.getCode(), message);
            }
            // 非 ApiResponse 类型，抛出业务异常
            throw new com.example.demo.common.exception.BusinessException(
                    ResultCode.TOO_MANY_REQUESTS.getCode(), message);
        }

        log.debug("限流放行: key={}", key);
        return joinPoint.proceed();
    }
}
