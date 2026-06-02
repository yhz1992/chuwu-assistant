package com.example.demo.modules.auth.service;

import com.example.demo.common.exception.BusinessException;
import com.example.demo.common.utils.JwtUtils;
import com.example.demo.common.utils.SnowflakeIdUtils;
import com.example.demo.modules.auth.dto.WechatLoginRequest;
import com.example.demo.modules.auth.dto.WechatLoginResponse;
import com.example.demo.modules.auth.entity.User;
import com.example.demo.modules.auth.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

/**
 * 认证服务单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("认证服务测试")
class AuthServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private SnowflakeIdUtils snowflakeIdUtils;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private AuthService authService;

    private static final String TEST_CODE = "test-wx-code";
    private static final String TEST_OPENID = "dev_test-wx-code";
    private static final String TEST_USER_ID = "u_123456";
    private static final String TEST_ACCESS_TOKEN = "access-token-123";
    private static final String TEST_REFRESH_TOKEN = "refresh-token-456";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "appId", "test-appid");
        ReflectionTestUtils.setField(authService, "secret", "test-secret");
        ReflectionTestUtils.setField(authService, "devMode", true);

        lenient().when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        lenient().when(snowflakeIdUtils.nextIdWithPrefix("u_")).thenReturn(TEST_USER_ID);
        lenient().when(jwtUtils.generateAccessToken(anyString(), anyString())).thenReturn(TEST_ACCESS_TOKEN);
        lenient().when(jwtUtils.generateRefreshToken(anyString())).thenReturn(TEST_REFRESH_TOKEN);
    }

    @Nested
    @DisplayName("微信登录 - 新用户注册")
    class NewUserLogin {

        @Test
        @DisplayName("新用户首次登录应自动注册并返回 token")
        void shouldRegisterNewUserAndReturnToken() {
            WechatLoginRequest request = new WechatLoginRequest();
            request.setCode(TEST_CODE);
            request.setNickname("测试用户");
            request.setAvatar("https://example.com/avatar.png");

            when(userMapper.selectByOpenid(TEST_OPENID)).thenReturn(null);

            WechatLoginResponse response = authService.wechatLogin(request);

            assertThat(response).isNotNull();
            assertThat(response.getToken()).isEqualTo(TEST_ACCESS_TOKEN);
            assertThat(response.getRefreshToken()).isEqualTo(TEST_REFRESH_TOKEN);
            assertThat(response.getUser()).isNotNull();
            assertThat(response.getUser().getId()).isEqualTo(TEST_USER_ID);
            assertThat(response.getUser().getNickname()).isEqualTo("测试用户");

            // 用 ArgumentCaptor 验证 insert 参数
            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userMapper).insert(captor.capture());
            User inserted = captor.getValue();
            assertThat(inserted.getId()).isEqualTo(TEST_USER_ID);
            assertThat(inserted.getNickname()).isEqualTo("测试用户");
            assertThat(inserted.getOpenid()).isEqualTo(TEST_OPENID);

            verify(valueOperations).set(
                eq("refresh:" + TEST_USER_ID),
                eq(TEST_REFRESH_TOKEN),
                eq(30L),
                eq(TimeUnit.DAYS)
            );
        }
    }

    @Nested
    @DisplayName("微信登录 - 已注册用户登录")
    class ExistingUserLogin {

        @Test
        @DisplayName("已注册用户登录应更新信息并返回 token")
        void shouldUpdateExistingUserAndReturnToken() {
            WechatLoginRequest request = new WechatLoginRequest();
            request.setCode(TEST_CODE);
            request.setNickname("更新昵称");
            request.setAvatar("https://example.com/new-avatar.png");

            User existingUser = new User();
            existingUser.setId(TEST_USER_ID);
            existingUser.setOpenid(TEST_OPENID);
            existingUser.setNickname("旧昵称");
            existingUser.setAvatar("https://example.com/old-avatar.png");
            existingUser.setCreatedAt(LocalDateTime.now().minusDays(7));

            when(userMapper.selectByOpenid(TEST_OPENID)).thenReturn(existingUser);

            WechatLoginResponse response = authService.wechatLogin(request);

            assertThat(response.getUser().getId()).isEqualTo(TEST_USER_ID);
            assertThat(response.getUser().getNickname()).isEqualTo("更新昵称");

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userMapper).updateById(captor.capture());
            User updated = captor.getValue();
            assertThat(updated.getNickname()).isEqualTo("更新昵称");
            assertThat(updated.getAvatar()).isEqualTo("https://example.com/new-avatar.png");
        }
    }

    @Nested
    @DisplayName("Token 刷新")
    class TokenRefresh {

        @Test
        @DisplayName("刷新成功应返回新 token 对")
        void shouldReturnNewTokensOnValidRefresh() {
            when(jwtUtils.getUserIdFromRefreshToken(TEST_REFRESH_TOKEN)).thenReturn(TEST_USER_ID);
            when(valueOperations.get("refresh:" + TEST_USER_ID)).thenReturn(TEST_REFRESH_TOKEN);
            when(jwtUtils.getOpenId(TEST_REFRESH_TOKEN)).thenReturn(TEST_OPENID);

            var result = authService.refreshToken(TEST_REFRESH_TOKEN);

            assertThat(result).containsEntry("token", TEST_ACCESS_TOKEN);
            assertThat(result).containsEntry("refreshToken", TEST_REFRESH_TOKEN);
            assertThat(result).containsKey("expiresIn");
        }

        @Test
        @DisplayName("无效 refresh token 应抛出异常")
        void shouldThrowExceptionOnInvalidToken() {
            when(jwtUtils.getUserIdFromRefreshToken("invalid-token")).thenReturn(null);

            assertThatThrownBy(() -> authService.refreshToken("invalid-token"))
                .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("Redis 中不存在的 refresh token 应抛出异常")
        void shouldThrowExceptionWhenTokenNotFoundInRedis() {
            when(jwtUtils.getUserIdFromRefreshToken(TEST_REFRESH_TOKEN)).thenReturn(TEST_USER_ID);
            when(valueOperations.get("refresh:" + TEST_USER_ID)).thenReturn(null);

            assertThatThrownBy(() -> authService.refreshToken(TEST_REFRESH_TOKEN))
                .isInstanceOf(BusinessException.class);
        }
    }
}
