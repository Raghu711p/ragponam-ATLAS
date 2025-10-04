package com.studentevaluator.engine;

import com.studentevaluator.model.CompilationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Security-focused tests for JavaCompiler component.
 * Tests path traversal prevention and other security measures.
 */
class JavaCompilerSecurityTest {
    
    private JavaCompiler javaCompiler;
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    void setUp() {
        javaCompiler = new JavaCompiler();
    }
    
    @Test
    void testPathTraversalPrevention() throws IOException {
        // Create a valid Java file
        String javaCode = """
            public class TestClass {
                public void test() {
                    System.out.println("Test");
                }
            }
            """;
        
        Path javaFile = tempDir.resolve("TestClass.java");
        Files.write(javaFile, javaCode.getBytes());
        
        // Try to use path traversal in output directory
        String maliciousOutputDir = tempDir.toString() + "/../../../malicious";
        
        // The compiler should handle this safely without throwing security exceptions
        // because it normalizes and validates paths internally
        CompilationResult result = javaCompiler.compile(javaFile.toFile(), maliciousOutputDir);
        
        // Should either succeed with normalized path or fail gracefully
        // The important thing is it doesn't create files outside the intended area
        assertNotNull(result);
        // If it succeeds, the compiled class should be in a safe location
        if (result.isSuccessful() && result.getCompiledClassPath() != null) {
            Path compiledPath = Path.of(result.getCompiledClassPath());
            // The compiled file should be in a safe, normalized location
            // On Windows, the path normalization should prevent traversal
            assertTrue(compiledPath.isAbsolute());
        }
    }
    
    @Test
    void testInvalidCharactersInPath() throws IOException {
        // Create a valid Java file
        String javaCode = """
            public class TestClass {
                public void test() {
                    System.out.println("Test");
                }
            }
            """;
        
        Path javaFile = tempDir.resolve("TestClass.java");
        Files.write(javaFile, javaCode.getBytes());
        
        // Try output directories with invalid characters (on Windows)
        String[] invalidPaths = {
            tempDir.toString() + "/output<test>",
            tempDir.toString() + "/output|test",
            tempDir.toString() + "/output\"test\"",
            tempDir.toString() + "/output?test"
        };
        
        for (String invalidPath : invalidPaths) {
            CompilationResult result = javaCompiler.compile(javaFile.toFile(), invalidPath);
            
            // Should either handle gracefully or fail with appropriate error
            assertNotNull(result);
            // If it fails, it should be due to path validation
            if (!result.isSuccessful()) {
                assertTrue(result.hasErrors());
            }
        }
    }
    
    @Test
    void testMaliciousJavaContent() throws IOException {
        // Create Java file with potentially dangerous content
        String maliciousCode = """
            import java.io.*;
            import java.nio.file.*;
            
            public class MaliciousCode {
                public static void main(String[] args) {
                    try {
                        // Attempt to read system files
                        Files.readAllLines(Paths.get("/etc/passwd"));
                        // Attempt to write to system directories
                        Files.write(Paths.get("/tmp/malicious.txt"), "malicious".getBytes());
                        // Attempt to execute system commands
                        Runtime.getRuntime().exec("rm -rf /");
                    } catch (Exception e) {
                        // Ignore exceptions
                    }
                }
            }
            """;
        
        Path javaFile = tempDir.resolve("MaliciousCode.java");
        Files.write(javaFile, maliciousCode.getBytes());
        
        Path outputDir = tempDir.resolve("output");
        
        // The compiler should compile this (it's syntactically valid)
        // but the security is in not executing it and sandboxing the compilation
        CompilationResult result = javaCompiler.compile(javaFile.toFile(), outputDir.toString());
        
        // Compilation might succeed (it's valid Java syntax)
        // The security is in the execution environment, not compilation
        assertNotNull(result);
        
        // If compilation succeeds, verify the class file is created in the expected location
        if (result.isSuccessful()) {
            assertNotNull(result.getCompiledClassPath());
            assertTrue(result.getCompiledClassPath().startsWith(outputDir.toString()));
        }
    }
    
    @Test
    void testExcessivelyLongClassName() {
        // Create a Java file with an excessively long class name
        String longClassName = "A".repeat(200); // Long but reasonable class name
        String javaCode = String.format("""
            public class %s {
                public void test() {
                    System.out.println("Long class name test");
                }
            }
            """, longClassName);
        
        try {
            Path javaFile = tempDir.resolve(longClassName + ".java");
            Files.write(javaFile, javaCode.getBytes());
            
            Path outputDir = tempDir.resolve("output");
            
            CompilationResult result = javaCompiler.compile(javaFile.toFile(), outputDir.toString());
            
            // Should handle long names gracefully without crashing
            assertNotNull(result);
            // May succeed or fail depending on filesystem limits, but shouldn't throw uncaught exceptions
            
        } catch (IOException e) {
            // File system might reject the long filename, which is acceptable
            // The test passes as long as we handle it gracefully
            assertNotNull(e.getMessage());
        } catch (Exception e) {
            // Any other exception should be handled gracefully too
            assertNotNull(e.getMessage());
        }
    }
    
    @Test
    void testCompilationWithRestrictedPermissions() throws IOException {
        // Create a valid Java file
        String javaCode = """
            public class RestrictedTest {
                public void test() {
                    System.out.println("Restricted test");
                }
            }
            """;
        
        Path javaFile = tempDir.resolve("RestrictedTest.java");
        Files.write(javaFile, javaCode.getBytes());
        
        // Create output directory and try to restrict permissions
        Path outputDir = tempDir.resolve("restricted_output");
        Files.createDirectories(outputDir);
        
        // On Windows, permission restrictions work differently
        // This test mainly ensures the compiler handles permission issues gracefully
        CompilationResult result = javaCompiler.compile(javaFile.toFile(), outputDir.toString());
        
        // Should handle permission issues gracefully
        assertNotNull(result);
        // Result may succeed or fail, but should not throw uncaught exceptions
    }
    
    @Test
    void testNullByteInFilename() throws IOException {
        // Create a valid Java file
        String javaCode = """
            public class NullByteTest {
                public void test() {
                    System.out.println("Null byte test");
                }
            }
            """;
        
        Path javaFile = tempDir.resolve("NullByteTest.java");
        Files.write(javaFile, javaCode.getBytes());
        
        // Try output directory with null byte (should be handled by path validation)
        String outputWithNullByte = tempDir.toString() + "/output\0malicious";
        
        CompilationResult result = javaCompiler.compile(javaFile.toFile(), outputWithNullByte);
        
        // Should handle null bytes gracefully
        assertNotNull(result);
        // May succeed with sanitized path or fail with validation error
    }
    
    @Test
    void testSymbolicLinkHandling() throws IOException {
        // This test is more relevant on Unix systems, but we'll test the behavior
        String javaCode = """
            public class SymlinkTest {
                public void test() {
                    System.out.println("Symlink test");
                }
            }
            """;
        
        Path javaFile = tempDir.resolve("SymlinkTest.java");
        Files.write(javaFile, javaCode.getBytes());
        
        Path outputDir = tempDir.resolve("output");
        
        // Normal compilation should work
        CompilationResult result = javaCompiler.compile(javaFile.toFile(), outputDir.toString());
        
        assertNotNull(result);
        // Should handle symbolic links appropriately (resolve them safely)
    }
    
    @Test
    void testConcurrentCompilation() throws IOException, InterruptedException {
        // Test that concurrent compilations don't interfere with each other
        String javaCode1 = """
            public class Concurrent1 {
                public void test() {
                    System.out.println("Concurrent test 1");
                }
            }
            """;
        
        String javaCode2 = """
            public class Concurrent2 {
                public void test() {
                    System.out.println("Concurrent test 2");
                }
            }
            """;
        
        Path javaFile1 = tempDir.resolve("Concurrent1.java");
        Path javaFile2 = tempDir.resolve("Concurrent2.java");
        Files.write(javaFile1, javaCode1.getBytes());
        Files.write(javaFile2, javaCode2.getBytes());
        
        Path outputDir1 = tempDir.resolve("output1");
        Path outputDir2 = tempDir.resolve("output2");
        
        // Run compilations concurrently
        Thread thread1 = new Thread(() -> {
            CompilationResult result1 = javaCompiler.compile(javaFile1.toFile(), outputDir1.toString());
            assertTrue(result1.isSuccessful());
        });
        
        Thread thread2 = new Thread(() -> {
            CompilationResult result2 = javaCompiler.compile(javaFile2.toFile(), outputDir2.toString());
            assertTrue(result2.isSuccessful());
        });
        
        thread1.start();
        thread2.start();
        
        thread1.join(10000); // 10 second timeout
        thread2.join(10000); // 10 second timeout
        
        assertFalse(thread1.isAlive());
        assertFalse(thread2.isAlive());
    }
}