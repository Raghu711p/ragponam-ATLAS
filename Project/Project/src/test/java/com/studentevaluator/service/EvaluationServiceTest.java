package com.studentevaluator.service;

import java.io.File;
import java.math.BigDecimal;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
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
import com.studentevaluator.model.TestExecutionResult;
import com.studentevaluator.repository.AssignmentRepository;
import com.studentevaluator.repository.DynamoDBRepository;
import com.studentevaluator.repository.EvaluationRepository;
import com.studentevaluator.repository.StudentRepository;

/**
 * Unit tests for EvaluationService.
 * Tests individual methods and business logic without external dependencies.
 */
@ExtendWith(MockitoExtension.class)
class EvaluationServiceTest {
    
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
    void testEvaluateAssignment_ValidInputs_Success() {
        // Arrange
        File mockFile = mock(File.class);
        when(mockFile.exists()).thenReturn(true);
        when(mockFile.canRead()).thenReturn(true);
        when(mockFile.getName()).thenReturn("Calculator.java");
        
        Assignment assignment = createTestAssignment();
        CompilationResult compilationResult = new CompilationResult(true, "Success", List.of(), "/path/to/class");
        TestExecutionResult testResult = new TestExecutionResult(3, 3, 0, List.of(), "All passed", 1000L);
        
        when(assignmentRepository.findById(ASSIGNMENT_ID)).thenReturn(Optional.of(assignment));
        when(javaCompiler.compile(any(File.class), anyString())).thenReturn(compilationResult);
        when(testRunner.runTests(anyString(), anyList())).thenReturn(testResult);
        when(evaluationRepository.save(any(Evaluation.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        EvaluationResult result = evaluationService.evaluateAssignment(STUDENT_ID, ASSIGNMENT_ID, mockFile, "/output");
        
        // Assert
        assertNotNull(result);
        assertEquals(EvaluationStatus.COMPLETED, result.getStatus());
        assertTrue(result.isSuccessful());
        assertEquals(BigDecimal.valueOf(3), result.getFinalScore());
        assertEquals(BigDecimal.valueOf(3), result.getMaxScore());
        
        verify(scoreCache).updateScore(STUDENT_ID, BigDecimal.valueOf(3));
        verify(dynamoDBRepository).storeCompilationLog(anyString(), eq("Success"), eq(STUDENT_ID), eq(ASSIGNMENT_ID));
        verify(dynamoDBRepository).storeTestExecutionLog(anyString(), eq("All passed"), eq(STUDENT_ID), eq(ASSIGNMENT_ID));
    }
    
    @Test
    void testEvaluateAssignment_NullStudentId_ReturnsFailure() {
        // Arrange
        File mockFile = mock(File.class);
        
        // Act
        EvaluationResult result = evaluationService.evaluateAssignment(null, ASSIGNMENT_ID, mockFile, "/output");
        
        // Assert
        assertEquals(EvaluationStatus.FAILED, result.getStatus());
        assertEquals("Invalid input parameters", result.getErrorMessage());
        
        verifyNoInteractions(javaCompiler);
        verifyNoInteractions(testRunner);
        verifyNoInteractions(scoreCache);
    }
    
    @Test
    void testEvaluateAssignment_NullAssignmentId_ReturnsFailure() {
        // Arrange
        File mockFile = mock(File.class);
        
        // Act
        EvaluationResult result = evaluationService.evaluateAssignment(STUDENT_ID, null, mockFile, "/output");
        
        // Assert
        assertEquals(EvaluationStatus.FAILED, result.getStatus());
        assertEquals("Invalid input parameters", result.getErrorMessage());
        
        verifyNoInteractions(javaCompiler);
        verifyNoInteractions(testRunner);
    }
    
    @Test
    void testEvaluateAssignment_InvalidFile_ReturnsFailure() {
        // Arrange
        File mockFile = mock(File.class);
        when(mockFile.exists()).thenReturn(false);
        
        // Act
        EvaluationResult result = evaluationService.evaluateAssignment(STUDENT_ID, ASSIGNMENT_ID, mockFile, "/output");
        
        // Assert
        assertEquals(EvaluationStatus.FAILED, result.getStatus());
        assertEquals("Invalid input parameters", result.getErrorMessage());
        
        verifyNoInteractions(javaCompiler);
        verifyNoInteractions(testRunner);
    }
    
    @Test
    void testEvaluateAssignment_NonJavaFile_ReturnsFailure() {
        // Arrange
        File mockFile = mock(File.class);
        when(mockFile.exists()).thenReturn(true);
        when(mockFile.canRead()).thenReturn(true);
        when(mockFile.getName()).thenReturn("document.txt");
        
        // Act
        EvaluationResult result = evaluationService.evaluateAssignment(STUDENT_ID, ASSIGNMENT_ID, mockFile, "/output");
        
        // Assert
        assertEquals(EvaluationStatus.FAILED, result.getStatus());
        assertEquals("Invalid input parameters", result.getErrorMessage());
        
        verifyNoInteractions(javaCompiler);
        verifyNoInteractions(testRunner);
    }
    
    @Test
    void testEvaluateAssignment_AssignmentNotFound_ReturnsFailure() {
        // Arrange
        File mockFile = mock(File.class);
        when(mockFile.exists()).thenReturn(true);
        when(mockFile.canRead()).thenReturn(true);
        when(mockFile.getName()).thenReturn("Calculator.java");
        
        when(assignmentRepository.findById(ASSIGNMENT_ID)).thenReturn(Optional.empty());
        
        // Act
        EvaluationResult result = evaluationService.evaluateAssignment(STUDENT_ID, ASSIGNMENT_ID, mockFile, "/output");
        
        // Assert
        assertEquals(EvaluationStatus.FAILED, result.getStatus());
        assertTrue(result.getErrorMessage().contains("Assignment not found"));
        
        verifyNoInteractions(javaCompiler);
        verifyNoInteractions(testRunner);
    }
    
    @Test
    void testEvaluateAssignment_CompilationFailure_ReturnsFailure() {
        // Arrange
        File mockFile = mock(File.class);
        when(mockFile.exists()).thenReturn(true);
        when(mockFile.canRead()).thenReturn(true);
        when(mockFile.getName()).thenReturn("Calculator.java");
        
        Assignment assignment = createTestAssignment();
        CompilationResult compilationResult = new CompilationResult(false, "Compilation failed", 
            List.of("Syntax error"), null);
        
        when(assignmentRepository.findById(ASSIGNMENT_ID)).thenReturn(Optional.of(assignment));
        when(javaCompiler.compile(any(File.class), anyString())).thenReturn(compilationResult);
        when(evaluationRepository.save(any(Evaluation.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        EvaluationResult result = evaluationService.evaluateAssignment(STUDENT_ID, ASSIGNMENT_ID, mockFile, "/output");
        
        // Assert
        assertEquals(EvaluationStatus.FAILED, result.getStatus());
        assertFalse(result.isSuccessful());
        assertEquals(BigDecimal.ZERO, result.getFinalScore());
        
        verify(javaCompiler).compile(mockFile, "/output");
        verifyNoInteractions(testRunner);
        verifyNoInteractions(scoreCache);
    }
    
    @Test
    void testEvaluateAssignmentAsync_Success() throws Exception {
        // Arrange
        File mockFile = mock(File.class);
        when(mockFile.exists()).thenReturn(true);
        when(mockFile.canRead()).thenReturn(true);
        when(mockFile.getName()).thenReturn("Calculator.java");
        
        Assignment assignment = createTestAssignment();
        CompilationResult compilationResult = new CompilationResult(true, "Success", List.of(), "/path/to/class");
        TestExecutionResult testResult = new TestExecutionResult(3, 3, 0, List.of(), "All passed", 1000L);
        
        when(assignmentRepository.findById(ASSIGNMENT_ID)).thenReturn(Optional.of(assignment));
        when(javaCompiler.compile(any(File.class), anyString())).thenReturn(compilationResult);
        when(testRunner.runTests(anyString(), anyList())).thenReturn(testResult);
        when(evaluationRepository.save(any(Evaluation.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        CompletableFuture<EvaluationResult> future = evaluationService.evaluateAssignmentAsync(
            STUDENT_ID, ASSIGNMENT_ID, mockFile, "/output");
        EvaluationResult result = future.get();
        
        // Assert
        assertNotNull(result);
        assertEquals(EvaluationStatus.COMPLETED, result.getStatus());
        assertTrue(result.isSuccessful());
        assertTrue(future.isDone());
    }
    
    @Test
    void testGetEvaluationResult_Found() {
        // Arrange
        Evaluation evaluation = createTestEvaluation();
        when(evaluationRepository.findById("eval123")).thenReturn(Optional.of(evaluation));
        
        // Act
        Optional<EvaluationResult> result = evaluationService.getEvaluationResult("eval123");
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals("eval123", result.get().getEvaluationId());
        assertEquals(EvaluationStatus.COMPLETED, result.get().getStatus());
    }
    
    @Test
    void testGetEvaluationResult_NotFound() {
        // Arrange
        when(evaluationRepository.findById("nonexistent")).thenReturn(Optional.empty());
        
        // Act
        Optional<EvaluationResult> result = evaluationService.getEvaluationResult("nonexistent");
        
        // Assert
        assertFalse(result.isPresent());
    }
    
    @Test
    void testGetStudentEvaluationHistory() {
        // Arrange
        List<Evaluation> evaluations = List.of(createTestEvaluation());
        when(evaluationRepository.findByStudentId(STUDENT_ID)).thenReturn(evaluations);
        
        // Act
        List<Evaluation> result = evaluationService.getStudentEvaluationHistory(STUDENT_ID);
        
        // Assert
        assertEquals(1, result.size());
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
        assertEquals(5, result.get().getTotalEvaluations());
        assertEquals(BigDecimal.valueOf(78.5), result.get().getAverageScore());
    }
    
    @Test
    void testHandleEvaluationFailure() {
        // Arrange
        Evaluation evaluation = createTestEvaluation();
        when(evaluationRepository.findById("eval123")).thenReturn(Optional.of(evaluation));
        when(evaluationRepository.save(any(Evaluation.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        evaluationService.handleEvaluationFailure("eval123", "Test error");
        
        // Assert
        verify(evaluationRepository).save(argThat(eval -> eval.getStatus() == EvaluationStatus.FAILED));
        verify(dynamoDBRepository).storeErrorLog(eq("eval123"), eq("Test error"), isNull(), isNull());
    }
    
    // Helper methods
    private Assignment createTestAssignment() {
        Assignment assignment = new Assignment();
        assignment.setAssignmentId(ASSIGNMENT_ID);
        assignment.setTitle("Test Assignment");
        assignment.setDescription("Test Description");
        assignment.setTestFilePath("/path/to/test.class");
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
}