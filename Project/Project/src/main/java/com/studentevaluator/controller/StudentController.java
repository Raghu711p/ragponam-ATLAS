package com.studentevaluator.controller;

import com.studentevaluator.dto.ErrorResponse;
import com.studentevaluator.dto.EvaluationResponse;
import com.studentevaluator.dto.StudentScoreResponse;
import com.studentevaluator.model.Evaluation;
import com.studentevaluator.service.EvaluationService;
import com.studentevaluator.service.EvaluationService.StudentEvaluationStats;
import com.studentevaluator.service.ScoreCache;

import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * REST controller for student operations including score retrieval and evaluation history.
 */
@RestController
@RequestMapping("/api/v1/students")
@CrossOrigin(origins = "*")
public class StudentController {
    
    private static final Logger logger = LoggerFactory.getLogger(StudentController.class);
    
    private final EvaluationService evaluationService;
    private final ScoreCache scoreCache;
    
    @Autowired
    public StudentController(EvaluationService evaluationService, ScoreCache scoreCache) {
        this.evaluationService = evaluationService;
        this.scoreCache = scoreCache;
    }
    
    /**
     * Get student's current score from cache.
     * GET /api/v1/students/{studentId}/score
     */
    @GetMapping("/{studentId}/score")
    public ResponseEntity<?> getStudentScore(@PathVariable String studentId) {
        logger.debug("Retrieving score for student: {}", studentId);
        
        try {
            Optional<BigDecimal> cachedScore = scoreCache.retrieveScore(studentId);
            
            if (cachedScore.isPresent()) {
                StudentScoreResponse response = new StudentScoreResponse(studentId, cachedScore.get().doubleValue());
                return ResponseEntity.ok(response);
            } else {
                ErrorResponse error = new ErrorResponse("SCORE_NOT_FOUND", 
                    "No cached score found for student: " + studentId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            
        } catch (Exception e) {
            logger.error("Error retrieving score for student: {}", studentId, e);
            ErrorResponse error = new ErrorResponse("INTERNAL_ERROR", "An unexpected error occurred");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Get comprehensive student score information including statistics.
     * GET /api/v1/students/{studentId}/scores
     */
    @GetMapping("/{studentId}/scores")
    public ResponseEntity<?> getStudentScores(@PathVariable String studentId) {
        logger.debug("Retrieving comprehensive scores for student: {}", studentId);
        
        try {
            // Get cached score
            Optional<BigDecimal> cachedScore = scoreCache.retrieveScore(studentId);
            
            // Get evaluation statistics
            Optional<StudentEvaluationStats> stats = evaluationService.getStudentEvaluationStats(studentId);
            
            if (stats.isPresent()) {
                Double scoreValue = cachedScore.map(BigDecimal::doubleValue).orElse(null);
                StudentScoreResponse response = new StudentScoreResponse(stats.get(), scoreValue);
                return ResponseEntity.ok(response);
            } else if (cachedScore.isPresent()) {
                // Return basic score if no stats available
                StudentScoreResponse response = new StudentScoreResponse(studentId, cachedScore.get().doubleValue());
                return ResponseEntity.ok(response);
            } else {
                ErrorResponse error = new ErrorResponse("STUDENT_DATA_NOT_FOUND", 
                    "No score data found for student: " + studentId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            
        } catch (Exception e) {
            logger.error("Error retrieving comprehensive scores for student: {}", studentId, e);
            ErrorResponse error = new ErrorResponse("INTERNAL_ERROR", "An unexpected error occurred");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Get student's evaluation history.
     * GET /api/v1/students/{studentId}/evaluations
     */
    @GetMapping("/{studentId}/evaluations")
    public ResponseEntity<?> getStudentEvaluationHistory(@PathVariable String studentId) {
        logger.debug("Retrieving evaluation history for student: {}", studentId);
        
        try {
            List<Evaluation> evaluations = evaluationService.getStudentEvaluationHistory(studentId);
            
            List<EvaluationResponse> responses = evaluations.stream()
                    .map(EvaluationResponse::new)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(responses);
            
        } catch (Exception e) {
            logger.error("Error retrieving evaluation history for student: {}", studentId, e);
            ErrorResponse error = new ErrorResponse("INTERNAL_ERROR", "An unexpected error occurred");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Get student's evaluation statistics.
     * GET /api/v1/students/{studentId}/stats
     */
    @GetMapping("/{studentId}/stats")
    public ResponseEntity<?> getStudentEvaluationStats(@PathVariable String studentId) {
        logger.debug("Retrieving evaluation statistics for student: {}", studentId);
        
        try {
            Optional<StudentEvaluationStats> stats = evaluationService.getStudentEvaluationStats(studentId);
            
            if (stats.isPresent()) {
                // Get cached score for complete response
                Optional<BigDecimal> cachedScore = scoreCache.retrieveScore(studentId);
                Double scoreValue = cachedScore.map(BigDecimal::doubleValue).orElse(null);
                StudentScoreResponse response = new StudentScoreResponse(stats.get(), scoreValue);
                return ResponseEntity.ok(response);
            } else {
                ErrorResponse error = new ErrorResponse("STATS_NOT_FOUND", 
                    "No evaluation statistics found for student: " + studentId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            
        } catch (Exception e) {
            logger.error("Error retrieving evaluation statistics for student: {}", studentId, e);
            ErrorResponse error = new ErrorResponse("INTERNAL_ERROR", "An unexpected error occurred");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Update student's cached score.
     * PUT /api/v1/students/{studentId}/score
     */
    @PutMapping("/{studentId}/score")
    public ResponseEntity<?> updateStudentScore(
            @PathVariable String studentId,
            @RequestParam("score") Double score) {
        
        logger.info("Updating cached score for student: {} to: {}", studentId, score);
        
        try {
            if (score == null || score < 0) {
                ErrorResponse error = new ErrorResponse("INVALID_SCORE", 
                    "Score must be a non-negative number");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            scoreCache.updateScore(studentId, BigDecimal.valueOf(score));
            
            StudentScoreResponse response = new StudentScoreResponse(studentId, score);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error updating score for student: {}", studentId, e);
            ErrorResponse error = new ErrorResponse("INTERNAL_ERROR", "An unexpected error occurred");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Clear student's cached score.
     * DELETE /api/v1/students/{studentId}/score
     */
    @DeleteMapping("/{studentId}/score")
    public ResponseEntity<?> clearStudentScore(@PathVariable String studentId) {
        logger.info("Clearing cached score for student: {}", studentId);
        
        try {
            boolean removed = scoreCache.containsStudent(studentId);
            if (removed) {
                scoreCache.invalidateScore(studentId);
            }
            
            if (removed) {
                return ResponseEntity.noContent().build();
            } else {
                ErrorResponse error = new ErrorResponse("SCORE_NOT_FOUND", 
                    "No cached score found for student: " + studentId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            
        } catch (Exception e) {
            logger.error("Error clearing score for student: {}", studentId, e);
            ErrorResponse error = new ErrorResponse("INTERNAL_ERROR", "An unexpected error occurred");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Get all cached student scores.
     * GET /api/v1/students/scores
     */
    @GetMapping("/scores")
    public ResponseEntity<?> getAllStudentScores() {
        logger.debug("Retrieving all cached student scores");
        
        try {
            Map<String, Double> allScores = scoreCache.getAllScores();
            List<StudentScoreResponse> responses = allScores.entrySet().stream()
                    .map(entry -> new StudentScoreResponse(entry.getKey(), entry.getValue()))
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(responses);
            
        } catch (Exception e) {
            logger.error("Error retrieving all student scores", e);
            ErrorResponse error = new ErrorResponse("INTERNAL_ERROR", "An unexpected error occurred");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}