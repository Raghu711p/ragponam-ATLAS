package com.studentevaluator.service;

import com.studentevaluator.exception.AssignmentNotFoundException;
import com.studentevaluator.exception.FileUploadException;
import com.studentevaluator.model.Assignment;
import com.studentevaluator.repository.AssignmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service class for handling assignment file uploads, validation, and management.
 * Provides functionality for Java file uploads, test file association, and file storage management.
 */
@Service
@Transactional
public class AssignmentService {
    
    private static final Logger logger = LoggerFactory.getLogger(AssignmentService.class);
    
    private final AssignmentRepository assignmentRepository;
    
    @Value("${evaluation.temp-directory:${java.io.tmpdir}/evaluations}")
    private String tempDirectory;
    
    @Value("${evaluation.max-file-size:1048576}")
    private long maxFileSize;
    
    private static final String ASSIGNMENTS_DIR = "assignments";
    private static final String TESTS_DIR = "tests";
    private static final String JAVA_FILE_EXTENSION = ".java";
    
    @Autowired
    public AssignmentService(AssignmentRepository assignmentRepository) {
        this.assignmentRepository = assignmentRepository;
    }
    
    /**
     * Upload and store a Java assignment file.
     * 
     * @param file the Java file to upload
     * @param assignmentId the unique assignment identifier
     * @param title the assignment title
     * @param description optional assignment description
     * @return the created Assignment entity
     * @throws FileUploadException if file validation or storage fails
     */
    @CacheEvict(value = {"assignments", "allAssignments"}, allEntries = true)
    public Assignment uploadAssignmentFile(MultipartFile file, String assignmentId, 
                                         String title, String description) {
        logger.info("Uploading assignment file for assignment ID: {}", assignmentId);
        
        try {
            // Validate file
            validateJavaFile(file);
            
            // Check if assignment already exists
            if (assignmentRepository.existsById(assignmentId)) {
                throw new FileUploadException("Assignment with ID " + assignmentId + " already exists");
            }
            
            // Create assignment directory structure
            Path assignmentDir = createAssignmentDirectory(assignmentId);
            
            // Store the file
            String storedFilePath = storeFile(file, assignmentDir, assignmentId + JAVA_FILE_EXTENSION);
            
            // Create and save assignment entity
            Assignment assignment = new Assignment(assignmentId, title, description, null);
            assignment = assignmentRepository.save(assignment);
            
            logger.info("Successfully uploaded assignment file: {} for assignment: {}", 
                       storedFilePath, assignmentId);
            
            return assignment;
            
        } catch (IOException e) {
            logger.error("Failed to upload assignment file for ID: {}", assignmentId, e);
            throw new FileUploadException("Failed to store assignment file", e);
        }
    }
    
    /**
     * Upload and associate test files with an existing assignment.
     * 
     * @param testFile the JUnit test file to upload
     * @param assignmentId the assignment ID to associate with
     * @return the updated Assignment entity
     * @throws AssignmentNotFoundException if assignment not found
     * @throws FileUploadException if file validation or storage fails
     */
    public Assignment uploadTestFile(MultipartFile testFile, String assignmentId) {
        logger.info("Uploading test file for assignment ID: {}", assignmentId);
        
        try {
            // Validate test file
            validateJavaFile(testFile);
            validateTestFileContent(testFile);
            
            // Find existing assignment
            Assignment assignment = assignmentRepository.findById(assignmentId)
                    .orElseThrow(() -> new AssignmentNotFoundException(assignmentId));
            
            // Create test directory if it doesn't exist
            Path testDir = createTestDirectory(assignmentId);
            
            // Store the test file
            String testFileName = generateTestFileName(testFile.getOriginalFilename());
            String storedTestPath = storeFile(testFile, testDir, testFileName);
            
            // Update assignment with test file path
            assignment.setTestFilePath(storedTestPath);
            assignment = assignmentRepository.save(assignment);
            
            logger.info("Successfully uploaded test file: {} for assignment: {}", 
                       storedTestPath, assignmentId);
            
            return assignment;
            
        } catch (IOException e) {
            logger.error("Failed to upload test file for assignment ID: {}", assignmentId, e);
            throw new FileUploadException("Failed to store test file", e);
        }
    }    

    /**
     * Retrieve assignment by ID.
     * 
     * @param assignmentId the assignment ID
     * @return Optional containing the assignment if found
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "assignments", key = "#assignmentId")
    public Optional<Assignment> getAssignment(String assignmentId) {
        logger.debug("Retrieving assignment with ID: {}", assignmentId);
        return assignmentRepository.findById(assignmentId);
    }
    
    /**
     * Retrieve all assignments.
     * 
     * @return list of all assignments
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "allAssignments")
    public List<Assignment> getAllAssignments() {
        logger.debug("Retrieving all assignments");
        return assignmentRepository.findAll();
    }
    
    /**
     * Search assignments by title pattern.
     * 
     * @param titlePattern the title pattern to search for
     * @return list of matching assignments
     */
    @Transactional(readOnly = true)
    public List<Assignment> searchAssignmentsByTitle(String titlePattern) {
        logger.debug("Searching assignments by title pattern: {}", titlePattern);
        return assignmentRepository.findByTitleContainingIgnoreCase(titlePattern);
    }
    
    /**
     * Get assignments created after a specific date.
     * 
     * @param date the date to filter by
     * @return list of assignments created after the date
     */
    @Transactional(readOnly = true)
    public List<Assignment> getAssignmentsCreatedAfter(LocalDateTime date) {
        logger.debug("Retrieving assignments created after: {}", date);
        return assignmentRepository.findByCreatedAtAfter(date);
    }
    
    /**
     * Get assignments that have test files associated.
     * 
     * @return list of assignments with test files
     */
    @Transactional(readOnly = true)
    public List<Assignment> getAssignmentsWithTests() {
        logger.debug("Retrieving assignments with test files");
        return assignmentRepository.findByTestFilePathIsNotNull();
    }
    
    /**
     * Update assignment metadata.
     * 
     * @param assignmentId the assignment ID
     * @param title new title (optional)
     * @param description new description (optional)
     * @return the updated assignment
     * @throws AssignmentNotFoundException if assignment not found
     */
    @CacheEvict(value = {"assignments", "allAssignments"}, key = "#assignmentId")
    public Assignment updateAssignmentMetadata(String assignmentId, String title, String description) {
        logger.info("Updating metadata for assignment ID: {}", assignmentId);
        
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new AssignmentNotFoundException(assignmentId));
        
        if (title != null && !title.trim().isEmpty()) {
            assignment.setTitle(title.trim());
        }
        
        if (description != null) {
            assignment.setDescription(description.trim().isEmpty() ? null : description.trim());
        }
        
        assignment = assignmentRepository.save(assignment);
        logger.info("Successfully updated metadata for assignment: {}", assignmentId);
        
        return assignment;
    }
    
    /**
     * Delete an assignment and its associated files.
     * 
     * @param assignmentId the assignment ID to delete
     * @throws AssignmentNotFoundException if assignment not found
     */
    public void deleteAssignment(String assignmentId) {
        logger.info("Deleting assignment with ID: {}", assignmentId);
        
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new AssignmentNotFoundException(assignmentId));
        
        // Clean up associated files
        cleanupAssignmentFiles(assignmentId);
        
        // Delete from database
        assignmentRepository.delete(assignment);
        
        logger.info("Successfully deleted assignment: {}", assignmentId);
    }
    
    /**
     * Remove test file association from an assignment.
     * 
     * @param assignmentId the assignment ID
     * @return the updated assignment
     * @throws AssignmentNotFoundException if assignment not found
     */
    public Assignment removeTestFileAssociation(String assignmentId) {
        logger.info("Removing test file association for assignment ID: {}", assignmentId);
        
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new AssignmentNotFoundException(assignmentId));
        
        // Clean up test files
        if (assignment.getTestFilePath() != null) {
            cleanupTestFiles(assignmentId);
            assignment.setTestFilePath(null);
            assignment = assignmentRepository.save(assignment);
        }
        
        logger.info("Successfully removed test file association for assignment: {}", assignmentId);
        return assignment;
    }
    
    /**
     * Get the file path for an assignment's Java file.
     * 
     * @param assignmentId the assignment ID
     * @return the file path if it exists
     */
    public Optional<String> getAssignmentFilePath(String assignmentId) {
        Path assignmentDir = Paths.get(tempDirectory, ASSIGNMENTS_DIR, assignmentId);
        Path javaFile = assignmentDir.resolve(assignmentId + JAVA_FILE_EXTENSION);
        
        if (Files.exists(javaFile)) {
            return Optional.of(javaFile.toString());
        }
        
        return Optional.empty();
    }
    
    /**
     * Check if an assignment has a test file associated.
     * 
     * @param assignmentId the assignment ID
     * @return true if assignment has test file
     */
    @Transactional(readOnly = true)
    public boolean hasTestFile(String assignmentId) {
        return assignmentRepository.hasTestFile(assignmentId);
    }
    
    // Private helper methods
    
    private void validateJavaFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FileUploadException("File cannot be null or empty");
        }
        
        if (file.getSize() > maxFileSize) {
            throw new FileUploadException("File size exceeds maximum allowed size: " + maxFileSize + " bytes");
        }
        
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(JAVA_FILE_EXTENSION)) {
            throw new FileUploadException("File must be a Java file with .java extension");
        }
        
        // Basic content validation
        try {
            String content = new String(file.getBytes());
            if (content.trim().isEmpty()) {
                throw new FileUploadException("File content cannot be empty");
            }
            
            // Check for basic Java class structure
            if (!content.contains("class") && !content.contains("interface") && !content.contains("enum")) {
                throw new FileUploadException("File does not appear to contain valid Java code");
            }
            
        } catch (IOException e) {
            throw new FileUploadException("Unable to read file content", e);
        }
    }
    
    private void validateTestFileContent(MultipartFile testFile) {
        try {
            String content = new String(testFile.getBytes());
            
            // Check for JUnit annotations or imports
            boolean hasJUnitImports = content.contains("import org.junit") || 
                                    content.contains("import static org.junit");
            boolean hasTestAnnotations = content.contains("@Test") || 
                                       content.contains("@BeforeEach") || 
                                       content.contains("@AfterEach");
            
            if (!hasJUnitImports && !hasTestAnnotations) {
                throw new FileUploadException("File does not appear to be a valid JUnit test file");
            }
            
        } catch (IOException e) {
            throw new FileUploadException("Unable to validate test file content", e);
        }
    }
    
    private Path createAssignmentDirectory(String assignmentId) throws IOException {
        Path assignmentDir = Paths.get(tempDirectory, ASSIGNMENTS_DIR, assignmentId);
        Files.createDirectories(assignmentDir);
        logger.debug("Created assignment directory: {}", assignmentDir);
        return assignmentDir;
    }
    
    private Path createTestDirectory(String assignmentId) throws IOException {
        Path testDir = Paths.get(tempDirectory, TESTS_DIR, assignmentId);
        Files.createDirectories(testDir);
        logger.debug("Created test directory: {}", testDir);
        return testDir;
    }
    
    private String storeFile(MultipartFile file, Path directory, String fileName) throws IOException {
        Path targetPath = directory.resolve(fileName);
        
        // Ensure the target directory exists
        Files.createDirectories(directory);
        
        // Copy file to target location
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        
        logger.debug("Stored file: {} to path: {}", fileName, targetPath);
        return targetPath.toString();
    }
    
    private String generateTestFileName(String originalFilename) {
        if (originalFilename == null) {
            return "Test_" + UUID.randomUUID().toString() + JAVA_FILE_EXTENSION;
        }
        
        // Remove extension and add Test suffix if not present
        String baseName = originalFilename.replaceAll("\\.java$", "");
        if (!baseName.toLowerCase().contains("test")) {
            baseName += "Test";
        }
        
        return baseName + JAVA_FILE_EXTENSION;
    }
    
    private void cleanupAssignmentFiles(String assignmentId) {
        try {
            // Clean up assignment files
            Path assignmentDir = Paths.get(tempDirectory, ASSIGNMENTS_DIR, assignmentId);
            if (Files.exists(assignmentDir)) {
                Files.walk(assignmentDir)
                     .sorted((a, b) -> b.compareTo(a)) // Delete files before directories
                     .forEach(path -> {
                         try {
                             Files.delete(path);
                         } catch (IOException e) {
                             logger.warn("Failed to delete file: {}", path, e);
                         }
                     });
            }
            
            // Clean up test files
            cleanupTestFiles(assignmentId);
            
        } catch (IOException e) {
            logger.warn("Failed to cleanup files for assignment: {}", assignmentId, e);
        }
    }
    
    private void cleanupTestFiles(String assignmentId) {
        try {
            Path testDir = Paths.get(tempDirectory, TESTS_DIR, assignmentId);
            if (Files.exists(testDir)) {
                Files.walk(testDir)
                     .sorted((a, b) -> b.compareTo(a)) // Delete files before directories
                     .forEach(path -> {
                         try {
                             Files.delete(path);
                         } catch (IOException e) {
                             logger.warn("Failed to delete test file: {}", path, e);
                         }
                     });
            }
        } catch (IOException e) {
            logger.warn("Failed to cleanup test files for assignment: {}", assignmentId, e);
        }
    }
}