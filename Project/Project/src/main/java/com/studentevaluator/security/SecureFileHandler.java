package com.studentevaluator.security;

import com.studentevaluator.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.SecureRandom;
import java.util.Set;

/**
 * Secure file handling component that provides safe file operations
 * with proper permissions and sandboxing.
 */
@Component
public class SecureFileHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(SecureFileHandler.class);
    
    @Value("${evaluation.temp-directory:${java.io.tmpdir}/evaluations}")
    private String tempDirectory;
    
    @Value("${security.file.max-size:1048576}")
    private long maxFileSize;
    
    private final SecureRandom secureRandom = new SecureRandom();
    private final InputValidator inputValidator;
    
    // Secure file permissions (owner read/write only)
    private static final Set<PosixFilePermission> SECURE_FILE_PERMISSIONS = 
        PosixFilePermissions.fromString("rw-------");
    
    // Secure directory permissions (owner read/write/execute only)
    private static final Set<PosixFilePermission> SECURE_DIR_PERMISSIONS = 
        PosixFilePermissions.fromString("rwx------");
    
    public SecureFileHandler(InputValidator inputValidator) {
        this.inputValidator = inputValidator;
    }
    
    /**
     * Securely stores an uploaded file with proper validation and permissions.
     * 
     * @param file the uploaded file
     * @param relativePath the relative path within the temp directory
     * @param filename the target filename
     * @return the secure file path
     * @throws ValidationException if validation fails
     * @throws IOException if file operations fail
     */
    public Path storeFileSecurely(MultipartFile file, String relativePath, String filename) 
            throws IOException {
        
        // Validate inputs
        inputValidator.validateJavaFile(file, "uploaded file");
        inputValidator.validateFilePath(relativePath, "relative path");
        validateFilename(filename);
        
        // Create secure directory structure
        Path targetDir = createSecureDirectory(relativePath);
        
        // Generate secure filename if needed
        String secureFilename = sanitizeFilename(filename);
        Path targetPath = targetDir.resolve(secureFilename);
        
        // Ensure we're not overwriting existing files
        if (Files.exists(targetPath)) {
            secureFilename = generateUniqueFilename(secureFilename);
            targetPath = targetDir.resolve(secureFilename);
        }
        
        // Store file with secure permissions
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        setSecureFilePermissions(targetPath);
        
        logger.info("Securely stored file: {} at path: {}", filename, targetPath);
        return targetPath;
    }
    
    /**
     * Creates a secure temporary directory with restricted permissions.
     * 
     * @param relativePath the relative path within temp directory
     * @return the created directory path
     * @throws IOException if directory creation fails
     */
    public Path createSecureDirectory(String relativePath) throws IOException {
        inputValidator.validateFilePath(relativePath, "directory path");
        
        Path basePath = Paths.get(tempDirectory).normalize();
        Path targetPath = basePath.resolve(relativePath).normalize();
        
        // Ensure the target path is within the allowed base directory
        if (!targetPath.startsWith(basePath)) {
            throw new ValidationException("Directory path escapes allowed boundaries");
        }
        
        // Create directory with secure permissions
        Files.createDirectories(targetPath);
        setSecureDirectoryPermissions(targetPath);
        
        logger.debug("Created secure directory: {}", targetPath);
        return targetPath;
    }
    
    /**
     * Securely deletes a file or directory.
     * 
     * @param path the path to delete
     * @throws IOException if deletion fails
     */
    public void secureDelete(Path path) throws IOException {
        if (path == null || !Files.exists(path)) {
            return;
        }
        
        // Ensure path is within allowed boundaries
        Path basePath = Paths.get(tempDirectory).normalize();
        Path normalizedPath = path.normalize();
        
        if (!normalizedPath.startsWith(basePath)) {
            throw new ValidationException("Cannot delete file outside allowed directory");
        }
        
        if (Files.isDirectory(path)) {
            // Recursively delete directory contents
            Files.walk(path)
                 .sorted((a, b) -> b.compareTo(a)) // Delete files before directories
                 .forEach(p -> {
                     try {
                         Files.delete(p);
                     } catch (IOException e) {
                         logger.warn("Failed to delete: {}", p, e);
                     }
                 });
        } else {
            Files.delete(path);
        }
        
        logger.debug("Securely deleted: {}", path);
    }
    
    /**
     * Creates a sandboxed execution directory for compilation and testing.
     * 
     * @param identifier unique identifier for the sandbox
     * @return the sandbox directory path
     * @throws IOException if creation fails
     */
    public Path createSandboxDirectory(String identifier) throws IOException {
        inputValidator.validateIdentifier(identifier, "sandbox identifier", 50);
        
        String sandboxName = "sandbox_" + identifier + "_" + System.currentTimeMillis();
        Path sandboxPath = createSecureDirectory("sandboxes/" + sandboxName);
        
        // Create subdirectories for different purposes
        createSecureDirectory("sandboxes/" + sandboxName + "/src");
        createSecureDirectory("sandboxes/" + sandboxName + "/classes");
        createSecureDirectory("sandboxes/" + sandboxName + "/tests");
        createSecureDirectory("sandboxes/" + sandboxName + "/output");
        
        logger.info("Created sandbox directory: {}", sandboxPath);
        return sandboxPath;
    }
    
    /**
     * Validates that a file is safe to read/execute.
     * 
     * @param filePath the file path to validate
     * @throws ValidationException if file is not safe
     */
    public void validateFileForExecution(Path filePath) {
        if (filePath == null || !Files.exists(filePath)) {
            throw new ValidationException("File does not exist or is null");
        }
        
        // Ensure file is within allowed boundaries
        Path basePath = Paths.get(tempDirectory).normalize();
        Path normalizedPath = filePath.normalize();
        
        if (!normalizedPath.startsWith(basePath)) {
            throw new ValidationException("File is outside allowed execution directory");
        }
        
        // Check file size
        try {
            long fileSize = Files.size(filePath);
            if (fileSize > maxFileSize) {
                throw new ValidationException("File size exceeds maximum allowed size");
            }
        } catch (IOException e) {
            throw new ValidationException("Unable to check file size", e);
        }
        
        // Validate file extension
        String filename = filePath.getFileName().toString();
        if (!filename.toLowerCase().endsWith(".java") && !filename.toLowerCase().endsWith(".class")) {
            throw new ValidationException("File type not allowed for execution");
        }
        
        logger.debug("File validation passed for execution: {}", filePath);
    }
    
    /**
     * Gets the secure base directory for file operations.
     * 
     * @return the base directory path
     */
    public Path getSecureBaseDirectory() {
        return Paths.get(tempDirectory).normalize();
    }
    
    /**
     * Checks if a path is within the secure boundaries.
     * 
     * @param path the path to check
     * @return true if path is within boundaries
     */
    public boolean isWithinSecureBoundaries(Path path) {
        Path basePath = getSecureBaseDirectory();
        Path normalizedPath = path.normalize();
        return normalizedPath.startsWith(basePath);
    }
    
    // Private helper methods
    
    private void validateFilename(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            throw new ValidationException("Filename cannot be null or empty");
        }
        
        // Check for dangerous characters
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            throw new ValidationException("Filename contains invalid characters");
        }
        
        // Check length
        if (filename.length() > 255) {
            throw new ValidationException("Filename too long");
        }
    }
    
    private String sanitizeFilename(String filename) {
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
    
    private String generateUniqueFilename(String originalFilename) {
        String baseName = originalFilename;
        String extension = "";
        
        int lastDot = originalFilename.lastIndexOf('.');
        if (lastDot > 0) {
            baseName = originalFilename.substring(0, lastDot);
            extension = originalFilename.substring(lastDot);
        }
        
        String uniqueId = String.valueOf(Math.abs(secureRandom.nextInt()));
        return baseName + "_" + uniqueId + extension;
    }
    
    private void setSecureFilePermissions(Path filePath) {
        try {
            if (FileSystems.getDefault().supportedFileAttributeViews().contains("posix")) {
                Files.setPosixFilePermissions(filePath, SECURE_FILE_PERMISSIONS);
            } else {
                // For non-POSIX systems (Windows), set basic permissions
                File file = filePath.toFile();
                file.setReadable(true, true);  // Owner only
                file.setWritable(true, true);  // Owner only
                file.setExecutable(false);     // No execute
            }
        } catch (IOException e) {
            logger.warn("Failed to set secure permissions for file: {}", filePath, e);
        }
    }
    
    private void setSecureDirectoryPermissions(Path dirPath) {
        try {
            if (FileSystems.getDefault().supportedFileAttributeViews().contains("posix")) {
                Files.setPosixFilePermissions(dirPath, SECURE_DIR_PERMISSIONS);
            } else {
                // For non-POSIX systems (Windows), set basic permissions
                File dir = dirPath.toFile();
                dir.setReadable(true, true);   // Owner only
                dir.setWritable(true, true);   // Owner only
                dir.setExecutable(true, true); // Owner only (needed for directory access)
            }
        } catch (IOException e) {
            logger.warn("Failed to set secure permissions for directory: {}", dirPath, e);
        }
    }
}