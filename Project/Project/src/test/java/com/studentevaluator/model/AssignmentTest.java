package com.studentevaluator.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Assignment Entity Tests")
class AssignmentTest {
    
    private Validator validator;
    
    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }
    
    @Test
    @DisplayName("Should create assignment with valid data")
    void shouldCreateAssignmentWithValidData() {
        // Given
        String assignmentId = "ASG001";
        String title = "Java Basics Assignment";
        String description = "Basic Java programming exercises";
        String testFilePath = "/tests/java-basics-test.java";
        
        // When
        Assignment assignment = new Assignment(assignmentId, title, description, testFilePath);
        
        // Then
        assertEquals(assignmentId, assignment.getAssignmentId());
        assertEquals(title, assignment.getTitle());
        assertEquals(description, assignment.getDescription());
        assertEquals(testFilePath, assignment.getTestFilePath());
        assertNotNull(assignment.getCreatedAt());
        assertTrue(assignment.getCreatedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
    }
    
    @Test
    @DisplayName("Should create assignment with required fields only")
    void shouldCreateAssignmentWithRequiredFieldsOnly() {
        // Given
        String assignmentId = "ASG002";
        String title = "Advanced Java Assignment";
        
        // When
        Assignment assignment = new Assignment(assignmentId, title);
        
        // Then
        assertEquals(assignmentId, assignment.getAssignmentId());
        assertEquals(title, assignment.getTitle());
        assertNull(assignment.getDescription());
        assertNull(assignment.getTestFilePath());
        assertNotNull(assignment.getCreatedAt());
    }
    
    @Test
    @DisplayName("Should validate assignment ID is not blank")
    void shouldValidateAssignmentIdNotBlank() {
        // Given
        Assignment assignment = new Assignment();
        assignment.setAssignmentId("");
        assignment.setTitle("Java Assignment");
        
        // When
        Set<ConstraintViolation<Assignment>> violations = validator.validate(assignment);
        
        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Assignment ID cannot be blank")));
    }
    
    @Test
    @DisplayName("Should validate assignment title is not blank")
    void shouldValidateAssignmentTitleNotBlank() {
        // Given
        Assignment assignment = new Assignment();
        assignment.setAssignmentId("ASG001");
        assignment.setTitle("");
        
        // When
        Set<ConstraintViolation<Assignment>> violations = validator.validate(assignment);
        
        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Assignment title cannot be blank")));
    }
    
    @Test
    @DisplayName("Should validate assignment ID length")
    void shouldValidateAssignmentIdLength() {
        // Given
        String longAssignmentId = "A".repeat(51); // 51 characters
        Assignment assignment = new Assignment();
        assignment.setAssignmentId(longAssignmentId);
        assignment.setTitle("Java Assignment");
        
        // When
        Set<ConstraintViolation<Assignment>> violations = validator.validate(assignment);
        
        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Assignment ID cannot exceed 50 characters")));
    }
    
    @Test
    @DisplayName("Should validate assignment title length")
    void shouldValidateAssignmentTitleLength() {
        // Given
        String longTitle = "A".repeat(201); // 201 characters
        Assignment assignment = new Assignment();
        assignment.setAssignmentId("ASG001");
        assignment.setTitle(longTitle);
        
        // When
        Set<ConstraintViolation<Assignment>> violations = validator.validate(assignment);
        
        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Assignment title cannot exceed 200 characters")));
    }
    
    @Test
    @DisplayName("Should validate test file path length")
    void shouldValidateTestFilePathLength() {
        // Given
        String longPath = "A".repeat(501); // 501 characters
        Assignment assignment = new Assignment();
        assignment.setAssignmentId("ASG001");
        assignment.setTitle("Java Assignment");
        assignment.setTestFilePath(longPath);
        
        // When
        Set<ConstraintViolation<Assignment>> violations = validator.validate(assignment);
        
        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Test file path cannot exceed 500 characters")));
    }
    
    @Test
    @DisplayName("Should set created at on pre persist")
    void shouldSetCreatedAtOnPrePersist() {
        // Given
        Assignment assignment = new Assignment();
        assignment.setAssignmentId("ASG001");
        assignment.setTitle("Java Assignment");
        
        // When
        assignment.onCreate(); // Simulate @PrePersist
        
        // Then
        assertNotNull(assignment.getCreatedAt());
    }
    
    @Test
    @DisplayName("Should not override existing created at on pre persist")
    void shouldNotOverrideExistingCreatedAtOnPrePersist() {
        // Given
        LocalDateTime existingTime = LocalDateTime.now().minusDays(1);
        Assignment assignment = new Assignment();
        assignment.setAssignmentId("ASG001");
        assignment.setTitle("Java Assignment");
        assignment.setCreatedAt(existingTime);
        
        // When
        assignment.onCreate(); // Simulate @PrePersist
        
        // Then
        assertEquals(existingTime, assignment.getCreatedAt());
    }
    
    @Test
    @DisplayName("Should implement equals and hashCode correctly")
    void shouldImplementEqualsAndHashCodeCorrectly() {
        // Given
        Assignment assignment1 = new Assignment("ASG001", "Java Basics");
        Assignment assignment2 = new Assignment("ASG001", "Advanced Java");
        Assignment assignment3 = new Assignment("ASG002", "Java Basics");
        
        // Then
        assertEquals(assignment1, assignment2); // Same ID
        assertNotEquals(assignment1, assignment3); // Different ID
        assertEquals(assignment1.hashCode(), assignment2.hashCode()); // Same ID
        assertNotEquals(assignment1.hashCode(), assignment3.hashCode()); // Different ID
    }
    
    @Test
    @DisplayName("Should implement toString correctly")
    void shouldImplementToStringCorrectly() {
        // Given
        Assignment assignment = new Assignment("ASG001", "Java Basics", "Basic exercises", "/tests/basic.java");
        
        // When
        String toString = assignment.toString();
        
        // Then
        assertTrue(toString.contains("ASG001"));
        assertTrue(toString.contains("Java Basics"));
        assertTrue(toString.contains("Basic exercises"));
        assertTrue(toString.contains("/tests/basic.java"));
    }
}