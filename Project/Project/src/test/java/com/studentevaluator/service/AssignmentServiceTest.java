package com.studentevaluator.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import com.studentevaluator.exception.AssignmentNotFoundException;
import com.studentevaluator.exception.FileUploadException;
import com.studentevaluator.model.Assignment;
import com.studentevaluator.repository.AssignmentRepository;

/**
 * Unit tests for AssignmentService.
 * Tests file upload functionality, validation, and assignment management.
 */
@ExtendWith(MockitoExtension.class)
class AssignmentServiceTest {
    
    @Mock
    private AssignmentRepository assignmentRepository;
    
    @InjectMocks
    private AssignmentService assignmentService;
    
    @TempDir
    Path tempDir;
    
    private static final String ASSIGNMENT_ID = "test-assignment-1";
    private static final String ASSIGNMENT_TITLE = "Test Assignment";
    private static final String ASSIGNMENT_DESCRIPTION = "Test Description";
    
    @BeforeEach
    void setUp() {
        // Set temp directory for testing
        ReflectionTestUtils.setField(assignmentService, "tempDirectory", tempDir.toString());
        ReflectionTestUtils.setField(assignmentService, "maxFileSize", 1048576L); // 1MB
    }
    
    @Test
    void uploadAssignmentFile_ValidJavaFile_ShouldSucceed() {
        // Arrange
        String javaContent = "public class TestClass {\n    public void test() {}\n}";
        MultipartFile file = new MockMultipartFile("file", "TestClass.java", "text/plain", javaContent.getBytes());
        
        Assignment expectedAssignment = new Assignment(ASSIGNMENT_ID, ASSIGNMENT_TITLE, ASSIGNMENT_DESCRIPTION, null);
        
        when(assignmentRepository.existsById(ASSIGNMENT_ID)).thenReturn(false);
        when(assignmentRepository.save(any(Assignment.class))).thenReturn(expectedAssignment);
        
        // Act
        Assignment result = assignmentService.uploadAssignmentFile(file, ASSIGNMENT_ID, ASSIGNMENT_TITLE, ASSIGNMENT_DESCRIPTION);
        
        // Assert
        assertNotNull(result);
        assertEquals(ASSIGNMENT_ID, result.getAssignmentId());
        assertEquals(ASSIGNMENT_TITLE, result.getTitle());
        assertEquals(ASSIGNMENT_DESCRIPTION, result.getDescription());
        
        verify(assignmentRepository).existsById(ASSIGNMENT_ID);
        verify(assignmentRepository).save(any(Assignment.class));
        
        // Verify file was stored
        Path assignmentDir = tempDir.resolve("assignments").resolve(ASSIGNMENT_ID);
        Path storedFile = assignmentDir.resolve(ASSIGNMENT_ID + ".java");
        assertTrue(Files.exists(storedFile));
    }
    
    @Test
    void uploadAssignmentFile_DuplicateAssignmentId_ShouldThrowException() {
        // Arrange
        String javaContent = "public class TestClass {}";
        MultipartFile file = new MockMultipartFile("file", "TestClass.java", "text/plain", javaContent.getBytes());
        
        when(assignmentRepository.existsById(ASSIGNMENT_ID)).thenReturn(true);
        
        // Act & Assert
        FileUploadException exception = assertThrows(FileUploadException.class, () ->
            assignmentService.uploadAssignmentFile(file, ASSIGNMENT_ID, ASSIGNMENT_TITLE, ASSIGNMENT_DESCRIPTION)
        );
        
        assertTrue(exception.getMessage().contains("already exists"));
        verify(assignmentRepository).existsById(ASSIGNMENT_ID);
        verify(assignmentRepository, never()).save(any());
    }
    
    @Test
    void uploadAssignmentFile_NullFile_ShouldThrowException() {
        // Act & Assert
        FileUploadException exception = assertThrows(FileUploadException.class, () ->
            assignmentService.uploadAssignmentFile(null, ASSIGNMENT_ID, ASSIGNMENT_TITLE, ASSIGNMENT_DESCRIPTION)
        );
        
        assertEquals("File cannot be null or empty", exception.getMessage());
    }
    
    @Test
    void uploadAssignmentFile_EmptyFile_ShouldThrowException() {
        // Arrange
        MultipartFile file = new MockMultipartFile("file", "TestClass.java", "text/plain", new byte[0]);
        
        // Act & Assert
        FileUploadException exception = assertThrows(FileUploadException.class, () ->
            assignmentService.uploadAssignmentFile(file, ASSIGNMENT_ID, ASSIGNMENT_TITLE, ASSIGNMENT_DESCRIPTION)
        );
        
        assertEquals("File cannot be null or empty", exception.getMessage());
    }
    
    @Test
    void uploadAssignmentFile_NonJavaFile_ShouldThrowException() {
        // Arrange
        MultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "content".getBytes());
        
        // Act & Assert
        FileUploadException exception = assertThrows(FileUploadException.class, () ->
            assignmentService.uploadAssignmentFile(file, ASSIGNMENT_ID, ASSIGNMENT_TITLE, ASSIGNMENT_DESCRIPTION)
        );
        
        assertTrue(exception.getMessage().contains("Java file with .java extension"));
    }
    
    @Test
    void uploadAssignmentFile_FileTooLarge_ShouldThrowException() {
        // Arrange
        ReflectionTestUtils.setField(assignmentService, "maxFileSize", 10L); // Very small limit
        String content = "public class TestClass {}";
        MultipartFile file = new MockMultipartFile("file", "TestClass.java", "text/plain", content.getBytes());
        
        // Act & Assert
        FileUploadException exception = assertThrows(FileUploadException.class, () ->
            assignmentService.uploadAssignmentFile(file, ASSIGNMENT_ID, ASSIGNMENT_TITLE, ASSIGNMENT_DESCRIPTION)
        );
        
        assertTrue(exception.getMessage().contains("exceeds maximum allowed size"));
    }
    
    @Test
    void uploadAssignmentFile_EmptyContent_ShouldThrowException() {
        // Arrange
        MultipartFile file = new MockMultipartFile("file", "TestClass.java", "text/plain", "   ".getBytes());
        
        // Act & Assert
        FileUploadException exception = assertThrows(FileUploadException.class, () ->
            assignmentService.uploadAssignmentFile(file, ASSIGNMENT_ID, ASSIGNMENT_TITLE, ASSIGNMENT_DESCRIPTION)
        );
        
        assertEquals("File content cannot be empty", exception.getMessage());
    }
    
    @Test
    void uploadAssignmentFile_InvalidJavaContent_ShouldThrowException() {
        // Arrange
        String invalidContent = "This is not Java code";
        MultipartFile file = new MockMultipartFile("file", "TestClass.java", "text/plain", invalidContent.getBytes());
        
        // Act & Assert
        FileUploadException exception = assertThrows(FileUploadException.class, () ->
            assignmentService.uploadAssignmentFile(file, ASSIGNMENT_ID, ASSIGNMENT_TITLE, ASSIGNMENT_DESCRIPTION)
        );
        
        assertTrue(exception.getMessage().contains("does not appear to contain valid Java code"));
    }
    
    @Test
    void uploadTestFile_ValidJUnitTest_ShouldSucceed() {
        // Arrange
        String testContent = "import org.junit.jupiter.api.Test;\n" +
                           "public class TestClass {\n" +
                           "    @Test\n" +
                           "    public void testMethod() {}\n" +
                           "}";
        MultipartFile testFile = new MockMultipartFile("file", "TestClass.java", "text/plain", testContent.getBytes());
        
        Assignment existingAssignment = new Assignment(ASSIGNMENT_ID, ASSIGNMENT_TITLE, ASSIGNMENT_DESCRIPTION, null);
        Assignment updatedAssignment = new Assignment(ASSIGNMENT_ID, ASSIGNMENT_TITLE, ASSIGNMENT_DESCRIPTION, "test-path");
        
        when(assignmentRepository.findById(ASSIGNMENT_ID)).thenReturn(Optional.of(existingAssignment));
        when(assignmentRepository.save(any(Assignment.class))).thenReturn(updatedAssignment);
        
        // Act
        Assignment result = assignmentService.uploadTestFile(testFile, ASSIGNMENT_ID);
        
        // Assert
        assertNotNull(result);
        assertEquals(ASSIGNMENT_ID, result.getAssignmentId());
        assertNotNull(result.getTestFilePath());
        
        verify(assignmentRepository).findById(ASSIGNMENT_ID);
        verify(assignmentRepository).save(any(Assignment.class));
        
        // Verify test file was stored
        Path testDir = tempDir.resolve("tests").resolve(ASSIGNMENT_ID);
        assertTrue(Files.exists(testDir));
    }
    
    @Test
    void uploadTestFile_AssignmentNotFound_ShouldThrowException() {
        // Arrange
        String testContent = "import org.junit.jupiter.api.Test;\npublic class TestClass { @Test void test() {} }";
        MultipartFile testFile = new MockMultipartFile("file", "TestClass.java", "text/plain", testContent.getBytes());
        
        when(assignmentRepository.findById(ASSIGNMENT_ID)).thenReturn(Optional.empty());
        
        // Act & Assert
        AssignmentNotFoundException exception = assertThrows(AssignmentNotFoundException.class, () ->
            assignmentService.uploadTestFile(testFile, ASSIGNMENT_ID)
        );
        
        assertTrue(exception.getMessage().contains(ASSIGNMENT_ID));
        verify(assignmentRepository).findById(ASSIGNMENT_ID);
        verify(assignmentRepository, never()).save(any());
    }
    
    @Test
    void uploadTestFile_InvalidTestFile_ShouldThrowException() {
        // Arrange
        String invalidTestContent = "public class TestClass { public void method() {} }"; // No JUnit imports/annotations
        MultipartFile testFile = new MockMultipartFile("file", "TestClass.java", "text/plain", invalidTestContent.getBytes());
        
        // Act & Assert - validation happens before repository call, so no need to mock
        FileUploadException exception = assertThrows(FileUploadException.class, () ->
            assignmentService.uploadTestFile(testFile, ASSIGNMENT_ID)
        );
        
        assertTrue(exception.getMessage().contains("valid JUnit test file"));
    }
    
    @Test
    void getAssignment_ExistingAssignment_ShouldReturnAssignment() {
        // Arrange
        Assignment assignment = new Assignment(ASSIGNMENT_ID, ASSIGNMENT_TITLE);
        when(assignmentRepository.findById(ASSIGNMENT_ID)).thenReturn(Optional.of(assignment));
        
        // Act
        Optional<Assignment> result = assignmentService.getAssignment(ASSIGNMENT_ID);
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals(ASSIGNMENT_ID, result.get().getAssignmentId());
        verify(assignmentRepository).findById(ASSIGNMENT_ID);
    }
    
    @Test
    void getAssignment_NonExistingAssignment_ShouldReturnEmpty() {
        // Arrange
        when(assignmentRepository.findById(ASSIGNMENT_ID)).thenReturn(Optional.empty());
        
        // Act
        Optional<Assignment> result = assignmentService.getAssignment(ASSIGNMENT_ID);
        
        // Assert
        assertFalse(result.isPresent());
        verify(assignmentRepository).findById(ASSIGNMENT_ID);
    }
    
    @Test
    void getAllAssignments_ShouldReturnAllAssignments() {
        // Arrange
        List<Assignment> assignments = Arrays.asList(
            new Assignment("id1", "Title1"),
            new Assignment("id2", "Title2")
        );
        when(assignmentRepository.findAll()).thenReturn(assignments);
        
        // Act
        List<Assignment> result = assignmentService.getAllAssignments();
        
        // Assert
        assertEquals(2, result.size());
        verify(assignmentRepository).findAll();
    }
    
    @Test
    void searchAssignmentsByTitle_ShouldReturnMatchingAssignments() {
        // Arrange
        String titlePattern = "Test";
        List<Assignment> assignments = Arrays.asList(new Assignment("id1", "Test Assignment"));
        when(assignmentRepository.findByTitleContainingIgnoreCase(titlePattern)).thenReturn(assignments);
        
        // Act
        List<Assignment> result = assignmentService.searchAssignmentsByTitle(titlePattern);
        
        // Assert
        assertEquals(1, result.size());
        verify(assignmentRepository).findByTitleContainingIgnoreCase(titlePattern);
    }
    
    @Test
    void getAssignmentsCreatedAfter_ShouldReturnFilteredAssignments() {
        // Arrange
        LocalDateTime date = LocalDateTime.now().minusDays(1);
        List<Assignment> assignments = Arrays.asList(new Assignment("id1", "Recent Assignment"));
        when(assignmentRepository.findByCreatedAtAfter(date)).thenReturn(assignments);
        
        // Act
        List<Assignment> result = assignmentService.getAssignmentsCreatedAfter(date);
        
        // Assert
        assertEquals(1, result.size());
        verify(assignmentRepository).findByCreatedAtAfter(date);
    }
    
    @Test
    void getAssignmentsWithTests_ShouldReturnAssignmentsWithTestFiles() {
        // Arrange
        List<Assignment> assignments = Arrays.asList(new Assignment("id1", "Assignment with tests"));
        when(assignmentRepository.findByTestFilePathIsNotNull()).thenReturn(assignments);
        
        // Act
        List<Assignment> result = assignmentService.getAssignmentsWithTests();
        
        // Assert
        assertEquals(1, result.size());
        verify(assignmentRepository).findByTestFilePathIsNotNull();
    }
    
    @Test
    void updateAssignmentMetadata_ValidData_ShouldUpdateAssignment() {
        // Arrange
        String newTitle = "Updated Title";
        String newDescription = "Updated Description";
        Assignment existingAssignment = new Assignment(ASSIGNMENT_ID, ASSIGNMENT_TITLE);
        Assignment updatedAssignment = new Assignment(ASSIGNMENT_ID, newTitle, newDescription, null);
        
        when(assignmentRepository.findById(ASSIGNMENT_ID)).thenReturn(Optional.of(existingAssignment));
        when(assignmentRepository.save(any(Assignment.class))).thenReturn(updatedAssignment);
        
        // Act
        Assignment result = assignmentService.updateAssignmentMetadata(ASSIGNMENT_ID, newTitle, newDescription);
        
        // Assert
        assertEquals(newTitle, result.getTitle());
        assertEquals(newDescription, result.getDescription());
        verify(assignmentRepository).findById(ASSIGNMENT_ID);
        verify(assignmentRepository).save(any(Assignment.class));
    }
    
    @Test
    void updateAssignmentMetadata_AssignmentNotFound_ShouldThrowException() {
        // Arrange
        when(assignmentRepository.findById(ASSIGNMENT_ID)).thenReturn(Optional.empty());
        
        // Act & Assert
        AssignmentNotFoundException exception = assertThrows(AssignmentNotFoundException.class, () ->
            assignmentService.updateAssignmentMetadata(ASSIGNMENT_ID, "New Title", "New Description")
        );
        
        assertTrue(exception.getMessage().contains(ASSIGNMENT_ID));
    }
    
    @Test
    void deleteAssignment_ExistingAssignment_ShouldDeleteSuccessfully() {
        // Arrange
        Assignment assignment = new Assignment(ASSIGNMENT_ID, ASSIGNMENT_TITLE);
        when(assignmentRepository.findById(ASSIGNMENT_ID)).thenReturn(Optional.of(assignment));
        
        // Act
        assignmentService.deleteAssignment(ASSIGNMENT_ID);
        
        // Assert
        verify(assignmentRepository).findById(ASSIGNMENT_ID);
        verify(assignmentRepository).delete(assignment);
    }
    
    @Test
    void deleteAssignment_AssignmentNotFound_ShouldThrowException() {
        // Arrange
        when(assignmentRepository.findById(ASSIGNMENT_ID)).thenReturn(Optional.empty());
        
        // Act & Assert
        AssignmentNotFoundException exception = assertThrows(AssignmentNotFoundException.class, () ->
            assignmentService.deleteAssignment(ASSIGNMENT_ID)
        );
        
        assertTrue(exception.getMessage().contains(ASSIGNMENT_ID));
    }
    
    @Test
    void removeTestFileAssociation_AssignmentWithTestFile_ShouldRemoveAssociation() {
        // Arrange
        Assignment assignment = new Assignment(ASSIGNMENT_ID, ASSIGNMENT_TITLE, null, "test-file-path");
        Assignment updatedAssignment = new Assignment(ASSIGNMENT_ID, ASSIGNMENT_TITLE, null, null);
        
        when(assignmentRepository.findById(ASSIGNMENT_ID)).thenReturn(Optional.of(assignment));
        when(assignmentRepository.save(any(Assignment.class))).thenReturn(updatedAssignment);
        
        // Act
        Assignment result = assignmentService.removeTestFileAssociation(ASSIGNMENT_ID);
        
        // Assert
        assertNull(result.getTestFilePath());
        verify(assignmentRepository).findById(ASSIGNMENT_ID);
        verify(assignmentRepository).save(any(Assignment.class));
    }
    
    @Test
    void hasTestFile_ShouldDelegateToRepository() {
        // Arrange
        when(assignmentRepository.hasTestFile(ASSIGNMENT_ID)).thenReturn(true);
        
        // Act
        boolean result = assignmentService.hasTestFile(ASSIGNMENT_ID);
        
        // Assert
        assertTrue(result);
        verify(assignmentRepository).hasTestFile(ASSIGNMENT_ID);
    }
    
    @Test
    void getAssignmentFilePath_FileExists_ShouldReturnPath() throws IOException {
        // Arrange
        Path assignmentDir = tempDir.resolve("assignments").resolve(ASSIGNMENT_ID);
        Files.createDirectories(assignmentDir);
        Path javaFile = assignmentDir.resolve(ASSIGNMENT_ID + ".java");
        Files.createFile(javaFile);
        
        // Act
        Optional<String> result = assignmentService.getAssignmentFilePath(ASSIGNMENT_ID);
        
        // Assert
        assertTrue(result.isPresent());
        assertTrue(result.get().endsWith(ASSIGNMENT_ID + ".java"));
    }
    
    @Test
    void getAssignmentFilePath_FileNotExists_ShouldReturnEmpty() {
        // Act
        Optional<String> result = assignmentService.getAssignmentFilePath(ASSIGNMENT_ID);
        
        // Assert
        assertFalse(result.isPresent());
    }
}