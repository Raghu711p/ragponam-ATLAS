package com.studentevaluator.repository;

import com.studentevaluator.model.Assignment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for AssignmentRepository.
 */
class AssignmentRepositoryTest extends BaseRepositoryTest {
    
    @Autowired
    private AssignmentRepository assignmentRepository;
    
    private Assignment testAssignment1;
    private Assignment testAssignment2;
    private Assignment testAssignment3;
    
    @BeforeEach
    void setUpTestData() {
        // Clear any existing data
        assignmentRepository.deleteAll();
        
        // Create test assignments
        testAssignment1 = new Assignment("ASG001", "Basic Java Programming", 
                "Introduction to Java basics", "/tests/basic-java-tests.java");
        testAssignment2 = new Assignment("ASG002", "Object-Oriented Programming", 
                "OOP concepts in Java", "/tests/oop-tests.java");
        testAssignment3 = new Assignment("ASG003", "Data Structures", 
                "Arrays and Lists implementation", null);
        
        // Save test data
        assignmentRepository.saveAll(List.of(testAssignment1, testAssignment2, testAssignment3));
    }
    
    @Test
    void testFindByTitle() {
        Optional<Assignment> found = assignmentRepository.findByTitle("Basic Java Programming");
        assertThat(found).isPresent();
        assertThat(found.get().getAssignmentId()).isEqualTo("ASG001");
        assertThat(found.get().getDescription()).isEqualTo("Introduction to Java basics");
        
        Optional<Assignment> notFound = assignmentRepository.findByTitle("Nonexistent Assignment");
        assertThat(notFound).isEmpty();
    }
    
    @Test
    void testFindByTitleContainingIgnoreCase() {
        List<Assignment> results = assignmentRepository.findByTitleContainingIgnoreCase("java");
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTitle()).isEqualTo("Basic Java Programming");
        
        // Test case insensitive search
        List<Assignment> caseInsensitiveResults = assignmentRepository.findByTitleContainingIgnoreCase("PROGRAMMING");
        assertThat(caseInsensitiveResults).hasSize(2);
        assertThat(caseInsensitiveResults).extracting(Assignment::getTitle)
                .containsExactlyInAnyOrder("Basic Java Programming", "Object-Oriented Programming");
    }
    
    @Test
    void testFindByCreatedAtAfter() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(1);
        List<Assignment> results = assignmentRepository.findByCreatedAtAfter(cutoffTime);
        assertThat(results).hasSize(3);
        
        LocalDateTime futureTime = LocalDateTime.now().plusHours(1);
        List<Assignment> futureResults = assignmentRepository.findByCreatedAtAfter(futureTime);
        assertThat(futureResults).isEmpty();
    }
    
    @Test
    void testFindByCreatedAtBetween() {
        LocalDateTime start = LocalDateTime.now().minusHours(1);
        LocalDateTime end = LocalDateTime.now().plusHours(1);
        
        List<Assignment> results = assignmentRepository.findByCreatedAtBetween(start, end);
        assertThat(results).hasSize(3);
    }
    
    @Test
    void testFindByTestFilePathIsNotNull() {
        List<Assignment> withTestFiles = assignmentRepository.findByTestFilePathIsNotNull();
        assertThat(withTestFiles).hasSize(2);
        assertThat(withTestFiles).extracting(Assignment::getAssignmentId)
                .containsExactlyInAnyOrder("ASG001", "ASG002");
    }
    
    @Test
    void testFindByTestFilePathIsNull() {
        List<Assignment> withoutTestFiles = assignmentRepository.findByTestFilePathIsNull();
        assertThat(withoutTestFiles).hasSize(1);
        assertThat(withoutTestFiles.get(0).getAssignmentId()).isEqualTo("ASG003");
    }
    
    @Test
    void testFindByTestFilePathContaining() {
        List<Assignment> results = assignmentRepository.findByTestFilePathContaining("basic-java");
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getAssignmentId()).isEqualTo("ASG001");
        
        List<Assignment> testsResults = assignmentRepository.findByTestFilePathContaining("/tests/");
        assertThat(testsResults).hasSize(2);
    }
    
    @Test
    void testUpdateTestFilePath() {
        int updatedCount = assignmentRepository.updateTestFilePath("ASG003", "/tests/data-structures-tests.java");
        assertThat(updatedCount).isEqualTo(1);
        
        Optional<Assignment> updated = assignmentRepository.findById("ASG003");
        assertThat(updated).isPresent();
        assertThat(updated.get().getTestFilePath()).isEqualTo("/tests/data-structures-tests.java");
        
        // Test updating non-existent assignment
        int notUpdatedCount = assignmentRepository.updateTestFilePath("NONEXISTENT", "/some/path");
        assertThat(notUpdatedCount).isEqualTo(0);
    }
    
    @Test
    void testRemoveTestFileAssociation() {
        int removedCount = assignmentRepository.removeTestFileAssociation("ASG001");
        assertThat(removedCount).isEqualTo(1);
        
        Optional<Assignment> updated = assignmentRepository.findById("ASG001");
        assertThat(updated).isPresent();
        assertThat(updated.get().getTestFilePath()).isNull();
        
        // Test removing from non-existent assignment
        int notRemovedCount = assignmentRepository.removeTestFileAssociation("NONEXISTENT");
        assertThat(notRemovedCount).isEqualTo(0);
    }
    
    @Test
    void testFindByDescriptionPattern() {
        List<Assignment> results = assignmentRepository.findByDescriptionPattern("java");
        assertThat(results).hasSize(2);
        assertThat(results).extracting(Assignment::getAssignmentId)
                .containsExactlyInAnyOrder("ASG001", "ASG002");
        
        List<Assignment> conceptsResults = assignmentRepository.findByDescriptionPattern("concepts");
        assertThat(conceptsResults).hasSize(1);
        assertThat(conceptsResults.get(0).getAssignmentId()).isEqualTo("ASG002");
    }
    
    @Test
    void testHasTestFile() {
        boolean hasTestFile1 = assignmentRepository.hasTestFile("ASG001");
        assertThat(hasTestFile1).isTrue();
        
        boolean hasTestFile3 = assignmentRepository.hasTestFile("ASG003");
        assertThat(hasTestFile3).isFalse();
        
        // Test non-existent assignment
        boolean hasTestFileNonExistent = assignmentRepository.hasTestFile("NONEXISTENT");
        assertThat(hasTestFileNonExistent).isFalse();
    }
    
    @Test
    @Sql("/test-data/evaluations.sql")
    void testFindAssignmentsWithEvaluations() {
        List<Assignment> assignmentsWithEvaluations = assignmentRepository.findAssignmentsWithEvaluations();
        assertThat(assignmentsWithEvaluations).isNotNull();
        // Assertions will depend on the test data in the SQL script
    }
    
    @Test
    @Sql("/test-data/evaluations.sql")
    void testFindAssignmentsWithEvaluationCount() {
        List<Object[]> results = assignmentRepository.findAssignmentsWithEvaluationCount();
        assertThat(results).isNotNull();
        assertThat(results).hasSize(3); // All assignments should be returned
        
        // Each result should contain Assignment and Long (count)
        for (Object[] result : results) {
            assertThat(result).hasSize(2);
            assertThat(result[0]).isInstanceOf(Assignment.class);
            assertThat(result[1]).isInstanceOf(Long.class);
        }
    }
    
    @Test
    void testBasicCrudOperations() {
        // Test save
        Assignment newAssignment = new Assignment("ASG004", "Advanced Java", 
                "Advanced Java concepts", "/tests/advanced-java-tests.java");
        Assignment saved = assignmentRepository.save(newAssignment);
        assertThat(saved.getAssignmentId()).isEqualTo("ASG004");
        assertThat(saved.getCreatedAt()).isNotNull();
        
        // Test findById
        Optional<Assignment> found = assignmentRepository.findById("ASG004");
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Advanced Java");
        
        // Test update
        found.get().setDescription("Updated description");
        Assignment updated = assignmentRepository.save(found.get());
        assertThat(updated.getDescription()).isEqualTo("Updated description");
        
        // Test delete
        assignmentRepository.deleteById("ASG004");
        Optional<Assignment> deleted = assignmentRepository.findById("ASG004");
        assertThat(deleted).isEmpty();
    }
    
    @Test
    void testFindAll() {
        List<Assignment> allAssignments = assignmentRepository.findAll();
        assertThat(allAssignments).hasSize(3);
        assertThat(allAssignments).extracting(Assignment::getAssignmentId)
                .containsExactlyInAnyOrder("ASG001", "ASG002", "ASG003");
    }
    
    @Test
    void testCount() {
        long count = assignmentRepository.count();
        assertThat(count).isEqualTo(3);
    }
}