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
 * Unit tests for JavaCompiler component.
 * Tests compilation scenarios including successful and failed compilations,
 * file validation, and security measures.
 */
class JavaCompilerTest {
    
    private JavaCompiler javaCompiler;
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    void setUp() {
        javaCompiler = new JavaCompiler();
    }
    
    @Test
    void testSuccessfulCompilation() throws IOException {
        // Create a valid Java file
        String javaCode = """
            public class HelloWorld {
                public static void main(String[] args) {
                    System.out.println("Hello, World!");
                }
            }
            """;
        
        Path javaFile = tempDir.resolve("HelloWorld.java");
        Files.write(javaFile, javaCode.getBytes());
        
        Path outputDir = tempDir.resolve("output");
        
        // Compile the file
        CompilationResult result = javaCompiler.compile(javaFile.toFile(), outputDir.toString());
        
        // Verify successful compilation
        assertTrue(result.isSuccessful());
        assertFalse(result.hasErrors());
        assertNotNull(result.getCompiledClassPath());
        assertTrue(Files.exists(Path.of(result.getCompiledClassPath())));
    }
    
    @Test
    void testFailedCompilation() throws IOException {
        // Create an invalid Java file with syntax errors
        String javaCode = """
            public class InvalidCode {
                public static void main(String[] args) {
                    System.out.println("Missing semicolon")
                    int x = "string"; // Type mismatch
                }
            """; // Missing closing brace
        
        Path javaFile = tempDir.resolve("InvalidCode.java");
        Files.write(javaFile, javaCode.getBytes());
        
        Path outputDir = tempDir.resolve("output");
        
        // Compile the file
        CompilationResult result = javaCompiler.compile(javaFile.toFile(), outputDir.toString());
        
        // Verify failed compilation
        assertFalse(result.isSuccessful());
        assertTrue(result.hasErrors());
        assertNull(result.getCompiledClassPath());
        assertFalse(result.getErrors().isEmpty());
    }
    
    @Test
    void testCompilationWithPackage() throws IOException {
        // Create a Java file with package declaration
        String javaCode = """
            package com.example;
            
            public class PackagedClass {
                public void doSomething() {
                    System.out.println("Packaged class method");
                }
            }
            """;
        
        Path packageDir = tempDir.resolve("com").resolve("example");
        Files.createDirectories(packageDir);
        Path javaFile = packageDir.resolve("PackagedClass.java");
        Files.write(javaFile, javaCode.getBytes());
        
        Path outputDir = tempDir.resolve("output");
        
        // Compile the file
        CompilationResult result = javaCompiler.compile(javaFile.toFile(), outputDir.toString());
        
        // Verify successful compilation
        assertTrue(result.isSuccessful());
        assertFalse(result.hasErrors());
        assertNotNull(result.getCompiledClassPath());
        assertTrue(Files.exists(Path.of(result.getCompiledClassPath())));
    }
    
    @Test
    void testNullInputValidation() {
        // Test null javaFile
        CompilationResult result1 = javaCompiler.compile(null, tempDir.toString());
        assertFalse(result1.isSuccessful());
        assertTrue(result1.hasErrors());
        assertTrue(result1.getErrors().get(0).contains("cannot be null"));
        
        // Test null outputDir
        File dummyFile = new File("dummy.java");
        CompilationResult result2 = javaCompiler.compile(dummyFile, null);
        assertFalse(result2.isSuccessful());
        assertTrue(result2.hasErrors());
        assertTrue(result2.getErrors().get(0).contains("cannot be null"));
    }
    
    @Test
    void testNonExistentFile() {
        File nonExistentFile = new File("nonexistent.java");
        
        CompilationResult result = javaCompiler.compile(nonExistentFile, tempDir.toString());
        
        assertFalse(result.isSuccessful());
        assertTrue(result.hasErrors());
        assertTrue(result.getErrors().get(0).contains("does not exist"));
    }
    
    @Test
    void testInvalidFileExtension() throws IOException {
        // Create a file with wrong extension
        Path txtFile = tempDir.resolve("NotJava.txt");
        Files.write(txtFile, "This is not a Java file".getBytes());
        
        CompilationResult result = javaCompiler.compile(txtFile.toFile(), tempDir.toString());
        
        assertFalse(result.isSuccessful());
        assertTrue(result.hasErrors());
        assertTrue(result.getErrors().get(0).contains("Only .java files are allowed"));
    }
    
    @Test
    void testIsValidJavaFile() throws IOException {
        // Valid Java file
        Path validJavaFile = tempDir.resolve("Valid.java");
        Files.write(validJavaFile, "public class Valid {}".getBytes());
        assertTrue(javaCompiler.isValidJavaFile(validJavaFile.toFile()));
        
        // Invalid extension
        Path txtFile = tempDir.resolve("Invalid.txt");
        Files.write(txtFile, "content".getBytes());
        assertFalse(javaCompiler.isValidJavaFile(txtFile.toFile()));
        
        // Null file
        assertFalse(javaCompiler.isValidJavaFile(null));
        
        // Directory instead of file
        Path directory = tempDir.resolve("directory");
        Files.createDirectory(directory);
        assertFalse(javaCompiler.isValidJavaFile(directory.toFile()));
        
        // Case insensitive check
        Path upperCaseJava = tempDir.resolve("UpperCase.JAVA");
        Files.write(upperCaseJava, "public class UpperCase {}".getBytes());
        assertTrue(javaCompiler.isValidJavaFile(upperCaseJava.toFile()));
    }
    
    @Test
    void testIsValidFileSize() throws IOException {
        // Valid size file
        Path smallFile = tempDir.resolve("Small.java");
        Files.write(smallFile, "public class Small {}".getBytes());
        assertTrue(javaCompiler.isValidFileSize(smallFile.toFile()));
        
        // Create a large file (over 1MB)
        Path largeFile = tempDir.resolve("Large.java");
        byte[] largeContent = new byte[2 * 1024 * 1024]; // 2MB
        Files.write(largeFile, largeContent);
        assertFalse(javaCompiler.isValidFileSize(largeFile.toFile()));
        
        // Null file
        assertFalse(javaCompiler.isValidFileSize(null));
        
        // Non-existent file
        File nonExistent = new File("nonexistent.java");
        assertFalse(javaCompiler.isValidFileSize(nonExistent));
    }
    
    @Test
    void testLargeFileRejection() throws IOException {
        // Create a file that exceeds size limit
        Path largeFile = tempDir.resolve("Large.java");
        byte[] largeContent = new byte[2 * 1024 * 1024]; // 2MB
        // Fill with valid Java code pattern
        String pattern = "// This is a comment to make the file large\n";
        StringBuilder sb = new StringBuilder();
        sb.append("public class Large {\n");
        while (sb.length() < largeContent.length - 100) {
            sb.append(pattern);
        }
        sb.append("}\n");
        Files.write(largeFile, sb.toString().getBytes());
        
        CompilationResult result = javaCompiler.compile(largeFile.toFile(), tempDir.toString());
        
        assertFalse(result.isSuccessful());
        assertTrue(result.hasErrors());
        assertTrue(result.getErrors().get(0).contains("exceeds maximum allowed size"));
    }
    
    @Test
    void testIsCompilationSuccessful() {
        // Test with successful result
        CompilationResult successResult = new CompilationResult(true, "Success", "/path/to/class");
        assertTrue(javaCompiler.isCompilationSuccessful(successResult));
        
        // Test with failed result
        CompilationResult failResult = new CompilationResult(false, "Failed", java.util.Arrays.asList("Error"));
        assertFalse(javaCompiler.isCompilationSuccessful(failResult));
        
        // Test with null result
        assertFalse(javaCompiler.isCompilationSuccessful(null));
    }
    
    @Test
    void testCompilationOutput() throws IOException {
        // Create Java file with warnings
        String javaCode = """
            public class WithWarnings {
                @SuppressWarnings("unused")
                private int unusedField;
                
                public static void main(String[] args) {
                    System.out.println("Hello with warnings");
                }
            }
            """;
        
        Path javaFile = tempDir.resolve("WithWarnings.java");
        Files.write(javaFile, javaCode.getBytes());
        
        Path outputDir = tempDir.resolve("output");
        
        CompilationResult result = javaCompiler.compile(javaFile.toFile(), outputDir.toString());
        
        // Should still be successful despite warnings
        assertTrue(result.isSuccessful());
        assertNotNull(result.getOutput());
        // Output might be empty for successful compilations with no warnings
    }
    
    @Test
    void testMultipleCompilationErrors() throws IOException {
        // Create Java file with multiple semantic errors (valid syntax)
        String javaCode = """
            public class MultipleErrors {
                public static void main(String[] args) {
                    undeclaredVariable = 5;
                    String s = 123; // Type mismatch
                    nonExistentMethod();
                }
            }
            """;
        
        Path javaFile = tempDir.resolve("MultipleErrors.java");
        Files.write(javaFile, javaCode.getBytes());
        
        Path outputDir = tempDir.resolve("output");
        
        CompilationResult result = javaCompiler.compile(javaFile.toFile(), outputDir.toString());
        
        assertFalse(result.isSuccessful());
        assertTrue(result.hasErrors());
        // Should have multiple error messages (at least 2)
        assertTrue(result.getErrors().size() >= 2, "Expected at least 2 errors, but got: " + result.getErrors().size() + " - " + result.getErrors());
    }
    
    @Test
    void testOutputDirectoryCreation() throws IOException {
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
        
        // Use a non-existent output directory
        Path outputDir = tempDir.resolve("nested").resolve("output").resolve("dir");
        
        CompilationResult result = javaCompiler.compile(javaFile.toFile(), outputDir.toString());
        
        // Should create the directory and compile successfully
        assertTrue(result.isSuccessful());
        assertTrue(Files.exists(outputDir));
        assertNotNull(result.getCompiledClassPath());
    }
}