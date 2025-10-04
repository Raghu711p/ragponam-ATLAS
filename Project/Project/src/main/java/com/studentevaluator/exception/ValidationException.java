package com.studentevaluator.exception;

/**
 * Exception thrown when input validation fails
 */
public class ValidationException extends EvaluationException {
    
    public ValidationException(String message) {
        super(message);
    }
    
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}