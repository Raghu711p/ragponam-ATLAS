package com.studentevaluator.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EvaluationResult Model Tests")
class EvaluationResultTest {
    
    @Test
    @DisplayName("Should create evaluation result with basic fields")
    void shouldCreateEvaluationResultWithBasicFields() {
        // Given
        String evaluationId = "EVAL001";
        EvaluationStatus status = EvaluationStatus.PENDING;
        
        // When
        EvaluationResult result = new EvaluationResult(evaluationId, status);
        
        // Then
        assertEquals(evaluationId, result.getEvaluationId());
        assertEquals(status, result.getStatus());
        assertNotNull(result.getEvaluationTime());
        assertTrue(result.getEvaluationTime().isBefore(LocalDateTime.now().plusSeconds(1)));
    }
    
    @Test
    @DisplayName("Should create successful evaluation result")
    void shouldCreateSuccessfulEvaluationResult() {
        // Given
        String evaluationId = "EVAL001";
        CompilationResult compilationResult = new CompilationResult(true, "Success", "/path/class");
        TestExecutionResult testResult = new TestExecutionResult(10, 8, 2);
        BigDecimal finalScore = new BigDecimal("80.00");
        BigDecimal maxScore = new BigDecimal("100.00");
        EvaluationStatus status = EvaluationStatus.COMPLETED;
        
        // When
        EvaluationResult result = new EvaluationResult(evaluationId, compilationResult, 
                testResult, finalScore, maxScore, status);
        
        // Then
        assertEquals(evaluationId, result.getEvaluationId());
        assertEquals(compilationResult, result.getCompilationResult());
        assertEquals(testResult, result.getTestResult());
        assertEquals(finalScore, result.getFinalScore());
        assertEquals(maxScore, result.getMaxScore());
        assertEquals(status, result.getStatus());
        assertNotNull(result.getEvaluationTime());
    }
    
    @Test
    @DisplayName("Should create failed evaluation result")
    void shouldCreateFailedEvaluationResult() {
        // Given
        String evaluationId = "EVAL001";
        EvaluationStatus status = EvaluationStatus.FAILED;
        String errorMessage = "Compilation failed";
        
        // When
        EvaluationResult result = new EvaluationResult(evaluationId, status, errorMessage);
        
        // Then
        assertEquals(evaluationId, result.getEvaluationId());
        assertEquals(status, result.getStatus());
        assertEquals(errorMessage, result.getErrorMessage());
        assertNotNull(result.getEvaluationTime());
    }
    
    @Test
    @DisplayName("Should determine if evaluation is successful")
    void shouldDetermineIfEvaluationIsSuccessful() {
        // Given
        CompilationResult successfulCompilation = new CompilationResult(true, "Success", "/path");
        CompilationResult failedCompilation = new CompilationResult(false, "Failed", Arrays.asList("Error"));
        
        EvaluationResult successfulResult = new EvaluationResult("EVAL001", successfulCompilation, 
                new TestExecutionResult(), BigDecimal.TEN, BigDecimal.TEN, EvaluationStatus.COMPLETED);
        
        EvaluationResult failedCompilationResult = new EvaluationResult("EVAL002", failedCompilation, 
                null, null, null, EvaluationStatus.FAILED);
        
        EvaluationResult pendingResult = new EvaluationResult("EVAL003", EvaluationStatus.PENDING);
        
        // Then
        assertTrue(successfulResult.isSuccessful());
        assertFalse(failedCompilationResult.isSuccessful());
        assertFalse(pendingResult.isSuccessful());
    }
    
    @Test
    @DisplayName("Should determine if has compilation errors")
    void shouldDetermineIfHasCompilationErrors() {
        // Given
        CompilationResult successfulCompilation = new CompilationResult(true, "Success", "/path");
        CompilationResult failedCompilation = new CompilationResult(false, "Failed", Arrays.asList("Error"));
        
        EvaluationResult successfulResult = new EvaluationResult();
        successfulResult.setCompilationResult(successfulCompilation);
        
        EvaluationResult failedResult = new EvaluationResult();
        failedResult.setCompilationResult(failedCompilation);
        
        EvaluationResult noCompilationResult = new EvaluationResult();
        
        // Then
        assertFalse(successfulResult.hasCompilationErrors());
        assertTrue(failedResult.hasCompilationErrors());
        assertFalse(noCompilationResult.hasCompilationErrors());
    }
    
    @Test
    @DisplayName("Should determine if has test failures")
    void shouldDetermineIfHasTestFailures() {
        // Given
        TestExecutionResult successfulTests = new TestExecutionResult(5, 5, 0);
        TestExecutionResult failedTests = new TestExecutionResult(5, 3, 2);
        
        EvaluationResult successfulResult = new EvaluationResult();
        successfulResult.setTestResult(successfulTests);
        
        EvaluationResult failedResult = new EvaluationResult();
        failedResult.setTestResult(failedTests);
        
        EvaluationResult noTestResult = new EvaluationResult();
        
        // Then
        assertFalse(successfulResult.hasTestFailures());
        assertTrue(failedResult.hasTestFailures());
        assertFalse(noTestResult.hasTestFailures());
    }
    
    @Test
    @DisplayName("Should calculate score percentage correctly")
    void shouldCalculateScorePercentageCorrectly() {
        // Given
        EvaluationResult result1 = new EvaluationResult();
        result1.setFinalScore(new BigDecimal("80.00"));
        result1.setMaxScore(new BigDecimal("100.00"));
        
        EvaluationResult result2 = new EvaluationResult();
        result2.setFinalScore(new BigDecimal("45.50"));
        result2.setMaxScore(new BigDecimal("50.00"));
        
        EvaluationResult result3 = new EvaluationResult();
        result3.setFinalScore(null);
        result3.setMaxScore(new BigDecimal("100.00"));
        
        EvaluationResult result4 = new EvaluationResult();
        result4.setFinalScore(new BigDecimal("50.00"));
        result4.setMaxScore(BigDecimal.ZERO);
        
        // Then
        assertEquals(80.0, result1.getScorePercentage(), 0.01);
        assertEquals(91.0, result2.getScorePercentage(), 0.01);
        assertEquals(0.0, result3.getScorePercentage(), 0.01);
        assertEquals(0.0, result4.getScorePercentage(), 0.01);
    }
    
    @Test
    @DisplayName("Should handle null max score in percentage calculation")
    void shouldHandleNullMaxScoreInPercentageCalculation() {
        // Given
        EvaluationResult result = new EvaluationResult();
        result.setFinalScore(new BigDecimal("80.00"));
        result.setMaxScore(null);
        
        // Then
        assertEquals(0.0, result.getScorePercentage(), 0.01);
    }
    
    @Test
    @DisplayName("Should implement equals and hashCode correctly")
    void shouldImplementEqualsAndHashCodeCorrectly() {
        // Given
        CompilationResult compilation = new CompilationResult(true, "Success", "/path");
        TestExecutionResult testExecution = new TestExecutionResult(5, 5, 0);
        
        EvaluationResult result1 = new EvaluationResult("EVAL001", compilation, testExecution, 
                new BigDecimal("100"), new BigDecimal("100"), EvaluationStatus.COMPLETED);
        
        EvaluationResult result2 = new EvaluationResult("EVAL001", compilation, testExecution, 
                new BigDecimal("100"), new BigDecimal("100"), EvaluationStatus.COMPLETED);
        
        EvaluationResult result3 = new EvaluationResult("EVAL002", compilation, testExecution, 
                new BigDecimal("80"), new BigDecimal("100"), EvaluationStatus.COMPLETED);
        
        // Set same evaluation time for comparison
        LocalDateTime sameTime = LocalDateTime.now();
        result1.setEvaluationTime(sameTime);
        result2.setEvaluationTime(sameTime);
        
        // Then
        assertEquals(result1, result2);
        assertNotEquals(result1, result3);
        assertEquals(result1.hashCode(), result2.hashCode());
        assertNotEquals(result1.hashCode(), result3.hashCode());
    }
    
    @Test
    @DisplayName("Should implement toString correctly")
    void shouldImplementToStringCorrectly() {
        // Given
        EvaluationResult result = new EvaluationResult("EVAL001", EvaluationStatus.COMPLETED);
        result.setFinalScore(new BigDecimal("85.50"));
        result.setMaxScore(new BigDecimal("100.00"));
        
        // When
        String toString = result.toString();
        
        // Then
        assertTrue(toString.contains("EVAL001"));
        assertTrue(toString.contains("COMPLETED"));
        assertTrue(toString.contains("85.50"));
        assertTrue(toString.contains("100.00"));
        assertTrue(toString.contains("85.50%"));
    }
}