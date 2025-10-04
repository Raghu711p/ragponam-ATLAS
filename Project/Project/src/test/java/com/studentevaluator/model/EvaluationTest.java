package com.studentevaluator.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Evaluation Entity Tests")
class EvaluationTest {
    
    private Validator validator;
    
    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }
    
    @Test
    @DisplayName("Should create evaluation with valid data")
    void shouldCreateEvaluationWithValidData() {
        // Given
        String evaluationId = "EVAL001";
        String studentId = "STU001";
        String assignmentId = "ASG001";
        BigDecimal score = new BigDecimal("85.50");
        BigDecimal maxScore = new BigDecimal("100.00");
        EvaluationStatus status = EvaluationStatus.COMPLETED;
        
        // When
        Evaluation evaluation = new Evaluation(evaluationId, studentId, assignmentId, score, maxScore, status);
        
        // Then
        assertEquals(evaluationId, evaluation.getEvaluationId());
        assertEquals(studentId, evaluation.getStudentId());
        assertEquals(assignmentId, evaluation.getAssignmentId());
        assertEquals(score, evaluation.getScore());
        assertEquals(maxScore, evaluation.getMaxScore());
        assertEquals(status, evaluation.getStatus());
        assertNotNull(evaluation.getEvaluatedAt());
        assertTrue(evaluation.getEvaluatedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
    }
    
    @Test
    @DisplayName("Should create evaluation with required fields only")
    void shouldCreateEvaluationWithRequiredFieldsOnly() {
        // Given
        String evaluationId = "EVAL002";
        String studentId = "STU002";
        String assignmentId = "ASG002";
        EvaluationStatus status = EvaluationStatus.PENDING;
        
        // When
        Evaluation evaluation = new Evaluation(evaluationId, studentId, assignmentId, status);
        
        // Then
        assertEquals(evaluationId, evaluation.getEvaluationId());
        assertEquals(studentId, evaluation.getStudentId());
        assertEquals(assignmentId, evaluation.getAssignmentId());
        assertEquals(status, evaluation.getStatus());
        assertNull(evaluation.getScore());
        assertNull(evaluation.getMaxScore());
        assertNotNull(evaluation.getEvaluatedAt());
    }
    
    @Test
    @DisplayName("Should validate evaluation ID is not blank")
    void shouldValidateEvaluationIdNotBlank() {
        // Given
        Evaluation evaluation = new Evaluation();
        evaluation.setEvaluationId("");
        evaluation.setStudentId("STU001");
        evaluation.setAssignmentId("ASG001");
        evaluation.setStatus(EvaluationStatus.PENDING);
        
        // When
        Set<ConstraintViolation<Evaluation>> violations = validator.validate(evaluation);
        
        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Evaluation ID cannot be blank")));
    }
    
    @Test
    @DisplayName("Should validate student ID is not blank")
    void shouldValidateStudentIdNotBlank() {
        // Given
        Evaluation evaluation = new Evaluation();
        evaluation.setEvaluationId("EVAL001");
        evaluation.setStudentId("");
        evaluation.setAssignmentId("ASG001");
        evaluation.setStatus(EvaluationStatus.PENDING);
        
        // When
        Set<ConstraintViolation<Evaluation>> violations = validator.validate(evaluation);
        
        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Student ID cannot be blank")));
    }
    
    @Test
    @DisplayName("Should validate assignment ID is not blank")
    void shouldValidateAssignmentIdNotBlank() {
        // Given
        Evaluation evaluation = new Evaluation();
        evaluation.setEvaluationId("EVAL001");
        evaluation.setStudentId("STU001");
        evaluation.setAssignmentId("");
        evaluation.setStatus(EvaluationStatus.PENDING);
        
        // When
        Set<ConstraintViolation<Evaluation>> violations = validator.validate(evaluation);
        
        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Assignment ID cannot be blank")));
    }
    
    @Test
    @DisplayName("Should validate status is not null")
    void shouldValidateStatusNotNull() {
        // Given
        Evaluation evaluation = new Evaluation();
        evaluation.setEvaluationId("EVAL001");
        evaluation.setStudentId("STU001");
        evaluation.setAssignmentId("ASG001");
        evaluation.setStatus(null);
        
        // When
        Set<ConstraintViolation<Evaluation>> violations = validator.validate(evaluation);
        
        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Evaluation status cannot be null")));
    }
    
    @Test
    @DisplayName("Should validate score is not negative")
    void shouldValidateScoreNotNegative() {
        // Given
        Evaluation evaluation = new Evaluation();
        evaluation.setEvaluationId("EVAL001");
        evaluation.setStudentId("STU001");
        evaluation.setAssignmentId("ASG001");
        evaluation.setStatus(EvaluationStatus.COMPLETED);
        evaluation.setScore(new BigDecimal("-10.00"));
        
        // When
        Set<ConstraintViolation<Evaluation>> violations = validator.validate(evaluation);
        
        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Score cannot be negative")));
    }
    
    @Test
    @DisplayName("Should validate max score is not negative")
    void shouldValidateMaxScoreNotNegative() {
        // Given
        Evaluation evaluation = new Evaluation();
        evaluation.setEvaluationId("EVAL001");
        evaluation.setStudentId("STU001");
        evaluation.setAssignmentId("ASG001");
        evaluation.setStatus(EvaluationStatus.COMPLETED);
        evaluation.setMaxScore(new BigDecimal("-100.00"));
        
        // When
        Set<ConstraintViolation<Evaluation>> violations = validator.validate(evaluation);
        
        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Max score cannot be negative")));
    }
    
    @Test
    @DisplayName("Should validate score does not exceed maximum")
    void shouldValidateScoreDoesNotExceedMaximum() {
        // Given
        Evaluation evaluation = new Evaluation();
        evaluation.setEvaluationId("EVAL001");
        evaluation.setStudentId("STU001");
        evaluation.setAssignmentId("ASG001");
        evaluation.setStatus(EvaluationStatus.COMPLETED);
        evaluation.setScore(new BigDecimal("1000.00"));
        
        // When
        Set<ConstraintViolation<Evaluation>> violations = validator.validate(evaluation);
        
        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Score cannot exceed 999.99")));
    }
    
    @Test
    @DisplayName("Should set evaluated at on pre persist")
    void shouldSetEvaluatedAtOnPrePersist() {
        // Given
        Evaluation evaluation = new Evaluation();
        evaluation.setEvaluationId("EVAL001");
        evaluation.setStudentId("STU001");
        evaluation.setAssignmentId("ASG001");
        evaluation.setStatus(EvaluationStatus.PENDING);
        
        // When
        evaluation.onCreate(); // Simulate @PrePersist
        
        // Then
        assertNotNull(evaluation.getEvaluatedAt());
    }
    
    @Test
    @DisplayName("Should not override existing evaluated at on pre persist")
    void shouldNotOverrideExistingEvaluatedAtOnPrePersist() {
        // Given
        LocalDateTime existingTime = LocalDateTime.now().minusDays(1);
        Evaluation evaluation = new Evaluation();
        evaluation.setEvaluationId("EVAL001");
        evaluation.setStudentId("STU001");
        evaluation.setAssignmentId("ASG001");
        evaluation.setStatus(EvaluationStatus.PENDING);
        evaluation.setEvaluatedAt(existingTime);
        
        // When
        evaluation.onCreate(); // Simulate @PrePersist
        
        // Then
        assertEquals(existingTime, evaluation.getEvaluatedAt());
    }
    
    @Test
    @DisplayName("Should implement equals and hashCode correctly")
    void shouldImplementEqualsAndHashCodeCorrectly() {
        // Given
        Evaluation evaluation1 = new Evaluation("EVAL001", "STU001", "ASG001", EvaluationStatus.COMPLETED);
        Evaluation evaluation2 = new Evaluation("EVAL001", "STU002", "ASG002", EvaluationStatus.PENDING);
        Evaluation evaluation3 = new Evaluation("EVAL002", "STU001", "ASG001", EvaluationStatus.COMPLETED);
        
        // Then
        assertEquals(evaluation1, evaluation2); // Same ID
        assertNotEquals(evaluation1, evaluation3); // Different ID
        assertEquals(evaluation1.hashCode(), evaluation2.hashCode()); // Same ID
        assertNotEquals(evaluation1.hashCode(), evaluation3.hashCode()); // Different ID
    }
    
    @Test
    @DisplayName("Should implement toString correctly")
    void shouldImplementToStringCorrectly() {
        // Given
        Evaluation evaluation = new Evaluation("EVAL001", "STU001", "ASG001", 
                new BigDecimal("85.50"), new BigDecimal("100.00"), EvaluationStatus.COMPLETED);
        
        // When
        String toString = evaluation.toString();
        
        // Then
        assertTrue(toString.contains("EVAL001"));
        assertTrue(toString.contains("STU001"));
        assertTrue(toString.contains("ASG001"));
        assertTrue(toString.contains("85.50"));
        assertTrue(toString.contains("COMPLETED"));
    }
    
    @Test
    @DisplayName("Should handle foreign key relationships")
    void shouldHandleForeignKeyRelationships() {
        // Given
        Student student = new Student("STU001", "John Doe");
        Assignment assignment = new Assignment("ASG001", "Java Basics");
        Evaluation evaluation = new Evaluation("EVAL001", "STU001", "ASG001", EvaluationStatus.PENDING);
        
        // When
        evaluation.setStudent(student);
        evaluation.setAssignment(assignment);
        
        // Then
        assertEquals(student, evaluation.getStudent());
        assertEquals(assignment, evaluation.getAssignment());
        assertEquals("STU001", evaluation.getStudentId());
        assertEquals("ASG001", evaluation.getAssignmentId());
    }
}