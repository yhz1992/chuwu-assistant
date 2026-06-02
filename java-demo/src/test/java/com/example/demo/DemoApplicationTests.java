package com.example.demo;

import com.example.demo.common.config.TestConfig;
import com.example.demo.common.utils.JwtUtils;
import com.example.demo.common.utils.SnowflakeIdUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Spring Boot 应用集成测试
 */
@SpringBootTest
@Import(TestConfig.class)
class DemoApplicationTests {

    @Autowired
    private ApplicationContext context;

    @Autowired(required = false)
    private JwtUtils jwtUtils;

    @Autowired(required = false)
    private SnowflakeIdUtils snowflakeIdUtils;

    @Test
    @DisplayName("应用上下文应成功加载")
    void contextLoads() {
        assertThat(context).isNotNull();
    }

    @Test
    @DisplayName("JWT 工具类应正确注入")
    void jwtUtilsShouldBeAvailable() {
        assertThat(jwtUtils).isNotNull();
    }

    @Test
    @DisplayName("雪花 ID 生成器应正确注入")
    void snowflakeIdUtilsShouldBeAvailable() {
        assertThat(snowflakeIdUtils).isNotNull();
    }

    @Test
    @DisplayName("雪花 ID 应以正确前缀开头")
    void snowflakeIdShouldHavePrefix() {
        String id = snowflakeIdUtils.nextIdWithPrefix("test_");
        assertThat(id).startsWith("test_");
        assertThat(id.length()).isGreaterThan(5);
    }

    @Test
    @DisplayName("JWT token 生成和解析应正确")
    void jwtTokenShouldBeValid() {
        String token = jwtUtils.generateAccessToken("u_test_user", "test_openid");
        assertThat(token).isNotNull();

        String userId = jwtUtils.getUserIdFromAccessToken(token);
        assertThat(userId).isEqualTo("u_test_user");
    }

    @Test
    @DisplayName("所有 Controller Bean 应正确注册")
    void allControllersShouldBeRegistered() {
        String[] controllerBeans = {
            "authController",
            "userController",
            "homeController",
            "collectionController",
            "saleListController",
            "templateController",
            "shareController",
            "wishlistController",
            "uploadController",
            "feedbackController",
            "eventController"
        };

        for (String beanName : controllerBeans) {
            assertThat(context.containsBean(beanName))
                .as("Controller bean '%s' should be registered", beanName)
                .isTrue();
        }
    }
}
