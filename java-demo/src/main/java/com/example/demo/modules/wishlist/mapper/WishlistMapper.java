package com.example.demo.modules.wishlist.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.modules.wishlist.entity.WishlistItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 心愿单 Mapper
 */
@Mapper
public interface WishlistMapper extends BaseMapper<WishlistItem> {
}
