package com.studentevaluator.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.net.URI;

/**
 * Configuration class for DynamoDB client setup.
 * Configures DynamoDB client for both local development and production environments.
 */
@Configuration
public class DynamoDBConfig {
    
    @Value("${aws.dynamodb.endpoint:}")
    private String dynamoDbEndpoint;
    
    @Value("${aws.region:us-east-1}")
    private String awsRegion;
    
    /**
     * Creates and configures DynamoDB client bean.
     * 
     * @return configured DynamoDB client
     */
    @Bean
    public DynamoDbClient dynamoDbClient() {
        var clientBuilder = DynamoDbClient.builder()
                .region(Region.of(awsRegion))
                .credentialsProvider(DefaultCredentialsProvider.create());
        
        // If endpoint is specified (for local development), use it
        if (dynamoDbEndpoint != null && !dynamoDbEndpoint.trim().isEmpty()) {
            clientBuilder.endpointOverride(URI.create(dynamoDbEndpoint));
        }
        
        return clientBuilder.build();
    }
}