package com.studentevaluator.model;

/**
 * Enumeration representing the status of an evaluation process.
 */
public enum EvaluationStatus {
    PENDING("Evaluation is pending"),
    IN_PROGRESS("Evaluation is in progress"),
    COMPLETED("Evaluation completed successfully"),
    FAILED("Evaluation failed");
    
    private final String description;
    
    EvaluationStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}