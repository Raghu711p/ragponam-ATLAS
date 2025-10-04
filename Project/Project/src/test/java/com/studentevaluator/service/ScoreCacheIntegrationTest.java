package com.studentevaluator.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ScoreCache Integration Tests")
class ScoreCacheIntegrationTest {
    
    private ScoreCache scoreCache;
    
    @BeforeEach
    void setUp() {
        scoreCache = new ScoreCache();
    }
    
    @Test
    @DisplayName("Should demonstrate cache invalidation workflow")
    void shouldDemonstrateCacheInvalidationWorkflow() {
        // Given - simulate initial score storage
        String studentId = "student123";
        BigDecimal initialScore = new BigDecimal("85.00");
        BigDecimal updatedScore = new BigDecimal("92.50");
        
        // When - initial score is cached
        scoreCache.storeScore(studentId, initialScore);
        Optional<BigDecimal> cachedScore = scoreCache.retrieveScore(studentId);
        
        // Then - score should be in cache
        assertTrue(cachedScore.isPresent());
        assertEquals(initialScore, cachedScore.get());
        
        // When - simulating database update (would happen in service layer)
        // First invalidate cache, then update database, then cache new value
        scoreCache.invalidateScore(studentId);
        
        // Simulate database update happening here...
        // In real scenario, this would be a database operation
        
        // Cache the new score after database update
        scoreCache.storeScore(studentId, updatedScore);
        
        // Then - new score should be cached
        Optional<BigDecimal> newCachedScore = scoreCache.retrieveScore(studentId);
        assertTrue(newCachedScore.isPresent());
        assertEquals(updatedScore, newCachedScore.get());
    }
    
    @Test
    @DisplayName("Should handle cache-aside pattern correctly")
    void shouldHandleCacheAsidePatternCorrectly() {
        // Given
        String studentId = "student456";
        BigDecimal score = new BigDecimal("78.75");
        
        // When - cache miss scenario (typical cache-aside pattern)
        Optional<BigDecimal> cachedScore = scoreCache.retrieveScore(studentId);
        assertFalse(cachedScore.isPresent()); // Cache miss
        
        // Simulate database lookup would happen here...
        // For this test, we'll simulate finding the score in database
        BigDecimal databaseScore = score;
        
        // Store in cache after database lookup
        scoreCache.storeScore(studentId, databaseScore);
        
        // Then - subsequent lookups should hit cache
        Optional<BigDecimal> subsequentLookup = scoreCache.retrieveScore(studentId);
        assertTrue(subsequentLookup.isPresent());
        assertEquals(databaseScore, subsequentLookup.get());
    }
    
    @Test
    @DisplayName("Should handle concurrent cache operations during database updates")
    void shouldHandleConcurrentCacheOperationsDuringDatabaseUpdates() throws Exception {
        // Given
        String studentId = "student789";
        BigDecimal initialScore = new BigDecimal("80.00");
        scoreCache.storeScore(studentId, initialScore);
        
        ExecutorService executor = Executors.newFixedThreadPool(3);
        
        // When - concurrent operations: read, update, invalidate
        CompletableFuture<Optional<BigDecimal>> readOperation = CompletableFuture.supplyAsync(() -> {
            // Simulate multiple reads during update
            for (int i = 0; i < 10; i++) {
                scoreCache.retrieveScore(studentId);
                try {
                    Thread.sleep(1); // Small delay to increase chance of concurrency
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            return scoreCache.retrieveScore(studentId);
        }, executor);
        
        CompletableFuture<Void> updateOperation = CompletableFuture.runAsync(() -> {
            // Simulate database update process
            scoreCache.invalidateScore(studentId); // Invalidate first
            try {
                Thread.sleep(5); // Simulate database update time
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            scoreCache.storeScore(studentId, new BigDecimal("95.00")); // Store new value
        }, executor);
        
        CompletableFuture<Boolean> containsOperation = CompletableFuture.supplyAsync(() -> {
            // Check if student exists in cache during update
            boolean initialExists = scoreCache.containsStudent(studentId);
            try {
                Thread.sleep(10); // Wait for potential update
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return scoreCache.containsStudent(studentId);
        }, executor);
        
        // Then - all operations should complete without exceptions
        CompletableFuture.allOf(readOperation, updateOperation, containsOperation).get();
        
        // Final state should be consistent
        Optional<BigDecimal> finalScore = scoreCache.retrieveScore(studentId);
        assertTrue(finalScore.isPresent());
        assertEquals(new BigDecimal("95.00"), finalScore.get());
        
        executor.shutdown();
    }
    
    @Test
    @DisplayName("Should maintain cache consistency during bulk operations")
    void shouldMaintainCacheConsistencyDuringBulkOperations() {
        // Given - bulk data setup
        int numberOfStudents = 50;
        
        // When - bulk store operations
        for (int i = 0; i < numberOfStudents; i++) {
            String studentId = "bulkStudent_" + i;
            BigDecimal score = new BigDecimal(String.valueOf(70 + (i % 30)));
            scoreCache.storeScore(studentId, score);
        }
        
        assertEquals(numberOfStudents, scoreCache.size());
        
        // Bulk update operations
        for (int i = 0; i < numberOfStudents / 2; i++) {
            String studentId = "bulkStudent_" + i;
            BigDecimal newScore = new BigDecimal(String.valueOf(90 + (i % 10)));
            scoreCache.updateScore(studentId, newScore);
        }
        
        // Bulk invalidation
        for (int i = numberOfStudents / 2; i < numberOfStudents; i++) {
            String studentId = "bulkStudent_" + i;
            scoreCache.invalidateScore(studentId);
        }
        
        // Then - verify final state
        assertEquals(numberOfStudents / 2, scoreCache.size());
        
        // Verify updated scores are correct
        for (int i = 0; i < numberOfStudents / 2; i++) {
            String studentId = "bulkStudent_" + i;
            Optional<BigDecimal> score = scoreCache.retrieveScore(studentId);
            assertTrue(score.isPresent());
            assertTrue(score.get().compareTo(new BigDecimal("90")) >= 0);
        }
        
        // Verify invalidated scores are gone
        for (int i = numberOfStudents / 2; i < numberOfStudents; i++) {
            String studentId = "bulkStudent_" + i;
            assertFalse(scoreCache.containsStudent(studentId));
        }
    }
}