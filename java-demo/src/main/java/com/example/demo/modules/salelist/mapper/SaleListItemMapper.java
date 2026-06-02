package com.example.demo.modules.salelist.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.modules.salelist.entity.SaleListItem;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 出售清单商品 Mapper
 */
@Mapper
public interface SaleListItemMapper extends BaseMapper<SaleListItem> {

    /**
     * 根据清单 ID 查询所有商品（按排序序号排序）
     */
    @Select("SELECT * FROM sale_list_items WHERE sale_list_id = #{saleListId} ORDER BY sort_order")
    List<SaleListItem> selectBySaleListId(@Param("saleListId") String saleListId);

    /**
     * 根据清单 ID 删除所有商品
     */
    @Delete("DELETE FROM sale_list_items WHERE sale_list_id = #{saleListId}")
    int deleteBySaleListId(@Param("saleListId") String saleListId);
}
