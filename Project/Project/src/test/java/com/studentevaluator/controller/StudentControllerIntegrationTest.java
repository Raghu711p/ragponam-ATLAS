package com.studentevaluator.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studentevaluator.dto.ErrorResponse;
import com.studentevaluator.dto.EvaluationResponse;
import com.studentevaluator.dto.StudentScoreResponse;
import com.studentevaluator.model.Evaluation;
import com.studentevaluator.model.EvaluationStatus;
import com.studentevaluator.model.Student;
import com.studentevaluator.repository.EvaluationRepository;
import com.studentevaluator.repository.StudentRepository;
import com.studentevaluator.service.ScoreCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for StudentController using MockMvc.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@Transactional
class StudentControllerIntegrationTest {
    
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
    }
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private StudentRepository studentRepository;
    
    @Autowired
    private EvaluationRepository evaluationRepository;
    
    @Autowired
    private ScoreCache scoreCache;
    
    @BeforeEach
    void setUp() {
        // Clean up repositories and cache
        evaluationRepository.deleteAll();
        studentRepository.deleteAll();
        scoreCache.clearAll();
    }
    
    @Test
    void getStudentScore_ExistingScore_ShouldReturnScore() throws Exception {
        // Setup cached score
        scoreCache.updateScore("student-001", 85.5);
        
        MvcResult result = mockMvc.perform(get("/api/v1/students/student-001/score"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        
        String responseBody = result.getResponse().getContentAsString();
        StudentScoreResponse response = objectMapper.readValue(responseBody, StudentScoreResponse.class);
        
        assertThat(response.getStudentId()).isEqualTo("student-001");
        assertThat(response.getCurrentScore()).isEqualTo(85.5);
    }
    
    @Test
    void getStudentScore_NonExistentScore_ShouldReturnNotFound() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/students/nonexistent/score"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        
        String responseBody = result.getResponse().getContentAsString();
        ErrorResponse error = objectMapper.readValue(responseBody, ErrorResponse.class);
        
        assertThat(error.getCode()).isEqualTo("SCORE_NOT_FOUND");
        assertThat(error.getMessage()).contains("nonexistent");
    }
    
    @Test
    void getStudentScores_WithStatsAndCache_ShouldReturnComprehensiveData() throws Exception {
        // Create student and evaluations
        Student student = new Student("student-001", "John Doe", "john@example.com");
        studentRepository.save(student);
        
        Evaluation eval1 = new Evaluation();
        eval1.setEvaluationId("eval-001");
        eval1.setStudentId("student-001");
        eval1.setAssignmentId("assign-001");
        eval1.setScore(BigDecimal.valueOf(80));
        eval1.setMaxScore(BigDecimal.valueOf(100));
        eval1.setStatus(EvaluationStatus.COMPLETED);
        eval1.setEvaluatedAt(LocalDateTime.now());
        
        Evaluation eval2 = new Evaluation();
        eval2.setEvaluationId("eval-002");
        eval2.setStudentId("student-001");
        eval2.setAssignmentId("assign-002");
        eval2.setScore(BigDecimal.valueOf(90));
        eval2.setMaxScore(BigDecimal.valueOf(100));
        eval2.setStatus(EvaluationStatus.COMPLETED);
        eval2.setEvaluatedAt(LocalDateTime.now());
        
        evaluationRepository.save(eval1);
        evaluationRepository.save(eval2);
        
        // Setup cached score
        scoreCache.updateScore("student-001", 90.0);
        
        MvcResult result = mockMvc.perform(get("/api/v1/students/student-001/scores"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        
        String responseBody = result.getResponse().getContentAsString();
        StudentScoreResponse response = objectMapper.readValue(responseBody, StudentScoreResponse.class);
        
        assertThat(response.getStudentId()).isEqualTo("student-001");
        assertThat(response.getCurrentScore()).isEqualTo(90.0);
        assertThat(response.getTotalEvaluations()).isEqualTo(2);
        assertThat(response.getAverageScore()).isNotNull();
    }
    
    @Test
    void getStudentEvaluationHistory_ShouldReturnEvaluations() throws Exception {
        // Create student and evaluations
        Student student = new Student("student-001", "John Doe", "john@example.com");
        studentRepository.save(student);
        
        Evaluation eval1 = new Evaluation();
        eval1.setEvaluationId("eval-001");
        eval1.setStudentId("student-001");
        eval1.setAssignmentId("assign-001");
        eval1.setScore(BigDecimal.valueOf(80));
        eval1.setMaxScore(BigDecimal.valueOf(100));
        eval1.setStatus(EvaluationStatus.COMPLETED);
        eval1.setEvaluatedAt(LocalDateTime.now());
        
        Evaluation eval2 = new Evaluation();
        eval2.setEvaluationId("eval-002");
        eval2.setStudentId("student-001");
        eval2.setAssignmentId("assign-002");
        eval2.setScore(BigDecimal.valueOf(90));
        eval2.setMaxScore(BigDecimal.valueOf(100));
        eval2.setStatus(EvaluationStatus.COMPLETED);
        eval2.setEvaluatedAt(LocalDateTime.now());
        
        evaluationRepository.save(eval1);
        evaluationRepository.save(eval2);
        
        MvcResult result = mockMvc.perform(get("/api/v1/students/student-001/evaluations"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        
        String responseBody = result.getResponse().getContentAsString();
        EvaluationResponse[] responses = objectMapper.readValue(responseBody, EvaluationResponse[].class);
        
        assertThat(responses).hasSize(2);
        assertThat(responses[0].getStudentId()).isEqualTo("student-001");
        assertThat(responses[1].getStudentId()).isEqualTo("student-001");
    }
    
    @Test
    void getStudentEvaluationStats_WithData_ShouldReturnStats() throws Exception {
        // Create student and evaluations
        Student student = new Student("student-001", "John Doe", "john@example.com");
        studentRepository.save(student);
        
        Evaluation eval1 = new Evaluation();
        eval1.setEvaluationId("eval-001");
        eval1.setStudentId("student-001");
        eval1.setAssignmentId("assign-001");
        eval1.setScore(BigDecimal.valueOf(80));
        eval1.setMaxScore(BigDecimal.valueOf(100));
        eval1.setStatus(EvaluationStatus.COMPLETED);
        eval1.setEvaluatedAt(LocalDateTime.now());
        
        evaluationRepository.save(eval1);
        
        // Setup cached score
        scoreCache.updateScore("student-001", 80.0);
        
        MvcResult result = mockMvc.perform(get("/api/v1/students/student-001/stats"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        
        String responseBody = result.getResponse().getContentAsString();
        StudentScoreResponse response = objectMapper.readValue(responseBody, StudentScoreResponse.class);
        
        assertThat(response.getStudentId()).isEqualTo("student-001");
        assertThat(response.getCurrentScore()).isEqualTo(80.0);
        assertThat(response.getTotalEvaluations()).isEqualTo(1);
    }
    
    @Test
    void getStudentEvaluationStats_NoData_ShouldReturnNotFound() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/students/nonexistent/stats"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        
        String responseBody = result.getResponse().getContentAsString();
        ErrorResponse error = objectMapper.readValue(responseBody, ErrorResponse.class);
        
        assertThat(error.getCode()).isEqualTo("STATS_NOT_FOUND");
    }
    
    @Test
    void updateStudentScore_ValidScore_ShouldReturnUpdatedScore() throws Exception {
        MvcResult result = mockMvc.perform(put("/api/v1/students/student-001/score")
                .param("score", "95.5"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        
        String responseBody = result.getResponse().getContentAsString();
        StudentScoreResponse response = objectMapper.readValue(responseBody, StudentScoreResponse.class);
        
        assertThat(response.getStudentId()).isEqualTo("student-001");
        assertThat(response.getCurrentScore()).isEqualTo(95.5);
        
        // Verify cache was updated
        assertThat(scoreCache.retrieveScore("student-001")).isPresent();
    }
    
    @Test
    void updateStudentScore_InvalidScore_ShouldReturnBadRequest() throws Exception {
        MvcResult result = mockMvc.perform(put("/api/v1/students/student-001/score")
                .param("score", "-10"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        
        String responseBody = result.getResponse().getContentAsString();
        ErrorResponse error = objectMapper.readValue(responseBody, ErrorResponse.class);
        
        assertThat(error.getCode()).isEqualTo("INVALID_SCORE");
    }
    
    @Test
    void clearStudentScore_ExistingScore_ShouldReturnNoContent() throws Exception {
        // Setup cached score
        scoreCache.updateScore("student-001", 85.5);
        
        mockMvc.perform(delete("/api/v1/students/student-001/score"))
                .andExpect(status().isNoContent());
        
        // Verify score was removed
        assertThat(scoreCache.retrieveScore("student-001")).isEmpty();
    }
    
    @Test
    void clearStudentScore_NonExistentScore_ShouldReturnNotFound() throws Exception {
        MvcResult result = mockMvc.perform(delete("/api/v1/students/nonexistent/score"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        
        String responseBody = result.getResponse().getContentAsString();
        ErrorResponse error = objectMapper.readValue(responseBody, ErrorResponse.class);
        
        assertThat(error.getCode()).isEqualTo("SCORE_NOT_FOUND");
    }
    
    @Test
    void getAllStudentScores_ShouldReturnAllCachedScores() throws Exception {
        // Setup multiple cached scores
        scoreCache.updateScore("student-001", 85.5);
        scoreCache.updateScore("student-002", 92.0);
        scoreCache.updateScore("student-003", 78.5);
        
        MvcResult result = mockMvc.perform(get("/api/v1/students/scores"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        
        String responseBody = result.getResponse().getContentAsString();
        StudentScoreResponse[] responses = objectMapper.readValue(responseBody, StudentScoreResponse[].class);
        
        assertThat(responses).hasSize(3);
        // Note: ScoreCache returns hash-based keys, so we check for the presence of hash_ prefix
        assertThat(responses).allMatch(response -> response.getStudentId().startsWith("hash_"));
    }
}