package com.example.demo.modules.salelist.service;

import com.example.demo.common.exception.BusinessException;
import com.example.demo.common.utils.SnowflakeIdUtils;
import com.example.demo.modules.collection.entity.CollectionItem;
import com.example.demo.modules.collection.mapper.CollectionMapper;
import com.example.demo.modules.salelist.dto.*;
import com.example.demo.modules.salelist.entity.SaleList;
import com.example.demo.modules.salelist.entity.SaleListItem;
import com.example.demo.modules.salelist.mapper.SaleListItemMapper;
import com.example.demo.modules.salelist.mapper.SaleListMapper;
import com.example.demo.modules.share.entity.Share;
import com.example.demo.modules.share.mapper.ShareMapper;
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
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 出物清单服务单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("出物清单服务测试")
class SaleListServiceTest {

    @Mock
    private SaleListMapper saleListMapper;

    @Mock
    private SaleListItemMapper saleListItemMapper;

    @Mock
    private CollectionMapper collectionMapper;

    @Mock
    private ShareMapper shareMapper;

    @Mock
    private SnowflakeIdUtils snowflakeIdUtils;

    @Mock
    private TextGenerationService textGenerationService;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private SaleListService saleListService;

    private static final String USER_ID = "u_test_001";
    private static final String SALE_LIST_ID = "sl_001";
    private static final String COLLECTION_ID = "ci_001";

    private SaleList createTestSaleList() {
        SaleList saleList = new SaleList();
        saleList.setId(SALE_LIST_ID);
        saleList.setUserId(USER_ID);
        saleList.setTitle("测试清单");
        saleList.setDescription("测试描述");
        saleList.setStatus("draft");
        saleList.setTotalCount(2);
        saleList.setTotalPrice(new BigDecimal("150.00"));
        saleList.setGeneratedImage(null);
        saleList.setGeneratedPages(new ArrayList<>());
        saleList.setShareId(null);
        saleList.setTradeRule("{\"freeShipping\":true}");
        saleList.setWatermark(true);
        saleList.setVersion(1);
        saleList.setCreatedAt(LocalDateTime.now());
        saleList.setUpdatedAt(LocalDateTime.now());
        return saleList;
    }

    private CollectionItem createTestCollectionItem() {
        CollectionItem item = new CollectionItem();
        item.setId(COLLECTION_ID);
        item.setUserId(USER_ID);
        item.setName("测试谷子");
        item.setImages("[\"https://example.com/img1.jpg\"]");
        item.setCoverImage("https://example.com/img1.jpg");
        item.setWorkName("作品A");
        item.setCharacterName("角色X");
        item.setItemType("badge");
        item.setPurchasePrice(new BigDecimal("50.00"));
        item.setQuantity(2);
        item.setStatus("arrived");
        item.setIsForSale(false);
        item.setVersion(1);
        return item;
    }

    @BeforeEach
    void setUp() {
        lenient().when(snowflakeIdUtils.nextIdWithPrefix("sl_")).thenReturn(SALE_LIST_ID);
        lenient().when(snowflakeIdUtils.nextIdWithPrefix("shr_")).thenReturn("shr_001");
        lenient().when(snowflakeIdUtils.nextIdWithPrefix("sli_")).thenReturn("sli_001", "sli_002");
    }

    @Nested
    @DisplayName("创建清单 + 商品快照 images 字段")
    class CreateWithSnapshot {

        @Test
        @DisplayName("创建清单时快照 images 应为数组而非字符串（修复 B2）")
        void shouldStoreImagesAsArrayInSnapshot() {
            SaleListCreateRequest request = new SaleListCreateRequest();
            request.setTitle("测试清单");
            request.setDescription("描述");

            SaleListItemDTO itemDTO = new SaleListItemDTO();
            itemDTO.setCollectionItemId(COLLECTION_ID);
            itemDTO.setPrice(new BigDecimal("80.00"));
            itemDTO.setQuantity(1);
            request.setItems(List.of(itemDTO));

            CollectionItem collectionItem = createTestCollectionItem();
            when(collectionMapper.selectById(COLLECTION_ID)).thenReturn(collectionItem);
            when(saleListMapper.selectById(SALE_LIST_ID)).thenReturn(createTestSaleList());

            Map<String, String> result = saleListService.create(USER_ID, request);

            assertThat(result).containsEntry("id", SALE_LIST_ID);

            // 验证快照中的 images 是解析后的列表，不是原始字符串
            ArgumentCaptor<SaleListItem> captor = ArgumentCaptor.forClass(SaleListItem.class);
            verify(saleListItemMapper).insert(captor.capture());
            SaleListItem inserted = captor.getValue();

            String snapshot = inserted.getCollectionSnapshot();
            assertThat(snapshot).isNotNull();

            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> snapshotMap = objectMapper.readValue(snapshot, Map.class);
                Object images = snapshotMap.get("images");
                // 关键断言：images 应该是 List，不是 String
                assertThat(images).isInstanceOf(List.class);
                @SuppressWarnings("unchecked")
                List<String> imageList = (List<String>) images;
                assertThat(imageList).containsExactly("https://example.com/img1.jpg");
            } catch (Exception e) {
                throw new AssertionError("快照 JSON 无法解析: " + e.getMessage(), e);
            }

            assertThat(inserted.getName()).isEqualTo("测试谷子");
            assertThat(inserted.getCollectionItemId()).isEqualTo(COLLECTION_ID);
        }
    }

    @Nested
    @DisplayName("生成清单")
    class Generate {

        @Test
        @DisplayName("空清单生成应抛出异常")
        void shouldThrowExceptionForEmptySaleList() {
            SaleList saleList = createTestSaleList();
            when(saleListMapper.selectById(SALE_LIST_ID)).thenReturn(saleList);
            when(saleListItemMapper.selectBySaleListId(SALE_LIST_ID)).thenReturn(Collections.emptyList());

            SaleListGenerateRequest request = new SaleListGenerateRequest();
            request.setTemplateId("tpl_001");

            assertThatThrownBy(() -> saleListService.generate(USER_ID, SALE_LIST_ID, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("清单中没有商品");
        }

        @Test
        @DisplayName("生成成功应创建分享记录并更新清单状态")
        void shouldCreateShareAndUpdateStatus() {
            SaleList saleList = createTestSaleList();
            when(saleListMapper.selectById(SALE_LIST_ID)).thenReturn(saleList);

            SaleListItem item = new SaleListItem();
            item.setId("sli_001");
            item.setSaleListId(SALE_LIST_ID);
            item.setName("测试商品");
            item.setPrice(new BigDecimal("100.00"));
            when(saleListItemMapper.selectBySaleListId(SALE_LIST_ID)).thenReturn(List.of(item));

            Map<String, String> texts = Map.of("xianyu", "闲鱼文案", "xiaohongshu", "小红书文案");
            when(textGenerationService.generateAll(any(), any())).thenReturn(texts);

            SaleListGenerateRequest request = new SaleListGenerateRequest();
            request.setTemplateId("tpl_001");
            request.setWatermark(true);

            SaleListGenerateResponse response = saleListService.generate(USER_ID, SALE_LIST_ID, request);

            assertThat(response).isNotNull();
            assertThat(response.getSaleListId()).isEqualTo(SALE_LIST_ID);
            assertThat(response.getShareId()).isEqualTo("shr_001");
            assertThat(response.getTexts()).containsKeys("xianyu", "xiaohongshu");

            verify(shareMapper).insert(any(Share.class));

            ArgumentCaptor<SaleList> captor = ArgumentCaptor.forClass(SaleList.class);
            verify(saleListMapper).updateById(captor.capture());
            SaleList updated = captor.getValue();
            assertThat(updated.getStatus()).isEqualTo("generated");
            assertThat(updated.getShareId()).isEqualTo("shr_001");
        }
    }

    @Nested
    @DisplayName("所属权校验")
    class OwnershipValidation {

        @Test
        @DisplayName("访问其他用户的清单应抛出异常")
        void shouldThrowExceptionForOtherUserSaleList() {
            SaleList saleList = createTestSaleList();
            saleList.setUserId("u_other_user");
            when(saleListMapper.selectById(SALE_LIST_ID)).thenReturn(saleList);

            assertThatThrownBy(() ->
                saleListService.getDetail(USER_ID, SALE_LIST_ID)
            ).isInstanceOf(BusinessException.class);
        }
    }

    @Nested
    @DisplayName("删除清单")
    class Delete {

        @Test
        @DisplayName("逻辑删除清单应设置 deletedAt")
        void shouldSoftDeleteSaleList() {
            SaleList saleList = createTestSaleList();
            when(saleListMapper.selectById(SALE_LIST_ID)).thenReturn(saleList);
            when(saleListItemMapper.deleteBySaleListId(SALE_LIST_ID)).thenReturn(3);

            Boolean result = saleListService.delete(USER_ID, SALE_LIST_ID);

            assertThat(result).isTrue();

            ArgumentCaptor<SaleList> captor = ArgumentCaptor.forClass(SaleList.class);
            verify(saleListMapper).updateById(captor.capture());
            assertThat(captor.getValue().getDeletedAt()).isNotNull();

            verify(saleListItemMapper).deleteBySaleListId(SALE_LIST_ID);
        }
    }
}
