package com.studentevaluator.cli;

import com.studentevaluator.dto.AssignmentResponse;
import com.studentevaluator.dto.EvaluationResponse;
import com.studentevaluator.dto.StudentScoreResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CLIApiService.
 */
@ExtendWith(MockitoExtension.class)
class CLIApiServiceTest {
    
    @Mock
    private RestTemplate mockRestTemplate;
    
    private CLIApiService cliApiService;
    private Path tempDir;
    
    @BeforeEach
    void setUp() throws IOException {
        cliApiService = new CLIApiService();
        ReflectionTestUtils.setField(cliApiService, "restTemplate", mockRestTemplate);
        ReflectionTestUtils.setField(cliApiService, "baseUrl", "http://localhost:8080");
        
        tempDir = Files.createTempDirectory("cli-api-test");
    }
    
    @Test
    void testUploadAssignment_Success() throws IOException {
        // Create test file
        File testFile = createTestFile("TestAssignment.java", "public class TestAssignment {}");
        
        // Mock response
        AssignmentResponse mockResponse = new AssignmentResponse();
        mockResponse.setAssignmentId("test-assignment");
        mockResponse.setTitle("Test Assignment");
        mockResponse.setCreatedAt(LocalDateTime.now());
        
        ResponseEntity<AssignmentResponse> responseEntity = 
                new ResponseEntity<>(mockResponse, HttpStatus.CREATED);
        
        when(mockRestTemplate.postForEntity(anyString(), any(), eq(AssignmentResponse.class)))
                .thenReturn(responseEntity);
        
        // Execute
        AssignmentResponse result = cliApiService.uploadAssignment(
                testFile, "test-assignment", "Test Assignment", "Test description");
        
        // Verify
        assertNotNull(result);
        assertEquals("test-assignment", result.getAssignmentId());
        assertEquals("Test Assignment", result.getTitle());
        
        verify(mockRestTemplate).postForEntity(
                eq("http://localhost:8080/api/v1/evaluations/assignments"),
                any(),
                eq(AssignmentResponse.class)
        );
    }
    
    @Test
    void testUploadAssignment_Failure() throws IOException {
        // Create test file
        File testFile = createTestFile("TestAssignment.java", "public class TestAssignment {}");
        
        // Mock error response
        ResponseEntity<AssignmentResponse> responseEntity = 
                new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        
        when(mockRestTemplate.postForEntity(anyString(), any(), eq(AssignmentResponse.class)))
                .thenReturn(responseEntity);
        
        // Execute and verify exception
        CLIException exception = assertThrows(CLIException.class, () -> {
            cliApiService.uploadAssignment(testFile, "test-assignment", "Test Assignment", null);
        });
        
        assertTrue(exception.getMessage().contains("Failed to upload assignment"));
    }
    
    @Test
    void testUploadTestFile_Success() throws IOException {
        // Create test file
        File testFile = createTestFile("TestAssignmentTest.java", "public class TestAssignmentTest {}");
        
        // Mock response
        AssignmentResponse mockResponse = new AssignmentResponse();
        mockResponse.setAssignmentId("test-assignment");
        mockResponse.setHasTestFile(true);
        
        ResponseEntity<AssignmentResponse> responseEntity = 
                new ResponseEntity<>(mockResponse, HttpStatus.OK);
        
        when(mockRestTemplate.postForEntity(anyString(), any(), eq(AssignmentResponse.class)))
                .thenReturn(responseEntity);
        
        // Execute
        AssignmentResponse result = cliApiService.uploadTestFile(testFile, "test-assignment");
        
        // Verify
        assertNotNull(result);
        assertEquals("test-assignment", result.getAssignmentId());
        assertTrue(result.isHasTestFile());
        
        verify(mockRestTemplate).postForEntity(
                eq("http://localhost:8080/api/v1/evaluations/assignments/test-assignment/tests"),
                any(),
                eq(AssignmentResponse.class)
        );
    }
    
    @Test
    void testTriggerEvaluation_Success() throws IOException {
        // Create test file
        File submissionFile = createTestFile("StudentSubmission.java", "public class StudentSubmission {}");
        
        // Mock response
        EvaluationResponse mockResponse = new EvaluationResponse();
        mockResponse.setEvaluationId("eval-123");
        mockResponse.setStudentId("student-1");
        mockResponse.setAssignmentId("test-assignment");
        mockResponse.setStatus(com.studentevaluator.model.EvaluationStatus.COMPLETED);
        
        ResponseEntity<EvaluationResponse> responseEntity = 
                new ResponseEntity<>(mockResponse, HttpStatus.CREATED);
        
        when(mockRestTemplate.postForEntity(anyString(), any(), eq(EvaluationResponse.class)))
                .thenReturn(responseEntity);
        
        // Execute
        EvaluationResponse result = cliApiService.triggerEvaluation(
                submissionFile, "student-1", "test-assignment");
        
        // Verify
        assertNotNull(result);
        assertEquals("eval-123", result.getEvaluationId());
        assertEquals("student-1", result.getStudentId());
        assertEquals("test-assignment", result.getAssignmentId());
        
        verify(mockRestTemplate).postForEntity(
                eq("http://localhost:8080/api/v1/evaluations"),
                any(),
                eq(EvaluationResponse.class)
        );
    }
    
    @Test
    void testGetStudentScores_Success() {
        // Mock response
        StudentScoreResponse mockResponse = new StudentScoreResponse();
        mockResponse.setStudentId("student-1");
        mockResponse.setCurrentScore(85.0);
        
        ResponseEntity<StudentScoreResponse> responseEntity = 
                new ResponseEntity<>(mockResponse, HttpStatus.OK);
        
        when(mockRestTemplate.getForEntity(anyString(), eq(StudentScoreResponse.class)))
                .thenReturn(responseEntity);
        
        // Execute
        StudentScoreResponse result = cliApiService.getStudentScores("student-1");
        
        // Verify
        assertNotNull(result);
        assertEquals("student-1", result.getStudentId());
        assertEquals(85.0, result.getCurrentScore());
        
        verify(mockRestTemplate).getForEntity(
                eq("http://localhost:8080/api/v1/students/student-1/scores"),
                eq(StudentScoreResponse.class)
        );
    }
    
    @Test
    void testGetStudentEvaluationHistory_Success() {
        // Mock response
        EvaluationResponse eval1 = new EvaluationResponse();
        eval1.setEvaluationId("eval-1");
        eval1.setStudentId("student-1");
        
        EvaluationResponse eval2 = new EvaluationResponse();
        eval2.setEvaluationId("eval-2");
        eval2.setStudentId("student-1");
        
        EvaluationResponse[] mockArray = {eval1, eval2};
        
        ResponseEntity<EvaluationResponse[]> responseEntity = 
                new ResponseEntity<>(mockArray, HttpStatus.OK);
        
        when(mockRestTemplate.getForEntity(anyString(), eq(EvaluationResponse[].class)))
                .thenReturn(responseEntity);
        
        // Execute
        List<EvaluationResponse> result = cliApiService.getStudentEvaluationHistory("student-1");
        
        // Verify
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("eval-1", result.get(0).getEvaluationId());
        assertEquals("eval-2", result.get(1).getEvaluationId());
        
        verify(mockRestTemplate).getForEntity(
                eq("http://localhost:8080/api/v1/students/student-1/evaluations"),
                eq(EvaluationResponse[].class)
        );
    }
    
    @Test
    void testGetAllAssignments_Success() {
        // Mock response
        AssignmentResponse assignment1 = new AssignmentResponse();
        assignment1.setAssignmentId("assignment-1");
        assignment1.setTitle("Assignment 1");
        
        AssignmentResponse assignment2 = new AssignmentResponse();
        assignment2.setAssignmentId("assignment-2");
        assignment2.setTitle("Assignment 2");
        
        AssignmentResponse[] mockArray = {assignment1, assignment2};
        
        ResponseEntity<AssignmentResponse[]> responseEntity = 
                new ResponseEntity<>(mockArray, HttpStatus.OK);
        
        when(mockRestTemplate.getForEntity(anyString(), eq(AssignmentResponse[].class)))
                .thenReturn(responseEntity);
        
        // Execute
        List<AssignmentResponse> result = cliApiService.getAllAssignments();
        
        // Verify
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("assignment-1", result.get(0).getAssignmentId());
        assertEquals("assignment-2", result.get(1).getAssignmentId());
        
        verify(mockRestTemplate).getForEntity(
                eq("http://localhost:8080/api/v1/evaluations/assignments"),
                eq(AssignmentResponse[].class)
        );
    }
    
    @Test
    void testGetEvaluationResult_Success() {
        // Mock response
        EvaluationResponse mockResponse = new EvaluationResponse();
        mockResponse.setEvaluationId("eval-123");
        mockResponse.setStudentId("student-1");
        mockResponse.setAssignmentId("test-assignment");
        
        ResponseEntity<EvaluationResponse> responseEntity = 
                new ResponseEntity<>(mockResponse, HttpStatus.OK);
        
        when(mockRestTemplate.getForEntity(anyString(), eq(EvaluationResponse.class)))
                .thenReturn(responseEntity);
        
        // Execute
        EvaluationResponse result = cliApiService.getEvaluationResult("eval-123");
        
        // Verify
        assertNotNull(result);
        assertEquals("eval-123", result.getEvaluationId());
        assertEquals("student-1", result.getStudentId());
        assertEquals("test-assignment", result.getAssignmentId());
        
        verify(mockRestTemplate).getForEntity(
                eq("http://localhost:8080/api/v1/evaluations/eval-123"),
                eq(EvaluationResponse.class)
        );
    }
    
    @Test
    void testGetStudentScores_Failure() {
        // Mock error response
        ResponseEntity<StudentScoreResponse> responseEntity = 
                new ResponseEntity<>(HttpStatus.NOT_FOUND);
        
        when(mockRestTemplate.getForEntity(anyString(), eq(StudentScoreResponse.class)))
                .thenReturn(responseEntity);
        
        // Execute and verify exception
        CLIException exception = assertThrows(CLIException.class, () -> {
            cliApiService.getStudentScores("nonexistent-student");
        });
        
        assertTrue(exception.getMessage().contains("Failed to get student scores"));
    }
    
    // Helper method
    private File createTestFile(String fileName, String content) throws IOException {
        Path filePath = tempDir.resolve(fileName);
        Files.write(filePath, content.getBytes());
        return filePath.toFile();
    }
}