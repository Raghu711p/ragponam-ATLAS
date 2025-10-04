package com.studentevaluator.performance;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import com.studentevaluator.monitoring.PerformanceMonitor;

/**
 * Load tests to validate system performance under stress
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class LoadTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PerformanceMonitor performanceMonitor;

    @Test
    @DisplayName("System should handle concurrent API requests")
    void testConcurrentApiRequests() throws Exception {
        // Given
        int numberOfConcurrentRequests = 20;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfConcurrentRequests);
        List<CompletableFuture<ResponseTime>> futures = new ArrayList<>();
        
        String baseUrl = "http://localhost:" + port;
        
        // When
        for (int i = 0; i < numberOfConcurrentRequests; i++) {
            CompletableFuture<ResponseTime> future = CompletableFuture.supplyAsync(() -> {
                long startTime = System.currentTimeMillis();
                
                // Test health endpoint
                ResponseEntity<String> response = restTemplate.getForEntity(
                    baseUrl + "/actuator/health", String.class);
                
                long endTime = System.currentTimeMillis();
                
                return new ResponseTime(HttpStatus.valueOf(response.getStatusCode().value()), endTime - startTime);
            }, executor);
            
            futures.add(future);
        }
        
        // Wait for all requests to complete
        List<ResponseTime> responseTimes = new ArrayList<>();
        for (CompletableFuture<ResponseTime> future : futures) {
            responseTimes.add(future.get(30, TimeUnit.SECONDS));
        }
        
        executor.shutdown();
        
        // Then
        double averageResponseTime = responseTimes.stream()
            .mapToLong(ResponseTime::responseTime)
            .average()
            .orElse(0);
        
        long maxResponseTime = responseTimes.stream()
            .mapToLong(ResponseTime::responseTime)
            .max()
            .orElse(0);
        
        long successfulRequests = responseTimes.stream()
            .filter(rt -> rt.status() == HttpStatus.OK)
            .count();
        
        System.out.printf("Concurrent API requests: %d total%n", numberOfConcurrentRequests);
        System.out.printf("Successful requests: %d%n", successfulRequests);
        System.out.printf("Average response time: %.2f ms%n", averageResponseTime);
        System.out.printf("Maximum response time: %d ms%n", maxResponseTime);
        
        // Verify performance requirements
        assertEquals(numberOfConcurrentRequests, successfulRequests, 
            "All requests should be successful");
        
        assertTrue(averageResponseTime < 1000, 
            String.format("Average response time %.2f ms exceeds 1 second limit", averageResponseTime));
        
        assertTrue(maxResponseTime < 5000, 
            String.format("Maximum response time %d ms exceeds 5 second limit", maxResponseTime));
    }

    @Test
    @DisplayName("Monitoring endpoints should respond quickly")
    void testMonitoringEndpointPerformance() {
        // Given
        String baseUrl = "http://localhost:" + port;
        
        // Test various monitoring endpoints
        String[] endpoints = {
            "/actuator/health",
            "/actuator/metrics",
            "/actuator/info"
        };
        
        for (String endpoint : endpoints) {
            // When
            long startTime = System.currentTimeMillis();
            ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl + endpoint, String.class);
            long responseTime = System.currentTimeMillis() - startTime;
            
            // Then
            System.out.printf("Endpoint %s responded in %d ms%n", endpoint, responseTime);
            
            assertEquals(HttpStatus.OK, response.getStatusCode(), 
                "Endpoint " + endpoint + " should be accessible");
            
            assertTrue(responseTime < 2000, 
                String.format("Endpoint %s took %d ms, exceeding 2 second limit", endpoint, responseTime));
        }
    }

    @Test
    @DisplayName("Performance monitoring should track metrics correctly")
    void testPerformanceMonitoringAccuracy() {
        // Given
        PerformanceMonitor.PerformanceStats initialStats = performanceMonitor.getPerformanceStats();
        
        // When - simulate some performance data
        performanceMonitor.recordEvaluationTime(java.time.Duration.ofMillis(1500), true);
        performanceMonitor.recordEvaluationTime(java.time.Duration.ofMillis(2000), true);
        performanceMonitor.recordEvaluationTime(java.time.Duration.ofMillis(3000), false);
        
        performanceMonitor.recordCompilationTime(java.time.Duration.ofMillis(500), true);
        performanceMonitor.recordCompilationTime(java.time.Duration.ofMillis(800), false);
        
        performanceMonitor.recordTestExecutionTime(java.time.Duration.ofMillis(1000), true);
        
        // Then
        PerformanceMonitor.PerformanceStats finalStats = performanceMonitor.getPerformanceStats();
        
        System.out.printf("Performance stats: %s%n", finalStats);
        
        // Verify metrics are being tracked
        assertTrue(finalStats.getTotalEvaluations() >= initialStats.getTotalEvaluations() + 3,
            "Total evaluations should have increased");
        
        assertTrue(finalStats.getSuccessfulEvaluations() >= initialStats.getSuccessfulEvaluations() + 2,
            "Successful evaluations should have increased");
        
        assertTrue(finalStats.getFailedEvaluations() >= initialStats.getFailedEvaluations() + 1,
            "Failed evaluations should have increased");
        
        assertTrue(finalStats.getAverageEvaluationTime() > 0,
            "Average evaluation time should be positive");
    }

    @Test
    @DisplayName("System should maintain performance under sustained load")
    void testSustainedLoad() throws Exception {
        // Given
        int requestsPerSecond = 5;
        int durationSeconds = 10;
        int totalRequests = requestsPerSecond * durationSeconds;
        
        String baseUrl = "http://localhost:" + port;
        List<Long> responseTimes = new ArrayList<>();
        
        // When
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < totalRequests; i++) {
            long requestStart = System.currentTimeMillis();
            
            ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl + "/actuator/health", String.class);
            
            long requestTime = System.currentTimeMillis() - requestStart;
            responseTimes.add(requestTime);
            
            assertEquals(HttpStatus.OK, response.getStatusCode());
            
            // Maintain request rate
            long expectedTime = startTime + (i + 1) * 1000 / requestsPerSecond;
            long currentTime = System.currentTimeMillis();
            if (currentTime < expectedTime) {
                Thread.sleep(expectedTime - currentTime);
            }
        }
        
        long totalTime = System.currentTimeMillis() - startTime;
        
        // Then
        double averageResponseTime = responseTimes.stream()
            .mapToLong(Long::longValue)
            .average()
            .orElse(0);
        
        long maxResponseTime = responseTimes.stream()
            .mapToLong(Long::longValue)
            .max()
            .orElse(0);
        
        System.out.printf("Sustained load test: %d requests in %d ms%n", totalRequests, totalTime);
        System.out.printf("Average response time: %.2f ms%n", averageResponseTime);
        System.out.printf("Maximum response time: %d ms%n", maxResponseTime);
        
        // Verify performance doesn't degrade significantly
        assertTrue(averageResponseTime < 1000, 
            String.format("Average response time %.2f ms exceeds 1 second under sustained load", averageResponseTime));
        
        assertTrue(maxResponseTime < 3000, 
            String.format("Maximum response time %d ms exceeds 3 second limit under sustained load", maxResponseTime));
        
        // Verify we maintained the target request rate (allow 10% variance)
        double actualRate = (double) totalRequests / (totalTime / 1000.0);
        assertTrue(actualRate >= requestsPerSecond * 0.9, 
            String.format("Actual request rate %.2f/s is below target %d/s", actualRate, requestsPerSecond));
    }

    private record ResponseTime(HttpStatus status, long responseTime) {}
}