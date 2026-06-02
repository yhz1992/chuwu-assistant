package com.example.demo.common.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 配置类
 * 配置分页插件和乐观锁插件
 */
@Slf4j
@Configuration
public class MyBatisPlusConfig {

    /**
     * MyBatis-Plus 拦截器，注册分页插件和乐观锁插件
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        log.info("初始化 MyBatis-Plus 拦截器");
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 分页插件（MySQL 数据库）
        PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
        paginationInterceptor.setMaxLimit(500L); // 单页最大记录数限制
        paginationInterceptor.setOverflow(true); // 溢出时自动处理
        interceptor.addInnerInterceptor(paginationInterceptor);

        // 乐观锁插件
        OptimisticLockerInnerInterceptor optimisticLockerInterceptor = new OptimisticLockerInnerInterceptor();
        interceptor.addInnerInterceptor(optimisticLockerInterceptor);

        log.info("MyBatis-Plus 拦截器配置完成（分页 + 乐观锁）");
        return interceptor;
    }
}
