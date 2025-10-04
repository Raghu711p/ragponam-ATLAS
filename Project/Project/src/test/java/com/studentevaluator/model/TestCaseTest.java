package com.studentevaluator.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TestCase Model Tests")
class TestCaseTest {
    
    @Test
    @DisplayName("Should create test case for passed test")
    void shouldCreateTestCaseForPassedTest() {
        // Given
        String testName = "testAddition";
        boolean passed = true;
        long executionTime = 150L;
        
        // When
        TestCase testCase = new TestCase(testName, passed, executionTime);
        
        // Then
        assertEquals(testName, testCase.getTestName());
        assertTrue(testCase.isPassed());
        assertEquals(executionTime, testCase.getExecutionTimeMs());
        assertNull(testCase.getFailureMessage());
        assertNull(testCase.getStackTrace());
    }
    
    @Test
    @DisplayName("Should create test case for failed test")
    void shouldCreateTestCaseForFailedTest() {
        // Given
        String testName = "testDivision";
        boolean passed = false;
        String failureMessage = "Expected 2 but was 3";
        String stackTrace = "java.lang.AssertionError at line 42";
        long executionTime = 200L;
        
        // When
        TestCase testCase = new TestCase(testName, passed, failureMessage, stackTrace, executionTime);
        
        // Then
        assertEquals(testName, testCase.getTestName());
        assertFalse(testCase.isPassed());
        assertEquals(failureMessage, testCase.getFailureMessage());
        assertEquals(stackTrace, testCase.getStackTrace());
        assertEquals(executionTime, testCase.getExecutionTimeMs());
    }
    
    @Test
    @DisplayName("Should create test case with default constructor")
    void shouldCreateTestCaseWithDefaultConstructor() {
        // When
        TestCase testCase = new TestCase();
        
        // Then
        assertNull(testCase.getTestName());
        assertFalse(testCase.isPassed()); // default boolean value
        assertNull(testCase.getFailureMessage());
        assertNull(testCase.getStackTrace());
        assertEquals(0L, testCase.getExecutionTimeMs()); // default long value
    }
    
    @Test
    @DisplayName("Should set and get all properties correctly")
    void shouldSetAndGetAllPropertiesCorrectly() {
        // Given
        TestCase testCase = new TestCase();
        String testName = "testMultiplication";
        boolean passed = true;
        String failureMessage = "No failure";
        String stackTrace = "No stack trace";
        long executionTime = 100L;
        
        // When
        testCase.setTestName(testName);
        testCase.setPassed(passed);
        testCase.setFailureMessage(failureMessage);
        testCase.setStackTrace(stackTrace);
        testCase.setExecutionTimeMs(executionTime);
        
        // Then
        assertEquals(testName, testCase.getTestName());
        assertTrue(testCase.isPassed());
        assertEquals(failureMessage, testCase.getFailureMessage());
        assertEquals(stackTrace, testCase.getStackTrace());
        assertEquals(executionTime, testCase.getExecutionTimeMs());
    }
    
    @Test
    @DisplayName("Should implement equals and hashCode correctly")
    void shouldImplementEqualsAndHashCodeCorrectly() {
        // Given
        TestCase testCase1 = new TestCase("test1", true, 100L);
        TestCase testCase2 = new TestCase("test1", true, 100L);
        TestCase testCase3 = new TestCase("test2", false, "Failed", "Stack", 200L);
        
        // Then
        assertEquals(testCase1, testCase2);
        assertNotEquals(testCase1, testCase3);
        assertEquals(testCase1.hashCode(), testCase2.hashCode());
        assertNotEquals(testCase1.hashCode(), testCase3.hashCode());
    }
    
    @Test
    @DisplayName("Should implement toString correctly")
    void shouldImplementToStringCorrectly() {
        // Given
        TestCase testCase = new TestCase("testSubtraction", false, "Expected 5 but was 4", "Stack trace", 180L);
        
        // When
        String toString = testCase.toString();
        
        // Then
        assertTrue(toString.contains("testSubtraction"));
        assertTrue(toString.contains("passed=false"));
        assertTrue(toString.contains("Expected 5 but was 4"));
        assertTrue(toString.contains("180"));
    }
    
    @Test
    @DisplayName("Should handle null values correctly")
    void shouldHandleNullValuesCorrectly() {
        // Given
        TestCase testCase = new TestCase();
        
        // When
        testCase.setTestName(null);
        testCase.setFailureMessage(null);
        testCase.setStackTrace(null);
        
        // Then
        assertNull(testCase.getTestName());
        assertNull(testCase.getFailureMessage());
        assertNull(testCase.getStackTrace());
    }
    
    @Test
    @DisplayName("Should handle equals with null and different types")
    void shouldHandleEqualsWithNullAndDifferentTypes() {
        // Given
        TestCase testCase = new TestCase("test1", true, 100L);
        
        // Then
        assertNotEquals(testCase, null);
        assertNotEquals(testCase, "not a test case");
        assertEquals(testCase, testCase); // reflexive
    }
}