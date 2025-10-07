package com.university.academic.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * 缓存配置类
 * 使用Caffeine作为缓存实现，提供高性能的本地缓存
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * 缓存名称常量
     */
    public static final String CACHE_COURSES = "courses";
    public static final String CACHE_SEMESTERS = "semesters";
    public static final String CACHE_DEPARTMENTS = "departments";
    public static final String CACHE_MAJORS = "majors";
    public static final String CACHE_SYSTEM_CONFIGS = "systemConfigs";
    public static final String CACHE_ACTIVE_SEMESTER = "activeSemester";

    /**
     * 配置缓存管理器
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
                CACHE_COURSES,
                CACHE_SEMESTERS,
                CACHE_DEPARTMENTS,
                CACHE_MAJORS,
                CACHE_SYSTEM_CONFIGS,
                CACHE_ACTIVE_SEMESTER
        );
        
        cacheManager.setCaffeine(Caffeine.newBuilder()
                // 设置最后一次写入或访问后经过固定时间过期
                .expireAfterWrite(30, TimeUnit.MINUTES)
                // 初始的缓存空间大小
                .initialCapacity(100)
                // 缓存的最大条目数
                .maximumSize(1000)
                // 开启缓存统计
                .recordStats());
        
        return cacheManager;
    }

    /**
     * 配置特定缓存的过期策略
     */
    @Bean
    public Caffeine<Object, Object> caffeineConfig() {
        return Caffeine.newBuilder()
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .maximumSize(1000)
                .recordStats();
    }
}

