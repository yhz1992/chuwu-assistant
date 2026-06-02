package com.example.demo.modules.home.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.demo.modules.auth.mapper.UserMapper;
import com.example.demo.modules.collection.entity.CollectionItem;
import com.example.demo.modules.collection.mapper.CollectionMapper;
import com.example.demo.modules.home.dto.HomeOverviewResponse;
import com.example.demo.modules.salelist.entity.SaleList;
import com.example.demo.modules.salelist.mapper.SaleListMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

/**
 * 首页服务单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("首页服务测试")
class HomeServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private CollectionMapper collectionMapper;

    @Mock
    private SaleListMapper saleListMapper;

    @InjectMocks
    private HomeService homeService;

    private static final String USER_ID = "u_test_001";

    @BeforeEach
    void setUp() {
        // 统计数据
        when(userMapper.countCollections(USER_ID)).thenReturn(10L);
        when(userMapper.countSaleLists(USER_ID)).thenReturn(5L);
        when(userMapper.countSoldItems(USER_ID)).thenReturn(3L);
        when(userMapper.countWishlistItems(USER_ID)).thenReturn(8L);
    }

    @Nested
    @DisplayName("首页概览")
    class GetOverview {

        @Test
        @DisplayName("应返回正确的统计数据")
        void shouldReturnCorrectStats() {
            // Arrange
            when(collectionMapper.selectList(any(QueryWrapper.class))).thenReturn(List.of());
            when(saleListMapper.selectList(any(QueryWrapper.class))).thenReturn(List.of());

            // Act
            HomeOverviewResponse response = homeService.getOverview(USER_ID);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getStats()).isNotNull();
            assertThat(response.getStats().getTotalCollections()).isEqualTo(10L);
            assertThat(response.getStats().getTotalSaleLists()).isEqualTo(5L);
            assertThat(response.getStats().getTotalSoldItems()).isEqualTo(3L);
            assertThat(response.getStats().getWishlistCount()).isEqualTo(8L);
        }

        @Test
        @DisplayName("最近清单应包含 totalPrice 和 createdAt（修复 B1）")
        void shouldReturnTotalPriceAndCreatedAtInRecentSaleLists() {
            // Arrange
            when(collectionMapper.selectList(any(QueryWrapper.class))).thenReturn(List.of());

            SaleList saleList = new SaleList();
            saleList.setId("sl_001");
            saleList.setUserId(USER_ID);
            saleList.setTitle("测试出物清单");
            saleList.setStatus("draft");
            saleList.setTotalCount(5);
            saleList.setTotalPrice(new BigDecimal("299.50"));
            saleList.setCreatedAt(LocalDateTime.of(2026, 5, 20, 14, 30));
            when(saleListMapper.selectList(any(QueryWrapper.class))).thenReturn(List.of(saleList));

            // Act
            HomeOverviewResponse response = homeService.getOverview(USER_ID);

            // Assert
            assertThat(response.getRecentSaleLists()).hasSize(1);
            HomeOverviewResponse.RecentSaleList recent = response.getRecentSaleLists().get(0);
            assertThat(recent.getId()).isEqualTo("sl_001");
            assertThat(recent.getTitle()).isEqualTo("测试出物清单");
            assertThat(recent.getTotalCount()).isEqualTo(5);
            // 关键断言：totalPrice 和 createdAt 不应为 null
            assertThat(recent.getTotalPrice()).isNotNull();
            assertThat(recent.getTotalPrice()).isEqualByComparingTo(new BigDecimal("299.50"));
            assertThat(recent.getCreatedAt()).isNotNull();
            assertThat(recent.getCreatedAt()).isEqualTo("2026-05-20T14:30:00");
        }

        @Test
        @DisplayName("最近收藏应包含完整字段")
        void shouldReturnCompleteRecentCollections() {
            // Arrange
            CollectionItem item = new CollectionItem();
            item.setId("ci_001");
            item.setName("稀有谷子");
            item.setCoverImage("https://example.com/cover.jpg");
            item.setStatus("arrived");
            item.setItemType("badge");
            item.setWorkName("热门作品");
            item.setCharacterName("主角");

            when(collectionMapper.selectList(any(QueryWrapper.class))).thenReturn(List.of(item));
            when(saleListMapper.selectList(any(QueryWrapper.class))).thenReturn(List.of());

            // Act
            HomeOverviewResponse response = homeService.getOverview(USER_ID);

            // Assert
            assertThat(response.getRecentCollections()).hasSize(1);
            HomeOverviewResponse.RecentCollection recent = response.getRecentCollections().get(0);
            assertThat(recent.getName()).isEqualTo("稀有谷子");
            assertThat(recent.getCoverImage()).isEqualTo("https://example.com/cover.jpg");
            assertThat(recent.getWorkName()).isEqualTo("热门作品");
            assertThat(recent.getCharacterName()).isEqualTo("主角");
        }

        @Test
        @DisplayName("空数据时应返回空列表")
        void shouldReturnEmptyListsForNewUser() {
            // Arrange
            when(userMapper.countCollections(USER_ID)).thenReturn(0L);
            when(userMapper.countSaleLists(USER_ID)).thenReturn(0L);
            when(userMapper.countSoldItems(USER_ID)).thenReturn(0L);
            when(userMapper.countWishlistItems(USER_ID)).thenReturn(0L);
            when(collectionMapper.selectList(any(QueryWrapper.class))).thenReturn(List.of());
            when(saleListMapper.selectList(any(QueryWrapper.class))).thenReturn(List.of());

            // Act
            HomeOverviewResponse response = homeService.getOverview(USER_ID);

            // Assert
            assertThat(response.getStats().getTotalCollections()).isZero();
            assertThat(response.getRecentCollections()).isEmpty();
            assertThat(response.getRecentSaleLists()).isEmpty();
        }
    }
}
