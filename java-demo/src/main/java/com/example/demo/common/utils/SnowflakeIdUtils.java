package com.example.demo.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.NetworkInterface;
import java.security.SecureRandom;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 雪花 ID 生成工具类
 * 支持添加业务前缀，如 u_（用户）、ci_（藏品）、sl_（清单）、sli_（商品）等
 *
 * 结构：1位符号位 + 41位时间戳（毫秒） + 10位工作机器ID + 12位序列号
 */
@Slf4j
@Component
public class SnowflakeIdUtils {

    /** 起始时间戳（2024-01-01 00:00:00） */
    private static final long START_EPOCH = 1704067200000L;

    /** 机器 ID 位数 */
    private static final long WORKER_ID_BITS = 10L;

    /** 序列号位数 */
    private static final long SEQUENCE_BITS = 12L;

    /** 最大机器 ID */
    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);

    /** 机器 ID 左移位数 */
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;

    /** 时间戳左移位数 */
    private static final long TIMESTAMP_LEFT_SHIFT = WORKER_ID_BITS + SEQUENCE_BITS;

    /** 序列号掩码 */
    private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);

    /** 机器 ID */
    private final long workerId;

    /** 序列号 */
    private final AtomicLong sequence = new AtomicLong(0L);

    /** 上次生成 ID 的时间戳 */
    private long lastTimestamp = -1L;

    public SnowflakeIdUtils() {
        this.workerId = initWorkerId();
        log.info("雪花ID生成器初始化完成，机器ID: {}", workerId);
    }

    /**
     * 获取下一个 ID（不带前缀）
     *
     * @return 雪花 ID
     */
    public long nextId() {
        return generateId();
    }

    /**
     * 获取带前缀的 ID（用于业务标识）
     *
     * @param prefix 业务前缀，如 "u_", "ci_", "sl_", "sli_", "wi_", "tpl_", "shr_", "fb_"
     * @return 带前缀的 ID 字符串，如 "u_1234567890123456"
     */
    public String nextIdWithPrefix(String prefix) {
        return prefix + generateId();
    }

    /**
     * 生成下一个 ID
     */
    private synchronized long generateId() {
        long currentTimestamp = System.currentTimeMillis();

        // 时钟回拨处理
        if (currentTimestamp < lastTimestamp) {
            long offset = lastTimestamp - currentTimestamp;
            if (offset > 5000) {
                throw new RuntimeException("时钟回拨超过5秒，拒绝生成ID");
            }
            // 轻微回拨，等待时间追上
            while (currentTimestamp < lastTimestamp) {
                currentTimestamp = System.currentTimeMillis();
            }
        }

        if (currentTimestamp == lastTimestamp) {
            long seq = sequence.incrementAndGet() & SEQUENCE_MASK;
            if (seq == 0) {
                // 当前毫秒序列号已用完，等待到下一毫秒
                while (currentTimestamp <= lastTimestamp) {
                    currentTimestamp = System.currentTimeMillis();
                }
            }
            sequence.set(seq);
        } else {
            sequence.set(0L);
        }

        lastTimestamp = currentTimestamp;

        return ((currentTimestamp - START_EPOCH) << TIMESTAMP_LEFT_SHIFT)
                | (workerId << WORKER_ID_SHIFT)
                | sequence.get();
    }

    /**
     * 初始化机器 ID，通过 MAC 地址计算
     */
    private static long initWorkerId() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            if (interfaces != null) {
                while (interfaces.hasMoreElements()) {
                    NetworkInterface ni = interfaces.nextElement();
                    byte[] mac = ni.getHardwareAddress();
                    if (mac != null && mac.length > 0) {
                        long id = ((long) mac[mac.length - 1] & 0xFF)
                                | (((long) mac[mac.length - 2] & 0xFF) << 8);
                        return id & MAX_WORKER_ID;
                    }
                }
            }
        } catch (Exception e) {
            log.warn("获取MAC地址失败，使用随机数作为机器ID", e);
        }
        // 兜底：使用随机数
        return new SecureRandom().nextInt((int) MAX_WORKER_ID);
    }
}
