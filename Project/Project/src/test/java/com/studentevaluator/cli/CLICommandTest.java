package com.studentevaluator.cli;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for individual CLI commands without Spring context.
 */
@ExtendWith(MockitoExtension.class)
class CLICommandTest {
    
    @Mock
    private CLIApiService mockApiService;
    
    private Path tempDir;
    
    @BeforeEach
    void setUp() throws IOException {
        tempDir = Files.createTempDirectory("cli-command-test");
    }
    
    @Test
    void testSubmitCommand_FileValidation() throws Exception {
        // Create test file
        File testFile = createTestFile("TestAssignment.java", "public class TestAssignment {}");
        
        // Create command
        SubmitCommand command = new SubmitCommand();
        setField(command, "assignmentFile", testFile);
        setField(command, "assignmentId", "test-assignment");
        setField(command, "title", "Test Assignment");
        setField(command, "apiService", mockApiService);
        
        // Mock successful API call
        when(mockApiService.uploadAssignment(any(), any(), any(), any()))
                .thenReturn(new com.studentevaluator.dto.AssignmentResponse());
        
        // Execute command
        Integer result = command.call();
        
        // Verify
        assertEquals(0, result);
        verify(mockApiService).uploadAssignment(testFile, "test-assignment", "Test Assignment", null);
    }
    
    @Test
    void testSubmitCommand_NonJavaFile() throws Exception {
        // Create non-Java file
        File testFile = createTestFile("test.txt", "not java content");
        
        // Create command
        SubmitCommand command = new SubmitCommand();
        setField(command, "assignmentFile", testFile);
        setField(command, "assignmentId", "test-assignment");
        setField(command, "title", "Test Assignment");
        setField(command, "apiService", mockApiService);
        
        // Execute command
        Integer result = command.call();
        
        // Verify
        assertEquals(1, result);
        verifyNoInteractions(mockApiService);
    }
    
    @Test
    void testUploadTestsCommand_FileValidation() throws Exception {
        // Create test file
        File testFile = createTestFile("TestAssignmentTest.java", "public class TestAssignmentTest {}");
        
        // Create command
        UploadTestsCommand command = new UploadTestsCommand();
        setField(command, "testFile", testFile);
        setField(command, "assignmentId", "test-assignment");
        setField(command, "apiService", mockApiService);
        
        // Mock successful API call
        when(mockApiService.uploadTestFile(any(), any()))
                .thenReturn(new com.studentevaluator.dto.AssignmentResponse());
        
        // Execute command
        Integer result = command.call();
        
        // Verify
        assertEquals(0, result);
        verify(mockApiService).uploadTestFile(testFile, "test-assignment");
    }
    
    @Test
    void testEvaluateCommand_FileValidation() throws Exception {
        // Create submission file
        File submissionFile = createTestFile("StudentSubmission.java", "public class StudentSubmission {}");
        
        // Create command
        EvaluateCommand command = new EvaluateCommand();
        setField(command, "submissionFile", submissionFile);
        setField(command, "studentId", "student-1");
        setField(command, "assignmentId", "test-assignment");
        setField(command, "verbose", false);
        setField(command, "apiService", mockApiService);
        
        // Mock successful API call
        com.studentevaluator.dto.EvaluationResponse mockResponse = 
                new com.studentevaluator.dto.EvaluationResponse();
        mockResponse.setStatus(com.studentevaluator.model.EvaluationStatus.COMPLETED);
        when(mockApiService.triggerEvaluation(any(), any(), any()))
                .thenReturn(mockResponse);
        
        // Execute command
        Integer result = command.call();
        
        // Verify
        assertEquals(0, result);
        verify(mockApiService).triggerEvaluation(submissionFile, "student-1", "test-assignment");
    }
    
    @Test
    void testResultsCommand_NoOptions() throws Exception {
        // Create command with no options
        ResultsCommand command = new ResultsCommand();
        setField(command, "apiService", mockApiService);
        
        // Execute command
        Integer result = command.call();
        
        // Verify
        assertEquals(1, result);
        verifyNoInteractions(mockApiService);
    }
    
    @Test
    void testResultsCommand_StudentScores() throws Exception {
        // Create command
        ResultsCommand command = new ResultsCommand();
        setField(command, "studentId", "student-1");
        setField(command, "showHistory", false);
        setField(command, "verbose", false);
        setField(command, "apiService", mockApiService);
        
        // Mock successful API call
        com.studentevaluator.dto.StudentScoreResponse mockResponse = 
                new com.studentevaluator.dto.StudentScoreResponse();
        mockResponse.setStudentId("student-1");
        mockResponse.setCurrentScore(85.0);
        when(mockApiService.getStudentScores(any()))
                .thenReturn(mockResponse);
        
        // Execute command
        Integer result = command.call();
        
        // Verify
        assertEquals(0, result);
        verify(mockApiService).getStudentScores("student-1");
    }
    
    @Test
    void testPicocliCommandLineIntegration() {
        // Test that commands can be created and configured with Picocli
        CommandLine cmd = new CommandLine(new EvaluatorCLI(new TestFactory()));
        
        // Verify subcommands are registered
        assertTrue(cmd.getSubcommands().containsKey("submit"));
        assertTrue(cmd.getSubcommands().containsKey("upload-tests"));
        assertTrue(cmd.getSubcommands().containsKey("evaluate"));
        assertTrue(cmd.getSubcommands().containsKey("results"));
    }
    
    // Helper methods
    
    private File createTestFile(String fileName, String content) throws IOException {
        Path filePath = tempDir.resolve(fileName);
        Files.write(filePath, content.getBytes());
        return filePath.toFile();
    }
    
    private void setField(Object target, String fieldName, Object value) throws Exception {
        java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
    
    /**
     * Test factory for Picocli that creates instances without Spring.
     */
    private static class TestFactory implements CommandLine.IFactory {
        @Override
        public <K> K create(Class<K> cls) throws Exception {
            return cls.getDeclaredConstructor().newInstance();
        }
    }
}