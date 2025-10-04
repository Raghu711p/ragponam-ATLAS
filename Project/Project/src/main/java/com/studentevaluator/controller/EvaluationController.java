package com.studentevaluator.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.studentevaluator.dto.AssignmentResponse;
import com.studentevaluator.dto.AssignmentUploadRequest;
import com.studentevaluator.dto.ErrorResponse;
import com.studentevaluator.dto.EvaluationRequest;
import com.studentevaluator.dto.EvaluationResponse;
import com.studentevaluator.exception.AssignmentNotFoundException;
import com.studentevaluator.exception.FileUploadException;
import com.studentevaluator.exception.ValidationException;
import com.studentevaluator.model.Assignment;
import com.studentevaluator.model.EvaluationResult;
import com.studentevaluator.security.InputValidator;
import com.studentevaluator.security.SecureFileHandler;
import com.studentevaluator.service.AssignmentService;
import com.studentevaluator.service.EvaluationService;

import jakarta.validation.Valid;

/**
 * REST controller for evaluation operations including assignment upload, test upload, and evaluation triggering.
 */
@RestController
@RequestMapping("/api/v1/evaluations")
@CrossOrigin(origins = "*")
public class EvaluationController {
    
    private static final Logger logger = LoggerFactory.getLogger(EvaluationController.class);
    
    private final AssignmentService assignmentService;
    private final EvaluationService evaluationService;
    private final InputValidator inputValidator;
    private final SecureFileHandler secureFileHandler;
    
    @Value("${evaluation.temp-directory:${java.io.tmpdir}/evaluations}")
    private String tempDirectory;
    
    @Autowired
    public EvaluationController(AssignmentService assignmentService, EvaluationService evaluationService,
                               InputValidator inputValidator, SecureFileHandler secureFileHandler) {
        this.assignmentService = assignmentService;
        this.evaluationService = evaluationService;
        this.inputValidator = inputValidator;
        this.secureFileHandler = secureFileHandler;
    }
    
    /**
     * Upload assignment file.
     * POST /api/v1/evaluations/assignments
     */
    @PostMapping("/assignments")
    public ResponseEntity<?> uploadAssignment(
            @RequestParam("file") MultipartFile file,
            @Valid @ModelAttribute AssignmentUploadRequest request) {
        
        logger.info("Uploading assignment: {} with title: {}", request.getAssignmentId(), request.getTitle());
        
        try {
            // Security validation
            inputValidator.validateJavaFile(file, "assignment file");
            inputValidator.validateIdentifier(request.getAssignmentId(), "assignment ID", 50);
            inputValidator.validateSafeText(request.getTitle(), "title", 200);
            inputValidator.validateSafeText(request.getDescription(), "description", 1000);
            
            Assignment assignment = assignmentService.uploadAssignmentFile(
                file, request.getAssignmentId(), request.getTitle(), request.getDescription());
            
            AssignmentResponse response = new AssignmentResponse(assignment);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (ValidationException e) {
            logger.error("Validation failed for assignment: {}", request.getAssignmentId(), e);
            ErrorResponse error = new ErrorResponse("VALIDATION_ERROR", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            
        } catch (FileUploadException e) {
            logger.error("File upload failed for assignment: {}", request.getAssignmentId(), e);
            ErrorResponse error = new ErrorResponse("FILE_UPLOAD_ERROR", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            
        } catch (Exception e) {
            logger.error("Unexpected error uploading assignment: {}", request.getAssignmentId(), e);
            ErrorResponse error = new ErrorResponse("INTERNAL_ERROR", "An unexpected error occurred");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Upload test file for an assignment.
     * POST /api/v1/evaluations/assignments/{assignmentId}/tests
     */
    @PostMapping("/assignments/{assignmentId}/tests")
    public ResponseEntity<?> uploadTestFile(
            @PathVariable String assignmentId,
            @RequestParam("file") MultipartFile file) {
        
        logger.info("Uploading test file for assignment: {}", assignmentId);
        
        try {
            // Security validation
            inputValidator.validateIdentifier(assignmentId, "assignment ID", 50);
            inputValidator.validateJavaFile(file, "test file");
            
            Assignment assignment = assignmentService.uploadTestFile(file, assignmentId);
            AssignmentResponse response = new AssignmentResponse(assignment);
            return ResponseEntity.ok(response);
            
        } catch (ValidationException e) {
            logger.error("Validation failed for test file upload: {}", assignmentId, e);
            ErrorResponse error = new ErrorResponse("VALIDATION_ERROR", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            
        } catch (AssignmentNotFoundException e) {
            logger.error("Assignment not found: {}", assignmentId, e);
            ErrorResponse error = new ErrorResponse("ASSIGNMENT_NOT_FOUND", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            
        } catch (FileUploadException e) {
            logger.error("Test file upload failed for assignment: {}", assignmentId, e);
            ErrorResponse error = new ErrorResponse("FILE_UPLOAD_ERROR", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            
        } catch (Exception e) {
            logger.error("Unexpected error uploading test file for assignment: {}", assignmentId, e);
            ErrorResponse error = new ErrorResponse("INTERNAL_ERROR", "An unexpected error occurred");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Get all assignments.
     * GET /api/v1/evaluations/assignments
     */
    @GetMapping("/assignments")
    public ResponseEntity<List<AssignmentResponse>> getAllAssignments() {
        logger.debug("Retrieving all assignments");
        
        try {
            List<Assignment> assignments = assignmentService.getAllAssignments();
            List<AssignmentResponse> responses = assignments.stream()
                    .map(AssignmentResponse::new)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(responses);
            
        } catch (Exception e) {
            logger.error("Error retrieving assignments", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get assignment by ID.
     * GET /api/v1/evaluations/assignments/{assignmentId}
     */
    @GetMapping("/assignments/{assignmentId}")
    public ResponseEntity<?> getAssignment(@PathVariable String assignmentId) {
        logger.debug("Retrieving assignment: {}", assignmentId);
        
        try {
            // Security validation
            inputValidator.validateIdentifier(assignmentId, "assignment ID", 50);
            
            Optional<Assignment> assignment = assignmentService.getAssignment(assignmentId);
            
            if (assignment.isPresent()) {
                AssignmentResponse response = new AssignmentResponse(assignment.get());
                return ResponseEntity.ok(response);
            } else {
                ErrorResponse error = new ErrorResponse("ASSIGNMENT_NOT_FOUND", 
                    "Assignment not found with ID: " + assignmentId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            
        } catch (ValidationException e) {
            logger.error("Validation failed for assignment retrieval: {}", assignmentId, e);
            ErrorResponse error = new ErrorResponse("VALIDATION_ERROR", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            
        } catch (Exception e) {
            logger.error("Error retrieving assignment: {}", assignmentId, e);
            ErrorResponse error = new ErrorResponse("INTERNAL_ERROR", "An unexpected error occurred");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Trigger evaluation for a student submission.
     * POST /api/v1/evaluations
     */
    @PostMapping
    public ResponseEntity<?> triggerEvaluation(
            @RequestParam("file") MultipartFile submissionFile,
            @Valid @ModelAttribute EvaluationRequest request) {
        
        logger.info("Triggering evaluation for student: {} assignment: {}", 
                   request.getStudentId(), request.getAssignmentId());
        
        try {
            // Security validation
            inputValidator.validateJavaFile(submissionFile, "submission file");
            inputValidator.validateIdentifier(request.getStudentId(), "student ID", 50);
            inputValidator.validateIdentifier(request.getAssignmentId(), "assignment ID", 50);
            
            // Validate assignment exists
            Optional<Assignment> assignment = assignmentService.getAssignment(request.getAssignmentId());
            if (!assignment.isPresent()) {
                ErrorResponse error = new ErrorResponse("ASSIGNMENT_NOT_FOUND", 
                    "Assignment not found with ID: " + request.getAssignmentId());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            
            // Store submission file securely
            Path tempSubmissionPath = secureFileHandler.storeFileSecurely(
                submissionFile, 
                "submissions/" + request.getStudentId() + "/" + request.getAssignmentId(),
                submissionFile.getOriginalFilename()
            );
            
            // Create secure sandbox for evaluation
            Path sandboxPath = secureFileHandler.createSandboxDirectory(
                request.getStudentId() + "_" + request.getAssignmentId()
            );
            
            // Trigger evaluation
            EvaluationResult result = evaluationService.evaluateAssignment(
                request.getStudentId(), request.getAssignmentId(), 
                tempSubmissionPath.toFile(), sandboxPath.toString());
            
            EvaluationResponse response = new EvaluationResponse(result);
            
            // Clean up temporary files
            secureFileHandler.secureDelete(tempSubmissionPath);
            secureFileHandler.secureDelete(sandboxPath);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (ValidationException e) {
            logger.error("Validation failed for evaluation: student {} assignment {}", 
                        request.getStudentId(), request.getAssignmentId(), e);
            ErrorResponse error = new ErrorResponse("VALIDATION_ERROR", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            
        } catch (AssignmentNotFoundException e) {
            logger.error("Assignment not found: {}", request.getAssignmentId(), e);
            ErrorResponse error = new ErrorResponse("ASSIGNMENT_NOT_FOUND", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            
        } catch (FileUploadException e) {
            logger.error("Submission file error for student: {} assignment: {}", 
                        request.getStudentId(), request.getAssignmentId(), e);
            ErrorResponse error = new ErrorResponse("FILE_UPLOAD_ERROR", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            
        } catch (Exception e) {
            logger.error("Evaluation failed for student: {} assignment: {}", 
                        request.getStudentId(), request.getAssignmentId(), e);
            ErrorResponse error = new ErrorResponse("EVALUATION_ERROR", "Evaluation failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Trigger asynchronous evaluation for a student submission.
     * POST /api/v1/evaluations/async
     */
    @PostMapping("/async")
    public ResponseEntity<?> triggerAsyncEvaluation(
            @RequestParam("file") MultipartFile submissionFile,
            @Valid @ModelAttribute EvaluationRequest request) {
        
        logger.info("Triggering async evaluation for student: {} assignment: {}", 
                   request.getStudentId(), request.getAssignmentId());
        
        try {
            // Validate assignment exists
            Optional<Assignment> assignment = assignmentService.getAssignment(request.getAssignmentId());
            if (!assignment.isPresent()) {
                ErrorResponse error = new ErrorResponse("ASSIGNMENT_NOT_FOUND", 
                    "Assignment not found with ID: " + request.getAssignmentId());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            
            // Store submission file temporarily
            File tempSubmissionFile = storeTemporarySubmissionFile(submissionFile, 
                request.getStudentId(), request.getAssignmentId());
            
            // Create output directory for compilation
            String outputDirectory = createOutputDirectory(request.getStudentId(), request.getAssignmentId());
            
            // Trigger async evaluation
            CompletableFuture<EvaluationResult> futureResult = evaluationService.evaluateAssignmentAsync(
                request.getStudentId(), request.getAssignmentId(), tempSubmissionFile, outputDirectory);
            
            // Return immediate response with evaluation ID
            String evaluationId = "eval_" + System.currentTimeMillis();
            EvaluationResponse response = new EvaluationResponse();
            response.setEvaluationId(evaluationId);
            response.setStudentId(request.getStudentId());
            response.setAssignmentId(request.getAssignmentId());
            response.setStatus(com.studentevaluator.model.EvaluationStatus.IN_PROGRESS);
            
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
            
        } catch (Exception e) {
            logger.error("Async evaluation failed for student: {} assignment: {}", 
                        request.getStudentId(), request.getAssignmentId(), e);
            ErrorResponse error = new ErrorResponse("EVALUATION_ERROR", "Async evaluation failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Get evaluation result by ID.
     * GET /api/v1/evaluations/{evaluationId}
     */
    @GetMapping("/{evaluationId}")
    public ResponseEntity<?> getEvaluationResult(@PathVariable String evaluationId) {
        logger.debug("Retrieving evaluation result: {}", evaluationId);
        
        try {
            Optional<EvaluationResult> result = evaluationService.getEvaluationResult(evaluationId);
            
            if (result.isPresent()) {
                EvaluationResponse response = new EvaluationResponse(result.get());
                return ResponseEntity.ok(response);
            } else {
                ErrorResponse error = new ErrorResponse("EVALUATION_NOT_FOUND", 
                    "Evaluation not found with ID: " + evaluationId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            
        } catch (Exception e) {
            logger.error("Error retrieving evaluation result: {}", evaluationId, e);
            ErrorResponse error = new ErrorResponse("INTERNAL_ERROR", "An unexpected error occurred");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    // Private helper methods
    
    private File storeTemporarySubmissionFile(MultipartFile file, String studentId, String assignmentId) 
            throws IOException {
        
        // Validate file
        if (file == null || file.isEmpty()) {
            throw new FileUploadException("Submission file cannot be null or empty");
        }
        
        if (!file.getOriginalFilename().toLowerCase().endsWith(".java")) {
            throw new FileUploadException("Submission file must be a Java file");
        }
        
        // Create temporary directory structure
        Path submissionDir = Paths.get(tempDirectory, "submissions", studentId, assignmentId);
        Files.createDirectories(submissionDir);
        
        // Store file
        String fileName = file.getOriginalFilename();
        Path targetPath = submissionDir.resolve(fileName);
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        
        logger.debug("Stored temporary submission file: {}", targetPath);
        return targetPath.toFile();
    }
    
    private String createOutputDirectory(String studentId, String assignmentId) throws IOException {
        Path outputDir = Paths.get(tempDirectory, "output", studentId, assignmentId);
        Files.createDirectories(outputDir);
        return outputDir.toString();
    }
    
    private void cleanupTemporaryFile(File file) {
        try {
            if (file != null && file.exists()) {
                file.delete();
                logger.debug("Cleaned up temporary file: {}", file.getPath());
            }
        } catch (Exception e) {
            logger.warn("Failed to cleanup temporary file: {}", file.getPath(), e);
        }
    }
}