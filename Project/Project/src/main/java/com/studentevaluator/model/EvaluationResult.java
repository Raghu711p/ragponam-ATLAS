package com.studentevaluator.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Model class representing the complete result of an evaluation process.
 * Combines compilation and test execution results with final scoring.
 */
public class EvaluationResult {
    
    private String evaluationId;
    private CompilationResult compilationResult;
    private TestExecutionResult testResult;
    private BigDecimal finalScore;
    private BigDecimal maxScore;
    private EvaluationStatus status;
    private LocalDateTime evaluationTime;
    private String errorMessage;
    
    // Default constructor
    public EvaluationResult() {
        this.evaluationTime = LocalDateTime.now();
    }
    
    // Constructor with basic fields
    public EvaluationResult(String evaluationId, EvaluationStatus status) {
        this.evaluationId = evaluationId;
        this.status = status;
        this.evaluationTime = LocalDateTime.now();
    }
    
    // Constructor for successful evaluation
    public EvaluationResult(String evaluationId, CompilationResult compilationResult, 
                           TestExecutionResult testResult, BigDecimal finalScore, 
                           BigDecimal maxScore, EvaluationStatus status) {
        this.evaluationId = evaluationId;
        this.compilationResult = compilationResult;
        this.testResult = testResult;
        this.finalScore = finalScore;
        this.maxScore = maxScore;
        this.status = status;
        this.evaluationTime = LocalDateTime.now();
    }
    
    // Constructor for failed evaluation
    public EvaluationResult(String evaluationId, EvaluationStatus status, String errorMessage) {
        this.evaluationId = evaluationId;
        this.status = status;
        this.errorMessage = errorMessage;
        this.evaluationTime = LocalDateTime.now();
    }
    
    // Getters and Setters
    public String getEvaluationId() {
        return evaluationId;
    }
    
    public void setEvaluationId(String evaluationId) {
        this.evaluationId = evaluationId;
    }
    
    public CompilationResult getCompilationResult() {
        return compilationResult;
    }
    
    public void setCompilationResult(CompilationResult compilationResult) {
        this.compilationResult = compilationResult;
    }
    
    public TestExecutionResult getTestResult() {
        return testResult;
    }
    
    public void setTestResult(TestExecutionResult testResult) {
        this.testResult = testResult;
    }
    
    public BigDecimal getFinalScore() {
        return finalScore;
    }
    
    public void setFinalScore(BigDecimal finalScore) {
        this.finalScore = finalScore;
    }
    
    public BigDecimal getMaxScore() {
        return maxScore;
    }
    
    public void setMaxScore(BigDecimal maxScore) {
        this.maxScore = maxScore;
    }
    
    public EvaluationStatus getStatus() {
        return status;
    }
    
    public void setStatus(EvaluationStatus status) {
        this.status = status;
    }
    
    public LocalDateTime getEvaluationTime() {
        return evaluationTime;
    }
    
    public void setEvaluationTime(LocalDateTime evaluationTime) {
        this.evaluationTime = evaluationTime;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    // Utility methods
    public boolean isSuccessful() {
        return status == EvaluationStatus.COMPLETED && compilationResult != null && compilationResult.isSuccessful();
    }
    
    public boolean hasCompilationErrors() {
        return compilationResult != null && !compilationResult.isSuccessful();
    }
    
    public boolean hasTestFailures() {
        return testResult != null && testResult.hasFailures();
    }
    
    public double getScorePercentage() {
        if (maxScore == null || maxScore.compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }
        if (finalScore == null) {
            return 0.0;
        }
        return finalScore.divide(maxScore, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .doubleValue();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EvaluationResult that = (EvaluationResult) o;
        return Objects.equals(evaluationId, that.evaluationId) &&
                Objects.equals(compilationResult, that.compilationResult) &&
                Objects.equals(testResult, that.testResult) &&
                Objects.equals(finalScore, that.finalScore) &&
                Objects.equals(maxScore, that.maxScore) &&
                status == that.status &&
                Objects.equals(evaluationTime, that.evaluationTime) &&
                Objects.equals(errorMessage, that.errorMessage);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(evaluationId, compilationResult, testResult, finalScore, 
                           maxScore, status, evaluationTime, errorMessage);
    }
    
    @Override
    public String toString() {
        return "EvaluationResult{" +
                "evaluationId='" + evaluationId + '\'' +
                ", status=" + status +
                ", finalScore=" + finalScore +
                ", maxScore=" + maxScore +
                ", scorePercentage=" + String.format("%.2f", getScorePercentage()) + "%" +
                ", evaluationTime=" + evaluationTime +
                '}';
    }
}