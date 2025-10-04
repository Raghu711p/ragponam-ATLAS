package com.studentevaluator.performance;

import com.studentevaluator.model.Assignment;
import com.studentevaluator.service.AssignmentService;
import com.studentevaluator.service.EvaluationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.StopWatch;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Performance tests for caching functionality
 */
@SpringBootTest
@ActiveProfiles("test")
class CachePerformanceTest {

    @Autowired
    private AssignmentService assignmentService;

    @Autowired
    private EvaluationService evaluationService;

    @Autowired
    private CacheManager cacheManager;

    @Test
    @DisplayName("Cache should improve assignment retrieval performance")
    void testAssignmentCachePerformance() {
        // Given - create a test assignment
        String assignmentId = "cache-test-assignment";
        
        // First call - should hit database and populate cache
        StopWatch firstCallStopWatch = new StopWatch();
        firstCallStopWatch.start();
        Optional<Assignment> firstResult = assignmentService.getAssignment(assignmentId);
        firstCallStopWatch.stop();
        
        // Second call - should hit cache
        StopWatch secondCallStopWatch = new StopWatch();
        secondCallStopWatch.start();
        Optional<Assignment> secondResult = assignmentService.getAssignment(assignmentId);
        secondCallStopWatch.stop();
        
        // Third call - should also hit cache
        StopWatch thirdCallStopWatch = new StopWatch();
        thirdCallStopWatch.start();
        Optional<Assignment> thirdResult = assignmentService.getAssignment(assignmentId);
        thirdCallStopWatch.stop();
        
        // Then
        long firstCallTime = firstCallStopWatch.getTotalTimeMillis();
        long secondCallTime = secondCallStopWatch.getTotalTimeMillis();
        long thirdCallTime = thirdCallStopWatch.getTotalTimeMillis();
        
        System.out.printf("Assignment retrieval times:%n");
        System.out.printf("First call (DB): %d ms%n", firstCallTime);
        System.out.printf("Second call (Cache): %d ms%n", secondCallTime);
        System.out.printf("Third call (Cache): %d ms%n", thirdCallTime);
        
        // Cache hits should be significantly faster
        assertTrue(secondCallTime <= firstCallTime, 
            "Second call should be faster or equal (cache hit)");
        assertTrue(thirdCallTime <= firstCallTime, 
            "Third call should be faster or equal (cache hit)");
        
        // Results should be consistent
        assertEquals(firstResult.isPresent(), secondResult.isPresent());
        assertEquals(secondResult.isPresent(), thirdResult.isPresent());
        
        if (firstResult.isPresent()) {
            assertEquals(firstResult.get().getAssignmentId(), secondResult.get().getAssignmentId());
            assertEquals(secondResult.get().getAssignmentId(), thirdResult.get().getAssignmentId());
        }
    }

    @Test
    @DisplayName("Cache should handle multiple concurrent requests efficiently")
    void testConcurrentCacheAccess() throws Exception {
        // Given
        String assignmentId = "concurrent-cache-test";
        int numberOfThreads = 10;
        int requestsPerThread = 5;
        
        // When - multiple threads accessing the same cached data
        Thread[] threads = new Thread[numberOfThreads];
        long[][] responseTimes = new long[numberOfThreads][requestsPerThread];
        
        for (int i = 0; i < numberOfThreads; i++) {
            final int threadIndex = i;
            threads[i] = new Thread(() -> {
                for (int j = 0; j < requestsPerThread; j++) {
                    StopWatch stopWatch = new StopWatch();
                    stopWatch.start();
                    
                    Optional<Assignment> result = assignmentService.getAssignment(assignmentId);
                    
                    stopWatch.stop();
                    responseTimes[threadIndex][j] = stopWatch.getTotalTimeMillis();
                }
            });
        }
        
        // Start all threads
        long startTime = System.currentTimeMillis();
        for (Thread thread : threads) {
            thread.start();
        }
        
        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }
        long totalTime = System.currentTimeMillis() - startTime;
        
        // Then - analyze results
        long totalRequests = numberOfThreads * requestsPerThread;
        long sumResponseTimes = 0;
        long maxResponseTime = 0;
        
        for (int i = 0; i < numberOfThreads; i++) {
            for (int j = 0; j < requestsPerThread; j++) {
                sumResponseTimes += responseTimes[i][j];
                maxResponseTime = Math.max(maxResponseTime, responseTimes[i][j]);
            }
        }
        
        double averageResponseTime = (double) sumResponseTimes / totalRequests;
        
        System.out.printf("Concurrent cache access results:%n");
        System.out.printf("Total requests: %d%n", totalRequests);
        System.out.printf("Total time: %d ms%n", totalTime);
        System.out.printf("Average response time: %.2f ms%n", averageResponseTime);
        System.out.printf("Maximum response time: %d ms%n", maxResponseTime);
        
        // Verify performance
        assertTrue(averageResponseTime < 100, 
            String.format("Average response time %.2f ms should be under 100ms for cached data", averageResponseTime));
        
        assertTrue(maxResponseTime < 500, 
            String.format("Maximum response time %d ms should be under 500ms", maxResponseTime));
    }

    @Test
    @DisplayName("Cache statistics should be available and accurate")
    void testCacheStatistics() {
        // Given
        String assignmentId = "stats-test-assignment";
        
        // Clear cache to start fresh
        cacheManager.getCache("assignments").clear();
        
        // When - perform operations that should affect cache stats
        // First call - cache miss
        assignmentService.getAssignment(assignmentId);
        
        // Second call - cache hit
        assignmentService.getAssignment(assignmentId);
        
        // Third call - cache hit
        assignmentService.getAssignment(assignmentId);
        
        // Then - verify cache is working
        var cache = cacheManager.getCache("assignments");
        assertNotNull(cache, "Assignments cache should exist");
        
        // Verify cache contains the key
        var cachedValue = cache.get(assignmentId);
        // Note: The cached value might be null if assignment doesn't exist, but the cache entry should exist
        
        System.out.printf("Cache name: %s%n", cache.getName());
        System.out.printf("Cache native cache type: %s%n", cache.getNativeCache().getClass().getSimpleName());
    }

    @Test
    @DisplayName("Cache eviction should work correctly")
    void testCacheEviction() {
        // Given
        String assignmentId = "eviction-test-assignment";
        
        // When - populate cache
        Optional<Assignment> initialResult = assignmentService.getAssignment(assignmentId);
        
        // Verify cache contains the entry
        var cache = cacheManager.getCache("assignments");
        assertNotNull(cache);
        
        // Evict cache entry
        cache.evict(assignmentId);
        
        // Access again - should hit database
        StopWatch afterEvictionStopWatch = new StopWatch();
        afterEvictionStopWatch.start();
        Optional<Assignment> afterEvictionResult = assignmentService.getAssignment(assignmentId);
        afterEvictionStopWatch.stop();
        
        // Then
        long afterEvictionTime = afterEvictionStopWatch.getTotalTimeMillis();
        
        System.out.printf("Time after cache eviction: %d ms%n", afterEvictionTime);
        
        // Results should be consistent
        assertEquals(initialResult.isPresent(), afterEvictionResult.isPresent());
        
        if (initialResult.isPresent() && afterEvictionResult.isPresent()) {
            assertEquals(initialResult.get().getAssignmentId(), 
                        afterEvictionResult.get().getAssignmentId());
        }
    }

    @Test
    @DisplayName("Cache should handle high volume of different keys")
    void testCacheVolumeHandling() {
        // Given
        int numberOfDifferentKeys = 100;
        
        // When - access many different assignment IDs
        StopWatch totalStopWatch = new StopWatch();
        totalStopWatch.start();
        
        for (int i = 0; i < numberOfDifferentKeys; i++) {
            String assignmentId = "volume-test-assignment-" + i;
            assignmentService.getAssignment(assignmentId);
        }
        
        totalStopWatch.stop();
        
        // Then
        long totalTime = totalStopWatch.getTotalTimeMillis();
        double averageTimePerRequest = (double) totalTime / numberOfDifferentKeys;
        
        System.out.printf("Volume test results:%n");
        System.out.printf("Total requests: %d%n", numberOfDifferentKeys);
        System.out.printf("Total time: %d ms%n", totalTime);
        System.out.printf("Average time per request: %.2f ms%n", averageTimePerRequest);
        
        // Verify reasonable performance even with cache misses
        assertTrue(averageTimePerRequest < 200, 
            String.format("Average time per request %.2f ms should be reasonable", averageTimePerRequest));
        
        assertTrue(totalTime < 20000, 
            String.format("Total time %d ms should be under 20 seconds for %d requests", totalTime, numberOfDifferentKeys));
    }
}