package com.studentevaluator.engine;

import com.studentevaluator.model.TestCase;
import com.studentevaluator.model.TestExecutionResult;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * Demonstration class showing how to use the JUnitTestRunner.
 * This class is not a test itself, but demonstrates the functionality.
 */
public class JUnitTestRunnerDemo {
    
    public static void main(String[] args) {
        try {
            demonstrateJUnitTestRunner();
        } catch (Exception e) {
            System.err.println("Demo failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void demonstrateJUnitTestRunner() throws IOException {
        System.out.println("=== JUnit Test Runner Demonstration ===\n");
        
        // Create temporary directory for demo
        Path tempDir = Files.createTempDirectory("junit-runner-demo");
        System.out.println("Working directory: " + tempDir);
        
        try {
            // Create JUnitTestRunner instance
            JUnitTestRunner testRunner = new JUnitTestRunner();
            
            // Demo 1: Test with null inputs
            System.out.println("\n1. Testing with null inputs:");
            TestExecutionResult result1 = testRunner.runTests(null, Arrays.asList(new File("test.class")));
            printResult(result1);
            
            // Demo 2: Test with non-existent files
            System.out.println("\n2. Testing with non-existent files:");
            TestExecutionResult result2 = testRunner.runTests(
                tempDir.resolve("NonExistent.class").toString(),
                Arrays.asList(tempDir.resolve("NonExistentTest.class").toFile())
            );
            printResult(result2);
            
            // Demo 3: Test with timeout
            System.out.println("\n3. Testing timeout functionality:");
            TestExecutionResult result3 = testRunner.runTests(
                tempDir.toString(),
                Arrays.asList(tempDir.resolve("TimeoutTest.class").toFile()),
                100 // Very short timeout
            );
            printResult(result3);
            
            // Demo 4: Test with actual compiled classes (if Java compiler is available)
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            if (compiler != null) {
                System.out.println("\n4. Testing with compiled classes:");
                demonstrateWithCompiledClasses(testRunner, tempDir, compiler);
            } else {
                System.out.println("\n4. Skipping compiled class demo - Java compiler not available");
            }
            
        } finally {
            // Clean up
            deleteDirectory(tempDir.toFile());
            System.out.println("\nDemo completed. Temporary files cleaned up.");
        }
    }
    
    private static void demonstrateWithCompiledClasses(JUnitTestRunner testRunner, Path tempDir, JavaCompiler compiler) {
        try {
            // Create source directories
            Path srcDir = tempDir.resolve("src");
            Path classesDir = tempDir.resolve("classes");
            Files.createDirectories(srcDir);
            Files.createDirectories(classesDir);
            
            // Create a simple calculator class
            String calculatorSource = 
                "public class SimpleCalculator {\n" +
                "    public int add(int a, int b) { return a + b; }\n" +
                "    public int subtract(int a, int b) { return a - b; }\n" +
                "}\n";
            
            Path calculatorFile = srcDir.resolve("SimpleCalculator.java");
            Files.write(calculatorFile, calculatorSource.getBytes());
            
            // Create a test class
            String testSource = 
                "import org.junit.jupiter.api.Test;\n" +
                "import static org.junit.jupiter.api.Assertions.*;\n" +
                "\n" +
                "public class SimpleCalculatorTest {\n" +
                "    @Test\n" +
                "    void testAdd() {\n" +
                "        SimpleCalculator calc = new SimpleCalculator();\n" +
                "        assertEquals(5, calc.add(2, 3));\n" +
                "    }\n" +
                "    \n" +
                "    @Test\n" +
                "    void testSubtract() {\n" +
                "        SimpleCalculator calc = new SimpleCalculator();\n" +
                "        assertEquals(1, calc.subtract(3, 2));\n" +
                "    }\n" +
                "    \n" +
                "    @Test\n" +
                "    void testFailingTest() {\n" +
                "        SimpleCalculator calc = new SimpleCalculator();\n" +
                "        assertEquals(10, calc.add(2, 3)); // This will fail\n" +
                "    }\n" +
                "}\n";
            
            Path testFile = srcDir.resolve("SimpleCalculatorTest.java");
            Files.write(testFile, testSource.getBytes());
            
            // Compile the classes
            String[] compileArgs = {
                "-d", classesDir.toString(),
                "-cp", System.getProperty("java.class.path"),
                calculatorFile.toString(),
                testFile.toString()
            };
            
            int compileResult = compiler.run(null, null, null, compileArgs);
            
            if (compileResult == 0) {
                System.out.println("   Classes compiled successfully");
                
                // Run the tests
                TestExecutionResult result = testRunner.runTests(
                    classesDir.resolve("SimpleCalculator.class").toString(),
                    Arrays.asList(classesDir.resolve("SimpleCalculatorTest.class").toFile())
                );
                
                printResult(result);
                
                // Print detailed test case information
                if (!result.getTestCases().isEmpty()) {
                    System.out.println("   Individual test cases:");
                    for (TestCase testCase : result.getTestCases()) {
                        System.out.printf("     - %s: %s (%dms)%n", 
                            testCase.getTestName(), 
                            testCase.isPassed() ? "PASSED" : "FAILED",
                            testCase.getExecutionTimeMs());
                        if (!testCase.isPassed() && testCase.getFailureMessage() != null) {
                            System.out.println("       Failure: " + testCase.getFailureMessage());
                        }
                    }
                }
            } else {
                System.out.println("   Compilation failed with exit code: " + compileResult);
            }
            
        } catch (Exception e) {
            System.out.println("   Error in compiled class demo: " + e.getMessage());
        }
    }
    
    private static void printResult(TestExecutionResult result) {
        System.out.println("   Result:");
        System.out.println("     Total tests: " + result.getTotalTests());
        System.out.println("     Passed: " + result.getPassedTests());
        System.out.println("     Failed: " + result.getFailedTests());
        System.out.println("     Success rate: " + String.format("%.1f%%", result.getSuccessRate()));
        System.out.println("     Execution time: " + result.getTotalExecutionTimeMs() + "ms");
        
        String log = result.getExecutionLog();
        if (log != null && !log.trim().isEmpty()) {
            System.out.println("     Log excerpt: " + 
                (log.length() > 100 ? log.substring(0, 100) + "..." : log).replace("\n", " "));
        }
    }
    
    private static void deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }
            directory.delete();
        }
    }
}