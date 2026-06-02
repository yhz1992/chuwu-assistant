package com.example.demo.modules.collection.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.modules.collection.entity.CollectionItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 藏品 Mapper
 */
@Mapper
public interface CollectionMapper extends BaseMapper<CollectionItem> {

    /**
     * 按用户分页查询藏品（已过滤逻辑删除）
     *
     * @param page   分页参数
     * @param userId 用户 ID
     * @param wrapper 查询条件
     * @return 分页结果
     */
    @Select("SELECT * FROM collection_items ${ew.customSqlSegment}")
    IPage<CollectionItem> selectPageByUser(Page<CollectionItem> page, @Param("userId") String userId, @Param(Constants.WRAPPER) Wrapper<CollectionItem> wrapper);
}
