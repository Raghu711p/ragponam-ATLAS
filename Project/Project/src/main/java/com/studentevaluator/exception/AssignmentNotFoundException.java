package com.studentevaluator.exception;

/**
 * Exception thrown when an assignment is not found.
 */
public class AssignmentNotFoundException extends RuntimeException {
    
    public AssignmentNotFoundException(String message) {
        super(message);
    }
    
    public AssignmentNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}