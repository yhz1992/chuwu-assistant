package com.example.demo.modules.user.service;

import com.example.demo.common.exception.BusinessException;
import com.example.demo.common.response.ResultCode;
import com.example.demo.modules.auth.entity.User;
import com.example.demo.modules.auth.mapper.UserMapper;
import com.example.demo.modules.user.dto.UpdateUserRequest;
import com.example.demo.modules.user.dto.UserMeResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 用户服务
 * 处理用户信息查询、更新等业务逻辑
 */
@Slf4j
@Service
public class UserService {

    private final UserMapper userMapper;

    public UserService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public UserMeResponse getMe(String userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        UserMeResponse.UserStats stats = UserMeResponse.UserStats.builder()
                .collectionCount(userMapper.countCollections(userId))
                .saleListCount(userMapper.countSaleLists(userId))
                .wishlistCount(userMapper.countWishlistItems(userId))
                .soldCount(userMapper.countSoldItems(userId))
                .build();

        return UserMeResponse.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .stats(stats)
                .createdAt(user.getCreatedAt())
                .build();
    }

    @Transactional(rollbackFor = Exception.class)
    public UserMeResponse updateMe(String userId, UpdateUserRequest req) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        if (req.getNickname() != null) user.setNickname(req.getNickname());
        if (req.getAvatar() != null) user.setAvatar(req.getAvatar());
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
        log.info("用户信息已更新，userId: {}", userId);

        return getMe(userId);
    }
}
