package com.studentevaluator.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import com.studentevaluator.dto.ErrorResponse;
import com.studentevaluator.exception.AssignmentNotFoundException;
import com.studentevaluator.exception.CompilationException;
import com.studentevaluator.exception.EvaluationException;
import com.studentevaluator.exception.FileUploadException;
import com.studentevaluator.exception.StorageException;
import com.studentevaluator.exception.TestExecutionException;
import com.studentevaluator.exception.ValidationException;
import com.studentevaluator.service.ErrorLogService;

/**
 * Global exception handler for REST API endpoints with meaningful HTTP status codes and error messages.
 * Provides comprehensive error handling with structured logging and error log storage.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    @Autowired
    private ErrorLogService errorLogService;
    
    /**
     * Handle compilation exceptions with detailed error information.
     */
    @ExceptionHandler(CompilationException.class)
    public ResponseEntity<ErrorResponse> handleCompilationException(CompilationException e) {
        logger.error("Compilation failed: {}", e.getMessage(), e);
        errorLogService.logError("COMPILATION_ERROR", e.getMessage(), e.getDetails(), e);
        
        ErrorResponse error = new ErrorResponse(e.getErrorCode(), e.getMessage(), e.getDetails());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    /**
     * Handle test execution exceptions with detailed error information.
     */
    @ExceptionHandler(TestExecutionException.class)
    public ResponseEntity<ErrorResponse> handleTestExecutionException(TestExecutionException e) {
        logger.error("Test execution failed: {}", e.getMessage(), e);
        errorLogService.logError("TEST_EXECUTION_ERROR", e.getMessage(), e.getDetails(), e);
        
        ErrorResponse error = new ErrorResponse(e.getErrorCode(), e.getMessage(), e.getDetails());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
    
    /**
     * Handle storage exceptions with appropriate error codes.
     */
    @ExceptionHandler(StorageException.class)
    public ResponseEntity<ErrorResponse> handleStorageException(StorageException e) {
        logger.error("Storage error: {}", e.getMessage(), e);
        errorLogService.logError("STORAGE_ERROR", e.getMessage(), e.getDetails(), e);
        
        ErrorResponse error = new ErrorResponse(e.getErrorCode(), e.getMessage(), e.getDetails());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
    
    /**
     * Handle validation exceptions with field-specific errors.
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(ValidationException e) {
        logger.warn("Validation error: {}", e.getMessage());
        errorLogService.logError("VALIDATION_ERROR", e.getMessage(), e.getDetails(), null);
        
        ErrorResponse error = new ErrorResponse(e.getErrorCode(), e.getMessage(), e.getDetails());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    /**
     * Handle general evaluation exceptions.
     */
    @ExceptionHandler(EvaluationException.class)
    public ResponseEntity<ErrorResponse> handleEvaluationException(EvaluationException e) {
        logger.error("Evaluation error: {}", e.getMessage(), e);
        errorLogService.logError("EVALUATION_ERROR", e.getMessage(), e.getDetails(), e);
        
        ErrorResponse error = new ErrorResponse(e.getErrorCode(), e.getMessage(), e.getDetails());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
    
    /**
     * Handle assignment not found exceptions.
     */
    @ExceptionHandler(AssignmentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleAssignmentNotFoundException(AssignmentNotFoundException e) {
        logger.warn("Assignment not found: {}", e.getMessage());
        errorLogService.logError("ASSIGNMENT_NOT_FOUND", e.getMessage(), null, null);
        
        ErrorResponse error = new ErrorResponse("ASSIGNMENT_NOT_FOUND", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    /**
     * Handle file upload exceptions.
     */
    @ExceptionHandler(FileUploadException.class)
    public ResponseEntity<ErrorResponse> handleFileUploadException(FileUploadException e) {
        logger.warn("File upload error: {}", e.getMessage());
        errorLogService.logError("FILE_UPLOAD_ERROR", e.getMessage(), null, e);
        
        ErrorResponse error = new ErrorResponse("FILE_UPLOAD_ERROR", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    /**
     * Handle validation errors from @Valid annotations.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        logger.warn("Method argument validation error: {}", e.getMessage());
        
        List<String> details = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());
        
        errorLogService.logError("METHOD_ARGUMENT_VALIDATION_ERROR", "Request validation failed", details, null);
        
        ErrorResponse error = new ErrorResponse("VALIDATION_ERROR", "Request validation failed", details);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    /**
     * Handle file size exceeded exceptions.
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        logger.warn("File size exceeded: {}", e.getMessage());
        errorLogService.logError("FILE_SIZE_EXCEEDED", "File size exceeds the maximum allowed limit", null, e);
        
        ErrorResponse error = new ErrorResponse("FILE_SIZE_EXCEEDED", 
            "File size exceeds the maximum allowed limit");
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(error);
    }
    
    /**
     * Handle illegal argument exceptions.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        logger.warn("Invalid argument: {}", e.getMessage());
        errorLogService.logError("INVALID_ARGUMENT", e.getMessage(), null, e);
        
        ErrorResponse error = new ErrorResponse("INVALID_ARGUMENT", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    /**
     * Handle runtime exceptions.
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException e) {
        logger.error("Runtime error: {}", e.getMessage(), e);
        errorLogService.logError("RUNTIME_ERROR", e.getMessage(), null, e);
        
        ErrorResponse error = new ErrorResponse("RUNTIME_ERROR", 
            "An error occurred while processing the request");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
    
    /**
     * Handle all other exceptions.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception e) {
        logger.error("Unexpected error: {}", e.getMessage(), e);
        errorLogService.logError("INTERNAL_ERROR", e.getMessage(), null, e);
        
        ErrorResponse error = new ErrorResponse("INTERNAL_ERROR", 
            "An unexpected error occurred. Please try again later.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}