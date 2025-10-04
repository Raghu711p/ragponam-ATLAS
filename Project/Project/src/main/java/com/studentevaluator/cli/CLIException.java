package com.studentevaluator.cli;

/**
 * Exception class for CLI-specific errors.
 */
public class CLIException extends RuntimeException {
    
    public CLIException(String message) {
        super(message);
    }
    
    public CLIException(String message, Throwable cause) {
        super(message, cause);
    }
}