package com.example.demo.modules.event.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 埋点事件日志实体
 * 对应数据库表 event_logs
 */
@Data
@TableName("event_logs")
public class EventLog {

    /** 事件 ID，格式：ev_ + 雪花ID */
    @TableId
    private String id;

    /** 用户 ID（游客可为空） */
    private String userId;

    /** 事件名称 */
    private String eventName;

    /** 事件属性（JSON 对象） */
    @TableField()
    private Map<String, Object> properties;

    /** 客户端信息（JSON 对象） */
    @TableField()
    private Map<String, Object> clientInfo;

    /** 客户端 IP */
    private String ip;

    /** 创建时间 */
    private LocalDateTime createdAt;
}
