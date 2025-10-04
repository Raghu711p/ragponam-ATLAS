package com.studentevaluator.security;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

import com.studentevaluator.exception.ValidationException;

/**
 * Integration tests for security components working together.
 */
@SpringBootTest
@ActiveProfiles("test")
class SecurityIntegrationTest {
    
    @Autowired
    private InputValidator inputValidator;
    
    @Autowired
    private SecureFileHandler secureFileHandler;
    
    @Autowired
    private RateLimitingFilter rateLimitingFilter;
    
    @Test
    void testSecurityComponents_AreWiredCorrectly() {
        assertNotNull(inputValidator);
        assertNotNull(secureFileHandler);
        assertNotNull(rateLimitingFilter);
    }
    
    @Test
    void testEndToEndSecurityValidation_ValidFile_ShouldPass() throws IOException {
        // Test complete security validation flow
        String validContent = "public class Calculator {\n    public int add(int a, int b) {\n        return a + b;\n    }\n}";
        MockMultipartFile validFile = new MockMultipartFile(
            "file", "Calculator.java", "text/plain", validContent.getBytes());
        
        // Validate file
        assertDoesNotThrow(() -> inputValidator.validateJavaFile(validFile, "test file"));
        
        // Store file securely
        Path storedPath = secureFileHandler.storeFileSecurely(validFile, "test", "Calculator.java");
        assertTrue(secureFileHandler.isWithinSecureBoundaries(storedPath));
        
        // Validate for execution
        assertDoesNotThrow(() -> secureFileHandler.validateFileForExecution(storedPath));
        
        // Cleanup
        secureFileHandler.secureDelete(storedPath.getParent());
    }
    
    @Test
    void testEndToEndSecurityValidation_MaliciousFile_ShouldBlock() {
        // Test complete security validation flow with malicious content
        String maliciousContent = 
            "import java.lang.reflect.*;\n" +
            "public class Malicious {\n" +
            "    public void hack() {\n" +
            "        Runtime.getRuntime().exec(\"rm -rf /\");\n" +
            "    }\n" +
            "}";
        
        MockMultipartFile maliciousFile = new MockMultipartFile(
            "file", "Malicious.java", "text/plain", maliciousContent.getBytes());
        
        // Should be blocked at validation stage
        ValidationException exception = assertThrows(ValidationException.class,
            () -> inputValidator.validateJavaFile(maliciousFile, "malicious file"));
        
        assertTrue(exception.getMessage().contains("dangerous code") || 
                  exception.getMessage().contains("suspicious import"));
    }
    
    @Test
    void testSecurityValidation_PathTraversal_ShouldBlock() {
        // Test path traversal protection
        ValidationException exception = assertThrows(ValidationException.class,
            () -> inputValidator.validateFilePath("../../../etc/passwd", "file path"));
        
        assertTrue(exception.getMessage().contains("path traversal"));
    }
    
    @Test
    void testSecurityValidation_InputSanitization_ShouldWork() {
        // Test input sanitization
        String maliciousInput = "<script>alert('xss')</script>";
        String sanitized = inputValidator.sanitizeString(maliciousInput);
        
        assertFalse(sanitized.contains("<script>"));
        assertFalse(sanitized.contains("</script>"));
    }
    
    @Test
    void testRateLimitingFilter_Statistics_ShouldWork() {
        // Test rate limiting statistics
        RateLimitingFilter.RateLimitStats stats = rateLimitingFilter.getStats();
        
        assertNotNull(stats);
        assertTrue(stats.getActiveClients() >= 0);
        assertTrue(stats.getTotalRequests() >= 0);
        assertTrue(stats.getRequestsPerMinute() > 0);
    }
    
    @Test
    void testSecureFileHandler_SandboxCreation_ShouldWork() throws IOException {
        // Test sandbox creation
        Path sandboxPath = secureFileHandler.createSandboxDirectory("test123");
        
        assertTrue(secureFileHandler.isWithinSecureBoundaries(sandboxPath));
        assertTrue(sandboxPath.resolve("src").toFile().exists());
        assertTrue(sandboxPath.resolve("classes").toFile().exists());
        assertTrue(sandboxPath.resolve("tests").toFile().exists());
        assertTrue(sandboxPath.resolve("output").toFile().exists());
        
        // Cleanup
        secureFileHandler.secureDelete(sandboxPath);
    }
    
    @Test
    void testInputValidator_ComprehensiveValidation_ShouldWork() {
        // Test various validation scenarios
        assertDoesNotThrow(() -> inputValidator.validateIdentifier("test123", "identifier", 20));
        assertDoesNotThrow(() -> inputValidator.validateEmail("test@example.com", "email"));
        assertDoesNotThrow(() -> inputValidator.validateSafeText("Safe text 123", "text", 50));
        
        // Test invalid scenarios
        assertThrows(ValidationException.class, 
            () -> inputValidator.validateIdentifier("test@123", "identifier", 20));
        assertThrows(ValidationException.class, 
            () -> inputValidator.validateEmail("invalid-email", "email"));
        assertThrows(ValidationException.class, 
            () -> inputValidator.validateSafeText("Unsafe<script>", "text", 50));
    }
}