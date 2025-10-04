package com.studentevaluator.security;

import com.studentevaluator.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Comprehensive input validation component for security and data integrity.
 * Validates file uploads, user inputs, and prevents malicious content injection.
 */
@Component
public class InputValidator {
    
    private static final Logger logger = LoggerFactory.getLogger(InputValidator.class);
    
    // File validation constants
    private static final long MAX_FILE_SIZE = 1024 * 1024; // 1MB
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(".java");
    private static final List<String> DANGEROUS_KEYWORDS = Arrays.asList(
        "Runtime.getRuntime()", "ProcessBuilder", "System.exit", "System.setProperty",
        "Class.forName", "URLClassLoader", "ScriptEngine", "javax.script",
        "java.lang.reflect", "sun.misc.Unsafe", "java.io.FileInputStream",
        "java.io.FileOutputStream", "java.nio.file.Files.delete",
        "java.net.Socket", "java.net.ServerSocket", "java.net.URL"
    );
    
    // Input validation patterns
    private static final Pattern ALPHANUMERIC_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]+$");
    private static final Pattern SAFE_TEXT_PATTERN = Pattern.compile("^[a-zA-Z0-9\\s._-]+$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    private static final Pattern PATH_TRAVERSAL_PATTERN = Pattern.compile(".*(\\.\\./|\\.\\.\\\\).*");
    
    /**
     * Validates uploaded Java files for security and content safety.
     * 
     * @param file the uploaded file
     * @param fieldName the field name for error reporting
     * @throws ValidationException if validation fails
     */
    public void validateJavaFile(MultipartFile file, String fieldName) {
        if (file == null || file.isEmpty()) {
            throw new ValidationException(fieldName + " cannot be null or empty");
        }
        
        // Validate file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ValidationException(fieldName + " exceeds maximum size of " + MAX_FILE_SIZE + " bytes");
        }
        
        // Validate file extension
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !hasAllowedExtension(originalFilename)) {
            throw new ValidationException(fieldName + " must have a valid Java file extension");
        }
        
        // Validate filename for path traversal
        if (containsPathTraversal(originalFilename)) {
            throw new ValidationException(fieldName + " contains invalid path characters");
        }
        
        // Validate file content
        try {
            String content = new String(file.getBytes());
            validateJavaContent(content, fieldName);
        } catch (IOException e) {
            throw new ValidationException("Unable to read " + fieldName + " content", e);
        }
        
        logger.debug("File validation passed for: {}", originalFilename);
    }
    
    /**
     * Validates Java source code content for security risks.
     * 
     * @param content the Java source code
     * @param fieldName the field name for error reporting
     * @throws ValidationException if dangerous content is detected
     */
    public void validateJavaContent(String content, String fieldName) {
        if (content == null || content.trim().isEmpty()) {
            throw new ValidationException(fieldName + " content cannot be empty");
        }
        
        // Check for dangerous keywords
        for (String keyword : DANGEROUS_KEYWORDS) {
            if (content.contains(keyword)) {
                logger.warn("Dangerous keyword detected in {}: {}", fieldName, keyword);
                throw new ValidationException(fieldName + " contains potentially dangerous code: " + keyword);
            }
        }
        
        // Check for basic Java structure
        if (!content.contains("class") && !content.contains("interface") && !content.contains("enum")) {
            throw new ValidationException(fieldName + " does not contain valid Java code structure");
        }
        
        // Check for suspicious imports
        validateImports(content, fieldName);
        
        logger.debug("Java content validation passed for: {}", fieldName);
    }
    
    /**
     * Validates string input for alphanumeric characters only.
     * 
     * @param input the input string
     * @param fieldName the field name for error reporting
     * @param maxLength maximum allowed length
     * @throws ValidationException if validation fails
     */
    public void validateAlphanumeric(String input, String fieldName, int maxLength) {
        if (input == null || input.trim().isEmpty()) {
            throw new ValidationException(fieldName + " cannot be null or empty");
        }
        
        String trimmed = input.trim();
        
        if (trimmed.length() > maxLength) {
            throw new ValidationException(fieldName + " exceeds maximum length of " + maxLength);
        }
        
        if (!ALPHANUMERIC_PATTERN.matcher(trimmed).matches()) {
            throw new ValidationException(fieldName + " can only contain alphanumeric characters, underscores, and hyphens");
        }
        
        logger.debug("Alphanumeric validation passed for: {}", fieldName);
    }
    
    /**
     * Validates safe text input (allows spaces and basic punctuation).
     * 
     * @param input the input string
     * @param fieldName the field name for error reporting
     * @param maxLength maximum allowed length
     * @throws ValidationException if validation fails
     */
    public void validateSafeText(String input, String fieldName, int maxLength) {
        if (input != null) {
            String trimmed = input.trim();
            
            if (trimmed.length() > maxLength) {
                throw new ValidationException(fieldName + " exceeds maximum length of " + maxLength);
            }
            
            if (!SAFE_TEXT_PATTERN.matcher(trimmed).matches()) {
                throw new ValidationException(fieldName + " contains invalid characters");
            }
        }
        
        logger.debug("Safe text validation passed for: {}", fieldName);
    }
    
    /**
     * Validates email format.
     * 
     * @param email the email string
     * @param fieldName the field name for error reporting
     * @throws ValidationException if validation fails
     */
    public void validateEmail(String email, String fieldName) {
        if (email != null && !email.trim().isEmpty()) {
            String trimmed = email.trim();
            
            if (!EMAIL_PATTERN.matcher(trimmed).matches()) {
                throw new ValidationException(fieldName + " has invalid email format");
            }
        }
        
        logger.debug("Email validation passed for: {}", fieldName);
    }
    
    /**
     * Validates file path for security (prevents path traversal).
     * 
     * @param filePath the file path
     * @param fieldName the field name for error reporting
     * @throws ValidationException if validation fails
     */
    public void validateFilePath(String filePath, String fieldName) {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new ValidationException(fieldName + " cannot be null or empty");
        }
        
        if (containsPathTraversal(filePath)) {
            throw new ValidationException(fieldName + " contains path traversal characters");
        }
        
        // Ensure path is within allowed boundaries
        try {
            Path path = Paths.get(filePath).normalize();
            if (path.isAbsolute()) {
                throw new ValidationException(fieldName + " cannot be an absolute path");
            }
        } catch (Exception e) {
            throw new ValidationException(fieldName + " is not a valid path", e);
        }
        
        logger.debug("File path validation passed for: {}", fieldName);
    }
    
    /**
     * Sanitizes string input by removing potentially dangerous characters.
     * 
     * @param input the input string
     * @return sanitized string
     */
    public String sanitizeString(String input) {
        if (input == null) {
            return null;
        }
        
        return input.trim()
                   .replaceAll("[<>\"'&]", "") // Remove HTML/XML characters
                   .replaceAll("[\r\n\t]", " ") // Replace line breaks with spaces
                   .replaceAll("\\s+", " "); // Normalize whitespace
    }
    
    /**
     * Validates that a string is a valid identifier (for IDs, names, etc.).
     * 
     * @param identifier the identifier string
     * @param fieldName the field name for error reporting
     * @param maxLength maximum allowed length
     * @throws ValidationException if validation fails
     */
    public void validateIdentifier(String identifier, String fieldName, int maxLength) {
        validateAlphanumeric(identifier, fieldName, maxLength);
        
        // Additional checks for identifiers
        if (identifier.startsWith("-") || identifier.startsWith("_")) {
            throw new ValidationException(fieldName + " cannot start with hyphen or underscore");
        }
        
        if (identifier.endsWith("-") || identifier.endsWith("_")) {
            throw new ValidationException(fieldName + " cannot end with hyphen or underscore");
        }
    }
    
    // Private helper methods
    
    private boolean hasAllowedExtension(String filename) {
        String lowerFilename = filename.toLowerCase();
        return ALLOWED_EXTENSIONS.stream().anyMatch(lowerFilename::endsWith);
    }
    
    private boolean containsPathTraversal(String input) {
        return PATH_TRAVERSAL_PATTERN.matcher(input).matches();
    }
    
    private void validateImports(String content, String fieldName) {
        String[] lines = content.split("\n");
        
        for (String line : lines) {
            String trimmedLine = line.trim();
            if (trimmedLine.startsWith("import ")) {
                // Check for dangerous imports
                if (trimmedLine.contains("java.lang.reflect") ||
                    trimmedLine.contains("java.io.File") ||
                    trimmedLine.contains("java.net.") ||
                    trimmedLine.contains("javax.script") ||
                    trimmedLine.contains("sun.misc")) {
                    
                    logger.warn("Suspicious import detected in {}: {}", fieldName, trimmedLine);
                    throw new ValidationException(fieldName + " contains suspicious import: " + trimmedLine);
                }
            }
        }
    }
}