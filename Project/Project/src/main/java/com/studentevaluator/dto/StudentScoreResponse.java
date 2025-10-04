package com.studentevaluator.dto;

import java.math.BigDecimal;

import com.studentevaluator.service.EvaluationService.StudentEvaluationStats;

/**
 * DTO for student score responses.
 */
public class StudentScoreResponse {
    
    private String studentId;
    private Double currentScore;
    private int totalEvaluations;
    private BigDecimal averageScore;
    private BigDecimal minScore;
    private BigDecimal maxScore;
    
    public StudentScoreResponse() {}
    
    public StudentScoreResponse(String studentId, Double currentScore) {
        this.studentId = studentId;
        this.currentScore = currentScore;
    }
    
    public StudentScoreResponse(StudentEvaluationStats stats, Double currentScore) {
        this.studentId = stats.getStudentId();
        this.currentScore = currentScore;
        this.totalEvaluations = stats.getTotalEvaluations();
        this.averageScore = stats.getAverageScore();
        this.minScore = stats.getMinScore();
        this.maxScore = stats.getMaxScore();
    }
    
    public String getStudentId() {
        return studentId;
    }
    
    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }
    
    public Double getCurrentScore() {
        return currentScore;
    }
    
    public void setCurrentScore(Double currentScore) {
        this.currentScore = currentScore;
    }
    
    public int getTotalEvaluations() {
        return totalEvaluations;
    }
    
    public void setTotalEvaluations(int totalEvaluations) {
        this.totalEvaluations = totalEvaluations;
    }
    
    public BigDecimal getAverageScore() {
        return averageScore;
    }
    
    public void setAverageScore(BigDecimal averageScore) {
        this.averageScore = averageScore;
    }
    
    public BigDecimal getMinScore() {
        return minScore;
    }
    
    public void setMinScore(BigDecimal minScore) {
        this.minScore = minScore;
    }
    
    public BigDecimal getMaxScore() {
        return maxScore;
    }
    
    public void setMaxScore(BigDecimal maxScore) {
        this.maxScore = maxScore;
    }
}