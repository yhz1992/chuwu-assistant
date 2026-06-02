package com.example.demo.modules.share.service;

import com.example.demo.common.exception.BusinessException;
import com.example.demo.common.response.ResultCode;
import com.example.demo.modules.salelist.entity.SaleList;
import com.example.demo.modules.salelist.entity.SaleListItem;
import com.example.demo.modules.salelist.mapper.SaleListItemMapper;
import com.example.demo.modules.salelist.mapper.SaleListMapper;
import com.example.demo.modules.share.entity.Share;
import com.example.demo.modules.share.mapper.ShareMapper;
import com.example.demo.modules.auth.entity.User;
import com.example.demo.modules.auth.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ShareService {

    private final ShareMapper shareMapper;
    private final SaleListMapper saleListMapper;
    private final SaleListItemMapper saleListItemMapper;
    private final UserMapper userMapper;

    public ShareService(ShareMapper shareMapper, SaleListMapper saleListMapper,
                        SaleListItemMapper saleListItemMapper, UserMapper userMapper) {
        this.shareMapper = shareMapper;
        this.saleListMapper = saleListMapper;
        this.saleListItemMapper = saleListItemMapper;
        this.userMapper = userMapper;
    }

    public Map<String, Object> getShareDetail(String shareId) {
        Share share = shareMapper.selectById(shareId);
        if (share == null || (share.getRevokedAt() != null)) {
            throw new BusinessException(ResultCode.SHARE_NOT_FOUND);
        }

        SaleList saleList = saleListMapper.selectById(share.getSaleListId());
        if (saleList == null) {
            throw new BusinessException(ResultCode.SALE_LIST_NOT_FOUND);
        }

        User user = userMapper.selectById(share.getUserId());

        List<SaleListItem> items = saleListItemMapper.selectBySaleListId(saleList.getId());
        List<Map<String, Object>> itemList = items.stream().map(item -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("name", item.getName());
            map.put("image", item.getImage());
            map.put("price", item.getPrice());
            map.put("status", item.getStatus());
            map.put("flawNote", item.getFlawNote());
            return map;
        }).collect(Collectors.toList());

        Map<String, Object> publisher = new LinkedHashMap<>();
        publisher.put("nickname", user != null ? user.getNickname() : "匿名用户");
        publisher.put("avatar", user != null ? user.getAvatar() : "");

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("shareId", share.getId());
        result.put("title", saleList.getTitle());
        result.put("publisher", publisher);
        result.put("generatedImage", saleList.getGeneratedImage());
        result.put("items", itemList);
        result.put("disclaimer", "本页面仅为用户自助生成的信息展示，交易请自行确认。");
        result.put("createdAt", saleList.getCreatedAt());
        return result;
    }
}
