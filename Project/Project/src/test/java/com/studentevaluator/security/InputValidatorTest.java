package com.studentevaluator.security;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.studentevaluator.exception.ValidationException;

/**
 * Unit tests for InputValidator security component.
 */
class InputValidatorTest {
    
    private InputValidator inputValidator;
    
    @BeforeEach
    void setUp() {
        inputValidator = new InputValidator();
    }
    
    @Test
    void testValidateJavaFile_ValidFile_ShouldPass() {
        String validJavaContent = "public class Calculator {\n    public int add(int a, int b) {\n        return a + b;\n    }\n}";
        MultipartFile validFile = new MockMultipartFile(
            "file", "Calculator.java", "text/plain", validJavaContent.getBytes());
        
        assertDoesNotThrow(() -> inputValidator.validateJavaFile(validFile, "test file"));
    }
    
    @Test
    void testValidateJavaFile_NullFile_ShouldThrowException() {
        ValidationException exception = assertThrows(ValidationException.class,
            () -> inputValidator.validateJavaFile(null, "test file"));
        
        assertTrue(exception.getMessage().contains("cannot be null or empty"));
    }
    
    @Test
    void testValidateJavaFile_EmptyFile_ShouldThrowException() {
        MultipartFile emptyFile = new MockMultipartFile("file", "test.java", "text/plain", new byte[0]);
        
        ValidationException exception = assertThrows(ValidationException.class,
            () -> inputValidator.validateJavaFile(emptyFile, "test file"));
        
        assertTrue(exception.getMessage().contains("cannot be null or empty"));
    }
    
    @Test
    void testValidateJavaFile_TooLargeFile_ShouldThrowException() {
        byte[] largeContent = new byte[2 * 1024 * 1024]; // 2MB
        MultipartFile largeFile = new MockMultipartFile("file", "test.java", "text/plain", largeContent);
        
        ValidationException exception = assertThrows(ValidationException.class,
            () -> inputValidator.validateJavaFile(largeFile, "test file"));
        
        assertTrue(exception.getMessage().contains("exceeds maximum size"));
    }
    
    @Test
    void testValidateJavaFile_InvalidExtension_ShouldThrowException() {
        MultipartFile invalidFile = new MockMultipartFile(
            "file", "test.txt", "text/plain", "some content".getBytes());
        
        ValidationException exception = assertThrows(ValidationException.class,
            () -> inputValidator.validateJavaFile(invalidFile, "test file"));
        
        assertTrue(exception.getMessage().contains("valid Java file extension"));
    }
    
    @Test
    void testValidateJavaFile_PathTraversal_ShouldThrowException() {
        MultipartFile maliciousFile = new MockMultipartFile(
            "file", "../../../etc/passwd.java", "text/plain", "class Test {}".getBytes());
        
        ValidationException exception = assertThrows(ValidationException.class,
            () -> inputValidator.validateJavaFile(maliciousFile, "test file"));
        
        assertTrue(exception.getMessage().contains("invalid path characters"));
    }
    
    @Test
    void testValidateJavaContent_DangerousKeywords_ShouldThrowException() {
        String maliciousContent = "public class Malicious {\n" +
                                 "    public void hack() {\n" +
                                 "        Runtime.getRuntime().exec(\"rm -rf /\");\n" +
                                 "    }\n" +
                                 "}";
        
        ValidationException exception = assertThrows(ValidationException.class,
            () -> inputValidator.validateJavaContent(maliciousContent, "test content"));
        
        assertTrue(exception.getMessage().contains("potentially dangerous code"));
    }
    
    @Test
    void testValidateJavaContent_SuspiciousImports_ShouldThrowException() {
        String maliciousContent = "import java.lang.reflect.Method;\n" +
                                 "public class Malicious {\n" +
                                 "    // malicious code\n" +
                                 "}";
        
        ValidationException exception = assertThrows(ValidationException.class,
            () -> inputValidator.validateJavaContent(maliciousContent, "test content"));
        
        assertTrue(exception.getMessage().contains("suspicious import"));
    }
    
    @Test
    void testValidateAlphanumeric_ValidInput_ShouldPass() {
        assertDoesNotThrow(() -> inputValidator.validateAlphanumeric("test123_-", "test field", 20));
    }
    
    @Test
    void testValidateAlphanumeric_InvalidCharacters_ShouldThrowException() {
        ValidationException exception = assertThrows(ValidationException.class,
            () -> inputValidator.validateAlphanumeric("test@123", "test field", 20));
        
        assertTrue(exception.getMessage().contains("alphanumeric characters"));
    }
    
    @Test
    void testValidateAlphanumeric_TooLong_ShouldThrowException() {
        ValidationException exception = assertThrows(ValidationException.class,
            () -> inputValidator.validateAlphanumeric("verylongstring", "test field", 5));
        
        assertTrue(exception.getMessage().contains("exceeds maximum length"));
    }
    
    @Test
    void testValidateSafeText_ValidInput_ShouldPass() {
        assertDoesNotThrow(() -> inputValidator.validateSafeText("Test Assignment 1", "title", 50));
    }
    
    @Test
    void testValidateSafeText_InvalidCharacters_ShouldThrowException() {
        ValidationException exception = assertThrows(ValidationException.class,
            () -> inputValidator.validateSafeText("Test<script>alert('xss')</script>", "title", 100));
        
        assertTrue(exception.getMessage().contains("invalid characters"));
    }
    
    @Test
    void testValidateEmail_ValidEmail_ShouldPass() {
        assertDoesNotThrow(() -> inputValidator.validateEmail("test@example.com", "email"));
    }
    
    @Test
    void testValidateEmail_InvalidEmail_ShouldThrowException() {
        ValidationException exception = assertThrows(ValidationException.class,
            () -> inputValidator.validateEmail("invalid-email", "email"));
        
        assertTrue(exception.getMessage().contains("invalid email format"));
    }
    
    @Test
    void testValidateFilePath_ValidPath_ShouldPass() {
        assertDoesNotThrow(() -> inputValidator.validateFilePath("assignments/test.java", "file path"));
    }
    
    @Test
    void testValidateFilePath_PathTraversal_ShouldThrowException() {
        ValidationException exception = assertThrows(ValidationException.class,
            () -> inputValidator.validateFilePath("../../../etc/passwd", "file path"));
        
        assertTrue(exception.getMessage().contains("path traversal"));
    }
    
    @Test
    void testValidateFilePath_AbsolutePath_ShouldThrowException() {
        ValidationException exception = assertThrows(ValidationException.class,
            () -> inputValidator.validateFilePath("/etc/passwd", "file path"));
        
        assertTrue(exception.getMessage().contains("cannot be an absolute path"));
    }
    
    @Test
    void testSanitizeString_RemovesDangerousCharacters() {
        String input = "Test<script>alert('xss')</script>";
        String sanitized = inputValidator.sanitizeString(input);
        
        assertFalse(sanitized.contains("<"));
        assertFalse(sanitized.contains(">"));
        assertFalse(sanitized.contains("'"));
    }
    
    @Test
    void testValidateIdentifier_ValidIdentifier_ShouldPass() {
        assertDoesNotThrow(() -> inputValidator.validateIdentifier("test123", "identifier", 20));
    }
    
    @Test
    void testValidateIdentifier_StartsWithHyphen_ShouldThrowException() {
        ValidationException exception = assertThrows(ValidationException.class,
            () -> inputValidator.validateIdentifier("-test", "identifier", 20));
        
        assertTrue(exception.getMessage().contains("cannot start with hyphen"));
    }
    
    @Test
    void testValidateIdentifier_EndsWithUnderscore_ShouldThrowException() {
        ValidationException exception = assertThrows(ValidationException.class,
            () -> inputValidator.validateIdentifier("test_", "identifier", 20));
        
        assertTrue(exception.getMessage().contains("cannot end with"));
    }
}