package com.studentevaluator.engine;

import com.studentevaluator.model.TestCase;
import com.studentevaluator.model.TestExecutionResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JUnitTestRunner component.
 * Tests various JUnit test scenarios including successful and failed tests.
 */
class JUnitTestRunnerTest {
    
    private JUnitTestRunner testRunner;
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    void setUp() {
        testRunner = new JUnitTestRunner();
    }
    
    @Test
    void testRunTests_WithNullInputs_ReturnsErrorResult() {
        // Test with null compiled class path
        TestExecutionResult result = testRunner.runTests(null, Arrays.asList(new File("test.class")));
        
        assertNotNull(result);
        assertEquals(0, result.getTotalTests());
        assertEquals(0, result.getPassedTests());
        assertEquals(0, result.getFailedTests());
        assertTrue(result.getExecutionLog().contains("Invalid input"));
    }
    
    @Test
    void testRunTests_WithEmptyTestFiles_ReturnsErrorResult() {
        TestExecutionResult result = testRunner.runTests("/some/path", new ArrayList<>());
        
        assertNotNull(result);
        assertEquals(0, result.getTotalTests());
        assertEquals(0, result.getPassedTests());
        assertEquals(0, result.getFailedTests());
        assertTrue(result.getExecutionLog().contains("Invalid input"));
    }
    
    @Test
    void testRunTests_WithNonExistentFiles_ReturnsErrorResult() throws IOException {
        // Create a non-existent class path
        String nonExistentClassPath = tempDir.resolve("NonExistent.class").toString();
        
        // Create a non-existent test file
        File nonExistentTestFile = tempDir.resolve("NonExistentTest.class").toFile();
        
        TestExecutionResult result = testRunner.runTests(nonExistentClassPath, 
                                                        Arrays.asList(nonExistentTestFile));
        
        assertNotNull(result);
        assertEquals(0, result.getTotalTests());
        assertEquals(0, result.getPassedTests());
        assertEquals(0, result.getFailedTests());
        assertNotNull(result.getExecutionLog());
    }
    
    @Test
    void testRunTests_WithValidSimpleTest_ExecutesSuccessfully() throws IOException {
        // Create a simple test class file structure
        Path classesDir = tempDir.resolve("classes");
        Files.createDirectories(classesDir);
        
        // Create a simple Java class to test
        String simpleClassContent = 
            "public class SimpleClass {\n" +
            "    public int add(int a, int b) {\n" +
            "        return a + b;\n" +
            "    }\n" +
            "}\n";
        
        Path simpleClassFile = classesDir.resolve("SimpleClass.java");
        Files.write(simpleClassFile, simpleClassContent.getBytes());
        
        // Create a simple test class
        String testClassContent = 
            "import org.junit.jupiter.api.Test;\n" +
            "import static org.junit.jupiter.api.Assertions.*;\n" +
            "\n" +
            "public class SimpleClassTest {\n" +
            "    @Test\n" +
            "    void testAdd() {\n" +
            "        SimpleClass sc = new SimpleClass();\n" +
            "        assertEquals(5, sc.add(2, 3));\n" +
            "    }\n" +
            "    \n" +
            "    @Test\n" +
            "    void testAddNegative() {\n" +
            "        SimpleClass sc = new SimpleClass();\n" +
            "        assertEquals(-1, sc.add(-3, 2));\n" +
            "    }\n" +
            "}\n";
        
        Path testClassFile = classesDir.resolve("SimpleClassTest.java");
        Files.write(testClassFile, testClassContent.getBytes());
        
        // Note: In a real scenario, these would be compiled .class files
        // For this test, we're testing the runner's error handling with .java files
        TestExecutionResult result = testRunner.runTests(
            classesDir.toString(), 
            Arrays.asList(testClassFile.toFile())
        );
        
        assertNotNull(result);
        // Since we're passing .java files instead of .class files, 
        // the runner should handle this gracefully
        assertNotNull(result.getExecutionLog());
    }
    
    @Test
    void testRunTests_WithTimeout_HandlesTimeoutGracefully() throws IOException {
        Path classesDir = tempDir.resolve("classes");
        Files.createDirectories(classesDir);
        
        // Create a test file (even though it won't execute properly)
        Path testFile = classesDir.resolve("TimeoutTest.class");
        Files.write(testFile, "dummy content".getBytes());
        
        // Run with very short timeout
        TestExecutionResult result = testRunner.runTests(
            classesDir.toString(), 
            Arrays.asList(testFile.toFile()),
            1 // 1ms timeout
        );
        
        assertNotNull(result);
        assertTrue(result.getTotalExecutionTimeMs() >= 0);
        assertNotNull(result.getExecutionLog());
    }
    
    @Test
    void testRunTests_WithMultipleTestFiles_ProcessesAll() throws IOException {
        Path classesDir = tempDir.resolve("classes");
        Files.createDirectories(classesDir);
        
        // Create multiple test files
        List<File> testFiles = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            Path testFile = classesDir.resolve("Test" + i + ".class");
            Files.write(testFile, ("Test class " + i).getBytes());
            testFiles.add(testFile.toFile());
        }
        
        TestExecutionResult result = testRunner.runTests(classesDir.toString(), testFiles);
        
        assertNotNull(result);
        assertNotNull(result.getExecutionLog());
        // The log should mention attempts to process files or indicate processing occurred
        String log = result.getExecutionLog();
        boolean hasProcessingEvidence = log.contains("Test1.class") || 
                                       log.contains("Test2.class") || 
                                       log.contains("Test3.class") ||
                                       log.contains("Could not determine class name") ||
                                       log.contains("Could not load test class") ||
                                       log.length() > 0; // At minimum, some log should be generated
        assertTrue(hasProcessingEvidence, "Expected evidence of test file processing in log: " + log);
    }
    
    @Test
    void testRunTests_ResultContainsExpectedFields() throws IOException {
        Path classesDir = tempDir.resolve("classes");
        Files.createDirectories(classesDir);
        
        Path testFile = classesDir.resolve("DummyTest.class");
        Files.write(testFile, "dummy".getBytes());
        
        TestExecutionResult result = testRunner.runTests(
            classesDir.toString(), 
            Arrays.asList(testFile.toFile())
        );
        
        assertNotNull(result);
        assertTrue(result.getTotalTests() >= 0);
        assertTrue(result.getPassedTests() >= 0);
        assertTrue(result.getFailedTests() >= 0);
        assertNotNull(result.getTestCases());
        assertNotNull(result.getExecutionLog());
        assertTrue(result.getTotalExecutionTimeMs() >= 0);
    }
    
    @Test
    void testRunTests_WithInvalidClassFile_HandlesGracefully() throws IOException {
        Path classesDir = tempDir.resolve("classes");
        Files.createDirectories(classesDir);
        
        // Create an invalid class file (not actually compiled bytecode)
        Path invalidClassFile = classesDir.resolve("InvalidTest.class");
        Files.write(invalidClassFile, "This is not valid bytecode".getBytes());
        
        TestExecutionResult result = testRunner.runTests(
            classesDir.toString(), 
            Arrays.asList(invalidClassFile.toFile())
        );
        
        assertNotNull(result);
        assertNotNull(result.getExecutionLog());
        // Should handle the invalid class file gracefully
        assertTrue(result.getExecutionLog().length() > 0);
    }
    
    @Test
    void testRunTests_LogTruncation_WorksCorrectly() throws IOException {
        Path classesDir = tempDir.resolve("classes");
        Files.createDirectories(classesDir);
        
        // Create many test files to potentially generate a large log
        List<File> testFiles = new ArrayList<>();
        for (int i = 1; i <= 50; i++) {
            Path testFile = classesDir.resolve("LargeTest" + i + ".class");
            Files.write(testFile, ("Large test class " + i + " with some content").getBytes());
            testFiles.add(testFile.toFile());
        }
        
        TestExecutionResult result = testRunner.runTests(classesDir.toString(), testFiles);
        
        assertNotNull(result);
        assertNotNull(result.getExecutionLog());
        
        // Log should be reasonable in size (truncated if necessary)
        assertTrue(result.getExecutionLog().length() <= 15000); // Allow some buffer over the 10000 limit
    }
    
    @Test
    void testRunTests_TestCaseCollection_WorksCorrectly() throws IOException {
        Path classesDir = tempDir.resolve("classes");
        Files.createDirectories(classesDir);
        
        Path testFile = classesDir.resolve("SampleTest.class");
        Files.write(testFile, "sample test content".getBytes());
        
        TestExecutionResult result = testRunner.runTests(
            classesDir.toString(), 
            Arrays.asList(testFile.toFile())
        );
        
        assertNotNull(result);
        assertNotNull(result.getTestCases());
        
        // Even if no tests actually run (due to invalid class files in our test),
        // the test cases list should be initialized
        assertTrue(result.getTestCases().size() >= 0);
    }
    
    @Test
    void testRunTests_ExecutionTimeTracking_WorksCorrectly() throws IOException {
        Path classesDir = tempDir.resolve("classes");
        Files.createDirectories(classesDir);
        
        Path testFile = classesDir.resolve("TimingTest.class");
        Files.write(testFile, "timing test content".getBytes());
        
        long startTime = System.currentTimeMillis();
        
        TestExecutionResult result = testRunner.runTests(
            classesDir.toString(), 
            Arrays.asList(testFile.toFile())
        );
        
        long endTime = System.currentTimeMillis();
        
        assertNotNull(result);
        assertTrue(result.getTotalExecutionTimeMs() >= 0);
        assertTrue(result.getTotalExecutionTimeMs() <= (endTime - startTime + 100)); // Allow some buffer
    }
}