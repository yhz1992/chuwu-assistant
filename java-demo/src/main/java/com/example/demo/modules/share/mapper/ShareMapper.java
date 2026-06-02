package com.example.demo.modules.share.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.modules.share.entity.Share;
import org.apache.ibatis.annotations.Mapper;

/**
 * 分享记录 Mapper
 */
@Mapper
public interface ShareMapper extends BaseMapper<Share> {
}
