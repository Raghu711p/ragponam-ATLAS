package com.studentevaluator.service;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import com.studentevaluator.exception.FileUploadException;
import com.studentevaluator.repository.AssignmentRepository;

/**
 * Focused tests for file validation logic in AssignmentService.
 * Tests various edge cases and validation scenarios.
 */
@ExtendWith(MockitoExtension.class)
class AssignmentServiceFileValidationTest {
    
    @Mock
    private AssignmentRepository assignmentRepository;
    
    @InjectMocks
    private AssignmentService assignmentService;
    
    @TempDir
    Path tempDir;
    
    private static final String ASSIGNMENT_ID = "validation-test";
    private static final String ASSIGNMENT_TITLE = "Validation Test";
    
    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(assignmentService, "tempDirectory", tempDir.toString());
        ReflectionTestUtils.setField(assignmentService, "maxFileSize", 1024L); // 1KB for testing
    }
    
    @Test
    void validateJavaFile_NullFile_ShouldThrowException() {
        // Act & Assert
        FileUploadException exception = assertThrows(FileUploadException.class, () ->
            assignmentService.uploadAssignmentFile(null, ASSIGNMENT_ID, ASSIGNMENT_TITLE, null)
        );
        
        assertEquals("File cannot be null or empty", exception.getMessage());
    }
    
    @Test
    void validateJavaFile_EmptyFile_ShouldThrowException() {
        // Arrange
        MultipartFile emptyFile = new MockMultipartFile("file", "Test.java", "text/plain", new byte[0]);
        
        // Act & Assert
        FileUploadException exception = assertThrows(FileUploadException.class, () ->
            assignmentService.uploadAssignmentFile(emptyFile, ASSIGNMENT_ID, ASSIGNMENT_TITLE, null)
        );
        
        assertEquals("File cannot be null or empty", exception.getMessage());
    }
    
    @Test
    void validateJavaFile_FileTooLarge_ShouldThrowException() {
        // Arrange - Create file larger than 1KB limit
        byte[] largeContent = new byte[2048]; // 2KB
        MultipartFile largeFile = new MockMultipartFile("file", "Test.java", "text/plain", largeContent);
        
        // Act & Assert
        FileUploadException exception = assertThrows(FileUploadException.class, () ->
            assignmentService.uploadAssignmentFile(largeFile, ASSIGNMENT_ID, ASSIGNMENT_TITLE, null)
        );
        
        assertTrue(exception.getMessage().contains("exceeds maximum allowed size"));
    }
    
    @Test
    void validateJavaFile_NonJavaExtension_ShouldThrowException() {
        // Test various non-Java extensions
        String[] invalidExtensions = {".txt", ".py", ".cpp", ".js", ".html", ""};
        
        for (String extension : invalidExtensions) {
            String filename = "TestFile" + extension;
            MultipartFile file = new MockMultipartFile("file", filename, "text/plain", "content".getBytes());
            
            FileUploadException exception = assertThrows(FileUploadException.class, () ->
                assignmentService.uploadAssignmentFile(file, ASSIGNMENT_ID + extension, ASSIGNMENT_TITLE, null)
            );
            
            assertTrue(exception.getMessage().contains("Java file with .java extension"), 
                      "Failed for extension: " + extension);
        }
    }
    
    @Test
    void validateJavaFile_NullFilename_ShouldThrowException() {
        // Arrange
        MultipartFile fileWithNullName = new MockMultipartFile("file", null, "text/plain", "content".getBytes());
        
        // Act & Assert
        FileUploadException exception = assertThrows(FileUploadException.class, () ->
            assignmentService.uploadAssignmentFile(fileWithNullName, ASSIGNMENT_ID, ASSIGNMENT_TITLE, null)
        );
        
        assertTrue(exception.getMessage().contains("Java file with .java extension"));
    }
    
    @Test
    void validateJavaFile_CaseInsensitiveExtension_ShouldAcceptVariousCases() {
        // Arrange
        String[] validExtensions = {".java", ".JAVA", ".Java", ".jAvA"};
        
        for (String extension : validExtensions) {
            when(assignmentRepository.existsById(ASSIGNMENT_ID + extension)).thenReturn(false);
            
            String javaContent = "public class Test" + extension.replace(".", "") + " {}";
            String filename = "Test" + extension;
            MultipartFile file = new MockMultipartFile("file", filename, "text/plain", javaContent.getBytes());
            
            // Should not throw exception for valid Java extensions (case insensitive)
            assertDoesNotThrow(() ->
                assignmentService.uploadAssignmentFile(file, ASSIGNMENT_ID + extension, ASSIGNMENT_TITLE, null)
            );
        }
    }
    
    @Test
    void validateJavaFile_EmptyContent_ShouldThrowException() {
        // Test various forms of empty content
        String[] emptyContents = {"", "   ", "\n\n\n", "\t\t", "    \n   \t   "};
        
        for (int i = 0; i < emptyContents.length; i++) {
            final String content = emptyContents[i];
            final String testId = ASSIGNMENT_ID + i;
            MultipartFile file = new MockMultipartFile("file", "Test" + i + ".java", "text/plain", content.getBytes());
            
            FileUploadException exception = assertThrows(FileUploadException.class, () ->
                assignmentService.uploadAssignmentFile(file, testId, ASSIGNMENT_TITLE, null)
            );
            
            assertTrue(exception.getMessage().contains("File content cannot be empty") || 
                      exception.getMessage().contains("File cannot be null or empty"));
        }
    }
    
    @Test
    void validateJavaFile_InvalidJavaContent_ShouldThrowException() {
        // Test various forms of invalid Java content
        String[] invalidContents = {
            "This is just text",
            "function myFunction() { return 42; }", // JavaScript
            "def my_function(): return 42", // Python
            "#include <stdio.h>", // C/C++
            "SELECT * FROM users;", // SQL
            "Hello World!"
        };
        
        for (int i = 0; i < invalidContents.length; i++) {
            final String content = invalidContents[i];
            final String testId = ASSIGNMENT_ID + i;
            MultipartFile file = new MockMultipartFile("file", "Test" + i + ".java", "text/plain", content.getBytes());
            
            FileUploadException exception = assertThrows(FileUploadException.class, () ->
                assignmentService.uploadAssignmentFile(file, testId, ASSIGNMENT_TITLE, null)
            );
            
            assertTrue(exception.getMessage().contains("does not appear to contain valid Java code"));
        }
    }
    
    @Test
    void validateJavaFile_ValidJavaContent_ShouldAccept() {
        // Test various forms of valid Java content
        String[] validContents = {
            "public class Test {}",
            "class Test { public void method() {} }",
            "public interface TestInterface {}",
            "interface TestInterface { void method(); }",
            "public enum TestEnum { VALUE1, VALUE2 }",
            "enum TestEnum { A, B, C }",
            "public class Test {\n    // Comment\n    public void test() {\n        System.out.println(\"Hello\");\n    }\n}",
            "import java.util.*;\npublic class Test { List<String> list; }"
        };
        
        for (int i = 0; i < validContents.length; i++) {
            final String testId = ASSIGNMENT_ID + i;
            when(assignmentRepository.existsById(testId)).thenReturn(false);
            
            final String content = validContents[i];
            MultipartFile file = new MockMultipartFile("file", "Test" + i + ".java", "text/plain", content.getBytes());
            
            // Should not throw exception for valid Java content
            assertDoesNotThrow(() ->
                assignmentService.uploadAssignmentFile(file, testId, ASSIGNMENT_TITLE, null)
            );
        }
    }
    
    @Test
    void validateTestFileContent_ValidJUnitContent_ShouldAccept() {
        // Test various forms of valid JUnit content
        String[] validTestContents = {
            "import org.junit.jupiter.api.Test;\npublic class TestClass { @Test void test() {} }",
            "import static org.junit.jupiter.api.Assertions.*;\npublic class TestClass {}",
            "import org.junit.jupiter.api.BeforeEach;\npublic class TestClass { @BeforeEach void setup() {} }",
            "import org.junit.jupiter.api.AfterEach;\npublic class TestClass { @AfterEach void cleanup() {} }",
            "import org.junit.*;\npublic class TestClass {}",
            "public class TestClass {\n    @Test\n    public void testMethod() {\n        // test code\n    }\n}",
            "class TestClass {\n    @BeforeEach\n    void setup() {}\n    @Test\n    void test() {}\n}"
        };
        
        // First create an assignment for test file upload
        when(assignmentRepository.existsById(ASSIGNMENT_ID)).thenReturn(false);
        String assignmentContent = "public class Assignment {}";
        MultipartFile assignmentFile = new MockMultipartFile("file", "Assignment.java", "text/plain", assignmentContent.getBytes());
        assignmentService.uploadAssignmentFile(assignmentFile, ASSIGNMENT_ID, ASSIGNMENT_TITLE, null);
        
        // Mock the assignment exists for test file uploads
        when(assignmentRepository.findById(ASSIGNMENT_ID)).thenReturn(
            java.util.Optional.of(new com.studentevaluator.model.Assignment(ASSIGNMENT_ID, ASSIGNMENT_TITLE))
        );
        
        for (int i = 0; i < validTestContents.length; i++) {
            String content = validTestContents[i];
            MultipartFile testFile = new MockMultipartFile("file", "Test" + i + ".java", "text/plain", content.getBytes());
            
            // Should not throw exception for valid JUnit content
            assertDoesNotThrow(() ->
                assignmentService.uploadTestFile(testFile, ASSIGNMENT_ID)
            );
        }
    }
    
    @Test
    void validateTestFileContent_InvalidJUnitContent_ShouldThrowException() {
        // Test various forms of invalid JUnit content
        String[] invalidTestContents = {
            "public class TestClass { public void method() {} }", // No JUnit imports or annotations
            "class TestClass { void test() {} }", // No JUnit markers
            "import java.util.*;\npublic class TestClass {}", // Wrong imports
            "public class TestClass {\n    public static void main(String[] args) {}\n}", // Main method, not test
            "// Just a comment\npublic class TestClass {}"
        };
        
        // Test file validation happens before repository calls, so no mocking needed
        for (int i = 0; i < invalidTestContents.length; i++) {
            String content = invalidTestContents[i];
            MultipartFile testFile = new MockMultipartFile("file", "Test" + i + ".java", "text/plain", content.getBytes());
            
            FileUploadException exception = assertThrows(FileUploadException.class, () ->
                assignmentService.uploadTestFile(testFile, ASSIGNMENT_ID)
            );
            
            assertTrue(exception.getMessage().contains("valid JUnit test file"));
        }
    }
    
    @Test
    void validateJavaFile_SpecialCharactersInFilename_ShouldHandle() {
        // Test filenames with special characters
        String[] specialFilenames = {
            "Test-File.java",
            "Test_File.java", 
            "TestFile123.java",
            "Test File.java", // Space in filename
            "Test.File.java", // Multiple dots
            "测试.java" // Unicode characters
        };
        
        for (int i = 0; i < specialFilenames.length; i++) {
            final String testId = ASSIGNMENT_ID + i;
            when(assignmentRepository.existsById(testId)).thenReturn(false);
            
            final String content = "public class TestFile" + i + " {}";
            MultipartFile file = new MockMultipartFile("file", specialFilenames[i], "text/plain", content.getBytes());
            
            // Should handle special characters in filenames
            assertDoesNotThrow(() ->
                assignmentService.uploadAssignmentFile(file, testId, ASSIGNMENT_TITLE, null)
            );
        }
    }
}