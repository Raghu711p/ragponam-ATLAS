package com.studentevaluator.repository;

import com.studentevaluator.model.Assignment;
import com.studentevaluator.model.Evaluation;
import com.studentevaluator.model.EvaluationStatus;
import com.studentevaluator.model.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for EvaluationRepository.
 */
class EvaluationRepositoryTest extends BaseRepositoryTest {
    
    @Autowired
    private EvaluationRepository evaluationRepository;
    
    @Autowired
    private StudentRepository studentRepository;
    
    @Autowired
    private AssignmentRepository assignmentRepository;
    
    private Student testStudent1;
    private Student testStudent2;
    private Assignment testAssignment1;
    private Assignment testAssignment2;
    private Evaluation testEvaluation1;
    private Evaluation testEvaluation2;
    private Evaluation testEvaluation3;
    
    @BeforeEach
    void setUpTestData() {
        // Clear existing data
        evaluationRepository.deleteAll();
        studentRepository.deleteAll();
        assignmentRepository.deleteAll();
        
        // Create test students
        testStudent1 = new Student("STU001", "John Doe", "john.doe@example.com");
        testStudent2 = new Student("STU002", "Jane Smith", "jane.smith@example.com");
        studentRepository.saveAll(List.of(testStudent1, testStudent2));
        
        // Create test assignments
        testAssignment1 = new Assignment("ASG001", "Basic Java", "Java basics", null);
        testAssignment2 = new Assignment("ASG002", "OOP Java", "OOP concepts", null);
        assignmentRepository.saveAll(List.of(testAssignment1, testAssignment2));
        
        // Create test evaluations
        testEvaluation1 = new Evaluation("EVAL001", "STU001", "ASG001", 
                new BigDecimal("85.50"), new BigDecimal("100.00"), EvaluationStatus.COMPLETED);
        testEvaluation2 = new Evaluation("EVAL002", "STU001", "ASG002", 
                new BigDecimal("92.00"), new BigDecimal("100.00"), EvaluationStatus.COMPLETED);
        testEvaluation3 = new Evaluation("EVAL003", "STU002", "ASG001", 
                new BigDecimal("78.25"), new BigDecimal("100.00"), EvaluationStatus.FAILED);
        
        evaluationRepository.saveAll(List.of(testEvaluation1, testEvaluation2, testEvaluation3));
    }
    
    @Test
    void testFindByStudentId() {
        List<Evaluation> student1Evaluations = evaluationRepository.findByStudentId("STU001");
        assertThat(student1Evaluations).hasSize(2);
        assertThat(student1Evaluations).extracting(Evaluation::getEvaluationId)
                .containsExactlyInAnyOrder("EVAL001", "EVAL002");
        
        List<Evaluation> student2Evaluations = evaluationRepository.findByStudentId("STU002");
        assertThat(student2Evaluations).hasSize(1);
        assertThat(student2Evaluations.get(0).getEvaluationId()).isEqualTo("EVAL003");
    }
    
    @Test
    void testFindByAssignmentId() {
        List<Evaluation> assignment1Evaluations = evaluationRepository.findByAssignmentId("ASG001");
        assertThat(assignment1Evaluations).hasSize(2);
        assertThat(assignment1Evaluations).extracting(Evaluation::getEvaluationId)
                .containsExactlyInAnyOrder("EVAL001", "EVAL003");
        
        List<Evaluation> assignment2Evaluations = evaluationRepository.findByAssignmentId("ASG002");
        assertThat(assignment2Evaluations).hasSize(1);
        assertThat(assignment2Evaluations.get(0).getEvaluationId()).isEqualTo("EVAL002");
    }
    
    @Test
    void testFindByStudentIdAndAssignmentId() {
        List<Evaluation> specificEvaluations = evaluationRepository
                .findByStudentIdAndAssignmentId("STU001", "ASG001");
        assertThat(specificEvaluations).hasSize(1);
        assertThat(specificEvaluations.get(0).getEvaluationId()).isEqualTo("EVAL001");
        
        List<Evaluation> noEvaluations = evaluationRepository
                .findByStudentIdAndAssignmentId("STU002", "ASG002");
        assertThat(noEvaluations).isEmpty();
    }
    
    @Test
    void testFindByStatus() {
        List<Evaluation> completedEvaluations = evaluationRepository.findByStatus(EvaluationStatus.COMPLETED);
        assertThat(completedEvaluations).hasSize(2);
        assertThat(completedEvaluations).extracting(Evaluation::getEvaluationId)
                .containsExactlyInAnyOrder("EVAL001", "EVAL002");
        
        List<Evaluation> failedEvaluations = evaluationRepository.findByStatus(EvaluationStatus.FAILED);
        assertThat(failedEvaluations).hasSize(1);
        assertThat(failedEvaluations.get(0).getEvaluationId()).isEqualTo("EVAL003");
        
        List<Evaluation> pendingEvaluations = evaluationRepository.findByStatus(EvaluationStatus.PENDING);
        assertThat(pendingEvaluations).isEmpty();
    }
    
    @Test
    void testFindByStudentIdAndStatus() {
        List<Evaluation> student1Completed = evaluationRepository
                .findByStudentIdAndStatus("STU001", EvaluationStatus.COMPLETED);
        assertThat(student1Completed).hasSize(2);
        
        List<Evaluation> student2Failed = evaluationRepository
                .findByStudentIdAndStatus("STU002", EvaluationStatus.FAILED);
        assertThat(student2Failed).hasSize(1);
        assertThat(student2Failed.get(0).getEvaluationId()).isEqualTo("EVAL003");
    }
    
    @Test
    void testFindByEvaluatedAtAfter() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(1);
        List<Evaluation> recentEvaluations = evaluationRepository.findByEvaluatedAtAfter(cutoffTime);
        assertThat(recentEvaluations).hasSize(3);
        
        LocalDateTime futureTime = LocalDateTime.now().plusHours(1);
        List<Evaluation> futureEvaluations = evaluationRepository.findByEvaluatedAtAfter(futureTime);
        assertThat(futureEvaluations).isEmpty();
    }
    
    @Test
    void testFindByEvaluatedAtBetween() {
        LocalDateTime start = LocalDateTime.now().minusHours(1);
        LocalDateTime end = LocalDateTime.now().plusHours(1);
        
        List<Evaluation> evaluationsInRange = evaluationRepository.findByEvaluatedAtBetween(start, end);
        assertThat(evaluationsInRange).hasSize(3);
    }
    
    @Test
    void testFindByScoreGreaterThanEqual() {
        List<Evaluation> highScores = evaluationRepository
                .findByScoreGreaterThanEqual(new BigDecimal("85.00"));
        assertThat(highScores).hasSize(2);
        assertThat(highScores).extracting(Evaluation::getEvaluationId)
                .containsExactlyInAnyOrder("EVAL001", "EVAL002");
        
        List<Evaluation> veryHighScores = evaluationRepository
                .findByScoreGreaterThanEqual(new BigDecimal("90.00"));
        assertThat(veryHighScores).hasSize(1);
        assertThat(veryHighScores.get(0).getEvaluationId()).isEqualTo("EVAL002");
    }
    
    @Test
    void testFindByScoreBetween() {
        List<Evaluation> midRangeScores = evaluationRepository
                .findByScoreBetween(new BigDecimal("80.00"), new BigDecimal("90.00"));
        assertThat(midRangeScores).hasSize(1);
        assertThat(midRangeScores.get(0).getEvaluationId()).isEqualTo("EVAL001");
    }
    
    @Test
    void testFindTopByStudentIdAndAssignmentIdOrderByEvaluatedAtDesc() {
        Optional<Evaluation> latestEvaluation = evaluationRepository
                .findTopByStudentIdAndAssignmentIdOrderByEvaluatedAtDesc("STU001", "ASG001");
        assertThat(latestEvaluation).isPresent();
        assertThat(latestEvaluation.get().getEvaluationId()).isEqualTo("EVAL001");
        
        Optional<Evaluation> noEvaluation = evaluationRepository
                .findTopByStudentIdAndAssignmentIdOrderByEvaluatedAtDesc("STU002", "ASG002");
        assertThat(noEvaluation).isEmpty();
    }
    
    @Test
    void testFindTopByStudentIdAndAssignmentIdOrderByScoreDesc() {
        Optional<Evaluation> highestScore = evaluationRepository
                .findTopByStudentIdAndAssignmentIdOrderByScoreDesc("STU001", "ASG001");
        assertThat(highestScore).isPresent();
        assertThat(highestScore.get().getEvaluationId()).isEqualTo("EVAL001");
    }
    
    @Test
    void testGetAverageScoreByStudent() {
        Optional<BigDecimal> avgScore = evaluationRepository.getAverageScoreByStudent("STU001");
        assertThat(avgScore).isPresent();
        // Average of 85.50 and 92.00 = 88.75
        assertThat(avgScore.get()).isEqualByComparingTo(new BigDecimal("88.75"));
        
        Optional<BigDecimal> noAvgScore = evaluationRepository.getAverageScoreByStudent("NONEXISTENT");
        assertThat(noAvgScore).isEmpty();
    }
    
    @Test
    void testGetAverageScoreByAssignment() {
        Optional<BigDecimal> avgScore = evaluationRepository.getAverageScoreByAssignment("ASG001");
        assertThat(avgScore).isPresent();
        // Only EVAL001 is completed for ASG001 (EVAL003 is FAILED)
        assertThat(avgScore.get()).isEqualByComparingTo(new BigDecimal("85.50"));
    }
    
    @Test
    void testGetTotalScoreByStudent() {
        Optional<BigDecimal> totalScore = evaluationRepository.getTotalScoreByStudent("STU001");
        assertThat(totalScore).isPresent();
        // Sum of 85.50 and 92.00 = 177.50
        assertThat(totalScore.get()).isEqualByComparingTo(new BigDecimal("177.50"));
    }
    
    @Test
    void testGetHighestScoreByStudent() {
        Optional<BigDecimal> highestScore = evaluationRepository.getHighestScoreByStudent("STU001");
        assertThat(highestScore).isPresent();
        assertThat(highestScore.get()).isEqualByComparingTo(new BigDecimal("92.00"));
    }
    
    @Test
    void testCountByStatus() {
        long completedCount = evaluationRepository.countByStatus(EvaluationStatus.COMPLETED);
        assertThat(completedCount).isEqualTo(2);
        
        long failedCount = evaluationRepository.countByStatus(EvaluationStatus.FAILED);
        assertThat(failedCount).isEqualTo(1);
        
        long pendingCount = evaluationRepository.countByStatus(EvaluationStatus.PENDING);
        assertThat(pendingCount).isEqualTo(0);
    }
    
    @Test
    void testCountByStudentId() {
        long student1Count = evaluationRepository.countByStudentId("STU001");
        assertThat(student1Count).isEqualTo(2);
        
        long student2Count = evaluationRepository.countByStudentId("STU002");
        assertThat(student2Count).isEqualTo(1);
    }
    
    @Test
    void testCountByAssignmentId() {
        long assignment1Count = evaluationRepository.countByAssignmentId("ASG001");
        assertThat(assignment1Count).isEqualTo(2);
        
        long assignment2Count = evaluationRepository.countByAssignmentId("ASG002");
        assertThat(assignment2Count).isEqualTo(1);
    }
    
    @Test
    void testFindEvaluationsAbovePercentageThreshold() {
        List<Evaluation> above80Percent = evaluationRepository
                .findEvaluationsAbovePercentageThreshold(80.0);
        assertThat(above80Percent).hasSize(2); // EVAL001 (85.5%) and EVAL002 (92%)
        
        List<Evaluation> above90Percent = evaluationRepository
                .findEvaluationsAbovePercentageThreshold(90.0);
        assertThat(above90Percent).hasSize(1); // Only EVAL002 (92%)
    }
    
    @Test
    void testFindRecentEvaluationsByStudent() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusMinutes(1);
        List<Evaluation> recentEvaluations = evaluationRepository
                .findRecentEvaluationsByStudent("STU001", cutoffDate);
        assertThat(recentEvaluations).hasSize(2);
        
        LocalDateTime futureDate = LocalDateTime.now().plusHours(1);
        List<Evaluation> noRecentEvaluations = evaluationRepository
                .findRecentEvaluationsByStudent("STU001", futureDate);
        assertThat(noRecentEvaluations).isEmpty();
    }
    
    @Test
    void testGetEvaluationStatsByStudent() {
        Object[] stats = evaluationRepository.getEvaluationStatsByStudent("STU001");
        assertThat(stats).hasSize(4);
        assertThat(stats[0]).isEqualTo(2L); // count
        assertThat(stats[1]).isEqualTo(new BigDecimal("88.75")); // average
        assertThat(stats[2]).isEqualTo(new BigDecimal("85.50")); // min
        assertThat(stats[3]).isEqualTo(new BigDecimal("92.00")); // max
    }
    
    @Test
    void testBasicCrudOperations() {
        // Test save
        Evaluation newEvaluation = new Evaluation("EVAL004", "STU002", "ASG002", 
                new BigDecimal("88.00"), new BigDecimal("100.00"), EvaluationStatus.COMPLETED);
        Evaluation saved = evaluationRepository.save(newEvaluation);
        assertThat(saved.getEvaluationId()).isEqualTo("EVAL004");
        assertThat(saved.getEvaluatedAt()).isNotNull();
        
        // Test findById
        Optional<Evaluation> found = evaluationRepository.findById("EVAL004");
        assertThat(found).isPresent();
        assertThat(found.get().getScore()).isEqualByComparingTo(new BigDecimal("88.00"));
        
        // Test update
        found.get().setScore(new BigDecimal("90.00"));
        Evaluation updated = evaluationRepository.save(found.get());
        assertThat(updated.getScore()).isEqualByComparingTo(new BigDecimal("90.00"));
        
        // Test delete
        evaluationRepository.deleteById("EVAL004");
        Optional<Evaluation> deleted = evaluationRepository.findById("EVAL004");
        assertThat(deleted).isEmpty();
    }
}