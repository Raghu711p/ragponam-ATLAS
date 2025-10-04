package com.studentevaluator.service;

import java.io.File;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.studentevaluator.engine.JUnitTestRunner;
import com.studentevaluator.engine.JavaCompiler;
import com.studentevaluator.exception.CompilationException;
import com.studentevaluator.exception.StorageException;
import com.studentevaluator.exception.TestExecutionException;
import com.studentevaluator.model.Assignment;
import com.studentevaluator.model.CompilationResult;
import com.studentevaluator.model.Evaluation;
import com.studentevaluator.model.EvaluationResult;
import com.studentevaluator.model.EvaluationStatus;
import com.studentevaluator.model.TestExecutionResult;
import com.studentevaluator.repository.AssignmentRepository;
import com.studentevaluator.repository.DynamoDBRepository;
import com.studentevaluator.repository.EvaluationRepository;
import com.studentevaluator.repository.StudentRepository;

/**
 * Service that orchestrates the evaluation process by coordinating compilation and testing workflow.
 * Handles async evaluation processing, result calculation, and error handling.
 */
@Service
public class EvaluationService {
    
    private static final Logger logger = LoggerFactory.getLogger(EvaluationService.class);
    
    private final JavaCompiler javaCompiler;
    private final JUnitTestRunner testRunner;
    private final EvaluationRepository evaluationRepository;
    private final AssignmentRepository assignmentRepository;
    private final StudentRepository studentRepository;
    private final DynamoDBRepository dynamoDBRepository;
    private final ScoreCache scoreCache;
    private final ErrorLogService errorLogService;
    private final com.studentevaluator.monitoring.PerformanceMonitor performanceMonitor;
    
    @Autowired
    public EvaluationService(JavaCompiler javaCompiler,
                           JUnitTestRunner testRunner,
                           EvaluationRepository evaluationRepository,
                           AssignmentRepository assignmentRepository,
                           StudentRepository studentRepository,
                           DynamoDBRepository dynamoDBRepository,
                           ScoreCache scoreCache,
                           ErrorLogService errorLogService,
                           com.studentevaluator.monitoring.PerformanceMonitor performanceMonitor) {
        this.javaCompiler = javaCompiler;
        this.testRunner = testRunner;
        this.evaluationRepository = evaluationRepository;
        this.assignmentRepository = assignmentRepository;
        this.studentRepository = studentRepository;
        this.dynamoDBRepository = dynamoDBRepository;
        this.scoreCache = scoreCache;
        this.errorLogService = errorLogService;
        this.performanceMonitor = performanceMonitor;
    }
    
    /**
     * Synchronously evaluates a student's assignment submission.
     * 
     * @param studentId the student identifier
     * @param assignmentId the assignment identifier
     * @param submissionFile the Java file submitted by the student
     * @param outputDirectory the directory where compiled classes should be placed
     * @return EvaluationResult containing the complete evaluation outcome
     */
    public EvaluationResult evaluateAssignment(String studentId, String assignmentId, 
                                             File submissionFile, String outputDirectory) {
        String evaluationId = generateEvaluationId();
        
        // Set up MDC for structured logging
        MDC.put("evaluationId", evaluationId);
        MDC.put("studentId", studentId);
        MDC.put("assignmentId", assignmentId);
        
        logger.info("Starting evaluation process - evaluationId: {}, studentId: {}, assignmentId: {}", 
                   evaluationId, studentId, assignmentId);
        
        try {
            // Validate inputs
            if (!validateInputs(studentId, assignmentId, submissionFile)) {
                logger.warn("Input validation failed for evaluation {}", evaluationId);
                errorLogService.logWarning("INPUT_VALIDATION_FAILED", 
                    "Invalid input parameters for evaluation", 
                    Map.of("evaluationId", evaluationId, "studentId", studentId, "assignmentId", assignmentId));
                return createFailedResult(evaluationId, "Invalid input parameters");
            }
            
            // Get assignment details
            Optional<Assignment> assignmentOpt = assignmentRepository.findById(assignmentId);
            if (!assignmentOpt.isPresent()) {
                logger.warn("Assignment not found: {}", assignmentId);
                errorLogService.logWarning("ASSIGNMENT_NOT_FOUND", 
                    "Assignment not found during evaluation", 
                    Map.of("assignmentId", assignmentId, "evaluationId", evaluationId));
                return createFailedResult(evaluationId, "Assignment not found: " + assignmentId);
            }
            Assignment assignment = assignmentOpt.get();
            logger.debug("Retrieved assignment details for: {}", assignmentId);
            
            // Create evaluation record
            logger.debug("Creating evaluation record for: {}", evaluationId);
            Evaluation evaluation = createEvaluationRecord(evaluationId, studentId, assignmentId);
            evaluationRepository.save(evaluation);
            
            // Execute evaluation workflow with performance monitoring
            logger.info("Executing evaluation workflow for: {}", evaluationId);
            Instant startTime = Instant.now();
            EvaluationResult result = executeEvaluationWorkflow(evaluationId, assignment, 
                                                              submissionFile, outputDirectory, 
                                                              studentId, assignmentId);
            Duration evaluationDuration = Duration.between(startTime, Instant.now());
            
            // Record performance metrics
            boolean success = result.getStatus() == EvaluationStatus.COMPLETED;
            performanceMonitor.recordEvaluationTime(evaluationDuration, success);
            
            // Update evaluation record with results
            logger.debug("Updating evaluation record with results for: {}", evaluationId);
            updateEvaluationRecord(evaluation, result);
            evaluationRepository.save(evaluation);
            
            // Evict caches for updated data
            evictStudentCaches(studentId);
            
            // Update score cache
            if (result.isSuccessful() && result.getFinalScore() != null) {
                logger.debug("Updating score cache for student: {} with score: {}", studentId, result.getFinalScore());
                scoreCache.updateScore(studentId, result.getFinalScore());
            }
            
            logger.info("Completed evaluation successfully - evaluationId: {}, status: {}, score: {}", 
                       evaluationId, result.getStatus(), result.getFinalScore());
            
            errorLogService.logInfo("EVALUATION_COMPLETED", 
                "Evaluation completed successfully", 
                Map.of("evaluationId", evaluationId, "status", result.getStatus().toString(), 
                       "score", result.getFinalScore() != null ? result.getFinalScore().toString() : "null"));
            
            return result;
            
        } catch (CompilationException e) {
            logger.error("Compilation failed for evaluation: {}", evaluationId, e);
            errorLogService.logError("COMPILATION_FAILED", e.getMessage(), e.getDetails(), e);
            return createFailedResult(evaluationId, "Compilation failed: " + e.getMessage());
            
        } catch (TestExecutionException e) {
            logger.error("Test execution failed for evaluation: {}", evaluationId, e);
            errorLogService.logError("TEST_EXECUTION_FAILED", e.getMessage(), e.getDetails(), e);
            return createFailedResult(evaluationId, "Test execution failed: " + e.getMessage());
            
        } catch (StorageException e) {
            logger.error("Storage error during evaluation: {}", evaluationId, e);
            errorLogService.logError("STORAGE_ERROR", e.getMessage(), e.getDetails(), e);
            return createFailedResult(evaluationId, "Storage error: " + e.getMessage());
            
        } catch (Exception e) {
            logger.error("Unexpected error during evaluation: {}", evaluationId, e);
            errorLogService.logError("EVALUATION_UNEXPECTED_ERROR", e.getMessage(), null, e);
            dynamoDBRepository.storeErrorLog(evaluationId, 
                "Evaluation failed: " + e.getMessage() + "\n" + getStackTrace(e), 
                studentId, assignmentId);
            
            return createFailedResult(evaluationId, "Evaluation failed: " + e.getMessage());
        } finally {
            // Clear MDC to prevent memory leaks
            MDC.clear();
        }
    }
    
    /**
     * Asynchronously evaluates a student's assignment submission.
     * 
     * @param studentId the student identifier
     * @param assignmentId the assignment identifier
     * @param submissionFile the Java file submitted by the student
     * @param outputDirectory the directory where compiled classes should be placed
     * @return CompletableFuture containing the evaluation result
     */
    @Async("evaluationTaskExecutor")
    public CompletableFuture<EvaluationResult> evaluateAssignmentAsync(String studentId, String assignmentId, 
                                                                      File submissionFile, String outputDirectory) {
        logger.info("Starting async evaluation for student {} assignment {}", studentId, assignmentId);
        
        try {
            EvaluationResult result = evaluateAssignment(studentId, assignmentId, submissionFile, outputDirectory);
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            logger.error("Async evaluation failed for student {} assignment {}", studentId, assignmentId, e);
            
            String evaluationId = generateEvaluationId();
            EvaluationResult failedResult = createFailedResult(evaluationId, "Async evaluation failed: " + e.getMessage());
            return CompletableFuture.completedFuture(failedResult);
        }
    }    

    /**
     * Executes the core evaluation workflow: compilation -> testing -> result calculation.
     */
    private EvaluationResult executeEvaluationWorkflow(String evaluationId, Assignment assignment,
                                                     File submissionFile, String outputDirectory,
                                                     String studentId, String assignmentId) {
        
        // Step 1: Compile the Java file
        logger.debug("Starting compilation for evaluation {}", evaluationId);
        CompilationResult compilationResult = javaCompiler.compile(submissionFile, outputDirectory);
        
        // Store compilation log
        dynamoDBRepository.storeCompilationLog(evaluationId, compilationResult.getOutput(), 
                                             studentId, assignmentId);
        
        // Check if compilation failed
        if (!compilationResult.isSuccessful()) {
            logger.warn("Compilation failed for evaluation {}", evaluationId);
            return new EvaluationResult(evaluationId, compilationResult, null, 
                                      BigDecimal.ZERO, BigDecimal.ZERO, EvaluationStatus.FAILED);
        }
        
        // Step 2: Run tests if compilation was successful
        logger.debug("Starting test execution for evaluation {}", evaluationId);
        TestExecutionResult testResult = null;
        
        try {
            List<File> testFiles = getTestFilesForAssignment(assignment);
            if (testFiles.isEmpty()) {
                logger.warn("No test files found for assignment {}", assignmentId);
                return createFailedResult(evaluationId, "No test files available for assignment");
            }
            
            testResult = testRunner.runTests(compilationResult.getCompiledClassPath(), testFiles);
            
            // Store test execution log
            dynamoDBRepository.storeTestExecutionLog(evaluationId, testResult.getExecutionLog(), 
                                                   studentId, assignmentId);
            
        } catch (Exception e) {
            logger.error("Test execution failed for evaluation {}", evaluationId, e);
            dynamoDBRepository.storeErrorLog(evaluationId, 
                "Test execution failed: " + e.getMessage() + "\n" + getStackTrace(e), 
                studentId, assignmentId);
            
            return new EvaluationResult(evaluationId, compilationResult, null, 
                                      BigDecimal.ZERO, BigDecimal.ZERO, EvaluationStatus.FAILED);
        }
        
        // Step 3: Calculate final score
        logger.debug("Calculating final score for evaluation {}", evaluationId);
        BigDecimal finalScore = calculateFinalScore(testResult);
        BigDecimal maxScore = calculateMaxScore(testResult);
        
        // Create successful result
        EvaluationResult result = new EvaluationResult(evaluationId, compilationResult, testResult, 
                                                      finalScore, maxScore, EvaluationStatus.COMPLETED);
        
        logger.info("Evaluation {} completed successfully. Score: {}/{}", 
                   evaluationId, finalScore, maxScore);
        
        return result;
    }
    
    /**
     * Calculates the final score based on test execution results.
     */
    private BigDecimal calculateFinalScore(TestExecutionResult testResult) {
        if (testResult == null || testResult.getTotalTests() == 0) {
            return BigDecimal.ZERO;
        }
        
        // Simple scoring: 1 point per passed test
        return BigDecimal.valueOf(testResult.getPassedTests());
    }
    
    /**
     * Calculates the maximum possible score based on test execution results.
     */
    private BigDecimal calculateMaxScore(TestExecutionResult testResult) {
        if (testResult == null) {
            return BigDecimal.ZERO;
        }
        
        return BigDecimal.valueOf(testResult.getTotalTests());
    }
    
    /**
     * Validates input parameters for evaluation.
     */
    private boolean validateInputs(String studentId, String assignmentId, File submissionFile) {
        if (studentId == null || studentId.trim().isEmpty()) {
            logger.warn("Invalid studentId: {}", studentId);
            return false;
        }
        
        if (assignmentId == null || assignmentId.trim().isEmpty()) {
            logger.warn("Invalid assignmentId: {}", assignmentId);
            return false;
        }
        
        if (submissionFile == null || !submissionFile.exists() || !submissionFile.canRead()) {
            logger.warn("Invalid submission file: {}", submissionFile);
            return false;
        }
        
        if (!submissionFile.getName().toLowerCase().endsWith(".java")) {
            logger.warn("Submission file is not a Java file: {}", submissionFile.getName());
            return false;
        }
        
        return true;
    }
    
    /**
     * Creates an evaluation record in the database.
     */
    private Evaluation createEvaluationRecord(String evaluationId, String studentId, String assignmentId) {
        Evaluation evaluation = new Evaluation();
        evaluation.setEvaluationId(evaluationId);
        evaluation.setStudentId(studentId);
        evaluation.setAssignmentId(assignmentId);
        evaluation.setStatus(EvaluationStatus.IN_PROGRESS);
        evaluation.setEvaluatedAt(LocalDateTime.now());
        evaluation.setScore(BigDecimal.ZERO);
        evaluation.setMaxScore(BigDecimal.ZERO);
        
        return evaluation;
    }
    
    /**
     * Updates an evaluation record with the results.
     */
    private void updateEvaluationRecord(Evaluation evaluation, EvaluationResult result) {
        evaluation.setStatus(result.getStatus());
        evaluation.setScore(result.getFinalScore() != null ? result.getFinalScore() : BigDecimal.ZERO);
        evaluation.setMaxScore(result.getMaxScore() != null ? result.getMaxScore() : BigDecimal.ZERO);
        evaluation.setEvaluatedAt(LocalDateTime.now());
    }
    
    /**
     * Gets test files associated with an assignment.
     */
    private List<File> getTestFilesForAssignment(Assignment assignment) {
        // This is a simplified implementation
        // In a real system, you'd have a more sophisticated way to manage test files
        String testFilePath = assignment.getTestFilePath();
        if (testFilePath == null || testFilePath.trim().isEmpty()) {
            return List.of();
        }
        
        File testFile = new File(testFilePath);
        if (testFile.exists() && testFile.canRead()) {
            return List.of(testFile);
        }
        
        return List.of();
    }
    
    /**
     * Creates a failed evaluation result.
     */
    private EvaluationResult createFailedResult(String evaluationId, String errorMessage) {
        return new EvaluationResult(evaluationId, EvaluationStatus.FAILED, errorMessage);
    }
    
    /**
     * Generates a unique evaluation ID.
     */
    private String generateEvaluationId() {
        return "eval_" + UUID.randomUUID().toString().replace("-", "");
    }
    
    /**
     * Converts exception stack trace to string.
     */
    private String getStackTrace(Exception e) {
        java.io.StringWriter sw = new java.io.StringWriter();
        java.io.PrintWriter pw = new java.io.PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }
    
    /**
     * Retrieves evaluation result by ID.
     */
    @Cacheable(value = "evaluationResults", key = "#evaluationId")
    public Optional<EvaluationResult> getEvaluationResult(String evaluationId) {
        try {
            Optional<Evaluation> evaluationOpt = evaluationRepository.findById(evaluationId);
            if (!evaluationOpt.isPresent()) {
                return Optional.empty();
            }
            
            Evaluation evaluation = evaluationOpt.get();
            
            // Create basic result from database record
            EvaluationResult result = new EvaluationResult();
            result.setEvaluationId(evaluation.getEvaluationId());
            result.setStatus(evaluation.getStatus());
            result.setFinalScore(evaluation.getScore());
            result.setMaxScore(evaluation.getMaxScore());
            result.setEvaluationTime(evaluation.getEvaluatedAt());
            
            return Optional.of(result);
            
        } catch (Exception e) {
            logger.error("Failed to retrieve evaluation result for ID {}", evaluationId, e);
            return Optional.empty();
        }
    }
    
    /**
     * Retrieves evaluation history for a student.
     */
    @Cacheable(value = "studentEvaluations", key = "#studentId")
    public List<Evaluation> getStudentEvaluationHistory(String studentId) {
        try {
            return evaluationRepository.findByStudentId(studentId);
        } catch (Exception e) {
            logger.error("Failed to retrieve evaluation history for student {}", studentId, e);
            return List.of();
        }
    }
    
    /**
     * Retrieves evaluation statistics for a student.
     */
    @Cacheable(value = "studentStats", key = "#studentId")
    public Optional<StudentEvaluationStats> getStudentEvaluationStats(String studentId) {
        try {
            Object[] stats = evaluationRepository.getEvaluationStatsByStudent(studentId);
            if (stats == null || stats.length < 4) {
                return Optional.empty();
            }
            
            Long count = (Long) stats[0];
            BigDecimal average = (BigDecimal) stats[1];
            BigDecimal min = (BigDecimal) stats[2];
            BigDecimal max = (BigDecimal) stats[3];
            
            StudentEvaluationStats evaluationStats = new StudentEvaluationStats(
                studentId, count != null ? count.intValue() : 0, average, min, max);
            
            return Optional.of(evaluationStats);
            
        } catch (Exception e) {
            logger.error("Failed to retrieve evaluation stats for student {}", studentId, e);
            return Optional.empty();
        }
    }
    
    /**
     * Evicts caches related to a student when their data is updated.
     */
    @CacheEvict(value = {"studentEvaluations", "studentStats"}, key = "#studentId")
    public void evictStudentCaches(String studentId) {
        logger.debug("Evicting caches for student: {}", studentId);
    }

    /**
     * Handles recovery from failed evaluations by cleaning up resources and updating status.
     */
    public void handleEvaluationFailure(String evaluationId, String errorMessage) {
        try {
            logger.info("Handling evaluation failure for {}: {}", evaluationId, errorMessage);
            
            // Update evaluation status in database
            Optional<Evaluation> evaluationOpt = evaluationRepository.findById(evaluationId);
            if (evaluationOpt.isPresent()) {
                Evaluation evaluation = evaluationOpt.get();
                evaluation.setStatus(EvaluationStatus.FAILED);
                evaluation.setEvaluatedAt(LocalDateTime.now());
                evaluationRepository.save(evaluation);
            }
            
            // Store error log in DynamoDB
            dynamoDBRepository.storeErrorLog(evaluationId, errorMessage, null, null);
            
            logger.info("Evaluation failure handled for {}", evaluationId);
            
        } catch (Exception e) {
            logger.error("Failed to handle evaluation failure for {}", evaluationId, e);
        }
    }
    
    /**
     * Inner class for student evaluation statistics.
     */
    public static class StudentEvaluationStats {
        private final String studentId;
        private final int totalEvaluations;
        private final BigDecimal averageScore;
        private final BigDecimal minScore;
        private final BigDecimal maxScore;
        
        public StudentEvaluationStats(String studentId, int totalEvaluations, 
                                    BigDecimal averageScore, BigDecimal minScore, BigDecimal maxScore) {
            this.studentId = studentId;
            this.totalEvaluations = totalEvaluations;
            this.averageScore = averageScore;
            this.minScore = minScore;
            this.maxScore = maxScore;
        }
        
        // Getters
        public String getStudentId() { return studentId; }
        public int getTotalEvaluations() { return totalEvaluations; }
        public BigDecimal getAverageScore() { return averageScore; }
        public BigDecimal getMinScore() { return minScore; }
        public BigDecimal getMaxScore() { return maxScore; }
        
        @Override
        public String toString() {
            return String.format("StudentEvaluationStats{studentId='%s', total=%d, avg=%.2f, min=%.2f, max=%.2f}",
                               studentId, totalEvaluations, 
                               averageScore != null ? averageScore.doubleValue() : 0.0,
                               minScore != null ? minScore.doubleValue() : 0.0,
                               maxScore != null ? maxScore.doubleValue() : 0.0);
        }
    }
}