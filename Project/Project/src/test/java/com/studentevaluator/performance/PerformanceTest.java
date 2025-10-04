package com.studentevaluator.performance;

import com.studentevaluator.model.Assignment;
import com.studentevaluator.model.EvaluationResult;
import com.studentevaluator.model.EvaluationStatus;
import com.studentevaluator.service.AssignmentService;
import com.studentevaluator.service.EvaluationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.StopWatch;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Performance tests to validate system meets response time requirements
 */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "evaluation.timeout=30000",
    "evaluation.max-concurrent-evaluations=10"
})
class PerformanceTest {

    @Autowired
    private EvaluationService evaluationService;

    @Autowired
    private AssignmentService assignmentService;

    private String tempDirectory;
    private Assignment testAssignment;

    @BeforeEach
    void setUp() throws IOException {
        tempDirectory = System.getProperty("java.io.tmpdir") + "/performance-test";
        Path tempPath = Paths.get(tempDirectory);
        if (!Files.exists(tempPath)) {
            Files.createDirectories(tempPath);
        }

        // Create a test assignment
        createTestAssignment();
    }

    @Test
    @DisplayName("Single evaluation should complete within 30 seconds")
    void testSingleEvaluationPerformance() throws IOException {
        // Given
        File submissionFile = createTestSubmissionFile();
        String studentId = "perf-test-student-1";
        
        // When
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        
        EvaluationResult result = evaluationService.evaluateAssignment(
            studentId, testAssignment.getAssignmentId(), submissionFile, tempDirectory);
        
        stopWatch.stop();
        
        // Then
        assertNotNull(result);
        long executionTimeMs = stopWatch.getTotalTimeMillis();
        
        System.out.printf("Single evaluation completed in %d ms%n", executionTimeMs);
        
        // Verify performance requirement: should complete within 30 seconds
        assertTrue(executionTimeMs < 30000, 
            String.format("Evaluation took %d ms, exceeding 30 second limit", executionTimeMs));
        
        // Verify result is valid
        assertNotNull(result.getEvaluationId());
        assertTrue(result.getStatus() == EvaluationStatus.COMPLETED || 
                  result.getStatus() == EvaluationStatus.FAILED);
    }

    @Test
    @DisplayName("Concurrent evaluations should maintain performance")
    void testConcurrentEvaluationPerformance() throws Exception {
        // Given
        int numberOfConcurrentEvaluations = 5;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfConcurrentEvaluations);
        List<CompletableFuture<Long>> futures = new ArrayList<>();
        
        // When
        StopWatch totalStopWatch = new StopWatch();
        totalStopWatch.start();
        
        for (int i = 0; i < numberOfConcurrentEvaluations; i++) {
            final int index = i;
            CompletableFuture<Long> future = CompletableFuture.supplyAsync(() -> {
                try {
                    File submissionFile = createTestSubmissionFile();
                    String studentId = "perf-test-student-" + index;
                    
                    StopWatch individualStopWatch = new StopWatch();
                    individualStopWatch.start();
                    
                    EvaluationResult result = evaluationService.evaluateAssignment(
                        studentId, testAssignment.getAssignmentId(), submissionFile, tempDirectory);
                    
                    individualStopWatch.stop();
                    
                    assertNotNull(result);
                    return individualStopWatch.getTotalTimeMillis();
                    
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }, executor);
            
            futures.add(future);
        }
        
        // Wait for all evaluations to complete
        List<Long> executionTimes = new ArrayList<>();
        for (CompletableFuture<Long> future : futures) {
            executionTimes.add(future.get(60, TimeUnit.SECONDS));
        }
        
        totalStopWatch.stop();
        executor.shutdown();
        
        // Then
        long totalTime = totalStopWatch.getTotalTimeMillis();
        double averageTime = executionTimes.stream().mapToLong(Long::longValue).average().orElse(0);
        long maxTime = executionTimes.stream().mapToLong(Long::longValue).max().orElse(0);
        
        System.out.printf("Concurrent evaluations: %d completed in %d ms total%n", 
                         numberOfConcurrentEvaluations, totalTime);
        System.out.printf("Average individual time: %.2f ms%n", averageTime);
        System.out.printf("Maximum individual time: %d ms%n", maxTime);
        
        // Verify performance requirements
        assertTrue(maxTime < 45000, 
            String.format("Slowest evaluation took %d ms, exceeding 45 second limit for concurrent operations", maxTime));
        
        assertTrue(averageTime < 35000, 
            String.format("Average evaluation time %.2f ms exceeds 35 second limit", averageTime));
    }

    @Test
    @DisplayName("API response time should be under 5 seconds for evaluation status")
    void testApiResponseTime() {
        // Given
        String evaluationId = "test-evaluation-id";
        
        // When
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        
        // This tests the retrieval performance, not the actual evaluation
        var result = evaluationService.getEvaluationResult(evaluationId);
        
        stopWatch.stop();
        
        // Then
        long responseTime = stopWatch.getTotalTimeMillis();
        System.out.printf("API response time: %d ms%n", responseTime);
        
        // Verify API response time requirement: should respond within 5 seconds
        assertTrue(responseTime < 5000, 
            String.format("API response took %d ms, exceeding 5 second limit", responseTime));
    }

    @Test
    @DisplayName("Memory usage should remain stable during evaluations")
    void testMemoryUsage() throws IOException {
        // Given
        Runtime runtime = Runtime.getRuntime();
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();
        
        // When - perform multiple evaluations
        for (int i = 0; i < 3; i++) {
            File submissionFile = createTestSubmissionFile();
            String studentId = "memory-test-student-" + i;
            
            EvaluationResult result = evaluationService.evaluateAssignment(
                studentId, testAssignment.getAssignmentId(), submissionFile, tempDirectory);
            
            assertNotNull(result);
            
            // Force garbage collection
            System.gc();
            Thread.yield();
        }
        
        // Then
        long finalMemory = runtime.totalMemory() - runtime.freeMemory();
        long memoryIncrease = finalMemory - initialMemory;
        
        System.out.printf("Initial memory: %d bytes%n", initialMemory);
        System.out.printf("Final memory: %d bytes%n", finalMemory);
        System.out.printf("Memory increase: %d bytes%n", memoryIncrease);
        
        // Verify memory usage doesn't grow excessively (allow up to 50MB increase)
        assertTrue(memoryIncrease < 50 * 1024 * 1024, 
            String.format("Memory increased by %d bytes, exceeding 50MB limit", memoryIncrease));
    }

    private void createTestAssignment() throws IOException {
        String javaCode = """
            public class Calculator {
                public int add(int a, int b) {
                    return a + b;
                }
                
                public int subtract(int a, int b) {
                    return a - b;
                }
            }
            """;

        String testCode = """
            import org.junit.jupiter.api.Test;
            import static org.junit.jupiter.api.Assertions.*;
            
            public class CalculatorTest {
                @Test
                public void testAdd() {
                    Calculator calc = new Calculator();
                    assertEquals(5, calc.add(2, 3));
                }
                
                @Test
                public void testSubtract() {
                    Calculator calc = new Calculator();
                    assertEquals(1, calc.subtract(3, 2));
                }
            }
            """;

        MockMultipartFile assignmentFile = new MockMultipartFile(
            "file", "Calculator.java", "text/plain", javaCode.getBytes());
        
        MockMultipartFile testFile = new MockMultipartFile(
            "testFile", "CalculatorTest.java", "text/plain", testCode.getBytes());

        testAssignment = assignmentService.uploadAssignmentFile(
            assignmentFile, "perf-test-assignment", "Performance Test Assignment", 
            "Assignment for performance testing");
        
        assignmentService.uploadTestFile(testFile, testAssignment.getAssignmentId());
    }

    private File createTestSubmissionFile() throws IOException {
        String submissionCode = """
            public class Calculator {
                public int add(int a, int b) {
                    return a + b;
                }
                
                public int subtract(int a, int b) {
                    return a - b;
                }
            }
            """;

        Path submissionPath = Paths.get(tempDirectory, "Calculator.java");
        Files.write(submissionPath, submissionCode.getBytes());
        return submissionPath.toFile();
    }
}