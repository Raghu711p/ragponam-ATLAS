package com.studentevaluator.service;

import com.studentevaluator.engine.JavaCompiler;
import com.studentevaluator.engine.JUnitTestRunner;
import com.studentevaluator.model.*;
import com.studentevaluator.repository.AssignmentRepository;
import com.studentevaluator.repository.DynamoDBRepository;
import com.studentevaluator.repository.EvaluationRepository;
import com.studentevaluator.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Integration tests for EvaluationService.
 * Tests the complete evaluation workflow from submission to result storage.
 */
@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class EvaluationServiceIntegrationTest {
    
    @Mock
    private JavaCompiler javaCompiler;
    
    @Mock
    private JUnitTestRunner testRunner;
    
    @Mock
    private EvaluationRepository evaluationRepository;
    
    @Mock
    private AssignmentRepository assignmentRepository;
    
    @Mock
    private StudentRepository studentRepository;
    
    @Mock
    private DynamoDBRepository dynamoDBRepository;
    
    @Mock
    private ScoreCache scoreCache;
    
    private EvaluationService evaluationService;
    
    @TempDir
    Path tempDir;
    
    private static final String STUDENT_ID = "student123";
    private static final String ASSIGNMENT_ID = "assignment456";
    private static final String EVALUATION_ID = "eval_789";
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        evaluationService = new EvaluationService(
            javaCompiler, testRunner, evaluationRepository, assignmentRepository,
            studentRepository, dynamoDBRepository, scoreCache
        );
    }
    
    @Test
    void testSuccessfulEvaluationWorkflow() throws IOException {
        // Arrange
        File submissionFile = createTestJavaFile("Calculator.java", getValidJavaCode());
        String outputDir = tempDir.toString();
        
        Assignment assignment = createTestAssignment();
        CompilationResult compilationResult = createSuccessfulCompilationResult();
        TestExecutionResult testResult = createSuccessfulTestResult();
        
        when(assignmentRepository.findById(ASSIGNMENT_ID)).thenReturn(Optional.of(assignment));
        when(javaCompiler.compile(submissionFile, outputDir)).thenReturn(compilationResult);
        when(testRunner.runTests(anyString(), anyList())).thenReturn(testResult);
        when(evaluationRepository.save(any(Evaluation.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        EvaluationResult result = evaluationService.evaluateAssignment(STUDENT_ID, ASSIGNMENT_ID, submissionFile, outputDir);
        
        // Assert
        assertNotNull(result);
        assertEquals(EvaluationStatus.COMPLETED, result.getStatus());
        assertTrue(result.isSuccessful());
        assertEquals(BigDecimal.valueOf(3), result.getFinalScore());
        assertEquals(BigDecimal.valueOf(3), result.getMaxScore());
        
        // Verify interactions
        verify(assignmentRepository).findById(ASSIGNMENT_ID);
        verify(javaCompiler).compile(submissionFile, outputDir);
        verify(testRunner).runTests(anyString(), anyList());
        verify(evaluationRepository, times(2)).save(any(Evaluation.class));
        verify(dynamoDBRepository).storeCompilationLog(anyString(), anyString(), eq(STUDENT_ID), eq(ASSIGNMENT_ID));
        verify(dynamoDBRepository).storeTestExecutionLog(anyString(), anyString(), eq(STUDENT_ID), eq(ASSIGNMENT_ID));
        verify(scoreCache).updateScore(STUDENT_ID, BigDecimal.valueOf(3));
    }
    
    @Test
    void testEvaluationWithCompilationFailure() throws IOException {
        // Arrange
        File submissionFile = createTestJavaFile("InvalidCode.java", getInvalidJavaCode());
        String outputDir = tempDir.toString();
        
        Assignment assignment = createTestAssignment();
        CompilationResult compilationResult = createFailedCompilationResult();
        
        when(assignmentRepository.findById(ASSIGNMENT_ID)).thenReturn(Optional.of(assignment));
        when(javaCompiler.compile(submissionFile, outputDir)).thenReturn(compilationResult);
        when(evaluationRepository.save(any(Evaluation.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        EvaluationResult result = evaluationService.evaluateAssignment(STUDENT_ID, ASSIGNMENT_ID, submissionFile, outputDir);
        
        // Assert
        assertNotNull(result);
        assertEquals(EvaluationStatus.FAILED, result.getStatus());
        assertFalse(result.isSuccessful());
        assertEquals(BigDecimal.ZERO, result.getFinalScore());
        assertEquals(BigDecimal.ZERO, result.getMaxScore());
        assertTrue(result.hasCompilationErrors());
        
        // Verify interactions
        verify(javaCompiler).compile(submissionFile, outputDir);
        verify(testRunner, never()).runTests(anyString(), anyList());
        verify(dynamoDBRepository).storeCompilationLog(anyString(), anyString(), eq(STUDENT_ID), eq(ASSIGNMENT_ID));
        verify(scoreCache, never()).updateScore(anyString(), any(BigDecimal.class));
    }
    
    @Test
    void testEvaluationWithTestFailures() throws IOException {
        // Arrange
        File submissionFile = createTestJavaFile("Calculator.java", getValidJavaCode());
        String outputDir = tempDir.toString();
        
        Assignment assignment = createTestAssignment();
        CompilationResult compilationResult = createSuccessfulCompilationResult();
        TestExecutionResult testResult = createFailedTestResult();
        
        when(assignmentRepository.findById(ASSIGNMENT_ID)).thenReturn(Optional.of(assignment));
        when(javaCompiler.compile(submissionFile, outputDir)).thenReturn(compilationResult);
        when(testRunner.runTests(anyString(), anyList())).thenReturn(testResult);
        when(evaluationRepository.save(any(Evaluation.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        EvaluationResult result = evaluationService.evaluateAssignment(STUDENT_ID, ASSIGNMENT_ID, submissionFile, outputDir);
        
        // Assert
        assertNotNull(result);
        assertEquals(EvaluationStatus.COMPLETED, result.getStatus());
        assertTrue(result.isSuccessful()); // Compilation succeeded
        assertEquals(BigDecimal.valueOf(1), result.getFinalScore()); // Only 1 test passed
        assertEquals(BigDecimal.valueOf(3), result.getMaxScore());
        assertTrue(result.hasTestFailures());
        
        // Verify interactions
        verify(testRunner).runTests(anyString(), anyList());
        verify(dynamoDBRepository).storeTestExecutionLog(anyString(), anyString(), eq(STUDENT_ID), eq(ASSIGNMENT_ID));
        verify(scoreCache).updateScore(STUDENT_ID, BigDecimal.valueOf(1));
    }
    
    @Test
    void testAsyncEvaluationWorkflow() throws Exception {
        // Arrange
        File submissionFile = createTestJavaFile("Calculator.java", getValidJavaCode());
        String outputDir = tempDir.toString();
        
        Assignment assignment = createTestAssignment();
        CompilationResult compilationResult = createSuccessfulCompilationResult();
        TestExecutionResult testResult = createSuccessfulTestResult();
        
        when(assignmentRepository.findById(ASSIGNMENT_ID)).thenReturn(Optional.of(assignment));
        when(javaCompiler.compile(submissionFile, outputDir)).thenReturn(compilationResult);
        when(testRunner.runTests(anyString(), anyList())).thenReturn(testResult);
        when(evaluationRepository.save(any(Evaluation.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        CompletableFuture<EvaluationResult> future = evaluationService.evaluateAssignmentAsync(
            STUDENT_ID, ASSIGNMENT_ID, submissionFile, outputDir);
        
        EvaluationResult result = future.get(); // Wait for completion
        
        // Assert
        assertNotNull(result);
        assertEquals(EvaluationStatus.COMPLETED, result.getStatus());
        assertTrue(result.isSuccessful());
        
        // Verify async execution completed
        assertTrue(future.isDone());
        assertFalse(future.isCancelled());
    }
    
    @Test
    void testEvaluationWithInvalidInputs() {
        // Test null student ID
        EvaluationResult result1 = evaluationService.evaluateAssignment(null, ASSIGNMENT_ID, 
            new File("test.java"), tempDir.toString());
        assertEquals(EvaluationStatus.FAILED, result1.getStatus());
        
        // Test null assignment ID
        EvaluationResult result2 = evaluationService.evaluateAssignment(STUDENT_ID, null, 
            new File("test.java"), tempDir.toString());
        assertEquals(EvaluationStatus.FAILED, result2.getStatus());
        
        // Test non-existent file
        EvaluationResult result3 = evaluationService.evaluateAssignment(STUDENT_ID, ASSIGNMENT_ID, 
            new File("nonexistent.java"), tempDir.toString());
        assertEquals(EvaluationStatus.FAILED, result3.getStatus());
        
        // Test non-Java file
        EvaluationResult result4 = evaluationService.evaluateAssignment(STUDENT_ID, ASSIGNMENT_ID, 
            new File("test.txt"), tempDir.toString());
        assertEquals(EvaluationStatus.FAILED, result4.getStatus());
    }
    
    @Test
    void testEvaluationWithMissingAssignment() throws IOException {
        // Arrange
        File submissionFile = createTestJavaFile("Calculator.java", getValidJavaCode());
        String outputDir = tempDir.toString();
        
        when(assignmentRepository.findById(ASSIGNMENT_ID)).thenReturn(Optional.empty());
        
        // Act
        EvaluationResult result = evaluationService.evaluateAssignment(STUDENT_ID, ASSIGNMENT_ID, submissionFile, outputDir);
        
        // Assert
        assertEquals(EvaluationStatus.FAILED, result.getStatus());
        assertTrue(result.getErrorMessage().contains("Assignment not found"));
        
        // Verify no further processing occurred
        verify(javaCompiler, never()).compile(any(File.class), anyString());
        verify(testRunner, never()).runTests(anyString(), anyList());
    }
    
    @Test
    void testGetEvaluationResult() {
        // Arrange
        Evaluation evaluation = createTestEvaluation();
        when(evaluationRepository.findById(EVALUATION_ID)).thenReturn(Optional.of(evaluation));
        
        // Act
        Optional<EvaluationResult> result = evaluationService.getEvaluationResult(EVALUATION_ID);
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals(EVALUATION_ID, result.get().getEvaluationId());
        assertEquals(EvaluationStatus.COMPLETED, result.get().getStatus());
        assertEquals(BigDecimal.valueOf(85), result.get().getFinalScore());
    }
    
    @Test
    void testGetStudentEvaluationHistory() {
        // Arrange
        List<Evaluation> evaluations = List.of(createTestEvaluation(), createTestEvaluation());
        when(evaluationRepository.findByStudentId(STUDENT_ID)).thenReturn(evaluations);
        
        // Act
        List<Evaluation> result = evaluationService.getStudentEvaluationHistory(STUDENT_ID);
        
        // Assert
        assertEquals(2, result.size());
        verify(evaluationRepository).findByStudentId(STUDENT_ID);
    }
    
    @Test
    void testGetStudentEvaluationStats() {
        // Arrange
        Object[] stats = {5L, BigDecimal.valueOf(78.5), BigDecimal.valueOf(65), BigDecimal.valueOf(95)};
        when(evaluationRepository.getEvaluationStatsByStudent(STUDENT_ID)).thenReturn(stats);
        
        // Act
        Optional<EvaluationService.StudentEvaluationStats> result = 
            evaluationService.getStudentEvaluationStats(STUDENT_ID);
        
        // Assert
        assertTrue(result.isPresent());
        EvaluationService.StudentEvaluationStats evaluationStats = result.get();
        assertEquals(STUDENT_ID, evaluationStats.getStudentId());
        assertEquals(5, evaluationStats.getTotalEvaluations());
        assertEquals(BigDecimal.valueOf(78.5), evaluationStats.getAverageScore());
        assertEquals(BigDecimal.valueOf(65), evaluationStats.getMinScore());
        assertEquals(BigDecimal.valueOf(95), evaluationStats.getMaxScore());
    }
    
    @Test
    void testHandleEvaluationFailure() {
        // Arrange
        Evaluation evaluation = createTestEvaluation();
        when(evaluationRepository.findById(EVALUATION_ID)).thenReturn(Optional.of(evaluation));
        when(evaluationRepository.save(any(Evaluation.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        evaluationService.handleEvaluationFailure(EVALUATION_ID, "Test error message");
        
        // Assert
        verify(evaluationRepository).findById(EVALUATION_ID);
        verify(evaluationRepository).save(argThat(eval -> eval.getStatus() == EvaluationStatus.FAILED));
        verify(dynamoDBRepository).storeErrorLog(eq(EVALUATION_ID), eq("Test error message"), isNull(), isNull());
    }
    
    // Helper methods for creating test data
    
    private File createTestJavaFile(String fileName, String content) throws IOException {
        Path filePath = tempDir.resolve(fileName);
        Files.write(filePath, content.getBytes());
        return filePath.toFile();
    }
    
    private Assignment createTestAssignment() {
        Assignment assignment = new Assignment();
        assignment.setAssignmentId(ASSIGNMENT_ID);
        assignment.setTitle("Test Assignment");
        assignment.setDescription("Test Description");
        assignment.setTestFilePath(tempDir.resolve("TestFile.class").toString());
        assignment.setCreatedAt(LocalDateTime.now());
        return assignment;
    }
    
    private Evaluation createTestEvaluation() {
        Evaluation evaluation = new Evaluation();
        evaluation.setEvaluationId(EVALUATION_ID);
        evaluation.setStudentId(STUDENT_ID);
        evaluation.setAssignmentId(ASSIGNMENT_ID);
        evaluation.setScore(BigDecimal.valueOf(85));
        evaluation.setMaxScore(BigDecimal.valueOf(100));
        evaluation.setStatus(EvaluationStatus.COMPLETED);
        evaluation.setEvaluatedAt(LocalDateTime.now());
        return evaluation;
    }
    
    private CompilationResult createSuccessfulCompilationResult() {
        return new CompilationResult(true, "Compilation successful", 
            List.of(), tempDir.resolve("Calculator.class").toString());
    }
    
    private CompilationResult createFailedCompilationResult() {
        return new CompilationResult(false, "Compilation failed", 
            List.of("Syntax error on line 5"), null);
    }
    
    private TestExecutionResult createSuccessfulTestResult() {
        List<TestCase> testCases = List.of(
            new TestCase("testAdd", true, 100L),
            new TestCase("testSubtract", true, 150L),
            new TestCase("testMultiply", true, 120L)
        );
        return new TestExecutionResult(3, 3, 0, testCases, "All tests passed", 370L);
    }
    
    private TestExecutionResult createFailedTestResult() {
        List<TestCase> testCases = List.of(
            new TestCase("testAdd", true, 100L),
            new TestCase("testSubtract", false, "Expected 5 but was 3", "AssertionError", 150L),
            new TestCase("testMultiply", false, "Expected 20 but was 15", "AssertionError", 120L)
        );
        return new TestExecutionResult(3, 1, 2, testCases, "Some tests failed", 370L);
    }
    
    private String getValidJavaCode() {
        return """
            public class Calculator {
                public int add(int a, int b) {
                    return a + b;
                }
                
                public int subtract(int a, int b) {
                    return a - b;
                }
                
                public int multiply(int a, int b) {
                    return a * b;
                }
            }
            """;
    }
    
    private String getInvalidJavaCode() {
        return """
            public class InvalidCode {
                public int add(int a, int b {  // Missing closing parenthesis
                    return a + b;
                }
            }
            """;
    }
}