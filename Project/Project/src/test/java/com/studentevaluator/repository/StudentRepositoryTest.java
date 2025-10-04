package com.studentevaluator.repository;

import com.studentevaluator.model.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for StudentRepository.
 */
class StudentRepositoryTest extends BaseRepositoryTest {
    
    @Autowired
    private StudentRepository studentRepository;
    
    private Student testStudent1;
    private Student testStudent2;
    private Student testStudent3;
    
    @BeforeEach
    void setUpTestData() {
        // Clear any existing data
        studentRepository.deleteAll();
        
        // Create test students
        testStudent1 = new Student("STU001", "John Doe", "john.doe@example.com");
        testStudent2 = new Student("STU002", "Jane Smith", "jane.smith@example.com");
        testStudent3 = new Student("STU003", "Bob Johnson");
        
        // Save test data
        studentRepository.saveAll(List.of(testStudent1, testStudent2, testStudent3));
    }
    
    @Test
    void testFindByEmail() {
        // Test finding existing student by email
        Optional<Student> found = studentRepository.findByEmail("john.doe@example.com");
        assertThat(found).isPresent();
        assertThat(found.get().getStudentId()).isEqualTo("STU001");
        assertThat(found.get().getName()).isEqualTo("John Doe");
        
        // Test finding non-existing student by email
        Optional<Student> notFound = studentRepository.findByEmail("nonexistent@example.com");
        assertThat(notFound).isEmpty();
    }
    
    @Test
    void testFindByNameContainingIgnoreCase() {
        List<Student> results = studentRepository.findByNameContainingIgnoreCase("john");
        assertThat(results).hasSize(2);
        assertThat(results).extracting(Student::getName)
                .containsExactlyInAnyOrder("John Doe", "Bob Johnson");
        
        // Test case insensitive search
        List<Student> caseInsensitiveResults = studentRepository.findByNameContainingIgnoreCase("JANE");
        assertThat(caseInsensitiveResults).hasSize(1);
        assertThat(caseInsensitiveResults.get(0).getName()).isEqualTo("Jane Smith");
    }
    
    @Test
    void testFindByCreatedAtAfter() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(1);
        List<Student> results = studentRepository.findByCreatedAtAfter(cutoffTime);
        assertThat(results).hasSize(3);
        
        // Test with future date
        LocalDateTime futureTime = LocalDateTime.now().plusHours(1);
        List<Student> futureResults = studentRepository.findByCreatedAtAfter(futureTime);
        assertThat(futureResults).isEmpty();
    }
    
    @Test
    void testFindByCreatedAtBetween() {
        LocalDateTime start = LocalDateTime.now().minusHours(1);
        LocalDateTime end = LocalDateTime.now().plusHours(1);
        
        List<Student> results = studentRepository.findByCreatedAtBetween(start, end);
        assertThat(results).hasSize(3);
        
        // Test with narrow range
        LocalDateTime narrowStart = LocalDateTime.now().plusMinutes(1);
        LocalDateTime narrowEnd = LocalDateTime.now().plusMinutes(2);
        List<Student> narrowResults = studentRepository.findByCreatedAtBetween(narrowStart, narrowEnd);
        assertThat(narrowResults).isEmpty();
    }
    
    @Test
    void testExistsByEmail() {
        assertThat(studentRepository.existsByEmail("john.doe@example.com")).isTrue();
        assertThat(studentRepository.existsByEmail("nonexistent@example.com")).isFalse();
    }
    
    @Test
    void testCountByCreatedAtAfter() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(1);
        long count = studentRepository.countByCreatedAtAfter(cutoffTime);
        assertThat(count).isEqualTo(3);
        
        LocalDateTime futureTime = LocalDateTime.now().plusHours(1);
        long futureCount = studentRepository.countByCreatedAtAfter(futureTime);
        assertThat(futureCount).isEqualTo(0);
    }
    
    @Test
    void testFindByNamePattern() {
        List<Student> results = studentRepository.findByNamePattern("doe");
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("John Doe");
        
        // Test partial match
        List<Student> partialResults = studentRepository.findByNamePattern("j");
        assertThat(partialResults).hasSize(3); // John, Jane, Johnson
    }
    
    @Test
    @Sql("/test-data/evaluations.sql")
    void testFindStudentsWithEvaluations() {
        // This test requires evaluation data to be present
        // The SQL script should insert some evaluation records
        List<Student> studentsWithEvaluations = studentRepository.findStudentsWithEvaluations();
        // Assertions will depend on the test data in the SQL script
        assertThat(studentsWithEvaluations).isNotNull();
    }
    
    @Test
    @Sql("/test-data/evaluations.sql")
    void testFindStudentsWithEvaluationCount() {
        List<Object[]> results = studentRepository.findStudentsWithEvaluationCount();
        assertThat(results).isNotNull();
        assertThat(results).hasSize(3); // All students should be returned
        
        // Each result should contain Student and Long (count)
        for (Object[] result : results) {
            assertThat(result).hasSize(2);
            assertThat(result[0]).isInstanceOf(Student.class);
            assertThat(result[1]).isInstanceOf(Long.class);
        }
    }
    
    @Test
    void testBasicCrudOperations() {
        // Test save
        Student newStudent = new Student("STU004", "Alice Brown", "alice.brown@example.com");
        Student saved = studentRepository.save(newStudent);
        assertThat(saved.getStudentId()).isEqualTo("STU004");
        assertThat(saved.getCreatedAt()).isNotNull();
        
        // Test findById
        Optional<Student> found = studentRepository.findById("STU004");
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Alice Brown");
        
        // Test update
        found.get().setEmail("alice.updated@example.com");
        Student updated = studentRepository.save(found.get());
        assertThat(updated.getEmail()).isEqualTo("alice.updated@example.com");
        
        // Test delete
        studentRepository.deleteById("STU004");
        Optional<Student> deleted = studentRepository.findById("STU004");
        assertThat(deleted).isEmpty();
    }
    
    @Test
    void testFindAll() {
        List<Student> allStudents = studentRepository.findAll();
        assertThat(allStudents).hasSize(3);
        assertThat(allStudents).extracting(Student::getStudentId)
                .containsExactlyInAnyOrder("STU001", "STU002", "STU003");
    }
    
    @Test
    void testCount() {
        long count = studentRepository.count();
        assertThat(count).isEqualTo(3);
    }
}