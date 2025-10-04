package com.studentevaluator.config;

import java.util.concurrent.TimeUnit;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.benmanes.caffeine.cache.Caffeine;

/**
 * Cache configuration for the Student Evaluator System
 */
@Configuration
@EnableCaching
public class CacheConfig {

    public static final String STUDENT_SCORES_CACHE = "studentScores";
    public static final String EVALUATION_RESULTS_CACHE = "evaluationResults";
    public static final String ASSIGNMENT_CACHE = "assignments";
    public static final String COMPILATION_CACHE = "compilationResults";

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        
        // Configure different cache specifications for different use cases
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .recordStats());
        
        // Register cache names
        cacheManager.setCacheNames(java.util.Arrays.asList(
                STUDENT_SCORES_CACHE,
                EVALUATION_RESULTS_CACHE,
                ASSIGNMENT_CACHE,
                COMPILATION_CACHE,
                "assignments",
                "allAssignments",
                "studentEvaluations",
                "studentStats"
        ));
        
        return cacheManager;
    }

    @Bean
    public Caffeine<Object, Object> caffeineConfig() {
        return Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .recordStats();
    }
}