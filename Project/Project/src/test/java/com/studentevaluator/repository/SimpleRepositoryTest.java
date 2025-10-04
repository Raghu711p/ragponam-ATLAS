package com.studentevaluator.repository;

import com.studentevaluator.model.Assignment;
import com.studentevaluator.model.Evaluation;
import com.studentevaluator.model.EvaluationStatus;
import com.studentevaluator.model.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Simple integration tests for repositories using H2 in-memory database.
 * This test doesn't require Docker/Testcontainers.
 */
@DataJpaTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
class SimpleRepositoryTest {
    
    @Autowired
    private StudentRepository studentRepository;
    
    @Autowired
    private AssignmentRepository assignmentRepository;
    
    @Autowired
    private EvaluationRepository evaluationRepository;
    
    @BeforeEach
    void setUp() {
        // Tables will be created automatically by Hibernate
        // No need to clean up since we're using create-drop
    }
    
    @Test
    void testStudentRepositoryBasicOperations() {
        // Create and save a student
        Student student = new Student("STU001", "John Doe", "john.doe@example.com");
        Student saved = studentRepository.save(student);
        
        assertThat(saved.getStudentId()).isEqualTo("STU001");
        assertThat(saved.getName()).isEqualTo("John Doe");
        assertThat(saved.getEmail()).isEqualTo("john.doe@example.com");
        assertThat(saved.getCreatedAt()).isNotNull();
        
        // Test findById
        Optional<Student> found = studentRepository.findById("STU001");
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("John Doe");
        
        // Test findByEmail
        Optional<Student> foundByEmail = studentRepository.findByEmail("john.doe@example.com");
        assertThat(foundByEmail).isPresent();
        assertThat(foundByEmail.get().getStudentId()).isEqualTo("STU001");
        
        // Test existsByEmail
        assertThat(studentRepository.existsByEmail("john.doe@example.com")).isTrue();
        assertThat(studentRepository.existsByEmail("nonexistent@example.com")).isFalse();
        
        // Test count
        assertThat(studentRepository.count()).isEqualTo(1);
    }
    
    @Test
    void testAssignmentRepositoryBasicOperations() {
        // Create and save an assignment
        Assignment assignment = new Assignment("ASG001", "Basic Java Programming", 
                "Introduction to Java basics", "/tests/basic-java-tests.java");
        Assignment saved = assignmentRepository.save(assignment);
        
        assertThat(saved.getAssignmentId()).isEqualTo("ASG001");
        assertThat(saved.getTitle()).isEqualTo("Basic Java Programming");
        assertThat(saved.getDescription()).isEqualTo("Introduction to Java basics");
        assertThat(saved.getTestFilePath()).isEqualTo("/tests/basic-java-tests.java");
        assertThat(saved.getCreatedAt()).isNotNull();
        
        // Test findById
        Optional<Assignment> found = assignmentRepository.findById("ASG001");
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Basic Java Programming");
        
        // Test findByTitle
        Optional<Assignment> foundByTitle = assignmentRepository.findByTitle("Basic Java Programming");
        assertThat(foundByTitle).isPresent();
        assertThat(foundByTitle.get().getAssignmentId()).isEqualTo("ASG001");
        
        // Test hasTestFile
        assertThat(assignmentRepository.hasTestFile("ASG001")).isTrue();
        
        // Test updateTestFilePath
        int updated = assignmentRepository.updateTestFilePath("ASG001", "/tests/updated-tests.java");
        assertThat(updated).isEqualTo(1);
        
        Optional<Assignment> updatedAssignment = assignmentRepository.findById("ASG001");
        assertThat(updatedAssignment).isPresent();
        assertThat(updatedAssignment.get().getTestFilePath()).isEqualTo("/tests/updated-tests.java");
    }
    
    @Test
    void testEvaluationRepositoryBasicOperations() {
        // First create student and assignment
        Student student = new Student("STU001", "John Doe", "john.doe@example.com");
        studentRepository.save(student);
        
        Assignment assignment = new Assignment("ASG001", "Basic Java", "Java basics", null);
        assignmentRepository.save(assignment);
        
        // Create and save evaluation
        Evaluation evaluation = new Evaluation("EVAL001", "STU001", "ASG001", 
                new BigDecimal("85.50"), new BigDecimal("100.00"), EvaluationStatus.COMPLETED);
        Evaluation saved = evaluationRepository.save(evaluation);
        
        assertThat(saved.getEvaluationId()).isEqualTo("EVAL001");
        assertThat(saved.getStudentId()).isEqualTo("STU001");
        assertThat(saved.getAssignmentId()).isEqualTo("ASG001");
        assertThat(saved.getScore()).isEqualByComparingTo(new BigDecimal("85.50"));
        assertThat(saved.getMaxScore()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(saved.getStatus()).isEqualTo(EvaluationStatus.COMPLETED);
        assertThat(saved.getEvaluatedAt()).isNotNull();
        
        // Test findById
        Optional<Evaluation> found = evaluationRepository.findById("EVAL001");
        assertThat(found).isPresent();
        assertThat(found.get().getScore()).isEqualByComparingTo(new BigDecimal("85.50"));
        
        // Test findByStudentId
        List<Evaluation> studentEvaluations = evaluationRepository.findByStudentId("STU001");
        assertThat(studentEvaluations).hasSize(1);
        assertThat(studentEvaluations.get(0).getEvaluationId()).isEqualTo("EVAL001");
        
        // Test findByAssignmentId
        List<Evaluation> assignmentEvaluations = evaluationRepository.findByAssignmentId("ASG001");
        assertThat(assignmentEvaluations).hasSize(1);
        assertThat(assignmentEvaluations.get(0).getEvaluationId()).isEqualTo("EVAL001");
        
        // Test findByStatus
        List<Evaluation> completedEvaluations = evaluationRepository.findByStatus(EvaluationStatus.COMPLETED);
        assertThat(completedEvaluations).hasSize(1);
        assertThat(completedEvaluations.get(0).getEvaluationId()).isEqualTo("EVAL001");
        
        // Test countByStatus
        long completedCount = evaluationRepository.countByStatus(EvaluationStatus.COMPLETED);
        assertThat(completedCount).isEqualTo(1);
        
        long pendingCount = evaluationRepository.countByStatus(EvaluationStatus.PENDING);
        assertThat(pendingCount).isEqualTo(0);
    }
    
    @Test
    void testRepositoryRelationships() {
        // Create test data
        Student student1 = new Student("STU001", "John Doe", "john.doe@example.com");
        Student student2 = new Student("STU002", "Jane Smith", "jane.smith@example.com");
        studentRepository.saveAll(List.of(student1, student2));
        
        Assignment assignment1 = new Assignment("ASG001", "Java Basics", "Basic Java concepts", null);
        Assignment assignment2 = new Assignment("ASG002", "OOP Java", "Object-oriented programming", null);
        assignmentRepository.saveAll(List.of(assignment1, assignment2));
        
        // Create evaluations
        Evaluation eval1 = new Evaluation("EVAL001", "STU001", "ASG001", 
                new BigDecimal("85.50"), new BigDecimal("100.00"), EvaluationStatus.COMPLETED);
        Evaluation eval2 = new Evaluation("EVAL002", "STU001", "ASG002", 
                new BigDecimal("92.00"), new BigDecimal("100.00"), EvaluationStatus.COMPLETED);
        Evaluation eval3 = new Evaluation("EVAL003", "STU002", "ASG001", 
                new BigDecimal("78.25"), new BigDecimal("100.00"), EvaluationStatus.COMPLETED);
        
        evaluationRepository.saveAll(List.of(eval1, eval2, eval3));
        
        // Test complex queries
        List<Evaluation> student1Evaluations = evaluationRepository.findByStudentId("STU001");
        assertThat(student1Evaluations).hasSize(2);
        
        List<Evaluation> assignment1Evaluations = evaluationRepository.findByAssignmentId("ASG001");
        assertThat(assignment1Evaluations).hasSize(2);
        
        // Test average score calculation
        Optional<BigDecimal> avgScoreStudent1 = evaluationRepository.getAverageScoreByStudent("STU001");
        assertThat(avgScoreStudent1).isPresent();
        // Average of 85.50 and 92.00 = 88.75
        assertThat(avgScoreStudent1.get()).isEqualByComparingTo(new BigDecimal("88.75"));
        
        // Test total score calculation
        Optional<BigDecimal> totalScoreStudent1 = evaluationRepository.getTotalScoreByStudent("STU001");
        assertThat(totalScoreStudent1).isPresent();
        // Sum of 85.50 and 92.00 = 177.50
        assertThat(totalScoreStudent1.get()).isEqualByComparingTo(new BigDecimal("177.50"));
        
        // Test highest score
        Optional<BigDecimal> highestScoreStudent1 = evaluationRepository.getHighestScoreByStudent("STU001");
        assertThat(highestScoreStudent1).isPresent();
        assertThat(highestScoreStudent1.get()).isEqualByComparingTo(new BigDecimal("92.00"));
    }
    
    @Test
    void testStudentRepositoryCustomQueries() {
        // Create test students
        Student student1 = new Student("STU001", "John Doe", "john.doe@example.com");
        Student student2 = new Student("STU002", "Jane Johnson", "jane.johnson@example.com");
        Student student3 = new Student("STU003", "Bob Smith", "bob.smith@example.com");
        studentRepository.saveAll(List.of(student1, student2, student3));
        
        // Test name pattern search
        List<Student> johnsons = studentRepository.findByNameContainingIgnoreCase("john");
        assertThat(johnsons).hasSize(2);
        assertThat(johnsons).extracting(Student::getName)
                .containsExactlyInAnyOrder("John Doe", "Jane Johnson");
        
        // Test custom name pattern query
        List<Student> namePatternResults = studentRepository.findByNamePattern("doe");
        assertThat(namePatternResults).hasSize(1);
        assertThat(namePatternResults.get(0).getName()).isEqualTo("John Doe");
    }
    
    @Test
    void testAssignmentRepositoryCustomQueries() {
        // Create test assignments
        Assignment assignment1 = new Assignment("ASG001", "Basic Java Programming", 
                "Introduction to Java basics", "/tests/basic-java-tests.java");
        Assignment assignment2 = new Assignment("ASG002", "Advanced Java Programming", 
                "Advanced Java concepts", "/tests/advanced-java-tests.java");
        Assignment assignment3 = new Assignment("ASG003", "Data Structures", 
                "Arrays and Lists implementation", null);
        
        assignmentRepository.saveAll(List.of(assignment1, assignment2, assignment3));
        
        // Test title search
        List<Assignment> programmingAssignments = assignmentRepository.findByTitleContainingIgnoreCase("programming");
        assertThat(programmingAssignments).hasSize(2);
        
        // Test assignments with test files
        List<Assignment> withTestFiles = assignmentRepository.findByTestFilePathIsNotNull();
        assertThat(withTestFiles).hasSize(2);
        
        // Test assignments without test files
        List<Assignment> withoutTestFiles = assignmentRepository.findByTestFilePathIsNull();
        assertThat(withoutTestFiles).hasSize(1);
        assertThat(withoutTestFiles.get(0).getAssignmentId()).isEqualTo("ASG003");
        
        // Test description pattern search
        List<Assignment> javaAssignments = assignmentRepository.findByDescriptionPattern("java");
        assertThat(javaAssignments).hasSize(2);
    }
}