package com.studentevaluator.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for StorageException.
 */
class StorageExceptionTest {
    
    @Test
    void testStorageExceptionBasicConstructor() {
        String message = "Storage error occurred";
        StorageException exception = new StorageException(message);
        
        assertEquals(message, exception.getMessage());
        assertEquals("STORAGE_ERROR", exception.getErrorCode());
        assertNull(exception.getDetails());
    }
    
    @Test
    void testStorageExceptionWithCause() {
        String message = "Storage error occurred";
        RuntimeException cause = new RuntimeException("Root cause");
        StorageException exception = new StorageException(message, cause);
        
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
        assertEquals("STORAGE_ERROR", exception.getErrorCode());
    }
    
    @Test
    void testStorageExceptionWithErrorCode() {
        String errorCode = "CUSTOM_STORAGE_ERROR";
        String message = "Custom storage error";
        StorageException exception = new StorageException(errorCode, message);
        
        assertEquals(message, exception.getMessage());
        assertEquals(errorCode, exception.getErrorCode());
    }
    
    @Test
    void testStorageExceptionWithErrorCodeAndCause() {
        String errorCode = "CUSTOM_STORAGE_ERROR";
        String message = "Custom storage error";
        RuntimeException cause = new RuntimeException("Root cause");
        StorageException exception = new StorageException(errorCode, message, cause);
        
        assertEquals(message, exception.getMessage());
        assertEquals(errorCode, exception.getErrorCode());
        assertEquals(cause, exception.getCause());
    }
    
    @Test
    void testDatabaseConnectionFactory() {
        String message = "Database connection failed";
        RuntimeException cause = new RuntimeException("Connection timeout");
        StorageException exception = StorageException.databaseConnection(message, cause);
        
        assertEquals(message, exception.getMessage());
        assertEquals("DATABASE_CONNECTION_ERROR", exception.getErrorCode());
        assertEquals(cause, exception.getCause());
    }
    
    @Test
    void testDynamoDbErrorFactory() {
        String message = "DynamoDB operation failed";
        RuntimeException cause = new RuntimeException("AWS error");
        StorageException exception = StorageException.dynamoDbError(message, cause);
        
        assertEquals(message, exception.getMessage());
        assertEquals("DYNAMODB_ERROR", exception.getErrorCode());
        assertEquals(cause, exception.getCause());
    }
    
    @Test
    void testDataIntegrityErrorFactory() {
        String message = "Data integrity violation";
        StorageException exception = StorageException.dataIntegrityError(message);
        
        assertEquals(message, exception.getMessage());
        assertEquals("DATA_INTEGRITY_ERROR", exception.getErrorCode());
        assertNull(exception.getCause());
    }
    
    @Test
    void testFileStorageErrorFactory() {
        String message = "File storage failed";
        RuntimeException cause = new RuntimeException("Disk full");
        StorageException exception = StorageException.fileStorageError(message, cause);
        
        assertEquals(message, exception.getMessage());
        assertEquals("FILE_STORAGE_ERROR", exception.getErrorCode());
        assertEquals(cause, exception.getCause());
    }
}