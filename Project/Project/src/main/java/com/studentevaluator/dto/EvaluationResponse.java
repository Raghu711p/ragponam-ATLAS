package com.studentevaluator.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.studentevaluator.model.Evaluation;
import com.studentevaluator.model.EvaluationResult;
import com.studentevaluator.model.EvaluationStatus;

/**
 * DTO for evaluation responses.
 */
public class EvaluationResponse {
    
    private String evaluationId;
    private String studentId;
    private String assignmentId;
    private BigDecimal score;
    private BigDecimal maxScore;
    private EvaluationStatus status;
    private LocalDateTime evaluatedAt;
    private String errorMessage;
    
    public EvaluationResponse() {}
    
    public EvaluationResponse(Evaluation evaluation) {
        this.evaluationId = evaluation.getEvaluationId();
        this.studentId = evaluation.getStudentId();
        this.assignmentId = evaluation.getAssignmentId();
        this.score = evaluation.getScore();
        this.maxScore = evaluation.getMaxScore();
        this.status = evaluation.getStatus();
        this.evaluatedAt = evaluation.getEvaluatedAt();
    }
    
    public EvaluationResponse(EvaluationResult result) {
        this.evaluationId = result.getEvaluationId();
        this.score = result.getFinalScore();
        this.maxScore = result.getMaxScore();
        this.status = result.getStatus();
        this.evaluatedAt = result.getEvaluationTime();
        this.errorMessage = result.getErrorMessage();
    }
    
    public String getEvaluationId() {
        return evaluationId;
    }
    
    public void setEvaluationId(String evaluationId) {
        this.evaluationId = evaluationId;
    }
    
    public String getStudentId() {
        return studentId;
    }
    
    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }
    
    public String getAssignmentId() {
        return assignmentId;
    }
    
    public void setAssignmentId(String assignmentId) {
        this.assignmentId = assignmentId;
    }
    
    public BigDecimal getScore() {
        return score;
    }
    
    public void setScore(BigDecimal score) {
        this.score = score;
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
    
    public LocalDateTime getEvaluatedAt() {
        return evaluatedAt;
    }
    
    public void setEvaluatedAt(LocalDateTime evaluatedAt) {
        this.evaluatedAt = evaluatedAt;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}