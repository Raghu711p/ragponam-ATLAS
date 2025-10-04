package com.studentevaluator.exception;

import java.util.List;

/**
 * Exception thrown when Java compilation fails.
 * Contains specific compilation error details for debugging.
 */
public class CompilationException extends EvaluationException {
    
    public CompilationException(String message) {
        super("COMPILATION_FAILED", message);
    }
    
    public CompilationException(String message, Throwable cause) {
        super("COMPILATION_FAILED", message, cause);
    }
    
    public CompilationException(String message, List<String> compilationErrors) {
        super("COMPILATION_FAILED", message, new CompilationErrorDetails(compilationErrors));
    }
    
    public CompilationException(String message, String fileName, int lineNumber, List<String> errors) {
        super("COMPILATION_FAILED", message, new CompilationErrorDetails(fileName, lineNumber, errors));
    }
    
    /**
     * Details about compilation errors for structured error responses
     */
    public static class CompilationErrorDetails {
        private final String fileName;
        private final Integer lineNumber;
        private final List<String> errors;
        
        public CompilationErrorDetails(List<String> errors) {
            this.fileName = null;
            this.lineNumber = null;
            this.errors = errors;
        }
        
        public CompilationErrorDetails(String fileName, Integer lineNumber, List<String> errors) {
            this.fileName = fileName;
            this.lineNumber = lineNumber;
            this.errors = errors;
        }
        
        public String getFileName() {
            return fileName;
        }
        
        public Integer getLineNumber() {
            return lineNumber;
        }
        
        public List<String> getErrors() {
            return errors;
        }
    }
}