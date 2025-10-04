package com.studentevaluator.config;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import picocli.CommandLine;
import picocli.spring.PicocliSpringFactory;

/**
 * Configuration for Picocli CLI framework integration with Spring Boot.
 */
@Configuration
public class PicocliConfig {
    
    @Bean
    @Primary
    public CommandLine.IFactory picocliFactory(ApplicationContext applicationContext) {
        return new PicocliSpringFactory(applicationContext);
    }
}