package com.studentevaluator.service;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import org.mockito.junit.jupiter.MockitoExtension;

import com.studentevaluator.repository.DynamoDBRepository;

/**
 * Unit tests for ErrorLogService.
 */
@ExtendWith(MockitoExtension.class)
class ErrorLogServiceTest {
    
    @Mock
    private DynamoDBRepository dynamoDBRepository;
    
    @InjectMocks
    private ErrorLogService errorLogService;
    
    @BeforeEach
    void setUp() {
        // Reset mocks before each test
        reset(dynamoDBRepository);
    }
    
    @Test
    void testLogError() {
        String errorType = "TEST_ERROR";
        String message = "Test error message";
        String details = "Test error details";
        RuntimeException exception = new RuntimeException("Test exception");
        
        errorLogService.logError(errorType, message, details, exception);
        
        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(dynamoDBRepository).saveErrorLog(captor.capture());
        
        Map<String, Object> errorLog = captor.getValue();
        
        assertNotNull(errorLog.get("error_id"));
        assertNotNull(errorLog.get("timestamp"));
        assertEquals(errorType, errorLog.get("error_type"));
        assertEquals(message, errorLog.get("message"));
        assertEquals("ERROR", errorLog.get("level"));
        assertEquals(details, errorLog.get("details"));
        assertNotNull(errorLog.get("exception"));
        assertNotNull(errorLog.get("thread"));
        assertNotNull(errorLog.get("system_timestamp"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> exceptionInfo = (Map<String, Object>) errorLog.get("exception");
        assertEquals("RuntimeException", exceptionInfo.get("exception_class"));
        assertEquals("Test exception", exceptionInfo.get("exception_message"));
        assertNotNull(exceptionInfo.get("stack_trace"));
    }
    
    @Test
    void testLogErrorWithoutDetails() {
        String errorType = "TEST_ERROR";
        String message = "Test error message";
        
        errorLogService.logError(errorType, message, null, null);
        
        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(dynamoDBRepository).saveErrorLog(captor.capture());
        
        Map<String, Object> errorLog = captor.getValue();
        
        assertEquals(errorType, errorLog.get("error_type"));
        assertEquals(message, errorLog.get("message"));
        assertEquals("ERROR", errorLog.get("level"));
        assertFalse(errorLog.containsKey("details"));
        assertFalse(errorLog.containsKey("exception"));
    }
    
    @Test
    void testLogErrorWithCause() {
        String errorType = "TEST_ERROR";
        String message = "Test error message";
        RuntimeException cause = new RuntimeException("Root cause");
        RuntimeException exception = new RuntimeException("Test exception", cause);
        
        errorLogService.logError(errorType, message, null, exception);
        
        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(dynamoDBRepository).saveErrorLog(captor.capture());
        
        Map<String, Object> errorLog = captor.getValue();
        
        @SuppressWarnings("unchecked")
        Map<String, Object> exceptionInfo = (Map<String, Object>) errorLog.get("exception");
        assertEquals("Root cause", exceptionInfo.get("cause"));
    }
    
    @Test
    void testLogWarning() {
        String warningType = "TEST_WARNING";
        String message = "Test warning message";
        String details = "Test warning details";
        
        errorLogService.logWarning(warningType, message, details);
        
        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(dynamoDBRepository).saveErrorLog(captor.capture());
        
        Map<String, Object> warningLog = captor.getValue();
        
        assertEquals(warningType, warningLog.get("error_type"));
        assertEquals(message, warningLog.get("message"));
        assertEquals("WARNING", warningLog.get("level"));
        assertEquals(details, warningLog.get("details"));
        assertFalse(warningLog.containsKey("exception"));
    }
    
    @Test
    void testLogInfo() {
        String infoType = "TEST_INFO";
        String message = "Test info message";
        String details = "Test info details";
        
        errorLogService.logInfo(infoType, message, details);
        
        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(dynamoDBRepository).saveErrorLog(captor.capture());
        
        Map<String, Object> infoLog = captor.getValue();
        
        assertEquals(infoType, infoLog.get("error_type"));
        assertEquals(message, infoLog.get("message"));
        assertEquals("INFO", infoLog.get("level"));
        assertEquals(details, infoLog.get("details"));
        assertFalse(infoLog.containsKey("exception"));
    }
    
    @Test
    void testLogErrorHandlesDynamoDBFailure() {
        String errorType = "TEST_ERROR";
        String message = "Test error message";
        
        doThrow(new RuntimeException("DynamoDB error")).when(dynamoDBRepository).saveErrorLog(any());
        
        // Should not throw exception, just log the failure
        assertDoesNotThrow(() -> errorLogService.logError(errorType, message, null, null));
        
        verify(dynamoDBRepository).saveErrorLog(any());
    }
    
    @Test
    void testLogWarningHandlesDynamoDBFailure() {
        String warningType = "TEST_WARNING";
        String message = "Test warning message";
        
        doThrow(new RuntimeException("DynamoDB error")).when(dynamoDBRepository).saveErrorLog(any());
        
        // Should not throw exception, just log the failure
        assertDoesNotThrow(() -> errorLogService.logWarning(warningType, message, null));
        
        verify(dynamoDBRepository).saveErrorLog(any());
    }
    
    @Test
    void testLogInfoHandlesDynamoDBFailure() {
        String infoType = "TEST_INFO";
        String message = "Test info message";
        
        doThrow(new RuntimeException("DynamoDB error")).when(dynamoDBRepository).saveErrorLog(any());
        
        // Should not throw exception, just log the failure
        assertDoesNotThrow(() -> errorLogService.logInfo(infoType, message, null));
        
        verify(dynamoDBRepository).saveErrorLog(any());
    }
}