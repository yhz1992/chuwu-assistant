package com.example.demo.modules.event.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.modules.event.entity.EventLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 埋点事件日志 Mapper
 */
@Mapper
public interface EventLogMapper extends BaseMapper<EventLog> {
}
