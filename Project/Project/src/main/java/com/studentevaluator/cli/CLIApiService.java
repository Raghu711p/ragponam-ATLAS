package com.studentevaluator.cli;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studentevaluator.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * Service for making HTTP requests to the REST API from CLI commands.
 */
@Service
public class CLIApiService {
    
    private static final Logger logger = LoggerFactory.getLogger(CLIApiService.class);
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    @Value("${cli.api.base-url:http://localhost:8080}")
    private String baseUrl;
    
    public CLIApiService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Upload assignment file via REST API.
     */
    public AssignmentResponse uploadAssignment(File file, String assignmentId, String title, String description) {
        try {
            String url = baseUrl + "/api/v1/evaluations/assignments";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new FileSystemResource(file));
            body.add("assignmentId", assignmentId);
            body.add("title", title);
            if (description != null) {
                body.add("description", description);
            }
            
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            
            ResponseEntity<AssignmentResponse> response = restTemplate.postForEntity(
                url, requestEntity, AssignmentResponse.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            } else {
                throw new CLIException("Failed to upload assignment: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            logger.error("Error uploading assignment", e);
            throw new CLIException("Failed to upload assignment: " + e.getMessage());
        }
    }
    
    /**
     * Upload test file for assignment via REST API.
     */
    public AssignmentResponse uploadTestFile(File file, String assignmentId) {
        try {
            String url = baseUrl + "/api/v1/evaluations/assignments/" + assignmentId + "/tests";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new FileSystemResource(file));
            
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            
            ResponseEntity<AssignmentResponse> response = restTemplate.postForEntity(
                url, requestEntity, AssignmentResponse.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            } else {
                throw new CLIException("Failed to upload test file: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            logger.error("Error uploading test file", e);
            throw new CLIException("Failed to upload test file: " + e.getMessage());
        }
    }
    
    /**
     * Trigger evaluation via REST API.
     */
    public EvaluationResponse triggerEvaluation(File submissionFile, String studentId, String assignmentId) {
        try {
            String url = baseUrl + "/api/v1/evaluations";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new FileSystemResource(submissionFile));
            body.add("studentId", studentId);
            body.add("assignmentId", assignmentId);
            
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            
            ResponseEntity<EvaluationResponse> response = restTemplate.postForEntity(
                url, requestEntity, EvaluationResponse.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            } else {
                throw new CLIException("Failed to trigger evaluation: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            logger.error("Error triggering evaluation", e);
            throw new CLIException("Failed to trigger evaluation: " + e.getMessage());
        }
    }
    
    /**
     * Get student scores via REST API.
     */
    public StudentScoreResponse getStudentScores(String studentId) {
        try {
            String url = baseUrl + "/api/v1/students/" + studentId + "/scores";
            
            ResponseEntity<StudentScoreResponse> response = restTemplate.getForEntity(
                url, StudentScoreResponse.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            } else {
                throw new CLIException("Failed to get student scores: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            logger.error("Error getting student scores", e);
            throw new CLIException("Failed to get student scores: " + e.getMessage());
        }
    }
    
    /**
     * Get student evaluation history via REST API.
     */
    public List<EvaluationResponse> getStudentEvaluationHistory(String studentId) {
        try {
            String url = baseUrl + "/api/v1/students/" + studentId + "/evaluations";
            
            ResponseEntity<EvaluationResponse[]> response = restTemplate.getForEntity(
                url, EvaluationResponse[].class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                return Arrays.asList(response.getBody());
            } else {
                throw new CLIException("Failed to get evaluation history: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            logger.error("Error getting evaluation history", e);
            throw new CLIException("Failed to get evaluation history: " + e.getMessage());
        }
    }
    
    /**
     * Get all assignments via REST API.
     */
    public List<AssignmentResponse> getAllAssignments() {
        try {
            String url = baseUrl + "/api/v1/evaluations/assignments";
            
            ResponseEntity<AssignmentResponse[]> response = restTemplate.getForEntity(
                url, AssignmentResponse[].class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                return Arrays.asList(response.getBody());
            } else {
                throw new CLIException("Failed to get assignments: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            logger.error("Error getting assignments", e);
            throw new CLIException("Failed to get assignments: " + e.getMessage());
        }
    }
    
    /**
     * Get evaluation result by ID via REST API.
     */
    public EvaluationResponse getEvaluationResult(String evaluationId) {
        try {
            String url = baseUrl + "/api/v1/evaluations/" + evaluationId;
            
            ResponseEntity<EvaluationResponse> response = restTemplate.getForEntity(
                url, EvaluationResponse.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            } else {
                throw new CLIException("Failed to get evaluation result: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            logger.error("Error getting evaluation result", e);
            throw new CLIException("Failed to get evaluation result: " + e.getMessage());
        }
    }
}