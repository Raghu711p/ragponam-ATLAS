package com.studentevaluator.repository;

import com.studentevaluator.model.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Assignment entity operations.
 * Provides methods for assignment management and test file association.
 */
@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, String> {
    
    /**
     * Find assignment by title.
     * 
     * @param title the title to search for
     * @return Optional containing the assignment if found
     */
    Optional<Assignment> findByTitle(String title);
    
    /**
     * Find assignments by title containing the given string (case-insensitive).
     * 
     * @param title the title pattern to search for
     * @return list of assignments matching the title pattern
     */
    List<Assignment> findByTitleContainingIgnoreCase(String title);
    
    /**
     * Find assignments created after a specific date.
     * 
     * @param date the date to filter by
     * @return list of assignments created after the given date
     */
    List<Assignment> findByCreatedAtAfter(LocalDateTime date);
    
    /**
     * Find assignments created between two dates.
     * 
     * @param startDate the start date
     * @param endDate the end date
     * @return list of assignments created within the date range
     */
    List<Assignment> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Find assignments that have test files associated.
     * 
     * @return list of assignments with test files
     */
    List<Assignment> findByTestFilePathIsNotNull();
    
    /**
     * Find assignments that do not have test files associated.
     * 
     * @return list of assignments without test files
     */
    List<Assignment> findByTestFilePathIsNull();
    
    /**
     * Find assignments by test file path pattern.
     * 
     * @param pathPattern the path pattern to search for
     * @return list of assignments matching the test file path pattern
     */
    List<Assignment> findByTestFilePathContaining(String pathPattern);
    
    /**
     * Update test file path for an assignment.
     * Custom method to associate test files with assignments.
     * 
     * @param assignmentId the assignment ID
     * @param testFilePath the path to the test file
     * @return number of updated records
     */
    @Modifying
    @Transactional
    @Query("UPDATE Assignment a SET a.testFilePath = :testFilePath WHERE a.assignmentId = :assignmentId")
    int updateTestFilePath(@Param("assignmentId") String assignmentId, 
                          @Param("testFilePath") String testFilePath);
    
    /**
     * Remove test file association from an assignment.
     * 
     * @param assignmentId the assignment ID
     * @return number of updated records
     */
    @Modifying
    @Transactional
    @Query("UPDATE Assignment a SET a.testFilePath = NULL WHERE a.assignmentId = :assignmentId")
    int removeTestFileAssociation(@Param("assignmentId") String assignmentId);
    
    /**
     * Find assignments with evaluations.
     * Custom query to find assignments that have been evaluated.
     * 
     * @return list of assignments with evaluations
     */
    @Query("SELECT DISTINCT a FROM Assignment a WHERE a.assignmentId IN " +
           "(SELECT e.assignmentId FROM Evaluation e)")
    List<Assignment> findAssignmentsWithEvaluations();
    
    /**
     * Find assignments by description pattern.
     * 
     * @param descriptionPattern the description pattern to search for
     * @return list of assignments matching the description pattern
     */
    @Query("SELECT a FROM Assignment a WHERE LOWER(a.description) LIKE LOWER(CONCAT('%', :descriptionPattern, '%'))")
    List<Assignment> findByDescriptionPattern(@Param("descriptionPattern") String descriptionPattern);
    
    /**
     * Find assignments with evaluation count.
     * Custom query to get assignments along with their evaluation count.
     * 
     * @return list of Object arrays containing assignment and evaluation count
     */
    @Query("SELECT a, COUNT(e) FROM Assignment a LEFT JOIN Evaluation e ON a.assignmentId = e.assignmentId " +
           "GROUP BY a.assignmentId, a.title, a.description, a.testFilePath, a.createdAt")
    List<Object[]> findAssignmentsWithEvaluationCount();
    
    /**
     * Check if assignment has associated test file.
     * 
     * @param assignmentId the assignment ID to check
     * @return true if assignment has test file associated
     */
    @Query("SELECT CASE WHEN a.testFilePath IS NOT NULL THEN true ELSE false END " +
           "FROM Assignment a WHERE a.assignmentId = :assignmentId")
    boolean hasTestFile(@Param("assignmentId") String assignmentId);
}