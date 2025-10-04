package com.studentevaluator.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for EvaluationException and its hierarchy.
 */
class EvaluationExceptionTest {
    
    @Test
    void testEvaluationExceptionBasicConstructor() {
        String message = "Test evaluation error";
        EvaluationException exception = new EvaluationException(message);
        
        assertEquals(message, exception.getMessage());
        assertEquals("EVALUATION_ERROR", exception.getErrorCode());
        assertNull(exception.getDetails());
    }
    
    @Test
    void testEvaluationExceptionWithCause() {
        String message = "Test evaluation error";
        RuntimeException cause = new RuntimeException("Root cause");
        EvaluationException exception = new EvaluationException(message, cause);
        
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
        assertEquals("EVALUATION_ERROR", exception.getErrorCode());
    }
    
    @Test
    void testEvaluationExceptionWithErrorCode() {
        String errorCode = "CUSTOM_ERROR";
        String message = "Custom error message";
        EvaluationException exception = new EvaluationException(errorCode, message);
        
        assertEquals(message, exception.getMessage());
        assertEquals(errorCode, exception.getErrorCode());
        assertNull(exception.getDetails());
    }
    
    @Test
    void testEvaluationExceptionWithDetails() {
        String errorCode = "CUSTOM_ERROR";
        String message = "Custom error message";
        String details = "Additional error details";
        EvaluationException exception = new EvaluationException(errorCode, message, details);
        
        assertEquals(message, exception.getMessage());
        assertEquals(errorCode, exception.getErrorCode());
        assertEquals(details, exception.getDetails());
    }
    
    @Test
    void testEvaluationExceptionFullConstructor() {
        String errorCode = "CUSTOM_ERROR";
        String message = "Custom error message";
        String details = "Additional error details";
        RuntimeException cause = new RuntimeException("Root cause");
        EvaluationException exception = new EvaluationException(errorCode, message, details, cause);
        
        assertEquals(message, exception.getMessage());
        assertEquals(errorCode, exception.getErrorCode());
        assertEquals(details, exception.getDetails());
        assertEquals(cause, exception.getCause());
    }
}