package com.studentevaluator.engine;

import com.studentevaluator.model.TestCase;
import com.studentevaluator.model.TestExecutionResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for JUnitTestRunner that compile and execute real test classes.
 */
class JUnitTestRunnerIntegrationTest {
    
    private JUnitTestRunner testRunner;
    private JavaCompiler compiler;
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    void setUp() {
        testRunner = new JUnitTestRunner();
        compiler = ToolProvider.getSystemJavaCompiler();
        
        // Skip tests if compiler is not available (running on JRE instead of JDK)
        if (compiler == null) {
            System.out.println("Skipping integration tests - Java compiler not available");
        }
    }
    
    @Test
    void testRunTests_WithCompiledPassingTests_ExecutesSuccessfully() throws IOException {
        if (compiler == null) return; // Skip if no compiler available
        
        // Create source and output directories
        Path srcDir = tempDir.resolve("src");
        Path classesDir = tempDir.resolve("classes");
        Files.createDirectories(srcDir);
        Files.createDirectories(classesDir);
        
        // Create a simple class to test
        String calculatorClass = 
            "public class Calculator {\n" +
            "    public int add(int a, int b) {\n" +
            "        return a + b;\n" +
            "    }\n" +
            "    \n" +
            "    public int multiply(int a, int b) {\n" +
            "        return a * b;\n" +
            "    }\n" +
            "}\n";
        
        Path calculatorFile = srcDir.resolve("Calculator.java");
        Files.write(calculatorFile, calculatorClass.getBytes());
        
        // Create a test class with passing tests
        String testClass = 
            "import org.junit.jupiter.api.Test;\n" +
            "import static org.junit.jupiter.api.Assertions.*;\n" +
            "\n" +
            "public class CalculatorTest {\n" +
            "    \n" +
            "    @Test\n" +
            "    void testAdd() {\n" +
            "        Calculator calc = new Calculator();\n" +
            "        assertEquals(5, calc.add(2, 3));\n" +
            "    }\n" +
            "    \n" +
            "    @Test\n" +
            "    void testAddNegative() {\n" +
            "        Calculator calc = new Calculator();\n" +
            "        assertEquals(-1, calc.add(-3, 2));\n" +
            "    }\n" +
            "    \n" +
            "    @Test\n" +
            "    void testMultiply() {\n" +
            "        Calculator calc = new Calculator();\n" +
            "        assertEquals(6, calc.multiply(2, 3));\n" +
            "    }\n" +
            "}\n";
        
        Path testFile = srcDir.resolve("CalculatorTest.java");
        Files.write(testFile, testClass.getBytes());
        
        // Compile the classes
        compileJavaFiles(Arrays.asList(calculatorFile.toFile(), testFile.toFile()), classesDir);
        
        // Run the tests
        Path compiledTestClass = classesDir.resolve("CalculatorTest.class");
        TestExecutionResult result = testRunner.runTests(
            classesDir.resolve("Calculator.class").toString(),
            Arrays.asList(compiledTestClass.toFile())
        );
        
        // Verify results
        assertNotNull(result);
        assertEquals(3, result.getTotalTests());
        assertEquals(3, result.getPassedTests());
        assertEquals(0, result.getFailedTests());
        assertTrue(result.allTestsPassed());
        assertEquals(100.0, result.getSuccessRate());
        
        // Verify test cases
        List<TestCase> testCases = result.getTestCases();
        assertEquals(3, testCases.size());
        
        for (TestCase testCase : testCases) {
            assertTrue(testCase.isPassed());
            assertNull(testCase.getFailureMessage());
            assertTrue(testCase.getExecutionTimeMs() >= 0);
        }
        
        // Verify execution log
        String log = result.getExecutionLog();
        assertNotNull(log);
        assertTrue(log.contains("PASSED"));
        assertTrue(log.contains("testAdd"));
        assertTrue(log.contains("testMultiply"));
    }
    
    @Test
    void testRunTests_WithCompiledFailingTests_CapturesFailures() throws IOException {
        if (compiler == null) return; // Skip if no compiler available
        
        // Create source and output directories
        Path srcDir = tempDir.resolve("src");
        Path classesDir = tempDir.resolve("classes");
        Files.createDirectories(srcDir);
        Files.createDirectories(classesDir);
        
        // Create a simple class to test
        String mathClass = 
            "public class MathUtils {\n" +
            "    public int divide(int a, int b) {\n" +
            "        if (b == 0) throw new IllegalArgumentException(\"Division by zero\");\n" +
            "        return a / b;\n" +
            "    }\n" +
            "}\n";
        
        Path mathFile = srcDir.resolve("MathUtils.java");
        Files.write(mathFile, mathClass.getBytes());
        
        // Create a test class with both passing and failing tests
        String testClass = 
            "import org.junit.jupiter.api.Test;\n" +
            "import static org.junit.jupiter.api.Assertions.*;\n" +
            "\n" +
            "public class MathUtilsTest {\n" +
            "    \n" +
            "    @Test\n" +
            "    void testDivideSuccess() {\n" +
            "        MathUtils math = new MathUtils();\n" +
            "        assertEquals(2, math.divide(6, 3));\n" +
            "    }\n" +
            "    \n" +
            "    @Test\n" +
            "    void testDivideByZero() {\n" +
            "        MathUtils math = new MathUtils();\n" +
            "        assertThrows(IllegalArgumentException.class, () -> math.divide(5, 0));\n" +
            "    }\n" +
            "    \n" +
            "    @Test\n" +
            "    void testFailingAssertion() {\n" +
            "        MathUtils math = new MathUtils();\n" +
            "        assertEquals(3, math.divide(6, 3)); // This should fail (6/3 = 2, not 3)\n" +
            "    }\n" +
            "}\n";
        
        Path testFile = srcDir.resolve("MathUtilsTest.java");
        Files.write(testFile, testClass.getBytes());
        
        // Compile the classes
        compileJavaFiles(Arrays.asList(mathFile.toFile(), testFile.toFile()), classesDir);
        
        // Run the tests
        Path compiledTestClass = classesDir.resolve("MathUtilsTest.class");
        TestExecutionResult result = testRunner.runTests(
            classesDir.resolve("MathUtils.class").toString(),
            Arrays.asList(compiledTestClass.toFile())
        );
        
        // Verify results
        assertNotNull(result);
        assertEquals(3, result.getTotalTests());
        assertEquals(2, result.getPassedTests());
        assertEquals(1, result.getFailedTests());
        assertFalse(result.allTestsPassed());
        assertTrue(result.hasFailures());
        assertEquals(66.67, result.getSuccessRate(), 0.01);
        
        // Verify test cases
        List<TestCase> testCases = result.getTestCases();
        assertEquals(3, testCases.size());
        
        int passedCount = 0;
        int failedCount = 0;
        TestCase failedTest = null;
        
        for (TestCase testCase : testCases) {
            if (testCase.isPassed()) {
                passedCount++;
                assertNull(testCase.getFailureMessage());
            } else {
                failedCount++;
                failedTest = testCase;
                assertNotNull(testCase.getFailureMessage());
                assertNotNull(testCase.getStackTrace());
            }
            assertTrue(testCase.getExecutionTimeMs() >= 0);
        }
        
        assertEquals(2, passedCount);
        assertEquals(1, failedCount);
        
        // Verify the failed test details
        assertNotNull(failedTest);
        assertTrue(failedTest.getTestName().contains("testFailingAssertion"));
        assertTrue(failedTest.getFailureMessage().contains("expected") || 
                  failedTest.getFailureMessage().contains("Expected"));
        
        // Verify execution log
        String log = result.getExecutionLog();
        assertNotNull(log);
        assertTrue(log.contains("PASSED"));
        assertTrue(log.contains("FAILED"));
        assertTrue(log.contains("testFailingAssertion"));
    }
    
    @Test
    void testRunTests_WithTimeout_StopsLongRunningTests() throws IOException {
        if (compiler == null) return; // Skip if no compiler available
        
        // Create source and output directories
        Path srcDir = tempDir.resolve("src");
        Path classesDir = tempDir.resolve("classes");
        Files.createDirectories(srcDir);
        Files.createDirectories(classesDir);
        
        // Create a test class with a long-running test
        String testClass = 
            "import org.junit.jupiter.api.Test;\n" +
            "import static org.junit.jupiter.api.Assertions.*;\n" +
            "\n" +
            "public class TimeoutTest {\n" +
            "    \n" +
            "    @Test\n" +
            "    void testQuick() {\n" +
            "        assertEquals(1, 1);\n" +
            "    }\n" +
            "    \n" +
            "    @Test\n" +
            "    void testSlow() {\n" +
            "        try {\n" +
            "            Thread.sleep(5000); // Sleep for 5 seconds\n" +
            "        } catch (InterruptedException e) {\n" +
            "            Thread.currentThread().interrupt();\n" +
            "        }\n" +
            "        assertEquals(1, 1);\n" +
            "    }\n" +
            "}\n";
        
        Path testFile = srcDir.resolve("TimeoutTest.java");
        Files.write(testFile, testClass.getBytes());
        
        // Compile the test class
        compileJavaFiles(Arrays.asList(testFile.toFile()), classesDir);
        
        // Run the tests with a short timeout
        Path compiledTestClass = classesDir.resolve("TimeoutTest.class");
        long startTime = System.currentTimeMillis();
        
        TestExecutionResult result = testRunner.runTests(
            classesDir.toString(),
            Arrays.asList(compiledTestClass.toFile()),
            2000 // 2 second timeout
        );
        
        long endTime = System.currentTimeMillis();
        long actualDuration = endTime - startTime;
        
        // Verify that the test was stopped due to timeout
        assertNotNull(result);
        assertTrue(actualDuration < 4000); // Should be much less than the 5 second sleep
        
        // The execution log should indicate timeout
        String log = result.getExecutionLog();
        assertNotNull(log);
        assertTrue(log.contains("timed out") || log.contains("timeout") || 
                  result.getTotalTests() == 0); // May not execute any tests due to timeout
    }
    
    /**
     * Helper method to compile Java files.
     */
    private void compileJavaFiles(List<File> sourceFiles, Path outputDir) throws IOException {
        if (compiler == null) {
            throw new IllegalStateException("Java compiler not available");
        }
        
        // Prepare compilation arguments
        String[] args = new String[sourceFiles.size() + 4];
        args[0] = "-d";
        args[1] = outputDir.toString();
        args[2] = "-cp";
        args[3] = System.getProperty("java.class.path");
        
        for (int i = 0; i < sourceFiles.size(); i++) {
            args[i + 4] = sourceFiles.get(i).getAbsolutePath();
        }
        
        // Compile
        int result = compiler.run(null, null, null, args);
        if (result != 0) {
            throw new RuntimeException("Compilation failed with exit code: " + result);
        }
    }
}