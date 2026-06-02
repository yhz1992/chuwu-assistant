package com.example.demo.modules.auth.service;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.demo.common.exception.BusinessException;
import com.example.demo.common.response.ResultCode;
import com.example.demo.common.utils.JwtUtils;
import com.example.demo.common.utils.SnowflakeIdUtils;
import com.example.demo.modules.auth.dto.WechatLoginRequest;
import com.example.demo.modules.auth.dto.WechatLoginResponse;
import com.example.demo.modules.auth.entity.User;
import com.example.demo.modules.auth.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class AuthService {

    private final UserMapper userMapper;
    private final JwtUtils jwtUtils;
    private final SnowflakeIdUtils snowflakeIdUtils;
    private final OkHttpClient okHttpClient;
    private final StringRedisTemplate stringRedisTemplate;

    private static final String WECHAT_URL = "https://api.weixin.qq.com/sns/jscode2session";
    private static final String REFRESH_KEY_PREFIX = "refresh:";
    private static final long REFRESH_TTL_DAYS = 30L;
    private static final long ACCESS_EXPIRE_SECONDS = 604800L;

    @Value("${wechat.appid}")
    private String appId;

    @Value("${wechat.secret}")
    private String secret;

    @Value("${wechat.dev-mode:false}")
    private boolean devMode;

    public AuthService(UserMapper userMapper, JwtUtils jwtUtils,
                       SnowflakeIdUtils snowflakeIdUtils,
                       StringRedisTemplate stringRedisTemplate) {
        this.userMapper = userMapper;
        this.jwtUtils = jwtUtils;
        this.snowflakeIdUtils = snowflakeIdUtils;
        this.okHttpClient = new OkHttpClient();
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * 微信小程序登录（开发模式下使用模拟登录）
     */
    @Transactional(rollbackFor = Exception.class)
    public WechatLoginResponse wechatLogin(WechatLoginRequest req) {
        String openid;
        if (devMode) {
            // 开发模式：用 code 作为模拟 openid
            openid = "dev_" + (req.getCode() != null ? req.getCode() : System.currentTimeMillis());
            log.info("开发模式登录，模拟openid: {}", openid);
        } else {
            openid = getOpenidFromWeChat(req.getCode());
        }

        // 2. 查用户，不存在则创建
        User user = userMapper.selectByOpenid(openid);

        if (user == null) {
            user = new User();
            user.setId(snowflakeIdUtils.nextIdWithPrefix("u_"));
            user.setOpenid(openid);
            user.setNickname(req.getNickname());
            user.setAvatar(req.getAvatar());
            user.setMembershipLevel(0);
            user.setViolationCount(0);
            user.setStatus("active");
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            userMapper.insert(user);
            log.info("新用户注册成功，userId: {}", user.getId());
        } else {
            if (req.getNickname() != null) {
                user.setNickname(req.getNickname());
            }
            if (req.getAvatar() != null) {
                user.setAvatar(req.getAvatar());
            }
            user.setUpdatedAt(LocalDateTime.now());
            userMapper.updateById(user);
        }

        // 3. 生成 token（userId 就是带前缀的字符串，如 "u_123456"）
        String userId = user.getId();
        String accessToken = jwtUtils.generateAccessToken(userId, openid);
        String refreshToken = jwtUtils.generateRefreshToken(userId);

        // 4. refresh token 存 Redis
        String redisKey = REFRESH_KEY_PREFIX + userId;
        stringRedisTemplate.opsForValue().set(redisKey, refreshToken, REFRESH_TTL_DAYS, TimeUnit.DAYS);

        // 5. 构建响应
        return WechatLoginResponse.builder()
                .token(accessToken)
                .refreshToken(refreshToken)
                .user(WechatLoginResponse.UserInfo.builder()
                        .id(user.getId())
                        .nickname(user.getNickname())
                        .avatar(user.getAvatar())
                        .createdAt(user.getCreatedAt())
                        .build())
                .build();
    }

    /**
     * 刷新 token
     */
    public Map<String, Object> refreshToken(String refreshToken) {
        // 1. 解出 userId
        String userId = jwtUtils.getUserIdFromRefreshToken(refreshToken);
        if (userId == null) {
            throw new BusinessException(ResultCode.REFRESH_TOKEN_INVALID);
        }

        // 2. Redis 验证
        String redisKey = REFRESH_KEY_PREFIX + userId;
        String storedToken = stringRedisTemplate.opsForValue().get(redisKey);
        if (storedToken == null || !storedToken.equals(refreshToken)) {
            throw new BusinessException(ResultCode.REFRESH_TOKEN_INVALID);
        }

        // 3. 生成新 token 对
        String newAccessToken = jwtUtils.generateAccessToken(userId, jwtUtils.getOpenId(refreshToken));
        String newRefreshToken = jwtUtils.generateRefreshToken(userId);

        // 4. 更新 Redis
        stringRedisTemplate.opsForValue().set(redisKey, newRefreshToken, REFRESH_TTL_DAYS, TimeUnit.DAYS);

        Map<String, Object> result = new HashMap<>();
        result.put("token", newAccessToken);
        result.put("refreshToken", newRefreshToken);
        result.put("expiresIn", ACCESS_EXPIRE_SECONDS);
        return result;
    }

    // ==================== 私有方法 ====================

    private String getOpenidFromWeChat(String code) {
        String url = WECHAT_URL + "?appid=" + appId
                + "&secret=" + secret
                + "&js_code=" + code
                + "&grant_type=authorization_code";

        try {
            Request request = new Request.Builder().url(url).get().build();
            Response response = okHttpClient.newCall(request).execute();
            String responseBody = response.body() != null ? response.body().string() : "";
            JSONObject json = JSONUtil.parseObj(responseBody);

            Integer errcode = json.getInt("errcode");
            if (errcode != null && errcode != 0) {
                String errmsg = json.getStr("errmsg", "未知错误");
                log.error("微信登录失败，errcode: {}, errmsg: {}", errcode, errmsg);
                throw new BusinessException(ResultCode.WX_LOGIN_FAILED, "微信登录失败: " + errmsg);
            }

            String openid = json.getStr("openid");
            if (openid == null || openid.isEmpty()) {
                log.error("微信接口未返回 openid");
                throw new BusinessException(ResultCode.WX_LOGIN_FAILED);
            }
            return openid;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("调用微信接口异常", e);
            throw new BusinessException(ResultCode.THIRD_PARTY_ERROR, "微信服务调用异常");
        }
    }
}
