package com.studentevaluator.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studentevaluator.dto.AssignmentResponse;
import com.studentevaluator.dto.ErrorResponse;
import com.studentevaluator.dto.EvaluationResponse;
import com.studentevaluator.model.Assignment;
import com.studentevaluator.model.EvaluationStatus;
import com.studentevaluator.repository.AssignmentRepository;
import com.studentevaluator.repository.EvaluationRepository;
import com.studentevaluator.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for EvaluationController using TestRestTemplate.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@Transactional
class EvaluationControllerIntegrationTest {
    
    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("student_evaluator_test")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true);
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("evaluation.temp-directory", () -> System.getProperty("java.io.tmpdir") + "/test-evaluations");
    }
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private AssignmentRepository assignmentRepository;
    
    @Autowired
    private EvaluationRepository evaluationRepository;
    
    @Autowired
    private StudentRepository studentRepository;
    
    private MockMultipartFile validJavaFile;
    private MockMultipartFile validTestFile;
    private MockMultipartFile invalidFile;
    
    @BeforeEach
    void setUp() throws Exception {
        // Clean up repositories
        evaluationRepository.deleteAll();
        assignmentRepository.deleteAll();
        studentRepository.deleteAll();
        
        // Create test files
        String javaContent = """
            public class Calculator {
                public int add(int a, int b) {
                    return a + b;
                }
                
                public int subtract(int a, int b) {
                    return a - b;
                }
            }
            """;
        
        String testContent = """
            import org.junit.jupiter.api.Test;
            import static org.junit.jupiter.api.Assertions.*;
            
            public class CalculatorTest {
                @Test
                public void testAdd() {
                    Calculator calc = new Calculator();
                    assertEquals(5, calc.add(2, 3));
                }
                
                @Test
                public void testSubtract() {
                    Calculator calc = new Calculator();
                    assertEquals(1, calc.subtract(3, 2));
                }
            }
            """;
        
        validJavaFile = new MockMultipartFile(
            "file", "Calculator.java", "text/plain", javaContent.getBytes());
        
        validTestFile = new MockMultipartFile(
            "file", "CalculatorTest.java", "text/plain", testContent.getBytes());
        
        invalidFile = new MockMultipartFile(
            "file", "invalid.txt", "text/plain", "not java content".getBytes());
        
        // Ensure temp directory exists
        Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"), "test-evaluations");
        Files.createDirectories(tempDir);
    }
    
    @Test
    void uploadAssignment_ValidFile_ShouldReturnCreated() throws Exception {
        MvcResult result = mockMvc.perform(multipart("/api/v1/evaluations/assignments")
                .file(validJavaFile)
                .param("assignmentId", "calc-001")
                .param("title", "Calculator Assignment")
                .param("description", "Basic calculator implementation"))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        
        String responseBody = result.getResponse().getContentAsString();
        AssignmentResponse response = objectMapper.readValue(responseBody, AssignmentResponse.class);
        
        assertThat(response.getAssignmentId()).isEqualTo("calc-001");
        assertThat(response.getTitle()).isEqualTo("Calculator Assignment");
        assertThat(response.getDescription()).isEqualTo("Basic calculator implementation");
        assertThat(response.isHasTestFile()).isFalse();
        assertThat(response.getCreatedAt()).isNotNull();
        
        // Verify database record
        assertThat(assignmentRepository.existsById("calc-001")).isTrue();
    }
    
    @Test
    void uploadAssignment_InvalidFile_ShouldReturnBadRequest() throws Exception {
        MvcResult result = mockMvc.perform(multipart("/api/v1/evaluations/assignments")
                .file(invalidFile)
                .param("assignmentId", "calc-002")
                .param("title", "Calculator Assignment"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        
        String responseBody = result.getResponse().getContentAsString();
        ErrorResponse error = objectMapper.readValue(responseBody, ErrorResponse.class);
        
        assertThat(error.getCode()).isEqualTo("FILE_UPLOAD_ERROR");
        assertThat(error.getMessage()).contains("Java file");
    }
    
    @Test
    void uploadAssignment_MissingRequiredFields_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(multipart("/api/v1/evaluations/assignments")
                .file(validJavaFile)
                .param("assignmentId", "")
                .param("title", ""))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
    
    @Test
    void uploadTestFile_ValidFile_ShouldReturnOk() throws Exception {
        // First create an assignment
        Assignment assignment = new Assignment("calc-001", "Calculator Assignment", "Test assignment", null);
        assignmentRepository.save(assignment);
        
        MvcResult result = mockMvc.perform(multipart("/api/v1/evaluations/assignments/calc-001/tests")
                .file(validTestFile))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        
        String responseBody = result.getResponse().getContentAsString();
        AssignmentResponse response = objectMapper.readValue(responseBody, AssignmentResponse.class);
        
        assertThat(response.getAssignmentId()).isEqualTo("calc-001");
        assertThat(response.isHasTestFile()).isTrue();
        
        // Verify database record
        Assignment updatedAssignment = assignmentRepository.findById("calc-001").orElse(null);
        assertThat(updatedAssignment).isNotNull();
        assertThat(updatedAssignment.getTestFilePath()).isNotNull();
    }
    
    @Test
    void uploadTestFile_AssignmentNotFound_ShouldReturnNotFound() throws Exception {
        MvcResult result = mockMvc.perform(multipart("/api/v1/evaluations/assignments/nonexistent/tests")
                .file(validTestFile))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        
        String responseBody = result.getResponse().getContentAsString();
        ErrorResponse error = objectMapper.readValue(responseBody, ErrorResponse.class);
        
        assertThat(error.getCode()).isEqualTo("ASSIGNMENT_NOT_FOUND");
        assertThat(error.getMessage()).contains("nonexistent");
    }
    
    @Test
    void getAllAssignments_ShouldReturnList() throws Exception {
        // Create test assignments
        Assignment assignment1 = new Assignment("calc-001", "Calculator 1", "First calculator", null);
        Assignment assignment2 = new Assignment("calc-002", "Calculator 2", "Second calculator", null);
        assignmentRepository.save(assignment1);
        assignmentRepository.save(assignment2);
        
        MvcResult result = mockMvc.perform(get("/api/v1/evaluations/assignments"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        
        String responseBody = result.getResponse().getContentAsString();
        AssignmentResponse[] responses = objectMapper.readValue(responseBody, AssignmentResponse[].class);
        
        assertThat(responses).hasSize(2);
        assertThat(responses[0].getAssignmentId()).isIn("calc-001", "calc-002");
        assertThat(responses[1].getAssignmentId()).isIn("calc-001", "calc-002");
    }
    
    @Test
    void getAssignment_ExistingId_ShouldReturnAssignment() throws Exception {
        Assignment assignment = new Assignment("calc-001", "Calculator Assignment", "Test assignment", null);
        assignmentRepository.save(assignment);
        
        MvcResult result = mockMvc.perform(get("/api/v1/evaluations/assignments/calc-001"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        
        String responseBody = result.getResponse().getContentAsString();
        AssignmentResponse response = objectMapper.readValue(responseBody, AssignmentResponse.class);
        
        assertThat(response.getAssignmentId()).isEqualTo("calc-001");
        assertThat(response.getTitle()).isEqualTo("Calculator Assignment");
    }
    
    @Test
    void getAssignment_NonExistentId_ShouldReturnNotFound() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/evaluations/assignments/nonexistent"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        
        String responseBody = result.getResponse().getContentAsString();
        ErrorResponse error = objectMapper.readValue(responseBody, ErrorResponse.class);
        
        assertThat(error.getCode()).isEqualTo("ASSIGNMENT_NOT_FOUND");
    }
    
    @Test
    void triggerEvaluation_ValidRequest_ShouldReturnCreated() throws Exception {
        // Create assignment with test file
        Assignment assignment = new Assignment("calc-001", "Calculator Assignment", "Test assignment", "/tmp/test/CalculatorTest.java");
        assignmentRepository.save(assignment);
        
        // Create submission file
        String submissionContent = """
            public class Calculator {
                public int add(int a, int b) {
                    return a + b;
                }
            }
            """;
        
        MockMultipartFile submissionFile = new MockMultipartFile(
            "file", "Calculator.java", "text/plain", submissionContent.getBytes());
        
        MvcResult result = mockMvc.perform(multipart("/api/v1/evaluations")
                .file(submissionFile)
                .param("studentId", "student-001")
                .param("assignmentId", "calc-001"))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        
        String responseBody = result.getResponse().getContentAsString();
        EvaluationResponse response = objectMapper.readValue(responseBody, EvaluationResponse.class);
        
        assertThat(response.getEvaluationId()).isNotNull();
        assertThat(response.getStudentId()).isEqualTo("student-001");
        assertThat(response.getAssignmentId()).isEqualTo("calc-001");
        assertThat(response.getStatus()).isIn(EvaluationStatus.COMPLETED, EvaluationStatus.FAILED);
    }
    
    @Test
    void triggerEvaluation_AssignmentNotFound_ShouldReturnNotFound() throws Exception {
        MockMultipartFile submissionFile = new MockMultipartFile(
            "file", "Calculator.java", "text/plain", "public class Calculator {}".getBytes());
        
        MvcResult result = mockMvc.perform(multipart("/api/v1/evaluations")
                .file(submissionFile)
                .param("studentId", "student-001")
                .param("assignmentId", "nonexistent"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        
        String responseBody = result.getResponse().getContentAsString();
        ErrorResponse error = objectMapper.readValue(responseBody, ErrorResponse.class);
        
        assertThat(error.getCode()).isEqualTo("ASSIGNMENT_NOT_FOUND");
    }
    
    @Test
    void triggerAsyncEvaluation_ValidRequest_ShouldReturnAccepted() throws Exception {
        // Create assignment
        Assignment assignment = new Assignment("calc-001", "Calculator Assignment", "Test assignment", null);
        assignmentRepository.save(assignment);
        
        MockMultipartFile submissionFile = new MockMultipartFile(
            "file", "Calculator.java", "text/plain", "public class Calculator {}".getBytes());
        
        MvcResult result = mockMvc.perform(multipart("/api/v1/evaluations/async")
                .file(submissionFile)
                .param("studentId", "student-001")
                .param("assignmentId", "calc-001"))
                .andExpect(status().isAccepted())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        
        String responseBody = result.getResponse().getContentAsString();
        EvaluationResponse response = objectMapper.readValue(responseBody, EvaluationResponse.class);
        
        assertThat(response.getEvaluationId()).isNotNull();
        assertThat(response.getStudentId()).isEqualTo("student-001");
        assertThat(response.getAssignmentId()).isEqualTo("calc-001");
        assertThat(response.getStatus()).isEqualTo(EvaluationStatus.IN_PROGRESS);
    }
}