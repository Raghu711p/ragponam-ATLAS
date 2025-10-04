package com.studentevaluator.model;

import java.util.Objects;

/**
 * Model class representing an individual test case result.
 * Contains test case name, status, and failure details if applicable.
 */
public class TestCase {
    
    private String testName;
    private boolean passed;
    private String failureMessage;
    private String stackTrace;
    private long executionTimeMs;
    
    // Default constructor
    public TestCase() {
    }
    
    // Constructor for passed test
    public TestCase(String testName, boolean passed, long executionTimeMs) {
        this.testName = testName;
        this.passed = passed;
        this.executionTimeMs = executionTimeMs;
    }
    
    // Constructor for failed test
    public TestCase(String testName, boolean passed, String failureMessage, String stackTrace, long executionTimeMs) {
        this.testName = testName;
        this.passed = passed;
        this.failureMessage = failureMessage;
        this.stackTrace = stackTrace;
        this.executionTimeMs = executionTimeMs;
    }
    
    // Getters and Setters
    public String getTestName() {
        return testName;
    }
    
    public void setTestName(String testName) {
        this.testName = testName;
    }
    
    public boolean isPassed() {
        return passed;
    }
    
    public void setPassed(boolean passed) {
        this.passed = passed;
    }
    
    public String getFailureMessage() {
        return failureMessage;
    }
    
    public void setFailureMessage(String failureMessage) {
        this.failureMessage = failureMessage;
    }
    
    public String getStackTrace() {
        return stackTrace;
    }
    
    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
    }
    
    public long getExecutionTimeMs() {
        return executionTimeMs;
    }
    
    public void setExecutionTimeMs(long executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestCase testCase = (TestCase) o;
        return passed == testCase.passed &&
                executionTimeMs == testCase.executionTimeMs &&
                Objects.equals(testName, testCase.testName) &&
                Objects.equals(failureMessage, testCase.failureMessage) &&
                Objects.equals(stackTrace, testCase.stackTrace);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(testName, passed, failureMessage, stackTrace, executionTimeMs);
    }
    
    @Override
    public String toString() {
        return "TestCase{" +
                "testName='" + testName + '\'' +
                ", passed=" + passed +
                ", failureMessage='" + failureMessage + '\'' +
                ", executionTimeMs=" + executionTimeMs +
                '}';
    }
}