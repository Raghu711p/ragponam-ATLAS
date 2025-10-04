package com.studentevaluator.exception;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.studentevaluator.exception.CompilationException.CompilationErrorDetails;

/**
 * Unit tests for CompilationException.
 */
class CompilationExceptionTest {
    
    @Test
    void testCompilationExceptionBasicConstructor() {
        String message = "Compilation failed";
        CompilationException exception = new CompilationException(message);
        
        assertEquals(message, exception.getMessage());
        assertEquals("COMPILATION_FAILED", exception.getErrorCode());
        assertNull(exception.getDetails());
    }
    
    @Test
    void testCompilationExceptionWithCause() {
        String message = "Compilation failed";
        RuntimeException cause = new RuntimeException("Root cause");
        CompilationException exception = new CompilationException(message, cause);
        
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
        assertEquals("COMPILATION_FAILED", exception.getErrorCode());
    }
    
    @Test
    void testCompilationExceptionWithErrors() {
        String message = "Compilation failed";
        List<String> errors = Arrays.asList("Error 1", "Error 2");
        CompilationException exception = new CompilationException(message, errors);
        
        assertEquals(message, exception.getMessage());
        assertEquals("COMPILATION_FAILED", exception.getErrorCode());
        
        assertNotNull(exception.getDetails());
        assertTrue(exception.getDetails() instanceof CompilationErrorDetails);
        
        CompilationErrorDetails details = (CompilationErrorDetails) exception.getDetails();
        assertEquals(errors, details.getErrors());
        assertNull(details.getFileName());
        assertNull(details.getLineNumber());
    }
    
    @Test
    void testCompilationExceptionWithFileDetails() {
        String message = "Compilation failed";
        String fileName = "Test.java";
        int lineNumber = 15;
        List<String> errors = Arrays.asList("cannot find symbol: variable x");
        
        CompilationException exception = new CompilationException(message, fileName, lineNumber, errors);
        
        assertEquals(message, exception.getMessage());
        assertEquals("COMPILATION_FAILED", exception.getErrorCode());
        
        assertNotNull(exception.getDetails());
        assertTrue(exception.getDetails() instanceof CompilationErrorDetails);
        
        CompilationErrorDetails details = (CompilationErrorDetails) exception.getDetails();
        assertEquals(fileName, details.getFileName());
        assertEquals(lineNumber, details.getLineNumber());
        assertEquals(errors, details.getErrors());
    }
    
    @Test
    void testCompilationErrorDetailsBasicConstructor() {
        List<String> errors = Arrays.asList("Error 1", "Error 2");
        CompilationErrorDetails details = new CompilationErrorDetails(errors);
        
        assertEquals(errors, details.getErrors());
        assertNull(details.getFileName());
        assertNull(details.getLineNumber());
    }
    
    @Test
    void testCompilationErrorDetailsFullConstructor() {
        String fileName = "Test.java";
        Integer lineNumber = 10;
        List<String> errors = Arrays.asList("Syntax error");
        
        CompilationErrorDetails details = new CompilationErrorDetails(fileName, lineNumber, errors);
        
        assertEquals(fileName, details.getFileName());
        assertEquals(lineNumber, details.getLineNumber());
        assertEquals(errors, details.getErrors());
    }
}