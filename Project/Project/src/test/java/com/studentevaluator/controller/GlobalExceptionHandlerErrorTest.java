package com.studentevaluator.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import com.studentevaluator.dto.ErrorResponse;
import com.studentevaluator.exception.CompilationException;
import com.studentevaluator.exception.EvaluationException;
import com.studentevaluator.exception.StorageException;
import com.studentevaluator.exception.TestExecutionException;
import com.studentevaluator.exception.ValidationException;
import com.studentevaluator.service.ErrorLogService;

/**
 * Unit tests for GlobalExceptionHandler error handling scenarios.
 */
@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerErrorTest {
    
    @Mock
    private ErrorLogService errorLogService;
    
    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;
    
    @BeforeEach
    void setUp() {
        reset(errorLogService);
    }
    
    @Test
    void testHandleCompilationException() {
        List<String> errors = Arrays.asList("cannot find symbol: variable x");
        CompilationException exception = new CompilationException("Compilation failed", errors);
        
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleCompilationException(exception);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("COMPILATION_FAILED", response.getBody().getCode());
        assertEquals("Compilation failed", response.getBody().getMessage());
        assertNotNull(response.getBody().getDetails());
        
        verify(errorLogService).logError(eq("COMPILATION_ERROR"), eq("Compilation failed"), 
                                       any(), eq(exception));
    }
    
    @Test
    void testHandleTestExecutionException() {
        List<String> errors = Arrays.asList("Test failed: expected 5 but was 4");
        TestExecutionException exception = new TestExecutionException("Test execution failed", errors);
        
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleTestExecutionException(exception);
        
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("TEST_EXECUTION_FAILED", response.getBody().getCode());
        assertEquals("Test execution failed", response.getBody().getMessage());
        assertNotNull(response.getBody().getDetails());
        
        verify(errorLogService).logError(eq("TEST_EXECUTION_ERROR"), eq("Test execution failed"), 
                                       any(), eq(exception));
    }
    
    @Test
    void testHandleStorageException() {
        StorageException exception = StorageException.databaseConnection("Database connection failed", 
                                                                        new RuntimeException("Connection timeout"));
        
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleStorageException(exception);
        
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("DATABASE_CONNECTION_ERROR", response.getBody().getCode());
        assertEquals("Database connection failed", response.getBody().getMessage());
        
        verify(errorLogService).logError(eq("STORAGE_ERROR"), eq("Database connection failed"), 
                                       any(), eq(exception));
    }
    
    @Test
    void testHandleValidationException() {
        Map<String, List<String>> fieldErrors = Map.of("name", Arrays.asList("Name is required"));
        ValidationException exception = new ValidationException("Validation failed", fieldErrors);
        
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleValidationException(exception);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("VALIDATION_ERROR", response.getBody().getCode());
        assertEquals("Validation failed", response.getBody().getMessage());
        assertNotNull(response.getBody().getDetails());
        
        verify(errorLogService).logError(eq("VALIDATION_ERROR"), eq("Validation failed"), 
                                       any(), isNull());
    }
    
    @Test
    void testHandleEvaluationException() {
        EvaluationException exception = new EvaluationException("CUSTOM_ERROR", "Custom evaluation error");
        
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleEvaluationException(exception);
        
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("CUSTOM_ERROR", response.getBody().getCode());
        assertEquals("Custom evaluation error", response.getBody().getMessage());
        
        verify(errorLogService).logError(eq("EVALUATION_ERROR"), eq("Custom evaluation error"), 
                                       any(), eq(exception));
    }
    
    @Test
    void testHandleMethodArgumentNotValidException() {
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        
        FieldError fieldError1 = new FieldError("object", "field1", "Field1 is required");
        FieldError fieldError2 = new FieldError("object", "field2", "Field2 is invalid");
        List<FieldError> fieldErrors = Arrays.asList(fieldError1, fieldError2);
        
        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(fieldErrors);
        when(exception.getMessage()).thenReturn("Validation failed");
        
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleMethodArgumentNotValidException(exception);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("VALIDATION_ERROR", response.getBody().getCode());
        assertEquals("Request validation failed", response.getBody().getMessage());
        
        @SuppressWarnings("unchecked")
        List<String> details = (List<String>) response.getBody().getDetails();
        assertEquals(2, details.size());
        assertTrue(details.contains("Field1 is required"));
        assertTrue(details.contains("Field2 is invalid"));
        
        verify(errorLogService).logError(eq("METHOD_ARGUMENT_VALIDATION_ERROR"), 
                                       eq("Request validation failed"), any(), isNull());
    }
    
    @Test
    void testHandleMaxUploadSizeExceededException() {
        MaxUploadSizeExceededException exception = new MaxUploadSizeExceededException(1024L);
        
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleMaxUploadSizeExceededException(exception);
        
        assertEquals(HttpStatus.PAYLOAD_TOO_LARGE, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("FILE_SIZE_EXCEEDED", response.getBody().getCode());
        assertEquals("File size exceeds the maximum allowed limit", response.getBody().getMessage());
        
        verify(errorLogService).logError(eq("FILE_SIZE_EXCEEDED"), 
                                       eq("File size exceeds the maximum allowed limit"), 
                                       isNull(), eq(exception));
    }
    
    @Test
    void testHandleIllegalArgumentException() {
        IllegalArgumentException exception = new IllegalArgumentException("Invalid argument provided");
        
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleIllegalArgumentException(exception);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("INVALID_ARGUMENT", response.getBody().getCode());
        assertEquals("Invalid argument provided", response.getBody().getMessage());
        
        verify(errorLogService).logError(eq("INVALID_ARGUMENT"), eq("Invalid argument provided"), 
                                       isNull(), eq(exception));
    }
    
    @Test
    void testHandleRuntimeException() {
        RuntimeException exception = new RuntimeException("Unexpected runtime error");
        
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleRuntimeException(exception);
        
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("RUNTIME_ERROR", response.getBody().getCode());
        assertEquals("An error occurred while processing the request", response.getBody().getMessage());
        
        verify(errorLogService).logError(eq("RUNTIME_ERROR"), eq("Unexpected runtime error"), 
                                       isNull(), eq(exception));
    }
    
    @Test
    void testHandleGenericException() {
        Exception exception = new Exception("Generic exception");
        
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleGenericException(exception);
        
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("INTERNAL_ERROR", response.getBody().getCode());
        assertEquals("An unexpected error occurred. Please try again later.", response.getBody().getMessage());
        
        verify(errorLogService).logError(eq("INTERNAL_ERROR"), eq("Generic exception"), 
                                       isNull(), eq(exception));
    }
}