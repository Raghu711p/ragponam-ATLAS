package com.studentevaluator.security;

import com.studentevaluator.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

/**
 * Unit tests for SecureFileHandler.
 */
@ExtendWith(MockitoExtension.class)
class SecureFileHandlerTest {
    
    @TempDir
    Path tempDir;
    
    @Mock
    private InputValidator inputValidator;
    
    private SecureFileHandler secureFileHandler;
    
    @BeforeEach
    void setUp() {
        secureFileHandler = new SecureFileHandler(inputValidator);
        ReflectionTestUtils.setField(secureFileHandler, "tempDirectory", tempDir.toString());
        ReflectionTestUtils.setField(secureFileHandler, "maxFileSize", 1024L * 1024L);
    }
    
    @Test
    void testStoreFileSecurely_ValidFile_ShouldStoreSuccessfully() throws IOException {
        // Setup
        String javaContent = "public class Test { }";
        MultipartFile file = new MockMultipartFile("test.java", "Test.java", "text/plain", javaContent.getBytes());
        
        doNothing().when(inputValidator).validateJavaFile(any(), anyString());
        doNothing().when(inputValidator).validateFilePath(anyString(), anyString());
        
        // Execute
        Path storedPath = secureFileHandler.storeFileSecurely(file, "assignments", "Test.java");
        
        // Verify
        assertTrue(Files.exists(storedPath));
        assertEquals(javaContent, Files.readString(storedPath));
        assertTrue(storedPath.toString().contains("assignments"));
    }
    
    @Test
    void testStoreFileSecurely_InvalidFile_ShouldThrowException() {
        // Setup
        MultipartFile file = new MockMultipartFile("test.java", "Test.java", "text/plain", "content".getBytes());
        
        when(inputValidator.validateJavaFile(any(), anyString()))
            .thenThrow(new ValidationException("Invalid file"));
        
        // Execute & Verify
        ValidationException exception = assertThrows(ValidationException.class,
            () -> secureFileHandler.storeFileSecurely(file, "assignments", "Test.java"));
        
        assertEquals("Invalid file", exception.getMessage());
    }
    
    @Test
    void testCreateSecureDirectory_ValidPath_ShouldCreateDirectory() throws IOException {
        // Setup
        doNothing().when(inputValidator).validateFilePath(anyString(), anyString());
        
        // Execute
        Path createdDir = secureFileHandler.createSecureDirectory("test/subdir");
        
        // Verify
        assertTrue(Files.exists(createdDir));
        assertTrue(Files.isDirectory(createdDir));
        assertTrue(createdDir.toString().contains("test/subdir"));
    }
    
    @Test
    void testCreateSecureDirectory_PathTraversal_ShouldThrowException() {
        // Setup
        when(inputValidator.validateFilePath(anyString(), anyString()))
            .thenThrow(new ValidationException("Path traversal detected"));
        
        // Execute & Verify
        ValidationException exception = assertThrows(ValidationException.class,
            () -> secureFileHandler.createSecureDirectory("../../../etc"));
        
        assertEquals("Path traversal detected", exception.getMessage());
    }
    
    @Test
    void testSecureDelete_ValidFile_ShouldDeleteSuccessfully() throws IOException {
        // Setup
        Path testFile = tempDir.resolve("test.txt");
        Files.createFile(testFile);
        assertTrue(Files.exists(testFile));
        
        // Execute
        secureFileHandler.secureDelete(testFile);
        
        // Verify
        assertFalse(Files.exists(testFile));
    }
    
    @Test
    void testSecureDelete_Directory_ShouldDeleteRecursively() throws IOException {
        // Setup
        Path testDir = tempDir.resolve("testdir");
        Path subDir = testDir.resolve("subdir");
        Path testFile = subDir.resolve("test.txt");
        
        Files.createDirectories(subDir);
        Files.createFile(testFile);
        
        assertTrue(Files.exists(testDir));
        assertTrue(Files.exists(testFile));
        
        // Execute
        secureFileHandler.secureDelete(testDir);
        
        // Verify
        assertFalse(Files.exists(testDir));
        assertFalse(Files.exists(testFile));
    }
    
    @Test
    void testSecureDelete_FileOutsideBoundaries_ShouldThrowException() {
        // Setup
        Path outsideFile = Paths.get("/etc/passwd");
        
        // Execute & Verify
        ValidationException exception = assertThrows(ValidationException.class,
            () -> secureFileHandler.secureDelete(outsideFile));
        
        assertTrue(exception.getMessage().contains("outside allowed directory"));
    }
    
    @Test
    void testCreateSandboxDirectory_ValidIdentifier_ShouldCreateSandbox() throws IOException {
        // Setup
        doNothing().when(inputValidator).validateIdentifier(anyString(), anyString(), any(Integer.class));
        doNothing().when(inputValidator).validateFilePath(anyString(), anyString());
        
        // Execute
        Path sandboxPath = secureFileHandler.createSandboxDirectory("test123");
        
        // Verify
        assertTrue(Files.exists(sandboxPath));
        assertTrue(Files.exists(sandboxPath.resolve("src")));
        assertTrue(Files.exists(sandboxPath.resolve("classes")));
        assertTrue(Files.exists(sandboxPath.resolve("tests")));
        assertTrue(Files.exists(sandboxPath.resolve("output")));
    }
    
    @Test
    void testValidateFileForExecution_ValidFile_ShouldPass() throws IOException {
        // Setup
        Path testFile = tempDir.resolve("Test.java");
        Files.createFile(testFile);
        Files.write(testFile, "public class Test {}".getBytes());
        
        // Execute & Verify
        assertDoesNotThrow(() -> secureFileHandler.validateFileForExecution(testFile));
    }
    
    @Test
    void testValidateFileForExecution_FileOutsideBoundaries_ShouldThrowException() {
        // Setup
        Path outsideFile = Paths.get("/etc/passwd");
        
        // Execute & Verify
        ValidationException exception = assertThrows(ValidationException.class,
            () -> secureFileHandler.validateFileForExecution(outsideFile));
        
        assertTrue(exception.getMessage().contains("outside allowed execution directory"));
    }
    
    @Test
    void testValidateFileForExecution_InvalidExtension_ShouldThrowException() throws IOException {
        // Setup
        Path testFile = tempDir.resolve("test.txt");
        Files.createFile(testFile);
        
        // Execute & Verify
        ValidationException exception = assertThrows(ValidationException.class,
            () -> secureFileHandler.validateFileForExecution(testFile));
        
        assertTrue(exception.getMessage().contains("File type not allowed"));
    }
    
    @Test
    void testValidateFileForExecution_NonExistentFile_ShouldThrowException() {
        // Setup
        Path nonExistentFile = tempDir.resolve("nonexistent.java");
        
        // Execute & Verify
        ValidationException exception = assertThrows(ValidationException.class,
            () -> secureFileHandler.validateFileForExecution(nonExistentFile));
        
        assertTrue(exception.getMessage().contains("does not exist"));
    }
    
    @Test
    void testGetSecureBaseDirectory_ShouldReturnCorrectPath() {
        Path baseDir = secureFileHandler.getSecureBaseDirectory();
        
        assertEquals(tempDir.normalize(), baseDir);
    }
    
    @Test
    void testIsWithinSecureBoundaries_ValidPath_ShouldReturnTrue() {
        Path validPath = tempDir.resolve("subdir/file.java");
        
        assertTrue(secureFileHandler.isWithinSecureBoundaries(validPath));
    }
    
    @Test
    void testIsWithinSecureBoundaries_InvalidPath_ShouldReturnFalse() {
        Path invalidPath = Paths.get("/etc/passwd");
        
        assertFalse(secureFileHandler.isWithinSecureBoundaries(invalidPath));
    }
}