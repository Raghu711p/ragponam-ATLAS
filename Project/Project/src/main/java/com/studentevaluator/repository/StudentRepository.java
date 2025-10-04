package com.studentevaluator.repository;

import com.studentevaluator.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Student entity operations.
 * Extends JpaRepository to provide basic CRUD operations and custom query methods.
 */
@Repository
public interface StudentRepository extends JpaRepository<Student, String> {
    
    /**
     * Find student by email address.
     * 
     * @param email the email address to search for
     * @return Optional containing the student if found
     */
    Optional<Student> findByEmail(String email);
    
    /**
     * Find students by name containing the given string (case-insensitive).
     * 
     * @param name the name pattern to search for
     * @return list of students matching the name pattern
     */
    List<Student> findByNameContainingIgnoreCase(String name);
    
    /**
     * Find students created after a specific date.
     * 
     * @param date the date to filter by
     * @return list of students created after the given date
     */
    List<Student> findByCreatedAtAfter(LocalDateTime date);
    
    /**
     * Find students created between two dates.
     * 
     * @param startDate the start date
     * @param endDate the end date
     * @return list of students created within the date range
     */
    List<Student> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Check if a student exists by email.
     * 
     * @param email the email to check
     * @return true if student exists with the given email
     */
    boolean existsByEmail(String email);
    
    /**
     * Count students created after a specific date.
     * 
     * @param date the date to filter by
     * @return count of students created after the given date
     */
    long countByCreatedAtAfter(LocalDateTime date);
    
    /**
     * Find students who have submitted evaluations.
     * Custom query to find students with at least one evaluation.
     * 
     * @return list of students who have evaluations
     */
    @Query("SELECT DISTINCT s FROM Student s WHERE s.studentId IN " +
           "(SELECT e.studentId FROM Evaluation e)")
    List<Student> findStudentsWithEvaluations();
    
    /**
     * Find students by name pattern with custom query.
     * 
     * @param namePattern the name pattern to search for
     * @return list of students matching the pattern
     */
    @Query("SELECT s FROM Student s WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :namePattern, '%'))")
    List<Student> findByNamePattern(@Param("namePattern") String namePattern);
    
    /**
     * Find students with evaluation count.
     * Custom query to get students along with their evaluation count.
     * 
     * @return list of Object arrays containing student and evaluation count
     */
    @Query("SELECT s, COUNT(e) FROM Student s LEFT JOIN Evaluation e ON s.studentId = e.studentId " +
           "GROUP BY s.studentId, s.name, s.email, s.createdAt")
    List<Object[]> findStudentsWithEvaluationCount();
}