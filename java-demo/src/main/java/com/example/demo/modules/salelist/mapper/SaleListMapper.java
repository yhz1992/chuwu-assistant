package com.example.demo.modules.salelist.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.modules.salelist.entity.SaleList;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 出售清单 Mapper
 */
@Mapper
public interface SaleListMapper extends BaseMapper<SaleList> {

    /**
     * 按用户查询所有未删除的清单（按创建时间倒序）
     */
    @Select("SELECT * FROM sale_lists WHERE user_id = #{userId} AND deleted_at IS NULL ORDER BY created_at DESC")
    List<SaleList> selectByUserId(@Param("userId") String userId);
}
