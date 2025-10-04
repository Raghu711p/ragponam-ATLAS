package com.studentevaluator.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TestExecutionResult Model Tests")
class TestExecutionResultTest {
    
    @Test
    @DisplayName("Should create test execution result with basic counts")
    void shouldCreateTestExecutionResultWithBasicCounts() {
        // Given
        int totalTests = 10;
        int passedTests = 8;
        int failedTests = 2;
        
        // When
        TestExecutionResult result = new TestExecutionResult(totalTests, passedTests, failedTests);
        
        // Then
        assertEquals(totalTests, result.getTotalTests());
        assertEquals(passedTests, result.getPassedTests());
        assertEquals(failedTests, result.getFailedTests());
        assertNotNull(result.getTestCases());
        assertTrue(result.getTestCases().isEmpty());
    }
    
    @Test
    @DisplayName("Should create test execution result with all fields")
    void shouldCreateTestExecutionResultWithAllFields() {
        // Given
        int totalTests = 3;
        int passedTests = 2;
        int failedTests = 1;
        List<TestCase> testCases = Arrays.asList(
            new TestCase("test1", true, 100L),
            new TestCase("test2", true, 150L),
            new TestCase("test3", false, "Assertion failed", "Stack trace", 200L)
        );
        String executionLog = "Test execution completed";
        long totalExecutionTime = 450L;
        
        // When
        TestExecutionResult result = new TestExecutionResult(totalTests, passedTests, failedTests, 
                testCases, executionLog, totalExecutionTime);
        
        // Then
        assertEquals(totalTests, result.getTotalTests());
        assertEquals(passedTests, result.getPassedTests());
        assertEquals(failedTests, result.getFailedTests());
        assertEquals(3, result.getTestCases().size());
        assertEquals(executionLog, result.getExecutionLog());
        assertEquals(totalExecutionTime, result.getTotalExecutionTimeMs());
    }
    
    @Test
    @DisplayName("Should calculate success rate correctly")
    void shouldCalculateSuccessRateCorrectly() {
        // Given
        TestExecutionResult result1 = new TestExecutionResult(10, 8, 2);
        TestExecutionResult result2 = new TestExecutionResult(5, 5, 0);
        TestExecutionResult result3 = new TestExecutionResult(0, 0, 0);
        
        // Then
        assertEquals(80.0, result1.getSuccessRate(), 0.01);
        assertEquals(100.0, result2.getSuccessRate(), 0.01);
        assertEquals(0.0, result3.getSuccessRate(), 0.01);
    }
    
    @Test
    @DisplayName("Should determine if all tests passed")
    void shouldDetermineIfAllTestsPassed() {
        // Given
        TestExecutionResult allPassed = new TestExecutionResult(5, 5, 0);
        TestExecutionResult someFailed = new TestExecutionResult(5, 3, 2);
        TestExecutionResult noTests = new TestExecutionResult(0, 0, 0);
        
        // Then
        assertTrue(allPassed.allTestsPassed());
        assertFalse(someFailed.allTestsPassed());
        assertFalse(noTests.allTestsPassed());
    }
    
    @Test
    @DisplayName("Should determine if has failures")
    void shouldDetermineIfHasFailures() {
        // Given
        TestExecutionResult withFailures = new TestExecutionResult(5, 3, 2);
        TestExecutionResult noFailures = new TestExecutionResult(5, 5, 0);
        
        // Then
        assertTrue(withFailures.hasFailures());
        assertFalse(noFailures.hasFailures());
    }
    
    @Test
    @DisplayName("Should add test case to existing list")
    void shouldAddTestCaseToExistingList() {
        // Given
        TestExecutionResult result = new TestExecutionResult();
        TestCase testCase1 = new TestCase("test1", true, 100L);
        TestCase testCase2 = new TestCase("test2", false, "Failed", "Stack", 200L);
        
        // When
        result.addTestCase(testCase1);
        result.addTestCase(testCase2);
        
        // Then
        assertEquals(2, result.getTestCases().size());
        assertTrue(result.getTestCases().contains(testCase1));
        assertTrue(result.getTestCases().contains(testCase2));
    }
    
    @Test
    @DisplayName("Should initialize test cases list when adding to null list")
    void shouldInitializeTestCasesListWhenAddingToNullList() {
        // Given
        TestExecutionResult result = new TestExecutionResult();
        result.setTestCases(null);
        TestCase testCase = new TestCase("test1", true, 100L);
        
        // When
        result.addTestCase(testCase);
        
        // Then
        assertNotNull(result.getTestCases());
        assertEquals(1, result.getTestCases().size());
        assertTrue(result.getTestCases().contains(testCase));
    }
    
    @Test
    @DisplayName("Should set test cases with defensive copy")
    void shouldSetTestCasesWithDefensiveCopy() {
        // Given
        List<TestCase> originalTestCases = new ArrayList<>(Arrays.asList(
            new TestCase("test1", true, 100L),
            new TestCase("test2", false, "Failed", "Stack", 200L)
        ));
        TestExecutionResult result = new TestExecutionResult();
        
        // When
        result.setTestCases(originalTestCases);
        originalTestCases.clear(); // Modify original list
        
        // Then
        assertEquals(2, result.getTestCases().size()); // Should not be affected
    }
    
    @Test
    @DisplayName("Should handle null test cases list")
    void shouldHandleNullTestCasesList() {
        // When
        TestExecutionResult result = new TestExecutionResult(5, 3, 2, null, "Log", 1000L);
        
        // Then
        assertNotNull(result.getTestCases());
        assertTrue(result.getTestCases().isEmpty());
    }
    
    @Test
    @DisplayName("Should implement equals and hashCode correctly")
    void shouldImplementEqualsAndHashCodeCorrectly() {
        // Given
        List<TestCase> testCases = Arrays.asList(new TestCase("test1", true, 100L));
        TestExecutionResult result1 = new TestExecutionResult(5, 4, 1, testCases, "Log", 1000L);
        TestExecutionResult result2 = new TestExecutionResult(5, 4, 1, testCases, "Log", 1000L);
        TestExecutionResult result3 = new TestExecutionResult(3, 2, 1, testCases, "Log", 500L);
        
        // Then
        assertEquals(result1, result2);
        assertNotEquals(result1, result3);
        assertEquals(result1.hashCode(), result2.hashCode());
        assertNotEquals(result1.hashCode(), result3.hashCode());
    }
    
    @Test
    @DisplayName("Should implement toString correctly")
    void shouldImplementToStringCorrectly() {
        // Given
        TestExecutionResult result = new TestExecutionResult(10, 8, 2);
        
        // When
        String toString = result.toString();
        
        // Then
        assertTrue(toString.contains("totalTests=10"));
        assertTrue(toString.contains("passedTests=8"));
        assertTrue(toString.contains("failedTests=2"));
        assertTrue(toString.contains("successRate=80.00%"));
    }
}