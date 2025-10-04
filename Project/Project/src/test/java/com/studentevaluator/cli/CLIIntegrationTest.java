package com.studentevaluator.cli;

import com.studentevaluator.dto.AssignmentResponse;
import com.studentevaluator.dto.EvaluationResponse;
import com.studentevaluator.dto.StudentScoreResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Integration tests for CLI commands that interact with the REST API.
 */
@ExtendWith(MockitoExtension.class)
@SpringBootTest
@ActiveProfiles("test")
class CLIIntegrationTest {
    
    @Mock
    private CLIApiService mockApiService;
    
    private CommandLine.IFactory factory;
    private Path tempDir;
    
    @BeforeEach
    void setUp() throws IOException {
        factory = new TestPicocliFactory();
        tempDir = Files.createTempDirectory("cli-test");
    }
    
    @Test
    void testSubmitCommand_Success() throws Exception {
        // Create test file
        File testFile = createTestJavaFile("TestAssignment.java", "public class TestAssignment {}");
        
        // Mock API response
        AssignmentResponse mockResponse = new AssignmentResponse();
        mockResponse.setAssignmentId("test-assignment");
        mockResponse.setTitle("Test Assignment");
        mockResponse.setCreatedAt(LocalDateTime.now());
        
        when(mockApiService.uploadAssignment(any(File.class), eq("test-assignment"), 
                eq("Test Assignment"), eq("Test description")))
                .thenReturn(mockResponse);
        
        // Create and configure command
        SubmitCommand command = new SubmitCommand();
        setField(command, "assignmentFile", testFile);
        setField(command, "assignmentId", "test-assignment");
        setField(command, "title", "Test Assignment");
        setField(command, "description", "Test description");
        setField(command, "apiService", mockApiService);
        
        // Execute command
        Integer result = command.call();
        
        // Verify
        assertEquals(0, result);
        verify(mockApiService).uploadAssignment(testFile, "test-assignment", 
                "Test Assignment", "Test description");
    }
    
    @Test
    void testSubmitCommand_FileNotFound() throws Exception {
        File nonExistentFile = new File(tempDir.toFile(), "nonexistent.java");
        
        // Create and configure command
        SubmitCommand command = new SubmitCommand();
        setField(command, "assignmentFile", nonExistentFile);
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
    void testSubmitCommand_InvalidFileExtension() throws Exception {
        File testFile = createTestFile("test.txt", "not java content");
        
        // Create and configure command
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
    void testUploadTestsCommand_Success() throws Exception {
        // Create test file
        File testFile = createTestJavaFile("TestAssignmentTest.java", 
                "public class TestAssignmentTest {}");
        
        // Mock API response
        AssignmentResponse mockResponse = new AssignmentResponse();
        mockResponse.setAssignmentId("test-assignment");
        mockResponse.setTitle("Test Assignment");
        mockResponse.setHasTestFile(true);
        mockResponse.setCreatedAt(LocalDateTime.now());
        
        when(mockApiService.uploadTestFile(any(File.class), eq("test-assignment")))
                .thenReturn(mockResponse);
        
        // Create and configure command
        UploadTestsCommand command = new UploadTestsCommand();
        setField(command, "testFile", testFile);
        setField(command, "assignmentId", "test-assignment");
        setField(command, "apiService", mockApiService);
        
        // Execute command
        Integer result = command.call();
        
        // Verify
        assertEquals(0, result);
        verify(mockApiService).uploadTestFile(testFile, "test-assignment");
    }
    
    @Test
    void testEvaluateCommand_Success() throws Exception {
        // Create test file
        File submissionFile = createTestJavaFile("StudentSubmission.java", 
                "public class StudentSubmission {}");
        
        // Mock API response
        EvaluationResponse mockResponse = new EvaluationResponse();
        mockResponse.setEvaluationId("eval-123");
        mockResponse.setStudentId("student-1");
        mockResponse.setAssignmentId("test-assignment");
        mockResponse.setStatus(com.studentevaluator.model.EvaluationStatus.COMPLETED);
        mockResponse.setScore(java.math.BigDecimal.valueOf(85.0));
        mockResponse.setMaxScore(java.math.BigDecimal.valueOf(100.0));
        mockResponse.setEvaluatedAt(LocalDateTime.now());
        
        when(mockApiService.triggerEvaluation(any(File.class), eq("student-1"), 
                eq("test-assignment")))
                .thenReturn(mockResponse);
        
        // Create and configure command
        EvaluateCommand command = new EvaluateCommand();
        setField(command, "submissionFile", submissionFile);
        setField(command, "studentId", "student-1");
        setField(command, "assignmentId", "test-assignment");
        setField(command, "verbose", false);
        setField(command, "apiService", mockApiService);
        
        // Execute command
        Integer result = command.call();
        
        // Verify
        assertEquals(0, result);
        verify(mockApiService).triggerEvaluation(submissionFile, "student-1", "test-assignment");
    }
    
    @Test
    void testResultsCommand_StudentScores() throws Exception {
        // Mock API response
        StudentScoreResponse mockResponse = new StudentScoreResponse();
        mockResponse.setStudentId("student-1");
        mockResponse.setCurrentScore(85.0);
        
        when(mockApiService.getStudentScores(eq("student-1")))
                .thenReturn(mockResponse);
        
        // Create and configure command
        ResultsCommand command = new ResultsCommand();
        setField(command, "studentId", "student-1");
        setField(command, "showHistory", false);
        setField(command, "verbose", false);
        setField(command, "apiService", mockApiService);
        
        // Execute command
        Integer result = command.call();
        
        // Verify
        assertEquals(0, result);
        verify(mockApiService).getStudentScores("student-1");
    }
    
    @Test
    void testResultsCommand_StudentHistory() throws Exception {
        // Mock API response
        EvaluationResponse eval1 = new EvaluationResponse();
        eval1.setEvaluationId("eval-1");
        eval1.setStudentId("student-1");
        eval1.setAssignmentId("assignment-1");
        eval1.setStatus(com.studentevaluator.model.EvaluationStatus.COMPLETED);
        eval1.setScore(java.math.BigDecimal.valueOf(85.0));
        
        EvaluationResponse eval2 = new EvaluationResponse();
        eval2.setEvaluationId("eval-2");
        eval2.setStudentId("student-1");
        eval2.setAssignmentId("assignment-2");
        eval2.setStatus(com.studentevaluator.model.EvaluationStatus.COMPLETED);
        eval2.setScore(java.math.BigDecimal.valueOf(92.0));
        
        List<EvaluationResponse> mockHistory = Arrays.asList(eval1, eval2);
        
        when(mockApiService.getStudentEvaluationHistory(eq("student-1")))
                .thenReturn(mockHistory);
        
        // Create and configure command
        ResultsCommand command = new ResultsCommand();
        setField(command, "studentId", "student-1");
        setField(command, "showHistory", true);
        setField(command, "verbose", false);
        setField(command, "apiService", mockApiService);
        
        // Execute command
        Integer result = command.call();
        
        // Verify
        assertEquals(0, result);
        verify(mockApiService).getStudentEvaluationHistory("student-1");
    }
    
    @Test
    void testResultsCommand_EvaluationResult() throws Exception {
        // Mock API response
        EvaluationResponse mockResponse = new EvaluationResponse();
        mockResponse.setEvaluationId("eval-123");
        mockResponse.setStudentId("student-1");
        mockResponse.setAssignmentId("test-assignment");
        mockResponse.setStatus(com.studentevaluator.model.EvaluationStatus.COMPLETED);
        mockResponse.setScore(java.math.BigDecimal.valueOf(85.0));
        
        when(mockApiService.getEvaluationResult(eq("eval-123")))
                .thenReturn(mockResponse);
        
        // Create and configure command
        ResultsCommand command = new ResultsCommand();
        setField(command, "evaluationId", "eval-123");
        setField(command, "verbose", false);
        setField(command, "apiService", mockApiService);
        
        // Execute command
        Integer result = command.call();
        
        // Verify
        assertEquals(0, result);
        verify(mockApiService).getEvaluationResult("eval-123");
    }
    
    @Test
    void testResultsCommand_NoOptions() throws Exception {
        // Create and configure command with no options
        ResultsCommand command = new ResultsCommand();
        setField(command, "apiService", mockApiService);
        
        // Execute command
        Integer result = command.call();
        
        // Verify
        assertEquals(1, result);
        verifyNoInteractions(mockApiService);
    }
    
    // Helper methods
    
    private File createTestJavaFile(String fileName, String content) throws IOException {
        return createTestFile(fileName, content);
    }
    
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
     * Test factory for Picocli that doesn't require Spring context.
     */
    private static class TestPicocliFactory implements CommandLine.IFactory {
        @Override
        public <K> K create(Class<K> cls) throws Exception {
            return cls.getDeclaredConstructor().newInstance();
        }
    }
}