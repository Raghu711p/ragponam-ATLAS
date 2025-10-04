package com.studentevaluator.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.studentevaluator.security.RateLimitingFilter;

/**
 * Security configuration for the Student Evaluator System.
 * Configures rate limiting, CORS, and other security measures.
 */
@Configuration
@EnableScheduling
public class SecurityConfig implements WebMvcConfigurer {
    
    private final RateLimitingFilter rateLimitingFilter;
    
    @Autowired
    public SecurityConfig(RateLimitingFilter rateLimitingFilter) {
        this.rateLimitingFilter = rateLimitingFilter;
    }
    
    /**
     * Register the rate limiting filter.
     */
    @Bean
    public FilterRegistrationBean<RateLimitingFilter> rateLimitingFilterRegistration() {
        FilterRegistrationBean<RateLimitingFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(rateLimitingFilter);
        registration.addUrlPatterns("/api/*");
        registration.setOrder(1);
        registration.setName("rateLimitingFilter");
        return registration;
    }
    
    /**
     * Configure CORS settings.
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:3000", "http://localhost:8080")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(false)
                .maxAge(3600);
    }
    
    /**
     * Scheduled task to clean up old rate limiting records.
     */
    @Scheduled(fixedRate = 300000) // Every 5 minutes
    public void cleanupRateLimitingRecords() {
        rateLimitingFilter.cleanupOldRecords();
    }
}