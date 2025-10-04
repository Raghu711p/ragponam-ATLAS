package com.studentevaluator.exception;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.studentevaluator.exception.TestExecutionException.TestExecutionErrorDetails;

/**
 * Unit tests for TestExecutionException.
 */
class TestExecutionExceptionTest {
    
    @Test
    void testTestExecutionExceptionBasicConstructor() {
        String message = "Test execution failed";
        TestExecutionException exception = new TestExecutionException(message);
        
        assertEquals(message, exception.getMessage());
        assertEquals("TEST_EXECUTION_FAILED", exception.getErrorCode());
        assertNull(exception.getDetails());
    }
    
    @Test
    void testTestExecutionExceptionWithCause() {
        String message = "Test execution failed";
        RuntimeException cause = new RuntimeException("Root cause");
        TestExecutionException exception = new TestExecutionException(message, cause);
        
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
        assertEquals("TEST_EXECUTION_FAILED", exception.getErrorCode());
    }
    
    @Test
    void testTestExecutionExceptionWithErrors() {
        String message = "Test execution failed";
        List<String> errors = Arrays.asList("Test error 1", "Test error 2");
        TestExecutionException exception = new TestExecutionException(message, errors);
        
        assertEquals(message, exception.getMessage());
        assertEquals("TEST_EXECUTION_FAILED", exception.getErrorCode());
        
        assertNotNull(exception.getDetails());
        assertTrue(exception.getDetails() instanceof TestExecutionErrorDetails);
        
        TestExecutionErrorDetails details = (TestExecutionErrorDetails) exception.getDetails();
        assertEquals(errors, details.getTestErrors());
        assertNull(details.getTestClass());
        assertNull(details.getTestMethod());
        assertNull(details.getErrorMessage());
    }
    
    @Test
    void testTestExecutionExceptionWithTestDetails() {
        String message = "Test execution failed";
        String testClass = "CalculatorTest";
        String testMethod = "testAdd";
        String errorMessage = "Expected 5 but was 4";
        
        TestExecutionException exception = new TestExecutionException(message, testClass, testMethod, errorMessage);
        
        assertEquals(message, exception.getMessage());
        assertEquals("TEST_EXECUTION_FAILED", exception.getErrorCode());
        
        assertNotNull(exception.getDetails());
        assertTrue(exception.getDetails() instanceof TestExecutionErrorDetails);
        
        TestExecutionErrorDetails details = (TestExecutionErrorDetails) exception.getDetails();
        assertEquals(testClass, details.getTestClass());
        assertEquals(testMethod, details.getTestMethod());
        assertEquals(errorMessage, details.getErrorMessage());
        assertNull(details.getTestErrors());
    }
    
    @Test
    void testTestExecutionErrorDetailsBasicConstructor() {
        List<String> errors = Arrays.asList("Error 1", "Error 2");
        TestExecutionErrorDetails details = new TestExecutionErrorDetails(errors);
        
        assertEquals(errors, details.getTestErrors());
        assertNull(details.getTestClass());
        assertNull(details.getTestMethod());
        assertNull(details.getErrorMessage());
    }
    
    @Test
    void testTestExecutionErrorDetailsFullConstructor() {
        String testClass = "MyTest";
        String testMethod = "myTestMethod";
        String errorMessage = "Test failed";
        
        TestExecutionErrorDetails details = new TestExecutionErrorDetails(testClass, testMethod, errorMessage);
        
        assertEquals(testClass, details.getTestClass());
        assertEquals(testMethod, details.getTestMethod());
        assertEquals(errorMessage, details.getErrorMessage());
        assertNull(details.getTestErrors());
    }
}