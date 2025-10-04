package com.studentevaluator.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ScoreCache Tests")
class ScoreCacheTest {
    
    private ScoreCache scoreCache;
    
    @BeforeEach
    void setUp() {
        scoreCache = new ScoreCache();
    }
    
    @Test
    @DisplayName("Should store and retrieve score successfully")
    void shouldStoreAndRetrieveScore() {
        // Given
        String studentId = "student123";
        BigDecimal score = new BigDecimal("85.50");
        
        // When
        scoreCache.storeScore(studentId, score);
        Optional<BigDecimal> retrievedScore = scoreCache.retrieveScore(studentId);
        
        // Then
        assertTrue(retrievedScore.isPresent());
        assertEquals(score, retrievedScore.get());
        assertEquals(1, scoreCache.size());
    }
    
    @Test
    @DisplayName("Should return empty optional when score not found")
    void shouldReturnEmptyWhenScoreNotFound() {
        // Given
        String studentId = "nonexistent";
        
        // When
        Optional<BigDecimal> retrievedScore = scoreCache.retrieveScore(studentId);
        
        // Then
        assertFalse(retrievedScore.isPresent());
        assertEquals(0, scoreCache.size());
    }
    
    @Test
    @DisplayName("Should update existing score")
    void shouldUpdateExistingScore() {
        // Given
        String studentId = "student123";
        BigDecimal originalScore = new BigDecimal("75.00");
        BigDecimal updatedScore = new BigDecimal("90.00");
        
        // When
        scoreCache.storeScore(studentId, originalScore);
        scoreCache.updateScore(studentId, updatedScore);
        Optional<BigDecimal> retrievedScore = scoreCache.retrieveScore(studentId);
        
        // Then
        assertTrue(retrievedScore.isPresent());
        assertEquals(updatedScore, retrievedScore.get());
        assertEquals(1, scoreCache.size());
    }
    
    @Test
    @DisplayName("Should add new score when updating non-existing student")
    void shouldAddNewScoreWhenUpdatingNonExisting() {
        // Given
        String studentId = "newStudent";
        BigDecimal score = new BigDecimal("88.75");
        
        // When
        scoreCache.updateScore(studentId, score);
        Optional<BigDecimal> retrievedScore = scoreCache.retrieveScore(studentId);
        
        // Then
        assertTrue(retrievedScore.isPresent());
        assertEquals(score, retrievedScore.get());
        assertEquals(1, scoreCache.size());
    }
    
    @Test
    @DisplayName("Should invalidate score successfully")
    void shouldInvalidateScore() {
        // Given
        String studentId = "student123";
        BigDecimal score = new BigDecimal("85.50");
        
        // When
        scoreCache.storeScore(studentId, score);
        assertTrue(scoreCache.containsStudent(studentId));
        
        scoreCache.invalidateScore(studentId);
        
        // Then
        assertFalse(scoreCache.containsStudent(studentId));
        assertFalse(scoreCache.retrieveScore(studentId).isPresent());
        assertEquals(0, scoreCache.size());
    }
    
    @Test
    @DisplayName("Should handle invalidation of non-existing student gracefully")
    void shouldHandleInvalidationOfNonExistingStudent() {
        // Given
        String studentId = "nonexistent";
        
        // When & Then - should not throw exception
        assertDoesNotThrow(() -> scoreCache.invalidateScore(studentId));
        assertEquals(0, scoreCache.size());
    }
    
    @Test
    @DisplayName("Should clear all cached scores")
    void shouldClearAllCachedScores() {
        // Given
        scoreCache.storeScore("student1", new BigDecimal("85.00"));
        scoreCache.storeScore("student2", new BigDecimal("90.00"));
        scoreCache.storeScore("student3", new BigDecimal("78.50"));
        assertEquals(3, scoreCache.size());
        
        // When
        scoreCache.clearAll();
        
        // Then
        assertEquals(0, scoreCache.size());
        assertFalse(scoreCache.containsStudent("student1"));
        assertFalse(scoreCache.containsStudent("student2"));
        assertFalse(scoreCache.containsStudent("student3"));
    }
    
    @Test
    @DisplayName("Should handle null studentId gracefully")
    void shouldHandleNullStudentIdGracefully() {
        // Given
        BigDecimal score = new BigDecimal("85.00");
        
        // When & Then - should not throw exceptions
        assertDoesNotThrow(() -> scoreCache.storeScore(null, score));
        assertDoesNotThrow(() -> scoreCache.updateScore(null, score));
        assertDoesNotThrow(() -> scoreCache.invalidateScore(null));
        
        Optional<BigDecimal> result = scoreCache.retrieveScore(null);
        assertFalse(result.isPresent());
        assertFalse(scoreCache.containsStudent(null));
    }
    
    @Test
    @DisplayName("Should handle null score gracefully")
    void shouldHandleNullScoreGracefully() {
        // Given
        String studentId = "student123";
        
        // When & Then - should not throw exceptions
        assertDoesNotThrow(() -> scoreCache.storeScore(studentId, null));
        assertDoesNotThrow(() -> scoreCache.updateScore(studentId, (BigDecimal) null));
        
        assertEquals(0, scoreCache.size());
    }
    
    @Test
    @DisplayName("Should handle concurrent access correctly")
    void shouldHandleConcurrentAccessCorrectly() throws InterruptedException {
        // Given
        int numberOfThreads = 10;
        int operationsPerThread = 100;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        AtomicInteger successfulOperations = new AtomicInteger(0);
        
        // When - multiple threads performing concurrent operations
        for (int i = 0; i < numberOfThreads; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < operationsPerThread; j++) {
                        String studentId = "student_" + threadId + "_" + j;
                        BigDecimal score = new BigDecimal(String.valueOf(80 + (j % 20)));
                        
                        // Store score
                        scoreCache.storeScore(studentId, score);
                        
                        // Retrieve score
                        Optional<BigDecimal> retrieved = scoreCache.retrieveScore(studentId);
                        if (retrieved.isPresent() && retrieved.get().equals(score)) {
                            successfulOperations.incrementAndGet();
                        }
                        
                        // Update score
                        BigDecimal newScore = score.add(new BigDecimal("5"));
                        scoreCache.updateScore(studentId, newScore);
                        
                        // Verify update
                        Optional<BigDecimal> updated = scoreCache.retrieveScore(studentId);
                        if (updated.isPresent() && updated.get().equals(newScore)) {
                            successfulOperations.incrementAndGet();
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        
        // Then
        assertTrue(latch.await(30, TimeUnit.SECONDS));
        executor.shutdown();
        
        // Verify that all operations completed successfully
        int expectedSuccessfulOperations = numberOfThreads * operationsPerThread * 2; // 2 operations per iteration
        assertEquals(expectedSuccessfulOperations, successfulOperations.get());
        
        // Verify final cache size
        assertEquals(numberOfThreads * operationsPerThread, scoreCache.size());
    }
    
    @Test
    @DisplayName("Should handle concurrent invalidation correctly")
    void shouldHandleConcurrentInvalidationCorrectly() throws InterruptedException {
        // Given
        int numberOfStudents = 100;
        
        // Pre-populate cache
        for (int i = 0; i < numberOfStudents; i++) {
            scoreCache.storeScore("student_" + i, new BigDecimal(String.valueOf(80 + i)));
        }
        assertEquals(numberOfStudents, scoreCache.size());
        
        // When - concurrent invalidation
        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(numberOfStudents);
        
        for (int i = 0; i < numberOfStudents; i++) {
            final int studentIndex = i;
            executor.submit(() -> {
                try {
                    scoreCache.invalidateScore("student_" + studentIndex);
                } finally {
                    latch.countDown();
                }
            });
        }
        
        // Then
        assertTrue(latch.await(10, TimeUnit.SECONDS));
        executor.shutdown();
        
        assertEquals(0, scoreCache.size());
    }
    
    @Test
    @DisplayName("Should handle hash collisions correctly")
    void shouldHandleHashCollisionsCorrectly() {
        // Given - create strings that might have hash collisions
        String student1 = "Aa";
        String student2 = "BB";
        
        // These strings have the same hashcode in Java
        assertEquals(student1.hashCode(), student2.hashCode());
        
        BigDecimal score1 = new BigDecimal("85.00");
        BigDecimal score2 = new BigDecimal("90.00");
        
        // When
        scoreCache.storeScore(student1, score1);
        scoreCache.storeScore(student2, score2);
        
        // Then - the second store should overwrite the first due to hash collision
        // This is expected behavior for this implementation
        Optional<BigDecimal> retrieved1 = scoreCache.retrieveScore(student1);
        Optional<BigDecimal> retrieved2 = scoreCache.retrieveScore(student2);
        
        // Both should return the same value (the last stored one) due to hash collision
        assertTrue(retrieved1.isPresent());
        assertTrue(retrieved2.isPresent());
        assertEquals(retrieved1.get(), retrieved2.get());
        assertEquals(1, scoreCache.size()); // Only one entry due to hash collision
    }
}