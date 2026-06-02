package com.example.demo.common.interceptor;

import lombok.extern.slf4j.Slf4j;

/**
 * 用户上下文 ThreadLocal 工具类
 */
@Slf4j
public class UserContext {

    private static final ThreadLocal<String> USER_ID_HOLDER = new ThreadLocal<>();

    public static void setUserId(String userId) {
        USER_ID_HOLDER.set(userId);
    }

    public static String getUserId() {
        return USER_ID_HOLDER.get();
    }

    public static void clear() {
        USER_ID_HOLDER.remove();
    }
}
