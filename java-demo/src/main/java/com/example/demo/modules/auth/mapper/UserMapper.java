package com.example.demo.modules.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.modules.auth.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户 Mapper
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据 openid 查询未删除的用户
     */
    @Select("SELECT * FROM users WHERE openid = #{openid} AND deleted_at IS NULL")
    User selectByOpenid(@Param("openid") String openid);

    /**
     * 统计用户的藏品数量
     */
    @Select("SELECT COUNT(*) FROM collection_items WHERE user_id = #{userId} AND deleted_at IS NULL")
    long countCollections(@Param("userId") String userId);

    /**
     * 统计用户的出售清单数量
     */
    @Select("SELECT COUNT(*) FROM sale_lists WHERE user_id = #{userId} AND deleted_at IS NULL")
    long countSaleLists(@Param("userId") String userId);

    /**
     * 统计用户的心愿单数量
     */
    @Select("SELECT COUNT(*) FROM wishlist_items WHERE user_id = #{userId}")
    long countWishlistItems(@Param("userId") String userId);

    /**
     * 统计用户的已售出商品数量（通过 sale_lists 联表查询）
     */
    @Select("SELECT COUNT(*) FROM sale_list_items sli INNER JOIN sale_lists sl ON sli.sale_list_id = sl.id WHERE sl.user_id = #{userId} AND sli.status = 'sold' AND sl.deleted_at IS NULL")
    long countSoldItems(@Param("userId") String userId);
}
