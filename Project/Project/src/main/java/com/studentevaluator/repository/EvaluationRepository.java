package com.studentevaluator.repository;

import com.studentevaluator.model.Evaluation;
import com.studentevaluator.model.EvaluationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Evaluation entity operations.
 * Provides methods for score retrieval, filtering, and evaluation management.
 */
@Repository
public interface EvaluationRepository extends JpaRepository<Evaluation, String> {
    
    /**
     * Find evaluations by student ID.
     * 
     * @param studentId the student ID to search for
     * @return list of evaluations for the student
     */
    List<Evaluation> findByStudentId(String studentId);
    
    /**
     * Find evaluations by assignment ID.
     * 
     * @param assignmentId the assignment ID to search for
     * @return list of evaluations for the assignment
     */
    List<Evaluation> findByAssignmentId(String assignmentId);
    
    /**
     * Find evaluations by student and assignment.
     * 
     * @param studentId the student ID
     * @param assignmentId the assignment ID
     * @return list of evaluations for the specific student-assignment combination
     */
    List<Evaluation> findByStudentIdAndAssignmentId(String studentId, String assignmentId);
    
    /**
     * Find evaluations by status.
     * 
     * @param status the evaluation status to filter by
     * @return list of evaluations with the specified status
     */
    List<Evaluation> findByStatus(EvaluationStatus status);
    
    /**
     * Find evaluations by student ID and status.
     * 
     * @param studentId the student ID
     * @param status the evaluation status
     * @return list of evaluations matching both criteria
     */
    List<Evaluation> findByStudentIdAndStatus(String studentId, EvaluationStatus status);
    
    /**
     * Find evaluations evaluated after a specific date.
     * 
     * @param date the date to filter by
     * @return list of evaluations evaluated after the given date
     */
    List<Evaluation> findByEvaluatedAtAfter(LocalDateTime date);
    
    /**
     * Find evaluations evaluated between two dates.
     * 
     * @param startDate the start date
     * @param endDate the end date
     * @return list of evaluations evaluated within the date range
     */
    List<Evaluation> findByEvaluatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Find evaluations with score greater than or equal to minimum.
     * 
     * @param minScore the minimum score threshold
     * @return list of evaluations with score >= minScore
     */
    List<Evaluation> findByScoreGreaterThanEqual(BigDecimal minScore);
    
    /**
     * Find evaluations with score between two values.
     * 
     * @param minScore the minimum score
     * @param maxScore the maximum score
     * @return list of evaluations with score in the range
     */
    List<Evaluation> findByScoreBetween(BigDecimal minScore, BigDecimal maxScore);
    
    /**
     * Find the latest evaluation for a student-assignment combination.
     * 
     * @param studentId the student ID
     * @param assignmentId the assignment ID
     * @return Optional containing the latest evaluation if found
     */
    Optional<Evaluation> findTopByStudentIdAndAssignmentIdOrderByEvaluatedAtDesc(String studentId, String assignmentId);
    
    /**
     * Find the highest score for a student-assignment combination.
     * 
     * @param studentId the student ID
     * @param assignmentId the assignment ID
     * @return Optional containing the evaluation with highest score if found
     */
    Optional<Evaluation> findTopByStudentIdAndAssignmentIdOrderByScoreDesc(String studentId, String assignmentId);
    
    /**
     * Get average score for a student across all assignments.
     * 
     * @param studentId the student ID
     * @return average score for the student
     */
    @Query("SELECT AVG(e.score) FROM Evaluation e WHERE e.studentId = :studentId AND e.status = 'COMPLETED'")
    Optional<BigDecimal> getAverageScoreByStudent(@Param("studentId") String studentId);
    
    /**
     * Get average score for an assignment across all students.
     * 
     * @param assignmentId the assignment ID
     * @return average score for the assignment
     */
    @Query("SELECT AVG(e.score) FROM Evaluation e WHERE e.assignmentId = :assignmentId AND e.status = 'COMPLETED'")
    Optional<BigDecimal> getAverageScoreByAssignment(@Param("assignmentId") String assignmentId);
    
    /**
     * Get total score for a student across all assignments.
     * 
     * @param studentId the student ID
     * @return total score for the student
     */
    @Query("SELECT SUM(e.score) FROM Evaluation e WHERE e.studentId = :studentId AND e.status = 'COMPLETED'")
    Optional<BigDecimal> getTotalScoreByStudent(@Param("studentId") String studentId);
    
    /**
     * Get highest score for a student.
     * 
     * @param studentId the student ID
     * @return highest score achieved by the student
     */
    @Query("SELECT MAX(e.score) FROM Evaluation e WHERE e.studentId = :studentId AND e.status = 'COMPLETED'")
    Optional<BigDecimal> getHighestScoreByStudent(@Param("studentId") String studentId);
    
    /**
     * Count evaluations by status.
     * 
     * @param status the evaluation status
     * @return count of evaluations with the specified status
     */
    long countByStatus(EvaluationStatus status);
    
    /**
     * Count evaluations for a student.
     * 
     * @param studentId the student ID
     * @return count of evaluations for the student
     */
    long countByStudentId(String studentId);
    
    /**
     * Count evaluations for an assignment.
     * 
     * @param assignmentId the assignment ID
     * @return count of evaluations for the assignment
     */
    long countByAssignmentId(String assignmentId);
    
    /**
     * Find evaluations with score percentage above threshold.
     * Custom query to find evaluations where (score/maxScore) * 100 >= threshold.
     * 
     * @param threshold the percentage threshold (0-100)
     * @return list of evaluations meeting the threshold
     */
    @Query("SELECT e FROM Evaluation e WHERE e.status = 'COMPLETED' AND e.maxScore > 0 " +
           "AND (e.score / e.maxScore) * 100 >= :threshold")
    List<Evaluation> findEvaluationsAbovePercentageThreshold(@Param("threshold") double threshold);
    
    /**
     * Find recent evaluations for a student (last N days).
     * 
     * @param studentId the student ID
     * @param days number of days to look back
     * @return list of recent evaluations for the student
     */
    @Query("SELECT e FROM Evaluation e WHERE e.studentId = :studentId " +
           "AND e.evaluatedAt >= :cutoffDate ORDER BY e.evaluatedAt DESC")
    List<Evaluation> findRecentEvaluationsByStudent(@Param("studentId") String studentId, 
                                                   @Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * Get evaluation statistics for a student.
     * Returns count, average, min, max scores.
     * 
     * @param studentId the student ID
     * @return Object array containing statistics
     */
    @Query("SELECT COUNT(e), AVG(e.score), MIN(e.score), MAX(e.score) " +
           "FROM Evaluation e WHERE e.studentId = :studentId AND e.status = 'COMPLETED'")
    Object[] getEvaluationStatsByStudent(@Param("studentId") String studentId);
}