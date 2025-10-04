package com.studentevaluator.exception;

/**
 * Base exception for all evaluation-related errors in the Student Evaluator System.
 * This serves as the parent class for all custom exceptions in the application.
 */
public class EvaluationException extends RuntimeException {
    
    private final String errorCode;
    private final Object details;
    
    public EvaluationException(String message) {
        super(message);
        this.errorCode = "EVALUATION_ERROR";
        this.details = null;
    }
    
    public EvaluationException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "EVALUATION_ERROR";
        this.details = null;
    }
    
    public EvaluationException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.details = null;
    }
    
    public EvaluationException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.details = null;
    }
    
    public EvaluationException(String errorCode, String message, Object details) {
        super(message);
        this.errorCode = errorCode;
        this.details = details;
    }
    
    public EvaluationException(String errorCode, String message, Object details, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.details = details;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public Object getDetails() {
        return details;
    }
}