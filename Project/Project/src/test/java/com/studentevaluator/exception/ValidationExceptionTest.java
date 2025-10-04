package com.studentevaluator.exception;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.studentevaluator.exception.ValidationException.ValidationErrorDetails;

/**
 * Unit tests for ValidationException.
 */
class ValidationExceptionTest {
    
    @Test
    void testValidationExceptionBasicConstructor() {
        String message = "Validation failed";
        ValidationException exception = new ValidationException(message);
        
        assertEquals(message, exception.getMessage());
        assertEquals("VALIDATION_ERROR", exception.getErrorCode());
        assertNull(exception.getDetails());
    }
    
    @Test
    void testValidationExceptionWithFieldErrors() {
        String message = "Validation failed";
        Map<String, List<String>> fieldErrors = Map.of(
            "name", Arrays.asList("Name is required"),
            "email", Arrays.asList("Email is invalid", "Email is required")
        );
        ValidationException exception = new ValidationException(message, fieldErrors);
        
        assertEquals(message, exception.getMessage());
        assertEquals("VALIDATION_ERROR", exception.getErrorCode());
        
        assertNotNull(exception.getDetails());
        assertTrue(exception.getDetails() instanceof ValidationErrorDetails);
        
        ValidationErrorDetails details = (ValidationErrorDetails) exception.getDetails();
        assertEquals(fieldErrors, details.getFieldErrors());
    }
    
    @Test
    void testValidationExceptionWithSingleFieldError() {
        String field = "username";
        String errorMessage = "Username is too short";
        ValidationException exception = new ValidationException(field, errorMessage);
        
        assertEquals("Validation failed for field: " + field, exception.getMessage());
        assertEquals("VALIDATION_ERROR", exception.getErrorCode());
        
        assertNotNull(exception.getDetails());
        assertTrue(exception.getDetails() instanceof ValidationErrorDetails);
        
        ValidationErrorDetails details = (ValidationErrorDetails) exception.getDetails();
        Map<String, List<String>> fieldErrors = details.getFieldErrors();
        
        assertTrue(fieldErrors.containsKey(field));
        assertEquals(Arrays.asList(errorMessage), fieldErrors.get(field));
    }
    
    @Test
    void testValidationErrorDetailsConstructor() {
        Map<String, List<String>> fieldErrors = Map.of(
            "field1", Arrays.asList("Error 1"),
            "field2", Arrays.asList("Error 2", "Error 3")
        );
        
        ValidationErrorDetails details = new ValidationErrorDetails(fieldErrors);
        assertEquals(fieldErrors, details.getFieldErrors());
    }
}