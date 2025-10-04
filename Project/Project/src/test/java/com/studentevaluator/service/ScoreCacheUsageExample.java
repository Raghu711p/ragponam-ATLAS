package com.studentevaluator.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Example demonstrating how ScoreCache would be used in a service layer
 * with proper cache invalidation when database updates occur.
 */
@DisplayName("ScoreCache Usage Examples")
class ScoreCacheUsageExample {
    
    private ScoreCache scoreCache;
    
    @BeforeEach
    void setUp() {
        scoreCache = new ScoreCache();
    }
    
    @Test
    @DisplayName("Example: Service layer using cache-aside pattern")
    void exampleServiceLayerCacheAsidePattern() {
        // This demonstrates how a service would use the cache
        String studentId = "student123";
        
        // 1. Try to get score from cache first (cache-aside pattern)
        Optional<BigDecimal> cachedScore = scoreCache.retrieveScore(studentId);
        
        if (cachedScore.isEmpty()) {
            // 2. Cache miss - would fetch from database in real scenario
            BigDecimal scoreFromDatabase = new BigDecimal("87.50"); // Simulated DB fetch
            
            // 3. Store in cache for future lookups
            scoreCache.storeScore(studentId, scoreFromDatabase);
            
            // 4. Return the score
            assertEquals(scoreFromDatabase, scoreCache.retrieveScore(studentId).orElse(null));
        }
    }
    
    @Test
    @DisplayName("Example: Database update with cache invalidation")
    void exampleDatabaseUpdateWithCacheInvalidation() {
        String studentId = "student456";
        BigDecimal originalScore = new BigDecimal("75.00");
        BigDecimal updatedScore = new BigDecimal("92.00");
        
        // 1. Initial score is cached
        scoreCache.storeScore(studentId, originalScore);
        assertEquals(originalScore, scoreCache.retrieveScore(studentId).orElse(null));
        
        // 2. When updating score in database, follow this pattern:
        
        // Step 2a: Invalidate cache BEFORE database update
        scoreCache.invalidateScore(studentId);
        
        // Step 2b: Update database (simulated)
        // In real scenario: evaluationRepository.updateScore(studentId, updatedScore);
        
        // Step 2c: Cache the new value after successful database update
        scoreCache.storeScore(studentId, updatedScore);
        
        // 3. Verify the updated score is now cached
        assertEquals(updatedScore, scoreCache.retrieveScore(studentId).orElse(null));
    }
    
    @Test
    @DisplayName("Example: Bulk score updates with cache management")
    void exampleBulkScoreUpdatesWithCacheManagement() {
        // Simulate bulk evaluation results
        String[] studentIds = {"student1", "student2", "student3"};
        BigDecimal[] scores = {
            new BigDecimal("85.00"),
            new BigDecimal("92.50"),
            new BigDecimal("78.75")
        };
        
        // 1. Cache initial scores
        for (int i = 0; i < studentIds.length; i++) {
            scoreCache.storeScore(studentIds[i], scores[i]);
        }
        
        assertEquals(3, scoreCache.size());
        
        // 2. Simulate bulk database update scenario
        // In a real service, you might want to:
        // - Invalidate all affected cache entries
        // - Perform bulk database update
        // - Re-cache updated values
        
        for (String studentId : studentIds) {
            scoreCache.invalidateScore(studentId);
        }
        
        assertEquals(0, scoreCache.size());
        
        // 3. After database update, cache new values
        BigDecimal[] updatedScores = {
            new BigDecimal("90.00"),
            new BigDecimal("95.00"),
            new BigDecimal("82.50")
        };
        
        for (int i = 0; i < studentIds.length; i++) {
            scoreCache.storeScore(studentIds[i], updatedScores[i]);
        }
        
        // 4. Verify all updated scores are cached
        for (int i = 0; i < studentIds.length; i++) {
            assertEquals(updatedScores[i], scoreCache.retrieveScore(studentIds[i]).orElse(null));
        }
    }
    
    @Test
    @DisplayName("Example: Hash collision handling in production scenario")
    void exampleHashCollisionHandling() {
        // This demonstrates the limitation of using hashCode for keys
        // In production, you might want to consider this behavior
        
        String student1 = "Aa";  // These strings have the same hashCode
        String student2 = "BB";  // in Java due to hash collision
        
        assertEquals(student1.hashCode(), student2.hashCode());
        
        BigDecimal score1 = new BigDecimal("85.00");
        BigDecimal score2 = new BigDecimal("90.00");
        
        // Store first score
        scoreCache.storeScore(student1, score1);
        assertEquals(score1, scoreCache.retrieveScore(student1).orElse(null));
        
        // Store second score - this will overwrite the first due to hash collision
        scoreCache.storeScore(student2, score2);
        
        // Both students will now return the same score due to collision
        assertEquals(score2, scoreCache.retrieveScore(student1).orElse(null));
        assertEquals(score2, scoreCache.retrieveScore(student2).orElse(null));
        
        // Only one entry exists in the cache due to collision
        assertEquals(1, scoreCache.size());
        
        // Note: In production, you might want to use a different key strategy
        // or accept this limitation based on your use case requirements
    }
}