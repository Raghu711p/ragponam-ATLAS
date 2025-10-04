package com.studentevaluator.engine;

import com.studentevaluator.model.CompilationResult;
import org.springframework.stereotype.Component;

import javax.tools.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Java compilation engine that compiles Java source files using javax.tools.JavaCompiler.
 * Provides security measures to prevent path traversal and restrict file access.
 */
@Component
public class JavaCompiler {
    
    private static final String JAVA_FILE_EXTENSION = ".java";
    private static final String CLASS_FILE_EXTENSION = ".class";
    private static final int MAX_FILE_SIZE_MB = 1;
    private static final int MAX_FILE_SIZE_BYTES = MAX_FILE_SIZE_MB * 1024 * 1024;
    
    private final javax.tools.JavaCompiler systemCompiler;
    
    public JavaCompiler() {
        this.systemCompiler = ToolProvider.getSystemJavaCompiler();
        if (this.systemCompiler == null) {
            throw new IllegalStateException("Java compiler not available. Make sure you're running on a JDK, not JRE.");
        }
    }
    
    /**
     * Compiles a Java source file to the specified output directory.
     * 
     * @param javaFile The Java source file to compile
     * @param outputDir The directory where compiled classes should be placed
     * @return CompilationResult containing compilation status and details
     */
    public CompilationResult compile(File javaFile, String outputDir) {
        try {
            // Validate input parameters
            if (javaFile == null || outputDir == null) {
                return new CompilationResult(false, "", 
                    Arrays.asList("Invalid input: javaFile and outputDir cannot be null"));
            }
            
            // Validate file exists and is readable
            if (!javaFile.exists() || !javaFile.canRead()) {
                return new CompilationResult(false, "", 
                    Arrays.asList("File does not exist or is not readable: " + javaFile.getAbsolutePath()));
            }
            
            // Validate file extension
            if (!isValidJavaFile(javaFile)) {
                return new CompilationResult(false, "", 
                    Arrays.asList("Invalid file type. Only .java files are allowed."));
            }
            
            // Validate file size
            if (!isValidFileSize(javaFile)) {
                return new CompilationResult(false, "", 
                    Arrays.asList("File size exceeds maximum allowed size of " + MAX_FILE_SIZE_MB + "MB"));
            }
            
            // Security: Validate and sanitize paths
            Path sanitizedJavaPath = sanitizePath(javaFile.toPath());
            Path sanitizedOutputPath = sanitizePath(Paths.get(outputDir));
            
            // Create output directory if it doesn't exist
            Files.createDirectories(sanitizedOutputPath);
            
            // Prepare compilation
            DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
            
            try (StandardJavaFileManager fileManager = systemCompiler.getStandardFileManager(diagnostics, null, null)) {
                // Set output directory
                fileManager.setLocation(StandardLocation.CLASS_OUTPUT, Arrays.asList(sanitizedOutputPath.toFile()));
                
                // Get compilation units
                Iterable<? extends JavaFileObject> compilationUnits = 
                    fileManager.getJavaFileObjectsFromFiles(Arrays.asList(sanitizedJavaPath.toFile()));
                
                // Prepare compilation options
                List<String> options = Arrays.asList(
                    "-d", sanitizedOutputPath.toString(),
                    "-cp", System.getProperty("java.class.path")
                );
                
                // Perform compilation
                javax.tools.JavaCompiler.CompilationTask task = systemCompiler.getTask(
                    null, fileManager, diagnostics, options, null, compilationUnits);
                
                boolean success = task.call();
                
                // Collect compilation output and errors
                List<String> errors = new ArrayList<>();
                StringBuilder output = new StringBuilder();
                
                for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
                    String message = formatDiagnostic(diagnostic);
                    if (diagnostic.getKind() == Diagnostic.Kind.ERROR) {
                        errors.add(message);
                    }
                    output.append(message).append("\n");
                }
                
                // Determine compiled class path
                String compiledClassPath = null;
                if (success) {
                    compiledClassPath = findCompiledClassPath(sanitizedJavaPath, sanitizedOutputPath);
                }
                
                return new CompilationResult(success, output.toString().trim(), errors, compiledClassPath);
            }
            
        } catch (Exception e) {
            return new CompilationResult(false, "", 
                Arrays.asList("Compilation failed with exception: " + e.getMessage()));
        }
    }
    
    /**
     * Validates that the file is a Java source file.
     */
    public boolean isValidJavaFile(File file) {
        if (file == null || !file.isFile()) {
            return false;
        }
        
        String fileName = file.getName().toLowerCase();
        return fileName.endsWith(JAVA_FILE_EXTENSION);
    }
    
    /**
     * Validates that the file size is within acceptable limits.
     */
    public boolean isValidFileSize(File file) {
        if (file == null || !file.exists()) {
            return false;
        }
        
        return file.length() <= MAX_FILE_SIZE_BYTES;
    }
    
    /**
     * Sanitizes file paths to prevent path traversal attacks.
     * Ensures paths are within allowed boundaries and don't contain malicious patterns.
     */
    private Path sanitizePath(Path path) throws IOException {
        // Normalize the path to resolve any ".." or "." components
        Path normalizedPath = path.normalize();
        
        // Convert to absolute path
        Path absolutePath = normalizedPath.toAbsolutePath();
        
        // Check for path traversal attempts in the normalized path
        String normalizedString = normalizedPath.toString();
        if (normalizedString.contains("..") || normalizedString.contains("~")) {
            throw new SecurityException("Path traversal attempt detected: " + normalizedString);
        }
        
        // Additional security checks for suspicious patterns (excluding valid Windows drive letters)
        String pathString = absolutePath.toString();
        // Remove Windows drive letter (e.g., "C:") from the check
        String pathToCheck = pathString.replaceFirst("^[A-Za-z]:", "");
        if (pathToCheck.matches(".*[<>\"|?*].*")) {
            throw new SecurityException("Invalid characters in path: " + pathString);
        }
        
        return absolutePath;
    }
    
    /**
     * Formats diagnostic messages for better readability.
     */
    private String formatDiagnostic(Diagnostic<? extends JavaFileObject> diagnostic) {
        StringBuilder sb = new StringBuilder();
        
        sb.append(diagnostic.getKind().toString().toLowerCase());
        
        if (diagnostic.getSource() != null) {
            sb.append(" in ").append(diagnostic.getSource().getName());
        }
        
        if (diagnostic.getLineNumber() != Diagnostic.NOPOS) {
            sb.append(" at line ").append(diagnostic.getLineNumber());
        }
        
        if (diagnostic.getColumnNumber() != Diagnostic.NOPOS) {
            sb.append(", column ").append(diagnostic.getColumnNumber());
        }
        
        sb.append(": ").append(diagnostic.getMessage(null));
        
        return sb.toString();
    }
    
    /**
     * Finds the path to the compiled class file.
     */
    private String findCompiledClassPath(Path javaFilePath, Path outputDir) {
        try {
            String javaFileName = javaFilePath.getFileName().toString();
            String className = javaFileName.substring(0, javaFileName.lastIndexOf(JAVA_FILE_EXTENSION));
            String classFileName = className + CLASS_FILE_EXTENSION;
            
            Path classFilePath = outputDir.resolve(classFileName);
            
            if (Files.exists(classFilePath)) {
                return classFilePath.toString();
            }
            
            // If not found in root, search subdirectories (for packaged classes)
            try {
                return Files.walk(outputDir)
                    .filter(path -> path.getFileName().toString().equals(classFileName))
                    .findFirst()
                    .map(Path::toString)
                    .orElse(null);
            } catch (IOException e) {
                return null;
            }
            
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Checks if compilation was successful based on the result.
     */
    public boolean isCompilationSuccessful(CompilationResult result) {
        return result != null && result.isSuccessful();
    }
}