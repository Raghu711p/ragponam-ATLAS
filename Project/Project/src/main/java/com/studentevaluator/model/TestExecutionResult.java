package com.studentevaluator.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Model class representing the result of JUnit test execution.
 * Contains test statistics, individual test case results, and execution logs.
 */
public class TestExecutionResult {
    
    private int totalTests;
    private int passedTests;
    private int failedTests;
    private List<TestCase> testCases;
    private String executionLog;
    private long totalExecutionTimeMs;
    
    // Default constructor
    public TestExecutionResult() {
        this.testCases = new ArrayList<>();
    }
    
    // Constructor with basic test counts
    public TestExecutionResult(int totalTests, int passedTests, int failedTests) {
        this.totalTests = totalTests;
        this.passedTests = passedTests;
        this.failedTests = failedTests;
        this.testCases = new ArrayList<>();
    }
    
    // Constructor with all fields
    public TestExecutionResult(int totalTests, int passedTests, int failedTests, 
                             List<TestCase> testCases, String executionLog, long totalExecutionTimeMs) {
        this.totalTests = totalTests;
        this.passedTests = passedTests;
        this.failedTests = failedTests;
        this.testCases = testCases != null ? new ArrayList<>(testCases) : new ArrayList<>();
        this.executionLog = executionLog;
        this.totalExecutionTimeMs = totalExecutionTimeMs;
    }
    
    // Getters and Setters
    public int getTotalTests() {
        return totalTests;
    }
    
    public void setTotalTests(int totalTests) {
        this.totalTests = totalTests;
    }
    
    public int getPassedTests() {
        return passedTests;
    }
    
    public void setPassedTests(int passedTests) {
        this.passedTests = passedTests;
    }
    
    public int getFailedTests() {
        return failedTests;
    }
    
    public void setFailedTests(int failedTests) {
        this.failedTests = failedTests;
    }
    
    public List<TestCase> getTestCases() {
        return testCases;
    }
    
    public void setTestCases(List<TestCase> testCases) {
        this.testCases = testCases != null ? new ArrayList<>(testCases) : new ArrayList<>();
    }
    
    public void addTestCase(TestCase testCase) {
        if (this.testCases == null) {
            this.testCases = new ArrayList<>();
        }
        this.testCases.add(testCase);
    }
    
    public String getExecutionLog() {
        return executionLog;
    }
    
    public void setExecutionLog(String executionLog) {
        this.executionLog = executionLog;
    }
    
    public long getTotalExecutionTimeMs() {
        return totalExecutionTimeMs;
    }
    
    public void setTotalExecutionTimeMs(long totalExecutionTimeMs) {
        this.totalExecutionTimeMs = totalExecutionTimeMs;
    }
    
    // Utility methods
    public double getSuccessRate() {
        if (totalTests == 0) {
            return 0.0;
        }
        return (double) passedTests / totalTests * 100.0;
    }
    
    public boolean allTestsPassed() {
        return totalTests > 0 && passedTests == totalTests;
    }
    
    public boolean hasFailures() {
        return failedTests > 0;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestExecutionResult that = (TestExecutionResult) o;
        return totalTests == that.totalTests &&
                passedTests == that.passedTests &&
                failedTests == that.failedTests &&
                totalExecutionTimeMs == that.totalExecutionTimeMs &&
                Objects.equals(testCases, that.testCases) &&
                Objects.equals(executionLog, that.executionLog);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(totalTests, passedTests, failedTests, testCases, executionLog, totalExecutionTimeMs);
    }
    
    @Override
    public String toString() {
        return "TestExecutionResult{" +
                "totalTests=" + totalTests +
                ", passedTests=" + passedTests +
                ", failedTests=" + failedTests +
                ", successRate=" + String.format("%.2f", getSuccessRate()) + "%" +
                ", totalExecutionTimeMs=" + totalExecutionTimeMs +
                '}';
    }
}