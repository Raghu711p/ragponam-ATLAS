package com.studentevaluator.exception;

/**
 * Exception thrown when database or storage operations fail.
 * Covers both MySQL and DynamoDB storage errors.
 */
public class StorageException extends EvaluationException {
    
    public StorageException(String message) {
        super("STORAGE_ERROR", message);
    }
    
    public StorageException(String message, Throwable cause) {
        super("STORAGE_ERROR", message, cause);
    }
    
    public StorageException(String errorCode, String message) {
        super(errorCode, message);
    }
    
    public StorageException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
    
    /**
     * Factory methods for specific storage error types
     */
    public static StorageException databaseConnection(String message, Throwable cause) {
        return new StorageException("DATABASE_CONNECTION_ERROR", message, cause);
    }
    
    public static StorageException dynamoDbError(String message, Throwable cause) {
        return new StorageException("DYNAMODB_ERROR", message, cause);
    }
    
    public static StorageException dataIntegrityError(String message) {
        return new StorageException("DATA_INTEGRITY_ERROR", message);
    }
    
    public static StorageException fileStorageError(String message, Throwable cause) {
        return new StorageException("FILE_STORAGE_ERROR", message, cause);
    }
}