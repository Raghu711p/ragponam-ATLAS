package com.studentevaluator.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
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
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.studentevaluator.exception.AssignmentNotFoundException;
import com.studentevaluator.exception.FileUploadException;
import com.studentevaluator.model.Assignment;
import com.studentevaluator.repository.AssignmentRepository;

/**
 * Integration tests for AssignmentService.
 * Tests the service with real database interactions.
 */
@DataJpaTest
@Import(AssignmentService.class)
@ActiveProfiles("test")
@Transactional
class AssignmentServiceIntegrationTest {
    
    @Autowired
    private AssignmentService assignmentService;
    
    @Autowired
    private AssignmentRepository assignmentRepository;
    
    @TempDir
    Path tempDir;
    
    private static final String ASSIGNMENT_ID = "integration-test-1";
    private static final String ASSIGNMENT_TITLE = "Integration Test Assignment";
    private static final String ASSIGNMENT_DESCRIPTION = "Test Description for Integration";
    
    @BeforeEach
    void setUp() {
        // Set temp directory for testing
        ReflectionTestUtils.setField(assignmentService, "tempDirectory", tempDir.toString());
        ReflectionTestUtils.setField(assignmentService, "maxFileSize", 1048576L); // 1MB
    }
    
    @Test
    void uploadAssignmentFile_CompleteWorkflow_ShouldPersistAndStoreFile() throws IOException {
        // Arrange
        String javaContent = "public class Calculator {\n" +
                           "    public int add(int a, int b) {\n" +
                           "        return a + b;\n" +
                           "    }\n" +
                           "}";
        MultipartFile file = new MockMultipartFile("file", "Calculator.java", "text/plain", javaContent.getBytes());
        
        // Act
        Assignment result = assignmentService.uploadAssignmentFile(file, ASSIGNMENT_ID, ASSIGNMENT_TITLE, ASSIGNMENT_DESCRIPTION);
        
        // Assert
        assertNotNull(result);
        assertEquals(ASSIGNMENT_ID, result.getAssignmentId());
        assertEquals(ASSIGNMENT_TITLE, result.getTitle());
        assertEquals(ASSIGNMENT_DESCRIPTION, result.getDescription());
        assertNotNull(result.getCreatedAt());
        
        // Verify persistence in database
        Optional<Assignment> savedAssignment = assignmentRepository.findById(ASSIGNMENT_ID);
        assertTrue(savedAssignment.isPresent());
        assertEquals(ASSIGNMENT_TITLE, savedAssignment.get().getTitle());
        
        // Verify file storage
        Path assignmentDir = tempDir.resolve("assignments").resolve(ASSIGNMENT_ID);
        Path storedFile = assignmentDir.resolve(ASSIGNMENT_ID + ".java");
        assertTrue(Files.exists(storedFile));
        
        String storedContent = Files.readString(storedFile);
        assertEquals(javaContent, storedContent);
    }
    
    @Test
    void uploadTestFile_WithExistingAssignment_ShouldUpdateAssignmentAndStoreFile() throws IOException {
        // Arrange - First create an assignment
        Assignment assignment = new Assignment(ASSIGNMENT_ID, ASSIGNMENT_TITLE, ASSIGNMENT_DESCRIPTION, null);
        assignmentRepository.save(assignment);
        
        String testContent = "import org.junit.jupiter.api.Test;\n" +
                           "import static org.junit.jupiter.api.Assertions.*;\n" +
                           "public class CalculatorTest {\n" +
                           "    @Test\n" +
                           "    public void testAdd() {\n" +
                           "        Calculator calc = new Calculator();\n" +
                           "        assertEquals(5, calc.add(2, 3));\n" +
                           "    }\n" +
                           "}";
        MultipartFile testFile = new MockMultipartFile("testFile", "CalculatorTest.java", "text/plain", testContent.getBytes());
        
        // Act
        Assignment result = assignmentService.uploadTestFile(testFile, ASSIGNMENT_ID);
        
        // Assert
        assertNotNull(result);
        assertEquals(ASSIGNMENT_ID, result.getAssignmentId());
        assertNotNull(result.getTestFilePath());
        
        // Verify database update
        Optional<Assignment> updatedAssignment = assignmentRepository.findById(ASSIGNMENT_ID);
        assertTrue(updatedAssignment.isPresent());
        assertNotNull(updatedAssignment.get().getTestFilePath());
        
        // Verify test file storage
        Path testDir = tempDir.resolve("tests").resolve(ASSIGNMENT_ID);
        assertTrue(Files.exists(testDir));
        
        // Find the stored test file (name might be generated)
        List<Path> testFiles = Files.list(testDir).toList();
        assertEquals(1, testFiles.size());
        
        String storedTestContent = Files.readString(testFiles.get(0));
        assertEquals(testContent, storedTestContent);
    }
    
    @Test
    void completeAssignmentLifecycle_ShouldWorkEndToEnd() throws IOException {
        // Step 1: Upload assignment file
        String javaContent = "public class MathUtils {\n" +
                           "    public int multiply(int a, int b) {\n" +
                           "        return a * b;\n" +
                           "    }\n" +
                           "}";
        MultipartFile assignmentFile = new MockMultipartFile("file", "MathUtils.java", "text/plain", javaContent.getBytes());
        
        Assignment assignment = assignmentService.uploadAssignmentFile(assignmentFile, ASSIGNMENT_ID, ASSIGNMENT_TITLE, ASSIGNMENT_DESCRIPTION);
        assertNotNull(assignment);
        
        // Step 2: Upload test file
        String testContent = "import org.junit.jupiter.api.Test;\n" +
                           "import static org.junit.jupiter.api.Assertions.*;\n" +
                           "public class MathUtilsTest {\n" +
                           "    @Test\n" +
                           "    public void testMultiply() {\n" +
                           "        MathUtils utils = new MathUtils();\n" +
                           "        assertEquals(6, utils.multiply(2, 3));\n" +
                           "    }\n" +
                           "}";
        MultipartFile testFile = new MockMultipartFile("testFile", "MathUtilsTest.java", "text/plain", testContent.getBytes());
        
        Assignment updatedAssignment = assignmentService.uploadTestFile(testFile, ASSIGNMENT_ID);
        assertNotNull(updatedAssignment.getTestFilePath());
        
        // Step 3: Retrieve assignment
        Optional<Assignment> retrievedAssignment = assignmentService.getAssignment(ASSIGNMENT_ID);
        assertTrue(retrievedAssignment.isPresent());
        assertEquals(ASSIGNMENT_TITLE, retrievedAssignment.get().getTitle());
        
        // Step 4: Update metadata
        String newTitle = "Updated Math Utils Assignment";
        String newDescription = "Updated description for math utilities";
        Assignment metadataUpdated = assignmentService.updateAssignmentMetadata(ASSIGNMENT_ID, newTitle, newDescription);
        assertEquals(newTitle, metadataUpdated.getTitle());
        assertEquals(newDescription, metadataUpdated.getDescription());
        
        // Step 5: Verify file paths
        Optional<String> assignmentFilePath = assignmentService.getAssignmentFilePath(ASSIGNMENT_ID);
        assertTrue(assignmentFilePath.isPresent());
        assertTrue(Files.exists(Path.of(assignmentFilePath.get())));
        
        // Step 6: Check test file association
        assertTrue(assignmentService.hasTestFile(ASSIGNMENT_ID));
        
        // Step 7: Remove test file association
        Assignment testRemoved = assignmentService.removeTestFileAssociation(ASSIGNMENT_ID);
        assertNull(testRemoved.getTestFilePath());
        assertFalse(assignmentService.hasTestFile(ASSIGNMENT_ID));
        
        // Step 8: Delete assignment
        assignmentService.deleteAssignment(ASSIGNMENT_ID);
        Optional<Assignment> deletedAssignment = assignmentRepository.findById(ASSIGNMENT_ID);
        assertFalse(deletedAssignment.isPresent());
    }
    
    @Test
    void searchAndFilterOperations_ShouldWorkWithDatabase() {
        // Arrange - Create multiple assignments
        Assignment assignment1 = new Assignment("search-1", "Java Basics Assignment", "Basic Java concepts", null);
        Assignment assignment2 = new Assignment("search-2", "Advanced Java Assignment", "Advanced Java topics", null);
        Assignment assignment3 = new Assignment("search-3", "Python Assignment", "Python programming", null);
        
        assignmentRepository.saveAll(List.of(assignment1, assignment2, assignment3));
        
        // Test search by title
        List<Assignment> javaAssignments = assignmentService.searchAssignmentsByTitle("Java");
        assertEquals(2, javaAssignments.size());
        
        List<Assignment> basicAssignments = assignmentService.searchAssignmentsByTitle("Basic");
        assertEquals(1, basicAssignments.size());
        assertEquals("search-1", basicAssignments.get(0).getAssignmentId());
        
        // Test get all assignments
        List<Assignment> allAssignments = assignmentService.getAllAssignments();
        assertEquals(3, allAssignments.size());
        
        // Test get assignments created after a date
        LocalDateTime pastDate = LocalDateTime.now().minusHours(1);
        List<Assignment> recentAssignments = assignmentService.getAssignmentsCreatedAfter(pastDate);
        assertEquals(3, recentAssignments.size()); // All should be recent
        
        LocalDateTime futureDate = LocalDateTime.now().plusHours(1);
        List<Assignment> futureAssignments = assignmentService.getAssignmentsCreatedAfter(futureDate);
        assertEquals(0, futureAssignments.size()); // None should be in the future
    }
    
    @Test
    void assignmentsWithTestFiles_ShouldFilterCorrectly() {
        // Arrange - Create assignments with and without test files
        Assignment withTest = new Assignment("with-test", "Assignment With Test", null, "/path/to/test.java");
        Assignment withoutTest = new Assignment("without-test", "Assignment Without Test", null, null);
        
        assignmentRepository.saveAll(List.of(withTest, withoutTest));
        
        // Act
        List<Assignment> assignmentsWithTests = assignmentService.getAssignmentsWithTests();
        
        // Assert
        assertEquals(1, assignmentsWithTests.size());
        assertEquals("with-test", assignmentsWithTests.get(0).getAssignmentId());
    }
    
    @Test
    void errorHandling_ShouldThrowAppropriateExceptions() {
        // Test assignment not found scenarios
        assertThrows(AssignmentNotFoundException.class, () ->
            assignmentService.getAssignment("non-existent-id")
                .orElseThrow(() -> new AssignmentNotFoundException("non-existent-id"))
        );
        
        assertThrows(AssignmentNotFoundException.class, () ->
            assignmentService.updateAssignmentMetadata("non-existent-id", "New Title", "New Description")
        );
        
        assertThrows(AssignmentNotFoundException.class, () ->
            assignmentService.deleteAssignment("non-existent-id")
        );
        
        // Test file upload to non-existent assignment
        String testContent = "import org.junit.jupiter.api.Test;\npublic class Test { @Test void test() {} }";
        MultipartFile testFile = new MockMultipartFile("file", "Test.java", "text/plain", testContent.getBytes());
        
        assertThrows(AssignmentNotFoundException.class, () ->
            assignmentService.uploadTestFile(testFile, "non-existent-assignment")
        );
    }
    
    @Test
    void fileValidation_ShouldRejectInvalidFiles() {
        // Test various invalid file scenarios
        
        // Empty file
        MultipartFile emptyFile = new MockMultipartFile("file", "Test.java", "text/plain", new byte[0]);
        assertThrows(FileUploadException.class, () ->
            assignmentService.uploadAssignmentFile(emptyFile, ASSIGNMENT_ID, ASSIGNMENT_TITLE, ASSIGNMENT_DESCRIPTION)
        );
        
        // Non-Java file
        MultipartFile txtFile = new MockMultipartFile("file", "test.txt", "text/plain", "content".getBytes());
        assertThrows(FileUploadException.class, () ->
            assignmentService.uploadAssignmentFile(txtFile, ASSIGNMENT_ID, ASSIGNMENT_TITLE, ASSIGNMENT_DESCRIPTION)
        );
        
        // File with no Java content
        MultipartFile noJavaContent = new MockMultipartFile("file", "Test.java", "text/plain", "Just some text".getBytes());
        assertThrows(FileUploadException.class, () ->
            assignmentService.uploadAssignmentFile(noJavaContent, ASSIGNMENT_ID, ASSIGNMENT_TITLE, ASSIGNMENT_DESCRIPTION)
        );
        
        // Create assignment first for test file validation
        Assignment assignment = new Assignment(ASSIGNMENT_ID, ASSIGNMENT_TITLE);
        assignmentRepository.save(assignment);
        
        // Test file without JUnit content
        MultipartFile noJUnitContent = new MockMultipartFile("file", "Test.java", "text/plain", 
            "public class Test { public void method() {} }".getBytes());
        assertThrows(FileUploadException.class, () ->
            assignmentService.uploadTestFile(noJUnitContent, ASSIGNMENT_ID)
        );
    }
    
    @Test
    void duplicateAssignmentId_ShouldPreventDuplicates() {
        // Arrange - Create an assignment first
        String javaContent = "public class Test {}";
        MultipartFile file1 = new MockMultipartFile("file", "Test.java", "text/plain", javaContent.getBytes());
        
        // First upload should succeed
        Assignment first = assignmentService.uploadAssignmentFile(file1, ASSIGNMENT_ID, ASSIGNMENT_TITLE, ASSIGNMENT_DESCRIPTION);
        assertNotNull(first);
        
        // Second upload with same ID should fail
        MultipartFile file2 = new MockMultipartFile("file", "Test2.java", "text/plain", javaContent.getBytes());
        assertThrows(FileUploadException.class, () ->
            assignmentService.uploadAssignmentFile(file2, ASSIGNMENT_ID, "Different Title", "Different Description")
        );
    }
}