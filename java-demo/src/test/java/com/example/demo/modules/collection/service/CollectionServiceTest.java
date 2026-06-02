package com.example.demo.modules.collection.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.demo.common.exception.BusinessException;
import com.example.demo.common.utils.SnowflakeIdUtils;
import com.example.demo.modules.collection.dto.CollectionCreateRequest;
import com.example.demo.modules.collection.dto.CollectionDetailResponse;
import com.example.demo.modules.collection.dto.CollectionUpdateRequest;
import com.example.demo.modules.collection.entity.CollectionItem;
import com.example.demo.modules.collection.mapper.CollectionMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 收藏服务单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("收藏服务测试")
class CollectionServiceTest {

    @Mock
    private CollectionMapper collectionMapper;

    @Mock
    private SnowflakeIdUtils snowflakeIdUtils;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private CollectionService collectionService;

    private static final String USER_ID = "u_test_001";
    private static final String COLLECTION_ID = "ci_001";

    private CollectionItem createTestItem() {
        CollectionItem item = new CollectionItem();
        item.setId(COLLECTION_ID);
        item.setUserId(USER_ID);
        item.setName("测试谷子");
        item.setImages("[\"https://example.com/img1.jpg\",\"https://example.com/img2.jpg\"]");
        item.setCoverImage("https://example.com/img1.jpg");
        item.setWorkName("作品名");
        item.setCharacterName("角色名");
        item.setItemType("badge");
        item.setPurchasePrice(new BigDecimal("59.00"));
        item.setQuantity(1);
        item.setStatus("arrived");
        item.setIsForSale(false);
        item.setAuditStatus("pending");
        item.setSearchText("测试谷子 作品名 角色名");
        item.setSortIndex(System.currentTimeMillis());
        item.setVersion(1);
        item.setCreatedAt(LocalDateTime.now());
        item.setUpdatedAt(LocalDateTime.now());
        return item;
    }

    @Nested
    @DisplayName("查询藏品详情")
    class GetDetail {

        @Test
        @DisplayName("查询存在的藏品应返回详情")
        void shouldReturnDetailForExistingItem() {
            CollectionItem item = createTestItem();
            when(collectionMapper.selectById(COLLECTION_ID)).thenReturn(item);

            CollectionDetailResponse result = collectionService.getDetail(USER_ID, COLLECTION_ID);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(COLLECTION_ID);
            assertThat(result.getName()).isEqualTo("测试谷子");
            assertThat(result.getImages()).hasSize(2);
            assertThat(result.getImages().get(0)).isEqualTo("https://example.com/img1.jpg");
            assertThat(result.getWorkName()).isEqualTo("作品名");
        }

        @Test
        @DisplayName("查询不存在的藏品应抛出异常")
        void shouldThrowExceptionForNonExistentItem() {
            when(collectionMapper.selectById("nonexistent")).thenReturn(null);

            assertThatThrownBy(() -> collectionService.getDetail(USER_ID, "nonexistent"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("藏品不存在");
        }

        @Test
        @DisplayName("查询已删除的藏品应抛出异常")
        void shouldThrowExceptionForDeletedItem() {
            CollectionItem item = createTestItem();
            item.setDeletedAt(LocalDateTime.now());
            when(collectionMapper.selectById(COLLECTION_ID)).thenReturn(item);

            assertThatThrownBy(() -> collectionService.getDetail(USER_ID, COLLECTION_ID))
                .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("查询其他用户的藏品应抛出异常")
        void shouldThrowExceptionForOtherUserItem() {
            CollectionItem item = createTestItem();
            when(collectionMapper.selectById(COLLECTION_ID)).thenReturn(item);

            assertThatThrownBy(() -> collectionService.getDetail("u_other_user", COLLECTION_ID))
                .isInstanceOf(BusinessException.class);
        }
    }

    @Nested
    @DisplayName("创建藏品")
    class Create {

        @Test
        @DisplayName("创建藏品成功应返回新 ID")
        void shouldCreateCollectionSuccessfully() {
            var request = new CollectionCreateRequest();
            request.setName("新谷子");
            request.setImages(List.of("https://example.com/new.jpg"));
            request.setWorkName("新作品");
            request.setCharacterName("新角色");
            request.setItemType("acrylic_stand");
            request.setPurchasePrice(new BigDecimal("99.00"));
            request.setQuantity(1);
            request.setStatus("arrived");

            when(snowflakeIdUtils.nextIdWithPrefix("ci_")).thenReturn(COLLECTION_ID);
            when(collectionMapper.selectCount(any(QueryWrapper.class))).thenReturn(10L);

            Map<String, String> result = collectionService.create(USER_ID, request);

            assertThat(result).containsEntry("id", COLLECTION_ID);

            ArgumentCaptor<CollectionItem> captor = ArgumentCaptor.forClass(CollectionItem.class);
            verify(collectionMapper).insert(captor.capture());
            CollectionItem inserted = captor.getValue();
            assertThat(inserted.getName()).isEqualTo("新谷子");
            assertThat(inserted.getUserId()).isEqualTo(USER_ID);
            assertThat(inserted.getWorkName()).isEqualTo("新作品");
        }

        @Test
        @DisplayName("超过免费上限应抛出异常")
        void shouldThrowExceptionWhenExceedingFreeLimit() {
            var request = new CollectionCreateRequest();
            request.setName("新谷子");
            request.setImages(List.of("https://example.com/new.jpg"));

            when(collectionMapper.selectCount(any(QueryWrapper.class))).thenReturn(50L);

            assertThatThrownBy(() -> collectionService.create(USER_ID, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("免费版最多添加50件藏品");
        }

        @Test
        @DisplayName("创建藏品时封面图默认取第一张")
        void shouldSetFirstImageAsCover() {
            var request = new CollectionCreateRequest();
            request.setName("新谷子");
            request.setImages(List.of("img1.jpg", "img2.jpg", "img3.jpg"));
            request.setQuantity(1);

            when(snowflakeIdUtils.nextIdWithPrefix("ci_")).thenReturn(COLLECTION_ID);
            when(collectionMapper.selectCount(any(QueryWrapper.class))).thenReturn(0L);

            collectionService.create(USER_ID, request);

            ArgumentCaptor<CollectionItem> captor = ArgumentCaptor.forClass(CollectionItem.class);
            verify(collectionMapper).insert(captor.capture());
            assertThat(captor.getValue().getCoverImage()).isEqualTo("img1.jpg");
        }
    }

    @Nested
    @DisplayName("更新藏品")
    class Update {

        @Test
        @DisplayName("更新藏品字段应生效")
        void shouldUpdateFieldsCorrectly() {
            CollectionItem item = createTestItem();
            when(collectionMapper.selectById(COLLECTION_ID)).thenReturn(item);

            var request = new CollectionUpdateRequest();
            request.setName("改名后的谷子");
            request.setStatus("for_sale");

            Map<String, Object> result = collectionService.update(USER_ID, COLLECTION_ID, request);

            assertThat(result).containsKey("id");

            ArgumentCaptor<CollectionItem> captor = ArgumentCaptor.forClass(CollectionItem.class);
            verify(collectionMapper).updateById(captor.capture());
            CollectionItem updated = captor.getValue();
            assertThat(updated.getName()).isEqualTo("改名后的谷子");
            assertThat(updated.getStatus()).isEqualTo("for_sale");
            assertThat(updated.getIsForSale()).isTrue();
        }

        @Test
        @DisplayName("更新不存在的藏品应抛出异常")
        void shouldThrowExceptionForNonExistentItem() {
            when(collectionMapper.selectById("nonexistent")).thenReturn(null);

            assertThatThrownBy(() ->
                collectionService.update(USER_ID, "nonexistent", new CollectionUpdateRequest())
            ).isInstanceOf(BusinessException.class);
        }
    }

    @Nested
    @DisplayName("删除藏品")
    class Delete {

        @Test
        @DisplayName("逻辑删除应设置 deletedAt")
        void shouldSoftDeleteItem() {
            CollectionItem item = createTestItem();
            when(collectionMapper.selectById(COLLECTION_ID)).thenReturn(item);

            Boolean result = collectionService.delete(USER_ID, COLLECTION_ID);

            assertThat(result).isTrue();

            ArgumentCaptor<CollectionItem> captor = ArgumentCaptor.forClass(CollectionItem.class);
            verify(collectionMapper).updateById(captor.capture());
            assertThat(captor.getValue().getDeletedAt()).isNotNull();
        }

        @Test
        @DisplayName("重复删除应抛出异常")
        void shouldThrowExceptionOnDoubleDelete() {
            CollectionItem item = createTestItem();
            item.setDeletedAt(LocalDateTime.now());
            when(collectionMapper.selectById(COLLECTION_ID)).thenReturn(item);

            assertThatThrownBy(() -> collectionService.delete(USER_ID, COLLECTION_ID))
                .isInstanceOf(BusinessException.class);
        }
    }

    @Nested
    @DisplayName("images 字段序列化")
    class ImagesSerialization {

        @Test
        @DisplayName("详情查询应正确解析 images 为数组")
        void shouldParseImagesAsArray() {
            CollectionItem item = createTestItem();
            item.setImages("[\"a.jpg\",\"b.jpg\",\"c.jpg\"]");
            when(collectionMapper.selectById(COLLECTION_ID)).thenReturn(item);

            CollectionDetailResponse result = collectionService.getDetail(USER_ID, COLLECTION_ID);

            assertThat(result.getImages()).isInstanceOf(List.class);
            assertThat(result.getImages()).containsExactly("a.jpg", "b.jpg", "c.jpg");
        }

        @Test
        @DisplayName("空 images 应返回空数组而不是 null")
        void shouldReturnEmptyListForNullImages() {
            CollectionItem item = createTestItem();
            item.setImages(null);
            when(collectionMapper.selectById(COLLECTION_ID)).thenReturn(item);

            CollectionDetailResponse result = collectionService.getDetail(USER_ID, COLLECTION_ID);

            assertThat(result.getImages()).isNotNull();
            assertThat(result.getImages()).isEmpty();
        }
    }
}
