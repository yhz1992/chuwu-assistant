package com.example.demo.common.config;

import com.example.demo.common.interceptor.AuthInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

/**
 * Web MVC 配置类
 * 注册 AuthInterceptor 拦截器，并配置 CORS 和放行路径
 */
@Slf4j
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    public WebMvcConfig(AuthInterceptor authInterceptor) {
        this.authInterceptor = authInterceptor;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/v1/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
        log.info("CORS 跨域配置完成：允许所有来源访问 /api/v1/**");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        log.info("注册 AuthInterceptor 拦截器");
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/v1/**")                    // 拦截所有 /api/v1/ 路径
                .excludePathPatterns(
                        "/api/v1/auth/**",                        // 认证接口放行
                        "/api/v1/shares/**",                      // 对外分享接口放行
                        "/api/v1/shares/public/**",               // 对外公开分享接口放行
                        "/doc.html",                              // Knife4j 文档
                        "/v3/api-docs/**",                        // Swagger API 文档
                        "/swagger-resources/**",                  // Swagger 资源
                        "/webjars/**"                             // WebJars
                );
        log.info("AuthInterceptor 拦截器注册完成，已放行路径：/api/v1/auth/**, /api/v1/shares/**, /doc.html, /v3/api-docs/**");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置本地上传文件的静态资源映射（开发环境）
        String uploadPath = Paths.get(System.getProperty("user.dir")).getParent().resolve("uploads").toAbsolutePath().toUri().toString();
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadPath);
        log.info("静态资源映射配置完成：/uploads/** -> {}", uploadPath);
    }
}
