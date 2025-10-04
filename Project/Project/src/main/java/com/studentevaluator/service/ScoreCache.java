package com.studentevaluator.service;

import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Thread-safe cache for storing student scores using hashcodes for quick lookup.
 * Uses ConcurrentHashMap to ensure thread safety in concurrent access scenarios.
 */
@Component
public class ScoreCache {
    
    private static final Logger logger = LoggerFactory.getLogger(ScoreCache.class);
    
    private final Map<Integer, BigDecimal> studentScoreMap = new ConcurrentHashMap<>();
    
    /**
     * Stores a student's score in the cache using the student ID hashcode as key.
     * 
     * @param studentId the student identifier
     * @param score the score to cache
     */
    public void storeScore(String studentId, BigDecimal score) {
        if (studentId == null || score == null) {
            logger.warn("Cannot store score: studentId or score is null");
            return;
        }
        
        int hashKey = studentId.hashCode();
        studentScoreMap.put(hashKey, score);
        logger.debug("Stored score {} for student {} (hash: {})", score, studentId, hashKey);
    }
    
    /**
     * Retrieves a student's score from the cache using the student ID hashcode.
     * 
     * @param studentId the student identifier
     * @return Optional containing the score if found, empty otherwise
     */
    public Optional<BigDecimal> retrieveScore(String studentId) {
        if (studentId == null) {
            logger.warn("Cannot retrieve score: studentId is null");
            return Optional.empty();
        }
        
        int hashKey = studentId.hashCode();
        BigDecimal score = studentScoreMap.get(hashKey);
        
        if (score != null) {
            logger.debug("Retrieved score {} for student {} (hash: {})", score, studentId, hashKey);
        } else {
            logger.debug("No cached score found for student {} (hash: {})", studentId, hashKey);
        }
        
        return Optional.ofNullable(score);
    }
    
    /**
     * Updates a student's score in the cache. If the student doesn't exist in cache,
     * the score will be added.
     * 
     * @param studentId the student identifier
     * @param newScore the new score to update
     */
    public void updateScore(String studentId, BigDecimal newScore) {
        if (studentId == null || newScore == null) {
            logger.warn("Cannot update score: studentId or newScore is null");
            return;
        }
        
        int hashKey = studentId.hashCode();
        BigDecimal oldScore = studentScoreMap.put(hashKey, newScore);
        
        if (oldScore != null) {
            logger.debug("Updated score for student {} (hash: {}) from {} to {}", 
                        studentId, hashKey, oldScore, newScore);
        } else {
            logger.debug("Added new score {} for student {} (hash: {})", 
                        newScore, studentId, hashKey);
        }
    }
    
    /**
     * Invalidates (removes) a student's score from the cache.
     * This should be called when scores are updated in the database.
     * 
     * @param studentId the student identifier whose score should be invalidated
     */
    public void invalidateScore(String studentId) {
        if (studentId == null) {
            logger.warn("Cannot invalidate score: studentId is null");
            return;
        }
        
        int hashKey = studentId.hashCode();
        BigDecimal removedScore = studentScoreMap.remove(hashKey);
        
        if (removedScore != null) {
            logger.debug("Invalidated cached score {} for student {} (hash: {})", 
                        removedScore, studentId, hashKey);
        } else {
            logger.debug("No cached score to invalidate for student {} (hash: {})", 
                        studentId, hashKey);
        }
    }
    
    /**
     * Clears all cached scores. This method should be used with caution
     * as it will remove all cached data.
     */
    public void clearAll() {
        int size = studentScoreMap.size();
        studentScoreMap.clear();
        logger.info("Cleared all cached scores. Removed {} entries", size);
    }
    
    /**
     * Returns the current size of the cache.
     * 
     * @return number of cached score entries
     */
    public int size() {
        return studentScoreMap.size();
    }
    
    /**
     * Checks if the cache contains a score for the given student.
     * 
     * @param studentId the student identifier
     * @return true if cache contains score for student, false otherwise
     */
    public boolean containsStudent(String studentId) {
        if (studentId == null) {
            return false;
        }
        return studentScoreMap.containsKey(studentId.hashCode());
    }
    
    /**
     * Updates a student's score in the cache using Double value.
     * Convenience method for API controllers.
     * 
     * @param studentId the student identifier
     * @param score the score as Double
     */
    public void updateScore(String studentId, Double score) {
        if (score != null) {
            updateScore(studentId, BigDecimal.valueOf(score));
        }
    }
    
    /**
     * Gets all cached scores as a map of student IDs to scores.
     * Note: This reconstructs student IDs from hashcodes, which may have collisions.
     * 
     * @return Map of student hash codes to scores
     */
    public Map<String, Double> getAllScores() {
        Map<String, Double> result = new ConcurrentHashMap<>();
        studentScoreMap.forEach((hash, score) -> {
            // We can't reconstruct the original student ID from hash, so use hash as key
            result.put("hash_" + hash, score.doubleValue());
        });
        return result;
    }
    
    /**
     * Clears all cached scores. Alias for clearAll() for consistency.
     */
    public void clearAllScores() {
        clearAll();
    }
}