package com.example.demo.common.interceptor;

import com.example.demo.common.exception.BusinessException;
import com.example.demo.common.response.ResultCode;
import com.example.demo.common.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT 认证拦截器
 * 从请求头 "Authorization: Bearer <token>" 中解析 JWT，将 user_id 存入 ThreadLocal
 */
@Slf4j
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtils jwtUtils;

    public AuthInterceptor(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // OPTIONS 预检请求直接放行（CORS 跨域）
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 从请求头获取 token
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        // 提取 Bearer token
        String token = authHeader.substring(7);

        // 验证 token 并获取 user_id
        String userId = jwtUtils.getUserIdFromAccessToken(token);
        if (userId == null) {
            throw new BusinessException(ResultCode.INVALID_TOKEN);
        }

        // 将 user_id 存入 ThreadLocal
        UserContext.setUserId(userId);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 请求完成后清除 ThreadLocal，防止内存泄漏
        UserContext.clear();
    }
}
