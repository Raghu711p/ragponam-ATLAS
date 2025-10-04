package com.studentevaluator.service;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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
import com.studentevaluator.repository.AssignmentRepository;
import com.studentevaluator.repository.DynamoDBRepository;
import com.studentevaluator.repository.EvaluationRepository;
import com.studentevaluator.repository.StudentRepository;

/**
 * Simple test to verify EvaluationService functionality.
 */
@ExtendWith(MockitoExtension.class)
class EvaluationServiceSimpleTest {
    
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
    
    @Test
    void testEvaluationServiceCreation() {
        // Test that EvaluationService can be created with all dependencies
        EvaluationService evaluationService = new EvaluationService(
            javaCompiler, testRunner, evaluationRepository, assignmentRepository,
            studentRepository, dynamoDBRepository, scoreCache
        );
        
        assertNotNull(evaluationService);
    }
    
    @Test
    void testSuccessfulEvaluationWithNoTestFiles() {
        // Arrange
        EvaluationService evaluationService = new EvaluationService(
            javaCompiler, testRunner, evaluationRepository, assignmentRepository,
            studentRepository, dynamoDBRepository, scoreCache
        );
        
        File mockFile = mock(File.class);
        when(mockFile.exists()).thenReturn(true);
        when(mockFile.canRead()).thenReturn(true);
        when(mockFile.getName()).thenReturn("Calculator.java");
        
        Assignment assignment = new Assignment();
        assignment.setAssignmentId("test-assignment");
        assignment.setTitle("Test Assignment");
        assignment.setTestFilePath(null); // No test file path
        assignment.setCreatedAt(LocalDateTime.now());
        
        CompilationResult compilationResult = new CompilationResult(true, "Success", List.of(), "/path/to/class");
        
        when(assignmentRepository.findById("test-assignment")).thenReturn(Optional.of(assignment));
        when(javaCompiler.compile(any(File.class), anyString())).thenReturn(compilationResult);
        when(evaluationRepository.save(any(Evaluation.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        EvaluationResult result = evaluationService.evaluateAssignment("student123", "test-assignment", mockFile, "/output");
        
        // Assert
        assertNotNull(result);
        assertEquals(EvaluationStatus.FAILED, result.getStatus()); // Should fail because no test files
        assertEquals("No test files available for assignment", result.getErrorMessage());
        
        // Verify interactions
        verify(javaCompiler).compile(mockFile, "/output");
        verify(dynamoDBRepository).storeCompilationLog(anyString(), eq("Success"), eq("student123"), eq("test-assignment"));
        verifyNoInteractions(testRunner); // Should not run tests if no test files
        verifyNoInteractions(scoreCache); // Should not update cache on failure
    }
    
    @Test
    void testInvalidInputHandling() {
        // Arrange
        EvaluationService evaluationService = new EvaluationService(
            javaCompiler, testRunner, evaluationRepository, assignmentRepository,
            studentRepository, dynamoDBRepository, scoreCache
        );
        
        File mockFile = mock(File.class);
        
        // Act & Assert - null student ID
        EvaluationResult result1 = evaluationService.evaluateAssignment(null, "assignment", mockFile, "/output");
        assertEquals(EvaluationStatus.FAILED, result1.getStatus());
        assertEquals("Invalid input parameters", result1.getErrorMessage());
        
        // Act & Assert - null assignment ID
        EvaluationResult result2 = evaluationService.evaluateAssignment("student", null, mockFile, "/output");
        assertEquals(EvaluationStatus.FAILED, result2.getStatus());
        assertEquals("Invalid input parameters", result2.getErrorMessage());
        
        // Verify no interactions with other services
        verifyNoInteractions(javaCompiler);
        verifyNoInteractions(testRunner);
        verifyNoInteractions(scoreCache);
    }
}