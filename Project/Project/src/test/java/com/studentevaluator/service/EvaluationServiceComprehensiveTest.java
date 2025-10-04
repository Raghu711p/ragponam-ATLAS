package com.studentevaluator.service;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.studentevaluator.engine.JUnitTestRunner;
import com.studentevaluator.engine.JavaCompiler;
import com.studentevaluator.model.Assignment;
import com.studentevaluator.model.CompilationResult;
import com.studentevaluator.model.Evaluation;
import com.studentevaluator.model.EvaluationResult;
import com.studentevaluator.model.EvaluationStatus;
import com.studentevaluator.model.TestCase;
import com.studentevaluator.model.TestExecutionResult;
import com.studentevaluator.repository.AssignmentRepository;
import com.studentevaluator.repository.DynamoDBRepository;
import com.studentevaluator.repository.EvaluationRepository;
import com.studentevaluator.repository.StudentRepository;

/**
 * Comprehensive test for EvaluationService covering all major functionality.
 * This test demonstrates the complete evaluation orchestration workflow.
 */
@ExtendWith(MockitoExtension.class)
class EvaluationServiceComprehensiveTest {
    
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
    
    @BeforeEach
    void setUp() {
        evaluationService = new EvaluationService(
            javaCompiler, testRunner, evaluationRepository, assignmentRepository,
            studentRepository, dynamoDBRepository, scoreCache
        );
    }
    
    @Test
    void testCompleteSuccessfulEvaluationWorkflow() throws IOException {
        // Arrange - Create a real test file
        File submissionFile = createTestJavaFile("Calculator.java", getValidJavaCode());
        File testFile = createTestJavaFile("CalculatorTest.class", "test content");
        
        Assignment assignment = createTestAssignment();
        assignment.setTestFilePath(testFile.getAbsolutePath());
        
        CompilationResult compilationResult = new CompilationResult(true, "Compilation successful", 
            List.of(), tempDir.resolve("Calculator.class").toString());
        
        TestExecutionResult testResult = new TestExecutionResult(5, 4, 1, 
            createTestCases(), "4 tests passed, 1 failed", 2500L);
        
        when(assignmentRepository.findById(ASSIGNMENT_ID)).thenReturn(Optional.of(assignment));
        when(javaCompiler.compile(submissionFile, tempDir.toString())).thenReturn(compilationResult);
        when(testRunner.runTests(anyString(), anyList())).thenReturn(testResult);
        when(evaluationRepository.save(any(Evaluation.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        EvaluationResult result = evaluationService.evaluateAssignment(
            STUDENT_ID, ASSIGNMENT_ID, submissionFile, tempDir.toString());
        
        // Assert - Verify complete workflow
        assertNotNull(result);
        assertEquals(EvaluationStatus.COMPLETED, result.getStatus());
        assertTrue(result.isSuccessful());
        assertEquals(BigDecimal.valueOf(4), result.getFinalScore()); // 4 passed tests
        assertEquals(BigDecimal.valueOf(5), result.getMaxScore()); // 5 total tests
        assertEquals(80.0, result.getScorePercentage(), 0.01); // 4/5 = 80%
        
        // Verify all components were called in correct order
        verify(assignmentRepository).findById(ASSIGNMENT_ID);
        verify(javaCompiler).compile(submissionFile, tempDir.toString());
        verify(testRunner).runTests(compilationResult.getCompiledClassPath(), List.of(testFile));
        verify(evaluationRepository, times(2)).save(any(Evaluation.class)); // Initial save + final update
        verify(scoreCache).updateScore(STUDENT_ID, BigDecimal.valueOf(4));
        
        // Verify logging to DynamoDB
        verify(dynamoDBRepository).storeCompilationLog(anyString(), eq("Compilation successful"), 
            eq(STUDENT_ID), eq(ASSIGNMENT_ID));
        verify(dynamoDBRepository).storeTestExecutionLog(anyString(), eq("4 tests passed, 1 failed"), 
            eq(STUDENT_ID), eq(ASSIGNMENT_ID));
    }
    
    @Test
    void testEvaluationWithCompilationFailure() throws IOException {
        // Arrange
        File submissionFile = createTestJavaFile("InvalidCode.java", getInvalidJavaCode());
        Assignment assignment = createTestAssignment();
        
        CompilationResult compilationResult = new CompilationResult(false, "Compilation failed", 
            List.of("Syntax error on line 3: missing semicolon", "Undefined variable 'x'"), null);
        
        when(assignmentRepository.findById(ASSIGNMENT_ID)).thenReturn(Optional.of(assignment));
        when(javaCompiler.compile(submissionFile, tempDir.toString())).thenReturn(compilationResult);
        when(evaluationRepository.save(any(Evaluation.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        EvaluationResult result = evaluationService.evaluateAssignment(
            STUDENT_ID, ASSIGNMENT_ID, submissionFile, tempDir.toString());
        
        // Assert
        assertNotNull(result);
        assertEquals(EvaluationStatus.FAILED, result.getStatus());
        assertFalse(result.isSuccessful());
        assertTrue(result.hasCompilationErrors());
        assertEquals(BigDecimal.ZERO, result.getFinalScore());
        assertEquals(BigDecimal.ZERO, result.getMaxScore());
        
        // Verify compilation was attempted but tests were not run
        verify(javaCompiler).compile(submissionFile, tempDir.toString());
        verifyNoInteractions(testRunner);
        verifyNoInteractions(scoreCache);
        
        // Verify compilation failure was logged
        verify(dynamoDBRepository).storeCompilationLog(anyString(), eq("Compilation failed"), 
            eq(STUDENT_ID), eq(ASSIGNMENT_ID));
    }
    
    @Test
    void testAsyncEvaluationExecution() throws Exception {
        // Arrange
        File submissionFile = createTestJavaFile("Calculator.java", getValidJavaCode());
        Assignment assignment = createTestAssignment();
        
        CompilationResult compilationResult = new CompilationResult(true, "Success", List.of(), "/path/to/class");
        
        when(assignmentRepository.findById(ASSIGNMENT_ID)).thenReturn(Optional.of(assignment));
        when(javaCompiler.compile(any(File.class), anyString())).thenReturn(compilationResult);
        when(evaluationRepository.save(any(Evaluation.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        CompletableFuture<EvaluationResult> future = evaluationService.evaluateAssignmentAsync(
            STUDENT_ID, ASSIGNMENT_ID, submissionFile, tempDir.toString());
        
        // Wait for completion
        EvaluationResult result = future.get();
        
        // Assert
        assertNotNull(result);
        assertTrue(future.isDone());
        assertFalse(future.isCancelled());
        assertEquals(EvaluationStatus.FAILED, result.getStatus()); // Fails due to no test files
    }
    
    @Test
    void testErrorHandlingAndRecovery() {
        // Arrange
        File mockFile = mock(File.class);
        when(mockFile.exists()).thenReturn(true);
        when(mockFile.canRead()).thenReturn(true);
        when(mockFile.getName()).thenReturn("Calculator.java");
        
        Assignment assignment = createTestAssignment();
        when(assignmentRepository.findById(ASSIGNMENT_ID)).thenReturn(Optional.of(assignment));
        when(javaCompiler.compile(any(File.class), anyString())).thenThrow(new RuntimeException("Compiler crashed"));
        when(evaluationRepository.save(any(Evaluation.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        EvaluationResult result = evaluationService.evaluateAssignment(
            STUDENT_ID, ASSIGNMENT_ID, mockFile, tempDir.toString());
        
        // Assert
        assertEquals(EvaluationStatus.FAILED, result.getStatus());
        assertTrue(result.getErrorMessage().contains("Evaluation failed"));
        
        // Verify error was logged to DynamoDB
        verify(dynamoDBRepository).storeErrorLog(anyString(), contains("Compiler crashed"), 
            eq(STUDENT_ID), eq(ASSIGNMENT_ID));
    }
    
    @Test
    void testEvaluationResultRetrieval() {
        // Arrange
        Evaluation evaluation = createTestEvaluation();
        when(evaluationRepository.findById("eval123")).thenReturn(Optional.of(evaluation));
        
        // Act
        Optional<EvaluationResult> result = evaluationService.getEvaluationResult("eval123");
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals("eval123", result.get().getEvaluationId());
        assertEquals(EvaluationStatus.COMPLETED, result.get().getStatus());
        assertEquals(BigDecimal.valueOf(85), result.get().getFinalScore());
        assertEquals(BigDecimal.valueOf(100), result.get().getMaxScore());
    }
    
    @Test
    void testStudentEvaluationHistory() {
        // Arrange
        List<Evaluation> evaluations = List.of(
            createTestEvaluation(),
            createTestEvaluation()
        );
        when(evaluationRepository.findByStudentId(STUDENT_ID)).thenReturn(evaluations);
        
        // Act
        List<Evaluation> result = evaluationService.getStudentEvaluationHistory(STUDENT_ID);
        
        // Assert
        assertEquals(2, result.size());
        verify(evaluationRepository).findByStudentId(STUDENT_ID);
    }
    
    @Test
    void testStudentEvaluationStatistics() {
        // Arrange
        Object[] stats = {10L, BigDecimal.valueOf(82.5), BigDecimal.valueOf(65), BigDecimal.valueOf(98)};
        when(evaluationRepository.getEvaluationStatsByStudent(STUDENT_ID)).thenReturn(stats);
        
        // Act
        Optional<EvaluationService.StudentEvaluationStats> result = 
            evaluationService.getStudentEvaluationStats(STUDENT_ID);
        
        // Assert
        assertTrue(result.isPresent());
        EvaluationService.StudentEvaluationStats evaluationStats = result.get();
        assertEquals(STUDENT_ID, evaluationStats.getStudentId());
        assertEquals(10, evaluationStats.getTotalEvaluations());
        assertEquals(BigDecimal.valueOf(82.5), evaluationStats.getAverageScore());
        assertEquals(BigDecimal.valueOf(65), evaluationStats.getMinScore());
        assertEquals(BigDecimal.valueOf(98), evaluationStats.getMaxScore());
    }
    
    @Test
    void testEvaluationFailureHandling() {
        // Arrange
        Evaluation evaluation = createTestEvaluation();
        when(evaluationRepository.findById("eval123")).thenReturn(Optional.of(evaluation));
        when(evaluationRepository.save(any(Evaluation.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        evaluationService.handleEvaluationFailure("eval123", "Test execution timeout");
        
        // Assert
        verify(evaluationRepository).save(argThat(eval -> 
            eval.getStatus() == EvaluationStatus.FAILED));
        verify(dynamoDBRepository).storeErrorLog(eq("eval123"), eq("Test execution timeout"), 
            isNull(), isNull());
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
        assignment.setTitle("Calculator Assignment");
        assignment.setDescription("Implement basic calculator operations");
        assignment.setCreatedAt(LocalDateTime.now());
        return assignment;
    }
    
    private Evaluation createTestEvaluation() {
        Evaluation evaluation = new Evaluation();
        evaluation.setEvaluationId("eval123");
        evaluation.setStudentId(STUDENT_ID);
        evaluation.setAssignmentId(ASSIGNMENT_ID);
        evaluation.setScore(BigDecimal.valueOf(85));
        evaluation.setMaxScore(BigDecimal.valueOf(100));
        evaluation.setStatus(EvaluationStatus.COMPLETED);
        evaluation.setEvaluatedAt(LocalDateTime.now());
        return evaluation;
    }
    
    private List<TestCase> createTestCases() {
        return List.of(
            new TestCase("testAdd", true, 150L),
            new TestCase("testSubtract", true, 120L),
            new TestCase("testMultiply", true, 180L),
            new TestCase("testDivide", true, 200L),
            new TestCase("testDivideByZero", false, "Division by zero", "ArithmeticException", 100L)
        );
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
                
                public int divide(int a, int b) {
                    if (b == 0) throw new ArithmeticException("Division by zero");
                    return a / b;
                }
            }
            """;
    }
    
    private String getInvalidJavaCode() {
        return """
            public class InvalidCode {
                public int add(int a, int b {  // Missing closing parenthesis
                    return a + b
                }  // Missing semicolon
                
                public int subtract(int a, int b) {
                    return a - x;  // Undefined variable
                }
            }
            """;
    }
}