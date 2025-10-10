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
 * 使用Caffeine作为缓存实现
 *
 * @author Academic System Team
 */
@Configuration
@EnableCaching
public class CacheConfig {

    // 缓存名称常量
    public static final String CACHE_COURSES = "courses";
    public static final String CACHE_DEPARTMENTS = "departments";
    public static final String CACHE_MAJORS = "majors";
    public static final String CACHE_SEMESTERS = "semesters";
    public static final String CACHE_ACTIVE_SEMESTER = "activeSemester";
    public static final String CACHE_EXAMS = "exams";
    public static final String CACHE_EXAM_ROOMS = "examRooms";
    public static final String CACHE_EVALUATION_PERIODS = "evaluationPeriods";
    public static final String CACHE_ACTIVE_EVALUATION_PERIOD = "activeEvaluationPeriod";
    public static final String CACHE_EVALUATION_STATS = "evaluationStats";

    /**
     * 配置缓存管理器
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        
        // 配置默认的缓存策略
        cacheManager.setCaffeine(Caffeine.newBuilder()
                // 初始容量
                .initialCapacity(100)
                // 最大容量
                .maximumSize(1000)
                // 写入后过期时间
                .expireAfterWrite(30, TimeUnit.MINUTES)
                // 记录缓存统计信息
                .recordStats());
        
        return cacheManager;
    }

    /**
     * 考试缓存配置
     */
    @Bean
    public Caffeine<Object, Object> examCacheConfig() {
        return Caffeine.newBuilder()
                .initialCapacity(50)
                .maximumSize(500)
                .expireAfterWrite(15, TimeUnit.MINUTES)
                .recordStats();
    }

    /**
     * 考场缓存配置
     */
    @Bean
    public Caffeine<Object, Object> examRoomCacheConfig() {
        return Caffeine.newBuilder()
                .initialCapacity(100)
                .maximumSize(1000)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .recordStats();
    }
}
