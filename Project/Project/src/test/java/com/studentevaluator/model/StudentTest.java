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

@DisplayName("Student Entity Tests")
class StudentTest {
    
    private Validator validator;
    
    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }
    
    @Test
    @DisplayName("Should create student with valid data")
    void shouldCreateStudentWithValidData() {
        // Given
        String studentId = "STU001";
        String name = "John Doe";
        String email = "john.doe@example.com";
        
        // When
        Student student = new Student(studentId, name, email);
        
        // Then
        assertEquals(studentId, student.getStudentId());
        assertEquals(name, student.getName());
        assertEquals(email, student.getEmail());
        assertNotNull(student.getCreatedAt());
        assertTrue(student.getCreatedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
    }
    
    @Test
    @DisplayName("Should create student with required fields only")
    void shouldCreateStudentWithRequiredFieldsOnly() {
        // Given
        String studentId = "STU002";
        String name = "Jane Smith";
        
        // When
        Student student = new Student(studentId, name);
        
        // Then
        assertEquals(studentId, student.getStudentId());
        assertEquals(name, student.getName());
        assertNull(student.getEmail());
        assertNotNull(student.getCreatedAt());
    }
    
    @Test
    @DisplayName("Should validate student ID is not blank")
    void shouldValidateStudentIdNotBlank() {
        // Given
        Student student = new Student();
        student.setStudentId("");
        student.setName("John Doe");
        
        // When
        Set<ConstraintViolation<Student>> violations = validator.validate(student);
        
        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Student ID cannot be blank")));
    }
    
    @Test
    @DisplayName("Should validate student name is not blank")
    void shouldValidateStudentNameNotBlank() {
        // Given
        Student student = new Student();
        student.setStudentId("STU001");
        student.setName("");
        
        // When
        Set<ConstraintViolation<Student>> violations = validator.validate(student);
        
        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Student name cannot be blank")));
    }
    
    @Test
    @DisplayName("Should validate email format")
    void shouldValidateEmailFormat() {
        // Given
        Student student = new Student();
        student.setStudentId("STU001");
        student.setName("John Doe");
        student.setEmail("invalid-email");
        
        // When
        Set<ConstraintViolation<Student>> violations = validator.validate(student);
        
        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Email should be valid")));
    }
    
    @Test
    @DisplayName("Should validate student ID length")
    void shouldValidateStudentIdLength() {
        // Given
        String longStudentId = "A".repeat(51); // 51 characters
        Student student = new Student();
        student.setStudentId(longStudentId);
        student.setName("John Doe");
        
        // When
        Set<ConstraintViolation<Student>> violations = validator.validate(student);
        
        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Student ID cannot exceed 50 characters")));
    }
    
    @Test
    @DisplayName("Should validate student name length")
    void shouldValidateStudentNameLength() {
        // Given
        String longName = "A".repeat(101); // 101 characters
        Student student = new Student();
        student.setStudentId("STU001");
        student.setName(longName);
        
        // When
        Set<ConstraintViolation<Student>> violations = validator.validate(student);
        
        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Student name cannot exceed 100 characters")));
    }
    
    @Test
    @DisplayName("Should validate email length")
    void shouldValidateEmailLength() {
        // Given
        String longEmail = "a".repeat(90) + "@example.com"; // > 100 characters
        Student student = new Student();
        student.setStudentId("STU001");
        student.setName("John Doe");
        student.setEmail(longEmail);
        
        // When
        Set<ConstraintViolation<Student>> violations = validator.validate(student);
        
        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Email cannot exceed 100 characters")));
    }
    
    @Test
    @DisplayName("Should set created at on pre persist")
    void shouldSetCreatedAtOnPrePersist() {
        // Given
        Student student = new Student();
        student.setStudentId("STU001");
        student.setName("John Doe");
        
        // When
        student.onCreate(); // Simulate @PrePersist
        
        // Then
        assertNotNull(student.getCreatedAt());
    }
    
    @Test
    @DisplayName("Should not override existing created at on pre persist")
    void shouldNotOverrideExistingCreatedAtOnPrePersist() {
        // Given
        LocalDateTime existingTime = LocalDateTime.now().minusDays(1);
        Student student = new Student();
        student.setStudentId("STU001");
        student.setName("John Doe");
        student.setCreatedAt(existingTime);
        
        // When
        student.onCreate(); // Simulate @PrePersist
        
        // Then
        assertEquals(existingTime, student.getCreatedAt());
    }
    
    @Test
    @DisplayName("Should implement equals and hashCode correctly")
    void shouldImplementEqualsAndHashCodeCorrectly() {
        // Given
        Student student1 = new Student("STU001", "John Doe");
        Student student2 = new Student("STU001", "Jane Smith");
        Student student3 = new Student("STU002", "John Doe");
        
        // Then
        assertEquals(student1, student2); // Same ID
        assertNotEquals(student1, student3); // Different ID
        assertEquals(student1.hashCode(), student2.hashCode()); // Same ID
        assertNotEquals(student1.hashCode(), student3.hashCode()); // Different ID
    }
    
    @Test
    @DisplayName("Should implement toString correctly")
    void shouldImplementToStringCorrectly() {
        // Given
        Student student = new Student("STU001", "John Doe", "john@example.com");
        
        // When
        String toString = student.toString();
        
        // Then
        assertTrue(toString.contains("STU001"));
        assertTrue(toString.contains("John Doe"));
        assertTrue(toString.contains("john@example.com"));
    }
}