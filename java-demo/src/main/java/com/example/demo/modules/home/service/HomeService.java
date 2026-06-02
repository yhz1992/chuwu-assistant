package com.example.demo.modules.home.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.demo.modules.auth.entity.User;
import com.example.demo.modules.auth.mapper.UserMapper;
import com.example.demo.modules.collection.entity.CollectionItem;
import com.example.demo.modules.collection.mapper.CollectionMapper;
import com.example.demo.modules.home.dto.HomeOverviewResponse;
import com.example.demo.modules.salelist.entity.SaleList;
import com.example.demo.modules.salelist.mapper.SaleListMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 首页服务
 */
@Slf4j
@Service
public class HomeService {

    private final UserMapper userMapper;
    private final CollectionMapper collectionMapper;
    private final SaleListMapper saleListMapper;

    public HomeService(UserMapper userMapper, CollectionMapper collectionMapper, SaleListMapper saleListMapper) {
        this.userMapper = userMapper;
        this.collectionMapper = collectionMapper;
        this.saleListMapper = saleListMapper;
    }

    /**
     * 获取首页概览数据
     *
     * @param userId 用户 ID
     * @return 首页概览响应
     */
    public HomeOverviewResponse getOverview(String userId) {
        // 统计数据
        long totalCollections = userMapper.countCollections(userId);
        long totalSaleLists = userMapper.countSaleLists(userId);
        long totalSoldItems = userMapper.countSoldItems(userId);
        long wishlistCount = userMapper.countWishlistItems(userId);

        HomeOverviewResponse.HomeStats stats = HomeOverviewResponse.HomeStats.builder()
                .totalCollections(totalCollections)
                .totalSaleLists(totalSaleLists)
                .totalSoldItems(totalSoldItems)
                .wishlistCount(wishlistCount)
                .build();

        // 最近 6 条收藏
        List<HomeOverviewResponse.RecentCollection> recentCollections = collectionMapper.selectList(
                new QueryWrapper<CollectionItem>()
                        .eq("user_id", userId)
                        .isNull("deleted_at")
                        .orderByDesc("created_at")
                        .last("LIMIT 6")
        ).stream().map(item -> HomeOverviewResponse.RecentCollection.builder()
                .id(item.getId())
                .name(item.getName())
                .coverImage(item.getCoverImage())
                .status(item.getStatus())
                .itemType(item.getItemType())
                .workName(item.getWorkName())
                .characterName(item.getCharacterName())
                .build()
        ).collect(Collectors.toList());

        // 最近 3 条清单
        List<HomeOverviewResponse.RecentSaleList> recentSaleLists = saleListMapper.selectList(
                new QueryWrapper<SaleList>()
                        .eq("user_id", userId)
                        .isNull("deleted_at")
                        .orderByDesc("created_at")
                        .last("LIMIT 3")
        ).stream().map(saleList -> HomeOverviewResponse.RecentSaleList.builder()
                .id(saleList.getId())
                .title(saleList.getTitle())
                .status(saleList.getStatus())
                .totalCount(saleList.getTotalCount())
                .totalPrice(saleList.getTotalPrice())
                .createdAt(saleList.getCreatedAt())
                .build()
        ).collect(Collectors.toList());

        return HomeOverviewResponse.builder()
                .stats(stats)
                .recentCollections(recentCollections)
                .recentSaleLists(recentSaleLists)
                .build();
    }
}
