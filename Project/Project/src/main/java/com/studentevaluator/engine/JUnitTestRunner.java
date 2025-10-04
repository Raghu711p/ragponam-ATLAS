package com.studentevaluator.engine;

import com.studentevaluator.model.TestCase;
import com.studentevaluator.model.TestExecutionResult;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;

/**
 * JUnit test execution engine that dynamically loads and executes test classes.
 * Provides timeout mechanisms and detailed test result collection.
 */
@Component
public class JUnitTestRunner {
    
    private static final long DEFAULT_TIMEOUT_MS = 30000; // 30 seconds
    private static final int MAX_LOG_SIZE = 10000; // Maximum log size in characters
    
    /**
     * Runs JUnit tests against a compiled class with associated test files.
     * 
     * @param compiledClassPath Path to the compiled class file
     * @param testFiles List of test class files to execute
     * @return TestExecutionResult containing test results and logs
     */
    public TestExecutionResult runTests(String compiledClassPath, List<File> testFiles) {
        return runTests(compiledClassPath, testFiles, DEFAULT_TIMEOUT_MS);
    }
    
    /**
     * Runs JUnit tests with a custom timeout.
     * 
     * @param compiledClassPath Path to the compiled class file
     * @param testFiles List of test class files to execute
     * @param timeoutMs Timeout in milliseconds
     * @return TestExecutionResult containing test results and logs
     */
    public TestExecutionResult runTests(String compiledClassPath, List<File> testFiles, long timeoutMs) {
        if (compiledClassPath == null || testFiles == null || testFiles.isEmpty()) {
            return new TestExecutionResult(0, 0, 0, new ArrayList<>(), 
                "Invalid input: compiledClassPath and testFiles cannot be null or empty", 0);
        }
        
        long startTime = System.currentTimeMillis();
        StringBuilder executionLog = new StringBuilder();
        List<TestCase> testCases = new ArrayList<>();
        
        try {
            // Set up class loader for dynamic loading
            URLClassLoader classLoader = createClassLoader(compiledClassPath, testFiles);
            
            // Execute tests with timeout
            CompletableFuture<TestExecutionResult> future = CompletableFuture.supplyAsync(() -> 
                executeTestsInternal(classLoader, testFiles, executionLog, testCases));
            
            TestExecutionResult result = future.get(timeoutMs, TimeUnit.MILLISECONDS);
            
            long totalTime = System.currentTimeMillis() - startTime;
            result.setTotalExecutionTimeMs(totalTime);
            
            return result;
            
        } catch (TimeoutException e) {
            executionLog.append("Test execution timed out after ").append(timeoutMs).append("ms\n");
            return new TestExecutionResult(0, 0, 0, testCases, 
                truncateLog(executionLog.toString()), System.currentTimeMillis() - startTime);
                
        } catch (Exception e) {
            executionLog.append("Test execution failed with exception: ").append(e.getMessage()).append("\n");
            return new TestExecutionResult(0, 0, 0, testCases, 
                truncateLog(executionLog.toString()), System.currentTimeMillis() - startTime);
        }
    }
    
    /**
     * Internal method to execute tests using JUnit Platform.
     */
    private TestExecutionResult executeTestsInternal(URLClassLoader classLoader, List<File> testFiles, 
                                                   StringBuilder executionLog, List<TestCase> testCases) {
        
        int totalTests = 0;
        int passedTests = 0;
        int failedTests = 0;
        
        try {
            // Set context class loader
            Thread.currentThread().setContextClassLoader(classLoader);
            
            for (File testFile : testFiles) {
                try {
                    String className = getClassNameFromFile(testFile);
                    if (className == null) {
                        executionLog.append("Could not determine class name for file: ")
                                  .append(testFile.getName()).append("\n");
                        continue;
                    }
                    
                    // Load the test class
                    Class<?> testClass = classLoader.loadClass(className);
                    
                    // Create launcher and discovery request
                    Launcher launcher = LauncherFactory.create();
                    LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                        .selectors(DiscoverySelectors.selectClass(testClass))
                        .build();
                    
                    // Create test execution listener
                    TestResultCollector collector = new TestResultCollector(testCases, executionLog);
                    
                    // Execute tests
                    launcher.execute(request, collector);
                    
                    // Update counters
                    totalTests += collector.getTotalTests();
                    passedTests += collector.getPassedTests();
                    failedTests += collector.getFailedTests();
                    
                } catch (ClassNotFoundException e) {
                    executionLog.append("Could not load test class from file: ")
                              .append(testFile.getName())
                              .append(" - ").append(e.getMessage()).append("\n");
                } catch (Exception e) {
                    executionLog.append("Error executing tests from file: ")
                              .append(testFile.getName())
                              .append(" - ").append(e.getMessage()).append("\n");
                }
            }
            
        } catch (Exception e) {
            executionLog.append("Test execution failed: ").append(e.getMessage()).append("\n");
        } finally {
            // Reset context class loader
            Thread.currentThread().setContextClassLoader(ClassLoader.getSystemClassLoader());
            
            // Close class loader
            try {
                classLoader.close();
            } catch (IOException e) {
                executionLog.append("Warning: Could not close class loader: ").append(e.getMessage()).append("\n");
            }
        }
        
        return new TestExecutionResult(totalTests, passedTests, failedTests, testCases, 
                                     truncateLog(executionLog.toString()), 0);
    }
    
    /**
     * Creates a URLClassLoader for dynamic class loading.
     */
    private URLClassLoader createClassLoader(String compiledClassPath, List<File> testFiles) throws Exception {
        Set<URL> urls = new HashSet<>();
        
        // Add compiled class directory
        Path classPath = Paths.get(compiledClassPath);
        if (Files.isRegularFile(classPath)) {
            // If it's a file, add its parent directory
            urls.add(classPath.getParent().toUri().toURL());
        } else {
            // If it's a directory, add it directly
            urls.add(classPath.toUri().toURL());
        }
        
        // Add test file directories
        for (File testFile : testFiles) {
            if (testFile.exists()) {
                if (testFile.isFile()) {
                    urls.add(testFile.getParentFile().toURI().toURL());
                } else {
                    urls.add(testFile.toURI().toURL());
                }
            }
        }
        
        // Add current classpath
        String currentClasspath = System.getProperty("java.class.path");
        if (currentClasspath != null) {
            for (String path : currentClasspath.split(File.pathSeparator)) {
                try {
                    urls.add(new File(path).toURI().toURL());
                } catch (Exception e) {
                    // Ignore invalid classpath entries
                }
            }
        }
        
        return new URLClassLoader(urls.toArray(new URL[0]), ClassLoader.getSystemClassLoader());
    }
    
    /**
     * Extracts class name from a compiled class file.
     */
    private String getClassNameFromFile(File classFile) {
        if (classFile == null || !classFile.exists() || !classFile.getName().endsWith(".class")) {
            return null;
        }
        
        try {
            String fileName = classFile.getName();
            String className = fileName.substring(0, fileName.lastIndexOf(".class"));
            
            // Handle package structure by examining the file path
            String absolutePath = classFile.getAbsolutePath();
            
            // Find the root of the class path by looking for common patterns
            // This is a simplified approach - in a real system, you'd want more robust package detection
            if (absolutePath.contains("target" + File.separator + "classes")) {
                String classesPath = absolutePath.substring(absolutePath.indexOf("target" + File.separator + "classes") + 
                                                          ("target" + File.separator + "classes").length() + 1);
                return classesPath.replace(File.separator, ".").replace(".class", "");
            }
            
            return className;
            
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Truncates log output to prevent memory issues.
     */
    private String truncateLog(String log) {
        if (log == null) {
            return "";
        }
        
        if (log.length() <= MAX_LOG_SIZE) {
            return log;
        }
        
        return log.substring(0, MAX_LOG_SIZE) + "\n... [Log truncated due to size limit] ...";
    }
    
    /**
     * Test execution listener that collects test results.
     */
    private static class TestResultCollector implements TestExecutionListener {
        
        private final List<TestCase> testCases;
        private final StringBuilder executionLog;
        private final Map<TestIdentifier, Long> testStartTimes;
        
        private int totalTests = 0;
        private int passedTests = 0;
        private int failedTests = 0;
        
        public TestResultCollector(List<TestCase> testCases, StringBuilder executionLog) {
            this.testCases = testCases;
            this.executionLog = executionLog;
            this.testStartTimes = new HashMap<>();
        }
        
        @Override
        public void executionStarted(TestIdentifier testIdentifier) {
            if (testIdentifier.isTest()) {
                testStartTimes.put(testIdentifier, System.currentTimeMillis());
                executionLog.append("Starting test: ").append(testIdentifier.getDisplayName()).append("\n");
            }
        }
        
        @Override
        public void executionFinished(TestIdentifier testIdentifier, 
                                    org.junit.platform.engine.TestExecutionResult testExecutionResult) {
            if (testIdentifier.isTest()) {
                totalTests++;
                
                long startTime = testStartTimes.getOrDefault(testIdentifier, System.currentTimeMillis());
                long executionTime = System.currentTimeMillis() - startTime;
                
                String testName = testIdentifier.getDisplayName();
                boolean passed = testExecutionResult.getStatus() == 
                               org.junit.platform.engine.TestExecutionResult.Status.SUCCESSFUL;
                
                if (passed) {
                    passedTests++;
                    testCases.add(new TestCase(testName, true, executionTime));
                    executionLog.append("PASSED: ").append(testName)
                              .append(" (").append(executionTime).append("ms)\n");
                } else {
                    failedTests++;
                    
                    String failureMessage = "";
                    String stackTrace = "";
                    
                    if (testExecutionResult.getThrowable().isPresent()) {
                        Throwable throwable = testExecutionResult.getThrowable().get();
                        failureMessage = throwable.getMessage();
                        
                        StringWriter sw = new StringWriter();
                        PrintWriter pw = new PrintWriter(sw);
                        throwable.printStackTrace(pw);
                        stackTrace = sw.toString();
                    }
                    
                    testCases.add(new TestCase(testName, false, failureMessage, stackTrace, executionTime));
                    executionLog.append("FAILED: ").append(testName)
                              .append(" (").append(executionTime).append("ms)")
                              .append(" - ").append(failureMessage).append("\n");
                }
            }
        }
        
        public int getTotalTests() {
            return totalTests;
        }
        
        public int getPassedTests() {
            return passedTests;
        }
        
        public int getFailedTests() {
            return failedTests;
        }
    }
}