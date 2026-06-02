package com.example.demo.modules.event.service;

import com.example.demo.common.utils.SnowflakeIdUtils;
import com.example.demo.modules.event.dto.EventTrackRequest;
import com.example.demo.modules.event.entity.EventLog;
import com.example.demo.modules.event.mapper.EventLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 埋点事件服务
 * 处理用户行为事件追踪，异步写入事件日志
 */
@Slf4j
@Service
public class EventService {

    private final EventLogMapper eventLogMapper;
    private final SnowflakeIdUtils snowflakeIdUtils;

    public EventService(EventLogMapper eventLogMapper, SnowflakeIdUtils snowflakeIdUtils) {
        this.eventLogMapper = eventLogMapper;
        this.snowflakeIdUtils = snowflakeIdUtils;
    }

    /**
     * 追踪事件（异步处理）
     *
     * @param userId  用户 ID（可为 null）
     * @param req     事件追踪请求
     * @param clientIp 客户端 IP
     */
    @Async
    public void track(String userId, EventTrackRequest req, String clientIp) {
        EventLog eventLog = new EventLog();
        eventLog.setId(snowflakeIdUtils.nextIdWithPrefix("ev_"));
        eventLog.setUserId(userId);
        eventLog.setEventName(req.getEventName());
        eventLog.setProperties(req.getProperties());
        eventLog.setIp(clientIp);
        eventLog.setCreatedAt(LocalDateTime.now());

        eventLogMapper.insert(eventLog);
        log.debug("事件追踪成功，eventName: {}, userId: {}", req.getEventName(), userId);
    }
}
