package com.example.demo.modules.feedback.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.modules.feedback.entity.Feedback;
import org.apache.ibatis.annotations.Mapper;

/**
 * 反馈 Mapper
 */
@Mapper
public interface FeedbackMapper extends BaseMapper<Feedback> {
}
