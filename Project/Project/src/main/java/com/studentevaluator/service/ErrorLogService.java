package com.studentevaluator.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.studentevaluator.repository.DynamoDBRepository;

/**
 * Service for logging errors to DynamoDB for troubleshooting and analysis.
 * Provides structured error logging with detailed context information.
 */
@Service
public class ErrorLogService {
    
    private static final Logger logger = LoggerFactory.getLogger(ErrorLogService.class);
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
    
    @Autowired
    private DynamoDBRepository dynamoDBRepository;
    
    /**
     * Log an error to DynamoDB with structured information.
     * 
     * @param errorType The type/category of error
     * @param message The error message
     * @param details Additional error details (can be null)
     * @param exception The exception that caused the error (can be null)
     */
    public void logError(String errorType, String message, Object details, Throwable exception) {
        try {
            String errorId = UUID.randomUUID().toString();
            String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
            
            Map<String, Object> errorLog = new HashMap<>();
            errorLog.put("error_id", errorId);
            errorLog.put("timestamp", timestamp);
            errorLog.put("error_type", errorType);
            errorLog.put("message", message);
            errorLog.put("level", "ERROR");
            
            if (details != null) {
                errorLog.put("details", details);
            }
            
            if (exception != null) {
                Map<String, Object> exceptionInfo = new HashMap<>();
                exceptionInfo.put("exception_class", exception.getClass().getSimpleName());
                exceptionInfo.put("exception_message", exception.getMessage());
                exceptionInfo.put("stack_trace", getStackTraceAsString(exception));
                
                if (exception.getCause() != null) {
                    exceptionInfo.put("cause", exception.getCause().getMessage());
                }
                
                errorLog.put("exception", exceptionInfo);
            }
            
            // Add system context
            errorLog.put("thread", Thread.currentThread().getName());
            errorLog.put("system_timestamp", System.currentTimeMillis());
            
            dynamoDBRepository.saveErrorLog(errorLog);
            
            logger.debug("Error logged to DynamoDB with ID: {}", errorId);
            
        } catch (Exception e) {
            // Don't let error logging failures break the application
            logger.error("Failed to log error to DynamoDB: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Log a warning to DynamoDB.
     * 
     * @param warningType The type/category of warning
     * @param message The warning message
     * @param details Additional warning details (can be null)
     */
    public void logWarning(String warningType, String message, Object details) {
        try {
            String warningId = UUID.randomUUID().toString();
            String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
            
            Map<String, Object> warningLog = new HashMap<>();
            warningLog.put("error_id", warningId);
            warningLog.put("timestamp", timestamp);
            warningLog.put("error_type", warningType);
            warningLog.put("message", message);
            warningLog.put("level", "WARNING");
            
            if (details != null) {
                warningLog.put("details", details);
            }
            
            warningLog.put("thread", Thread.currentThread().getName());
            warningLog.put("system_timestamp", System.currentTimeMillis());
            
            dynamoDBRepository.saveErrorLog(warningLog);
            
            logger.debug("Warning logged to DynamoDB with ID: {}", warningId);
            
        } catch (Exception e) {
            logger.error("Failed to log warning to DynamoDB: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Log an info message to DynamoDB for audit purposes.
     * 
     * @param infoType The type/category of info
     * @param message The info message
     * @param details Additional info details (can be null)
     */
    public void logInfo(String infoType, String message, Object details) {
        try {
            String infoId = UUID.randomUUID().toString();
            String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
            
            Map<String, Object> infoLog = new HashMap<>();
            infoLog.put("error_id", infoId);
            infoLog.put("timestamp", timestamp);
            infoLog.put("error_type", infoType);
            infoLog.put("message", message);
            infoLog.put("level", "INFO");
            
            if (details != null) {
                infoLog.put("details", details);
            }
            
            infoLog.put("thread", Thread.currentThread().getName());
            infoLog.put("system_timestamp", System.currentTimeMillis());
            
            dynamoDBRepository.saveErrorLog(infoLog);
            
            logger.debug("Info logged to DynamoDB with ID: {}", infoId);
            
        } catch (Exception e) {
            logger.error("Failed to log info to DynamoDB: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Convert exception stack trace to string.
     */
    private String getStackTraceAsString(Throwable exception) {
        StringBuilder sb = new StringBuilder();
        sb.append(exception.getClass().getName()).append(": ").append(exception.getMessage()).append("\n");
        
        for (StackTraceElement element : exception.getStackTrace()) {
            sb.append("\tat ").append(element.toString()).append("\n");
        }
        
        return sb.toString();
    }
}