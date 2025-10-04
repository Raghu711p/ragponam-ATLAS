package com.studentevaluator.exception;

import java.util.List;

/**
 * Exception thrown when JUnit test execution fails or encounters errors.
 * Contains specific test execution error details for debugging.
 */
public class TestExecutionException extends EvaluationException {
    
    public TestExecutionException(String message) {
        super("TEST_EXECUTION_FAILED", message);
    }
    
    public TestExecutionException(String message, Throwable cause) {
        super("TEST_EXECUTION_FAILED", message, cause);
    }
    
    public TestExecutionException(String message, List<String> testErrors) {
        super("TEST_EXECUTION_FAILED", message, new TestExecutionErrorDetails(testErrors));
    }
    
    public TestExecutionException(String message, String testClass, String testMethod, String errorMessage) {
        super("TEST_EXECUTION_FAILED", message, new TestExecutionErrorDetails(testClass, testMethod, errorMessage));
    }
    
    /**
     * Details about test execution errors for structured error responses
     */
    public static class TestExecutionErrorDetails {
        private final String testClass;
        private final String testMethod;
        private final String errorMessage;
        private final List<String> testErrors;
        
        public TestExecutionErrorDetails(List<String> testErrors) {
            this.testClass = null;
            this.testMethod = null;
            this.errorMessage = null;
            this.testErrors = testErrors;
        }
        
        public TestExecutionErrorDetails(String testClass, String testMethod, String errorMessage) {
            this.testClass = testClass;
            this.testMethod = testMethod;
            this.errorMessage = errorMessage;
            this.testErrors = null;
        }
        
        public String getTestClass() {
            return testClass;
        }
        
        public String getTestMethod() {
            return testMethod;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
        
        public List<String> getTestErrors() {
            return testErrors;
        }
    }
}