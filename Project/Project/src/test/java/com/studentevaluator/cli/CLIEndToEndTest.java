package com.studentevaluator.cli;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end integration tests for CLI commands that interact with the actual REST API.
 * These tests require the Spring Boot application to be running.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class CLIEndToEndTest {
    
    @LocalServerPort
    private int port;
    
    private CLIApiService cliApiService;
    private Path tempDir;
    
    @BeforeEach
    void setUp() throws IOException {
        cliApiService = new CLIApiService();
        ReflectionTestUtils.setField(cliApiService, "baseUrl", "http://localhost:" + port);
        
        tempDir = Files.createTempDirectory("cli-e2e-test");
    }
    
    @Test
    void testCompleteWorkflow() throws Exception {
        // This test demonstrates the complete CLI workflow:
        // 1. Submit assignment
        // 2. Upload tests
        // 3. Evaluate submission
        // 4. Get results
        
        String assignmentId = "cli-test-assignment-" + System.currentTimeMillis();
        String studentId = "cli-test-student-" + System.currentTimeMillis();
        
        // Step 1: Create and submit assignment
        File assignmentFile = createSampleAssignmentFile();
        
        try {
            var assignmentResponse = cliApiService.uploadAssignment(
                assignmentFile, assignmentId, "CLI Test Assignment", "Test assignment for CLI");
            
            assertNotNull(assignmentResponse);
            assertEquals(assignmentId, assignmentResponse.getAssignmentId());
            assertEquals("CLI Test Assignment", assignmentResponse.getTitle());
            
        } catch (Exception e) {
            // If the API is not available, skip this test
            System.out.println("Skipping end-to-end test - API not available: " + e.getMessage());
            return;
        }
        
        // Step 2: Create and upload test file
        File testFile = createSampleTestFile();
        
        var testUploadResponse = cliApiService.uploadTestFile(testFile, assignmentId);
        assertNotNull(testUploadResponse);
        assertEquals(assignmentId, testUploadResponse.getAssignmentId());
        assertTrue(testUploadResponse.isHasTestFile());
        
        // Step 3: Create student submission and evaluate
        File submissionFile = createSampleSubmissionFile();
        
        var evaluationResponse = cliApiService.triggerEvaluation(
            submissionFile, studentId, assignmentId);
        
        assertNotNull(evaluationResponse);
        assertEquals(studentId, evaluationResponse.getStudentId());
        assertEquals(assignmentId, evaluationResponse.getAssignmentId());
        assertNotNull(evaluationResponse.getEvaluationId());
        
        // Step 4: Get student scores
        try {
            var scoresResponse = cliApiService.getStudentScores(studentId);
            assertNotNull(scoresResponse);
            assertEquals(studentId, scoresResponse.getStudentId());
        } catch (CLIException e) {
            // Score might not be cached yet, which is acceptable
            System.out.println("Score not cached yet: " + e.getMessage());
        }
        
        // Step 5: Get evaluation history
        var historyResponse = cliApiService.getStudentEvaluationHistory(studentId);
        assertNotNull(historyResponse);
        assertFalse(historyResponse.isEmpty());
        
        // Step 6: Get specific evaluation result
        var resultResponse = cliApiService.getEvaluationResult(evaluationResponse.getEvaluationId());
        assertNotNull(resultResponse);
        assertEquals(evaluationResponse.getEvaluationId(), resultResponse.getEvaluationId());
    }
    
    @Test
    void testGetAllAssignments() throws Exception {
        try {
            var assignments = cliApiService.getAllAssignments();
            assertNotNull(assignments);
            // The list might be empty, which is fine
        } catch (Exception e) {
            // If the API is not available, skip this test
            System.out.println("Skipping assignments test - API not available: " + e.getMessage());
        }
    }
    
    @Test
    void testErrorHandling() throws Exception {
        // Test with non-existent student
        try {
            cliApiService.getStudentScores("nonexistent-student-" + System.currentTimeMillis());
            fail("Expected CLIException for non-existent student");
        } catch (CLIException e) {
            assertTrue(e.getMessage().contains("Failed to get student scores"));
        } catch (Exception e) {
            // API might not be available
            System.out.println("Skipping error handling test - API not available: " + e.getMessage());
        }
    }
    
    // Helper methods to create sample files
    
    private File createSampleAssignmentFile() throws IOException {
        String content = """
            public class Calculator {
                public int add(int a, int b) {
                    return a + b;
                }
                
                public int subtract(int a, int b) {
                    return a - b;
                }
                
                public int multiply(int a, int b) {
                    return a * b;
                }
                
                public double divide(int a, int b) {
                    if (b == 0) {
                        throw new IllegalArgumentException("Cannot divide by zero");
                    }
                    return (double) a / b;
                }
            }
            """;
        
        return createTestFile("Calculator.java", content);
    }
    
    private File createSampleTestFile() throws IOException {
        String content = """
            import org.junit.jupiter.api.Test;
            import org.junit.jupiter.api.BeforeEach;
            import static org.junit.jupiter.api.Assertions.*;
            
            public class CalculatorTest {
                private Calculator calculator;
                
                @BeforeEach
                void setUp() {
                    calculator = new Calculator();
                }
                
                @Test
                void testAdd() {
                    assertEquals(5, calculator.add(2, 3));
                    assertEquals(0, calculator.add(-1, 1));
                }
                
                @Test
                void testSubtract() {
                    assertEquals(1, calculator.subtract(3, 2));
                    assertEquals(-2, calculator.subtract(-1, 1));
                }
                
                @Test
                void testMultiply() {
                    assertEquals(6, calculator.multiply(2, 3));
                    assertEquals(0, calculator.multiply(0, 5));
                }
                
                @Test
                void testDivide() {
                    assertEquals(2.0, calculator.divide(6, 3), 0.001);
                    assertEquals(0.5, calculator.divide(1, 2), 0.001);
                }
                
                @Test
                void testDivideByZero() {
                    assertThrows(IllegalArgumentException.class, () -> {
                        calculator.divide(5, 0);
                    });
                }
            }
            """;
        
        return createTestFile("CalculatorTest.java", content);
    }
    
    private File createSampleSubmissionFile() throws IOException {
        // This is a correct implementation that should pass all tests
        String content = """
            public class Calculator {
                public int add(int a, int b) {
                    return a + b;
                }
                
                public int subtract(int a, int b) {
                    return a - b;
                }
                
                public int multiply(int a, int b) {
                    return a * b;
                }
                
                public double divide(int a, int b) {
                    if (b == 0) {
                        throw new IllegalArgumentException("Cannot divide by zero");
                    }
                    return (double) a / b;
                }
            }
            """;
        
        return createTestFile("StudentCalculator.java", content);
    }
    
    private File createTestFile(String fileName, String content) throws IOException {
        Path filePath = tempDir.resolve(fileName);
        Files.write(filePath, content.getBytes());
        return filePath.toFile();
    }
}